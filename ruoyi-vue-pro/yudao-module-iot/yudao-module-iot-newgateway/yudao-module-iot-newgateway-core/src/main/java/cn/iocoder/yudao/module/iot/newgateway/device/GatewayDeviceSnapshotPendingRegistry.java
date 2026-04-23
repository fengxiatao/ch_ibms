package cn.iocoder.yudao.module.iot.newgateway.device;

import cn.iocoder.yudao.module.iot.core.mq.message.GatewayDeviceSnapshotReplyMessage;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Request-Reply：等待 Biz 侧快照应答。
 */
@Component
public class GatewayDeviceSnapshotPendingRegistry {

    private final ConcurrentHashMap<String, CompletableFuture<GatewayDeviceSnapshotReplyMessage>> pending =
            new ConcurrentHashMap<>();

    public CompletableFuture<GatewayDeviceSnapshotReplyMessage> register(String correlationId) {
        CompletableFuture<GatewayDeviceSnapshotReplyMessage> f = new CompletableFuture<>();
        pending.put(correlationId, f);
        return f;
    }

    public void complete(GatewayDeviceSnapshotReplyMessage reply) {
        if (reply == null || reply.getCorrelationId() == null) {
            return;
        }
        CompletableFuture<GatewayDeviceSnapshotReplyMessage> f = pending.remove(reply.getCorrelationId());
        if (f != null) {
            f.complete(reply);
        }
    }

    public void completeExceptionally(String correlationId, Throwable ex) {
        if (correlationId == null) {
            return;
        }
        CompletableFuture<GatewayDeviceSnapshotReplyMessage> f = pending.remove(correlationId);
        if (f != null) {
            f.completeExceptionally(ex != null ? ex : new java.util.concurrent.TimeoutException());
        }
    }
}
