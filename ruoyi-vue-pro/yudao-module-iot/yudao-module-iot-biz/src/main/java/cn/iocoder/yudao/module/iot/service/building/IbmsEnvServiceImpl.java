package cn.iocoder.yudao.module.iot.service.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.env.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnvAlarmDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnvDataRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnvSensorDO;
import cn.iocoder.yudao.module.iot.dal.mysql.building.IbmsEnvAlarmMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.building.IbmsEnvDataRecordMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.building.IbmsEnvSensorMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 环境监测 Service 实现类
 *
 * @author 智慧楼宇系统
 */
@Service
@Validated
public class IbmsEnvServiceImpl implements IbmsEnvService {

    @Resource
    private IbmsEnvSensorMapper envSensorMapper;

    @Resource
    private IbmsEnvDataRecordMapper envDataRecordMapper;

    @Resource
    private IbmsEnvAlarmMapper envAlarmMapper;

    // ======================= 传感器管理 =======================

    @Override
    public Long createEnvSensor(IbmsEnvSensorSaveReqVO createReqVO) {
        IbmsEnvSensorDO sensor = BeanUtils.toBean(createReqVO, IbmsEnvSensorDO.class);
        sensor.setStatus(0); // 默认离线
        envSensorMapper.insert(sensor);
        return sensor.getId();
    }

    @Override
    public void updateEnvSensor(IbmsEnvSensorSaveReqVO updateReqVO) {
        // 校验存在
        validateEnvSensorExists(updateReqVO.getId());
        // 更新
        IbmsEnvSensorDO updateObj = BeanUtils.toBean(updateReqVO, IbmsEnvSensorDO.class);
        envSensorMapper.updateById(updateObj);
    }

    @Override
    public void deleteEnvSensor(Long id) {
        // 校验存在
        validateEnvSensorExists(id);
        // 删除
        envSensorMapper.deleteById(id);
    }

    private void validateEnvSensorExists(Long id) {
        if (envSensorMapper.selectById(id) == null) {
            throw exception(ENV_SENSOR_NOT_EXISTS);
        }
    }

    @Override
    public IbmsEnvSensorDO getEnvSensor(Long id) {
        return envSensorMapper.selectById(id);
    }

    @Override
    public PageResult<IbmsEnvSensorDO> getEnvSensorPage(IbmsEnvSensorPageReqVO pageReqVO) {
        return envSensorMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IbmsEnvSensorDO> getEnvSensorList(IbmsEnvSensorPageReqVO reqVO) {
        return envSensorMapper.selectList(reqVO);
    }

    // ======================= 数据记录 =======================

    @Override
    public PageResult<IbmsEnvDataRecordDO> getEnvDataRecordPage(IbmsEnvDataRecordPageReqVO pageReqVO) {
        return envDataRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public IbmsEnvDataRecordDO getLatestEnvDataRecord(Long sensorId) {
        return envDataRecordMapper.selectLatestBySensorId(sensorId);
    }

    @Override
    public List<IbmsEnvDataRecordDO> getEnvDataRecordHistory(Long sensorId, int limit) {
        return envDataRecordMapper.selectLatestBySensorId(sensorId, limit);
    }

    // ======================= 告警管理 =======================

    @Override
    public PageResult<IbmsEnvAlarmDO> getEnvAlarmPage(IbmsEnvAlarmPageReqVO pageReqVO) {
        return envAlarmMapper.selectPage(pageReqVO);
    }

    @Override
    public void handleEnvAlarm(Long id, String handler, String handleRemark) {
        IbmsEnvAlarmDO alarm = envAlarmMapper.selectById(id);
        if (alarm == null) {
            throw exception(ENV_ALARM_NOT_EXISTS);
        }
        IbmsEnvAlarmDO updateObj = new IbmsEnvAlarmDO();
        updateObj.setId(id);
        updateObj.setStatus(2); // 已处理
        updateObj.setHandler(handler);
        updateObj.setHandleRemark(handleRemark);
        updateObj.setHandleTime(LocalDateTime.now());
        envAlarmMapper.updateById(updateObj);
    }

    @Override
    public void ignoreEnvAlarm(Long id, String handler, String handleRemark) {
        IbmsEnvAlarmDO alarm = envAlarmMapper.selectById(id);
        if (alarm == null) {
            throw exception(ENV_ALARM_NOT_EXISTS);
        }
        IbmsEnvAlarmDO updateObj = new IbmsEnvAlarmDO();
        updateObj.setId(id);
        updateObj.setStatus(3); // 已忽略
        updateObj.setHandler(handler);
        updateObj.setHandleRemark(handleRemark);
        updateObj.setHandleTime(LocalDateTime.now());
        envAlarmMapper.updateById(updateObj);
    }

    // ======================= 统计分析 =======================

    @Override
    public IbmsEnvStatisticsVO getEnvStatistics() {
        IbmsEnvStatisticsVO vo = new IbmsEnvStatisticsVO();

        // 传感器统计
        vo.setTotalCount(envSensorMapper.selectCount());
        vo.setOnlineCount(envSensorMapper.selectCountByStatus(1));
        vo.setOfflineCount(envSensorMapper.selectCountByStatus(0));
        vo.setFaultCount(envSensorMapper.selectCountByStatus(2));

        // 按类型统计
        vo.setTempHumidityCount(envSensorMapper.selectCountByType(1));
        vo.setPm25Count(envSensorMapper.selectCountByType(2));
        vo.setCo2Count(envSensorMapper.selectCountByType(3));
        vo.setNoiseCount(envSensorMapper.selectCountByType(4));
        vo.setIlluminationCount(envSensorMapper.selectCountByType(5));
        vo.setPressureCount(envSensorMapper.selectCountByType(6));

        // 告警统计
        vo.setUnhandledAlarmCount(envAlarmMapper.selectCountByStatus(0));

        // 室外环境数据（模拟数据，实际应从传感器读取）
        vo.setOutdoorTemperature(new BigDecimal("18.5"));
        vo.setOutdoorHumidity(new BigDecimal("65"));
        vo.setOutdoorPm25(new BigDecimal("35"));
        vo.setWindDirection("东南风");
        vo.setWindSpeed(new BigDecimal("3.2"));

        return vo;
    }

}
