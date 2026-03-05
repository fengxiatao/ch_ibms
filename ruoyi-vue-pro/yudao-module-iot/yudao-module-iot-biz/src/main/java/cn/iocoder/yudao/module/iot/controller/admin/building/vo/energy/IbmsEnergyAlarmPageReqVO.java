package cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 能耗告警分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsEnergyAlarmPageReqVO extends PageParam {

    @Schema(description = "仪表ID")
    private Long meterId;

    @Schema(description = "仪表名称")
    private String meterName;

    @Schema(description = "告警类型 1-超标告警 2-异常告警 3-离线告警 4-通信故障")
    private Integer alarmType;

    @Schema(description = "告警级别 1-一般 2-重要 3-紧急")
    private Integer alarmLevel;

    @Schema(description = "状态 0-未处理 1-处理中 2-已处理 3-已忽略")
    private Integer status;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

}
