# STOMP Interview Questions — Java Lead Consultant (10+ Years)

## STOMP and WebSocket Fundamentals

1. What is STOMP, and what problem does it solve over raw WebSocket?
2. What is the relationship between WebSocket and STOMP?
3. Can STOMP run only over WebSocket? Explain.
4. What are the main STOMP frame types?
5. Explain the STOMP `CONNECT`, `CONNECTED`, `SEND`, `SUBSCRIBE`, `UNSUBSCRIBE`, `MESSAGE`, `ACK`, `NACK`, and `DISCONNECT` frames.
6. What is a STOMP destination?
7. What is the difference between `/topic` and `/queue` destinations in a typical Spring STOMP setup?
8. How does STOMP support publish-subscribe messaging?
9. How does STOMP support point-to-point messaging?
10. What is a STOMP subscription ID, and why is it important?
11. What are STOMP headers, and how are they used?
12. What is the difference between STOMP and MQTT?
13. What is the difference between STOMP and AMQP?
14. When would you choose STOMP over raw WebSocket?
15. When would raw WebSocket be a better choice than STOMP?
16. What are STOMP heartbeats, and why are they needed?
17. How do STOMP heartbeats help detect dead connections?
18. What happens when a WebSocket connection is lost but the STOMP client is still subscribed?
19. What is STOMP message acknowledgment?
20. When would you use `ACK` versus auto-acknowledgment?

## Spring Boot and Spring WebSocket

21. How do you enable STOMP support in a Spring Boot application?
22. What is `@EnableWebSocketMessageBroker`, and what does it configure?
23. What is the role of `WebSocketMessageBrokerConfigurer` in Spring?
24. What is the difference between `enableSimpleBroker` and `enableStompBrokerRelay`?
25. What is the purpose of `setApplicationDestinationPrefixes`?
26. What is the purpose of `setUserDestinationPrefix`?
27. What is the difference between `/topic`, `/queue`, `/app`, and `/user` destination prefixes in Spring?
28. How does Spring route a message sent to `/app/hello` to a controller method?
29. What is the role of `@MessageMapping` in Spring?
30. What is the role of `@SendTo` in Spring STOMP?
31. What is the difference between `@SendTo` and `@SendToUser`?
32. How do you send a message programmatically using `SimpMessagingTemplate`?
33. How do you send a message to a specific user in Spring STOMP?
34. How do you send a message to a specific session?
35. How do you access STOMP session details in a Spring controller or service?
36. How do you handle client disconnect events in Spring STOMP?
37. How do you track connected users and active sessions in Spring?
38. What message converters are available for STOMP payloads in Spring?
39. How would you use JSON payloads with Spring STOMP?
40. How would you handle malformed or unsupported STOMP messages in Spring?

## Subscriptions and Messaging Patterns

41. How would you design topic-based broadcast messaging using STOMP?
42. How would you design user-specific notifications using STOMP?
43. How would you design private one-to-one chat using STOMP?
44. How would you design group chat using STOMP?
45. How would you design dynamic topics based on user permissions?
46. How would you prevent a user from subscribing to another user’s private destination?
47. How would you implement a subscription approval or authorization mechanism?
48. How would you handle multiple subscriptions from the same client?
49. How would you unsubscribe a client from a topic programmatically?
50. How would you broadcast an event only to users who have an active subscription?
51. How would you implement message ordering guarantees in STOMP?
52. How would you handle duplicate messages received by a STOMP client?
53. How would you implement message acknowledgment for critical notifications?
54. How would you implement redelivery for unacknowledged STOMP messages?
55. How would you design a request-response interaction over STOMP?

## Scaling and Distributed Architecture

56. Why does a Spring STOMP application with an in-memory broker have scaling limitations?
57. What happens when a STOMP application is deployed with multiple pods and uses the default simple broker?
58. How would you scale a Spring STOMP application horizontally?
59. What is a STOMP broker relay, and when would you use one?
60. How would you integrate Spring STOMP with RabbitMQ as an external broker?
61. How would you integrate Spring STOMP with ActiveMQ or Artemis?
62. How would you use Redis Pub/Sub to fan out STOMP messages across multiple application instances?
63. How does Redis Pub/Sub differ from an external STOMP broker relay?
64. How would you maintain a mapping between user IDs, WebSocket sessions, and server pods?
65. Why is a user-to-pod connection registry important in a distributed WebSocket/STOMP system?
66. How would you use Redis to implement a distributed connection registry?
67. How would you route a message to a user connected to a different pod?
68. How would you handle a pod crash while a user’s WebSocket session is registered in Redis?
69. How would you design sticky sessions for WebSocket/STOMP connections, and what are their limitations?
70. Why may sticky sessions alone be insufficient for a scaled STOMP system?
71. How would you perform rolling deployments without dropping active STOMP connections?
72. How would you gracefully drain WebSocket connections during a Kubernetes pod shutdown?
73. How would you handle reconnection and subscription restoration after a client reconnects?
74. How would you design a global STOMP-based notification system across multiple regions?
75. How would you decide between Redis Pub/Sub, RabbitMQ STOMP relay, Kafka, and a dedicated WebSocket gateway?

## Reliability, Delivery, and Backpressure

76. Is STOMP delivery guaranteed by default?
77. What delivery semantics can STOMP provide: at-most-once, at-least-once, or exactly-once?
78. How would you design at-least-once delivery for STOMP notifications?
79. How would you prevent duplicate notifications on client reconnects?
80. How would you implement idempotent message processing on the client?
81. How would you handle missed messages while a user is offline?
82. How would you design offline message storage and replay for STOMP clients?
83. How would you implement a message inbox or unread-notification count using Redis and MongoDB?
84. How would you handle backpressure when a client cannot consume messages fast enough?
85. What happens if a server publishes messages faster than a slow client can process them?
86. How would you limit message rate per user or per connection?
87. How would you handle large payloads over WebSocket/STOMP?
88. How would you compress STOMP messages?
89. How would you implement message priority in a STOMP-based notification system?
90. How would you implement delayed or scheduled notifications with STOMP?
91. How would you implement retry and dead-letter handling for failed notification deliveries?
92. How would you distinguish between “message sent to broker,” “message delivered to connection,” and “message seen by user”?
93. How would you implement delivery receipts or read receipts in a STOMP-based chat application?
94. How would you design exactly-once user experience even if the underlying transport is at-least-once?
95. How would you test message-loss and duplicate-delivery scenarios in a STOMP system?

## Security

96. How would you secure a STOMP endpoint in Spring Boot?
97. How would you authenticate a WebSocket/STOMP connection?
98. How would you propagate JWT authentication from the WebSocket handshake into STOMP messages?
99. How would you authorize a STOMP `SUBSCRIBE` request?
100. How would you authorize a STOMP `SEND` request?
101. How would you prevent unauthorized users from subscribing to private user destinations?
102. How would you implement per-topic authorization rules?
103. How would you prevent one tenant from subscribing to another tenant’s topic?
104. How would you validate the destination in a STOMP message before processing it?
105. How would you prevent STOMP destination injection or malicious destination patterns?
106. How would you secure WebSocket endpoints behind an API gateway or reverse proxy?
107. What CORS and origin restrictions should be applied to WebSocket/STOMP endpoints?
108. How would you protect against Cross-Site WebSocket Hijacking?
109. How would you enforce TLS/WSS for STOMP communication?
110. How would you rate-limit STOMP `SEND` and `SUBSCRIBE` operations?
111. How would you audit sensitive STOMP messages without logging private payload data?
112. How would you handle token expiry during a long-lived WebSocket connection?
113. How would you force-disconnect a user after logout, ban, or security event?
114. How would you secure STOMP communication between backend services and an external broker?
115. How would you apply different security policies for public topics, private queues, and internal service topics?

## Observability and Troubleshooting

116. Which metrics would you monitor for a STOMP/WebSocket application?
117. How would you monitor active WebSocket connections, subscriptions, message throughput, and error rates?
118. How would you identify memory leaks caused by unclosed WebSocket sessions?
119. How would you detect zombie WebSocket connections?
120. How would you troubleshoot clients that connect but do not receive messages?
121. How would you troubleshoot messages published by one pod but not received by clients connected to another pod?
122. How would you identify whether a delivery failure is caused by the client, server, broker, or network?
123. How would you trace a STOMP message across multiple services?
124. How would you propagate correlation IDs through STOMP messages?
125. How would you log STOMP traffic safely without exposing sensitive user data?
126. How would you monitor heartbeat failures and reconnect storms?
127. How would you identify and mitigate a sudden surge in WebSocket connections?
128. How would you perform load testing for STOMP-based real-time features?
129. How would you simulate thousands of concurrent STOMP clients in a test environment?
130. How would you define SLOs for message delivery latency, connection success rate, and message loss?

## Integration with Kafka, Redis, and Microservices

131. How would you use Kafka as the source of domain events and STOMP as the client delivery channel?
132. How would you design a notification microservice that consumes Kafka events and pushes them through STOMP?
133. How would you prevent duplicate notification delivery when a Kafka consumer restarts?
134. How would you use Redis to cache user subscription preferences before pushing notifications?
135. How would you use Redis Pub/Sub to notify all STOMP server pods about a user-specific event?
136. How would you compare Redis Pub/Sub, Kafka, and RabbitMQ for distributing real-time messages to WebSocket pods?
137. When would you choose Kafka over Redis Pub/Sub for a real-time notification pipeline?
138. When would Redis Pub/Sub be preferable to Kafka for cross-pod WebSocket fan-out?
139. How would you design an event-driven order-status update flow using Kafka, MongoDB, Redis, and STOMP?
140. How would you ensure that a Kafka event is not pushed to a user who has disconnected?
141. How would you handle event ordering when multiple Kafka partitions contribute to the same user’s notification stream?
142. How would you use MongoDB to persist notification history while using STOMP for real-time delivery?
143. How would you use Redis to track unread notifications and delivery state?
144. How would you design a real-time dashboard where backend services publish aggregates and clients subscribe through STOMP?
145. How would you integrate a gRPC-based backend service with a STOMP notification layer?
146. How would you design a chat service using Spring Boot, STOMP, Redis, MongoDB, and Kafka?
147. How would you design a collaborative editing or live-activity feed using STOMP?
148. How would you design a live sports-score, delivery-tracking, or ride-status system using STOMP?
149. How would you design a multi-tenant real-time notification platform using STOMP?
150. How would you separate concerns between an API service, Kafka consumer, WebSocket gateway, and STOMP broker?

## Scenario-Based Questions

151. A user connected to Pod A publishes a message, but users connected to Pod B do not receive it. What are the possible causes and fixes?
152. Clients receive duplicate notifications after reconnecting. How would you identify and fix the root cause?
153. Users report missing notifications after a brief network interruption. How would you design recovery?
154. Your STOMP application’s memory usage grows continuously. How would you investigate it?
155. WebSocket connections are dropping frequently in Kubernetes. How would you troubleshoot it?
156. A load balancer health check passes, but clients cannot establish WebSocket connections. What could be wrong?
157. A STOMP client can connect but cannot subscribe to a private destination. How would you debug authorization?
158. A user receives notifications intended for another user. How would you investigate and prevent this security issue?
159. A Kafka consumer publishes order updates to STOMP, but some updates arrive out of order. How would you fix it?
160. A high-traffic live event causes a reconnect storm. How would you protect the system?
161. A client subscribes to thousands of topics and degrades server performance. How would you control this?
162. A malicious client sends excessive STOMP messages. How would you mitigate it?
163. A notification must reach a user within 500 milliseconds, but current delivery often takes several seconds. How would you optimize the pipeline?
164. You need to notify both online and offline users. How would you design the architecture?
165. A user has multiple devices and browser tabs open. How would you ensure consistent notification delivery?
166. A user logs out from one device but should remain connected on another. How would you manage session state?
167. A message was published to STOMP, but the user claims it was never received. How would you establish end-to-end traceability?
168. Your WebSocket gateway needs to scale from 10,000 to 1 million concurrent users. What architectural changes would you make?
169. A STOMP-based chat feature must support message history, unread counts, typing indicators, and presence. How would you design it?
170. A business wants real-time order tracking with reliable delivery even if the user refreshes the page. How would you design the solution?

## Architecture and Design Deep-Dive Questions

171. Design a scalable real-time notification system using Spring Boot, STOMP, Redis, and Kafka.
172. Design a multi-instance Spring STOMP application that supports user-specific messaging without sticky sessions.
173. Design a chat application with STOMP, presence, typing indicators, message history, and offline delivery.
174. Design a live order-tracking system where order events originate in Kafka and reach web clients through STOMP.
175. Design a real-time dashboard for operational metrics with role-based topic subscriptions.
176. Design a WebSocket/STOMP gateway that can handle millions of concurrent connections.
177. Design a secure multi-tenant STOMP notification platform.
178. Design a delivery-reliable notification system with offline replay, acknowledgments, and idempotency.
179. Design a hybrid architecture using REST for CRUD, Kafka for domain events, Redis for state, and STOMP for real-time client updates.
180. Design a global real-time platform with regional WebSocket gateways, cross-region event routing, and failover.
