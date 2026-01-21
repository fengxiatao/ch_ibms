package cn.iocoder.yudao.module.iot.controller.admin.access.vo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁设备完整配置响应 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 门禁设备完整配置响应 VO")
@Data
public class IotAccessDeviceConfigRespVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "110")
    private Long id;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号门门禁")
    private String deviceName;

    @Schema(description = "设备编号", example = "DEV001")
    private String deviceCode;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17")
    private Long productId;

    @Schema(description = "产品名称", example = "指纹一体机")
    private String productName;

    @Schema(description = "设备IP地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "192.168.1.207")
    private String ipAddress;

    @Schema(description = "设备端口", example = "37777")
    private Integer port;

    @Schema(description = "登录用户名", example = "admin")
    private String username;

    @Schema(description = "设备状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer state;

    @Schema(description = "设备状态描述", example = "在线")
    private String stateDesc;

    @Schema(description = "在线时长(秒)", example = "3600")
    private Long onlineDuration;

    @Schema(description = "上线时间")
    private LocalDateTime onlineTime;

    @Schema(description = "离线时间")
    private LocalDateTime offlineTime;

    @Schema(description = "激活时间")
    private LocalDateTime activeTime;

    @Schema(description = "登录句柄", example = "12345678")
    private Long loginHandle;

    @Schema(description = "登录句柄状态", example = "已连接")
    private String loginHandleStatus;

    @Schema(description = "设备能力集")
    private DeviceCapabilities capabilities;

    @Schema(description = "通道数量", example = "4")
    private Integer channelCount;

    @Schema(description = "通道列表")
    private List<ChannelInfo> channels;

    @Schema(description = "设备配置JSON")
    private String config;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 设备能力集
     */
    @Schema(description = "设备能力集")
    @Data
    public static class DeviceCapabilities {
        
        @Schema(description = "支持密码认证", example = "true")
        private Boolean supportPassword;

        @Schema(description = "支持卡片认证", example = "true")
        private Boolean supportCard;

        @Schema(description = "支持指纹认证", example = "true")
        private Boolean supportFingerprint;

        @Schema(description = "支持人脸认证", example = "true")
        private Boolean supportFace;

        @Schema(description = "支持二维码认证", example = "false")
        private Boolean supportQrCode;

        @Schema(description = "支持远程开门", example = "true")
        private Boolean supportRemoteOpen;

        @Schema(description = "支持常开常闭", example = "true")
        private Boolean supportAlwaysMode;

        @Schema(description = "最大用户数", example = "10000")
        private Integer maxUsers;

        @Schema(description = "最大卡片数", example = "10000")
        private Integer maxCards;

        @Schema(description = "最大指纹数", example = "3000")
        private Integer maxFingerprints;

        @Schema(description = "最大人脸数", example = "3000")
        private Integer maxFaces;
    }

    /**
     * 通道信息
     */
    @Schema(description = "通道信息")
    @Data
    public static class ChannelInfo {
        
        @Schema(description = "通道ID", example = "1")
        private Long id;

        @Schema(description = "通道索引", example = "0")
        private Integer channelIndex;

        @Schema(description = "通道名称", example = "一号门")
        private String channelName;

        @Schema(description = "门状态", example = "关闭")
        private String doorStatus;

        @Schema(description = "锁状态", example = "已锁")
        private String lockStatus;

        @Schema(description = "常开常闭状态", example = "正常")
        private String alwaysMode;

        @Schema(description = "开门时长(秒)", example = "5")
        private Integer openDuration;

        @Schema(description = "报警时长(秒)", example = "30")
        private Integer alarmDuration;
    }

}
