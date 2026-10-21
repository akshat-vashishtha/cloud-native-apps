package com.learning.chat.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.kafka")
@Validated
public record ChatKafkaProperties(
        @NotNull @Valid Topics topics,
        @NotNull @Valid Consumer consumer
) {
    public record Topics(
            @NotBlank(message = "Chat events topic must not be blank")
            String chatEvents,
            @NotBlank(message = "Transactions topic must not be blank")
            String transactions
    ) {}

    public record Consumer(
            @NotBlank(message = "Kafka consumer group ID must not be blank")
            String groupId
    ) {}
}
