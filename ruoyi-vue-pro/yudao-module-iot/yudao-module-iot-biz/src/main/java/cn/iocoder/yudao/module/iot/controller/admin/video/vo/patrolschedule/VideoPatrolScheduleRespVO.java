package cn.iocoder.yudao.module.iot.controller.admin.video.vo.patrolschedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 视频定时轮巡计划 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 视频定时轮巡计划 Response VO")
@Data
public class VideoPatrolScheduleRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "计划名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "工作日轮巡")
    private String name;

    @Schema(description = "轮巡计划ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long patrolPlanId;

    @Schema(description = "轮巡计划名称", example = "大厅监控轮巡")
    private String patrolPlanName;

    @Schema(description = "计划类型：1-日计划 2-周计划", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer scheduleType;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "08:00:00")
    private LocalTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "18:00:00")
    private LocalTime endTime;

    @Schema(description = "周几执行（周计划）：1,2,3,4,5,6,7 逗号分隔", example = "1,2,3,4,5")
    private String weekDays;

    @Schema(description = "状态：0-禁用 1-启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "备注", example = "工作日自动轮巡")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
