# Technical Implementation Design
Status: agreed   Owner: @Elena & @Leo (Reviewer: @Alex)   Updated: 2026-10-22
Inputs: context/architecture.md, context/platform-best-practices.md

## Summary
Technical design for Kafka, STOMP, MongoDB, Redis, KPI, and banking flows. **Live vs planned** called out below. Industry target practices (outbox claim, DLT, STOMP server→client by `userId`, Mongo Criteria, gRPC clients, agentic Spring AI) are normative in [`platform-best-practices.md`](platform-best-practices.md)—do not treat this file alone as complete.

---

## Decisions

- **IMPL-001**: **Transactional Outbox & Domain Event Streams (Kafka)**
  - *Why*: Guarantees zero message loss during database writes in `Business-Operation-Service`. Critical events written to `outbox_events` in the same Mongo txn, then published to Kafka.
  - *Status*: **Partial** — banking outbox live; chat/fraud still fire-and-forget or produce-only. Target: PBP-001 + atomic Criteria claim ([platform-best-practices.md](platform-best-practices.md) §1).
  - *Rejected*: Direct publishing inside HTTP threads for ledger events.

- **IMPL-002**: **Idempotent Consumer & Exponential Backoff DLQ Topics**
  - *Why*: Consumers must dedupe by `eventId` and use Spring Kafka retry + DLT.
  - *Status*: **Not implemented yet** (docs previously overstated). KPI/Chat listeners can swallow errors. Target: PBP-002.
  - *Rejected*: Blind processing / catch-and-commit after side effects.

- **IMPL-003**: **Spring AI Agentic Concierge + gRPC Tools**
  - *Why*: `ChatClient` + routing/specialist agents + `@Tool` backed by **gRPC clients** to Business-Ops; HITL confirm over STOMP; stream tokens server→client.
  - *Status*: **Design only**. Normative detail: platform-best-practices §6 + `services/intelligence-ai-service.md`.
  - *Rejected*: Unconstrained auto-transfer from a single prompt; REST for internal AI tools when proto exists.

- **IMPL-004**: **MongoDB Isolation + Criteria for Complex Access**
  - *Why*: DB-per-service + compound indexes; **Criteria/`MongoTemplate`** for atomic claim, upsert, multi-field queries (PBP-004). Derived repositories remain for simple CRUD.
  - *Status*: **Partial** — repos dominant; Criteria mainly in KPI upsert.
  - *Rejected*: Encoding all queries as long derived method names; `findAll` + filter in memory.

- **IMPL-005**: **Redis Multi-Tier Namespace & TTL Strategy**
  - *Why*: Colon-separated namespaces with TTLs. **Live today:** Spring Session (`gateway:session` / cookie `APP_SESSION`), `idempotency:transfer:{key}`, `velocity:{accountId}`, Spring Cache for users/roles/accounts. **Planned:** `semcache:`, `chat:presence:`, gateway `ratelimit:`.
  - *Rejected*: Unstructured key naming without TTL expiration.

- **IMPL-006**: **Qdrant Vector Collections with Cosine Similarity & Payload Filtering**
  - *Why*: 1536-dimensional vector collections (`banking_knowledge_base`, `user_chat_longterm_memory`) with HNSW indexing and payload filters (e.g., `userId`, `category`) for lightning-fast filtered RAG lookups.
  - *Rejected*: Full-text keyword search for complex semantic inquiries.

---

## Details

### ⚡ 1. Messaging & AI Implementation Design (@Elena)

#### 1.1 Kafka Topic Topology & Partitioning

| Topic Name | Key | Producer | Consumers (code) | Status |
| :--- | :--- | :--- | :--- | :--- |
| `banking.transactions.v1` | `sourceAccountId` (outbox aggregateId) | Business-Ops outbox | KPI (`TransactionKpiConsumer`), Chat (`TransactionNotificationConsumer`) | **Implemented** |
| `fraud.alerts.v1` | accountId | KPI on velocity breach | — | **Produced only** |
| `chat.events.v1` | channelId | Chat STOMP telemetry | — | **Produced only** |
| `banking.accounts.v1` | accountId | Business-Ops | KPI | **Planned** |
| `*.DLT` / `@RetryableTopic` | — | Error handler | Ops | **Planned** |
| (future) AI consumers | — | — | Intelligence-Ai | **Design only** |

#### 1.2 Event Payload Schemas (JSON)

##### `TransactionCompletedEvent` (`banking.transactions.v1`)
```json
{
  "eventId": "evt_987654321",
  "eventType": "TRANSACTION_COMPLETED",
  "occurredAt": "2026-10-10T12:00:00Z",
  "payload": {
    "transactionId": "tx_1001",
    "sourceAccountId": "acc_001",
    "targetAccountId": "acc_002",
    "amount": 150.00,
    "currency": "USD",
    "transactionType": "TRANSFER",
    "status": "SUCCESS",
    "userId": "usr_5501",
    "description": "Dinner split"
  }
}
```

##### `ChatEvent` (`chat.events.v1`)
```json
{
  "eventId": "evt_123456789",
  "eventType": "CHAT_MESSAGE_SENT",
  "occurredAt": "2026-10-10T12:00:05Z",
  "payload": {
    "messageId": "msg_8801",
    "channelId": "chan_direct_usr5501_ai",
    "senderId": "usr_5501",
    "recipientId": "ai_assistant",
    "content": "Can you check my balance and transfer $50 to Alice?",
    "messageType": "USER_PROMPT"
  }
}
```

#### 1.3 Spring AI 2.0 MCP Tools & Agent Flows

In `Intelligence-Ai-Service`, we configure Spring AI `ChatClient` with declarative tools backed by gRPC stubs to `Business-Operation-Service`:

```java
@Component
public class BankingMcpTools {

    private final BankingGrpcClient bankingGrpcClient;

    public BankingMcpTools(BankingGrpcClient bankingGrpcClient) {
        this.bankingGrpcClient = bankingGrpcClient;
    }

    @Tool(description = "Check account balance and status for a given account ID")
    public AccountBalanceResponse checkBalance(
            @ToolParam(description = "The account ID to query") String accountId,
            @ToolParam(description = "The authenticated user ID") String userId) {
        return bankingGrpcClient.getBalance(accountId, userId);
    }

    @Tool(description = "Execute a monetary transfer between two accounts")
    public TransferResponse transferFunds(
            @ToolParam(description = "Source account ID") String sourceAccountId,
            @ToolParam(description = "Destination account ID") String targetAccountId,
            @ToolParam(description = "Amount to transfer") BigDecimal amount,
            @ToolParam(description = "Currency code e.g. USD") String currency,
            @ToolParam(description = "Authenticated user ID") String userId) {
        return bankingGrpcClient.transfer(sourceAccountId, targetAccountId, amount, currency, userId);
    }

    @Tool(description = "Lock or unlock a debit card for security")
    public CardStatusResponse updateCardStatus(
            @ToolParam(description = "Card ID to lock/unlock") String cardId,
            @ToolParam(description = "New status: LOCKED or ACTIVE") String status,
            @ToolParam(description = "Authenticated user ID") String userId) {
        return bankingGrpcClient.setCardStatus(cardId, status, userId);
    }
}
```

#### 1.4 STOMP Bidirectional Contract (target — PBP-003)

**Today:** Client→server `@MessageMapping` is solid; server→client push exists for Kafka notifications but is keyed by `accountId` (mismatch with JWT `userId`).

**Target server→client destinations** (client must subscribe on connect):
| Destination | Publisher | Purpose |
| :--- | :--- | :--- |
| `/user/queue/notifications` | Chat (from Kafka txn events) | Debit/credit alerts — key = **userId** |
| `/user/queue/ai-stream` | Intelligence-Ai | Token streaming |
| `/user/queue/confirmations` | Intelligence-Ai | HITL confirm payloads |
| `/topic/presence` | Chat | Online/offline (Redis refcount later) |

AI stream pattern (when AI service exists):
```java
chatClient.prompt(prompt)
    .stream()
    .content()
    .subscribe(token -> simpMessagingTemplate.convertAndSendToUser(
        userId, "/queue/ai-stream", new StreamTokenDto(token, messageId)
    ));
```

---

### 🗄️ 2. Data & Caching Implementation Design (@Leo)

#### 2.1 MongoDB Collections & Indexing Matrix

| Database | Collection | Status | Notes |
| :--- | :--- | :--- | :--- |
| `user_rbac_db` | `users`, `roles` | Implemented | Unique email; Redis Spring Cache |
| `banking_ops_db` | `accounts`, `transactions`, `cards`, `outbox_events` | Implemented | Outbox name is **`outbox_events`** (not `outbox`); TTL purge on published |
| `chat_db` | `messages` | Implemented | Channel + timestamp index |
| `kpi_analytics_db` | `daily_metrics` | Implemented | Upsert by currency + window |
| `ai_memory_db` | `chat_turns` | **Planned** | Needs Intelligence-Ai service |

#### 2.2 Redis Key Schema, TTLs & Eviction

| Key Pattern | Data Structure | TTL | Purpose | Status |
| :--- | :--- | :--- | :--- | :--- |
| Spring Session (`gateway:session` / cookie `APP_SESSION`) | Session store | ~8h | Gateway OAuth2 BFF session + tokens | **Implemented** |
| `idempotency:transfer:{key}` | String / lock | short | Transfer Idempotency-Key | **Implemented** |
| `velocity:{accountId}` | Counter + TTL | ~5m (config) | KPI fraud velocity window | **Implemented** |
| Spring Cache `users` / `roles` / `accounts` | Cache entries | app TTL | Entity read-through | **Implemented** |
| `semcache:{sha256(prompt)}` | String | 12h | AI semantic cache | **Planned** |
| `chat:presence:{userId}` | String | 5m | Presence | **Planned** (chat has no Redis today) |
| `ratelimit:{ip}:{minute}` | Counter | 2m | Gateway rate limit | **Planned** (explicitly out of scope for now) |

#### 2.3 Qdrant Vector Database Collections

##### 1. `banking_knowledge_base`
- **Purpose**: Financial FAQs, banking terms, transaction limit policies, security guidelines for RAG.
- **Vector Dimension**: 1536 (OpenAI `text-embedding-3-small` / standard embeddings).
- **Distance Metric**: `Cosine`.
- **Payload Schema**:
  ```json
  {
    "docId": "doc_faq_transfer_limits",
    "category": "SECURITY_POLICY",
    "title": "Daily Wire and ACH Transfer Limits",
    "contentChunk": "Standard accounts have a daily transfer limit of $5,000. Premium accounts can transfer up to $25,000 per 24 hours.",
    "updatedAt": "2026-10-01T00:00:00Z"
  }
  ```

##### 2. `user_chat_longterm_memory`
- **Purpose**: Episodic long-term memory capturing past user financial goals, conversational milestones, and preferences.
- **Vector Dimension**: 1536.
- **Distance Metric**: `Cosine`.
- **Payload Schema**:
  ```json
  {
    "memoryId": "mem_9901",
    "userId": "usr_5501",
    "conversationDate": "2026-08-15",
    "summaryText": "User established a goal to save $5,000 for a car downpayment by December 2026.",
    "tags": ["savings_goal", "budget"]
  }
  ```

---

## Open questions
- *Q1*: Consume `chat.events.v1` in KPI now, or defer until engagement dashboards are needed?
- *Q2*: For Qdrant local testing, Testcontainers `qdrant/qdrant:latest`? *(Recommendation: Yes when AI service starts)*.

## Change requests (to upstream files)
- None (architecture §2b aligned 2026-10-22).

## Next steps
- Add KPI/chat fraud consumers or document as fire-and-forget.
- Scaffold `Intelligence-Ai-Service` when ready for MCP + Qdrant.
