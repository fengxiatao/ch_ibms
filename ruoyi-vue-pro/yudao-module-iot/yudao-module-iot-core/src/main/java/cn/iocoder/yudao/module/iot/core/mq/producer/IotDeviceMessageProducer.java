package cn.iocoder.yudao.module.iot.core.mq.producer;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import lombok.RequiredArgsConstructor;

/**
 * IoT 设备消息生产者
 * 
 * <p>已重构为使用统一的 4 条核心消息通道：</p>
 * <ul>
 *   <li>{@link IotMessageTopics#DEVICE_SERVICE_INVOKE} - 命令下行</li>
 *   <li>{@link IotMessageTopics#DEVICE_SERVICE_RESULT} - 命令回执</li>
 *   <li>{@link IotMessageTopics#DEVICE_STATE_CHANGED} - 状态变化</li>
 *   <li>{@link IotMessageTopics#DEVICE_EVENT_REPORTED} - 事件上报</li>
 * </ul>
 *
 * @author 长辉信息科技有限公司
 */
@RequiredArgsConstructor
public class IotDeviceMessageProducer {

    private final IotMessageBus messageBus;

    /**
     * 发送设备事件消息（上行：设备 → 平台）
     * 
     * <p>用于设备事件上报，会发送到 {@link IotMessageTopics#DEVICE_EVENT_REPORTED}</p>
     *
     * @param message 设备消息
     */
    public void sendDeviceMessage(IotDeviceMessage message) {
        // 使用新的统一事件上报通道
        messageBus.post(IotMessageTopics.DEVICE_EVENT_REPORTED, message);
    }

    /**
     * 发送命令到网关（下行：平台 → 网关）
     * 
     * <p>用于命令下发，会发送到 {@link IotMessageTopics#DEVICE_SERVICE_INVOKE}</p>
     * <p>注意：serverId 参数已废弃，新架构通过 deviceType 路由到对应的网关处理器</p>
     *
     * @param serverId 网关的 serverId 标识（已废弃，仅保留兼容性）
     * @param message 设备消息
     */
    public void sendDeviceMessageToGateway(String serverId, IotDeviceMessage message) {
        // 使用新的统一命令下发通道
        messageBus.post(IotMessageTopics.DEVICE_SERVICE_INVOKE, message);
    }

}
