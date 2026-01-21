package cn.iocoder.yudao.module.iot.controller.admin.task.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 任务执行日志 Response VO")
@Data
public class TaskLogRespVO {

    @Schema(description = "日志ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "任务配置ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long taskConfigId;

    @Schema(description = "实体类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "PRODUCT")
    private String entityType;

    @Schema(description = "实体ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long entityId;

    @Schema(description = "实体名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "大华摄像头")
    private String entityName;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "DEVICE_STATUS_SYNC")
    private String jobType;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "设备状态同步")
    private String jobName;

    @Schema(description = "执行状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "SUCCESS")
    private String executionStatus;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "执行时长(毫秒)", example = "1000")
    private Integer durationMs;

    @Schema(description = "执行器信息", example = "192.168.1.100:thread-1")
    private String executorInfo;

    @Schema(description = "执行结果摘要", example = "成功同步10个设备")
    private String resultSummary;

    @Schema(description = "执行结果详情")
    private String resultDetail;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "异常堆栈")
    private String errorStack;

    @Schema(description = "影响数量", example = "10")
    private Integer affectedCount;

    @Schema(description = "重试次数", example = "0")
    private Integer retryCount;

    @Schema(description = "父日志ID", example = "1024")
    private Long parentLogId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
























































