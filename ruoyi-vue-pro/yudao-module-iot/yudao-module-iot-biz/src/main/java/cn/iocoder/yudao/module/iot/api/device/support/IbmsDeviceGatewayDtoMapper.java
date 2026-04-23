package cn.iocoder.yudao.module.iot.api.device.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;

/**
 * 将 IBMS 设备台账映射为网关 RPC 使用的 {@link IotDeviceRespDTO}。
 * <p>
 * 约定：<b>网关侧命令与连接中的 deviceId 与 {@code ibms_device.id} 一致</b>；
 * 接入参数以 {@link IbmsDeviceDO#getExtra()} JSON 为主（如 tcpPort、username、password），
 * 列字段 {@code ip}、{@code protocol} 可在 extra 缺省时作为补充。
 * </p>
 */
public final class IbmsDeviceGatewayDtoMapper {

    private IbmsDeviceGatewayDtoMapper() {
    }

    public static IotDeviceRespDTO toGatewayDto(IbmsDeviceDO d) {
        if (d == null) {
            return null;
        }
        JSONObject cfg = parseExtraObject(d.getExtra());
        if (StrUtil.isBlank(cfg.getStr("ip")) && StrUtil.isBlank(cfg.getStr("host"))) {
            if (StrUtil.isNotBlank(d.getIp())) {
                cfg.set("ip", d.getIp().trim());
            }
        }
        if (StrUtil.isBlank(cfg.getStr("protocol")) && StrUtil.isNotBlank(d.getProtocol())) {
            cfg.set("protocol", d.getProtocol().trim());
        }

        IotDeviceRespDTO dto = new IotDeviceRespDTO();
        dto.setId(d.getId());
        dto.setTenantId(d.getTenantId());
        dto.setDeviceName(d.getName());
        dto.setProductKey(d.getProductKey());
        String deviceKey = StrUtil.trimToNull(cfg.getStr("deviceKey"));
        dto.setDeviceKey(deviceKey != null ? deviceKey : d.getDeviceCode());
        dto.setAddress(resolveAddress(d, cfg));
        dto.setConfig(cfg.toString());
        dto.setBrand(d.getBrand());
        dto.setDeviceType(resolveGatewayDeviceType(d.getSystemCode(), d.getDeviceTypeCode()));
        return dto;
    }

    private static JSONObject parseExtraObject(String extra) {
        if (StrUtil.isBlank(extra)) {
            return new JSONObject();
        }
        try {
            return JSONUtil.parseObj(extra.trim());
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    /**
     * IBMS 台账类型映射到 newgateway 插件 deviceType（最小规则集，可按项目扩展）。
     */
    public static String resolveGatewayDeviceType(String systemCode, String deviceTypeCode) {
        if (deviceTypeCode == null) {
            return null;
        }
        String u = deviceTypeCode.trim().toUpperCase();
        if ("NVR".equals(u)) {
            return "NVR";
        }
        if ("CAM".equals(u) && systemCode != null && "VI".equalsIgnoreCase(systemCode.trim())) {
            return "IPC";
        }
        return u;
    }

    private static String resolveAddress(IbmsDeviceDO d, JSONObject cfg) {
        String ip = cfg.getStr("ip");
        if (StrUtil.isNotBlank(ip)) {
            return ip.trim();
        }
        String host = cfg.getStr("host");
        if (StrUtil.isNotBlank(host)) {
            return host.trim();
        }
        return StrUtil.trimToNull(d.getIp());
    }
}
