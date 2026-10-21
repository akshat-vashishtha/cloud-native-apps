package com.learning.user.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.jwt")
@Validated
public record JwtProperties(
        @Positive(message = "Token validity seconds must be positive")
        long accessTokenValiditySeconds,

        @NotBlank(message = "JWT issuer must not be blank")
        String issuer
) {}