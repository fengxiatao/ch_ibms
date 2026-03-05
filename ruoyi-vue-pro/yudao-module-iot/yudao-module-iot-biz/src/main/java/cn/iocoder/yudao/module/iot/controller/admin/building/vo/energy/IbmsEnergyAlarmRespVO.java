package cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 能耗告警 Response VO")
@Data
public class IbmsEnergyAlarmRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "仪表ID")
    private Long meterId;

    @Schema(description = "仪表编码")
    private String meterCode;

    @Schema(description = "仪表名称")
    private String meterName;

    @Schema(description = "告警类型 1-超标告警 2-异常告警 3-离线告警 4-通信故障")
    private Integer alarmType;

    @Schema(description = "告警级别 1-一般 2-重要 3-紧急")
    private Integer alarmLevel;

    @Schema(description = "告警内容")
    private String alarmContent;

    @Schema(description = "告警值")
    private String alarmValue;

    @Schema(description = "阈值")
    private String thresholdValue;

    @Schema(description = "告警时间")
    private LocalDateTime alarmTime;

    @Schema(description = "状态 0-未处理 1-处理中 2-已处理 3-已忽略")
    private Integer status;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理人")
    private String handler;

    @Schema(description = "处理备注")
    private String handleRemark;

}
