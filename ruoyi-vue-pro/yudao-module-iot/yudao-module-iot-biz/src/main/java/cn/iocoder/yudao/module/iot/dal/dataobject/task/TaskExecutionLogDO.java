package cn.iocoder.yudao.module.iot.dal.dataobject.task;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 任务执行日志 DO
 *
 * @author 芋道源码
 */
@TableName("iot_task_execution_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecutionLogDO extends TenantBaseDO {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务配置ID
     */
    private Long taskConfigId;

    /**
     * 实体类型
     */
    private String entityType;

    /**
     * 实体ID
     */
    private Long entityId;

    /**
     * 实体名称
     */
    private String entityName;

    /**
     * 任务类型
     */
    private String jobType;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 执行状态: SUCCESS/FAILED/TIMEOUT/CANCELLED
     */
    private String executionStatus;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行时长(毫秒)
     */
    private Integer durationMs;

    /**
     * 执行器信息（服务器IP、线程等）
     */
    private String executorInfo;

    /**
     * 执行结果摘要
     */
    private String resultSummary;

    /**
     * 执行结果详情（JSON或文本）
     */
    private String resultDetail;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 异常堆栈（如果失败）
     */
    private String errorStack;

    /**
     * 影响数量（如检查设备数、发送告警数等）
     */
    private Integer affectedCount;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 父日志ID（重试时关联原始日志）
     */
    private Long parentLogId;

}




