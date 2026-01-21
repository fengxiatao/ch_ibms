package cn.iocoder.yudao.module.iot.service.patrolplan;

import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.task.PatrolTaskSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolTaskDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 轮巡任务 Service 接口
 *
 * @author 长辉信息
 */
public interface PatrolTaskService {

    /**
     * 创建轮巡任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPatrolTask(@Valid PatrolTaskSaveReqVO createReqVO);

    /**
     * 更新轮巡任务
     *
     * @param updateReqVO 更新信息
     */
    void updatePatrolTask(@Valid PatrolTaskSaveReqVO updateReqVO);

    /**
     * 删除轮巡任务
     *
     * @param id 编号
     */
    void deletePatrolTask(Long id);

    /**
     * 获得轮巡任务
     *
     * @param id 编号
     * @return 轮巡任务
     */
    IotVideoPatrolTaskDO getPatrolTask(Long id);

    /**
     * 获得指定计划下的任务列表
     *
     * @param planId 计划ID
     * @return 任务列表
     */
    List<IotVideoPatrolTaskDO> getPatrolTaskListByPlanId(Long planId);

    /**
     * 启动轮巡任务
     *
     * @param id 编号
     */
    void startPatrolTask(Long id);

    /**
     * 暂停轮巡任务
     *
     * @param id 编号
     */
    void pausePatrolTask(Long id);

    /**
     * 停止轮巡任务
     *
     * @param id 编号
     */
    void stopPatrolTask(Long id);

}
