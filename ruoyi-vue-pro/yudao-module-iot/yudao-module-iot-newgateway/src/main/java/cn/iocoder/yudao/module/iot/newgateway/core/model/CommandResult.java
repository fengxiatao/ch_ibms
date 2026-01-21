package cn.iocoder.yudao.module.iot.newgateway.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

/**
 * 命令执行结果
 * <p>
 * 封装设备命令执行的结果信息，包括执行状态、消息和返回数据。
 * 提供静态工厂方法便于创建成功或失败的结果对象。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * // 成功结果
 * CommandResult success = CommandResult.success(Map.of("message", "命令已发送"));
 *
 * // 失败结果
 * CommandResult failure = CommandResult.failure("设备未连接");
 *
 * // 带数据的成功结果
 * CommandResult result = CommandResult.success(Map.of(
 *     "status", "online",
 *     "timestamp", System.currentTimeMillis()
 * ));
 * }
 * </pre>
 *
 * @author IoT Gateway Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandResult {

    /**
     * 执行是否成功
     */
    private boolean success;

    /**
     * 结果消息
     * <p>
     * 成功时可为空或包含提示信息，失败时包含错误原因。
     * </p>
     */
    private String message;

    /**
     * 返回数据
     * <p>
     * 命令执行后返回的数据，如设备状态、查询结果等。
     * </p>
     */
    private Map<String, Object> data;

    /**
     * 创建成功结果
     *
     * @param data 返回数据
     * @return 成功的命令结果
     */
    public static CommandResult success(Map<String, Object> data) {
        return CommandResult.builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * 创建成功结果（无数据）
     *
     * @return 成功的命令结果
     */
    public static CommandResult success() {
        return CommandResult.builder()
                .success(true)
                .data(Collections.emptyMap())
                .build();
    }

    /**
     * 创建成功结果（带消息）
     *
     * @param message 成功消息
     * @return 成功的命令结果
     */
    public static CommandResult success(String message) {
        return CommandResult.builder()
                .success(true)
                .message(message)
                .data(Collections.emptyMap())
                .build();
    }

    /**
     * 创建成功结果（带消息和数据）
     *
     * @param message 成功消息
     * @param data    返回数据
     * @return 成功的命令结果
     */
    public static CommandResult success(String message, Map<String, Object> data) {
        return CommandResult.builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param message 错误消息
     * @return 失败的命令结果
     */
    public static CommandResult failure(String message) {
        return CommandResult.builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * 创建失败结果（带数据）
     *
     * @param message 错误消息
     * @param data    附加数据（如错误详情）
     * @return 失败的命令结果
     */
    public static CommandResult failure(String message, Map<String, Object> data) {
        return CommandResult.builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }
}
