package cn.iocoder.yudao.module.iot.service.epatrol;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolTaskPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolTaskRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolTaskStatisticsVO;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolTaskSubmitReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolTaskDO;

import jakarta.validation.Valid;

/**
 * 电子巡更 - 巡更任务 Service 接口
 *
 * @author 长辉信息
 */
public interface EpatrolTaskService {

    /**
     * 获得巡更任务
     */
    EpatrolTaskDO getTask(Long id);

    /**
     * 获得巡更任务详情（包含记录列表）
     */
    EpatrolTaskRespVO getTaskDetail(Long id);

    /**
     * 获得巡更任务分页
     */
    PageResult<EpatrolTaskDO> getTaskPage(EpatrolTaskPageReqVO pageReqVO);

    /**
     * 获得巡更任务分页（包含详细信息：计划名称、路线名称、人员名称）
     */
    PageResult<EpatrolTaskRespVO> getTaskPageWithDetails(EpatrolTaskPageReqVO pageReqVO);

    /**
     * 提交巡更结果
     */
    void submitTask(@Valid EpatrolTaskSubmitReqVO submitReqVO);

    /**
     * 获取任务统计信息
     */
    EpatrolTaskStatisticsVO getTaskStatistics(EpatrolTaskPageReqVO reqVO);

}
