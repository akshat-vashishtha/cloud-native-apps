# 03. Spring Security 6, OAuth2/OIDC & Zero-Trust Microservices

> **Target Band:** 8–12 Years Experience (Lead Engineer / Staff Engineer / Technical Architect)  
> **Total Scenarios:** 34 (22 `[P0 - Real-World Scenarios & Outage Triage]` + 12 `[P1 - Architectural Trade-offs & Deep Design]`)  
> **Focus:** Gateway BFF Opaque Cookie vs JWT Relay, JWKS Key Rotation outages, Instant JWT Revocation at scale, Confused Deputy / Audience bypass, Cross-Tenant BOLA, `SecurityContext` propagation in async/virtual threads, and Zero-Trust mTLS.

---

## 🎯 Section 1: [P0 - Real-World Scenarios & Outage Triage] (Q1–Q22)

### Q1. JWKS Key Rotation Outage: 100% of API Requests Fail with `401 Invalid Signature` After Auth Server Rotates Keys
- **Experience Focus:** Staff Engineer / Technical Architect
- **Target Company Archetype:** FinTech | Tier-1 High-Scale
- **The Real-World Scenario:** Your OAuth2 Authorization Server rotates its RSA/ECDSA signing keypair at midnight. Immediately, all 30 downstream Spring Boot Resource Servers reject newly issued JWTs with `401 Unauthorized (Signed JWT rejected: Another algorithm expected, or no matching key(s) found)` for 15 minutes, OR all 30 services DDoS the Auth Server's `/oauth2/jwks` endpoint simultaneously!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Two classic JWKS rotation bugs:
    1. **Zero-Grace-Period Key Swap:** The Auth Server immediately started signing tokens with the new key (`kid=key-2026-11`) *before* downstream Resource Servers had fetched `kid=key-2026-11`, or removed `kid=key-2026-10` from `/oauth2/jwks` while tokens signed with `kid=key-2026-10` were still valid for another 15 minutes!
    2. **JWKS Cache Stampede on Unknown `kid`:** When Nimbus `NimbusJwtDecoder` / `RemoteJWKSet` sees an unknown `kid` in a token header, it invalidates its local cache and makes a synchronous HTTP call to `/oauth2/jwks`. If an attacker sends random fake `kid` values in forged JWT headers, your Resource Servers DDoS your own Auth Server!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Republish both the old and new public keys (`kid-old` and `kid-new`) in the JWKS endpoint JSON array and trigger a cache refresh.
  - **3. Permanent Architectural / Code Fix:**
    1. **Two-Phase Key Rotation Protocol:**
       - Phase 1 ($T=0$): Publish the new **public** key (`kid-2`) in `/oauth2/jwks` alongside `kid-1`, but keep signing tokens with `kid-1` for 15 minutes (so all caches warm up passively).
       - Phase 2 ($T=15\text{m}$): Switch active signing key to `kid-2`, while keeping `kid-1` in `/oauth2/jwks` until `max(access_token_ttl)` expires ($T=30\text{m}$).
    2. **Rate-Limit JWKS Refreshes (`RateLimitedJWKSetSource`):** Configure Nimbus `JWKSourceBuilder.create(jwksUrl).rateLimited(30_000)...` so forged tokens with random `kid` headers cannot trigger more than 1 JWKS network fetch per 30 seconds!
  - **4. Trade-offs to Defend in Front of Interviewer:** Passive Two-Phase JWKS Rotation + Rate-Limited Cache (zero downtime, immune to forged-`kid` DDoS) vs Redis Pub/Sub push invalidation on key rotation (faster propagation, adds operational coupling).
  - **5. Senior Trap to Avoid:** Replacing the key in `/oauth2/jwks` in-place with the **same `kid`**! Resource Servers index cached public keys by `kid`—if you change the key material without changing `kid`, cached decoders will never know the key changed until their TTL expires!

---

### Q2. Instant JWT Revocation When an Admin Terminates a Compromised User or Stolen Session
- **Experience Focus:** Staff Engineer / Technical Architect
- **Target Company Archetype:** FinTech (Banking / Payments) | Tier-1 High-Scale
- **The Real-World Scenario:** A customer reports their phone/session was hijacked, or an enterprise Admin clicks "Deactivate Employee Immediately." Because your microservices validate stateless JWTs locally (15-minute expiry), the attacker continues draining the wallet for 14 minutes and 50 seconds after logout/ban!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Stateless JWT validation only checks cryptographic signature and `exp` timestamp locally in memory. By design, a pure stateless JWT cannot be revoked before its `exp` time without consulting state.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Block the `sub` (User ID) or `jti` (JWT ID) at the Edge API Gateway via Redis denylist rule.
  - **3. Permanent Architectural / Code Fix:** Use the **Phantom Token / BFF Opaque Session + Short-Lived Internal JWT + Redis Denylist** hybrid architecture:
    1. **Browser to API Gateway (BFF):** Browser holds only an `HttpOnly`, `Secure`, `SameSite=Strict` opaque session cookie (`APP_SESSION`) backed by Redis at the Gateway. Revoking the session in Redis at the Gateway kills external access in **0 milliseconds**!
    2. **Defense-in-Depth for Internal JWTs:** Keep internal JWT TTL short (3–5 minutes), and for high-value actions (funds transfer, password change) or banned users, maintain a lightweight Redis Denylist (`SETEX revoked:jti:{jti} <remaining_ttl> 1` and `revoked:user:{userId} <ban_timestamp>`) checked by a custom `OAuth2TokenValidator<Jwt>`.
  - **4. Trade-offs to Defend in Front of Interviewer:** OAuth2 Token Introspection RFC 7662 on every request (100% synchronous Auth Server bottleneck, kills latency) vs Stateless JWT + Redis Denylist of *only* revoked `jti`/`sub` with TTL matching remaining token life (99.99% of tokens are not revoked so Redis memory is tiny, sub-millisecond check, instant revocation).
  - **5. Senior Trap to Avoid:** Storing *all* issued active JWTs in Redis (`whitelist`) instead of storing *only revoked* `jti`s (`denylist` with `EXPIRE = token.exp - now`)—a denylist is 1,000x smaller in memory because only $<0.1\%$ of tokens are ever revoked early!

---

### Q3. Spring `@Bean` `Filter` Double Execution Bug (`OncePerRequestFilter` Running Twice or Bypassing Security)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** Tier-1 High-Scale | FinTech
- **The Real-World Scenario:** A developer creates a custom `TenantSecurityFilter extends Filter` (or `OncePerRequestFilter`), marks the class with `@Component`, AND also registers it inside `SecurityFilterChain` using `.addFilterBefore(tenantSecurityFilter, UsernamePasswordAuthenticationFilter.class)`. Suddenly, the filter runs **twice** or runs on `@PermitAll` public paths where it shouldn't, throwing `401` on `/actuator/health`!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** In Spring Boot, **every** `@Component` or `@Bean` that implements `jakarta.servlet.Filter` is **automatically registered with the root Servlet Container (Tomcat)** across `/*` by `ServletContextInitializer`, completely outside `FilterChainProxy` (Spring Security)! When you *also* pass that bean into `http.addFilterBefore(...)`, it is registered a **second time** inside Spring Security's `FilterChainProxy`! Even if it extends `OncePerRequestFilter` (which skips the 2nd invocation), it executes **first in the root Tomcat chain** before Spring Security's path matchers (`securityMatcher` / `permitAll`) ever run!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Remove `@Component` from the custom security filter class and instantiate it directly inside `@Configuration` (or register a `FilterRegistrationBean<TenantSecurityFilter>` with `registration.setEnabled(false)`).
  - **3. Permanent Architectural / Code Fix:**
    1. Either do **not** declare custom `SecurityFilterChain` filters as top-level `@Component` beans, OR
    2. If the filter needs dependency injection as a `@Bean`, declare a matching `FilterRegistrationBean` with `setEnabled(false)`:
       ```java
       @Bean
       FilterRegistrationBean<TenantSecurityFilter> disableServletAutoReg(TenantSecurityFilter filter) {
           FilterRegistrationBean<TenantSecurityFilter> reg = new FilterRegistrationBean<>(filter);
           reg.setEnabled(false); // Prevents Tomcat root registration; only runs inside SecurityFilterChain!
           return reg;
       }
       ```
  - **4. Trade-offs to Defend in Front of Interviewer:** Non-bean instantiation `new MySecurityFilter(dep1, dep2)` inside `SecurityFilterChain` `@Bean` method (zero risk of Tomcat auto-registration, clean and explicit) vs `@Component` + `FilterRegistrationBean.setEnabled(false)`.
  - **5. Senior Trap to Avoid:** Using `webSecurity.ignoring().requestMatchers("/public/**")` in Spring Security 6—Spring Security 6 deprecated `webSecurity.ignoring()` for HTTP endpoints because it bypasses security response headers (`X-Content-Type-Options`, `Strict-Transport-Security`, CORS); always use `authorizeHttpRequests(auth -> auth.requestMatchers("/public/**").permitAll())` instead!

---

### Q4. `SecurityContextHolder` Returning `null` Inside `@Async`, `CompletableFuture`, or Virtual Threads
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech | Tier-1 High-Scale
- **The Real-World Scenario:** An authenticated controller calls an `@Async` service method (or `CompletableFuture.supplyAsync`) which invokes a Feign/`RestClient` interceptor that forwards the user's JWT from `SecurityContextHolder.getContext().getAuthentication()`. Inside the async thread, `getAuthentication()` returns `null`, and all downstream calls fail with `401 Unauthorized`!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** By default, `SecurityContextHolder` uses `MODE_THREADLOCAL`, binding the `Authentication` object strictly to the incoming HTTP Servlet thread. Worker threads in `@Async` pools or `CompletableFuture` have an empty `ThreadLocal` `SecurityContext`.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Wrap the `Executor` in `DelegatingSecurityContextAsyncTaskExecutor` (or `DelegatingSecurityContextExecutorService`).
  - **3. Permanent Architectural / Code Fix:**
    1. **Explicitly pass domain identity** (`UserPrincipal` / `TenantId` record) as a method parameter to async/background tasks where possible (cleanest, zero hidden thread-local coupling).
    2. Wrap all application `TaskExecutor` beans with **`DelegatingSecurityContextAsyncTaskExecutor`** (or Micrometer's `ContextPropagatingTaskDecorator` paired with `SecurityContextAccessor`), which captures the `SecurityContext` on the submitting thread, binds it to the worker thread before `run()`, and **clears it in `finally`** so pooled threads never leak credentials to the next task!
  - **4. Trade-offs to Defend in Front of Interviewer:** `DelegatingSecurityContextAsyncTaskExecutor` (safe for pooled threads, cleans up in `finally`) vs `SecurityContextHolder.MODE_INHERITABLETHREADLOCAL` (DANGEROUS in thread pools—leaks the first user's `SecurityContext` permanently to reused pool threads!).
  - **5. Senior Trap to Avoid:** Setting `-Dspring.security.strategy=MODE_INHERITABLETHREADLOCAL` in a microservice that uses pooled `ThreadPoolTaskExecutor`—as soon as a pooled thread is created during User A's request, `InheritableThreadLocal` copies User A's identity onto that thread and **never overwrites or clears it** when the thread is reused for User B!

---

### Q5. Confused Deputy Attack: JWT From Service A Accepted by Service B Due to Missing Audience (`aud`) Validation
- **Experience Focus:** Staff Engineer / Principal Architect
- **Target Company Archetype:** FinTech | Tier-1 High-Scale
- **The Real-World Scenario:** Both your low-privilege `Customer-Feedback-Service` and high-privilege `Core-Ledger-Service` trust JWTs signed by the same internal OAuth2 Authorization Server (`issuer-uri: https://auth.internal`). A compromised pod in `Customer-Feedback-Service` takes a valid user JWT sent to it and replays that exact JWT against `Core-Ledger-Service` to initiate a wire transfer—and `Core-Ledger-Service` accepts it!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** By default, Spring Security's `JwtValidators.createDefaultWithIssuer(issuerUri)` validates only 3 things: **Signature (`jwks`)**, **Expiration (`exp`/`nbf`)**, and **Issuer (`iss`)**. It does **NOT** validate the **Audience (`aud`)** claim by default! Therefore, any token issued by `auth.internal` for *any* client/service is accepted by *every* microservice in the company (**Confused Deputy Vulnerability**).
  - **2. Immediate Incident Mitigation (First 15 minutes):** Add a `JwtClaimValidator<List<String>>` enforcing `aud.contains("core-ledger-service")` on `Core-Ledger-Service`'s `JwtDecoder`.
  - **3. Permanent Architectural / Code Fix:**
    1. Configure a composite `OAuth2TokenValidator<Jwt>` in your shared Resource Server starter that enforces `iss`, `exp`, AND service-specific **`aud` (`AudienceValidator`)**:
       ```java
       OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
           JwtClaimNames.AUD, aud -> aud != null && aud.contains("core-ledger-service"));
       OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
       jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator));
       ```
    2. Use **OAuth2 Token Exchange (RFC 8693)** when Service A needs to call Service B on behalf of a user, exchanging the token for a narrow-scoped token with `aud = service-b`.
  - **4. Trade-offs to Defend in Front of Interviewer:** Simple Token Relay (zero extra network hop to Auth Server, vulnerable to lateral replay if `aud` is broad) vs RFC 8693 OAuth2 Token Exchange / Sender-Constrained mTLS tokens (cryptographically scopes each hop's audience and permissions, adds token-exchange caching latency).
  - **5. Senior Trap to Avoid:** Assuming `spring.security.oauth2.resourceserver.jwt.issuer-uri` validates `aud` automatically—you must explicitly configure `spring.security.oauth2.resourceserver.jwt.audiences=my-service-id` (added in Spring Boot 3.x) or a custom validator!

---

### Q6. Broken Object Level Authorization (BOLA / IDOR) Bypassing `@PreAuthorize("hasRole('USER')")`
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech | Modern Product
- **The Real-World Scenario:** Your endpoint `GET /api/v1/accounts/{accountId}/statements` is protected by `@PreAuthorize("hasAuthority('SCOPE_statements:read')")`. An authenticated user with a valid token changes `{accountId}` in the URL from `acc_101` to `acc_102` and downloads another customer's bank statement!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** This is **OWASP API1:2023 Broken Object Level Authorization (BOLA / IDOR)**. Role/Scope checks (`hasRole` / `hasAuthority`) only verify **Function-Level Authorization** ("Can this user call the statements endpoint at all?"), NOT **Object-Level Ownership** ("Does `acc_102` actually belong to `jwt.sub`?").
  - **2. Immediate Incident Mitigation (First 15 minutes):** Update the database query in the service/repository from `findById(accountId)` to `findByIdAndOwnerUserId(accountId, currentUserId)` returning `404 Not Found` if it doesn't match.
  - **3. Permanent Architectural / Code Fix:**
    1. **Query-Level Ownership Enforcement (Best):** Make `ownerUserId` / `tenantId` (extracted strictly from `SecurityContext`, NEVER from request body/headers) a mandatory parameter in all repository lookup methods: `accountRepository.findByAccountIdAndUserId(accountId, principal.userId())`.
    2. **Spring Security 6 SpEL Bean Method Authorization:** Or attach `@PreAuthorize("@accountAccessGuard.canRead(authentication, #accountId)")` combined with `@PostAuthorize("returnObject.ownerId == authentication.name")`.
    3. Use non-enumerable identifiers (UUIDv7 or random external public IDs) instead of sequential `1, 2, 3` auto-increment IDs.
  - **4. Trade-offs to Defend in Front of Interviewer:** `@PreAuthorize("@guard.check(#id)")` (clean declarative separation, executes 1 extra lookup query before the service method unless cached) vs Repository `findByIdAndUserId(id, userId)` (single atomic SQL query, zero extra DB round-trip, impossible to bypass).
  - **5. Senior Trap to Avoid:** Returning `403 Forbidden` when User A requests User B's resource ID—returning `403` for existing IDs and `404` for non-existent IDs allows attackers to **enumerate valid account IDs**! Return `404 Not Found` when ownership lookup fails.

---

### Q7. JWT Payload Bloat (`431 Request Header Fields Too Large`) When Enterprise Users Have 400 RBAC Permissions
- **Experience Focus:** Staff Engineer / Technical Architect
- **Target Company Archetype:** Enterprise SaaS | FinTech
- **The Real-World Scenario:** Enterprise admin users belong to 25 groups and hold 450 granular permissions (`invoice:create`, `invoice:approve`, `report:export`, etc.). When they log in, their JWT grows to **18KB**, causing Nginx / Envoy / Spring Boot Tomcat (`server.max-http-request-header-size: 8KB`) to reject all their requests with `HTTP 431 Request Header Fields Too Large`!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Stuffing fine-grained authorization permissions or multi-organization entitlements directly into a JWT violates token design: JWTs are transmitted in the `Authorization` header on **every HTTP call** and must stay compact ($< 2\text{KB}$).
  - **2. Immediate Incident Mitigation (First 15 minutes):** Temporarily raise `server.max-http-request-header-size=32KB` at Ingress and Tomcat while stripping fine-grained permission arrays out of the JWT.
  - **3. Permanent Architectural / Code Fix:** Keep the JWT strictly as an **Identity & Coarse Session Token** (`sub`, `tenant_id`, `roles`, `jti`, plus a `perm_version` hash—total size $< 800$ bytes):
    1. Store the user's expanded permission set in **Redis** keyed by `rbac:perms:{userId}:{tenantId}` (or `rbac:role:{roleId}` bitmap/set).
    2. In downstream Resource Servers, implement a custom `JwtAuthenticationConverter` (backed by a 60-second local Caffeine L1 cache + Redis L2 cache invalidated on `perm_version` change) that hydrates `GrantedAuthority` instances on the fly in $<0.5\text{ms}$!
  - **4. Trade-offs to Defend in Front of Interviewer:** Fat JWT with all permissions (zero Redis lookup, breaks HTTP 8KB header limits, stale permissions until token expiry) vs Thin Identity JWT + `perm_version` + Redis/Caffeine Permission Hydration (tiny 600-byte header, instant permission updates when admin edits roles, $<0.5\text{ms}$ cached lookup).
  - **5. Senior Trap to Avoid:** Permanently setting `max-http-request-header-size=64KB` across all services—which opens your entire fleet to Slowloris/memory exhaustion DoS attacks and wastes 64KB per HTTP connection buffer!

---

### Q8. Header Spoofing Bypass (`X-User-Id` / `X-Tenant-Id` Injected by External Client)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech | Tier-1 High-Scale
- **The Real-World Scenario:** Your API Gateway validates the user's session and forwards `X-User-Id` and `X-Roles` headers to downstream microservices. A penetration tester sends a request to the API Gateway with an *already-attached* header `X-User-Id: admin-001` on a public/semi-authenticated route, or reaches a microservice directly inside the VPC, and gains full admin access!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Two fatal Zero-Trust violations:
    1. The API Gateway did not **strip incoming internal headers** (`X-User-*`, `X-Tenant-*`, `X-Forwarded-*`) from external untrusted requests before running gateway filters.
    2. Downstream microservices blindly trusted plaintext HTTP headers (`@RequestHeader("X-User-Id")`) instead of verifying a cryptographic signature!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Configure the API Gateway pre-filter to unconditionally strip/overwrite all `X-Internal-*`, `X-User-*`, and `X-Tenant-*` headers at the edge before authentication runs.
  - **3. Permanent Architectural / Code Fix:** Enforce **Zero-Trust Internal Authentication**:
    1. **Gateway Token Relay / Phantom Token:** The API Gateway exchanges the external `APP_SESSION` cookie for a **cryptographically signed internal JWT** (`Authorization: Bearer <internal-jwt>`).
    2. Downstream microservices **never** trust raw `X-User-Id` headers; they validate the JWT signature against the Auth Server's JWKS and extract `sub` and `tenant_id` exclusively from the verified `Jwt` principal!
    3. Enforce **Istio / Linkerd strict mTLS + `AuthorizationPolicy`** so only the API Gateway's SPIFFE identity (`spiffe://cluster.local/ns/gateway/sa/gateway-sa`) can connect to internal pods.
  - **4. Trade-offs to Defend in Front of Interviewer:** Plaintext `X-User-Id` header from Gateway (zero CPU cost, catastrophic total compromise if any SSRF or internal pod bypasses Gateway) vs Signed Internal JWT + Service Mesh mTLS (true Zero-Trust, `<0.2ms` ECDSA/RSA public key verification in memory).
  - **5. Senior Trap to Avoid:** Saying "Our microservices are inside a private VPC subnet so they don't need authentication." A single SSRF vulnerability in an image-resizer pod inside the VPC would allow an attacker to call every microservice with `X-User-Id: CEO`!

---

### Q9. Spring Security 6 `DeferredSecurityContext` & Async Streaming (`StreamingResponseBody` / SSE) `AccessDeniedException` on Response Commit
- **Experience Focus:** Staff Engineer
- **Target Company Archetype:** Modern Product | Tier-1 High-Scale
- **The Real-World Scenario:** After upgrading to Spring Boot 3 / Spring Security 6, a controller returning `StreamingResponseBody` (or `SseEmitter` / `Callable<ResponseEntity>`) succeeds on the initial call, but when the async dispatch completes and flushes the final chunk, Spring Security throws `AccessDeniedException` (after HTTP `200 OK` headers were already written to the socket!).
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Servlet async requests (`StreamingResponseBody`, `SseEmitter`, `Mono`) go through **two** Servlet container dispatches:
    1. `DispatcherType.REQUEST` (initial controller invocation).
    2. `DispatcherType.ASYNC` (when the background thread finishes writing and completes the async context).
    In Spring Security 6, `AuthorizationFilter` runs on **every dispatch type** (including `ASYNC` and `ERROR`), whereas older `FilterSecurityInterceptor` only ran on `REQUEST`! If the `SecurityContext` was stateless (`SessionCreationPolicy.STATELESS`) and not propagated to the `AsyncContext`, on the `ASYNC` re-dispatch the user is now `AnonymousAuthenticationToken`, triggering `AccessDeniedException`!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Permit `DispatcherType.ASYNC` and `DispatcherType.ERROR` in `SecurityFilterChain`:
    `.authorizeHttpRequests(auth -> auth.dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()...)` or configure `DelegatingSecurityContextAsyncTaskExecutor` for MVC async support (`WebMvcConfigurer.configureAsyncSupport`).
  - **3. Permanent Architectural / Code Fix:**
    1. Register `DelegatingSecurityContextAsyncTaskExecutor` inside `WebMvcConfigurer.configureAsyncSupport(AsyncSupportConfigurer configurer)` so the `Authentication` principal is preserved across `ASYNC` dispatches.
    2. Explicitly configure `.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()` so `/error` dispatches don't mask `400`/`500` errors behind a bogus `401`/`403`!
  - **4. Trade-offs to Defend in Front of Interviewer:** Propagating `SecurityContext` across `AsyncSupportConfigurer` (maintains full security audit principal during async streaming) vs `.dispatcherTypeMatchers(ASYNC).permitAll()` (skips redundant re-authorization on final async completion after initial `REQUEST` already authorized it).
  - **5. Senior Trap to Avoid:** Wondering why a `404 Not Found` or `500 Internal Server Error` in Spring Boot 3 suddenly returns `401 Unauthorized` or `403 Forbidden` to the client—that happens when `/error` (`DispatcherType.ERROR`) is blocked by `anyRequest().authenticated()` without `.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()`!

---

### Q10. CORS Preflight (`OPTIONS`) Requests Failing with `401 Unauthorized` in Spring Security 6
- **Experience Focus:** Lead Engineer
- **Target Company Archetype:** Modern Product | Enterprise SaaS
- **The Real-World Scenario:** Your Next.js SPA on `https://app.company.com` calls your API on `https://api.company.com` with `Authorization: Bearer <token>`. You added `@CrossOrigin` on the `@RestController`, yet every browser call fails in the console with `CORS error: Response to preflight request doesn't pass access control check: HTTP 401 Unauthorized`!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Browser CORS preflight `OPTIONS` requests **never include the `Authorization` header or cookies** by W3C specification! Because Spring Security's `SecurityFilterChain` executes **before** Spring MVC (`DispatcherServlet` where `@CrossOrigin` lives), if `.cors(Customizer.withDefaults())` is not enabled on `HttpSecurity`, Spring Security intercepts the unauthenticated `OPTIONS` request, sees no `Bearer` token, and rejects it with `401 Unauthorized` before `CorsFilter` ever runs!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Add `.cors(Customizer.withDefaults())` inside `SecurityFilterChain` and define a `CorsConfigurationSource` `@Bean`.
  - **3. Permanent Architectural / Code Fix:**
    1. Configure CORS in **one single layer** (preferably at the **API Gateway**; or if direct to service, via `.cors(Customizer.withDefaults())` + `CorsConfigurationSource` bean in `SecurityFilterChain`—never mix Gateway CORS headers AND downstream service CORS headers, or browsers fail with `Multiple 'Access-Control-Allow-Origin' headers are not allowed`!).
    2. Never use `allowedOrigins("*")` when `allowCredentials(true)` is enabled—use explicit origin allowlists or `allowedOriginPatterns("https://*.company.com")`.
  - **4. Trade-offs to Defend in Front of Interviewer:** Centralizing CORS at the API Gateway (single source of truth, downstream microservices don't need browser CORS config) vs Per-Microservice CORS (causes duplicate `Access-Control-Allow-Origin` header crashes if Gateway also adds them).
  - **5. Senior Trap to Avoid:** Adding `ResponseDedupeFilter` as a band-aid instead of disabling downstream CORS when the API Gateway already handles CORS at the edge.

---

### Q11. OAuth2 Refresh Token Race Condition & False-Positive Reuse Detection Logging Out Active Users
- **Experience Focus:** Staff Engineer / Technical Architect
- **Target Company Archetype:** FinTech | Modern Product
- **The Real-World Scenario:** You enabled **Refresh Token Rotation** (each refresh token is single-use; replaying an already-used refresh token revokes the entire token family for security). In production, whenever a user opens 3 browser tabs after being idle, all 3 tabs fire `/auth/refresh` simultaneously, and the user is forcibly logged out with a security alert!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** When the access token expires, Tab 1, Tab 2, and Tab 3 (or 3 parallel API requests on a single page) all detect the expired token at the exact same millisecond and send the **same Refresh Token `RT_1`** to the Auth Server / BFF. Request 1 rotates `RT_1 -> RT_2`. Two milliseconds later, Request 2 arrives with `RT_1`. The Auth Server sees `RT_1` was already consumed, assumes a hacker stole `RT_1`, and **nukes the entire session family (`RT_2` included)**!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Introduce a **10-second Replay Grace Window** on the BFF / Auth Server where presenting an already-rotated `RT_1` within 10 seconds of its rotation returns the newly minted token pair (`AT_2`, `RT_2`) instead of revoking the family.
  - **3. Permanent Architectural / Code Fix:**
    1. **BFF Cookie Architecture (Best):** Move token refresh entirely into the backend **API Gateway / BFF** synchronized per session ID via a Redis lock (`SET lock:refresh:{sessionId} NX PX 5000`). If 5 concurrent requests arrive with an expired access token, Request 1 refreshes the token while Requests 2–5 wait 30ms on the lock and reuse the newly refreshed token!
    2. **Server-Side Grace Window:** Allow a narrow 5–10s idempotency window caching `old_rt_hash -> new_token_response` in Redis.
  - **4. Trade-offs to Defend in Front of Interviewer:** Strict zero-window single-use rotation (theoretically purest, causes constant false-positive logouts on mobile flaky networks and multi-tab browsers) vs BFF Mutex Coalescing + 5-second idempotency cache (eliminates multi-tab race conditions while still catching real offline token theft).
  - **5. Senior Trap to Avoid:** Storing Refresh Tokens in browser `localStorage`! Any XSS vulnerability in an npm dependency can read `localStorage` and exfiltrate the Refresh Token. Always keep Refresh Tokens inside the Backend-For-Frontend (BFF) Redis store or in an `HttpOnly; Secure; SameSite=Strict; Path=/auth/refresh` cookie.

---

### Q12. BCrypt Password Hashing CPU Starvation Under Login Surge / Credential Stuffing Attack
- **Experience Focus:** Staff Engineer / Technical Architect
- **Target Company Archetype:** FinTech | Tier-1 High-Scale
- **The Real-World Scenario:** Your Auth Server uses `BCryptPasswordEncoder(12)` (~250ms pure CPU per hash). During a botnet credential-stuffing attack (500 login attempts/sec with non-existent usernames) or a morning login surge, Auth Server CPU hits 100%, and even token validation / JWKS endpoints time out!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. Password hashing (`BCrypt`, `Argon2id`) is **intentionally slow and CPU-intensive** to resist GPU cracking. On an 8-core pod, `8 cores / 0.25s = 32 logins/sec` maximum physical capacity!
    2. Worse, when a user was not found in the database, either the code skipped BCrypt (opening a **User Enumeration Timing Attack** where valid users take 250ms and invalid users take 2ms) OR ran BCrypt on every unthrottled bot request, exhausting all CPU cores!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Enable strict edge rate-limiting on `POST /oauth2/token` and `POST /login` by IP + username (`5 attempts/min`), and isolate password hashing onto a dedicated bounded thread pool / Bulkhead so `/oauth2/jwks` and `/userinfo` are never starved.
  - **3. Permanent Architectural / Code Fix:**
    1. **Edge Rate Limiting + Device Fingerprinting / Turnstile:** Block credential stuffing at the WAF / API Gateway before it reaches the JVM.
    2. **CPU Bulkhead:** Execute `passwordEncoder.matches()` inside a dedicated `Semaphore(cpuCores - 2)` bulkhead so at least 2 CPU cores always remain free for JWKS, token refresh, and health probes!
    3. **Constant-Time User Enumeration Defense:** Spring Security's `DaoAuthenticationProvider` hashes against a pre-computed `userNotFoundEncodedPassword` when a username doesn't exist—keep that enabled, but strictly behind IP/username rate limits.
  - **4. Trade-offs to Defend in Front of Interviewer:** `BCrypt` cost factor 10 (~65ms: 4x higher throughput per core, still strong for high-entropy passwords) vs `Argon2id` (memory-hard + CPU-hard, resists ASIC/GPU cracking best, requires careful memory bulkhead sizing so concurrent logins don't OOM the pod!).
  - **5. Senior Trap to Avoid:** Configuring `Argon2PasswordEncoder` with `64MB` memory cost per hash without a concurrency `Semaphore`—100 concurrent login requests will instantly allocate $100 \times 64\text{MB} = 6.4\text{GB}$ of native/heap memory and crash the Auth Server pod with `OOMKilled`!

---

### Q13. JWT Algorithm Confusion Attack (`alg: none` & `RS256` to `HS256` Public Key HMAC Exploit)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech | Security Engineering
- **The Real-World Scenario:** A security audit tests whether an attacker can download your Auth Server's public RSA key from `/oauth2/jwks`, change the JWT header from `{"alg": "RS256"}` to `{"alg": "HS256"}`, sign the forged admin JWT using the **public RSA key bytes as the HMAC secret**, and have your Resource Server accept it!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** In older or poorly configured JWT libraries, the verifier read the `alg` header **from the untrusted incoming JWT itself** to decide how to verify the signature. If the token said `HS256` (symmetric HMAC), the library took the configured verification key (the public RSA key string, which everyone in the world knows!) and used it as a symmetric HMAC secret!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Explicitly pin the expected asymmetric algorithm (`SignatureAlgorithm.RS256` or `ES256`) on `NimbusJwtDecoder.withJwkSetUri(uri).jwsAlgorithm(SignatureAlgorithm.RS256).build()`.
  - **3. Permanent Architectural / Code Fix:**
    1. Never trust the `alg` header dynamically without an explicit server-side algorithm allowlist (`jwsAlgorithms(algs -> algs.add(SignatureAlgorithm.ES256))`).
    2. In Nimbus / Spring Security 6, `NimbusJwtDecoder` binds `JWKSource` strictly to asymmetric `JWSVerificationKeySelector(JWSAlgorithm.RS256, jwkSource)`—verify that no custom `JwtDecoder` accepts both `SecretKey` and `PublicKey` interchangeably.
  - **4. Trade-offs to Defend in Front of Interviewer:** `ES256` (ECDSA P-256: shorter 64-byte signature, 3x faster signing, smaller JWT header size) vs `RS256` (RSA-2048: 256-byte signature, faster verification, universal legacy compatibility) vs `HS256` (symmetric shared secret—NEVER use across microservices because any compromised consumer service can forge tokens for all other services!).
  - **5. Senior Trap to Avoid:** Using symmetric `HS256` JWTs in a microservice architecture! With `HS256`, every microservice that *verifies* tokens holds the exact same secret key needed to *mint/forge* tokens! Always use asymmetric keys (`ES256` / `RS256` / `EdDSA`) where only the Auth Server holds the Private Key and Resource Servers only hold the Public Key.

---

### Q14. Spring Security 6 RequestMatcher Ordering Trap & Path Traversal Bypass (`AntPathRequestMatcher` vs `PathPatternRequestMatcher`)
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech | Tier-1 High-Scale
- **The Real-World Scenario:** A developer configures `SecurityFilterChain` with:
  ```java
  auth.requestMatchers("/api/v1/**").authenticated()
      .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
  ```
  In production, any regular logged-in customer (`ROLE_USER`) can access `/api/v1/admin/delete-user`!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Spring Security evaluates `requestMatchers` **strictly in top-to-bottom declaration order and stops at the FIRST match**! Because `/api/v1/admin/delete-user` matches the first rule `/api/v1/**`, Spring Security applies `.authenticated()`, sees the user is logged in, and **never evaluates the second rule** `/api/v1/admin/**`!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Reorder `SecurityFilterChain` matchers from **most specific to least specific** (`/api/v1/admin/**` first, `/api/v1/**` second, `anyRequest().denyAll()` or `.authenticated()` last).
  - **3. Permanent Architectural / Code Fix:**
    1. Always order URL matchers most-specific-first, ending with `.anyRequest().authenticated()`.
    2. Enforce **Defense-in-Depth Method Security (`@EnableMethodSecurity`)**: annotate admin controllers/methods with `@PreAuthorize("hasRole('ADMIN')")` so even if a URL matcher order is misconfigured in `SecurityFilterChain`, the AOP method guard blocks execution!
    3. Write `@WebMvcTest` integration tests asserting both `200 OK` for `ROLE_ADMIN` and `403 Forbidden` for `ROLE_USER` on every admin endpoint.
  - **4. Trade-offs to Defend in Front of Interviewer:** URL-level `SecurityFilterChain` rules (centralized overview, vulnerable to ordering bugs and path variations) vs Method-level `@PreAuthorize` (co-located with business logic, works even for internal/gRPC/Kafka invocations, requires both layers for defense-in-depth).
  - **5. Senior Trap to Avoid:** Relying solely on URL matchers without `@PreAuthorize`, or forgetting that in Spring Security 6, `ROLE_` prefix is automatically prepended by `hasRole("ADMIN")` (looks for `ROLE_ADMIN`), whereas `JwtAuthenticationConverter` maps `scope`/`scp` claims to `SCOPE_*` by default!

---

### Q15. `Scope` (`SCOPE_*`) vs `Role` (`ROLE_*`) Mapping Mismatch in Spring Security 6 OAuth2 Resource Server
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech | Enterprise SaaS
- **The Real-World Scenario:** Your JWT contains `"roles": ["ADMIN", "OPERATOR"]` and `"permissions": ["ledger:write"]`. You annotate your controller with `@PreAuthorize("hasRole('ADMIN')")`, but every request with a valid JWT fails with `403 Forbidden`!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Out of the box, Spring Security's default `JwtAuthenticationConverter` (via `JwtGrantedAuthoritiesConverter`) **only looks at the `scope` or `scp` claim** in the JWT and prefixes each value with **`SCOPE_`**! It completely ignores custom claims like `roles`, `authorities`, `groups`, or `permissions`, and never creates `ROLE_ADMIN`!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Register a custom `JwtAuthenticationConverter` bean that extracts `roles` (prefixing `ROLE_`) and `permissions` into the `Collection<GrantedAuthority>`.
  - **3. Permanent Architectural / Code Fix:**
    1. Define a standardized platform `JwtAuthenticationConverter` that maps both OAuth2 client scopes (`SCOPE_*` — what the *client app* is allowed to do) and user RBAC roles/permissions (`ROLE_*` / `PERM_*` — what the *human user* is allowed to do).
    2. Understand the architectural distinction:
       - **Scope (`SCOPE_payments:write`):** Delegated capability granted to the *OAuth2 Client Application* (e.g., third-party partner app vs first-party mobile app).
       - **Role / Permission (`ROLE_ADMIN`, `ledger:approve`):** Entitlement of the *Authenticated Principal*.
  - **4. Trade-offs to Defend in Front of Interviewer:** Conflating Scopes and Roles into one claim (breaks when third-party OAuth clients integrate—a user who is an `ADMIN` shouldn't grant a third-party "Budget Tracker App" full `ADMIN` rights!) vs Enforcing **Intersection of Client Scope $\cap$ User Permission** (`hasAuthority('SCOPE_payments:write') and hasAuthority('PERM_payments:approve')`).
  - **5. Senior Trap to Avoid:** Treating OAuth2 `scope` as a replacement for user RBAC roles—in OAuth2, `scope` bounds what the **Client** can do on behalf of the user, not who the user is!

---

### Q16. SSRF (Server-Side Request Forgery) via Webhook URL / Avatar Fetch Exposing Cloud Instance Metadata (IMDS)
- **Experience Focus:** Staff Engineer / Security Architect
- **Target Company Archetype:** FinTech | Cloud-Native SaaS
- **The Real-World Scenario:** Your merchant onboarding service lets merchants test their webhook URL (`POST /api/v1/webhooks/test {"url": "https://merchant.com/hook"}`). An attacker passes `"url": "http://169.254.169.254/latest/meta-data/iam/security-credentials/"` (or `"http://internal-ledger-service.prod.svc.cluster.local:8080/actuator/env"`) and steals cloud IAM credentials and internal service data!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Your Spring Boot `RestClient` executed an outbound HTTP request to a user-supplied URL without validating the resolved target IP address, allowing the attacker to use your pod as a proxy inside your private Kubernetes cluster and cloud VPC (**OWASP Top 10 SSRF**).
  - **2. Immediate Incident Mitigation (First 15 minutes):** Block RFC 1918 private IP ranges (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`), loopback (`127.0.0.0/8`, `::1`), and link-local cloud metadata (`169.254.169.254`) before connecting, and enforce IMDSv2 / disable node IMDS access from pods via Kubernetes `NetworkPolicy`.
  - **3. Permanent Architectural / Code Fix:**
    1. **DNS Rebinding-Safe Socket Resolver:** Do NOT just check the URL string with a regex (an attacker can register `evil.com` pointing to `169.254.169.254` or use DNS rebinding where the first DNS lookup returns a public IP and the second lookup inside `HttpClient` returns `127.0.0.1`!). Implement a custom `DnsResolver` inside Apache `HttpClient 5` / Netty that resolves the DNS once, verifies the resolved `InetAddress` is not private/loopback/link-local (`!addr.isSiteLocalAddress() && !addr.isLoopbackAddress() && !addr.isLinkLocalAddress()`), and connects directly to that verified IP!
    2. **Dedicated Egress Proxy (Smokescreen):** Route all untrusted customer webhook deliveries through an isolated egress proxy in a DMZ subnet with Kubernetes `NetworkPolicy` blocking all access to `10.0.0.0/8` and `169.254.169.254/32`.
  - **4. Trade-offs to Defend in Front of Interviewer:** Regex URL validation in application code (trivially bypassed via DNS rebinding, decimal/octal IP encoding like `http://2130706433/`, or HTTP `302` redirects!) vs Custom `DnsResolver` + Disabled HTTP Redirects + Isolated Egress Proxy with `NetworkPolicy` (100% immune to DNS rebinding and internal cluster probing).
  - **5. Senior Trap to Avoid:** Checking `InetAddress.getByName(host)` *before* calling `restClient.post().uri(url)`—because `restClient` performs a **second** DNS lookup when opening the socket (TOCTOU DNS Rebinding Attack), and following an HTTP `302 Redirect` from `https://attacker.com` to `http://169.254.169.254/` bypasses initial URL validation unless `.followRedirects(Redirect.NEVER)` is set!

---

### Q17. OAuth2 Authorization Code Interception & PKCE Enforcement in Public SPAs / Mobile Apps
- **Experience Focus:** Lead Engineer / Technical Architect
- **Target Company Archetype:** FinTech | Modern Product
- **The Real-World Scenario:** Your mobile app / React SPA uses OAuth2 Authorization Code flow. A malicious app on the user's phone registers the same custom URI scheme (`myapp://callback`), intercepts the `?code=xyz` redirect from the system browser, and exchanges it for the user's access token!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Public clients (SPAs and mobile apps) cannot safely hold a static `client_secret`. Without **PKCE (Proof Key for Code Exchange, RFC 7636)**, anyone who intercepts the short-lived `code` in the redirect URL can exchange it at `/oauth2/token`.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Enable `requireProofKey(true)` on `ClientSettings` in Spring Authorization Server and disallow plain `code_challenge_method=plain` (enforce `S256` only).
  - **3. Permanent Architectural / Code Fix:**
    1. Enforce **PKCE (`S256`)** for all clients (OAuth 2.1 mandates PKCE even for confidential clients!):
       - Client generates random `code_verifier` and sends `code_challenge = BASE64URL(SHA256(code_verifier))` to `/oauth2/authorize`.
       - When exchanging `code` at `/oauth2/token`, client must send the raw `code_verifier`. Even if a malicious app steals `code`, it does not know `code_verifier`!
    2. Use Universal Links / App Links (`https://auth.company.com/callback`) instead of custom URI schemes (`myapp://`), and use the **BFF Pattern** so the browser never handles tokens directly.
  - **4. Trade-offs to Defend in Front of Interviewer:** SPA handling OAuth2 + PKCE in browser memory (still exposes access token to in-page XSS attacks) vs **BFF (Backend-For-Frontend) Confidential Client + PKCE** (Gateway performs the OAuth2 + PKCE handshake, stores tokens in Redis, and gives the browser only an `HttpOnly` cookie—immune to both code interception and token theft via XSS).
  - **5. Senior Trap to Avoid:** Allowing `code_challenge_method=plain` in production—if `plain` is allowed, an attacker who can see the initial authorization request also sees the verifier. Enforce `S256` exclusively.

---

### Q18. Webhook Signature Verification Timing Attack & Replay Attack on Payment Callbacks
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech (Razorpay / Stripe / Adyen Integration)
- **The Real-World Scenario:** Your payment service exposes `POST /webhooks/stripe` to receive payment success notifications. A security review finds three vulnerabilities:
  1. You verify the HMAC signature using `expectedSig.equals(headerSig)`.
  2. You verify the signature against the re-serialized Jackson DTO (`objectMapper.writeValueAsBytes(dto)`) instead of the raw HTTP request bytes.
  3. An attacker captures a valid webhook payload and replays it 2 hours later.
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. `String.equals()` short-circuits on the **first mismatched byte**, leaking signature bytes via nanosecond timing side-channels.
    2. Deserializing JSON into a Java DTO and re-serializing changes key ordering and whitespace, breaking the HMAC signature when the upstream provider adds a new field or formats spaces differently!
    3. Without verifying the signed timestamp and storing a deduplication event ID, valid signed webhooks can be replayed indefinitely.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Read the raw `byte[]` body (`@RequestBody byte[] rawBody`), verify HMAC using `MessageDigest.isEqual(expectedBytes, actualBytes)`, and reject timestamps older than 5 minutes.
  - **3. Permanent Architectural / Code Fix:**
    1. **Raw Bytes Verification:** Compute `HMAC-SHA256(secret, timestamp + "." + rawBodyString)` before JSON deserialization.
    2. **Constant-Time Comparison:** Always use `MessageDigest.isEqual(a, b)` (never `String.equals` or `Arrays.equals`).
    3. **Replay Protection (Timestamp + Idempotency):** Reject if `|now - webhookTimestamp| > 300s`, AND insert `event_id` into a unique database table / Redis (`SET webhook:evt:{eventId} 1 NX EX 86400`) inside the transaction.
  - **4. Trade-offs to Defend in Front of Interviewer:** Timestamp window check alone (stateless, still allows replay *within* the 5-minute window!) vs Timestamp window (5m) + Redis/DB Event ID Deduplication (24h TTL: blocks replay within the 5m window AND blocks replays after 24h via timestamp expiry, so Redis only needs to store 5–10 minutes of event IDs!).
  - **5. Senior Trap to Avoid:** Caching `event_id` in Redis for only 24 hours *without* checking the signed timestamp—if you don't check the timestamp, an attacker can wait 25 hours until the Redis key expires and replay the webhook successfully! Combining a **5-minute timestamp check + 10-minute Redis `event_id` cache** guarantees 100% replay immunity forever with minimal memory!

---

### Q19. CSRF Vulnerability When Using `SameSite=Lax` or `SameSite=None` Session Cookies in BFF
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech | Modern Product
- **The Real-World Scenario:** You migrated from `localStorage` JWTs to an `HttpOnly` `APP_SESSION` cookie on your API Gateway BFF, and called `.csrf(AbstractHttpConfigurer::disable)` in Spring Security because "we are a REST API." During a security audit, the red team executes a Cross-Site Request Forgery (CSRF) attack from an external site that triggers a state-changing action!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Disabling CSRF is **only safe when authentication is sent via an explicit `Authorization: Bearer` header** (which browsers never attach automatically). The moment you use **Cookies** (`APP_SESSION`) for authentication, the browser automatically attaches that cookie on cross-origin form posts (`SameSite=None`) or top-level navigation requests!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Set `SameSite=Strict` on the `APP_SESSION` cookie, enforce strict `Origin` / `Referer` header validation on all `POST`/`PUT`/`DELETE`/`PATCH` requests, and re-enable CSRF token validation (`CookieCsrfTokenRepository` with `SpaCsrfTokenRequestHandler` in Spring Security 6).
  - **3. Permanent Architectural / Code Fix:**
    1. Configure Spring Security 6's **SPA CSRF Protection (`SpaCsrfTokenRequestHandler`)** + `SameSite=Strict` (or `Lax`) on `APP_SESSION`.
    2. Require a custom header (e.g., `X-Requested-With: XMLHttpRequest` or `Content-Type: application/json` with strict `Consumes` enforcement, as HTML `<form>` tags cannot set `Content-Type: application/json` or custom headers without triggering a CORS preflight block!).
  - **4. Trade-offs to Defend in Front of Interviewer:** Stateless Double-Submit Cookie Pattern (`CookieCsrfTokenRepository.withHttpOnlyFalse()`: works across stateless gateway pods without extra Redis state) vs Server-Side Synchronizer Token in Session (strongest binding to session, already available if BFF uses Spring Session Redis).
  - **5. Senior Trap to Avoid:** Blindly copying `.csrf(AbstractHttpConfigurer::disable)` into an API Gateway BFF that uses `APP_SESSION` cookies! Rule of thumb: **Bearer Header = CSRF not applicable; Cookie Auth = CSRF protection mandatory**.

---

### Q20. Open Redirect Vulnerability in OAuth2 Login `redirect_uri` & Post-Login `continue` Parameter
- **Experience Focus:** Lead Engineer / Security Architect
- **Target Company Archetype:** FinTech | Enterprise SaaS
- **The Real-World Scenario:** Your login page accepts `https://auth.company.com/login?redirect_uri=https://evil-phishing.com`. Because the URL starts with `https://auth.company.com`, corporate email filters trust the link. After the victim logs in, your Auth Server redirects them (along with session state or tokens) to `https://evil-phishing.com`, which shows a fake "Re-enter your 2FA code" screen!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. The custom post-login `AuthenticationSuccessHandler` called `response.sendRedirect(request.getParameter("redirect_uri"))` without validating that the target URL is a relative path or an exact match in a strict allowlist.
    2. Or in OAuth2 client registration, a wildcard redirect URI (`https://*.company.com/*`) was registered, allowing an attacker who takes over a forgotten subdomain or open redirector on a marketing site to steal authorization codes.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Restrict post-login redirects strictly to relative paths starting with a single `/` (and NOT starting with `//` or `/\`, as browsers treat `//evil.com` as protocol-relative redirect to `https://evil.com`!).
  - **3. Permanent Architectural / Code Fix:**
    1. In OAuth 2.1 / Spring Authorization Server: Enforce **exact string matching** on registered `redirect_uris` (zero wildcards `*` allowed).
    2. For internal post-login `returnTo` paths: Validate `url.startsWith("/") && !url.startsWith("//") && !url.startsWith("/\\")` and parse with `URI.create(url).getHost() == null`.
  - **4. Trade-offs to Defend in Front of Interviewer:** Exact `redirect_uri` matching per environment (requires registering staging/preview URLs explicitly via API, 100% immune to subdomain/wildcard hijacking) vs Wildcard matching (convenient for PR preview environments, banned by OAuth 2.1 security best current practice RFC 9700).
  - **5. Senior Trap to Avoid:** Checking only `if (redirectUrl.startsWith("/"))` to ensure a URL is relative—because `redirectUrl = "//evil.com"` starts with `"/"`, yet every browser interprets `Location: //evil.com` as an external redirect to `https://evil.com`!

---

### Q21. Sensitive PII & Bearer Tokens Leaking into Production JSON Logs & Distributed Traces
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech (PCI-DSS / RBI / GDPR Compliance)
- **The Real-World Scenario:** An compliance audit discovers that when downstream `RestClient` calls fail or debug logging is toggled, raw `Authorization: Bearer eyJ...` headers, PAN numbers, Aadhaar numbers, and CVVs are printed into Elasticsearch/Splunk logs and OpenTelemetry span attributes!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. Developers used Lombok `@Data` or Java `record` default `toString()` on request DTOs containing `password`, `cvv`, or `panNumber`, and logged `log.info("Processing request: {}", request)`.
    2. HTTP request/response logging filters dumped all headers without redacting `Authorization`, `Cookie`, and `Set-Cookie`.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Disable HTTP header/body debug logging via Actuator `/actuator/loggers`, purge the affected Elasticsearch indices, and rotate leaked tokens.
  - **3. Permanent Architectural / Code Fix:** Implement **3-Layer Automated PII Redaction**:
    1. **Domain / DTO Layer:** Wrap sensitive fields in a `Masked<T>` value type or custom `@Sensitive` Jackson/toString serializer whose `toString()` always returns `"***REDACTED***"`.
    2. **Jackson / Logback Layer:** Configure `LogstashEncoder` with `MaskingJsonGeneratorDecorator` (matching field names `password`, `token`, `secret`, `cvv`, `pan`, `authorization` and regex patterns for 16-digit PAN / JWT `eyJ[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+`).
    3. **OpenTelemetry Collector Layer:** Add the OTel `redaction` / `transform` processor before exporting spans to Tempo/Datadog.
  - **4. Trade-offs to Defend in Front of Interviewer:** DTO-level `toString()` override + `@JsonSerialize` masking (zero regex CPU cost at log time, prevents accidental leak at the source) vs Regex masking in Logback `MaskingJsonGeneratorDecorator` (catches raw strings inside exception messages, costs ~2% extra CPU on log serialization—combine both!).
  - **5. Senior Trap to Avoid:** Using raw Java `record LoginRequest(String username, String password) {}` without overriding `toString()`! Java `record` automatically generates a `toString()` that prints `LoginRequest[username=alice, password=PlaintextSecret123!]` whenever any framework or catch block logs the record!

---

### Q22. WebSocket / STOMP Authentication Bypass & Expired Token Persistence on Long-Lived Connections
- **Experience Focus:** Staff Engineer / Technical Architect
- **Target Company Archetype:** FinTech (Live Trading) | Modern Product (Chat)
- **The Real-World Scenario:** Two security holes are found in your Spring WebSocket (`STOMP`) service:
  1. Passing `?token=eyJ...` in the WebSocket handshake URL (`wss://api.company.com/ws?token=eyJ...`) leaks the JWT into every proxy/load-balancer access log and browser history.
  2. Once a user connects with a 15-minute JWT at 9:00 AM, the WebSocket TCP connection stays open for **8 hours**, continuing to send and receive messages at 4:00 PM even after the user's account was revoked at 9:30 AM!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. Browser `new WebSocket(url)` API does not allow setting custom `Authorization` HTTP headers during the initial HTTP Upgrade handshake (unless using cookies or STOMP `CONNECT` frame headers). Putting tokens in URL query parameters leaks them into Kubernetes Ingress, CloudFront, and Envoy access logs.
    2. HTTP security filters (`SecurityFilterChain`) only run **once** during the initial HTTP `101 Switching Protocols` handshake; subsequent STOMP frames (`SEND`, `SUBSCRIBE`) flow over the raw upgraded TCP socket without hitting Servlet filters!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Authenticate via the BFF `HttpOnly` `APP_SESSION` cookie on handshake (or STOMP `CONNECT` frame `Authorization` header inside a `ChannelInterceptor`), and validate token expiration on every inbound `SEND`/`SUBSCRIBE` frame.
  - **3. Permanent Architectural / Code Fix:**
    1. **Handshake / Connect Auth:** Use the BFF `HttpOnly` cookie on handshake OR validate the `Authorization` header inside `ChannelInterceptor.preSend()` when `StompCommand.CONNECT` arrives (never put JWTs in URL query strings!).
    2. **Per-Frame Expiration & Revocation Check:** In `ChannelInterceptor.preSend()` for `SEND` and `SUBSCRIBE` frames, check `if (Instant.now().isAfter(jwt.getExpiresAt()))` (supporting a client `TOKEN_REFRESH` STOMP control message to update the session principal without dropping the TCP socket) and subscribe to Redis Pub/Sub `user:revoked` events to immediately `session.close(CloseStatus.POLICY_VIOLATION)` when a user is banned!
  - **4. Trade-offs to Defend in Front of Interviewer:** Disconnecting WebSocket every 15 minutes on JWT expiry (causes reconnect storms) vs In-band STOMP `REFRESH_TOKEN` frame + Redis Revocation Pub/Sub (keeps TCP connection warm while enforcing continuous authorization).
  - **5. Senior Trap to Avoid:** Passing JWTs in `?access_token=...` query parameters for WebSockets or SSE—URL query strings are logged in cleartext across every CDN, ALB, Ingress controller, and browser history!

---

## 🚀 Section 2: [P1 - Architectural Trade-offs & Deep Design] (Q23–Q34)

### Q23. API Gateway BFF Pattern (`Opaque Cookie` $\rightarrow$ `Internal JWT`) vs Direct SPA-to-Microservice JWT Architecture
- **Experience Focus:** Principal Architect / Staff Engineer
- **Target Company Archetype:** FinTech | Tier-1 High-Scale
- **The Real-World Scenario:** You are designing the authentication architecture for a flagship financial super-app (Web SPA + Mobile + 20 Backend Microservices). Should the browser hold the OAuth2 Access/Refresh JWTs directly, or should the Spring Cloud Gateway act as an OAuth2 BFF holding tokens in Redis and issuing an opaque `APP_SESSION` cookie?
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Storing raw JWT Access & Refresh tokens in a browser SPA (`localStorage` or `sessionStorage`) exposes tokens to **any XSS attack** (a single compromised npm package can exfiltrate the tokens and impersonate the user from anywhere in the world). On the other hand, internal microservices want self-contained **JWTs** so they don't all hammer a central session database on every internal hop.
  - **2. Immediate Incident Mitigation (First 15 minutes):** N/A (Core Security Architecture).
  - **3. Permanent Architectural / Code Fix:** Combine both into the **Token Relay / Phantom Token BFF Architecture**:
    1. **External Edge (Browser $\leftrightarrow$ API Gateway):** Browser receives only an opaque `APP_SESSION` cookie (`HttpOnly; Secure; SameSite=Strict`). Zero tokens ever touch JavaScript!
    2. **Gateway Boundary (`Spring Cloud Gateway` + Redis):** Gateway looks up `APP_SESSION` in Redis, retrieves the OAuth2 Access Token (transparently refreshing it via the Refresh Token in Redis if expired), and attaches `Authorization: Bearer <Access-JWT>` to the downstream request.
    3. **Internal Mesh (Gateway $\rightarrow$ Microservices $\rightarrow$ Microservices):** Downstream Resource Servers verify the JWT signature locally in memory via cached JWKS in `<0.1ms` with zero Redis coupling!
  - **4. Trade-offs to Defend in Front of Interviewer:** Pure Stateless JWT to Browser (zero Gateway Redis state, vulnerable to XSS token theft, impossible to revoke instantly, exposes internal claims structure to public users) vs Gateway Opaque Cookie BFF + Internal JWT Relay (100% XSS token theft immunity, instant session revocation at Gateway, zero internal claim leakage, costs 1 fast `<1ms` Redis lookup at the edge Gateway).
  - **5. Senior Trap to Avoid:** Sending the opaque `APP_SESSION` cookie all the way to 20 downstream microservices and having all 20 microservices connect to the same central Session Redis cluster—that creates a massive distributed monolith coupling all 20 services to one Redis schema! Translate `Opaque Cookie -> JWT` at the Gateway boundary.

---

### Q24. Zero-Trust Service-to-Service Security: Service Mesh mTLS (SPIFFE/SPIRE) + User JWT vs OAuth2 Client Credentials
- **Experience Focus:** Principal Architect / Staff Engineer
- **Target Company Archetype:** FinTech (Morgan Stanley / Stripe / Uber) | Tier-1 High-Scale
- **The Real-World Scenario:** When `Order-Service` calls `Payment-Service` during a user checkout, how should `Payment-Service` verify **both** (1) that the network caller is genuinely `Order-Service` (and not a rogue `Analytics-Service` pod), AND (2) that an authenticated end-user actually authorized this specific payment?
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Relying on *only* the User JWT allows any compromised microservice that receives the User JWT to call `Payment-Service` (lateral movement). Relying on *only* mTLS or OAuth2 `client_credentials` (`Order-Service` identity) loses the end-user's identity and allows `Order-Service` to move money for arbitrary users without proof of user presence!
  - **2. Immediate Incident Mitigation (First 15 minutes):** N/A (Deep Zero-Trust Design).
  - **3. Permanent Architectural / Code Fix:** Implement **Dual-Identity Zero-Trust Enforcement (Caller Transport Identity + User Subject Identity)**:
    1. **Transport Layer (Who is the calling machine?):** Istio / Linkerd enforces **Strict mTLS** using short-lived X.509 workload certificates with SPIFFE IDs (`spiffe://prod.cluster.local/ns/checkout/sa/order-service-sa`). An Istio `AuthorizationPolicy` on `Payment-Service` rejects any TCP connection not originating from `order-service-sa`.
    2. **Application Layer (Who is the human user & what did they authorize?):** `Order-Service` propagates the user's JWT (or exchanges it via **RFC 8693 OAuth2 Token Exchange** for a token with `aud = payment-service`, `sub = user-123`, `act = order-service`).
  - **4. Trade-offs to Defend in Front of Interviewer:** Sidecar Service Mesh mTLS (zero Java SSL keystore rotation code, automatic 24h cert rotation via Envoy/SPIRE, `<1ms` sidecar hop) vs Application-level OAuth2 `client_credentials` + User Token in dual headers (works across non-K8s/multi-cloud boundaries without a mesh).
  - **5. Senior Trap to Avoid:** Replacing the User's JWT with a service-wide `client_credentials` token when calling downstream services! Once you drop the User's JWT and use a god-mode service token, the downstream service loses `sub` (User ID) and `tenant_id` cryptographic proof, re-opening BOLA/IDOR bugs if a developer passes the wrong `userId` parameter!

---

### Q25. Designing Fine-Grained Authorization at Scale: RBAC vs ABAC vs ReBAC (Google Zanzibar / OpenFGA / SpiceDB)
- **Experience Focus:** Principal Architect / Staff Engineer
- **Target Company Archetype:** Enterprise SaaS | Modern Product | FinTech
- **The Real-World Scenario:** You are building a collaborative B2B platform where a user can access a `Document` if: (a) they own it, (b) it was shared directly with them, (c) it is inside a `Folder` shared with their `Team`, or (d) they are an `OrgAdmin` of the parent `Organization`. Simple `@PreAuthorize("hasRole('ADMIN')")` cannot express this hierarchy. How do you architect this?
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    - **RBAC (Role-Based):** Coarse (`Admin`, `Editor`, `Viewer`). Fails when permissions depend on object hierarchies or per-resource sharing.
    - **ABAC (Attribute-Based / OPA Rego):** Evaluates attributes (`user.department == resource.department && time < 18:00`), great for policy rules, hard to answer "List all documents Alice can view" (Reverse Index Problem).
    - **ReBAC (Relationship-Based / Google Zanzibar — OpenFGA / SpiceDB):** Models authorization as a directed graph of tuples (`user:alice#member@team:eng`, `team:eng#viewer@folder:q4`, `folder:q4#parent@doc:budget`).
  - **2. Immediate Incident Mitigation (First 15 minutes):** N/A (System Design).
  - **3. Permanent Architectural / Code Fix:**
    1. Keep **coarse authentication & tenant roles (RBAC)** in Spring Security (`SecurityFilterChain`).
    2. For hierarchical resource sharing (`Org -> Team -> Folder -> Document`), integrate a **Zanzibar-inspired ReBAC engine (OpenFGA / SpiceDB)** or a custom Spring `PermissionEvaluator` backed by materialized permission tuples.
    3. Pass a **Zookie / Consistency Token** on write operations to solve the **New Enemy Problem** (ensuring that if Alice is removed from a group and the document is updated 1ms later, a stale cache replica cannot allow Alice to read the secret update!).
  - **4. Trade-offs to Defend in Front of Interviewer:** Evaluating ownership via SQL `JOIN`s in every microservice DB (fast for 1 service, impossible when `Teams` live in `User-Service` and `Documents` live in `Doc-Service`) vs Centralized ReBAC (`OpenFGA`/`SpiceDB`: decouples relationship graph across microservices, requires replicating relationship tuples via Outbox/Kafka).
  - **5. Senior Trap to Avoid:** Not knowing the **"New Enemy Problem"** in distributed authorization caches—if Alice's access to a folder is revoked at $T=1$, and Bob posts a confidential file to that folder at $T=2$, a permission cache with a 60s TTL would let Alice read Bob's new file unless revocation invalidates or versions the consistency token (Zookie)!

---

### Q26.Cryptographic Key Management & Field-Level Encryption (Envelope Encryption with KMS / Key Vault)
- **Experience Focus:** Staff Engineer / Principal Architect
- **Target Company Archetype:** FinTech (PCI-DSS / Banking)
- **The Real-World Scenario:** You must encrypt 50 million customer PAN / Bank Account numbers in PostgreSQL (`AES-256-GCM`) AND STILL support exact-match lookup `findByPanNumber(String pan)` without decrypting all 50 million rows in memory, while rotating encryption keys annually without a 12-hour database downtime!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. Calling AWS KMS / Azure Key Vault over the network for every row encryption/decryption adds 15ms network latency per row and hits cloud KMS rate limits (`10,000 RPS` quota) immediately.
    2. `AES-256-GCM` uses a random 12-byte Initialization Vector (IV/Nonce) for every encryption, so encrypting `"ABCDE1234F"` twice produces **two completely different ciphertexts**, making SQL `WHERE pan_encrypted = ?` impossible!
  - **2. Immediate Incident Mitigation (First 15 minutes):** N/A (Architectural Design).
  - **3. Permanent Architectural / Code Fix:**
    1. **Envelope Encryption (`KEK` + `DEK`):** Keep the Master Key (**KEK — Key Encryption Key**) inside AWS KMS / Azure Key Vault HSM. Generate a local **DEK (Data Encryption Key)** encrypted by the KEK, cache the decrypted DEK in JVM memory, and perform `AES-256-GCM` in-memory in `<2 microseconds`! Prefix each ciphertext with a 2-byte `key_version` (`v2:iv:ciphertext:tag`) so new writes use `v2` immediately while a background job lazily re-encrypts `v1` rows with zero downtime!
    2. **Blind Indexing for Exact Search:** Store a second column `pan_blind_index` containing `HMAC-SHA256(blind_index_secret_key, normalizedPan)` (truncated or full 32 bytes). Because HMAC is deterministic and keyed with a secret key (not a plain SHA-256 hash which can be rainbow-table cracked for 10-character PANs!), you can index `pan_blind_index` in PostgreSQL B-Tree and run `WHERE pan_blind_index = :hmac` in 1ms!
  - **4. Trade-offs to Defend in Front of Interviewer:** Plain `SHA-256(pan)` for search index (BROKEN—PAN format `[A-Z]{5}[0-9]{4}[A-Z]` has only ~175 billion combinations; a GPU cracks an unsalted SHA-256 of all PANs in minutes!) vs **`HMAC-SHA256(secretPepperFromKMS, pan)` Blind Index** (cannot be brute-forced from a stolen DB dump without the KMS secret key).
  - **5. Senior Trap to Avoid:** Using `AES-ECB` or a static hardcoded IV in `AES-GCM` to make ciphertexts searchable—reusing a Nonce/IV with the same key in `AES-GCM` **catastrophically breaks GCM cryptography**, allowing an attacker to XOR two ciphertexts and recover the authentication key (`GHASH`) and plaintext! Always use random 12-byte IVs for `AES-GCM` + a separate `HMAC-SHA256` column for blind index lookups.

---

### Q27. OAuth2 Authorization Server High Availability, Session Affinity & Authorization Code State Storage
- **Experience Focus:** Staff Engineer / Technical Architect
- **Target Company Archetype:** FinTech | Tier-1 High-Scale
- **The Real-World Scenario:** You deploy 4 pods of **Spring Authorization Server** behind a round-robin Kubernetes Service. Users randomly fail login at step 2 (`/oauth2/authorize` succeeds on Pod 1, but when the browser POSTs `/oauth2/token` with `?code=abc`, it lands on Pod 2 and fails with `invalid_grant`!).
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** The default sample configuration of Spring Authorization Server uses `InMemoryOAuth2AuthorizationService` (which stores issued authorization codes in a local JVM `ConcurrentHashMap`) and local HTTP sessions (`JSESSIONID` in Pod 1's memory)! When Pod 2 receives the `/oauth2/token` code exchange request, Pod 2's `ConcurrentHashMap` has never heard of `code=abc`!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Enable sticky sessions (`ClientIP` / cookie affinity) on the Ingress controller as an immediate 5-minute stopgap while deploying distributed state backing.
  - **3. Permanent Architectural / Code Fix:** Make all Spring Authorization Server pods **100% stateless at the JVM level**:
    1. Replace `InMemoryOAuth2AuthorizationService` with `JdbcOAuth2AuthorizationService` (or a custom **Redis-backed `OAuth2AuthorizationService`** using TTLs for ephemeral 5-minute authorization codes).
    2. Use `@EnableRedisHttpSession` (Spring Session Redis) for the login flow's temporary `JSESSIONID` security context.
    3. Load the JWK signing keyset from **Azure Key Vault / AWS KMS / Kubernetes Secret** so all 4 pods sign tokens with the exact same active keypair (`kid`) and serve the exact same `/oauth2/jwks` response!
  - **4. Trade-offs to Defend in Front of Interviewer:** `JdbcOAuth2AuthorizationService` (durable in Postgres, creates heavy write/delete churn on Postgres for short-lived 60-second authorization codes and access tokens) vs **Redis-backed `OAuth2AuthorizationService` for ephemeral codes + Stateless JWTs** (automatic Redis `EXPIRE` cleanup with zero Postgres vacuum bloat).
  - **5. Senior Trap to Avoid:** Generating an in-memory `KeyPairGenerator.getInstance("RSA")` at startup inside `@Bean JWKSource`—if you have 4 Auth Server pods and each generates a random RSA keypair in memory at startup, Pod 1 signs a token whose public key only exists on Pod 1's `/oauth2/jwks`, causing a 75% signature verification failure rate across the cluster!

---

### Q28. Securing External Partner APIs: mTLS + OAuth2 Client Credentials vs HMAC Request Signing vs API Keys
- **Experience Focus:** Principal Architect / Staff Engineer
- **Target Company Archetype:** FinTech (Payment Gateway / Banking BaaS)
- **The Real-World Scenario:** You are designing the external B2B API for enterprise merchants to initiate payouts ($10M+/day). Product wants simple `X-Api-Key` headers for developer experience; Security wants mutual TLS (mTLS) + asymmetric payload signing. How do you design a tiered B2B API security architecture?
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** A static `X-Api-Key` sent over standard TLS proves who holds the bearer string, but if leaked in a CI log or proxy, can be replayed from anywhere, and does not protect against payload tampering if TLS terminates at a shared CDN/proxy.
  - **2. Immediate Incident Mitigation (First 15 minutes):** N/A (Architecture Design).
  - **3. Permanent Architectural / Code Fix:** Design a **Defense-in-Depth B2B Partner Authentication Stack**:
    1. **Transport Binding (mTLS / RFC 8705 Certificate-Bound OAuth2 Tokens):** Terminate mTLS at the API Gateway, extract the client certificate thumbprint (`x5t#S256`), and verify it matches the `cnf` claim inside the OAuth2 access token! Even if an attacker steals the OAuth2 bearer token, it is **useless without the partner's private mTLS X.509 key**!
    2. **Payload Non-Repudiation (Detached JWS Signature / Digest Header):** For money-movement endpoints (`POST /v1/payouts`), require a `X-Signature` header (`RSASSA-PSS` or `ECDSA` signature over `timestamp + method + path +Idempotency-Key + SHA256(body)`) using the merchant's uploaded public key. This provides **cryptographic non-repudiation** (even an internal rogue admin at your company cannot forge a merchant payout request!).
  - **4. Trade-offs to Defend in Front of Interviewer:** Simple API Key / OAuth2 Bearer (fast developer onboarding, suitable for read-only/sandbox APIs) vs RFC 8705 mTLS Sender-Constrained Token + Asymmetric JWS Request Signing (mandatory for high-value payouts/banking, provides cryptographic non-repudiation and replay immunity).
  - **5. Senior Trap to Avoid:** Using symmetric HMAC (`HMAC-SHA256`) for legal non-repudiation with enterprise banking partners—because with symmetric HMAC, *your own server* also holds the shared secret, so the merchant can claim in court "Your server forged that $1M payout request!" Asymmetric `ECDSA`/`RSA-PSS` request signing ensures **only** the merchant holds the private key.

---

### Q29. Preventing Mass Assignment / Over-Posting Vulnerabilities in Spring Boot 3 REST Controllers
- **Experience Focus:** Lead Engineer / Staff Engineer
- **Target Company Archetype:** FinTech | Modern Product
- **The Real-World Scenario:** A user profile update endpoint `PATCH /api/v1/users/me` binds the JSON body to a `UserDto` or JPA `UserEntity` and maps it onto the persisted user. An attacker sends `{"displayName": "Bob", "role": "ADMIN", "kycVerified": true, "walletBalance": 999999}` and elevates their own privileges!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** This is **OWASP API3:2023 Broken Object Property Level Authorization (BOPLA / Mass Assignment)**. Reusing the same DTO (or worse, binding a JPA `@Entity` or MapStruct universal updater) for both read responses and write requests allows Jackson + MapStruct (`BeanUtils.copyProperties`) to overwrite internal security/financial fields that the user should never control.
  - **2. Immediate Incident Mitigation (First 15 minutes):** Replace the shared DTO in `@RequestBody` with a dedicated, narrowly scoped command record: `public record UpdateProfileRequest(@NotBlank @Size(max = 60) String displayName, String avatarUrl) {}`.
  - **3. Permanent Architectural / Code Fix:**
    1. **Strict Command Records per Endpoint:** Never reuse a Read/Response DTO or Domain Model as a `@RequestBody` input. Create a purpose-built Java `record` containing **strictly the exact fields mutable by that specific actor in that specific use case**.
    2. **Ban `BeanUtils.copyProperties`:** Enforce an ArchUnit rule banning `org.springframework.beans.BeanUtils.copyProperties` (which blindly copies matching field names via reflection) in favor of explicit compile-time MapStruct mappers with `@Mapping(target = "role", ignore = true)` or domain behavior methods (`user.updateProfile(cmd.displayName(), cmd.avatarUrl())`).
  - **4. Trade-offs to Defend in Front of Interviewer:** `@JsonIgnore` / `@JsonProperty(access = Access.READ_ONLY)` on shared DTOs (fragile: one forgotten annotation on a newly added field causes a critical vulnerability) vs Dedicated Command `record` per write use case (physically impossible to mass-assign fields that do not exist on the record!).
  - **5. Senior Trap to Avoid:** Using `BeanUtils.copyProperties(sourceDto, targetEntity)` in a service layer—it bypasses domain invariant validation and silently overwrites any new field added to both classes in the future.

---

### Q30. Step-Up Authentication & Human-in-the-Loop (HITL) Cryptographic Approval Tokens for High-Risk Actions
- **Experience Focus:** Staff Engineer / Principal Architect
- **Target Company Archetype:** FinTech (Banking / Wealth / Agentic AI)
- **The Real-World Scenario:** A logged-in user (or an autonomous AI Agent acting on behalf of a user) wants to execute a high-risk operation: wiring $50,000, adding a new beneficiary, or deleting a production cluster. Standard session auth (`Bearer` token) is already present, but you must enforce fresh MFA / Biometric / Human confirmation bound to the **exact transaction parameters**.
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Simply checking a boolean flag `mfa_verified: true` in the user's session is vulnerable to **Transaction Manipulation / Replay**: an attacker or hallucinating AI agent can pass a step-up challenge once for a $10 transfer and then execute a $50,000 transfer to a different account during the 5-minute MFA window!
  - **2. Immediate Incident Mitigation (First 15 minutes):** N/A (Architectural Design).
  - **3. Permanent Architectural / Code Fix:** Implement **RFC 9470 OAuth 2.0 Step-Up Authentication + Dynamic Linking (PSD2 / FIDO2 Transaction Confirmation)**:
    1. When `POST /v1/transfers` is called above the risk threshold without a transaction-bound step-up token, return `401/403` with `WWW-Authenticate: Bearer error="insufficient_user_authentication", acr_values="phr", max_age=60` and create a pending `challengeId` hashing `{amount, currency, recipientIban, nonce}`.
    2. When the user completes Passkey/OTP step-up, the Auth Server issues a single-use, 60-second **Transaction Approval Token (`txn_hash = SHA256(amount + currency + recipientIban)`)**.
    3. `TransferService` verifies both the signature of the Approval Token AND that `token.txn_hash == SHA256(request.amount + request.currency + request.recipientIban)` before committing!
  - **4. Trade-offs to Defend in Front of Interviewer:** Session-level `acr=mfa` check (reusable for 5 minutes across any action) vs **Dynamic Transaction-Linked Approval Token (`txn_hash`)** (cryptographically binds the human's biometric/OTP signature to the exact recipient and dollar amount—mandatory under PSD2 SCA and for Agentic AI HITL guardrails).
  - **5. Senior Trap to Avoid:** Storing a simple boolean `isOtpVerified = true` in Redis keyed only by `userId`—which allows an attacker to initiate a legitimate ₹10 transfer to trigger the user's OTP, and race a ₹10,00,000 transfer the millisecond the boolean flips to `true`!

---

### Q31. Spring Security FilterChain Performance Optimization Under 50,000 RPS (Stateless Microservices)
- **Experience Focus:** Staff Engineer / Technical Architect
- **Target Company Archetype:** Tier-1 High-Scale
- **The Real-World Scenario:** CPU profiling (`async-profiler`) of your internal high-throughput microservice (50,000 RPS) reveals 18% of CPU time is spent inside Spring Security's default filter chain, HTTP session lookup, and RSA signature verification on every single request. How do you optimize `SecurityFilterChain` for maximum throughput without weakening security?
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):**
    1. Even in a stateless REST API, if you don't disable unused default filters (`RequestCacheAwareFilter`, `LogoutFilter`, `AnonymousAuthenticationFilter`, session creation), Spring executes 12–15 filters per request.
    2. If the same client sends 1,000 requests/second with the **exact same JWT**, `NimbusJwtDecoder` parses the Base64 JSON and runs heavy RSA/ECDSA cryptographic signature math 1,000 times per second on the exact same string!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Enable a short-lived local Caffeine cache around `JwtDecoder` (caching `SHA256(rawJwtString) -> verified Jwt` for `min(60s, token.exp - now)`).
  - **3. Permanent Architectural / Code Fix:**
    1. **Cache Verified `Jwt` Instances in Caffeine:** Wrap `NimbusJwtDecoder` with a bounded Caffeine cache (`maximumSize(10_000)`, `expireAfterWrite(30, SECONDS)`), still checking `exp` and revocation denylist on every call, eliminating 99% of redundant RSA/ECDSA cryptographic CPU cycles and JSON parsing allocations!
    2. **Slim Stateless FilterChain:** Set `.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))`, `.requestCache(RequestCacheConfigurer::disable)`, `.csrf(AbstractHttpConfigurer::disable)`, `.formLogin(AbstractHttpConfigurer::disable)`, `.httpBasic(AbstractHttpConfigurer::disable)`, `.logout(AbstractHttpConfigurer::disable)`.
  - **4. Trade-offs to Defend in Front of Interviewer:** Verifying RSA/ECDSA signature on every request (zero memory overhead, ~50–150 microseconds CPU per request = 5–7 CPU cores at 50k RPS!) vs Caching verified `Jwt` object in Caffeine keyed by token SHA-256 hash (`<50 nanoseconds` lookup, saves 95% of auth CPU, requires checking `Instant.now().isBefore(jwt.getExpiresAt())` on cache hit).
  - **5. Senior Trap to Avoid:** Caching the raw JWT string Directly as the `Map` key without bounding length or hashing, or forgetting to validate `jwt.getExpiresAt()` when returning a cached `Jwt` instance!

---

### Q32. Securing Asynchronous Messaging (Kafka Event Payloads & Consumer Authorization Context)
- **Experience Focus:** Staff Engineer / Principal Architect
- **Target Company Archetype:** FinTech | Tier-1 High-Scale
- **The Real-World Scenario:** On synchronous HTTP paths, Spring Security checks `@PreAuthorize` and tenant isolation. However, when commands/events flow asynchronously over **Apache Kafka**, consumers run with zero `SecurityContext`, allowing a poisoned or cross-tenant Kafka message to execute privileged service methods without tenant or role validation!
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** Kafka consumers run on background `KafkaMessageListenerContainer` threads outside `SecurityFilterChain`. If service methods rely on `TenantContext` or `SecurityContextHolder` (or if any producer on the shared Kafka cluster can publish to `payment-commands` topic), the async backdoor bypasses all HTTP security controls!
  - **2. Immediate Incident Mitigation (First 15 minutes):** Enforce Kafka ACLs / mTLS per topic (only `order-service` principal can `WRITE` to `order-events`) and populate `TenantContext` / `SecurityContext` inside a Spring Kafka `RecordInterceptor`.
  - **3. Permanent Architectural / Code Fix:**
    1. **Infrastructure ACLs:** Configure Kafka mTLS / SASL + Topic ACLs so each microservice has least-privilege read/write access to strictly its own topics.
    2. **Cryptographic Event Provenance / Signed Claims:** Propagate a compact signed security header (or HMAC/JWS envelope containing `tenantId`, `actorId`, `scopes`, `issuedAt`) in Kafka Record Headers.
    3. **Spring Kafka `RecordInterceptor`:** Verify the header signature before the `@KafkaListener` method runs, bind the verified `Authentication` and `TenantId` to `SecurityContextHolder` / `ScopedValue`, and clear it in `afterRecord()`!
  - **4. Trade-offs to Defend in Front of Interviewer:** Passing full user JWT in Kafka message headers (fails when the consumer processes a delayed message or DLT retry 30 minutes later after the 15-minute JWT has expired!) vs **Signed Event Provenance Header + Broker mTLS ACLs** (verifies who emitted the event and which tenant it belongs to, without failing on DLT replay due to access-token TTL expiry).
  - **5. Senior Trap to Avoid:** Putting a 15-minute user Access JWT into a Kafka message and validating its `exp` timestamp in the consumer—the moment a consumer lags by 16 minutes or replays a Dead Letter Topic (`DLT`) message the next morning, 100% of messages fail with `JwtExpiredException`! Validate `iat` (issued-at) + producer signature + tenant binding for async events, not short-lived HTTP `exp`.

---

### Q33. Multi-Region Active-Active OAuth2 Token Validation & Session Replication Architecture
- **Experience Focus:** Principal Architect / Staff Engineer
- **Target Company Archetype:** Tier-1 High-Scale | Global FinTech
- **The Real-World Scenario:** Your platform runs Active-Active across `ap-south-1` (Mumbai) and `ap-southeast-1` (Singapore). A user logs in through Mumbai, and 2 seconds later DNS / Global Load Balancer routes their next request to Singapore. How do you ensure seamless authentication and instant revocation across both regions even if the cross-region WAN link lags by 150ms?
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** If sessions only live in Mumbai's local Redis, Singapore returns `401 Unauthorized`. Conversely, if Singapore makes a synchronous 150ms cross-region call to Mumbai's Auth Server or Redis on every HTTP request, every request in Singapore suffers a 150ms latency penalty and fails if Mumbai goes down!
  - **2. Immediate Incident Mitigation (First 15 minutes):** N/A (Multi-Region Architecture).
  - **3. Permanent Architectural / Code Fix:**
    1. **Federated Regional JWKS:** Deploy Auth Server instances in both Mumbai and Singapore. Replicate public JWKS keys across both regions (each region signs with its own regional `kid`, e.g., `mum-2026-11` and `sin-2026-11`, while the global `/oauth2/jwks` endpoint exposes **both** public keys!). Any region can verify tokens minted by any other region locally in 0ms!
    2. **Active-Active Geo-Replicated Redis (Redis Enterprise CRDT / Azure Cache for Redis Enterprise Active Geo-Replication):** Replicate BFF session state and the Revocation Denylist asynchronously across regions (`~50–100ms` replication lag), with regional stickiness at Cloudflare/FrontDoor so 99.9% of a user's traffic stays in their home region unless a regional failover occurs.
  - **4. Trade-offs to Defend in Front of Interviewer:** Global Single-Issuer vs Regional Issuers (`auth.in.company.com`, `auth.sg.company.com`) with Multi-Tenant `JwtIssuerAuthenticationManagerResolver` in Spring Security 6 (isolates regional blast radius so a compromised key in one region can be revoked independently!).
  - **5. Senior Trap to Avoid:** Sharing the exact same **Private Signing Key** across multiple regions and cloud accounts—instead, give each region its own Private Signing Key (`kid=region-a`, `kid=region-b`) and publish the union of all **Public Keys** in the shared JWKS bundle!

---

### Q34. Passwordless WebAuthn / Passkeys (FIDO2) & Phishing-Resistant Authentication in Spring Security 6.3+
- **Experience Focus:** Staff Engineer / Technical Architect
- **Target Company Archetype:** FinTech | Modern Product
- **The Real-World Scenario:** SMS OTPs are being intercepted via SIM-swapping and real-time reverse-proxy phishing kits (`Evilginx`). Leadership asks you to design a phishing-resistant Passkey (WebAuthn / FIDO2) authentication flow using Spring Security 6.3/6.4+. Why is WebAuthn mathematically immune to reverse-proxy phishing whereas TOTP / SMS OTP is not?
- **Answer in Simple Words:**
  - **1. Root Cause Analysis (What actually happened?):** With SMS OTP or Google Authenticator 6-digit TOTP, a user tricked into visiting `https://login-company.evil.com` types their 6-digit code, and the attacker's reverse proxy simply forwards those 6 digits to the real `https://login.company.com`. The server cannot tell that the user typed the code into a fake domain!
  - **2. Immediate Incident Mitigation (First 15 minutes):** N/A (Security Architecture Upgrade).
  - **3. Permanent Architectural / Code Fix:** Implement **FIDO2 / WebAuthn Passkeys**:
    1. During registration, the user's device Secure Enclave / TPM generates an asymmetric keypair (`ES256`/`EdDSA`) bound specifically to the **Relying Party ID (`rpId = company.com`)** and stores the Public Key + Credential ID + `signCount` on your server.
    2. During login/step-up, the server generates a random single-use `challenge` (stored in Redis for 120s).
    3. The browser/OS hardware signs `clientDataJSON` (which contains the **exact browser origin** `https://login.company.com` + `challenge` + TLS binding) using the hardware Private Key.
    4. Even if `evil.com` proxies the challenge, the browser sees the origin is `https://evil.com`, refuses to find a key for `company.com` (or signs `origin: https://evil.com`), which your Spring Security `WebAuthnAuthenticationProvider` rejects immediately!
  - **4. Trade-offs to Defend in Front of Interviewer:** Device-bound Passkeys (highest security, monotonic `signCount` detects cloned hardware keys, requires recovery flow if phone is lost) vs Synced Multi-Device Passkeys (iCloud Keychain / Google Password Manager: seamless UX across devices, `signCount` is `0`, relies on cloud account security).
  - **5. Senior Trap to Avoid:** Generating the WebAuthn `challenge` on the client side or failing to consume/delete the `challenge` from Redis immediately upon first verification—which would allow an attacker to replay a captured WebAuthn assertion signature!
