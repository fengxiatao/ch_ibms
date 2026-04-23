package cn.iocoder.yudao.module.iot.service.ibms.device.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.service.video.IbmsDeviceVideoNetworkResolver;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 台账 {@link IbmsDeviceDO} 与 {@link IbmsDeviceRuntimeDO} 上可读的状态/展示辅助方法。
 */
public final class IbmsDeviceLedgerRuntimeHelper {

    private IbmsDeviceLedgerRuntimeHelper() {
    }

    /**
     * 优先运行态 {@code state}，否则读台账 {@code extra.gatewayRuntimeState}。
     */
    public static Integer resolveDeviceState(IbmsDeviceDO ledger, IbmsDeviceRuntimeDO runtime) {
        if (runtime != null && runtime.getState() != null) {
            return runtime.getState();
        }
        return parseGatewayRuntimeStateFromExtra(ledger != null ? ledger.getExtra() : null);
    }

    public static Integer parseGatewayRuntimeStateFromExtra(String extraJson) {
        if (StrUtil.isBlank(extraJson)) {
            return null;
        }
        try {
            return JSONUtil.parseObj(extraJson.trim()).getInt("gatewayRuntimeState");
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 供仍依赖 {@link IotDeviceDO} 的通道同步等逻辑使用：用 IBMS 台账 + 运行态拼出与历史 NVR 设备 config 等价的壳对象。
     */
    public static IotDeviceDO buildLegacyNvrDeviceShell(IbmsDeviceDO ibms, IbmsDeviceRuntimeDO runtime) {
        if (ibms == null) {
            return null;
        }
        IbmsDeviceVideoNetworkResolver.NetworkParams net = IbmsDeviceVideoNetworkResolver.resolve(ibms, runtime);
        GenericDeviceConfig cfg = new GenericDeviceConfig();
        cfg.setDeviceType("NVR");
        String ip = StringUtils.defaultIfBlank(net.ip, "");
        cfg.set("ipAddress", ip);
        cfg.set("ip", ip);
        cfg.set("username", StringUtils.defaultIfBlank(net.username, "admin"));
        cfg.set("password", StringUtils.defaultIfBlank(net.password, "admin123"));
        cfg.set("httpPort", net.httpPort);
        cfg.set("rtspPort", net.rtspPort);
        IotDeviceDO d = new IotDeviceDO();
        d.setId(ibms.getId());
        d.setDeviceName(ibms.getName());
        d.setNickname(ibms.getNickname());
        d.setProductId(ibms.getIbmsProductId() != null ? ibms.getIbmsProductId() : 4L);
        d.setConfig(cfg);
        Integer st = resolveDeviceState(ibms, runtime);
        d.setState(st);
        return d;
    }

    /**
     * 供门禁能力刷新等仍依赖 {@link IotDeviceDO} 的路径使用：从台账 {@code extra} 与运行态拼 {@link GenericDeviceConfig} 壳。
     */
    public static IotDeviceDO buildLegacyAccessDeviceShell(IbmsDeviceDO ibms, IbmsDeviceRuntimeDO runtime) {
        if (ibms == null) {
            return null;
        }
        IbmsDeviceVideoNetworkResolver.NetworkParams net = IbmsDeviceVideoNetworkResolver.resolve(ibms, runtime);
        GenericDeviceConfig cfg = new GenericDeviceConfig();
        cfg.setDeviceType(AccessDeviceTypeConstants.getAccessDeviceType(ibms));
        cfg.setIpAddress(StringUtils.defaultIfBlank(net.ip, ""));
        int port = 37777;
        if (StrUtil.isNotBlank(ibms.getExtra())) {
            try {
                cn.hutool.json.JSONObject ex = JSONUtil.parseObj(ibms.getExtra().trim());
                Integer p = ex.getInt("tcpPort");
                if (p == null) {
                    p = ex.getInt("port");
                }
                if (p != null) {
                    port = p;
                }
                Object caps = ex.get("accessCapabilities");
                if (caps != null) {
                    cfg.set("accessCapabilities", caps);
                }
            } catch (Exception ignored) {
                // keep defaults
            }
        }
        if (runtime != null && runtime.getConfig() != null) {
            try {
                Map<String, Object> m = runtime.getConfig().toMap();
                Object tcp = m.get("tcpPort");
                if (tcp instanceof Number) {
                    port = ((Number) tcp).intValue();
                } else if (tcp != null) {
                    try {
                        port = Integer.parseInt(tcp.toString());
                    } catch (NumberFormatException ignore) {
                        // ignore
                    }
                }
            } catch (Exception ignored) {
                // ignore
            }
        }
        cfg.setPort(port);
        cfg.set("username", StringUtils.defaultIfBlank(net.username, "admin"));
        cfg.set("password", StringUtils.defaultIfBlank(net.password, "admin123"));

        IotDeviceDO d = new IotDeviceDO();
        d.setId(ibms.getId());
        d.setTenantId(ibms.getTenantId());
        d.setSubsystemCode(ibms.getSubsystemCode());
        d.setDeviceName(ibms.getName());
        d.setNickname(ibms.getNickname());
        d.setDeviceKey(ibms.getDeviceKey());
        d.setProductId(ibms.getIbmsProductId());
        d.setProductKey(ibms.getProductKey());
        d.setConfig(cfg);
        d.setState(resolveDeviceState(ibms, runtime));
        if (runtime != null) {
            d.setOnlineTime(runtime.getOnlineTime());
            d.setOfflineTime(runtime.getOfflineTime());
            d.setActiveTime(runtime.getActiveTime());
            d.setFirmwareId(runtime.getFirmwareId());
        }
        return d;
    }

    /**
     * 供 {@link cn.iocoder.yudao.module.iot.collector.camera.UniversalCameraCollector} 等仍依赖 {@link IotDeviceDO} 的轮询逻辑使用。
     */
    public static IotDeviceDO buildLegacyCameraCollectorShell(IbmsDeviceDO ibms, IbmsDeviceRuntimeDO runtime) {
        if (ibms == null) {
            return null;
        }
        IbmsDeviceVideoNetworkResolver.NetworkParams net = IbmsDeviceVideoNetworkResolver.resolve(ibms, runtime);
        GenericDeviceConfig cfg = new GenericDeviceConfig();
        cfg.setDeviceType("CAMERA");
        String ip = StringUtils.defaultIfBlank(net.ip, "");
        cfg.setIpAddress(ip);
        cfg.set("ip", ip);
        cfg.set("username", StringUtils.defaultIfBlank(net.username, "admin"));
        cfg.set("password", StringUtils.defaultIfBlank(net.password, "admin123"));
        cfg.set("httpPort", net.httpPort);
        cfg.set("rtspPort", net.rtspPort);
        cfg.set("vendor", resolveCameraVendorFromIbms(ibms));

        IotDeviceDO d = new IotDeviceDO();
        d.setId(ibms.getId());
        d.setTenantId(ibms.getTenantId());
        d.setDeviceName(ibms.getName());
        d.setNickname(ibms.getNickname());
        d.setProductId(ibms.getIbmsProductId());
        d.setProductKey(ibms.getProductKey());
        d.setDeviceKey(ibms.getDeviceKey());
        d.setConfig(cfg);
        d.setState(resolveDeviceState(ibms, runtime));
        if (runtime != null) {
            d.setOnlineTime(runtime.getOnlineTime());
            d.setOfflineTime(runtime.getOfflineTime());
            d.setActiveTime(runtime.getActiveTime());
            d.setFirmwareId(runtime.getFirmwareId());
        }
        return d;
    }

    /**
     * OTA 推送/进度：仅需 id、租户、在线态、产品、运行态固件等字段的兼容壳。
     */
    public static IotDeviceDO buildLegacyOtaDeviceShell(IbmsDeviceDO ibms, IbmsDeviceRuntimeDO runtime) {
        if (ibms == null) {
            return null;
        }
        IotDeviceDO d = new IotDeviceDO();
        d.setId(ibms.getId());
        d.setTenantId(ibms.getTenantId());
        d.setDeviceName(ibms.getName());
        d.setProductId(ibms.getIbmsProductId());
        d.setProductKey(StrUtil.blankToDefault(ibms.getProductKey(), null));
        d.setState(resolveDeviceState(ibms, runtime));
        if (runtime != null && runtime.getFirmwareId() != null) {
            d.setFirmwareId(runtime.getFirmwareId());
        }
        return d;
    }

    private static String resolveCameraVendorFromIbms(IbmsDeviceDO ibms) {
        String brand = ibms.getBrand();
        if (StrUtil.isNotBlank(brand)) {
            String b = brand.trim();
            if (b.equalsIgnoreCase("DAH") || b.equalsIgnoreCase("DAHUA")) {
                return "dahua";
            }
            if (b.equalsIgnoreCase("HIK") || b.equalsIgnoreCase("HIKVISION")) {
                return "hikvision";
            }
        }
        if (StrUtil.isNotBlank(ibms.getExtra())) {
            try {
                cn.hutool.json.JSONObject ex = JSONUtil.parseObj(ibms.getExtra().trim());
                String v = ex.getStr("vendor");
                if (StrUtil.isNotBlank(v)) {
                    return v.trim();
                }
            } catch (Exception ignored) {
                // ignore
            }
        }
        return "";
    }
}
