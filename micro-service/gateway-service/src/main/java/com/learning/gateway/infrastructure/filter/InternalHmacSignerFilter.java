package com.learning.gateway.infrastructure.filter;

import com.learning.gateway.infrastructure.config.GatewaySecurityProperties;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Downstream Security Filter (Order = -50).
 * <p>
 * Implements ARCH-004:
 * 1. Strips any incoming spoofed internal headers (X-User-Id, X-User-Roles, X-Internal-HMAC, X-Timestamp).
 * 2. Generates a fresh HMAC-SHA256 signature using format "timestamp::userId::roles".
 * 3. Injects validated claims and signature into the downstream request.
 */
@Component
public class InternalHmacSignerFilter implements GlobalFilter, Ordered {

    private final HmacUtils hmacUtils;

    public InternalHmacSignerFilter(GatewaySecurityProperties securityProperties) {
        this.hmacUtils = new HmacUtils(
                HmacAlgorithms.HMAC_SHA_256,
                securityProperties.internalHmacSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String userId = exchange.getAttribute("userId");
        String roles = exchange.getAttribute("roles");
        String permissions = exchange.getAttribute("permissions");

        // If not an authenticated user request (e.g. public login/register/actuator), pass through
        if (userId == null) {
            return chain.filter(exchange);
        }

        long currentTimeStamp = Instant.now().getEpochSecond();
        // Payload format matching downstream InternalHmacFilter: timestamp::userId::roles::permissions
        String payloadToSign = currentTimeStamp + "::" + userId + "::" + (roles != null ? roles : "") + "::" + (permissions != null ? permissions : "");
        String hmacSignature = hmacUtils.hmacHex(payloadToSign);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .headers(httpHeaders -> {
                    httpHeaders.remove("X-User-Id");
                    httpHeaders.remove("X-User-Roles");
                    httpHeaders.remove("X-User-Permissions");
                    httpHeaders.remove("X-Internal-HMAC");
                    httpHeaders.remove("X-Timestamp");
                })
                .header("X-User-Id", userId)
                .header("X-User-Roles", roles != null ? roles : "")
                .header("X-User-Permissions", permissions != null ? permissions : "")
                .header("X-Timestamp", String.valueOf(currentTimeStamp))
                .header("X-Internal-HMAC", hmacSignature)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -50; // Execute after BffAuthenticationFilter (-100)
    }
}
