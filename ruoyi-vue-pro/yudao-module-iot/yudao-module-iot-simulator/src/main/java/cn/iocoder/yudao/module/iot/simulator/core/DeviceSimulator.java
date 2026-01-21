package cn.iocoder.yudao.module.iot.simulator.core;

import java.util.concurrent.CompletableFuture;

/**
 * 设备模拟器基础接口
 * 所有协议的设备模拟器都需要实现此接口
 *
 * @author Kiro
 */
public interface DeviceSimulator {

    /**
     * 获取设备唯一标识
     *
     * @return 设备ID
     */
    String getDeviceId();

    /**
     * 获取协议类型
     *
     * @return 协议类型标识
     */
    String getProtocolType();

    /**
     * 启动模拟器
     *
     * @return 异步完成结果
     */
    CompletableFuture<Void> start();

    /**
     * 停止模拟器
     *
     * @return 异步完成结果
     */
    CompletableFuture<Void> stop();

    /**
     * 获取当前状态
     *
     * @return 模拟器状态
     */
    SimulatorState getState();

    /**
     * 发送消息
     *
     * @param message 要发送的消息对象
     */
    void sendMessage(Object message);

    /**
     * 获取配置
     *
     * @return 模拟器配置
     */
    SimulatorConfig getConfig();
}
