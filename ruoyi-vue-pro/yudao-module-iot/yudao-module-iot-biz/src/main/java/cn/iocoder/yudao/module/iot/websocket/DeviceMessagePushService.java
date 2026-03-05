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

    /**
     * 推送门状态变化事件
     * <p>当门禁操作（开门/关门/常开/常闭）成功后调用，通知前端更新门状态</p>
     * 
     * @param deviceId      设备ID
     * @param deviceType    设备类型（ACCESS_GEN1/ACCESS_GEN2）
     * @param channelId     通道ID
     * @param channelNo     通道号
     * @param doorStatus    门状态（0-关闭, 1-打开, 2-未知）
     * @param lockStatus    锁状态（0-已锁, 1-已解锁, 2-未知）
     * @param alwaysMode    控制模式（0-正常, 1-常开, 2-常闭）
     * @param action        操作类型（OPEN_DOOR/CLOSE_DOOR/ALWAYS_OPEN/ALWAYS_CLOSE/CANCEL_ALWAYS）
     */
    public void pushDoorStateChange(Long deviceId, String deviceType, Long channelId, Integer channelNo,
                                     Integer doorStatus, Integer lockStatus, Integer alwaysMode, String action) {
        try {
            java.util.Map<String, Object> eventData = new java.util.HashMap<>();
            eventData.put("channelId", channelId);
            eventData.put("channelNo", channelNo);
            eventData.put("action", action);
            eventData.put("timestamp", System.currentTimeMillis());
            
            // 只有非 null 的字段才放入 eventData（null 表示"不更新"）
            if (doorStatus != null) {
                eventData.put("doorStatus", doorStatus);
                eventData.put("doorStatusDesc", getDoorStatusDesc(doorStatus));
            }
            if (lockStatus != null) {
                eventData.put("lockStatus", lockStatus);
                eventData.put("lockStatusDesc", getLockStatusDesc(lockStatus));
            }
            if (alwaysMode != null) {
                eventData.put("alwaysMode", alwaysMode);
                eventData.put("alwaysModeDesc", getAlwaysModeDesc(alwaysMode));
            }

            // 使用 DOOR_STATE_CHANGE 事件类型
            pushDeviceEvent(deviceId, deviceType, "DOOR_STATE_CHANGE", eventData);
            
            log.info("[DeviceMessagePushService] 📡 推送门状态变化: deviceId={}, channelNo={}, action={}, " +
                    "doorStatus={}, lockStatus={}, alwaysMode={}", 
                    deviceId, channelNo, action, doorStatus, lockStatus, alwaysMode);
        } catch (Exception e) {
            log.error("[DeviceMessagePushService] ❌ 推送门状态变化失败: deviceId={}, channelNo={}, error={}",
                    deviceId, channelNo, e.getMessage(), e);
        }
    }

    private String getDoorStatusDesc(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "关闭";
            case 1 -> "打开";
            default -> "未知";
        };
    }

    private String getLockStatusDesc(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "已锁";
            case 1 -> "已解锁";
            default -> "未知";
        };
    }

    private String getAlwaysModeDesc(Integer mode) {
        if (mode == null) return "正常";
        return switch (mode) {
            case 0 -> "正常";
            case 1 -> "常开";
            case 2 -> "常闭";
            default -> "正常";
        };
    }
}
