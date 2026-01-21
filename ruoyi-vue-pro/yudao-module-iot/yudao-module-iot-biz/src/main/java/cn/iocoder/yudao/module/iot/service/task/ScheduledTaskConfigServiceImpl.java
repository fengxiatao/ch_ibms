package cn.iocoder.yudao.module.iot.service.task;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.task.vo.config.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.task.JobTypeDefinitionDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.task.ScheduledTaskConfigDO;
import cn.iocoder.yudao.module.iot.dal.mysql.task.JobTypeDefinitionMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.task.ScheduledTaskConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.TASK_CONFIG_NOT_EXISTS;

/**
 * 定时任务配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class ScheduledTaskConfigServiceImpl implements ScheduledTaskConfigService {

    @Resource
    private ScheduledTaskConfigMapper taskConfigMapper;

    @Resource
    private JobTypeDefinitionMapper jobTypeDefinitionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(ScheduledTaskConfigSaveReqVO createReqVO) {
        // 使用 Hutool 的 BeanUtil 进行对象拷贝，自动处理类型转换
        ScheduledTaskConfigDO task = BeanUtil.copyProperties(createReqVO, ScheduledTaskConfigDO.class);

        // 设置默认值
        if (task.getPriority() == null) {
            task.setPriority(5);
        }
        if (task.getFromProduct() == null) {
            task.setFromProduct(false);
        }
        task.setExecutionCount(0);
        task.setSuccessCount(0);
        task.setFailCount(0);

        // 计算下次执行时间
        if (Boolean.TRUE.equals(task.getEnabled())) {
            task.setNextExecutionTime(calculateNextExecutionTime(task));
        }

        taskConfigMapper.insert(task);
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(ScheduledTaskConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateTaskExists(updateReqVO.getId());

        // 打印调试信息
        log.info("[定时任务配置] 更新任务，接收到的enabled值: {}, 类型: {}",
                updateReqVO.getEnabled(),
                updateReqVO.getEnabled() != null ? updateReqVO.getEnabled().getClass().getName() : "null");

        // 使用 Hutool 的 BeanUtil 进行对象拷贝，自动处理类型转换
        ScheduledTaskConfigDO updateObj = BeanUtil.copyProperties(updateReqVO, ScheduledTaskConfigDO.class);

        // 重新计算下次执行时间
        if (Boolean.TRUE.equals(updateObj.getEnabled())) {
            updateObj.setNextExecutionTime(calculateNextExecutionTime(updateObj));
        }

        taskConfigMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        // 校验存在
        validateTaskExists(id);
        // 删除
        taskConfigMapper.deleteById(id);
    }

    private void validateTaskExists(Long id) {
        if (taskConfigMapper.selectById(id) == null) {
            throw exception(TASK_CONFIG_NOT_EXISTS);
        }
    }

    @Override
    public ScheduledTaskConfigDO getTask(Long id) {
        return taskConfigMapper.selectById(id);
    }

    @Override
    public List<ScheduledTaskConfigDO> getTaskListByEntity(String entityType, Long entityId) {
        return taskConfigMapper.selectListByEntity(entityType, entityId);
    }

    @Override
    public PageResult<TaskMonitorRespVO> getTaskMonitorPage(TaskMonitorPageReqVO pageReqVO) {
        // 查询分页
        PageResult<ScheduledTaskConfigDO> pageResult = taskConfigMapper.selectPage(pageReqVO,
                new LambdaQueryWrapperX<ScheduledTaskConfigDO>()
                        .eqIfPresent(ScheduledTaskConfigDO::getEntityType, pageReqVO.getEntityType())
                        .eqIfPresent(ScheduledTaskConfigDO::getJobType, pageReqVO.getJobType())
                        .eqIfPresent(ScheduledTaskConfigDO::getEnabled, pageReqVO.getEnabled())
                        .like(pageReqVO.getKeyword() != null, ScheduledTaskConfigDO::getEntityName, pageReqVO.getKeyword())
                        .or(pageReqVO.getKeyword() != null, query -> query.like(ScheduledTaskConfigDO::getJobName, pageReqVO.getKeyword()))
                        .orderByDesc(ScheduledTaskConfigDO::getCreateTime));

        // 转换VO
        return new PageResult<>(convertToMonitorVO(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public TaskStatisticsRespVO getStatistics() {
        TaskStatisticsRespVO statistics = new TaskStatisticsRespVO();
        statistics.setTotal(taskConfigMapper.selectCount());
        statistics.setEnabled(taskConfigMapper.countEnabled());
        statistics.setDisabled(taskConfigMapper.countDisabled());

        // 运行中的任务数（上次执行状态为RUNNING）
        statistics.setRunning(taskConfigMapper.selectCount(new LambdaQueryWrapperX<ScheduledTaskConfigDO>()
                .eq(ScheduledTaskConfigDO::getLastExecutionStatus, "RUNNING")));

        // 这些数据需要从执行日志表统计，这里先返回0
        statistics.setRecentSuccess(0L);
        statistics.setRecentFailed(0L);

        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTask(Long id, Boolean enabled) {
        // 校验存在
        ScheduledTaskConfigDO task = validateTaskExists2(id);

        // 更新状态
        ScheduledTaskConfigDO updateObj = new ScheduledTaskConfigDO();
        updateObj.setId(id);
        updateObj.setEnabled(enabled);

        // 如果启用，计算下次执行时间
        if (enabled) {
            updateObj.setNextExecutionTime(calculateNextExecutionTime(task));
        } else {
            updateObj.setNextExecutionTime(null);
        }

        taskConfigMapper.updateById(updateObj);
    }

    @Override
    public void executeTaskNow(Long id) {
        // 这个方法需要触发立即执行，具体实现需要与调度器集成
        log.info("立即执行任务: {}", id);
        // TODO: 调用调度器立即执行该任务
    }

    @Override
    public List<ScheduledTaskConfigDO> getEnabledTasks() {
        return taskConfigMapper.selectEnabledList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateExecutionStatus(Long id, String status, LocalDateTime nextExecutionTime) {
        ScheduledTaskConfigDO updateObj = new ScheduledTaskConfigDO();
        updateObj.setId(id);
        updateObj.setLastExecutionStatus(status);
        updateObj.setLastExecutionTime(LocalDateTime.now());
        updateObj.setNextExecutionTime(nextExecutionTime);
        taskConfigMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateExecutionStatistics(Long id, boolean success, int durationMs) {
        ScheduledTaskConfigDO task = taskConfigMapper.selectById(id);
        if (task == null) {
            return;
        }

        // 更新统计数据
        ScheduledTaskConfigDO updateObj = new ScheduledTaskConfigDO();
        updateObj.setId(id);
        updateObj.setExecutionCount(task.getExecutionCount() + 1);

        if (success) {
            updateObj.setSuccessCount(task.getSuccessCount() + 1);
        } else {
            updateObj.setFailCount(task.getFailCount() + 1);
        }

        // 计算平均耗时
        int totalDuration = (task.getAvgDurationMs() != null ? task.getAvgDurationMs() : 0) * task.getExecutionCount() + durationMs;
        updateObj.setAvgDurationMs(totalDuration / (task.getExecutionCount() + 1));

        taskConfigMapper.updateById(updateObj);
    }

    private ScheduledTaskConfigDO validateTaskExists2(Long id) {
        ScheduledTaskConfigDO task = taskConfigMapper.selectById(id);
        if (task == null) {
            throw exception(TASK_CONFIG_NOT_EXISTS);
        }
        return task;
    }

    private LocalDateTime calculateNextExecutionTime(ScheduledTaskConfigDO task) {
        // 简单实现：如果有间隔时间，就在当前时间基础上加上间隔
        if (task.getIntervalSeconds() != null && task.getIntervalSeconds() > 0) {
            return LocalDateTime.now().plusSeconds(task.getIntervalSeconds());
        }
        // TODO: 如果有Cron表达式，需要使用Cron解析器计算下次执行时间
        return LocalDateTime.now().plusMinutes(10); // 默认10分钟后
    }

    private List<TaskMonitorRespVO> convertToMonitorVO(List<ScheduledTaskConfigDO> list) {
        return list.stream().map(task -> {
            TaskMonitorRespVO vo = new TaskMonitorRespVO();
            vo.setId(task.getId());
            vo.setEntityType(task.getEntityType());
            vo.setEntityId(task.getEntityId());
            vo.setEntityName(task.getEntityName());
            vo.setJobType(task.getJobType());
            vo.setJobName(task.getJobName());
            vo.setEnabled(task.getEnabled());
            vo.setCronExpression(task.getCronExpression());
            vo.setIntervalSeconds(task.getIntervalSeconds());
            vo.setPriority(task.getPriority());
            vo.setLastExecutionTime(task.getLastExecutionTime());
            vo.setLastExecutionStatus(task.getLastExecutionStatus());
            vo.setExecutionCount(task.getExecutionCount());
            vo.setSuccessCount(task.getSuccessCount());
            vo.setFailCount(task.getFailCount());
            vo.setAvgDurationMs(task.getAvgDurationMs());
            vo.setNextExecutionTime(task.getNextExecutionTime());
            vo.setCreateTime(task.getCreateTime());
            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }

}


