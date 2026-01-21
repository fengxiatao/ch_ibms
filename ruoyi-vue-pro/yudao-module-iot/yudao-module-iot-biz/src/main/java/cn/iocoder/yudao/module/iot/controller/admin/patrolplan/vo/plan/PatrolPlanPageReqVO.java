package cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.plan;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 轮巡计划分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PatrolPlanPageReqVO extends PageParam {

    @Schema(description = "计划名称", example = "大堂轮巡计划")
    private String planName;

    @Schema(description = "计划编码", example = "PLAN_LOBBY")
    private String planCode;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "运行状态", example = "running")
    private String runningStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
