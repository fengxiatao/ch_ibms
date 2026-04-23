package cn.iocoder.yudao.module.iot.newgateway.core;

/**
 * 网关常量定义
 * 
 * 定义网关模块使用的常量值
 */
public final class GatewayConstants {

    /**
     * 模块名称
     */
    public static final String MODULE_NAME = "iot-newgateway";

    /**
     * 默认心跳超时时间（毫秒）
     */
    public static final long DEFAULT_HEARTBEAT_TIMEOUT = 60000L;

    /**
     * 默认重连间隔（毫秒）
     */
    public static final long DEFAULT_RECONNECT_INTERVAL = 30000L;

    private GatewayConstants() {
        // 私有构造函数，防止实例化
    }
}
