package cn.iocoder.yudao.module.iot.dal.dataobject.device.config;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;

/**
 * 设备配置辅助工具类
 * <p>
 * 提供 null-safe 的方法从设备对象中提取配置信息，
 * 避免在业务代码中重复进行 null 检查。
 *
 * @author system
 * @since 2024-12-18
 */
public final class DeviceConfigHelper {

    /**
     * 私有构造函数，防止实例化
     */
    private DeviceConfigHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 安全获取设备 IP 地址
     * <p>
     * 从设备的 config 字段中提取 IP 地址。
     * 如果设备为 null、config 为 null 或 config 中没有 IP 地址，则返回 null。
     *
     * @param device 设备对象，可以为 null
     * @return IP 地址，如果无法获取则返回 null
     */
    public static String getIpAddress(IotDeviceDO device) {
        if (device == null) {
            return null;
        }
        DeviceConfig config = device.getConfig();
        if (config == null) {
            return null;
        }
        return config.getIpAddress();
    }

    /**
     * 安全获取设备端口号
     * <p>
     * 从设备的 config 字段中提取端口号。
     * 如果设备为 null、config 为 null 或 config 中没有端口号，则返回 null。
     *
     * @param device 设备对象，可以为 null
     * @return 端口号，如果无法获取则返回 null
     */
    public static Integer getPort(IotDeviceDO device) {
        if (device == null) {
            return null;
        }
        DeviceConfig config = device.getConfig();
        if (config == null) {
            return null;
        }
        return config.getPort();
    }

    /**
     * 检查设备是否有有效的 IP 地址
     * <p>
     * 判断设备的 config 中是否包含非空的 IP 地址。
     *
     * @param device 设备对象，可以为 null
     * @return 如果设备有有效的 IP 地址返回 true，否则返回 false
     */
    public static boolean hasIpAddress(IotDeviceDO device) {
        String ipAddress = getIpAddress(device);
        return ipAddress != null && !ipAddress.trim().isEmpty();
    }

    /**
     * 检查设备是否有有效的端口号
     * <p>
     * 判断设备的 config 中是否包含有效的端口号（1-65535）。
     *
     * @param device 设备对象，可以为 null
     * @return 如果设备有有效的端口号返回 true，否则返回 false
     */
    public static boolean hasPort(IotDeviceDO device) {
        Integer port = getPort(device);
        return port != null && port >= 1 && port <= 65535;
    }

    /**
     * 获取设备的网络地址（IP:Port 格式）
     * <p>
     * 如果设备同时有 IP 地址和端口号，返回 "IP:Port" 格式的字符串。
     * 如果只有 IP 地址，返回 IP 地址。
     * 如果没有 IP 地址，返回 null。
     *
     * @param device 设备对象，可以为 null
     * @return 网络地址字符串，如果无法获取则返回 null
     */
    public static String getNetworkAddress(IotDeviceDO device) {
        String ipAddress = getIpAddress(device);
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return null;
        }
        Integer port = getPort(device);
        if (port != null && port >= 1 && port <= 65535) {
            return ipAddress + ":" + port;
        }
        return ipAddress;
    }

    /**
     * 将 DeviceConfig 对象序列化为 JSON 字符串
     * <p>
     * 使用 DeviceConfig.toMap() 方法获取配置数据，然后序列化为 JSON。
     * 这样可以避免直接调用 toString() 导致的格式问题（如 GenericDeviceConfig(...)）。
     *
     * @param config 设备配置对象，可以为 null
     * @return JSON 字符串，如果 config 为 null 则返回 null
     */
    public static String toJson(DeviceConfig config) {
        if (config == null) {
            return null;
        }
        try {
            return JSONUtil.toJsonStr(config.toMap());
        } catch (Exception e) {
            // 如果序列化失败，返回 null
            return null;
        }
    }

}
