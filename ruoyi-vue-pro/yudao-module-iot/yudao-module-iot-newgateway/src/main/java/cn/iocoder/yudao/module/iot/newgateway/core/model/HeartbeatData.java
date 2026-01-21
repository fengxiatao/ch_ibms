package cn.iocoder.yudao.module.iot.newgateway.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 心跳数据
 * <p>
 * 封装设备心跳消息中的数据，用于被动连接设备的心跳处理。
 * 不同设备类型可以在 payload 中携带设备特定的心跳信息。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * HeartbeatData heartbeat = HeartbeatData.builder()
 *     .deviceId(12345L)
 *     .timestamp(System.currentTimeMillis())
 *     .payload(Map.of(
 *         "signalStrength", 85,
 *         "batteryLevel", 90
 *     ))
 *     .build();
 * }
 * </pre>
 *
 * @author IoT Gateway Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeartbeatData {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 心跳时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 心跳负载数据
     * <p>
     * 设备特定的心跳信息，如信号强度、电池电量、设备状态等。
     * </p>
     */
    private Map<String, Object> payload;

    /**
     * 创建简单心跳数据（仅包含设备ID和时间戳）
     *
     * @param deviceId 设备ID
     * @return 心跳数据
     */
    public static HeartbeatData of(Long deviceId) {
        return HeartbeatData.builder()
                .deviceId(deviceId)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建带负载的心跳数据
     *
     * @param deviceId 设备ID
     * @param payload  负载数据
     * @return 心跳数据
     */
    public static HeartbeatData of(Long deviceId, Map<String, Object> payload) {
        return HeartbeatData.builder()
                .deviceId(deviceId)
                .timestamp(System.currentTimeMillis())
                .payload(payload)
                .build();
    }

    /**
     * 获取负载中的指定字段值
     *
     * @param key 字段名
     * @param <T> 返回类型
     * @return 字段值，如果不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getPayloadValue(String key) {
        return payload != null ? (T) payload.get(key) : null;
    }
}
