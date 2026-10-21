package com.learning.gateway.infrastructure.filter;

import com.learning.gateway.application.dto.ValidatedClaims;
import com.learning.gateway.infrastructure.security.JwtPublicKeyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * BFF (Backend-For-Frontend) Authentication Filter.
 * <p>
 * Implements ARCH-002:
 * 1. Checks for opaque HttpOnly "APP_SESSION" cookie (sub-millisecond O(1) Redis fast-path).
 * 2. Fallback: Validates asymmetric Bearer JWT, generates an opaque session ID, persists
 *    claims to Redis (session:{id}), and issues the HttpOnly "APP_SESSION" cookie to the caller.
 * 3. Handles "/api/v1/auth/logout" to instantly revoke Redis session and expire client cookie.
 */
@Component
public class BffAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(BffAuthenticationFilter.class);

    public static final String SESSION_COOKIE_NAME = "APP_SESSION";
    private static final String SESSION_KEY_PREFIX = "session:";
    private static final Duration SESSION_TTL = Duration.ofHours(24);

    private static final List<String> PUBLIC_URLS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/public-key",
            "/actuator"
    );

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    private final JwtPublicKeyValidator jwtPublicKeyValidator;

    public BffAuthenticationFilter(ReactiveStringRedisTemplate reactiveStringRedisTemplate,
                                   JwtPublicKeyValidator jwtPublicKeyValidator) {
        this.reactiveStringRedisTemplate = reactiveStringRedisTemplate;
        this.jwtPublicKeyValidator = jwtPublicKeyValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1. Session Revocation / Logout: Purge Redis session and expire browser cookie
        if ("/api/v1/auth/logout".equalsIgnoreCase(path)) {
            return handleLogout(exchange);
        }

        // 2. Bypass public authentication & actuator routes
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // 3. Fast-Path: Check for BFF Opaque Session Cookie (Sub-millisecond Redis read)
        HttpCookie appSessionCookie = exchange.getRequest().getCookies().getFirst(SESSION_COOKIE_NAME);
        if (appSessionCookie != null && !appSessionCookie.getValue().isBlank()) {
            return validateAppSessionCookie(exchange, chain, appSessionCookie.getValue());
        }

        // 4. Fallback: Validate Bearer JWT, Establish Redis Session, and Issue APP_SESSION Cookie
        return validateBearerToken(exchange, chain, path);
    }

    /**
     * Fast-Path: Reads opaque session from Redis in O(1) time without asymmetric RSA math.
     */
    private Mono<Void> validateAppSessionCookie(ServerWebExchange exchange, GatewayFilterChain chain, String sessionId) {
        return reactiveStringRedisTemplate.opsForHash().entries(SESSION_KEY_PREFIX + sessionId)
                .collectMap(entry -> (String) entry.getKey(), entry -> (String) entry.getValue())
                .flatMap(sessionData -> {
                    if (sessionData.isEmpty() || !sessionData.containsKey("userId")) {
                        log.warn("Invalid or expired BFF session cookie: {}", sessionId);
                        // Clean up stale cookie on client
                        expireSessionCookie(exchange);
                        return unauthorized(exchange);
                    }

                    // Attach verified session attributes to exchange for InternalHmacSignerFilter
                    exchange.getAttributes().put("userId", sessionData.get("userId"));
                    exchange.getAttributes().put("roles", sessionData.getOrDefault("roles", ""));
                    exchange.getAttributes().put("permissions", sessionData.getOrDefault("permissions", ""));
                    exchange.getAttributes().put("email", sessionData.getOrDefault("email", ""));
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    log.error("Redis session lookup failed for ID: {}", sessionId, e);
                    return unauthorized(exchange);
                });
    }

    /**
     * Bearer Token Fallback: Validates RS256 token, creates session in Redis, and issues APP_SESSION cookie.
     */
    private Mono<Void> validateBearerToken(ServerWebExchange exchange, GatewayFilterChain chain, String path) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            ValidatedClaims claims = jwtPublicKeyValidator.validate(token);

            if (claims == null) {
                log.warn("Bearer token validation failed for path: {}", path);
                return unauthorized(exchange);
            }

            String rolesStr = String.join(",", claims.roles());
            String permsStr = String.join(",", claims.permissions());
            exchange.getAttributes().put("userId", claims.userId());
            exchange.getAttributes().put("roles", rolesStr);
            exchange.getAttributes().put("permissions", permsStr);
            exchange.getAttributes().put("email", claims.email() != null ? claims.email() : "");

            // 1. Establish BFF Opaque Session: Generate sessionId and payload
            String sessionId = UUID.randomUUID().toString();
            String sessionKey = SESSION_KEY_PREFIX + sessionId;
            Map<String, String> sessionData = Map.of(
                    "userId", claims.userId(),
                    "roles", rolesStr,
                    "permissions", permsStr,
                    "email", claims.email() != null ? claims.email() : ""
            );

            // 2. Issue HttpOnly APP_SESSION cookie to browser
            ResponseCookie sessionCookie = ResponseCookie.from(SESSION_COOKIE_NAME, sessionId)
                    .httpOnly(true)
                    .secure(false) // Set to true in production over HTTPS
                    .path("/")
                    .maxAge(SESSION_TTL)
                    .sameSite("Lax")
                    .build();
            exchange.getResponse().addCookie(sessionCookie);

            // 3. Save session in Redis with 24h TTL, then continue downstream filter chain
            return reactiveStringRedisTemplate.opsForHash().putAll(sessionKey, sessionData)
                    .then(reactiveStringRedisTemplate.expire(sessionKey, SESSION_TTL))
                    .then(chain.filter(exchange))
                    .onErrorResume(e -> {
                        log.error("Failed to persist BFF session in Redis for user: {}", claims.userId(), e);
                        // Fault-tolerant: proceed with request even if Redis session write failed
                        return chain.filter(exchange);
                    });
        }

        // No valid session or token provided on protected route
        log.warn("Unauthorized access attempt on protected path: {}", path);
        return unauthorized(exchange);
    }

    /**
     * Handles /api/v1/auth/logout: Revokes Redis session key and clears browser cookie.
     */
    private Mono<Void> handleLogout(ServerWebExchange exchange) {
        HttpCookie appSessionCookie = exchange.getRequest().getCookies().getFirst(SESSION_COOKIE_NAME);
        expireSessionCookie(exchange);

        Mono<Void> purgeRedisSession = Mono.empty();
        if (appSessionCookie != null && !appSessionCookie.getValue().isBlank()) {
            purgeRedisSession = reactiveStringRedisTemplate.delete(SESSION_KEY_PREFIX + appSessionCookie.getValue()).then();
        }

        return purgeRedisSession.then(Mono.defer(() -> {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            byte[] bytes = "{\"message\":\"Logged out successfully\"}".getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }));
    }

    private void expireSessionCookie(ServerWebExchange exchange) {
        ResponseCookie expiredCookie = ResponseCookie.from(SESSION_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .build();
        exchange.getResponse().addCookie(expiredCookie);
    }

    @Override
    public int getOrder() {
        return -100; // Execute early in filter chain, before HMAC signing (-50)
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_URLS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
