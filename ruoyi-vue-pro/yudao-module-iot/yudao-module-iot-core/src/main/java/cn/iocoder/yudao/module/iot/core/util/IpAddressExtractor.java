package cn.iocoder.yudao.module.iot.core.util;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * IP 地址提取工具类
 * <p>
 * 统一处理项目中不同设备配置使用的各种 IP 地址字段名：
 * <ul>
 *   <li>ip - 标准字段名</li>
 *   <li>ipAddress - 门禁设备、报警主机等使用</li>
 *   <li>address - 部分设备使用</li>
 *   <li>host - 部分配置使用</li>
 * </ul>
 * <p>
 * 使用优先级：ip > ipAddress > address > host
 *
 * @author 长辉信息科技有限公司
 * @since 2024-12-18
 */
public class IpAddressExtractor {

    /**
     * IP 地址字段名列表（按优先级排序）
     */
    private static final String[] IP_FIELD_NAMES = {"ip", "ipAddress", "address", "host"};

    /**
     * IPv4 地址正则表达式
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    /**
     * 从配置 Map 中提取 IP 地址
     *
     * @param config 配置 Map
     * @return IP 地址，如果未找到则返回 null
     */
    public static String extract(Map<String, Object> config) {
        if (config == null || config.isEmpty()) {
            return null;
        }

        for (String fieldName : IP_FIELD_NAMES) {
            Object value = config.get(fieldName);
            if (value != null) {
                String ip = value.toString().trim();
                if (!ip.isEmpty()) {
                    return ip;
                }
            }
        }

        return null;
    }

    /**
     * 从配置 Map 中提取并验证 IP 地址
     *
     * @param config 配置 Map
     * @return 有效的 IP 地址，如果未找到或无效则返回 null
     */
    public static String extractAndValidate(Map<String, Object> config) {
        String ip = extract(config);
        if (ip != null && isValidIpv4(ip)) {
            return ip;
        }
        return null;
    }

    /**
     * 验证是否为有效的 IPv4 地址
     *
     * @param ip IP 地址字符串
     * @return 是否有效
     */
    public static boolean isValidIpv4(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return IPV4_PATTERN.matcher(ip).matches();
    }

    /**
     * 获取支持的 IP 字段名列表
     *
     * @return 字段名数组
     */
    public static String[] getSupportedFieldNames() {
        return IP_FIELD_NAMES.clone();
    }
}
