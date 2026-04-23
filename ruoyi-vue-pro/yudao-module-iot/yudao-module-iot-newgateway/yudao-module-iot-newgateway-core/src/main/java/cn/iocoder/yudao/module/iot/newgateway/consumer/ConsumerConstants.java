package cn.iocoder.yudao.module.iot.newgateway.consumer;

/**
 * 消费者常量定义
 * 
 * <p>定义消息消费者使用的常量值</p>
 * 
 * <p>注意：网关侧只有一个命令消费者（DeviceCommandConsumer），
 * 监听统一的 {@code iot_device_service_invoke} 主题</p>
 */
public final class ConsumerConstants {

    /**
     * 消费者组名称前缀
     */
    public static final String CONSUMER_GROUP_PREFIX = "iot-newgateway-";

    /**
     * 设备控制命令消费者组
     * <p>监听 {@code iot_device_service_invoke} 主题</p>
     */
    public static final String CONSUMER_GROUP_DEVICE_COMMAND = CONSUMER_GROUP_PREFIX + "device-command";

    /**
     * 设备台账变更（Biz → Gateway）
     */
    public static final String CONSUMER_GROUP_DEVICE_PROFILE = CONSUMER_GROUP_PREFIX + "device-profile";

    /**
     * 设备快照 Request-Reply 应答
     */
    public static final String CONSUMER_GROUP_DEVICE_SNAPSHOT_REPLY = CONSUMER_GROUP_PREFIX + "device-snapshot-reply";

    private ConsumerConstants() {
        // 私有构造函数，防止实例化
    }
}
