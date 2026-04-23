package cn.iocoder.yudao.module.iot.newgateway.plugins.nvr;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 同 deviceType=NVR 下按品牌选择 SDK 路径（对齐 IPC 多厂家思路）。
 * <p>当前进程仅集成大华 NetSDK；海康路径预留，避免误用大华 SDK。</p>
 */
@Component
public class NvrAdapterFactory {

    public enum NvrSdkVendor {
        DAHUA,
        HIKVISION
    }

    public NvrSdkVendor resolve(DeviceConnectionInfo info) {
        if (info == null) {
            return NvrSdkVendor.DAHUA;
        }
        String hint = StrUtil.isBlank(info.getVendor()) ? null : info.getVendor().trim();
        if (hint == null && info.getConfig() != null) {
            hint = firstString(info.getConfig(), "brand", "vendor", "vendorKey", "manufacturer");
        }
        if (StrUtil.isBlank(hint)) {
            return NvrSdkVendor.DAHUA;
        }
        String u = hint.toUpperCase();
        if (u.contains("HIK") || u.contains("海康")) {
            return NvrSdkVendor.HIKVISION;
        }
        if (u.contains("DAH") || u.contains("大华")) {
            return NvrSdkVendor.DAHUA;
        }
        return NvrSdkVendor.DAHUA;
    }

    private static String firstString(Map<String, Object> cfg, String... keys) {
        for (String k : keys) {
            Object v = cfg.get(k);
            if (v != null && StrUtil.isNotBlank(v.toString())) {
                return v.toString().trim();
            }
        }
        return null;
    }
}
