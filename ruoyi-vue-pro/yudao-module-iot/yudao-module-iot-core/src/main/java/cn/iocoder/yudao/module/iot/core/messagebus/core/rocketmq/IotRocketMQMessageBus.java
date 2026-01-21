package cn.iocoder.yudao.module.iot.core.messagebus.core.rocketmq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.messagebus.config.IotMessageBusProperties;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageEnvelope;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * 基于 RocketMQ 的 {@link IotMessageBus} 实现类
 *
 * 统一对外发送 {@link IotMessageEnvelope}，消费端默认自动解包为 payload。
 */
@RequiredArgsConstructor
@Slf4j
public class IotRocketMQMessageBus implements IotMessageBus {

    private final IotMessageBusProperties messageBusProperties;

    private final RocketMQProperties rocketMQProperties;

    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 主题对应的消费者映射
     */
    private final List<DefaultMQPushConsumer> topicConsumers = new ArrayList<>();

    /**
     * 销毁时关闭所有消费者
     */
    @PreDestroy
    public void destroy() {
        for (DefaultMQPushConsumer consumer : topicConsumers) {
            try {
                consumer.shutdown();
                log.info("[destroy][关闭 group({}) 的消费者成功]", consumer.getConsumerGroup());
            } catch (Exception e) {
                log.error("[destroy]关闭 group({}) 的消费者异常]", consumer.getConsumerGroup(), e);
            }
        }
    }

    @Override
    public void post(String topic, Object message) {
        Object sendMessage = (message instanceof IotMessageEnvelope)
                ? message
                : IotMessageEnvelope.wrap(topic, message);
        // TODO @长辉开发团队：需要 orderly！
        SendResult result = rocketMQTemplate.syncSend(topic, JsonUtils.toJsonString(sendMessage));
        log.info("[post][topic({}) 发送消息({}) result({})]", topic, sendMessage, result);
    }

    @Override
    @SneakyThrows
    public void register(IotMessageSubscriber<?> subscriber) {
        Type type = TypeUtil.getTypeArgument(subscriber.getClass(), 0);
        if (type == null) {
            throw new IllegalStateException(String.format("类型(%s) 需要设置消息类型", getClass().getName()));
        }

        // 1.1 创建 DefaultMQPushConsumer
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
        // 优先使用 IoT 消息总线配置的 nameServer，如果为空则 fallback 到 RocketMQ 配置
        String nameServer = StrUtil.blankToDefault(messageBusProperties.getNameServer(),
                rocketMQProperties.getNameServer());
        if (StrUtil.isBlank(nameServer)) {
            throw new IllegalStateException("RocketMQ NameServer 地址未配置！请在 yudao.iot.message-bus.name-server 或 rocketmq.name-server 中配置");
        }
        consumer.setNamesrvAddr(nameServer);
        log.info("[register][使用 RocketMQ NameServer: {}]", nameServer);
        
        // 使用带前缀的消费者组名（区分不同环境）
        String consumerGroup = messageBusProperties.getPrefixedConsumerGroup(subscriber.getGroup());
        consumer.setConsumerGroup(consumerGroup);
        log.info("[register][消费者组名: {} (原始: {})]", consumerGroup, subscriber.getGroup());
        
        // 【关键配置1】设置消费起点
        // - CONSUME_FROM_FIRST_OFFSET: 从队列最早消息开始消费（新消费者组首次启动时）
        // - 已存在的消费者组会从上次消费位置继续，不受此配置影响
        // - 避免服务重启期间发送的消息被跳过
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        
        // 【关键配置2】设置实例名称，避免同一机器多实例时冲突
        // 使用带前缀的 group + UUID 确保唯一性
        String instanceName = consumerGroup + "_" + UUID.randomUUID().toString().substring(0, 8);
        consumer.setInstanceName(instanceName);
        
        // 【关键配置3】消费超时时间（分钟），默认 15 分钟
        consumer.setConsumeTimeout(15);
        
        // 【关键配置4】最大重试次数，超过后进入死信队列
        consumer.setMaxReconsumeTimes(16);
        
        // 【关键配置5】消费线程数配置
        consumer.setConsumeThreadMin(4);
        consumer.setConsumeThreadMax(16);
        
        // 【关键配置6】拉取消息批次大小
        consumer.setPullBatchSize(32);
        
        // 【关键配置7】提高消费实时性的关键配置
        // 拉取间隔设置为0，表示立即拉取下一批消息
        consumer.setPullInterval(0);
        // 每次拉取的消息数量（单个队列）
        consumer.setConsumeMessageBatchMaxSize(1);
        // 拉取线程数（增加并行度）
        consumer.setPullThresholdForQueue(1000);
        
        // 1.2 订阅主题
        consumer.subscribe(subscriber.getTopic(), "*");
        
        // 1.3 设置消息监听器
        consumer.setMessageListener((MessageListenerConcurrently) (messages, context) -> {
            for (MessageExt messageExt : messages) {
                try {
                    // 计算消息延迟（从发送到消费的时间差）
                    long delay = System.currentTimeMillis() - messageExt.getBornTimestamp();
                    log.info("[onMessage][topic({}/{}) msgId({}) 开始处理消息, 消息延迟={}ms, queueId={}, reconsumeTimes={}]",
                            subscriber.getTopic(), subscriber.getGroup(), messageExt.getMsgId(), 
                            delay, messageExt.getQueueId(), messageExt.getReconsumeTimes());
                    
                    Object parsed = parseMessage(messageExt.getBody(), type);
                    Long tenantId = resolveTenantId(parsed);
                    runWithTenant(tenantId, () -> subscriber.onMessage(castToSubscriberType(parsed)));
                    
                    log.debug("[onMessage][topic({}/{}) msgId({}) 消息处理完成]",
                            subscriber.getTopic(), subscriber.getGroup(), messageExt.getMsgId());
                } catch (Exception ex) {
                    log.error("[onMessage][topic({}/{}) msgId({}) 消费者({}) 处理异常, reconsumeTimes={}]",
                            subscriber.getTopic(), subscriber.getGroup(), messageExt.getMsgId(),
                            subscriber.getClass().getName(), messageExt.getReconsumeTimes(), ex);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        // 1.4 启动消费者
        consumer.start();
        
        // 打印消费者队列分配信息（帮助诊断队列分配问题）
        try {
            var allocatedQueues = consumer.fetchSubscribeMessageQueues(subscriber.getTopic());
            log.info("[register][消费者队列分配] topic={}, group={}, 分配队列数={}, 队列列表={}", 
                    subscriber.getTopic(), consumerGroup, 
                    allocatedQueues != null ? allocatedQueues.size() : 0, allocatedQueues);
        } catch (Exception e) {
            log.warn("[register][获取队列分配信息失败] topic={}, error={}", subscriber.getTopic(), e.getMessage());
        }
        
        log.info("[register][消费者已启动] topic={}, group={}, instanceName={}, consumeFromWhere={}",
                subscriber.getTopic(), consumerGroup, instanceName, ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        // 2. 保存消费者引用
        topicConsumers.add(consumer);
    }

    private <T> T castToSubscriberType(Object value) {
        return (T) value;
    }

    private Object parseMessage(byte[] body, Type subscriberType) {
        // 若订阅者显式订阅 Envelope，则原样返回
        if (isEnvelopeType(subscriberType)) {
            return JsonUtils.parseObject(body, subscriberType);
        }

        // 默认：识别 Envelope 并解包 payload
        JsonNode root = JsonUtils.parseObject(body, JsonNode.class);
        if (root != null && root.hasNonNull("schemaVersion") && root.has("payload")) {
            JsonNode payload = root.get("payload");
            if (payload == null || payload.isNull()) {
                return null;
            }
            return JsonUtils.parseObject(payload.toString(), subscriberType);
        }

        // 兼容旧消息（未封装 Envelope）
        return JsonUtils.parseObject(body, subscriberType);
    }

    private boolean isEnvelopeType(Type type) {
        if (type == IotMessageEnvelope.class) {
            return true;
        }
        if (type instanceof ParameterizedType) {
            Type raw = ((ParameterizedType) type).getRawType();
            return raw == IotMessageEnvelope.class;
        }
        return false;
    }

    private Long resolveTenantId(Object message) {
        if (message == null) {
            return null;
        }
        if (message instanceof IotMessageEnvelope) {
            return ((IotMessageEnvelope) message).getTenantId();
        }
        try {
            Method m = message.getClass().getMethod("getTenantId");
            Object v = m.invoke(message);
            if (v instanceof Number) {
                return ((Number) v).longValue();
            }
            if (v instanceof String) {
                try {
                    return Long.parseLong((String) v);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void runWithTenant(Long tenantId, Runnable runnable) {
        try {
            Class<?> tenantUtils = Class.forName("cn.iocoder.yudao.framework.tenant.core.util.TenantUtils");
            if (tenantId != null) {
                Method execute = tenantUtils.getMethod("execute", Long.class, Runnable.class);
                execute.invoke(null, tenantId, runnable);
            } else {
                Method executeIgnore = tenantUtils.getMethod("executeIgnore", Runnable.class);
                executeIgnore.invoke(null, runnable);
            }
        } catch (ClassNotFoundException e) {
            runnable.run();
        } catch (Exception e) {
            runnable.run();
        }
    }

}

