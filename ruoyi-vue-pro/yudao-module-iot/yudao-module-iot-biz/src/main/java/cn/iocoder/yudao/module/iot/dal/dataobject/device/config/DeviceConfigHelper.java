package cn.iocoder.yudao.module.iot.dal.dataobject.device.config;

import cn.hutool.json.JSONUtil;

/**
 * 设备配置辅助工具类
 * <p>
 * 提供 null-safe 的方法从 {@link DeviceConfig} 中提取配置信息，
 * 避免在业务代码中重复进行 null 检查。
 *
 * <p>2026-05-08 单源化：移除针对 {@code IotDeviceDO} 的便捷重载，
 * 业务侧应通过 IBMS 设备视图（{@code IbmsDeviceRuntimeDO} 等）
 * 拿到 {@link DeviceConfig} 后再调用本工具，禁止以 {@code IotDeviceDO} 直读。</p>
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
     * 安全获取 IP 地址（基于 {@link DeviceConfig} 直读，供 IBMS 单源视图使用）。
     */
    public static String getIpAddress(DeviceConfig config) {
        if (config == null) {
            return null;
        }
        return config.getIpAddress();
    }

    /**
     * 安全获取端口号（基于 {@link DeviceConfig} 直读，供 IBMS 单源视图使用）。
     */
    public static Integer getPort(DeviceConfig config) {
        if (config == null) {
            return null;
        }
        return config.getPort();
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
