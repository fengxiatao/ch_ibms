package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessEventLogDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessEventLogMapper;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.websocket.DeviceMessagePushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 门禁事件日志 Service 实现类
 * 
 * <p>重构说明：</p>
 * <ul>
 *   <li>移除了旧的 AccessMessagePushService 依赖</li>
 *   <li>使用统一的 DeviceMessagePushService 推送门禁事件</li>
 * </ul>
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class IotAccessEventLogServiceImpl implements IotAccessEventLogService {

    @Resource
    private IotAccessEventLogMapper eventLogMapper;

    @Resource
    private DeviceMessagePushService deviceMessagePushService;

    @Resource
    private IotDeviceService deviceService;

    @Override
    public Long saveEventLog(IotAccessEventLogDO eventLog) {
        if (eventLog.getEventTime() == null) {
            eventLog.setEventTime(LocalDateTime.now());
        }
        eventLogMapper.insert(eventLog);
        
        // 推送WebSocket消息
        pushEventToWebSocket(eventLog);
        
        return eventLog.getId();
    }

    /**
     * 推送事件到WebSocket（使用统一的 DeviceMessagePushService）
     */
    private void pushEventToWebSocket(IotAccessEventLogDO eventLog) {
        try {
            // 解析真实设备类型（ACCESS_GEN1 / ACCESS_GEN2）
            String deviceType = resolveAccessDeviceType(eventLog.getDeviceId());

            // 构建事件数据
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("eventId", eventLog.getId());
            eventData.put("deviceId", eventLog.getDeviceId());
            eventData.put("deviceName", eventLog.getDeviceName());
            eventData.put("channelId", eventLog.getChannelId());
            eventData.put("channelName", eventLog.getChannelName());
            eventData.put("eventType", eventLog.getEventType());
            eventData.put("eventDesc", eventLog.getEventDesc());
            eventData.put("personId", eventLog.getPersonId());
            eventData.put("personName", eventLog.getPersonName());
            eventData.put("personCode", eventLog.getPersonCode());
            eventData.put("cardNo", eventLog.getCardNo());
            eventData.put("verifyMode", eventLog.getVerifyMode());
            eventData.put("verifyResult", eventLog.getVerifyResult());
            eventData.put("verifyResultDesc", eventLog.getVerifyResultDesc());
            eventData.put("failReason", eventLog.getFailReason());
            eventData.put("success", eventLog.getSuccess());
            // 图片字段：优先 captureUrl，否则 snapshotUrl
            eventData.put("captureUrl", eventLog.getCaptureUrl() != null ? eventLog.getCaptureUrl() : eventLog.getSnapshotUrl());
            eventData.put("eventTime", eventLog.getEventTime());
            
            // 使用统一的 DeviceMessagePushService 推送
            deviceMessagePushService.pushDeviceEvent(
                    eventLog.getDeviceId(),
                    deviceType,
                    eventLog.getEventType(),
                    eventData);
        } catch (Exception e) {
            log.error("[pushEventToWebSocket] 推送门禁事件失败: eventId={}", eventLog.getId(), e);
        }
    }

    /**
     * 解析门禁设备类型（优先使用 iot_device.config.deviceType）
     */
    private String resolveAccessDeviceType(Long deviceId) {
        try {
            if (deviceId == null || deviceService == null) {
                return AccessDeviceTypeConstants.ACCESS_GEN2;
            }
            IotDeviceDO device = deviceService.getDevice(deviceId);
            if (device == null) {
                return AccessDeviceTypeConstants.ACCESS_GEN2;
            }

            Boolean supportVideo = null;
            String configDeviceType = null;
            if (device.getConfig() instanceof AccessDeviceConfig) {
                AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
                supportVideo = config.getSupportVideo();
                // AccessDeviceConfig 可能包含 deviceType（若没有则忽略）
                try {
                    configDeviceType = (String) AccessDeviceConfig.class.getMethod("getDeviceType").invoke(config);
                } catch (Throwable ignored) {
                    // ignored
                }
            } else if (device.getConfig() instanceof GenericDeviceConfig) {
                GenericDeviceConfig config = (GenericDeviceConfig) device.getConfig();
                Object supportVideoObj = config.get("supportVideo");
                if (supportVideoObj instanceof Boolean) {
                    supportVideo = (Boolean) supportVideoObj;
                }
                Object deviceTypeObj = config.get("deviceType");
                configDeviceType = deviceTypeObj != null ? deviceTypeObj.toString() : null;
            }
            return AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, supportVideo);
        } catch (Exception e) {
            log.debug("[resolveAccessDeviceType] 解析设备类型失败: deviceId={}, error={}", deviceId, e.getMessage());
            return AccessDeviceTypeConstants.ACCESS_GEN2;
        }
    }

    @Override
    public IotAccessEventLogDO getEventLog(Long id) {
        return eventLogMapper.selectById(id);
    }

    @Override
    public PageResult<IotAccessEventLogDO> getEventLogPage(Long deviceId, Long channelId, String eventType,
                                                            Long personId, LocalDateTime startTime, LocalDateTime endTime,
                                                            Integer pageNo, Integer pageSize) {
        return eventLogMapper.selectPage(deviceId, channelId, eventType, personId,
                startTime, endTime, pageNo, pageSize);
    }

    @Override
    public PageResult<IotAccessEventLogDO> getEventLogPageEx(Long deviceId, Long channelId, String eventType,
                                                              String eventCategory, Long personId, Integer verifyResult,
                                                              LocalDateTime startTime, LocalDateTime endTime,
                                                              Integer pageNo, Integer pageSize) {
        return eventLogMapper.selectPageEx(deviceId, channelId, eventType, eventCategory, personId, verifyResult,
                startTime, endTime, pageNo, pageSize);
    }

    @Override
    public List<IotAccessEventLogDO> getRecentEvents(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        return eventLogMapper.selectRecentList(limit);
    }

    @Override
    public List<IotAccessEventLogDO> getDeviceRecentEvents(Long deviceId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        return eventLogMapper.selectRecentListByDeviceId(deviceId, limit);
    }

    @Override
    public Long recordEvent(Long deviceId, String deviceName, Long channelId, String channelName,
                            String eventType, String eventDesc, Long personId, String personName,
                            String credentialType, String credentialData, Boolean success, String failReason) {
        IotAccessEventLogDO eventLog = new IotAccessEventLogDO();
        eventLog.setDeviceId(deviceId);
        eventLog.setDeviceName(deviceName);
        eventLog.setChannelId(channelId);
        eventLog.setChannelName(channelName);
        eventLog.setEventType(eventType);
        eventLog.setEventDesc(eventDesc);
        eventLog.setPersonId(personId);
        eventLog.setPersonName(personName);
        eventLog.setCredentialType(credentialType);
        eventLog.setCredentialData(credentialData);
        eventLog.setSuccess(success);
        eventLog.setFailReason(failReason);
        eventLog.setEventTime(java.time.LocalDateTime.now());
        
        return saveEventLog(eventLog);
    }

    @Override
    public EventStatistics getTodayStatistics() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        
        // 查询今日所有事件
        List<IotAccessEventLogDO> todayEvents = eventLogMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<IotAccessEventLogDO>()
                .ge(IotAccessEventLogDO::getEventTime, todayStart)
                .lt(IotAccessEventLogDO::getEventTime, todayEnd)
        );
        
        long total = todayEvents.size();
        long alarmCount = 0;
        long normalCount = 0;
        long abnormalCount = 0;
        
        // 报警事件类型
        java.util.Set<String> alarmTypes = java.util.Set.of(
            "DOOR_NOT_CLOSED", "BREAK_IN", "REPEAT_ENTER", "MALICIOUS_OPEN", 
            "DURESS", "TAMPER_ALARM", "LOCAL_ALARM", "DOOR_SENSOR_ALARM", 
            "FORCED_OPEN", "DURESS_ALARM"
        );
        
        for (IotAccessEventLogDO event : todayEvents) {
            String eventType = event.getEventType();
            Integer verifyResult = event.getVerifyResult();
            
            if (eventType != null && alarmTypes.contains(eventType)) {
                alarmCount++;
            } else if (verifyResult != null && verifyResult == 0) {
                abnormalCount++;
            } else {
                normalCount++;
            }
        }
        
        return new EventStatistics(total, alarmCount, normalCount, abnormalCount);
    }

}
