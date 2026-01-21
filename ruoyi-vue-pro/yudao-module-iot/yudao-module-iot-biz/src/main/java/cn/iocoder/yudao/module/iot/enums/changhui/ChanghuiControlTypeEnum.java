package cn.iocoder.yudao.module.iot.enums.changhui;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 长辉设备控制类型枚举
 * 
 * <p>定义设备远程控制的操作类型
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
@Getter
@AllArgsConstructor
public enum ChanghuiControlTypeEnum {

    /**
     * 模式切换
     */
    MODE_SWITCH("MODE_SWITCH", "模式切换"),

    /**
     * 手动控制
     */
    MANUAL_CONTROL("MANUAL_CONTROL", "手动控制"),

    /**
     * 自动控制
     */
    AUTO_CONTROL("AUTO_CONTROL", "自动控制");

    /**
     * 控制类型编码
     */
    private final String code;

    /**
     * 控制类型描述
     */
    private final String description;

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举，如果未找到返回null
     */
    public static ChanghuiControlTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ChanghuiControlTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

}
