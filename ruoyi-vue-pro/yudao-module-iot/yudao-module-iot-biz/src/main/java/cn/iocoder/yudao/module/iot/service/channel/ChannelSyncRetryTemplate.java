package cn.iocoder.yudao.module.iot.service.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 通道同步重试模板
 * 
 * <p>用于处理通道同步失败时的自动重试逻辑，
 * 采用指数退避策略，最多重试 3 次。</p>
 * 
 * <p>指数退避间隔：</p>
 * <ul>
 *     <li>第1次重试：1秒后</li>
 *     <li>第2次重试：2秒后</li>
 *     <li>第3次重试：4秒后</li>
 * </ul>
 * 
 * <p>Requirements: 9.7</p>
 *
 * @author 长辉信息科技有限公司
 */
@Slf4j
@Component
public class ChannelSyncRetryTemplate {

    /**
     * 最大重试次数
     */
    public static final int MAX_RETRIES = 3;

    /**
     * 基础等待时间（毫秒）- 1秒
     */
    public static final long BASE_WAIT_MS = 1000;

    /**
     * 执行带重试的通道同步操作
     *
     * @param operation 要执行的同步操作
     * @param deviceId 设备ID（用于日志）
     * @return 同步结果
     */
    public IotDeviceChannelService.AccessChannelSyncResult executeWithRetry(
            Supplier<IotDeviceChannelService.AccessChannelSyncResult> operation,
            Long deviceId) {
        return executeWithRetry(operation, deviceId, MAX_RETRIES);
    }

    /**
     * 执行带重试的通道同步操作（自定义重试次数）
     *
     * @param operation 要执行的同步操作
     * @param deviceId 设备ID（用于日志）
     * @param maxRetries 最大重试次数
     * @return 同步结果
     */
    public IotDeviceChannelService.AccessChannelSyncResult executeWithRetry(
            Supplier<IotDeviceChannelService.AccessChannelSyncResult> operation,
            Long deviceId,
            int maxRetries) {
        
        int attempts = 0;
        Exception lastException = null;
        IotDeviceChannelService.AccessChannelSyncResult lastResult = null;
        
        while (attempts <= maxRetries) {
            try {
                IotDeviceChannelService.AccessChannelSyncResult result = operation.get();
                
                // 如果同步成功，直接返回
                if (result.isSuccess()) {
                    if (attempts > 0) {
                        log.info("[ChannelSyncRetry] ✅ 通道同步在第 {} 次重试后成功: deviceId={}", 
                                attempts, deviceId);
                    }
                    return result;
                }
                
                // 同步失败，记录结果
                lastResult = result;
                attempts++;
                
                if (attempts <= maxRetries) {
                    long waitTime = calculateWaitTime(attempts);
                    log.warn("[ChannelSyncRetry] 通道同步失败，第 {} 次重试（共 {} 次）: deviceId={}, error={}, 等待 {}ms",
                            attempts, maxRetries, deviceId, result.getErrorMessage(), waitTime);
                    
                    // 指数退避等待
                    sleep(waitTime);
                }
                
            } catch (Exception e) {
                lastException = e;
                attempts++;
                
                if (attempts <= maxRetries) {
                    long waitTime = calculateWaitTime(attempts);
                    log.error("[ChannelSyncRetry] 通道同步异常，第 {} 次重试（共 {} 次）: deviceId={}, error={}, 等待 {}ms",
                            attempts, maxRetries, deviceId, e.getMessage(), waitTime);
                    
                    // 指数退避等待
                    sleep(waitTime);
                }
            }
        }
        
        // 所有重试都失败
        String errorMessage;
        if (lastException != null) {
            errorMessage = String.format("通道同步失败，已重试 %d 次: %s", maxRetries, lastException.getMessage());
            log.error("[ChannelSyncRetry] ❌ 通道同步最终失败: deviceId={}, retries={}, error={}",
                    deviceId, maxRetries, lastException.getMessage(), lastException);
        } else if (lastResult != null) {
            errorMessage = String.format("通道同步失败，已重试 %d 次: %s", maxRetries, lastResult.getErrorMessage());
            log.error("[ChannelSyncRetry] ❌ 通道同步最终失败: deviceId={}, retries={}, error={}",
                    deviceId, maxRetries, lastResult.getErrorMessage());
        } else {
            errorMessage = String.format("通道同步失败，已重试 %d 次", maxRetries);
            log.error("[ChannelSyncRetry] ❌ 通道同步最终失败: deviceId={}, retries={}", deviceId, maxRetries);
        }
        
        return IotDeviceChannelService.AccessChannelSyncResult.failure(errorMessage);
    }

    /**
     * 计算指数退避等待时间
     * <p>
     * 公式：waitTime = BASE_WAIT_MS * 2^(attempt-1)
     * <ul>
     *     <li>第1次重试：1000 * 2^0 = 1000ms (1秒)</li>
     *     <li>第2次重试：1000 * 2^1 = 2000ms (2秒)</li>
     *     <li>第3次重试：1000 * 2^2 = 4000ms (4秒)</li>
     * </ul>
     *
     * @param attempt 当前重试次数（从1开始）
     * @return 等待时间（毫秒）
     */
    long calculateWaitTime(int attempt) {
        return BASE_WAIT_MS * (1L << (attempt - 1));
    }

    /**
     * 休眠指定时间
     *
     * @param millis 休眠时间（毫秒）
     */
    void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[ChannelSyncRetry] 重试等待被中断");
        }
    }

    /**
     * 获取最大重试次数（用于测试）
     *
     * @return 最大重试次数
     */
    public int getMaxRetries() {
        return MAX_RETRIES;
    }

    /**
     * 获取基础等待时间（用于测试）
     *
     * @return 基础等待时间（毫秒）
     */
    public long getBaseWaitMs() {
        return BASE_WAIT_MS;
    }
}
