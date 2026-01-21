package cn.iocoder.yudao.module.iot.core.gateway.dto.access;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备激活状态枚举
 * 
 * @author 长辉信息科技有限公司
 */
@Getter
@AllArgsConstructor
public enum DeviceActivationStatusEnum {
    
    /** 未激活 - 设备尚未登录 */
    NOT_ACTIVATED(0, "未激活"),
    
    /** 激活中 - 正在登录设备 */
    ACTIVATING(1, "激活中"),
    
    /** 已激活 - 设备已成功登录 */
    ACTIVATED(2, "已激活"),
    
    /** 激活失败 - 登录设备失败 */
    FAILED(3, "激活失败"),
    
    /** 重连中 - 设备断线后正在重连 */
    RECONNECTING(4, "重连中");
    
    /** 状态码 */
    private final int code;
    
    /** 状态描述 */
    private final String description;
    
    /**
     * 根据状态码获取枚举
     */
    public static DeviceActivationStatusEnum fromCode(int code) {
        for (DeviceActivationStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return NOT_ACTIVATED;
    }
    
    /**
     * 根据名称获取枚举
     */
    public static DeviceActivationStatusEnum fromName(String name) {
        if (name == null) {
            return NOT_ACTIVATED;
        }
        for (DeviceActivationStatusEnum status : values()) {
            if (status.name().equals(name)) {
                return status;
            }
        }
        return NOT_ACTIVATED;
    }
    
    /**
     * 判断是否为终态（已激活或失败）
     */
    public boolean isTerminal() {
        return this == ACTIVATED || this == FAILED;
    }
    
    /**
     * 判断是否可以进行激活操作
     */
    public boolean canActivate() {
        return this == NOT_ACTIVATED || this == FAILED;
    }
}
