# Redis Interview Questions — Java Lead Consultant (10+ Years)

## Core Redis Fundamentals

1. What is Redis, and why is it commonly used in modern microservice architectures?
2. How does Redis differ from MongoDB, PostgreSQL, and traditional relational databases?
3. What are Redis’s main data structures, and when would you use each?
4. Explain the difference between Redis Strings, Hashes, Lists, Sets, Sorted Sets, Streams, Bitmaps, and HyperLogLog.
5. When would you choose a Redis Hash over a JSON string stored in Redis?
6. What is the difference between Redis Lists and Redis Streams?
7. How does Redis achieve high performance?
8. What is single-threaded execution in Redis, and what are its implications for application design?
9. What are Redis pipelining and transaction capabilities?
10. How do Redis transactions differ from database transactions?
11. What is the role of Lua scripting in Redis, and when should it be used?
12. What are the risks of using long-running Lua scripts in a shared Redis instance?
13. What is key expiration in Redis, and how does Redis remove expired keys?
14. What is the difference between passive and active key expiration?
15. What are Redis namespaces and key-naming conventions in a multi-service environment?

## Caching and Cache Strategy

16. Explain cache-aside, read-through, write-through, write-behind, and write-around caching patterns.
17. Which caching pattern would you choose for a product catalog service, and why?
18. How do you decide the TTL for cached data?
19. What is cache stampede, and how do you prevent it?
20. What is cache penetration, and how can it be mitigated?
21. What is cache avalanche, and what design safeguards would you apply?
22. What is a hot key in Redis, and how do you identify and handle it?
23. What is a big key in Redis, and why can it cause production issues?
24. How would you design a multi-level caching strategy using local cache and Redis?
25. How do you handle cache invalidation when underlying data changes in MongoDB or a relational database?
26. How would you invalidate caches across multiple microservices?
27. How would you cache API responses in a Spring Boot microservice?
28. How would you cache user sessions securely in Redis?
29. How would you design caching for personalized data where cache reuse is limited?
30. How would you handle stale cache data in an eventually consistent system?
31. What eviction policies are available in Redis, and how do you choose one?
32. What is the difference between `allkeys-lru`, `allkeys-lfu`, `volatile-lru`, and `noeviction`?
33. How would you size Redis memory for a high-traffic caching workload?
34. How would you monitor cache hit ratio, miss ratio, eviction rate, and latency?
35. When should Redis not be used as a cache?

## Distributed Systems and Consistency

36. How does Redis replication work?
37. Is Redis replication synchronous or asynchronous by default, and what are the consequences?
38. What is the difference between Redis Sentinel and Redis Cluster?
39. When would you choose Redis Sentinel over Redis Cluster?
40. How does Redis Cluster partition data across nodes?
41. What are hash slots in Redis Cluster?
42. What happens when a Redis Cluster primary fails?
43. What are the limitations of Redis Cluster with respect to multi-key operations?
44. What are Redis hash tags, and why are they useful in a clustered setup?
45. How do you handle cross-slot operations in Redis Cluster?
46. What is a split-brain scenario in Redis, and how can it occur?
47. How would you design Redis for high availability in a multi-region deployment?
48. What consistency guarantees can you expect from Redis in a primary-replica setup?
49. How would you handle read-your-own-writes requirements with Redis replicas?
50. How would you design a Redis deployment for a financial or order-processing system where data loss is unacceptable?

## Persistence and Durability

51. Explain RDB persistence in Redis.
52. Explain AOF persistence in Redis.
53. What are the trade-offs between RDB and AOF?
54. When would you use both RDB and AOF together?
55. What is the impact of AOF `everysec` versus `always` configuration?
56. How would you design Redis persistence for a cache-only workload?
57. How would you design Redis persistence for queues, counters, or session data?
58. What happens to Redis data during a failover or restart?
59. How would you back up and restore a Redis deployment safely?
60. How would you prevent data loss during Redis maintenance or version upgrades?

## Distributed Locking and Coordination

61. How would you implement a distributed lock using Redis?
62. Why should a Redis distributed lock have an expiry time?
63. What is the purpose of a unique lock token when acquiring a Redis lock?
64. How would you safely release a Redis distributed lock?
65. What problems can occur if a lock holder crashes before releasing the lock?
66. What is Redlock, and what are its trade-offs?
67. When would you avoid using Redis for distributed locking?
68. How would you implement a distributed mutex for a scheduled job running across multiple Kubernetes pods?
69. How would you prevent duplicate processing of an event consumed by multiple service instances?
70. How would you design an idempotency mechanism using Redis?

## Rate Limiting, Counters, and Leaderboards

71. How would you implement a rate limiter using Redis?
72. Compare fixed-window, sliding-window, token-bucket, and leaky-bucket rate-limiting approaches.
73. How would you implement a distributed rate limiter for an API gateway?
74. How would you handle rate limiting across multiple API gateway instances?
75. How would you design a real-time leaderboard using Redis?
76. How would you implement trending products, trending hashtags, or most-viewed articles using Redis?
77. How would you implement a unique visitor counter using Redis?
78. How would you track online users in real time using Redis?
79. How would you implement feature flags or dynamic configuration using Redis?
80. How would you implement a distributed sequence generator or ID generator using Redis?

## Messaging, Streams, and Event-Driven Architecture

81. What are Redis Pub/Sub and its limitations?
82. How does Redis Streams differ from Redis Pub/Sub?
83. What are consumer groups in Redis Streams?
84. How does Redis Streams compare with Kafka?
85. When would you choose Redis Streams over Kafka?
86. When would you choose Kafka over Redis Streams?
87. How would you design an event-driven microservices architecture using Kafka and Redis together?
88. How would you use Redis to cache event payloads or aggregate views produced by Kafka consumers?
89. How would you handle delayed message processing using Redis?
90. How would you implement a retry mechanism for failed events using Redis?
91. How would you design a dead-letter queue using Redis?
92. How would you prevent duplicate event consumption in a Redis Streams consumer group?
93. How would you implement a saga or orchestration state store using Redis?
94. How would you use Redis to maintain a materialized view updated by domain events?
95. How would you design real-time notifications using Redis Pub/Sub, Redis Streams, WebSocket, and STOMP?

## Spring Boot and Java Integration

96. How do you integrate Redis with a Spring Boot application?
97. What is Spring Data Redis, and what abstractions does it provide?
98. What is the difference between `RedisTemplate` and `StringRedisTemplate`?
99. How would you configure Redis serialization in Spring Boot?
100. What serialization options are available for storing Java objects in Redis?
101. What are the risks of using JDK serialization for Redis values?
102. How would you use Spring Cache abstraction with Redis?
103. How would you implement custom cache eviction logic in Spring Boot?
104. How would you configure Redis connection pooling in Spring Boot?
105. How would you handle Redis connection failures gracefully in a Spring Boot service?
106. How would you implement a cache fallback strategy when Redis is unavailable?
107. How would you use Redis with Spring Session?
108. How would you secure Redis credentials and endpoints in a Spring Boot/Kubernetes environment?
109. How would you test Redis-dependent code in integration tests?
110. How would you use Testcontainers to test Redis-based features?

## Microservices and Architecture Use Cases

111. How would you use Redis in a microservices architecture with multiple teams and bounded contexts?
112. Should each microservice have its own Redis instance, or should Redis be shared? Justify your approach.
113. How would you prevent key collisions between services sharing Redis?
114. How would you isolate a noisy service that consumes excessive Redis memory or connections?
115. How would you design a Redis-based session store for a horizontally scaled Spring Boot application?
116. How would you design a distributed API rate limiter for a microservices platform?
117. How would you design a service discovery or health-status cache using Redis?
118. How would you use Redis to reduce load on MongoDB in a read-heavy microservice?
119. How would you design a product catalog service using MongoDB as the source of truth and Redis as the cache?
120. How would you handle cache consistency between MongoDB writes and Redis reads?
121. How would you design a shopping cart service using Redis?
122. How would you handle cart persistence, expiry, and recovery in Redis?
123. How would you design an inventory reservation workflow using Redis?
124. How would you prevent overselling in an e-commerce flash-sale scenario?
125. How would you design a distributed idempotency layer for payment or order APIs?
126. How would you use Redis for request deduplication in a high-throughput API?
127. How would you design a real-time analytics dashboard using Redis?
128. How would you use Redis to store API response metadata, correlation IDs, or distributed tracing context?
129. How would you use Redis in an API gateway for authentication, throttling, and routing metadata?
130. How would you use Redis with gRPC services for caching, rate limiting, or service coordination?

## Scenario-Based Questions

131. A Spring Boot service is experiencing high database load. How would you decide whether Redis caching is the right solution?
132. Your Redis cache hit ratio is high, but database load remains high. What could be wrong?
133. Your Redis memory usage is continuously increasing. How would you investigate and resolve it?
134. A Redis key has become extremely large and is affecting latency. How would you handle it?
135. A particular Redis key is receiving disproportionate traffic. How would you mitigate the hot-key problem?
136. Your application sees intermittent Redis timeouts during peak traffic. How would you troubleshoot it?
137. Redis latency spikes occur during RDB snapshot creation. How would you address this?
138. After a Redis failover, users report stale data. How would you investigate and prevent this?
139. A Redis primary fails, but the application continues writing to a stale connection. How would you handle this?
140. Multiple instances of a scheduled job are executing simultaneously. How would you fix this using Redis?
141. A payment API occasionally processes the same request twice. How would you implement idempotency using Redis?
142. During a flash sale, inventory is being oversold despite application-level checks. How would you redesign the solution?
143. Your Kafka consumer updates MongoDB and Redis, but Redis sometimes contains stale data. How would you make the flow more reliable?
144. A downstream service publishes events to Kafka, and multiple consumers need cached reference data. How would you use Redis effectively?
145. Your WebSocket/STOMP notification service must deliver real-time updates across multiple application pods. How would you use Redis?
146. A client is sending excessive requests and affecting service stability. How would you implement distributed rate limiting?
147. You need to cache search results, but the same query can produce different results for different users. How would you design the cache key?
148. A Redis Cluster migration is required due to capacity growth. How would you plan it with minimal downtime?
149. Your Redis instance is shared by several microservices, and one service’s Lua script is blocking others. How would you resolve this?
150. You need to guarantee that a cache entry is refreshed before expiry without causing a thundering herd. How would you design it?

## Architecture and Design Deep-Dive Questions

151. Design a highly available Redis caching layer for a global e-commerce platform.
152. Design a Redis-based session management solution for a microservices platform behind an API gateway.
153. Design a real-time notification system using Spring Boot, WebSocket, STOMP, Redis, and Kafka.
154. Design a distributed rate-limiting solution for public APIs.
155. Design a leaderboard system for a gaming or fantasy-sports application.
156. Design a distributed locking mechanism for a scheduled batch-processing system.
157. Design an inventory management system that prevents overselling during high concurrency.
158. Design a caching strategy for a product search service backed by MongoDB or Elasticsearch.
159. Design a Redis-based feature-flag and configuration service.
160. Design an event-driven order-processing system using Kafka, MongoDB, Redis, and Spring Boot.
161. Design a multi-tenant caching solution where tenants have different data sensitivity and TTL requirements.
162. Design a Redis deployment strategy for a system requiring both low-latency caching and durable operational data.
163. Design a cache invalidation strategy for data shared across order, payment, inventory, and notification services.
164. Design a Redis-based idempotency and deduplication framework for REST and gRPC APIs.
165. Design a Redis architecture that can survive a full availability-zone failure.

## Operational Excellence and Troubleshooting

166. Which Redis metrics would you monitor in production?
167. What Redis commands or tools would you use to inspect memory usage, slow commands, and connected clients?
168. How would you identify slow Redis commands in production?
169. What are dangerous Redis commands in production, and how would you restrict them?
170. How would you perform capacity planning for Redis?
171. How would you handle Redis upgrades with zero or minimal downtime?
172. How would you secure Redis in a cloud or Kubernetes environment?
173. How would you configure Redis authentication, TLS, and network isolation?
174. How would you handle Redis key migration between environments?
175. How would you create runbooks for Redis failover, memory pressure, latency incidents, and cache stampedes?