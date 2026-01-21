package cn.iocoder.yudao.module.iot.enums.device;

/**
 * 门禁设备类型常量
 * 
 * <p>用于区分一代门禁控制器和二代门禁（人脸一体机）</p>
 * 
 * <p>判断逻辑：</p>
 * <ul>
 *   <li>supportVideo = true → ACCESS_GEN2（二代门禁，人脸一体机，支持视频预览）</li>
 *   <li>supportVideo = false 或 null → ACCESS_GEN1（一代门禁控制器，不支持视频）</li>
 * </ul>
 *
 * @author 长辉信息科技有限公司
 */
public final class AccessDeviceTypeConstants {

    /**
     * 门禁一代设备类型
     * <p>传统门禁控制器，使用 Recordset 操作进行用户/卡管理</p>
     */
    public static final String ACCESS_GEN1 = "ACCESS_GEN1";

    /**
     * 门禁二代设备类型
     * <p>人脸一体机，使用标准 API 进行用户/卡/人脸管理，支持人脸和指纹识别</p>
     */
    public static final String ACCESS_GEN2 = "ACCESS_GEN2";

    private AccessDeviceTypeConstants() {
        // 私有构造函数，防止实例化
    }

    /**
     * 根据是否支持视频判断设备类型
     * 
     * @param supportVideo 是否支持视频预览
     * @return 设备类型（ACCESS_GEN1 或 ACCESS_GEN2）
     */
    public static String getDeviceType(Boolean supportVideo) {
        return Boolean.TRUE.equals(supportVideo) ? ACCESS_GEN2 : ACCESS_GEN1;
    }

    /**
     * 根据配置中显式标识的 deviceType 优先判断设备类型
     * <p>
     * 现场数据可能在 iot_device.config 中直接写入 "deviceType": "ACCESS_GEN1"/"ACCESS_GEN2"，
     * 此时不能再依赖 supportVideo 推断（例如一代也可能 supportVideo=true）。
     *
     * @param configDeviceType 配置中的 deviceType（可为 null）
     * @param supportVideo     是否支持视频（兜底推断）
     * @return ACCESS_GEN1 或 ACCESS_GEN2
     */
    public static String resolveDeviceType(String configDeviceType, Boolean supportVideo) {
        if (configDeviceType != null) {
            String normalized = configDeviceType.trim().toUpperCase();
            if (ACCESS_GEN1.equals(normalized)) {
                return ACCESS_GEN1;
            }
            if (ACCESS_GEN2.equals(normalized)) {
                return ACCESS_GEN2;
            }
        }
        return getDeviceType(supportVideo);
    }
}
