package cn.iocoder.yudao.module.iot.websocket.message.unified;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一命令结果消息 DTO
 * 
 * <p>用于 WebSocket 推送命令执行结果到前端。</p>
 * <p>所有设备类型使用统一的消息格式，通过 deviceType 字段区分。</p>
 * 
 * <p>Requirements: 8.3</p>
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedCommandResultMessage {

    /**
     * 消息类型
     * <p>固定值: "COMMAND_RESULT"</p>
     */
    private String messageType;

    /**
     * 请求ID
     * <p>用于关联命令请求和响应</p>
     */
    private String requestId;

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
     * 命令是否执行成功
     */
    private Boolean success;

    /**
     * 结果消息
     * <p>成功或失败的描述信息</p>
     */
    private String message;

    /**
     * 结果数据
     * <p>命令执行返回的数据，根据命令类型不同，数据结构也不同</p>
     */
    private Object data;

    /**
     * 时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 消息类型常量
     */
    public static final String MESSAGE_TYPE = "COMMAND_RESULT";

    /**
     * 创建成功的命令结果消息
     * 
     * @param requestId 请求ID
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param message 成功消息
     * @param data 结果数据
     * @return 命令结果消息
     */
    public static UnifiedCommandResultMessage success(String requestId, Long deviceId, 
                                                       String deviceType, String message, Object data) {
        return UnifiedCommandResultMessage.builder()
                .messageType(MESSAGE_TYPE)
                .requestId(requestId)
                .deviceId(deviceId)
                .deviceType(deviceType)
                .success(true)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建失败的命令结果消息
     * 
     * @param requestId 请求ID
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param message 失败消息
     * @return 命令结果消息
     */
    public static UnifiedCommandResultMessage failure(String requestId, Long deviceId, 
                                                       String deviceType, String message) {
        return UnifiedCommandResultMessage.builder()
                .messageType(MESSAGE_TYPE)
                .requestId(requestId)
                .deviceId(deviceId)
                .deviceType(deviceType)
                .success(false)
                .message(message)
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建命令结果消息的便捷方法
     * 
     * @param requestId 请求ID
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param success 是否成功
     * @param message 结果消息
     * @param data 结果数据
     * @return 命令结果消息
     */
    public static UnifiedCommandResultMessage of(String requestId, Long deviceId, String deviceType,
                                                  Boolean success, String message, Object data) {
        return UnifiedCommandResultMessage.builder()
                .messageType(MESSAGE_TYPE)
                .requestId(requestId)
                .deviceId(deviceId)
                .deviceType(deviceType)
                .success(success)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建命令结果消息的便捷方法（带时间戳）
     * 
     * @param requestId 请求ID
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param success 是否成功
     * @param message 结果消息
     * @param data 结果数据
     * @param timestamp 时间戳
     * @return 命令结果消息
     */
    public static UnifiedCommandResultMessage of(String requestId, Long deviceId, String deviceType,
                                                  Boolean success, String message, Object data, Long timestamp) {
        return UnifiedCommandResultMessage.builder()
                .messageType(MESSAGE_TYPE)
                .requestId(requestId)
                .deviceId(deviceId)
                .deviceType(deviceType)
                .success(success)
                .message(message)
                .data(data)
                .timestamp(timestamp != null ? timestamp : System.currentTimeMillis())
                .build();
    }
}
