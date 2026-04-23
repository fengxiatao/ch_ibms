package cn.iocoder.yudao.module.iot.service.ibms.device.support;

import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;

import java.util.Map;

/**
 * 大华 SDK 等场景下从运行态 config 解析 TCP 登录端口（默认 37777）。
 */
public final class IbmsDeviceDahuaSdkHelper {

    private IbmsDeviceDahuaSdkHelper() {
    }

    public static int resolveDahuaSdkPort(IbmsDeviceRuntimeDO runtime) {
        if (runtime == null || runtime.getConfig() == null) {
            return 37777;
        }
        try {
            Map<String, Object> m = runtime.getConfig().toMap();
            Object p = m.get("port");
            if (p == null) {
                p = m.get("tcpPort");
            }
            if (p instanceof Number) {
                return ((Number) p).intValue();
            }
            if (p != null) {
                return Integer.parseInt(p.toString());
            }
        } catch (Exception ignored) {
            // ignore
        }
        return 37777;
    }
}
