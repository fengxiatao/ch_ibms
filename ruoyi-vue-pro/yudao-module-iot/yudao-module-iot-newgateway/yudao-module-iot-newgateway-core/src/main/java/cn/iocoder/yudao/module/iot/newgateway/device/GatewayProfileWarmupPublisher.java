package cn.iocoder.yudao.module.iot.newgateway.device;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.GatewayProfileWarmupRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 向 Biz 发送 Profile 批量预热请求（单向，依赖 {@link DeviceProfileChangedConsumer} 灌缓存）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayProfileWarmupPublisher {

    private final IotMessageBus messageBus;

    public void publish(Long tenantId, String source) {
        if (tenantId == null) {
            return;
        }
        GatewayProfileWarmupRequestMessage msg = GatewayProfileWarmupRequestMessage.builder()
                .tenantId(tenantId)
                .source(source != null ? source : "iot-newgateway")
                .build();
        messageBus.post(IotMessageTopics.GATEWAY_PROFILE_WARMUP_REQUEST, msg);
        log.info("[GatewayProfileWarmupPublisher] 已请求 Biz 批量预热 tenantId={}", tenantId);
    }
}
