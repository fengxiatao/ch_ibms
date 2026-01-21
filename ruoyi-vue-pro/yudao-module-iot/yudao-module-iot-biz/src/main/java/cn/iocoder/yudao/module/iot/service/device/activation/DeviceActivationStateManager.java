package cn.iocoder.yudao.module.iot.service.device.activation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备激活状态管理器
 * 
 * <p>统一管理设备激活过程中的状态，避免多处重复维护状态导致不一致。</p>
 * 
 * <p>激活流程状态：</p>
 * <ul>
 *   <li>activating - 正在激活中</li>
 *   <li>completed - 激活完成</li>
 *   <li>failed - 激活失败</li>
 *   <li>not_found - 未找到激活请求</li>
 * </ul>
 * 
 * <p>Requirements: 统一管理激活状态，消除重复逻辑</p>
 *
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
public class DeviceActivationStateManager {

    /**
     * 激活状态：activating, completed, failed
     */
    public static final String STATUS_ACTIVATING = "activating";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_NOT_FOUND = "not_found";

    /**
     * 激活状态缓存: activationId -> status
     */
    private final Map<String, String> activationStatusMap = new ConcurrentHashMap<>();

    /**
     * 激活结果缓存: activationId -> deviceId
     */
    private final Map<String, Long> activationResultMap = new ConcurrentHashMap<>();

    /**
     * 设备ID到激活ID映射: deviceId -> activationId
     */
    private final Map<Long, String> deviceActivationMap = new ConcurrentHashMap<>();

    /**
     * 激活错误信息映射: activationId -> errorMessage
     */
    private final Map<String, String> activationErrorMap = new ConcurrentHashMap<>();

    /**
     * 设备需要通道同步标志: deviceId -> needsSync
     */
    private final Map<Long, Boolean> deviceNeedsSyncChannel = new ConcurrentHashMap<>();

    // ==================== 激活流程管理 ====================

    /**
     * 开始设备激活
     * 
     * @param activationId 激活ID
     * @param deviceId 设备ID
     */
    public void startActivation(String activationId, Long deviceId) {
        activationStatusMap.put(activationId, STATUS_ACTIVATING);
        deviceActivationMap.put(deviceId, activationId);
        log.info("[DeviceActivationStateManager] 开始激活: activationId={}, deviceId={}", 
                activationId, deviceId);
    }

    /**
     * 完成设备激活
     * 
     * @param deviceId 设备ID
     */
    public void completeActivation(Long deviceId) {
        String activationId = deviceActivationMap.get(deviceId);
        if (activationId == null) {
            log.debug("[DeviceActivationStateManager] 设备无激活请求: deviceId={}", deviceId);
            return;
        }

        // 检查当前状态，只有 activating 状态才能转为 completed
        String currentStatus = activationStatusMap.get(activationId);
        if (!STATUS_ACTIVATING.equals(currentStatus)) {
            log.debug("[DeviceActivationStateManager] 激活状态不是 activating，跳过完成: activationId={}, currentStatus={}", 
                    activationId, currentStatus);
            return;
        }

        activationStatusMap.put(activationId, STATUS_COMPLETED);
        activationResultMap.put(activationId, deviceId);
        log.info("[DeviceActivationStateManager] 激活完成: activationId={}, deviceId={}", 
                activationId, deviceId);
    }

    /**
     * 激活失败
     * 
     * @param deviceId 设备ID
     * @param errorMessage 错误信息
     */
    public void failActivation(Long deviceId, String errorMessage) {
        String activationId = deviceActivationMap.get(deviceId);
        if (activationId == null) {
            log.debug("[DeviceActivationStateManager] 设备无激活请求: deviceId={}", deviceId);
            return;
        }

        // 检查当前状态，只有 activating 状态才能转为 failed
        String currentStatus = activationStatusMap.get(activationId);
        if (!STATUS_ACTIVATING.equals(currentStatus)) {
            log.debug("[DeviceActivationStateManager] 激活状态不是 activating，跳过失败: activationId={}, currentStatus={}", 
                    activationId, currentStatus);
            return;
        }

        activationStatusMap.put(activationId, STATUS_FAILED);
        activationErrorMap.put(activationId, errorMessage != null ? errorMessage : "Device activation failed");
        log.warn("[DeviceActivationStateManager] 激活失败: activationId={}, deviceId={}, error={}", 
                activationId, deviceId, errorMessage);
    }

    // ==================== 状态查询 ====================

    /**
     * 获取激活状态
     * 
     * @param activationId 激活ID
     * @return 激活状态
     */
    public String getActivationStatus(String activationId) {
        String status = activationStatusMap.getOrDefault(activationId, STATUS_NOT_FOUND);
        log.debug("[DeviceActivationStateManager] 查询激活状态: activationId={}, status={}", 
                activationId, status);
        return status;
    }

    /**
     * 获取激活结果
     * 
     * @param activationId 激活ID
     * @return 设备ID，如果激活未完成或不存在则返回null
     */
    public Long getActivationResult(String activationId) {
        String status = activationStatusMap.get(activationId);
        
        if (status == null) {
            log.warn("[DeviceActivationStateManager] 激活请求不存在: activationId={}", activationId);
            return null;
        }
        
        if (STATUS_ACTIVATING.equals(status)) {
            log.debug("[DeviceActivationStateManager] 激活进行中: activationId={}", activationId);
            return null;
        }
        
        Long deviceId = activationResultMap.get(activationId);
        log.info("[DeviceActivationStateManager] 获取激活结果: activationId={}, deviceId={}", 
                activationId, deviceId);
        
        return deviceId;
    }

    /**
     * 获取激活状态详情（包含错误信息）
     * 
     * @param activationId 激活ID
     * @return 激活状态和结果
     */
    public Map<String, Object> getActivationStatusDetail(String activationId) {
        String status = activationStatusMap.get(activationId);
        
        if (status == null) {
            log.warn("[DeviceActivationStateManager] 激活请求不存在: activationId={}", activationId);
            return null;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        
        if (STATUS_COMPLETED.equals(status)) {
            Long deviceId = activationResultMap.get(activationId);
            result.put("deviceId", deviceId);
            log.info("[DeviceActivationStateManager] 激活成功: activationId={}, deviceId={}", 
                    activationId, deviceId);
        } else if (STATUS_FAILED.equals(status)) {
            String errorMessage = activationErrorMap.get(activationId);
            result.put("errorMessage", errorMessage != null ? errorMessage : "Device activation failed");
            log.warn("[DeviceActivationStateManager] 激活失败: activationId={}, error={}", 
                    activationId, errorMessage);
        } else {
            log.debug("[DeviceActivationStateManager] 激活进行中: activationId={}", activationId);
        }
        
        return result;
    }

    /**
     * 判断设备是否处于激活状态
     * 
     * @param deviceId 设备ID
     * @return 是否正在激活
     */
    public boolean isActivating(Long deviceId) {
        String activationId = deviceActivationMap.get(deviceId);
        if (activationId == null) {
            return false;
        }
        return STATUS_ACTIVATING.equals(activationStatusMap.get(activationId));
    }

    // ==================== 通道同步管理 ====================

    /**
     * 标记设备需要通道同步
     * 
     * @param deviceId 设备ID
     */
    public void markNeedsSyncChannel(Long deviceId) {
        deviceNeedsSyncChannel.put(deviceId, true);
        log.debug("[DeviceActivationStateManager] 标记设备需要通道同步: deviceId={}", deviceId);
    }

    /**
     * 检查并清除通道同步标记
     * 
     * @param deviceId 设备ID
     * @return 是否需要同步
     */
    public boolean checkAndClearNeedsSyncChannel(Long deviceId) {
        Boolean needsSync = deviceNeedsSyncChannel.remove(deviceId);
        return Boolean.TRUE.equals(needsSync);
    }

    /**
     * 检查设备是否需要通道同步
     * 
     * @param deviceId 设备ID
     * @return 是否需要同步
     */
    public boolean needsSyncChannel(Long deviceId) {
        return Boolean.TRUE.equals(deviceNeedsSyncChannel.get(deviceId));
    }

    // ==================== 清理方法 ====================

    /**
     * 清理设备激活状态
     * 
     * @param deviceId 设备ID
     */
    public void clearActivationState(Long deviceId) {
        String activationId = deviceActivationMap.remove(deviceId);
        if (activationId != null) {
            // 不立即清理 activationStatusMap 和 activationResultMap，
            // 允许前端查询一段时间
            log.debug("[DeviceActivationStateManager] 清理设备激活映射: deviceId={}, activationId={}", 
                    deviceId, activationId);
        }
        deviceNeedsSyncChannel.remove(deviceId);
    }

    /**
     * 清理过期的激活状态（可由定时任务调用）
     * 
     * @param maxAgeMs 最大保留时间（毫秒）
     */
    public void cleanupExpiredActivations(long maxAgeMs) {
        // TODO: 实现基于时间的清理逻辑
        log.debug("[DeviceActivationStateManager] 清理过期激活状态: maxAgeMs={}", maxAgeMs);
    }
}
