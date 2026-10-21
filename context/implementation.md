# Technical Implementation Design
Status: agreed   Owner: @Elena & @Leo (Reviewer: @Alex)   Updated: 2026-10-10
Inputs: context/architecture.md

## Summary
Comprehensive technical design for the Digital Banking microservices ecosystem. Defines concrete Apache Kafka topic partitioning and transactional event schemas, Spring AI 2.0 Model Context Protocol (MCP) tool contracts, MongoDB document schemas with optimized indexes, Redis key namespaces with TTL policies, and Qdrant vector database collection configurations for dual-tier AI memory and semantic search.

---

## Decisions

- **IMPL-001**: **Transactional Outbox & Domain Event Streams (Kafka)**
  - *Why*: Guarantees zero message loss during database writes in `Business-Operation-Service`. Events (`banking.transactions.v1`, `banking.accounts.v1`, `chat.events.v1`) are written to an outbox collection in the same transaction as state changes and published reliably to Kafka.
  - *Rejected*: Direct publishing inside HTTP request threads (susceptible to dual-write failures).

- **IMPL-002**: **Idempotent Consumer & Exponential Backoff DLQ Topics**
  - *Why*: Kafka consumers in `KPI-Analytics-Service` and `Chat-Communication-Service` maintain deduplication checks using MongoDB unique constraint/Redis keys and leverage Spring Kafka `@RetryableTopic` with Dead Letter Topics (`*.DLT`).
  - *Rejected*: Blind message processing without idempotency checks.

- **IMPL-003**: **Spring AI 2.0 MCP (Model Context Protocol) Banking Tools**
  - *Why*: Expose declarative Spring AI `@Tool` functions calling `Business-Operation-Service` via gRPC stubs (`checkBalance`, `executeTransfer`, `getRecentTransactions`, `lockCard`) with strict input schema validation.
  - *Rejected*: Hardcoding business logic directly inside the AI service.

- **IMPL-004**: **MongoDB Isolated Databases & Compound Indexing Strategy**
  - *Why*: Independent MongoDB databases (`user_rbac_db`, `banking_ops_db`, `chat_db`, `kpi_analytics_db`, `ai_memory_db`) with compound indexes on high-frequency query paths (e.g., `{accountId: 1, createdAt: -1}`).
  - *Rejected*: Single collection/shared database.

- **IMPL-005**: **Redis Multi-Tier Namespace & TTL Strategy**
  - *Why*: Clear colon-separated namespaces (`session:{id}`, `semcache:{hash}`, `chat:presence:{userId}`, `ratelimit:{ip}`) with strict TTLs to ensure memory safety and sub-millisecond retrieval.
  - *Rejected*: Unstructured key naming without TTL expiration.

- **IMPL-006**: **Qdrant Vector Collections with Cosine Similarity & Payload Filtering**
  - *Why*: 1536-dimensional vector collections (`banking_knowledge_base`, `user_chat_longterm_memory`) with HNSW indexing and payload filters (e.g., `userId`, `category`) for lightning-fast filtered RAG lookups.
  - *Rejected*: Full-text keyword search for complex semantic inquiries.

---

## Details

### ⚡ 1. Messaging & AI Implementation Design (@Elena)

#### 1.1 Kafka Topic Topology & Partitioning

| Topic Name | Partitions | Key | Retention | Producer | Consumers |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `banking.transactions.v1` | 6 | `accountId` | 30 days | `Business-Operation-Svc` | `KPI-Analytics-Svc`, `Intelligence-Ai-Svc` |
| `banking.accounts.v1` | 3 | `accountId` | 30 days | `Business-Operation-Svc` | `KPI-Analytics-Svc` |
| `chat.events.v1` | 6 | `channelId` | 7 days | `Chat-Communication-Svc` | `KPI-Analytics-Svc`, `Intelligence-Ai-Svc` |
| `banking.transactions.v1.DLT` | 2 | `accountId` | 90 days | Kafka Error Handler | Ops Alerting / Dead Letter Queue |

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

#### 1.4 Live Token Streaming over STOMP WebSockets
- Client subscribes to destination: `/user/queue/ai-stream`.
- As Spring AI generates tokens from GPT-4 via `chatClient.prompt().stream().content()`, tokens are published in real-time to the STOMP destination:
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

| Database | Collection | Key Fields | Indexes |
| :--- | :--- | :--- | :--- |
| `user_rbac_db` | `users` | `id`, `email`, `passwordHash`, `roles`, `status` | `email` (Unique), `roles` |
| `banking_ops_db` | `accounts` | `id`, `userId`, `accountNumber`, `balance`, `currency`, `status` | `userId` (Asc), `accountNumber` (Unique) |
| `banking_ops_db` | `transactions` | `id`, `sourceAccountId`, `targetAccountId`, `amount`, `type`, `createdAt` | `{sourceAccountId: 1, createdAt: -1}`, `{targetAccountId: 1, createdAt: -1}` |
| `banking_ops_db` | `outbox` | `id`, `aggregateType`, `payload`, `status`, `createdAt` | `{status: 1, createdAt: 1}` (Outbox Polling) |
| `chat_db` | `messages` | `id`, `channelId`, `senderId`, `content`, `timestamp` | `{channelId: 1, timestamp: -1}` |
| `kpi_analytics_db` | `daily_metrics` | `id`, `date`, `totalVolume`, `transactionCount`, `categoryBreakdown` | `date` (Unique) |
| `ai_memory_db` | `chat_turns` | `id`, `userId`, `sessionId`, `role`, `content`, `timestamp` | `{userId: 1, sessionId: 1, timestamp: -1}` |

#### 2.2 Redis Key Schema, TTLs & Eviction

| Key Pattern | Data Structure | TTL | Purpose |
| :--- | :--- | :--- | :--- |
| `session:{sessionId}` | `Hash` (`userId`, `roles`, `expiresAt`) | 24 Hours | BFF Opaque Session Cache |
| `semcache:{sha256(prompt)}` | `String` (JSON Response) | 12 Hours | Semantic AI Completion Cache |
| `chat:presence:{userId}` | `String` (`ONLINE` / `AWAY`) | 5 Minutes (Heartbeat) | Real-time presence state |
| `chat:channel:{channelId}` | `Pub/Sub` | Transient | Multi-pod STOMP message relay |
| `ratelimit:{ip}:{minute}` | `String` (Counter) | 2 Minutes | Gateway rate-limiting |

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
- *Q1*: Do we want to configure Spring Kafka with Spring Cloud Stream, or standard pure `spring-kafka` templates and `@KafkaListener` annotations? *(Recommendation: Pure `spring-kafka` for maximum transparency & control)*.
- *Q2*: For Qdrant local testing in development, shall we use Testcontainers `qdrant/qdrant:latest`? *(Recommendation: Yes)*.

## Change requests (to upstream files)
- None.

## Next steps
- **Phase 3: Per-Service Specifications**:
  - `@Alex`, `@Elena`, and `@Leo` will generate the service blueprints in `context/services/<service-name>.md` starting with `Gateway-Service` and `Business-Operation-Service`.
