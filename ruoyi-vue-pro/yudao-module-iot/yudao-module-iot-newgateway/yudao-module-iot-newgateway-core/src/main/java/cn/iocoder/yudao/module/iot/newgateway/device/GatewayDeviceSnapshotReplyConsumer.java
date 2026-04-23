package cn.iocoder.yudao.module.iot.newgateway.device;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.GatewayDeviceSnapshotReplyMessage;
import cn.iocoder.yudao.module.iot.newgateway.consumer.ConsumerConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayDeviceSnapshotReplyConsumer implements IotMessageSubscriber<GatewayDeviceSnapshotReplyMessage> {

    private final IotMessageBus messageBus;
    private final GatewayDeviceSnapshotPendingRegistry pendingRegistry;

    @PostConstruct
    public void init() {
        messageBus.register(this);
    }

    @Override
    public String getTopic() {
        return IotMessageTopics.GATEWAY_DEVICE_SNAPSHOT_REPLY;
    }

    @Override
    public String getGroup() {
        return ConsumerConstants.CONSUMER_GROUP_DEVICE_SNAPSHOT_REPLY;
    }

    @Override
    public void onMessage(GatewayDeviceSnapshotReplyMessage message) {
        if (message == null) {
            return;
        }
        pendingRegistry.complete(message);
        log.debug("[GatewayDeviceSnapshotReplyConsumer] correlationId={} code={}", message.getCorrelationId(), message.getCode());
    }
}
