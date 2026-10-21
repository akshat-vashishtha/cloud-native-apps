package com.learning.chat.infrastructure.listener;

import com.learning.chat.application.dto.UserPresenceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    // 1. Session connected event handler
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        Principal principal = headerAccessor.getUser();
        String userId = principal != null ? principal.getName() : null;
        if (userId == null && headerAccessor.getSessionAttributes() != null) {
            userId = (String) headerAccessor.getSessionAttributes().get("userId");
        }

        log.info("New WebSocket connection: sessionId={}, userId={}", sessionId, userId);

        if (userId != null) {
            messagingTemplate.convertAndSend("/topic/presence", UserPresenceDto.online(userId));
        }
    }

    // 2. Session disconnect event handler
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        Principal principal = headerAccessor.getUser();
        String userId = principal != null ? principal.getName() : null;
        if (userId == null && headerAccessor.getSessionAttributes() != null) {
            userId = (String) headerAccessor.getSessionAttributes().get("userId");
        }

        log.info("User disconnected: sessionId={}, userId={}", sessionId, userId);

        if (userId != null) {
            messagingTemplate.convertAndSend("/topic/presence", UserPresenceDto.offline(userId));
        }
    }
}
