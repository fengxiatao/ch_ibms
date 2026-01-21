package cn.iocoder.yudao.module.iot.newgateway.core.health;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 网关健康检查端点
 * 
 * <p>提供详细的网关和插件健康状态信息。</p>
 * 
 * <h2>端点访问</h2>
 * <ul>
 *     <li>GET /actuator/gateway - 获取网关整体状态</li>
 *     <li>GET /actuator/gateway/{pluginId} - 获取指定插件状态</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 */
@Component
@Endpoint(id = "gateway")
@Slf4j
@RequiredArgsConstructor
public class GatewayHealthEndpoint {

    private static final String LOG_PREFIX = "[GatewayHealthEndpoint]";

    private final DevicePluginRegistry pluginRegistry;

    /**
     * 获取网关整体健康状态
     *
     * @return 网关健康状态信息
     */
    @ReadOperation
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        
        try {
            // 基本信息
            result.put("status", "UP");
            result.put("timestamp", System.currentTimeMillis());
            
            // 插件统计
            Collection<PluginDescriptor> allPlugins = pluginRegistry.getAllPlugins();
            Collection<PluginDescriptor> enabledPlugins = pluginRegistry.getEnabledPlugins();
            
            Map<String, Object> pluginStats = new LinkedHashMap<>();
            pluginStats.put("total", allPlugins.size());
            pluginStats.put("enabled", enabledPlugins.size());
            pluginStats.put("disabled", allPlugins.size() - enabledPlugins.size());
            result.put("plugins", pluginStats);
            
            // 设备类型统计
            Map<String, Object> deviceTypes = new LinkedHashMap<>();
            for (PluginDescriptor plugin : enabledPlugins) {
                Map<String, Object> pluginInfo = new LinkedHashMap<>();
                pluginInfo.put("name", plugin.getName());
                pluginInfo.put("vendor", plugin.getVendor());
                pluginInfo.put("status", plugin.getStatus() != null ? plugin.getStatus().name() : "UNKNOWN");
                pluginInfo.put("enabled", plugin.isEnabled());
                
                // 获取在线设备数量
                int onlineCount = getOnlineDeviceCount(plugin);
                pluginInfo.put("onlineDevices", onlineCount);
                
                deviceTypes.put(plugin.getDeviceType(), pluginInfo);
            }
            result.put("deviceTypes", deviceTypes);
            
            // 支持的设备类型列表
            result.put("supportedDeviceTypes", pluginRegistry.getSupportedDeviceTypes());
            
        } catch (Exception e) {
            log.error("{} 获取健康状态失败", LOG_PREFIX, e);
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取指定插件的详细状态
     *
     * @param pluginId 插件ID
     * @return 插件详细状态信息
     */
    @ReadOperation
    public Map<String, Object> pluginHealth(@Selector String pluginId) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        try {
            PluginDescriptor plugin = pluginRegistry.getPlugin(pluginId);
            
            if (plugin == null) {
                result.put("status", "NOT_FOUND");
                result.put("message", "插件不存在: " + pluginId);
                return result;
            }
            
            result.put("status", plugin.isEnabled() ? "UP" : "DISABLED");
            result.put("id", plugin.getId());
            result.put("name", plugin.getName());
            result.put("deviceType", plugin.getDeviceType());
            result.put("vendor", plugin.getVendor());
            result.put("description", plugin.getDescription());
            result.put("enabled", plugin.isEnabled());
            result.put("pluginStatus", plugin.getStatus() != null ? plugin.getStatus().name() : "UNKNOWN");
            
            if (plugin.getStartTime() != null) {
                result.put("startTime", plugin.getStartTime());
                result.put("uptime", System.currentTimeMillis() - plugin.getStartTime());
            }
            
            // 获取在线设备数量
            int onlineCount = getOnlineDeviceCount(plugin);
            result.put("onlineDevices", onlineCount);
            
            // 健康检查
            boolean healthy = checkPluginHealth(plugin);
            result.put("healthy", healthy);
            
        } catch (Exception e) {
            log.error("{} 获取插件状态失败: pluginId={}", LOG_PREFIX, pluginId, e);
            result.put("status", "ERROR");
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

    /**
     * 检查插件健康状态
     */
    private boolean checkPluginHealth(PluginDescriptor plugin) {
        try {
            Object instance = plugin.getInstance();
            if (instance == null) {
                return false;
            }
            
            if (instance instanceof PluginHealthIndicator.HealthCheckable) {
                return ((PluginHealthIndicator.HealthCheckable) instance).onHealthCheck();
            }
            
            return plugin.isEnabled() && plugin.getStatus() == PluginDescriptor.PluginStatus.REGISTERED;
        } catch (Exception e) {
            return false;
        }
    }
}
