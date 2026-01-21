package cn.iocoder.yudao.module.infra.dal.dataobject.job;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.infra.enums.job.JobStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 定时任务 DO
 *
 * @author 长辉信息科技有限公司
 */
@TableName("infra_job")
@KeySequence("infra_job_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TenantIgnore
public class JobDO extends BaseDO {

    /**
     * 任务编号
     */
    @TableId
    private Long id;
    /**
     * 任务名称
     */
    private String name;
    /**
     * 任务状态
     *
     * 枚举 {@link JobStatusEnum}
     */
    private Integer status;
    /**
     * 处理器的名字
     */
    private String handlerName;
    /**
     * 处理器的参数
     */
    private String handlerParam;
    /**
     * CRON 表达式
     */
    private String cronExpression;

    // ========== 重试相关字段 ==========
    /**
     * 重试次数
     * 如果不重试，则设置为 0
     */
    private Integer retryCount;
    /**
     * 重试间隔，单位：毫秒
     * 如果没有间隔，则设置为 0
     */
    private Integer retryInterval;

    // ========== 监控相关字段 ==========
    /**
     * 监控超时时间，单位：毫秒
     * 为空时，表示不监控
     *
     * 注意，这里的超时的目的，不是进行任务的取消，而是告警任务的执行时间过长
     */
    private Integer monitorTimeout;

    // ========== 业务扩展字段 ==========
    /**
     * 业务类型
     * 
     * 枚举 {@link cn.iocoder.yudao.module.infra.enums.job.JobBusinessType}
     * 如：IOT_DEVICE_OFFLINE_CHECK, LIGHTING_SCHEDULE 等
     */
    private String businessType;
    
    /**
     * 业务模块
     * 
     * 如：iot, lighting, spatial, energy, security, system
     * 用于按业务模块进行任务管理和监控
     */
    private String businessModule;
    
    /**
     * 优先级
     * 
     * 范围：1-9，数字越小优先级越高
     * 1-关键任务，3-高优先级，5-普通优先级，7-低优先级，9-后台任务
     */
    private Integer priority;
    
    /**
     * 是否允许并发执行
     * 
     * true-允许多个实例同时执行
     * false-同一时间只能有一个实例执行
     */
    private Boolean concurrent;
    
    /**
     * 任务分组
     * 
     * 用于资源隔离，同一分组的任务共享资源池
     * 如：iot, lighting, spatial, energy, security, system
     */
    private String jobGroup;
    
    /**
     * 最大并发数
     * 
     * 当 concurrent=true 时，限制最多同时执行的实例数
     * 默认为 1
     */
    private Integer maxConcurrentCount;
    
    /**
     * 冲突策略
     * 
     * 枚举 {@link cn.iocoder.yudao.module.infra.enums.job.JobConflictStrategy}
     * SKIP-跳过，QUEUE-排队，INTERRUPT-中断，CONCURRENT-并发
     */
    private String conflictStrategy;
    
    /**
     * 依赖的任务ID列表
     * 
     * 格式：逗号分隔的任务ID，如："101,102,103"
     * 只有所有依赖任务都完成后，本任务才会执行
     */
    private String dependJobs;
    
    /**
     * 资源限制配置
     * 
     * JSON格式，如：{"maxMemory": 512, "maxCpu": 50, "maxDuration": 300000}
     * - maxMemory: 最大内存(MB)
     * - maxCpu: 最大CPU使用率(%)
     * - maxDuration: 最大执行时长(毫秒)
     */
    private String resourceLimit;

}
