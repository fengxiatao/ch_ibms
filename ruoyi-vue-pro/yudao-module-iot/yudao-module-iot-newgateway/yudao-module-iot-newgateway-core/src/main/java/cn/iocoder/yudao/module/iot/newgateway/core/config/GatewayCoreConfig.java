package cn.iocoder.yudao.module.iot.newgateway.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * 网关核心配置类
 * <p>
 * 提供网关核心框架的配置，包括心跳检测、线程池等核心参数。
 * 使用独立的配置前缀 iot.newgateway.core，与旧 gateway 的配置隔离。
 * </p>
 *
 * <p>配置示例：</p>
 * <pre>
 * iot:
 *   newgateway:
 *     core:
 *       heartbeat-check-interval: 30000
 *       device-offline-threshold: 90000
 *       worker-threads: 4
 * </pre>
 *
 * @author IoT Gateway Team
 * @see GatewayPluginProperties
 */
@Data
@Configuration
@Validated
@ConfigurationProperties(prefix = "iot.newgateway.core")
@EnableConfigurationProperties({GatewayPluginProperties.class, GatewayStartupProperties.class})
public class GatewayCoreConfig {

    /**
     * 心跳检测间隔（毫秒）
     * <p>
     * 定期检查设备心跳状态的时间间隔。
     * 建议设置为设备心跳间隔的 1/2，以便及时检测。
     * </p>
     */
    private long heartbeatCheckInterval = 30000;

    /**
     * 设备离线阈值（毫秒）
     * <p>
     * 设备超过此时间未发送心跳则判定为离线。
     * 
     * 4G网络设备建议计算公式：
     *   离线阈值 = 心跳间隔 × 容忍丢失次数 + 网络抖动容忍
     * 
     * 对于1分钟心跳的4G设备：
     *   60秒 × 3 + 60秒 = 240秒 (4分钟)
     * </p>
     */
    private long deviceOfflineThreshold = 240000;

    /**
     * 工作线程数
     * <p>
     * 用于处理设备消息的线程池大小
     * </p>
     */
    private int workerThreads = 4;

    /**
     * 是否启用健康检查端点
     */
    private boolean healthCheckEnabled = true;

    /**
     * 是否启用指标收集
     */
    private boolean metricsEnabled = true;

    /**
     * 消息发布重试次数
     */
    private int messagePublishRetries = 3;

    /**
     * 消息发布重试间隔（毫秒）
     */
    private long messagePublishRetryInterval = 1000;
}
