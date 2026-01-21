package cn.iocoder.yudao.module.iot.newgateway.core.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关启动初始化 Actuator 端点
 * <p>
 * 提供初始化状态查询和手动重试功能。
 * </p>
 *
 * <p>端点路径：/actuator/gatewayStartup</p>
 *
 * @author IoT Gateway Team
 */
@Slf4j
@Component
@Endpoint(id = "gatewayStartup")
@RequiredArgsConstructor
public class GatewayStartupEndpoint {

    private final GatewayStartupInitializer initializer;
    private final DeviceInitRetryManager retryManager;

    /**
     * 获取初始化状态和统计信息
     *
     * @return 状态信息
     */
    @ReadOperation
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();

        // 初始化状态
        result.put("status", initializer.getStatus().name());

        // 统计信息
        InitializationStatistics stats = initializer.getStatistics();
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", stats.getTotalCount().get());
        statistics.put("successCount", stats.getSuccessCount().get());
        statistics.put("failedCount", stats.getFailedCount().get());
        statistics.put("skippedCount", stats.getSkippedCount().get());
        statistics.put("successRate", String.format("%.1f%%", stats.getSuccessRate()));
        statistics.put("totalTimeMs", stats.getTotalTimeMs());
        result.put("statistics", statistics);

        // 重试队列信息
        Map<String, Object> retryInfo = new HashMap<>();
        retryInfo.put("queueSize", retryManager.getQueueSize());
        retryInfo.put("pendingDevices", retryManager.getPendingRetryDevices());
        retryInfo.put("failedDevices", retryManager.getFailedDevices());
        result.put("retryQueue", retryInfo);

        // 按设备类型统计
        Map<String, Object> byType = new HashMap<>();
        stats.getByDeviceType().forEach((type, typeStats) -> {
            Map<String, Object> typeInfo = new HashMap<>();
            typeInfo.put("success", typeStats.getSuccessCount().get());
            typeInfo.put("failed", typeStats.getFailedCount().get());
            typeInfo.put("skipped", typeStats.getSkippedCount().get());
            byType.put(type, typeInfo);
        });
        result.put("byDeviceType", byType);

        return result;
    }

    /**
     * 手动触发失败设备重试
     *
     * @return 操作结果
     */
    @WriteOperation
    public Map<String, Object> retryFailed() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Long> failedDevices = retryManager.getFailedDevices();
            int count = failedDevices.size();

            if (count == 0) {
                result.put("success", true);
                result.put("message", "没有需要重试的失败设备");
                return result;
            }

            log.info("[GatewayStartup] 手动触发 {} 个失败设备重试", count);
            initializer.retryFailedDevices();

            result.put("success", true);
            result.put("message", String.format("已触发 %d 个设备重试", count));
            result.put("deviceIds", failedDevices);

        } catch (Exception e) {
            log.error("[GatewayStartup] 手动重试失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "重试失败: " + e.getMessage());
        }

        return result;
    }
}
