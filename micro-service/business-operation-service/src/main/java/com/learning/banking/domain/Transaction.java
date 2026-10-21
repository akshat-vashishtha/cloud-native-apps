package com.learning.banking.domain;

import com.learning.banking.domain.enums.TransactionStatus;
import com.learning.banking.domain.enums.TransactionType;

import java.time.Instant;
import java.util.Objects;

public class Transaction {

    private final String id;
    private final String sourceAccountId;
    private final String targetAccountId;
    private final Money amount;
    private final TransactionType type;
    private final TransactionStatus status;
    private final String description;
    private final Instant createdAt;

    public static Transaction createTransfer(String id, String sourceAccountId, String targetAccountId, Money amount, String description) {
        return new Transaction(
                id,
                sourceAccountId,
                targetAccountId,
                amount,
                TransactionType.TRANSFER,
                TransactionStatus.SUCCESS,
                description,
                Instant.now()
        );
    }

    public Transaction(String id, String sourceAccountId, String targetAccountId, Money amount, TransactionType type, TransactionStatus status, String description, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = Objects.requireNonNull(amount, "amount cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getId() { return id; }
    public String getSourceAccountId() { return sourceAccountId; }
    public String getTargetAccountId() { return targetAccountId; }
    public Money getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }

}
