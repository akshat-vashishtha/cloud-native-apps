# Architecture, HLD & System Design Interview Questions  
## Java Lead Consultant — 10+ Years Experience

## Architecture Fundamentals

1. What is the difference between solution architecture, enterprise architecture, and application architecture?
2. What is High-Level Design (HLD), and what should an HLD document contain?
3. What is Low-Level Design (LLD), and how does it differ from HLD?
4. How do you gather functional and non-functional requirements before designing a system?
5. What are the most important non-functional requirements in a distributed system?
6. How do you translate business requirements into technical architecture?
7. What is the difference between scalability, elasticity, reliability, availability, resiliency, and fault tolerance?
8. What is the difference between horizontal and vertical scaling?
9. What is statelessness, and why is it important for horizontally scalable services?
10. What is a single point of failure, and how do you eliminate one?
11. What is the difference between availability and durability?
12. What is the difference between latency, throughput, and bandwidth?
13. How do you define and measure P50, P90, P95, and P99 latency?
14. What is the difference between SLA, SLO, and SLI?
15. What is the CAP theorem, and how does it influence architectural decisions?
16. What is the PACELC theorem, and how does it extend CAP?
17. What is the difference between strong consistency, eventual consistency, and causal consistency?
18. What is the difference between synchronous and asynchronous communication?
19. What is loose coupling, and how do you achieve it in a distributed system?
20. What are architecture drivers, and how do you prioritize conflicting drivers such as cost, latency, and consistency?

## HLD and System Design Framework

21. How would you approach an HLD interview or a new system-design problem from scratch?
22. What clarifying questions would you ask before designing a system?
23. How would you estimate users, requests per second, storage, bandwidth, and cache size for a proposed system?
24. How do you calculate peak QPS from daily active users and average request patterns?
25. How do you decide whether a system is read-heavy or write-heavy, and why does it matter?
26. How do you identify the core entities, relationships, and data flows in a system?
27. How do you decide the major components of an HLD?
28. How do you draw and explain a high-level architecture diagram effectively?
29. How do you identify the critical path in a system design?
30. How do you identify bottlenecks in a proposed architecture?
31. How do you decide what to deep-dive during a system-design discussion?
32. How do you handle changing requirements during a design discussion?
33. How do you document assumptions in an HLD?
34. How do you define success metrics and acceptance criteria for an architecture?
35. How do you perform capacity planning for a new system?
36. How do you design for scale from day one without over-engineering?
37. How do you balance over-engineering and under-engineering in an initial design?
38. How do you create a migration strategy for an existing legacy system?
39. How do you perform architecture trade-off analysis?
40. How do you communicate an architecture decision to business and engineering stakeholders?

## Microservices Architecture

41. What is a microservices architecture, and when is it appropriate?
42. What are the advantages and disadvantages of microservices compared with a monolith?
43. What is a modular monolith, and when would you choose it over microservices?
44. How do you decompose a business domain into microservices?
45. What is Domain-Driven Design, and how does it help in service decomposition?
46. What are bounded contexts, aggregates, entities, value objects, and domain events?
47. What is the difference between a service boundary and a team boundary?
48. What is the database-per-service pattern, and what are its trade-offs?
49. How would you handle shared data between microservices?
50. What is data duplication in microservices, and when is it acceptable?
51. What is an anti-corruption layer?
52. What is the strangler fig pattern, and when would you use it?
53. What is the API gateway pattern, and what responsibilities should it own?
54. What is the Backend-for-Frontend (BFF) pattern?
55. What is service discovery, and how does it work in Kubernetes?
56. What is a service mesh, and what problems does it solve?
57. What is the sidecar pattern?
58. What is the ambassador pattern?
59. What is the circuit-breaker pattern?
60. What is the bulkhead pattern?
61. What is the retry pattern, and what risks does it introduce?
62. What is the saga pattern?
63. What is the difference between orchestration and choreography?
64. What is the outbox pattern, and why is it useful?
65. What is the transactional inbox pattern?
66. What is event sourcing, and when would you use it?
67. What is CQRS, and how does it relate to event-driven architecture?
68. What is a materialized view?
69. What is eventual consistency in a microservices system, and how do you manage it?
70. What is distributed tracing, and why is it essential in microservices?

## API and Communication Design

71. How would you choose between REST, gRPC, GraphQL, messaging, and WebSocket/STOMP?
72. When would you choose REST over gRPC?
73. When would you choose gRPC over REST?
74. When would you choose Kafka over REST or gRPC?
75. When would you choose WebSocket/STOMP over Kafka?
76. How would you design API versioning for public and internal APIs?
77. How would you handle backward compatibility in APIs and event schemas?
78. How would you design pagination, filtering, sorting, and search APIs?
79. How would you design idempotent APIs?
80. How would you design bulk APIs without creating performance issues?
81. How would you design long-running operations in a synchronous API?
82. How would you use asynchronous request-reply patterns?
83. How would you design a callback or webhook mechanism?
84. How would you handle partial failure when a request fans out to multiple services?
85. How would you aggregate responses from multiple microservices?
86. How would you design an API gateway for authentication, rate limiting, routing, and observability?
87. How would you secure internal service-to-service communication?
88. How would you choose between client-side and server-side load balancing?
89. How would you design timeouts, retries, circuit breakers, and fallbacks across service calls?
90. How would you prevent cascading failures in a microservices system?

## Data Architecture and Storage

91. How would you choose between SQL, NoSQL, document, key-value, wide-column, graph, and time-series databases?
92. When would you choose MongoDB over a relational database?
93. When would a relational database be a better choice than MongoDB?
94. How would you design a data model for high read throughput?
95. How would you design a data model for high write throughput?
96. What is database sharding, and what challenges does it introduce?
97. What is horizontal partitioning versus vertical partitioning?
98. What is read-replica-based scaling, and what consistency issues can arise?
99. What is write amplification, and how does it affect database performance?
100. What is indexing, and how do you decide which indexes to create?
101. How would you identify and resolve slow queries in MongoDB?
102. How would you design schema evolution for MongoDB collections?
103. How would you handle large documents and unbounded arrays in MongoDB?
104. How would you design a multi-tenant data model?
105. How would you choose between shared database, schema-per-tenant, and database-per-tenant models?
106. How would you design data archival and retention policies?
107. How would you handle GDPR, data residency, and right-to-be-forgotten requirements?
108. How would you design a change-data-capture pipeline?
109. How would you keep a search index, cache, and primary database consistent?
110. How would you design a disaster-recovery strategy for critical data stores?

## Caching Architecture

111. When should you introduce caching in a system?
112. What are cache-aside, read-through, write-through, write-behind, and write-around patterns?
113. How would you choose TTLs for cached data?
114. How would you handle cache invalidation across multiple services?
115. What is cache stampede, and how would you prevent it?
116. What is cache penetration, and how would you mitigate it?
117. What is cache avalanche, and how would you protect the system?
118. What are hot keys and big keys in Redis?
119. How would you design a multi-level cache using local cache and Redis?
120. How would you decide between Redis, Memcached, CDN, and application-level caching?
121. How would you cache personalized data safely?
122. How would you cache search results or recommendation data?
123. How would you handle stale cache data in an eventually consistent system?
124. How would you monitor cache effectiveness?
125. When would caching be a poor design choice?

## Messaging and Event-Driven Architecture

126. What is event-driven architecture, and what problems does it solve?
127. What is the difference between an event, a command, and a message?
128. What is the difference between a message queue and an event stream?
129. Compare Kafka, RabbitMQ, ActiveMQ, and cloud-managed queues.
130. What are Kafka topics, partitions, producers, consumers, and consumer groups?
131. How does Kafka achieve scalability and fault tolerance?
132. What is the relationship between partition count, consumer count, and parallelism?
133. How would you choose a partition key?
134. What are the trade-offs of a poor partition-key strategy?
135. What is message ordering in Kafka, and when can ordering be guaranteed?
136. What are at-most-once, at-least-once, and exactly-once delivery semantics?
137. How would you design idempotent Kafka consumers?
138. What is a dead-letter queue, and how would you design one?
139. How would you handle poison messages?
140. How would you design retries, backoff, and jitter for failed message processing?
141. How would you prevent consumer lag from affecting business outcomes?
142. How would you monitor Kafka consumer lag, throughput, and partition skew?
143. How would you handle schema evolution in Kafka using Avro, Protobuf, or JSON Schema?
144. What is a schema registry, and why is it important?
145. How would you design event enrichment, event transformation, and event routing?
146. What is event-carried state transfer?
147. What is the outbox pattern, and how does it prevent dual-write inconsistency?
148. How would you design a saga for a distributed business transaction?
149. How would you choose between choreography and orchestration for a multi-service workflow?
150. How would you design event replay and reprocessing safely?

## Scalability and Performance

151. How would you scale a Spring Boot microservice horizontally?
152. How would you design a stateless service in Java?
153. How would you handle session state in a horizontally scaled application?
154. How would you design connection pooling for databases, Redis, Kafka, and gRPC?
155. How would you identify CPU-bound versus I/O-bound performance issues?
156. How would you tune JVM garbage collection for a latency-sensitive service?
157. How would you identify memory leaks in a Java microservice?
158. How would you design batching without increasing end-user latency?
159. How would you implement asynchronous processing in Spring Boot?
160. How would you use Kafka to decouple high-volume write paths?
161. How would you design read scaling for a high-traffic product catalog?
162. How would you design write scaling for a high-volume event-ingestion system?
163. How would you handle traffic spikes during a flash sale or live event?
164. How would you design auto-scaling policies in Kubernetes?
165. How would you prevent a noisy neighbor from affecting other services?
166. How would you perform load testing and capacity validation?
167. How would you identify whether a bottleneck is in application code, database, cache, network, or downstream service?
168. How would you optimize P99 latency rather than only average latency?
169. How would you design a global system with low latency across regions?
170. How would you decide between performance optimization and architectural change?

## Availability, Resilience, and Failure Handling

171. How would you design a system for 99.99% availability?
172. What is the difference between active-active and active-passive deployments?
173. What is failover, and how would you design automated failover?
174. How would you design multi-AZ and multi-region architectures?
175. What is a blast radius, and how do you reduce it?
176. How would you isolate failures in a microservices system?
177. How would you implement graceful degradation?
178. How would you design fallback responses for critical user journeys?
179. How would you handle a database failover without data loss?
180. How would you handle a Kafka broker failure?
181. How would you handle a Redis primary failure?
182. How would you handle a dependency that becomes slow rather than unavailable?
183. How would you design health checks, readiness probes, and liveness probes?
184. How would you perform rolling deployments with zero downtime?
185. How would you design blue-green and canary deployments?
186. How would you use feature flags for safe releases?
187. How would you conduct chaos engineering experiments?
188. How would you design a disaster-recovery plan and define RTO/RPO?
189. How would you handle data corruption or accidental deletion?
190. How would you create incident runbooks for critical failure scenarios?

## Observability and Operational Excellence

191. What are the three pillars of observability?
192. How would you design centralized logging for microservices?
193. How would you implement distributed tracing across REST, gRPC, Kafka, and asynchronous workflows?
194. How would you propagate correlation IDs and trace context across services?
195. Which metrics would you monitor for a microservices platform?
196. How would you define actionable alerts without creating alert fatigue?
197. How would you monitor golden signals: latency, traffic, errors, and saturation?
198. How would you troubleshoot a production incident involving multiple services?
199. How would you distinguish between infrastructure failure, application failure, and dependency failure?
200. How would you design audit logging for security and compliance?
201. How would you implement configuration management across environments?
202. How would you manage secrets securely in Kubernetes?
203. How would you design a CI/CD pipeline for microservices?
204. How would you implement automated testing across unit, integration, contract, performance, and security layers?
205. How would you ensure architectural governance across multiple teams?

## Security and Compliance Architecture

206. How would you design authentication and authorization for a microservices platform?
207. How would you choose between JWTs, opaque tokens, and session-based authentication?
208. How would you design token validation at an API gateway and downstream services?
209. How would you secure service-to-service communication?
210. How would you design a zero-trust architecture?
211. How would you enforce least privilege across services and data stores?
212. How would you design multi-tenant isolation?
213. How would you secure sensitive data at rest and in transit?
214. How would you handle secrets, certificates, and key rotation?
215. How would you design auditability for regulated workloads?
216. How would you protect APIs from abuse, bots, and credential-stuffing attacks?
217. How would you design rate limiting and quota enforcement?
218. How would you handle data privacy, retention, and deletion requirements?
219. How would you design secure event publishing when events contain personal data?
220. How would you balance developer velocity with security governance?

## Scenario-Based HLD Questions

221. Design a URL shortener with high read throughput, analytics, and custom aliases.
222. Design a rate limiter for a global public API.
223. Design a distributed idempotency framework for payment APIs.
224. Design a notification service that supports email, SMS, push, WebSocket, and in-app notifications.
225. Design a real-time chat application with presence, typing indicators, message history, and offline delivery.
226. Design a live sports-score or live-auction system.
227. Design a ride-hailing system with driver location updates, matching, and trip tracking.
228. Design a food-delivery platform with order placement, restaurant acceptance, rider assignment, and live tracking.
229. Design an e-commerce product catalog with search, recommendations, pricing, and inventory.
230. Design an e-commerce checkout and payment system that prevents overselling.
231. Design an inventory-management system for a flash sale.
232. Design an order-management system with order, payment, inventory, shipping, and notification services.
233. Design a wallet or ledger system with strong consistency and auditability.
234. Design a payment-processing system with retries, reconciliation, and idempotency.
235. Design a banking transaction system with fraud detection and real-time alerts.
236. Design a job-scheduling system that executes millions of jobs reliably.
237. Design a distributed task queue with priorities, retries, and dead-letter handling.
238. Design a feature-flag service with low-latency reads and auditability.
239. Design a configuration-management service for thousands of microservices.
240. Design a distributed log-ingestion and observability platform.
241. Design a metrics-ingestion and alerting system.
242. Design a video-streaming platform with adaptive bitrate and CDN delivery.
243. Design a social-media feed with fan-out, ranking, and real-time updates.
244. Design a news-feed system for millions of users with high read throughput.
245. Design a trending-topics or hashtag-ranking system.
246. Design a recommendation engine with offline batch processing and online serving.
247. Design a search-as-you-type autocomplete service.
248. Design a location-based service such as “nearby restaurants” or “available drivers.”
249. Design a multi-tenant SaaS platform with tenant isolation, usage metering, and billing.
250. Design a document-collaboration system with real-time updates and conflict resolution.

## Java/Spring Stack Architecture Use Cases

251. Design a Spring Boot microservices platform using REST externally and gRPC internally.
252. Design an event-driven order platform using Spring Boot, Kafka, MongoDB, and Redis.
253. Design a real-time notification platform using Spring Boot, STOMP, Redis Pub/Sub, and Kafka.
254. Design a high-throughput API gateway using Spring Cloud Gateway, Redis, and OAuth2.
255. Design a caching layer for a read-heavy product service using Redis and MongoDB.
256. Design a distributed locking and scheduling framework for Spring Boot services running on Kubernetes.
257. Design a Kafka-based data pipeline that updates MongoDB, Redis, Elasticsearch, and downstream services.
258. Design an outbox-based reliable event-publishing mechanism in Spring Boot.
259. Design a CQRS-based order system with Kafka, MongoDB, and Redis-backed read models.
260. Design a saga-based payment and inventory workflow using Spring Boot and Kafka.
261. Design a multi-tenant Spring Boot SaaS platform with MongoDB, Redis, and OAuth2.
262. Design a secure API platform with Spring Security, OAuth2, API gateway, and service-to-service mTLS.
263. Design a gRPC-based internal service mesh for synchronous microservice communication.
264. Design a WebSocket/STOMP-based live dashboard with Kafka event ingestion and Redis-backed state.
265. Design a migration from a monolithic Spring application to microservices with minimal business disruption.

## End-to-End Architecture Deep-Dive Questions

266. Design a scalable, highly available e-commerce platform supporting 10 million daily active users.
267. Design a global payment platform with strict consistency, compliance, and fraud-detection requirements.
268. Design a real-time logistics platform tracking millions of vehicles or deliveries.
269. Design a social platform supporting feeds, notifications, messaging, and media uploads.
270. Design a streaming-analytics platform ingesting millions of events per second.
271. Design a healthcare platform with strict privacy, audit, and availability requirements.
272. Design a financial-trading platform with low-latency order execution and market-data distribution.
273. Design a multi-region SaaS platform with data residency, failover, and tenant isolation.
274. Design a platform that must evolve from a monolith to microservices without interrupting customers.
275. Design a system that must handle 10x traffic growth within six months while controlling cloud cost.
