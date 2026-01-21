package cn.iocoder.yudao.module.iot.newgateway.core.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备配置解析器
 * <p>
 * 从 iot_device 表的 config JSON 字段中提取设备连接参数。
 * 支持嵌套的 data 结构（GenericDeviceConfig 格式）。
 * </p>
 *
 * <p>支持的配置格式：</p>
 * <pre>
 * // 格式1：扁平结构
 * {
 *   "ipAddress": "192.168.1.100",
 *   "port": 37777,
 *   "username": "admin",
 *   "password": "admin123",
 *   "vendor": "dahua",
 *   "deviceType": "ACCESS"
 * }
 *
 * // 格式2：嵌套 data 结构（GenericDeviceConfig）
 * {
 *   "vendor": "dahua",
 *   "deviceType": "ACCESS",
 *   "data": {
 *     "ipAddress": "192.168.1.100",
 *     "port": 37777,
 *     "username": "admin",
 *     "password": "admin123"
 *   }
 * }
 * </pre>
 *
 * @author IoT Gateway Team
 * @see DeviceConnectionInfo
 */
@Slf4j
@Component
public class DeviceConfigParser {

    // ==================== 厂商常量 ====================
    public static final String VENDOR_DAHUA = "dahua";
    public static final String VENDOR_HIKVISION = "hikvision";

    // ==================== 默认端口 ====================
    public static final int DEFAULT_PORT_DAHUA = 37777;
    public static final int DEFAULT_PORT_HIKVISION = 8000;
    public static final int DEFAULT_PORT_OTHER = 80;

    // ==================== 配置字段名 ====================
    private static final String FIELD_IP_ADDRESS = "ipAddress";
    private static final String FIELD_IP = "ip";
    private static final String FIELD_PORT = "port";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_PASSWORD = "password";
    private static final String FIELD_VENDOR = "vendor";
    private static final String FIELD_DEVICE_TYPE = "deviceType";
    private static final String FIELD_CONNECTION_MODE = "connectionMode";
    private static final String FIELD_DATA = "data";

    /**
     * 解析设备配置
     *
     * @param configJson 配置 JSON 字符串
     * @param deviceType 设备类型（用于确定默认值，可为 null）
     * @return 解析后的连接信息，如果解析失败返回 null
     */
    public DeviceConnectionInfo parse(String configJson, String deviceType) {
        if (StrUtil.isBlank(configJson) || "null".equalsIgnoreCase(configJson.trim())) {
            log.debug("[DeviceConfigParser] 配置为空");
            return null;
        }

        try {
            // 解析并扁平化配置
            Map<String, Object> flatConfig = flattenConfig(configJson);
            if (flatConfig.isEmpty()) {
                log.debug("[DeviceConfigParser] 解析后配置为空");
                return null;
            }

            // 提取连接参数
            String ipAddress = getStringValue(flatConfig, FIELD_IP_ADDRESS);
            if (ipAddress == null) {
                ipAddress = getStringValue(flatConfig, FIELD_IP);
            }

            Integer port = getIntegerValue(flatConfig, FIELD_PORT);
            String username = getStringValue(flatConfig, FIELD_USERNAME);
            String password = getStringValue(flatConfig, FIELD_PASSWORD);
            String vendor = getStringValue(flatConfig, FIELD_VENDOR);
            String configDeviceType = getStringValue(flatConfig, FIELD_DEVICE_TYPE);
            String connectionModeStr = getStringValue(flatConfig, FIELD_CONNECTION_MODE);

            // 使用配置中的 deviceType，如果没有则使用参数传入的
            String effectiveDeviceType = configDeviceType != null ? configDeviceType : deviceType;

            // 应用默认端口
            if (port == null) {
                port = getDefaultPort(vendor, effectiveDeviceType);
            }

            // 解析连接模式
            ConnectionMode connectionMode = parseConnectionMode(connectionModeStr, effectiveDeviceType);

            DeviceConnectionInfo info = DeviceConnectionInfo.builder()
                    .ipAddress(ipAddress)
                    .port(port)
                    .username(username)
                    .password(password)
                    .vendor(vendor)
                    .deviceType(effectiveDeviceType)
                    .connectionMode(connectionMode)
                    .build();

            log.debug("[DeviceConfigParser] 配置解析完成: ip={}, port={}, vendor={}, deviceType={}, connectionMode={}",
                    ipAddress, port, vendor, effectiveDeviceType, connectionMode);

            return info;

        } catch (Exception e) {
            log.warn("[DeviceConfigParser] 解析配置失败: configJson={}, error={}", configJson, e.getMessage());
            return null;
        }
    }

    /**
     * 验证配置完整性
     *
     * @param info 连接信息
     * @return 验证结果
     */
    public ValidationResult validate(DeviceConnectionInfo info) {
        if (info == null) {
            return ValidationResult.invalid("配置信息为空");
        }

        if (StrUtil.isBlank(info.getIpAddress())) {
            return ValidationResult.invalid("缺少 IP 地址");
        }

        if (info.getPort() == null || info.getPort() <= 0 || info.getPort() > 65535) {
            return ValidationResult.invalid("端口无效: " + info.getPort());
        }

        // 主动连接设备需要用户名和密码
        if (info.getConnectionMode() == ConnectionMode.ACTIVE) {
            if (StrUtil.isBlank(info.getUsername())) {
                return ValidationResult.invalid("主动连接设备缺少用户名");
            }
            if (StrUtil.isBlank(info.getPassword())) {
                return ValidationResult.invalid("主动连接设备缺少密码");
            }
        }

        return ValidationResult.valid();
    }

    /**
     * 扁平化配置 JSON
     * <p>
     * 支持嵌套的 data 结构，data 中的字段优先级高于顶层字段。
     * </p>
     *
     * @param configJson 配置 JSON 字符串
     * @return 扁平化的配置 Map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> flattenConfig(String configJson) {
        Map<String, Object> result = new HashMap<>();

        if (StrUtil.isBlank(configJson) || "null".equalsIgnoreCase(configJson.trim())) {
            return result;
        }

        try {
            JSONObject configObj = JSONUtil.parseObj(configJson);
            if (configObj == null || configObj.isEmpty()) {
                return result;
            }

            // 1. 先复制顶层字段（跳过 data）
            for (String key : configObj.keySet()) {
                if (!FIELD_DATA.equals(key)) {
                    Object value = configObj.get(key);
                    if (isValidValue(value)) {
                        result.put(key, value);
                    }
                }
            }

            // 2. 处理嵌套的 data 结构（优先级更高，覆盖顶层）
            Object dataObj = configObj.get(FIELD_DATA);
            if (dataObj instanceof JSONObject) {
                JSONObject dataMap = (JSONObject) dataObj;
                log.debug("[DeviceConfigParser] 检测到嵌套 data 结构，提取字段: {}", dataMap.keySet());

                for (String key : dataMap.keySet()) {
                    Object value = dataMap.get(key);
                    if (isValidValue(value)) {
                        result.put(key, value);
                    }
                }
            } else if (dataObj instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                log.debug("[DeviceConfigParser] 检测到嵌套 data 结构（Map），提取字段: {}", dataMap.keySet());

                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    if (isValidValue(entry.getValue())) {
                        result.put(entry.getKey(), entry.getValue());
                    }
                }
            }

            return result;

        } catch (Exception e) {
            log.warn("[DeviceConfigParser] 扁平化配置失败: {}", e.getMessage());
            return result;
        }
    }

    /**
     * 获取默认端口
     *
     * @param vendor     厂商
     * @param deviceType 设备类型
     * @return 默认端口
     */
    public int getDefaultPort(String vendor, String deviceType) {
        if (vendor == null) {
            return DEFAULT_PORT_OTHER;
        }

        String normalizedVendor = vendor.toLowerCase().trim();

        if (normalizedVendor.contains(VENDOR_DAHUA)) {
            return DEFAULT_PORT_DAHUA;
        }

        if (normalizedVendor.contains(VENDOR_HIKVISION)) {
            return DEFAULT_PORT_HIKVISION;
        }

        return DEFAULT_PORT_OTHER;
    }

    /**
     * 解析连接模式
     *
     * @param connectionModeStr 连接模式字符串
     * @param deviceType        设备类型
     * @return 连接模式
     */
    private ConnectionMode parseConnectionMode(String connectionModeStr, String deviceType) {
        // 如果配置中明确指定了连接模式
        if (StrUtil.isNotBlank(connectionModeStr)) {
            try {
                return ConnectionMode.valueOf(connectionModeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[DeviceConfigParser] 无效的连接模式: {}", connectionModeStr);
            }
        }

        // 根据设备类型推断连接模式
        if (deviceType != null) {
            String upperType = deviceType.toUpperCase();
            // 被动连接设备类型
            if (upperType.contains("ALARM") || upperType.contains("CHANGHUI") || upperType.contains("DETONG")) {
                return ConnectionMode.PASSIVE;
            }
        }

        // 默认为主动连接
        return ConnectionMode.ACTIVE;
    }

    // ==================== 工具方法 ====================

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null || value instanceof JSONNull) {
            return null;
        }
        String strValue = value.toString().trim();
        if (strValue.isEmpty() || "null".equalsIgnoreCase(strValue)) {
            return null;
        }
        return strValue;
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null || value instanceof JSONNull) {
            return null;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            String strValue = value.toString().trim();
            if (strValue.isEmpty() || "null".equalsIgnoreCase(strValue)) {
                return null;
            }
            return Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isValidValue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof JSONNull) {
            return false;
        }
        if (value instanceof String) {
            String strValue = (String) value;
            return !strValue.trim().isEmpty() && !"null".equalsIgnoreCase(strValue);
        }
        return true;
    }

    // ==================== 验证结果类 ====================

    /**
     * 配置验证结果
     */
    @Data
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
    }
}
