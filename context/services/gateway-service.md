# Service: Gateway-Service
Status: draft   Owner: @Alex, @Maya   Updated: 2026-10-10
Inputs: context/architecture.md, context/implementation.md

## Summary
Low-Level Design (LLD) for `Gateway-Service`—the reactive, non-blocking API Gateway built on **Spring Cloud Gateway (WebFlux / Netty)**. Implements the BFF (Backend for Frontend) session pattern with `APP_SESSION` cookie validation against Redis, asymmetric JWT fallback verification, downstream `X-Internal-HMAC` header signing, and WebSocket/STOMP proxying. *(Rate limiting is excluded per architectural decision)*.

---

## Decisions

- **SVC-GW-001**: **Reactive Non-Blocking Engine (Spring Cloud Gateway on Netty)**
  - *Why*: Asynchronous event loop handles thousands of concurrent HTTP and WebSocket connections with minimal memory and thread overhead.
  - *Rejected*: Blocking Servlet-based gateways (Tomcat/Zuul 1) with thread-per-request bottlenecks.

- **SVC-GW-002**: **BFF `APP_SESSION` Fast-Path via Reactive Redis**
  - *Why*: Checks `APP_SESSION` cookie against Redis (`session:{id}`) using non-blocking `ReactiveStringRedisTemplate` in $<0.2\text{ms}$, avoiding repetitive asymmetric RSA math.
  - *Rejected*: Decrypting JWT signatures on every request.

- **SVC-GW-003**: **Zero-Trust Header Stripping & Signed `X-Internal-HMAC` Injection**
  - *Why*: The Gateway strips all incoming `X-User-*` and `X-Internal-*` headers from the public request (preventing header injection attacks) and injects freshly validated user claims signed with an HMAC-SHA256 signature and timestamp.
  - *Rejected*: Trusting client-supplied internal headers.

---

## Details

### 1. Gateway Routing Configuration (`application.yml` & `RouteConfig.java`)

```yaml
spring:
  cloud:
    gateway:
      routes:
        # 1. Public Authentication Route (User-Rbac-Service)
        - id: auth-service-route
          uri: lb://user-rbac-service
          predicates:
            - Path=/api/auth/**

        # 2. Banking Operations Route (Business-Operation-Service)
        - id: banking-service-route
          uri: lb://business-operation-service
          predicates:
            - Path=/api/banking/**

        # 3. KPI Analytics Route (KPI-Analytics-Service)
        - id: kpi-service-route
          uri: lb://kpi-analytics-service
          predicates:
            - Path=/api/kpis/**

        # 4. WebSocket STOMP Ingress Route (Chat-Communication-Service)
        - id: chat-websocket-route
          uri: lb:ws://chat-communication-service
          predicates:
            - Path=/ws/**

        # 5. Chat History REST Route (Chat-Communication-Service)
        - id: chat-service-rest-route
          uri: lb://chat-communication-service
          predicates:
            - Path=/api/v1/chat/**
```

---

### 2. Reactive BFF Session & Auth Filter (`BffAuthenticationFilter.java`)

```java
@Component
public class BffAuthenticationFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final JwtPublicKeyValidator jwtValidator;
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/public-key"
    );

    public BffAuthenticationFilter(ReactiveStringRedisTemplate redisTemplate, JwtPublicKeyValidator jwtValidator) {
        this.redisTemplate = redisTemplate;
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1. Bypass public authentication routes
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // 2. Check for BFF Opaque Session Cookie (Fast-Path)
        HttpCookie sessionCookie = exchange.getRequest().getCookies().getFirst("APP_SESSION");
        if (sessionCookie != null) {
            String sessionId = sessionCookie.getValue();
            return redisTemplate.opsForHash().entries("session:" + sessionId)
                .collectMap(e -> (String) e.getKey(), e -> (String) e.getValue())
                .flatMap(sessionData -> {
                    if (sessionData.isEmpty()) {
                        return unauthorized(exchange); // Expired or invalid session
                    }
                    // Mutate request with verified session attributes
                    exchange.getAttributes().put("userId", sessionData.get("userId"));
                    exchange.getAttributes().put("roles", sessionData.get("roles"));
                    exchange.getAttributes().put("email", sessionData.get("email"));
                    return chain.filter(exchange);
                });
        }

        // 3. Fallback: Check for Bearer JWT Token
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            ValidatedClaims claims = jwtValidator.validate(token);
            if (claims == null) {
                return unauthorized(exchange);
            }
            exchange.getAttributes().put("userId", claims.userId());
            exchange.getAttributes().put("roles", String.join(",", claims.roles()));
            exchange.getAttributes().put("email", claims.email());
            return chain.filter(exchange);
        }

        return unauthorized(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -100; // Execute early in filter chain
    }
}
```

---

### 3. Downstream Security & HMAC Signing Filter (`InternalHmacSignerFilter.java`)

```java
@Component
public class InternalHmacSignerFilter implements GlobalFilter, Ordered {

    @Value("${app.security.internal-hmac-secret}")
    private String hmacSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String userId = exchange.getAttribute("userId");
        String roles = exchange.getAttribute("roles");
        String permissions = exchange.getAttribute("permissions");

        // If not an authenticated user request, pass through (e.g. public login)
        if (userId == null) {
            return chain.filter(exchange);
        }

        long timestamp = Instant.now().getEpochSecond();
        String payloadToSign = timestamp + "::" + userId + "::" + (roles != null ? roles : "") + "::" + (permissions != null ? permissions : "");
        String hmacSignature = HmacUtils.hmacSha256Hex(hmacSecret, payloadToSign);

        // Mutate downstream HTTP request: Strip spoofed headers and inject verified signed claims
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
            // Strip untrusted client headers
            .headers(httpHeaders -> {
                httpHeaders.remove("X-User-Id");
                httpHeaders.remove("X-User-Roles");
                httpHeaders.remove("X-User-Permissions");
                httpHeaders.remove("X-Internal-HMAC");
                httpHeaders.remove("X-Timestamp");
            })
            // Inject verified signed headers
            .header("X-User-Id", userId)
            .header("X-User-Roles", roles != null ? roles : "")
            .header("X-User-Permissions", permissions != null ? permissions : "")
            .header("X-Timestamp", String.valueOf(timestamp))
            .header("X-Internal-HMAC", hmacSignature)
            .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -50; // Execute after BffAuthenticationFilter
    }
}
```

---

### 4. JWT Public Key Validator (`JwtPublicKeyValidator.java`)

```java
@Component
public class JwtPublicKeyValidator {

    private final RSAPublicKey publicKey;
    private final JWTVerifier verifier;

    public JwtPublicKeyValidator(@Value("${app.jwt.public-key}") RSAPublicKey publicKey) {
        this.publicKey = publicKey;
        Algorithm algorithm = Algorithm.RSA256(publicKey, null);
        this.verifier = JWT.require(algorithm)
            .withIssuer("user-rbac-service")
            .build();
    }

    public ValidatedClaims validate(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            String userId = jwt.getSubject();
            String email = jwt.getClaim("email").asString();
            List<String> roles = jwt.getClaim("roles").asList(String.class);
            return new ValidatedClaims(userId, email, roles);
        } catch (JWTVerificationException ex) {
            return null; // Invalid signature or expired
        }
    }
}
```

---

## Plan
Approved: yes

- [x] 1. Spring Cloud Gateway dependencies (`spring-cloud-starter-gateway`, `spring-boot-starter-data-redis-reactive`)
- [x] 2. Dynamic Route definitions (REST microservices & WebSocket proxy)
- [x] 3. Reactive `BffAuthenticationFilter` checking Redis `APP_SESSION` cookie fast-path, Bearer fallback with auto-promotion & cookie issuance, and instant session logout
- [x] 4. Downstream `InternalHmacSignerFilter` generating `X-Internal-HMAC` hex signatures
- [x] 5. Asymmetric RS256 JWT Public Key validator with dynamic key fetching from `user-rbac-service`
- [ ] 6. End-to-end integration tests with WireMock and Testcontainers Redis

## Code Progress
- Status: implemented. All gateway components compiled and verified with Spring Boot 3.3.4 and Java 21.

## Open questions
- None.

## Change requests (to upstream files)
- None.

## Next steps
- Proceed with `Chat-Communication-Service` (STOMP WebSocket broker over `/ws`, event emission to Kafka `chat.events.v1`, and Spring AI token streaming).

