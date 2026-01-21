package cn.iocoder.yudao.module.iot.job.unified.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务执行记录
 * 
 * @author IBMS Team
 */
@Data
public class JobExecutionRecord {
    
    private Long id;
    
    /**
     * 实体类型
     */
    private String entityType;
    
    /**
     * 实体ID
     */
    private Long entityId;
    
    /**
     * 任务类型
     */
    private String jobType;
    
    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecuteTime;
    
    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecuteTime;
    
    /**
     * 执行状态: 1-成功 2-失败
     */
    private Integer executeStatus;
    
    /**
     * 执行结果
     */
    private String executeResult;
    
    /**
     * 执行时长（毫秒）
     */
    private Integer executeDuration;
}






