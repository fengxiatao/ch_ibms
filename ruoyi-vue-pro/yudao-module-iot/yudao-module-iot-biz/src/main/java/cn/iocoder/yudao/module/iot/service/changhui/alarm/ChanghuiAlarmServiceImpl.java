package cn.iocoder.yudao.module.iot.service.changhui.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.alarm.ChanghuiAlarmPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.alarm.ChanghuiAlarmRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.alarm.ChanghuiAlarmSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.changhui.ChanghuiAlarmConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiAlarmDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.changhui.ChanghuiAlarmMapper;
import cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiAlarmTypeEnum;
import cn.iocoder.yudao.module.iot.service.changhui.device.ChanghuiDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.CHANGHUI_ALARM_NOT_EXISTS;

/**
 * 长辉设备报警管理 Service 实现类
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
@Slf4j
public class ChanghuiAlarmServiceImpl implements ChanghuiAlarmService {

    /** 报警状态：未确认 */
    private static final Integer STATUS_UNACKNOWLEDGED = 0;
    /** 报警状态：已确认 */
    private static final Integer STATUS_ACKNOWLEDGED = 1;

    @Resource
    private ChanghuiAlarmMapper alarmMapper;

    @Resource
    private ChanghuiDeviceService deviceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveAlarm(ChanghuiAlarmSaveReqVO reqVO) {
        ChanghuiAlarmDO alarm = ChanghuiAlarmConvert.INSTANCE.convert(reqVO);
        
        // 如果没有设备ID，尝试根据测站编码查找
        if (alarm.getDeviceId() == null && alarm.getStationCode() != null) {
            ChanghuiDeviceDO device = deviceService.getDeviceDOByStationCode(alarm.getStationCode());
            if (device != null) {
                alarm.setDeviceId(device.getId());
            }
        }
        
        // 如果没有报警时间，使用当前时间
        if (alarm.getAlarmTime() == null) {
            alarm.setAlarmTime(LocalDateTime.now());
        }
        
        // 设置初始状态为未确认
        alarm.setStatus(STATUS_UNACKNOWLEDGED);
        
        alarmMapper.insert(alarm);
        log.info("[saveAlarm] 报警保存成功: stationCode={}, alarmType={}, alarmValue={}",
                alarm.getStationCode(), alarm.getAlarmType(), alarm.getAlarmValue());
        return alarm.getId();
    }

    @Override
    public PageResult<ChanghuiAlarmRespVO> getAlarmPage(ChanghuiAlarmPageReqVO reqVO) {
        PageResult<ChanghuiAlarmDO> pageResult = alarmMapper.selectPage(
                reqVO.getStationCode(), reqVO.getDeviceId(), reqVO.getAlarmType(),
                reqVO.getStatus(), reqVO.getStartTime(), reqVO.getEndTime(),
                reqVO.getPageNo(), reqVO.getPageSize());
        PageResult<ChanghuiAlarmRespVO> result = ChanghuiAlarmConvert.INSTANCE.convertPage(pageResult);
        // 填充额外字段
        result.getList().forEach(this::enrichAlarmRespVO);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acknowledgeAlarm(Long alarmId, String ackUser) {
        // 校验报警是否存在
        ChanghuiAlarmDO alarm = alarmMapper.selectById(alarmId);
        if (alarm == null) {
            throw exception(CHANGHUI_ALARM_NOT_EXISTS);
        }
        
        // 如果已经确认，直接返回
        if (STATUS_ACKNOWLEDGED.equals(alarm.getStatus())) {
            log.debug("[acknowledgeAlarm] 报警已确认，跳过: alarmId={}", alarmId);
            return;
        }
        
        // 更新状态
        ChanghuiAlarmDO updateObj = new ChanghuiAlarmDO();
        updateObj.setId(alarmId);
        updateObj.setStatus(STATUS_ACKNOWLEDGED);
        updateObj.setAckTime(LocalDateTime.now());
        updateObj.setAckUser(ackUser);
        alarmMapper.updateById(updateObj);
        
        log.info("[acknowledgeAlarm] 报警确认成功: alarmId={}, ackUser={}", alarmId, ackUser);
    }

    @Override
    public Long getUnacknowledgedCount() {
        return alarmMapper.selectUnacknowledgedCount();
    }

    @Override
    public Long getUnacknowledgedCountByStationCode(String stationCode) {
        return alarmMapper.selectUnacknowledgedCountByStationCode(stationCode);
    }

    @Override
    public ChanghuiAlarmRespVO getAlarm(Long alarmId) {
        ChanghuiAlarmDO alarm = alarmMapper.selectById(alarmId);
        if (alarm == null) {
            return null;
        }
        return enrichAlarmRespVO(ChanghuiAlarmConvert.INSTANCE.convert(alarm));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAlarmByDeviceId(Long deviceId) {
        int count = alarmMapper.deleteByDeviceId(deviceId);
        log.info("[deleteAlarmByDeviceId] 删除设备报警: deviceId={}, count={}", deviceId, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAlarmByStationCode(String stationCode) {
        int count = alarmMapper.deleteByStationCode(stationCode);
        log.info("[deleteAlarmByStationCode] 删除设备报警: stationCode={}, count={}", stationCode, count);
    }

    /**
     * 填充报警响应VO的额外字段
     */
    private ChanghuiAlarmRespVO enrichAlarmRespVO(ChanghuiAlarmRespVO respVO) {
        if (respVO == null) {
            return null;
        }
        // 填充报警类型名称
        if (respVO.getAlarmType() != null) {
            ChanghuiAlarmTypeEnum alarmTypeEnum = ChanghuiAlarmTypeEnum.getByCode(respVO.getAlarmType());
            if (alarmTypeEnum != null) {
                respVO.setAlarmTypeName(alarmTypeEnum.getDescription());
            } else {
                respVO.setAlarmTypeName(respVO.getAlarmType());
            }
        }
        // 填充状态名称
        if (respVO.getStatus() != null) {
            respVO.setStatusName(STATUS_ACKNOWLEDGED.equals(respVO.getStatus()) ? "已确认" : "未确认");
        }
        return respVO;
    }

}
