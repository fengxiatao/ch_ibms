package cn.iocoder.yudao.module.iot.newgateway.core.capability;

import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceCapabilitySnapshot;

/**
 * 插件能力快照采集（与 {@code @DevicePlugin#capabilityRefreshEnabled} 对齐，供定时任务扩展）。
 */
public interface GatewayCapabilityProvider {

    /**
     * @return 网关 deviceType，如 NVR
     */
    String supportedDeviceType();

    /**
     * @return 采集失败返回 null
     */
    DeviceCapabilitySnapshot buildSnapshot(Long deviceId);
}
