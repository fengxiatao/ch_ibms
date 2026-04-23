package cn.iocoder.yudao.module.iot.service.video;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import org.apache.commons.lang3.StringUtils;

/**
 * 从 IBMS 台账 + 运行态 config + 台账 extra 解析视频/RTSP 所需的网络与认证参数（与 {@code DhVideoController} 逻辑一致）。
 */
public final class IbmsDeviceVideoNetworkResolver {

    private IbmsDeviceVideoNetworkResolver() {
    }

    public static final class NetworkParams {
        public String ip = "";
        public int httpPort = 80;
        public int rtspPort = 554;
        public String username;
        public String password;
    }

    public static NetworkParams resolve(IbmsDeviceDO device, IbmsDeviceRuntimeDO runtime) {
        NetworkParams p = new NetworkParams();
        if (device == null) {
            return p;
        }
        p.ip = StringUtils.defaultIfBlank(StrUtil.trimToEmpty(device.getIp()), "");
        DeviceConfig runtimeConfig = runtime != null ? runtime.getConfig() : null;
        if (StringUtils.isBlank(p.ip) && runtimeConfig != null) {
            p.ip = StringUtils.defaultIfBlank(runtimeConfig.getIpAddress(), "");
        }
        if (runtimeConfig != null) {
            try {
                JSONObject cfg = JSONUtil.parseObj(JSONUtil.toJsonStr(runtimeConfig.toMap()));
                p.httpPort = cfg.getInt("httpPort", p.httpPort);
                p.rtspPort = cfg.getInt("rtspPort", p.rtspPort);
                p.username = StringUtils.defaultIfBlank(cfg.getStr("username"), p.username);
                p.password = StringUtils.defaultIfBlank(cfg.getStr("password"), p.password);
                p.ip = StringUtils.defaultIfBlank(p.ip, cfg.getStr("ip"));
            } catch (Exception ignored) {
                // ignore
            }
        }
        applyLedgerExtra(device.getExtra(), p);
        p.ip = StringUtils.defaultIfBlank(p.ip, "");
        return p;
    }

    private static void applyLedgerExtra(String extraJson, NetworkParams p) {
        if (StrUtil.isBlank(extraJson)) {
            return;
        }
        try {
            JSONObject ex = JSONUtil.parseObj(extraJson.trim());
            if (StringUtils.isBlank(p.ip)) {
                p.ip = StringUtils.defaultIfBlank(ex.getStr("ip"), "");
                if (StringUtils.isBlank(p.ip)) {
                    p.ip = StringUtils.defaultIfBlank(ex.getStr("host"), "");
                }
            }
            if (ex.containsKey("httpPort")) {
                p.httpPort = ex.getInt("httpPort", p.httpPort);
            }
            if (ex.containsKey("rtspPort")) {
                p.rtspPort = ex.getInt("rtspPort", p.rtspPort);
            }
            if (StringUtils.isBlank(p.username)) {
                p.username = ex.getStr("username");
            }
            if (StringUtils.isBlank(p.password)) {
                p.password = ex.getStr("password");
            }
        } catch (Exception ignored) {
            // ignore
        }
    }
}
