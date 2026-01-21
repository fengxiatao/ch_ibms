package cn.iocoder.yudao.module.iot.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 设备连接模式枚举
 * 
 * 定义了 IoT 设备与平台之间的两种连接模式：
 * - ACTIVE: 主动连接模式，平台主动连接设备（如 SDK 登录）
 * - PASSIVE: 被动连接模式，设备主动连接平台（如 TCP 心跳）
 *
 * @author 长辉信息科技有限公司
 */
@RequiredArgsConstructor
@Getter
public enum ConnectionMode {

    /**
     * 主动连接模式 - 平台主动连接设备（SDK 登录）
     * 适用于：摄像头、NVR、门禁控制器
     */
    ACTIVE(1, "主动连接"),

    /**
     * 被动连接模式 - 设备主动连接平台（TCP 心跳）
     * 适用于：报警主机、德通设备
     */
    PASSIVE(2, "被动连接");

    /**
     * 模式代码
     */
    private final Integer code;

    /**
     * 模式名称
     */
    private final String name;

    /**
     * 根据代码获取连接模式
     *
     * @param code 模式代码
     * @return 连接模式，如果未找到则返回 null
     */
    public static ConnectionMode fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ConnectionMode mode : values()) {
            if (mode.getCode().equals(code)) {
                return mode;
            }
        }
        return null;
    }

    /**
     * 判断是否为主动连接模式
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 判断是否为被动连接模式
     */
    public boolean isPassive() {
        return this == PASSIVE;
    }
}
