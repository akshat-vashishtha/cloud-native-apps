# Service: KPI-Analytics-Service (Real-Time Consumer & Velocity Analytics Engine)
Status: implemented   Owner: @Elena, @Leo (Reviewer: @Alex)   Updated: 2026-10-22
Inputs: context/architecture.md, context/implementation.md

## Summary
Stateless analytics engine: Spring Kafka + Redis + MongoDB. Consumes **`banking.transactions.v1` only** (does **not** yet consume `chat.events.v1`). Redis velocity window, Mongo `daily_metrics` rollups, produces `fraud.alerts.v1` (no consumer). REST APIs are **JWT resource servers** (`kpi:read_analytics`). HMAC filter **retired**.

### Flow (live)
```
Business-Ops outbox → banking.transactions.v1
        → TransactionKpiConsumer
             → Redis INCR velocity:{accountId} (+TTL)
             → if count ≥ threshold → fraud.alerts.v1
             → Mongo upsert daily_metrics (Criteria + MongoTemplate — good pattern)
```

### Best-practice gaps (see platform-best-practices.md §1)
- Add **consumer idempotency** (`eventId`) before INCR/upsert.
- Add **retry + DLT**; do not swallow listener exceptions after side effects.
- Extend Criteria/`Aggregation` for richer historical KPI queries (PBP-004).

---

## Decisions

- **SVC-KPI-001**: **`@KafkaListener` + Redis Stateless Architecture**
  - *Why*: No RocksDB; pods stay ephemeral.
  - *Alternative*: Kafka Streams — deferred.

- **SVC-KPI-002**: **Sliding-Window Velocity via Redis (`INCR` + TTL)**
  - *Why*: Sub-ms distributed window on `velocity:{accountId}`; emit `fraud.alerts.v1` when ≥ threshold (config).
  - *Alternative*: In-memory windows — rejected.

- **SVC-KPI-003**: **REST Velocity Queries via Redis**
  - *Why*: `GET /api/v1/kpi/realtime/velocity/{accountId}` O(1).

- **SVC-KPI-004**: **Atomic MongoDB Daily Rollups (`MongoTemplate.upsert`)**
  - *Why*: `$inc` on `{currency, windowStart}`.

- **SVC-KPI-005**: **OAuth2 JWT Resource Server**
  - *Why*: Align with ARCH-004; replace HMAC. Authority `kpi:read_analytics` for analytics APIs.
  - *Rejected*: Internal HMAC as primary trust.

- **SVC-KPI-006**: **Externalized `KpiKafkaProperties`**
  - *Why*: Topics, group id, window, threshold via `@ConfigurationProperties`.

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
