package cn.iocoder.yudao.module.iot.service.patrolplan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.plan.PatrolPlanPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.plan.PatrolPlanSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolPlanDO;

import jakarta.validation.Valid;

/**
 * 轮巡计划 Service 接口
 *
 * @author 长辉信息
 */
public interface PatrolPlanService {

    /**
     * 创建轮巡计划
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPatrolPlan(@Valid PatrolPlanSaveReqVO createReqVO);

    /**
     * 更新轮巡计划
     *
     * @param updateReqVO 更新信息
     */
    void updatePatrolPlan(@Valid PatrolPlanSaveReqVO updateReqVO);

    /**
     * 删除轮巡计划
     *
     * @param id 编号
     */
    void deletePatrolPlan(Long id);

    /**
     * 获得轮巡计划
     *
     * @param id 编号
     * @return 轮巡计划
     */
    IotVideoPatrolPlanDO getPatrolPlan(Long id);

    /**
     * 获得轮巡计划分页
     *
     * @param pageReqVO 分页查询
     * @return 轮巡计划分页
     */
    PageResult<IotVideoPatrolPlanDO> getPatrolPlanPage(PatrolPlanPageReqVO pageReqVO);

    /**
     * 启动轮巡计划
     *
     * @param id 编号
     */
    void startPatrolPlan(Long id);

    /**
     * 暂停轮巡计划
     *
     * @param id 编号
     */
    void pausePatrolPlan(Long id);

    /**
     * 停止轮巡计划
     *
     * @param id 编号
     */
    void stopPatrolPlan(Long id);

}
