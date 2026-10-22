# gRPC Interview Questions — Java Lead Consultant (10+ Years)

## gRPC Fundamentals

1. What is gRPC, and how does it differ from REST?
2. Why is gRPC commonly preferred for internal service-to-service communication in microservices?
3. What are the main components of a gRPC system?
4. What is Protocol Buffers, and why does gRPC use it?
5. How does gRPC use HTTP/2?
6. What are the benefits of HTTP/2 multiplexing in gRPC?
7. What is a `.proto` file, and what does it define?
8. What is the difference between a service definition, RPC method, request message, and response message in Protobuf?
9. What are the four types of gRPC communication patterns?
10. Explain unary, server streaming, client streaming, and bidirectional streaming RPCs.
11. When would you choose unary RPC over streaming RPC?
12. What is gRPC channel, stub, and interceptor?
13. What is the difference between a blocking stub and an asynchronous stub in Java?
14. What is Protobuf schema evolution, and why is backward and forward compatibility important?
15. What are Protobuf field numbers, and why should they never be reused or changed carelessly?
16. What are the differences between `optional`, `repeated`, `oneof`, `map`, `enum`, and nested messages in Protobuf?
17. How does Protobuf handle unknown fields during schema evolution?
18. What are the limitations of Protobuf compared with JSON?
19. When would you choose REST over gRPC?
20. When would gRPC be a poor fit for an API?

## Protobuf and API Contract Design

21. How would you design a versioning strategy for gRPC APIs?
22. How would you evolve a Protobuf contract without breaking existing clients?
23. What is the difference between adding a new field, renaming a field, changing a field type, and changing a field number?
24. How would you deprecate a field or RPC method safely?
25. How would you design error responses in gRPC?
26. What are gRPC status codes, and which ones are commonly used?
27. How would you communicate domain-specific validation errors through gRPC?
28. How would you design pagination for a gRPC API?
29. How would you design filtering, sorting, and search APIs using Protobuf?
30. How would you represent dates, timestamps, money, and IDs in Protobuf?
31. How would you avoid overly large Protobuf messages?
32. How would you design a shared Protobuf contract repository for multiple microservices?
33. How would you manage Protobuf code generation in a Java/Spring Boot multi-module project?
34. How would you prevent breaking changes in shared `.proto` files across teams?
35. How would you document a gRPC API for internal and external consumers?

## Streaming and Real-Time Communication

36. How does server streaming work in gRPC?
37. How does client streaming work in gRPC?
38. How does bidirectional streaming work in gRPC?
39. What are suitable use cases for server streaming?
40. What are suitable use cases for client streaming?
41. What are suitable use cases for bidirectional streaming?
42. How does gRPC handle backpressure in streaming calls?
43. What happens if a client or server becomes slow during a streaming RPC?
44. How would you design a long-lived bidirectional stream between two microservices?
45. What are the operational risks of long-lived gRPC streams?
46. How would you handle reconnection after a streaming connection fails?
47. How would you implement heartbeats or keep-alive for gRPC streams?
48. How would you gracefully drain active streams during a deployment or pod shutdown?
49. How would you design a real-time notification service using gRPC streaming?
50. How would you compare gRPC streaming with WebSocket, STOMP, SSE, and Kafka?
51. When would you choose Kafka over gRPC streaming for event distribution?
52. When would you choose gRPC streaming over Kafka?
53. How would you design a chat, trading, telemetry, or live-dashboard service using gRPC?
54. How would you limit the number of concurrent streams per client?
55. How would you handle partial failures during a client-streaming upload?

## Deadlines, Timeouts, and Cancellation

56. What is a gRPC deadline?
57. How is a deadline different from a timeout?
58. How does deadline propagation work across multiple gRPC calls?
59. What happens when a deadline is exceeded?
60. What is the `DEADLINE_EXCEEDED` status code, and how should clients handle it?
61. How would you set deadlines in a Java gRPC client?
62. How would you check remaining time or cancellation status on the server?
63. Why should a server respect the caller’s deadline?
64. How would you propagate deadlines through a chain of Spring Boot microservices?
65. How would you design appropriate deadline budgets for a multi-service request path?
66. What is gRPC cancellation, and how does it differ from deadline expiry?
67. How would you cancel an ongoing RPC from the client?
68. How would you clean up resources when a server-side RPC is cancelled?
69. How would you prevent a downstream service from continuing expensive work after the caller has timed out?
70. How would you handle deadline propagation when one hop uses REST and another uses gRPC?

## Error Handling and Resilience

71. What are common gRPC error status codes?
72. What is the difference between `UNAVAILABLE`, `DEADLINE_EXCEEDED`, `INTERNAL`, `INVALID_ARGUMENT`, and `NOT_FOUND`?
73. How would you map business exceptions in a Spring Boot service to gRPC status codes?
74. How would you implement global exception handling in gRPC?
75. When is it safe to retry a failed gRPC call?
76. Which gRPC errors are retryable, and which are not?
77. What is retry budget, and why is it important?
78. How would you implement retries with exponential backoff and jitter?
79. How would you prevent retry storms in a microservices system?
80. How would you implement circuit breaking for gRPC clients?
81. How would you implement fallback behavior when a gRPC dependency fails?
82. How would you implement bulkheading to isolate failures in downstream gRPC calls?
83. How would you handle partial failure when a request fans out to multiple gRPC services?
84. How would you design idempotent gRPC APIs?
85. How would you implement idempotency keys for gRPC-based payment or order APIs?
86. How would you handle duplicate requests in a gRPC service?
87. How would you design graceful degradation for a gRPC-dependent feature?
88. How would you distinguish between a client bug, network failure, and server-side failure using gRPC status codes and logs?

## Security

89. How would you secure gRPC communication in production?
90. What is TLS/mTLS in gRPC, and when would you require mutual TLS?
91. How would you implement authentication in a gRPC service?
92. How would you propagate JWTs or OAuth tokens through gRPC metadata?
93. How would you validate authorization in a gRPC interceptor?
94. What is gRPC metadata, and how is it different from HTTP headers?
95. How would you pass correlation IDs, tenant IDs, or user context through gRPC metadata?
96. How would you secure gRPC services exposed outside a Kubernetes cluster?
97. Why is gRPC-Web needed for browser clients, and what are its limitations?
98. How would you expose a gRPC API to external partners securely?
99. How would you prevent unauthorized access to internal gRPC methods?
100. How would you handle secrets, certificates, and key rotation in a Kubernetes-based gRPC deployment?
101. How would you apply rate limiting and quota enforcement to gRPC APIs?
102. How would you secure sensitive fields in Protobuf messages?
103. How would you audit security-relevant gRPC calls?

## Interceptors and Observability

104. What is a gRPC interceptor?
105. What is the difference between a client interceptor and a server interceptor?
106. What is the difference between a unary interceptor and a streaming interceptor?
107. How would you implement logging, metrics, tracing, and authentication using interceptors?
108. How would you propagate trace context across gRPC calls?
109. How would you integrate gRPC with OpenTelemetry, Micrometer, Prometheus, Grafana, and distributed tracing?
110. Which metrics would you monitor for a gRPC service?
111. How would you identify slow gRPC methods in production?
112. How would you monitor request rate, error rate, latency percentiles, active streams, and connection health?
113. How would you implement request/response logging without exposing sensitive data?
114. How would you add correlation IDs to every gRPC request and log entry?
115. How would you implement audit logging for critical gRPC operations?
116. How would you troubleshoot a gRPC latency issue across multiple microservices?
117. How would you identify whether latency is caused by serialization, network, server processing, or downstream dependencies?
118. How would you implement health checks for gRPC services?
119. What is the gRPC Health Checking Protocol?
120. How would you configure Kubernetes liveness, readiness, and startup probes for a gRPC service?

## Spring Boot and Java Integration

121. How would you expose a gRPC service from a Spring Boot application?
122. How would you call a gRPC service from another Spring Boot microservice?
123. How would you configure gRPC server port, TLS, reflection, and shutdown behavior in Spring Boot?
124. How would you generate Java classes from `.proto` files using Maven or Gradle?
125. How would you organize generated Protobuf code in a Java project?
126. How would you register gRPC services as Spring beans?
127. How would you use gRPC client interceptors in Spring Boot?
128. How would you configure connection pooling, keep-alive, and channel reuse for gRPC clients?
129. Why should gRPC channels be reused rather than created per request?
130. How would you handle graceful shutdown of gRPC servers in Spring Boot/Kubernetes?
131. How would you test a gRPC service in a Spring Boot application?
132. How would you write integration tests for gRPC client-server communication?
133. How would you mock a downstream gRPC service during testing?
134. How would you use Testcontainers or in-process gRPC servers for testing?
135. How would you handle Protobuf serialization issues in Java?
136. How would you expose both REST and gRPC endpoints from the same Spring Boot service?
137. How would you decide which endpoints should be REST and which should be gRPC?
138. How would you integrate gRPC with Spring Security?
139. How would you handle large payloads and file uploads through gRPC in Java?
140. How would you tune gRPC thread pools, event loops, and message size limits in a Spring Boot service?

## Load Balancing and Service Discovery

141. How does gRPC load balancing work?
142. What is the difference between client-side and server-side load balancing in gRPC?
143. Why can traditional L4 load balancers cause issues with gRPC?
144. How does HTTP/2 long-lived connection behavior affect load balancing?
145. What is the difference between connection-level and request-level load balancing?
146. How would you load balance unary gRPC calls in Kubernetes?
147. How would you load balance long-lived streaming gRPC calls?
148. What is a proxyless gRPC service mesh, and when would you use it?
149. How would you use a service mesh such as Istio or Linkerd with gRPC?
150. How would you integrate gRPC with Kubernetes services, DNS, and service discovery?
151. How would you handle uneven traffic distribution across gRPC server pods?
152. How would you implement zone-aware or locality-aware routing?
153. How would you handle rolling deployments without breaking active gRPC streams?
154. What is graceful drain in gRPC, and how would you implement it?
155. How would you design a blue-green or canary deployment strategy for gRPC services?

## Microservices and Architecture Use Cases

156. How would you decide between gRPC, REST, Kafka, and messaging for inter-service communication?
157. In which scenarios would you use gRPC for synchronous communication and Kafka for asynchronous communication?
158. How would you design a synchronous order-validation call using gRPC and an asynchronous order-event flow using Kafka?
159. How would you prevent tight coupling between producer and consumer when using shared gRPC contracts?
160. How would you design service boundaries for gRPC APIs in a domain-driven microservices system?
161. Should each microservice expose its own gRPC API, or should there be a shared API layer? Justify your choice.
162. How would you design a BFF (Backend for Frontend) that uses gRPC internally and REST externally?
163. How would you use gRPC between an API gateway and backend microservices?
164. How would you design a gRPC-based aggregation service that calls multiple downstream services?
165. How would you handle fan-out calls to several gRPC services and aggregate their responses?
166. How would you design a product, pricing, inventory, and recommendation service interaction using gRPC?
167. How would you use gRPC with Redis caching in a high-throughput microservice?
168. How would you use gRPC with MongoDB in a read-heavy service?
169. How would you design a real-time position, order, or notification service using gRPC and Kafka?
170. How would you implement event sourcing or CQRS with gRPC query/command services?
171. How would you design a multi-tenant gRPC platform?
172. How would you design versioned gRPC APIs for multiple consumer teams?
173. How would you manage gRPC contract ownership across domain teams?
174. How would you design a gRPC gateway for mobile, web, and partner integrations?
175. How would you choose between gRPC, GraphQL, and REST for a public-facing API?

## Scenario-Based Questions

176. A gRPC service is receiving increased latency during peak traffic. How would you identify the bottleneck?
177. Clients intermittently receive `DEADLINE_EXCEEDED`, but server logs show successful processing. What could be happening?
178. A downstream gRPC call sometimes takes longer than the client deadline. How would you redesign the interaction?
179. A service makes three sequential gRPC calls, and the overall request frequently exceeds its deadline. How would you optimize it?
180. A gRPC client is retrying failed requests and accidentally amplifying load on an already struggling service. How would you fix it?
181. During a Kubernetes rolling deployment, active gRPC streams are disconnected. How would you make deployments graceful?
182. A gRPC service has multiple pods, but most traffic goes to only one pod. Why might this happen, and how would you resolve it?
183. A long-lived bidirectional stream is pinned to one backend pod, causing uneven CPU utilization. How would you address it?
184. A client streaming upload fails midway. How would you ensure the server does not process partial or duplicate data?
185. A payment gRPC API receives duplicate requests due to client retries. How would you make it idempotent?
186. A gRPC service returns `UNAVAILABLE` after a pod restart. How would you improve client resilience?
187. A consumer team needs a new field in a shared Protobuf contract. How would you add it without breaking other consumers?
188. A field in a `.proto` file was accidentally removed, causing runtime failures. How would you recover and prevent recurrence?
189. A gRPC response message has grown very large and is affecting latency and memory. How would you redesign it?
190. A service needs to stream thousands of records to a client. How would you design pagination, flow control, and failure recovery?
191. A browser client needs to consume a gRPC API. What options and trade-offs would you consider?
192. A mobile client has unstable network connectivity and frequently disconnects from streaming RPCs. How would you design for reliability?
193. A gRPC API must support both internal Java services and external REST consumers. How would you design the architecture?
194. A service mesh is introducing latency to gRPC calls. How would you investigate whether the mesh is the cause?
195. You need to migrate an existing REST microservice to gRPC without disrupting consumers. How would you plan the migration?
196. A gRPC service must call a legacy REST system. How would you design the integration and error mapping?
197. Multiple teams share one Protobuf repository, and contract changes frequently break consumers. How would you improve governance?
198. A gRPC service is deployed across multiple regions. How would you design routing, failover, and data consistency?
199. A critical gRPC dependency fails, but the user journey must continue. How would you design fallback and degradation?
200. You are designing a new microservices platform with Kafka, Redis, MongoDB, Spring Boot, WebSocket/STOMP, and gRPC. Where would gRPC fit, and where would you explicitly avoid it?

## Senior Architecture and Design Deep-Dive Questions

201. Design a gRPC-based order management system with order, payment, inventory, notification, and shipping services.
202. Design a real-time trading or market-data platform using gRPC streaming and Kafka.
203. Design a gRPC-based API gateway and backend-for-frontend architecture.
204. Design a highly available gRPC service deployment on Kubernetes.
205. Design a secure multi-tenant gRPC platform with JWT authentication, tenant isolation, and rate limiting.
206. Design a gRPC-based distributed tracing and observability strategy.
207. Design a gRPC contract-versioning and governance model for 20+ microservices.
208. Design a resilient gRPC client framework with retries, circuit breaking, timeouts, metrics, and tracing.
209. Design a real-time notification system using gRPC streaming, Redis Pub/Sub, Kafka, and WebSocket/STOMP.
210. Design a high-throughput file-ingestion service using gRPC client streaming.
211. Design a gRPC-based aggregation layer that queries inventory, pricing, promotions, and recommendations.
212. Design a migration strategy from REST to gRPC for an existing microservices estate.
213. Design a canary deployment strategy for a gRPC service with strict backward compatibility requirements.
214. Design a global gRPC platform with multi-region failover and locality-aware routing.
215. Design a gRPC-based command/query separation model for an event-driven system using Kafka and MongoDB.