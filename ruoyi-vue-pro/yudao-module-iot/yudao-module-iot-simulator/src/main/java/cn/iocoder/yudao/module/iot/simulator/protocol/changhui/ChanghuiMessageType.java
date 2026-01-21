package cn.iocoder.yudao.module.iot.simulator.protocol.changhui;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 长辉协议消息类型
 * 
 * <p>定义协议中的所有AFN（应用功能码）</p>
 *
 * @author IoT Simulator Team
 */
@Getter
@AllArgsConstructor
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
     */
    DATA_WATER_LEVEL((byte) 0x06, "水位数据"),

    /**
     * 瞬时流量数据 (AFN=0x07)
     */
    DATA_INSTANT_FLOW((byte) 0x07, "瞬时流量"),

    /**
     * 瞬时流速数据 (AFN=0x08)
     */
    DATA_INSTANT_VELOCITY((byte) 0x08, "瞬时流速"),

    /**
     * 未知消息类型
     */
    UNKNOWN((byte) 0xFF, "未知");

    /**
     * AFN（应用功能码）
     */
    private final byte afn;

    /**
     * 描述
     */
    private final String description;

    /**
     * 根据AFN获取消息类型
     *
     * @param afn AFN值
     * @return 消息类型
     */
    public static ChanghuiMessageType fromAfn(byte afn) {
        for (ChanghuiMessageType type : values()) {
            if (type.getAfn() == afn) {
                return type;
            }
        }
        return UNKNOWN;
    }

    /**
     * 是否为升级状态消息
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
     * 是否为升级命令消息
     *
     * @return 是否为升级命令消息
     */
    public boolean isUpgradeCommand() {
        return this == UPGRADE_TRIGGER || this == UPGRADE_URL;
    }
}




















