package cn.iocoder.yudao.module.iot.enums.device;

/**
 * 长辉设备类型常量
 * 
 * <p>统一管理长辉、德通等使用相同TCP协议的设备常量
 * <p>与 newgateway 插件保持一致</p>
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）</p>
 *
 * @author 长辉信息科技有限公司
 */
public final class ChanghuiDeviceTypeConstants {

    // ==================== 设备类型标识 ====================

    /**
     * 长辉设备类型
     * <p>与 newgateway 的 PluginConstants.DEVICE_TYPE_CHANGHUI 保持一致</p>
     */
    public static final String CHANGHUI = "CHANGHUI";

    /**
     * 德通设备类型（兼容旧代码）
     * @deprecated 请使用 {@link #CHANGHUI}
     */
    @Deprecated
    public static final String DETONG = "DETONG";

    // ==================== 设备类型编码 ====================

    /**
     * 测控一体化闸门
     */
    public static final int DEVICE_TYPE_INTEGRATED_GATE = 1;

    /**
     * 测控分体式闸门
     */
    public static final int DEVICE_TYPE_SPLIT_GATE = 2;

    /**
     * 退水闸
     */
    public static final int DEVICE_TYPE_DRAIN_GATE = 3;

    /**
     * 节制闸
     */
    public static final int DEVICE_TYPE_CONTROL_GATE = 4;

    /**
     * 进水闸
     */
    public static final int DEVICE_TYPE_INLET_GATE = 5;

    /**
     * 水位计
     */
    public static final int DEVICE_TYPE_WATER_LEVEL_METER = 6;

    /**
     * 流量计
     */
    public static final int DEVICE_TYPE_FLOW_METER = 7;

    /**
     * 流速仪
     */
    public static final int DEVICE_TYPE_CURRENT_METER = 8;

    /**
     * 渗压计
     */
    public static final int DEVICE_TYPE_PIEZOMETER = 9;

    /**
     * 荷载计
     */
    public static final int DEVICE_TYPE_LOAD_METER = 10;

    /**
     * 温度计
     */
    public static final int DEVICE_TYPE_THERMOMETER = 11;

    // ==================== 命令类型常量 ====================

    /**
     * 升级触发命令
     * <p>发送 AFN=0x02 升级触发帧</p>
     */
    public static final String COMMAND_UPGRADE_TRIGGER = "UPGRADE_TRIGGER";

    /**
     * 升级URL下发命令
     * <p>发送 AFN=0x10 升级URL帧</p>
     */
    public static final String COMMAND_UPGRADE_URL = "UPGRADE_URL";

    /**
     * 查询状态命令
     */
    public static final String COMMAND_QUERY_STATUS = "QUERY_STATUS";

    /**
     * 模式切换命令
     */
    public static final String COMMAND_MODE_SWITCH = "MODE_SWITCH";

    /**
     * 手动控制命令
     */
    public static final String COMMAND_MANUAL_CONTROL = "MANUAL_CONTROL";

    /**
     * 自动控制命令
     */
    public static final String COMMAND_AUTO_CONTROL = "AUTO_CONTROL";

    // ==================== 参数字段常量 ====================

    /**
     * 升级URL参数
     */
    public static final String PARAM_URL = "url";

    /**
     * 测站编码参数
     */
    public static final String PARAM_STATION_CODE = "stationCode";

    /**
     * 租户ID参数
     */
    public static final String PARAM_TENANT_ID = "tenantId";

    /**
     * 控制动作参数
     */
    public static final String PARAM_ACTION = "action";

    /**
     * 目标值参数
     */
    public static final String PARAM_TARGET_VALUE = "targetValue";

    /**
     * 控制模式参数
     */
    public static final String PARAM_CONTROL_MODE = "controlMode";

    // ==================== 升级状态常量 ====================

    /**
     * 升级状态：开始
     */
    public static final String UPGRADE_STATUS_START = "START";

    /**
     * 升级状态：进行中
     */
    public static final String UPGRADE_STATUS_PROGRESS = "PROGRESS";

    /**
     * 升级状态：完成
     */
    public static final String UPGRADE_STATUS_COMPLETE = "COMPLETE";

    /**
     * 升级状态：失败
     */
    public static final String UPGRADE_STATUS_FAILED = "FAILED";

    // ==================== 设备状态常量 ====================

    /**
     * 设备状态：离线
     */
    public static final int STATUS_OFFLINE = 0;

    /**
     * 设备状态：在线
     */
    public static final int STATUS_ONLINE = 1;

    // ==================== 消息主题常量 ====================

    /**
     * 长辉设备事件主题前缀
     */
    public static final String TOPIC_PREFIX = "changhui";

    /**
     * 设备状态变更主题
     */
    public static final String TOPIC_DEVICE_STATUS = "changhui/device/status";

    /**
     * 设备数据上报主题
     */
    public static final String TOPIC_DEVICE_DATA = "changhui/device/data";

    /**
     * 设备报警主题
     */
    public static final String TOPIC_DEVICE_ALARM = "changhui/device/alarm";

    /**
     * 升级进度主题
     */
    public static final String TOPIC_UPGRADE_PROGRESS = "changhui/upgrade/progress";

    private ChanghuiDeviceTypeConstants() {
        // 私有构造函数，防止实例化
    }
}
