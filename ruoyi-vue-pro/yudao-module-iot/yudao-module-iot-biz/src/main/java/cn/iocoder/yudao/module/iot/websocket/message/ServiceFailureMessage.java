package cn.iocoder.yudao.module.iot.websocket.message;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务失败消息
 *
 * 用于WebSocket实时推送设备服务调用失败事件
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceFailureMessage extends IotMessage {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 服务名称（snapshot, getPlayUrl, startRecording等）
     */
    private String serviceName;

    /**
     * 失败原因代码（SERVICE_TIMEOUT, DEVICE_OFFLINE, NETWORK_ERROR等）
     */
    private String failureReason;

    /**
     * 错误消息详情
     */
    private String errorMessage;

    /**
     * 请求ID（用于追踪）
     */
    private String requestId;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 服务调用耗时（毫秒）
     */
    private Long durationMs;

    public ServiceFailureMessage() {
        this.setType("service_failure");
        this.setTimestamp(System.currentTimeMillis());
    }

    /**
     * 构建快照服务失败消息的便捷方法
     */
    public static ServiceFailureMessage forSnapshot(Long deviceId, String deviceName, 
                                                     String errorMessage, String requestId) {
        ServiceFailureMessage message = new ServiceFailureMessage();
        message.setDeviceId(deviceId);
        message.setDeviceName(deviceName);
        message.setServiceName("snapshot");
        message.setFailureReason("SERVICE_INVOKE_FAILED");
        message.setErrorMessage(errorMessage);
        message.setRequestId(requestId);
        message.setRetryCount(0);
        return message;
    }

    /**
     * 构建播放URL服务失败消息的便捷方法
     */
    public static ServiceFailureMessage forGetPlayUrl(Long deviceId, String deviceName,
                                                       String errorMessage, String requestId) {
        ServiceFailureMessage message = new ServiceFailureMessage();
        message.setDeviceId(deviceId);
        message.setDeviceName(deviceName);
        message.setServiceName("getPlayUrl");
        message.setFailureReason("SERVICE_INVOKE_FAILED");
        message.setErrorMessage(errorMessage);
        message.setRequestId(requestId);
        message.setRetryCount(0);
        return message;
    }
}

