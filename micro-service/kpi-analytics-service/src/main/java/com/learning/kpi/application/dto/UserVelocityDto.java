package com.learning.kpi.application.dto;

import java.time.Instant;

public record UserVelocityDto(
        String accountId,
        long windowTransactionCount,
        boolean isFraudRisk,
        Instant evaluatedAt
) {}
