package cn.iocoder.yudao.module.iot.simulator.core;

import lombok.Data;

/**
 * 连接配置
 *
 * @author Kiro
 */
@Data
public class ConnectionConfig {

    /**
     * 连接ID
     */
    private String connectionId;

    /**
     * 服务器主机地址
     */
    private String host;

    /**
     * 服务器端口
     */
    private int port;

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 10000;

    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 30000;

    /**
     * 是否自动重连
     */
    private boolean autoReconnect = true;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 重试间隔（毫秒）
     */
    private int retryInterval = 5000;
}
