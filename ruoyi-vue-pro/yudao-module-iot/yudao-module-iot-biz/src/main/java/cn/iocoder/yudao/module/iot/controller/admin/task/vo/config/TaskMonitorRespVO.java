package cn.iocoder.yudao.module.iot.controller.admin.task.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 任务监控 Response VO")
@Data
public class TaskMonitorRespVO {

    @Schema(description = "任务配置ID", example = "1024")
    private Long id;

    @Schema(description = "实体类型", example = "DEVICE")
    private String entityType;

    @Schema(description = "实体ID", example = "123")
    private Long entityId;

    @Schema(description = "实体名称", example = "摄像头-001")
    private String entityName;

    @Schema(description = "任务类型", example = "IOT_DEVICE_OFFLINE_CHECK")
    private String jobType;

    @Schema(description = "任务名称", example = "设备离线检查")
    private String jobName;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "Cron表达式", example = "0 */10 * * * ?")
    private String cronExpression;

    @Schema(description = "执行间隔(秒)", example = "600")
    private Integer intervalSeconds;

    @Schema(description = "优先级", example = "5")
    private Integer priority;

    @Schema(description = "上次执行时间")
    private LocalDateTime lastExecutionTime;

    @Schema(description = "上次执行状态", example = "SUCCESS")
    private String lastExecutionStatus;

    @Schema(description = "总执行次数", example = "100")
    private Integer executionCount;

    @Schema(description = "成功次数", example = "98")
    private Integer successCount;

    @Schema(description = "失败次数", example = "2")
    private Integer failCount;

    @Schema(description = "平均执行时长(毫秒)", example = "250")
    private Integer avgDurationMs;

    @Schema(description = "下次执行时间")
    private LocalDateTime nextExecutionTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}




