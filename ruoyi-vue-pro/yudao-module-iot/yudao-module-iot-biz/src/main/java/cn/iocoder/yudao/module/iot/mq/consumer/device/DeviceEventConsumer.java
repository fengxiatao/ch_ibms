package cn.iocoder.yudao.module.iot.mq.consumer.device;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageEnvelope;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.mq.consumer.device.handler.DeviceEventHandler;
import cn.iocoder.yudao.module.iot.websocket.DeviceMessagePushService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备事件消费者
 * 
 * <p>统一处理所有设备的事件上报。</p>
 * 
 * <p>功能：</p>
 * <ul>
 *   <li>订阅 DEVICE_EVENT_REPORTED 主题</li>
 *   <li>根据 deviceType 路由到对应的事件处理器</li>
 *   <li>调用 DeviceMessagePushService 推送事件到前端</li>
 * </ul>
 * 
 * <p>Requirements: 6.1, 6.2, 6.3, 6.4, 6.5</p>
 *
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
public class DeviceEventConsumer implements IotMessageSubscriber<IotMessageEnvelope> {

    /**
     * 消费者组名
     */
    private static final String CONSUMER_GROUP = "iot-biz-device-event";

    private final IotMessageBus messageBus;
    private final DeviceMessagePushService pushService;
    private final Map<String, DeviceEventHandler> handlers;

    /**
     * 构造函数
     * 
     * @param messageBus 消息总线
     * @param pushService WebSocket 推送服务
     * @param handlerList 设备事件处理器列表（Spring 自动注入所有实现）
     */
    @Autowired
    public DeviceEventConsumer(IotMessageBus messageBus,
                                DeviceMessagePushService pushService,
                                @Autowired(required = false) List<DeviceEventHandler> handlerList) {
        this.messageBus = messageBus;
        this.pushService = pushService;
        this.handlers = new HashMap<>();
        
        // 注册所有处理器
        if (handlerList != null) {
            for (DeviceEventHandler handler : handlerList) {
                handlers.put(handler.getDeviceType(), handler);
                log.info("[DeviceEventConsumer] 注册事件处理器: deviceType={}", handler.getDeviceType());
            }
        }
    }

    /**
     * 初始化：注册到消息总线
     */
    @PostConstruct
    public void init() {
        messageBus.register(this);
        log.info("[DeviceEventConsumer] 已注册到消息总线: topic={}, group={}", 
                getTopic(), getGroup());
    }

    @Override
    public String getTopic() {
        return IotMessageTopics.DEVICE_EVENT_REPORTED;
    }

    @Override
    public String getGroup() {
        return CONSUMER_GROUP;
    }

    @Override
    public void onMessage(IotMessageEnvelope envelope) {
        if (envelope == null || envelope.getPayload() == null) {
            log.warn("[DeviceEventConsumer] 收到空 Envelope 或 payload，忽略");
            return;
        }

        Map<String, Object> payloadMap = parsePayloadToMap(envelope.getPayload());
        if (payloadMap == null || payloadMap.isEmpty()) {
            log.warn("[DeviceEventConsumer] payload 解析为空，忽略: envelope={}", envelope);
            return;
        }

        // 有些网关侧会上报 IotDeviceMessage（顶层含 method/deviceId/params...），业务事件参数在 payload.params 里
        // 这类消息如果直接用 payloadMap，会导致 deviceType=UNKNOWN 且 handler 拿不到 eventCode 等字段
        Map<String, Object> eventParams = extractEventParams(payloadMap);

        String deviceType = envelope.getDeviceType() != null ? envelope.getDeviceType() : extractDeviceType(payloadMap, eventParams);
        Long deviceId = envelope.getDeviceId() != null ? envelope.getDeviceId() : getLong(payloadMap, "deviceId", null);
        String eventType = determineEventType(payloadMap);

        // 统一转换为 IotDeviceMessage，复用现有 DeviceEventHandler 生态
        IotDeviceMessage message = IotDeviceMessage.requestOf(eventType, eventParams);
        message.setDeviceId(deviceId);
        message.setTenantId(envelope.getTenantId() != null ? envelope.getTenantId() : extractTenantId(payloadMap, eventParams));
        message.setRequestId(envelope.getRequestId() != null ? envelope.getRequestId() : getString(payloadMap, "requestId", null));

        log.info("[DeviceEventConsumer] 收到事件: deviceType={}, deviceId={}, eventType={}",
                deviceType, deviceId, eventType);

        try {
            // 1. 路由到对应的处理器存储事件
            routeToHandler(deviceType, message);

            // 2. 推送到前端
            // 门禁事件：由门禁专用链路（AccessEventHandler/IotAccessEventLogServiceImpl）负责 push，避免重复推送
            if (deviceType != null && deviceType.startsWith("ACCESS_")) {
                return;
            }
            // 推送事件参数（更贴近业务侧消费结构）
            pushToFrontend(deviceId, deviceType, eventType, eventParams);

        } catch (Exception e) {
            log.error("[DeviceEventConsumer] 处理事件失败: deviceType={}, deviceId={}, eventType={}, error={}",
                    deviceType, deviceId, eventType, e.getMessage(), e);
        }
    }


    /**
     * 从消息中提取设备类型
     * 
     * @param message 设备消息
     * @return 设备类型，如果无法提取则返回 "UNKNOWN"
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parsePayloadToMap(Object payload) {
        try {
            // RocketMQ Envelope 自动解包时，payload 通常是 LinkedHashMap
            if (payload instanceof Map) {
                return (Map<String, Object>) payload;
            }
            // 兜底：按 JSON 解析
            return JsonUtils.parseObject(JsonUtils.toJsonString(payload), Map.class);
        } catch (Exception e) {
            log.error("[DeviceEventConsumer] payload 解析失败: payload={}", payload, e);
            return null;
        }
    }

    private String determineEventType(Map<String, Object> payload) {
        // 优先使用 eventTypeName / method；没有则回退到 eventType；再回退 UNKNOWN
        String eventTypeName = getString(payload, "eventTypeName", null);
        if (eventTypeName != null && !eventTypeName.isEmpty()) {
            return eventTypeName;
        }
        String method = getString(payload, "method", null);
        if (method != null && !method.isEmpty()) {
            return method;
        }
        Object eventType = payload.get("eventType");
        return eventType != null ? String.valueOf(eventType) : "UNKNOWN";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractEventParams(Map<String, Object> payloadMap) {
        Object params = payloadMap.get("params");
        if (params instanceof Map) {
            return (Map<String, Object>) params;
        }
        return payloadMap;
    }

    private String extractDeviceType(Map<String, Object> payloadMap, Map<String, Object> eventParams) {
        // 1) payload 顶层 deviceType（少数链路会这么发）
        String fromPayload = getString(payloadMap, "deviceType", null);
        if (fromPayload != null && !fromPayload.isEmpty()) {
            return fromPayload;
        }
        // 2) eventParams.deviceType（网关侧常见：IotDeviceMessage.params.deviceType）
        String fromParams = getString(eventParams, "deviceType", null);
        if (fromParams != null && !fromParams.isEmpty()) {
            return fromParams;
        }
        return "UNKNOWN";
    }

    private Long extractTenantId(Map<String, Object> payloadMap, Map<String, Object> eventParams) {
        Long tenantId = getLong(payloadMap, "tenantId", null);
        if (tenantId != null) {
            return tenantId;
        }
        return getLong(eventParams, "tenantId", null);
    }

    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object v = map.get(key);
        return v != null ? v.toString() : defaultValue;
    }

    private Long getLong(Map<String, Object> map, String key, Long defaultValue) {
        Object v = map.get(key);
        if (v == null) {
            return defaultValue;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        try {
            return Long.parseLong(v.toString());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    /**
     * 路由到对应的处理器
     * 
     * @param deviceType 设备类型
     * @param message 设备消息
     */
    void routeToHandler(String deviceType, IotDeviceMessage message) {
        DeviceEventHandler handler = handlers.get(deviceType);
        if (handler != null) {
            try {
                handler.handleEvent(message);
                log.debug("[DeviceEventConsumer] 事件处理器执行完成: deviceType={}", deviceType);
            } catch (Exception e) {
                log.error("[DeviceEventConsumer] 事件处理器执行失败: deviceType={}, error={}",
                        deviceType, e.getMessage(), e);
            }
        } else {
            log.debug("[DeviceEventConsumer] 未找到事件处理器: deviceType={}", deviceType);
        }
    }

    /**
     * 推送事件到前端
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param eventType 事件类型
     * @param message 原始消息
     */
    void pushToFrontend(Long deviceId, String deviceType, String eventType, Object eventData) {
        try {
            pushService.pushDeviceEvent(deviceId, deviceType, eventType, eventData);
            log.debug("[DeviceEventConsumer] 已推送到前端: deviceId={}, eventType={}", 
                    deviceId, eventType);
        } catch (Exception e) {
            log.error("[DeviceEventConsumer] 推送到前端失败: deviceId={}, eventType={}, error={}",
                    deviceId, eventType, e.getMessage(), e);
        }
    }

    /**
     * 获取已注册的处理器数量（用于测试）
     * 
     * @return 处理器数量
     */
    int getHandlerCount() {
        return handlers.size();
    }

    /**
     * 检查是否有指定设备类型的处理器（用于测试）
     * 
     * @param deviceType 设备类型
     * @return 是否存在处理器
     */
    boolean hasHandler(String deviceType) {
        return handlers.containsKey(deviceType);
    }
}
