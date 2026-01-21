package cn.iocoder.yudao.module.iot.core.messagebus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

/**
 * IoT 消息总线配置属性
 *
 * @author 长辉信息科技有限公司
 */
@ConfigurationProperties("yudao.iot.message-bus")
@Data
@Validated
public class IotMessageBusProperties {

    /**
     * 消息总线类型
     *
     * 可选值：local、redis、rocketmq、rabbitmq
     */
    @NotNull(message = "IoT 消息总线类型不能为空")
    private String type = "local";

    /**
     * RocketMQ NameServer 地址
     *
     * 当 type 为 rocketmq 时必填
     * 例如：127.0.0.1:9876
     */
    private String nameServer;

    /**
     * 消费者组前缀
     * 
     * <p>用于区分不同环境（开发/测试/生产）的消费者组，避免共享同一个 RocketMQ 时队列分配冲突。</p>
     * 
     * <p>示例：</p>
     * <ul>
     *   <li>开发环境设置为 "dev-"，消费者组变成 "dev-iot-biz-device-result"</li>
     *   <li>测试环境设置为 "test-"，消费者组变成 "test-iot-biz-device-result"</li>
     *   <li>生产环境留空或设置为 ""，消费者组保持 "iot-biz-device-result"</li>
     * </ul>
     * 
     * <p>配置示例：</p>
     * <pre>
     * yudao:
     *   iot:
     *     message-bus:
     *       consumer-group-prefix: dev-
     * </pre>
     */
    private String consumerGroupPrefix = "";

    /**
     * 获取带前缀的消费者组名
     * 
     * @param groupName 原始消费者组名
     * @return 带前缀的消费者组名
     */
    public String getPrefixedConsumerGroup(String groupName) {
        if (consumerGroupPrefix == null || consumerGroupPrefix.isEmpty()) {
            return groupName;
        }
        return consumerGroupPrefix + groupName;
    }

}