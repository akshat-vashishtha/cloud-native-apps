package com.learning.banking.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.banking.application.dto.AccountResponse;
import com.learning.banking.application.dto.CreateAccountRequest;
import com.learning.banking.application.dto.TransferFundsRequest;
import com.learning.banking.application.dto.TransferResponse;
import com.learning.banking.application.exception.AccountNotFoundException;
import com.learning.banking.application.exception.CardNotFoundException;
import com.learning.banking.domain.Account;
import com.learning.banking.domain.Card;
import com.learning.banking.domain.Money;
import com.learning.banking.domain.Transaction;
import com.learning.banking.domain.enums.CardStatus;
import com.learning.banking.domain.event.TransactionCompletedEvent;
import com.learning.banking.domain.exception.ConcurrentTransactionException;
import com.learning.banking.domain.exception.UnauthorizedAccountAccessException;
import com.learning.banking.infrastructure.cache.IdempotencyService;
import com.learning.banking.infrastructure.mapper.BankingMapper;
import com.learning.banking.infrastructure.persistence.document.AccountDocument;
import com.learning.banking.infrastructure.persistence.document.CardDocument;
import com.learning.banking.infrastructure.persistence.document.OutboxEventDocument;
import com.learning.banking.infrastructure.persistence.document.TransactionDocument;
import com.learning.banking.infrastructure.persistence.repository.AccountRepository;
import com.learning.banking.infrastructure.persistence.repository.CardRepository;
import com.learning.banking.infrastructure.persistence.repository.OutboxEventRepository;
import com.learning.banking.infrastructure.persistence.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class BankingServiceImpl implements BankingService {

    private static final Logger log = LoggerFactory.getLogger(BankingServiceImpl.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final BankingMapper bankingMapper;
    private final ObjectMapper objectMapper;
    private final IdempotencyService idempotencyService;

    public BankingServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, CardRepository cardRepository,
                              OutboxEventRepository outboxEventRepository, BankingMapper bankingMapper, ObjectMapper objectMapper, IdempotencyService idempotencyService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.bankingMapper = bankingMapper;
        this.objectMapper = objectMapper;
        this.idempotencyService = idempotencyService;
    }

    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {
        String accountId = UUID.randomUUID().toString();
        String accountNumber = generateAccountNumber();
        Account account = Account.createNew(accountId, request.userId(), accountNumber, request.currency());
        AccountDocument accountDocument = accountRepository.save(bankingMapper.toDocument(account));
        log.info("Created new account [{}] for user [{}]", accountNumber, request.userId());
        return toAccountResponse(bankingMapper.toDomain(accountDocument));
    }

    @Override
    @Cacheable(value = "accounts", key = "#accountId")
    public AccountResponse getAccountById(String accountId) {
       AccountDocument accountDocument = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));
        return toAccountResponse(bankingMapper.toDomain(accountDocument));
    }

    @Override
    public AccountResponse getAccountByAccountNumber(String accountNumber) {
        AccountDocument accountDocument = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new AccountNotFoundException("Account not found with account number: " + accountNumber));
        return toAccountResponse(bankingMapper.toDomain(accountDocument));
    }

    @Override
    @Retryable (retryFor = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 50, multiplier = 2))
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "accounts", key = "#request.sourceAccountId()"),
            @CacheEvict(value = "accounts", key = "#request.targetAccountId()")
    })
    public TransferResponse transferFunds(TransferFundsRequest request, String authenticatedUserId, String idempotencyKey) {

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<TransferResponse> cachedResponse = idempotencyService.getResponse(idempotencyKey);
            if (cachedResponse.isPresent()) {
                log.info("Idempotency hit! Returning cached transfer response for key: {}", idempotencyKey);
                return cachedResponse.get();
            }

            boolean acquireLock = idempotencyService.acquireLock(idempotencyKey);
            if (!acquireLock) {
                log.warn("Concurrent duplicate request detected for idempotency key: {}", idempotencyKey);
                throw new ConcurrentTransactionException("A transaction with this idempotency key is currently being processed");
            }
        }

        try {
            AccountDocument sourceAccountDocument = accountRepository.findById(request.sourceAccountId()).orElseThrow(() -> new AccountNotFoundException("Source account not found: " + request.sourceAccountId()));
            AccountDocument targetAccountDocument = accountRepository.findById(request.targetAccountId()).orElseThrow(() -> new AccountNotFoundException("Target account not found: " + request.targetAccountId()));

            // Ownership Validation: Only account owner can debit funds!
            if (authenticatedUserId != null && !sourceAccountDocument.getUserId().equals(authenticatedUserId)) {
                log.warn("Security violation: User [{}] attempted to debit account [{}] owned by [{}]",
                        authenticatedUserId, request.sourceAccountId(), sourceAccountDocument.getUserId());
                throw new UnauthorizedAccountAccessException("You are not authorized to transfer funds from this account");
            }

            Account sourceAccount = bankingMapper.toDomain(sourceAccountDocument);
            Account targetAccount = bankingMapper.toDomain(targetAccountDocument);

            Money transferAmountMoney = Money.of(request.amount(), request.currency());
            sourceAccount.debit(transferAmountMoney);
            targetAccount.credit(transferAmountMoney);

            accountRepository.save(bankingMapper.toDocument(sourceAccount));
            accountRepository.save(bankingMapper.toDocument(targetAccount));

            String transactionId = UUID.randomUUID().toString();
            Transaction transaction = Transaction.createTransfer(transactionId, sourceAccount.getId(), targetAccount.getId(), transferAmountMoney, request.description());
            TransactionDocument transactionDocument = transactionRepository.save(bankingMapper.toDocument(transaction));
            saveOutboxEvent(transactionDocument, sourceAccount.getId(), targetAccount.getId(), request);

            log.info("Transferred {} {} from account [{}] to account [{}]",
                    request.amount(), request.currency(), sourceAccount.getAccountNumber(), targetAccount.getAccountNumber());
            TransferResponse response = new TransferResponse(
                    transactionId,
                    sourceAccount.getId(),
                    targetAccount.getId(),
                    request.amount(),
                    request.currency(),
                    "SUCCESS",
                    request.description(),
                    transactionDocument.getCreatedAt()
            );

            // Cache response in Redis for 24h to support idempotent replays
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                idempotencyService.saveResponse(idempotencyKey, response);
            }

            return response;
        } catch (Exception e) {
            // Release idempotency lock on failure so client can correct and retry
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                idempotencyService.releaseLock(idempotencyKey);
            }
            throw e;
        }
    }

    @Override
    public void updateCardStatus(String cardId, String userId, CardStatus status) {
        CardDocument cardDocument = cardRepository.findByIdAndUserId(cardId, userId).orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + cardId + " for user: " + userId));
        Card card = bankingMapper.toDomain(cardDocument);
        if (status == CardStatus.LOCKED) {
            card.lock();
        } else if (status == CardStatus.ACTIVE) {
            card.unlock();
        }
        cardRepository.save(bankingMapper.toDocument(card));
        log.info("Updated card [{}] status to [{}] for user [{}]", cardId, status, userId);
    }

    private void saveOutboxEvent(TransactionDocument transactionDocument, String sourceAccId, String targetAccId, TransferFundsRequest request) {
        try {
            TransactionCompletedEvent.TransactionPayload payload = new TransactionCompletedEvent.TransactionPayload(
                    transactionDocument.getId(),
                    sourceAccId,
                    targetAccId,
                    request.amount(),
                    request.currency(),
                    "TRANSFER",
                    "SUCCESS",
                    request.description()
            );
            TransactionCompletedEvent event = new TransactionCompletedEvent(
                    UUID.randomUUID().toString(),
                    "TRANSACTION_COMPLETED",
                    Instant.now(),
                    payload
            );
            String jsonPayload = objectMapper.writeValueAsString(event);
            OutboxEventDocument outboxDoc = OutboxEventDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("TRANSACTION")
                    .aggregateId(sourceAccId) // Used as Kafka Partition Key for FIFO ordering!
                    .eventType("TRANSACTION_COMPLETED")
                    .payload(jsonPayload)
                    .status("PENDING")
                    .createdAt(Instant.now())
                    .build();
            outboxEventRepository.save(outboxDoc);
        } catch (Exception e) {
            log.error("Failed to serialize outbox event for transaction: {}", transactionDocument.getId(), e);
            throw new IllegalStateException("Failed to create outbox event", e);
        }
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUserId(),
                account.getAccountNumber(),
                account.getBalance().amount(),
                account.getBalance().currency(),
                account.getStatus().name(),
                account.getCreatedAt()
        );
    }

    private String generateAccountNumber() {
        long accountNumber = ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
        return String.valueOf(accountNumber);
    }
}
