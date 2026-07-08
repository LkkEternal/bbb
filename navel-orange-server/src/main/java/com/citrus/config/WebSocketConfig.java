package com.citrus.config;

import com.citrus.websocket.DetectionWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket 配置 — 注册检测数据实时推送端点
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DetectionWebSocketHandler detectionWebSocketHandler;

    public WebSocketConfig(DetectionWebSocketHandler detectionWebSocketHandler) {
        this.detectionWebSocketHandler = detectionWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(detectionWebSocketHandler, "/ws/detections")
                .setAllowedOrigins("*");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(65536);
        container.setMaxBinaryMessageBufferSize(65536);
        container.setMaxSessionIdleTimeout(600000L); // 10分钟无活动断开
        return container;
    }
}
