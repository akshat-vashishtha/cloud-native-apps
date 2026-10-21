package com.learning.kpi.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.kafka")
@Validated
public record KpiKafkaProperties(
        @NotNull @Valid Topics topics,
        @NotNull @Valid Consumer consumer,
        @NotNull @Valid Velocity velocity
) {
    public record Topics(
            @NotBlank(message = "Transactions topic must not be blank")
            String transactions,

            @NotBlank(message = "Fraud alerts topic must not be blank")
            String fraudAlerts
    ) {}

    public record Consumer(
            @NotBlank(message = "Consumer group ID must not be blank")
            String groupId
    ) {}

    public record Velocity(
            @Positive(message = "Velocity window minutes must be positive")
            long windowMinutes,

            @Positive(message = "Velocity alert threshold must be positive")
            long threshold
    ) {}
}
