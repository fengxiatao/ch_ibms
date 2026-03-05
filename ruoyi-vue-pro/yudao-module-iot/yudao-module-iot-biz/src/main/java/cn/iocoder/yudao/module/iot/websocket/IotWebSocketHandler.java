package cn.iocoder.yudao.module.iot.websocket;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.websocket.message.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT WebSocket 处理器
 *
 * 职责：
 * - 管理用户 WebSocket 连接（Session 管理）
 * - 处理客户端消息（心跳、订阅请求等）
 * - 推送消息给客户端（设备状态、告警事件、统计数据）
 *
 * @author 芋道源码
 */
@Slf4j
@Component("iotWebSocketHandler")  // 明确指定 Bean 名称，避免与框架的 webSocketHandler 冲突
public class IotWebSocketHandler extends TextWebSocketHandler {

    /**
     * 用户连接映射（userId -> WebSocketSession）
     */
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * Session 到 UserId 的反向映射（用于快速查找）
     */
    private final Map<String, Long> sessionToUserId = new ConcurrentHashMap<>();

    /**
     * Session 写锁映射（用于保证每个 Session 的写入操作线程安全）
     * <p>
     * WebSocketSession.sendMessage() 不是线程安全的，
     * 多线程并发写入会导致 IllegalStateException: TEXT_PARTIAL_WRITING
     * </p>
     */
    private final Map<String, Object> sessionWriteLocks = new ConcurrentHashMap<>();

    /**
     * 连接建立后的处理
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            // 从 URL 参数获取 userId
            Long userId = extractUserId(session);
            if (userId == null) {
                log.warn("[IoT WebSocket] ⚠️ 连接缺少 userId 参数，拒绝连接: {}", session.getId());
                session.close(CloseStatus.BAD_DATA.withReason("Missing userId parameter"));
                return;
            }

            // 如果用户已有连接，先关闭旧连接
            WebSocketSession oldSession = sessions.get(userId);
            if (oldSession != null && oldSession.isOpen()) {
                log.info("[IoT WebSocket] 🔄 用户 {} 重复连接，关闭旧连接", userId);
                oldSession.close(CloseStatus.NORMAL.withReason("New connection established"));
            }

            // 保存新连接
            sessions.put(userId, session);
            sessionToUserId.put(session.getId(), userId);
            sessionWriteLocks.put(session.getId(), new Object());  // 创建写锁

            log.info("[IoT WebSocket] ✅ 用户 {} 连接成功，sessionId={}, 当前在线用户数: {}", 
                    userId, session.getId(), sessions.size());

            // 发送连接确认消息
            sendMessage(session, IotMessage.connected());

        } catch (Exception e) {
            log.error("[IoT WebSocket] ❌ 处理连接建立失败: {}", e.getMessage(), e);
            session.close(CloseStatus.SERVER_ERROR.withReason("Connection establishment failed"));
        }
    }

    /**
     * 接收文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            log.debug("[IoT WebSocket] 📨 收到消息: sessionId={}, payload={}", session.getId(), payload);

            // 解析消息
            IotMessage iotMessage = JSONUtil.toBean(payload, IotMessage.class);

            // 处理不同类型的消息
            switch (iotMessage.getType()) {
                case "ping":
                    // 心跳请求，响应 pong
                    sendMessage(session, IotMessage.pong());
                    log.debug("[IoT WebSocket] 💓 心跳响应: sessionId={}", session.getId());
                    break;

                default:
                    log.warn("[IoT WebSocket] ⚠️ 未知消息类型: type={}, sessionId={}", 
                            iotMessage.getType(), session.getId());
            }

        } catch (Exception e) {
            log.error("[IoT WebSocket] ❌ 处理消息失败: sessionId={}, error={}", 
                    session.getId(), e.getMessage(), e);
        }
    }

    /**
     * 连接关闭后的处理
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = sessionToUserId.remove(session.getId());
        sessionWriteLocks.remove(session.getId());  // 移除写锁
        if (userId != null) {
            sessions.remove(userId);
            log.info("[IoT WebSocket] 🔌 用户 {} 断开连接，sessionId={}, 原因={}, 当前在线用户数: {}", 
                    userId, session.getId(), status, sessions.size());
        } else {
            log.warn("[IoT WebSocket] ⚠️ 未找到 sessionId 对应的用户: {}", session.getId());
        }
    }

    /**
     * 传输错误处理
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = sessionToUserId.get(session.getId());
        log.error("[IoT WebSocket] ❌ 传输错误: userId={}, sessionId={}, error={}", 
                userId, session.getId(), exception.getMessage(), exception);

        // 关闭出错的连接
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR.withReason("Transport error"));
        }
    }

    // ============= 消息推送方法 =============

    /**
     * 广播消息（所有在线用户）
     */
    public void broadcast(IotMessage message) {
        log.info("[IoT WebSocket] 📢 广播消息: type={}, 在线用户数={}", message.getType(), sessions.size());
        sessions.values().forEach(session -> sendMessage(session, message));
    }

    /**
     * 单播消息（指定用户）
     */
    public void sendToUser(Long userId, IotMessage message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
            log.debug("[IoT WebSocket] 📤 发送消息给用户: userId={}, type={}", userId, message.getType());
        } else {
            log.warn("[IoT WebSocket] ⚠️ 用户不在线或连接已关闭: userId={}", userId);
        }
    }

    /**
     * 批量发送消息（多个用户）
     */
    public void sendToUsers(Iterable<Long> userIds, IotMessage message) {
        userIds.forEach(userId -> sendToUser(userId, message));
    }

    /**
     * 推送设备状态更新消息
     */
    public void pushDeviceStatus(DeviceStatusMessage status) {
        broadcast(IotMessage.deviceStatus(status));
        log.info("[IoT WebSocket] 📡 推送设备状态更新: deviceId={}, status={}", 
                status.getDeviceId(), status.getStatus());
    }

    /**
     * 推送告警事件消息
     */
    public void pushAlarmEvent(AlarmEventMessage alarm) {
        broadcast(IotMessage.alarmEvent(alarm));
        log.info("[IoT WebSocket] 🚨 推送告警事件: id={}, level={}, title={}", 
                alarm.getId(), alarm.getLevel(), alarm.getTitle());
    }

    /**
     * 推送设备统计数据
     */
    public void pushDeviceStats(DeviceStatsMessage stats) {
        broadcast(IotMessage.deviceStats(stats));
        log.info("[IoT WebSocket] 📊 推送设备统计: online={}, offline={}, alarm={}, rate={}%", 
                stats.getOnline(), stats.getOffline(), stats.getAlarm(), stats.getRate());
    }

    /**
     * 推送快照更新
     */
    public void pushSnapshotUpdate(Object data) {
        broadcast(IotMessage.snapshotUpdate(data));
        log.debug("[IoT WebSocket] 📸 推送快照更新");
    }

    /**
     * 推送服务失败消息
     */
    public void pushServiceFailure(ServiceFailureMessage failure) {
        broadcast(IotMessage.serviceFailure(failure));
        log.warn("[IoT WebSocket] ⚠️ 推送服务失败: deviceId={}, service={}, reason={}", 
                failure.getDeviceId(), failure.getServiceName(), failure.getFailureReason());
    }

    // ============= 内部辅助方法 =============

    /**
     * 从 Session 中提取 userId
     */
    private Long extractUserId(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query != null && query.contains("userId=")) {
                String userId = query.substring(query.indexOf("userId=") + 7);
                // 移除可能的其他参数
                if (userId.contains("&")) {
                    userId = userId.substring(0, userId.indexOf("&"));
                }
                return Long.parseLong(userId);
            }
        } catch (Exception e) {
            log.error("[IoT WebSocket] ❌ 提取 userId 失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 发送消息到 Session（线程安全）
     * <p>
     * WebSocketSession.sendMessage() 不是线程安全的，
     * 使用 synchronized 保证同一 Session 的写入操作串行化，
     * 避免 IllegalStateException: TEXT_PARTIAL_WRITING 错误
     * </p>
     */
    private void sendMessage(WebSocketSession session, IotMessage message) {
        if (session == null || !session.isOpen()) {
            log.warn("[IoT WebSocket] ⚠️ Session 已关闭或为空，无法发送消息");
            return;
        }
        
        // 获取该 Session 的写锁
        Object lock = sessionWriteLocks.get(session.getId());
        if (lock == null) {
            // 如果锁不存在（可能是旧连接），使用 session 本身作为锁
            lock = session;
        }
        
        synchronized (lock) {
            try {
                if (session.isOpen()) {
                    String json = JSONUtil.toJsonStr(message);
                    session.sendMessage(new TextMessage(json));
                } else {
                    log.warn("[IoT WebSocket] ⚠️ Session 已关闭，无法发送消息: sessionId={}", session.getId());
                }
            } catch (IOException e) {
                log.error("[IoT WebSocket] ❌ 发送消息失败: sessionId={}, error={}", 
                        session.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return sessions.size();
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }
}

