package cn.iocoder.yudao.module.iot.simulator.core;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 模拟器监控指标
 *
 * @author Kiro
 */
@Getter
public class SimulatorMetrics {

    /**
     * 创建的模拟器数量
     */
    private final AtomicLong createdCount = new AtomicLong(0);

    /**
     * 停止的模拟器数量
     */
    private final AtomicLong stoppedCount = new AtomicLong(0);

    /**
     * 发送的消息数量
     */
    private final AtomicLong sentMessageCount = new AtomicLong(0);

    /**
     * 接收的消息数量
     */
    private final AtomicLong receivedMessageCount = new AtomicLong(0);

    /**
     * 发送的字节数
     */
    private final AtomicLong sentBytes = new AtomicLong(0);

    /**
     * 接收的字节数
     */
    private final AtomicLong receivedBytes = new AtomicLong(0);

    /**
     * 错误数量
     */
    private final AtomicLong errorCount = new AtomicLong(0);

    /**
     * 连接成功次数
     */
    private final AtomicLong connectionSuccessCount = new AtomicLong(0);

    /**
     * 连接失败次数
     */
    private final AtomicLong connectionFailureCount = new AtomicLong(0);

    public void incrementCreatedCount() {
        createdCount.incrementAndGet();
    }

    public void incrementStoppedCount() {
        stoppedCount.incrementAndGet();
    }

    public void incrementSentMessageCount() {
        sentMessageCount.incrementAndGet();
    }

    public void incrementReceivedMessageCount() {
        receivedMessageCount.incrementAndGet();
    }

    public void addSentBytes(long bytes) {
        sentBytes.addAndGet(bytes);
    }

    public void addReceivedBytes(long bytes) {
        receivedBytes.addAndGet(bytes);
    }

    public void incrementErrorCount() {
        errorCount.incrementAndGet();
    }

    public void incrementConnectionSuccessCount() {
        connectionSuccessCount.incrementAndGet();
    }

    public void incrementConnectionFailureCount() {
        connectionFailureCount.incrementAndGet();
    }

    /**
     * 重置所有指标
     */
    public void reset() {
        createdCount.set(0);
        stoppedCount.set(0);
        sentMessageCount.set(0);
        receivedMessageCount.set(0);
        sentBytes.set(0);
        receivedBytes.set(0);
        errorCount.set(0);
        connectionSuccessCount.set(0);
        connectionFailureCount.set(0);
    }

    @Override
    public String toString() {
        return String.format(
            "SimulatorMetrics{created=%d, stopped=%d, sentMsg=%d, recvMsg=%d, " +
            "sentBytes=%d, recvBytes=%d, errors=%d, connSuccess=%d, connFail=%d}",
            createdCount.get(), stoppedCount.get(),
            sentMessageCount.get(), receivedMessageCount.get(),
            sentBytes.get(), receivedBytes.get(),
            errorCount.get(), connectionSuccessCount.get(), connectionFailureCount.get()
        );
    }
}
