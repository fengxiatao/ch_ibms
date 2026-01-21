package cn.iocoder.yudao.module.iot.dal.dataobject.task;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 定时任务配置 DO
 *
 * @author 芋道源码
 */
@TableName("iot_scheduled_task_config")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTaskConfigDO extends BaseDO {

    /**
     * 任务配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 实体类型: PRODUCT/DEVICE/CAMPUS/BUILDING/FLOOR/AREA
     */
    private String entityType;

    /**
     * 实体ID
     */
    private Long entityId;

    /**
     * 实体名称（冗余字段，便于查询）
     */
    private String entityName;

    /**
     * 任务类型: IOT_DEVICE_OFFLINE_CHECK等
     */
    private String jobType;

    /**
     * 任务名称（中文描述）
     */
    private String jobName;

    /**
     * 是否启用: false-禁用, true-启用
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean enabled;

    /**
     * Cron表达式
     */
    private String cronExpression;

    /**
     * 执行间隔(秒)，与cron二选一
     */
    private Integer intervalSeconds;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务配置参数（JSON格式）
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String jobConfig;

    /**
     * 优先级: 1-最高, 10-最低
     */
    private Integer priority;

    /**
     * 冲突策略: SKIP/QUEUE/INTERRUPT/CONCURRENT
     */
    private String conflictStrategy;

    /**
     * 超时时间(秒)
     */
    private Integer timeoutSeconds;

    /**
     * 失败重试次数
     */
    private Integer retryCount;

    /**
     * 失败告警
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean alertOnFailure;

    /**
     * 运行状态: RUNNING/STOPPED/EXECUTING
     */
    private String status;

    /**
     * 是否继承自产品: false-否, true-是
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean fromProduct;

    /**
     * 所属产品ID（设备任务时有值）
     */
    private Long productId;

    /**
     * 上次执行时间
     */
    private LocalDateTime lastExecutionTime;

    /**
     * 上次执行状态: SUCCESS/FAILURE/RUNNING
     */
    private String lastExecutionStatus;

    /**
     * 上次执行消息
     */
    private String lastExecutionMessage;

    /**
     * 总执行次数
     */
    private Integer executionCount;

    /**
     * 成功次数
     */
    private Integer successCount;

    /**
     * 失败次数
     */
    private Integer failCount;

    /**
     * 平均执行时长(毫秒)
     */
    private Integer avgDurationMs;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecutionTime;

}




