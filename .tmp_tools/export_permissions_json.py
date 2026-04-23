import json


def load_extracted(path: str) -> list[str]:
    perms: list[str] = []
    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        for line in f:
            line = line.strip().lstrip("\ufeff")
            if not line or line.startswith("java_files_scanned") or line.startswith("permissions_count"):
                continue
            perms.append(line)
    # stable de-dup
    seen = set()
    out = []
    for p in perms:
        if p in seen:
            continue
        seen.add(p)
        out.append(p)
    return out


def main() -> None:
    perms = load_extracted("E:/ch/.tmp_tools/permissions.txt")
    s = json.dumps(perms, ensure_ascii=False)
    import sys
    sys.stdout.buffer.write(s.encode("utf-8"))


if __name__ == "__main__":
    main()

