package cn.iocoder.yudao.module.iot.controller.admin.access.vo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门禁通道详细信息响应 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 门禁通道详细信息响应 VO")
@Data
public class IotAccessChannelDetailRespVO {

    @Schema(description = "通道ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "110")
    private Long deviceId;

    @Schema(description = "设备名称", example = "一号门门禁")
    private String deviceName;

    @Schema(description = "通道号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer channelNo;

    @Schema(description = "通道索引(从0开始)", example = "0")
    private Integer channelIndex;

    @Schema(description = "通道名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号门")
    private String channelName;

    @Schema(description = "通道编码", example = "CH-ACCESS-001")
    private String channelCode;

    @Schema(description = "通道类型", example = "ACCESS")
    private String channelType;

    @Schema(description = "通道子类型", example = "CARD_READER")
    private String channelSubType;

    @Schema(description = "安装位置", example = "A栋1层前台大厅")
    private String location;

    // ========== 门禁通道专用字段 ==========

    @Schema(description = "门点名称", example = "前门")
    private String doorName;

    @Schema(description = "门方向", example = "IN")
    private String doorDirection;

    @Schema(description = "读卡器类型", example = "IC")
    private String cardReaderType;

    @Schema(description = "锁类型", example = "ELECTRIC")
    private String lockType;

    // ========== 通道配置 ==========

    @Schema(description = "开门时长(秒)", example = "5")
    private Integer openDuration;

    @Schema(description = "报警时长(秒)", example = "30")
    private Integer alarmDuration;

    @Schema(description = "超时报警开关", example = "true")
    private Boolean timeoutAlarmEnabled;

    @Schema(description = "强制报警开关", example = "true")
    private Boolean forceAlarmEnabled;

    // ========== 实时状态 ==========

    @Schema(description = "门状态", example = "关闭")
    private String doorStatus;

    @Schema(description = "门状态代码", example = "0")
    private Integer doorStatusCode;

    @Schema(description = "锁状态", example = "已锁")
    private String lockStatus;

    @Schema(description = "锁状态代码", example = "0")
    private Integer lockStatusCode;

    @Schema(description = "常开常闭状态", example = "正常")
    private String alwaysMode;

    @Schema(description = "常开常闭状态代码", example = "0")
    private Integer alwaysModeCode;

    @Schema(description = "在线状态", example = "1")
    private Integer onlineStatus;

    @Schema(description = "在线状态描述", example = "在线")
    private String onlineStatusDesc;

    @Schema(description = "启用状态", example = "1")
    private Integer enableStatus;

    @Schema(description = "启用状态描述", example = "启用")
    private String enableStatusDesc;

    @Schema(description = "报警状态", example = "0")
    private Integer alarmStatus;

    @Schema(description = "报警状态描述", example = "正常")
    private String alarmStatusDesc;

    @Schema(description = "最后在线时间")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "最后同步时间")
    private LocalDateTime lastSyncTime;

    // ========== 扩展配置 ==========

    @Schema(description = "扩展配置JSON")
    private String config;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
