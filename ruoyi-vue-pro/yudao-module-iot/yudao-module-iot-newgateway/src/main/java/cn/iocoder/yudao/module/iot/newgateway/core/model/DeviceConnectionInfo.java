package cn.iocoder.yudao.module.iot.newgateway.core.model;

import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 设备连接信息
 * <p>
 * 封装主动连接设备所需的连接参数，如 IP 地址、端口、用户名、密码等。
 * 用于 {@link cn.iocoder.yudao.module.iot.newgateway.core.handler.ActiveDeviceHandler#login(DeviceConnectionInfo)} 方法。
 * </p>
 *
 * @author IoT Gateway Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceConnectionInfo {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * IP 地址
     */
    private String ipAddress;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 厂商
     */
    private String vendor;

    /**
     * 连接模式
     */
    private ConnectionMode connectionMode;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 附加配置
     * <p>
     * 设备特定的连接配置参数。
     * </p>
     */
    private Map<String, Object> config;

    /**
     * 获取配置值
     *
     * @param key 配置键
     * @param <T> 值类型
     * @return 配置值，如果不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key) {
        return config != null ? (T) config.get(key) : null;
    }

    /**
     * 获取配置值，带默认值
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @param <T>          值类型
     * @return 配置值，如果不存在则返回默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key, T defaultValue) {
        if (config == null) {
            return defaultValue;
        }
        T value = (T) config.get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 创建基本连接信息
     *
     * @param deviceId  设备ID
     * @param ipAddress IP 地址
     * @param port      端口
     * @param username  用户名
     * @param password  密码
     * @return 连接信息
     */
    public static DeviceConnectionInfo of(Long deviceId, String ipAddress, Integer port,
                                          String username, String password) {
        return DeviceConnectionInfo.builder()
                .deviceId(deviceId)
                .ipAddress(ipAddress)
                .port(port)
                .username(username)
                .password(password)
                .build();
    }
}
