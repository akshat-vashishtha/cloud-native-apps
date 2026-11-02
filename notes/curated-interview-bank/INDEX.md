# Staff / Principal Java & Cloud-Native Architect — Curated Scenario Interview Bank (`8–12+ Years Experience`)

> **Total Curated Production Scenarios:** **413 Deep-Dive Scenarios & Architecture Trade-offs** across **12 Domain Modules**  
> **Structure per Module:**
> - **Section 1 (`[P0 - Real-World Scenarios & Outage Triage]` — ~65%):** 22–23 real production incidents, outage forensics, 15-minute mitigations, and permanent architectural fixes.
> - **Section 2 (`[P1 - Architectural Trade-offs & Deep Design]` — ~35%):** 12 Staff/Principal design trade-offs, capacity math, internals, and Senior Traps to avoid.
>
> **5-Part Answer Framework Used in Every Single Question:**
> 1. **Root Cause Analysis (What actually happened?)**
> 2. **Immediate Incident Mitigation (First 15 minutes)**
> 3. **Permanent Architectural / Code Fix**
> 4. **Trade-offs to Defend in Front of Interviewer**
> 5. **Senior Trap to Avoid**

---

## 📊 Master Module Catalog (`413` Fully Answered Scenarios)

| # | Module Document | Total Scenarios (`P0` + `P1`) | Core Production & Triage Focus |
| :--- | :--- | :---: | :--- |
| **01** | [`01-core-java-concurrency-and-jvm-triage.md`](./01-core-java-concurrency-and-jvm-triage.md) | **35** (`23 P0` + `12 P1`) | Java 21 Virtual Thread carrier pinning (`synchronized`/JNI), Container `OOMKilled (137)` at 55% Heap (`NativeMemoryTracking`, Direct ByteBuffers, `glibc` arenas), `ForkJoinPool.commonPool()` starvation, 3-snapshot thread dump forensics, ZGC vs G1GC (`Humongous` allocations), JMM `happens-before`, False Sharing (`@Contended`), `ScopedValue` vs `ThreadLocal`. |
| **02** | [`02-spring-boot-3-and-production-pitfalls.md`](./02-spring-boot-3-and-production-pitfalls.md) | **35** (`23 P0` + `12 P1`) | `HikariCP` connection starvation (`idle in transaction` while holding `@Transactional` over HTTP/LLM calls), `spring.jpa.open-in-view` anti-pattern, Spring AOP proxy self-invocation traps, `@TransactionalEventListener(AFTER_COMMIT)` gotchas, Micrometer high-cardinality tag OOM, Graceful shutdown (`preStop` + `server.shutdown=graceful`), CDS/AOT startup optimization. |
| **03** | [`03-spring-security-6-and-zero-trust.md`](./03-spring-security-6-and-zero-trust.md) | **34** (`22 P0` + `12 P1`) | JWKS key rotation `401` stampedes, BFF Opaque Cookie (`APP_SESSION`) vs Stateless JWT, Instant JWT revocation at scale, Confused Deputy (`aud` claim bypass), Cross-Tenant BOLA/IDOR, `SecurityContext` loss across Virtual Threads/`CompletableFuture`, SSRF to Cloud IMDS (`169.254.169.254`), OAuth 2.1 PKCE, RFC 8693 Token Exchange, Spire/Istio mTLS. |
| **04** | [`04-distributed-systems-and-failure-modes.md`](./04-distributed-systems-and-failure-modes.md) | **35** (`23 P0` + `12 P1`) | Dual-write disasters, Transactional Outbox (`Debezium CDC` vs `SKIP LOCKED` polling), Saga Orchestration vs Choreography & zombie compensations, Distributed Locks with monotonic **Fencing Tokens**, Idempotency keys (`INSERT ... ON CONFLICT`), Retry storms (`M^N` amplification) & Adaptive Token Budgets, Metastable failures, Split-Brain & Lease leadership. |
| **05** | [`05-system-design-hld-real-world-scale.md`](./05-system-design-hld-real-world-scale.md) | **34** (`22 P0` + `12 P1`) | Flash Sale `500k RPS` inventory reservation (Redis Lua + Async Outbox), Double-Entry Immutable Payment Ledger (`NUMERIC(19,4)`), Hot Merchant Account Contention (**Sub-Account Sharding / Slot Splitting**), Global Distributed Rate Limiter (Local Token Batch + Redis Lua), WhatsApp/Slack WebSocket fan-out, Multi-Region Active-Active DR (`RPO=0` vs `RPO<5s`). |
| **06** | [`06-low-level-design-lld-and-concurrency.md`](./06-low-level-design-lld-and-concurrency.md) | **34** (`22 P0` + `12 P1`) | Machine Coding: Lock-free/Segmented LRU & `W-TinyLFU` Cache with `CompletableFuture` **Single-Flight** stampede collapsing, Lock-free CAS Token Bucket & Sliding Window Rate Limiter (`ConcurrentHashMap.compute`), Hierarchical Hashed Timing Wheel Scheduler, Circuit Breaker (`AtomicReference<State>` CAS), Lock-Free Order Matching Book (`TreeMap` + `ArrayDeque`), Seat Booking without Deadlocks (Canonical Lock Ordering). |
| **07** | [`07-kafka-and-event-streaming-disasters.md`](./07-kafka-and-event-streaming-disasters.md) | **35** (`23 P0` + `12 P1`) | Consumer Rebalance Storms (`max.poll.interval.ms` vs `session.timeout.ms`), `CooperativeStickyAssignor` & Static Membership (`group.instance.id`), Poison Pill Deserialization (`ErrorHandlingDeserializer` + Non-Blocking `@RetryableTopic`), Hot Partition Key Skew, `acks=all` + `min.insync.replicas=2` availability math, Zombie Producers & EOS (`transactional.id`), Parallel Consumer key-ordered concurrency. |
| **08** | [`08-caching-redis-and-in-memory-traps.md`](./08-caching-redis-and-in-memory-traps.md) | **34** (`22 P0` + `12 P1`) | Cache Stampede (Single-Flight + Logical Expiry / XFetch), Cache Penetration (`Bloom` / `Cuckoo` Filter + Negative Caching), Cache Avalanche (Jittered TTL + Two-Tier L1 Caffeine / L2 Redis), Hot-Key Single-Shard CPU saturation (L1 Near-Cache + RESP3 Client-Side Invalidation), Redis Cluster `{hash_tag}` vs `CROSSSLOT`, BigKey freezes (`UNLINK`/`SCAN`), Cache-Aside vs DB `Read-Modify-Write` stale race, Fork-based `BGSAVE` Copy-on-Write OOM (`vm.overcommit_memory=1`). |
| **09** | [`09-databases-sql-and-mongo-production.md`](./09-databases-sql-and-mongo-production.md) | **35** (`23 P0` + `12 P1`) | PostgreSQL Zero-Downtime DDL (`lock_timeout = '2s'`, `CREATE INDEX CONCURRENTLY`, `NOT VALID`), MVCC Dead-Tuple Bloat & `XID` Transaction Wraparound, Composite Index **ESR Rule (`Equality -> Sort -> Range`)**, Keyset/Seek Cursor Pagination vs `OFFSET 500000`, `Write Skew` under `REPEATABLE READ`, `PgBouncer` transaction pooling (`HikariCP` sizing formula `(Cores * 2) + Disk`), MongoDB `16MB` BSON array trap (Bucket Pattern) & `w:majority` + `readConcern:majority`. |
| **10** | [`10-protocols-grpc-and-websocket-scale.md`](./10-protocols-grpc-and-websocket-scale.md) | **34** (`22 P0` + `12 P1`) | gRPC HTTP/2 sticky connection load-balancing disaster on K8s ClusterIP (Headless Service `round_robin` / Envoy L7 / `MAX_CONNECTION_AGE`), Protobuf wire breaking changes (`reserved` tags & field names), gRPC `Deadline` propagation & `Context.fork()`, Blocking Netty EventLoop threads, HTTP/2 `maxConcurrentStreams` & BDP flow-control windows, Multi-Pod WebSocket/STOMP scaling (`StompBrokerRelay` / Redis PubSub), Slow-Consumer OOM protection (`sendBufferSizeLimit`). |
| **11** | [`11-kubernetes-and-azure-cloud-operations.md`](./11-kubernetes-and-azure-cloud-operations.md) | **34** (`22 P0` + `12 P1`) | Linux CFS CPU Throttling at 30% avg CPU (`cfs_quota_us` multi-threaded JVM bursts), Container `OOMKilled` (`-XX:MaxRAMPercentage=70.0` + `-XX:MaxDirectMemorySize`), Liveness vs Readiness vs Startup Probe cascading restart death spirals, Zero-Downtime Rolling Update (`preStop: sleep 10` endpoint propagation race), CoreDNS `ndots:5` DNS storms, Azure AKS Outbound SNAT Port Exhaustion (NAT Gateway + Private Endpoints), `PodDisruptionBudget` deadlocks, `topologySpreadConstraints`, KEDA Kafka-lag scaling. |
| **12** | [`12-spring-ai-and-agentic-production.md`](./12-spring-ai-and-agentic-production.md) | **34** (`22 P0` + `12 P1`) | Cross-Tenant RAG Vector Store data leaks (Mandatory Metadata Pre-Filtering + PG RLS), Runaway `$12k` Agentic Tool-Calling loops (`maxSteps` + Duplicate Tool Call Detector + TPM budgets), Indirect Prompt Injection & Excessive Agency (`OWASP LLM01/LLM06`), Holding `@Transactional` across `chatClient.call()`, Native `JSON_SCHEMA` Constrained Decoding, Hybrid Search (`BM25 + Vector + RRF + Cross-Encoder Reranker`), Contextual Chunking, MCP Server (`SSE/HTTP` + OAuth2), Human-in-the-Loop (`HITL`) durable checkpointing, Prompt Prefix KV-Caching, GPU VRAM `KV-Cache` sizing (`vLLM PagedAttention`). |

---

## 🧭 Targeted Interview Preparation Roadmaps by Interview Round

### Round 1: Java Internals, Concurrency & Machine Coding / LLD (`60–90 mins`)
1. [`01-core-java-concurrency-and-jvm-triage.md`](./01-core-java-concurrency-and-jvm-triage.md) — Virtual thread pinning, JMM, GC pauses, Off-heap memory.
2. [`06-low-level-design-lld-and-concurrency.md`](./06-low-level-design-lld-and-concurrency.md) — Concurrent LRU Cache with Single-Flight, Rate Limiter, Timing Wheel, Circuit Breaker, Order Book.
3. [`02-spring-boot-3-and-production-pitfalls.md`](./02-spring-boot-3-and-production-pitfalls.md) — Transaction propagation, AOP proxies, HikariCP internals.

### Round 2: Distributed Systems, Microservices & Data Engineering (`60–90 mins`)
1. [`04-distributed-systems-and-failure-modes.md`](./04-distributed-systems-and-failure-modes.md) — Outbox, Sagas, Fencing Tokens, Idempotency, Retry Storms.
2. [`07-kafka-and-event-streaming-disasters.md`](./07-kafka-and-event-streaming-disasters.md) — Rebalance storms, Partition skew, DLT topologies, EOS.
3. [`08-caching-redis-and-in-memory-traps.md`](./08-caching-redis-and-in-memory-traps.md) — Stampede, Penetration, Avalanche, Hot Keys, Cluster `CROSSSLOT`.
4. [`09-databases-sql-and-mongo-production.md`](./09-databases-sql-and-mongo-production.md) — Zero-downtime DDL, MVCC, ESR indexing, Keyset pagination, Lock queues.

### Round 3: High-Level System Design (HLD), Cloud Scale & Protocols (`60–90 mins`)
1. [`05-system-design-hld-real-world-scale.md`](./05-system-design-hld-real-world-scale.md) — Flash sales, Payment ledgers, Hot-account sharding, Global rate limiting, Multi-region DR.
2. [`10-protocols-grpc-and-websocket-scale.md`](./10-protocols-grpc-and-websocket-scale.md) — gRPC L7 load balancing, Deadlines, WebSocket/STOMP cluster relay & backpressure.
3. [`11-kubernetes-and-azure-cloud-operations.md`](./11-kubernetes-and-azure-cloud-operations.md) — CFS throttling, Probes, Rolling updates, CoreDNS `ndots:5`, SNAT exhaustion, KEDA.

### Round 4: Staff/Principal Architecture, Security & GenAI Production (`60 mins`)
1. [`03-spring-security-6-and-zero-trust.md`](./03-spring-security-6-and-zero-trust.md) — BFF Cookie vs JWT, JWKS rotation, BOLA/IDOR, OAuth 2.1, Zero-Trust mTLS.
2. [`12-spring-ai-and-agentic-production.md`](./12-spring-ai-and-agentic-production.md) — Multi-tenant RAG security, Hybrid Search (`RRF`), Agentic cost/loop guardrails, MCP servers, HITL workflows, GenAI OpenTelemetry observability.

---

## ⚡ Top 15 "Staff Engineer Litmus Test" Questions Across All 12 Modules

If you only have **2 hours** before a Staff/Principal Backend Architect interview, review these 15 high-signal scenarios first:

1. **Java 21 Virtual Threads:** Why does a `synchronized` block around socket/JDBC I/O freeze an 8-core pod at 12% CPU, and how does `ReentrantLock` fix it? ([Module 01 - Q1](./01-core-java-concurrency-and-jvm-triage.md))
2. **Spring Boot + HikariCP:** Why does calling a `500ms` REST/LLM API inside `@Transactional` exhaust a 20-connection DB pool at just `40 RPS` while DB CPU sits at 5%? ([Module 02 - Q1](./02-spring-boot-3-and-production-pitfalls.md))
3. **OAuth2 & Zero-Trust:** Why does rotating a signing key in `oauth2-authorization-server` trigger a `401` outage or JWKS DDoS storm across 30 Resource Servers unless Stale-While-Revalidate + Single-Flight `kid`-miss refresh is configured? ([Module 03 - Q1](./03-spring-security-6-and-zero-trust.md))
4. **Distributed Locks:** Why is a Redis TTL lock (`SET NX PX`) **unsafe** for financial mutations when a JVM hits a 12-second GC pause, and how do **Monotonic Fencing Tokens** solve it? ([Module 04 - Q4](./04-distributed-systems-and-failure-modes.md))
5. **High-Scale System Design:** How do you process `5,000 TPS` of credits/debits on a single celebrity/merchant wallet row without PostgreSQL row-lock serialization killing throughput (**Sub-Account Slot Sharding `1..64`**)? ([Module 05 - Q3](./05-system-design-hld-real-world-scale.md))
6. **Concurrent LLD:** How do you prevent 1,000 threads from firing duplicate DB queries on a cache miss in an in-memory cache using `ConcurrentHashMap.computeIfAbsent(key, k -> CompletableFuture)` (**Request Collapsing / Single-Flight**)? ([Module 06 - Q1](./06-low-level-design-lld-and-concurrency.md))
7. **Apache Kafka:** Why does a downstream latency spike trigger an infinite `CommitFailedException` Rebalance Storm (`max.poll.interval.ms` vs `max.poll.records`), and how do you pause/resume partitions or use non-blocking retry topics? ([Module 07 - Q1](./07-kafka-and-event-streaming-disasters.md))
8. **Redis Cluster:** Why can a single hot key (`150,000 RPS`) pin 1 Redis Cluster node at 100% CPU while the other 19 nodes sit at 5%, and why can't `{hash_tag}` fix a single hot key? ([Module 08 - Q4](./08-caching-redis-and-in-memory-traps.md))
9. **PostgreSQL Production DDL:** Why does a `1ms` `ALTER TABLE orders ADD COLUMN ...` lock 100% of `SELECT` and `INSERT` queries on a busy table (**The `ACCESS EXCLUSIVE` Lock Queue Sandwich**), and why is `SET LOCAL lock_timeout = '2s'` mandatory? ([Module 09 - Q1](./09-databases-sql-and-mongo-production.md))
10. **PostgreSQL Indexing:** How do you design composite B-Tree indexes using the **ESR Rule (`Equality -> Sort -> Range`)** so queries never suffer in-memory `Sort` or index filter scans? ([Module 09 - Q4](./09-databases-sql-and-mongo-production.md))
11. **gRPC on Kubernetes:** Why does scaling a gRPC service from 2 pods to 10 pods leave the 8 new pods at `0%` traffic behind a standard Kubernetes `ClusterIP` Service? ([Module 10 - Q1](./10-protocols-grpc-and-websocket-scale.md))
12. **WebSocket/STOMP Scale:** Why do messages sent via `SimpMessagingTemplate.convertAndSendToUser()` vanish 80% of the time when `chat-communication-service` scales beyond 1 pod with `enableSimpleBroker`? ([Module 10 - Q2](./10-protocols-grpc-and-websocket-scale.md))
13. **Kubernetes CFS Throttling:** How can a Java pod at only **30% average CPU utilization** suffer **65% Linux CFS CPU Throttling** and `800ms` p99 latency spikes? ([Module 11 - Q1](./11-kubernetes-and-azure-cloud-operations.md))
14. **Kubernetes Zero-Downtime Deploys:** Why does `server.shutdown=graceful` STILL produce `502 Bad Gateway` errors during a rolling update unless paired with a `lifecycle.preStop` hook (`sleep 10`)? ([Module 11 - Q4](./11-kubernetes-and-azure-cloud-operations.md))
15. **Spring AI 1.0 & RAG Security:** Why does naive multi-tenant RAG leak Tenant B's documents to Tenant A, why must `tenantId` NEVER be passed as an `@ToolParam` chosen by the LLM, and how does **Hybrid Search (`BM25 + Vector + RRF + Cross-Encoder Reranker`)** fix exact SKU/error-code retrieval? ([Module 12 - Q1 & Q6](./12-spring-ai-and-agentic-production.md))
