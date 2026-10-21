package com.learning.gateway.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.learning.gateway.application.dto.ValidatedClaims;
import com.learning.gateway.infrastructure.config.GatewaySecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class JwtPublicKeyValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtPublicKeyValidator.class);

    private final WebClient webClient;
    private final GatewaySecurityProperties securityProperties;
    private JWTVerifier verifier;

    public JwtPublicKeyValidator(WebClient.Builder webClientBuilder,
                                 GatewaySecurityProperties securityProperties) {
        this.webClient = webClientBuilder.build();
        this.securityProperties = securityProperties;
    }

    public ValidatedClaims validate(String token) {
        try {
            ensureVerifierInitialized();
            DecodedJWT decodedJWT = verifier.verify(token);
            String userId = decodedJWT.getSubject();
            String email = decodedJWT.getClaim("email").asString();
            List<String> roles = decodedJWT.getClaim("roles").asList(String.class);
            List<String> permissions = decodedJWT.getClaim("permissions").asList(String.class);
            return new ValidatedClaims(userId, email, roles != null ? roles : List.of(), permissions != null ? permissions : List.of());
        } catch (JWTVerificationException ex) {
            log.warn("Invalid or expired JWT token: {}", ex.getMessage());
            return null;
        } catch (Exception ex) {
            log.error("Failed to verify JWT token", ex);
            return null;
        }
    }

    private synchronized void ensureVerifierInitialized() {
        if (this.verifier != null) {
            return;
        }

        try {
            Map<String, String> response = webClient.get().uri(securityProperties.userServiceUri() + "/api/v1/auth/public-key")
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                    .block();
            if (response == null || !response.containsKey("publicKey")) {
                throw new IllegalStateException("Failed to obtain public key from user-rbac-service");
            }

            String publicKeyBase64 = response.get("publicKey");
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(encodedKeySpec);

            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            this.verifier = JWT.require(algorithm).withIssuer("user-rbac-service").build();

            log.info("Successfully fetched public key from user-rbac-service [{}] and initialized RS256 verifier", securityProperties.userServiceUri());
        } catch (Exception e) {
            log.error("Could not fetch public key from user-rbac-service on {}", securityProperties.userServiceUri(), e);
            throw new IllegalStateException("Authentication service unavailable", e);
        }
    }
}
