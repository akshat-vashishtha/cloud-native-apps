package com.learning.ai.application.dto;

import java.time.Instant;

public record ConfirmActionResponseDto(
        String confirmationToken,
        String actionType,
        String status,
        String message,
        Instant executedAt
) {}
