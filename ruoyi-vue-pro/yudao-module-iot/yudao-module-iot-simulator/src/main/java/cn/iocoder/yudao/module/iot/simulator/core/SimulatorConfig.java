package cn.iocoder.yudao.module.iot.simulator.core;

import lombok.Data;

/**
 * 基础模拟器配置
 * 所有协议的模拟器配置都应继承此类
 *
 * @author Kiro
 */
@Data
public abstract class SimulatorConfig {

    /**
     * 协议类型
     */
    private String protocolType;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 服务器地址
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口
     */
    private int serverPort;

    /**
     * 行为模式
     */
    private SimulatorMode mode = SimulatorMode.SUCCESS;

    /**
     * 心跳间隔（秒）
     */
    private int heartbeatInterval = 30;

    /**
     * 响应延迟（毫秒，用于timeout模式）
     */
    private int responseDelay = 0;

    /**
     * 最大重连次数
     */
    private int maxRetries = 3;

    /**
     * 重连间隔（秒）
     */
    private int retryInterval = 5;

    /**
     * 连接超时（毫秒）
     */
    private int connectTimeout = 10000;
}
