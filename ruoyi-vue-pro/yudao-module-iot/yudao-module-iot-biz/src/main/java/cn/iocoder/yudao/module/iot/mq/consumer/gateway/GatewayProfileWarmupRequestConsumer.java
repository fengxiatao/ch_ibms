package cn.iocoder.yudao.module.iot.mq.consumer.gateway;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.GatewayProfileWarmupRequestMessage;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 消费网关发起的 Profile 批量预热请求，在指定租户上下文中重推 IBMS 设备 Profile。
 * <p>
 * 租户上下文由 IoT 消息总线（RocketMQ）根据消息 {@code tenantId} 注入，此处直接调用与「管理端全量重推」相同逻辑。
 */
@Slf4j
@Component
public class GatewayProfileWarmupRequestConsumer implements IotMessageSubscriber<GatewayProfileWarmupRequestMessage> {

    public static final String CONSUMER_GROUP = "iot-biz-gateway-profile-warmup";

    @Resource
    private IotMessageBus messageBus;

    @Resource
    private IbmsDeviceService ibmsDeviceService;

    @PostConstruct
    public void init() {
        messageBus.register(this);
    }

    @Override
    public String getTopic() {
        return IotMessageTopics.GATEWAY_PROFILE_WARMUP_REQUEST;
    }

    @Override
    public String getGroup() {
        return CONSUMER_GROUP;
    }

    @Override
    public void onMessage(GatewayProfileWarmupRequestMessage message) {
        if (message == null || message.getTenantId() == null) {
            log.warn("[GatewayProfileWarmupRequestConsumer] 非法请求: {}", message);
            return;
        }
        Long tenantId = message.getTenantId();
        try {
            int n = ibmsDeviceService.repushAllGatewayProfiles();
            log.info("[GatewayProfileWarmupRequestConsumer] tenantId={} 已重推 profile 条数={} source={}",
                    tenantId, n, message.getSource());
        } catch (Exception e) {
            log.error("[GatewayProfileWarmupRequestConsumer] 处理失败 tenantId={}", tenantId, e);
        }
    }
}
