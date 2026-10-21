package com.learning.chat.infrastructure.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.util.List;
import java.util.Map;

public class UserHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(UserHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 1. Read verified user from InternalHmacFilter
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String authenticatedUserId = (String) servletRequest.getServletRequest().getAttribute("authenticatedUserId");
            if (authenticatedUserId != null && !authenticatedUserId.isBlank()) {
                attributes.put("userId", authenticatedUserId);
                log.info("WebSocket handshake verified via InternalHmacFilter for user: {}", authenticatedUserId);
                return true;
            }

            // Local Testing / Postman Fallback: Query parameter support
            String queryUserId = servletRequest.getServletRequest().getParameter("userId");
            if (queryUserId != null && !queryUserId.isBlank()) {
                attributes.put("userId", queryUserId);
                log.info("WebSocket handshake authenticated via query parameter: {}", queryUserId);
                return true;
            }
        }

        // 2. Direct Header Fallback
        List<String> userIdHeaders = request.getHeaders().get("X-User-Id");
        if (userIdHeaders != null && !userIdHeaders.isEmpty()) {
            String userId = userIdHeaders.getFirst();
            attributes.put("userId", userId);
            log.info("WebSocket handshake authenticated via header for user: {}", userId);
            return true;
        }

        log.warn("WebSocket handshake rejected: No authenticated user found");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
