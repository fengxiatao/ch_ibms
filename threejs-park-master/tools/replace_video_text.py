#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Burned-in subtitle / logo text replacement using OpenCV inpaint + Pillow.

Usage:
  1) Calibrate (save first frame to measure x,y,w,h in Paint etc.):
     python replace_video_text.py -i path/to/bi.mp4 --dump-frame 0 --dump-path frame0.png

  2) Copy replace_regions.example.json to replace_regions.json and set regions (寓/信 各一块矩形).

  3) Quick check one frame (no full encode):
     python replace_video_text.py -i path/to/bi.mp4 --preview 0

  4) Full run (needs ffmpeg in PATH for H.264 output):
     python replace_video_text.py -i path/to/bi.mp4 -c replace_regions.json -o bi_fixed.mp4

Requires: opencv-python-headless, Pillow, numpy, and ffmpeg executable.
"""

from __future__ import annotations

import argparse
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path
from typing import Any

import cv2
import numpy as np
from PIL import Image, ImageDraw, ImageFont


def default_cjk_font() -> str | None:
    windir = os.environ.get("WINDIR", r"C:\Windows")
    fonts = Path(windir) / "Fonts"
    for name in ("msyh.ttc", "msyhbd.ttc", "simhei.ttf", "simsun.ttc"):
        p = fonts / name
        if p.is_file():
            return str(p)
    # Linux common paths
    for p in (
        "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.otf",
        "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
        "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
    ):
        if os.path.isfile(p):
            return p
    return None


def load_config(path: Path) -> dict[str, Any]:
    with path.open(encoding="utf-8") as f:
        data = json.load(f)
    if "regions" not in data or not isinstance(data["regions"], list):
        raise ValueError("config must contain a non-empty 'regions' list")
    for i, r in enumerate(data["regions"]):
        for k in ("x", "y", "w", "h", "replace_with"):
            if k not in r:
                raise ValueError(f"regions[{i}] missing key '{k}'")
    return data


def fit_font_size(
    text: str,
    font_path: str,
    box_w: int,
    box_h: int,
    max_size: int = 200,
) -> ImageFont.FreeTypeFont:
    low, high = 8, max_size
    best = None
    while low <= high:
        mid = (low + high) // 2
        try:
            font = _truetype(font_path, mid)
        except OSError:
            font = ImageFont.load_default()
            return font
        bbox = font.getbbox(text)
        tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
        if tw <= box_w - 4 and th <= box_h - 4:
            best = font
            low = mid + 1
        else:
            high = mid - 1
    if best is None:
        return _truetype(font_path, 12)
    return best


def _truetype(path: str, size: int) -> ImageFont.FreeTypeFont:
    """OpenType/CID fonts; .ttc on Windows needs index=0."""
    p = path.lower()
    if p.endswith(".ttc") or p.endswith(".otc"):
        return ImageFont.truetype(path, size, index=0)
    return ImageFont.truetype(path, size)


def render_char_rgba(
    text: str,
    font: ImageFont.FreeTypeFont,
    color: tuple[int, int, int],
    outline_color: tuple[int, int, int] | None,
    outline_width: int,
) -> tuple[Image.Image, int, int]:
    """Return RGBA image and (offset_x, offset_y) to align by top-left of glyph bbox."""
    dummy = Image.new("RGBA", (4, 4))
    draw = ImageDraw.Draw(dummy)
    bbox = draw.textbbox((0, 0), text, font=font)
    pad = outline_width + 4
    w = bbox[2] - bbox[0] + pad * 2
    h = bbox[3] - bbox[1] + pad * 2
    img = Image.new("RGBA", (max(w, 8), max(h, 8)), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    ox = pad - bbox[0]
    oy = pad - bbox[1]
    pos = (ox, oy)
    if outline_width > 0 and outline_color:
        for dx in range(-outline_width, outline_width + 1):
            for dy in range(-outline_width, outline_width + 1):
                if dx == 0 and dy == 0:
                    continue
                draw.text(
                    (pos[0] + dx, pos[1] + dy),
                    text,
                    font=font,
                    fill=outline_color + (255,),
                )
    draw.text(pos, text, font=font, fill=color + (255,))
    return img, ox, oy


def apply_region(
    frame_bgr: np.ndarray,
    region: dict[str, Any],
    font: ImageFont.FreeTypeFont,
    text_color: tuple[int, int, int],
    outline_color: tuple[int, int, int] | None,
    outline_width: int,
    inpaint_radius: int,
) -> None:
    x, y, w, h = int(region["x"]), int(region["y"]), int(region["w"]), int(region["h"])
    text = str(region["replace_with"])
    fh, fw = frame_bgr.shape[:2]
    x = max(0, min(x, fw - 1))
    y = max(0, min(y, fh - 1))
    w = max(1, min(w, fw - x))
    h = max(1, min(h, fh - y))

    mask = np.zeros((fh, fw), dtype=np.uint8)
    mask[y : y + h, x : x + w] = 255
    kernel = np.ones((3, 3), np.uint8)
    mask = cv2.dilate(mask, kernel, iterations=1)
    repaired = cv2.inpaint(frame_bgr, mask, inpaint_radius, cv2.INPAINT_TELEA)
    frame_bgr[:, :] = repaired

    rgba, _ox, _oy = render_char_rgba(
        text, font, text_color, outline_color, outline_width
    )
    tw, th = rgba.size
    cx = x + w // 2
    cy = y + h // 2
    px = cx - tw // 2
    py = cy - th // 2
    px = max(0, min(px, fw - tw))
    py = max(0, min(py, fh - th))

    patch = np.array(rgba)
    b, g, r, a = cv2.split(patch)
    roi = frame_bgr[py : py + th, px : px + tw]
    if roi.shape[0] != th or roi.shape[1] != tw:
        return
    a_f = (a.astype(np.float32) / 255.0)[..., None]
    roi_f = roi.astype(np.float32)
    rgb = cv2.merge([b, g, r]).astype(np.float32)
    blended = (a_f * rgb + (1 - a_f) * roi_f).astype(np.uint8)
    frame_bgr[py : py + th, px : px + tw] = blended


def has_audio_stream(video_path: Path) -> bool:
    ffprobe = shutil.which("ffprobe")
    if not ffprobe:
        return False
    try:
        r = subprocess.run(
            [
                ffprobe,
                "-v",
                "error",
                "-select_streams",
                "a",
                "-show_entries",
                "stream=index",
                "-of",
                "csv=p=0",
                str(video_path),
            ],
            capture_output=True,
            text=True,
            check=False,
        )
        return bool(r.stdout.strip())
    except OSError:
        return False


def merge_audio(video_path: Path, audio_source: Path, output_path: Path) -> None:
    ffmpeg = shutil.which("ffmpeg")
    if not ffmpeg:
        raise RuntimeError("ffmpeg not found in PATH")
    cmd = [
        ffmpeg,
        "-y",
        "-i",
        str(video_path),
        "-i",
        str(audio_source),
        "-map",
        "0:v:0",
        "-map",
        "1:a:0",
        "-c:v",
        "copy",
        "-c:a",
        "aac",
        "-shortest",
        str(output_path),
    ]
    subprocess.run(cmd, check=True)


def write_video_ffmpeg_pipe(
    cap: cv2.VideoCapture,
    out_path: Path,
    fps: float,
    width: int,
    height: int,
    process_frame,
) -> None:
    ffmpeg = shutil.which("ffmpeg")
    if not ffmpeg:
        raise RuntimeError("ffmpeg not found in PATH; install ffmpeg or add to PATH")

    cmd = [
        ffmpeg,
        "-y",
        "-f",
        "rawvideo",
        "-vcodec",
        "rawvideo",
        "-s",
        f"{width}x{height}",
        "-pix_fmt",
        "bgr24",
        "-r",
        str(fps),
        "-i",
        "-",
        "-an",
        "-c:v",
        "libx264",
        "-pix_fmt",
        "yuv420p",
        "-crf",
        "18",
        str(out_path),
    ]
    proc = subprocess.Popen(
        cmd,
        stdin=subprocess.PIPE,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.PIPE,
    )
    assert proc.stdin is not None
    n = 0
    while True:
        ok, frame = cap.read()
        if not ok:
            break
        if frame.shape[1] != width or frame.shape[0] != height:
            frame = cv2.resize(frame, (width, height))
        process_frame(frame)
        proc.stdin.write(frame.astype(np.uint8).tobytes())
        n += 1
    proc.stdin.close()
    err = proc.stderr.read().decode("utf-8", errors="replace") if proc.stderr else ""
    code = proc.wait()
    if code != 0:
        raise RuntimeError(f"ffmpeg failed (exit {code}): {err[-2000:]}")


def main() -> int:
    ap = argparse.ArgumentParser(description="Replace burned-in chars in video (inpaint + overlay).")
    ap.add_argument("-i", "--input", required=True, type=Path, help="Input MP4")
    ap.add_argument("-o", "--output", type=Path, default=None, help="Output MP4 (not used with --dump-frame)")
    ap.add_argument(
        "-c",
        "--config",
        type=Path,
        default=None,
        help="JSON config (default: replace_regions.json, else replace_regions.example.json)",
    )
    ap.add_argument("--dump-frame", type=int, metavar="N", help="Save frame index N as PNG and exit")
    ap.add_argument("--dump-path", type=Path, default=None, help="Path for --dump-frame (default: frame_N.png)")
    ap.add_argument(
        "--preview",
        type=int,
        metavar="N",
        help="Apply config to frame N only, save PNG (fast calibration; no ffmpeg encode)",
    )
    ap.add_argument(
        "--preview-out",
        type=Path,
        default=None,
        help="Output path for --preview (default: preview_frame_N.png)",
    )
    args = ap.parse_args()

    inp = args.input
    if not inp.is_file():
        print(f"Input not found: {inp}", file=sys.stderr)
        return 1

    cap = cv2.VideoCapture(str(inp))
    if not cap.isOpened():
        print("Could not open video", file=sys.stderr)
        return 1

    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    fps = cap.get(cv2.CAP_PROP_FPS) or 25.0

    if args.dump_frame is not None:
        idx = args.dump_frame
        cap.set(cv2.CAP_PROP_POS_FRAMES, idx)
        ok, frame = cap.read()
        cap.release()
        if not ok:
            print(f"Could not read frame {idx}", file=sys.stderr)
            return 1
        dump = args.dump_path or Path(f"frame_{idx}.png")
        cv2.imwrite(str(dump), frame)
        print(f"Wrote {dump} ({width}x{height})")
        return 0

    base = Path(__file__).resolve().parent
    cfg_path = args.config
    if cfg_path is None:
        cfg_path = base / "replace_regions.json"
        if not cfg_path.is_file():
            cfg_path = base / "replace_regions.example.json"
            if cfg_path.is_file():
                print(
                    "Using replace_regions.example.json — copy to replace_regions.json and edit x,y,w,h.",
                    file=sys.stderr,
                )
    if not cfg_path.is_file():
        print(f"Config not found: {cfg_path}", file=sys.stderr)
        return 1

    cfg = load_config(cfg_path)

    if args.preview is not None:
        regions = cfg["regions"]
        font_path = cfg.get("font_path") or default_cjk_font()
        if not font_path:
            print("No font found. Set font_path in JSON.", file=sys.stderr)
            cap.release()
            return 1
        text_color = tuple(cfg.get("text_color", [255, 255, 255]))
        oc = cfg.get("outline_color")
        outline_color = tuple(oc) if oc is not None else (0, 0, 0)
        outline_width = int(cfg.get("outline_width", 1))
        inpaint_radius = int(cfg.get("inpaint_radius", 5))
        idx = args.preview
        cap.set(cv2.CAP_PROP_POS_FRAMES, idx)
        ok, frame = cap.read()
        cap.release()
        if not ok:
            print(f"Could not read frame {idx}", file=sys.stderr)
            return 1
        for r in regions:
            fs = cfg.get("font_size")
            if fs:
                try:
                    font = _truetype(font_path, int(fs))
                except OSError:
                    font = ImageFont.load_default()
            else:
                font = fit_font_size(
                    str(r["replace_with"]),
                    font_path,
                    int(r["w"]),
                    int(r["h"]),
                )
            apply_region(
                frame,
                r,
                font,
                text_color,
                outline_color,
                outline_width,
                inpaint_radius,
            )
        out_p = args.preview_out or Path(f"preview_frame_{idx}.png")
        cv2.imwrite(str(out_p), frame)
        print(f"Wrote {out_p} (tune regions in JSON, then run without --preview)")
        return 0

    regions = cfg["regions"]
    font_path = cfg.get("font_path") or default_cjk_font()
    if not font_path:
        print("No font found. Set font_path in JSON to a .ttf/.ttc file.", file=sys.stderr)
        cap.release()
        return 1

    text_color = tuple(cfg.get("text_color", [255, 255, 255]))
    oc = cfg.get("outline_color")
    outline_color = tuple(oc) if oc is not None else (0, 0, 0)
    outline_width = int(cfg.get("outline_width", 1))
    inpaint_radius = int(cfg.get("inpaint_radius", 5))

    out_final = args.output or inp.with_name(inp.stem + "_fixed.mp4")
    tmp_video = out_final.with_suffix(".video_only.tmp.mp4")

    def process_frame(frame: np.ndarray) -> None:
        for r in regions:
            fs = cfg.get("font_size")
            if fs:
                try:
                    font = _truetype(font_path, int(fs))
                except OSError:
                    font = ImageFont.load_default()
            else:
                font = fit_font_size(
                    str(r["replace_with"]),
                    font_path,
                    int(r["w"]),
                    int(r["h"]),
                )
            apply_region(
                frame,
                r,
                font,
                text_color,
                outline_color,
                outline_width,
                inpaint_radius,
            )

    print(f"Processing {inp} -> {out_final} ({width}x{height} @ {fps:.3f} fps)")
    try:
        write_video_ffmpeg_pipe(cap, tmp_video, fps, width, height, process_frame)
    finally:
        cap.release()

    if has_audio_stream(inp):
        try:
            merge_audio(tmp_video, inp, out_final)
            tmp_video.unlink(missing_ok=True)
            print(f"Done (video + audio): {out_final}")
        except Exception as e:
            print(f"Audio merge failed ({e}); leaving video-only: {tmp_video}", file=sys.stderr)
            if out_final.exists():
                out_final.unlink()
            tmp_video.rename(out_final)
            print(f"Done (video only): {out_final}")
    else:
        if out_final.exists() and out_final != tmp_video:
            out_final.unlink()
        tmp_video.rename(out_final)
        print(f"Done (no audio in source): {out_final}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
