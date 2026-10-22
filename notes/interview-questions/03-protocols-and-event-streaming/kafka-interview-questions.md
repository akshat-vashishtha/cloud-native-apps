# Spring Boot + Kafka Interview Questions (10-Year Java Lead)

## 1. Event-Driven Design
1. Compare event notification, event-carried state transfer, and event sourcing. When would you choose each?
2. How do you solve the dual-write problem when a service must update its DB and publish an event?
3. Explain the transactional outbox pattern. Polling publisher vs CDC (Debezium): how do you choose?
4. Does Kafka guarantee exactly-once? What are the limits of EOS in a Spring Boot service that also writes to a DB?
5. How do you guarantee ordering of events for one aggregate, and what breaks that guarantee?
6. Choreography vs orchestration for sagas: trade-offs, compensation handling, and timeouts?
7. How do you evolve event schemas without breaking consumers? How do Schema Registry, Avro, and Protobuf help?
8. How do you design an event: key, payload, headers, event ID, correlation ID, versioning?
9. Events vs commands: when should a service publish an event and when should it call another service synchronously?
10. How do you handle large payloads in events?
11. How do you implement idempotent consumers? Where do you keep the deduplication state?
12. What is CQRS, and how do Kafka events keep read models in sync?

---

## 2. Architecture
1. How do you decide topic design: naming, partitions, retention, compaction, one topic per event type vs per aggregate?
2. How do you choose the partition count, and what happens if you increase it later?
3. What causes rebalance storms, and how do you reduce them (cooperative assignor, static membership, KIP-848)?
4. Kafka vs RabbitMQ vs Azure Service Bus vs Event Hubs: how do you choose?
5. Kafka Streams vs plain consumers vs Spring Cloud Stream: when do you use each?
6. What are Kafka Queues (share consumers), and how do they differ from consumer groups?
7. How do you design multi-region or disaster recovery for Kafka (MirrorMaker 2, active-active vs active-passive)?
8. How do you enforce data ownership between microservices when events are shared?
9. How do you test Kafka-based services (Embedded Kafka vs Testcontainers, contract tests)?
10. What changed with KRaft replacing ZooKeeper, and how does it affect your deployment and tests?
11. How do you secure Kafka (SASL/SSL, ACLs, secrets handling) in an Azure/AKS setup?
12. How do you handle multi-tenancy in topics and consumer groups?

---

## 3. Configuration
1. Walk through a production-safe producer configuration and justify each property (acks, idempotence, delivery.timeout.ms, linger.ms, batch.size, compression).
2. How do replication.factor, min.insync.replicas, and acks interact? What if two brokers go down?
3. Why is unclean.leader.election.enable usually false?
4. Explain max.poll.records, max.poll.interval.ms, session.timeout.ms, and heartbeat.interval.ms. Why does a consumer get kicked out of a group?
5. Compare Spring Kafka AckMode options. When do you use manual acknowledgement?
6. How do you handle deserialization failures (ErrorHandlingDeserializer, trusted packages, type mapping)?
7. How does concurrency work in ConcurrentKafkaListenerContainerFactory, and what is the relationship to the partition count?
8. How do you manage topic creation: NewTopic beans vs IaC vs admin scripts?
9. What does auto.offset.reset do, and what pitfalls does it cause for new consumer groups?
10. How do you configure transactions in Spring Kafka (transactional.id prefix, KafkaTransactionManager, read_committed)?
11. How do you configure observability (Micrometer, tracing across async hops)?

---

## 4. Fallback and Resilience
1. Explain DefaultErrorHandler, back-off policies, and DeadLetterPublishingRecoverer.
2. How do you classify exceptions as retryable vs non-retryable?
3. Blocking retries vs non-blocking retries (@RetryableTopic): trade-offs for ordering, throughput, and topic count?
4. What do you do with messages in a DLT? Who owns them, and how do you replay them?
5. Kafka is unavailable or slow when your service publishes. What happens to the user’s request?
6. A downstream dependency (DB, REST API) is down while consumers are running. How do you avoid retry exhaustion and mass DLT-ing?
7. How do you use circuit breakers or bulkheads inside a Kafka consumer?
8. How do you pause and resume a listener container safely?
9. How do you recover from a bad deployment that processed events incorrectly (offset reset, replay, compensation)?
10. How do you handle a poison pill message that blocks a partition?
11. How do retries interact with idempotency?

---

## 5. Performance
1. How do you tune producers and consumers for throughput vs latency?
2. Consumer lag is growing. How do you diagnose and fix it?
3. How do you scale consumption beyond the partition count while preserving per-key ordering?
4. When do you use batch listeners, and how do you handle partial failures in a batch?
5. How do you detect and fix hot partitions and key skew?
6. Why is Kafka fast (sequential I/O, page cache, zero-copy, batching)?
7. JSON vs Avro vs Protobuf: impact on size, CPU, and compatibility?
8. How do you size a Kafka cluster and a Spring Boot consumer fleet for a target throughput?
9. Can you use virtual threads with Kafka consumers? What are the caveats?
10. Which metrics and alerts do you set up for Kafka producers, consumers, and brokers?
11. How do you avoid your idempotency store (Redis or Mongo) becoming the bottleneck?

---

## 6. Architectural Scenario Questions
1. Your monolith calls five services via REST in a chain. Migrate it to event-driven with zero downtime. How do you sequence it?
2. Order, Payment, Inventory, and Notification services must stay consistent without distributed transactions. Design the flow and failure handling.
3. Two services need the same data. One team wants shared DB access, another wants events. How do you decide?
4. A payment event was processed twice in production. Walk through how it happened and how you prevent it.
5. Events from two different topics (order created, payment completed) arrive out of order. How should the consumer cope?
6. Another team wants to change the schema of an event your five services consume. How do you handle it?
7. You must reprocess six months of historical events into a new read model. How do you do it without affecting live traffic?
8. A new compliance rule requires deleting a user’s data (GDPR) while events are retained in Kafka. What are your options?
9. Your team is split on Kafka vs Azure Service Bus for a new workload. How do you run the decision?
10. A consumer team keeps losing messages in production and blames Kafka. How do you investigate?
11. How do you migrate from a self-managed Kafka cluster to a managed offering with no data loss?

---

## 7. System Design Scenario Questions
1. Design a real-time chat platform (gateway, services, Kafka, Redis, MongoDB) handling millions of concurrent users. Cover topics, partitioning keys, ordering, and delivery guarantees.
2. Design a notification system (email, SMS, push) with retries, prioritisation, rate limiting, and deduplication.
3. Design an order-processing system for flash sales with 100x traffic spikes. How do Kafka and your consumers absorb the load?
4. Design a payments ledger with Kafka as the backbone. How do you guarantee no lost or duplicate transactions?
5. Design an audit and activity-tracking pipeline for all microservices, with search and long-term retention.
6. Design a real-time analytics dashboard (clicks, orders per minute) using Kafka Streams or a similar approach.
7. Design a CDC pipeline from MongoDB to downstream services and a search index.
8. Design an AI-request platform where requests are queued, processed by LLM workers, and results are streamed back. How do you handle slow workers, retries, and backpressure?
9. Design a multi-region, highly available event platform with RPO and RTO targets.
10. At 2 AM, consumer lag reaches 5 million after a release. What are your first 30 minutes, and what would have prevented it?

---
---

# Questions and Answers

## 1. Event-Driven Design

### 1. Compare event notification, event-carried state transfer, and event sourcing. When would you choose each?
- **Event Notification:** The event contains minimal information (e.g., entity ID and action: `{"orderId": "123", "status": "CREATED"}`). The consumer must query the producer's API/DB to get the full state.
  - *When to choose:* When payloads must stay tiny, when consumers need real-time up-to-the-second state directly from the source, or for cache invalidation.
  - *Trade-off:* High temporal coupling; causes query stampedes on the producer service.
- **Event-Carried State Transfer (ECST):** The event contains the complete snapshot or state delta needed by downstream consumers (e.g., order lines, customer info, totals).
  - *When to choose:* Cross-microservice bounded context communication. Consumers maintain their own local read projections and operate autonomously without calling the producer.
  - *Trade-off:* Higher Kafka storage/network usage; requires strict schema evolution and contract governance.
- **Event Sourcing:** State changes are stored as an immutable sequence of domain events (`OrderCreated`, `PaymentReceived`, `ItemAdded`). The event log itself is the primary source of truth. Current state is reconstructed by replaying events.
  - *When to choose:* Audit-critical domains (financial ledgers, trading, complex state machines) where historical state at any point in time must be proven.
  - *Trade-off:* High architectural complexity; requires snapshots and CQRS to support performant read queries.

### 2. How do you solve the dual-write problem when a service must update its DB and publish an event?
- **The Problem:** Writing to a database and publishing to Kafka are two separate network operations. If the DB commits and Kafka publish fails, or if Kafka publish succeeds and DB transaction rolls back, data consistency is broken.
- **Solution 1 - Transactional Outbox Pattern (Recommended):** The business entity and the outbound event are committed atomically in the *same local database transaction* (into an `outbox` table). A separate worker or CDC engine reads the outbox table and guarantees delivery to Kafka.
- **Solution 2 - Change Data Capture (CDC via Debezium):** The service only writes to its primary database tables. Debezium tails the database transaction log (PostgreSQL WAL, MySQL binlog) and streams changes directly into Kafka.
- **Anti-pattern to Avoid:** Distributed 2PC (Two-Phase Commit / XA) between database and Kafka. Kafka does not natively support XA; attempting distributed 2PC degrades performance and causes distributed lock contention.

### 3. Explain the transactional outbox pattern. Polling publisher vs CDC (Debezium): how do you choose?
- **Mechanism:** The application writes its business record and an `event_outbox` record inside a single ACID database transaction.
- **Polling Publisher:** A scheduled background thread (e.g., Spring `@Scheduled`) queries `event_outbox` using `SELECT ... FOR UPDATE SKIP LOCKED`, publishes to Kafka, and marks the record as processed or deletes it.
  - *Pros:* Simple to implement; no extra infrastructure needed.
  - *Cons:* Adds polling overhead to the relational database; polling interval adds latency (hundreds of ms); table bloat if not vacuumed properly.
- **Log-Based CDC (Debezium):** A Kafka Connect Debezium connector streams inserts into `event_outbox` directly from the database's write-ahead log (WAL) without executing SQL queries.
  - *Pros:* Zero database query overhead; near-zero latency (<10ms); natural ordering preserved from the transaction log.
  - *Cons:* Requires managing Kafka Connect infrastructure; operations and schema evolution require operational maturity.
- **How to Choose:** Choose Polling for low-throughput (<100 msgs/sec) or simple setups without Kafka Connect. Choose Debezium CDC for high-throughput, low-latency, mission-critical systems.

### 4. Does Kafka guarantee exactly-once? What are the limits of EOS in a Spring Boot service that also writes to a DB?
- **Kafka's EOS Scope:** Kafka provides Exactly-Once Semantics (EOS) strictly **within the Kafka boundary** (Kafka-to-Kafka) using transactional producers, idempotent producers, and `read_committed` consumers (e.g., in Kafka Streams or consume-transform-produce flows).
- **Limits with a Database:** When a consumer reads from Kafka and writes to a database (or calls an external API), Kafka's transaction manager cannot atomically commit both the database transaction and the Kafka offset in a single step without distributed 2PC.
  - If the database commits but the Kafka offset commit fails, Kafka redelivers the message on consumer restart (duplicate DB write).
  - If Kafka commits offset first and DB crashes, message is lost.
- **How to Achieve Effective EOS:**
  1. Accept **At-Least-Once** transport from Kafka.
  2. Implement an **Idempotent Consumer** on the database side (e.g., inserting into a `processed_events` table inside the DB transaction, or using natural business keys/upserts).

### 5. How do you guarantee ordering of events for one aggregate, and what breaks that guarantee?
- **Guarantee Mechanism:** Kafka guarantees strict FIFO ordering **only within a single partition**. By using the aggregate ID (e.g., `orderId`) as the record's **Partition Key**, all events for that aggregate hash to the exact same partition (`abs(Murmur2(key)) % totalPartitions`) and are read sequentially by a single consumer thread.
- **What Breaks the Guarantee:**
  1. **Producer Retries without Idempotence:** If message 1 fails and retries after message 2 succeeds, order flips. (*Fix:* `enable.idempotence=true` and `max.in.flight.requests.per.connection <= 5`).
  2. **Topic Partition Count Increase:** Modulo hashing changes. Future events for the aggregate land in a different partition than historical ones.
  3. **Multi-Threaded Consumer Processing:** If a consumer fetches a batch and delegates records to an async thread pool (`ExecutorService` / `CompletableFuture`), processing completes out-of-order.
  4. **Non-Blocking Retry Topics (`@RetryableTopic`):** Failed messages are routed to retry topics while subsequent messages on the main partition continue processing ahead of them.

### 6. Choreography vs orchestration for sagas: trade-offs, compensation handling, and timeouts?
- **Choreography (Event-Driven):** Services publish domain events and react to each other's events without a central coordinator.
  - *Pros:* Loose runtime coupling, high autonomy, simple for short workflows (2–3 steps).
  - *Cons:* Cyclic dependencies risk; difficult to track global state; hard to coordinate distributed timeouts; compensation cascades require reverse event publishing across multiple services.
- **Orchestration (Command-Driven):** A central coordinator (e.g., Temporal, Camunda, or a custom Saga state machine service) sends explicit commands to participants and listens for replies.
  - *Pros:* Single source of truth for saga state; easy visualization; trivial timeout management (orchestrator fires timers); straightforward compensation (orchestrator sends explicit rollback commands).
  - *Cons:* Orchestrator becomes a coordinator bottleneck; higher coupling between orchestrator and domain tasks.
- **Architectural Rule:** Use Choreography for simple 2–3 step linear flows. Use Orchestration for complex, mission-critical financial or multi-branch workflows with extensive compensation logic.

### 7. How do you evolve event schemas without breaking consumers? How do Schema Registry, Avro, and Protobuf help?
- **Schema Registry Role:** Centralized repository for schemas (Avro, Protobuf, JSON Schema). Producers and consumers register and validate schemas against compatibility rules.
- **Wire Format:** Messages carry a 5-byte header (1 magic byte + 4-byte Schema ID) followed by the binary payload. Consumers lookup the writer schema by ID from their local cache and deserialize using their local reader schema.
- **Compatibility Modes:**
  - `BACKWARD`: New schema can read data written by previous schema (consumers upgraded before producers). Only delete optional fields; only add fields with defaults.
  - `FORWARD`: Old schema can read data written by new schema (producers upgraded before consumers). Only add optional fields; only delete fields with defaults.
  - `FULL`: Both backward and forward compatible. Enables independent producer and consumer deployments.
  - `*_TRANSITIVE`: Validates compatibility against all previous versions, not just $N-1$ (crucial when consumers skip intermediate releases).

### 8. How do you design an event: key, payload, headers, event ID, correlation ID, versioning?
- **Key:** Business aggregate ID (`orderId`, `customerId`) to ensure partition affinity and ordering.
- **Payload:** Domain data representing the state change. Follow CloudEvents or an enterprise envelope standard. Keep domain models decoupled from persistence models.
- **Headers:** Metadata for infrastructure and routing:
  - `id` / `eventId`: Unique UUID for consumer deduplication.
  - `correlationId`: Traces request chain across distributed microservices.
  - `causationId`: The event or command ID that triggered this event.
  - `timestamp`: Event creation time (epoch ms).
  - `type` / `eventType`: Fully qualified event name (e.g., `com.enterprise.order.created`).
  - `version` / `schemaVersion`: Schema version indicator (e.g., `v2`).
  - `traceparent`: W3C trace context for distributed tracing (OpenTelemetry).

### 9. Events vs commands: when should a service publish an event and when should it call another service synchronously?
- **Command:** An instruction with intent to trigger a specific action (point-to-point, 1-to-1). Can be rejected.
  - *Use Synchronous REST/gRPC when:* The caller must know the immediate outcome to continue (e.g., User Authentication, Payment Gateway Authorization, direct queries for read consistency).
- **Event:** An immutable fact stating something that already occurred (broadcast, 1-to-many). Cannot be rejected.
  - *Use Asynchronous Kafka Events when:* The operation is eventually consistent (e.g., Order fulfillment, invoice generation, notifications), requires high throughput, fan-out to multiple consumers, or decoupled service availability.

### 10. How do you handle large payloads in events?
- **Why it matters:** Messages >1MB degrade Kafka page cache performance, increase broker memory pressure, and cause GC spikes.
- **Pattern 1 - Claim-Check Pattern (Best Practice):** Producer uploads the heavy binary/document (e.g., 20MB PDF, high-res images) to S3 / Azure Blob Storage. Producer publishes a lightweight Kafka event containing the storage URI, metadata, and hash. Consumers download payload on demand.
- **Pattern 2 - Compression:** Use `compression.type=zstd` or `snappy` on the producer. Shrinks JSON payloads by 70–80% with minimal CPU overhead.
- **Pattern 3 - Chunking (Avoid if possible):** Split large payload into smaller chunks and reassemble on the consumer. Fragile across rebalances and increases consumer memory footprint.

### 11. How do you implement idempotent consumers? Where do you keep the deduplication state?
- **Deduplication Store in Relational DB:** Insert the `event_id` into a `processed_events` table inside the *same ACID transaction* that updates business data.
  - *Pros:* 100% atomic. If the business write rolls back, the deduplication entry rolls back as well.
- **Deduplication Store in Redis:** Execute an atomic `SET key val NX EX 86400` (24-hour TTL) before executing business logic.
  - *Pros:* Ultra-low latency (<1ms check); offloads primary database.
  - *Cons:* Non-atomic with database. If the DB fails after Redis sets the key, the event will never be reprocessed without manual intervention.
- **Natural Business Idempotency:** Use database upserts (`ON CONFLICT DO UPDATE`) or state machine validation (e.g., `UPDATE orders SET status = 'PAID' WHERE id = :id AND status = 'PENDING'`).

### 12. What is CQRS, and how do Kafka events keep read models in sync?
- **CQRS Concept:** Command Query Responsibility Segregation separates write mutations (Commands) from read operations (Queries) into dedicated models or databases.
- **Sync Mechanism with Kafka:**
  1. The Command service writes to the write DB (e.g., PostgreSQL) and emits domain events via Transactional Outbox.
  2. Kafka topics stream events to Projection Consumers.
  3. Projection consumers denormalize and update the Read Store (e.g., Elasticsearch for search, Redis for caching, MongoDB for document retrieval).
- **Handling Inconsistencies:** Read models are eventually consistent. Projections must track entity version/sequence numbers to discard out-of-order stale events.

---

## 2. Architecture

### 1. How do you decide topic design: naming, partitions, retention, compaction, one topic per event type vs per aggregate?
- **Naming Convention:** `<env>.<domain>.<aggregate>.<event-type-or-dataset>.<version>` (e.g., `prod.payments.billing.invoice-created.v1`).
- **Partitions:** Sized based on producer write throughput and consumer read throughput targets ($\max(P/P_c, C/C_c)$).
- **Retention:**
  - `cleanup.policy=delete`: Time/size-based retention for transactional event streams.
  - `cleanup.policy=compact`: Retains latest record per key indefinitely. Used for reference data, read-model bootstrapping, and state recovery.
- **One Topic per Aggregate vs Per Event Type:**
  - *Per Aggregate (Recommended for DDD):* All lifecycle events for an aggregate (`OrderCreated`, `OrderShipped`, `OrderCancelled`) go to one topic with `orderId` as key. Preserves absolute ordering across all lifecycle transitions.
  - *Per Event Type:* Consumers subscribe only to what they need, but ordering across different event types for the same aggregate is lost.

### 2. How do you choose the partition count, and what happens if you increase it later?
- **Sizing Rule:** $\text{Partitions} = \max\left(\frac{\text{Target Throughput}}{\text{Single Producer Throughput}}, \frac{\text{Target Throughput}}{\text{Single Consumer Throughput}}\right)$.
- **Increasing Partitions Later:**
  - Existing records are not moved.
  - The default partition hash modulo formula changes: $\text{Murmur2}(\text{key}) \pmod{\text{NewPartitionCount}}$.
  - Future events for an existing aggregate ID will hash to a **different partition**, permanently breaking sequential ordering between old and new events for that entity.
  - *Mitigation:* Over-partition initially (e.g., 24 or 48 partitions), or migrate to a new topic version with a planned consumer cutover.

### 3. What causes rebalance storms, and how do you reduce them (cooperative assignor, static membership, KIP-848)?
- **Causes:**
  1. Slow batch processing exceeding `max.poll.interval.ms`.
  2. JVM GC pauses or network blips exceeding `session.timeout.ms`.
  3. Frequent pod restarts during rolling Kubernetes deployments.
  4. Classic "Eager" rebalance protocol revoking all partitions across all consumers simultaneously.
- **Mitigations:**
  1. **Cooperative Sticky Assignor (KIP-429):** Consumers incrementally migrate only the partitions that need reassigning without revoking unchanged partitions (`partition.assignment.strategy=CooperativeStickyAssignor`).
  2. **Static Membership (KIP-345):** Set `group.instance.id=pod-name`. If a pod restarts within `session.timeout.ms`, coordinator does not trigger a rebalance.
  3. **Next-Gen Consumer Protocol (KIP-848):** Moves assignment logic to the broker coordinator, eliminating multiple client-side synchronization rounds.

### 4. Kafka vs RabbitMQ vs Azure Service Bus vs Event Hubs: how do you choose?
- **Kafka:** Distributed append-only log. Extreme throughput (>1M msgs/sec), native replayability, stream processing. Ideal for event sourcing, CDC, microservice event backbones.
- **RabbitMQ:** AMQP message broker. Smart broker, dumb consumer. Complex exchange routing, per-message acknowledgments. Ideal for transactional job queues, complex message routing, low-latency task distribution.
- **Azure Service Bus:** Enterprise cloud messaging. Native message sessions (FIFO), scheduled messages, dead-lettering, transactions, duplicate detection. Ideal for enterprise workflows requiring individual message lifecycle management.
- **Azure Event Hubs:** Kafka-compatible distributed partitioned log for Azure-native big data and telemetry streaming.

### 5. Kafka Streams vs plain consumers vs Spring Cloud Stream: when do you use each?
- **Plain Consumer (`@KafkaListener`):** Simple event ingestion, database persistence, triggering external APIs. Lowest memory overhead.
- **Kafka Streams:** Stateful stream processing, stream-stream/stream-table joins, windowed aggregations, interactive state stores (RocksDB). Built-in fault tolerance via changelog topics.
- **Spring Cloud Stream:** Multi-binder abstraction (switch between Kafka, RabbitMQ, etc. via config), functional programming model (`Function<Order, Payment>`). Use when platform abstraction across message brokers is a mandate.

### 6. What are Kafka Queues (share consumers), and how do they differ from consumer groups?
- **Standard Consumer Groups:** A partition can only be consumed by **one** consumer instance in a group. Causes head-of-line blocking if one message takes long to process; scaling is limited by partition count.
- **Share Consumers / Share Groups (KIP-932):**
  - Allows multiple consumers in the same share group to read from the **same partition concurrently**.
  - Acknowledgment happens at the **individual record level** (ACK, REJECT, RELEASE).
  - Eliminates head-of-line blocking and allows queue-like horizontal scaling directly on Kafka topics.

### 7. How do you design multi-region or disaster recovery for Kafka (MirrorMaker 2, active-active vs active-passive)?
- **Active-Passive (Recommended for Consistency):**
  - Primary region receives all producer writes and consumer reads.
  - MirrorMaker 2 continuously replicates topics and translates consumer offsets to Secondary region.
  - On disaster, point DNS/client bootstrap servers to Secondary; consumers resume from translated offsets.
  - RPO: Sub-second to few seconds; RTO: Minutes.
- **Active-Active:**
  - Both regions accept writes into region-specific topic namespaces (`us.orders`, `eu.orders`) to prevent replication loops.
  - Consumers consume from both topics and handle distributed conflict resolution and eventual consistency.

### 8. How do you enforce data ownership between microservices when events are shared?
1. **Private vs Public Topics:** Only the owning service writes to its topics. Other services are granted read-only ACL permissions.
2. **Anti-Corruption Layer (ACL):** Consuming services must map incoming external events into their internal domain models using dedicated DTOs and mappers. Never reuse external Kafka models as internal entities.
3. **Contract Governance:** All public event schemas are versioned and managed in Schema Registry with strict CI/CD compatibility validation.

### 9. How do you test Kafka-based services (Embedded Kafka vs Testcontainers, contract tests)?
- **Testcontainers (Production Standard):** Spins up a real Kafka container in Docker via `KafkaContainer`. Provides complete parity with production broker behavior, KRaft, and network conditions.
- **Embedded Kafka (`@EmbeddedKafka`):** Runs an in-memory Kafka broker inside the JVM. Fast for unit/slice tests, but can differ slightly in timing, KRaft nuances, and broker versions.
- **Contract Tests (Pact / Schema Registry Tests):** Verify that producer schemas and consumer schemas remain compatible during CI builds without spinning up running broker infrastructure.

### 10. What changed with KRaft replacing ZooKeeper, and how does it affect your deployment and tests?
- **What Changed:** Metadata is now stored and managed internally within Kafka itself via a dedicated Raft metadata quorum (`@metadata` topic), removing the external ZooKeeper cluster.
- **Impact on Deployments:**
  - Single process to manage, monitor, and secure.
  - Controller failover drops from tens of seconds to single-digit milliseconds.
  - Supports scaling to millions of partitions per cluster without ZooKeeper znode bottlenecks.
- **Impact on Tests:** Embedded and container tests start significantly faster and require less memory.

### 11. How do you secure Kafka (SASL/SSL, ACLs, secrets handling) in an Azure/AKS setup?
- **Encryption:** mTLS (Mutual TLS) for wire encryption across brokers and clients.
- **Authentication:** SASL/OAUTHBEARER integrating with Azure Entra ID (Managed Identities) so pods authenticate without static passwords, or SASL/SCRAM-SHA-512.
- **Authorization:** Granular Kafka ACLs (read/write permissions bound to principal identity).
- **Secrets Management:** Ingress certificates and passwords injected via Azure Key Vault Secrets Store CSI Driver directly into AKS pod volumes.

### 12. How do you handle multi-tenancy in topics and consumer groups?
- **Shared Topics with Tenant Header:** Single topic for all tenants; record header contains `tenant_id`. Cost-effective, but susceptible to noisy neighbors.
- **Topic-per-Tenant:** Strict isolation (`tenantA.orders`, `tenantB.orders`), but risks partition explosion on large tenant counts.
- **Kafka Quotas (Recommended):** Use Kafka client byte-rate and request quotas per user/client ID (`kafka-configs.sh --alter --entity-type users --entity-name tenantA --add-config producer_byte_rate=5242880`). Brokers throttle noisy tenants automatically.

---

## 3. Configuration

### 1. Walk through a production-safe producer configuration and justify each property (acks, idempotence, delivery.timeout.ms, linger.ms, batch.size, compression).
- `acks=all` (`-1`): Producer waits for all in-sync replicas to acknowledge before considering write successful (zero message loss).
- `enable.idempotence=true`: Assigns Producer ID and sequence numbers to records. Prevents duplicate writes on retry and guarantees per-partition ordering.
- `delivery.timeout.ms=120000`: Upper bound on the total time a record can retry before failing (must be $\ge \text{linger.ms} + \text{request.timeout.ms}$).
- `max.in.flight.requests.per.connection=5`: Enables pipeline throughput without violating ordering (safe with idempotence).
- `linger.ms=20`: Buffers records up to 20ms to assemble larger batches.
- `batch.size=65536` (64KB): Max batch size for batching requests, maximizing throughput.
- `compression.type=zstd` (or `snappy`): Compresses record batches, reducing network IO and broker disk usage.

### 2. How do replication.factor, min.insync.replicas, and acks interact? What if two brokers go down?
- **Interaction Rule:** For zero data loss, configure: $\text{Replication Factor (RF)} = 3$, $\text{min.insync.replicas (min.isr)} = 2$, and producer `acks=all`.
- **If Two Brokers Go Down in a 3-Broker Cluster:**
  - Remaining in-sync replicas = 1.
  - Since $1 < \text{min.insync.replicas (2)}$, the surviving broker refuses to accept new writes and throws `NotEnoughReplicasException`.
  - *Result:* Preserves data consistency over availability. Consumers can still read existing committed data.

### 3. Why is unclean.leader.election.enable usually false?
- Setting it to `true` allows an out-of-sync replica (missing committed records) to become the partition leader if all in-sync replicas crash.
- This causes **permanent silent data loss** and offset regression.
- Keep `false` in production so that writes halt until an in-sync replica recovers, ensuring zero data loss.

### 4. Explain max.poll.records, max.poll.interval.ms, session.timeout.ms, and heartbeat.interval.ms. Why does a consumer get kicked out of a group?
- `heartbeat.interval.ms` (e.g., 3s): Heartbeat thread ping interval to coordinator.
- `session.timeout.ms` (e.g., 45s): Max time coordinator waits for a heartbeat before declaring consumer dead.
- `max.poll.records` (e.g., 50–100): Maximum records returned in a single `poll()` call.
- `max.poll.interval.ms` (e.g., 300s): Maximum time allowed between consecutive `poll()` invocations by the application thread.
- **Why Evicted:** If consumer thread takes longer than `max.poll.interval.ms` to process a batch (e.g., slow DB or HTTP timeouts), the coordinator assumes the consumer is hung, evicts it, and triggers a rebalance.

### 5. Compare Spring Kafka AckMode options. When do you use manual acknowledgement?
- `RECORD`: Commit offset after each record is processed (high overhead).
- `BATCH` (Default): Commit offset when all records from the `poll()` batch complete.
- `TIME` / `COUNT` / `COUNT_TIME`: Commit based on elapsed time or processed count.
- `MANUAL`: Offset commit queued when `Acknowledgment.acknowledge()` is called; committed on batch completion.
- `MANUAL_IMMEDIATE`: Offset committed immediately and asynchronously to broker when `acknowledge()` is called.
- *When to use Manual Ack:* When processing records individually and needing strict control to only commit offsets for records that have been verified and saved to the database.

### 6. How do you handle deserialization failures (ErrorHandlingDeserializer, trusted packages, type mapping)?
- Wrap key/value deserializers in Spring's `ErrorHandlingDeserializer`:
  ```properties
  spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
  spring.kafka.consumer.properties.spring.deserializer.value.delegate.class=org.springframework.kafka.support.serializer.JsonDeserializer
  spring.kafka.consumer.properties.spring.json.trusted.packages=com.enterprise.*
  ```
- *Mechanism:* Catches deserialization exceptions inside `poll()`, attaches error headers to the record, passes `null` value to the listener, allowing `DefaultErrorHandler` to route the poisoned message directly to the DLT without crashing the container.

### 7. How does concurrency work in ConcurrentKafkaListenerContainerFactory, and what is the relationship to the partition count?
- `factory.setConcurrency(N)` spawns $N$ independent `KafkaMessageListenerContainer` worker threads.
- **Relationship:** Active threads $\le$ Assigned topic partitions.
  - If partitions = 6 and concurrency = 3: each thread gets 2 partitions.
  - If partitions = 6 and concurrency = 6: each thread gets 1 partition (max efficiency).
  - If partitions = 3 and concurrency = 6: 3 threads consume partitions; **3 threads sit permanently idle**.

### 8. How do you manage topic creation: NewTopic beans vs IaC vs admin scripts?
- `NewTopic` beans: Spring `KafkaAdmin` automatically provisions missing topics on boot. Good for local development and Testcontainers; dangerous in production (bypasses governance).
- Admin Shell Scripts: Useful for manual emergency DBA operations; non-declarative.
- **IaC (Terraform / Strimzi CRDs) (Production Standard):** Declarative, version-controlled, automated via CI/CD pipelines with change approvals. Production brokers should have `auto.create.topics.enable=false`.

### 9. What does auto.offset.reset do, and what pitfalls does it cause for new consumer groups?
- `latest`: New consumer group starts reading from the tail of the log (only new incoming messages).
  - *Pitfall:* Any messages produced while the consumer was deploying or provisioning its group are **permanently skipped**.
- `earliest`: New consumer group starts reading from offset 0 (beginning of the log).
  - *Pitfall:* On a topic with 30-day retention, a new consumer group will reprocess hundreds of millions of historical events, overwhelming downstream databases.
- `none`: Throws an exception if no offset is committed for the group.

### 10. How do you configure transactions in Spring Kafka (transactional.id prefix, KafkaTransactionManager, read_committed)?
- Configure `ProducerFactory` with `setTransactionIdPrefix("tx-service-")`.
- Provide a `KafkaTransactionManager` bean.
- Consumers must set `isolation.level=read_committed` so they only read records from committed transactions and skip aborted/rolled-back records.

### 11. How do you configure observability (Micrometer, tracing across async hops)?
- **Micrometer Tracing / OpenTelemetry:** Spring Kafka automatically binds with Micrometer Observation Registry.
- Producers inject W3C `traceparent` headers into Kafka record headers.
- Consumers extract `traceparent` from record headers and resume the distributed trace span across the async boundary.
- **Key Metrics:** `kafka.consumer.fetch.latency`, `records-lag-max`, `record-error-rate`.

---

## 4. Fallback and Resilience

### 1. Explain DefaultErrorHandler, back-off policies, and DeadLetterPublishingRecoverer.
- `DefaultErrorHandler`: Replaces legacy error handlers in Spring Kafka. Handles retrying failed record processing and delegating to a recoverer.
- **Back-Off Policies:** `FixedBackOff(interval, maxAttempts)` or `ExponentialBackOffWithMaxRetries` controls delay and retry attempts.
- `DeadLetterPublishingRecoverer`: When retries are exhausted, publishes the failed record to a dead-letter topic (default: `<topic>.DLT`) with error diagnostic headers (`exception-message`, `stacktrace`, `original-offset`).

### 2. How do you classify exceptions as retryable vs non-retryable?
- **Retryable (Transient):** Network timeouts, DB connection pool acquisition failure, optimistic locking exceptions, HTTP 503 from downstream service.
- **Non-Retryable (Permanent / Fatal):** `DeserializationException`, payload validation failure (`MethodArgumentNotValidException`), business rule violations (e.g., negative balance, unknown user ID).
- In Spring Kafka: configure `errorHandler.addNotRetryableExceptions(ValidationException.class)`.

### 3. Blocking retries vs non-blocking retries (@RetryableTopic): trade-offs for ordering, throughput, and topic count?
- **Blocking Retries (`DefaultErrorHandler`):**
  - Pauses the consumer thread and retries the record in place.
  - *Trade-off:* Preserves strict ordering for partition keys; blocks throughput for the entire partition (Head-of-Line blocking).
- **Non-Blocking Retries (`@RetryableTopic`):**
  - Failed message is published to a sequence of delayed retry topics (`orders-retry-1000`, `orders-retry-5000`) and the main partition moves forward.
  - *Trade-off:* Maintains high throughput; **breaks ordering** (subsequent records process before the retrying record); increases topic count.

### 4. What do you do with messages in a DLT? Who owns them, and how do you replay them?
- **Ownership:** The domain team owning the consumer owns the DLT and is responsible for triaging failures.
- **Replay Strategies:**
  1. Inspect failure headers (`kafka_dlt-exception-message`, `kafka_dlt-original-offset`).
  2. Once the bug or downstream service is fixed, run an administrative replay consumer that reads from `.DLT` and re-publishes records back to the main topic or executes business logic directly.

### 5. Kafka is unavailable or slow when your service publishes. What happens to the user’s request?
- **Risk:** Synchronous `kafkaTemplate.send().get()` will block user HTTP threads until `max.block.ms` (60s default) expires, exhausting Tomcat threads and crashing the API Gateway.
- **Mitigation:**
  - Fallback to saving the event into a local relational database `event_outbox` table.
  - Return `202 Accepted` to the client asynchronously.
  - Apply Circuit Breaker (Resilience4j) to fail fast when Kafka publish latency spikes.

### 6. A downstream dependency (DB, REST API) is down while consumers are running. How do you avoid retry exhaustion and mass DLT-ing?
- **The Disaster:** If downstream API is down for 30 minutes, consumers exhaust retries in seconds and send thousands of valid messages to the DLT.
- **Solution:** Monitor downstream health or catch consecutive retry failures. Call `MessageListenerContainer.pause()` to stop polling from Kafka without leaving the consumer group. Resume the container (`resume()`) when downstream health checks succeed.

### 7. How do you use circuit breakers or bulkheads inside a Kafka consumer?
- Wrap downstream external calls in a Resilience4j `CircuitBreaker`.
- When failure rate exceeds threshold, circuit opens and throws `CallNotPermittedException`.
- Catch this exception in the listener, avoid acknowledging the offset, and trigger container pause.

### 8. How do you pause and resume a listener container safely?
- Obtain container from `KafkaListenerEndpointRegistry`:
  ```java
  MessageListenerContainer container = registry.getListenerContainer("orderListenerId");
  container.pause();
  ```
- `pause()` stops the container from fetching records on subsequent `poll()` cycles while keeping the heartbeat thread alive, avoiding consumer group rebalances.

### 9. How do you recover from a bad deployment that processed events incorrectly (offset reset, replay, compensation)?
- **Offset Reset:** Stop consumer pods. Use `kafka-consumer-groups.sh --reset-offsets --to-datetime <time> --execute` to rewind offsets. Redeploy fixed code and resume (requires idempotent consumers).
- **Side-by-Side Consumer Group:** Spin up a new consumer group pointing to offset 0 streaming into a new database table, then switch read traffic.
- **Compensation Events:** Publish corrective reversing events (`OrderCorrectionEvent`) to amend corrupt state downstream.

### 10. How do you handle a poison pill message that blocks a partition?
- Ensure `ErrorHandlingDeserializer` is configured to route unparseable messages to the DLT.
- If a message crashes business logic continuously in production: manually advance the consumer group offset past the bad record via `kafka-consumer-groups.sh --shift-by 1 --execute`.

### 11. How do retries interact with idempotency?
- Retries inherently produce duplicate deliveries to consumers.
- Idempotent producers prevent producer-side duplicate writes to the broker.
- Consumer-side business idempotency (`processed_events` table or unique key constraints) is mandatory so retried messages do not cause duplicate side effects (e.g., duplicate charges).

---

## 5. Performance

### 1. How do you tune producers and consumers for throughput vs latency?
- **For High Throughput:**
  - Producer: `linger.ms=20..50`, `batch.size=65536..131072`, `compression.type=zstd`.
  - Consumer: `fetch.min.bytes=65536`, `fetch.max.wait.ms=500`, batch listeners.
- **For Ultra-Low Latency:**
  - Producer: `linger.ms=0`, `compression.type=none` or `lz4`.
  - Consumer: `fetch.min.bytes=1`, `fetch.max.wait.ms=50`.

### 2. Consumer lag is growing. How do you diagnose and fix it?
1. Check if lag is on all partitions or a single hot partition (`kafka-consumer-groups.sh --describe`).
2. Identify consumer bottleneck: CPU, GC pauses, slow database queries, or external REST API latency.
3. Scale consumer pods up to the partition count.
4. If already at 1:1 pod-to-partition ratio, switch to batch processing, optimize DB write queries (bulk inserts), or use in-memory key-based worker pools.

### 3. How do you scale consumption beyond the partition count while preserving per-key ordering?
- **In-Memory Key Hash Router:**
  - Single consumer thread fetches batch of records from the assigned partition.
  - Routes records into an internal array of in-memory queues based on key hash: `abs(key.hashCode()) % workerCount`.
  - Dedicated worker threads (or Java 21 Virtual Threads) process individual queues in parallel.
  - All records for the same key are processed sequentially by the same worker queue; independent keys process concurrently.

### 4. When do you use batch listeners, and how do you handle partial failures in a batch?
- Use batch listeners for high-throughput write workloads (e.g., bulk SQL inserts `saveAll()` or bulk Elasticsearch index operations).
- **Partial Failure Handling:**
  - Iterate through the batch. If record at index $i$ fails, throw `BatchListenerFailedException("Failed", ex, i)`.
  - Spring Kafka commits offsets up to index $i-1$, retries record $i$, or routes record $i$ to DLT, and resumes processing remaining records.

### 5. How do you detect and fix hot partitions and key skew?
- **Detection:** Monitor broker per-partition byte rate (`BytesInPerSec`). If partition 2 receives 10x more traffic than others, there is key skew.
- **Fix:** Key salting. If `tenantId="AMAZON"` is too hot, append a salt suffix: `tenantId + "_" + random(0..3)` to spread writes across 4 partitions.

### 6. Why is Kafka fast (sequential I/O, page cache, zero-copy, batching)?
- **Sequential Disk I/O:** Appending to log files sequentially avoids random disk seek overhead, matching raw memory write speeds.
- **OS Page Cache:** Relies on Linux page cache instead of large JVM heaps, avoiding JVM GC overhead.
- **Zero-Copy (`sendfile()`):** Data transfers directly from OS Page Cache to NIC buffer via DMA, bypassing JVM user space.
- **End-to-End Batching:** Compresses and sends records in batches across producer, broker, and consumer without re-compression.

### 7. JSON vs Avro vs Protobuf: impact on size, CPU, and compatibility?
- **JSON:** Human readable, but largest payload size (repeats field names as strings), highest CPU serialization cost, no native schema validation.
- **Avro:** Compact binary format (no field names in wire payload), schema enforced via Schema Registry, 60–80% smaller than JSON, fast serialization.
- **Protobuf:** Ultra-compact binary format, compile-time code generation, very low CPU overhead, field-tag-based compatibility.

### 8. How do you size a Kafka cluster and a Spring Boot consumer fleet for a target throughput?
- **Broker Throughput:** Calculate total data in (Producer throughput) + Replication overhead ($P \times (\text{RF}-1)$) + Consumer data out ($P \times \text{ConsumerGroups}$). Verify against broker 10Gbps/25Gbps NIC capacity.
- **Storage:** $\text{Daily Storage} = \text{Data Rate (MB/s)} \times 86400 \times \text{RF} \times \text{Retention Days}$.
- **Consumer Fleet:** $\text{Pods} = \frac{\text{Target Ingestion Rate}}{\text{Single Consumer Processing Rate}}$. Topic partitions must be $\ge \text{Pods}$.

### 9. Can you use virtual threads with Kafka consumers? What are the caveats?
- In Spring Boot 3.2+: set `spring.threads.virtual.enabled=true`.
- **Benefits:** Ideal for I/O-bound listeners calling external APIs or databases; allows high concurrency with small memory footprint.
- **Caveats:**
  - Avoid thread pinning: code inside `synchronized` blocks that performs blocking I/O will pin OS carrier threads. Use `ReentrantLock` instead.
  - Downstream connection pools: Spawning thousands of concurrent virtual threads can exhaust database connection pools (HikariCP).

### 10. Which metrics and alerts do you set up for Kafka producers, consumers, and brokers?
- **Broker:** `UnderReplicatedPartitions` ($> 0$), `OfflinePartitionsCount` ($> 0$), `ActiveControllerCount` ($\ne 1$), Disk usage $> 80\%$.
- **Consumer:** `records-lag-max` (spiking), `poll-idle-ratio-avg` ($< 0.1$), time between polls approaching `max.poll.interval.ms`.
- **Producer:** `record-error-rate` ($> 0$), `record-retry-rate`, `bufferpool-wait-time-ns`.

### 11. How do you avoid your idempotency store (Redis or Mongo) becoming the bottleneck?
- Use in-memory local cache (Caffeine) in front of Redis with short TTL to absorb hot duplicate traffic.
- Use Redis pipelining or `MGET` when processing batches instead of 100 individual network round-trips.
- Keep short TTLs (24–48 hours) on idempotency keys to keep Redis memory footprint compact.

---

## 6. Architectural Scenario Questions

### 1. Your monolith calls five services via REST in a chain. Migrate it to event-driven with zero downtime. How do you sequence it?
1. Implement Transactional Outbox inside the monolith to publish domain events to Kafka without changing existing REST endpoints.
2. Deploy the five microservices in "Shadow Mode" to consume Kafka events, compute results, and compare against monolith REST responses.
3. Switch downstream services one by one using feature flags to react directly to events instead of receiving synchronous REST calls.
4. Decommission the monolith orchestrator chain once all services operate autonomously via event choreography.

### 2. Order, Payment, Inventory, and Notification services must stay consistent without distributed transactions. Design the flow and failure handling.
- **Forward Flow:** `OrderCreated` $\rightarrow$ Inventory reserves stock $\rightarrow$ `StockReserved` $\rightarrow$ Payment charges card $\rightarrow$ `PaymentAuthorized` $\rightarrow$ Order confirmed $\rightarrow$ Notification sent.
- **Failure Handling (Saga Compensation):**
  - If payment fails: Payment service publishes `PaymentFailed`.
  - Order service listens and publishes `CompensateInventoryCommand`.
  - Inventory service releases reserved stock.
  - Order marked as `CANCELLED`.

### 3. Two services need the same data. One team wants shared DB access, another wants events. How do you decide?
- **Reject Shared DB:** Violates microservice bounded context isolation, creates schema coupling, database connection pool contention, and security risks.
- **Adopt Event-Carried State Transfer:** Service A emits canonical domain events. Service B consumes events and maintains its own private, optimized read projection.

### 4. A payment event was processed twice in production. Walk through how it happened and how you prevent it.
- **How it Happened:** Consumer processed payment successfully. Before committing offset to Kafka, consumer crashed or exceeded `max.poll.interval.ms`. Coordinator rebalanced partition to another consumer, which re-read the message and charged the customer again.
- **Prevention:**
  1. Pass a unique `Idempotency-Key` (e.g., `orderId`) to the payment gateway.
  2. Maintain a database unique constraint on `payment_transactions(order_id)` inside the service.

### 5. Events from two different topics (order created, payment completed) arrive out of order. How should the consumer cope?
- Implement a state buffer in the consumer's database.
- If `PaymentCompleted` arrives first: insert into a `pending_payments` table.
- When `OrderCreated` arrives: check `pending_payments`. If payment already exists, link it immediately and mark order as `PAID`.

### 6. Another team wants to change the schema of an event your five services consume. How do you handle it?
1. Enforce `FULL_TRANSITIVE` compatibility in Schema Registry CI/CD pipelines.
2. Ensure changes are purely additive (new optional fields with defaults).
3. If breaking changes are unavoidable: deploy a new topic version (`orders.v2`) and dual-publish until all five consumer teams migrate.

### 7. You must reprocess six months of historical events into a new read model. How do you do it without affecting live traffic?
1. Spin up a new instance of the projection service using a **brand-new Consumer Group ID**.
2. Configure `auto.offset.reset=earliest`.
3. Tune consumer for high throughput (`max.poll.records=1000`, bulk DB upserts).
4. Stream historical data into a separate read-model table/collection.
5. Once consumer lag reaches zero, switch application read pointers to the new table and tear down the temporary consumer group.

### 8. A new compliance rule requires deleting a user’s data (GDPR) while events are retained in Kafka. What are your options?
- **Crypto-Shredding (Best Practice):** Encrypt user PII with a unique per-user encryption key stored in a KMS. When a user requests deletion, delete their key from KMS. Historical events in Kafka become permanently undecryptable cipher text.
- **Log Compaction with Tombstones:** For compacted topics, publish a record with `key=userId` and `value=null`. Kafka log cleaner permanently purges historical records for that key.

### 9. Your team is split on Kafka vs Azure Service Bus for a new workload. How do you run the decision?
- Choose **Azure Service Bus** if you need: Individual message TTLs, message scheduling (send in 2 hours), dead-letter queues per subscription, message sessions, and low-to-medium throughput (<5k msgs/sec).
- Choose **Kafka** if you need: High throughput (>20k msgs/sec), long-term retention and event replayability, stream processing, or multi-cloud portability.

### 10. A consumer team keeps losing messages in production and blames Kafka. How do you investigate?
1. Check producer `acks` setting (was it `acks=0` or `acks=1` instead of `acks=all`?).
2. Check consumer `enable.auto.commit` (if `true`, offsets were committed before processing completed).
3. Check consumer error handler (were exceptions swallowed in a generic `catch (Exception e)` without logging or DLT routing?).
4. Check topic retention (`retention.ms`) to verify if messages were pruned before consumption.

### 11. How do you migrate from a self-managed Kafka cluster to a managed offering with no data loss?
1. Deploy MirrorMaker 2 to replicate all topics from self-managed cluster to Managed Kafka.
2. Migrate consumer applications to consume from the managed cluster (MM2 translates offsets).
3. Switch producer applications to write to the managed cluster.
4. Verify consumer lag is zero, then decommission the self-managed cluster.

---

## 7. System Design Scenario Questions

### 1. Design a real-time chat platform (gateway, services, Kafka, Redis, MongoDB) handling millions of concurrent users. Cover topics, partitioning keys, ordering, and delivery guarantees.
- **Ingress:** WebSocket Gateway pods maintain open client connections.
- **Kafka Topics:** `chat.room-messages` partitioned by `roomId` to guarantee strict chronological message order per room.
- **Worker Consumers:** Read messages from Kafka, persist full chat history into MongoDB (sharded by `roomId`), and publish to Redis Pub/Sub.
- **Egress:** Redis Pub/Sub distributes messages to the appropriate WebSocket gateway pods holding active recipient connections.
- **Delivery Guarantee:** At-least-once with client-side message deduplication via client-generated UUID.

### 2. Design a notification system (email, SMS, push) with retries, prioritisation, rate limiting, and deduplication.
- **Topics:** Split by priority: `notifications.priority.high` (OTPs, password resets) and `notifications.priority.low` (marketing).
- **Deduplication:** Check Redis `SET key val NX EX 300` using `userId + notificationType + messageHash`.
- **Rate Limiting:** Consumers enforce provider token-bucket rate limits using Redis before dispatching calls to SendGrid / Twilio.
- **Retries:** Use `@RetryableTopic` with exponential backoff routing to Dead Letter Topics for permanent provider failures.

### 3. Design an order-processing system for flash sales with 100x traffic spikes. How do Kafka and your consumers absorb the load?
- **Ingress Buffer:** API Gateway writes orders directly into a high-partition Kafka topic (`flash-orders`, e.g., 48 partitions) and returns `202 Accepted` immediately.
- **Traffic Leveling:** Kafka buffers incoming spikes; consumer fleet processes orders at a controlled rate without crashing relational databases.
- **Inventory Check:** Redis Atomic decrement (`DECRBY`) executes upfront to reject sold-out orders before hitting PostgreSQL.

### 4. Design a payments ledger with Kafka as the backbone. How do you guarantee no lost or duplicate transactions?
- **Outbox:** Core payment service writes to its DB and outbox table inside a single ACID transaction.
- **Kafka Partitioning:** Keyed by `accountId` to ensure sequential balance calculations per account.
- **Consumer Idempotency:** Ledger consumer writes double-entry journal records using unique transaction IDs (`PRIMARY KEY (transaction_id, entry_type)`), guaranteeing zero duplicate postings.

### 5. Design an audit and activity-tracking pipeline for all microservices, with search and long-term retention.
- **Ingestion:** Microservices publish CloudEvents audit messages to `enterprise.audit.log`.
- **Search Index (Hot):** Kafka Connect Elasticsearch/OpenSearch Sink indexes messages for 30-day interactive search.
- **Long-term Storage (Cold):** Kafka Connect S3/Azure Blob Sink exports data in Parquet format partitioned by `year/month/day` for 7-year regulatory compliance queries via AWS Athena / Azure Synapse.

### 6. Design a real-time analytics dashboard (clicks, orders per minute) using Kafka Streams or a similar approach.
- Ingest click and order events into Kafka.
- Kafka Streams application groups events by category and applies tumbling time windows:
  ```java
  stream.groupByKey()
        .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
        .count(Materialized.as("aggregated-counts"));
  ```
- Store window aggregates in RocksDB state stores and publish aggregate deltas to WebSocket servers for live dashboard updates.

### 7. Design a CDC pipeline from MongoDB to downstream services and a search index.
- Enable MongoDB replica set oplog / Change Streams.
- Deploy Debezium MongoDB Connector on Kafka Connect to stream oplog changes into topic `mongodb.inventory.products`.
- Downstream microservices consume CDC events to update local caches; Kafka Connect Elasticsearch Sink updates search index in near real-time.

### 8. Design an AI-request platform where requests are queued, processed by LLM workers, and results are streamed back. How do you handle slow workers, retries, and backpressure?
- **Ingress:** User prompts published to `ai.requests`.
- **LLM Consumers:** Worker thread polls single request, pauses listener container (`container.pause()`) while running long inference (15–30s) to prevent `max.poll.interval.ms` timeout, and resumes upon completion.
- **Streaming Egress:** Worker streams token chunks into `ai.completions.<requestId>` topic; SSE/WebSocket gateway reads chunks and streams to client browser.

### 9. Design a multi-region, highly available event platform with RPO and RTO targets.
- Deploy Active-Passive Kafka clusters across East US and West US.
- MirrorMaker 2 asynchronously replicates topics, consumer offsets, and schema definitions.
- Set RPO $\le 5$ seconds; RTO $\le 60$ seconds.
- Clients configure automated failover bootstrap servers or rely on global traffic manager DNS routing to Secondary upon Primary health failure.

### 10. At 2 AM, consumer lag reaches 5 million after a release. What are your first 30 minutes, and what would have prevented it?
- **First 30 Minutes:**
  1. *Min 0–5:* Check Grafana: Is lag uniform across all partitions or concentrated on one? Verify pod crash loops, memory/GC pauses, and DB connection pool saturation.
  2. *Min 5–10:* Containment: Roll back the bad release immediately (`kubectl rollout undo`). If volume spike, scale consumer pods up to the partition count.
  3. *Min 10–20:* Inspect thread dumps: Are consumer threads hung waiting for external database locks or timed-out REST calls?
  4. *Min 20–30:* Verify lag begins draining; monitor database CPU and DLT topics.
- **Prevention:** Performance regression testing in staging, canary deployments (Argo Rollouts), and circuit breakers to pause containers on downstream latency spikes.
