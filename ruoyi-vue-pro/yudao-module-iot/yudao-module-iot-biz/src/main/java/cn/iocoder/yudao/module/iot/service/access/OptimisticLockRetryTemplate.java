package cn.iocoder.yudao.module.iot.service.access;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 乐观锁重试模板
 * 
 * 用于处理乐观锁冲突时的自动重试逻辑，
 * 采用指数退避策略，最多重试 3 次。
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class OptimisticLockRetryTemplate {

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRIES = 3;

    /**
     * 基础等待时间（毫秒）
     */
    private static final long BASE_WAIT_MS = 50;

    /**
     * 执行带重试的操作
     *
     * @param operation 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     * @throws OptimisticLockingFailureException 如果所有重试都失败
     */
    public <T> T executeWithRetry(Supplier<T> operation) {
        return executeWithRetry(operation, MAX_RETRIES);
    }

    /**
     * 执行带重试的操作（自定义重试次数）
     *
     * @param operation 要执行的操作
     * @param maxRetries 最大重试次数
     * @param <T> 返回值类型
     * @return 操作结果
     * @throws OptimisticLockingFailureException 如果所有重试都失败
     */
    public <T> T executeWithRetry(Supplier<T> operation, int maxRetries) {
        int attempts = 0;
        OptimisticLockingFailureException lastException = null;
        
        while (attempts < maxRetries) {
            try {
                return operation.get();
            } catch (OptimisticLockingFailureException e) {
                attempts++;
                lastException = e;
                log.warn("[executeWithRetry] 乐观锁冲突，第 {} 次重试", attempts);
                
                if (attempts < maxRetries) {
                    // 指数退避等待
                    try {
                        long waitTime = BASE_WAIT_MS * (1L << (attempts - 1));
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试等待被中断", ie);
                    }
                }
            }
        }
        
        log.error("[executeWithRetry] 乐观锁冲突，已达最大重试次数 {}", maxRetries);
        throw lastException;
    }

    /**
     * 执行带重试的操作（无返回值）
     *
     * @param operation 要执行的操作
     * @throws OptimisticLockingFailureException 如果所有重试都失败
     */
    public void executeWithRetry(Runnable operation) {
        executeWithRetry(() -> {
            operation.run();
            return null;
        });
    }
}
