package cn.iocoder.yudao.module.iot.websocket;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT 告警 WebSocket 处理器
 *
 * 处理客户端WebSocket连接，实现告警实时推送功能。
 *
 * @author 长辉信息科技有限公司
 */
@Component("alertWebSocketHandler")  // 指定明确的Bean名称，避免与框架的webSocketHandler冲突
@Slf4j
public class AlertWebSocketHandler extends TextWebSocketHandler {

    /**
     * 存储所有活跃的WebSocket会话
     * Key: userId, Value: WebSocketSession
     */
    private static final Map<Long, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从session中获取用户ID（假设在握手时已设置）
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            SESSIONS.put(userId, session);
            log.info("[WebSocket] 用户连接成功: userId={}, sessionId={}", userId, session.getId());
            
            // 发送连接成功消息
            sendMessage(session, new WebSocketMessage("connected", "连接成功", null));
        } else {
            log.warn("[WebSocket] 无法获取用户ID，关闭连接: sessionId={}", session.getId());
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("[WebSocket] 收到消息: sessionId={}, payload={}", session.getId(), payload);
        
        // 处理心跳消息
        if ("ping".equals(payload)) {
            sendMessage(session, new WebSocketMessage("pong", "心跳响应", null));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            SESSIONS.remove(userId);
            log.info("[WebSocket] 用户断开连接: userId={}, sessionId={}, status={}", 
                    userId, session.getId(), status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[WebSocket] 传输错误: sessionId={}, error={}", 
                session.getId(), exception.getMessage(), exception);
        
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            SESSIONS.remove(userId);
        }
        
        if (session.isOpen()) {
            session.close();
        }
    }

    /**
     * 向指定用户推送告警消息
     *
     * @param userId 用户ID
     * @param alert 告警数据
     */
    public void pushAlert(Long userId, Object alert) {
        WebSocketSession session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                WebSocketMessage message = new WebSocketMessage("alert", "新告警", alert);
                sendMessage(session, message);
                log.info("[WebSocket] 告警推送成功: userId={}", userId);
            } catch (Exception e) {
                log.error("[WebSocket] 告警推送失败: userId={}, error={}", userId, e.getMessage());
            }
        } else {
            log.debug("[WebSocket] 用户未连接，跳过推送: userId={}", userId);
        }
    }

    /**
     * 向多个用户推送告警消息
     *
     * @param userIds 用户ID列表
     * @param alert 告警数据
     */
    public void pushAlertToUsers(java.util.List<Long> userIds, Object alert) {
        userIds.forEach(userId -> pushAlert(userId, alert));
    }

    /**
     * 广播告警消息给所有在线用户
     *
     * @param alert 告警数据
     */
    public void broadcastAlert(Object alert) {
        WebSocketMessage message = new WebSocketMessage("alert", "新告警", alert);
        broadcastMessage(message);
    }
    
    /**
     * 广播消息给所有在线用户（通用方法）
     *
     * @param message WebSocket消息对象
     */
    public void broadcastMessage(WebSocketMessage message) {
        SESSIONS.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    sendMessage(session, message);
                } catch (Exception e) {
                    log.error("[WebSocket] 广播消息失败: userId={}, error={}", userId, e.getMessage());
                }
            }
        });
        log.info("[WebSocket] 消息已广播给 {} 个在线用户", SESSIONS.size());
    }

    /**
     * 获取当前在线用户数
     *
     * @return 在线用户数
     */
    public int getOnlineUserCount() {
        return SESSIONS.size();
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return true if online
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = SESSIONS.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 从WebSocketSession中获取用户ID
     *
     * @param session WebSocketSession
     * @return 用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            // 从session attributes中获取用户ID
            Object userIdObj = session.getAttributes().get("userId");
            if (userIdObj != null) {
                return Long.valueOf(userIdObj.toString());
            }
            
            // 从URI中获取用户ID（例如: /ws/alert?userId=123）
            String query = session.getUri().getQuery();
            if (query != null && query.contains("userId=")) {
                String userId = query.split("userId=")[1].split("&")[0];
                return Long.valueOf(userId);
            }
        } catch (Exception e) {
            log.error("[WebSocket] 获取用户ID失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 发送消息到客户端
     *
     * @param session WebSocketSession
     * @param message 消息对象
     */
    private void sendMessage(WebSocketSession session, WebSocketMessage message) throws IOException {
        String json = JSONUtil.toJsonStr(message);
        session.sendMessage(new TextMessage(json));
    }

    /**
     * WebSocket消息封装
     */
    public static class WebSocketMessage {
        private String type;    // 消息类型
        private String message; // 消息内容
        private Object data;    // 数据
        private Long timestamp; // 时间戳

        public WebSocketMessage(String type, String message, Object data) {
            this.type = type;
            this.message = message;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public String getType() { return type; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
        public Long getTimestamp() { return timestamp; }
    }
}








