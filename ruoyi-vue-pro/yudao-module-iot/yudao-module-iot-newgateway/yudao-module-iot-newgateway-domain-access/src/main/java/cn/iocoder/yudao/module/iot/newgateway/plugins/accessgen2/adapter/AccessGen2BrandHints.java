package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.adapter;

import java.util.Map;

/**
 * 门禁二代设备信息中的品牌线索解析（与 IPC 多厂家策略对齐）。
 */
public final class AccessGen2BrandHints {

    private AccessGen2BrandHints() {
    }

    public static boolean looksLikeHikvision(Map<String, Object> deviceInfo) {
        if (deviceInfo == null || deviceInfo.isEmpty()) {
            return false;
        }
        if (containsAny(deviceInfo.get("vendor"), "HIKVISION", "海康", "HIK")) {
            return true;
        }
        if (containsAny(deviceInfo.get("brand"), "HIKVISION", "海康", "HIK")) {
            return true;
        }
        if (containsAny(deviceInfo.get("manufacturer"), "HIKVISION", "海康", "HIK")) {
            return true;
        }
        if (containsAny(deviceInfo.get("vendorKey"), "HIK", "HIKVISION")) {
            return true;
        }
        Object productKey = deviceInfo.get("productKey");
        if (productKey != null) {
            String pk = productKey.toString().toUpperCase();
            if (pk.contains("HIKVISION") || pk.contains("HIK_") || pk.contains("DS-")) {
                return true;
            }
        }
        Object model = deviceInfo.get("deviceModel");
        if (model != null) {
            String m = model.toString().toUpperCase();
            if (m.startsWith("DS-") || m.contains("HIKVISION")) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAny(Object value, String... tokens) {
        if (value == null) {
            return false;
        }
        String s = value.toString().toUpperCase();
        for (String t : tokens) {
            if (t != null && s.contains(t.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
