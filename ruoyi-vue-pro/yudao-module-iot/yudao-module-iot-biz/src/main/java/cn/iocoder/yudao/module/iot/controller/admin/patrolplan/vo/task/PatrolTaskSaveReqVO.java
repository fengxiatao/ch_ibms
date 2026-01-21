package cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 轮巡任务创建/更新 Request VO")
@Data
public class PatrolTaskSaveReqVO {

    @Schema(description = "任务ID", example = "1")
    private Long id;

    @Schema(description = "计划ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计划ID不能为空")
    private Long planId;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "大堂轮巡任务")
    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @Schema(description = "任务编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "TASK_LOBBY")
    @NotBlank(message = "任务编码不能为空")
    private String taskCode;

    @Schema(description = "任务描述", example = "大堂区域的轮巡任务")
    private String description;

    @Schema(description = "任务执行顺序", example = "1")
    private Integer taskOrder;

    @Schema(description = "任务总时长（秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
    @NotNull(message = "任务时长不能为空")
    private Integer duration;

    @Schema(description = "排班类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排班类型不能为空")
    private Integer scheduleType;

    @Schema(description = "排班配置（JSON）", example = "{\"type\":\"daily\"}")
    private Map<String, Object> scheduleConfig;

    @Schema(description = "时间段配置（JSON）", example = "[{\"start\":\"08:00\",\"end\":\"18:00\"}]")
    private List<Map<String, String>> timeSlots;

    @Schema(description = "轮巡模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "轮巡模式不能为空")
    private Integer loopMode;

    @Schema(description = "轮巡间隔（分钟）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "轮巡间隔不能为空")
    private Integer intervalMinutes;

    @Schema(description = "自动抓拍", example = "true")
    private Boolean autoSnapshot;

    @Schema(description = "自动录像", example = "false")
    private Boolean autoRecording;

    @Schema(description = "录像时长（秒）", example = "30")
    private Integer recordingDuration;

    @Schema(description = "AI分析", example = "true")
    private Boolean aiAnalysis;

    @Schema(description = "异常告警", example = "true")
    private Boolean alertOnAbnormal;

    @Schema(description = "告警用户ID列表（逗号分隔）", example = "1,2,3")
    private String alertUserIds;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "排序", example = "1")
    private Integer sort;

}
