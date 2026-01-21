package cn.iocoder.yudao.module.iot.core.gateway.dto;

import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 设备信息 DTO
 * 
 * 用于设备处理器的路由和分发，包含设备的基本标识信息和配置。
 * DeviceHandlerRegistry 根据此信息选择合适的处理器。
 *
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型
     * 如：ACCESS（门禁）、CAMERA（摄像头）、CHANGHUI（长辉）、ALARM（报警主机）
     */
    private String deviceType;

    /**
     * 设备厂商
     * 如：Dahua（大华）、Hikvision（海康）、ONVIF
     */
    private String vendor;

    /**
     * 设备IP地址
     */
    private String ipAddress;

    /**
     * 设备端口
     */
    private Integer port;

    /**
     * 连接模式
     * ACTIVE: 主动连接（SDK 登录）
     * PASSIVE: 被动连接（TCP 心跳）
     */
    private ConnectionMode connectionMode;

    /**
     * 设备配置
     * 包含设备特定的配置参数，如 IP、端口、用户名、密码等
     */
    private Map<String, Object> config;

    /**
     * 创建主动连接设备信息
     *
     * @param deviceId   设备ID
     * @param deviceType 设备类型
     * @param vendor     厂商
     * @param config     配置
     * @return DeviceInfo
     */
    public static DeviceInfo activeDevice(Long deviceId, String deviceType, String vendor, Map<String, Object> config) {
        return DeviceInfo.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .vendor(vendor)
                .connectionMode(ConnectionMode.ACTIVE)
                .config(config)
                .build();
    }

    /**
     * 创建主动连接设备信息（带完整信息）
     *
     * @param deviceId   设备ID
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @param vendor     厂商
     * @param ipAddress  IP地址
     * @param port       端口
     * @param config     配置
     * @return DeviceInfo
     */
    public static DeviceInfo activeDevice(Long deviceId, String deviceName, String deviceType, String vendor, 
                                          String ipAddress, Integer port, Map<String, Object> config) {
        return DeviceInfo.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .deviceType(deviceType)
                .vendor(vendor)
                .ipAddress(ipAddress)
                .port(port)
                .connectionMode(ConnectionMode.ACTIVE)
                .config(config)
                .build();
    }

    /**
     * 创建被动连接设备信息
     *
     * @param deviceId   设备ID
     * @param deviceType 设备类型
     * @param vendor     厂商
     * @param config     配置
     * @return DeviceInfo
     */
    public static DeviceInfo passiveDevice(Long deviceId, String deviceType, String vendor, Map<String, Object> config) {
        return DeviceInfo.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .vendor(vendor)
                .connectionMode(ConnectionMode.PASSIVE)
                .config(config)
                .build();
    }

    /**
     * 创建被动连接设备信息（带完整信息）
     *
     * @param deviceId   设备ID
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @param vendor     厂商
     * @param ipAddress  IP地址
     * @param port       端口
     * @param config     配置
     * @return DeviceInfo
     */
    public static DeviceInfo passiveDevice(Long deviceId, String deviceName, String deviceType, String vendor,
                                           String ipAddress, Integer port, Map<String, Object> config) {
        return DeviceInfo.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .deviceType(deviceType)
                .vendor(vendor)
                .ipAddress(ipAddress)
                .port(port)
                .connectionMode(ConnectionMode.PASSIVE)
                .config(config)
                .build();
    }

    /**
     * 判断是否为主动连接设备
     */
    public boolean isActiveConnection() {
        return connectionMode != null && connectionMode.isActive();
    }

    /**
     * 判断是否为被动连接设备
     */
    public boolean isPassiveConnection() {
        return connectionMode != null && connectionMode.isPassive();
    }

    /**
     * 获取配置值
     *
     * @param key 配置键
     * @param <T> 值类型
     * @return 配置值，如果不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key) {
        if (config == null) {
            return null;
        }
        return (T) config.get(key);
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
}
