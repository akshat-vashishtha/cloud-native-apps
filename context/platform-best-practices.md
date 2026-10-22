# Platform Best Practices (Kafka, STOMP, MongoDB, Redis, gRPC, Spring AI)
Status: implemented   Owner: @Elena, @Leo, @Alex   Updated: 2026-10-22
Inputs: context/architecture.md, context/implementation.md

## Summary
All P0–P2 industry best-practice targets across Kafka, STOMP, MongoDB Criteria, Redis, gRPC Clients, and Agentic Spring AI are **implemented and compiling** across the 7 microservices.

---

## Decisions & Implementation Matrix

| ID | Standard | Implementation in Codebase | Status |
| :--- | :--- | :--- | :--- |
| **PBP-001** | **Transactional Outbox with Atomic Claim** | `BankingCriteriaQueryAdapter.claimNextPendingOutboxEvent()` & `ChatCriteriaQueryAdapter.claimNextPendingOutboxEvent()` atomically transition `PENDING` -> `IN_FLIGHT` via `MongoTemplate.findAndModify`. | **Implemented** |
| **PBP-002** | **Idempotent Kafka Consumers + Retry/DLT** | `KafkaErrorHandlerConfig` (`DefaultErrorHandler` + `ExponentialBackOffWithMaxRetries` + `DeadLetterPublishingRecoverer` -> `*.DLT`) in KPI, Chat, and AI services. Redis `SETNX` deduplication (`event:dedupe:*`). | **Implemented** |
| **PBP-003** | **Bidirectional STOMP Keyed by JWT `userId` + ACL** | `TransactionCompletedEvent` carries `sourceUserId`/`targetUserId`; `TransactionNotificationConsumer` pushes to `/user/{userId}/queue/notifications`, `/user/{userId}/queue/ai-stream`, `/user/{userId}/queue/confirmations`. `StompDestinationAclInterceptor` enforces subscription ACLs. | **Implemented** |
| **PBP-004** | **MongoDB `Criteria` + `MongoTemplate` Query API** | `BankingCriteriaQueryAdapter`, `KpiCriteriaRepository`, `ChatCriteriaQueryAdapter`, and `MongoChatMemoryCriteriaAdapter` use `Criteria`, `Query`, `Update`, and `findAndModify`. | **Implemented** |
| **PBP-005** | **First-Class gRPC Client Stubs with Deadlines** | `GrpcBankingClientAdapter` (`@GrpcClient("banking-service")`) and `GrpcUserRbacClientAdapter` (`@GrpcClient("user-rbac-service")`) in `intelligence-ai-service` with per-call deadlines. | **Implemented** |
| **PBP-006** | **Agentic Spring AI Workflow + HITL + Semantic Cache** | `AgenticConciergeServiceImpl` classifies intent (`AgentClassification`), checks `RedisSemanticCacheAdapter`, loads `MongoChatMemoryCriteriaAdapter`, invokes specialist `ChatClient` with `BankingMcpTools` (`@Tool`), stages HITL tokens in `RedisConfirmationTokenAdapter`, and streams tokens to STOMP. | **Implemented** |
| **PBP-007** | **Atomic Redis Velocity Lua Script & Presence Refcount** | `TransactionKpiConsumer` executes atomic `INCR` + `EXPIRE` Lua script; `WebSocketEventListener` tracks per-user session sets (`chat:presence:sessions:{userId}`). | **Implemented** |
