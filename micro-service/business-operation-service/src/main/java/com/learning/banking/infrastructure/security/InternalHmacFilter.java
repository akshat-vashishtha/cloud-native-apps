package com.learning.banking.infrastructure.security;

import com.learning.banking.infrastructure.config.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

@Component
public class InternalHmacFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InternalHmacFilter.class);
    private static final long MAX_TIME_DRIFT_SECONDS = 300; // 5 Minutes replay window

    private final SecurityProperties securityProperties;
    private final HmacUtils hmacUtils;

    public InternalHmacFilter(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        this.hmacUtils = new HmacUtils(
                org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_256,
                securityProperties.internalHmacSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return requestURI.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String userRoles = request.getHeader("X-User-Roles");
        String userPermissions = request.getHeader("X-User-Permissions");
        String timeStampString = request.getHeader("X-Timestamp");
        String incomingHmac = request.getHeader("X-Internal-HMAC");

        if (userId == null || timeStampString == null || incomingHmac == null) {
            log.warn("Unauthorized internal request: Missing required security headers on {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing internal gateway authentication headers");
            return;
        }

        try {
            long requestTimeStamp = Long.parseLong(timeStampString);
            long currentTimeStamp = Instant.now().getEpochSecond();
            long absoluteTimeDifference = Math.abs(currentTimeStamp - requestTimeStamp);

            if (absoluteTimeDifference > MAX_TIME_DRIFT_SECONDS) {
                log.warn("Expired internal request. Drift: {}s on {}", absoluteTimeDifference, request.getRequestURI());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Request timestamp expired or out of allowed clock skew");
                return;
            }
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid X-Timestamp header format");
            return;
        }

        // 2. Cryptographic HMAC Verification with permissions
        String payloadToVerify = timeStampString + "::" + userId + "::" + (userRoles != null ? userRoles : "") + "::" + (userPermissions != null ? userPermissions : "");
        String calculatedHmac = hmacUtils.hmacHex(payloadToVerify);

        byte[] incomingHmacBytes = incomingHmac.getBytes(StandardCharsets.UTF_8);
        byte[] calculatedHmacBytes = calculatedHmac.getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(incomingHmacBytes, calculatedHmacBytes)) {
            log.warn("Invalid HMAC signature received on {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid internal HMAC signature");
            return;
        }

        // 3. Build Granted Authorities from dynamic permissions & roles
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
