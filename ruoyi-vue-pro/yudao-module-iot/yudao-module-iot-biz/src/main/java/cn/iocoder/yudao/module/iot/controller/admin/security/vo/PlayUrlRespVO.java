package cn.iocoder.yudao.module.iot.controller.admin.security.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 播放地址响应 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 播放地址响应 VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayUrlRespVO {

    @Schema(description = "WebSocket-FLV 播放地址（超低延时 < 500ms，Jessibuca 完美支持）⭐⭐⭐⭐⭐ 最推荐", example = "ws://192.168.1.246/live/camera_5.live.flv")
    private String wsFlvUrl;

    @Schema(description = "WebRTC 播放地址（超低延时 < 500ms，浏览器原生）⭐⭐⭐⭐⭐", example = "webrtc://192.168.1.246:8000/live/camera_5")
    private String webrtcUrl;

    @Schema(description = "WebSocket-FMP4 播放地址（低延时 ~1-2秒，无并发限制）", example = "ws://192.168.1.100:8080/live/camera_5.live.mp4")
    private String wsFmp4Url;

    @Schema(description = "HTTP-FMP4 播放地址（低延时 ~1-2秒，最多6并发）", example = "http://192.168.1.100:8080/live/camera_5.live.mp4")
    private String fmp4Url;

    @Schema(description = "FLV 播放地址（低延时 ~1秒，需flv.js）", example = "http://192.168.1.100:8080/live/camera_5.flv")
    private String flvUrl;

    @Schema(description = "HLS 播放地址（延时 5-10秒）", example = "http://192.168.1.100:8080/live/camera_5/hls.m3u8")
    private String hlsUrl;

    @Schema(description = "RTMP 播放地址（需专用播放器）", example = "rtmp://192.168.1.100:1935/live/camera_5")
    private String rtmpUrl;

    @Schema(description = "流标识", example = "camera_5")
    private String streamKey;
}



