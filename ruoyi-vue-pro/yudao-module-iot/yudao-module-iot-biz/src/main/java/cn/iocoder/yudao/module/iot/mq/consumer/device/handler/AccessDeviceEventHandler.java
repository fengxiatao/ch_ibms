package cn.iocoder.yudao.module.iot.mq.consumer.device.handler;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlEventMessage;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.service.access.AccessEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 门禁设备事件处理器
 * 
 * <p>处理门禁设备（一代和二代）的事件上报，包括：</p>
 * <ul>
 *   <li>刷卡事件 - 记录刷卡日志</li>
 *   <li>人脸识别事件 - 记录人脸识别日志</li>
 *   <li>开门事件 - 记录开门日志</li>
 *   <li>报警事件 - 记录门禁报警</li>
 * </ul>
 * 
 * <p>Requirements: 1.5, 6.2, 6.3</p>
 *
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AccessDeviceEventHandler implements DeviceEventHandler {

    private static final String LOG_PREFIX = "[AccessDeviceEventHandler]";

    /**
     * 设备类型标识 - 一代门禁
     */
    private static final String DEVICE_TYPE = AccessDeviceTypeConstants.ACCESS_GEN1;

    /**
     * 门禁专用事件处理器（落库 + 图片保存 + WS 推送）
     */
    private final AccessEventHandler accessEventHandler;

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

        try {
            // 统一：把 params 还原为门禁事件 DTO，交给门禁专用处理链路
            AccessControlEventMessage event = JsonUtils.parseObject(JsonUtils.toJsonString(params), AccessControlEventMessage.class);
            if (event != null) {
                // deviceId 以消息层为准兜底
                if (event.getDeviceId() == null) {
                    event.setDeviceId(deviceId);
                }
                // deviceType 兜底（老消息可能只在 extData 里带 deviceType）
                if (event.getDeviceType() == null) {
                    Object dt = params.get("deviceType");
                    if (dt == null && params.get("extData") instanceof Map) {
                        dt = ((Map<?, ?>) params.get("extData")).get("deviceType");
                    }
                    if (dt != null) {
                        event.setDeviceType(dt.toString());
                    } else {
                        event.setDeviceType(getDeviceType());
                    }
                }
                accessEventHandler.handleEvent(event);
                log.info("{} 门禁事件已交由 AccessEventHandler 处理: deviceId={}, eventType={}", 
                        LOG_PREFIX, deviceId, event.getEventType());
            } else {
                log.warn("{} 门禁事件反序列化为空，忽略: deviceId={}", LOG_PREFIX, deviceId);
            }
        } catch (Exception e) {
            log.error("{} 处理门禁事件失败: deviceId={}", LOG_PREFIX, deviceId, e);
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

}
