package cn.iocoder.yudao.module.iot.controller.admin.access.vo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门禁通道 Response VO
 */
@Schema(description = "管理后台 - 门禁通道 Response VO")
@Data
public class IotAccessChannelRespVO {

    @Schema(description = "通道ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备ID", example = "110")
    private Long deviceId;

    @Schema(description = "设备名称", example = "大门门禁")
    private String deviceName;

    @Schema(description = "通道号", example = "1")
    private Integer channelNo;

    @Schema(description = "通道名称", example = "1号门")
    private String channelName;

    @Schema(description = "门状态（open/closed/unknown）", example = "closed")
    private String doorStatus;

    @Schema(description = "锁状态（locked/unlocked/unknown）", example = "locked")
    private String lockStatus;

    @Schema(description = "是否常开", example = "false")
    private Boolean alwaysOpen;

    @Schema(description = "是否常闭", example = "false")
    private Boolean alwaysClosed;

    @Schema(description = "开门时长（秒）", example = "5")
    private Integer openDuration;

    @Schema(description = "报警时长（秒）", example = "30")
    private Integer alarmDuration;

    @Schema(description = "在线状态（0离线 1在线）", example = "1")
    private Integer onlineStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
