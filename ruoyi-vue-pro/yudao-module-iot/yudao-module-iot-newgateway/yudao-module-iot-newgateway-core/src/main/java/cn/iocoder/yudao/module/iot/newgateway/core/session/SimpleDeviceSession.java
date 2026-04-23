package cn.iocoder.yudao.module.iot.newgateway.core.session;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SimpleDeviceSession implements DeviceSession {
    Long deviceId;
    String pluginId;
    String vendorKey;
    long lastActiveEpochMillis;

    @Override
    public Long getDeviceId() {
        return deviceId;
    }

    @Override
    public String getPluginId() {
        return pluginId;
    }

    @Override
    public String getVendorKey() {
        return vendorKey;
    }

    @Override
    public long getLastActiveEpochMillis() {
        return lastActiveEpochMillis;
    }
}
