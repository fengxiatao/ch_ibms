package cn.iocoder.yudao.module.iot.controller.admin.task.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 任务配置保存 Request VO")
@Data
public class ScheduledTaskConfigSaveReqVO {

    @Schema(description = "任务配置ID", example = "1024")
    private Long id;

    @Schema(description = "实体类型", required = true, example = "DEVICE")
    @NotBlank(message = "实体类型不能为空")
    private String entityType;

    @Schema(description = "实体ID", required = true, example = "123")
    @NotNull(message = "实体ID不能为空")
    private Long entityId;

    @Schema(description = "实体名称", example = "摄像头-001")
    private String entityName;

    @Schema(description = "任务类型", required = true, example = "IOT_DEVICE_OFFLINE_CHECK")
    @NotBlank(message = "任务类型不能为空")
    private String jobType;

    @Schema(description = "任务名称", example = "设备离线检查")
    private String jobName;

    @Schema(description = "是否启用", required = true)
    @NotNull(message = "是否启用不能为空")
    private Boolean enabled;

    @Schema(description = "Cron表达式", example = "0 */10 * * * ?")
    private String cronExpression;

    @Schema(description = "执行间隔(秒)", example = "600")
    private Integer intervalSeconds;

    @Schema(description = "任务配置参数(JSON)", example = "{\"timeout\":30}")
    private String jobConfig;

    @Schema(description = "优先级", example = "5")
    private Integer priority;

    @Schema(description = "是否继承自产品")
    private Boolean fromProduct;

    @Schema(description = "所属产品ID", example = "100")
    private Long productId;

}


