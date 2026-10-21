# Service: KPI-Analytics-Service (Real-Time Consumer & Velocity Analytics Engine)
Status: implemented   Owner: @Elena, @Leo (Reviewer: @Alex)   Updated: 2026-10-15
Inputs: context/architecture.md, context/implementation.md

## Summary
Low-Level Design (LLD) for `KPI-Analytics-Service`—a real-time event analytics engine built with **Spring Kafka (`@KafkaListener`)** + **Redis** + **MongoDB**. Aligned with 15-Factor microservice standards for 100% stateless pod scaling. Implements sliding-window fraud velocity detection (5-minute sliding window via Redis `INCR` + TTL), high-velocity fraud alert publishing to `fraud.alerts.v1`, atomic daily transaction volume aggregations into MongoDB (`daily_metrics`), and edge HMAC verification for internal REST APIs.

---

## Decisions

- **SVC-KPI-001**: **Traditional `@KafkaListener` + Redis Stateless Architecture**
  - *Why*: Eliminates local disk dependencies (RocksDB) and complex partition rebalances. Makes Kubernetes pods 100% stateless with instant restart and scale-out capabilities.
  - *Alternative Considered*: Kafka Streams DSL (RocksDB embedded stores)—functional, but creates local disk overhead and state store recovery latency during pod restarts.

- **SVC-KPI-002**: **Sliding-Window Fraud Velocity Tracking via Redis (`INCR` + 5m TTL)**
  - *Why*: Provides sub-millisecond, atomic, distributed sliding-window tracking on `velocity:{accountId}`. Emits high-priority alert to `fraud.alerts.v1` immediately when velocity count $\ge 3$.
  - *Alternative Considered*: In-memory sliding windows (lost on pod restart) or heavy SQL window queries.

- **SVC-KPI-003**: **Real-Time REST Velocity Queries via Redis**
  - *Why*: REST Controller queries Redis directly via `GET /api/v1/kpi/realtime/velocity/{accountId}` for O(1) response times without database query overhead.
  - *Alternative Considered*: Querying database write logs.

- **SVC-KPI-004**: **Atomic MongoDB Daily Rollups (`MongoTemplate.upsert`)**
  - *Why*: Daily aggregations use MongoDB atomic `$inc` operators on `{currency, windowStart}`. Thread-safe, non-blocking, and durable for historical analytics.

- **SVC-KPI-005**: **Edge Internal HMAC Security**
  - *Why*: All REST endpoints protected by `InternalHmacFilter` verifying `X-Internal-HMAC` with 300s clock drift tolerance.

- **SVC-KPI-006**: **Externalized Configuration Properties (`KpiKafkaProperties`)**
  - *Why*: Kafka topics (`transactions`, `fraud-alerts`), consumer group ID, velocity window duration, and alert thresholds are externalized into pure, immutable, JSR-380 validated `@ConfigurationProperties` records and wired directly into `@KafkaListener(topics = "${app.kafka.topics.transactions}", groupId = "${app.kafka.consumer.group-id}")`. Zero hardcoded string literals.

---

## Core Components

### 1. Consumer Engine (`TransactionKpiConsumer.java`)
Consumes from `banking.transactions.v1`:
- Increments `velocity:{accountId}` in Redis with 5-minute TTL.
- Publishes to `fraud.alerts.v1` if transfers in window $\ge 3$.
- Executes atomic `$inc` upsert in MongoDB collection `daily_metrics`.

### 2. REST Controller (`KpiInteractiveQueryController.java`)
- `GET /api/v1/kpi/realtime/velocity/{accountId}`: Returns realtime velocity count, alert status, and timestamp.
- `GET /api/v1/kpi/historical/daily?currency=USD`: Returns top 30 daily rollup records from MongoDB.

### 3. Persistence (`DailyMetricDocument.java` & `DailyMetricRepository.java`)
- Compound index on `{'currency': 1, 'windowStart': -1}`.
- Tracks `totalVolume`, `transactionCount`, `typeCounts` map, `updatedAt`, and `@Version`.
