package cn.iocoder.yudao.module.iot.core.messagebus.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * IoT 消息信封（Envelope）
 *
 * 用于在消息总线（RocketMQ/Redis Stream）中为所有 Topic 提供统一的元数据字段：
 * - schemaVersion/messageId/eventId/requestId/occurredAt/traceId
 * - tenantId/deviceId/productId/deviceType/vendor
 *
 * payload 保持原有消息体，消费者侧默认会自动解包为 payload。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotMessageEnvelope {

    public static final String DEFAULT_SCHEMA_VERSION = "1.0";

    private String schemaVersion;

    /** 消息唯一ID（总线级） */
    private String messageId;

    /** 事件ID（幂等键）；无专用字段时默认等于 messageId */
    private String eventId;

    /** 请求ID（命令/请求-响应关联） */
    private String requestId;

    /** 发生时间（epoch milli） */
    private Long occurredAt;

    /** 链路追踪ID */
    private String traceId;

    /** Topic（方便排障/回溯） */
    private String topic;

    /** payload Java 类名（方便排障/回溯） */
    private String payloadType;

    // ========== 常用路由/上下文字段 ==========

    private Long tenantId;
    private Long deviceId;
    private Long productId;
    private String deviceType;
    private String vendor;

    // ========== 业务载荷 ==========

    private Object payload;

    public static IotMessageEnvelope wrap(String topic, Object payload) {
        if (payload instanceof IotMessageEnvelope) {
            return (IotMessageEnvelope) payload;
        }

        String messageId = UUID.randomUUID().toString();
        Long occurredAt = extractOccurredAt(payload);

        IotMessageEnvelope.IotMessageEnvelopeBuilder builder = IotMessageEnvelope.builder()
                .schemaVersion(DEFAULT_SCHEMA_VERSION)
                .messageId(messageId)
                .traceId(UUID.randomUUID().toString())
                .topic(topic)
                .payloadType(payload != null ? payload.getClass().getName() : null)
                .occurredAt(occurredAt)
                .payload(payload);

        // 尽量从 payload 上提取常见字段（按约定 getter）
        builder.tenantId(extractLong(payload, "getTenantId"));
        builder.deviceId(extractLong(payload, "getDeviceId"));
        builder.productId(extractLong(payload, "getProductId"));
        builder.deviceType(extractString(payload, "getDeviceType"));
        builder.vendor(extractString(payload, "getVendor"));

        String requestId = extractString(payload, "getRequestId");
        builder.requestId(requestId);

        // eventId 优先用 payload.getEventId / payload.getId，否则回落 messageId
        String eventId = extractString(payload, "getEventId");
        if (eventId == null || eventId.isEmpty()) {
            eventId = extractString(payload, "getId");
        }
        builder.eventId((eventId == null || eventId.isEmpty()) ? messageId : eventId);

        return builder.build();
    }

    private static Long extractOccurredAt(Object payload) {
        // 优先取 payload.getTimestamp()（millis）
        Long ts = extractLong(payload, "getTimestamp");
        if (ts != null) {
            return ts;
        }
        // 其次取 payload.getReportTime()（LocalDateTime）
        Object reportTime = extractObject(payload, "getReportTime");
        if (reportTime instanceof LocalDateTime) {
            return ((LocalDateTime) reportTime)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        }
        return System.currentTimeMillis();
    }

    private static Long extractLong(Object target, String method) {
        Object v = extractObject(target, method);
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        if (v instanceof String) {
            try {
                return Long.parseLong((String) v);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static String extractString(Object target, String method) {
        Object v = extractObject(target, method);
        return v != null ? String.valueOf(v) : null;
    }

    private static Object extractObject(Object target, String method) {
        if (target == null) {
            return null;
        }
        try {
            return target.getClass().getMethod(method).invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }
}
