package com.learning.banking.domain;

import com.learning.banking.domain.enums.AccountStatus;
import com.learning.banking.domain.exception.AccountInactiveException;

import java.time.Instant;
import java.util.Objects;

public class Account {

    private final String id;
    private final String userId;
    private final String accountNumber;
    private Money balance;
    private AccountStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public static Account createNew(String id, String userId, String accountNumber, String currency) {
        return new Account(id, userId, accountNumber, Money.zero(currency), AccountStatus.ACTIVE, Instant.now(), Instant.now());
    }

    public Account(String id, String userId, String accountNumber, Money balance, AccountStatus status, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.accountNumber = Objects.requireNonNull(accountNumber, "accountNumber cannot be null");
        this.balance = Objects.requireNonNull(balance, "balance cannot be null");
        this.status = status != null ? status : AccountStatus.ACTIVE;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public void debit(Money amount) {
        validateActiveStatus();
        this.balance = this.balance.subtract(amount);
        this.updatedAt =  Instant.now();
    }

    public void credit(Money amount) {
        validateActiveStatus();
        this.balance = this.balance.add(amount);
        this.updatedAt = Instant.now();
    }

    public void freeze() {
        this.status = AccountStatus.FROZEN;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    private void validateActiveStatus() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new AccountInactiveException("Account " + accountNumber + " is not active. Current status: " + status);
        }
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getAccountNumber() { return accountNumber; }
    public Money getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

}
