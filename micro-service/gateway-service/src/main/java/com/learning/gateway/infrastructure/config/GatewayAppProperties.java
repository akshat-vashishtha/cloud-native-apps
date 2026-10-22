package com.learning.gateway.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record GatewayAppProperties(
        @NotBlank String webBaseUrl,
        @NotBlank String sessionCookieName
) {
}
