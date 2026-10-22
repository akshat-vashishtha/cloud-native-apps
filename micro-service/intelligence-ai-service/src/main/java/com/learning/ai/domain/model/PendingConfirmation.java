package com.learning.ai.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record PendingConfirmation(
        String confirmationToken,
        String userId,
        String actionType,
        String sourceAccountId,
        String targetAccountId,
        BigDecimal amount,
        String currency,
        String description,
        String cardId,
        String cardStatus,
        String summary,
        Instant expiresAt
) {
    public PendingConfirmation {
        Objects.requireNonNull(confirmationToken, "Confirmation token is required");
        Objects.requireNonNull(userId, "User ID is required");
        Objects.requireNonNull(actionType, "Action type is required");
        Objects.requireNonNull(expiresAt, "Expiry is required");
    }
}
