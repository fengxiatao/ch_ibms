package cn.iocoder.yudao.module.iot.service.job;

import cn.iocoder.yudao.module.iot.dal.dataobject.job.JobExecutionRecordDO;
import cn.iocoder.yudao.module.iot.dal.mysql.job.JobExecutionRecordMapper;
import cn.iocoder.yudao.module.iot.job.unified.model.JobExecutionRecord;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 任务执行记录 Service 实现
 * 
 * @author IBMS Team
 */
@Service
@Slf4j
public class JobExecutionRecordServiceImpl implements JobExecutionRecordService {
    
    @Resource
    private JobExecutionRecordMapper jobExecutionRecordMapper;
    
    @Override
    public JobExecutionRecord getLastRecord(String entityType, Long entityId, String jobType) {
        LambdaQueryWrapper<JobExecutionRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobExecutionRecordDO::getEntityType, entityType)
               .eq(JobExecutionRecordDO::getEntityId, entityId)
               .eq(JobExecutionRecordDO::getJobType, jobType)
               .orderByDesc(JobExecutionRecordDO::getLastExecuteTime)
               .last("LIMIT 1");
        
        JobExecutionRecordDO recordDO = jobExecutionRecordMapper.selectOne(wrapper);
        
        if (recordDO == null) {
            return null;
        }
        
        // 转换为模型
        JobExecutionRecord record = new JobExecutionRecord();
        record.setId(recordDO.getId());
        record.setEntityType(recordDO.getEntityType());
        record.setEntityId(recordDO.getEntityId());
        record.setJobType(recordDO.getJobType());
        record.setLastExecuteTime(recordDO.getLastExecuteTime());
        record.setNextExecuteTime(recordDO.getNextExecuteTime());
        record.setExecuteStatus(recordDO.getExecuteStatus());
        record.setExecuteResult(recordDO.getExecuteResult());
        record.setExecuteDuration(recordDO.getExecuteDuration());
        
        return record;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordExecution(String entityType, Long entityId, String jobType,
                               LocalDateTime lastExecuteTime, LocalDateTime nextExecuteTime,
                               Integer executeStatus, String executeResult, Integer executeDuration) {
        JobExecutionRecordDO record = new JobExecutionRecordDO();
        record.setEntityType(entityType);
        record.setEntityId(entityId);
        record.setJobType(jobType);
        record.setLastExecuteTime(lastExecuteTime);
        record.setNextExecuteTime(nextExecuteTime);
        record.setExecuteStatus(executeStatus);
        record.setExecuteResult(executeResult);
        record.setExecuteDuration(executeDuration);
        
        jobExecutionRecordMapper.insert(record);
        
        log.debug("记录任务执行: entityType={}, entityId={}, jobType={}, status={}", 
                 entityType, entityId, jobType, executeStatus);
    }
}






