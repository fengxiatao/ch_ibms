package cn.iocoder.yudao.module.iot.newgateway.consumer;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageEnvelope;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.newgateway.core.executor.DeviceCommandExecutorService;
import cn.iocoder.yudao.module.iot.newgateway.core.handler.DeviceHandler;
import cn.iocoder.yudao.module.iot.newgateway.core.idempotent.CommandIdempotentService;
import cn.iocoder.yudao.module.iot.newgateway.core.model.CommandResult;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceCommand;
import cn.iocoder.yudao.module.iot.newgateway.core.registry.DevicePluginRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 设备控制命令消费者
 * <p>
 * 订阅 Biz 发送的设备控制命令（通过 IotMessageBus），根据设备类型路由到对应的插件处理器，
 * 并发布命令执行结果。
 * </p>
 *
 * <p>处理流程：</p>
 * <ol>
 *     <li>接收 {@link IotMessageTopics#DEVICE_SERVICE_INVOKE} 主题的消息</li>
 *     <li>过期检查：丢弃延迟过大的消息，避免处理过时命令</li>
 *     <li>幂等检查：防止消息重复消费</li>
 *     <li>解析消息中的设备信息和命令参数</li>
 *     <li>通过 {@link DevicePluginRegistry} 获取对应的设备处理器</li>
 *     <li>通过 {@link DeviceCommandExecutorService} 异步执行命令（线程池隔离）</li>
 *     <li>发布命令执行结果到 {@link IotMessageTopics#DEVICE_SERVICE_RESULT}</li>
 * </ol>
 *
 * <p>线程池隔离设计：</p>
 * <ul>
 *     <li>SDK 设备（门禁、NVR）：使用专用线程池（sdk-cmd），避免阻塞 RocketMQ 消费线程</li>
 *     <li>TCP 设备（报警、长辉）：使用通用线程池（dev-cmd），异步化执行流程</li>
 * </ul>
 *
 * <p>Requirements: 2.1, 4.1</p>
 *
 * @author IoT Gateway Team
 * @see DevicePluginRegistry
 * @see DeviceHandler
 * @see DeviceCommandExecutorService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceCommandConsumer implements IotMessageSubscriber<IotMessageEnvelope> {

    /**
     * 消息过期阈值（毫秒）
     * <p>
     * 超过此阈值的消息将被丢弃，默认 60 秒。
     * 设计考虑：
     * - 设备命令通常需要及时执行，延迟过大的命令已无意义
     * - 避免服务重启后大量积压消息导致刷屏和资源浪费
     * - 60 秒足够覆盖正常的网络延迟和消息队列处理时间
     * </p>
     */
    private static final long MESSAGE_EXPIRE_THRESHOLD_MS = 60_000L;

    /**
     * 过期消息统计计数器
     */
    private final AtomicLong expiredMessageCount = new AtomicLong(0);

    private final IotMessageBus messageBus;
    private final DevicePluginRegistry pluginRegistry;
    private final CommandIdempotentService idempotentService;
    private final DeviceCommandExecutorService commandExecutorService;

    /**
     * 初始化：注册到消息总线
     */
    @PostConstruct
    public void init() {
        String busType = messageBus.getClass().getSimpleName();
        log.info("========================================");
        log.info("[DeviceCommandConsumer] 🚀 正在初始化设备命令消费者...");
        log.info("[DeviceCommandConsumer] 消息总线类型: {}", busType);
        log.info("[DeviceCommandConsumer] 订阅主题: {}", getTopic());
        log.info("[DeviceCommandConsumer] 消费者组: {}", getGroup());
        
        // 检查是否使用了正确的消息总线
        if ("IotLocalMessageBus".equals(busType)) {
            log.error("========================================");
            log.error("[DeviceCommandConsumer] ⚠️ 严重错误：使用的是本地消息总线！");
            log.error("[DeviceCommandConsumer] 无法接收 Biz 模块发送的跨进程消息！");
            log.error("[DeviceCommandConsumer] 请检查配置 yudao.iot.message-bus.type 是否设置为 rocketmq");
            log.error("========================================");
        } else {
            log.info("[DeviceCommandConsumer] ✅ 使用 RocketMQ 消息总线，可接收跨进程消息");
        }
        
        messageBus.register(this);
        log.info("[DeviceCommandConsumer] ✅ 已成功注册到消息总线");
        log.info("========================================");
    }

    @Override
    public String getTopic() {
        return IotMessageTopics.DEVICE_SERVICE_INVOKE;
    }

    @Override
    public String getGroup() {
        return ConsumerConstants.CONSUMER_GROUP_DEVICE_COMMAND;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(IotMessageEnvelope envelope) {
        if (envelope == null || envelope.getPayload() == null) {
            log.warn("[DeviceCommandConsumer] 收到空消息，忽略");
            return;
        }

        // 1. 过期检查：丢弃延迟过大的消息
        long messageAge = System.currentTimeMillis() - (envelope.getOccurredAt() != null ? envelope.getOccurredAt() : System.currentTimeMillis());
        if (messageAge > MESSAGE_EXPIRE_THRESHOLD_MS) {
            long expiredCount = expiredMessageCount.incrementAndGet();
            // 每 100 条过期消息输出一次汇总日志，避免刷屏
            if (expiredCount % 100 == 1) {
                log.warn("[DeviceCommandConsumer] 丢弃过期消息: deviceId={}, requestId={}, 延迟={}ms (阈值={}ms), 累计丢弃={}",
                        envelope.getDeviceId(), envelope.getRequestId(), messageAge, MESSAGE_EXPIRE_THRESHOLD_MS, expiredCount);
            }
            return;
        }

        // 2. 解析 payload 为 IotDeviceMessage
        IotDeviceMessage message = parsePayload(envelope.getPayload());
        if (message == null) {
            log.warn("[DeviceCommandConsumer] 解析 payload 失败: envelope={}", envelope);
            return;
        }

        Long deviceId = message.getDeviceId();
        String requestId = message.getRequestId();
        String method = message.getMethod();

        log.info("[DeviceCommandConsumer] 收到设备控制命令: deviceId={}, requestId={}, method={}, params={}, 延迟={}ms",
                deviceId, requestId, method, message.getParams(), messageAge);

        // 3. 幂等检查：防止消息重复消费
        if (!idempotentService.tryAcquire(requestId)) {
            log.warn("[DeviceCommandConsumer] 重复命令，忽略: requestId={}, deviceId={}", requestId, deviceId);
            return;
        }

        try {
            // 解析消息参数
            Map<String, Object> params = parseParams(message);
            
            // 获取设备类型
            String deviceType = getStringParam(params, "deviceType");
            if (deviceType == null) {
                deviceType = getStringParam(params, "model");
            }

            // 获取命令类型
            String commandType = getStringParam(params, "commandType");
            if (commandType == null) {
                commandType = method; // 使用 method 作为命令类型
            }

            log.debug("[DeviceCommandConsumer] 解析命令参数: deviceId={}, deviceType={}, commandType={}",
                    deviceId, deviceType, commandType);

            // 验证必要参数
            if (deviceType == null || deviceType.trim().isEmpty()) {
                log.error("[DeviceCommandConsumer] 设备类型为空: deviceId={}", deviceId);
                publishCommandResult(requestId, deviceId, method, params, CommandResult.failure("设备类型为空"));
                return;
            }

            if (commandType == null || commandType.trim().isEmpty()) {
                log.error("[DeviceCommandConsumer] 命令类型为空: deviceId={}", deviceId);
                publishCommandResult(requestId, deviceId, method, params, CommandResult.failure("命令类型为空"));
                return;
            }

            // 获取设备处理器
            DeviceHandler handler = pluginRegistry.getHandler(deviceType);
            if (handler == null) {
                log.error("[DeviceCommandConsumer] 未找到设备处理器: deviceId={}, deviceType={}", deviceId, deviceType);
                publishCommandResult(requestId, deviceId, method, params, CommandResult.failure("未找到设备处理器: " + deviceType));
                return;
            }

            // 构建设备命令
            DeviceCommand command = DeviceCommand.builder()
                    .commandType(commandType)
                    .params(params)
                    .build();

            // 异步执行命令（线程池隔离）
            // SDK 设备（门禁、NVR）使用专用线程池，避免阻塞 RocketMQ 消费线程
            // TCP 设备（报警、长辉）使用通用线程池
            commandExecutorService.executeAsync(
                    handler,
                    deviceId,
                    deviceType,
                    command,
                    requestId,
                    method,
                    params
            );

            log.debug("[DeviceCommandConsumer] 命令已提交异步执行: deviceId={}, deviceType={}, commandType={}",
                    deviceId, deviceType, commandType);

        } catch (Exception e) {
            log.error("[DeviceCommandConsumer] 命令处理异常: deviceId={}", deviceId, e);
            publishCommandResult(requestId, deviceId, method, parseParams(message),
                    CommandResult.failure("命令处理异常: " + e.getMessage()));
        }
    }

    /**
     * 解析 Envelope 中的 payload 为 IotDeviceMessage
     */
    @SuppressWarnings("unchecked")
    private IotDeviceMessage parsePayload(Object payload) {
        if (payload instanceof IotDeviceMessage) {
            return (IotDeviceMessage) payload;
        }
        if (payload instanceof Map) {
            // payload 可能是 LinkedHashMap，需要转换
            try {
                Map<String, Object> map = (Map<String, Object>) payload;
                IotDeviceMessage message = new IotDeviceMessage();
                message.setDeviceId(extractLong(map, "deviceId"));
                message.setRequestId(extractString(map, "requestId"));
                message.setMethod(extractString(map, "method"));
                message.setParams(map.get("params"));
                message.setData(map.get("data"));
                message.setCode(extractInteger(map, "code"));
                message.setMsg(extractString(map, "msg"));
                return message;
            } catch (Exception e) {
                log.warn("[DeviceCommandConsumer] 解析 Map payload 失败: {}", e.getMessage());
                return null;
            }
        }
        log.warn("[DeviceCommandConsumer] 未知的 payload 类型: {}", payload.getClass().getName());
        return null;
    }

    /**
     * 从 Map 中提取 Long 值
     */
    private Long extractLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * 从 Map 中提取 Integer 值
     */
    private Integer extractInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * 从 Map 中提取 String 值（安全转换，避免 Integer -> String 强转异常）
     */
    private String extractString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 解析消息参数
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseParams(IotDeviceMessage message) {
        Object params = message.getParams();
        if (params instanceof Map) {
            return (Map<String, Object>) params;
        }
        return new HashMap<>();
    }

    /**
     * 获取字符串参数
     */
    private String getStringParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 发布命令执行结果
     */
    private void publishCommandResult(String requestId, Long deviceId, String method,
                                      Map<String, Object> params, CommandResult result) {
        try {
            // 将请求的 method/params 一并回传，便于 biz 侧提取 deviceType、serviceIdentifier 等信息
            // 否则 biz 侧会出现 deviceType=UNKNOWN、无法做通道同步等问题
            IotDeviceMessage resultMessage = IotDeviceMessage.builder()
                    .requestId(requestId)
                    .deviceId(deviceId)
                    .method(method)
                    .params(params)
                    .code(result.isSuccess() ? 0 : -1)
                    .msg(result.getMessage())
                    .data(result.getData())
                    .build();

            messageBus.post(IotMessageTopics.DEVICE_SERVICE_RESULT, resultMessage);
            log.debug("[DeviceCommandConsumer] 命令结果已发布: deviceId={}, success={}", deviceId, result.isSuccess());
        } catch (Exception e) {
            log.error("[DeviceCommandConsumer] 发布命令结果失败: deviceId={}", deviceId, e);
        }
    }

    /**
     * 获取过期消息统计数（用于监控）
     *
     * @return 累计丢弃的过期消息数量
     */
    public long getExpiredMessageCount() {
        return expiredMessageCount.get();
    }

    /**
     * 重置过期消息统计数
     */
    public void resetExpiredMessageCount() {
        expiredMessageCount.set(0);
        log.info("[DeviceCommandConsumer] 过期消息统计已重置");
    }
}
