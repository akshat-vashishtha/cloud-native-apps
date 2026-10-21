package com.learning.banking.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.kafka")
@Validated
public record BankingKafkaProperties(
        @NotNull @Valid Topics topics
) {
    public record Topics(
            @NotBlank(message = "Transactions topic must not be blank")
            String transactions
    ) {}
}
