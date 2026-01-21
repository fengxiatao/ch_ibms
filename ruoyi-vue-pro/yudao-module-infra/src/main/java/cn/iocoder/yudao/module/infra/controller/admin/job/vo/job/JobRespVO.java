package cn.iocoder.yudao.module.infra.controller.admin.job.vo.job;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.infra.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 定时任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class JobRespVO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("任务编号")
    private Long id;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试任务")
    @ExcelProperty("任务名称")
    private String name;

    @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "任务状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.JOB_STATUS)
    private Integer status;

    @Schema(description = "处理器的名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "sysUserSessionTimeoutJob")
    @ExcelProperty("处理器的名字")
    private String handlerName;

    @Schema(description = "处理器的参数", example = "yudao")
    @ExcelProperty("处理器的参数")
    private String handlerParam;

    @Schema(description = "CRON 表达式", requiredMode = Schema.RequiredMode.REQUIRED, example = "0/10 * * * * ? *")
    @ExcelProperty("CRON 表达式")
    private String cronExpression;

    @Schema(description = "重试次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "重试次数不能为空")
    private Integer retryCount;

    @Schema(description = "重试间隔", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Integer retryInterval;

    @Schema(description = "监控超时时间", example = "1000")
    @ExcelProperty("监控超时时间")
    private Integer monitorTimeout;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    // ========== 业务扩展字段 ==========
    
    @Schema(description = "业务类型", example = "IOT_DEVICE_OFFLINE_CHECK")
    @ExcelProperty("业务类型")
    private String businessType;
    
    @Schema(description = "业务模块", example = "iot")
    @ExcelProperty("业务模块")
    private String businessModule;
    
    @Schema(description = "优先级", example = "5")
    @ExcelProperty("优先级")
    private Integer priority;
    
    @Schema(description = "是否允许并发执行", example = "false")
    @ExcelProperty("是否允许并发")
    private Boolean concurrent;
    
    @Schema(description = "任务分组", example = "iot")
    @ExcelProperty("任务分组")
    private String jobGroup;
    
    @Schema(description = "最大并发数", example = "1")
    @ExcelProperty("最大并发数")
    private Integer maxConcurrentCount;
    
    @Schema(description = "冲突策略", example = "SKIP")
    @ExcelProperty("冲突策略")
    private String conflictStrategy;
    
    @Schema(description = "依赖的任务ID列表", example = "101,102,103")
    private String dependJobs;
    
    @Schema(description = "资源限制配置(JSON)", example = "{\"maxMemory\": 512, \"maxCpu\": 50}")
    private String resourceLimit;

}
