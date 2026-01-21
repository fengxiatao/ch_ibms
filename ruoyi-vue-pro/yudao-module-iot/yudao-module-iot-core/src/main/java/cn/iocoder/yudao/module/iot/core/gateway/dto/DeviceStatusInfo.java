package cn.iocoder.yudao.module.iot.core.gateway.dto;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备状态信息 DTO
 * 
 * 用于 API 响应，包含设备的当前状态和最后活跃时间戳。
 * 此 DTO 用于 API 层返回，数据库中只存储 state 字段。
 *
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatusInfo {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备状态
     */
    private IotDeviceStateEnum state;

    /**
     * 最后活跃时间戳（毫秒）
     */
    private Long lastSeenTimestamp;

    /**
     * 创建设备状态信息
     *
     * @param deviceId          设备ID
     * @param state             设备状态
     * @param lastSeenTimestamp 最后活跃时间戳
     * @return DeviceStatusInfo
     */
    public static DeviceStatusInfo of(Long deviceId, IotDeviceStateEnum state, Long lastSeenTimestamp) {
        return DeviceStatusInfo.builder()
                .deviceId(deviceId)
                .state(state)
                .lastSeenTimestamp(lastSeenTimestamp)
                .build();
    }

    /**
     * 创建设备状态信息（不含设备ID）
     *
     * @param state             设备状态
     * @param lastSeenTimestamp 最后活跃时间戳
     * @return DeviceStatusInfo
     */
    public static DeviceStatusInfo of(IotDeviceStateEnum state, Long lastSeenTimestamp) {
        return DeviceStatusInfo.builder()
                .state(state)
                .lastSeenTimestamp(lastSeenTimestamp)
                .build();
    }

    /**
     * 判断设备是否在线
     */
    public boolean isOnline() {
        return state != null && state == IotDeviceStateEnum.ONLINE;
    }

    /**
     * 判断设备是否离线
     */
    public boolean isOffline() {
        return state != null && state == IotDeviceStateEnum.OFFLINE;
    }

    /**
     * 判断设备是否未激活
     */
    public boolean isInactive() {
        return state != null && state == IotDeviceStateEnum.INACTIVE;
    }

    /**
     * 获取状态码
     *
     * @return 状态码，如果状态为 null 则返回 null
     */
    public Integer getStateCode() {
        return state != null ? state.getState() : null;
    }

    /**
     * 获取状态名称
     *
     * @return 状态名称，如果状态为 null 则返回 "未知"
     */
    public String getStateName() {
        return state != null ? state.getName() : "未知";
    }
}
