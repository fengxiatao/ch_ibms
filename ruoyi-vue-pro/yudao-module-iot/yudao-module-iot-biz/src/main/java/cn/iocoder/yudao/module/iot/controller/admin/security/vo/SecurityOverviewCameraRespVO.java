package cn.iocoder.yudao.module.iot.controller.admin.security.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 安防概览 - 摄像头响应 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 安防概览摄像头响应 VO")
@Data
public class SecurityOverviewCameraRespVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备名称", example = "device_192_168_1_201")
    private String deviceName;

    @Schema(description = "设备昵称", example = "大堂摄像头")
    private String nickname;

    @Schema(description = "设备位置（IP地址）", example = "192.168.1.201")
    private String location;

    @Schema(description = "是否在线", example = "true")
    private Boolean online;

    @Schema(description = "状态代码", example = "online")
    private String status;

    @Schema(description = "状态文本", example = "正常")
    private String statusText;

    @Schema(description = "实时抓图（base64）", example = "data:image/jpeg;base64,...")
    private String snapshotUrl;

    @Schema(description = "最后在线时间")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "设备配置信息")
    private DeviceInfo deviceInfo;

    @Schema(description = "设备秘钥", example = "5McgJPcXpau4LWCo_e4e3c69054bd4b7b9104ab6766f74d1a")
    private String deviceKey;

    @Data
    @Schema(description = "设备配置信息")
    public static class DeviceInfo {
        
        @Schema(description = "厂商", example = "onvif")
        private String vendor;

        @Schema(description = "HTTP端口", example = "80")
        private Integer httpPort;

        @Schema(description = "RTSP端口", example = "554")
        private Integer rtspPort;

        @Schema(description = "ONVIF端口", example = "8999")
        private Integer onvifPort;
    }
}

