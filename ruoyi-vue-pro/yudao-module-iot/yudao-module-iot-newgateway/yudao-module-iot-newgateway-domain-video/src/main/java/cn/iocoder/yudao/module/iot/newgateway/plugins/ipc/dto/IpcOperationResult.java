package cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * IPC 操作结果
 * 
 * @author IoT Gateway Team
 */
@Data
@Builder
public class IpcOperationResult {

    /** 是否成功 */
    private boolean success;
    
    /** 消息 */
    private String message;
    
    /** 错误码 */
    private int errorCode;
    
    /** 返回数据 */
    private Map<String, Object> data;

    public static IpcOperationResult success(String message) {
        return IpcOperationResult.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static IpcOperationResult success(String message, Map<String, Object> data) {
        return IpcOperationResult.builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static IpcOperationResult failure(String message) {
        return IpcOperationResult.builder()
                .success(false)
                .message(message)
                .build();
    }

    public static IpcOperationResult failure(String message, int errorCode) {
        return IpcOperationResult.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
