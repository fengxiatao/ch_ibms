package cn.iocoder.yudao.module.iot.newgateway.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 设备状态
 * <p>
 * 封装设备的当前状态信息，用于状态查询响应。
 * </p>
 *
 * @author IoT Gateway Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatus {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 是否在线
     */
    private boolean online;

    /**
     * 最后心跳时间（毫秒时间戳）
     */
    private Long lastHeartbeatTime;

    /**
     * 最后活动时间（毫秒时间戳）
     */
    private Long lastActiveTime;

    /**
     * 附加状态数据
     * <p>
     * 设备特定的状态信息，如信号强度、电池电量等。
     * </p>
     */
    private Map<String, Object> attributes;

    /**
     * 创建在线状态
     *
     * @param deviceId 设备ID
     * @return 在线状态
     */
    public static DeviceStatus online(Long deviceId) {
        return DeviceStatus.builder()
                .deviceId(deviceId)
                .online(true)
                .lastActiveTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建离线状态
     *
     * @param deviceId 设备ID
     * @return 离线状态
     */
    public static DeviceStatus offline(Long deviceId) {
        return DeviceStatus.builder()
                .deviceId(deviceId)
                .online(false)
                .build();
    }

    /**
     * 创建带属性的在线状态
     *
     * @param deviceId   设备ID
     * @param attributes 附加属性
     * @return 在线状态
     */
    public static DeviceStatus online(Long deviceId, Map<String, Object> attributes) {
        return DeviceStatus.builder()
                .deviceId(deviceId)
                .online(true)
                .lastActiveTime(System.currentTimeMillis())
                .attributes(attributes)
                .build();
    }

    /**
     * 获取属性值
     *
     * @param key 属性键
     * @param <T> 值类型
     * @return 属性值，如果不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return attributes != null ? (T) attributes.get(key) : null;
    }
}
