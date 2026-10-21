package com.learning.chat.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Inbound event payload consumed from Kafka topic: banking.transactions.v1
 */
public record TransactionEventDto(
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
