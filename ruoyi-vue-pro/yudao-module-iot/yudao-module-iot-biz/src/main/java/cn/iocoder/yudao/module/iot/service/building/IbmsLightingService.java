package cn.iocoder.yudao.module.iot.service.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.*;

import java.util.List;

/**
 * 智能照明 Service 接口
 *
 * @author 智慧楼宇系统
 */
public interface IbmsLightingService {

    // ======================= 回路管理 =======================

    /**
     * 获得照明回路分页
     */
    PageResult<IbmsLightingCircuitDO> getCircuitPage(IbmsLightingCircuitPageReqVO pageReqVO);

    /**
     * 获得照明回路
     */
    IbmsLightingCircuitDO getCircuit(Long id);

    /**
     * 控制回路开关
     */
    void controlCircuit(Long id, Integer status, String operator);

    /**
     * 调节回路亮度
     */
    void dimCircuit(Long id, Integer brightness, String operator);

    // ======================= 场景管理 =======================

    /**
     * 获得照明场景分页
     */
    PageResult<IbmsLightingSceneDO> getScenePage(IbmsLightingScenePageReqVO pageReqVO);

    /**
     * 获得照明场景列表（简单列表）
     */
    List<IbmsLightingSceneDO> getSceneSimpleList();

    /**
     * 执行场景
     */
    void executeScene(Long id, String operator);

    // ======================= 定时任务 =======================

    /**
     * 获得定时任务分页
     */
    PageResult<IbmsLightingScheduleDO> getSchedulePage(IbmsLightingSchedulePageReqVO pageReqVO);

    /**
     * 启用/禁用定时任务
     */
    void updateScheduleEnabled(Long id, Boolean enabled);

    // ======================= 设备管理 =======================

    /**
     * 获得照明网关分页
     */
    PageResult<IbmsLightingGatewayDO> getGatewayPage(IbmsLightingDevicePageReqVO pageReqVO);

    /**
     * 获得照明控制器分页
     */
    PageResult<IbmsLightingControllerDO> getControllerPage(IbmsLightingDevicePageReqVO pageReqVO);

    // ======================= 操作日志 =======================

    /**
     * 获得操作日志分页
     */
    PageResult<IbmsLightingOperationLogDO> getOperationLogPage(IbmsLightingOperationLogPageReqVO pageReqVO);

    // ======================= 告警管理 =======================

    /**
     * 获得照明告警分页
     */
    PageResult<IbmsLightingAlarmDO> getAlarmPage(IbmsLightingAlarmPageReqVO pageReqVO);

    /**
     * 处理告警
     */
    void handleAlarm(Long id, String handler, String handleRemark);

    // ======================= 统计分析 =======================

    /**
     * 获得智能照明统计数据
     */
    IbmsLightingStatisticsVO getStatistics();

}
