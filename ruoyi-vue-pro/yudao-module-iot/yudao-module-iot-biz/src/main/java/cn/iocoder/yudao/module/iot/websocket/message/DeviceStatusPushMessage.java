package cn.iocoder.yudao.module.iot.websocket.message;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceStateChangeMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备状态推送消息 DTO
 * 
 * <p>用于 WebSocket 向前端推送设备状态变更信息。</p>
 * 
 * <p>包含字段：</p>
 * <ul>
 *   <li>deviceId - 设备ID</li>
 *   <li>deviceName - 设备名称</li>
 *   <li>newState - 新状态</li>
 *   <li>previousState - 变更前状态</li>
 *   <li>timestamp - 变更时间戳</li>
 * </ul>
 * 
 * <p>Requirements: 2.3</p>
 *
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatusPushMessage {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 新状态码
     * @see IotDeviceStateEnum
     */
    private Integer newState;

    /**
     * 新状态名称（如：在线、离线、已激活、未激活）
     */
    private String newStateName;

    /**
     * 变更前状态码
     * @see IotDeviceStateEnum
     */
    private Integer previousState;

    /**
     * 变更前状态名称
     */
    private String previousStateName;

    /**
     * 变更时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 设备类型
     * <p>如：ACCESS（门禁）、CAMERA（摄像头）、NVR、CHANGHUI（长辉）、ALARM（报警主机）</p>
     */
    private String deviceType;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 从 DeviceStateChangeMessage 转换
     *
     * @param message 设备状态变更消息
     * @return DeviceStatusPushMessage
     */
    public static DeviceStatusPushMessage from(DeviceStateChangeMessage message) {
        if (message == null) {
            return null;
        }
        return DeviceStatusPushMessage.builder()
                .deviceId(message.getDeviceId())
                .deviceName(message.getDeviceName())
                .newState(message.getNewState())
                .newStateName(message.getNewStateName())
                .previousState(message.getPreviousState())
                .previousStateName(message.getPreviousStateName())
                .timestamp(message.getTimestamp())
                .deviceType(message.getDeviceType())
                .productId(message.getProductId())
                .build();
    }

    /**
     * 获取新状态枚举
     *
     * @return IotDeviceStateEnum，如果状态无效则返回 null
     */
    public IotDeviceStateEnum getNewStateEnum() {
        return IotDeviceStateEnum.fromState(newState);
    }

    /**
     * 获取变更前状态枚举
     *
     * @return IotDeviceStateEnum，如果状态无效则返回 null
     */
    public IotDeviceStateEnum getPreviousStateEnum() {
        return IotDeviceStateEnum.fromState(previousState);
    }

    /**
     * 判断是否为上线状态变更
     */
    public boolean isOnlineChange() {
        return newState != null && newState.equals(IotDeviceStateEnum.ONLINE.getState());
    }

    /**
     * 判断是否为离线状态变更
     */
    public boolean isOfflineChange() {
        return newState != null && newState.equals(IotDeviceStateEnum.OFFLINE.getState());
    }
}
