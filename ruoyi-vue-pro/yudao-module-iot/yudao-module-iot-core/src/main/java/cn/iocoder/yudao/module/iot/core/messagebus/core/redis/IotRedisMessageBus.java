package cn.iocoder.yudao.module.iot.core.messagebus.core.redis;

import cn.hutool.core.util.TypeUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageEnvelope;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import com.fasterxml.jackson.databind.JsonNode;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


import static cn.iocoder.yudao.framework.mq.redis.config.YudaoRedisMQConsumerAutoConfiguration.buildConsumerName;
import static cn.iocoder.yudao.framework.mq.redis.config.YudaoRedisMQConsumerAutoConfiguration.checkRedisVersion;

/**
 * Redis 的 {@link IotMessageBus} 实现类
 *
 * @author 长辉信息科技有限公司
 */
@Slf4j
public class IotRedisMessageBus implements IotMessageBus {

    private Object parseMessage(String json, Type subscriberType) {
        // 若订阅者显式订阅 Envelope，则原样返回
        if (isEnvelopeType(subscriberType)) {
            return JsonUtils.parseObject(json, subscriberType);
        }

        JsonNode root = JsonUtils.parseObject(json, JsonNode.class);
        if (root != null && root.hasNonNull("schemaVersion") && root.has("payload")) {
            JsonNode payload = root.get("payload");
            if (payload == null || payload.isNull()) {
                return null;
            }
            return JsonUtils.parseObject(payload.toString(), subscriberType);
        }

        // 兼容旧消息（未封装 Envelope）
        return JsonUtils.parseObject(json, subscriberType);
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

    private final RedisTemplate<String, ?> redisTemplate;

    private final StreamMessageListenerContainer<String, ObjectRecord<String, String>> redisStreamMessageListenerContainer;

    @Getter
    private final List<IotMessageSubscriber<?>> subscribers = new ArrayList<>();

    public IotRedisMessageBus(RedisTemplate<String, ?> redisTemplate) {
        this.redisTemplate = redisTemplate;
        checkRedisVersion(redisTemplate);
        // 创建 options 配置
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> containerOptions =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .batchSize(10) // 一次性最多拉取多少条消息
                        .targetType(String.class) // 目标类型。统一使用 String，通过自己封装的 AbstractStreamMessageListener 去反序列化
                        .build();
        // 创建 container 对象
        this.redisStreamMessageListenerContainer =
                StreamMessageListenerContainer.create(redisTemplate.getRequiredConnectionFactory(), containerOptions);
    }

    @PostConstruct
    public void init() {
        this.redisStreamMessageListenerContainer.start();
    }

    @PreDestroy
    public void destroy() {
        this.redisStreamMessageListenerContainer.stop();
    }

    @Override
    public void post(String topic, Object message) {
        Object sendMessage = (message instanceof cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageEnvelope)
                ? message
                : cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageEnvelope.wrap(topic, message);
        redisTemplate.opsForStream().add(StreamRecords.newRecord()
                .ofObject(JsonUtils.toJsonString(sendMessage)) // 设置内容
                .withStreamKey(topic)); // 设置 stream key
    }

    @Override
    public void register(IotMessageSubscriber<?> subscriber) {
        Type type = TypeUtil.getTypeArgument(subscriber.getClass(), 0);
        if (type == null) {
            throw new IllegalStateException(String.format("类型(%s) 需要设置消息类型", getClass().getName()));
        }

        // 创建 listener 对应的消费者分组
        try {
            redisTemplate.opsForStream().createGroup(subscriber.getTopic(), subscriber.getGroup());
        } catch (Exception ignore) {
        }
        // 创建 Consumer 对象
        String consumerName = buildConsumerName();
        Consumer consumer = Consumer.from(subscriber.getGroup(), consumerName);
        // 设置 Consumer 消费进度，以最小消费进度为准
        StreamOffset<String> streamOffset = StreamOffset.create(subscriber.getTopic(), ReadOffset.lastConsumed());
        // 设置 Consumer 监听
        StreamMessageListenerContainer.StreamReadRequestBuilder<String> builder = StreamMessageListenerContainer.StreamReadRequest
                .builder(streamOffset).consumer(consumer)
                .autoAcknowledge(false) // 不自动 ack
                .cancelOnError(throwable -> false); // 默认配置，发生异常就取消消费，显然不符合预期；因此，我们设置为 false
        redisStreamMessageListenerContainer.register(builder.build(), message -> {
            // 消费消息（默认识别 Envelope 并自动解包为 payload）
            Object parsed = parseMessage(message.getValue(), type);
            Long tenantId = resolveTenantId(parsed);
            runWithTenant(tenantId, () -> subscriber.onMessage(castToSubscriberType(parsed)));
            // ack 消息消费完成
            redisTemplate.opsForStream().acknowledge(subscriber.getGroup(), message);
        });

        this.subscribers.add(subscriber);
    }

    private <T> T castToSubscriberType(Object value) {
        return (T) value;
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

