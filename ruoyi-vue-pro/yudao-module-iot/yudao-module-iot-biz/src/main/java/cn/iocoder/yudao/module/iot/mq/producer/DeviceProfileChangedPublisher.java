package cn.iocoder.yudao.module.iot.mq.producer;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.DeviceProfileChangedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备台账/连接配置变更 → 网关本地缓存（RocketMQ）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceProfileChangedPublisher {

    private final IotMessageBus messageBus;

    public void publish(DeviceProfileChangedMessage message) {
        if (message == null || message.getDeviceId() == null) {
            log.warn("[DeviceProfileChangedPublisher] 忽略非法消息: {}", message);
            return;
        }
        try {
            messageBus.post(IotMessageTopics.DEVICE_PROFILE_CHANGED, message);
            log.debug("[DeviceProfileChangedPublisher] 已发布: op={}, deviceId={}", message.getOp(), message.getDeviceId());
        } catch (Exception e) {
            log.error("[DeviceProfileChangedPublisher] 发布失败 deviceId={}", message.getDeviceId(), e);
            throw e;
        }
    }
}
