package cn.iocoder.yudao.module.iot.newgateway.plugins.alarm;

/**
 * 报警主机消息类型枚举
 * 
 * <p>定义 PS600 OPC 协议支持的消息类型。</p>
 * 
 * <h2>消息类型说明</h2>
 * <ul>
 *     <li>HB - 心跳消息</li>
 *     <li>ARM_ALL - 全局布防</li>
 *     <li>ARM_STAY - 留守布防</li>
 *     <li>DISARM - 撤防</li>
 *     <li>ARM_PARTITION - 分区布防</li>
 *     <li>DISARM_PARTITION - 分区撤防</li>
 *     <li>BYPASS_ZONE - 旁路防区</li>
 *     <li>UNBYPASS_ZONE - 取消旁路</li>
 *     <li>QUERY_STATUS - 状态查询</li>
 *     <li>ALARM - 报警事件</li>
 *     <li>STATUS - 状态上报</li>
 *     <li>ACK - 确认响应</li>
 *     <li>NAK - 否定响应</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see AlarmProtocolCodec
 */
public enum AlarmMessageType {

    /**
     * 心跳消息
     */
    HEARTBEAT("HB", "心跳"),

    /**
     * 全局布防
     */
    ARM_ALL("ARM_ALL", "全局布防"),

    /**
     * 留守布防
     */
    ARM_STAY("ARM_STAY", "留守布防"),

    /**
     * 撤防
     */
    DISARM("DISARM", "撤防"),

    /**
     * 分区布防
     */
    ARM_PARTITION("ARM_PARTITION", "分区布防"),

    /**
     * 分区撤防
     */
    DISARM_PARTITION("DISARM_PARTITION", "分区撤防"),

    /**
     * 旁路防区
     */
    BYPASS_ZONE("BYPASS_ZONE", "旁路防区"),

    /**
     * 取消旁路
     */
    UNBYPASS_ZONE("UNBYPASS_ZONE", "取消旁路"),

    /**
     * 状态查询
     */
    QUERY_STATUS("QUERY_STATUS", "状态查询"),

    /**
     * 报警事件
     */
    ALARM("ALARM", "报警事件"),

    /**
     * 状态上报
     */
    STATUS("STATUS", "状态上报"),

    /**
     * 确认响应
     */
    ACK("ACK", "确认响应"),

    /**
     * 否定响应
     */
    NAK("NAK", "否定响应"),

    /**
     * 未知消息类型
     */
    UNKNOWN("UNKNOWN", "未知");

    /**
     * 命令代码
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    AlarmMessageType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取命令代码
     *
     * @return 命令代码
     */
    public String getCode() {
        return code;
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
     * 根据命令代码获取消息类型
     *
     * @param code 命令代码
     * @return 消息类型，如果未找到则返回 UNKNOWN
     */
    public static AlarmMessageType fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return UNKNOWN;
        }
        
        String upperCode = code.toUpperCase().trim();
        for (AlarmMessageType type : values()) {
            if (type.code.equals(upperCode)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    /**
     * 判断是否为控制命令
     *
     * @return 是否为控制命令
     */
    public boolean isControlCommand() {
        return this == ARM_ALL 
                || this == ARM_STAY 
                || this == DISARM 
                || this == ARM_PARTITION 
                || this == DISARM_PARTITION 
                || this == BYPASS_ZONE 
                || this == UNBYPASS_ZONE;
    }

    /**
     * 判断是否为事件消息
     *
     * @return 是否为事件消息
     */
    public boolean isEventMessage() {
        return this == ALARM || this == STATUS;
    }

    /**
     * 判断是否为响应消息
     *
     * @return 是否为响应消息
     */
    public boolean isResponseMessage() {
        return this == ACK || this == NAK;
    }

    /**
     * 判断是否需要密码
     *
     * @return 是否需要密码
     */
    public boolean requiresPassword() {
        return this == DISARM 
                || this == DISARM_PARTITION 
                || this == BYPASS_ZONE 
                || this == UNBYPASS_ZONE;
    }
}
