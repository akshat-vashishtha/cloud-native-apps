package com.learning.ai.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.kafka")
@Validated
public record AiKafkaProperties(
        @NotNull @Valid Topics topics,
        @NotNull @Valid Consumer consumer
) {
    public record Topics(
            @NotBlank String chatEvents,
            @NotBlank String aiStream,
            @NotBlank String aiConfirmations
    ) {}

    public record Consumer(
            @NotBlank String groupId
    ) {}
}
