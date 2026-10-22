# Cloud-Native Microservices & Distributed Systems: Interview Preparation Hub

A comprehensive, curated repository of interview questions and deep-dive technical discussions tailored for Senior / Staff Backend Engineers, Java Architects, and Cloud-Native Practitioners. 

This curriculum is organized into **6 logical domain tracks** reflecting the end-to-end journey from core language fundamentals up to cloud-native production deployment and agentic AI.

---

## 🗺️ Master Curriculum & Logical Flow

```
┌──────────────────────────────────────────────┐
│ Track 1: Language & Frameworks               │
│ Core Java 21+ · Spring Boot 3.x · Security   │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│ Track 2: Software Architecture & Design      │
│ Low-Level Design (LLD) · Microservices · HLD │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│ Track 3: Protocols & Event Streaming         │
│ Sync gRPC · Apache Kafka · STOMP/WebSockets  │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│ Track 4: Data Storage & Caching              │
│ Relational SQL · MongoDB NoSQL · Redis Cache │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│ Track 5: AI & Agentic Engineering            │
│ Spring AI 1.0 GA · Tool Calling · RAG · HITL │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│ Track 6: Cloud, Containers & DevOps          │
│ Kubernetes (AKS) · Azure Cloud & Zero-Trust  │
└──────────────────────────────────────────────┘
```

---

## 📚 Curriculum Index by Track

### [Track 1: Language & Frameworks](./01-language-and-frameworks)
Foundational engineering mastery covering modern Java 21, the Spring Boot 3 enterprise runtime, and OAuth2 security architecture.

| Topic | Document | Key Focus Areas |
| :--- | :--- | :--- |
| **Java 21+ & Core JVM** | [`java-interview-questions.md`](./01-language-and-frameworks/java-interview-questions.md) | Virtual Threads (Project Loom), JVM memory model, ZGC, concurrency primitives (`CompletableFuture`, `ReentrantLock`), records, sealed classes, pattern matching. |
| **Spring Boot 3.x** | [`spring-boot-interview-questions.md`](./01-language-and-frameworks/spring-boot-interview-questions.md) | Auto-configuration internals, Bean lifecycle, conditional beans, `@ConfigurationProperties`, Spring WebFlux vs MVC, Micrometer metrics, graceful shutdown. |
| **Spring Security & OAuth2** | [`spring-security-oauth2-interview-questions.md`](./01-language-and-frameworks/spring-security-oauth2-interview-questions.md) | SecurityFilterChain, OAuth2 Authorization Server vs Resource Server, JWKS token verification, opaque cookie BFF (`APP_SESSION`), RBAC permission filters. |

---

### [Track 2: Software Architecture & System Design](./02-software-architecture-and-design)
Architectural principles from object-oriented component boundaries to high-scale distributed systems design.

| Topic | Document | Key Focus Areas |
| :--- | :--- | :--- |
| **Low-Level Design (LLD)** | [`low-level-design-interview-questions.md`](./02-software-architecture-and-design/low-level-design-interview-questions.md) | SOLID principles, Clean/Hexagonal Architecture, Domain-Driven Design (DDD) Aggregates & Value Objects, Strategy, Factory, Observer, and Decorator patterns. |
| **Microservices Architecture** | [`microservices-interview-questions.md`](./02-software-architecture-and-design/microservices-interview-questions.md) | The 15-Factor microservice standards, Database-per-Service, Saga Choreography vs Orchestration, Transactional Outbox pattern, Resilience4j circuit breakers. |
| **Architecture, HLD & System Design** | [`architecture-hld-system-design-interview-questions.md`](./02-software-architecture-and-design/architecture-hld-system-design-interview-questions.md) | C4 modeling (L1–L4), horizontal scaling, CAP theorem, PACELC, distributed locks, rate limiting algorithms, write-heavy vs read-heavy architectures. |

---

### [Track 3: Protocols & Event Streaming](./03-protocols-and-event-streaming)
Inter-service communication standards spanning low-latency synchronous RPC, asynchronous log-based event streaming, and bidirectional browser push.

| Topic | Document | Key Focus Areas |
| :--- | :--- | :--- |
| **gRPC & Protocol Buffers** | [`grpc-interview-questions.md`](./03-protocols-and-event-streaming/grpc-interview-questions.md) | HTTP/2 multiplexing, Proto3 schema contracts, unary vs streaming RPC, client stubs, per-call deadlines, gRPC interceptors, error propagation. |
| **Apache Kafka & Event Streaming** | [`kafka-interview-questions.md`](./03-protocols-and-event-streaming/kafka-interview-questions.md) | Partition key hashing & FIFO ordering, Consumer Groups & rebalancing, consumer lag, idempotent producers/consumers, Retryable topics, Dead Letter Queues (`*.DLT`). |
| **STOMP & WebSockets** | [`stomp-websocket-interview-questions.md`](./03-protocols-and-event-streaming/stomp-websocket-interview-questions.md) | WebSocket protocol upgrade, STOMP framing, Spring `SimpleBroker` vs external Artemis relay, User Destinations (`/user/{userId}/queue/*`), destination ACLs. |

---

### [Track 4: Data Storage & Caching](./04-data-storage-and-caching)
Polyglot persistence strategies covering relational transaction isolation, atomic document operations, and distributed low-latency in-memory state.

| Topic | Document | Key Focus Areas |
| :--- | :--- | :--- |
| **SQL Databases** | [`sql-database-interview-questions.md`](./04-data-storage-and-caching/sql-database-interview-questions.md) | ACID properties, transaction isolation levels (Read Committed vs Repeatable Read vs Serializable), B-Tree indexing, execution plan analysis, deadlock resolution. |
| **MongoDB & NoSQL** | [`mongodb-interview-questions.md`](./04-data-storage-and-caching/mongodb-interview-questions.md) | Document schema design, embedding vs referencing, atomic `$inc` / `findAndModify`, MongoDB `Criteria` & `MongoTemplate`, compound indexes, TTL indexes. |
| **Redis & In-Memory State** | [`redis-interview-questions.md`](./04-data-storage-and-caching/redis-interview-questions.md) | Redis data structures (Strings, Hashes, Sets, Sorted Sets), distributed locks (Redlock / SETNX), atomic Lua scripts, sliding-window velocity counters, TTL eviction. |

---

### [Track 5: AI & Agentic Engineering](./05-ai-and-agentic-engineering)
The modern enterprise AI stack leveraging Spring AI 1.0 GA, multi-agent coordination, and secure LLM orchestration.

| Topic | Document | Key Focus Areas |
| :--- | :--- | :--- |
| **Spring AI & Agentic Systems** | [`spring-ai-interview-questions.md`](./05-ai-and-agentic-engineering/spring-ai-interview-questions.md) | Spring AI `ChatClient` builder, Advisor chains (`SafeGuardAdvisor`, `MessageChatMemoryAdvisor`), Model Context Protocol (MCP) & `@Tool` execution over gRPC, Human-in-the-Loop (HITL) 2-step verification, RAG vector retrieval, and OWASP Top 10 for LLMs. |

---

### [Track 6: Cloud, Containers & DevOps](./06-cloud-and-devops)
Production deployment, cluster networking, container security, and cloud infrastructure operations on Microsoft Azure.

| Topic | Document | Key Focus Areas |
| :--- | :--- | :--- |
| **Azure Cloud & Zero-Trust** | [`azure-cloud-interview-questions.md`](./06-cloud-and-devops/azure-cloud-interview-questions.md) | Azure Kubernetes Service (AKS), Azure CNI Overlay networking, Azure Workload Identity (federated credentials), Azure Key Vault Secret Store CSI Driver, Azure Monitor. |
| **Kubernetes & Container Orchestration** | [`kubernetes-interview-questions.md`](./06-cloud-and-devops/kubernetes-interview-questions.md) | Pod lifecycle & probes (Liveness / Readiness), Horizontal Pod Autoscaling (HPA), KEDA event-driven autoscaling based on Kafka lag, Istio Service Mesh (strict mTLS), NetworkPolicies. |

---

## 🎯 Recommended Study Pathways

### 1. The Core Backend Specialist (Java + Spring + Microservices)
1. [`01-language-and-frameworks/java-interview-questions.md`](./01-language-and-frameworks/java-interview-questions.md)
2. [`01-language-and-frameworks/spring-boot-interview-questions.md`](./01-language-and-frameworks/spring-boot-interview-questions.md)
3. [`02-software-architecture-and-design/microservices-interview-questions.md`](./02-software-architecture-and-design/microservices-interview-questions.md)
4. [`02-software-architecture-and-design/low-level-design-interview-questions.md`](./02-software-architecture-and-design/low-level-design-interview-questions.md)

### 2. The Distributed Systems & Messaging Specialist (Kafka + Streaming + Data)
1. [`03-protocols-and-event-streaming/kafka-interview-questions.md`](./03-protocols-and-event-streaming/kafka-interview-questions.md)
2. [`03-protocols-and-event-streaming/grpc-interview-questions.md`](./03-protocols-and-event-streaming/grpc-interview-questions.md)
3. [`04-data-storage-and-caching/redis-interview-questions.md`](./04-data-storage-and-caching/redis-interview-questions.md)
4. [`04-data-storage-and-caching/mongodb-interview-questions.md`](./04-data-storage-and-caching/mongodb-interview-questions.md)

### 3. The Modern AI Architect (Spring AI + Agentic Workflows)
1. [`05-ai-and-agentic-engineering/spring-ai-interview-questions.md`](./05-ai-and-agentic-engineering/spring-ai-interview-questions.md)
2. [`03-protocols-and-event-streaming/stomp-websocket-interview-questions.md`](./03-protocols-and-event-streaming/stomp-websocket-interview-questions.md)
3. [`04-data-storage-and-caching/redis-interview-questions.md`](./04-data-storage-and-caching/redis-interview-questions.md)
4. [`02-software-architecture-and-design/architecture-hld-system-design-interview-questions.md`](./02-software-architecture-and-design/architecture-hld-system-design-interview-questions.md)

### 4. The Cloud & DevOps Lead (Kubernetes + Azure + Security)
1. [`06-cloud-and-devops/kubernetes-interview-questions.md`](./06-cloud-and-devops/kubernetes-interview-questions.md)
2. [`06-cloud-and-devops/azure-cloud-interview-questions.md`](./06-cloud-and-devops/azure-cloud-interview-questions.md)
3. [`01-language-and-frameworks/spring-security-oauth2-interview-questions.md`](./01-language-and-frameworks/spring-security-oauth2-interview-questions.md)
