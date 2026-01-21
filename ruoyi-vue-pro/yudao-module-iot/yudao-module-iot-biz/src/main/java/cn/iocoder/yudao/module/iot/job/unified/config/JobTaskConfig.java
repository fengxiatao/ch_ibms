package cn.iocoder.yudao.module.iot.job.unified.config;

import lombok.Data;

/**
 * 单个任务配置
 * 
 * @author IBMS Team
 */
@Data
public class JobTaskConfig {
    
    /**
     * 是否启用
     */
    private boolean enabled;
    
    /**
     * 执行间隔
     */
    private int interval;
    
    /**
     * 时间单位: MINUTE / HOUR
     */
    private String unit;
    
    /**
     * 优先级 (1-9)
     */
    private int priority = 5;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 离线时是否通知
     */
    private Boolean notifyOnOffline;
}






