package cn.iocoder.yudao.module.iot.newgateway.plugins.nvr.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * NVR 操作结果
 */
@Data
@Builder
public class NvrOperationResult {

    /** 是否成功 */
    private boolean success;
    
    /** 消息 */
    private String message;
    
    /** 错误码 */
    private int errorCode;
    
    /** 返回数据 */
    private Map<String, Object> data;

    public static NvrOperationResult success(String message) {
        return NvrOperationResult.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static NvrOperationResult success(String message, Map<String, Object> data) {
        return NvrOperationResult.builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static NvrOperationResult failure(String message) {
        return NvrOperationResult.builder()
                .success(false)
                .message(message)
                .build();
    }

    public static NvrOperationResult failure(String message, int errorCode) {
        return NvrOperationResult.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
