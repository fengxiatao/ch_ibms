package cn.iocoder.yudao.module.iot.newgateway.core.startup;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 初始化统计信息
 * <p>
 * 线程安全的统计类，用于记录设备初始化的结果统计。
 * 支持按设备类型分组统计。
 * </p>
 *
 * @author IoT Gateway Team
 */
@Data
public class InitializationStatistics {

    /**
     * 总设备数
     */
    private final AtomicInteger totalCount = new AtomicInteger(0);

    /**
     * 成功数
     */
    private final AtomicInteger successCount = new AtomicInteger(0);

    /**
     * 失败数
     */
    private final AtomicInteger failedCount = new AtomicInteger(0);

    /**
     * 跳过数
     */
    private final AtomicInteger skippedCount = new AtomicInteger(0);

    /**
     * 开始时间（毫秒）
     */
    private volatile long startTime;

    /**
     * 结束时间（毫秒）
     */
    private volatile long endTime;

    /**
     * 按设备类型统计
     */
    private final Map<String, TypeStatistics> byDeviceType = new ConcurrentHashMap<>();

    /**
     * 开始统计
     */
    public void start() {
        this.startTime = System.currentTimeMillis();
        this.endTime = 0;
    }

    /**
     * 结束统计
     */
    public void finish() {
        this.endTime = System.currentTimeMillis();
    }

    /**
     * 设置总设备数
     *
     * @param total 总数
     */
    public void setTotal(int total) {
        this.totalCount.set(total);
    }

    /**
     * 记录成功
     *
     * @param deviceType 设备类型
     */
    public void recordSuccess(String deviceType) {
        successCount.incrementAndGet();
        getOrCreateTypeStats(deviceType).recordSuccess();
    }

    /**
     * 记录失败
     *
     * @param deviceType 设备类型
     * @param reason     失败原因
     */
    public void recordFailure(String deviceType, String reason) {
        failedCount.incrementAndGet();
        getOrCreateTypeStats(deviceType).recordFailure(reason);
    }

    /**
     * 记录跳过
     *
     * @param deviceType 设备类型
     * @param reason     跳过原因
     */
    public void recordSkipped(String deviceType, String reason) {
        skippedCount.incrementAndGet();
        getOrCreateTypeStats(deviceType).recordSkipped(reason);
    }

    /**
     * 记录初始化结果
     *
     * @param result 初始化结果
     */
    public void record(InitializationResult result) {
        if (result == null) {
            return;
        }

        String deviceType = result.getDeviceType() != null ? result.getDeviceType() : "UNKNOWN";

        switch (result.getStatus()) {
            case SUCCESS:
                recordSuccess(deviceType);
                break;
            case FAILED:
            case TIMEOUT:
                recordFailure(deviceType, result.getErrorMessage());
                break;
            case SKIPPED:
                recordSkipped(deviceType, result.getErrorMessage());
                break;
        }
    }

    /**
     * 获取总耗时（毫秒）
     *
     * @return 总耗时
     */
    public long getTotalTimeMs() {
        if (startTime == 0) {
            return 0;
        }
        long end = endTime > 0 ? endTime : System.currentTimeMillis();
        return end - startTime;
    }

    /**
     * 获取成功率（百分比）
     *
     * @return 成功率
     */
    public double getSuccessRate() {
        int total = totalCount.get();
        if (total == 0) {
            return 0.0;
        }
        return (successCount.get() * 100.0) / total;
    }

    /**
     * 生成汇总报告
     *
     * @return 汇总报告字符串
     */
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== 设备初始化统计报告 ==========\n");
        sb.append(String.format("总设备数: %d\n", totalCount.get()));
        sb.append(String.format("成功: %d\n", successCount.get()));
        sb.append(String.format("失败: %d\n", failedCount.get()));
        sb.append(String.format("跳过: %d\n", skippedCount.get()));
        sb.append(String.format("成功率: %.1f%%\n", getSuccessRate()));
        sb.append(String.format("总耗时: %d ms\n", getTotalTimeMs()));

        if (!byDeviceType.isEmpty()) {
            sb.append("\n---------- 按设备类型统计 ----------\n");
            byDeviceType.forEach((type, stats) -> {
                sb.append(String.format("  %s: 成功=%d, 失败=%d, 跳过=%d\n",
                        type, stats.getSuccessCount().get(),
                        stats.getFailedCount().get(), stats.getSkippedCount().get()));
            });
        }

        sb.append("==========================================\n");
        return sb.toString();
    }

    /**
     * 重置统计
     */
    public void reset() {
        totalCount.set(0);
        successCount.set(0);
        failedCount.set(0);
        skippedCount.set(0);
        startTime = 0;
        endTime = 0;
        byDeviceType.clear();
    }

    /**
     * 验证统计一致性
     * <p>
     * 检查 successCount + failedCount + skippedCount == totalCount
     * </p>
     *
     * @return true 如果统计一致
     */
    public boolean isConsistent() {
        int sum = successCount.get() + failedCount.get() + skippedCount.get();
        return sum == totalCount.get();
    }

    // ==================== 私有方法 ====================

    private TypeStatistics getOrCreateTypeStats(String deviceType) {
        String type = deviceType != null ? deviceType : "UNKNOWN";
        return byDeviceType.computeIfAbsent(type, k -> new TypeStatistics());
    }

    // ==================== 内部类 ====================

    /**
     * 按设备类型的统计
     */
    @Data
    public static class TypeStatistics {
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failedCount = new AtomicInteger(0);
        private final AtomicInteger skippedCount = new AtomicInteger(0);
        private volatile String lastFailureReason;
        private volatile String lastSkipReason;

        public void recordSuccess() {
            successCount.incrementAndGet();
        }

        public void recordFailure(String reason) {
            failedCount.incrementAndGet();
            this.lastFailureReason = reason;
        }

        public void recordSkipped(String reason) {
            skippedCount.incrementAndGet();
            this.lastSkipReason = reason;
        }

        public int getTotal() {
            return successCount.get() + failedCount.get() + skippedCount.get();
        }
    }
}
