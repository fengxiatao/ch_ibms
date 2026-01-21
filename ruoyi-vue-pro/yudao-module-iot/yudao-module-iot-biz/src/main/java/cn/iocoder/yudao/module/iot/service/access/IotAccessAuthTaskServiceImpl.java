package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.authtask.IotAccessAuthTaskPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessAuthTaskDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessAuthTaskDetailDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessAuthTaskDetailMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessAuthTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 门禁授权任务 Service 实现类
 * 
 * Requirements: 1.1, 1.2, 6.1, 6.3
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class IotAccessAuthTaskServiceImpl implements IotAccessAuthTaskService {

    /**
     * 默认批次大小
     */
    private static final int DEFAULT_BATCH_SIZE = 50;

    @Resource
    private IotAccessAuthTaskMapper taskMapper;

    @Resource
    private IotAccessAuthTaskDetailMapper taskDetailMapper;

    // ========== 任务管理 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(String taskType, Long groupId, Long personId) {
        // 参数校验
        validateTaskParams(taskType, groupId, personId);
        
        // 创建任务
        IotAccessAuthTaskDO task = IotAccessAuthTaskDO.builder()
                .taskType(taskType)
                .groupId(groupId)
                .personId(personId)
                .totalCount(0)
                .successCount(0)
                .failCount(0)
                .status(IotAccessAuthTaskDO.STATUS_PENDING)
                .build();
        taskMapper.insert(task);
        
        log.info("[createTask] 创建授权任务: taskId={}, taskType={}, groupId={}, personId={}", 
                task.getId(), taskType, groupId, personId);
        return task.getId();
    }

    /**
     * 校验任务参数
     */
    private void validateTaskParams(String taskType, Long groupId, Long personId) {
        if (IotAccessAuthTaskDO.TASK_TYPE_GROUP_DISPATCH.equals(taskType)) {
            if (groupId == null) {
                throw exception(ACCESS_AUTH_TASK_GROUP_ID_REQUIRED);
            }
        } else if (IotAccessAuthTaskDO.TASK_TYPE_PERSON_DISPATCH.equals(taskType)) {
            // 单人下发必须指定人员ID
            if (personId == null) {
                throw exception(ACCESS_AUTH_TASK_PERSON_ID_REQUIRED);
            }
        } else if (IotAccessAuthTaskDO.TASK_TYPE_REVOKE.equals(taskType)) {
            // 撤销任务：单人撤销需要personId，权限组批量撤销需要groupId
            if (personId == null && groupId == null) {
                throw exception(ACCESS_AUTH_TASK_PERSON_ID_REQUIRED);
            }
        }
    }

    @Override
    public IotAccessAuthTaskDO getTask(Long id) {
        return taskMapper.selectById(id);
    }

    @Override
    public PageResult<IotAccessAuthTaskDO> getTaskPage(IotAccessAuthTaskPageReqVO reqVO) {
        return taskMapper.selectPage(reqVO);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void startTask(Long taskId) {
        IotAccessAuthTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw exception(ACCESS_AUTH_TASK_NOT_EXISTS);
        }
        
        // 只有待执行状态的任务可以开始
        if (task.getStatus() != IotAccessAuthTaskDO.STATUS_PENDING) {
            throw exception(ACCESS_AUTH_TASK_STATUS_INVALID);
        }
        
        IotAccessAuthTaskDO updateTask = new IotAccessAuthTaskDO();
        updateTask.setId(taskId);
        updateTask.setStatus(IotAccessAuthTaskDO.STATUS_RUNNING);
        updateTask.setStartTime(LocalDateTime.now());
        taskMapper.updateById(updateTask);
        
        log.info("[startTask] 任务开始执行: taskId={}", taskId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateTaskStatus(Long taskId, Integer status, Integer successCount, Integer failCount) {
        // 使用 REQUIRES_NEW 确保独立事务，避免与外层事务锁竞争
        updateTaskStatusWithRetry(taskId, status, successCount, failCount, 3);
    }
    
    /**
     * 带重试的任务状态更新
     */
    private void updateTaskStatusWithRetry(Long taskId, Integer status, Integer successCount, Integer failCount, int maxRetries) {
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                IotAccessAuthTaskDO task = taskMapper.selectById(taskId);
                if (task == null) {
                    log.warn("[updateTaskStatus] 任务不存在: taskId={}", taskId);
                    return;
                }
                
                IotAccessAuthTaskDO updateTask = new IotAccessAuthTaskDO();
                updateTask.setId(taskId);
                if (status != null) {
                    updateTask.setStatus(status);
                }
                // 增量更新成功/失败计数
                if (successCount != null && successCount > 0) {
                    updateTask.setSuccessCount(task.getSuccessCount() + successCount);
                }
                if (failCount != null && failCount > 0) {
                    updateTask.setFailCount(task.getFailCount() + failCount);
                }
                taskMapper.updateById(updateTask);
                return; // 成功则退出
            } catch (org.springframework.dao.CannotAcquireLockException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    log.warn("[updateTaskStatus] 锁等待超时，已重试{}次，放弃: taskId={}", maxRetries, taskId);
                    // 不再抛异常，降级处理
                    return;
                }
                log.warn("[updateTaskStatus] 锁等待超时，第{}次重试: taskId={}", retryCount, taskId);
                try {
                    Thread.sleep(100 * retryCount); // 短暂退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void completeTask(Long taskId, String errorMessage) {
        // 使用 REQUIRES_NEW 确保独立事务，避免与外层事务锁竞争
        completeTaskWithRetry(taskId, errorMessage, 3);
    }
    
    /**
     * 带重试的任务完成
     */
    private void completeTaskWithRetry(Long taskId, String errorMessage, int maxRetries) {
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                IotAccessAuthTaskDO task = taskMapper.selectById(taskId);
                if (task == null) {
                    log.warn("[completeTask] 任务不存在: taskId={}", taskId);
                    return;
                }
                
                IotAccessAuthTaskDO updateTask = new IotAccessAuthTaskDO();
                updateTask.setId(taskId);
                updateTask.setEndTime(LocalDateTime.now());
                updateTask.setErrorMessage(errorMessage);
                
                // 根据成功/失败计数确定最终状态
                int finalStatus = task.calculateFinalStatus();
                updateTask.setStatus(finalStatus);
                
                taskMapper.updateById(updateTask);
                
                log.info("[completeTask] 任务完成: taskId={}, status={}, successCount={}, failCount={}", 
                        taskId, finalStatus, task.getSuccessCount(), task.getFailCount());
                return; // 成功则退出
            } catch (org.springframework.dao.CannotAcquireLockException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    log.warn("[completeTask] 锁等待超时，已重试{}次，放弃: taskId={}", maxRetries, taskId);
                    return;
                }
                log.warn("[completeTask] 锁等待超时，第{}次重试: taskId={}", retryCount, taskId);
                try {
                    Thread.sleep(100 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    // ========== 任务明细管理 ==========

    @Override
    public void addTaskDetails(Long taskId, List<IotAccessAuthTaskDetailDO> details) {
        if (details == null || details.isEmpty()) {
            return;
        }
        
        // 设置任务ID和初始状态
        for (IotAccessAuthTaskDetailDO detail : details) {
            detail.setTaskId(taskId);
            detail.setStatus(IotAccessAuthTaskDetailDO.STATUS_PENDING);
            detail.setRetryCount(0);
        }
        
        // 分批插入，每批使用独立事务
        int successBatches = 0;
        int failBatches = 0;
        
        for (int i = 0; i < details.size(); i += DEFAULT_BATCH_SIZE) {
            int endIndex = Math.min(i + DEFAULT_BATCH_SIZE, details.size());
            List<IotAccessAuthTaskDetailDO> batch = details.subList(i, endIndex);
            
            try {
                insertBatchInNewTransaction(batch);
                successBatches++;
            } catch (Exception e) {
                failBatches++;
                log.error("[addTaskDetails] 批次插入失败: taskId={}, batchIndex={}, error={}", 
                        taskId, i / DEFAULT_BATCH_SIZE, e.getMessage());
                // 继续处理下一批
            }
        }
        
        // 更新任务总数
        IotAccessAuthTaskDO updateTask = new IotAccessAuthTaskDO();
        updateTask.setId(taskId);
        updateTask.setTotalCount(details.size());
        taskMapper.updateById(updateTask);
        
        log.info("[addTaskDetails] 添加任务明细: taskId={}, count={}, successBatches={}, failBatches={}", 
                taskId, details.size(), successBatches, failBatches);
    }

    /**
     * 在新事务中批量插入明细
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void insertBatchInNewTransaction(List<IotAccessAuthTaskDetailDO> batch) {
        taskDetailMapper.insertBatch(batch);
    }

    @Override
    public List<IotAccessAuthTaskDetailDO> getTaskDetails(Long taskId) {
        return taskDetailMapper.selectListByTaskId(taskId);
    }

    @Override
    public List<IotAccessAuthTaskDetailDO> getFailedTaskDetails(Long taskId) {
        return taskDetailMapper.selectFailedByTaskId(taskId);
    }

    @Override
    public void updateTaskDetailStatus(Long detailId, Integer status, String errorMessage, String credentialTypes) {
        IotAccessAuthTaskDetailDO updateDetail = new IotAccessAuthTaskDetailDO();
        updateDetail.setId(detailId);
        updateDetail.setStatus(status);
        updateDetail.setErrorMessage(errorMessage);
        updateDetail.setCredentialTypes(credentialTypes);
        updateDetail.setExecuteTime(LocalDateTime.now());
        taskDetailMapper.updateById(updateDetail);
    }

    @Override
    public long countTaskDetails(Long taskId) {
        return taskDetailMapper.selectCountByTaskId(taskId);
    }

    @Override
    public long countTaskDetailsByStatus(Long taskId, Integer status) {
        return taskDetailMapper.selectCountByTaskIdAndStatus(taskId, status);
    }

    // ========== 重试功能 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long retryTask(Long taskId) {
        IotAccessAuthTaskDO task = validateTaskExists(taskId);
        
        // 只有部分失败或全部失败的任务可以重试
        if (task.getStatus() != IotAccessAuthTaskDO.STATUS_PARTIAL_FAILED 
                && task.getStatus() != IotAccessAuthTaskDO.STATUS_ALL_FAILED) {
            throw exception(ACCESS_AUTH_TASK_CANNOT_RETRY);
        }
        
        // 获取失败的明细
        List<IotAccessAuthTaskDetailDO> failedDetails = getFailedTaskDetails(taskId);
        if (failedDetails.isEmpty()) {
            throw exception(ACCESS_AUTH_TASK_NO_FAILED_DETAILS);
        }
        
        // 创建新的重试任务
        Long newTaskId = createTask(task.getTaskType(), task.getGroupId(), task.getPersonId());
        
        // 复制失败的明细到新任务
        for (IotAccessAuthTaskDetailDO detail : failedDetails) {
            IotAccessAuthTaskDetailDO newDetail = IotAccessAuthTaskDetailDO.builder()
                    .taskId(newTaskId)
                    .personId(detail.getPersonId())
                    .personCode(detail.getPersonCode())
                    .personName(detail.getPersonName())
                    .deviceId(detail.getDeviceId())
                    .deviceName(detail.getDeviceName())
                    .channelId(detail.getChannelId())
                    .status(IotAccessAuthTaskDetailDO.STATUS_PENDING)
                    .build();
            taskDetailMapper.insert(newDetail);
        }
        
        // 更新新任务的总数
        IotAccessAuthTaskDO updateTask = new IotAccessAuthTaskDO();
        updateTask.setId(newTaskId);
        updateTask.setTotalCount(failedDetails.size());
        taskMapper.updateById(updateTask);
        
        log.info("[retryTask] 创建重试任务: originalTaskId={}, newTaskId={}, failedCount={}", 
                taskId, newTaskId, failedDetails.size());
        
        return newTaskId;
    }

    // ========== 私有方法 ==========

    /**
     * 校验任务存在
     */
    private IotAccessAuthTaskDO validateTaskExists(Long taskId) {
        IotAccessAuthTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw exception(ACCESS_AUTH_TASK_NOT_EXISTS);
        }
        return task;
    }

}
