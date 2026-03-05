package cn.iocoder.yudao.module.iot.controller.admin.building.vo.env;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 环境告警记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsEnvAlarmPageReqVO extends PageParam {

    @Schema(description = "传感器ID")
    private Long sensorId;

    @Schema(description = "传感器名称")
    private String sensorName;

    @Schema(description = "告警类型 1-温度 2-湿度 3-PM2.5 4-CO2 5-噪音 6-光照 7-气压 8-离线")
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
