package cn.iocoder.yudao.module.iot.newgateway.core.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 网关监控指标
 * 
 * <p>提供网关和插件的监控指标，通过 Micrometer 暴露到 /actuator/metrics 端点。</p>
 * 
 * <h2>指标类型</h2>
 * <ul>
 *     <li>在线设备数量 (Gauge): gateway.devices.online</li>
 *     <li>消息计数 (Counter): gateway.messages.total</li>
 *     <li>错误计数 (Counter): gateway.errors.total</li>
 *     <li>命令执行时间 (Timer): gateway.commands.duration</li>
 * </ul>
 * 
 * <h2>使用示例</h2>
 * <pre>
 * {@code
 * // 记录消息
 * gatewayMetrics.incrementMessageCount("alarm", "heartbeat");
 * 
 * // 记录错误
 * gatewayMetrics.incrementErrorCount("alarm", "connection_failed");
 * 
 * // 更新在线设备数
 * gatewayMetrics.updateOnlineDeviceCount("alarm", 10);
 * }
 * </pre>
 * 
 * @author IoT Gateway Team
 */
@Component
@Slf4j
public class GatewayMetrics {

    private static final String LOG_PREFIX = "[GatewayMetrics]";

    private static final String METRIC_PREFIX = "gateway";

    private final MeterRegistry meterRegistry;

    // 在线设备数量（按插件）
    private final Map<String, AtomicInteger> onlineDeviceCounts = new ConcurrentHashMap<>();

    // 消息计数器（按插件和消息类型）
    private final Map<String, Counter> messageCounters = new ConcurrentHashMap<>();

    // 错误计数器（按插件和错误类型）
    private final Map<String, Counter> errorCounters = new ConcurrentHashMap<>();

    // 命令执行计时器（按插件和命令类型）
    private final Map<String, Timer> commandTimers = new ConcurrentHashMap<>();

    // 总在线设备数
    private final AtomicInteger totalOnlineDevices = new AtomicInteger(0);

    // 总消息数
    private final AtomicLong totalMessages = new AtomicLong(0);

    // 总错误数
    private final AtomicLong totalErrors = new AtomicLong(0);

    public GatewayMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeGlobalMetrics();
    }

    /**
     * 初始化全局指标
     */
    private void initializeGlobalMetrics() {
        // 总在线设备数
        Gauge.builder(METRIC_PREFIX + ".devices.online.total", totalOnlineDevices, AtomicInteger::get)
                .description("Total number of online devices")
                .register(meterRegistry);

        // 总消息数
        Gauge.builder(METRIC_PREFIX + ".messages.total", totalMessages, AtomicLong::get)
                .description("Total number of messages processed")
                .register(meterRegistry);

        // 总错误数
        Gauge.builder(METRIC_PREFIX + ".errors.total", totalErrors, AtomicLong::get)
                .description("Total number of errors")
                .register(meterRegistry);

        log.info("{} 全局指标初始化完成", LOG_PREFIX);
    }

    /**
     * 注册插件指标
     *
     * @param pluginId   插件ID
     * @param deviceType 设备类型
     */
    public void registerPluginMetrics(String pluginId, String deviceType) {
        // 在线设备数
        AtomicInteger onlineCount = new AtomicInteger(0);
        onlineDeviceCounts.put(pluginId, onlineCount);

        Gauge.builder(METRIC_PREFIX + ".devices.online", onlineCount, AtomicInteger::get)
                .tag("plugin", pluginId)
                .tag("deviceType", deviceType)
                .description("Number of online devices for plugin")
                .register(meterRegistry);

        log.info("{} 插件指标注册完成: pluginId={}, deviceType={}", LOG_PREFIX, pluginId, deviceType);
    }

    /**
     * 更新在线设备数量
     *
     * @param pluginId 插件ID
     * @param count    在线设备数量
     */
    public void updateOnlineDeviceCount(String pluginId, int count) {
        AtomicInteger counter = onlineDeviceCounts.get(pluginId);
        if (counter != null) {
            int oldCount = counter.getAndSet(count);
            // 更新总数
            totalOnlineDevices.addAndGet(count - oldCount);
        }
    }

    /**
     * 增加在线设备数量
     *
     * @param pluginId 插件ID
     */
    public void incrementOnlineDeviceCount(String pluginId) {
        AtomicInteger counter = onlineDeviceCounts.get(pluginId);
        if (counter != null) {
            counter.incrementAndGet();
            totalOnlineDevices.incrementAndGet();
        }
    }

    /**
     * 减少在线设备数量
     *
     * @param pluginId 插件ID
     */
    public void decrementOnlineDeviceCount(String pluginId) {
        AtomicInteger counter = onlineDeviceCounts.get(pluginId);
        if (counter != null) {
            counter.decrementAndGet();
            totalOnlineDevices.decrementAndGet();
        }
    }

    /**
     * 增加消息计数
     *
     * @param pluginId    插件ID
     * @param messageType 消息类型
     */
    public void incrementMessageCount(String pluginId, String messageType) {
        String key = pluginId + ":" + messageType;
        Counter counter = messageCounters.computeIfAbsent(key, k ->
                Counter.builder(METRIC_PREFIX + ".messages")
                        .tag("plugin", pluginId)
                        .tag("type", messageType)
                        .description("Number of messages processed")
                        .register(meterRegistry)
        );
        counter.increment();
        totalMessages.incrementAndGet();
    }

    /**
     * 增加错误计数
     *
     * @param pluginId  插件ID
     * @param errorType 错误类型
     */
    public void incrementErrorCount(String pluginId, String errorType) {
        String key = pluginId + ":" + errorType;
        Counter counter = errorCounters.computeIfAbsent(key, k ->
                Counter.builder(METRIC_PREFIX + ".errors")
                        .tag("plugin", pluginId)
                        .tag("type", errorType)
                        .description("Number of errors")
                        .register(meterRegistry)
        );
        counter.increment();
        totalErrors.incrementAndGet();
    }

    /**
     * 记录命令执行时间
     *
     * @param pluginId    插件ID
     * @param commandType 命令类型
     * @param runnable    要执行的操作
     */
    public void recordCommandDuration(String pluginId, String commandType, Runnable runnable) {
        String key = pluginId + ":" + commandType;
        Timer timer = commandTimers.computeIfAbsent(key, k ->
                Timer.builder(METRIC_PREFIX + ".commands.duration")
                        .tag("plugin", pluginId)
                        .tag("command", commandType)
                        .description("Command execution duration")
                        .register(meterRegistry)
        );
        timer.record(runnable);
    }

    /**
     * 记录命令执行时间
     *
     * @param pluginId    插件ID
     * @param commandType 命令类型
     * @param supplier    要执行的操作
     * @param <T>         返回类型
     * @return 操作结果
     */
    public <T> T recordCommandDuration(String pluginId, String commandType, Supplier<T> supplier) {
        String key = pluginId + ":" + commandType;
        Timer timer = commandTimers.computeIfAbsent(key, k ->
                Timer.builder(METRIC_PREFIX + ".commands.duration")
                        .tag("plugin", pluginId)
                        .tag("command", commandType)
                        .description("Command execution duration")
                        .register(meterRegistry)
        );
        return timer.record(supplier);
    }

    /**
     * 获取插件在线设备数量
     *
     * @param pluginId 插件ID
     * @return 在线设备数量
     */
    public int getOnlineDeviceCount(String pluginId) {
        AtomicInteger counter = onlineDeviceCounts.get(pluginId);
        return counter != null ? counter.get() : 0;
    }

    /**
     * 获取总在线设备数量
     *
     * @return 总在线设备数量
     */
    public int getTotalOnlineDeviceCount() {
        return totalOnlineDevices.get();
    }

    /**
     * 获取总消息数量
     *
     * @return 总消息数量
     */
    public long getTotalMessageCount() {
        return totalMessages.get();
    }

    /**
     * 获取总错误数量
     *
     * @return 总错误数量
     */
    public long getTotalErrorCount() {
        return totalErrors.get();
    }
}
