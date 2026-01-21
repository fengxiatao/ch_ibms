package cn.iocoder.yudao.module.iot.newgateway.core.startup;

import cn.iocoder.yudao.module.iot.newgateway.core.config.GatewayStartupProperties;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * 设备初始化重试管理器
 * <p>
 * 管理失败设备的重试逻辑，支持指数退避策略。
 * </p>
 *
 * @author IoT Gateway Team
 */
@Slf4j
@Component
public class DeviceInitRetryManager {

    private final GatewayStartupProperties properties;

    /**
     * 重试队列：deviceId -> RetryContext
     */
    private final Map<Long, RetryContext> retryQueue = new ConcurrentHashMap<>();

    /**
     * 调度执行器
     */
    private final ScheduledExecutorService scheduler;

    /**
     * 重试回调
     */
    private volatile Consumer<RetryContext> retryCallback;

    /**
     * 是否已关闭
     */
    private volatile boolean shutdown = false;

    public DeviceInitRetryManager(GatewayStartupProperties properties) {
        this.properties = properties;
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "device-init-retry");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 设置重试回调
     *
     * @param callback 重试回调函数
     */
    public void setRetryCallback(Consumer<RetryContext> callback) {
        this.retryCallback = callback;
    }

    /**
     * 调度重试
     *
     * @param deviceId       设备ID
     * @param deviceName     设备名称
     * @param connectionInfo 连接信息
     * @param failureReason  失败原因
     */
    public void scheduleRetry(Long deviceId, String deviceName, 
                              DeviceConnectionInfo connectionInfo, String failureReason) {
        if (shutdown) {
            log.warn("[GatewayStartup] 重试管理器已关闭，忽略重试请求: deviceId={}", deviceId);
            return;
        }

        RetryContext context = retryQueue.computeIfAbsent(deviceId, 
                id -> RetryContext.create(id, deviceName, connectionInfo));

        // 检查是否达到最大重试次数
        if (context.getRetryCount() >= properties.getMaxRetryCount()) {
            context.setMaxRetriesReached(true);
            log.warn("[GatewayStartup] 设备达到最大重试次数，停止重试: deviceId={}, retryCount={}, lastReason={}",
                    deviceId, context.getRetryCount(), failureReason);
            return;
        }

        // 记录失败信息
        context.recordFailure(failureReason);

        // 计算下次重试时间（指数退避）
        long initialIntervalMs = properties.getInitialRetryIntervalSeconds() * 1000L;
        context.calculateNextRetryTime(initialIntervalMs);

        long delayMs = context.getRemainingWaitTime();
        log.info("[GatewayStartup] 调度设备重试: deviceId={}, retryCount={}, delayMs={}, reason={}",
                deviceId, context.getRetryCount(), delayMs, failureReason);

        // 调度重试任务
        scheduler.schedule(() -> executeRetry(deviceId), delayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行重试
     *
     * @param deviceId 设备ID
     */
    public void executeRetry(Long deviceId) {
        if (shutdown) {
            return;
        }

        RetryContext context = retryQueue.get(deviceId);
        if (context == null) {
            log.warn("[GatewayStartup] 重试时未找到上下文: deviceId={}", deviceId);
            return;
        }

        if (context.isMaxRetriesReached()) {
            log.debug("[GatewayStartup] 设备已达到最大重试次数，跳过: deviceId={}", deviceId);
            return;
        }

        // 增加重试次数
        context.incrementRetryCount();

        log.info("[GatewayStartup] 执行设备重试: deviceId={}, retryCount={}/{}",
                deviceId, context.getRetryCount(), properties.getMaxRetryCount());

        // 调用重试回调
        if (retryCallback != null) {
            try {
                retryCallback.accept(context);
            } catch (Exception e) {
                log.error("[GatewayStartup] 重试回调执行异常: deviceId={}, error={}", deviceId, e.getMessage(), e);
                // 重试失败，继续调度下一次重试
                scheduleRetry(deviceId, context.getDeviceName(), 
                        context.getConnectionInfo(), e.getMessage());
            }
        }
    }

    /**
     * 记录重试失败并调度下一次重试
     *
     * @param deviceId 设备ID
     * @param reason   失败原因
     */
    public void recordFailureAndScheduleNext(Long deviceId, String reason) {
        RetryContext context = retryQueue.get(deviceId);
        if (context == null) {
            return;
        }

        scheduleRetry(deviceId, context.getDeviceName(), context.getConnectionInfo(), reason);
    }

    /**
     * 获取重试次数
     *
     * @param deviceId 设备ID
     * @return 重试次数
     */
    public int getRetryCount(Long deviceId) {
        RetryContext context = retryQueue.get(deviceId);
        return context != null ? context.getRetryCount() : 0;
    }

    /**
     * 重置重试计数（重试成功后调用）
     *
     * @param deviceId 设备ID
     */
    public void resetRetryCount(Long deviceId) {
        retryQueue.remove(deviceId);
        log.debug("[GatewayStartup] 重置重试计数: deviceId={}", deviceId);
    }

    /**
     * 取消重试
     *
     * @param deviceId 设备ID
     */
    public void cancelRetry(Long deviceId) {
        RetryContext removed = retryQueue.remove(deviceId);
        if (removed != null) {
            log.info("[GatewayStartup] 取消设备重试: deviceId={}", deviceId);
        }
    }

    /**
     * 获取所有待重试设备ID
     *
     * @return 待重试设备ID列表
     */
    public List<Long> getPendingRetryDevices() {
        List<Long> pending = new ArrayList<>();
        retryQueue.forEach((deviceId, context) -> {
            if (!context.isMaxRetriesReached()) {
                pending.add(deviceId);
            }
        });
        return pending;
    }

    /**
     * 获取所有失败设备ID（达到最大重试次数）
     *
     * @return 失败设备ID列表
     */
    public List<Long> getFailedDevices() {
        List<Long> failed = new ArrayList<>();
        retryQueue.forEach((deviceId, context) -> {
            if (context.isMaxRetriesReached()) {
                failed.add(deviceId);
            }
        });
        return failed;
    }

    /**
     * 获取重试上下文
     *
     * @param deviceId 设备ID
     * @return 重试上下文
     */
    public RetryContext getRetryContext(Long deviceId) {
        return retryQueue.get(deviceId);
    }

    /**
     * 获取重试队列大小
     *
     * @return 队列大小
     */
    public int getQueueSize() {
        return retryQueue.size();
    }

    /**
     * 计算指数退避时间（毫秒）
     *
     * @param retryCount 重试次数（从 0 开始）
     * @return 退避时间（毫秒）
     */
    public long calculateBackoffMs(int retryCount) {
        return properties.calculateBackoffMs(retryCount);
    }

    /**
     * 手动触发所有失败设备的重试
     */
    public void retryAllFailed() {
        log.info("[GatewayStartup] 手动触发所有失败设备重试");
        retryQueue.forEach((deviceId, context) -> {
            if (context.isMaxRetriesReached()) {
                // 重置状态，允许重新重试
                context.setMaxRetriesReached(false);
                context.setRetryCount(0);
                executeRetry(deviceId);
            }
        });
    }

    /**
     * 清空重试队列
     */
    public void clear() {
        retryQueue.clear();
        log.info("[GatewayStartup] 清空重试队列");
    }

    @PreDestroy
    public void shutdown() {
        shutdown = true;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("[GatewayStartup] 重试管理器已关闭");
    }
}
