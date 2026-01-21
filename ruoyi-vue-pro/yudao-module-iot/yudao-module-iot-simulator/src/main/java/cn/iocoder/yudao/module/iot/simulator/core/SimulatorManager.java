package cn.iocoder.yudao.module.iot.simulator.core;

import cn.iocoder.yudao.module.iot.simulator.protocol.changhui.ChanghuiDeviceSimulator;
import cn.iocoder.yudao.module.iot.simulator.protocol.changhui.ChanghuiSimulatorConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 模拟器管理器
 * 管理多个模拟器实例，提供线程池和监控
 *
 * @author Kiro
 */
@Slf4j
@Component
public class SimulatorManager implements DisposableBean {

    /**
     * 模拟器实例映射
     */
    private final Map<String, DeviceSimulator> simulators = new ConcurrentHashMap<>();

    /**
     * 线程池
     */
    private final ExecutorService executorService;

    /**
     * 监控指标
     */
    private final SimulatorMetrics metrics;

    public SimulatorManager() {
        this.executorService = Executors.newFixedThreadPool(10);
        this.metrics = new SimulatorMetrics();
        log.info("SimulatorManager initialized with thread pool size: 10");
    }

    /**
     * 创建并返回模拟器（不自动启动）
     *
     * @param config 模拟器配置
     * @return 设备模拟器实例
     */
    public DeviceSimulator createSimulator(SimulatorConfig config) {
        String deviceId = config.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            throw new IllegalArgumentException("Device ID is required");
        }

        if (simulators.containsKey(deviceId)) {
            log.warn("Simulator already exists for device: {}", deviceId);
            return simulators.get(deviceId);
        }

        DeviceSimulator simulator = createSimulatorInstance(config);
        simulators.put(deviceId, simulator);
        metrics.incrementCreatedCount();
        log.info("Created simulator for device: {}, protocol: {}", deviceId, config.getProtocolType());
        return simulator;
    }

    /**
     * 根据配置创建具体的模拟器实例
     */
    private DeviceSimulator createSimulatorInstance(SimulatorConfig config) {
        String protocolType = config.getProtocolType();
        
        // 支持长辉协议
        if (ChanghuiSimulatorConfig.PROTOCOL_TYPE.equals(protocolType)) {
            if (!(config instanceof ChanghuiSimulatorConfig)) {
                throw new IllegalArgumentException("Config must be ChanghuiSimulatorConfig for CHANGHUI protocol");
            }
            return new ChanghuiDeviceSimulator((ChanghuiSimulatorConfig) config);
        }
        
        throw new IllegalArgumentException("Unsupported protocol type: " + protocolType);
    }

    /**
     * 批量创建模拟器
     *
     * @param configs 配置列表
     * @return 模拟器列表
     */
    public List<DeviceSimulator> createSimulators(List<SimulatorConfig> configs) {
        List<DeviceSimulator> result = new ArrayList<>();
        for (SimulatorConfig config : configs) {
            result.add(createSimulator(config));
        }
        return result;
    }

    /**
     * 停止指定模拟器
     *
     * @param deviceId 设备ID
     */
    public void stopSimulator(String deviceId) {
        DeviceSimulator simulator = simulators.remove(deviceId);
        if (simulator != null) {
            simulator.stop().thenRun(() -> {
                metrics.incrementStoppedCount();
                log.info("Stopped simulator for device: {}", deviceId);
            });
        }
    }

    /**
     * 停止所有模拟器
     */
    public void stopAll() {
        log.info("Stopping all simulators...");
        simulators.forEach((id, simulator) -> {
            simulator.stop().thenRun(() -> {
                metrics.incrementStoppedCount();
                log.info("Stopped simulator: {}", id);
            });
        });
        simulators.clear();
    }

    /**
     * 获取指定模拟器
     *
     * @param deviceId 设备ID
     * @return 模拟器实例，不存在返回null
     */
    public DeviceSimulator getSimulator(String deviceId) {
        return simulators.get(deviceId);
    }

    /**
     * 获取所有模拟器
     *
     * @return 模拟器列表
     */
    public List<DeviceSimulator> getAllSimulators() {
        return new ArrayList<>(simulators.values());
    }

    /**
     * 获取监控指标
     *
     * @return 监控指标
     */
    public SimulatorMetrics getMetrics() {
        return metrics;
    }

    /**
     * 获取活跃模拟器数量
     *
     * @return 活跃数量
     */
    public int getActiveCount() {
        return (int) simulators.values().stream()
                .filter(s -> s.getState() == SimulatorState.RUNNING)
                .count();
    }

    @Override
    public void destroy() throws Exception {
        log.info("Shutting down SimulatorManager...");
        stopAll();
        executorService.shutdown();
        if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }
        log.info("SimulatorManager shutdown complete");
    }
}
