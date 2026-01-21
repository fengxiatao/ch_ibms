package cn.iocoder.yudao.module.iot.newgateway.plugins;

/**
 * 插件常量定义
 * 
 * 定义各设备插件使用的常量值
 */
public final class PluginConstants {

    // ==================== 设备类型 ====================

    /**
     * 报警主机设备类型
     */
    public static final String DEVICE_TYPE_ALARM = "ALARM";

    /**
     * 长辉TCP设备类型
     */
    public static final String DEVICE_TYPE_CHANGHUI = "CHANGHUI";

    /**
     * 门禁一代设备类型
     */
    public static final String DEVICE_TYPE_ACCESS_GEN1 = "ACCESS_GEN1";

    /**
     * 门禁二代设备类型
     */
    public static final String DEVICE_TYPE_ACCESS_GEN2 = "ACCESS_GEN2";

    /**
     * NVR设备类型
     */
    public static final String DEVICE_TYPE_NVR = "NVR";

    /**
     * IPC摄像头设备类型
     */
    public static final String DEVICE_TYPE_IPC = "IPC";

    // ==================== 插件ID ====================

    /**
     * 报警主机插件ID
     */
    public static final String PLUGIN_ID_ALARM = "alarm";

    /**
     * 长辉TCP插件ID
     */
    public static final String PLUGIN_ID_CHANGHUI = "changhui";

    /**
     * 门禁一代插件ID
     */
    public static final String PLUGIN_ID_ACCESS_GEN1 = "access-gen1";

    /**
     * 门禁二代插件ID
     */
    public static final String PLUGIN_ID_ACCESS_GEN2 = "access-gen2";

    /**
     * NVR插件ID
     */
    public static final String PLUGIN_ID_NVR = "nvr";

    /**
     * IPC摄像头插件ID
     */
    public static final String PLUGIN_ID_IPC = "ipc";

    private PluginConstants() {
        // 私有构造函数，防止实例化
    }
}
