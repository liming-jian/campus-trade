package com.campus_trade.websocket;

import com.campus_trade.config.JwtUtils;
import com.campus_trade.service.ChatService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final JwtUtils jwtUtils;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public WebSocketConfig(JwtUtils jwtUtils, ChatWebSocketHandler chatWebSocketHandler) {
        this.jwtUtils = jwtUtils;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(new WebSocketHandshakeInterceptor(jwtUtils))
                .setAllowedOrigins("*");
    }
}
