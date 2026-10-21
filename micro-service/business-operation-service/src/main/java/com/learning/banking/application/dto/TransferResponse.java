package com.learning.banking.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(String transactionId,
                               String sourceAccountId,
                               String targetAccountId,
                               BigDecimal amount,
                               String currency,
                               String status,
                               String description,
                               Instant timestamp) {
}
