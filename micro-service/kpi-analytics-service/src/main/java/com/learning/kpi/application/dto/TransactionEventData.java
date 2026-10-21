package com.learning.kpi.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionEventData(
        String eventId,
        String eventType,
        Instant occurredAt,
        TransactionPayload payload
) {
    public record TransactionPayload(
            String transactionId,
            String sourceAccountId,
            String targetAccountId,
            BigDecimal amount,
            String currency,
            String transactionType,
            String status,
            String description
    ) {}
}
