package cn.iocoder.yudao.module.iot.service.epatrol;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPlanPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPlanRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPlanSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolPlanDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 电子巡更 - 巡更计划 Service 接口
 *
 * @author 长辉信息
 */
public interface EpatrolPlanService {

    /**
     * 创建巡更计划
     */
    Long createPlan(@Valid EpatrolPlanSaveReqVO createReqVO);

    /**
     * 更新巡更计划
     */
    void updatePlan(@Valid EpatrolPlanSaveReqVO updateReqVO);

    /**
     * 删除巡更计划
     */
    void deletePlan(Long id);

    /**
     * 获得巡更计划
     */
    EpatrolPlanDO getPlan(Long id);

    /**
     * 获得巡更计划详情（包含时段列表）
     */
    EpatrolPlanRespVO getPlanDetail(Long id);

    /**
     * 获得巡更计划分页
     */
    PageResult<EpatrolPlanDO> getPlanPage(EpatrolPlanPageReqVO pageReqVO);

    /**
     * 获得所有执行中的计划
     */
    List<EpatrolPlanDO> getActivePlanList();

    /**
     * 更新巡更计划状态
     */
    void updatePlanStatus(Long id, Integer status);

    /**
     * 生成每日任务（定时任务调用）
     */
    void generateDailyTasks();

}
