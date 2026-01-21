package cn.iocoder.yudao.module.iot.core.messagebus.core.local;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 本地的 {@link IotMessageBus} 实现类
 *
 * 注意：仅适用于单机场景！！！
 *
 * @author 长辉信息科技有限公司
 */
@RequiredArgsConstructor
@Slf4j
public class IotLocalMessageBus implements IotMessageBus {

    private final ApplicationContext applicationContext;

    /**
     * 订阅者映射表
     * Key: topic
     */
    private final Map<String, List<IotMessageSubscriber<?>>> subscribers = new HashMap<>();

    @Override
    public void post(String topic, Object message) {
        applicationContext.publishEvent(new IotLocalMessage(topic, message));
    }

    @Override
    public void register(IotMessageSubscriber<?> subscriber) {
        String topic = subscriber.getTopic();
        List<IotMessageSubscriber<?>> topicSubscribers = subscribers.computeIfAbsent(topic, k -> new ArrayList<>());
        topicSubscribers.add(subscriber);
        log.info("[register][topic({}/{}) 注册消费者({})成功]",
                topic, subscriber.getGroup(), subscriber.getClass().getName());
    }

    @EventListener
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onMessage(IotLocalMessage message) {
        String topic = message.getTopic();
        List<IotMessageSubscriber<?>> topicSubscribers = subscribers.get(topic);
        if (CollUtil.isEmpty(topicSubscribers)) {
            return;
        }
        Object payload = message.getMessage();
        Long tenantId = resolveTenantId(payload);
        for (IotMessageSubscriber subscriber : topicSubscribers) {
            try {
                runWithTenant(tenantId, () -> subscriber.onMessage(payload));
            } catch (Exception ex) {
                log.error("[onMessage][topic({}/{}) message({}) 消费者({}) 处理异常]",
                        subscriber.getTopic(), subscriber.getGroup(), payload, subscriber.getClass().getName(), ex);
            }
        }
    }

    private Long resolveTenantId(Object message) {
        if (message == null) {
            return null;
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