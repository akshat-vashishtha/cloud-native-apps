package com.learning.oauth2.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record OAuth2ServerProperties(
        @NotBlank String issuerUri,
        @NotBlank String userServiceUri,
        @NotBlank String internalApiKey,
        GatewayClient gatewayClient
) {
    @Validated
    public record GatewayClient(
            @NotBlank String clientId,
            @NotBlank String clientSecret,
            @NotBlank String redirectUri
    ) {
    }
}
