package cn.iocoder.yudao.module.iot.websocket.message.unified;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一设备状态消息 DTO
 * 
 * <p>用于 WebSocket 推送设备状态变更到前端。</p>
 * <p>所有设备类型使用统一的消息格式，通过 deviceType 字段区分。</p>
 * 
 * <p>Requirements: 8.1</p>
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedDeviceStatusMessage {

    /**
     * 消息类型
     * <p>固定值: "DEVICE_STATUS"</p>
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
     * 设备状态
     * <p>可选值: ONLINE, OFFLINE, INACTIVE</p>
     */
    private String status;

    /**
     * 时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 消息类型常量
     */
    public static final String MESSAGE_TYPE = "DEVICE_STATUS";

    /**
     * 创建设备状态消息的便捷方法
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param status 设备状态
     * @return 设备状态消息
     */
    public static UnifiedDeviceStatusMessage of(Long deviceId, String deviceType, String status) {
        return UnifiedDeviceStatusMessage.builder()
                .messageType(MESSAGE_TYPE)
                .deviceId(deviceId)
                .deviceType(deviceType)
                .status(status)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建设备状态消息的便捷方法（带时间戳）
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param status 设备状态
     * @param timestamp 时间戳
     * @return 设备状态消息
     */
    public static UnifiedDeviceStatusMessage of(Long deviceId, String deviceType, String status, Long timestamp) {
        return UnifiedDeviceStatusMessage.builder()
                .messageType(MESSAGE_TYPE)
                .deviceId(deviceId)
                .deviceType(deviceType)
                .status(status)
                .timestamp(timestamp != null ? timestamp : System.currentTimeMillis())
                .build();
    }
}
