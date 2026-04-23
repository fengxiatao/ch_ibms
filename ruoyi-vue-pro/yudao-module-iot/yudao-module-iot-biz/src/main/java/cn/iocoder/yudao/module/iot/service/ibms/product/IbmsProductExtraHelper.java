package cn.iocoder.yudao.module.iot.service.ibms.product;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsProductExtra;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 统一解析 / 合并 / 序列化 {@code ibms_product.extra}。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IbmsProductExtraHelper {

    public static IbmsProductExtra parse(String extraJson) {
        IbmsProductExtra out = new IbmsProductExtra();
        if (StrUtil.isBlank(extraJson)) {
            return out;
        }
        try {
            JSONObject o = JSONUtil.parseObj(extraJson.trim());
            out.setProductKey(o.getStr("productKey"));
            out.setMenuIds(o.getStr("menuIds"));
            if (o.containsKey("primaryMenuId") && !o.isNull("primaryMenuId")) {
                out.setPrimaryMenuId(o.getLong("primaryMenuId"));
            }
            out.setPicUrl(o.getStr("picUrl"));
            if (o.containsKey("status") && !o.isNull("status")) {
                out.setStatus(o.getInt("status"));
            }
            if (o.containsKey("deviceType") && !o.isNull("deviceType")) {
                out.setDeviceType(o.getInt("deviceType"));
            }
            if (o.containsKey("netType") && !o.isNull("netType")) {
                out.setNetType(o.getInt("netType"));
            }
            if (o.containsKey("locationType") && !o.isNull("locationType")) {
                out.setLocationType(o.getInt("locationType"));
            }
            out.setCodecType(o.getStr("codecType"));
            out.setJobConfig(o.getStr("jobConfig"));
        } catch (Exception ignored) {
            // 保持空对象，避免坏 JSON 拖垮网关路径
        }
        return out;
    }

    public static IbmsProductExtra fromIotProduct(IotProductDO p) {
        if (p == null) {
            return new IbmsProductExtra();
        }
        IbmsProductExtra e = new IbmsProductExtra();
        e.setProductKey(p.getProductKey());
        e.setMenuIds(p.getMenuIds());
        e.setPrimaryMenuId(p.getPrimaryMenuId());
        e.setPicUrl(p.getPicUrl());
        e.setStatus(p.getStatus());
        e.setDeviceType(p.getDeviceType());
        e.setNetType(p.getNetType());
        e.setLocationType(p.getLocationType());
        e.setCodecType(p.getCodecType());
        e.setJobConfig(p.getJobConfig());
        return e;
    }

    /**
     * 将 patch 中非空字段合并进 base JSON（patch 优先）。
     */
    public static String merge(String baseExtraJson, IbmsProductExtra patch) {
        JSONObject o = new JSONObject();
        if (StrUtil.isNotBlank(baseExtraJson)) {
            try {
                o = JSONUtil.parseObj(baseExtraJson.trim());
            } catch (Exception ignored) {
                o = new JSONObject();
            }
        }
        if (patch == null) {
            return o.isEmpty() ? null : o.toString();
        }
        putIfPresent(o, "productKey", patch.getProductKey());
        putIfPresent(o, "menuIds", patch.getMenuIds());
        if (patch.getPrimaryMenuId() != null) {
            o.set("primaryMenuId", patch.getPrimaryMenuId());
        }
        putIfPresent(o, "picUrl", patch.getPicUrl());
        if (patch.getStatus() != null) {
            o.set("status", patch.getStatus());
        }
        if (patch.getDeviceType() != null) {
            o.set("deviceType", patch.getDeviceType());
        }
        if (patch.getNetType() != null) {
            o.set("netType", patch.getNetType());
        }
        if (patch.getLocationType() != null) {
            o.set("locationType", patch.getLocationType());
        }
        putIfPresent(o, "codecType", patch.getCodecType());
        putIfPresent(o, "jobConfig", patch.getJobConfig());
        return o.isEmpty() ? null : o.toString();
    }

    public static String toJson(IbmsProductExtra extra) {
        if (extra == null) {
            return null;
        }
        return merge(null, extra);
    }

    public static String getCodecTypeOrNull(IbmsProductDO product) {
        if (product == null) {
            return null;
        }
        IbmsProductExtra e = parse(product.getExtra());
        return StrUtil.trimToNull(e.getCodecType());
    }

    private static void putIfPresent(JSONObject o, String key, String val) {
        if (StrUtil.isNotBlank(val)) {
            o.set(key, val.trim());
        }
    }
}
