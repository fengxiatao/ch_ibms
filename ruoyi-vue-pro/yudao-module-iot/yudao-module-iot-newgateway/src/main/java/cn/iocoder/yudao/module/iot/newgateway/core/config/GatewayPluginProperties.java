package cn.iocoder.yudao.module.iot.newgateway.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 新网关插件配置属性
 * <p>
 * 使用独立的配置前缀 iot.newgateway，与旧 gateway 的配置隔离。
 * </p>
 *
 * <p>配置示例：</p>
 * <pre>
 * iot:
 *   newgateway:
 *     rpc:
 *       url: http://127.0.0.1:48888
 *       connect-timeout: 30s
 *       read-timeout: 30s
 *     plugins:
 *       enabled:
 *         alarm: true
 *         changhui: true
 *         access-gen1: false
 *       alarm:
 *         port: 9500
 *         heartbeat-timeout: 60000
 *       changhui:
 *         port: 9700
 *         heartbeat-timeout: 60000
 *         upgrade-timeout: 300000
 * </pre>
 *
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.core.registry.DevicePluginRegistry
 */
@Data
@Validated
@ConfigurationProperties(prefix = "iot.newgateway")
public class GatewayPluginProperties {

    /**
     * RPC 配置
     */
    private RpcProperties rpc = new RpcProperties();

    /**
     * 插件配置
     */
    private PluginsConfig plugins = new PluginsConfig();

    /**
     * 检查插件是否启用
     *
     * @param pluginId       插件ID
     * @param defaultEnabled 默认启用状态（来自 @DevicePlugin 注解）
     * @return 是否启用
     */
    public boolean isPluginEnabled(String pluginId, boolean defaultEnabled) {
        Boolean enabled = this.plugins.getEnabled().get(pluginId);
        return enabled != null ? enabled : defaultEnabled;
    }

    // ==================== RPC 配置类 ====================

    /**
     * RPC 配置
     */
    @Data
    public static class RpcProperties {
        /**
         * 业务服务地址
         */
        private String url = "http://127.0.0.1:48888";

        /**
         * 连接超时
         */
        private Duration connectTimeout = Duration.ofSeconds(30);

        /**
         * 读取超时
         */
        private Duration readTimeout = Duration.ofSeconds(30);
    }

    // ==================== 插件配置类 ====================

    /**
     * 插件配置
     */
    @Data
    public static class PluginsConfig {
        /**
         * 插件启用配置
         * <p>
         * key: 插件ID（如 "alarm", "changhui", "access-gen1"）
         * value: 是否启用
         * </p>
         * <p>
         * 如果插件ID不在此 Map 中，则使用插件注解中的 enabledByDefault 值。
         * </p>
         */
        private Map<String, Boolean> enabled = new HashMap<>();

        /**
         * 报警主机插件配置
         */
        private AlarmPluginConfig alarm = new AlarmPluginConfig();

        /**
         * 长辉TCP插件配置
         */
        private ChanghuiPluginConfig changhui = new ChanghuiPluginConfig();

        /**
         * 门禁一代插件配置
         */
        private AccessGen1PluginConfig accessGen1 = new AccessGen1PluginConfig();

        /**
         * 门禁二代插件配置
         */
        private AccessGen2PluginConfig accessGen2 = new AccessGen2PluginConfig();

        /**
         * NVR插件配置
         */
        private NvrPluginConfig nvr = new NvrPluginConfig();
    }

    /**
     * 报警主机插件配置
     */
    @Data
    public static class AlarmPluginConfig {
        /**
         * 监听端口
         */
        private int port = 9500;

        /**
         * 心跳超时时间（毫秒）
         */
        private long heartbeatTimeout = 60000;
    }

    /**
     * 长辉TCP插件配置
     */
    @Data
    public static class ChanghuiPluginConfig {
        /**
         * 监听端口
         */
        private int port = 9700;

        /**
         * 心跳超时时间（毫秒）
         */
        private long heartbeatTimeout = 60000;

        /**
         * 升级超时时间（毫秒）
         */
        private long upgradeTimeout = 300000;
    }

    /**
     * 门禁一代插件配置
     */
    @Data
    public static class AccessGen1PluginConfig {
        /**
         * SDK路径
         */
        private String sdkPath = "";

        /**
         * 重连间隔（毫秒）
         */
        private long reconnectInterval = 30000;
    }

    /**
     * 门禁二代插件配置
     */
    @Data
    public static class AccessGen2PluginConfig {
        /**
         * SDK路径
         */
        private String sdkPath = "";

        /**
         * 重连间隔（毫秒）
         */
        private long reconnectInterval = 30000;
    }

    /**
     * NVR插件配置
     */
    @Data
    public static class NvrPluginConfig {
        /**
         * SDK路径
         */
        private String sdkPath = "";

        /**
         * 重连间隔（毫秒）
         */
        private long reconnectInterval = 30000;
    }
}
