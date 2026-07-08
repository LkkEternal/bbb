package com.citrus.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 处理器 — 检测数据实时推送
 * 管理前端连接会话，广播检测事件
 */
@Slf4j
@Component
public class DetectionWebSocketHandler extends TextWebSocketHandler {

    /** 所有活跃的前端连接会话 */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public DetectionWebSocketHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket 客户端已连接: {}, 当前连接数: {}", session.getId(), sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("WebSocket 客户端已断开: {}, 剩余连接数: {}", session.getId(), sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 前端可以发送 ping 维持连接
        String payload = message.getPayload();
        if ("ping".equals(payload)) {
            try {
                session.sendMessage(new TextMessage("pong"));
            } catch (IOException e) {
                log.warn("WebSocket 发送 pong 失败: {}", e.getMessage());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket 传输错误, sessionId={}: {}", session.getId(), exception.getMessage());
        sessions.remove(session.getId());
    }

    /**
     * 广播检测事件给所有连接的客户端
     * @param eventType 事件类型: detection_new / detection_stats_update
     * @param payload   消息体
     */
    public void broadcast(String eventType, Object payload) {
        if (sessions.isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", eventType,
                    "data", payload,
                    "timestamp", System.currentTimeMillis()
            ));
            TextMessage message = new TextMessage(json);
            for (WebSocketSession session : sessions.values()) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.warn("WebSocket 发送失败, sessionId={}: {}", session.getId(), e.getMessage());
                        sessions.remove(session.getId());
                    }
                } else {
                    sessions.remove(session.getId());
                }
            }
        } catch (Exception e) {
            log.error("WebSocket 广播异常: {}", e.getMessage());
        }
    }

    /** 当前连接数 */
    public int getActiveSessionCount() {
        return sessions.size();
    }
}
