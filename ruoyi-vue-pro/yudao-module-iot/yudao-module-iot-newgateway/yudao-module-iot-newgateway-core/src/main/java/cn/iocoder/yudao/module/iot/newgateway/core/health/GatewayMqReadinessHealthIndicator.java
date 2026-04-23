package cn.iocoder.yudao.module.iot.newgateway.core.health;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.newgateway.core.session.DeviceSessionRegistry;
import cn.iocoder.yudao.module.iot.newgateway.core.startup.DeviceInitRetryManager;
import cn.iocoder.yudao.module.iot.newgateway.device.GatewayDeviceProfileCache;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 网关就绪探针：要求使用 RocketMQ 总线（与 Biz 解耦的唯一通道）。
 */
@Component
@RequiredArgsConstructor
public class GatewayMqReadinessHealthIndicator implements HealthIndicator {

    private final IotMessageBus messageBus;
    private final GatewayDeviceProfileCache deviceProfileCache;
    private final DeviceSessionRegistry deviceSessionRegistry;
    private final DeviceInitRetryManager deviceInitRetryManager;

    @Override
    public Health health() {
        String bus = messageBus.getClass().getSimpleName();
        boolean rocket = bus.contains("RocketMQ") || bus.contains("IotRocketMQ");
        if (!rocket) {
            return Health.down()
                    .withDetail("messageBus", bus)
                    .withDetail("reason", "必须使用 RocketMQ 消息总线连接 Biz")
                    .build();
        }
        return Health.up()
                .withDetail("messageBus", bus)
                .withDetail("cachedDeviceProfiles", deviceProfileCache.size())
                .withDetail("registeredDeviceSessions", deviceSessionRegistry.size())
                .withDetail("startupRetryQueueSize", deviceInitRetryManager.getQueueSize())
                .withDetail("startupRetryQueuePending", deviceInitRetryManager.getPendingRetryQueueCount())
                .withDetail("startupRetryQueueExhausted", deviceInitRetryManager.getExhaustedRetryQueueCount())
                .build();
    }
}
