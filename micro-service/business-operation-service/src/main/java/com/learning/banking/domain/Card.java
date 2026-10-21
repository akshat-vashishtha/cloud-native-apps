package com.learning.banking.domain;

import com.learning.banking.domain.enums.CardStatus;

import java.time.Instant;
import java.util.Objects;

public class Card {

    private final String id;
    private final String accountId;
    private final String userId;
    private final String cardNumberMasked; // e.g. "**** **** **** 1234"
    private CardStatus status;
    private final String expiryDate; // e.g. "12/28"
    private final Instant createdAt;

    public static Card createNew(String id, String accountId, String userId, String cardNumberMasked, String expiryDate) {
        return new Card(id, accountId, userId, cardNumberMasked, CardStatus.ACTIVE, expiryDate, Instant.now());
    }

    public Card(String id, String accountId, String userId, String cardNumberMasked,
                CardStatus status, String expiryDate, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.accountId = Objects.requireNonNull(accountId, "accountId cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.cardNumberMasked = Objects.requireNonNull(cardNumberMasked, "cardNumberMasked cannot be null");
        this.status = status != null ? status : CardStatus.ACTIVE;
        this.expiryDate = Objects.requireNonNull(expiryDate, "expiryDate cannot be null");
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public void lock() {
        this.status = CardStatus.LOCKED;
    }

    public void unlock() {
        this.status = CardStatus.ACTIVE;
    }

    // Getters
    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public String getUserId() { return userId; }
    public String getCardNumberMasked() { return cardNumberMasked; }
    public CardStatus getStatus() { return status; }
    public String getExpiryDate() { return expiryDate; }
    public Instant getCreatedAt() { return createdAt; }
}
