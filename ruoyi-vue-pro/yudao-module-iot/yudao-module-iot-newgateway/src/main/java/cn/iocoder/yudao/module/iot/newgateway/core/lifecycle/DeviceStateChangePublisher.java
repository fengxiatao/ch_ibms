package cn.iocoder.yudao.module.iot.newgateway.core.lifecycle;

import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceStateChangeMessage;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备状态变更消息发布器
 * 
 * <p>负责将设备状态变更消息发布到消息总线（RocketMQ），
 * 供 Biz 服务消费并更新数据库状态。</p>
 * 
 * <p>消息主题: {@link IotMessageTopics#DEVICE_STATE_CHANGED}</p>
 * 
 * <p>复用 iot-core 中的公共类：</p>
 * <ul>
 *   <li>DeviceStateChangeMessage - 状态变更消息</li>
 *   <li>IotMessageTopics - 消息主题常量</li>
 *   <li>IotMessageBus - 消息总线接口</li>
 * </ul>
 * 
 * <p>Requirements: 1.2, 1.3, 2.2</p>
 *
 * @author 长辉信息科技有限公司
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceStateChangePublisher {

    private final IotMessageBus messageBus;

    /**
     * 发布设备上线消息
     *
     * @param deviceId       设备ID
     * @param deviceName     设备名称
     * @param deviceType     设备类型
     * @param productId      产品ID
     * @param previousState  变更前状态
     * @param tenantId       租户ID
     * @param connectionMode 连接模式
     */
    public void publishOnline(Long deviceId, String deviceName, String deviceType,
                              Long productId, IotDeviceStateEnum previousState,
                              Long tenantId, ConnectionMode connectionMode) {
        DeviceStateChangeMessage message = DeviceStateChangeMessage.online(
                deviceId, deviceName, deviceType, productId,
                previousState != null ? previousState.getState() : null,
                tenantId, connectionMode);
        
        publish(message);
    }

    /**
     * 发布设备离线消息
     *
     * @param deviceId       设备ID
     * @param deviceName     设备名称
     * @param deviceType     设备类型
     * @param productId      产品ID
     * @param previousState  变更前状态
     * @param tenantId       租户ID
     * @param connectionMode 连接模式
     * @param reason         离线原因
     */
    public void publishOffline(Long deviceId, String deviceName, String deviceType,
                               Long productId, IotDeviceStateEnum previousState,
                               Long tenantId, ConnectionMode connectionMode, String reason) {
        DeviceStateChangeMessage message = DeviceStateChangeMessage.offline(
                deviceId, deviceName, deviceType, productId,
                previousState != null ? previousState.getState() : null,
                tenantId, connectionMode, reason);
        
        publish(message);
    }


    /**
     * 发布通用状态变更消息
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
     */
    public void publishStateChange(Long deviceId, String deviceName, String deviceType,
                                    Long productId, IotDeviceStateEnum previousState,
                                    IotDeviceStateEnum newState, Long tenantId,
                                    ConnectionMode connectionMode, String reason) {
        DeviceStateChangeMessage message = DeviceStateChangeMessage.of(
                deviceId, deviceName, deviceType, productId,
                previousState != null ? previousState.getState() : null,
                newState != null ? newState.getState() : null,
                tenantId, connectionMode, reason);
        
        publish(message);
    }

    /**
     * 发布状态变更消息（直接使用 DeviceStateChangeMessage）
     *
     * @param message 状态变更消息
     */
    public void publish(DeviceStateChangeMessage message) {
        if (message == null) {
            log.warn("[DeviceStateChangePublisher] 消息不能为空");
            return;
        }
        
        if (message.getDeviceId() == null) {
            log.warn("[DeviceStateChangePublisher] deviceId 不能为空");
            return;
        }

        try {
            messageBus.post(IotMessageTopics.DEVICE_STATE_CHANGED, message);
            
            log.info("[DeviceStateChangePublisher] 设备状态变更消息发送成功: deviceId={}, previousState={}, newState={}, reason={}",
                    message.getDeviceId(),
                    message.getPreviousStateName(),
                    message.getNewStateName(),
                    message.getReason());
        } catch (Exception e) {
            log.error("[DeviceStateChangePublisher] 设备状态变更消息发送失败: deviceId={}, error={}",
                    message.getDeviceId(), e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }
}
