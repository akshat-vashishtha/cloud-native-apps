package com.learning.chat.infrastructure.config;

import com.learning.chat.infrastructure.interceptor.UserHandshakeHandler;
import com.learning.chat.infrastructure.interceptor.UserHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. In-Memory SimpleBroker for Phase 1 (/topic = broadcast, /queue = private)
        registry.enableSimpleBroker("/topic", "/queue");
        // 2. Incoming destination prefix from clients to @MessageMapping controllers
        registry.setApplicationDestinationPrefixes("/app");
        // 3. User destination prefix for @SendToUser private routing
        registry.setUserDestinationPrefix("/user");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Raw WebSocket Endpoint (used by mobile apps and standard STOMP clients)
        registry.addEndpoint("/ws")
                .addInterceptors(new UserHandshakeInterceptor())
                .setHandshakeHandler(new UserHandshakeHandler())
                .setAllowedOriginPatterns("*");
        // 2. SockJS Fallback Endpoint (used by web browsers behind strict firewalls/proxies)
        registry.addEndpoint("/ws")
                .addInterceptors(new UserHandshakeInterceptor())
                .setHandshakeHandler(new UserHandshakeHandler())
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

}
