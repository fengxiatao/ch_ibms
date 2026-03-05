package cn.iocoder.yudao.module.iot.service.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.env.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnvAlarmDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnvDataRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnvSensorDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 环境监测 Service 接口
 *
 * @author 智慧楼宇系统
 */
public interface IbmsEnvService {

    // ======================= 传感器管理 =======================

    /**
     * 创建环境传感器
     */
    Long createEnvSensor(@Valid IbmsEnvSensorSaveReqVO createReqVO);

    /**
     * 更新环境传感器
     */
    void updateEnvSensor(@Valid IbmsEnvSensorSaveReqVO updateReqVO);

    /**
     * 删除环境传感器
     */
    void deleteEnvSensor(Long id);

    /**
     * 获得环境传感器
     */
    IbmsEnvSensorDO getEnvSensor(Long id);

    /**
     * 获得环境传感器分页
     */
    PageResult<IbmsEnvSensorDO> getEnvSensorPage(IbmsEnvSensorPageReqVO pageReqVO);

    /**
     * 获得环境传感器列表
     */
    List<IbmsEnvSensorDO> getEnvSensorList(IbmsEnvSensorPageReqVO reqVO);

    // ======================= 数据记录 =======================

    /**
     * 获得环境数据记录分页
     */
    PageResult<IbmsEnvDataRecordDO> getEnvDataRecordPage(IbmsEnvDataRecordPageReqVO pageReqVO);

    /**
     * 获得传感器最新数据
     */
    IbmsEnvDataRecordDO getLatestEnvDataRecord(Long sensorId);

    /**
     * 获得传感器历史数据（指定数量）
     */
    List<IbmsEnvDataRecordDO> getEnvDataRecordHistory(Long sensorId, int limit);

    // ======================= 告警管理 =======================

    /**
     * 获得环境告警分页
     */
    PageResult<IbmsEnvAlarmDO> getEnvAlarmPage(IbmsEnvAlarmPageReqVO pageReqVO);

    /**
     * 处理环境告警
     */
    void handleEnvAlarm(Long id, String handler, String handleRemark);

    /**
     * 忽略环境告警
     */
    void ignoreEnvAlarm(Long id, String handler, String handleRemark);

    // ======================= 统计分析 =======================

    /**
     * 获得环境监测统计数据
     */
    IbmsEnvStatisticsVO getEnvStatistics();

}
