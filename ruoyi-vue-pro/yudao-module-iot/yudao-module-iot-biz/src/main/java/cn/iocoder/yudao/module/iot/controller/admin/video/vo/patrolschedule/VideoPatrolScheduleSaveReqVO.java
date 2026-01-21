package cn.iocoder.yudao.module.iot.controller.admin.video.vo.patrolschedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/**
 * 视频定时轮巡计划新增/修改 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 视频定时轮巡计划新增/修改 Request VO")
@Data
public class VideoPatrolScheduleSaveReqVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "计划名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "工作日轮巡")
    @NotEmpty(message = "计划名称不能为空")
    private String name;

    @Schema(description = "轮巡计划ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "轮巡计划不能为空")
    private Long patrolPlanId;

    @Schema(description = "计划类型：1-日计划 2-周计划", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计划类型不能为空")
    private Integer scheduleType;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "08:00:00")
    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "18:00:00")
    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @Schema(description = "周几执行（周计划）：1,2,3,4,5,6,7 逗号分隔", example = "1,2,3,4,5")
    private String weekDays;

    @Schema(description = "状态：0-禁用 1-启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "工作日自动轮巡")
    private String remark;

}
