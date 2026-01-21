package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 报警主机 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotAlarmHostRespVO extends IotAlarmHostBaseVO {

    @Schema(description = "主机ID", example = "1")
    private Long id;

    @Schema(description = "在线状态：0-离线, 1-在线", example = "1")
    private Integer onlineStatus;

    @Schema(description = "布防状态：DISARM-撤防, ARM_ALL-全部布防, ARM_EMERGENCY-紧急布防", example = "DISARM")
    private String armStatus;

    @Schema(description = "系统状态：0-撤防, 1-外出布防, 2-居家布防", example = "0")
    private Integer systemStatus;

    @Schema(description = "报警状态：0-正常, 1-报警中", example = "0")
    private Integer alarmStatus;

    @Schema(description = "分区数量", example = "8")
    private Integer partitionCount;

    @Schema(description = "防区数量", example = "24")
    private Integer zoneCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
