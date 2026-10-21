package com.learning.user.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.learning.user.application.service.RoleService;
import com.learning.user.domain.User;
import com.learning.user.infrastructure.config.JwtProperties;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;

@Getter
@Service
public class JwtTokenService {

    private final RSAPrivateKey rsaPrivateKey;
    private final RSAPublicKey rsaPublicKey;
    private final JwtProperties jwtProperties;
    private final RoleService roleService;

    public JwtTokenService(RSAPrivateKey rsaPrivateKey,
                           RSAPublicKey rsaPublicKey,
                           JwtProperties jwtProperties,
                           RoleService roleService) {
        this.rsaPrivateKey = rsaPrivateKey;
        this.rsaPublicKey = rsaPublicKey;
        this.jwtProperties = jwtProperties;
        this.roleService = roleService;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofSeconds(jwtProperties.accessTokenValiditySeconds()));
        Algorithm algorithm = Algorithm.RSA256(rsaPublicKey, rsaPrivateKey);

        Set<String> permissions = roleService.resolvePermissionsForRoles(user.getRoles());

        return JWT.create()
                .withIssuer("user-rbac-service")
                .withSubject(user.getId())
                .withClaim("email", user.getEmail())
                .withClaim("fullName", user.getFullName())
                .withClaim("roles", new ArrayList<>(user.getRoles()))
                .withClaim("permissions", new ArrayList<>(permissions))
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(algorithm);
    }

    public DecodedJWT validateToken(String token) {
        Algorithm algorithm = Algorithm.RSA256(rsaPublicKey, null);
        return JWT.require(algorithm)
                .withIssuer("user-rbac-service")
                .build()
                .verify(token);
    }

    public String validateAndExtractUserId(String token) {
        return validateToken(token).getSubject();
    }
}
