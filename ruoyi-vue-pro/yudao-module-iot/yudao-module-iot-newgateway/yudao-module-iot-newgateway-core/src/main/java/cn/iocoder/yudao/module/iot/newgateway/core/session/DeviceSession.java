package cn.iocoder.yudao.module.iot.newgateway.core.session;

/**
 * 网关侧设备会话视图（连接句柄由插件管理，此处仅保留可观测字段）。
 */
public interface DeviceSession {

    Long getDeviceId();

    String getPluginId();

    String getVendorKey();

    long getLastActiveEpochMillis();
}
