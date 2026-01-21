package cn.iocoder.yudao.module.iot.newgateway.core.health;

import cn.iocoder.yudao.module.iot.newgateway.core.model.PluginDescriptor;
import cn.iocoder.yudao.module.iot.newgateway.core.registry.DevicePluginRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 插件健康检查指示器
 * 
 * <p>通过 Spring Boot Actuator 暴露插件健康状态。</p>
 * 
 * <h2>健康检查端点</h2>
 * <p>访问 /actuator/health 可以查看插件健康状态。</p>
 * 
 * <h2>健康状态判断</h2>
 * <ul>
 *     <li>UP: 所有已启用的插件都正常运行</li>
 *     <li>DOWN: 有插件运行异常</li>
 *     <li>UNKNOWN: 无法确定插件状态</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 */
@Component("gatewayPluginHealthIndicator")
@Slf4j
@RequiredArgsConstructor
public class PluginHealthIndicator implements HealthIndicator {

    private static final String LOG_PREFIX = "[PluginHealthIndicator]";

    private final DevicePluginRegistry pluginRegistry;

    @Override
    public Health health() {
        try {
            Collection<PluginDescriptor> allPlugins = pluginRegistry.getAllPlugins();
            Collection<PluginDescriptor> enabledPlugins = pluginRegistry.getEnabledPlugins();

            int totalCount = allPlugins.size();
            int enabledCount = enabledPlugins.size();
            int healthyCount = 0;
            int unhealthyCount = 0;

            Map<String, Object> pluginDetails = new HashMap<>();

            for (PluginDescriptor plugin : allPlugins) {
                Map<String, Object> pluginInfo = new HashMap<>();
                pluginInfo.put("name", plugin.getName());
                pluginInfo.put("deviceType", plugin.getDeviceType());
                pluginInfo.put("vendor", plugin.getVendor());
                pluginInfo.put("enabled", plugin.isEnabled());
                pluginInfo.put("status", plugin.getStatus() != null ? plugin.getStatus().name() : "UNKNOWN");

                if (plugin.isEnabled()) {
                    boolean healthy = checkPluginHealth(plugin);
                    pluginInfo.put("healthy", healthy);
                    if (healthy) {
                        healthyCount++;
                    } else {
                        unhealthyCount++;
                    }
                } else {
                    pluginInfo.put("healthy", "N/A");
                }

                pluginDetails.put(plugin.getId(), pluginInfo);
            }

            // 构建健康状态
            Health.Builder builder;
            if (unhealthyCount > 0) {
                builder = Health.down();
            } else if (enabledCount == 0) {
                builder = Health.unknown();
            } else {
                builder = Health.up();
            }

            return builder
                    .withDetail("totalPlugins", totalCount)
                    .withDetail("enabledPlugins", enabledCount)
                    .withDetail("healthyPlugins", healthyCount)
                    .withDetail("unhealthyPlugins", unhealthyCount)
                    .withDetail("plugins", pluginDetails)
                    .build();

        } catch (Exception e) {
            log.error("{} 健康检查失败", LOG_PREFIX, e);
            return Health.down()
                    .withException(e)
                    .build();
        }
    }

    /**
     * 检查单个插件的健康状态
     *
     * @param plugin 插件描述符
     * @return 是否健康
     */
    private boolean checkPluginHealth(PluginDescriptor plugin) {
        try {
            Object instance = plugin.getInstance();
            if (instance == null) {
                return false;
            }

            // 检查插件是否实现了 onHealthCheck 方法
            if (instance instanceof HealthCheckable) {
                return ((HealthCheckable) instance).onHealthCheck();
            }

            // 默认认为插件健康（如果没有实现 HealthCheckable 接口）
            return plugin.getStatus() == PluginDescriptor.PluginStatus.REGISTERED;
        } catch (Exception e) {
            log.warn("{} 插件健康检查异常: pluginId={}", LOG_PREFIX, plugin.getId(), e);
            return false;
        }
    }

    /**
     * 健康检查接口
     * 
     * <p>插件可以实现此接口来提供自定义的健康检查逻辑。</p>
     */
    public interface HealthCheckable {
        /**
         * 执行健康检查
         *
         * @return 是否健康
         */
        boolean onHealthCheck();
    }
}
