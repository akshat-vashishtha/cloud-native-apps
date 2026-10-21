# System Architecture: Cloud-Native Microservices Ecosystem
Status: agreed   Owner: @Alex   Updated: 2026-10-10
Inputs: context/00-idea.md

## Summary
A resilient, event-driven, and AI-native microservices ecosystem deployed on Azure Kubernetes Service (AKS). The architecture separates public edge ingress (`Gateway-Service`) from private internal services (`User-Rbac`, `Chat-Communication`, `Business-Operation`, `KPI-Analytics`, `Intelligence-Ai`). Inter-service communication combines high-throughput synchronous gRPC with asynchronous Kafka event streaming, supported by an advanced polyglot data tier comprising isolated MongoDB databases, a Redis distributed/semantic cache, and a Qdrant vector database.

---

## Decisions

- **ARCH-001**: **Dual-Protocol Gateway with Edge Token Verification (`Gateway-Service`)**
  - *Why*: The API Gateway performs cryptographic verification of incoming JWT/Sessions at the edge. Downstream microservices do not re-verify public tokens or query `User-Rbac-Service` on every request, eliminating bottlenecks and maintaining high throughput.
  - *Rejected*: Each microservice verifying public tokens with `User-Rbac-Service` (creates an $N$-hop network bottleneck).

- **ARCH-002**: **Hybrid BFF & Opaque Session Pattern (`APP_SESSION` Cookie + Redis Cache + JWT Fallback)**
  - *Why*: Eliminates repetitive asymmetric cryptographic JWT decryption on every request while preventing XSS. On login/first call, Gateway validates the JWT once, stores a lightweight session in Redis (`session:<id>` $\rightarrow$ `{userId, roles, jwtExpiry}`), and issues an opaque `APP_SESSION` HttpOnly cookie. Subsequent web requests perform a sub-millisecond $O(1)$ Redis lookup. Programmatic/mobile clients can still supply Bearer JWTs as a fallback. Enables instant session revocation (`DEL session:<id>`).
  - *Rejected*: Plain stateless JWT without revocation capabilities OR storing raw JWTs in browser localStorage.

- **ARCH-003**: **Concrete Domain: Digital Banking & Wallet Operations (`Business-Operation-Service`)**
  - *Why*: Provides clean transactional models (`Account`, `Transfer`, `Card`, `Payment`), rich real-time KPI event streaming (fraud detection, velocity, cash-flow metrics), and realistic AI Agent scenarios with Spring AI MCP tools ("Check balance", "Transfer funds", "Lock card").
  - *Rejected*: Abstract/generic domain without concrete transaction semantics.

- **ARCH-004**: **Dual-Zone Security (Edge Gateway + Signed `X-Internal-HMAC`)**
  - *Why*: Gateway strips incoming internal headers and injects verified claims (`X-User-Id`, `X-User-Roles`, `X-Timestamp`) signed with a shared HMAC-SHA256 signature (`X-Internal-HMAC`). Internal services verify this signature via a lightweight filter with timestamp replay protection. Production deployment hardens this with Azure Key Vault secret injection and pod-level NetworkPolicy ingress isolation (see **DEP-006**).
  - *Rejected*: Unauthenticated internal network or heavy public token re-verification on every hop.

- **ARCH-005**: **Hybrid Inter-Service Communication (gRPC Sync + Kafka Async)**
  - *Why*: gRPC for low-latency synchronous RPC queries (e.g., AI agent executing a banking tool, RBAC permission lookups) and Kafka for decoupled domain event streams (e.g., `TransactionCompletedEvent`, `AccountLockedEvent`).
  - *Rejected*: Pure REST or pure Kafka for all interactions.

- **ARCH-006**: **Database-Per-Service Isolation (MongoDB)**
  - *Why*: Each microservice owns its private MongoDB database (`user_rbac_db`, `chat_db`, `banking_ops_db`, `kpi_analytics_db`) to guarantee loose coupling.
  - *Rejected*: Shared monolithic database.

- **ARCH-007**: **STOMP WebSocket Message Broker (In-Memory SimpleBroker $\rightarrow$ Apache Artemis Relay Roadmap)**
  - *Why*: For learning and clean local setup, `Chat-Communication-Service` uses Spring Boot's built-in in-memory message broker (`enableSimpleBroker("/topic", "/queue")`). The production scaling roadmap plans an external Apache ActiveMQ Artemis STOMP broker relay (`enableStompBrokerRelay`).
  - *Rejected*: Forcing complex external broker setup on Day 1 before mastering core STOMP concepts.

- **ARCH-008**: **Dual-Tier AI Chat Memory (Short-Term MongoDB + Long-Term Qdrant)**
  - *Why*: Solves context window limits and memory retention. Short-term active working memory (last $N$ turns) is stored in MongoDB for exact conversational continuity; older historical conversations are summarized, embedded, and stored in Qdrant for semantic episodic recall.
  - *Rejected*: Storing entire multi-month chat histories in the active LLM context window.

- **ARCH-009**: **Saga Choreography for Distributed Transactions (Kafka)**
  - *Why*: Asynchronous event choreography (`transfer.initiated` $\rightarrow$ `account.debited` $\rightarrow$ `account.credited` $\rightarrow$ `transfer.completed` / compensating `account.refunded`) provides non-blocking, highly resilient distributed transaction consistency across accounts.
  - *Rejected*: Monolithic distributed 2-Phase Commit (2PC) or blocking HTTP orchestrators.

- **ARCH-010**: **Lightweight Scheduled Outbox Polling with Optimistic Locking**
  - *Why*: A dedicated Spring Boot background runner queries pending outbox records (`status: PENDING`), locks batches via optimistic document versioning (`@Version`), publishes to Kafka, and transitions to `PUBLISHED` (with exponential backoff on transient broker failures).
  - *Rejected*: Heavy external CDC infrastructure for early-stage complexity.

- **ARCH-011**: **Interactive 2-Step Confirmation for AI Tool Execution (Human-in-the-Loop)**
  - *Why*: Prevents hallucinated or unintended financial mutations. High-risk Spring AI tools (`transferFunds`, `lockCard`) emit a signed `ConfirmationToken` back to the STOMP chat stream. The action is only executed when the user explicitly clicks/confirms the action.
  - *Rejected*: Auto-executing financial transfers directly from unverified LLM completions.

- **ARCH-012**: **End-to-End Distributed Tracing & Idempotency Key Pattern**
  - *Why*: `Idempotency-Key` header cached in Redis prevents double charges on client retries. OpenTelemetry W3C `traceparent` context propagates seamlessly across HTTP, gRPC, and Kafka headers to MDC logs.
  - *Rejected*: Uncorrelated logs and non-idempotent mutation APIs.

---

## Details

### 1. Services & Bounded Contexts

```
                                  PUBLIC ZONE (Edge)
                               +----------------------+
                               |   Gateway-Service    |
                               | (REST / STOMP + JWT) |
                               +----------+-----------+
                                          |
                      Internal HMAC & Claims Propagation
                                          v
+-----------------------------------------------------------------------------------+
| PRIVATE KUBERNETES MESH (AKS)                                                     |
|                                                                                   |
|  +--------------------+   +-----------------------+   +------------------------+  |
|  |  User-Rbac-Service |   | Chat-Communication-Svc|   | Business-Operation-Svc |  |
|  |  - Auth & Roles    |   | - STOMP Real-Time     |   | - Domain Workflows     |  |
|  |  - Token Issuance  |   | - Session Presence    |   | - Business Operations  |  |
|  +---------+----------+   +-----------+-----------+   +-----------+------------+  |
|            |                          |                           |               |
|  +---------+----------+   +-----------+-----------+               |               |
|  | Intelligence-Ai-Svc|   |  KPI-Analytics-Service|               |               |
|  | - Spring AI 2.0    |   | - Event Ingestion     |               |               |
|  | - RAG & Tool Call  |   | - Aggregated Metrics  |               |               |
|  +---------+----------+   +-----------+-----------+               |               |
|            |                          |                           |               |
|            +--------------------------+---------------------------+               |
|                                       |                                           |
|       +-------------------------------+-------------------------------+           |
|       |                               |                               |           |
|       v                               v                               v           |
|  [ gRPC Mesh ]                [ Kafka Topics ]                [ Polyglot Data ]   |
|  - Fast internal RPC          - Domain Event Streams          - MongoDB (Isolated)|
|  - Sync RBAC verification     - Transactional Outbox          - Redis Cache & Pub |
|                               - Async Event Pub/Sub           - Qdrant Vector DB  |
+-----------------------------------------------------------------------------------+
```

#### Service Specifications
1. **`Gateway-Service`**
   - **Role**: Public edge routing, TLS termination, CORS, BFF `APP_SESSION` cookie validation against Redis, HMAC internal signing (`X-Internal-HMAC`). *(No rate limiting)*.
   - **Protocols**: HTTP/2, WebSocket/STOMP.
2. **`User-Rbac-Service`**
   - **Role**: User identity master, password hashing (BCrypt/Argon2), role assignment, token issuance, and granular RBAC permission matrix.
   - **Role & Permission Matrix**:
     - `ROLE_USER`: `account:read`, `transfer:execute`, `card:read`, `card:update_status`, `ai:chat`.
     - `ROLE_PREMIUM`: All `ROLE_USER` permissions + `transfer:high_limit`, `ai:premium_features`.
     - `ROLE_SUPPORT`: `account:read`, `chat:support_agent`, `card:read`.
     - `ROLE_ADMIN`: `user:manage_roles`, `user:audit`, `kpi:read_analytics`, `system:config`.
   - **Persistence**: `user_rbac_db` (MongoDB).
3. **`Chat-Communication-Service`**
   - **Role**: Real-time bi-directional STOMP/WebSocket communication engine. Uses Spring Boot's built-in in-memory `SimpleBroker` for local development/learning, with an external Apache ActiveMQ Artemis STOMP broker relay on the production roadmap. Streams live AI response tokens to users.
   - **Protocols**: STOMP over WebSocket (`/ws`), Kafka event emission (`chat.events.v1`).
   - **Persistence**: `chat_db` (MongoDB) + In-Memory Queue (Phase 1) / Apache Artemis Relay (Phase 2).
4. **`Business-Operation-Service` (Digital Banking & Wallet)**
   - **Role**: Bank accounts, funds transfer, balance checks, payment processing, debit/credit cards, and transaction ledger.
   - **Persistence**: `banking_ops_db` (MongoDB).
5. **`KPI-Analytics-Service`**
   - **Role**: Asynchronous ingestion of banking transactions and chat events from Kafka, real-time fraud velocity detection, financial summaries, and aggregated metric dashboards.
   - **Persistence**: `kpi_analytics_db` (MongoDB) + Redis (Pre-calculated aggregate caches).
6. **`Intelligence-Ai-Service`**
   - **Role**: AI Financial Concierge ("Sophia / Wealth AI"). Configures Spring AI 2.0 Agents with Tool Calling (MCP) to `Business-Operation-Service` over gRPC and Dual-Tier Chat Memory.
   - **Agents Defined**:
     - `FinancialConciergeAgent`: General inquiries, spending summaries, FAQ RAG search.
     - `TransactionAgent`: Account balance lookups and 2-Step verified fund transfers.
     - `SecurityAgent`: Card status lock/unlock and security alerts.
   - **Persistence & Memory**: 
     - *Short-Term Working Memory*: `ai_memory_db` (MongoDB) for immediate conversation turns.
     - *Long-Term Episodic Memory*: Qdrant Vector DB for semantic recall of past historical chats, financial goals, and FAQ embeddings.
     - *Cache*: Redis (Semantic Cache for instant completion lookups).

---

### 2. Communication Matrix (Sync / Async)

| Source | Target | Protocol | Pattern | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Client** | `Gateway-Service` | HTTPS / WSS | Request-Response / Duplex | Public API & STOMP connections |
| **`Gateway-Service`** | Microservices | HTTP / gRPC / STOMP | Reverse Proxy with HMAC | Routing validated traffic to internal pods |
| **Microservices** | `User-Rbac-Service` | gRPC (Proto3) | Sync Request-Response | Low-latency permission verification |
| **`Chat-Communication-Svc`** | `KPI-Analytics-Svc` | Kafka (`chat.events.v1`) | Async Pub/Sub | Chat activity telemetry |
| **`Business-Operation-Svc`** | All Services | Kafka (`business.events.v1`) | Async Event Streaming (Outbox) | State changes, orders, operation milestones |
| **`Intelligence-Ai-Svc`** | `Business-Operation-Svc` | gRPC (Proto3) | Sync Request-Response | AI Tool calling (executing domain operations) |
| **`Intelligence-Ai-Svc`** | Qdrant | gRPC / HTTP | Vector Similarity Search | Vector embeddings & RAG context retrieval |
| **`Intelligence-Ai-Svc`** | Redis | RESP3 | Semantic Cache Lookups | Instant semantic hit check before LLM query |

---

### 3. Clean Layered Architecture & Cross-Cutting Concerns

Each microservice follows a strict **Clean Layered Architecture** with unidirectional top-down dependencies:

```
+-----------------------------------------------------------------------+
| 1. PRESENTATION / CONTROLLER LAYER (Inbound)                          |
|    - REST Controllers, gRPC Services, STOMP WebSocket Handlers        |
|    - Request / Response DTOs, Input Validation (@Valid)               |
+-----------------------------------+-----------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
| 2. APPLICATION / SERVICE LAYER (Business Logic)                       |
|    - Application Services, Business Workflows, Transaction Boundaries |
|    - Orchestrates operations between Domain, Persistence, & Messaging |
+-----------------------------------+-----------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
| 3. DOMAIN LAYER (Enterprise Rules & State)                            |
|    - Domain Entities, Immutable Value Objects (Records), Enums        |
|    - Pure business logic & validations (Zero external framework leaks)|
+-----------------------------------+-----------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
| 4. PERSISTENCE & INFRASTRUCTURE LAYER (Data Access & External)        |
|    - Spring Data Repositories, Database Entities, DB Mappers          |
|    - Kafka Event Producers & Consumers, Redis Clients, Qdrant Clients |
|    - External HTTP / gRPC Client integrations, Security filters       |
+-----------------------------------------------------------------------+
```

- **Zero Entity Leaks**:
  - `API DTOs` are used in the Presentation Layer.
  - `Domain Entities` hold business state in the Service/Domain Layer.
  - `Document / DB Entities` are strictly isolated in the Persistence Layer.
  - Mappers translate objects cleanly between layers without exposing internal database schemas or domain internals.

---

### 4. Non-Functional Requirements (NFRs)

- **Latency**: Internal gRPC calls $< 10\text{ms}$ at p99.
- **Resilience**: Circuit breakers (Resilience4j) on gRPC clients; Kafka dead letter topics (DLT) for failed message handling.
- **Scalability**: Horizontal Pod Autoscaling (HPA) on CPU/memory and KEDA for Kafka consumer lag.
- **Security**: Zero-trust internal network; PII scrubbing in logs; TLS in transit.

---

## Open questions
- *Q1*: Which exact LLM model / Azure OpenAI endpoint or OpenAI provider configuration will be used by `Intelligence-Ai-Service`?
- *Q2*: What specific business domain/scenario shall we model inside `Business-Operation-Service` (e.g., E-commerce orders, project management workflows, or ticket fulfillment)?

## Change requests (to upstream files)
- None.

## Next steps
- **Handoff to Technical Design Phase**:
  - `@Elena` to specify Kafka topics, message schemas, and Spring AI 2.0 / MCP integration flows in `context/implementation.md`.
  - `@Leo` to specify MongoDB collections/indexes, Redis caching strategies, and Qdrant vector schemas in `context/implementation.md`.
