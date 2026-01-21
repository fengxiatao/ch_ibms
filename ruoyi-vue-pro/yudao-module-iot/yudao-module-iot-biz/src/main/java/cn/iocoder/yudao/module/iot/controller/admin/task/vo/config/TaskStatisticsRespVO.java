package cn.iocoder.yudao.module.iot.controller.admin.task.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 任务统计 Response VO")
@Data
public class TaskStatisticsRespVO {

    @Schema(description = "总任务数", example = "1234")
    private Long total;

    @Schema(description = "启用的任务数", example = "1200")
    private Long enabled;

    @Schema(description = "禁用的任务数", example = "34")
    private Long disabled;

    @Schema(description = "运行中的任务数", example = "15")
    private Long running;

    @Schema(description = "最近24小时成功次数", example = "1180")
    private Long recentSuccess;

    @Schema(description = "最近24小时失败次数", example = "5")
    private Long recentFailed;

}




