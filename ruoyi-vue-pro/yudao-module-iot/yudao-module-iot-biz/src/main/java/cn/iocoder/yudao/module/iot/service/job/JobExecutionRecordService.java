package cn.iocoder.yudao.module.iot.service.job;

import cn.iocoder.yudao.module.iot.job.unified.model.JobExecutionRecord;
import java.time.LocalDateTime;

/**
 * 任务执行记录 Service
 * 
 * @author IBMS Team
 */
public interface JobExecutionRecordService {
    
    /**
     * 获取最后一次执行记录
     * 
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param jobType 任务类型
     * @return 最后执行记录，如果不存在返回 null
     */
    JobExecutionRecord getLastRecord(String entityType, Long entityId, String jobType);
    
    /**
     * 记录任务执行结果
     * 
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param jobType 任务类型
     * @param lastExecuteTime 执行时间
     * @param nextExecuteTime 下次执行时间
     * @param executeStatus 执行状态: 1-成功 2-失败
     * @param executeResult 执行结果
     * @param executeDuration 执行时长（毫秒）
     */
    void recordExecution(String entityType, Long entityId, String jobType,
                        LocalDateTime lastExecuteTime, LocalDateTime nextExecuteTime,
                        Integer executeStatus, String executeResult, Integer executeDuration);
}






