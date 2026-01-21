package cn.iocoder.yudao.module.iot.newgateway.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 设备登录结果
 * <p>
 * 封装主动连接设备的登录结果，包括登录状态、登录句柄和设备信息。
 * 用于 {@link cn.iocoder.yudao.module.iot.newgateway.core.handler.ActiveDeviceHandler#login(DeviceConnectionInfo)} 方法的返回值。
 * </p>
 *
 * @author IoT Gateway Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResult {

    /**
     * 登录是否成功
     */
    private boolean success;

    /**
     * 登录句柄
     * <p>
     * SDK 登录成功后返回的句柄，用于后续操作。
     * 不同 SDK 的句柄类型可能不同（如 Long、String 等）。
     * </p>
     */
    private Object loginHandle;

    /**
     * 错误消息
     * <p>
     * 登录失败时的错误信息。
     * </p>
     */
    private String errorMessage;

    /**
     * 错误码
     * <p>
     * SDK 返回的错误码。
     * </p>
     */
    private Integer errorCode;

    /**
     * 设备信息
     * <p>
     * 登录成功后获取的设备信息，如序列号、通道数等。
     * </p>
     */
    private Map<String, Object> deviceInfo;

    /**
     * 创建成功结果
     *
     * @param loginHandle 登录句柄
     * @return 成功的登录结果
     */
    public static LoginResult success(Object loginHandle) {
        return LoginResult.builder()
                .success(true)
                .loginHandle(loginHandle)
                .build();
    }

    /**
     * 创建成功结果（带设备信息）
     *
     * @param loginHandle 登录句柄
     * @param deviceInfo  设备信息
     * @return 成功的登录结果
     */
    public static LoginResult success(Object loginHandle, Map<String, Object> deviceInfo) {
        return LoginResult.builder()
                .success(true)
                .loginHandle(loginHandle)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param errorMessage 错误消息
     * @return 失败的登录结果
     */
    public static LoginResult failure(String errorMessage) {
        return LoginResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 创建失败结果（带错误码）
     *
     * @param errorMessage 错误消息
     * @param errorCode    错误码
     * @return 失败的登录结果
     */
    public static LoginResult failure(String errorMessage, Integer errorCode) {
        return LoginResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .build();
    }

    /**
     * 获取登录句柄（类型转换）
     *
     * @param <T> 句柄类型
     * @return 登录句柄
     */
    @SuppressWarnings("unchecked")
    public <T> T getHandle() {
        return (T) loginHandle;
    }

    /**
     * 获取设备信息中的指定字段
     *
     * @param key 字段名
     * @param <T> 值类型
     * @return 字段值，如果不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getDeviceInfoValue(String key) {
        return deviceInfo != null ? (T) deviceInfo.get(key) : null;
    }
}
