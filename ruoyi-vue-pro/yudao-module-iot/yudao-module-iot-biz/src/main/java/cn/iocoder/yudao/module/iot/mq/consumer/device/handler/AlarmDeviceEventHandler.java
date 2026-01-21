package cn.iocoder.yudao.module.iot.mq.consumer.device.handler;

import cn.iocoder.yudao.module.iot.core.alarm.AlarmCidEventCodes;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmEventDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmHostDO;
import cn.iocoder.yudao.module.iot.service.alarm.IotAlarmEventService;
import cn.iocoder.yudao.module.iot.service.alarm.IotAlarmHostService;
import cn.iocoder.yudao.module.iot.service.alarm.IotAlarmZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 报警设备事件处理器
 * 
 * <p>处理报警主机设备的事件上报，包括：</p>
 * <ul>
 *   <li>布防/撤防事件 - 更新主机布防状态</li>
 *   <li>防区报警事件 - 更新防区报警状态</li>
 *   <li>防区旁路事件 - 更新防区旁路状态</li>
 *   <li>单防区布防/撤防事件 - 更新单防区布防状态</li>
 *   <li>所有事件 - 记录到报警事件表</li>
 * </ul>
 * 
 * <p>Requirements: 8.1-8.5, 9.1-9.6, 10.1-10.4</p>
 *
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AlarmDeviceEventHandler implements DeviceEventHandler {

    private static final String LOG_PREFIX = "[AlarmDeviceEventHandler]";

    /**
     * 设备类型标识
     */
    private static final String DEVICE_TYPE = "ALARM";

    private final IotAlarmHostService alarmHostService;
    private final IotAlarmZoneService alarmZoneService;
    private final IotAlarmEventService alarmEventService;

    @Override
    public String getDeviceType() {
        return DEVICE_TYPE;
    }

    @Override
    public void handleEvent(IotDeviceMessage message) {
        if (message == null) {
            log.warn("{} 收到空消息，忽略", LOG_PREFIX);
            return;
        }

        Long deviceId = message.getDeviceId();
        Map<String, Object> params = extractParams(message);
        
        if (params == null || params.isEmpty()) {
            log.warn("{} 事件参数为空: deviceId={}", LOG_PREFIX, deviceId);
            return;
        }

        String eventCode = getStringParam(params, "eventCode");
        if (eventCode == null || eventCode.isEmpty()) {
            log.warn("{} 事件码为空: deviceId={}", LOG_PREFIX, deviceId);
            return;
        }

        log.info("{} 处理报警事件: deviceId={}, eventCode={}", LOG_PREFIX, deviceId, eventCode);

        try {
            // 1. 处理主机布防/撤防事件
            handleArmDisarmEvent(deviceId, eventCode);

            // 2. 处理防区报警事件
            handleZoneAlarmEvent(deviceId, eventCode, params);

            // 3. 处理防区旁路事件
            handleZoneBypassEvent(deviceId, eventCode, params);

            // 4. 处理单防区布防/撤防事件
            handleSingleZoneArmEvent(deviceId, eventCode, params);

            // 5. 记录报警事件到数据库
            recordAlarmEvent(deviceId, eventCode, params);

        } catch (Exception e) {
            log.error("{} 处理报警事件失败: deviceId={}, eventCode={}", 
                    LOG_PREFIX, deviceId, eventCode, e);
        }
    }

    /**
     * 处理主机布防/撤防事件
     * 
     * @param deviceId 设备ID
     * @param eventCode 事件码
     */
    private void handleArmDisarmEvent(Long deviceId, String eventCode) {
        String armStatus = AlarmCidEventCodes.getArmStatus(eventCode);
        if (armStatus != null) {
            alarmHostService.updateHostArmStatusByDeviceId(deviceId, armStatus);
            log.info("{} 主机布防状态已更新: deviceId={}, eventCode={}, armStatus={}",
                    LOG_PREFIX, deviceId, eventCode, armStatus);
        }
    }

    /**
     * 处理防区报警事件
     * 
     * @param deviceId 设备ID
     * @param eventCode 事件码
     * @param params 事件参数
     */
    private void handleZoneAlarmEvent(Long deviceId, String eventCode, Map<String, Object> params) {
        if (AlarmCidEventCodes.isZoneAlarmEvent(eventCode)) {
            // 防区报警
            String area = getStringParam(params, "area");
            String point = getStringParam(params, "point");
            alarmZoneService.updateZoneStatusByDeviceIdAndZoneNo(deviceId, area, point, "ALARM");
            log.info("{} 防区报警状态已更新: deviceId={}, area={}, point={}, status=ALARM",
                    LOG_PREFIX, deviceId, area, point);
        } else if (AlarmCidEventCodes.isZoneAlarmRestoreEvent(eventCode)) {
            // 防区报警恢复
            String area = getStringParam(params, "area");
            String point = getStringParam(params, "point");
            alarmZoneService.updateZoneStatusByDeviceIdAndZoneNo(deviceId, area, point, "NORMAL");
            log.info("{} 防区报警状态已恢复: deviceId={}, area={}, point={}, status=NORMAL",
                    LOG_PREFIX, deviceId, area, point);
        }
    }

    /**
     * 处理防区旁路事件
     * 
     * @param deviceId 设备ID
     * @param eventCode 事件码
     * @param params 事件参数
     */
    private void handleZoneBypassEvent(Long deviceId, String eventCode, Map<String, Object> params) {
        if (AlarmCidEventCodes.isZoneBypassEvent(eventCode)) {
            // 防区旁路
            String area = getStringParam(params, "area");
            String point = getStringParam(params, "point");
            alarmZoneService.updateZoneStatusByDeviceIdAndZoneNo(deviceId, area, point, "BYPASS");
            log.info("{} 防区已旁路: deviceId={}, area={}, point={}",
                    LOG_PREFIX, deviceId, area, point);
        } else if (AlarmCidEventCodes.isZoneUnbypassEvent(eventCode)) {
            // 防区取消旁路
            String area = getStringParam(params, "area");
            String point = getStringParam(params, "point");
            alarmZoneService.updateZoneStatusByDeviceIdAndZoneNo(deviceId, area, point, "NORMAL");
            log.info("{} 防区已取消旁路: deviceId={}, area={}, point={}",
                    LOG_PREFIX, deviceId, area, point);
        }
    }

    /**
     * 处理单防区布防/撤防事件
     * 
     * @param deviceId 设备ID
     * @param eventCode 事件码
     * @param params 事件参数
     */
    private void handleSingleZoneArmEvent(Long deviceId, String eventCode, Map<String, Object> params) {
        if (AlarmCidEventCodes.isSingleZoneArmEvent(eventCode)) {
            // 单防区布防
            String area = getStringParam(params, "area");
            String point = getStringParam(params, "point");
            alarmZoneService.updateZoneStatusByDeviceIdAndZoneNo(deviceId, area, point, "ARMED");
            log.info("{} 单防区已布防: deviceId={}, area={}, point={}",
                    LOG_PREFIX, deviceId, area, point);
        } else if (AlarmCidEventCodes.isSingleZoneDisarmEvent(eventCode)) {
            // 单防区撤防
            String area = getStringParam(params, "area");
            String point = getStringParam(params, "point");
            alarmZoneService.updateZoneStatusByDeviceIdAndZoneNo(deviceId, area, point, "DISARMED");
            log.info("{} 单防区已撤防: deviceId={}, area={}, point={}",
                    LOG_PREFIX, deviceId, area, point);
        }
    }

    /**
     * 记录报警事件到数据库
     * 
     * @param deviceId 设备ID
     * @param eventCode 事件码
     * @param params 事件参数
     */
    private void recordAlarmEvent(Long deviceId, String eventCode, Map<String, Object> params) {
        try {
            // 从 deviceId 获取 hostId
            IotAlarmHostDO host = alarmHostService.getAlarmHostByDeviceId(deviceId);
            if (host == null) {
                log.warn("{} 未找到主机: deviceId={}", LOG_PREFIX, deviceId);
                return;
            }

            String eventName = getStringParam(params, "eventName");
            if (eventName == null || eventName.isEmpty()) {
                eventName = AlarmCidEventCodes.getEventDescription(eventCode);
            }
            
            String eventLevel = getStringParam(params, "eventLevel");
            if (eventLevel == null || eventLevel.isEmpty()) {
                eventLevel = determineEventLevel(eventCode);
            }

            String area = getStringParam(params, "area");
            String point = getStringParam(params, "point");
            String sequence = getStringParam(params, "sequence");

            // 构建事件记录（从主机获取租户ID，确保多租户数据隔离）
            IotAlarmEventDO eventDO = IotAlarmEventDO.builder()
                .hostId(host.getId())
                .eventCode(eventCode)
                .eventType(determineEventType(eventCode))
                .eventLevel(eventLevel)
                .areaNo(parseIntSafe(area))
                .zoneNo(parseIntSafe(point))
                .sequence(sequence)
                .eventDesc(eventName)
                .rawData(params.toString())
                .isNewEvent(AlarmCidEventCodes.isAlarmEvent(eventCode))
                .isHandled(false)
                .build();
            // 从主机继承租户ID（由于 @Builder 不包含父类字段，需单独设置）
            eventDO.setTenantId(host.getTenantId() != null ? host.getTenantId() : 1L);

            // 保存事件
            alarmEventService.saveEvent(eventDO);
            log.info("{} 报警事件已记录: hostId={}, eventCode={}, eventName={}",
                    LOG_PREFIX, host.getId(), eventCode, eventName);
        } catch (Exception e) {
            log.error("{} 记录报警事件失败: deviceId={}, eventCode={}", 
                    LOG_PREFIX, deviceId, eventCode, e);
        }
    }

    /**
     * 从消息中提取参数
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractParams(IotDeviceMessage message) {
        Object params = message.getParams();
        if (params instanceof Map) {
            return (Map<String, Object>) params;
        }
        return null;
    }

    /**
     * 获取字符串参数
     */
    private String getStringParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 安全解析整数
     */
    private Integer parseIntSafe(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 确定事件类型
     */
    private String determineEventType(String eventCode) {
        if (AlarmCidEventCodes.isArmDisarmEvent(eventCode)) {
            return AlarmCidEventCodes.DISARM.equals(eventCode) ? "DISARM" : "ARM";
        }
        if (AlarmCidEventCodes.isZoneAlarmEvent(eventCode)) {
            return "ALARM";
        }
        if (AlarmCidEventCodes.isZoneBypassEvent(eventCode) || 
            AlarmCidEventCodes.isZoneUnbypassEvent(eventCode)) {
            return "BYPASS";
        }
        if (AlarmCidEventCodes.isSingleZoneArmEvent(eventCode) ||
            AlarmCidEventCodes.isSingleZoneDisarmEvent(eventCode)) {
            return "ZONE_ARM";
        }
        return "INFO";
    }

    /**
     * 确定事件级别
     */
    private String determineEventLevel(String eventCode) {
        // 报警事件为 ERROR 级别
        if (AlarmCidEventCodes.isZoneAlarmEvent(eventCode)) {
            return "ERROR";
        }
        // 挟持报警为 CRITICAL 级别
        if (AlarmCidEventCodes.DURESS_ALARM.equals(eventCode)) {
            return "CRITICAL";
        }
        // 恢复事件为 INFO 级别
        if (AlarmCidEventCodes.isRestoreEvent(eventCode)) {
            return "INFO";
        }
        // 布防/撤防为 INFO 级别
        if (AlarmCidEventCodes.isArmDisarmEvent(eventCode)) {
            return "INFO";
        }
        // 默认为 WARNING 级别
        return "WARNING";
    }
}
