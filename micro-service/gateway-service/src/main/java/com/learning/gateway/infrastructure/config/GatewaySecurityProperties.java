package com.learning.gateway.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.security")
@Validated
public record GatewaySecurityProperties(
        @NotBlank(message = "Internal HMAC secret must not be blank")
        String internalHmacSecret,

        @NotBlank(message = "User service URI must not be blank")
        String userServiceUri
) { }
