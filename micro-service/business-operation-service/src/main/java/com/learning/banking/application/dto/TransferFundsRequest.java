package com.learning.banking.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferFundsRequest(

        @NotBlank(message = "Source account ID is required")
        String sourceAccountId,

        @NotBlank(message = "Target account ID is required")
        String targetAccountId,

        @NotNull( message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Transfer amount must be at least 0.01")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        String currency,

        String description
) {
}
