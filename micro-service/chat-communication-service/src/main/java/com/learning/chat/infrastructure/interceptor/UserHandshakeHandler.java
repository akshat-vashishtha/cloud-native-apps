package com.learning.chat.infrastructure.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Maps the authenticated userId from handshake attributes into a Principal object.
 * This enables:
 * 1. @SendToUser("/queue/...") targeting.
 * 2. Injecting 'Principal principal' into @MessageMapping controller methods.
 */
public class UserHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        String userId = (String) attributes.get("userId");
        if (userId == null) {
            return null;
        }
        // Returns a custom lightweight Principal holding the userId
        return new StompPrincipal(userId);
    }

    public record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
