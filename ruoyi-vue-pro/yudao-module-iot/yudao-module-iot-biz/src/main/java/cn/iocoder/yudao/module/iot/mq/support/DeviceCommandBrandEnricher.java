package cn.iocoder.yudao.module.iot.mq.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 在发布设备命令前，若调用方未传 {@code brand}，则按台账补全，满足网关多厂家路由契约。
 * <p>
 * 顺序：{@code ibms_device.brand} → {@code ibms_device.extra} 中的 {@code brand} / {@code manufacturer} / {@code vendorKey}。
 */
@Component
@RequiredArgsConstructor
public class DeviceCommandBrandEnricher {

    private final IbmsDeviceMapper ibmsDeviceMapper;

    public void enrichIfAbsent(Long deviceId, Map<String, Object> params) {
        if (deviceId == null || params == null) {
            return;
        }
        if (hasNonBlank(params, "brand")) {
            return;
        }

        IbmsDeviceDO ibms = ibmsDeviceMapper.selectById(deviceId);
        if (ibms == null) {
            return;
        }
        if (StrUtil.isNotBlank(ibms.getBrand())) {
            params.put("brand", ibms.getBrand().trim());
            return;
        }
        JSONObject cfg = parseExtra(ibms.getExtra());
        String b = firstNonBlank(cfg.get("brand"), cfg.get("manufacturer"), cfg.get("vendorKey"));
        if (StrUtil.isNotBlank(b)) {
            params.put("brand", b.toString().trim());
        }
    }

    private static JSONObject parseExtra(String extra) {
        if (StrUtil.isBlank(extra)) {
            return new JSONObject();
        }
        try {
            return JSONUtil.parseObj(extra.trim());
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private static boolean hasNonBlank(Map<String, Object> params, String key) {
        Object v = params.get(key);
        return v != null && StrUtil.isNotBlank(v.toString());
    }

    private static String firstNonBlank(Object... vals) {
        for (Object v : vals) {
            if (v == null) {
                continue;
            }
            String s = v.toString();
            if (StrUtil.isNotBlank(s)) {
                return s;
            }
        }
        return null;
    }
}
