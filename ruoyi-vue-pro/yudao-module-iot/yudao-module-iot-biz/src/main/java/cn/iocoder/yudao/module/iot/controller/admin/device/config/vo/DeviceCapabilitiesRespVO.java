package cn.iocoder.yudao.module.iot.controller.admin.device.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - IoT 设备能力集 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 设备能力集 Response VO")
@Data
public class DeviceCapabilitiesRespVO {

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "制造商", example = "Dahua")
    private String manufacturer;

    @Schema(description = "型号", example = "IPC-HFW1230S")
    private String model;

    @Schema(description = "固件版本", example = "2.840.0000000.18.R")
    private String firmwareVersion;

    @Schema(description = "支持的分辨率列表")
    private List<String> supportedResolutions;

    @Schema(description = "支持的编码格式列表")
    private List<String> supportedCodecs;

    @Schema(description = "支持的最大帧率", example = "30")
    private Integer maxFrameRate;

    @Schema(description = "是否支持PTZ", example = "false")
    private Boolean ptzSupported;

    @Schema(description = "是否支持音频", example = "true")
    private Boolean audioSupported;

    @Schema(description = "是否支持运动检测", example = "true")
    private Boolean motionDetectionSupported;

    @Schema(description = "是否支持视频遮挡检测", example = "true")
    private Boolean tamperDetectionSupported;

    @Schema(description = "是否支持DHCP", example = "true")
    private Boolean dhcpSupported;

    @Schema(description = "是否支持录像", example = "true")
    private Boolean recordingSupported;

    @Schema(description = "媒体配置文件列表")
    private List<MediaProfile> mediaProfiles;

    /**
     * 媒体配置文件
     */
    @Data
    public static class MediaProfile {
        @Schema(description = "配置文件名称", example = "Profile_1")
        private String name;

        @Schema(description = "配置文件Token", example = "ProfileToken1")
        private String token;

        @Schema(description = "视频编码", example = "H.264")
        private String videoEncoding;

        @Schema(description = "分辨率", example = "1920x1080")
        private String resolution;

        @Schema(description = "帧率", example = "25")
        private Integer frameRate;

        @Schema(description = "码率", example = "4096")
        private Integer bitrate;
    }
}












