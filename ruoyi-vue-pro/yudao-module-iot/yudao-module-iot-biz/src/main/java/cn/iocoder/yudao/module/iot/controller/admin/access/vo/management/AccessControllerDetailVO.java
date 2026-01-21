package cn.iocoder.yudao.module.iot.controller.admin.access.vo.management;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁控制器详情 VO
 * 
 * 包含控制器完整信息和门通道列表
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 门禁控制器详情 VO")
@Data
public class AccessControllerDetailVO {

    // ========== 设备基本信息 ==========

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "110")
    private Long deviceId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号门门禁控制器")
    private String deviceName;

    @Schema(description = "设备编码", example = "DEV001")
    private String deviceCode;

    @Schema(description = "产品ID", example = "17")
    private Long productId;

    @Schema(description = "产品名称", example = "指纹一体机")
    private String productName;

    // ========== 连接信息 ==========

    @Schema(description = "设备IP", requiredMode = Schema.RequiredMode.REQUIRED, example = "192.168.1.207")
    private String ipAddress;

    @Schema(description = "设备端口", example = "37777")
    private Integer port;

    @Schema(description = "登录用户名", example = "admin")
    private String username;

    // ========== 状态信息 ==========

    @Schema(description = "在线状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean online;

    @Schema(description = "设备状态（0-未激活 1-在线 2-离线）", example = "1")
    private Integer state;

    @Schema(description = "设备状态描述", example = "在线")
    private String stateDesc;

    @Schema(description = "最后上线时间")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "最后离线时间")
    private LocalDateTime lastOfflineTime;

    @Schema(description = "激活时间")
    private LocalDateTime activeTime;

    @Schema(description = "在线时长（秒）", example = "3600")
    private Long onlineDuration;

    // ========== 能力信息 ==========

    @Schema(description = "是否支持视频预览", example = "true")
    private Boolean supportVideo;

    @Schema(description = "门禁设备类型（ACCESS_GEN1/ACCESS_GEN2）", example = "ACCESS_GEN1")
    private String deviceType;

    @Schema(description = "设备能力集")
    private DeviceCapabilities capabilities;

    // ========== 通道信息 ==========

    @Schema(description = "门通道数量", example = "4")
    private Integer channelCount;

    @Schema(description = "门通道列表")
    private List<DoorChannelDetailVO> channels;

    // ========== 其他信息 ==========

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
     * 门通道详细信息
     */
    @Schema(description = "门通道详细信息")
    @Data
    public static class DoorChannelDetailVO {

        @Schema(description = "通道ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long channelId;

        @Schema(description = "通道号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer channelNo;

        @Schema(description = "通道名称", example = "一号门")
        private String channelName;

        @Schema(description = "门状态: 0-关闭, 1-打开, 2-未知", example = "0")
        private Integer doorStatus;

        @Schema(description = "门状态描述", example = "关闭")
        private String doorStatusDesc;

        @Schema(description = "锁状态: 0-锁定, 1-解锁, 2-未知", example = "0")
        private Integer lockStatus;

        @Schema(description = "锁状态描述", example = "已锁")
        private String lockStatusDesc;

        @Schema(description = "常开常闭模式: 0-正常, 1-常开, 2-常闭", example = "0")
        private Integer alwaysMode;

        @Schema(description = "常开常闭模式描述", example = "正常")
        private String alwaysModeDesc;

        @Schema(description = "开门时长（秒）", example = "5")
        private Integer openDuration;

        @Schema(description = "报警时长（秒）", example = "30")
        private Integer alarmDuration;

        @Schema(description = "是否可操作（设备在线时可操作）", example = "true")
        private Boolean operable;

        @Schema(description = "安装位置", example = "A栋1层前台")
        private String location;
    }

}
