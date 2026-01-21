package cn.iocoder.yudao.module.iot.core.gateway.dto.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备激活结果DTO
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivationResult {
    
    /** 是否成功 */
    private boolean success;
    
    /** 设备ID */
    private Long deviceId;
    
    /** 设备IP */
    private String ipAddress;
    
    /** 设备端口 */
    private Integer port;
    
    /** 消息 */
    private String message;
    
    /** 错误信息（失败时） */
    private String errorMessage;
    
    /** 激活耗时（毫秒） */
    private Long activationTimeMs;
    
    /**
     * 创建成功结果
     */
    public static ActivationResult success(Long deviceId, String ipAddress, Integer port, long activationTimeMs) {
        return ActivationResult.builder()
                .success(true)
                .deviceId(deviceId)
                .ipAddress(ipAddress)
                .port(port)
                .message("激活成功")
                .activationTimeMs(activationTimeMs)
                .build();
    }
    
    /**
     * 创建失败结果
     */
    public static ActivationResult failure(Long deviceId, String ipAddress, Integer port, String errorMessage) {
        return ActivationResult.builder()
                .success(false)
                .deviceId(deviceId)
                .ipAddress(ipAddress)
                .port(port)
                .message("激活失败")
                .errorMessage(errorMessage)
                .build();
    }
    
    /**
     * 创建跳过结果（配置不完整）
     */
    public static ActivationResult skipped(Long deviceId, String reason) {
        return ActivationResult.builder()
                .success(false)
                .deviceId(deviceId)
                .message("跳过激活")
                .errorMessage(reason)
                .build();
    }
}
