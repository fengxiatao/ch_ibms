package cn.iocoder.yudao.module.iot.core.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;

/**
 * IoT 设备状态枚举
 * 
 * 定义了设备在系统中的生命周期状态：
 * - INACTIVE: 未激活，设备已添加到系统但尚未连接
 * - ONLINE: 在线，设备已连接并正常工作
 * - OFFLINE: 离线，设备连接断开
 *
 * 状态转换规则（主动/被动连接设备统一）：
 * INACTIVE → ONLINE ↔ OFFLINE
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Getter
public enum IotDeviceStateEnum implements ArrayValuable<Integer> {

    INACTIVE(0, "未激活"),
    ONLINE(1, "在线"),
    OFFLINE(2, "离线");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotDeviceStateEnum::getState).toArray(Integer[]::new);

    /**
     * 统一的有效状态转换（主动/被动连接设备通用）
     * 
     * 注意：INACTIVE->OFFLINE 用于 Gateway 重启时重置设备状态，
     * 确保之前在线的设备被标记为离线，等待设备重新连接
     */
    private static final Set<String> VALID_TRANSITIONS = Set.of(
            "INACTIVE->ONLINE",
            "INACTIVE->OFFLINE",  // Gateway 重启时重置设备状态
            "ONLINE->OFFLINE",
            "OFFLINE->ONLINE"
    );

    /**
     * 状态
     */
    private final Integer state;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param state 状态码
     * @return 设备状态枚举，如果未找到则返回 null
     */
    public static IotDeviceStateEnum fromState(Integer state) {
        if (state == null) {
            return null;
        }
        for (IotDeviceStateEnum stateEnum : values()) {
            if (stateEnum.getState().equals(state)) {
                return stateEnum;
            }
        }
        return null;
    }

    public static boolean isOnline(Integer state) {
        return ONLINE.getState().equals(state);
    }

    public static boolean isNotOnline(Integer state) {
        return !isOnline(state);
    }

    public static boolean isOffline(Integer state) {
        return OFFLINE.getState().equals(state);
    }

    public static boolean isInactive(Integer state) {
        return INACTIVE.getState().equals(state);
    }

    /**
     * 验证状态转换是否有效（主动/被动连接设备通用）
     *
     * @param from 源状态
     * @param to   目标状态
     * @return 是否为有效转换
     */
    public static boolean isValidTransition(IotDeviceStateEnum from, IotDeviceStateEnum to) {
        if (from == null || to == null) {
            return false;
        }
        String transition = from.name() + "->" + to.name();
        return VALID_TRANSITIONS.contains(transition);
    }

    /**
     * 根据连接模式验证状态转换是否有效
     *
     * @param from           源状态
     * @param to             目标状态
     * @param connectionMode 连接模式（现在不再区分，保留参数兼容性）
     * @return 是否为有效转换
     */
    public static boolean isValidTransition(IotDeviceStateEnum from, IotDeviceStateEnum to, ConnectionMode connectionMode) {
        // 不再区分连接模式，统一使用相同的状态转换规则
        return isValidTransition(from, to);
    }

}
