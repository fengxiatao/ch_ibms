package cn.iocoder.yudao.module.iot.newgateway.device;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.DeviceProfileChangedMessage;
import cn.iocoder.yudao.module.iot.newgateway.consumer.ConsumerConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 消费 Biz 下发的设备台账变更，维护网关本地缓存。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceProfileChangedConsumer implements IotMessageSubscriber<DeviceProfileChangedMessage> {

    private final IotMessageBus messageBus;
    private final GatewayDeviceProfileCache cache;

    @PostConstruct
    public void init() {
        messageBus.register(this);
    }

    @Override
    public String getTopic() {
        return IotMessageTopics.DEVICE_PROFILE_CHANGED;
    }

    @Override
    public String getGroup() {
        return ConsumerConstants.CONSUMER_GROUP_DEVICE_PROFILE;
    }

    @Override
    public void onMessage(DeviceProfileChangedMessage message) {
        if (message == null || message.getDeviceId() == null) {
            return;
        }
        if (DeviceProfileChangedMessage.OP_DELETE.equalsIgnoreCase(message.getOp())) {
            cache.remove(message.getDeviceId());
            log.info("[DeviceProfileChangedConsumer] 已移除缓存 deviceId={}", message.getDeviceId());
            return;
        }
        IotDeviceRespDTO dto = new IotDeviceRespDTO();
        dto.setId(message.getDeviceId());
        dto.setTenantId(message.getTenantId());
        dto.setDeviceName(message.getDeviceName());
        dto.setProductKey(message.getProductKey());
        dto.setProductId(message.getProductId());
        dto.setAddress(message.getAddress());
        dto.setConfig(message.getConfig());
        dto.setDeviceType(message.getDeviceType());
        dto.setBrand(message.getBrand());
        cache.upsert(dto);
        log.debug("[DeviceProfileChangedConsumer] 已更新缓存 deviceId={}, deviceType={}", dto.getId(), dto.getDeviceType());
    }
}
