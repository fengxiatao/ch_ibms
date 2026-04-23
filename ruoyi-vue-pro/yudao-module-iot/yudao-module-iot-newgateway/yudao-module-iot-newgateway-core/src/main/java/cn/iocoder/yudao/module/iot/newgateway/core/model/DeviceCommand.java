package cn.iocoder.yudao.module.iot.newgateway.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备命令
 * <p>
 * 封装发送给设备的命令信息，包括命令类型和参数。
 * 不同设备类型支持不同的命令类型，参数通过 Map 传递以保持灵活性。
 * </p>
 *
 * <p>常见命令类型示例：</p>
 * <ul>
 *     <li>报警主机：ARM_ALL, ARM_STAY, DISARM, QUERY_STATUS, BYPASS_ZONE</li>
 *     <li>长辉设备：UPGRADE_TRIGGER, UPGRADE_URL</li>
 *     <li>门禁设备：OPEN_DOOR, CLOSE_DOOR, DISPATCH_AUTH, REVOKE_AUTH</li>
 *     <li>NVR设备：START_RECORDING, STOP_RECORDING, PTZ_CONTROL, QUERY_CHANNELS</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * // 创建升级命令
 * DeviceCommand upgradeCmd = DeviceCommand.builder()
 *     .commandType("UPGRADE_URL")
 *     .params(Map.of("url", "http://example.com/firmware.bin"))
 *     .build();
 *
 * // 获取参数
 * String url = upgradeCmd.getParam("url");
 * }
 * </pre>
 *
 * @author IoT Gateway Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCommand {

    /**
     * 命令类型
     * <p>
     * 标识命令的类型，如 "OPEN_DOOR", "UPGRADE_TRIGGER" 等。
     * 建议使用大写字母和下划线。
     * </p>
     */
    private String commandType;

    /**
     * 命令参数
     * <p>
     * 命令执行所需的参数，不同命令类型需要不同的参数。
     * </p>
     */
    private Map<String, Object> params;

    /**
     * 获取指定参数值
     *
     * @param key 参数名
     * @param <T> 返回类型
     * @return 参数值，如果不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getParam(String key) {
        return params != null ? (T) params.get(key) : null;
    }

    /**
     * 获取指定参数值，如果不存在则返回默认值
     *
     * @param key          参数名
     * @param defaultValue 默认值
     * @param <T>          返回类型
     * @return 参数值，如果不存在则返回默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getParam(String key, T defaultValue) {
        if (params == null) {
            return defaultValue;
        }
        T value = (T) params.get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取字符串类型参数
     *
     * @param key 参数名
     * @return 字符串参数值，如果不存在则返回 null
     */
    public String getStringParam(String key) {
        Object value = getParam(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取整数类型参数
     *
     * @param key 参数名
     * @return 整数参数值，如果不存在或无法转换则返回 null
     */
    public Integer getIntParam(String key) {
        Object value = getParam(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取长整数类型参数
     *
     * @param key 参数名
     * @return 长整数参数值，如果不存在或无法转换则返回 null
     */
    public Long getLongParam(String key) {
        Object value = getParam(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取布尔类型参数
     *
     * @param key 参数名
     * @return 布尔参数值，如果不存在则返回 null
     */
    public Boolean getBooleanParam(String key) {
        Object value = getParam(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * 检查是否包含指定参数
     *
     * @param key 参数名
     * @return 是否包含该参数
     */
    public boolean hasParam(String key) {
        return params != null && params.containsKey(key);
    }

    /**
     * 创建简单命令（无参数）
     *
     * @param commandType 命令类型
     * @return 设备命令
     */
    public static DeviceCommand of(String commandType) {
        return DeviceCommand.builder()
                .commandType(commandType)
                .params(new HashMap<>())
                .build();
    }

    /**
     * 创建带参数的命令
     *
     * @param commandType 命令类型
     * @param params      命令参数
     * @return 设备命令
     */
    public static DeviceCommand of(String commandType, Map<String, Object> params) {
        return DeviceCommand.builder()
                .commandType(commandType)
                .params(params != null ? params : new HashMap<>())
                .build();
    }

    /**
     * 创建带单个参数的命令
     *
     * @param commandType 命令类型
     * @param paramKey    参数名
     * @param paramValue  参数值
     * @return 设备命令
     */
    public static DeviceCommand of(String commandType, String paramKey, Object paramValue) {
        Map<String, Object> params = new HashMap<>();
        params.put(paramKey, paramValue);
        return DeviceCommand.builder()
                .commandType(commandType)
                .params(params)
                .build();
    }
}
