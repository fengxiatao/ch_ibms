package cn.iocoder.yudao.module.iot.controller.admin.camera.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 摄像头配置 Response VO")
@Data
public class IotCameraRespVO {

    @Schema(description = "摄像头ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long deviceId;

    @Schema(description = "主码流地址", example = "rtsp://192.168.1.202:554/cam/realmonitor?channel=1&subtype=0")
    private String streamUrlMain;

    @Schema(description = "子码流地址", example = "rtsp://192.168.1.202:554/cam/realmonitor?channel=1&subtype=1")
    private String streamUrlSub;

    @Schema(description = "RTSP端口", example = "554")
    private Integer rtspPort;

    @Schema(description = "ONVIF端口", example = "80")
    private Integer onvifPort;

    @Schema(description = "登录用户名", example = "admin")
    private String username;

    @Schema(description = "厂商", example = "大华")
    private String manufacturer;

    @Schema(description = "型号", example = "DH-IPC-HFW1230S")
    private String model;

    @Schema(description = "是否支持PTZ", example = "false")
    private Boolean ptzSupport;

    @Schema(description = "是否支持音频", example = "false")
    private Boolean audioSupport;

    @Schema(description = "分辨率", example = "1920*1080")
    private String resolution;

    @Schema(description = "帧率", example = "25")
    private Integer frameRate;

    @Schema(description = "码率(Kbps)", example = "2048")
    private Integer bitRate;

    @Schema(description = "预置位数量", example = "8")
    private Integer presetCount;

    @Schema(description = "亮度(0-100)", example = "50")
    private Integer brightness;

    @Schema(description = "对比度(0-100)", example = "50")
    private Integer contrast;

    @Schema(description = "饱和度(0-100)", example = "50")
    private Integer saturation;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}

