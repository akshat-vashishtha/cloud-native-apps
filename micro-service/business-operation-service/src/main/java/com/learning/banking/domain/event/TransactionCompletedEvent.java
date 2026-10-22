package com.learning.banking.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionCompletedEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        TransactionPayload payload
) {
    public record TransactionPayload(
            String transactionId,
            String sourceAccountId,
            String targetAccountId,
            String sourceUserId,
            String targetUserId,
            BigDecimal amount,
            String currency,
            String transactionType,
            String status,
            String description
    ) {}
}
