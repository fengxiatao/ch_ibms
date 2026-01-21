package cn.iocoder.yudao.module.iot.controller.admin.access.vo.management;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁控制器树形结构 VO
 * 
 * 用于门禁管理页面展示控制器及其下属门通道
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 门禁控制器树形结构 VO")
@Data
public class AccessControllerTreeVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "110")
    private Long deviceId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号门门禁控制器")
    private String deviceName;

    @Schema(description = "设备编码", example = "DEV001")
    private String deviceCode;

    @Schema(description = "设备IP", example = "192.168.1.207")
    private String ipAddress;

    @Schema(description = "设备端口", example = "37777")
    private Integer port;

    @Schema(description = "在线状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean online;

    @Schema(description = "设备状态（0-未激活 1-在线 2-离线）", example = "1")
    private Integer state;

    @Schema(description = "设备状态描述", example = "在线")
    private String stateDesc;

    @Schema(description = "最后上线时间")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "门通道数量", example = "4")
    private Integer channelCount;

    @Schema(description = "是否支持视频预览", example = "true")
    private Boolean supportVideo;

    @Schema(description = "门禁设备类型（ACCESS_GEN1/ACCESS_GEN2）", example = "ACCESS_GEN1")
    private String deviceType;

    @Schema(description = "门通道列表")
    private List<DoorChannelVO> channels;

    /**
     * 门通道信息
     */
    @Schema(description = "门通道信息")
    @Data
    public static class DoorChannelVO {

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

        @Schema(description = "是否可操作（设备在线时可操作）", example = "true")
        private Boolean operable;
    }

}
