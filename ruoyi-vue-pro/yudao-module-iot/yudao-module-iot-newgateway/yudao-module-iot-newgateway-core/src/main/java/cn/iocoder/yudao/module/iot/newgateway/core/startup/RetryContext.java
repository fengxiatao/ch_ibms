package cn.iocoder.yudao.module.iot.newgateway.core.startup;

import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo;
import lombok.Builder;
import lombok.Data;

/**
 * 重试上下文
 * <p>
 * 保存设备初始化重试所需的上下文信息。
 * </p>
 *
 * @author IoT Gateway Team
 */
@Data
@Builder
public class RetryContext {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备连接信息
     */
    private DeviceConnectionInfo connectionInfo;

    /**
     * 重试次数（从 0 开始）
     */
    private int retryCount;

    /**
     * 最后失败原因
     */
    private String lastFailureReason;

    /**
     * 下次重试时间（毫秒时间戳）
     */
    private long nextRetryTime;

    /**
     * 创建时间（毫秒时间戳）
     */
    private long createTime;

    /**
     * 最后重试时间（毫秒时间戳）
     */
    private long lastRetryTime;

    /**
     * 是否已达到最大重试次数
     */
    private boolean maxRetriesReached;

    /**
     * 增加重试次数
     *
     * @return 增加后的重试次数
     */
    public int incrementRetryCount() {
        return ++retryCount;
    }

    /**
     * 更新失败信息
     *
     * @param reason 失败原因
     */
    public void recordFailure(String reason) {
        this.lastFailureReason = reason;
        this.lastRetryTime = System.currentTimeMillis();
    }

    /**
     * 计算下次重试时间
     *
     * @param initialIntervalMs 初始重试间隔（毫秒）
     */
    public void calculateNextRetryTime(long initialIntervalMs) {
        // 指数退避：initialInterval * 2^retryCount
        long backoffMs = initialIntervalMs * (1L << retryCount);
        this.nextRetryTime = System.currentTimeMillis() + backoffMs;
    }

    /**
     * 检查是否可以重试
     *
     * @return true 如果当前时间已超过下次重试时间
     */
    public boolean isReadyForRetry() {
        return System.currentTimeMillis() >= nextRetryTime;
    }

    /**
     * 获取等待重试的剩余时间（毫秒）
     *
     * @return 剩余时间，如果已可重试则返回 0
     */
    public long getRemainingWaitTime() {
        long remaining = nextRetryTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * 创建新的重试上下文
     *
     * @param deviceId       设备ID
     * @param deviceName     设备名称
     * @param connectionInfo 连接信息
     * @return 重试上下文
     */
    public static RetryContext create(Long deviceId, String deviceName, DeviceConnectionInfo connectionInfo) {
        return RetryContext.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .connectionInfo(connectionInfo)
                .retryCount(0)
                .createTime(System.currentTimeMillis())
                .maxRetriesReached(false)
                .build();
    }
}
