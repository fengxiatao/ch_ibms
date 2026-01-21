package cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "管理后台 - 轮巡计划创建/更新 Request VO")
@Data
public class PatrolPlanSaveReqVO {

    @Schema(description = "计划ID", example = "1")
    private Long id;

    @Schema(description = "计划名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "大堂轮巡计划")
    @NotBlank(message = "计划名称不能为空")
    private String planName;

    @Schema(description = "计划编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "PLAN_LOBBY")
    @NotBlank(message = "计划编码不能为空")
    private String planCode;

    @Schema(description = "计划描述", example = "大堂区域的视频轮巡计划")
    private String description;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "循环模式：1-循环执行，2-执行一次", example = "1")
    private Integer loopMode;

    @Schema(description = "执行人", example = "admin")
    private String executor;

    @Schema(description = "执行人姓名", example = "管理员")
    private String executorName;

    @Schema(description = "开始日期", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "排序", example = "1")
    private Integer sort;

}
