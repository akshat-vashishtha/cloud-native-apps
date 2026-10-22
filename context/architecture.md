# System Architecture: Cloud-Native Microservices Ecosystem
Status: agreed   Owner: @Alex   Updated: 2026-10-22
Inputs: context/00-idea.md

## Summary
A resilient, event-driven, and AI-native microservices ecosystem deployed on Azure Kubernetes Service (AKS). Public edge is `web` (SPA) + `Gateway-Service` (OAuth2 BFF). Identity split: `oauth2-authorization-server` (tokens) and `User-Rbac` (users/RBAC). Downstream services are JWT resource servers. Sync gRPC + async Kafka; polyglot data (MongoDB, Redis, Qdrant).

> **Visual HLD Diagram:** Publication-grade C4 Level 2 Container Diagram available at [`docs/architecture-hld.drawio`](../docs/architecture-hld.drawio).

---

## Decisions

- **ARCH-001**: **OAuth2 Authorization Server as Sole Token Authority**
  - *Why*: Separates token issuance (keys, JWKS, refresh) from user/RBAC data. `user-rbac-service` owns identity only; `oauth2-authorization-server` issues JWTs.
  - *Rejected*: Minting JWTs inside `user-rbac-service`.

- **ARCH-002**: **BFF Gateway (`APP_SESSION` + TokenRelay)**
  - *Why*: Browser holds only an opaque session cookie. Gateway is a confidential OAuth2 client (Authorization Code); stores tokens in Redis session; forwards Bearer access tokens via Spring Cloud Gateway `TokenRelay`. SPA never sees tokens (XSS-safe).
  - *Rejected*: SPA localStorage JWTs; Resource Owner Password Credentials (removed in OAuth 2.1).

- **ARCH-003**: **Concrete Domain: Digital Banking & Wallet Operations (`Business-Operation-Service`)**
  - *Why*: Provides clean transactional models (`Account`, `Transfer`, `Card`, `Payment`), rich real-time KPI event streaming (fraud detection, velocity, cash-flow metrics), and realistic AI Agent scenarios with Spring AI MCP tools ("Check balance", "Transfer funds", "Lock card").
  - *Rejected*: Abstract/generic domain without concrete transaction semantics.

- **ARCH-004**: **Downstream OAuth2 Resource Servers (JWKS / `kid`)**
  - *Why*: Each microservice validates the access token signature using AS JWKS (cached public keys selected by JWT `kid`). Industry-standard Spring Security resource-server pattern. Custom `X-Internal-HMAC` identity trust is retired.
  - *Rejected*: Shared HMAC between gateway and services as the primary auth mechanism.

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

- **ARCH-013**: **Platform Best-Practice Targets (Kafka / STOMP / Mongo Criteria / gRPC Clients / Agentic Spring AI)**
  - *Why*: Codebase audit showed gaps vs industry norms (Kafka DLT/idempotent consumers, STOMP server→client keyed by `userId`, Mongo Criteria for atomic claims, missing gRPC clients, no Spring AI module). Canonical targets live in [`context/platform-best-practices.md`](platform-best-practices.md) (**PBP-001…006**).
  - *Rejected*: Treating current partial implementations as final architecture.

---

## Details

### 1. Services & Bounded Contexts

```
                         PUBLIC ZONE (Edge)
              +------------+      +----------------------+
              |  web SPA   | ---> |   Gateway-Service    |
              | (Next.js)  |      | OAuth2 BFF + TokenRelay|
              +------------+      +----------+-----------+
                                             |
                    Bearer JWT (TokenRelay) + Redis APP_SESSION
                                             v
+-----------------------------------------------------------------------------------+
| PRIVATE MESH                                                                      |
|  +------------------+  +------------------+  +---------------------+              |
|  | oauth2-AS (:9000)|  | User-Rbac        |  | Chat (STOMP /ws)    |              |
|  | JWKS + form login|  | users/RBAC only  |  | JWT handshake       |              |
|  +--------+---------+  +--------+---------+  +----------+----------+              |
|           |                     ^                       |                         |
|           | internal by-email   |                       | Kafka chat.events.v1    |
|           v                     |                       v                         |
|  +------------------+  +--------+---------+  +----------+----------+              |
|  | Business-Ops     |  | KPI-Analytics    |  | Intelligence-Ai     |              |
|  | outbox→Kafka     |  | txn velocity     |  | (design only)       |              |
|  +--------+---------+  +------------------+  +---------------------+              |
|           |                                                                       |
|           +-- banking.transactions.v1 --> KPI + Chat notifications                |
|  [ gRPC ] [ Kafka ] [ MongoDB per service ] [ Redis: session/cache/velocity ]      |
+-----------------------------------------------------------------------------------+
```

#### Service Specifications
0. **`web` (SPA)**
   - **Role**: Next.js client; Sign in redirects to gateway OAuth; register; cookie-based APIs; simple HTML/CSS.
1. **`Gateway-Service`**
   - **Role**: Public edge routing, CORS, OAuth2 client, Redis `APP_SESSION`, TokenRelay of access tokens. *(No rate limiting)*.
   - **Protocols**: HTTP/2, WebSocket/STOMP.
1b. **`oauth2-authorization-server`**
   - **Role**: Authorization Code + refresh; JWKS; form login; JWT claims (`roles`, `permissions`).
2. **`User-Rbac-Service`**
   - **Role**: User identity master, password hashing (BCrypt), role assignment, granular RBAC. **Does not mint tokens.** Provides internal authenticate/claims API for the AS.
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
   - **Role**: Consumes `banking.transactions.v1`; Redis sliding-window velocity; daily Mongo rollups; publishes `fraud.alerts.v1` (no consumer yet). Chat-topic consumption is **planned**, not coded.
   - **Persistence**: `kpi_analytics_db` (MongoDB) + Redis (`velocity:{accountId}`).
6. **`Intelligence-Ai-Service`** *(design only — no `micro-service` implementation yet)*
   - **Role**: AI Financial Concierge ("Sophia / Wealth AI"). Spring AI 2.0 Agents with Tool Calling (MCP) to `Business-Operation-Service` over gRPC and Dual-Tier Chat Memory.
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

| Source | Target | Protocol | Pattern | Purpose | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`web` SPA** | `Gateway-Service` | HTTPS | Cookie BFF | REST; OAuth start redirect | Implemented |
| **Browser** | `Gateway` → Chat | WSS `/ws` | STOMP proxy | Real-time chat | Implemented |
| **`Gateway-Service`** | Downstream APIs | HTTP | TokenRelay Bearer JWT | Edge routing | Implemented |
| **`oauth2-AS`** | `User-Rbac` | HTTP | Internal API key | Load user + password hash | Implemented |
| **Microservices** | `User-Rbac` | gRPC | Sync RPC | Profile / permission checks | Implemented |
| **`Business-Operation`** | Kafka | `banking.transactions.v1` | Outbox poll | Transfer completed events | Implemented |
| **Kafka** | `KPI-Analytics` | `banking.transactions.v1` | `@KafkaListener` | Velocity + daily metrics | Implemented |
| **Kafka** | `Chat` | `banking.transactions.v1` | `@KafkaListener` | STOMP user notifications | Implemented |
| **`Chat`** | Kafka | `chat.events.v1` | Produce | Chat telemetry | Produced; **no consumer yet** |
| **`KPI`** | Kafka | `fraud.alerts.v1` | Produce | High-velocity alerts | Produced; **no consumer yet** |
| **`Business-Operation`** | Kafka | `banking.accounts.v1` | — | Account lifecycle | **Planned** |
| **`Intelligence-Ai`** | Business / Qdrant / Redis | gRPC / HTTP | Tools + RAG | AI concierge | **Design only** |

### 2b. Implementation status snapshot (2026-10-22)

| Area | Live in code | Still design / gap |
| :--- | :--- | :--- |
| OAuth2 AS + Gateway BFF + JWT RS | Yes | — |
| Banking REST/gRPC + outbox → txn topic | Yes | Multi-account saga (ARCH-009); `banking.accounts.v1` |
| KPI velocity + daily APIs | Yes | Consume `chat.events.v1`; DLT; fraud consumers |
| Chat STOMP + Mongo messages | Yes | Artemis relay; AI token stream; Redis presence |
| Redis | Gateway Spring Session; banking idempotency; KPI velocity; user/role/account caches | Semantic cache; chat presence; gateway ratelimit |
| Intelligence-Ai / Qdrant | — | Entire service |

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
- *Q2*: Should STOMP transaction notifications key by `userId` (JWT `sub`) instead of `accountId` for `/user/.../queue/notifications`?

## Change requests (to upstream files)
- `@Elena` / `@Leo`: keep `implementation.md` topic & Redis tables aligned with §2b (done 2026-10-22).

## Next steps
- Execute **P0** items in [`platform-best-practices.md`](platform-best-practices.md) §7 (Kafka DLT/idempotency, STOMP userId push, outbox Criteria claim).
- Then gRPC clients + `Intelligence-Ai-Service` agentic stack (P1/P2).
