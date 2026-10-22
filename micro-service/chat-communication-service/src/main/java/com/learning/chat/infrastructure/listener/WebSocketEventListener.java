package com.learning.chat.infrastructure.listener;

import com.learning.chat.application.dto.UserPresenceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private static final String PRESENCE_SESSIONS_PREFIX = "chat:presence:sessions:";
    private static final String PRESENCE_STATE_PREFIX = "chat:presence:";

    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userId = extractUserId(headerAccessor);

        if (userId != null && sessionId != null) {
            String sessionsKey = PRESENCE_SESSIONS_PREFIX + userId;
            redisTemplate.opsForSet().add(sessionsKey, sessionId);
            redisTemplate.expire(sessionsKey, Duration.ofMinutes(30));
            redisTemplate.opsForValue().set(PRESENCE_STATE_PREFIX + userId, "ONLINE", Duration.ofMinutes(30));

            Long activeCount = redisTemplate.opsForSet().size(sessionsKey);
            if (activeCount != null && activeCount == 1L) {
                messagingTemplate.convertAndSend("/topic/presence", UserPresenceDto.online(userId));
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userId = extractUserId(headerAccessor);

        if (userId != null && sessionId != null) {
            String sessionsKey = PRESENCE_SESSIONS_PREFIX + userId;
            redisTemplate.opsForSet().remove(sessionsKey, sessionId);
            Long remaining = redisTemplate.opsForSet().size(sessionsKey);
            if (remaining == null || remaining == 0L) {
                redisTemplate.delete(PRESENCE_STATE_PREFIX + userId);
                messagingTemplate.convertAndSend("/topic/presence", UserPresenceDto.offline(userId));
            }
        }
    }

    private String extractUserId(StompHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            return principal.getName();
        }
        if (headerAccessor.getSessionAttributes() != null) {
            Object attr = headerAccessor.getSessionAttributes().get("userId");
            return attr != null ? attr.toString() : null;
        }
        return null;
    }
}
