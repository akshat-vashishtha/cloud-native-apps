# Spring Security & OAuth2 in Microservices — Interview Questions  
## Java Lead Consultant (10+ Years)

## Spring Security Fundamentals

1. What is Spring Security, and what are its core responsibilities?
2. Explain the Spring Security filter chain.
3. What is the `SecurityContext`, and how is it propagated in a Spring application?
4. What is the difference between authentication and authorization?
5. What is the difference between `AuthenticationManager`, `AuthenticationProvider`, and `UserDetailsService`?
6. What is the role of `SecurityFilterChain` in modern Spring Security?
7. What is the difference between `WebSecurityConfigurerAdapter` and the current `SecurityFilterChain` approach?
8. How does Spring Security handle CSRF, and when should CSRF protection be disabled?
9. What are CORS and CSRF, and how do they differ?
10. How would you configure CORS for a Spring Boot microservices platform?
11. What is method-level security in Spring?
12. What is the difference between `@PreAuthorize`, `@PostAuthorize`, `@Secured`, and JSR-250 annotations?
13. How would you implement role-based access control in Spring Security?
14. How would you implement permission-based or attribute-based access control?
15. How would you secure actuator endpoints in Spring Boot?
16. How would you secure Spring Boot admin endpoints differently from business APIs?
17. How would you handle authentication failures and authorization failures consistently?
18. How would you customize Spring Security error responses?
19. How would you implement security headers such as HSTS, CSP, X-Frame-Options, and Cache-Control?
20. How would you test Spring Security configurations?

## OAuth2 and OIDC Fundamentals

21. What is OAuth2, and what problem does it solve?
22. What is OpenID Connect, and how does it relate to OAuth2?
23. What is the difference between OAuth2 and JWT?
24. What are the main OAuth2 roles: client, resource owner, authorization server, and resource server?
25. What is an access token?
26. What is a refresh token?
27. What is an ID token, and how does it differ from an access token?
28. What are OAuth2 scopes?
29. What are OAuth2 claims?
30. What is the difference between scopes, roles, authorities, and permissions?
31. What are the common OAuth2 grant types?
32. Explain the authorization code flow.
33. Explain the authorization code flow with PKCE.
34. When should PKCE be used?
35. Explain the client credentials flow.
36. Explain the resource owner password credentials flow and why it is generally discouraged.
37. What is the implicit flow, and why is it deprecated?
38. What is token exchange, and when would you use it?
39. What is the difference between an authorization server and an identity provider?
40. What is issuer validation in OAuth2?

## JWT and Token Validation

41. What is a JWT, and what are its parts?
42. How is a JWT signed, and why is signature validation important?
43. What is the difference between symmetric signing and asymmetric signing?
44. When would you use HS256 versus RS256 or ES256?
45. What are the standard JWT claims such as `iss`, `sub`, `aud`, `exp`, `nbf`, `iat`, and `jti`?
46. Why should a resource server validate the `aud` claim?
47. What is a JWKS endpoint?
48. How does a resource server obtain public keys from an authorization server?
49. How does JWT key rotation work in Spring Security?
50. What happens if a JWT is signed with a key that is no longer published in JWKS?
51. What is the difference between JWT validation and token introspection?
52. When would you choose JWT validation over opaque-token introspection?
53. When would you choose opaque tokens over JWTs?
54. What are the security risks of putting sensitive data inside a JWT?
55. How would you prevent JWT tampering?
56. How would you validate custom claims in a Spring Security resource server?
57. How would you map JWT claims to Spring Security authorities?
58. How would you configure a `JwtDecoder` in Spring Boot?
59. How would you configure issuer-uri, jwk-set-uri, and audience validation?
60. How would you handle clock skew between services and the authorization server?

## Spring Boot Resource Server

61. How would you configure a Spring Boot service as an OAuth2 resource server?
62. What dependency is required for OAuth2 resource-server support in Spring Boot?
63. What is the role of `BearerTokenAuthenticationFilter`?
64. How does Spring Security extract a bearer token from an HTTP request?
65. What is `JwtAuthenticationConverter`, and when would you customize it?
66. How would you map scopes such as `orders.read` to Spring authorities?
67. How would you map roles from Keycloak, Auth0, Okta, or a custom IdP to Spring authorities?
68. How would you enforce access rules using scopes and roles?
69. How would you implement method-level authorization in a Spring Boot resource server?
70. How would you protect endpoints using `requestMatchers` and `hasAuthority`?
71. How would you secure a REST API differently from a gRPC API in Spring Boot?
72. How would you secure WebSocket/STOMP endpoints using Spring Security and OAuth2?
73. How would you validate OAuth2 tokens for gRPC calls?
74. How would you propagate an access token from a REST gateway to downstream microservices?
75. How would you implement a custom JWT claim validator?
76. How would you handle expired, malformed, or invalid tokens?
77. How would you return standards-compliant `401` and `403` responses?
78. How would you test a resource server with mocked JWTs?
79. How would you test OAuth2-protected endpoints in integration tests?
80. How would you avoid coupling every microservice to a specific identity provider?

## API Gateway Security Patterns

81. Should authentication be performed only at the API gateway, or also in downstream services? Justify your approach.
82. What is defense in depth in microservices security?
83. How would you design token validation at an API gateway and downstream services?
84. Should the API gateway forward the original JWT or replace it with a new token?
85. What is token relay in a microservices architecture?
86. What is token exchange, and how does it differ from token relay?
87. How would you implement token relay using Spring Cloud Gateway?
88. How would you prevent a downstream service from accepting tokens intended for another audience?
89. How would you design different audiences for different microservices?
90. How would you prevent privilege escalation through a compromised downstream service?
91. How would you enforce rate limiting and authentication together at an API gateway?
92. How would you handle public endpoints, authenticated endpoints, and admin endpoints at the gateway?
93. How would you hide internal microservices from external clients?
94. How would you secure internal service-to-service calls separately from external client calls?
95. How would you design a zero-trust security model for microservices?

## Service-to-Service Security

96. How would you secure communication between two backend microservices?
97. When would you use client credentials flow for service-to-service authentication?
98. How would you implement machine-to-machine authentication in Spring Boot?
99. How would you secure service-to-service calls using mTLS?
100. How would you compare OAuth2 client credentials, mTLS, and workload identity?
101. How would you secure Kafka producers and consumers?
102. How would you secure Kafka communication between microservices?
103. How would you secure Redis access in a microservices platform?
104. How would you secure MongoDB connections and enforce database-level authorization?
105. How would you secure gRPC service-to-service communication?
106. How would you use OAuth2 tokens with gRPC metadata?
107. How would you secure asynchronous event-driven communication where the original user token is unavailable?
108. How would you propagate user identity through Kafka events?
109. What are the security risks of placing user identity claims directly in Kafka event payloads?
110. How would you design service identity, workload identity, and user identity separately?

## Token Lifecycle and Revocation

111. How would you design access-token expiry and refresh-token rotation?
112. Why should access tokens be short-lived?
113. How would you securely store refresh tokens in a browser, mobile app, or backend service?
114. What is refresh-token rotation, and why is it important?
115. How would you detect refresh-token reuse?
116. How would you revoke a JWT access token before expiry?
117. Why is JWT revocation difficult?
118. How would you implement a token denylist using Redis?
119. What are the scalability implications of checking a token denylist on every request?
120. How would you revoke all tokens for a compromised user?
121. How would you revoke tokens for a compromised client application?
122. How would you handle logout in a stateless JWT-based microservices system?
123. What is single logout, and what challenges does it create in microservices?
124. How would you implement session invalidation across multiple services?
125. How would you handle token revocation when using opaque tokens?
126. How would you rotate signing keys without invalidating all active tokens?
127. How would you handle emergency key rotation after a signing-key compromise?
128. How would you design token lifetime policies for web, mobile, internal services, and long-running jobs?
129. How would you securely store OAuth2 client secrets in Kubernetes?
130. How would you audit token issuance, refresh, revocation, and validation failures?

## Authorization Architecture

131. How would you design centralized versus decentralized authorization in microservices?
132. What should be authorized centrally at the gateway, and what should be authorized in each microservice?
133. How would you implement fine-grained authorization such as “user can edit only their own order”?
134. How would you implement resource-based authorization in a microservice?
135. How would you implement ownership checks in Spring Security?
136. How would you design roles for a multi-tenant SaaS platform?
137. How would you prevent cross-tenant data access?
138. How would you propagate tenant context securely through HTTP, Kafka, gRPC, and STOMP?
139. How would you enforce tenant isolation at the API gateway, service layer, and database layer?
140. How would you design a policy engine or externalized authorization service?
141. When would you use OPA, OpenFGA, SpiceDB, or a custom authorization service?
142. How would you avoid making an external authorization service a latency bottleneck?
143. How would you cache authorization decisions safely?
144. How would you handle authorization-policy changes without redeploying services?
145. How would you audit authorization decisions for compliance?

## Microservices and Architecture Use Cases

146. Design an OAuth2-based security architecture for a microservices platform with an API gateway, multiple backend services, and a web/mobile frontend.
147. Design authentication and authorization for a Spring Boot microservices platform using Keycloak.
148. Design a token-validation strategy for high-throughput REST APIs.
149. Design secure service-to-service communication for 20+ microservices.
150. Design a secure event-driven architecture using Kafka, where consumer services need to act on behalf of users.
151. Design a secure real-time notification platform using WebSocket/STOMP and OAuth2.
152. Design a secure gRPC-based internal service mesh.
153. Design multi-tenant isolation for a SaaS application using OAuth2, Spring Security, MongoDB, and Redis.
154. Design a BFF architecture where different frontends receive differently scoped tokens.
155. Design a security model for public APIs, partner APIs, and internal APIs.
156. Design a migration path from session-based authentication to OAuth2/JWT-based authentication.
157. Design a security model for a platform with human users, service accounts, third-party integrations, and background jobs.
158. Design a zero-trust microservices platform with mTLS, OAuth2, service identity, and fine-grained authorization.
159. Design a secure API gateway that performs authentication, rate limiting, token exchange, and routing.
160. Design an authorization model for an e-commerce platform involving customers, sellers, support agents, and administrators.

## Scenario-Based Questions

161. A downstream microservice receives a valid JWT but rejects the request due to audience mismatch. How would you resolve it?
162. A user reports being logged out immediately after login. How would you troubleshoot token lifetime and refresh behavior?
163. A JWT is valid, but the user still receives `403 Forbidden`. How would you investigate the authorization failure?
164. A compromised access token must be invalidated immediately, but JWTs are stateless. How would you handle it?
165. A service is calling another service with a user’s access token, but the downstream service requires a different audience. How would you solve it?
166. An API gateway validates JWTs, but a direct internal call bypasses the gateway. How would you secure that path?
167. A background Kafka consumer must update a user’s data, but the original access token has expired. How would you design the flow?
168. A WebSocket connection remains authenticated after the user’s JWT expires. How would you handle this?
169. A user’s role is changed in the identity provider, but the change is not reflected until token expiry. How would you reduce this delay?
170. A client sends its access token in a URL query parameter. What risks does this create, and how would you remediate it?
171. A third-party integration needs limited access to specific APIs. How would you design scopes and client credentials?
172. A microservice needs to call a downstream API on behalf of a user without exposing the user’s original token. How would you implement it?
173. A tenant A user is able to access tenant B data through an API endpoint. How would you investigate and prevent this?
174. A service account token has excessive permissions. How would you apply least privilege?
175. A token-signing key is compromised. What immediate and long-term actions would you take?
176. Your authorization server is unavailable, and all resource servers reject requests. How would you improve resilience?
177. JWT validation is fast, but token introspection is adding high latency. How would you decide between them?
178. A mobile app refresh token is leaked. How would you detect and contain the impact?
179. A microservice logs full Authorization headers, exposing tokens. How would you remediate and prevent recurrence?
180. You need to support multiple identity providers across business units. How would you design a provider-agnostic security layer?

## Senior Architecture and Design Deep-Dive Questions

181. Design an end-to-end OAuth2/OIDC security architecture for a cloud-native microservices platform.
182. Design a token strategy balancing security, performance, user experience, and revocation requirements.
183. Design a centralized authorization service with low-latency policy decisions and caching.
184. Design a multi-tenant authorization model with tenant-aware JWT claims and database isolation.
185. Design a secure API gateway and BFF architecture for web, mobile, and partner clients.
186. Design secure communication for REST, gRPC, Kafka, WebSocket/STOMP, Redis, and MongoDB in one platform.
187. Design a zero-trust service-to-service security model using mTLS and workload identity.
188. Design a token revocation and emergency-response strategy for a large-scale platform.
189. Design a secure CI/CD and Kubernetes secrets-management strategy for OAuth2 clients and signing keys.
190. Design an observability and audit strategy for authentication, authorization, token lifecycle, and security incidents.
