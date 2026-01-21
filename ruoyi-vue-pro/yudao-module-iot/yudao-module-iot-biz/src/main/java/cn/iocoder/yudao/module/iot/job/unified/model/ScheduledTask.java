package cn.iocoder.yudao.module.iot.job.unified.model;

import cn.iocoder.yudao.module.iot.job.unified.config.JobTaskConfig;
import lombok.Builder;
import lombok.Data;

/**
 * 待执行的任务
 * 
 * @author IBMS Team
 */
@Data
@Builder
public class ScheduledTask {
    
    /**
     * 实体类型: PRODUCT / DEVICE / SYSTEM / FACILITY
     */
    private String entityType;
    
    /**
     * 实体ID
     */
    private Long entityId;
    
    /**
     * 任务类型: offlineCheck / healthCheck / dataCollect
     */
    private String jobType;
    
    /**
     * 任务配置
     */
    private JobTaskConfig config;
    
    /**
     * 优先级
     */
    private int priority;
    
    /**
     * 是否来自产品配置（用于区分设备任务来源）
     * true: 来自产品配置，继承给设备
     * false: 设备自己的配置
     */
    @Builder.Default
    private boolean fromProduct = false;
}

