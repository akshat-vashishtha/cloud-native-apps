# 06. Low-Level Design (LLD), Machine Coding & Concurrent Systems

> **Target Band:** 8–12 Years Experience (Lead Engineer / Staff Engineer / Technical Architect)  
> **Total Scenarios:** 34 (22 `[P0 - Real-World Scenarios & Outage Triage]` + 12 `[P1 - Architectural Trade-offs & Deep Design]`)  
> **Focus:** Thread-Safe In-Memory Cache with TTL & Single-Flight Stampede Protection, Rate Limiter Engine, Delay Task Scheduler, Circuit Breaker State Machine, Connection Pool, Stock Order Matching Engine, Parking/Seat Concurrency, and Clean SOLID/DDD Patterns.

---

## 🎯 Section 1: [P0 - Real-World Scenarios & Outage Triage] (Q1–Q22)

### Q1. LLD: Thread-Safe In-Memory LRU Cache with TTL & Single-Flight Cache-Stampede Protection
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** Tier-1 High-Scale (Uber / Amazon / Flipkart / Confluent Machine Coding)
- **The Real-World Scenario:** In a machine coding round, you build an LRU Cache using `Collections.synchronizedMap(new LinkedHashMap<>(cap, 0.75f, true))`. The interviewer points out 3 production flaws under 64 concurrent threads:
  1. Every `get(key)` mutates the doubly-linked list order, serializing 100% of read threads on a single global mutex!
  2. Expired keys that are never accessed again sit in memory until LRU eviction (`Passive-Only Expiry Leak`).
  3. When a hot key expires, 50 concurrent threads simultaneously miss the cache and call `loader.apply(key)` 50 times (**Cache Stampede**)!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Strict LRU requires mutating head/tail pointers on every read (`get`), turning a read-only operation into a globally locked write operation. And returning `null` on expiry without coalescing concurrent loads causes 50 duplicate DB calls.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Store `CompletableFuture<CacheEntry<V>>` in a `ConcurrentHashMap<K, CompletableFuture<CacheEntry<V>>>` and use `compute()` to coalesce concurrent misses for the same key into a **Single-Flight Load**.
  - **3. Permanent Architectural / Code Fix:** Design a **Segmented / Ring-Buffered Concurrent LRU + TTL Cache**:
    1. **Storage & Single-Flight Stampede Protection:**
       - Map: `ConcurrentHashMap<K, CompletableFuture<Node<K, V>>>`.
       - On `get(key, loader)`: If missing or expired, use atomic `map.compute(key, ...)` to insert an incomplete `CompletableFuture<Node<K,V>>` **before** running the slow `loader` outside the lock, so 49 concurrent callers simply call `.join()` on the exact same in-flight `CompletableFuture` (**1 DB call instead of 50!**). Remember: `if (future.isCompletedExceptionally())` remove it from the map so errors aren't cached forever!
    2. **Lock-Striped / Buffered Doubly Linked List (or $O(1)$ Min-Heap / Time-Wheel for Expiry):**
       - Split the cache into $S = 16$ independent hash segments (`Segment[key.hashCode() & 15]`), each with its own `ReentrantLock` + Doubly-Linked List (reducing lock contention by 16x), OR record read hits in a lock-free ring buffer (Caffeine pattern) and drain them in batches!
  - **4. Trade-offs to Defend in Front of Interviewer:** `synchronized LinkedHashMap` (5 lines of code, global mutex on every `get`, zero stampede protection) vs **Segmented `ConcurrentHashMap<K, CompletableFuture<Node>>` + Lazy Expiry on Read + Periodic Min-Heap/Wheel Active Sweeper** (16x concurrency, single-flight load coalescing, bounded memory).
  - **5. Senior Trap to Avoid:** Running `loader.apply(key)` **inside** `ConcurrentHashMap.compute(key, (k, v) -> loader.apply(k))` when the loader takes 50ms and you might have hash bucket collisions! Instead, have `compute()` create and return an empty `CompletableFuture` in **100 nanoseconds**, release the hash bucket lock immediately, and complete the `CompletableFuture` outside `compute()`!

---

### Q2. LLD: Pluggable Thread-Safe Rate Limiter Engine (Token Bucket vs Sliding Window Log vs Sliding Window Counter)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** Tier-1 High-Scale (Stripe / Atlassian / Razorpay / Uber)
- **The Real-World Scenario:** Design an extensible in-memory Rate Limiter (`boolean tryAcquire(String clientId, int permits)`) supporting 100,000 clients. A candidate implements a `ConcurrentHashMap<String, Deque<Long>>` storing timestamps of every request (`Sliding Window Log`) with `synchronized (deque)`. The interviewer asks: *"What happens when Client A has a limit of 50,000 requests/minute, and what happens to memory when 5 million transient guest IPs hit the server once and never return?"*
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. **Memory Explosion ($O(\text{Limit})$ per client):** Storing a `Long` object (`24 bytes` + Deque node `24 bytes` = `48 bytes`) for 50,000 requests/min consumes `2.4MB` for just *one* client!
    2. **Unbounded Map Key Leak:** `ConcurrentHashMap<String, Bucket>` without idle eviction leaks memory forever as millions of unique IPs pass through!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Replace the timestamp `Deque<Long>` with a **Lock-Free CAS Token Bucket** (`AtomicReference<BucketState>` or `long` bit-packed state) which uses **constant $O(1)$ memory (32 bytes per client)** regardless of whether the limit is 10/min or 10,000,000/min!
  - **3. Permanent Architectural / Code Fix:**
    1. **Strategy Pattern Interface (`RateLimiterStrategy`):**
       - `TokenBucketStrategy`: Stores `record State(double availableTokens, long lastRefillNanos)` inside an `AtomicReference<State>` updated via a lock-free `updateAndGet()` CAS loop:
         ```java
         long now = nanoClock.nanoTime();
         double elapsedSec = (now - prev.lastRefillNanos()) / 1e9;
         double newTokens = Math.min(capacity, prev.availableTokens() + elapsedSec * refillRatePerSec);
         if (newTokens < permits) return prev; // rejected
         return new State(newTokens - permits, now);
         ```
       - `SlidingWindowCounterStrategy`: Stores only 2 integers `(prevWindowCount, currWindowCount, currWindowStartSec)`—approximates sliding window with 99.9% accuracy in $O(1)$ space!
    2. **Inject `Clock` / `LongSupplier nanoClock` Constructor Dependency:** Never hardcode `System.nanoTime()` directly—inject a `Clock` interface so unit tests can advance time deterministically in `0ms` without `Thread.sleep()`!
    3. **Idle Client Eviction:** Hold per-client buckets in a bounded `Caffeine` map (`expireAfterAccess(10, MINUTES)`).
  - **4. Trade-offs to Defend in Front of Interviewer:** Sliding Window Log `Deque<Long>` (100% exact timestamp tracking, terrible $O(N)$ memory) vs Fixed Window Counter (allows 2x burst at window boundary `00:59.9` + `01:00.1`) vs **Lock-Free CAS Token Bucket / Sliding Window Counter** ($O(1)$ 32-byte memory, lock-free thread safety).
  - **5. Senior Trap to Avoid:** Using `Thread.sleep(1000)` in unit tests during a machine coding interview! Injecting a `FakeClock` (`clock.advance(Duration.ofSeconds(1))`) immediately signals Staff-level testability to the interviewer.

---

### Q3. LLD: In-Memory Delayed Job Scheduler (`ScheduledExecutorService` Clone with PriorityQueue, Cancellation & Graceful Shutdown)
- **Experience Focus:** Staff Engineer / Lead Engineer
- **Target Company Archetype:** Tier-1 High-Scale (LinkedIn / Rubrik / Confluent / Uber)
- **The Real-World Scenario:** Design a custom `TaskScheduler` supporting `schedule(Runnable, delayMs)`, `scheduleAtFixedRate(...)`, and `cancel(taskId)`. A candidate writes a background thread with `while (true) { Task t = queue.peek(); if (t != null && t.runAt <= now) pool.submit(queue.poll()); else Thread.sleep(10); }`. The interviewer asks: *"Why is `Thread.sleep(10)` a polling anti-pattern, and what happens when a new task with a 5ms delay is scheduled while the dispatcher thread is sleeping for 1 hour waiting for a 1-hour task?"*
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. Busy-polling every 10ms burns CPU unnecessarily when idle and adds up to 10ms jitter.
    2. If the dispatcher thread calls `Thread.sleep(t.runAt - now)` (say 1 hour) and a new task with a 5ms delay is inserted at the head of the min-heap (`PriorityQueue`), the dispatcher won't wake up for an hour unless woken via a **`Condition.awaitNanos()` signal**!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Guard the `PriorityQueue<ScheduledTask>` (ordered by `executeAtNanos ASC, sequenceNumber ASC`) with a `ReentrantLock` + `Condition newLeaderAvailable = lock.newCondition()`.
  - **3. Permanent Architectural / Code Fix:** Implement the **Leader-Wait `Condition.awaitNanos()` Pattern (How JDK `DelayQueue` works internally)**:
    1. **When Scheduling (`schedule(task)`):**
       ```java
       lock.lock();
       try {
           queue.offer(task);
           if (queue.peek() == task) {
               newLeaderAvailable.signal(); // New earliest task! Wake dispatcher immediately!
           }
       } finally { lock.unlock(); }
       ```
    2. **Dispatcher Loop (Zero Busy-Polling):**
       ```java
       lock.lock();
       try {
           while (running) {
               ScheduledTask first = queue.peek();
               if (first == null) {
                   newLeaderAvailable.await(); // Sleep indefinitely until first task arrives
               } else {
                   long delayNanos = first.executeAtNanos() - nanoClock.nanoTime();
                   if (delayNanos <= 0L) {
                       workerPool.execute(queue.poll()); // Ready! Dispatch to worker pool!
                   } else {
                       newLeaderAvailable.awaitNanos(delayNanos); // Sleep exact nanos OR wake early if earlier task arrives!
                   }
               }
           }
       } finally { lock.unlock(); }
       ```
    3. **O(1) / Lazy Cancellation:** Store `AtomicBoolean cancelled` on `ScheduledTask` (plus an optional index map for removal) so `cancel(taskId)` marks it `CANCELLED` in $O(1)$ without scanning the heap, or removes it cleanly.
  - **4. Trade-offs to Defend in Front of Interviewer:** Min-Heap `PriorityQueue` ($O(\log N)$ insert/poll, ideal up to ~100k tasks) vs **Hashed Hierarchical Timing Wheel (Netty / Kafka Purgatory)** ($O(1)$ insert and $O(1)$ cancellation even at 10,000,000 concurrent timers!).
  - **5. Senior Trap to Avoid:** Executing `task.run()` **directly on the single Dispatcher Thread** instead of handing ready tasks off to a `workerPool`! If one task takes 5 seconds on the Dispatcher Thread, every other timer scheduled during those 5 seconds is delayed by 5 seconds!

---

### Q4. LLD: Thread-Safe Circuit Breaker State Machine From Scratch (`CLOSED`, `OPEN`, `HALF_OPEN`)
- **Experience Focus:** Staff Engineer / Lead Engineer
- **Target Company Archetype:** FinTech (Razorpay / Stripe / PhonePe) | Tier-1 High-Scale
- **The Real-World Scenario:** Implement a production-grade Circuit Breaker (`<T> T execute(Callable<T> action)`) from scratch without external libraries. Most candidates use a `synchronized execute()` block (which serializes every network call through one lock!) or allow 1,000 threads to flood through when `waitDurationInOpenState` expires (`OPEN -> HALF_OPEN` stampede)!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. Holding a `synchronized` lock around `action.call()` blocks all concurrent requests while waiting for network I/O!
    2. When `now >= openTimestamp + waitDuration`, if 1,000 threads see the timer expired, all 1,000 threads enter `HALF_OPEN` simultaneously unless guarded by an atomic CAS state transition + bounded probe permit counter!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Never synchronize around `action.call()`; use `AtomicReference<State>` for lock-free state transitions (`CLOSED -> OPEN -> HALF_OPEN`) and an `AtomicInteger halfOpenPermits` to allow strictly $K$ probe calls in `HALF_OPEN`.
  - **3. Permanent Architectural / Code Fix:**
    1. **Immutable State Object inside `AtomicReference<CircuitState>`:**
       - `ClosedState(SlidingWindowRingBuffer metrics)`
       - `OpenState(long openedAtNanos)`
       - `HalfOpenState(AtomicInteger remainingProbePermits, AtomicInteger successes, AtomicInteger failures)`
    2. **Atomic `OPEN -> HALF_OPEN` Transition:**
       When a thread sees `OpenState` and `now - openedAtNanos >= waitDurationNanos`, it executes:
       ```java
       HalfOpenState next = new HalfOpenState(maxProbeCalls);
       if (stateRef.compareAndSet(currentOpenState, next)) {
           // Won transition! Consume 1 permit and run probe
       }
       ```
       In `HalfOpenState`, only threads where `remainingProbePermits.decrementAndGet() >= 0` are allowed to execute `action.call()`; all other concurrent threads fail fast with `CircuitBreakerOpenException`!
       - If any probe fails: `stateRef.compareAndSet(halfOpen, new OpenState(now))` (trips back to `OPEN` immediately!).
       - If all $K$ probes succeed: `stateRef.compareAndSet(halfOpen, new ClosedState())`!
  - **4. Trade-offs to Defend in Front of Interviewer:** Count-based Circular Array (`AtomicLongArray` of size 100) vs Time-based Buckets (`60 x 1-second buckets` using `LongAdder` per second bucket—zero contention under 100k RPS!).
  - **5. Senior Trap to Avoid:** Resetting the same mutable counters in-place during state transitions instead of replacing an **immutable `CircuitState` instance inside `AtomicReference<CircuitState>`**—mutable shared counters across `OPEN`/`HALF_OPEN`/`CLOSED` transitions cause subtle race conditions where a late failure from the previous `CLOSED` epoch trips the newly recovered `HALF_OPEN` state!

---

### Q5. LLD: Stock Exchange Limit Order Book & Matching Engine (Price-Time Priority FIFO at Sub-Millisecond Latency)
- **Experience Focus:** Staff Engineer / Principal Architect
- **Target Company Archetype:** FinTech / Trading (Morgan Stanley / Goldman Sachs / Zerodha / Groww / Coinbase)
- **The Real-World Scenario:** Design an in-memory Order Matching Engine for a stock ticker (`submitOrder`, `cancelOrder`, `modifyOrder`) enforcing strict **Price-Time Priority**:
  - Best Buy (Bid) = Highest Price first; ties broken by earliest arrival timestamp.
  - Best Sell (Ask) = Lowest Price first; ties broken by earliest arrival timestamp.
  A candidate uses a `PriorityQueue<Order>` for Bids and Asks. The interviewer asks: *"In real trading, 90% of orders are cancelled before matching (`cancelOrder(orderId)`). What is the time complexity of `priorityQueue.remove(order)` on 100,000 resting orders, and why did you use `double` for price?"*
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. `PriorityQueue.remove(Object)` is **$O(N)$ linear scan**! At 50,000 cancellations/sec, $O(N)$ scans stall the matching engine completely.
    2. Using IEEE-754 `double` for prices (`0.1 + 0.2 = 0.30000000000000004`) corrupts price equality comparisons!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Represent prices as **`long priceInTicks` (or paisa/cents)**, group orders into `PriceLevel` doubly-linked queues stored in a `TreeMap<Long, PriceLevel>`, and maintain a `HashMap<Long, OrderNode> orderIndex` for **$O(1)$ cancellation**!
  - **3. Permanent Architectural / Code Fix:** Use the **Classic LMAX / NASDAQ Limit Order Book Data Structure**:
    1. **Three Coordinated Data Structures (per Single-Writer Symbol Actor):**
       - `TreeMap<Long, PriceLevel> bids = new TreeMap<>(Comparator.reverseOrder())` (Highest Bid first: $O(1)$ `firstEntry()`, $O(\log P)$ where $P$ = number of distinct price levels, typically $<500$, NOT number of orders!).
       - `TreeMap<Long, PriceLevel> asks = new TreeMap<>()` (Lowest Ask first).
       - `Long2ObjectHashMap<OrderNode> orderById` (Maps `orderId -> OrderNode` pointer inside its `PriceLevel` doubly-linked list).
    2. **Time Complexity Achievable:**
       - **Best Bid / Best Ask lookup:** $O(1)$
       - **Match against resting orders at top level:** $O(1)$ per fill (dequeue head of `PriceLevel` doubly-linked list)
       - **Cancel Order by `orderId`:** **$O(1)$** (lookup `OrderNode` in `orderById`, unlink `prev <-> next` pointers in its `PriceLevel` in $O(1)$; only if `PriceLevel` becomes empty do we remove the level from `TreeMap`!).
  - **4. Trade-offs to Defend in Front of Interviewer:** Multi-threaded locks inside an Order Book (`ReentrantLock` per price level: slow cache-line bouncing and lock overhead) vs **Single-Writer Partitioned Actor per Stock Symbol (`AAPL` on Thread 1, `NVDA` on Thread 2)** (zero locks, 100% L1/L2 CPU cache locality, processes 500,000+ orders/sec per thread!).
  - **5. Senior Trap to Avoid:** Modifying an order's **price** or **increasing its quantity** in-place without moving it to the tail of the `PriceLevel` queue! By exchange rules, **reducing** quantity preserves time priority, whereas **increasing** quantity or changing price loses time priority (moves to tail of queue)!

---

### Q6. LLD: Concurrent Seat / Movie Ticket Booking System (BookMyShow — Atomic Multi-Seat Selection Without Deadlocks)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** Tier-1 Product (BookMyShow / Uber / Amazon Machine Coding)
- **The Real-World Scenario:** User A requests to lock seats `[A1, A2, A3]` for `show_101`, while at the exact same millisecond User B requests to lock seats `[A3, A2]`.
  1. If User A locks `A1`, `A2` and fails on `A3`, partial seats must NOT remain locked (`All-or-Nothing`).
  2. If User A locks `A2 -> A3` while User B locks `A3 -> A2`, the two threads **deadlock**!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Acquiring fine-grained per-seat locks in caller-supplied list order violates canonical lock ordering (causing deadlocks), and mutating seat states before acquiring all locks breaks atomicity.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Sort the requested `seatIds` in canonical ascending order (`seatIds.stream().sorted().distinct().toList()`) **before** acquiring locks!
  - **3. Permanent Architectural / Code Fix:**
    1. **Sort Locks Canonically + `tryLock` Rollback:**
       ```java
       List<String> sortedSeatIds = requestedSeats.stream().distinct().sorted().toList();
       List<ReentrantLock> acquiredLocks = new ArrayList<>();
       try {
           for (String seatId : sortedSeatIds) {
               ReentrantLock lock = show.getSeatLock(seatId);
               lock.lock();
               acquiredLocks.add(lock);
           }
           // Step 1: Validate ALL seats are AVAILABLE (or expired HOLD)
           for (String seatId : sortedSeatIds) {
               if (!show.isSeatAvailable(seatId, now)) {
                   throw new SeatUnavailableException(seatId); // Zero state mutated!
               }
           }
           // Step 2: Transition ALL seats atomically to HELD(bookingId, now + 8 mins)
           return show.createHold(userId, sortedSeatIds, now.plus(Duration.ofMinutes(8)));
       } finally {
           for (int i = acquiredLocks.size() - 1; i >= 0; i--) {
               acquiredLocks.get(i).unlock();
           }
       }
       ```
    2. **Lazy Expiry Check Inside `isSeatAvailable(seatId, now)`:** A seat is available if `status == AVAILABLE || (status == HELD && holdExpiresAt.isBefore(now))`—so even if a background cleanup thread hasn't run yet, an expired hold is **immediately bookable** at the exact millisecond its TTL expires!
  - **4. Trade-offs to Defend in Front of Interviewer:** Locking the entire `Show` object (`synchronized (show)`: simple, blocks 500 users booking different rows) vs **Per-Seat Lock Acquired in Sorted Order + Lazy TTL Evaluation** (users booking `[A1, A2]` and `[F5, F6]` run in 100% parallel with zero contention and zero deadlock risk!).
  - **5. Senior Trap to Avoid:** Relying solely on a background `ScheduledExecutorService` running every 10 seconds to flip expired `HELD` seats back to `AVAILABLE`—if a hold expires at `12:08:00.000` and User C tries to book that seat at `12:08:01.000`, checking `status == AVAILABLE || (status == HELD && now > expiresAt)` inline makes expiry instantaneous without waiting for the background sweeper!

---

### Q7. LLD: Custom Database Connection Pool (`HikariCP` Lite — Borrowing, Timeout, Validation & Leak Detection)
- **Experience Focus:** Staff Engineer / Lead Engineer
- **Target Company Archetype:** Tier-1 High-Scale | Infrastructure LLD
- **The Real-World Scenario:** Design a thread-safe `ConnectionPool` (`Connection getConnection(long timeoutMs)`, `void releaseConnection(Connection)`). Candidates often hand out the raw `java.sql.Connection` object (so when a caller uses `try (Connection c = pool.getConnection())`, `c.close()` closes the physical TCP socket instead of returning it to the pool!) or allow a caller to call `releaseConnection(c)` **twice** on the same connection, putting duplicate references into the idle queue!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. Returning the raw physical `Connection` means `try-with-resources` permanently destroys the pooled socket.
    2. Without an atomic per-wrapper `closed` guard (`AtomicBoolean`), calling `.close()` twice inserts the same physical connection into `BlockingQueue<PooledConnection>` twice, handing the **exact same connection to two different threads simultaneously**!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Wrap each physical connection in a `PooledConnectionProxy` (or Dynamic Proxy) whose `close()` uses `if (closed.compareAndSet(false, true))` to return the connection to the pool exactly once!
  - **3. Permanent Architectural / Code Fix:**
    1. **Double-Close Guard & Post-Close Method Trap:**
       - `PooledConnectionHandle` holds `AtomicBoolean borrowed = new AtomicBoolean(true)`.
       - Any call to `prepareStatement()` checks `if (!borrowed.get()) throw new SQLException("Connection already closed/returned to pool!")` (prevents a thread from continuing to use a connection after calling `.close()`!).
       - `close()` runs `if (borrowed.compareAndSet(true, false)) pool.recycle(physicalConn)`.
    2. **Clean State Reset on Recycle:** Before putting a recycled connection back into the `LinkedBlockingDeque<PhysicalConnection>` (using **LIFO `offerFirst` / `pollFirst`** so the warmest, most recently used connection is reused first while idle tail connections can expire!), reset dirty state (`if (!conn.getAutoCommit()) conn.rollback(); conn.setAutoCommit(true); conn.clearWarnings()`).
    3. **Connection Leak Detection Timer:** When `getConnection()` succeeds, capture `new Exception("Borrowed here")` (if leak detection is enabled) and schedule a timer for `leakDetectionThresholdMs`. If `close()` hasn't cancelled the timer before it fires, log the exact stack trace of the thread that leaked the connection!
  - **4. Trade-offs to Defend in Front of Interviewer:** FIFO Idle Queue (`offerLast`/`pollFirst`: cycles through all 20 connections evenly, keeping all 20 sockets warm even when only 2 are needed) vs **LIFO Idle Deque (`offerFirst`/`pollFirst` — HikariCP style)** (reuses the 2 hottest connections in CPU cache while letting the other 18 sit idle and shrink down to `minimumIdle`!).
  - **5. Senior Trap to Avoid:** Not rolling back uncommitted transactions (`if (!autoCommit) physicalConn.rollback()`) when a connection is returned to the pool—if Thread A threw an exception mid-transaction without committing and returned the connection to the pool, Thread B borrowing that connection inherits Thread A's uncommitted dirty transaction!

---

### Q8. LLD: Payment Orchestrator & Multi-Gateway Routing Engine (Eliminating a 600-Line `if-else` God Class)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech (Razorpay / PhonePe / Juspay / PayU)
- **The Real-World Scenario:** You inherit a 600-line `PaymentService.pay()` method full of nested `if (method.equals("UPI") && bank.equals("HDFC")) ... else if (method.equals("CARD")) ...` that directly instantiates SDK clients, calculates fees, retries, and updates the DB. Every time a new payment method (`BNPL`, `UPI_LITE`, `RU_PAY_CREDIT`) or gateway (`Razorpay`, `Cashfree`, `Stripe`) is added, developers modify this same class and break existing flows!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Classic violation of **Open-Closed Principle (OCP)**, **Single Responsibility Principle (SRP)**, and **Dependency Inversion (DIP)**. Payment Method validation, Gateway selection/routing, Fee calculation, and Provider SDK adaptation are 4 orthogonal axes of change tangled into one procedural method.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Extract a `PaymentGatewayAdapter` port interface and register implementations in a Spring map/registry keyed by `GatewayType`.
  - **3. Permanent Architectural / Code Fix:** Decompose into **4 Clean Composable Patterns**:
    1. **Strategy + Registry Pattern (`PaymentMethodValidator` & `PaymentGatewayAdapter`):**
       - `Map<PaymentMethodType, PaymentMethodHandler>` and `Map<GatewayId, PaymentGatewayAdapter>` injected via Spring constructor (zero `if-else` switches!).
    2. **Chain of Responsibility / Specification Pattern for Dynamic Gateway Routing (`RoutingRule`):**
       - `List<GatewayFilterRule>` filters eligible gateways (`CurrencySupportRule`, `CircuitBreakerHealthyRule`, `SuccessRateThresholdRule`) $\rightarrow$ `GatewayScoringStrategy` ranks remaining candidates (`LowestFeeStrategy` vs `HighestSuccessRateStrategy`).
    3. **Template Method Pattern for the Execution Lifecycle (`AbstractPaymentFlow`):**
       - Enforces the invariant skeleton: `1. Validate & Claim Idempotency -> 2. Reserve Ledger -> 3. Select Gateway -> 4. Invoke Adapter (outside DB txn!) -> 5. Persist Transition & Outbox Event`.
  - **4. Trade-offs to Defend in Front of Interviewer:** Giant `switch`/`if-else` block (fast to write on Day 1, high merge-conflict and regression blast radius with 10 engineers) vs **Plugin Registry + Routing Specification Pipeline** (adding a 7th gateway requires adding 1 new `@Component` class with **zero edits** to existing orchestrator code!).
  - **5. Senior Trap to Avoid:** Leaking third-party provider DTOs (`com.stripe.model.PaymentIntent` or `RazorpayOrder`) into your domain/application layer! Always translate provider responses into a canonical domain value object (`GatewayChargeResult`) inside the infrastructure adapter (**Anti-Corruption Layer**).

---

### Q9. LLD: Splitwise / Expense Sharing Debt Simplification Engine (Minimizing Total Transactions & Handling Rounding Remainder)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech / Consumer Product (Splitwise / Cred / Google Pay / Uber)
- **The Real-World Scenario:** Design an Expense Sharing & Settlement system (`Equal`, `Exact`, `Percentage`, `Shares` splits). Two production bugs hit naive implementations:
  1. **The ₹100 / 3 = ₹33.33 Penny Loss Bug:** Splitting ₹100.00 equally among 3 users using `BigDecimal` or `double` assigns ₹33.33 to each user ($\text{Total} = ₹99.99$), losing ₹0.01 on every bill and breaking the $\sum \text{Debits} == \sum \text{Credits}$ invariant!
  2. **Debt Simplification Cycle:** Alice owes Bob ₹100, Bob owes Charlie ₹100, and Charlie owes Alice ₹100. Naive balances require 3 payments instead of 0!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. Financial division of integer minor units (`10000 paisa / 3`) has a remainder (`10000 % 3 = 1 paisa`). Ignoring the remainder violates conservation of money.
    2. Simplifying a debt graph requires computing **Net Balance per User** ($\text{Net}(u) = \text{TotalOwedTo}(u) - \text{TotalOwedBy}(u)$) where $\sum_{u} \text{Net}(u) \equiv 0$.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Store all money in `long minorUnits` (`paisa`/`cents`) and allocate the remainder `R = totalPaisa % N` (`1 paisa` each) to the first `R` participants deterministically!
  - **3. Permanent Architectural / Code Fix:**
    1. **Exact Remainder Allocation Algorithm (`long paisa`):**
       ```java
       long baseShare = totalPaisa / n;
       long remainder = totalPaisa % n; // e.g., 10000 % 3 = 1
       // First 'remainder' users get (baseShare + 1) = 3334 paisa; rest get 3333 paisa.
       // Sum = 3334 + 3333 + 3333 = 10000 paisa (100% exact conservation of money!)
       ```
    2. **Debt Simplification Algorithm:**
       - Compute `netBalance[u]` for every member in the group. Discard anyone with `netBalance == 0`.
       - Put Creditors (`netBalance > 0`) in a Max-Heap and Debtors (`netBalance < 0`) in a Max-Heap (by absolute debt).
       - **Greedy Heap Match ($O(V \log V)$):** Repeatedly match the largest Debtor with the largest Creditor (`transfer = min(|debt|, credit)`), reducing at least one person's balance to `0` on every step ($\le V - 1$ total transfers!).
       - **Exact Minimum Transfers (NP-Hard Subset-Sum DFS with Bitmask DP for small groups $V \le 15$):** Partition zero-sum subsets to find the true mathematical minimum number of transactions!
  - **4. Trade-offs to Defend in Front of Interviewer:** Greedy Two-Heap Matching ($O(V \log V)$, guarantees at most $V - 1$ transactions, works for groups of 1,000 people in `<1ms`) vs Exact Zero-Sum Subset Bitmask DP ($O(3^V)$, finds 1 fewer transaction on disjoint sub-cycles, practical only for $V \le 15$).
  - **5. Senior Trap to Avoid:** Storing a pairwise mutable matrix `owed[userA][userB]` as the only source of truth instead of an **append-only list of `Expense` and `Settlement` ledger entries** plus derived net balances per group!

---

### Q10. LLD: In-Memory Pub-Sub Message Broker (Topic Partitioning, Consumer Groups, Independent Offsets & Backpressure)
- **Experience Focus:** Staff Engineer / Lead Engineer
- **Target Company Archetype:** Tier-1 High-Scale (Confluent / Rubrik / Uber / Cohesity Machine Coding)
- **The Real-World Scenario:** Design a thread-safe in-memory Kafka-like Pub-Sub Broker supporting multiple `Topics`, `Partitions`, and `ConsumerGroups` where:
  - Consumers in *different* `ConsumerGroups` each receive a full copy of all messages independently.
  - Consumers in the *same* `ConsumerGroup` split partitions among themselves without duplicate processing.
  - Slow Consumer Group B does NOT block Fast Consumer Group A!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Candidates often use a single `BlockingQueue<Message>` per topic where `queue.poll()` destructively removes the message—making it impossible for a second Consumer Group to read the same message or replay from offset 0!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Model each `Partition` as an **append-only indexed log (`CopyOnWriteArrayList` / segmented array of immutable `Message` records)** where reading never deletes messages—each `ConsumerGroup` simply tracks its own `AtomicLong offset` per partition!
  - **3. Permanent Architectural / Code Fix:**
    1. **`PartitionLog` (Append-Only Indexed Log with Condition Notification):**
       - Holds `List<Message> log` guarded by a `ReentrantReadWriteLock` (or lock-free segmented pages) + `ReentrantLock appendLock` with `Condition newMessagesAvailable`.
       - `append(Message)`: Assigns monotonic `offset = log.size()`, adds to list, and calls `newMessagesAvailable.signalAll()`.
       - `read(long offset)`: Reads `log.get(offset)` in $O(1)$ without mutating the log!
    2. **`ConsumerGroup` State (`ConcurrentHashMap<Integer, AtomicLong> partitionOffsets`):**
       - Each `ConsumerGroup` maintains its own `offset` cursor per partition (`0, 1, 2...`).
       - Each `PartitionWorker` thread for `(ConsumerGroup, Partition)` sleeps on `partition.awaitNextMessage(currentOffset)` when caught up (`currentOffset == log.size()`), waking immediately when a producer appends!
  - **4. Trade-offs to Defend in Front of Interviewer:** Destructive `BlockingQueue` per subscriber (duplicates memory $M \times$ for $M$ consumer groups, impossible to replay historical offsets) vs **Shared Append-Only Partition Log + Per-Group Offset Cursor** (1 single copy of messages in memory shared across 20 consumer groups, supports instant `resetOffset(0)` replay!).
  - **5. Senior Trap to Avoid:** Using `Object.notify()` (instead of `notifyAll()` / `Condition.signalAll()`) when a new message is appended to a Partition that has 5 different Consumer Groups waiting on it! `notify()` wakes only 1 thread, leaving the other 4 Consumer Groups sleeping!

---

### Q11. LLD: Multi-Level Cache Management System (L1, L2, L3 Read-Through / Write-Through with Cost & Latency Metrics)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** Tier-1 High-Scale (Flipkart / Amazon / Swiggy Machine Coding)
- **The Real-World Scenario:** Design an $N$-level cache hierarchy (`L1 -> L2 -> ... -> LN`) where each level has its own capacity, eviction policy (`LRU` / `LFU`), and read/write latency. When a key is missed in `L1` and `L2` but found in `L3`, it must automatically backfill `L2` and `L1` (**Read-Repair / Read-Through Promotion**) and return the total latency incurred across levels.
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Hardcoding `if (l1.get() == null) { if (l2.get() == null) ... }` violates OCP (cannot dynamically configure 2, 3, or 4 cache tiers) and couples cache topology with eviction algorithms.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Model the hierarchy using the **Chain of Responsibility Pattern (`CacheLevel` holding `Optional<CacheLevel> nextLevel`)** + a pluggable **`EvictionPolicy<K>` Strategy (`LruEvictionPolicy`, `LfuEvictionPolicy`)**.
  - **3. Permanent Architectural / Code Fix:**
    1. **Clean Separation of `Storage<K, V>` and `EvictionPolicy<K>`:**
       - `CacheTier<K, V>` composes `Storage<K, V>` + `EvictionPolicy<K>` + `TierMetrics`.
       - `LfuEvictionPolicy<K>` uses **Frequency Buckets (`HashMap<K, Node>` + `HashMap<Integer, DoublyLinkedList<Node>>` + `int minFreq`)** for strict **$O(1)$ `get` and $O(1)$ `evict`** (never use a `PriorityQueue` for LFU, which is $O(N)$ to update frequency!).
    2. **Recursive Chain Read with Automatic Upper-Tier Backfill:**
       ```java
       public ReadResult<V> get(K key) {
           V val = currentTier.get(key); // adds currentTier.readLatency
           if (val != null) return new ReadResult<>(val, currentTier.readLatency());
           if (nextTier == null) return new ReadResult<>(null, currentTier.readLatency());
           ReadResult<V> downstream = nextTier.get(key);
           long totalLatency = currentTier.readLatency() + downstream.latency();
           if (downstream.value() != null) {
               currentTier.put(key, downstream.value()); // Backfill current tier!
               totalLatency += currentTier.writeLatency();
           }
           return new ReadResult<>(downstream.value(), totalLatency);
       }
       ```
  - **4. Trade-offs to Defend in Front of Interviewer:** Write-Through across all tiers synchronously (100% consistent across L1..LN, pays sum of all write latencies) vs Write-Back / Async Backfill on Read Promotion (returns value to caller immediately and backfills L1/L2 asynchronously on a thread pool).
  - **5. Senior Trap to Avoid:** Implementing LFU eviction with a `TreeSet` or `PriorityQueue` where incrementing a key's frequency on `get()` takes $O(\log N)$ or $O(N)$—always know the **$O(1)$ Frequency-Bucket Doubly-Linked-List LFU** structure (`minFreq` pointer + `freqToNodesMap`)!

---

### Q12. LLD: Elevator Dispatch System (Replacing Naive FCFS "Thrashing" with SCAN / LOOK Directional Scheduling)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** Tier-1 Product / Enterprise LLD
- **The Real-World Scenario:** In a 50-floor building with 4 elevators, a naive First-Come-First-Served (FCFS) dispatcher moves Elevator 1 from Floor 2 $\rightarrow$ Floor 48 $\rightarrow$ Floor 3 $\rightarrow$ Floor 49 while passing right by 10 people waiting on intermediate floors in the same direction (**Elevator Thrashing**), starving passengers for 20 minutes!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** FCFS ignores spatial locality and direction of travel. Like disk head scheduling, an elevator moving `UP` from Floor 2 to Floor 48 should serve all intermediate `UP` stops (`Floors 10, 22, 35`) in ascending order before reversing direction!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Give each `ElevatorCar` two sorted stop sets: `TreeSet<Integer> upStops` (ascending) and `TreeSet<Integer> downStops` (descending `Comparator.reverseOrder()`) implementing the **LOOK Algorithm**.
  - **3. Permanent Architectural / Code Fix:**
    1. **LOOK Algorithm per `ElevatorCar` (Better than SCAN because it reverses at the highest requested floor instead of going all the way to Floor 50 when nobody requested Floor 50!):**
       - When moving `UP`: poll the smallest floor $\ge \text{currentFloor}$ from `upStops`. When no more `upStops` exist above `currentFloor`, switch direction to `DOWN` (serving `downStops`) or `IDLE`.
    2. **Pluggable `ElevatorDispatchStrategy` (Cost-Function Scoring):**
       - When a hall button `(floor = 15, direction = UP)` is pressed, the `Dispatcher` scores all 4 elevators using `calculateETA(car, 15, UP)`:
         - **Priority 1:** Idle car on Floor 15 (`cost = 0`).
         - **Priority 2:** Car currently below Floor 15 already moving `UP` (`cost = 15 - car.currentFloor`).
         - **Priority 3:** Nearest `IDLE` car.
         - **Priority 4:** Car moving away (cost includes distance to its furthest reversal floor + distance back to Floor 15).
  - **4. Trade-offs to Defend in Front of Interviewer:** SCAN Algorithm (goes all the way to Floor 50 and Floor 1 before reversing—wastes movement on empty top/bottom floors) vs **LOOK Algorithm (`TreeSet` upStops + `TreeSet` downStops)** (reverses immediately after serving the highest/lowest active request).
  - **5. Senior Trap to Avoid:** Merging hall `UP` and hall `DOWN` requests into a single `Set<Integer> stops`! If Passenger A at Floor 15 pressed the **`DOWN`** hall button and Elevator 1 is currently going **`UP`** from Floor 1 to Floor 40, Elevator 1 must **NOT** stop at Floor 15 on its way up!

---

### Q13. LLD: Vending Machine / Order Lifecycle State Pattern (Preventing Illegal Concurrent State Transitions)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** Tier-1 Product | FinTech Machine Coding
- **The Real-World Scenario:** Design a Vending Machine (or ATM / Order State Machine) with states `IDLE`, `HAS_MONEY`, `ITEM_SELECTED`, `DISPENSING`, `OUT_OF_STOCK`. Two bugs occur in candidate code:
  1. Using `if (state == DISPENSING)` across 8 methods makes adding a new state (`REFUNDING`) require editing every method.
  2. Two threads call `cancelAndRefund()` and `dispenseItem()` at the exact same millisecond—causing the machine to **both dispense the item AND refund the cash**!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. Scattering state conditionals across action methods violates the **GoF State Pattern**.
    2. Performing state checks and state mutations without an atomic transition guard allows a concurrent race between `cancel()` and `dispense()`.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Encapsulate each state as a stateless singleton implementing `VendingState`, and guard state transitions on the `VendingMachineContext` with a `ReentrantLock` (or atomic CAS state table).
  - **3. Permanent Architectural / Code Fix:**
    1. **State Pattern with Context Lock:**
       - `VendingMachineContext` holds the mutable data (`currentBalancePaisa`, `selectedSlot`, `inventory`) and `VendingState currentState`, and acquires `contextLock` around every public state transition (`insertCoin`, `selectItem`, `dispense`, `cancelRefund`).
       - Each `VendingState` implementation overrides only the operations valid in that state (defaulting all other methods on the interface to `throw new IllegalStateTransitionException("Cannot dispense while in " + name())`!).
    2. **Greedy + DP Change Dispenser (`CoinInventory`):**
       - Before transitioning to `DISPENSING`, verify **both** item stock $> 0$ AND that exact change (`insertedAmount - itemPrice`) can be formed from available coin denominations in `CoinInventory`! If exact change cannot be made, reject before deducting item stock!
  - **4. Trade-offs to Defend in Front of Interviewer:** Mutable fields inside `VendingState` objects (breaks thread safety if states are singletons) vs **Stateless Singleton `VendingState` Instances + Mutable `VendingMachineContext` guarded by `ReentrantLock`** (zero object allocation per transition, 100% thread-safe against concurrent refund/dispense races).
  - **5. Senior Trap to Avoid:** Deducting the item from `Inventory` *before* verifying that `CoinInventory` has the denominations needed to return change (`insertedAmount - price`)!

---

### Q14. LLD: Notification Dispatcher with Pluggable Channels, Per-Provider Rate Limiting, Failover & Deduplication
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech (Razorpay / PhonePe) | Tier-1 Product
- **The Real-World Scenario:** Design a Notification Engine supporting `SMS`, `EMAIL`, `PUSH`, and `WHATSAPP`, where each channel has multiple vendor providers (e.g., `SMS -> [Twilio, Kaleyra, SNS]`) with automatic failover if the primary vendor throws a retryable error or trips its circuit breaker, plus user preference opt-out checks and 5-minute duplicate suppression.
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Tight coupling between template rendering, user preference filtering, deduplication, and vendor SDK invocation makes the notification service brittle and untestable.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Separate the pipeline into **Interceptor Filter Chain (Dedup $\rightarrow$ Opt-Out Check $\rightarrow$ Rate Limit) $\rightarrow$ Channel Router $\rightarrow$ Provider Failover Chain**.
  - **3. Permanent Architectural / Code Fix:**
    1. **Idempotent Deduplication Guard:** Compute `dedupKey = notificationId != null ? notificationId : SHA256(userId + templateId + params + timeBucket5Min)` and check `dedupStore.putIfAbsent(dedupKey, IN_PROGRESS)` so retrying never sends duplicate SMS messages.
    2. **Provider Failover Selector (`ChannelProviderChain`):**
       ```java
       for (NotificationProvider provider : providersForChannel(channel)) {
           if (!provider.circuitBreaker().isCallPermitted() || !provider.rateLimiter().tryAcquire()) {
               continue; // Skip unhealthy or quota-exhausted vendor immediately!
           }
           try {
               return provider.send(renderedMessage);
           } catch (TransientProviderException ex) {
               provider.circuitBreaker().onError(ex);
               // Continue loop to fallback provider (e.g., Twilio -> Kaleyra)!
           }
       }
       throw new AllProvidersExhaustedException(channel);
       ```
  - **4. Trade-offs to Defend in Front of Interviewer:** Hardcoded primary/secondary `try-catch` blocks vs **Ordered Health-Aware Provider Strategy List** (automatically skips tripped circuit breakers or rate-limited vendors in $O(1)$ without waiting for timeouts!).
  - **5. Senior Trap to Avoid:** Enforcing user marketing opt-out preferences on **`CRITICAL_TRANSACTIONAL` messages (like 2FA Login OTPs or Fraud Alerts)**! Your `PreferenceFilter` must bypass opt-out checks when `message.priority() == Priority.OTP_SECURITY`.

---

### Q15. LLD: Parking Lot System with Multi-Gate Concurrency & Nearest-Spot Assignment (`TreeSet` per Vehicle/Spot Type)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** Tier-1 Product (Amazon / Uber / Flipkart LLD)
- **The Real-World Scenario:** A parking lot has 10 Entry Gates and 10,000 spots across 5 floors. When 10 cars enter simultaneously at all 10 Entry Gates, a naive implementation scans all 10,000 spots (`for (Spot s : allSpots) if (s.isFree()) ...`) and assigns the **exact same Spot #101 to two different cars**, OR takes $O(N)$ linear time on every entry!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. Scanning a flat list of 10,000 spots to find a free spot is $O(N)$ and requires locking the list.
    2. Checking `if (spot.isFree())` and then calling `spot.occupy()` without atomic removal from the available pool causes race conditions across entry gates.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Maintain pre-sorted available spot queues (`ConcurrentSkipListSet<ParkingSpot>` or `TreeSet<ParkingSpot>` guarded by per-SpotType lock) ordered by distance from entrance/floor!
  - **3. Permanent Architectural / Code Fix:**
    1. **Separate `availableSpotsByType` from `occupiedSpots` (`O(\log N)` Atomic Allocation):**
       - Maintain `Map<SpotType, NavigableSet<ParkingSpot>> availableSpots` (where `SpotType` = `TWO_WHEELER`, `COMPACT`, `LARGE`, `EV_CHARGER`).
       - Allocating a spot at any of the 10 Entry Gates is a single atomic **`availableSpots.get(type).pollFirst()`**! Because `pollFirst()` atomically removes and returns the lowest-ordered (nearest) spot in **$O(\log N)$**, two gates can **never** receive the same spot!
       - When a car exits at an Exit Gate, `availableSpots.get(type).add(spot)` returns the spot to the sorted available set in $O(\log N)$!
    2. **Composable Pricing Strategy (`PricingRule` Decorator / Strategy):**
       - `HourlyTieredPricingStrategy`, `WeekendSurgeDecorator`, `EvChargingAddonDecorator`.
  - **4. Trade-offs to Defend in Front of Interviewer:** Global `TreeSet` per `SpotType` (`pollFirst()` in $O(\log N)$, orders by `(floor, spotNumber)`) vs Per-Gate Distance-Ordered `TreeSet` (if Entry Gate 1 is on the North side and Entry Gate 2 is on the South side, each gate has its own distance comparator and claims the spot via `spot.tryOccupy()` CAS!).
  - **5. Senior Trap to Avoid:** Using `double` for parking fees and hardcoding `switch (vehicleType)` inside `Ticket.calculateFee()` instead of injecting a `FeeCalculationStrategy`!

---

### Q16. LLD: Logging Framework from Scratch (Async Ring-Buffer Appender, Log Levels, MDC Context & Non-Blocking Backpressure)
- **Experience Focus:** Staff Engineer / Lead Engineer
- **Target Company Archetype:** Tier-1 High-Scale
- **The Real-World Scenario:** Design a high-performance logging library (`Logger.info(...)` with `Console`, `RollingFile`, and `HttpSink` appenders). If 200 application threads write directly to a synchronized `FileOutputStream` on every `logger.info()`, disk I/O latency blocks business threads and drops throughput from 50,000 RPS to 2,000 RPS!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Synchronous disk `write()` + `flush()` inside a `synchronized` block serializes all application threads behind OS filesystem locks. Furthermore, formatting string arguments (`"User " + user + " bought " + item`) when `DEBUG` level is disabled wastes massive CPU and heap allocations!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Guard level check *before* string formatting (`if (!isEnabled(level)) return;`), capture thread-local MDC snapshot on the caller thread, and enqueue an immutable `LogEvent` into a bounded **Async `ArrayBlockingQueue` / RingBuffer** drained by a single background writer thread with batched `BufferedOutputStream` flushes!
  - **3. Permanent Architectural / Code Fix:**
    1. **Fast-Path Level Guard + Parameterized / `Supplier<String>` Lazy Evaluation:** Never format the message unless `level.severity() >= effectiveLevel.severity()`.
    2. **Caller-Thread Snapshot Before Async Handoff:** Before putting `LogEvent` into the async queue, capture `Instant.now()`, `Thread.currentThread().getName()`, and `MDC.getCopyOfContextMap()` on the **caller thread** (if you read `Thread.currentThread().getName()` or `MDC` inside the background writer thread, every log line will say `[thread=AsyncLogWriter-1]` with empty `traceId`!).
    3. **Tiered Overflow Backpressure Policy on Bounded Queue:** When the async queue is 95% full during a disk stall, **drop `TRACE`, `DEBUG`, and `INFO` logs immediately (Discard Policy)** so business threads never block, while preserving queue capacity for `WARN` and `ERROR` logs!
  - **4. Trade-offs to Defend in Front of Interviewer:** Blocking caller threads when log queue is full (guarantees 0 dropped `INFO` logs, causes a full production API outage if disk I/O stalls!) vs **Severity-Aware Drop Policy (Logback `discardingThreshold` pattern)** (drops `DEBUG`/`INFO` when queue $>80\%$ full while keeping `WARN`/`ERROR` and keeping the application 100% responsive!).
  - **5. Senior Trap to Avoid:** Forgetting to copy `Thread.currentThread().getName()` and `MDC` on the caller thread before enqueuing the `LogEvent` to the async appender thread!

---

### Q17. LLD: Thread-Safe Bounded Blocking Queue Using `ReentrantLock` and Two `Condition` Variables (And Why 1 Condition Causes Deadlock/Contention)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** Tier-1 High-Scale (Core Java Concurrency LLD)
- **The Real-World Scenario:** Implement a bounded `MyBlockingQueue<T>` from scratch (`put(T)`, `take()`, `offer(T, timeout)`). A candidate uses `synchronized` with a single `wait()` and `notify()`. Under high concurrency with 10 producers and 10 consumers on a queue of capacity 1, all 20 threads freeze in a **permanent livelock/deadlock**! Why did `notify()` deadlock the queue, and how do you fix it?
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** With a single monitor wait-set (`wait()` / `notify()`), **both waiting Producers (waiting for `notFull`) and waiting Consumers (waiting for `notEmpty`) sleep in the exact same wait queue**!
    When Producer 1 puts an item into the queue (making `count == 1`, FULL) and calls `notify()`, the JVM can wake up **Producer 2** instead of a Consumer! Producer 2 sees `count == 1` (still full!), goes right back to `wait()` without calling `notify()`, and now **every Producer and every Consumer is asleep forever (Lost Wakeup Deadlock)**!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Use `ReentrantLock` with **two separate `Condition` queues**: `Condition notFull = lock.newCondition()` and `Condition notEmpty = lock.newCondition()`.
  - **3. Permanent Architectural / Code Fix:**
    ```java
    public final class BoundedBlockingQueue<E> {
        private final Object[] items;
        private int takeIndex, putIndex, count;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition();
        private final Condition notFull = lock.newCondition();

        public BoundedBlockingQueue(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException();
            this.items = new Object[capacity];
        }

        public void put(E e) throws InterruptedException {
            Objects.requireNonNull(e);
            lock.lockInterruptibly();
            try {
                while (count == items.length) { // ALWAYS 'while', NEVER 'if'!
                    notFull.await();
                }
                items[putIndex] = e;
                if (++putIndex == items.length) putIndex = 0;
                count++;
                notEmpty.signal(); // Wakes ONLY a waiting Consumer (in 'notEmpty')!
            } finally {
                lock.unlock();
            }
        }

        @SuppressWarnings("unchecked")
        public E take() throws InterruptedException {
            lock.lockInterruptibly();
            try {
                while (count == 0) {
                    notEmpty.await();
                }
                E x = (E) items[takeIndex];
                items[takeIndex] = null; // Prevent memory leak of dequeued reference!
                if (++takeIndex == items.length) takeIndex = 0;
                count--;
                notFull.signal(); // Wakes ONLY a waiting Producer (in 'notFull')!
                return x;
            } finally {
                lock.unlock();
            }
        }
    }
    ```
  - **4. Trade-offs to Defend in Front of Interviewer:** Single Lock (`ArrayBlockingQueue` above: compact circular array, zero node allocation) vs Two-Lock Queue (`LinkedBlockingQueue` with separate `putLock` and `takeLock` + `AtomicInteger count`: allows a Producer and a Consumer to execute truly concurrently, at the cost of allocating a `Node` object per insert).
  - **5. Senior Trap to Avoid:** Two classic traps in one method:
    1. Using `if (count == 0) notEmpty.await()` instead of **`while (count == 0)`** (fails on OS **spurious wakeups** or if a barging thread steals the item before the woken thread re-acquires the lock!).
    2. Forgetting `items[takeIndex] = null;` on dequeue—leaving the reference inside the circular `Object[]` array causes a **silent memory leak** until the circular index wraps all the way around!

---

### Q18. LLD: E-Commerce Coupon & Promotion Stacking Engine (Specification + Decorator + Optimistic Budget Quota)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** E-Commerce / FinTech (Swiggy / Amazon / Flipkart / Cred)
- **The Real-World Scenario:** Design a Promotion Engine where coupons have complex eligibility rules (`MinCartValue >= ₹500`, `Category == ELECTRONICS`, `PaymentBank == HDFC`, `FirstTimeUser == true`, `MaxUsesPerUser <= 1`, `TotalCampaignBudget <= ₹10,00,000`), and certain promotions are **Stackable** (Store Discount + Bank Offer) while others are **Mutually Exclusive**.
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Putting eligibility rules inside coupon subclasses creates a combinatorial explosion (`FirstTimeUserHdfcElectronicsCoupon`), and calculating percentage discounts without a deterministic application order produces different totals (`10% off then ₹100 off` $\ne$ `₹100 off then 10% off`!).
  - **2. Immediate Incident Mitigation (First 15 minutes):** Use the **Composable Specification Pattern (`EligibilityRule<CartContext>`)** for eligibility (`ruleA.and(ruleB).and(ruleC)`) and enforce a **Canonical Priority Bucket Order** for stacking.
  - **3. Permanent Architectural / Code Fix:**
    1. **Composable `Specification<CheckoutContext>` Tree:**
       ```java
       @FunctionalInterface
       public interface EligibilitySpec {
           RuleResult evaluate(CheckoutContext ctx); // returns passed() or failureReason("Add ₹120 more to unlock!")
           default EligibilitySpec and(EligibilitySpec other) { ... }
       }
       ```
       Returning `RuleResult(boolean passed, String upsellHint)` instead of a bare `boolean` allows the UI to tell the user *"Add ₹120 more to save ₹200 with code DIWALI200!"*
    2. **Deterministic Stacking Pipeline by Exclusivity Group (`BucketOrder`):**
       - Step 1: `ITEM_LEVEL_DISCOUNT`
       - Step 2: `CART_COUPON` (Max 1 coupon from this bucket)
       - Step 3: `PAYMENT_INSTRUMENT_OFFER` (Calculated on the *post-coupon* net payable amount, capped at `maxDiscountPaisa`).
    3. **Atomic Campaign Budget Reservation:** Before confirming checkout, atomically decrement `remaining_budget_paisa >= discount_paisa` in Redis/DB so a viral ₹10 Lakh campaign never overspends ₹50 Lakhs!
  - **4. Trade-offs to Defend in Front of Interviewer:** Returning `boolean isEligible()` (useless for explaining *why* a coupon failed in the checkout UI) vs **Returning `EvaluationResult(passed, failureCode, userMessage)` from each Specification node**.
  - **5. Senior Trap to Avoid:** Allowing stacked discounts to reduce `finalPayableAmount` below `0` (negative cart total!) or calculating bank cashback on the *pre-coupon* gross subtotal instead of the *post-coupon* net subtotal!

---

### Q19. LLD: Ride-Sharing Surge Pricing & Driver Allocation Engine (Uber / Ola Machine Coding)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** Tier-1 Mobility / Delivery (Uber / Ola / Rapido / Swiggy)
- **The Real-World Scenario:** Design a Ride Matching & Fare Engine (`requestRide`, `acceptRide`, `completeRide`). Two real-world edge cases hit:
  1. The system offers `Ride_101` to the nearest driver (`Driver_A`). While `Driver_A` takes 15 seconds to tap "Accept", `Ride_102` is also offered to `Driver_A`! Both riders get matched to `Driver_A`!
  2. A rider requests a ride when surge is `1.2x` (₹240), takes 2 minutes to confirm while surge rises to `2.0x` (₹400), and is charged ₹400 without warning!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. Offering a ride to a driver without transitioning the driver from `AVAILABLE` to an exclusive time-bounded `OFFERED_LOCK` state allows concurrent dispatch threads to double-dispatch the same driver.
    2. Fare quotes must be bound to an immutable **Fare Quote Entity (`quoteId`, `expiresAt`)** so the rider pays the exact quoted surge multiplier they consented to!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Use `driver.tryTransitionState(AVAILABLE, OFFERED, 15_SECONDS)` via `AtomicReference<DriverState>` CAS before sending a dispatch offer to a driver.
  - **3. Permanent Architectural / Code Fix:**
    1. **`FareQuote` Immutable Value Object with TTL:**
       - `getEstimate(source, dest, vehicleType)` creates and caches `FareQuote(quoteId, baseFare, surgeMultiplier, totalFare, expiresAt = now + 180s)`.
       - `requestRide(quoteId)` validates `now <= quote.expiresAt()` and locks that exact `FareQuote` onto the `Trip`!
    2. **Atomic Driver Offer Lease State Machine:**
       - Find candidate drivers in nearby H3 hexagons where `driver.state() == AVAILABLE`.
       - Attempt atomic CAS: `driver.claimForOffer(tripId, expiresAt = now + 15s)`. Only the thread that wins the CAS sends the push offer to that driver. If the driver rejects or 15s elapses, the state reverts to `AVAILABLE` and the dispatcher moves to the next candidate driver!
  - **4. Trade-offs to Defend in Front of Interviewer:** Broadcast Offer to 10 drivers at once ("Fastest finger first": fast match, causes 9 drivers to get frustrating "Ride already taken" errors on tap) vs **Sequential 15-second Exclusive Offer Lease with Ranked Scoring** (predictable UX, zero race on accept).
  - **5. Senior Trap to Avoid:** Recomputing surge pricing at the *end* of the trip instead of locking the `surgeMultiplier` inside the `FareQuote` accepted by the rider at trip creation!

---

### Q20. LLD: Key-Value Store Supporting In-Memory Transactions (`BEGIN`, `GET`, `SET`, `DELETE`, `COMMIT`, `ROLLBACK`)
- **Experience Focus:** Staff Engineer / Lead Engineer
- **Target Company Archetype:** Tier-1 High-Scale (Confluent / CockroachDB / Databricks / Rubrik Coding Round)
- **The Real-World Scenario:** Design a thread-safe in-memory transactional Key-Value store supporting `begin()`, `get(k)`, `put(k, v)`, `delete(k)`, `commit()`, and `rollback()` (including nested transactions or concurrent multi-threaded transactions with Snapshot Isolation / Optimistic Concurrency Control).
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** If Transaction T1 mutates the global `ConcurrentHashMap` directly on `put(k, v)` and records an undo log for `rollback()`, then Concurrent Transaction T2 calling `get(k)` sees T1's **uncommitted dirty writes (`Dirty Read`)**!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Isolate uncommitted writes in a per-transaction **`WriteBuffer` (`Map<K, Optional<V>> localWriteSet`)** so `put()` and `delete()` never touch the global store until `commit()`!
  - **3. Permanent Architectural / Code Fix:** Implement **Multi-Version Optimistic Concurrency Control (MVCC / OCC Snapshot Isolation)**:
    1. **Global Store:** `ConcurrentHashMap<K, VersionedValue<V>> globalStore` (where `record VersionedValue<V>(V value, long version)` and `AtomicLong globalCommitEpoch`).
    2. **Transaction Context (`TransactionContext<K, V>`):**
       - Records `long readSnapshotVersion = globalCommitEpoch.get()`.
       - Holds `Map<K, Long> readSetVersions` (versions of keys read during the txn) and `Map<K, Optional<V>> writeSet` (`Optional.empty()` represents a `TOMBSTONE` deletion!).
       - **`get(k)` inside Txn:** First check `writeSet.get(k)` (so the transaction **reads its own uncommitted writes/deletes**!). If not in `writeSet`, read from `globalStore`, record `(k, entry.version)` in `readSetVersions`, and return `entry.value`.
       - **`rollback()`:** Simply clear `writeSet` in **$O(1)$**—zero global state was ever touched!
       - **`commit()`:** Acquire `commitLock` (or lock the sorted keys in `writeSet` + `readSet`), verify that for every key in `readSetVersions`/`writeSet`, `globalStore.get(k).version == recordedVersion` (if any key was modified by another committed txn since we read it, throw `SerializationConflictException` and abort!). Otherwise, apply all `writeSet` entries with `newVersion = globalCommitEpoch.incrementAndGet()`!
  - **4. Trade-offs to Defend in Front of Interviewer:** Undo Log mutating global map in place (suffers Dirty Reads and complex rollback on failure) vs **Per-Transaction `WriteSet` Buffer with `Optional.empty()` Tombstones + Version Validation at Commit** (`rollback()` is $O(1)$ no-op, zero dirty reads, true Snapshot Isolation!).
  - **5. Senior Trap to Avoid:** Using `null` inside `Map<K, V> writeSet` to mean "deleted"—because `writeSet.get(k) == null` cannot distinguish between *"Key `k` was not modified in this transaction (so fall through to globalStore)"* vs *"Key `k` was explicitly deleted in this transaction (so return `null` without reading globalStore)"*! Wrapping values in `Optional<V>` (or a dedicated `Tombstone` sentinel) solves this cleanly.

---

### Q21. LLD: Distributed / Local Circuit-Protected Feature Toggle & A/B Experiment Assignment Engine
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** Modern Product | FinTech
- **The Real-World Scenario:** Design an A/B Testing & Feature Flag SDK engine that assigns users deterministically to variants (`CONTROL: 50%`, `TREATMENT_A: 30%`, `TREATMENT_B: 20%`), supports targeting rules (`country IN [IN, SG] AND appVersion >= 4.2.0`), and supports **Mutual Exclusion Layers** (so a user in the "Checkout Redesign Experiment" is never simultaneously placed in the conflicting "One-Click Checkout Experiment").
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** If Experiment 1 and Experiment 2 both hash `hash(userId) % 100` independently without a **Layer Namespace**, a user can get Treatment in both conflicting checkout experiments simultaneously, corrupting experiment metrics!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Introduce the **Google Overlapping Experiment Infrastructure (`Layer` + `Bucket 0..9999`)** abstraction.
  - **3. Permanent Architectural / Code Fix:**
    1. **Layer-Based Bucketing (`0..9999` Basis Points):**
       - Mutually exclusive experiments share the same `Layer` (e.g., `layerId = "checkout-flow-layer"`), which owns `10,000` buckets (`0..9999`).
       - Experiment 1 is allocated buckets `[0..4999]` (50% of layer traffic); Experiment 2 is allocated buckets `[5000..9999]` (50% of layer traffic).
       - A user's layer bucket is `MurmurHash3(layerId + ":" + userId) % 10000`. It is mathematically impossible for any user to land in both Experiment 1 and Experiment 2!
    2. **Orthogonal Layers:** Non-conflicting experiments (e.g., `search-ranking-layer` vs `checkout-flow-layer`) use their own `layerId` as the hash salt, ensuring 100% statistical orthogonality across layers!
  - **4. Trade-offs to Defend in Front of Interviewer:** Global single traffic pool (can only run 5 non-overlapping 20% experiments across the entire company!) vs **Orthogonal Multi-Layer Bucketing (`layerId` salt for independent layers, shared `layerId` range for mutually exclusive experiments)** (allows running 1,000 concurrent experiments across the company while preventing conflicting UX collisions).
  - **5. Senior Trap to Avoid:** Changing variant weights (`50/50 -> 80/20`) by re-hashing without preserving bucket range monotonicity—always map variants to contiguous bucket ranges `[0..4999]` so expanding a treatment from 20% `[0..1999]` to 30% `[0..2999]` keeps 100% of existing treatment users in treatment!

---

### Q22. LLD: Designing an Extensible Rule / Workflow Engine (Avoiding Hardcoded Business Conditions & Supporting Dynamic JSON Rules)
- **Experience Focus:** Staff Engineer / Technical Architect
- **Target Company Archetype:** FinTech (Underwriting / KYC / Risk / Commission Rules)
- **The Real-World Scenario:** Business analysts change loan underwriting / merchant commission rules twice a week. Developers hardcoded these rules in Java, requiring a full code deployment for every tweak. A junior developer proposes using Java `ScriptEngine` (JavaScript / Groovy `eval()`) or Spring `SpEL` on raw database strings—opening a massive **Remote Code Execution (RCE)** vulnerability and slowing evaluation 50x!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Evaluating arbitrary scripts (`eval`, unrestricted `StandardEvaluationContext` in SpEL) allows expressions like `T(java.lang.Runtime).getRuntime().exec(...)` (RCE) and lacks compile-time type validation.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Replace arbitrary script execution with a **Typed JSON AST (Abstract Syntax Tree) / Composite Specification Engine** (`AND`, `OR`, `NOT`, `EQUALS`, `GREATER_THAN`, `IN_SET`).
  - **3. Permanent Architectural / Code Fix:**
    1. **Typed JSON AST Deserialized into Immutable `RuleNode` Hierarchy (Composite Pattern):**
       - `LogicalCompositeNode(LogicalOperator op, List<RuleNode> children)` (`AND`, `OR`, `NOT` with short-circuit evaluation).
       - `LeafPredicateNode<T>(FactExtractor<T> field, ComparisonOperator op, T threshold)`.
    2. **Schema & Dry-Run Validator on Rule Publish:** When an analyst updates a JSON rule in the Admin UI, validate that all referenced `field` names exist in the typed `FactRegistry`, run the new rule against a 1,000-row historical golden dataset (`Shadow / Dry-Run Preview`), and publish the validated immutable AST to pods via cache/config bus!
  - **4. Trade-offs to Defend in Front of Interviewer:** Heavyweight Rete Engine like Drools (steep learning curve, heavy memory footprint for simple stateless trees) vs Unrestricted SpEL/Groovy (RCE security nightmare) vs **Typed JSON AST Composite Specification Engine** (zero RCE surface, sub-microsecond evaluation, human-readable audit trail of which exact leaf node matched/failed).
  - **5. Senior Trap to Avoid:** Using Spring's `StandardEvaluationContext` with externally supplied SpEL strings—if SpEL is ever used for simple formulas, you MUST use **`SimpleEvaluationContext.forReadOnlyDataBinding()`** which disables `T(...)` type references, constructors, and arbitrary method invocations!

---

## 🚀 Section 2: [P1 - Architectural Trade-offs & Deep Design] (Q23–Q34)

### Q23. Enforcing Clean 4-Layer Architecture (`presentation`, `application`, `domain`, `infrastructure`) at Compile Time with ArchUnit
- **Experience Focus:** Principal Architect / Staff Engineer
- **Target Company Archetype:** FinTech | Tier-1 High-Scale (Workspace Standard)
- **The Real-World Scenario:** You establish a Clean 4-Layer Architecture (`presentation -> application -> domain <- infrastructure`) where `domain` must remain 100% pure Java with zero Spring/JPA/Mongo/Jackson annotations. Within 3 months, under deadline pressure, developers start importing `org.springframework.data.mongodb.core.mapping.Document` inside `domain.model`, injecting `MongoRepository` directly into `@RestController`s, and returning `@Entity` classes from REST endpoints!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Architecture guidelines written only in a Confluence wiki or `README.md` inevitably rot within weeks unless enforced automatically by **Compile-Time / CI Fitness Functions**.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Add **ArchUnit (`com.tngtech.archunit:archunit-junit5`)** tests to the Maven `test` phase so any PR violating layer boundaries fails the build in 2 seconds!
  - **3. Permanent Architectural / Code Fix:** Codify **4 Non-Negotiable ArchUnit Rules**:
    1. **Layered Dependency Direction:**
       ```java
       layeredArchitecture().consideringOnlyDependenciesInLayers()
           .layer("Presentation").definedBy("..presentation..")
           .layer("Application").definedBy("..application..")
           .layer("Domain").definedBy("..domain..")
           .layer("Infrastructure").definedBy("..infrastructure..")
           .whereLayer("Presentation").mayOnlyAccessLayers("Application", "Domain")
           .whereLayer("Application").mayOnlyAccessLayers("Domain")
           .whereLayer("Domain").mayNotAccessAnyLayer()
           .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain", "Application");
       ```
    2. **Pure Domain Rule:** `noClasses().that().resideInAPackage("..domain..").should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..", "com.fasterxml.jackson..")`.
    3. **Zero Entity Leak Rule:** `methods().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class).should().notHaveRawReturnType(resideInAPackage("..domain..").or(resideInAPackage("..infrastructure..entity..")))`.
  - **4. Trade-offs to Defend in Front of Interviewer:** Separate Maven sub-modules per layer (`order-domain.jar`, `order-infra.jar`: enforces boundaries via `javac`, increases `pom.xml` boilerplate across 10 microservices) vs **Single-Module Package-by-Layer + ArchUnit JUnit 5 Gate** (fast single-module build, 100% automated architectural enforcement in CI).
  - **5. Senior Trap to Avoid:** Relying on manual human PR code reviews to catch forbidden imports (`import jakarta.persistence.Entity` in `domain`)—humans miss imports in 50-file PRs; ArchUnit catches 100% of violations in `<1 second`.

---

### Q24. Rich Domain Model vs Anemic Domain Model: Where Business Invariants Actually Belong
- **Experience Focus:** Staff Engineer / Principal Architect
- **Target Company Archetype:** FinTech | Enterprise SaaS
- **The Real-World Scenario:** Across your codebase, domain entities (`Order`, `Wallet`, `LoanApplication`) are just dumb Java data bags with public `getStatus()` / `setStatus()` methods (Lombok `@Data`), while 15 different `*Service` classes contain duplicated `if (order.getStatus() == PAID) order.setStatus(SHIPPED)` logic. One service forgets to check `if (order.isFrozen())` before calling `order.setStatus(SHIPPED)`, shipping a fraud-frozen order!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Martin Fowler's **Anemic Domain Model Anti-Pattern**: when entities expose public setters (`setStatus`, `setBalance`) and have zero behavior, state invariants must be remembered and duplicated by every caller in the `application` layer.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Remove all public setters (`@Setter` / `@Data`) from Domain Aggregates and replace them with **Intent-Revealing Domain Behavior Methods (`order.markAsShipped(trackingNumber, now)`)** that enforce invariants internally!
  - **3. Permanent Architectural / Code Fix:**
    1. **Encapsulate State Transitions Inside the Aggregate Root:**
       ```java
       public final class Order {
           private OrderStatus status;
           private boolean fraudHold;

           public OrderShippedEvent markAsShipped(TrackingNumber tracking, Instant shippedAt) {
               if (this.fraudHold) {
                   throw new OrderUnderFraudHoldException(this.id);
               }
               if (this.status != OrderStatus.PAID) {
                   throw new IllegalOrderStateTransitionException(this.status, OrderStatus.SHIPPED);
               }
               this.status = OrderStatus.SHIPPED;
               this.shippedAt = Objects.requireNonNull(shippedAt);
               return new OrderShippedEvent(this.id, tracking, shippedAt);
           }
       }
       ```
    2. **Thin Application Use-Case Service:** The Application Service (`ShipOrderUseCase`) only coordinates infrastructure (1. Load `Order` from Repository Port $\rightarrow$ 2. Call `order.markAsShipped(...)` $\rightarrow$ 3. Save `Order` and publish returned Domain Event). It contains **zero domain `if-else` business math**!
  - **4. Trade-offs to Defend in Front of Interviewer:** Anemic CRUD Model (fine for simple pass-through reference tables with zero business rules) vs **Rich Domain Aggregate Root with Private State & Pure Unit Testability** (100% of business rules can be tested in `1ms` JUnit tests with zero mocks, zero Spring context, and zero possibility of a caller bypassing an invariant!).
  - **5. Senior Trap to Avoid:** Injecting Repositories or External HTTP Clients *inside* a Domain Entity (`order.ship(shippingClient, orderRepo)`)! Domain Entities must stay **100% pure, synchronous, and I/O-free**; the Application Service fetches required data from ports first and passes values into the Domain Entity's method.

---

### Q25. Lock-Free Concurrency in Java (`VarHandle`, `AtomicStampedReference` ABA Problem, and `LongAdder` Cell Striping)
- **Experience Focus:** Staff Engineer / Principal Architect
- **Target Company Archetype:** Tier-1 High-Scale | Low-Latency Trading
- **The Real-World Scenario:** You build a lock-free stack/pool using `AtomicReference.compareAndSet(expectedTop, newTop)`. Under high concurrency:
  1. Thread 1 reads `top = A` (where `A.next = B`) and gets context-switched out.
  2. Thread 2 pops `A`, pops `B`, and pushes `A` back onto the stack (where `A.next` is now `null`!).
  3. Thread 1 wakes up, executes `compareAndSet(A, B)`—which **succeeds** because `top` is `A` again, setting `top` to the already-popped detached node `B` (**The Classic Lock-Free ABA Corruption**)!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** `compareAndSet(expected, next)` only checks whether the pointer address currently equals `expected` (`A == A`), not whether the structure was mutated `A -> B -> A` in the meantime!
  - **2. Immediate Incident Mitigation (First 15 minutes):** If nodes can be reused or pushed back, pair the reference with a monotonically increasing **stamp/version** (`AtomicStampedReference<Node>` or an immutable wrapper record containing `(Node ref, long epoch)`), OR never reuse/mutate `Node` instances once popped!
  - **3. Permanent Architectural / Code Fix:**
    1. **Leverage JVM GC to Eliminate ABA Naturally:** In Java (unlike C++ where `free()`d memory addresses are immediately reused by `malloc()`), if every push allocates a fresh **immutable `Node(value, next)`** and popped nodes are never re-inserted, a memory reference `A` can **never** reappear at `top` while Thread 1 still holds a reference to `A`!
    2. **When Objects/Keys Are Reused:** Use `AtomicStampedReference<V>` (`compareAndSet(expectedRef, newRef, expectedStamp, expectedStamp + 1)`) so `A@stamp1 != A@stamp2`.
    3. **Use `VarHandle` Instead of `AtomicReferenceFieldUpdater` / `Unsafe`:** For memory-compact classes with millions of instances where wrapping fields in `AtomicReference` adds 16 bytes of object-header overhead per instance, declare `private volatile Node head;` and manipulate it directly via a `private static final VarHandle HEAD_VH`!
  - **4. Trade-offs to Defend in Front of Interviewer:** `AtomicReference<T>` (simple, adds 16–24 bytes object wrapper overhead per field) vs **`VarHandle` on a `volatile` field** (zero wrapper object allocation, full access to `getAcquire`, `setRelease`, and `compareAndSet` memory ordering modes).
  - **5. Senior Trap to Avoid:** Saying "Java does not have the ABA problem because of Garbage Collection"—GC only prevents ABA if you **allocate a new immutable node on every operation**; if you pool and reuse node objects (like in a custom object pool or ring buffer), ABA happens in Java just like in C++!

---

### Q26. False Sharing in High-Throughput Concurrent Counters & Queues (`@Contended` / Cache-Line Padding)
- **Experience Focus:** Staff Engineer / Principal Architect
- **Target Company Archetype:** Low-Latency FinTech | Tier-1 High-Scale (LMAX Disruptor / Netty / Kafka)
- **The Real-World Scenario:** You write a high-throughput ring buffer with `volatile long producerIndex` and `volatile long consumerIndex` declared side-by-side in the same class. Even though the Producer Thread ONLY writes to `producerIndex` and the Consumer Thread ONLY writes to `consumerIndex`, throughput is 5x slower than expected and CPU `L1-dcache-load-misses` are through the roof!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** CPU cores do not lock or invalidate individual 8-byte `long` variables—they invalidate entire **64-byte CPU Cache Lines** (MESI Cache Coherency Protocol)! Because `producerIndex` (8 bytes) and `consumerIndex` (8 bytes) sit adjacent in memory inside the **exact same 64-byte cache line**, every time Core 1 updates `producerIndex`, the hardware invalidates Core 2's L1 cache line containing `consumerIndex` (**False Sharing / Cache-Line Ping-Pong**)!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Pad 7 dummy `long p1, p2, p3, p4, p5, p6, p7` fields (56 bytes) between `producerIndex` and `consumerIndex`, or use `@jdk.internal.vm.annotation.Contended` (how JDK `LongAdder.Cell` and `ForkJoinPool` work!).
  - **3. Permanent Architectural / Code Fix:**
    1. **Cache-Line Padding (Inheritance Hierarchy Trick Used by LMAX Disruptor & JCTools):**
       Because the JVM can reorder fields declared in the *same* class, place the padding `long`s in superclasses (`ProducerPad -> ProducerIndexHolder -> MidPad -> ConsumerIndexHolder`) so `producerIndex` and `consumerIndex` are guaranteed to sit on completely separate 64-byte cache lines!
    2. **Local Cached Shadow Cursor:** Even better (Disruptor pattern): the Producer caches `cachedConsumerIndex` locally in a non-volatile field and only reads the volatile `consumerIndex` when the ring buffer appears full, eliminating 99% of cross-core cache reads altogether!
  - **4. Trade-offs to Defend in Front of Interviewer:** Unpadded adjacent `volatile` cursors (saves 112 bytes of RAM, suffers 5x–10x CPU cache-coherency slowdown under contention) vs **64-Byte Cache-Line Padding + Shadow Cursor Caching** (costs 128 bytes of RAM once per queue, increases throughput from 5M ops/sec to 50M+ ops/sec!).
  - **5. Senior Trap to Avoid:** Padding with `long[] pad = new long[8]` as an object reference field! A `long[]` field only puts an 8-byte *reference pointer* inside your object while allocating the array elsewhere on the heap—doing nothing to separate the two `volatile long` fields inside your object! You must use primitive `long p1..p7` fields or `@Contended`.

---

### Q27. Designing an Idempotent State Machine with Optimistic vs Pessimistic Locking in Domain Services
- **Experience Focus:** Staff Engineer / Lead Engineer
- **Target Company Archetype:** FinTech | Tier-1 High-Scale
- **The Real-World Scenario:** Your `OrderStateMachine` processes events (`PAYMENT_SUCCESS`, `MERCHANT_ACCEPTED`, `DRIVER_ASSIGNED`, `DELIVERED`, `CANCELLED`). In production, webhooks arrive twice (`PAYMENT_SUCCESS` delivered twice) or out of order, and a concurrent admin cancellation races with `DELIVERED`. How do you design the state transition table in Java so that:
  1. Duplicate events for an *already reached* state return `200 OK (No-Op)` idempotently.
  2. Invalid transitions (`DELIVERED -> CANCELLED`) throw a typed domain exception.
  3. Concurrent transitions never overwrite each other.
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** A naive transition table that only checks `if (currentState != CREATED) throw InvalidStateException()` fails when `PAYMENT_SUCCESS` is delivered a second time after `currentState` is already `PAID`, turning a harmless duplicate webhook into a `400`/`500` error!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Distinguish **Idempotent Self-Transition (`currentState == targetState` or `currentState.isAfter(targetState)`)** from **Illegal Transition (`TERMINAL_CANCELLED -> PAID`)**.
  - **3. Permanent Architectural / Code Fix:**
    1. **Declarative Enum Transition Matrix (`EnumMap<OrderState, Set<OrderState>>`):**
       ```java
       public enum OrderState {
           CREATED, PAID, ACCEPTED, DELIVERED, CANCELLED;

           public TransitionOutcome validateTransitionTo(OrderState target) {
               if (this == target) {
                   return TransitionOutcome.IDEMPOTENT_NO_OP; // Already in target state!
               }
               if (ALLOWED_NEXT_STATES.get(this).contains(target)) {
                   return TransitionOutcome.PROCEED;
               }
               throw new IllegalOrderTransitionException(this, target);
           }
       }
       ```
    2. **Concurrency Guard:** Pair the domain state transition with `@Version private Long version;` on the JPA/Mongo entity so if two pods race (`DELIVERED` vs `CANCELLED`), the database commits exactly one (`UPDATE orders SET state = ?, version = version + 1 WHERE id = ? AND version = ?`) and the loser gets `OptimisticLockingFailureException`, reloads the new state, and runs `validateTransitionTo()` again!
  - **4. Trade-offs to Defend in Front of Interviewer:** Spring StateMachine framework (heavyweight, complex configuration for simple entity lifecycles) vs **Pure Domain `Enum` State Table + JPA `@Version` Optimistic Locking** (zero external dependencies, 100% unit-testable in 1ms, self-documenting).
  - **5. Senior Trap to Avoid:** Treating `PAID -> PAID` (duplicate webhook delivery) as an error instead of returning `TransitionOutcome.IDEMPOTENT_NO_OP` and skipping duplicate outbox event emission!

---

### Q28. Anti-Corruption Layer (ACL) & MapStruct Boundary Design: Preventing Third-Party & Database Schema Leaks
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech | Enterprise SaaS (Workspace Standard)
- **The Real-World Scenario:** Your microservice integrates with a legacy SOAP/REST banking core (`finacle_acct_stat_cd = "001"`, `bal_amt_str = "000125000"`) and stores data in MongoDB (`@Document`). Previously, developers passed the bank's JSON DTO directly through the application service and stored it in MongoDB. Six months later, you add a second banking partner (`fiserv`) with completely different field names, and 40 classes across your codebase break!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Without an **Anti-Corruption Layer (ACL)** at the infrastructure boundary, external third-party schemas and database persistence annotations (`@Document`, `@Entity`) infect the core domain model, coupling your business logic to external vendor quirks.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Define a pure Domain Port (`BankCorePort`) speaking only in Domain Value Objects (`AccountId`, `Money`, `AccountStatus`), and isolate all vendor-specific DTOs and **MapStruct** mappers strictly inside `infrastructure.adapter.finacle`.
  - **3. Permanent Architectural / Code Fix:**
    1. **Strict 3-Model Separation with Compile-Time MapStruct Mappers:**
       - `presentation.dto.*` (`CreatePaymentRequestDto`, `PaymentResponseDto` with `@Valid` Jakarta Bean Validation) $\xrightarrow{\text{MapStruct}}$
       - `domain.model.*` (Pure Java immutable Records & Rich Aggregates with Value Objects like `Money`, `MerchantId`) $\xrightarrow{\text{MapStruct}}$
       - `infrastructure.persistence.entity.*` (`PaymentDocument` / `PaymentJpaEntity`) & `infrastructure.client.dto.*` (`FinacleTransferRequest`).
    2. Configure MapStruct with `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)` so adding a field to a Domain Object without mapping it causes an **immediate compile-time error** instead of silently dropping `null` in production!
  - **4. Trade-offs to Defend in Front of Interviewer:** Reusing `@Entity` as the REST DTO and Domain Model (saves writing mapper interfaces, causes LazyInitializationException, mass-assignment security bugs, and breaks external APIs whenever a DB column changes) vs **Dedicated DTO / Domain / Entity Models + MapStruct (`ReportingPolicy.ERROR`)** (zero runtime reflection overhead—MapStruct generates plain `get`/`set` Java bytecode at compile time!).
  - **5. Senior Trap to Avoid:** Leaving MapStruct's `unmappedTargetPolicy` at its default (`WARN`)—when a developer adds `String currency` to a DTO and forgets a typo in the field name, `WARN` compiles cleanly and silently sends `currency = null` to production! Always enforce `unmappedTargetPolicy = ReportingPolicy.ERROR`.

---

### Q29. Designing Thread-Safe Producer-Consumer Batching (`MicroBatcher<T, R>` Coalescing Individual Calls Into Bulk DB/API Calls)
- **Experience Focus:** Staff Engineer / Principal Architect
- **Target Company Archetype:** Tier-1 High-Scale (FinTech Ledger / ML Inference / Bulk DB Writes)
- **The Real-World Scenario:** 500 concurrent HTTP request threads each need to insert 1 ledger entry (or score 1 item against an ML model). Making 500 individual round-trips/sec takes 500 IOPS, whereas batching up to `100 items` OR waiting at most `10 milliseconds` (`maxBatchSize = 100, maxLingerMs = 10ms`) reduces DB/GPU round-trips by **100x** while returning each caller's specific result to its waiting HTTP thread! How do you implement `CompletableFuture<R> submit(T item)`?
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Each caller thread wants a synchronous/future result for its *own* single item `T`, while under the hood a background flusher needs to collect `List<Envelope<T, R>>` (up to 100 items or 10ms timeout), execute `Map<T, R> results = bulkProcessor.apply(batch)`, and complete each individual caller's `CompletableFuture<R>`!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Wrap each submission in `record BatchItem<T, R>(T payload, CompletableFuture<R> promise)` and enqueue into a bounded `LinkedBlockingQueue<BatchItem<T, R>>`.
  - **3. Permanent Architectural / Code Fix:** Implement the **Size-or-Time Triggered `MicroBatcher` (used inside Kafka `RecordAccumulator` & Google `Queues.drain`)**:
    ```java
    public CompletableFuture<R> submit(T item) {
        CompletableFuture<R> future = new CompletableFuture<>();
        if (!queue.offer(new BatchItem<>(item, future))) {
            future.completeExceptionally(new RejectedExecutionException("Batcher queue full"));
        }
        return future;
    }

    // Background Drain Loop:
    List<BatchItem<T, R>> batch = new ArrayList<>(maxBatchSize);
    while (running) {
        BatchItem<T, R> first = queue.take(); // Sleep with 0% CPU when completely idle!
        batch.add(first);
        long deadlineNanos = System.nanoTime() + maxLingerNanos;
        while (batch.size() < maxBatchSize) {
            long remaining = deadlineNanos - System.nanoTime();
            if (remaining <= 0) break;
            BatchItem<T, R> next = queue.poll(remaining, TimeUnit.NANOSECONDS);
            if (next == null) break; // Linger timer expired! Flush immediately!
            batch.add(next);
        }
        flushBatchAsync(List.copyOf(batch));
        batch.clear();
    }
    ```
    In `flushBatchAsync(batch)`, wrap `bulkProcessor.apply(...)` in `try-catch` so if the batch call throws an exception, **every `BatchItem.promise()` in that batch calls `completeExceptionally(ex)`** (never leave caller futures hanging forever on batch failure!).
  - **4. Trade-offs to Defend in Front of Interviewer:** `maxLingerMs = 10ms` (adds at most 10ms latency during low traffic, adds `0ms` latency during high traffic because `batch.size() == 100` fills in `<1ms` and flushes immediately!) vs Individual unbatched calls.
  - **5. Senior Trap to Avoid:** Forgetting to call `item.promise().completeExceptionally(t)` inside a `catch (Throwable t)` block when `bulkProcessor.apply(batch)` fails—which causes all 100 waiting HTTP threads to hang forever!

---

### Q30. Designing a Thread-Safe In-Memory File System / Hierarchical Directory Tree (`mkdir -p`, `ls`, `mv`, `rm -rf`) Without Global Lock Contention
- **Experience Focus:** Staff Engineer / Lead Engineer
- **Target Company Archetype:** Tier-1 High-Scale (Rubrik / Cohesity / Nutanix / Google Machine Coding)
- **The Real-World Scenario:** Design an in-memory POSIX-like File System (`mkdir`, `writeFile`, `readFile`, `move("/a/b/c", "/x/y/z")`, `delete`). Two concurrency traps arise:
  1. Locking the root `/` node with an exclusive lock on every file operation serializes the entire file system to 1 thread.
  2. Thread 1 runs `move("/a/b", "/x/y/b")` while Thread 2 runs `move("/x/y", "/a/b/y")`—causing either a **Lock Ordering Deadlock** OR detaching a subtree into a **disconnected cyclic loop** (`/a/b/y/b/y...`) where a directory becomes a child of its own subdirectory!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. Tree traversal (`/` $\rightarrow$ `a` $\rightarrow$ `b`) only needs **Read Locks (`shared`)** on ancestor directories and a **Write Lock (`exclusive`)** on the immediate parent directory being mutated (**Lock Crabbing / Hand-over-Hand Locking**).
    2. Moving directory `A` inside its own descendant `A/B/C` creates an orphaned cycle, and locking two directory paths (`sourceParent` and `targetParent`) in arbitrary order causes deadlocks.
  - **2. Immediate Incident Mitigation (First 15 minutes):** For `move(src, dst)`, acquire locks on `srcParent` and `dstParent` in **canonical lexicographical path order** (or hold a tree-structural move mutex while checking that `dst` is not a descendant of `src`).
  - **3. Permanent Architectural / Code Fix:**
    1. **Composite Pattern (`FsNode` sealed interface implemented by `DirectoryNode` and `FileNode`):**
       - `DirectoryNode` holds `ConcurrentSkipListMap<String, FsNode> children` (keeps `ls` naturally sorted in $O(1)$ iteration!) and a `ReentrantReadWriteLock rwLock`.
    2. **Cycle Prevention & Canonical Locking on `move(srcPath, dstPath)`:**
       - Validate `!dstCanonicalPath.startsWith(srcCanonicalPath + "/")` (cannot move `/a/b` into `/a/b/c`!).
       - Lock `firstParent = minByPath(srcParent, dstParent)` and `secondParent = maxByPath(srcParent, dstParent)` to guarantee **zero deadlocks** across concurrent `move()` calls!
  - **4. Trade-offs to Defend in Front of Interviewer:** Global `ReentrantReadWriteLock` on the entire FileSystem (10 lines of code, fast concurrent reads, serializes concurrent writes in unrelated folders `/users/alice` and `/users/bob`) vs **Per-Directory `ReentrantReadWriteLock` + Canonical Lock Ordering on Cross-Directory `move()`** (high write concurrency across different folders, deadlock-free).
  - **5. Senior Trap to Avoid:** Not checking whether a user is moving a directory into its own subdirectory (`mv /a/b /a/b/c/d`)—which detaches `/a/b` from `/` and leaks the entire subtree in a self-referencing cycle!

---

### Q31. Designing a Pluggable Retry & Backoff Executor (`RetryPolicy`, Full Jitter, Retry Budget & Non-Retryable Exception Predicate)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech | Tier-1 High-Scale
- **The Real-World Scenario:** Design a clean, non-blocking `RetryExecutor` utility (`<T> CompletableFuture<T> executeAsync(Supplier<CompletableFuture<T>> action)`). A candidate writes a loop that calls `Thread.sleep(backoff)` between retries (blocking the caller thread or Netty event-loop thread!) and retries on `IllegalArgumentException` and `NullPointerException`.
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Calling `Thread.sleep()` inside an async retry loop blocks the thread completing the failed `CompletableFuture` (which is often a shared Netty I/O event loop thread!), freezing all other network sockets on that event loop!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Schedule delayed retries non-blockingly using **`CompletableFuture.delayedExecutor(delayNanos, TimeUnit.NANOSECONDS, scheduledPool)`**!
  - **3. Permanent Architectural / Code Fix:**
    1. **Non-Blocking Recursive `CompletableFuture` Retry Chain with Full Jitter:**
       ```java
       public <T> CompletableFuture<T> executeAsync(Supplier<CompletableFuture<T>> action) {
           return attempt(action, 1);
       }

       private <T> CompletableFuture<T> attempt(Supplier<CompletableFuture<T>> action, int attemptNum) {
           return action.get().handle((result, ex) -> {
               if (ex == null) {
                   retryBudget.recordSuccess();
                   return CompletableFuture.completedFuture(result);
               }
               Throwable cause = (ex instanceof CompletionException ce && ce.getCause() != null) ? ce.getCause() : ex;
               if (attemptNum >= maxAttempts || !retryPredicate.test(cause) || !retryBudget.tryWithdrawRetryPermit()) {
                   return CompletableFuture.<T>failedFuture(cause);
               }
               long maxSleepMs = Math.min(maxBackoffMs, baseBackoffMs * (1L << (attemptNum - 1)));
               long jitteredSleepMs = ThreadLocalRandom.current().nextLong(0, maxSleepMs + 1); // AWS Full Jitter!
               Executor delayed = CompletableFuture.delayedExecutor(jitteredSleepMs, TimeUnit.MILLISECONDS, scheduler);
               return CompletableFuture.supplyAsync(() -> attempt(action, attemptNum + 1), delayed)
                                       .thenCompose(Function.identity());
           }).thenCompose(Function.identity());
       }
       ```
    2. Notice **zero threads are blocked during the backoff sleep** (`CompletableFuture.delayedExecutor`), `CompletionException` is unwrapped before testing `retryPredicate`, and `ThreadLocalRandom` is used instead of contended `java.util.Random`!
  - **4. Trade-offs to Defend in Front of Interviewer:** Blocking `Thread.sleep()` retry (acceptable only on Virtual Threads, fatal on WebFlux/Netty/Platform pools) vs **`CompletableFuture.delayedExecutor` + Full Jitter + Token-Bucket `RetryBudget`** (zero blocked threads, bounded retry amplification).
  - **5. Senior Trap to Avoid:** Passing `ex` directly to `retryPredicate.test(ex)` inside `.handle((res, ex) -> ...)` without unwrapping `CompletionException` / `ExecutionException`—causing `ex instanceof SocketTimeoutException` to always evaluate to `false` because `ex` is wrapped inside `CompletionException`!

---

### Q32. Designing Value Objects in Java 21 (`Money`, `Percentage`, `EmailAddress`, `TenantId`) to Eliminate "Primitive Obsession" Bugs
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech | Modern Product (Workspace Standard)
- **The Real-World Scenario:** A payment transfer method has signature `void transfer(String fromAccountId, String toAccountId, String tenantId, double amount, String currency)`. A caller accidentally swaps argument order: `transfer(tenantId, fromAccountId, toAccountId, 100.0, "USD")`—which compiles cleanly with zero warnings! Worse, another method adds a `USD` amount to an `INR` amount because both are raw `BigDecimal`s!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** **Primitive Obsession**: using raw `String`, `double`, or `Long` for distinct domain concepts removes compiler type safety and forces every service method to repeat null/blank/regex/currency validation.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Replace raw primitives in domain signatures with **Self-Validating Java `record` Value Objects** (`AccountId`, `TenantId`, `Money`).
  - **3. Permanent Architectural / Code Fix:**
    1. **Self-Validating Compact Record Constructor for `Money`:**
       ```java
       public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {
           public Money {
               Objects.requireNonNull(amount, "amount must not be null");
               Objects.requireNonNull(currency, "currency must not be null");
               amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
           }

           public Money add(Money other) {
               requireSameCurrency(other);
               return new Money(this.amount.add(other.amount), this.currency);
           }

           private void requireSameCurrency(Money other) {
               if (!this.currency.equals(other.currency)) {
                   throw new CurrencyMismatchException(this.currency, other.currency);
               }
           }
       }
       ```
    2. Now `transfer(AccountId source, AccountId destination, TenantId tenant, Money amount)` makes parameter-order bugs a **compile-time error** and makes adding `USD + INR` impossible!
  - **4. Trade-offs to Defend in Front of Interviewer:** Raw `String` and `BigDecimal` everywhere (zero wrapper classes, high risk of swapped IDs, missing scale rounding, or cross-currency addition bugs) vs **Compact Java `record` Value Objects** (10 lines of code, immutable, self-validating at construction boundary, compile-time type safety).
  - **5. Senior Trap to Avoid:** Using `new BigDecimal(0.1)` (double constructor, which equals `0.1000000000000000055511151231257827021181583404541015625`!) or calling `bigDecimalA.equals(bigDecimalB)` to compare monetary equality! In Java, `new BigDecimal("10.0").equals(new BigDecimal("10.00"))` returns **`false`** because `BigDecimal.equals()` compares both value AND `scale`! Always normalize scale in the `Money` compact constructor or use `compareTo() == 0`!

---

### Q33. LLD: Designing a Multi-Tenant Quota & Concurrency Limiter (Fair Sharing Across Free, Pro & Enterprise Tiers with Borrowing)
- **Experience Focus:** Staff Engineer / Principal Architect
- **Target Company Archetype:** Enterprise SaaS | Cloud Platform | AI Gateway
- **The Real-World Scenario:** Your worker cluster has a total capacity of `1,000 concurrent executions`. You have 3 tenant tiers (`FREE`, `PRO`, `ENTERPRISE`). If you give each tenant a strict static cap (`10 slots`), 90% of cluster capacity sits idle when other tenants are quiet; if you allow unbounded sharing, one bursty tenant takes all 1,000 slots and starves `ENTERPRISE` customers! How do you design a **Work-Conserving Hierarchical Concurrency Limiter** in Java?
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Static partitioning wastes idle capacity (**Non-Work-Conserving**), whereas a single shared `Semaphore(1000)` provides zero isolation against noisy neighbors.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Use a **Two-Tier Guaranteed Reserve + Shared Burst Pool (`Semaphore`)** design.
  - **3. Permanent Architectural / Code Fix:** Implement a **Guaranteed Min + Elastic Shared Burst Limiter**:
    1. **Global Capacity (`1,000`) Split Into:**
       - **Guaranteed Reserved Pool (`600`):** Reserved for each active tenant's `minGuaranteedSlots` (e.g., Enterprise always gets at least 20 guaranteed slots that nobody can steal).
       - **Shared Elastic Burst Pool (`Semaphore(400)`):** Any tenant exceeding its `minGuaranteedSlots` (up to its `maxBurstCeiling = 100`) can borrow a permit from the `sharedBurstPool` **only if the cluster has spare capacity** (`sharedBurstPool.AvailablePermits > 100` for `FREE`, `> 20` for `PRO`, `>= 1` for `ENTERPRISE`!).
    2. **Deterministic Release Token (`PermitLease implements AutoCloseable`):** When `acquire(tenantId)` succeeds, return an `AutoCloseable PermitLease` that records whether the permit came from the tenant's `ReservedPool` or the `SharedBurstPool`, so `lease.close()` in a `try-with-resources` block always returns the permit to the exact pool it was borrowed from!
  - **4. Trade-offs to Defend in Front of Interviewer:** Hard per-tenant `Semaphore` (wastes 80% of idle cluster capacity) vs **Reserved Floor + Tier-Thresholded Elastic Burst Pool with `AutoCloseable` Lease** (allows a single tenant to burst 5x when the cluster is quiet, while instantaneously protecting `ENTERPRISE` reserved capacity the millisecond load rises!).
  - **5. Senior Trap to Avoid:** Having callers call `limiter.release(tenantId)` manually instead of returning an **`AutoCloseable` `Lease` token** whose `close()` method is idempotent (`AtomicBoolean released`)—manual `release()` calls without an `AutoCloseable` token are guaranteed to leak permits when exceptions occur or if a developer calls `release()` twice!

---

### Q34. LLD Interview Execution Playbook: How to Structure a 45-Minute Machine Coding / LLD Round at Staff Level
- **Experience Focus:** Lead Engineer / Staff Engineer / Technical Architect
- **Target Company Archetype:** Uber | Amazon | Flipkart | Razorpay | Stripe | Confluent
- **The Real-World Scenario:** You have 45–60 minutes in a live LLD / Machine Coding interview to design and code a working system (e.g., Rate Limiter, In-Memory Cache, Order Matching Book, or Splitwise). Most candidates either spend 35 minutes drawing UML diagrams and write 0 running code, OR jump straight into coding a monolithic class in minute 1 and get torn apart on concurrency and extensibility in minute 40!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** At the 8–12 YoE bar, interviewers score you on **4 concrete dimensions**:
    1. **Clarifying Concurrency & Scale Invariants (First 5 mins)**
    2. **Clean Domain Model, Value Objects & Strategy/Port Interfaces (Mins 5–15)**
    3. **Thread-Safe Core Implementation with Exact Data Structures (Mins 15–35)**
    4. **Deterministic Unit & Concurrency Tests Without `Thread.sleep()` (Mins 35–45)**
  - **2. Immediate Incident Mitigation (First 15 minutes):** N/A (Interview Strategy).
  - **3. Permanent Architectural / Code Fix:** Execute this **5-Step Staff LLD Blueprint** in every round:
    1. **Step 1 (Ask the 3 High-Signal Staff Questions in Minute 1):**
       - *"What is the concurrency model—will multiple threads call this concurrently, and do we prefer fail-fast optimistic concurrency or blocking?"*
       - *"What is the hot path time-complexity expectation ($O(1)$ vs $O(\log N)$) and memory bound?"*
       - *"Which axis of the requirements is most likely to evolve (e.g., pluggable eviction policy, pricing rule, or routing strategy)?"*
    2. **Step 2 (Define Immutable Value Objects & Interfaces First):** Write Java `record`s for domain entities/events, inject a `Clock` interface for time-dependent logic, and define the primary `Strategy` interface.
    3. **Step 3 (Choose the Exact Right Concurrent Data Structure):**
       - Keyed Concurrency $\rightarrow$ `ConcurrentHashMap.compute()` / Striped `ReentrantLock[16]`.
       - Ordered Time/Price Range $\rightarrow$ `ConcurrentSkipListMap` or `TreeMap` inside lock.
       - Single-Flight Deduplication $\rightarrow$ `ConcurrentHashMap<K, CompletableFuture<V>>`.
    4. **Step 4 (Use Custom Domain Exceptions, Never Return Magic `-1` or `null`):** Throw `DuplicateIdempotencyKeyException`, `InsufficientBalanceException`, `IllegalStateTransitionException`.
    5. **Step 5 (Write 1 Concurrent Stress Test with `CountDownLatch` + `ExecutorService`):** In the final 5 minutes, write a 10-line test firing 50 threads simultaneously on a `CountDownLatch` start gun asserting that e.g. 50 threads buying 10 items results in **exactly 10 successes and 40 rejections (`remainingStock == 0`)**!
  - **4. Trade-offs to Defend in Front of Interviewer:** Explicitly state what you kept in-memory for the 45-minute scope and how it maps to production (`ConcurrentHashMap.compute` $\rightarrow$ Redis Lua / Postgres `UNIQUE + FOR UPDATE SKIP LOCKED`).
  - **5. Senior Trap to Avoid:** Writing a `main()` method with `System.out.println` instead of clean, repeatable **JUnit 5 tests (`@Test`) with `CountDownLatch` concurrency verification and an injected `FakeClock`**!
