package cn.iocoder.yudao.module.iot.websocket;

import cn.iocoder.yudao.module.iot.websocket.message.IotMessage;
import cn.iocoder.yudao.module.iot.websocket.message.unified.UnifiedCommandResultMessage;
import cn.iocoder.yudao.module.iot.websocket.message.unified.UnifiedDeviceEventMessage;
import cn.iocoder.yudao.module.iot.websocket.message.unified.UnifiedDeviceStatusMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备消息推送服务
 * 
 * <p>统一的 WebSocket 消息推送入口，用于向前端推送设备相关消息。</p>
 * 
 * <p>功能：</p>
 * <ul>
 *   <li>推送设备状态变更</li>
 *   <li>推送设备事件</li>
 *   <li>推送命令执行结果</li>
 * </ul>
 * 
 * <p>Requirements: 7.4, 7.5, 8.1, 8.2, 8.3</p>
 *
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
public class DeviceMessagePushService {

    @Resource(name = "deviceStatusWebSocketHandler")
    private DeviceStatusWebSocketHandler webSocketHandler;

    /**
     * 推送设备状态
     * 
     * @param deviceId   设备ID
     * @param deviceType 设备类型
     * @param status     设备状态
     * @param timestamp  时间戳（毫秒），如果为null则使用当前时间
     */
    public void pushDeviceStatus(Long deviceId, String deviceType, String status, Long timestamp) {
        UnifiedDeviceStatusMessage message = UnifiedDeviceStatusMessage.of(
                deviceId, deviceType, status, timestamp);
        
        IotMessage iotMessage = new IotMessage(
                UnifiedDeviceStatusMessage.MESSAGE_TYPE, 
                message, 
                System.currentTimeMillis());
        
        webSocketHandler.broadcast(iotMessage);
        log.debug("[DeviceMessagePushService] 状态推送: deviceId={}, deviceType={}, status={}", 
                deviceId, deviceType, status);
    }

    /**
     * 推送设备事件
     * 
     * @param deviceId   设备ID
     * @param deviceType 设备类型
     * @param eventType  事件类型
     * @param eventData  事件数据
     */
    public void pushDeviceEvent(Long deviceId, String deviceType, String eventType, Object eventData) {
        UnifiedDeviceEventMessage message = UnifiedDeviceEventMessage.of(
                deviceId, deviceType, eventType, eventData);
        
        IotMessage iotMessage = new IotMessage(
                UnifiedDeviceEventMessage.MESSAGE_TYPE, 
                message, 
                System.currentTimeMillis());
        
        webSocketHandler.broadcast(iotMessage);
        log.debug("[DeviceMessagePushService] 事件推送: deviceId={}, deviceType={}, eventType={}", 
                deviceId, deviceType, eventType);
    }

    /**
     * 推送命令执行结果
     * 
     * @param deviceId   设备ID
     * @param deviceType 设备类型
     * @param requestId  请求ID
     * @param success    是否成功
     * @param message    结果消息
     * @param data       结果数据
     */
    public void pushCommandResult(Long deviceId, String deviceType, String requestId, 
                                   boolean success, String message, Object data) {
        UnifiedCommandResultMessage resultMessage = UnifiedCommandResultMessage.of(
                requestId, deviceId, deviceType, success, message, data);
        
        IotMessage iotMessage = new IotMessage(
                UnifiedCommandResultMessage.MESSAGE_TYPE, 
                resultMessage, 
                System.currentTimeMillis());
        
        webSocketHandler.broadcast(iotMessage);
        log.debug("[DeviceMessagePushService] 结果推送: deviceId={}, deviceType={}, requestId={}, success={}", 
                deviceId, deviceType, requestId, success);
    }

    /**
     * 推送命令执行结果（使用预构建的消息）
     * 
     * @param resultMessage 命令结果消息
     */
    public void pushCommandResult(UnifiedCommandResultMessage resultMessage) {
        IotMessage iotMessage = new IotMessage(
                UnifiedCommandResultMessage.MESSAGE_TYPE, 
                resultMessage, 
                System.currentTimeMillis());
        
        webSocketHandler.broadcast(iotMessage);
        log.debug("[DeviceMessagePushService] 结果推送: deviceId={}, deviceType={}, requestId={}, success={}", 
                resultMessage.getDeviceId(), resultMessage.getDeviceType(), 
                resultMessage.getRequestId(), resultMessage.getSuccess());
    }

    /**
     * 推送录像事件
     * <p>从 IotMessagePushService 迁移，保持相同的消息格式以兼容前端</p>
     * 
     * @param event 录像事件数据（Map 格式，包含 type, recordingId, deviceId 等字段）
     */
    public void pushRecordingEvent(Object event) {
        try {
            IotMessage iotMessage = IotMessage.recordingEvent(event);
            webSocketHandler.broadcast(iotMessage);
            log.debug("[DeviceMessagePushService] 录像事件推送: event={}", event);
        } catch (Exception e) {
            log.error("[DeviceMessagePushService] ❌ 推送录像事件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送服务失败事件
     * <p>从 IotMessagePushService 迁移</p>
     * 
     * @param failure 服务失败消息
     */
    public void pushServiceFailure(cn.iocoder.yudao.module.iot.websocket.message.ServiceFailureMessage failure) {
        try {
            IotMessage iotMessage = IotMessage.serviceFailure(failure);
            webSocketHandler.broadcast(iotMessage);
            log.info("[DeviceMessagePushService] 📢 推送服务失败: deviceId={}, service={}, reason={}",
                    failure.getDeviceId(), failure.getServiceName(), failure.getFailureReason());
        } catch (Exception e) {
            log.error("[DeviceMessagePushService] ❌ 推送服务失败事件异常: deviceId={}, service={}, error={}",
                    failure.getDeviceId(), failure.getServiceName(), e.getMessage(), e);
        }
    }
}
