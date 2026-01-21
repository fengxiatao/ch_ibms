package cn.iocoder.yudao.module.iot.core.gateway.dto;

import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;


/**
 * 设备状态变更消息 DTO
 * 
 * <p>用于 Gateway → Biz 的消息总线传输，当设备状态发生变化时，
 * Gateway 通过 RocketMQ 发送此消息到 Biz 服务。</p>
 * 
 * <p>Biz 服务收到消息后：</p>
 * <ul>
 *   <li>更新数据库中的设备状态（state 字段）</li>
 *   <li>根据状态类型更新 onlineTime/offlineTime/activeTime</li>
 *   <li>通过 WebSocket 推送状态变更给前端</li>
 * </ul>
 * 
 * <p>消息主题: {@link cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics#DEVICE_STATE_CHANGED}</p>
 * 
 * <p>Requirements: 1.4, 6.1</p>
 *
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStateChangeMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 设备标识信息 ====================

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型
     * <p>如：ACCESS（门禁）、CAMERA（摄像头）、NVR、CHANGHUI（长辉）、ALARM（报警主机）</p>
     */
    private String deviceType;

    /**
     * 产品ID
     */
    private Long productId;

    // ==================== 状态变更信息 ====================

    /**
     * 变更前状态
     * @see IotDeviceStateEnum
     */
    private Integer previousState;

    /**
     * 变更后状态（新状态）
     * @see IotDeviceStateEnum
     */
    private Integer newState;

    /**
     * 变更时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 状态变更原因
     * <p>如：心跳超时、保活检测失败、主动断开、SDK登录成功等</p>
     */
    private String reason;

    // ==================== 租户和连接信息 ====================

    /**
     * 请求ID（链路追踪/幂等关联）
     */
    private String requestId;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 连接模式
     * @see ConnectionMode
     */
    private String connectionMode;


    // ==================== 工厂方法 ====================

    /**
     * 创建设备上线消息
     *
     * @param deviceId       设备ID
     * @param deviceName     设备名称
     * @param deviceType     设备类型
     * @param productId      产品ID
     * @param previousState  变更前状态
     * @param tenantId       租户ID
     * @param connectionMode 连接模式
     * @return DeviceStateChangeMessage
     */
    public static DeviceStateChangeMessage online(Long deviceId, String deviceName, String deviceType,
                                                   Long productId, Integer previousState, Long tenantId,
                                                   ConnectionMode connectionMode) {
        return DeviceStateChangeMessage.builder()
                .requestId(UUID.randomUUID().toString())
                .deviceId(deviceId)

                .deviceName(deviceName)
                .deviceType(deviceType)
                .productId(productId)
                .previousState(previousState)
                .newState(IotDeviceStateEnum.ONLINE.getState())
                .timestamp(System.currentTimeMillis())
                .reason("设备上线")
                .tenantId(tenantId)
                .connectionMode(connectionMode != null ? connectionMode.name() : null)
                .build();
    }

    /**
     * 创建设备离线消息
     *
     * @param deviceId       设备ID
     * @param deviceName     设备名称
     * @param deviceType     设备类型
     * @param productId      产品ID
     * @param previousState  变更前状态
     * @param tenantId       租户ID
     * @param connectionMode 连接模式
     * @param reason         离线原因
     * @return DeviceStateChangeMessage
     */
    public static DeviceStateChangeMessage offline(Long deviceId, String deviceName, String deviceType,
                                                    Long productId, Integer previousState, Long tenantId,
                                                    ConnectionMode connectionMode, String reason) {
        return DeviceStateChangeMessage.builder()
                .requestId(UUID.randomUUID().toString())
                .deviceId(deviceId)

                .deviceName(deviceName)
                .deviceType(deviceType)
                .productId(productId)
                .previousState(previousState)
                .newState(IotDeviceStateEnum.OFFLINE.getState())
                .timestamp(System.currentTimeMillis())
                .reason(reason != null ? reason : "设备离线")
                .tenantId(tenantId)
                .connectionMode(connectionMode != null ? connectionMode.name() : null)
                .build();
    }

    /**
     * 创建通用状态变更消息
     *
     * @param deviceId       设备ID
     * @param deviceName     设备名称
     * @param deviceType     设备类型
     * @param productId      产品ID
     * @param previousState  变更前状态
     * @param newState       变更后状态
     * @param tenantId       租户ID
     * @param connectionMode 连接模式
     * @param reason         变更原因
     * @return DeviceStateChangeMessage
     */
    public static DeviceStateChangeMessage of(Long deviceId, String deviceName, String deviceType,
                                               Long productId, Integer previousState, Integer newState,
                                               Long tenantId, ConnectionMode connectionMode, String reason) {
        return DeviceStateChangeMessage.builder()
                .requestId(UUID.randomUUID().toString())
                .deviceId(deviceId)

                .deviceName(deviceName)
                .deviceType(deviceType)
                .productId(productId)
                .previousState(previousState)
                .newState(newState)
                .timestamp(System.currentTimeMillis())
                .reason(reason)
                .tenantId(tenantId)
                .connectionMode(connectionMode != null ? connectionMode.name() : null)
                .build();
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断是否为上线状态变更
     */
    @JsonIgnore
    public boolean isOnlineChange() {
        return newState != null && newState.equals(IotDeviceStateEnum.ONLINE.getState());
    }

    /**
     * 判断是否为离线状态变更
     */
    @JsonIgnore
    public boolean isOfflineChange() {
        return newState != null && newState.equals(IotDeviceStateEnum.OFFLINE.getState());
    }

    /**
     * 获取新状态枚举
     *
     * @return IotDeviceStateEnum，如果状态无效则返回 null
     */
    @JsonIgnore
    public IotDeviceStateEnum getNewStateEnum() {
        return IotDeviceStateEnum.fromState(newState);
    }

    /**
     * 获取变更前状态枚举
     *
     * @return IotDeviceStateEnum，如果状态无效则返回 null
     */
    @JsonIgnore
    public IotDeviceStateEnum getPreviousStateEnum() {
        return IotDeviceStateEnum.fromState(previousState);
    }

    /**
     * 获取连接模式枚举
     *
     * @return ConnectionMode，如果连接模式无效则返回 null
     */
    @JsonIgnore
    public ConnectionMode getConnectionModeEnum() {
        if (connectionMode == null) {
            return null;
        }
        try {
            return ConnectionMode.valueOf(connectionMode);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 获取新状态名称
     *
     * @return 状态名称，如果状态无效则返回 "未知"
     */
    @JsonIgnore
    public String getNewStateName() {
        IotDeviceStateEnum stateEnum = getNewStateEnum();
        return stateEnum != null ? stateEnum.getName() : "未知";
    }

    /**
     * 获取变更前状态名称
     *
     * @return 状态名称，如果状态无效则返回 "未知"
     */
    @JsonIgnore
    public String getPreviousStateName() {
        IotDeviceStateEnum stateEnum = getPreviousStateEnum();
        return stateEnum != null ? stateEnum.getName() : "未知";
    }
}
