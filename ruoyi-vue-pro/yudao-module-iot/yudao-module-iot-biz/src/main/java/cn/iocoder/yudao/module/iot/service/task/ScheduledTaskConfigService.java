package cn.iocoder.yudao.module.iot.service.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.task.vo.config.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.task.ScheduledTaskConfigDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务配置 Service 接口
 *
 * @author 芋道源码
 */
public interface ScheduledTaskConfigService {

    /**
     * 创建任务配置
     */
    Long createTask(ScheduledTaskConfigSaveReqVO createReqVO);

    /**
     * 更新任务配置
     */
    void updateTask(ScheduledTaskConfigSaveReqVO updateReqVO);

    /**
     * 删除任务配置
     */
    void deleteTask(Long id);

    /**
     * 获取任务配置
     */
    ScheduledTaskConfigDO getTask(Long id);

    /**
     * 获取实体的任务配置列表
     */
    List<ScheduledTaskConfigDO> getTaskListByEntity(String entityType, Long entityId);

    /**
     * 获取任务监控列表（分页）
     */
    PageResult<TaskMonitorRespVO> getTaskMonitorPage(TaskMonitorPageReqVO pageReqVO);

    /**
     * 获取统计数据
     */
    TaskStatisticsRespVO getStatistics();

    /**
     * 启用/禁用任务
     */
    void toggleTask(Long id, Boolean enabled);

    /**
     * 立即执行任务
     */
    void executeTaskNow(Long id);

    /**
     * 获取所有启用的任务配置（供调度器使用）
     */
    List<ScheduledTaskConfigDO> getEnabledTasks();

    /**
     * 更新任务执行状态
     */
    void updateExecutionStatus(Long id, String status, LocalDateTime nextExecutionTime);

    /**
     * 更新任务执行统计
     */
    void updateExecutionStatistics(Long id, boolean success, int durationMs);

}




