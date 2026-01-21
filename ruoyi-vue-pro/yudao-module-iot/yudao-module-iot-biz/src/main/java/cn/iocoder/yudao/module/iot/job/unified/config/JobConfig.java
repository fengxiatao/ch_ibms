package cn.iocoder.yudao.module.iot.job.unified.config;

import lombok.Data;

/**
 * 任务配置（对应 jobConfig JSON 字段）
 * 
 * @author IBMS Team
 */
@Data
public class JobConfig {
    
    /**
     * 离线检查配置
     */
    private JobTaskConfig offlineCheck;
    
    /**
     * 健康检查配置
     */
    private JobTaskConfig healthCheck;
    
    /**
     * 数据采集配置
     */
    private JobTaskConfig dataCollect;
    
    // ========== 空间设施任务 ==========
    
    /**
     * 设备统计任务配置（园区）
     */
    private JobTaskConfig deviceStatistics;
    
    /**
     * 能耗统计任务配置（建筑）
     */
    private JobTaskConfig energyStatistics;
    
    /**
     * 环境监测任务配置（楼层）
     */
    private JobTaskConfig envMonitor;
    
    /**
     * 设备巡检任务配置（区域）
     */
    private JobTaskConfig deviceInspection;
}

