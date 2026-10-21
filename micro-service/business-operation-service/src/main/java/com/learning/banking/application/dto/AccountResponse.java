package com.learning.banking.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        String id,
        String userId,
        String accountNumber,
        BigDecimal balance,
        String currency,
        String status,
        Instant createdAt
) {
}
