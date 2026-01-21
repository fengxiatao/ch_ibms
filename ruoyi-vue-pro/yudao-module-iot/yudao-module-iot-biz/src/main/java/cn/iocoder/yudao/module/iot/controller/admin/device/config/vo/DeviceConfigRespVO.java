package cn.iocoder.yudao.module.iot.controller.admin.device.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - IoT 设备配置 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 设备配置 Response VO")
@Data
public class DeviceConfigRespVO {

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "设备名称", example = "摄像头-1906")
    private String deviceName;

    // ========== 网络配置 ==========

    @Schema(description = "网络配置")
    private NetworkConfig networkConfig;

    // ========== 事件配置 ==========

    @Schema(description = "事件配置")
    private EventConfig eventConfig;

    // ========== 视频配置 ==========

    @Schema(description = "视频配置")
    private VideoConfig videoConfig;

    /**
     * 网络配置
     */
    @Data
    public static class NetworkConfig {
        @Schema(description = "IP地址", example = "192.168.1.202")
        private String ipAddress;

        @Schema(description = "子网掩码", example = "255.255.255.0")
        private String subnetMask;

        @Schema(description = "网关", example = "192.168.1.1")
        private String gateway;

        @Schema(description = "DNS服务器", example = "8.8.8.8")
        private String dns;

        @Schema(description = "DHCP启用", example = "false")
        private Boolean dhcpEnabled;

        @Schema(description = "HTTP端口", example = "80")
        private Integer httpPort;

        @Schema(description = "RTSP端口", example = "554")
        private Integer rtspPort;

        @Schema(description = "ONVIF端口", example = "8999")
        private Integer onvifPort;
    }

    /**
     * 事件配置
     */
    @Data
    public static class EventConfig {
        @Schema(description = "运动检测启用", example = "true")
        private Boolean motionDetectionEnabled;

        @Schema(description = "运动检测灵敏度 (1-100)", example = "50")
        private Integer motionSensitivity;

        @Schema(description = "视频丢失检测启用", example = "true")
        private Boolean videoLossEnabled;

        @Schema(description = "视频遮挡检测启用", example = "true")
        private Boolean tamperDetectionEnabled;

        @Schema(description = "音频检测启用", example = "false")
        private Boolean audioDetectionEnabled;

        @Schema(description = "音频检测阈值 (1-100)", example = "30")
        private Integer audioThreshold;
    }

    /**
     * 视频配置
     */
    @Data
    public static class VideoConfig {
        @Schema(description = "分辨率", example = "1920x1080")
        private String resolution;

        @Schema(description = "帧率", example = "25")
        private Integer frameRate;

        @Schema(description = "码率 (kbps)", example = "4096")
        private Integer bitrate;

        @Schema(description = "编码格式", example = "H.264")
        private String codecType;

        @Schema(description = "图像质量 (1-100)", example = "80")
        private Integer quality;

        @Schema(description = "GOP长度", example = "50")
        private Integer gopLength;
    }
}












