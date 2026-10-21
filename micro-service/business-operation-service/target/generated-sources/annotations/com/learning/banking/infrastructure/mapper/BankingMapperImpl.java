package com.learning.banking.infrastructure.mapper;

import com.learning.banking.domain.Account;
import com.learning.banking.domain.Card;
import com.learning.banking.domain.Money;
import com.learning.banking.domain.Transaction;
import com.learning.banking.domain.enums.AccountStatus;
import com.learning.banking.domain.enums.CardStatus;
import com.learning.banking.domain.enums.TransactionStatus;
import com.learning.banking.domain.enums.TransactionType;
import com.learning.banking.infrastructure.persistence.document.AccountDocument;
import com.learning.banking.infrastructure.persistence.document.CardDocument;
import com.learning.banking.infrastructure.persistence.document.TransactionDocument;
import java.math.BigDecimal;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-21T13:34:07+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.200.v20261008-1115, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class BankingMapperImpl implements BankingMapper {

    @Override
    public AccountDocument toDocument(Account account) {
        if ( account == null ) {
            return null;
        }

        AccountDocument.AccountDocumentBuilder accountDocument = AccountDocument.builder();

        accountDocument.balanceAmount( accountBalanceAmount( account ) );
        accountDocument.currency( accountBalanceCurrency( account ) );
        accountDocument.accountNumber( account.getAccountNumber() );
        accountDocument.createdAt( account.getCreatedAt() );
        accountDocument.id( account.getId() );
        accountDocument.status( account.getStatus() );
        accountDocument.userId( account.getUserId() );

        return accountDocument.build();
    }

    @Override
    public Account toDomain(AccountDocument document) {
        if ( document == null ) {
            return null;
        }

        String id = null;
        String userId = null;
        String accountNumber = null;
        AccountStatus status = null;
        Instant createdAt = null;

        id = document.getId();
        userId = document.getUserId();
        accountNumber = document.getAccountNumber();
        status = document.getStatus();
        createdAt = document.getCreatedAt();

        Money balance = mapMoney(document.getBalanceAmount(), document.getCurrency());
        Instant updatedAt = null;

        Account account = new Account( id, userId, accountNumber, balance, status, createdAt, updatedAt );

        return account;
    }

    @Override
    public TransactionDocument toDocument(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        TransactionDocument.TransactionDocumentBuilder transactionDocument = TransactionDocument.builder();

        transactionDocument.amount( transactionAmountAmount( transaction ) );
        transactionDocument.currency( transactionAmountCurrency( transaction ) );
        transactionDocument.createdAt( transaction.getCreatedAt() );
        transactionDocument.description( transaction.getDescription() );
        transactionDocument.id( transaction.getId() );
        transactionDocument.sourceAccountId( transaction.getSourceAccountId() );
        transactionDocument.status( transaction.getStatus() );
        transactionDocument.targetAccountId( transaction.getTargetAccountId() );
        transactionDocument.type( transaction.getType() );

        return transactionDocument.build();
    }

    @Override
    public Transaction toDomain(TransactionDocument document) {
        if ( document == null ) {
            return null;
        }

        String id = null;
        String sourceAccountId = null;
        String targetAccountId = null;
        TransactionType type = null;
        TransactionStatus status = null;
        String description = null;
        Instant createdAt = null;

        id = document.getId();
        sourceAccountId = document.getSourceAccountId();
        targetAccountId = document.getTargetAccountId();
        type = document.getType();
        status = document.getStatus();
        description = document.getDescription();
        createdAt = document.getCreatedAt();

        Money amount = mapMoney(document.getAmount(), document.getCurrency());

        Transaction transaction = new Transaction( id, sourceAccountId, targetAccountId, amount, type, status, description, createdAt );

        return transaction;
    }

    @Override
    public CardDocument toDocument(Card card) {
        if ( card == null ) {
            return null;
        }

        CardDocument.CardDocumentBuilder cardDocument = CardDocument.builder();

        cardDocument.accountId( card.getAccountId() );
        cardDocument.cardNumberMasked( card.getCardNumberMasked() );
        cardDocument.createdAt( card.getCreatedAt() );
        cardDocument.expiryDate( card.getExpiryDate() );
        cardDocument.id( card.getId() );
        cardDocument.status( card.getStatus() );
        cardDocument.userId( card.getUserId() );

        return cardDocument.build();
    }

    @Override
    public Card toDomain(CardDocument document) {
        if ( document == null ) {
            return null;
        }

        String id = null;
        String accountId = null;
        String userId = null;
        String cardNumberMasked = null;
        CardStatus status = null;
        String expiryDate = null;
        Instant createdAt = null;

        id = document.getId();
        accountId = document.getAccountId();
        userId = document.getUserId();
        cardNumberMasked = document.getCardNumberMasked();
        status = document.getStatus();
        expiryDate = document.getExpiryDate();
        createdAt = document.getCreatedAt();

        Card card = new Card( id, accountId, userId, cardNumberMasked, status, expiryDate, createdAt );

        return card;
    }

    private BigDecimal accountBalanceAmount(Account account) {
        if ( account == null ) {
            return null;
        }
        Money balance = account.getBalance();
        if ( balance == null ) {
            return null;
        }
        BigDecimal amount = balance.amount();
        if ( amount == null ) {
            return null;
        }
        return amount;
    }

    private String accountBalanceCurrency(Account account) {
        if ( account == null ) {
            return null;
        }
        Money balance = account.getBalance();
        if ( balance == null ) {
            return null;
        }
        String currency = balance.currency();
        if ( currency == null ) {
            return null;
        }
        return currency;
    }

    private BigDecimal transactionAmountAmount(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }
        Money amount = transaction.getAmount();
        if ( amount == null ) {
            return null;
        }
        BigDecimal amount1 = amount.amount();
        if ( amount1 == null ) {
            return null;
        }
        return amount1;
    }

    private String transactionAmountCurrency(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }
        Money amount = transaction.getAmount();
        if ( amount == null ) {
            return null;
        }
        String currency = amount.currency();
        if ( currency == null ) {
            return null;
        }
        return currency;
    }
}
