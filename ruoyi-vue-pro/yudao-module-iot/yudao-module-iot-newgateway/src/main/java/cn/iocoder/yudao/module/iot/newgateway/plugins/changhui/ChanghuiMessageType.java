package cn.iocoder.yudao.module.iot.newgateway.plugins.changhui;

/**
 * 长辉消息类型枚举
 * 
 * <p>定义长辉协议中的消息类型，基于 AFN（应用功能码）字段。</p>
 * 
 * <h2>消息类型分类</h2>
 * <ul>
 *     <li>心跳消息: 0x3C</li>
 *     <li>升级相关: 0x02, 0x10, 0x15, 0x66, 0x67, 0x68</li>
 *     <li>数据采集: 0x06, 0x07, 0x08, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x83</li>
 *     <li>控制命令: 0x20, 0x21, 0x22, 0x23</li>
 *     <li>报警消息: 0x30</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see ChanghuiProtocolCodec
 * @see ChanghuiMessage
 */
public enum ChanghuiMessageType {

    // ==================== 心跳消息 ====================

    /**
     * 心跳消息 (AFN=0x3C)
     */
    HEARTBEAT((byte) 0x3C, "心跳"),

    // ==================== 升级相关消息 ====================

    /**
     * 升级触发命令 (AFN=0x02)
     */
    UPGRADE_TRIGGER((byte) 0x02, "升级触发"),

    /**
     * 升级URL下发 (AFN=0x10)
     */
    UPGRADE_URL((byte) 0x10, "升级URL"),

    /**
     * 升级开始状态上报 (AFN=0x15)
     */
    UPGRADE_STATUS_START((byte) 0x15, "升级开始"),

    /**
     * 升级进度上报 (AFN=0x66)
     */
    UPGRADE_STATUS_PROGRESS((byte) 0x66, "升级进度"),

    /**
     * 升级完成上报 (AFN=0x67)
     */
    UPGRADE_STATUS_COMPLETE((byte) 0x67, "升级完成"),

    /**
     * 升级失败上报 (AFN=0x68)
     */
    UPGRADE_STATUS_FAILED((byte) 0x68, "升级失败"),

    // ==================== 数据采集消息 ====================

    /**
     * 水位数据 (AFN=0x06)
     * <p>数据格式：水位值（米）</p>
     */
    DATA_WATER_LEVEL((byte) 0x06, "水位数据"),

    /**
     * 瞬时流量数据 (AFN=0x07)
     * <p>数据格式：流量值（L/s）</p>
     */
    DATA_INSTANT_FLOW((byte) 0x07, "瞬时流量"),

    /**
     * 瞬时流速数据 (AFN=0x08)
     * <p>数据格式：流速值（m/s）</p>
     */
    DATA_INSTANT_VELOCITY((byte) 0x08, "瞬时流速"),

    /**
     * 累计流量数据 (AFN=0x0A)
     * <p>数据格式：累计流量值（m³）</p>
     */
    DATA_CUMULATIVE_FLOW((byte) 0x0A, "累计流量"),

    /**
     * 闸位数据 (AFN=0x0B)
     * <p>数据格式：闸位值（mm）</p>
     */
    DATA_GATE_POSITION((byte) 0x0B, "闸位数据"),

    /**
     * 渗压数据 (AFN=0x0C)
     * <p>数据格式：渗压值（kPa）</p>
     */
    DATA_SEEPAGE_PRESSURE((byte) 0x0C, "渗压数据"),

    /**
     * 温度数据 (AFN=0x0D)
     * <p>数据格式：温度值（°C）</p>
     */
    DATA_TEMPERATURE((byte) 0x0D, "温度数据"),

    /**
     * 荷载数据 (AFN=0x0E)
     * <p>数据格式：荷载值（kN）</p>
     */
    DATA_LOAD((byte) 0x0E, "荷载数据"),

    /**
     * 多指标查询 (AFN=0x83)
     * <p>查询多个指标的数据</p>
     */
    DATA_MULTI_QUERY((byte) 0x83, "多指标查询"),

    // ==================== 控制命令消息 ====================

    /**
     * 模式切换命令 (AFN=0x20)
     * <p>切换设备工作模式（手动/自动）</p>
     */
    CONTROL_MODE_SWITCH((byte) 0x20, "模式切换"),

    /**
     * 手动控制命令 (AFN=0x21)
     * <p>手动控制：升/降/停</p>
     */
    CONTROL_MANUAL((byte) 0x21, "手动控制"),

    /**
     * 自动控制命令 (AFN=0x22)
     * <p>自动控制：设置目标值</p>
     */
    CONTROL_AUTO((byte) 0x22, "自动控制"),

    /**
     * 控制响应 (AFN=0x23)
     * <p>控制命令的响应</p>
     */
    CONTROL_RESPONSE((byte) 0x23, "控制响应"),

    // ==================== 报警消息 ====================

    /**
     * 报警消息 (AFN=0x30)
     * <p>设备报警上报</p>
     */
    ALARM((byte) 0x30, "报警消息"),

    // ==================== 未知消息 ====================

    /**
     * 未知消息类型
     */
    UNKNOWN((byte) 0x00, "未知");

    /**
     * AFN 值
     */
    private final byte afn;

    /**
     * 描述
     */
    private final String description;

    ChanghuiMessageType(byte afn, String description) {
        this.afn = afn;
        this.description = description;
    }

    /**
     * 获取 AFN 值
     *
     * @return AFN 值
     */
    public byte getAfn() {
        return afn;
    }

    /**
     * 获取描述
     *
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据 AFN 值获取消息类型
     *
     * @param afn AFN 值
     * @return 消息类型，如果未找到则返回 UNKNOWN
     */
    public static ChanghuiMessageType fromAfn(byte afn) {
        for (ChanghuiMessageType type : values()) {
            if (type.afn == afn) {
                return type;
            }
        }
        return UNKNOWN;
    }

    /**
     * 判断是否为升级状态消息
     *
     * @return 是否为升级状态消息
     */
    public boolean isUpgradeStatus() {
        return this == UPGRADE_STATUS_START
                || this == UPGRADE_STATUS_PROGRESS
                || this == UPGRADE_STATUS_COMPLETE
                || this == UPGRADE_STATUS_FAILED;
    }

    /**
     * 判断是否为升级相关消息（包括命令和状态）
     *
     * @return 是否为升级相关消息
     */
    public boolean isUpgradeRelated() {
        return this == UPGRADE_TRIGGER
                || this == UPGRADE_URL
                || isUpgradeStatus();
    }

    /**
     * 判断是否为数据采集消息
     *
     * @return 是否为数据采集消息
     */
    public boolean isDataCollection() {
        return this == DATA_WATER_LEVEL
                || this == DATA_INSTANT_FLOW
                || this == DATA_INSTANT_VELOCITY
                || this == DATA_CUMULATIVE_FLOW
                || this == DATA_GATE_POSITION
                || this == DATA_SEEPAGE_PRESSURE
                || this == DATA_TEMPERATURE
                || this == DATA_LOAD
                || this == DATA_MULTI_QUERY;
    }

    /**
     * 判断是否为控制命令消息
     *
     * @return 是否为控制命令消息
     */
    public boolean isControlCommand() {
        return this == CONTROL_MODE_SWITCH
                || this == CONTROL_MANUAL
                || this == CONTROL_AUTO
                || this == CONTROL_RESPONSE;
    }

    /**
     * 判断是否为报警消息
     *
     * @return 是否为报警消息
     */
    public boolean isAlarm() {
        return this == ALARM;
    }

    /**
     * 获取消息类型的分类
     *
     * @return 消息分类
     */
    public String getCategory() {
        if (this == HEARTBEAT) {
            return "HEARTBEAT";
        } else if (isUpgradeRelated()) {
            return "UPGRADE";
        } else if (isDataCollection()) {
            return "DATA";
        } else if (isControlCommand()) {
            return "CONTROL";
        } else if (isAlarm()) {
            return "ALARM";
        } else {
            return "UNKNOWN";
        }
    }
}
