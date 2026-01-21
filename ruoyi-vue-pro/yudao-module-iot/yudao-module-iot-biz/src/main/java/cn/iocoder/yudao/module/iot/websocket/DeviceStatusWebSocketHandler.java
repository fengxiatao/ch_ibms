package cn.iocoder.yudao.module.iot.websocket;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.websocket.message.DeviceStatusPushMessage;
import cn.iocoder.yudao.module.iot.websocket.message.IotMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 设备状态 WebSocket 处理器
 * 
 * <p>处理前端的 WebSocket 连接和设备状态消息推送。</p>
 * 
 * <p>功能：</p>
 * <ul>
 *   <li>管理 WebSocket 连接</li>
 *   <li>支持订阅特定设备类型的状态变更</li>
 *   <li>广播设备状态变更消息</li>
 *   <li>支持批量推送</li>
 * </ul>
 * 
 * <p>Requirements: 2.2</p>
 *
 * @author 长辉信息科技有限公司
 */
@Slf4j
@Component("deviceStatusWebSocketHandler")
public class DeviceStatusWebSocketHandler extends TextWebSocketHandler {

    /**
     * 用户连接映射（userId -> WebSocketSession）
     */
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * Session 到 UserId 的反向映射
     */
    private final Map<String, Long> sessionToUserId = new ConcurrentHashMap<>();

    /**
     * 设备类型订阅关系（sessionId -> 订阅的设备类型集合）
     * <p>如果为空集合，表示订阅所有设备类型</p>
     * Requirements: 2.5
     */
    private final Map<String, Set<String>> deviceTypeSubscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            Long userId = extractUserId(session);
            if (userId == null) {
                log.warn("[DeviceStatus WebSocket] ⚠️ 连接缺少 userId 参数，拒绝连接: {}", session.getId());
                session.close(CloseStatus.BAD_DATA.withReason("Missing userId parameter"));
                return;
            }

            // 如果用户已有连接，先关闭旧连接
            WebSocketSession oldSession = sessions.get(userId);
            if (oldSession != null && oldSession.isOpen()) {
                log.info("[DeviceStatus WebSocket] 🔄 用户 {} 重复连接，关闭旧连接", userId);
                // 清理旧连接的订阅
                deviceTypeSubscriptions.remove(oldSession.getId());
                try {
                    oldSession.close(CloseStatus.NORMAL.withReason("New connection established"));
                } catch (IOException e) {
                    // 忽略关闭旧连接时的异常（可能连接已经被客户端关闭）
                    log.debug("[DeviceStatus WebSocket] 关闭旧连接时发生异常（已忽略）: {}", e.getMessage());
                }
            }

            sessions.put(userId, session);
            sessionToUserId.put(session.getId(), userId);
            // 默认订阅所有设备类型（空集合表示订阅所有）
            deviceTypeSubscriptions.put(session.getId(), new CopyOnWriteArraySet<>());

            log.info("[DeviceStatus WebSocket] ✅ 用户 {} 连接成功，sessionId={}, 当前在线用户数: {}",
                    userId, session.getId(), sessions.size());

            // 发送连接确认消息
            // Requirements: 2.2 - 前端建立 WebSocket 连接后加入广播列表
            sendMessage(session, IotMessage.connected());

        } catch (Exception e) {
            log.error("[DeviceStatus WebSocket] ❌ 处理连接建立失败: {}", e.getMessage(), e);
            session.close(CloseStatus.SERVER_ERROR.withReason("Connection establishment failed"));
        }
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            log.debug("[DeviceStatus WebSocket] 📨 收到消息: sessionId={}, payload={}", session.getId(), payload);

            IotMessage iotMessage = JSONUtil.toBean(payload, IotMessage.class);

            switch (iotMessage.getType()) {
                case "ping":
                    sendMessage(session, IotMessage.pong());
                    log.debug("[DeviceStatus WebSocket] 💓 心跳响应: sessionId={}", session.getId());
                    break;
                case "subscribe":
                    // 处理订阅特定设备类型
                    // Requirements: 2.5 - 前端订阅特定设备类型的状态变更
                    handleSubscribe(session, iotMessage);
                    break;
                case "unsubscribe":
                    // 处理取消订阅
                    handleUnsubscribe(session, iotMessage);
                    break;
                default:
                    log.warn("[DeviceStatus WebSocket] ⚠️ 未知消息类型: type={}", iotMessage.getType());
            }

        } catch (Exception e) {
            log.error("[DeviceStatus WebSocket] ❌ 处理消息失败: sessionId={}, error={}",
                    session.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = sessionToUserId.remove(session.getId());
        deviceTypeSubscriptions.remove(session.getId());
        if (userId != null) {
            sessions.remove(userId);
            log.info("[DeviceStatus WebSocket] 🔌 用户 {} 断开连接，sessionId={}, 原因={}, 当前在线用户数: {}",
                    userId, session.getId(), status, sessions.size());
        }
        // Requirements: 2.2 - 前端断开 WebSocket 连接后从广播列表移除
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = sessionToUserId.get(session.getId());
        log.error("[DeviceStatus WebSocket] ❌ 传输错误: userId={}, sessionId={}, error={}",
                userId, session.getId(), exception.getMessage(), exception);

        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR.withReason("Transport error"));
        }
    }

    // ============= 订阅管理方法 =============

    /**
     * 处理订阅消息
     * <p>订阅特定设备类型的状态变更</p>
     * Requirements: 2.5
     *
     * @param session WebSocket会话
     * @param message 订阅消息
     */
    private void handleSubscribe(WebSocketSession session, IotMessage message) {
        try {
            Object data = message.getData();
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) data;
                Object deviceTypesObj = dataMap.get("deviceTypes");
                if (deviceTypesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> deviceTypes = (List<String>) deviceTypesObj;
                    Set<String> subscriptions = deviceTypeSubscriptions.computeIfAbsent(
                            session.getId(), k -> new CopyOnWriteArraySet<>());
                    subscriptions.addAll(deviceTypes);
                    log.info("[DeviceStatus WebSocket] 📋 用户订阅设备类型: sessionId={}, deviceTypes={}",
                            session.getId(), deviceTypes);
                }
            }
        } catch (Exception e) {
            log.error("[DeviceStatus WebSocket] ❌ 处理订阅失败: sessionId={}, error={}",
                    session.getId(), e.getMessage(), e);
        }
    }

    /**
     * 处理取消订阅消息
     *
     * @param session WebSocket会话
     * @param message 取消订阅消息
     */
    private void handleUnsubscribe(WebSocketSession session, IotMessage message) {
        try {
            Object data = message.getData();
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) data;
                Object deviceTypesObj = dataMap.get("deviceTypes");
                if (deviceTypesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> deviceTypes = (List<String>) deviceTypesObj;
                    Set<String> subscriptions = deviceTypeSubscriptions.get(session.getId());
                    if (subscriptions != null) {
                        subscriptions.removeAll(deviceTypes);
                        log.info("[DeviceStatus WebSocket] 📋 用户取消订阅设备类型: sessionId={}, deviceTypes={}",
                                session.getId(), deviceTypes);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[DeviceStatus WebSocket] ❌ 处理取消订阅失败: sessionId={}, error={}",
                    session.getId(), e.getMessage(), e);
        }
    }

    // ============= 消息推送方法 =============

    /**
     * 广播消息（所有在线用户）
     *
     * @param message 消息
     */
    public void broadcast(IotMessage message) {
        log.info("[DeviceStatus WebSocket] 📢 广播消息: type={}, 在线用户数={}", message.getType(), sessions.size());
        sessions.values().forEach(session -> sendMessage(session, message));
    }

    /**
     * 推送设备状态变更
     * <p>根据订阅关系过滤推送</p>
     * Requirements: 2.1, 2.5
     *
     * @param statusMessage 设备状态推送消息
     */
    public void pushDeviceStatusChange(DeviceStatusPushMessage statusMessage) {
        IotMessage message = IotMessage.deviceStatusChange(statusMessage);
        String deviceType = statusMessage.getDeviceType();

        int pushCount = 0;
        for (Map.Entry<Long, WebSocketSession> entry : sessions.entrySet()) {
            WebSocketSession session = entry.getValue();
            if (shouldPushToSession(session, deviceType)) {
                sendMessage(session, message);
                pushCount++;
            }
        }

        log.info("[DeviceStatus WebSocket] 📡 推送设备状态变更: deviceId={}, deviceName={}, " +
                        "newState={}, previousState={}, 推送用户数={}",
                statusMessage.getDeviceId(), statusMessage.getDeviceName(),
                statusMessage.getNewStateName(), statusMessage.getPreviousStateName(), pushCount);
    }

    /**
     * 批量推送设备状态变更
     * Requirements: 2.1
     *
     * @param statusMessages 设备状态推送消息列表
     */
    public void pushDeviceStatusChangeBatch(List<DeviceStatusPushMessage> statusMessages) {
        if (statusMessages == null || statusMessages.isEmpty()) {
            return;
        }

        log.info("[DeviceStatus WebSocket] 📡 批量推送设备状态变更: count={}", statusMessages.size());

        for (DeviceStatusPushMessage statusMessage : statusMessages) {
            pushDeviceStatusChange(statusMessage);
        }
    }

    /**
     * 判断是否应该推送给指定会话
     * <p>根据订阅关系判断</p>
     * Requirements: 2.5
     *
     * @param session    WebSocket会话
     * @param deviceType 设备类型
     * @return 是否应该推送
     */
    private boolean shouldPushToSession(WebSocketSession session, String deviceType) {
        if (!session.isOpen()) {
            return false;
        }

        Set<String> subscriptions = deviceTypeSubscriptions.get(session.getId());
        // 如果订阅集合为空或null，表示订阅所有设备类型
        if (subscriptions == null || subscriptions.isEmpty()) {
            return true;
        }

        // 如果设备类型为空，也推送（兼容旧数据）
        if (deviceType == null || deviceType.isEmpty()) {
            return true;
        }

        // 检查是否订阅了该设备类型
        return subscriptions.contains(deviceType);
    }

    // ============= 内部辅助方法 =============

    /**
     * 从 WebSocket 会话中提取用户ID
     *
     * @param session WebSocket会话
     * @return 用户ID，如果提取失败返回null
     */
    private Long extractUserId(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query != null && query.contains("userId=")) {
                String userId = query.substring(query.indexOf("userId=") + 7);
                if (userId.contains("&")) {
                    userId = userId.substring(0, userId.indexOf("&"));
                }
                return Long.parseLong(userId);
            }
        } catch (Exception e) {
            log.error("[DeviceStatus WebSocket] ❌ 提取 userId 失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 发送消息到指定会话
     * <p>使用同步块确保同一会话的消息发送是线程安全的，
     * 避免并发发送时出现 TEXT_PARTIAL_WRITING 状态错误</p>
     *
     * @param session WebSocket会话
     * @param message 消息
     */
    private void sendMessage(WebSocketSession session, IotMessage message) {
        if (session == null || !session.isOpen()) {
            return;
        }
        // 同步发送，防止并发写入导致 IllegalStateException: TEXT_PARTIAL_WRITING
        synchronized (session) {
            try {
                if (session.isOpen()) {
                    String json = JSONUtil.toJsonStr(message);
                    session.sendMessage(new TextMessage(json));
                }
            } catch (IOException e) {
                log.error("[DeviceStatus WebSocket] ❌ 发送消息失败: sessionId={}, error={}",
                        session.getId(), e.getMessage(), e);
            } catch (IllegalStateException e) {
                // 处理会话状态异常（如连接正在关闭）
                log.warn("[DeviceStatus WebSocket] ⚠️ 会话状态异常，跳过发送: sessionId={}, error={}",
                        session.getId(), e.getMessage());
            }
        }
    }

    /**
     * 获取在线用户数
     *
     * @return 在线用户数
     */
    public int getOnlineUserCount() {
        return sessions.size();
    }

    /**
     * 获取指定用户的会话
     *
     * @param userId 用户ID
     * @return WebSocket会话，如果不存在返回null
     */
    public WebSocketSession getSession(Long userId) {
        return sessions.get(userId);
    }

    /**
     * 判断用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }
}
