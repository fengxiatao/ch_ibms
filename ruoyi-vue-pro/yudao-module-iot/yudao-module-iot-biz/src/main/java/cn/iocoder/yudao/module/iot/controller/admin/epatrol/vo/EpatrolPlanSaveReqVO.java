package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "管理后台 - 电子巡更计划新增/修改 Request VO")
@Data
public class EpatrolPlanSaveReqVO {

    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "计划名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "日常巡更计划")
    @NotBlank(message = "计划名称不能为空")
    private String planName;

    @Schema(description = "巡更路线ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "巡更路线不能为空")
    private Long routeId;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01")
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "星期选择", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2,3,4,5]")
    @NotEmpty(message = "星期选择不能为空")
    private List<Integer> weekdays;

    @Schema(description = "时段列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "时段列表不能为空")
    private List<PlanPeriodItem> periods;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Data
    @Schema(description = "计划时段项")
    public static class PlanPeriodItem {

        @Schema(description = "时段ID（修改时使用）", example = "1")
        private Long id;

        @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "08:00")
        @NotNull(message = "开始时间不能为空")
        private LocalTime startTime;

        @Schema(description = "巡更时长（分钟）", requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
        @NotNull(message = "巡更时长不能为空")
        private Integer durationMinutes;

        @Schema(description = "巡更人员ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2]")
        @NotEmpty(message = "巡更人员不能为空")
        private List<Long> personIds;

    }

}
