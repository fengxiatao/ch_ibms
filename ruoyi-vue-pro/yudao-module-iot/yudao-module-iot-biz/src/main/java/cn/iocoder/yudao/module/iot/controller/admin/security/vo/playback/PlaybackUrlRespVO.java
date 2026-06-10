package cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaybackUrlRespVO {

    @Schema(description = "通道ID")
    private Long cameraId;

    @Schema(description = "通道名称")
    private String cameraName;

    @Schema(description = "rtsp 地址（可选）")
    private String rtspUrl;

    @Schema(description = "WebSocket-FLV 地址")
    private String wsFlvUrl;

    @Schema(description = "HTTP-FLV 地址")
    private String flvUrl;

    @Schema(description = "webrtc API 地址")
    private String webrtcUrl;

    @Schema(description = "流ID（用于关闭流），这里等同 stream")
    private String streamId;

    @Schema(description = "app")
    private String app;

    @Schema(description = "stream")
    private String stream;
}

