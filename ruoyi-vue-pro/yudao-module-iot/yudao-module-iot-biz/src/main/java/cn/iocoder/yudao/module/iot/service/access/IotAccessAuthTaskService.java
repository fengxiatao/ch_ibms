package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.authtask.IotAccessAuthTaskPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessAuthTaskDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessAuthTaskDetailDO;

import java.util.List;

/**
 * 门禁授权任务 Service 接口
 * 
 * 负责授权任务的创建、查询、状态更新等操作
 * 
 * Requirements: 1.1, 1.2, 6.1, 6.3
 *
 * @author 芋道源码
 */
public interface IotAccessAuthTaskService {

    // ========== 任务管理 ==========

    /**
     * 创建授权任务
     *
     * @param taskType 任务类型（group_dispatch/person_dispatch/revoke）
     * @param groupId  权限组ID（权限组下发时必填）
     * @param personId 人员ID（单人下发/撤销时必填）
     * @return 任务ID
     */
    Long createTask(String taskType, Long groupId, Long personId);

    /**
     * 获取任务
     *
     * @param id 任务ID
     * @return 任务信息
     */
    IotAccessAuthTaskDO getTask(Long id);

    /**
     * 获取任务分页
     *
     * @param reqVO 分页查询参数
     * @return 任务分页
     */
    PageResult<IotAccessAuthTaskDO> getTaskPage(IotAccessAuthTaskPageReqVO reqVO);

    /**
     * 更新任务状态为执行中
     *
     * @param taskId 任务ID
     */
    void startTask(Long taskId);

    /**
     * 更新任务状态（增量更新成功/失败计数）
     *
     * @param taskId       任务ID
     * @param status       状态
     * @param successCount 成功数量（增量）
     * @param failCount    失败数量（增量）
     */
    void updateTaskStatus(Long taskId, Integer status, Integer successCount, Integer failCount);

    /**
     * 完成任务（根据成功/失败计数自动确定最终状态）
     *
     * @param taskId       任务ID
     * @param errorMessage 错误信息（可选）
     */
    void completeTask(Long taskId, String errorMessage);

    // ========== 任务明细管理 ==========

    /**
     * 批量添加任务明细
     *
     * @param taskId  任务ID
     * @param details 任务明细列表
     */
    void addTaskDetails(Long taskId, List<IotAccessAuthTaskDetailDO> details);

    /**
     * 获取任务明细列表
     *
     * @param taskId 任务ID
     * @return 任务明细列表
     */
    List<IotAccessAuthTaskDetailDO> getTaskDetails(Long taskId);

    /**
     * 获取失败的任务明细列表
     *
     * @param taskId 任务ID
     * @return 失败的任务明细列表
     */
    List<IotAccessAuthTaskDetailDO> getFailedTaskDetails(Long taskId);

    /**
     * 更新任务明细状态
     *
     * @param detailId        明细ID
     * @param status          状态
     * @param errorMessage    错误信息
     * @param credentialTypes 下发的凭证类型
     */
    void updateTaskDetailStatus(Long detailId, Integer status, String errorMessage, String credentialTypes);

    /**
     * 统计任务明细数量
     *
     * @param taskId 任务ID
     * @return 明细总数
     */
    long countTaskDetails(Long taskId);

    /**
     * 统计指定状态的任务明细数量
     *
     * @param taskId 任务ID
     * @param status 状态
     * @return 明细数量
     */
    long countTaskDetailsByStatus(Long taskId, Integer status);

    // ========== 重试功能 ==========

    /**
     * 重试失败的任务（只重新下发失败的明细）
     *
     * @param taskId 任务ID
     * @return 新创建的重试任务ID
     */
    Long retryTask(Long taskId);

    // ========== 兼容旧接口 ==========

    /**
     * 创建授权任务（兼容旧接口）
     * @deprecated 使用 {@link #createTask(String, Long, Long)} 代替
     */
    @Deprecated
    default Long createAuthTask(IotAccessAuthTaskDO task) {
        return createTask(task.getTaskType(), task.getGroupId(), task.getPersonId());
    }

    /**
     * 获取任务（兼容旧接口）
     * @deprecated 使用 {@link #getTask(Long)} 代替
     */
    @Deprecated
    default IotAccessAuthTaskDO getAuthTask(Long id) {
        return getTask(id);
    }
    
    /**
     * 为指定人员创建授权任务
     * 
     * @param personId 人员ID
     * @return 任务ID
     */
    default Long createAuthTaskForPerson(Long personId) {
        return createTask("person_dispatch", null, personId);
    }

}
