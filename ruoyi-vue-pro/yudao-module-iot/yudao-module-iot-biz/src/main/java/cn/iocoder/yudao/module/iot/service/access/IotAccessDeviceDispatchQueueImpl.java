package cn.iocoder.yudao.module.iot.service.access;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 设备下发队列实现类
 * 
 * 核心设计：
 * 1. 每个设备有独立的任务队列
 * 2. 同一设备的任务串行执行（通过单线程执行器）
 * 3. 不同设备的任务可以并发执行（受全局线程池限制）
 * 
 * Requirements: 10.2, 10.4
 *
 * @author Kiro
 */
@Slf4j
@Service
public class IotAccessDeviceDispatchQueueImpl implements IotAccessDeviceDispatchQueue {

    /**
     * 全局最大并发数
     */
    @Value("${iot.access.dispatch.max-concurrent:10}")
    private int maxConcurrent;

    /**
     * 设备队列映射：deviceId -> DeviceQueue
     */
    private final ConcurrentHashMap<Long, DeviceQueue> deviceQueues = new ConcurrentHashMap<>();

    /**
     * 全局线程池（限制总并发数）
     */
    private ExecutorService globalExecutor;

    /**
     * 信号量（控制全局并发数）
     */
    private Semaphore globalSemaphore;

    /**
     * 统计：已完成任务数
     */
    private final AtomicLong completedTaskCount = new AtomicLong(0);

    /**
     * 统计：失败任务数
     */
    private final AtomicLong failedTaskCount = new AtomicLong(0);

    /**
     * 是否已关闭
     */
    private volatile boolean shutdown = false;

    @PostConstruct
    public void init() {
        globalExecutor = Executors.newFixedThreadPool(maxConcurrent, r -> {
            Thread t = new Thread(r, "access-dispatch-worker");
            t.setDaemon(true);
            return t;
        });
        globalSemaphore = new Semaphore(maxConcurrent);
        log.info("[init] 设备下发队列初始化完成, maxConcurrent={}", maxConcurrent);
    }

    @PreDestroy
    @Override
    public void shutdown() {
        shutdown = true;
        
        // 取消所有待执行任务
        for (DeviceQueue queue : deviceQueues.values()) {
            queue.cancelAll();
        }
        
        // 关闭线程池
        globalExecutor.shutdown();
        try {
            if (!globalExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                globalExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            globalExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log.info("[shutdown] 设备下发队列已关闭");
    }

    @Override
    public void submit(Long deviceId, Runnable task) {
        if (shutdown) {
            log.warn("[submit] 队列已关闭，拒绝任务: deviceId={}", deviceId);
            return;
        }
        
        // 获取或创建设备队列
        DeviceQueue deviceQueue = deviceQueues.computeIfAbsent(deviceId, 
                id -> new DeviceQueue(id, globalExecutor, globalSemaphore, 
                        completedTaskCount, failedTaskCount));
        
        // 提交任务
        deviceQueue.submit(task);
        
        log.debug("[submit] 任务已提交: deviceId={}, pendingCount={}", 
                deviceId, deviceQueue.getPendingCount());
    }

    @Override
    public QueueStatus getQueueStatus(Long deviceId) {
        DeviceQueue queue = deviceQueues.get(deviceId);
        if (queue == null) {
            return new QueueStatus(deviceId, 0, false, null);
        }
        return new QueueStatus(deviceId, queue.getPendingCount(), 
                queue.isExecuting(), queue.getLastExecuteTime());
    }

    @Override
    public int cancelPendingTasks(Long deviceId) {
        DeviceQueue queue = deviceQueues.get(deviceId);
        if (queue == null) {
            return 0;
        }
        return queue.cancelAll();
    }

    @Override
    public QueueStatistics getStatistics() {
        int activeDeviceCount = 0;
        int totalPendingCount = 0;
        int executingDeviceCount = 0;
        
        for (DeviceQueue queue : deviceQueues.values()) {
            int pending = queue.getPendingCount();
            if (pending > 0 || queue.isExecuting()) {
                activeDeviceCount++;
                totalPendingCount += pending;
                if (queue.isExecuting()) {
                    executingDeviceCount++;
                }
            }
        }
        
        return new QueueStatistics(activeDeviceCount, totalPendingCount, executingDeviceCount,
                completedTaskCount.get(), failedTaskCount.get());
    }

    /**
     * 设备队列
     * 确保同一设备的任务串行执行
     */
    private static class DeviceQueue {
        private final Long deviceId;
        private final ExecutorService globalExecutor;
        private final Semaphore globalSemaphore;
        private final AtomicLong completedCounter;
        private final AtomicLong failedCounter;
        
        /**
         * 任务队列
         */
        private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
        
        /**
         * 是否正在执行
         */
        private final AtomicBoolean executing = new AtomicBoolean(false);
        
        /**
         * 最后执行时间
         */
        private volatile Long lastExecuteTime;

        DeviceQueue(Long deviceId, ExecutorService globalExecutor, Semaphore globalSemaphore,
                   AtomicLong completedCounter, AtomicLong failedCounter) {
            this.deviceId = deviceId;
            this.globalExecutor = globalExecutor;
            this.globalSemaphore = globalSemaphore;
            this.completedCounter = completedCounter;
            this.failedCounter = failedCounter;
        }

        /**
         * 提交任务
         */
        void submit(Runnable task) {
            taskQueue.offer(task);
            tryExecuteNext();
        }

        /**
         * 尝试执行下一个任务
         */
        private void tryExecuteNext() {
            // 如果已经在执行，则不重复启动
            if (!executing.compareAndSet(false, true)) {
                return;
            }
            
            // 获取下一个任务
            Runnable task = taskQueue.poll();
            if (task == null) {
                executing.set(false);
                return;
            }
            
            // 提交到全局线程池执行
            globalExecutor.submit(() -> {
                try {
                    // 获取全局信号量（限制总并发数）
                    globalSemaphore.acquire();
                    try {
                        lastExecuteTime = System.currentTimeMillis();
                        task.run();
                        completedCounter.incrementAndGet();
                    } finally {
                        globalSemaphore.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failedCounter.incrementAndGet();
                } catch (Exception e) {
                    log.error("[DeviceQueue] 任务执行异常: deviceId={}", deviceId, e);
                    failedCounter.incrementAndGet();
                } finally {
                    executing.set(false);
                    // 继续执行下一个任务
                    if (!taskQueue.isEmpty()) {
                        tryExecuteNext();
                    }
                }
            });
        }

        /**
         * 取消所有待执行任务
         */
        int cancelAll() {
            int count = 0;
            while (taskQueue.poll() != null) {
                count++;
            }
            return count;
        }

        int getPendingCount() {
            return taskQueue.size();
        }

        boolean isExecuting() {
            return executing.get();
        }

        Long getLastExecuteTime() {
            return lastExecuteTime;
        }
    }
}
