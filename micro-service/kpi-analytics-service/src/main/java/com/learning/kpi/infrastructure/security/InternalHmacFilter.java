package com.learning.kpi.infrastructure.security;

import com.learning.kpi.infrastructure.config.KpiSecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates HMAC signatures from Gateway and populates Spring SecurityContext with authorities.
 */
@Component
public class InternalHmacFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InternalHmacFilter.class);
    private static final long MAX_TIME_DRIFT_SECONDS = 300; // 5 Minutes drift tolerance

    private final KpiSecurityProperties securityProperties;
    private final HmacUtils hmacUtils;

    public InternalHmacFilter(KpiSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        this.hmacUtils = new HmacUtils(
                HmacAlgorithms.HMAC_SHA_256,
                securityProperties.internalHmacSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String userRoles = request.getHeader("X-User-Roles");
        String userPermissions = request.getHeader("X-User-Permissions");
        String timestampStr = request.getHeader("X-Timestamp");
        String incomingHmac = request.getHeader("X-Internal-HMAC");

        if (userId == null || timestampStr == null || incomingHmac == null) {
            log.warn("Unauthorized internal request on {}: Missing security headers", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing internal gateway authentication headers");
            return;
        }

        try {
            long requestTimestamp = Long.parseLong(timestampStr);
            long currentTimestamp = Instant.now().getEpochSecond();
            long drift = Math.abs(currentTimestamp - requestTimestamp);

            if (drift > MAX_TIME_DRIFT_SECONDS) {
                log.warn("Expired internal request. Drift: {}s on {}", drift, request.getRequestURI());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Request timestamp expired or clock skew exceeded");
                return;
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid X-Timestamp header format");
            return;
        }

        // Cryptographic HMAC Verification with permissions
        String payloadToVerify = timestampStr + "::" + userId + "::" + (userRoles != null ? userRoles : "") + "::" + (userPermissions != null ? userPermissions : "");
        String calculatedHmac = hmacUtils.hmacHex(payloadToVerify);

        byte[] incomingBytes = incomingHmac.getBytes(StandardCharsets.UTF_8);
        byte[] calculatedBytes = calculatedHmac.getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(incomingBytes, calculatedBytes)) {
            log.warn("Invalid HMAC signature received on {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid internal HMAC signature");
            return;
        }

        // Build Granted Authorities from dynamic permissions & roles
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (userPermissions != null && !userPermissions.isBlank()) {
            for (String perm : userPermissions.split(",")) {
                if (!perm.trim().isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority(perm.trim()));
                }
            }
        }
        if (userRoles != null && !userRoles.isBlank()) {
            for (String role : userRoles.split(",")) {
                if (!role.trim().isEmpty()) {
                    String r = role.trim();
                    authorities.add(new SimpleGrantedAuthority(r.startsWith("ROLE_") ? r : "ROLE_" + r));
                }
            }
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        request.setAttribute("authenticatedUserId", userId);
        request.setAttribute("authenticatedUserRoles", userRoles != null ? userRoles : "");
        request.setAttribute("authenticatedUserPermissions", userPermissions != null ? userPermissions : "");

        filterChain.doFilter(request, response);
    }
}
