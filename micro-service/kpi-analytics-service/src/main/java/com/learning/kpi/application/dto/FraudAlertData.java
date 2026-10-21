package com.learning.kpi.application.dto;

import java.time.Instant;

public record FraudAlertData(
        String accountId,
        String alertType,
        String reason,
        long transactionCount,
        Instant detectedAt
) {}
