package com.learning.chat.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionEventDto(
        String eventId,
        String eventType,
        Instant occurredAt,
        TransactionPayload payload
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
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
