package cn.iocoder.yudao.module.iot.newgateway.core.metrics;

import cn.iocoder.yudao.module.iot.newgateway.core.connection.ConnectionManager;
import cn.iocoder.yudao.module.iot.newgateway.core.model.PluginDescriptor;
import cn.iocoder.yudao.module.iot.newgateway.core.registry.DevicePluginRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 网关指标端点
 * 
 * <p>提供网关和插件的详细指标信息。</p>
 * 
 * <h2>端点访问</h2>
 * <ul>
 *     <li>GET /actuator/gateway-metrics - 获取所有指标</li>
 *     <li>GET /actuator/gateway-metrics/{pluginId} - 获取指定插件指标</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 */
@Component
@Endpoint(id = "gateway-metrics")
@Slf4j
@RequiredArgsConstructor
public class GatewayMetricsEndpoint {

    private static final String LOG_PREFIX = "[GatewayMetricsEndpoint]";

    private final DevicePluginRegistry pluginRegistry;
    private final GatewayMetrics gatewayMetrics;

    /**
     * 获取所有指标
     *
     * @return 指标信息
     */
    @ReadOperation
    public Map<String, Object> metrics() {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            // 时间戳
            result.put("timestamp", System.currentTimeMillis());

            // 全局统计
            Map<String, Object> global = new LinkedHashMap<>();
            global.put("totalOnlineDevices", gatewayMetrics.getTotalOnlineDeviceCount());
            global.put("totalMessages", gatewayMetrics.getTotalMessageCount());
            global.put("totalErrors", gatewayMetrics.getTotalErrorCount());
            result.put("global", global);

            // 插件统计
            Map<String, Object> plugins = new LinkedHashMap<>();
            Collection<PluginDescriptor> allPlugins = pluginRegistry.getAllPlugins();

            for (PluginDescriptor plugin : allPlugins) {
                if (!plugin.isEnabled()) {
                    continue;
                }

                Map<String, Object> pluginMetrics = new LinkedHashMap<>();
                pluginMetrics.put("name", plugin.getName());
                pluginMetrics.put("deviceType", plugin.getDeviceType());
                pluginMetrics.put("status", plugin.getStatus() != null ? plugin.getStatus().name() : "UNKNOWN");

                // 在线设备数
                int onlineCount = getOnlineDeviceCount(plugin);
                pluginMetrics.put("onlineDevices", onlineCount);

                // 从 GatewayMetrics 获取指标
                pluginMetrics.put("messageCount", gatewayMetrics.getOnlineDeviceCount(plugin.getId()));

                plugins.put(plugin.getId(), pluginMetrics);
            }
            result.put("plugins", plugins);

        } catch (Exception e) {
            log.error("{} 获取指标失败", LOG_PREFIX, e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 获取指定插件的指标
     *
     * @param pluginId 插件ID
     * @return 插件指标信息
     */
    @ReadOperation
    public Map<String, Object> pluginMetrics(@Selector String pluginId) {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            PluginDescriptor plugin = pluginRegistry.getPlugin(pluginId);

            if (plugin == null) {
                result.put("status", "NOT_FOUND");
                result.put("message", "插件不存在: " + pluginId);
                return result;
            }

            result.put("timestamp", System.currentTimeMillis());
            result.put("pluginId", pluginId);
            result.put("name", plugin.getName());
            result.put("deviceType", plugin.getDeviceType());
            result.put("enabled", plugin.isEnabled());
            result.put("status", plugin.getStatus() != null ? plugin.getStatus().name() : "UNKNOWN");

            // 在线设备数
            int onlineCount = getOnlineDeviceCount(plugin);
            result.put("onlineDevices", onlineCount);

            // 运行时间
            if (plugin.getStartTime() != null) {
                result.put("startTime", plugin.getStartTime());
                result.put("uptimeMs", System.currentTimeMillis() - plugin.getStartTime());
            }

        } catch (Exception e) {
            log.error("{} 获取插件指标失败: pluginId={}", LOG_PREFIX, pluginId, e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 获取插件的在线设备数量
     */
    private int getOnlineDeviceCount(PluginDescriptor plugin) {
        try {
            Object instance = plugin.getInstance();
            if (instance == null) {
                return 0;
            }

            // 尝试通过反射获取 connectionManager 字段
            java.lang.reflect.Field[] fields = instance.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                if (ConnectionManager.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    ConnectionManager<?> connectionManager = (ConnectionManager<?>) field.get(instance);
                    if (connectionManager != null) {
                        return connectionManager.getOnlineCount();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("{} 获取在线设备数量失败: pluginId={}", LOG_PREFIX, plugin.getId(), e);
        }
        return 0;
    }
}
