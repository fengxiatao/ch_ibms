package cn.iocoder.yudao.module.iot.websocket.message.unified;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一设备事件消息 DTO
 * 
 * <p>用于 WebSocket 推送设备事件到前端。</p>
 * <p>所有设备类型使用统一的消息格式，通过 deviceType 字段区分。</p>
 * 
 * <p>Requirements: 8.2</p>
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedDeviceEventMessage {

    /**
     * 消息类型
     * <p>固定值: "DEVICE_EVENT"</p>
     */
    private String messageType;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备类型
     * <p>可选值: ACCESS_GEN1, ACCESS_GEN2, ALARM, NVR, CHANGHUI</p>
     */
    private String deviceType;

    /**
     * 事件类型
     * <p>例如: CARD_SWIPE, ALARM, MOTION_DETECT, DOOR_OPEN 等</p>
     */
    private String eventType;

    /**
     * 事件数据
     * <p>根据事件类型不同，数据结构也不同</p>
     */
    private Object eventData;

    /**
     * 时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 消息类型常量
     */
    public static final String MESSAGE_TYPE = "DEVICE_EVENT";

    /**
     * 创建设备事件消息的便捷方法
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param eventType 事件类型
     * @param eventData 事件数据
     * @return 设备事件消息
     */
    public static UnifiedDeviceEventMessage of(Long deviceId, String deviceType, 
                                                String eventType, Object eventData) {
        return UnifiedDeviceEventMessage.builder()
                .messageType(MESSAGE_TYPE)
                .deviceId(deviceId)
                .deviceType(deviceType)
                .eventType(eventType)
                .eventData(eventData)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建设备事件消息的便捷方法（带时间戳）
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param eventType 事件类型
     * @param eventData 事件数据
     * @param timestamp 时间戳
     * @return 设备事件消息
     */
    public static UnifiedDeviceEventMessage of(Long deviceId, String deviceType, 
                                                String eventType, Object eventData, Long timestamp) {
        return UnifiedDeviceEventMessage.builder()
                .messageType(MESSAGE_TYPE)
                .deviceId(deviceId)
                .deviceType(deviceType)
                .eventType(eventType)
                .eventData(eventData)
                .timestamp(timestamp != null ? timestamp : System.currentTimeMillis())
                .build();
    }
}
