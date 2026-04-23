package cn.iocoder.yudao.module.iot.newgateway.device;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.GatewayDeviceSnapshotReplyMessage;
import cn.iocoder.yudao.module.iot.core.mq.message.GatewayDeviceSnapshotRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 经 MQ 向 Biz 请求单设备快照（阻塞等待应答，仅供网关侧补缓存使用）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayDeviceSnapshotClient {

    private final IotMessageBus messageBus;
    private final GatewayDeviceSnapshotPendingRegistry pendingRegistry;

    public IotDeviceRespDTO fetchSnapshot(Long deviceId, Long tenantId, Duration timeout) throws Exception {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<GatewayDeviceSnapshotReplyMessage> future = pendingRegistry.register(correlationId);
        GatewayDeviceSnapshotRequestMessage req = GatewayDeviceSnapshotRequestMessage.builder()
                .correlationId(correlationId)
                .deviceId(deviceId)
                .tenantId(tenantId)
                .source("iot-newgateway")
                .build();
        messageBus.post(IotMessageTopics.GATEWAY_DEVICE_SNAPSHOT_REQUEST, req);
        long ms = timeout != null ? timeout.toMillis() : 30_000L;
        try {
            GatewayDeviceSnapshotReplyMessage reply = future.get(ms, TimeUnit.MILLISECONDS);
            if (reply == null) {
                return null;
            }
            if (reply.getCode() != null && reply.getCode().equals(GlobalErrorCodeConstants.SUCCESS.getCode())
                    && reply.getDevice() != null) {
                return reply.getDevice();
            }
            log.warn("[GatewayDeviceSnapshotClient] 快照失败 deviceId={} code={} msg={}",
                    deviceId, reply.getCode(), reply.getMsg());
            return null;
        } catch (TimeoutException e) {
            pendingRegistry.completeExceptionally(correlationId, e);
            throw e;
        }
    }
}
