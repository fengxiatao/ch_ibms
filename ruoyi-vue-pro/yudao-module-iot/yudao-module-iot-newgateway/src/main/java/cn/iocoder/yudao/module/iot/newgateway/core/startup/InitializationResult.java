package cn.iocoder.yudao.module.iot.newgateway.core.startup;

import lombok.Builder;
import lombok.Data;

/**
 * 设备初始化结果
 * <p>
 * 记录单个设备初始化的结果，包括状态、耗时和错误信息。
 * </p>
 *
 * @author IoT Gateway Team
 */
@Data
@Builder
public class InitializationResult {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 结果状态
     */
    private ResultStatus status;

    /**
     * 错误信息（失败时）
     */
    private String errorMessage;

    /**
     * 耗时（毫秒）
     */
    private long durationMs;

    /**
     * 结果状态枚举
     */
    public enum ResultStatus {
        /**
         * 成功
         */
        SUCCESS,
        /**
         * 失败
         */
        FAILED,
        /**
         * 跳过（被动设备或配置不完整）
         */
        SKIPPED,
        /**
         * 超时
         */
        TIMEOUT
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建成功结果
     *
     * @param deviceId   设备ID
     * @param deviceType 设备类型
     * @param durationMs 耗时（毫秒）
     * @return 成功结果
     */
    public static InitializationResult success(Long deviceId, String deviceType, long durationMs) {
        return InitializationResult.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .status(ResultStatus.SUCCESS)
                .durationMs(durationMs)
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param deviceId     设备ID
     * @param deviceType   设备类型
     * @param errorMessage 错误信息
     * @param durationMs   耗时（毫秒）
     * @return 失败结果
     */
    public static InitializationResult failed(Long deviceId, String deviceType, String errorMessage, long durationMs) {
        return InitializationResult.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .status(ResultStatus.FAILED)
                .errorMessage(errorMessage)
                .durationMs(durationMs)
                .build();
    }

    /**
     * 创建跳过结果
     *
     * @param deviceId   设备ID
     * @param deviceType 设备类型
     * @param reason     跳过原因
     * @return 跳过结果
     */
    public static InitializationResult skipped(Long deviceId, String deviceType, String reason) {
        return InitializationResult.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .status(ResultStatus.SKIPPED)
                .errorMessage(reason)
                .durationMs(0)
                .build();
    }

    /**
     * 创建超时结果
     *
     * @param deviceId   设备ID
     * @param deviceType 设备类型
     * @return 超时结果
     */
    public static InitializationResult timeout(Long deviceId, String deviceType) {
        return InitializationResult.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .status(ResultStatus.TIMEOUT)
                .errorMessage("初始化超时")
                .build();
    }

    /**
     * 判断是否成功
     *
     * @return true 如果成功
     */
    public boolean isSuccess() {
        return status == ResultStatus.SUCCESS;
    }

    /**
     * 判断是否失败（包括超时）
     *
     * @return true 如果失败或超时
     */
    public boolean isFailed() {
        return status == ResultStatus.FAILED || status == ResultStatus.TIMEOUT;
    }

    /**
     * 判断是否跳过
     *
     * @return true 如果跳过
     */
    public boolean isSkipped() {
        return status == ResultStatus.SKIPPED;
    }
}
