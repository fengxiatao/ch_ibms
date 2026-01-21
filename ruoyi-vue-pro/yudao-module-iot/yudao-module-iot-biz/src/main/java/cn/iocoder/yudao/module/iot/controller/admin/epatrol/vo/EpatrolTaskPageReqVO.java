package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 电子巡更任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EpatrolTaskPageReqVO extends PageParam {

    @Schema(description = "任务编号", example = "RW20240101001")
    private String taskCode;

    @Schema(description = "计划ID", example = "1")
    private Long planId;

    @Schema(description = "路线ID", example = "1")
    private Long routeId;

    @Schema(description = "任务状态", example = "0")
    private Integer status;

    @Schema(description = "任务日期-开始", example = "2024-01-01")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate taskDateStart;

    @Schema(description = "任务日期-结束", example = "2024-01-31")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate taskDateEnd;

}
