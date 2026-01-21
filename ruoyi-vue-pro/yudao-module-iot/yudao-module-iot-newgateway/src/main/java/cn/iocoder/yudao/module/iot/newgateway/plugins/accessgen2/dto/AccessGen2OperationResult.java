package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 门禁二代设备操作结果
 * 
 * <p>封装门禁二代设备各种操作（开门、授权、人脸下发等）的返回结果。</p>
 * 
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.AccessGen2SdkWrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessGen2OperationResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 操作消息
     */
    private String message;

    /**
     * 错误码
     * <p>
     * SDK 返回的错误码，成功时为 0。
     * </p>
     */
    private int errorCode;

    /**
     * 附加数据
     * <p>
     * 操作成功时可能返回的附加信息，如记录编号等。
     * </p>
     */
    private Map<String, Object> data;

    /**
     * 创建成功结果
     *
     * @param message 成功消息
     * @return 操作结果
     */
    public static AccessGen2OperationResult success(String message) {
        return AccessGen2OperationResult.builder()
                .success(true)
                .message(message)
                .errorCode(0)
                .build();
    }

    /**
     * 创建成功结果（带附加数据）
     *
     * @param message 成功消息
     * @param data    附加数据
     * @return 操作结果
     */
    public static AccessGen2OperationResult success(String message, Map<String, Object> data) {
        return AccessGen2OperationResult.builder()
                .success(true)
                .message(message)
                .errorCode(0)
                .data(data)
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param message 错误消息
     * @return 操作结果
     */
    public static AccessGen2OperationResult failure(String message) {
        return AccessGen2OperationResult.builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * 创建失败结果（带错误码）
     *
     * @param message   错误消息
     * @param errorCode 错误码
     * @return 操作结果
     */
    public static AccessGen2OperationResult failure(String message, int errorCode) {
        return AccessGen2OperationResult.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
