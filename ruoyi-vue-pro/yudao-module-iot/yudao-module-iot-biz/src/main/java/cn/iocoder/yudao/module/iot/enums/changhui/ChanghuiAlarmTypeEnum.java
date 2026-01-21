package cn.iocoder.yudao.module.iot.enums.changhui;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 长辉设备报警类型枚举
 * 
 * <p>定义设备可能产生的各类报警类型
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
@Getter
@AllArgsConstructor
public enum ChanghuiAlarmTypeEnum {

    /**
     * 过力矩报警
     */
    OVER_TORQUE("OVER_TORQUE", "过力矩"),

    /**
     * 过电流报警
     */
    OVER_CURRENT("OVER_CURRENT", "过电流"),

    /**
     * 过电压报警
     */
    OVER_VOLTAGE("OVER_VOLTAGE", "过电压"),

    /**
     * 低电压报警
     */
    LOW_VOLTAGE("LOW_VOLTAGE", "低电压"),

    /**
     * 水位超限报警
     */
    WATER_LEVEL("WATER_LEVEL", "水位超限"),

    /**
     * 闸位超限报警
     */
    GATE_POSITION("GATE_POSITION", "闸位超限"),

    /**
     * 通信故障报警
     */
    COMMUNICATION_FAULT("COMMUNICATION_FAULT", "通信故障"),

    /**
     * 设备故障报警
     */
    DEVICE_FAULT("DEVICE_FAULT", "设备故障");

    /**
     * 报警类型编码
     */
    private final String code;

    /**
     * 报警类型描述
     */
    private final String description;

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举，如果未找到返回null
     */
    public static ChanghuiAlarmTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ChanghuiAlarmTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

}
