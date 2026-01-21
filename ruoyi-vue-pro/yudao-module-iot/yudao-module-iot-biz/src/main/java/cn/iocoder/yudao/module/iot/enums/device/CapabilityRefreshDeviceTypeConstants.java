package cn.iocoder.yudao.module.iot.enums.device;

/**
 * 需要能力刷新的设备类型常量（与 newgateway 插件定义保持一致）
 *
 * <p>目标：让业务侧定时任务/激活后刷新只覆盖“有能力概念”的设备（如门禁一/二代、NVR），
 * 避免对报警主机、长辉 TCP 等设备做无意义能力刷新。</p>
 */
public final class CapabilityRefreshDeviceTypeConstants {

    private CapabilityRefreshDeviceTypeConstants() {}

    /**
     * 是否需要刷新能力
     *
     * @param deviceType newgateway 插件 deviceType（如 ACCESS_GEN1/ACCESS_GEN2/NVR）
     */
    public static boolean isCapabilityRefreshEnabled(String deviceType) {
        if (deviceType == null || deviceType.isBlank()) {
            return false;
        }
        return AccessDeviceTypeConstants.ACCESS_GEN1.equalsIgnoreCase(deviceType)
                || AccessDeviceTypeConstants.ACCESS_GEN2.equalsIgnoreCase(deviceType)
                || NvrDeviceTypeConstants.NVR.equalsIgnoreCase(deviceType);
    }
}

