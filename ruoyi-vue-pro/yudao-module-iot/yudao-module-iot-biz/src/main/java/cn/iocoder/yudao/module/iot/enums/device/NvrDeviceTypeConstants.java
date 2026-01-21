package cn.iocoder.yudao.module.iot.enums.device;

/**
 * NVR 设备类型常量
 * 
 * <p>用于标识 NVR/DVR 设备类型，与 newgateway 插件保持一致</p>
 *
 * @author 长辉信息科技有限公司
 */
public final class NvrDeviceTypeConstants {

    /**
     * NVR 设备类型
     * <p>与 newgateway 的 PluginConstants.DEVICE_TYPE_NVR 保持一致</p>
     */
    public static final String NVR = "NVR";

    // ==================== 命令类型常量 ====================

    /**
     * PTZ 控制命令
     */
    public static final String COMMAND_PTZ_CONTROL = "PTZ_CONTROL";

    /**
     * PTZ 停止命令
     */
    public static final String COMMAND_PTZ_STOP = "PTZ_STOP";

    /**
     * 搜索录像命令
     */
    public static final String COMMAND_SEARCH_RECORDS = "SEARCH_RECORDS";

    /**
     * 开始录像命令
     */
    public static final String COMMAND_START_RECORDING = "START_RECORDING";

    /**
     * 停止录像命令
     */
    public static final String COMMAND_STOP_RECORDING = "STOP_RECORDING";

    /**
     * 扫描通道命令
     */
    public static final String COMMAND_SCAN_CHANNELS = "SCAN_CHANNELS";

    /**
     * 获取截图命令
     */
    public static final String COMMAND_CAPTURE_SNAPSHOT = "CAPTURE_SNAPSHOT";

    /**
     * 获取能力集命令
     */
    public static final String COMMAND_GET_CAPABILITIES = "GET_CAPABILITIES";

    // ==================== PTZ 方向常量 ====================

    /**
     * PTZ 向上
     */
    public static final String PTZ_UP = "UP";

    /**
     * PTZ 向下
     */
    public static final String PTZ_DOWN = "DOWN";

    /**
     * PTZ 向左
     */
    public static final String PTZ_LEFT = "LEFT";

    /**
     * PTZ 向右
     */
    public static final String PTZ_RIGHT = "RIGHT";

    /**
     * PTZ 放大
     */
    public static final String PTZ_ZOOM_IN = "ZOOM_IN";

    /**
     * PTZ 缩小
     */
    public static final String PTZ_ZOOM_OUT = "ZOOM_OUT";

    // ==================== 参数字段常量 ====================

    /**
     * IP 地址参数
     */
    public static final String PARAM_IP = "ip";

    /**
     * 端口参数
     */
    public static final String PARAM_PORT = "port";

    /**
     * 用户名参数
     */
    public static final String PARAM_USERNAME = "username";

    /**
     * 密码参数
     */
    public static final String PARAM_PASSWORD = "password";

    /**
     * 通道号参数
     */
    public static final String PARAM_CHANNEL_NO = "channelNo";

    /**
     * PTZ 方向参数
     */
    public static final String PARAM_DIRECTION = "direction";

    /**
     * PTZ 速度参数
     */
    public static final String PARAM_SPEED = "speed";

    /**
     * Pan 参数（水平移动速度）
     */
    public static final String PARAM_PAN = "pan";

    /**
     * Tilt 参数（垂直移动速度）
     */
    public static final String PARAM_TILT = "tilt";

    /**
     * Zoom 参数（变焦速度）
     */
    public static final String PARAM_ZOOM = "zoom";

    /**
     * 超时时间参数（毫秒）
     */
    public static final String PARAM_TIMEOUT_MS = "timeoutMs";

    /**
     * 开始时间参数
     */
    public static final String PARAM_START_TIME = "startTime";

    /**
     * 结束时间参数
     */
    public static final String PARAM_END_TIME = "endTime";

    /**
     * 录像类型参数
     */
    public static final String PARAM_RECORD_TYPE = "recordType";

    private NvrDeviceTypeConstants() {
        // 私有构造函数，防止实例化
    }
}
