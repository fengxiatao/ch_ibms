package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 门禁一代设备操作结果
 * 
 * <p>封装门禁一代设备各种操作（开门、关门、卡片管理等）的返回结果。</p>
 * 
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.AccessGen1SdkWrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessGen1OperationResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 操作消息
     * <p>
     * 成功时为操作描述，失败时为错误信息。
     * </p>
     */
    private String message;

    /**
     * 附加数据
     * <p>
     * 某些操作可能返回额外数据，如插入记录后返回的 recNo。
     * </p>
     */
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    /**
     * 错误码
     * <p>
     * SDK 返回的错误码，仅在失败时有效。
     * </p>
     */
    private Integer errorCode;

    /**
     * 创建成功结果
     *
     * @param message 成功消息
     * @return 操作结果
     */
    public static AccessGen1OperationResult success(String message) {
        return AccessGen1OperationResult.builder()
                .success(true)
                .message(message)
                .data(new HashMap<>())
                .build();
    }

    /**
     * 创建成功结果（带附加数据）
     *
     * @param message 成功消息
     * @param data    附加数据
     * @return 操作结果
     */
    public static AccessGen1OperationResult success(String message, Map<String, Object> data) {
        return AccessGen1OperationResult.builder()
                .success(true)
                .message(message)
                .data(data != null ? data : new HashMap<>())
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param message 错误信息
     * @return 操作结果
     */
    public static AccessGen1OperationResult failure(String message) {
        return AccessGen1OperationResult.builder()
                .success(false)
                .message(message)
                .data(new HashMap<>())
                .build();
    }

    /**
     * 创建失败结果（带错误码）
     *
     * @param message   错误信息
     * @param errorCode 错误码
     * @return 操作结果
     */
    public static AccessGen1OperationResult failure(String message, Integer errorCode) {
        return AccessGen1OperationResult.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .data(new HashMap<>())
                .build();
    }

    /**
     * 获取附加数据中的值
     *
     * @param key 键
     * @param <T> 值类型
     * @return 值，如果不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        return data != null ? (T) data.get(key) : null;
    }

    /**
     * 获取附加数据中的整数值
     *
     * @param key 键
     * @return 整数值，如果不存在或类型不匹配则返回 null
     */
    public Integer getIntData(String key) {
        if (data == null) {
            return null;
        }
        Object value = data.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * 获取附加数据中的字符串值
     *
     * @param key 键
     * @return 字符串值，如果不存在则返回 null
     */
    public String getStringData(String key) {
        if (data == null) {
            return null;
        }
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }
}
