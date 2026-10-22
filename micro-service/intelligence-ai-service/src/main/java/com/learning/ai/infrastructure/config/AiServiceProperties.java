package com.learning.ai.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.ai")
@Validated
public record AiServiceProperties(
        @Positive int shortTermMemoryWindow,
        @Positive long semanticCacheTtlHours,
        @Positive long confirmationTtlMinutes,
        @Positive long grpcDeadlineMs
) {}
