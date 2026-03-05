package cn.iocoder.yudao.module.iot.service.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.bac.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.*;
import cn.iocoder.yudao.module.iot.dal.mysql.building.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 楼宇自控 Service 实现类
 *
 * @author 智慧楼宇系统
 */
@Service
@Validated
public class IbmsBacServiceImpl implements IbmsBacService {

    @Resource
    private IbmsHvacDeviceMapper hvacDeviceMapper;

    @Resource
    private IbmsWaterDeviceMapper waterDeviceMapper;

    @Resource
    private IbmsBacAlarmMapper alarmMapper;

    @Resource
    private IbmsBacSystemLogMapper systemLogMapper;

    // ======================= 暖通设备管理 =======================

    @Override
    public PageResult<IbmsHvacDeviceDO> getHvacDevicePage(IbmsHvacDevicePageReqVO pageReqVO) {
        return hvacDeviceMapper.selectPage(pageReqVO);
    }

    @Override
    public IbmsHvacDeviceDO getHvacDevice(Long id) {
        return hvacDeviceMapper.selectById(id);
    }

    @Override
    public void controlHvacDevice(Long id, Integer runningStatus, String operator) {
        IbmsHvacDeviceDO device = hvacDeviceMapper.selectById(id);
        if (device == null) {
            throw exception(HVAC_DEVICE_NOT_EXISTS);
        }

        // 更新设备运行状态
        IbmsHvacDeviceDO updateObj = new IbmsHvacDeviceDO();
        updateObj.setId(id);
        updateObj.setStatus(runningStatus);
        hvacDeviceMapper.updateById(updateObj);

        // 记录系统日志
        recordSystemLog(1, 1, device.getDeviceName(),
                String.format("运行状态变更为:%s", runningStatus == 1 ? "运行" : (runningStatus == 0 ? "停止" : "待机")),
                operator);
    }

    @Override
    public void setHvacDeviceParams(Long id, Integer runMode, BigDecimal setTemperature, Integer fanSpeed, String operator) {
        IbmsHvacDeviceDO device = hvacDeviceMapper.selectById(id);
        if (device == null) {
            throw exception(HVAC_DEVICE_NOT_EXISTS);
        }

        // 更新设备参数
        IbmsHvacDeviceDO updateObj = new IbmsHvacDeviceDO();
        updateObj.setId(id);
        if (runMode != null) {
            updateObj.setRunMode(runMode);
        }
        if (setTemperature != null) {
            updateObj.setSetTemp(setTemperature);
        }
        if (fanSpeed != null) {
            updateObj.setWindSpeed(fanSpeed);
        }
        hvacDeviceMapper.updateById(updateObj);

        // 记录系统日志
        recordSystemLog(1, 1, device.getDeviceName(),
                String.format("参数设置:运行模式=%s,设定温度=%s,风速=%s", runMode, setTemperature, fanSpeed),
                operator);
    }

    // ======================= 给排水设备管理 =======================

    @Override
    public PageResult<IbmsWaterDeviceDO> getWaterDevicePage(IbmsWaterDevicePageReqVO pageReqVO) {
        return waterDeviceMapper.selectPage(pageReqVO);
    }

    @Override
    public IbmsWaterDeviceDO getWaterDevice(Long id) {
        return waterDeviceMapper.selectById(id);
    }

    @Override
    public void controlWaterDevice(Long id, Integer runningStatus, String operator) {
        IbmsWaterDeviceDO device = waterDeviceMapper.selectById(id);
        if (device == null) {
            throw exception(WATER_DEVICE_NOT_EXISTS);
        }

        // 更新设备运行状态
        IbmsWaterDeviceDO updateObj = new IbmsWaterDeviceDO();
        updateObj.setId(id);
        updateObj.setStatus(runningStatus);
        waterDeviceMapper.updateById(updateObj);

        // 记录系统日志
        recordSystemLog(1, 2, device.getDeviceName(),
                String.format("运行状态变更为:%s", runningStatus == 1 ? "运行" : (runningStatus == 0 ? "停止" : "待机")),
                operator);
    }

    // ======================= 告警管理 =======================

    @Override
    public PageResult<IbmsBacAlarmDO> getAlarmPage(IbmsBacAlarmPageReqVO pageReqVO) {
        return alarmMapper.selectPage(pageReqVO);
    }

    @Override
    public void handleAlarm(Long id, String handler, String handleRemark) {
        IbmsBacAlarmDO alarm = alarmMapper.selectById(id);
        if (alarm == null) {
            throw exception(BAC_ALARM_NOT_EXISTS);
        }

        IbmsBacAlarmDO updateObj = new IbmsBacAlarmDO();
        updateObj.setId(id);
        updateObj.setStatus(2); // 已处理
        updateObj.setHandler(handler);
        updateObj.setHandleRemark(handleRemark);
        updateObj.setHandleTime(LocalDateTime.now());
        alarmMapper.updateById(updateObj);
    }

    // ======================= 系统日志 =======================

    @Override
    public PageResult<IbmsBacSystemLogDO> getSystemLogPage(IbmsBacSystemLogPageReqVO pageReqVO) {
        return systemLogMapper.selectPage(pageReqVO);
    }

    /**
     * 记录系统日志
     */
    private void recordSystemLog(Integer logType, Integer deviceType, String deviceName, String eventDesc, String operator) {
        IbmsBacSystemLogDO log = new IbmsBacSystemLogDO();
        log.setLogType(logType);
        log.setDeviceType(deviceType);
        log.setDeviceName(deviceName);
        log.setEventDesc(eventDesc);
        log.setOperator(operator);
        log.setLogTime(LocalDateTime.now());
        systemLogMapper.insert(log);
    }

    // ======================= 统计分析 =======================

    @Override
    public IbmsBacStatisticsVO getStatistics() {
        IbmsBacStatisticsVO vo = new IbmsBacStatisticsVO();

        // 暖通设备统计
        vo.setHvacTotalCount(hvacDeviceMapper.selectCount());
        vo.setHvacOnlineCount(hvacDeviceMapper.selectCountByStatus(1)); // 运行中的设备
        vo.setHvacRunningCount(hvacDeviceMapper.selectCountByStatus(1)); // 运行状态设备
        vo.setHvacFaultCount(hvacDeviceMapper.selectCountByStatus(3)); // 故障状态

        // 暖通设备类型统计
        vo.setAirConditionerCount(hvacDeviceMapper.selectCountByType(1));
        vo.setFreshAirCount(hvacDeviceMapper.selectCountByType(2));
        vo.setSupplyFanCount(hvacDeviceMapper.selectCountByType(3));
        vo.setExhaustFanCount(hvacDeviceMapper.selectCountByType(4));

        // 给排水设备统计
        vo.setWaterTotalCount(waterDeviceMapper.selectCount());
        vo.setWaterOnlineCount(waterDeviceMapper.selectCountByStatus(1));
        vo.setWaterFaultCount(waterDeviceMapper.selectCountByStatus(2));

        // 给排水设备类型统计
        vo.setDomesticPumpCount(waterDeviceMapper.selectCountByType(1));
        vo.setFirePumpCount(waterDeviceMapper.selectCountByType(2));
        vo.setSewagePumpCount(waterDeviceMapper.selectCountByType(3));
        vo.setWaterTankCount(waterDeviceMapper.selectCountByType(4));

        // 告警统计
        vo.setUnhandledAlarmCount(alarmMapper.selectCountByStatus(0));
        vo.setUrgentAlarmCount(alarmMapper.selectCountByLevel(3));

        // 功率统计（示例数据）
        vo.setHvacCurrentPower(new BigDecimal("350.0"));
        vo.setWaterCurrentPower(new BigDecimal("45.5"));

        return vo;
    }

}
