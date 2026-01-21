package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.schedule;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 定时布防任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotAlarmSchedulePageReqVO extends PageParam {

    @Schema(description = "报警主机ID", example = "1")
    private Long hostId;

    @Schema(description = "任务名称", example = "夜间布防")
    private String taskName;

    @Schema(description = "布防类型", example = "ARM_ALL")
    private String armType;

    @Schema(description = "状态：0-禁用, 1-启用", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
