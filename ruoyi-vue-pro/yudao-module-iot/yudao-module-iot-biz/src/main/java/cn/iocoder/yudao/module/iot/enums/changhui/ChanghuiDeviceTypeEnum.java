package cn.iocoder.yudao.module.iot.enums.changhui;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 长辉设备类型枚举
 * 
 * <p>统一管理长辉、德通等使用相同TCP协议的设备类型
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
@Getter
@AllArgsConstructor
public enum ChanghuiDeviceTypeEnum {

    /**
     * 测控一体化闸门
     */
    INTEGRATED_GATE(1, "测控一体化闸门"),
    
    /**
     * 测控分体式闸门
     */
    SPLIT_GATE(2, "测控分体式闸门"),
    
    /**
     * 退水闸
     */
    DRAIN_GATE(3, "退水闸"),
    
    /**
     * 节制闸
     */
    CONTROL_GATE(4, "节制闸"),
    
    /**
     * 进水闸
     */
    INLET_GATE(5, "进水闸"),
    
    /**
     * 水位计
     */
    WATER_LEVEL_METER(6, "水位计"),
    
    /**
     * 流量计
     */
    FLOW_METER(7, "流量计"),
    
    /**
     * 流速仪
     */
    CURRENT_METER(8, "流速仪"),
    
    /**
     * 渗压计
     */
    PIEZOMETER(9, "渗压计"),
    
    /**
     * 荷载计
     */
    LOAD_METER(10, "荷载计"),
    
    /**
     * 温度计
     */
    THERMOMETER(11, "温度计"),
    
    /**
     * 启闭机
     */
    HOIST(12, "启闭机");

    /**
     * 设备类型编码
     */
    private final Integer code;

    /**
     * 设备类型描述
     */
    private final String description;

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举，如果未找到返回null
     */
    public static ChanghuiDeviceTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ChanghuiDeviceTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据描述获取枚举
     *
     * @param description 描述
     * @return 枚举，如果未找到返回null
     */
    public static ChanghuiDeviceTypeEnum getByDescription(String description) {
        if (description == null) {
            return null;
        }
        for (ChanghuiDeviceTypeEnum type : values()) {
            if (type.getDescription().equals(description)) {
                return type;
            }
        }
        return null;
    }

}
