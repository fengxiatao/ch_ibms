package cn.iocoder.yudao.module.iot.mq.manager;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 设备命令响应管理器
 * 
 * <p>统一管理所有等待响应的设备命令请求，实现请求-响应模式。</p>
 * 
 * <p>使用方式：</p>
 * <ol>
 *   <li>调用 {@link #registerRequest(String)} 注册一个等待响应的请求</li>
 *   <li>发送命令到消息总线</li>
 *   <li>调用 {@link #waitForResponse(String, int)} 等待响应</li>
 *   <li>{@link cn.iocoder.yudao.module.iot.mq.consumer.device.DeviceServiceResultConsumer} 
 *       收到响应后调用 {@link #completeRequest(String, IotDeviceMessage)} 来完成请求</li>
 * </ol>
 * 
 * <p>此设计遵循统一消费者组架构：</p>
 * <ul>
 *   <li>只有 DeviceServiceResultConsumer (iot-biz-device-result) 消费 DEVICE_SERVICE_RESULT</li>
 *   <li>所有需要同步等待响应的服务都通过此管理器来等待</li>
 * </ul>
 *
 * @author 长辉信息科技有限公司
 */
@Slf4j
@Component
public class DeviceCommandResponseManager {

    /**
     * 等待响应的请求映射
     * Key: requestId
     * Value: CompletableFuture，用于等待响应
     */
    private final Map<String, CompletableFuture<IotDeviceMessage>> pendingRequests = new ConcurrentHashMap<>();

    /**
     * 注册一个等待响应的请求
     * 
     * @param requestId 请求ID
     * @return CompletableFuture，用于等待响应
     */
    public CompletableFuture<IotDeviceMessage> registerRequest(String requestId) {
        if (requestId == null || requestId.isEmpty()) {
            throw new IllegalArgumentException("requestId 不能为空");
        }
        
        CompletableFuture<IotDeviceMessage> future = new CompletableFuture<>();
        CompletableFuture<IotDeviceMessage> existing = pendingRequests.putIfAbsent(requestId, future);
        
        if (existing != null) {
            log.warn("[DeviceCommandResponseManager] requestId 已存在: {}", requestId);
            return existing;
        }
        
        // 使用 INFO 级别便于排查问题
        log.info("[DeviceCommandResponseManager] 注册请求: requestId={}, pendingCount={}", 
                requestId, pendingRequests.size());
        return future;
    }

    /**
     * 等待响应（带超时）
     * 
     * <p>重要：此方法需要传入 {@link #registerRequest(String)} 返回的 future，
     * 而不是重新从 pendingRequests 中获取，以避免竞态条件。</p>
     * 
     * @param requestId 请求ID
     * @param future 注册时返回的 CompletableFuture
     * @param timeoutSeconds 超时时间（秒）
     * @return 响应消息
     * @throws TimeoutException 超时异常
     */
    public IotDeviceMessage waitForResponse(String requestId, CompletableFuture<IotDeviceMessage> future, int timeoutSeconds) throws TimeoutException {
        if (future == null) {
            throw new IllegalStateException("future 不能为空: requestId=" + requestId);
        }
        
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            pendingRequests.remove(requestId);
            // 增加详细日志，记录当前等待中的请求数量
            log.warn("[DeviceCommandResponseManager] ⏰ 等待响应超时: requestId={}, timeout={}s, pendingCount={}（可能是 RocketMQ 消费者未收到消息）", 
                    requestId, timeoutSeconds, pendingRequests.size());
            throw new TimeoutException("等待响应超时: requestId=" + requestId + ", timeout=" + timeoutSeconds + "s");
        } catch (Exception e) {
            pendingRequests.remove(requestId);
            throw new RuntimeException("等待响应失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 等待响应（带超时）- 兼容旧调用
     * 
     * @param requestId 请求ID
     * @param timeoutSeconds 超时时间（秒）
     * @return 响应消息
     * @throws TimeoutException 超时异常
     * @deprecated 请使用 {@link #waitForResponse(String, CompletableFuture, int)} 避免竞态条件
     */
    @Deprecated
    public IotDeviceMessage waitForResponse(String requestId, int timeoutSeconds) throws TimeoutException {
        CompletableFuture<IotDeviceMessage> future = pendingRequests.get(requestId);
        if (future == null) {
            throw new IllegalStateException("请求未注册: requestId=" + requestId);
        }
        return waitForResponse(requestId, future, timeoutSeconds);
    }

    /**
     * 完成请求（由 DeviceServiceResultConsumer 调用）
     * 
     * @param requestId 请求ID
     * @param response 响应消息
     * @return 是否成功匹配到等待的请求
     */
    public boolean completeRequest(String requestId, IotDeviceMessage response) {
        if (requestId == null) {
            return false;
        }
        
        CompletableFuture<IotDeviceMessage> future = pendingRequests.remove(requestId);
        if (future != null) {
            future.complete(response);
            // 使用 INFO 级别便于排查问题
            log.info("[DeviceCommandResponseManager] ✅ 响应已匹配: requestId={}, deviceId={}, method={}, pendingCount={}", 
                    requestId, response.getDeviceId(), response.getMethod(), pendingRequests.size());
            return true;
        }
        
        // 没有等待这个响应的请求，记录日志便于排查
        log.debug("[DeviceCommandResponseManager] 响应无匹配的等待请求（可能是异步命令）: requestId={}, deviceId={}", 
                requestId, response != null ? response.getDeviceId() : "null");
        return false;
    }

    /**
     * 取消等待的请求
     * 
     * @param requestId 请求ID
     */
    public void cancelRequest(String requestId) {
        if (requestId == null) {
            return;
        }
        
        CompletableFuture<IotDeviceMessage> future = pendingRequests.remove(requestId);
        if (future != null) {
            future.cancel(true);
            log.debug("[DeviceCommandResponseManager] 请求已取消: requestId={}", requestId);
        }
    }

    /**
     * 获取当前等待中的请求数量（用于监控）
     * 
     * @return 等待中的请求数量
     */
    public int getPendingRequestCount() {
        return pendingRequests.size();
    }

    /**
     * 检查是否有指定请求正在等待（用于测试）
     * 
     * @param requestId 请求ID
     * @return 是否正在等待
     */
    public boolean hasPendingRequest(String requestId) {
        return pendingRequests.containsKey(requestId);
    }
}
