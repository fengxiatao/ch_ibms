package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "管理后台 - 电子巡更计划 Response VO")
@Data
public class EpatrolPlanRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "计划编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "JH20240101001")
    private String planCode;

    @Schema(description = "计划名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "日常巡更计划")
    private String planName;

    @Schema(description = "巡更路线ID", example = "1")
    private Long routeId;

    @Schema(description = "巡更路线名称", example = "白班巡更路线")
    private String routeName;

    @Schema(description = "开始日期", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "星期选择", example = "[1,2,3,4,5]")
    private List<Integer> weekdays;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "时段列表")
    private List<PlanPeriodRespVO> periods;

    @Schema(description = "巡更人员姓名列表（用于列表展示）")
    private List<String> personNames;

    @Schema(description = "执行时间段描述（用于列表展示）")
    private String timeRangeDesc;

    @Data
    @Schema(description = "计划时段 Response VO")
    public static class PlanPeriodRespVO {

        @Schema(description = "时段ID", example = "1")
        private Long id;

        @Schema(description = "开始时间", example = "08:00")
        private LocalTime startTime;

        @Schema(description = "结束时间", example = "09:00")
        private LocalTime endTime;

        @Schema(description = "巡更时长（分钟）", example = "60")
        private Integer durationMinutes;

        @Schema(description = "巡更人员ID列表", example = "[1,2]")
        private List<Long> personIds;

        @Schema(description = "巡更人员姓名列表", example = "[\"张三\",\"李四\"]")
        private List<String> personNames;

    }

}
