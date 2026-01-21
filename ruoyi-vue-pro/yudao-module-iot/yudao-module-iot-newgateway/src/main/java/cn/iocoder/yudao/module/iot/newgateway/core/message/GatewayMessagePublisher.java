package cn.iocoder.yudao.module.iot.newgateway.core.message;

import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceStateChangeMessage;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.GatewayEventDTO;
import cn.iocoder.yudao.module.iot.newgateway.core.model.CommandResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 网关消息发布器
 * 
 * <p>统一的消息发布入口，所有设备插件通过此组件发布消息到消息总线。</p>
 * 
 * <p>提供三种消息发布能力：</p>
 * <ul>
 *   <li>{@link #publishStateChange(DeviceStateChangeMessage)} - 发布设备状态变更消息</li>
 *   <li>{@link #publishEvent(String, GatewayEventDTO)} - 发布设备事件消息（强类型 DTO）</li>
 *   <li>{@link #publishCommandResult(String, CommandResult)} - 发布命令执行结果</li>
 * </ul>
 * 
 * <p>复用 iot-core 中的公共类：</p>
 * <ul>
 *   <li>DeviceStateChangeMessage - 设备状态变更消息</li>
 *   <li>IotMessageTopics - 消息主题常量</li>
 *   <li>IotMessageBus - 消息总线接口</li>
 * </ul>
 * 
 * <p><b>强类型约束：</b></p>
 * <ul>
 *   <li>事件消息必须实现 {@link GatewayEventDTO} 接口</li>
 *   <li>禁止使用 Map 类型发布事件消息</li>
 *   <li>运行时会验证消息类型，拒绝 Map 类型</li>
 * </ul>
 * 
 * <p>Requirements: 2.1, 2.3, 4.1, 4.2</p>
 *
 * @author 长辉信息科技有限公司
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayMessagePublisher {

    /**
     * 消息总线（复用 iot-core）
     */
    private final IotMessageBus messageBus;

    /**
     * 发布设备状态变更消息
     * 
     * <p>使用 iot-core 中的 DeviceStateChangeMessage 和 IotMessageTopics.DEVICE_STATE_CHANGED</p>
     * 
     * @param message 状态变更消息
     */
    public void publishStateChange(DeviceStateChangeMessage message) {
        if (message == null) {
            log.warn("[GatewayMessagePublisher] 无效的状态变更消息: message is null");
            return;
        }
        
        if (message.getDeviceId() == null) {
            log.warn("[GatewayMessagePublisher] 无效的状态变更消息: deviceId is null");
            return;
        }

        try {
            // 复用 iot-core 中定义的主题
            messageBus.post(IotMessageTopics.DEVICE_STATE_CHANGED, message);
            
            log.info("[GatewayMessagePublisher] 状态变更消息已发布: deviceId={}, previousState={}, newState={}, reason={}",
                    message.getDeviceId(),
                    message.getPreviousStateName(),
                    message.getNewStateName(),
                    message.getReason());
        } catch (Exception e) {
            log.error("[GatewayMessagePublisher] 发布状态变更消息失败: deviceId={}, error={}",
                    message.getDeviceId(), e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 发布设备事件消息（强类型 DTO）
     * 
     * <p>用于发布各类设备事件，如报警事件、门禁事件等。</p>
     * <p><b>注意：</b>事件消息必须实现 {@link GatewayEventDTO} 接口，禁止使用 Map 类型。</p>
     * 
     * @param topic 消息主题（建议使用 IotMessageTopics 中定义的常量）
     * @param event 事件消息对象（必须实现 GatewayEventDTO 接口）
     */
    public void publishEvent(String topic, GatewayEventDTO event) {
        if (topic == null || topic.isEmpty()) {
            log.warn("[GatewayMessagePublisher] 无效的事件消息: topic is null or empty");
            return;
        }
        
        if (event == null) {
            log.warn("[GatewayMessagePublisher] 无效的事件消息: event is null, topic={}", topic);
            return;
        }

        try {
            messageBus.post(topic, event);
            log.debug("[GatewayMessagePublisher] 事件消息已发布: topic={}, eventType={}, deviceId={}, deviceType={}",
                    topic, event.getClass().getSimpleName(), event.getDeviceId(), event.getDeviceType());
        } catch (Exception e) {
            log.error("[GatewayMessagePublisher] 发布事件消息失败: topic={}, error={}",
                    topic, e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 发布设备事件消息（带运行时类型检查）
     * 
     * <p>此方法接受 Object 类型，但会在运行时检查类型。</p>
     * <p><b>警告：</b>如果传入 Map 类型，将抛出 IllegalArgumentException。</p>
     * <p><b>建议：</b>请迁移到 {@link #publishEvent(String, GatewayEventDTO)} 方法。</p>
     * 
     * @param topic 消息主题
     * @param event 事件消息对象（禁止 Map 类型）
     * @throws IllegalArgumentException 如果 event 是 Map 类型
     */
    public void publishEvent(String topic, Object event) {
        if (topic == null || topic.isEmpty()) {
            log.warn("[GatewayMessagePublisher] 无效的事件消息: topic is null or empty");
            return;
        }
        
        if (event == null) {
            log.warn("[GatewayMessagePublisher] 无效的事件消息: event is null, topic={}", topic);
            return;
        }
        
        // 运行时类型检查：禁止 Map 类型
        validateNotMapType(event, "event");

        try {
            messageBus.post(topic, event);
            log.debug("[GatewayMessagePublisher] 事件消息已发布: topic={}, eventType={}",
                    topic, event.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("[GatewayMessagePublisher] 发布事件消息失败: topic={}, error={}",
                    topic, e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 发布命令执行结果
     * 
     * <p>用于发布设备命令执行的结果，供业务层消费。</p>
     * 
     * @param topic  消息主题（建议使用 IotMessageTopics 中定义的常量）
     * @param result 命令执行结果
     */
    public void publishCommandResult(String topic, CommandResult result) {
        if (topic == null || topic.isEmpty()) {
            log.warn("[GatewayMessagePublisher] 无效的命令响应消息: topic is null or empty");
            return;
        }
        
        if (result == null) {
            log.warn("[GatewayMessagePublisher] 无效的命令响应消息: result is null, topic={}", topic);
            return;
        }

        try {
            messageBus.post(topic, result);
            log.debug("[GatewayMessagePublisher] 命令响应已发布: topic={}, success={}",
                    topic, result.isSuccess());
        } catch (Exception e) {
            log.error("[GatewayMessagePublisher] 发布命令响应失败: topic={}, error={}",
                    topic, e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 发布通用消息
     * 
     * <p>用于发布任意类型的消息到指定主题。</p>
     * <p><b>警告：</b>如果传入 Map 类型，将抛出 IllegalArgumentException。</p>
     * 
     * @param topic   消息主题
     * @param message 消息对象
     * @throws IllegalArgumentException 如果 message 是 Map 类型
     */
    public void publish(String topic, Object message) {
        if (topic == null || topic.isEmpty()) {
            log.warn("[GatewayMessagePublisher] 无效的消息: topic is null or empty");
            return;
        }
        
        if (message == null) {
            log.warn("[GatewayMessagePublisher] 无效的消息: message is null, topic={}", topic);
            return;
        }
        
        // 运行时类型检查：禁止 Map 类型
        validateNotMapType(message, "message");

        try {
            messageBus.post(topic, message);
            log.debug("[GatewayMessagePublisher] 消息已发布: topic={}, messageType={}",
                    topic, message.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("[GatewayMessagePublisher] 发布消息失败: topic={}, error={}",
                    topic, e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 验证消息不是 Map 类型
     * 
     * <p>根据 Requirements 4.1, 4.2，禁止使用 Map 类型发布消息。</p>
     * 
     * @param obj       要验证的对象
     * @param paramName 参数名称（用于错误消息）
     * @throws IllegalArgumentException 如果对象是 Map 类型
     */
    private void validateNotMapType(Object obj, String paramName) {
        if (obj instanceof Map) {
            String errorMsg = String.format(
                "[GatewayMessagePublisher] 禁止使用 Map 类型发布消息: %s 类型为 %s。" +
                "请使用强类型 DTO（实现 GatewayEventDTO 接口）。Requirements: 4.1, 4.2",
                paramName, obj.getClass().getName()
            );
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }
}
