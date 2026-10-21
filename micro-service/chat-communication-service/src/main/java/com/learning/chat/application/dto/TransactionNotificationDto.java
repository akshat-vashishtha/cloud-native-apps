package com.learning.chat.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Outbound STOMP notification pushed to user's private queue: /user/{accountId}/queue/notifications
 */
public record TransactionNotificationDto(
        String notificationId,
        String type, // "CREDIT" or "DEBIT"
        String title,
        String message,
        BigDecimal amount,
        String currency,
        String transactionId,
        String accountId,
        String counterpartyAccount,
        Instant timestamp
) {}
