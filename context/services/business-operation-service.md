# Service: Business-Operation-Service (Digital Banking Core)
Status: implemented   Owner: @Alex, @Elena, @Leo   Updated: 2026-10-22
Inputs: context/architecture.md, context/implementation.md

## Summary
Digital banking core: accounts, transfers, cards; MongoDB `banking_ops_db` (`accounts`, `transactions`, `cards`, **`outbox_events`**); outbox poller → `banking.transactions.v1`; Redis idempotency + account cache; gRPC for future AI tools. Security: **JWT resource server** (HMAC retired). Single-DB transfers today — multi-service saga (ARCH-009) and `banking.accounts.v1` are **not** implemented yet.

### Business flow (live)
```
Client → Gateway TokenRelay → REST /api/v1/banking/**
  → BankingServiceImpl (domain debit/credit, @Version retry)
  → write transaction + outbox_events (PENDING) same Mongo txn
  → OutboxEventPublisher poll → Kafka banking.transactions.v1
       → KPI (velocity/metrics)
       → Chat (STOMP notifications)
```

### Best-practice gaps (see platform-best-practices.md)
- Outbox poll should **atomically claim** via Mongo `Criteria`/`findAndModify` (PBP-004 / §1).
- gRPC **server** exists; **clients** (AI) not yet (PBP-005).
- Prefer Criteria for complex account/txn queries instead of only derived repos.

---

## Decisions

- **SVC-BIZ-001**: **Pure Domain Invariants & Money Value Object**
  - *Why*: State transitions (e.g., `debit()`, `credit()`, `freeze()`) are guarded inside domain models with an immutable `Money` record (`BigDecimal amount`, `Currency currency`), completely decoupled from Spring/MongoDB annotations.
  - *Rejected*: Anemic domain models with logic scattered in services.

- **SVC-BIZ-002**: **Dual Presentation (REST Controller + gRPC Server)**
  - *Why*: REST API for web/mobile client operations via Gateway, and Protobuf/gRPC (`banking_service.proto`) for low-latency synchronous AI tool calls from `Intelligence-Ai-Service`.
  - *Rejected*: Forcing internal AI service to call external REST APIs.

- **SVC-BIZ-003**: **Scheduled Outbox Runner with Optimistic Locking**
  - *Why*: Background Spring task polls `outbox_events` with `status: PENDING`, locks using `@Version`, publishes to `banking.transactions.v1`, and marks `PUBLISHED`.
  - *Rejected*: Direct Kafka publishing inside the HTTP request thread.

- **SVC-BIZ-004**: **Kafka Partition Key on `sourceAccountId`**
  - *Why*: Ensures strict chronological FIFO delivery of debit and credit events on Kafka consumer partitions in `KPI-Analytics-Service`.
  - *Rejected*: Random round-robin partitioning causing out-of-order ledger events.

- **SVC-BIZ-005**: **`@Retryable` Optimistic Concurrency Control**
  - *Why*: Handles simultaneous balance mutations safely using MongoDB `@Version` with Spring `@Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)`.
  - *Rejected*: Uncontrolled balance overrides or heavy distributed table locks.

- **SVC-BIZ-006**: **MongoDB Automated Outbox Purge via TTL Index**
  - *Why*: TTL index on `processedAt` (`expireAfter = "7d"`) ensures published outbox events are automatically purged after 7 days, maintaining a tiny collection size and fast polling index.
  - *Rejected*: Manual batch deletion scripts.

---

## Details

### 1. Domain Layer (Pure Java - Zero Framework Leaks)

#### Value Objects & Enums
```java
public record Money(BigDecimal amount, String currency) {
    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount()), this.currency);
    }
    public Money subtract(Money other) {
        validateSameCurrency(other);
        if (this.amount.compareTo(other.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient account balance");
        }
        return new Money(this.amount.subtract(other.amount()), this.currency);
    }
    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency())) {
            throw new CurrencyMismatchException("Currency mismatch");
        }
    }
}

public enum AccountStatus { ACTIVE, FROZEN, CLOSED }
public enum TransactionType { TRANSFER, DEPOSIT, WITHDRAWAL }
public enum TransactionStatus { PENDING, COMPLETED, FAILED }
public enum CardStatus { ACTIVE, LOCKED, BLOCKED }
```

#### Domain Entities & Aggregate Root
```java
public class BankAccount {
    private final String id;
    private final String userId;
    private final String accountNumber;
    private Money balance;
    private AccountStatus status;
    private final Instant createdAt;

    public BankAccount(String id, String userId, String accountNumber, Money initialBalance) {
        this.id = id;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public void debit(Money amount) {
        ensureActive();
        this.balance = this.balance.subtract(amount);
    }

    public void credit(Money amount) {
        ensureActive();
        this.balance = this.balance.add(amount);
    }

    public void freeze() {
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot freeze a closed account");
        }
        this.status = AccountStatus.FROZEN;
    }

    public void unfreeze() {
        if (this.status != AccountStatus.FROZEN) {
            throw new IllegalStateException("Account is not frozen");
        }
        this.status = AccountStatus.ACTIVE;
    }

    private void ensureActive() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Account is " + this.status);
        }
    }
    // Getters...
}
```

---

### 2. Application & Service Layer

#### Service Interface
```java
public interface BankingService {
    AccountDto openAccount(OpenAccountCommand command);
    TransferResultDto executeTransfer(TransferCommand command, String idempotencyKey);
    AccountDto getAccountBalance(String accountId, String authenticatedUserId);
    CardDto updateCardStatus(String cardId, CardStatus newStatus, String authenticatedUserId);
}
```

#### Transactional Transfer Workflow (Single MongoDB Transaction)
```java
@Service
public class BankingServiceImpl implements BankingService {

    private final AccountMongoRepository accountRepository;
    private final TransactionMongoRepository transactionRepository;
    private final OutboxMongoRepository outboxRepository;
    private final BankingMapper mapper;

    @Transactional
    @Override
    public TransferResultDto executeTransfer(TransferCommand cmd, String idempotencyKey) {
        // 1. Load source & target accounts
        BankAccount source = accountRepository.findById(cmd.sourceAccountId())
            .map(mapper::toDomain)
            .orElseThrow(() -> new AccountNotFoundException("Source account not found"));
        
        BankAccount target = accountRepository.findById(cmd.targetAccountId())
            .map(mapper::toDomain)
            .orElseThrow(() -> new AccountNotFoundException("Target account not found"));

        // 2. Execute Domain logic (Invariants enforced)
        Money transferMoney = new Money(cmd.amount(), cmd.currency());
        source.debit(transferMoney);
        target.credit(transferMoney);

        // 3. Save updated account documents
        accountRepository.save(mapper.toDocument(source));
        accountRepository.save(mapper.toDocument(target));

        // 4. Save transaction ledger document
        TransactionDocument txDoc = new TransactionDocument(
            UUID.randomUUID().toString(),
            source.getId(),
            target.getId(),
            cmd.amount(),
            cmd.currency(),
            TransactionType.TRANSFER,
            TransactionStatus.COMPLETED,
            cmd.description(),
            Instant.now()
        );
        transactionRepository.save(txDoc);

        // 5. Insert Transactional Outbox Event
        OutboxDocument outboxEvent = new OutboxDocument(
            UUID.randomUUID().toString(),
            "TRANSACTION_COMPLETED",
            source.getId(),
            toJson(new TransactionCompletedEvent(txDoc.getId(), source.getId(), target.getId(), cmd.amount(), cmd.currency(), Instant.now())),
            OutboxStatus.PENDING,
            Instant.now()
        );
        outboxRepository.save(outboxEvent);

        return new TransferResultDto(txDoc.getId(), "SUCCESS", "Transfer completed successfully");
    }
}
```

---

### 3. Outbox Publisher Runner

```java
@Component
public class OutboxEventPublisher {

    private final OutboxMongoRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 500)
    public void publishPendingOutboxEvents() {
        List<OutboxDocument> pendingEvents = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        for (OutboxDocument event : pendingEvents) {
            try {
                kafkaTemplate.send("banking.transactions.v1", event.getAggregateId(), event.getPayload()).get();
                event.setStatus(OutboxStatus.PUBLISHED);
                event.setProcessedAt(Instant.now());
                outboxRepository.save(event);
            } catch (Exception ex) {
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() > 5) {
                    event.setStatus(OutboxStatus.FAILED);
                }
                outboxRepository.save(event);
            }
        }
    }
}
```

---

### 4. gRPC Interface (`banking_service.proto`)

```protobuf
syntax = "proto3";

package banking.v1;

option java_multiple_files = true;
option java_package = "com.banking.grpc.v1";

service BankingGrpcService {
  rpc GetAccountBalance (GetBalanceRequest) returns (GetBalanceResponse);
  rpc TransferFunds (TransferFundsRequest) returns (TransferFundsResponse);
  rpc UpdateCardStatus (UpdateCardRequest) returns (UpdateCardResponse);
}

message GetBalanceRequest {
  string account_id = 1;
  string user_id = 2;
}

message GetBalanceResponse {
  string account_id = 1;
  double balance = 2;
  string currency = 3;
  string status = 4;
}

message TransferFundsRequest {
  string source_account_id = 1;
  string target_account_id = 2;
  double amount = 3;
  string currency = 4;
  string user_id = 5;
}

message TransferFundsResponse {
  string transaction_id = 1;
  string status = 2;
  string message = 3;
}

message UpdateCardRequest {
  string card_id = 1;
  string status = 2;
  string user_id = 3;
}

message UpdateCardResponse {
  string card_id = 1;
  string status = 2;
}
```

---

### 5. Persistence Layer (MongoDB Documents)

```java
@Document(collection = "accounts")
public class AccountDocument {
    @Id private String id;
    @Indexed private String userId;
    @Indexed(unique = true) private String accountNumber;
    private BigDecimal balance;
    private String currency;
    private AccountStatus status;
    private Instant createdAt;
    @Version private Long version; // Optimistic locking
}

@Document(collection = "transactions")
@CompoundIndexes({
    @CompoundIndex(name = "src_created_idx", def = "{'sourceAccountId': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "tgt_created_idx", def = "{'targetAccountId': 1, 'createdAt': -1}")
})
public class TransactionDocument {
    @Id private String id;
    private String sourceAccountId;
    private String targetAccountId;
    private BigDecimal amount;
    private String currency;
    private TransactionType type;
    private TransactionStatus status;
    private String description;
    private Instant createdAt;
}

@Document(collection = "outbox_events")
@CompoundIndex(name = "status_created_idx", def = "{'status': 1, 'createdAt': 1}")
public class OutboxDocument {
    @Id private String id;
    private String eventType;
    private String aggregateId;
    private String payload;
    private OutboxStatus status;
    private int retryCount;
    private Instant createdAt;
    private Instant processedAt;
    @Version private Long version;
}
```

---

## Plan
Approved: yes

- [x] 1. Domain models (`Money`, `Account`, `Card`, `AccountStatus`, `TransactionType`)
- [x] 2. MongoDB documents (`AccountDocument`, `TransactionDocument`, `CardDocument`, `OutboxEventDocument`) & Repositories
- [x] 3. Application service & transfer workflow with transactional outbox
- [x] 4. Scheduled outbox publisher to Kafka topic `banking.transactions.v1`
- [x] 5. Protobuf definitions & gRPC service implementation (`BankingGrpcServiceImpl`)
- [x] 6. REST Controllers with `@Valid` DTOs and HMAC security filter (`InternalHmacFilter`)
- [ ] 7. Integration tests using Testcontainers (MongoDB + Kafka)

## Code Progress
- All core layers implemented: Domain, Persistence, Application, REST Presentation, Zero-Trust Gateway HMAC verification, Kafka Outbox publisher, and gRPC Server.
- Clean build verified: `mvn clean compile` succeeds with 0 errors (`BUILD SUCCESS`).

## Open questions
- None.

## Change requests (to upstream files)
- None.

## Next steps
- Ready for integration testing or next microservice (`gateway-service`).
