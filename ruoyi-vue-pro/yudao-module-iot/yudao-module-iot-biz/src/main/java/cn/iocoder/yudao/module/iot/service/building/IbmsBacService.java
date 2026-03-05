package cn.iocoder.yudao.module.iot.service.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.bac.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.*;

import java.util.List;

/**
 * 楼宇自控 Service 接口
 *
 * @author 智慧楼宇系统
 */
public interface IbmsBacService {

    // ======================= 暖通设备管理 =======================

    /**
     * 获得暖通设备分页
     */
    PageResult<IbmsHvacDeviceDO> getHvacDevicePage(IbmsHvacDevicePageReqVO pageReqVO);

    /**
     * 获得暖通设备
     */
    IbmsHvacDeviceDO getHvacDevice(Long id);

    /**
     * 控制暖通设备启停
     */
    void controlHvacDevice(Long id, Integer runningStatus, String operator);

    /**
     * 设置暖通设备参数
     */
    void setHvacDeviceParams(Long id, Integer runMode, java.math.BigDecimal setTemperature, Integer fanSpeed, String operator);

    // ======================= 给排水设备管理 =======================

    /**
     * 获得给排水设备分页
     */
    PageResult<IbmsWaterDeviceDO> getWaterDevicePage(IbmsWaterDevicePageReqVO pageReqVO);

    /**
     * 获得给排水设备
     */
    IbmsWaterDeviceDO getWaterDevice(Long id);

    /**
     * 控制给排水设备启停
     */
    void controlWaterDevice(Long id, Integer runningStatus, String operator);

    // ======================= 告警管理 =======================

    /**
     * 获得楼宇自控告警分页
     */
    PageResult<IbmsBacAlarmDO> getAlarmPage(IbmsBacAlarmPageReqVO pageReqVO);

    /**
     * 处理告警
     */
    void handleAlarm(Long id, String handler, String handleRemark);

    // ======================= 系统日志 =======================

    /**
     * 获得系统日志分页
     */
    PageResult<IbmsBacSystemLogDO> getSystemLogPage(IbmsBacSystemLogPageReqVO pageReqVO);

    // ======================= 统计分析 =======================

    /**
     * 获得楼宇自控统计数据
     */
    IbmsBacStatisticsVO getStatistics();

}
