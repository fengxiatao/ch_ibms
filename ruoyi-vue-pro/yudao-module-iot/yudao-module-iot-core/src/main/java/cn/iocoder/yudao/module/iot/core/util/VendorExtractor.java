package cn.iocoder.yudao.module.iot.core.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 设备厂商提取工具类
 * <p>
 * 用于从设备配置中提取厂商信息，支持：
 * <ul>
 *   <li>从 JSON 字符串提取</li>
 *   <li>从 Map 提取（支持嵌套结构如 GenericDeviceConfig.data）</li>
 *   <li>根据端口号推断厂商</li>
 * </ul>
 * 
 * <h2>与设备处理器框架的关系</h2>
 * <p>
 * 本工具类与 Gateway 层的 DeviceHandlerRegistry 配合使用：
 * <ul>
 *   <li>本类负责从各种配置格式中提取和标准化厂商名称</li>
 *   <li>DeviceHandlerRegistry 使用标准化后的厂商名称查找处理器</li>
 *   <li>处理器的 supports() 方法应使用 {@link #normalizeVendor(String)} 进行厂商匹配</li>
 * </ul>
 * 
 * <h2>厂商命名规范</h2>
 * <table border="1">
 *   <tr><th>标准名称</th><th>别名</th></tr>
 *   <tr><td>Dahua</td><td>大华</td></tr>
 *   <tr><td>Hikvision</td><td>海康, Hik</td></tr>
 *   <tr><td>ONVIF</td><td>-</td></tr>
 * </table>
 *
 * @author 长辉信息科技有限公司
 * @since 2024-12-17
 */
@Slf4j
public class VendorExtractor {

    /** 大华设备默认端口 */
    public static final int DAHUA_DEFAULT_PORT = 37777;
    /** 海康设备默认端口 */
    public static final int HIKVISION_DEFAULT_PORT = 8000;
    /** ONVIF 默认端口 */
    public static final int ONVIF_DEFAULT_PORT = 80;

    /** 厂商标识：大华 */
    public static final String VENDOR_DAHUA = "Dahua";
    /** 厂商标识：海康 */
    public static final String VENDOR_HIKVISION = "Hikvision";
    /** 厂商标识：ONVIF */
    public static final String VENDOR_ONVIF = "ONVIF";

    private VendorExtractor() {
        // 工具类，禁止实例化
    }

    /**
     * 从设备配置中提取厂商信息
     * <p>
     * 支持以下类型的配置：
     * <ul>
     *   <li>JSON 字符串</li>
     *   <li>Map 对象</li>
     * </ul>
     *
     * @param config 设备配置（可能是 JSON 字符串或 Map）
     * @return 厂商标识，如 "Dahua"、"Hikvision"，或 null
     */
    @SuppressWarnings("unchecked")
    public static String extractVendor(Object config) {
        if (config == null) {
            return null;
        }

        try {
            // 1. 如果是 String，尝试解析为 JSON
            if (config instanceof String) {
                String configStr = (String) config;
                if (StrUtil.isBlank(configStr) || "null".equalsIgnoreCase(configStr.trim())) {
                    return null;
                }
                try {
                    JSONObject jsonObject = JSONUtil.parseObj(configStr);
                    return extractVendorFromMap(jsonObject);
                } catch (Exception e) {
                    log.debug("[VendorExtractor] 解析 JSON 配置失败: {}", configStr);
                    return null;
                }
            }

            // 2. 如果是 Map，直接提取
            if (config instanceof Map) {
                return extractVendorFromMap((Map<String, Object>) config);
            }

            log.debug("[VendorExtractor] 不支持的配置类型: {}", config.getClass().getName());
            return null;

        } catch (Exception e) {
            log.warn("[VendorExtractor] 提取厂商信息异常: config={}", config, e);
            return null;
        }
    }

    /**
     * 从 Map 中提取厂商信息
     * <p>
     * 支持嵌套结构（如 GenericDeviceConfig.data），提取顺序：
     * <ol>
     *   <li>直接从顶层获取 vendor 字段</li>
     *   <li>从 data 嵌套结构中获取 vendor 字段</li>
     *   <li>根据其他配置字段推断厂商</li>
     * </ol>
     *
     * @param configMap 配置 Map
     * @return 厂商标识，或 null
     */
    @SuppressWarnings("unchecked")
    public static String extractVendorFromMap(Map<String, Object> configMap) {
        if (configMap == null || configMap.isEmpty()) {
            return null;
        }

        // 1. 直接从顶层获取 vendor
        String vendor = getStringValue(configMap, "vendor");
        if (StrUtil.isNotBlank(vendor)) {
            return normalizeVendor(vendor);
        }

        // 2. 从 data 嵌套结构中获取 vendor（GenericDeviceConfig 格式）
        Object dataObj = configMap.get("data");
        if (dataObj instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) dataObj;
            vendor = getStringValue(dataMap, "vendor");
            if (StrUtil.isNotBlank(vendor)) {
                return normalizeVendor(vendor);
            }
            // 如果 data 中也没有 vendor，尝试从 data 中推断
            String inferredVendor = inferVendor(dataMap);
            if (inferredVendor != null) {
                return inferredVendor;
            }
        }

        // 3. 尝试从顶层配置推断厂商
        return inferVendor(configMap);
    }

    /**
     * 根据其他配置字段推断厂商
     * <p>
     * 推断规则：
     * <ul>
     *   <li>端口 37777 → Dahua（大华）</li>
     *   <li>端口 8000 → Hikvision（海康）</li>
     *   <li>deviceType 包含 "dahua" → Dahua</li>
     *   <li>deviceType 包含 "hikvision" 或 "hik" → Hikvision</li>
     * </ul>
     *
     * @param configMap 配置 Map
     * @return 推断的厂商标识，或 null
     */
    @SuppressWarnings("unchecked")
    public static String inferVendor(Map<String, Object> configMap) {
        if (configMap == null || configMap.isEmpty()) {
            return null;
        }

        // 1. 根据端口推断
        Integer port = getIntegerValue(configMap, "port");
        if (port == null) {
            port = getIntegerValue(configMap, "tcpPort");
        }
        if (port != null) {
            if (port == DAHUA_DEFAULT_PORT) {
                return VENDOR_DAHUA;
            } else if (port == HIKVISION_DEFAULT_PORT) {
                return VENDOR_HIKVISION;
            }
        }

        // 2. 根据 deviceType 推断
        String deviceType = getStringValue(configMap, "deviceType");
        if (StrUtil.isNotBlank(deviceType)) {
            String lowerType = deviceType.toLowerCase();
            if (lowerType.contains("dahua") || lowerType.contains("大华")) {
                return VENDOR_DAHUA;
            } else if (lowerType.contains("hikvision") || lowerType.contains("hik") || lowerType.contains("海康")) {
                return VENDOR_HIKVISION;
            }
        }

        // 3. 检查 data 嵌套结构中的端口
        Object dataObj = configMap.get("data");
        if (dataObj instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) dataObj;
            port = getIntegerValue(dataMap, "port");
            if (port == null) {
                port = getIntegerValue(dataMap, "tcpPort");
            }
            if (port != null) {
                if (port == DAHUA_DEFAULT_PORT) {
                    return VENDOR_DAHUA;
                } else if (port == HIKVISION_DEFAULT_PORT) {
                    return VENDOR_HIKVISION;
                }
            }
        }

        return null;
    }

    /**
     * 标准化厂商名称
     * <p>
     * 将各种形式的厂商名称统一为标准格式
     *
     * @param vendor 原始厂商名称
     * @return 标准化后的厂商名称
     */
    public static String normalizeVendor(String vendor) {
        if (StrUtil.isBlank(vendor)) {
            return null;
        }
        String lowerVendor = vendor.toLowerCase().trim();
        if (lowerVendor.equals("dahua") || lowerVendor.equals("大华")) {
            return VENDOR_DAHUA;
        } else if (lowerVendor.equals("hikvision") || lowerVendor.equals("hik") || lowerVendor.equals("海康")) {
            return VENDOR_HIKVISION;
        } else if (lowerVendor.equals("onvif")) {
            return VENDOR_ONVIF;
        }
        // 返回原始值（首字母大写）
        return StrUtil.upperFirst(vendor.trim());
    }

    /**
     * 从 Map 中安全获取字符串值
     */
    private static String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        // 处理 Hutool JSONNull
        if (value instanceof cn.hutool.json.JSONNull) {
            return null;
        }
        String strValue = value.toString();
        return StrUtil.isBlank(strValue) || "null".equalsIgnoreCase(strValue) ? null : strValue;
    }

    /**
     * 从 Map 中安全获取整数值
     */
    private static Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        // 处理 Hutool JSONNull
        if (value instanceof cn.hutool.json.JSONNull) {
            return null;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            String strValue = value.toString();
            if (StrUtil.isBlank(strValue) || "null".equalsIgnoreCase(strValue)) {
                return null;
            }
            return Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
