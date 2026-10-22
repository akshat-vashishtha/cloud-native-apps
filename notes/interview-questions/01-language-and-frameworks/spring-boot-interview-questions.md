# Spring & Spring Boot Interview Questions (10-Year Java Lead)

Code samples target Spring Boot 3.x / Spring Framework 6.x unless marked Boot 4 / Framework 7. Verify annotation attributes and internal event ordering against the reference docs for your exact version.

## Table of Contents

- [1. Spring Core (IoC, DI, AOP)](#1-spring-core-ioc-di-aop)
- [2. Spring Boot Fundamentals](#2-spring-boot-fundamentals)
- [3. Application Startup Lifecycle](#3-application-startup-lifecycle)
- [4. Request Journey: Tomcat to RestController](#4-request-journey-tomcat-to-restcontroller)
- [5. Spring MVC and REST](#5-spring-mvc-and-rest)
- [6. Spring Data JPA and Transactions](#6-spring-data-jpa-and-transactions)
- [7. Spring Security](#7-spring-security)
- [8. Testing](#8-testing)
- [9. Performance and Production Readiness](#9-performance-and-production-readiness)
- [10. Spring Boot 3 / 4 and Framework 6 / 7 Features](#10-spring-boot-3--4-and-framework-6--7-features)
- [11. Code and Debugging Questions (with Solutions)](#11-code-and-debugging-questions-with-solutions)
- [12. Architectural Scenario Questions](#12-architectural-scenario-questions)
- [13. System Design Scenario Questions](#13-system-design-scenario-questions)
- [14. Rapid-Fire Questions](#14-rapid-fire-questions)

---

## 1. Spring Core (IoC, DI, AOP)

1. Explain Inversion of Control and Dependency Injection. How does the `ApplicationContext` differ from a `BeanFactory`?
2. Constructor vs setter vs field injection: which do you prefer and why (immutability, testability, circular dependencies)?
3. Explain the bean lifecycle in detail (instantiation, populate, Aware callbacks, `BeanPostProcessor`, `@PostConstruct`, `InitializingBean`, `@PreDestroy`).
4. What are the bean scopes (singleton, prototype, request, session, application)? How do you inject a prototype bean into a singleton correctly?
5. Is a singleton bean thread-safe? What are the common mistakes with mutable state in singletons?
6. How does Spring resolve circular dependencies? Why does constructor injection fail, and how should you fix the design?
7. `@Component` vs `@Service` vs `@Repository` vs `@Bean` vs `@Configuration`: differences and when to use each. What does `@Repository` add (exception translation)?
8. What does `@Configuration` do differently from `@Component` (CGLIB proxying, `proxyBeanMethods`)?
9. `@Autowired` vs `@Resource` vs `@Inject`. How does Spring resolve ambiguity (`@Primary`, `@Qualifier`, name matching)?
10. What are `BeanFactoryPostProcessor` and `BeanPostProcessor`? Give a practical use of each.
11. How does Spring AOP work (JDK dynamic proxy vs CGLIB)? Why does a self-invocation bypass the proxy?
12. Spring AOP vs AspectJ: when do you need load-time or compile-time weaving?
13. Which Spring features rely on proxies (`@Transactional`, `@Async`, `@Cacheable`, `@Retryable`, `@PreAuthorize`)? What limitations follow (public methods, self-invocation, final classes)?
14. How do you use `@Conditional`, `@Profile`, and `@ConditionalOnProperty`? How would you write a custom condition?
15. What are Spring events (`ApplicationEvent`, `@EventListener`, `@TransactionalEventListener`)? Are they synchronous by default? What is the phase problem with transactions?
16. What is `@Lazy`, and when does lazy initialisation help or hurt?
17. What is the difference between `@Value`, `@ConfigurationProperties`, and `Environment`?
18. How does Spring handle `Optional`, `ObjectProvider`, and `List<T>` / `Map<String, T>` injection?
19. What is the `BeanRegistrar` / programmatic bean registration API in Framework 7, and when would you use it over `@Bean` or `ImportBeanDefinitionRegistrar`?

## 2. Spring Boot Fundamentals

1. What does `@SpringBootApplication` combine? How does component scanning decide what to load?
2. How does auto-configuration work end to end (`AutoConfiguration.imports`, `@ConditionalOnClass`, `@ConditionalOnMissingBean`, ordering)?
3. How do you debug why an auto-configuration did or did not apply (`--debug`, conditions report, `/actuator/conditions`)?
4. How do you write a custom starter? What goes in the autoconfigure module vs the starter module?
5. Explain externalised configuration and property source precedence (command line, env vars, `application-{profile}.yml`, config tree, Config Server).
6. How do you bind configuration safely (`@ConfigurationProperties`, records, validation, relaxed binding, `@ConfigurationPropertiesScan`)?
7. How do profiles work? How do you avoid profile sprawl, and what are the risks of `spring.profiles.active` set in the wrong place?
8. How does the embedded server work (Tomcat, Jetty, Undertow)? How do you tune threads, connections, and timeouts?
9. What is the fat JAR layout? How do layered JARs help Docker builds?
10. What are the `CommandLineRunner`, `ApplicationRunner`, and `SmartLifecycle` hooks? When do you use each?
11. How do you implement graceful shutdown, and how does it interact with Kubernetes readiness and `preStop`?
12. What is Spring Boot Actuator? Which endpoints do you expose in production and how do you secure them?
13. How do health groups, liveness and readiness probes work in Spring Boot? What should and should not be in readiness?
14. How do you customise logging (Logback, structured logging, MDC, correlation IDs)?
15. How do you reduce startup time and memory (lazy init, AOT, CDS, GraalVM native image, class data sharing)?
16. How do you manage multiple environments and secrets (Config Server, Kubernetes ConfigMaps/Secrets, Key Vault, `spring.config.import`)?
17. What is `DevTools`, and why must it not ship to production?

## 3. Application Startup Lifecycle

1. Walk through `SpringApplication.run()` step by step, from `main()` to `ApplicationReadyEvent`.
2. What happens in the `SpringApplication` constructor (web application type deduction from the classpath, initialisers, listeners, main class detection)?
3. What is the bootstrap context (`BootstrapRegistryInitializer`, `DefaultBootstrapContext`), when does it exist, and who uses it (Config Data, Config Server)?
4. List the `SpringApplication` run events in order (`ApplicationStartingEvent`, `ApplicationEnvironmentPreparedEvent`, `ApplicationContextInitializedEvent`, `ApplicationPreparedEvent`, `ContextRefreshedEvent`, `WebServerInitializedEvent`, `ApplicationStartedEvent`, `AvailabilityChangeEvent`, `ApplicationReadyEvent`, `ApplicationFailedEvent`). Which ones can a normal `@EventListener` bean receive, and why can't it receive the early ones?
5. How is the `Environment` prepared? Who loads `application.yml` (`ConfigDataEnvironmentPostProcessor`), when are profiles activated, and how do you add a custom `EnvironmentPostProcessor`?
6. Which application context class is created for a servlet web app, and what is different for reactive and non-web apps?
7. What does `prepareContext` do (apply initialisers, register the primary source, load bean definitions) before the refresh starts?
8. Explain `AbstractApplicationContext.refresh()` step by step (`prepareRefresh`, `obtainFreshBeanFactory`, `prepareBeanFactory`, `postProcessBeanFactory`, `invokeBeanFactoryPostProcessors`, `registerBeanPostProcessors`, `initMessageSource`, `initApplicationEventMulticaster`, `onRefresh`, `registerListeners`, `finishBeanFactoryInitialization`, `finishRefresh`).
9. When does `ConfigurationClassPostProcessor` run, and how do `@ComponentScan`, `@Import`, `@Bean`, and auto-configuration imports turn into bean definitions before any regular bean is created?
10. When are `BeanPostProcessor`s registered? Why do beans created too early not get proxied (the "not eligible for getting processed by all BeanPostProcessors" warning)?
11. When are non-lazy singleton beans instantiated, and what changes with `spring.main.lazy-initialization=true`?
12. When and where is the embedded Tomcat created, and when does it actually start accepting connections? Why does Boot separate "web server created" from "web server started"?
13. When is `DispatcherServlet` registered with Tomcat, and when is it initialised? When are `@RequestMapping` handler mappings built? Why can the first request be slower than later ones?
14. Put these in order: `@PostConstruct`, `InitializingBean.afterPropertiesSet`, `@Bean(initMethod)`, `SmartInitializingSingleton.afterSingletonsInstantiated`, `ContextRefreshedEvent`, `SmartLifecycle.start()`, `ApplicationRunner` / `CommandLineRunner`, `ApplicationReadyEvent`.
15. Where would you put each of these: cache warm-up, schema/config validation, starting a Kafka listener, registering with service discovery, a startup smoke test? Compare `@PostConstruct`, runners, `SmartLifecycle`, and `ApplicationReadyEvent`.
16. How do liveness and readiness states change during startup and shutdown (`LivenessState`, `ReadinessState`, `AvailabilityChangeEvent`), and how does that map to Kubernetes probes?
17. What happens when startup fails (`FailureAnalyzer`, `ApplicationFailedEvent`, exit code, context closure)? How do you write a custom failure analyser?
18. What happens if a runner throws an exception or blocks for a long time? What does that do to readiness?
19. How do you measure startup time per bean (`ApplicationStartup`, `BufferingApplicationStartup`, `/actuator/startup`, `--debug`)?
20. Describe the shutdown sequence after SIGTERM (shutdown hook, `ContextClosedEvent`, `SmartLifecycle` stop in reverse phase order, graceful web server shutdown, `@PreDestroy`, `DisposableBean`). Where does readiness change to `REFUSING_TRAFFIC`?
21. What gets created differently when `management.server.port` differs from `server.port` (child management context)?
22. How do AOT processing, native images, CDS, lazy init, and Boot 4's modular auto-configuration change the startup path?

### Answer Outline: Startup Flow (for self-check)

```text
main()
 └─ new SpringApplication(primarySources)
      - deduce web app type (servlet / reactive / none)
      - load ApplicationContextInitializers + ApplicationListeners
      - deduce main class
 └─ run(args)
      1. Start timer, create bootstrap context, publish ApplicationStartingEvent
      2. Prepare Environment
           - property sources: args, system props, env vars
           - EnvironmentPostProcessors (Config Data loads application.yml, profiles, spring.config.import)
           - ApplicationEnvironmentPreparedEvent
      3. Print banner
      4. Create ApplicationContext (servlet web server application context)
      5. prepareContext
           - set environment, apply initialisers, ApplicationContextInitializedEvent
           - register main class as a bean source
           - ApplicationPreparedEvent
      6. refreshContext -> refresh()
           - invokeBeanFactoryPostProcessors (component scan, @Import, auto-config, @Bean definitions)
           - registerBeanPostProcessors (proxies, @Autowired processing, @Transactional infra)
           - onRefresh: create embedded web server (Tomcat set up, not yet accepting traffic)
           - finishBeanFactoryInitialization: instantiate non-lazy singletons
                 (@PostConstruct, afterPropertiesSet, initMethod run per bean)
           - finishRefresh: SmartLifecycle beans start, web server starts accepting connections,
                 ContextRefreshedEvent, ServletWebServerInitializedEvent
      7. ApplicationStartedEvent, readiness still not ACCEPTING_TRAFFIC
      8. callRunners (ApplicationRunner / CommandLineRunner by @Order)
      9. ApplicationReadyEvent, availability: liveness CORRECT, readiness ACCEPTING_TRAFFIC
 On any failure: FailureAnalyzers, ApplicationFailedEvent, context closed, non-zero exit
```

## 4. Request Journey: Tomcat to RestController

1. Trace a `GET /api/orders/42` request from the client socket to your `@RestController` method and back, naming each component on the way.
2. What happens inside Tomcat before your code runs (Acceptor, Poller, worker thread pool, `Http11Processor`, `CoyoteAdapter`, Engine / Host / Context / Wrapper pipeline)? What do `maxThreads`, `acceptCount`, `maxConnections`, and `connectionTimeout` control?
3. Which thread executes your controller method? Explain the thread-per-request model and what changes with virtual threads.
4. How does the container route the request to `DispatcherServlet` (servlet mapping `/`)? Where do static resources, `/error`, and actuator endpoints fit?
5. List the typical filters in a Boot MVC application in order (character encoding, form content, request context, observation / metrics, Spring Security filter chain, others). Who decides the order (`FilterRegistrationBean`, `@Order`, `OrderedFilter` constants)?
6. How is Spring Security plugged in (`DelegatingFilterProxy`, `FilterChainProxy`, `SecurityFilterChain`)? How is that different from an ordinary servlet filter?
7. What does `DispatcherServlet.doDispatch()` do step by step (multipart check, `getHandler`, `getHandlerAdapter`, interceptors, `handle`, view/response processing, exception resolution)?
8. How does `RequestMappingHandlerMapping` pick the handler (path, method, params, headers, consumes, produces, version)? Which status codes result when matching fails (404, 405, 415, 406)?
9. What is a `HandlerAdapter`, and why does Spring MVC need it (strategy pattern, different handler types)?
10. How do `HandlerInterceptor.preHandle`, `postHandle`, and `afterCompletion` work? What is the call order with several interceptors, and what happens when `preHandle` returns false or the controller throws?
11. How are controller arguments resolved (`HandlerMethodArgumentResolver`, `@RequestBody` through `HttpMessageConverter`, `@PathVariable`, `@RequestParam`, `ConversionService`, data binding, `@Valid`)?
12. How is the return value written (`HandlerMethodReturnValueHandler`, `RequestResponseBodyMethodProcessor`, content negotiation using `Accept`, Jackson converter, `ResponseBodyAdvice`)?
13. Where does validation run, and which exceptions occur for `@RequestBody` validation vs `@RequestParam` / `@PathVariable` validation?
14. If the controller throws an exception, who handles it, and in what order do the `HandlerExceptionResolver`s run (`ExceptionHandlerExceptionResolver`, `ResponseStatusExceptionResolver`, `DefaultHandlerExceptionResolver`)? What happens if nobody handles it (`/error`, `BasicErrorController`, error dispatch)?
15. Why does `@ControllerAdvice` not catch an exception thrown in a servlet filter or Spring Security filter? How do you handle it properly (`AuthenticationEntryPoint`, `AccessDeniedHandler`, `HandlerExceptionResolver` delegation, try/catch in the filter)?
16. Where do AOP proxies (`@Transactional`, `@PreAuthorize`, `@Cacheable`) sit in the controller to service to repository call chain? Is the controller itself usually proxied?
17. Where does the transaction begin and end for a typical request? With Open Session In View, where does the `EntityManager` open and close, and how does that affect lazy loading during JSON serialisation?
18. Order these for an incoming and outgoing request: servlet filter, Spring Security filter, `HandlerInterceptor`, `@ControllerAdvice`, an AOP aspect around the controller, and the `@Transactional` interceptor on a service.
19. How are `RequestContextHolder`, `SecurityContextHolder`, `LocaleContextHolder`, and MDC bound to the thread? What breaks when you hop threads (`@Async`, `CompletableFuture`, executors)?
20. How does async processing change the journey (`Callable`, `DeferredResult`, async dispatch, second pass through filters, SSE, async timeouts)?
21. How do keep-alive, HTTP/2, compression, and timeouts work at Tomcat level (`connectionTimeout`, `keepAliveTimeout`, `maxKeepAliveRequests`)? What is a client-side vs server-side timeout?
22. Where do metrics, tracing, and access logs hook in (observation filter, `WebMvcMetrics`, Tomcat access log, trace propagation, MDC)?
23. How does the same request differ in WebFlux on Netty (event loop, `DispatcherHandler`, `WebFilter`, no servlet, no thread-per-request)?
24. How does a multipart upload flow through the stack (`MultipartResolver`, size limits, Tomcat `maxPostSize` / `maxSwallowSize`, memory vs disk, streaming)?
25. Where do you add a new cross-cutting concern: servlet filter, interceptor, AOP aspect, or controller advice? State your decision criteria.
26. How does API versioning (Framework 7) fit into handler mapping and the request journey?

### Answer Outline: Request Journey (for self-check)

```text
Client
  │  TCP / TLS / HTTP
  ▼
Tomcat Connector (NIO)
  - Acceptor accepts socket, Poller registers it on a selector
  - worker thread from the pool (http-nio-8080-exec-N) takes over
  - Http11Processor parses HTTP -> CoyoteAdapter builds Request/Response
  ▼
Tomcat container pipeline: Engine -> Host -> Context -> Wrapper (DispatcherServlet)
  ▼
Servlet filter chain (in order)
  - encoding, form content, request context, observation/metrics ...
  - Spring Security: DelegatingFilterProxy -> FilterChainProxy -> SecurityFilterChain
        (context holder, CSRF, authentication, exception translation, authorization)
  ▼
DispatcherServlet.doDispatch()
  1. getHandler        -> HandlerExecutionChain (handler method + interceptors)
  2. getHandlerAdapter -> RequestMappingHandlerAdapter
  3. interceptors preHandle (in order)
  4. adapter.handle    -> resolve arguments (path vars, @RequestBody via HttpMessageConverter, @Valid)
  5. invoke controller method (may call AOP-proxied services: security, transaction, cache)
  6. return value handling -> content negotiation -> HttpMessageConverter writes the body
  7. interceptors postHandle (reverse order)
  8. on exception: HandlerExceptionResolvers -> @ControllerAdvice / ResponseStatus / default
  9. interceptors afterCompletion (reverse order)
  ▼
Servlet filters unwind (reverse order)
  ▼
Tomcat writes the response, keeps the connection alive or closes it
  ▼
Client

Exceptions in filters bypass DispatcherServlet, so @ControllerAdvice does not see them;
unhandled errors are forwarded to /error (BasicErrorController).
```

## 5. Spring MVC and REST

1. `@Controller` vs `@RestController`. `@RequestBody`, `@RequestParam`, `@PathVariable`, `@ModelAttribute`, `@RequestHeader`: when do you use each?
2. How do you design global exception handling (`@ControllerAdvice`, `ProblemDetail`, RFC 7807/9457)?
3. How do you do request validation (Bean Validation, `@Valid`, `@Validated`, groups, custom validators)? What is the difference between `MethodArgumentNotValidException` and `ConstraintViolationException`?
4. Filters vs interceptors vs `@ControllerAdvice` vs AOP: when do you use each?
5. How do you design REST APIs well (resource naming, status codes, idempotency, pagination, filtering, HATEOAS, versioning)?
6. How do you handle API versioning (URL, header, media type)? What does Framework 7 offer natively?
7. How do you implement idempotent POST endpoints (idempotency keys)?
8. How do you handle large file upload and download (streaming, `StreamingResponseBody`, multipart limits)?
9. How do you customise JSON serialisation (Jackson modules, `@JsonView`, date handling, unknown properties, polymorphism)? What changes with Jackson 3?
10. Content negotiation, CORS, and CSRF: how do they work and how do you configure them?
11. `RestTemplate` vs `WebClient` vs `RestClient` vs HTTP interface clients (`@HttpExchange`): which do you use and why?
12. How do you set timeouts, connection pooling, retries, and error handling on outbound HTTP calls?
13. Spring MVC (blocking) vs WebFlux (reactive): when do you choose each, and how do virtual threads change that decision?
14. How do you implement async request processing (`DeferredResult`, `Callable`, `@Async`, SSE, WebSocket)?
15. How do you document APIs (springdoc-openapi, contract-first) and enforce backward compatibility?
16. How do you implement rate limiting, request tracing, and correlation IDs?
17. How do you implement ETag / conditional requests and caching headers?

## 6. Spring Data JPA and Transactions

1. How does `@Transactional` work internally (proxy, `TransactionInterceptor`, `PlatformTransactionManager`, thread-bound resources)?
2. Explain propagation levels (`REQUIRED`, `REQUIRES_NEW`, `NESTED`, `SUPPORTS`, `NOT_SUPPORTED`, `MANDATORY`, `NEVER`) with a real use case for `REQUIRES_NEW`.
3. Rollback rules: why does a checked exception not roll back by default? How do you change it?
4. Why does `@Transactional` not work on private methods or self-invocation? How do you fix it?
5. What is the persistence context? Explain first-level cache, dirty checking, flush modes, and `EntityManager` lifecycle.
6. Entity states (transient, managed, detached, removed). `save()` vs `saveAndFlush()` vs `merge()` vs `persist()`.
7. What is the N+1 select problem, and how do you fix it (`JOIN FETCH`, `@EntityGraph`, batch size, DTO projections)?
8. `LAZY` vs `EAGER`. What is `LazyInitializationException`, and why is Open Session in View considered an anti-pattern?
9. How do you map relationships correctly (`@OneToMany` owning side, `mappedBy`, cascade, `orphanRemoval`, bidirectional helper methods)?
10. Optimistic vs pessimistic locking in JPA (`@Version`, `LockModeType`). How do you handle `OptimisticLockException`?
11. How do you implement pagination and sorting efficiently (`Pageable`, `Slice`, keyset pagination)? Why can `Page` be expensive?
12. Derived queries vs `@Query` vs Specifications vs Querydsl vs native queries: when do you use each?
13. What are projections (interface, class-based, dynamic)? When do they reduce load?
14. How do you handle bulk operations and batch inserts (`saveAll`, `hibernate.jdbc.batch_size`, `@Modifying`, `IDENTITY` generator limitations)?
15. How do you configure HikariCP (pool size, timeouts, leak detection)? How do you size a pool?
16. How do you manage schema changes (Flyway/Liquibase, `ddl-auto` risks in production)?
17. How do you implement auditing (`@CreatedDate`, `@LastModifiedBy`, Envers)?
18. How do you manage multiple data sources or read/write splitting?
19. `JdbcTemplate` vs `JdbcClient` vs JPA: when do you drop below JPA?
20. How do you use `@TransactionalEventListener` to publish events only after commit? How does it relate to the outbox pattern?
21. What happens to a transaction when you call an `@Async` method or a new thread from a transactional method?

## 7. Spring Security

1. Explain the Spring Security architecture (`DelegatingFilterProxy`, `FilterChainProxy`, `SecurityFilterChain`, `AuthenticationManager`, `SecurityContextHolder`).
2. How do you configure security in Spring Security 6+ (`SecurityFilterChain` bean, lambda DSL) now that `WebSecurityConfigurerAdapter` is gone?
3. Authentication vs authorisation. How do `AuthenticationProvider`, `UserDetailsService`, and `PasswordEncoder` fit together?
4. How do you implement stateless JWT authentication? Where do you validate tokens, and how do you handle refresh and revocation?
5. OAuth2 roles: client, resource server, authorisation server. How do you configure a resource server with JWT or opaque tokens?
6. Authorization Code with PKCE vs Client Credentials: when do you use each?
7. How do you secure service-to-service calls (client credentials, token relay, mTLS)?
8. Method security: `@PreAuthorize`, `@PostAuthorize`, `@Secured`, SpEL expressions. What are the proxy limitations?
9. CSRF: when can you disable it, and when must you not? How do SPAs and cookies change that?
10. CORS vs CSRF vs XSS: how does each attack work and what is the Spring mitigation?
11. How do you implement RBAC vs ABAC, and how do you map JWT claims to authorities?
12. How do you handle password storage (BCrypt, Argon2, `DelegatingPasswordEncoder`, upgrade strategy)?
13. How do you secure Actuator, H2 console, and Swagger UI in production?
14. How do you implement multi-tenancy security (tenant claim, tenant-scoped queries, filters)?
15. How do you audit security events (`AuthenticationSuccessEvent`, failures, access denied)?
16. How do you protect against brute force, token theft, and session fixation?
17. How do you test secured endpoints (`@WithMockUser`, `jwt()` post-processor, `@WithUserDetails`)?

## 8. Testing

1. Test slices: `@WebMvcTest`, `@DataJpaTest`, `@JsonTest`, `@RestClientTest`, `@SpringBootTest`. When do you use each, and what gets loaded?
2. `@MockBean` / `@MockitoBean` vs `@Mock` vs `@SpyBean`: differences, context caching impact.
3. How does Spring's test context caching work, and what breaks it (`@MockBean` combinations, `@DirtiesContext`)?
4. How do you use Testcontainers with `@ServiceConnection` for databases, Kafka, MongoDB, and Redis?
5. How do you test transactional code (`@Transactional` on tests, rollback behaviour, why it can hide bugs)?
6. How do you test security rules and exception handlers?
7. How do you do contract testing (Spring Cloud Contract, Pact) between services?
8. How do you test asynchronous code, scheduled jobs, and event listeners?
9. How do you test REST clients (MockRestServiceServer, WireMock, `RestTestClient` in Framework 7)?
10. What is your test pyramid for a Spring Boot microservice, and what do you avoid testing with `@SpringBootTest`?
11. How do you test configuration properties and conditional beans (`ApplicationContextRunner`)?
12. How do you keep integration tests fast and non-flaky in CI?
13. How do you test filters, interceptors, and the full request journey (`MockMvc` vs `@SpringBootTest(webEnvironment = RANDOM_PORT)` with a real Tomcat)? What does `MockMvc` not exercise?

## 9. Performance and Production Readiness

1. How do you profile a slow Spring Boot endpoint (Micrometer timers, tracing, JFR, async-profiler, SQL logging)?
2. What are the typical causes of slow startup and high memory in Spring Boot, and how do you fix them?
3. How do you tune Tomcat (`server.tomcat.threads.max`, `accept-count`, `max-connections`, keep-alive) for a given load?
4. Do virtual threads (`spring.threads.virtual.enabled`) help? What are the pitfalls (pinning, connection pools, `ThreadLocal`, unbounded concurrency)?
5. How do you configure caching (`@Cacheable`, `@CacheEvict`, TTL, Redis, cache stampede, serialisation)? Why does `@Cacheable` fail on self-invocation?
6. How do you handle thread pools for `@Async` and `@Scheduled` correctly (custom executor, rejection policy, context propagation)?
7. How do you run scheduled jobs safely across multiple instances (ShedLock, Quartz clustering, leader election, Kubernetes CronJob)?
8. How do you capture metrics and traces (Micrometer, OpenTelemetry, Prometheus, Grafana)? Which metrics do you alert on?
9. How do you diagnose memory leaks and OOMKilled pods (heap vs container memory, `MaxRAMPercentage`, heap dump, metaspace)?
10. How do you handle backpressure and rate limiting inside a Spring Boot service?
11. How do you tune JSON serialisation and payload size for performance?
12. How do you design health checks and graceful shutdown so rolling deployments do not drop requests?
13. How do you do zero-downtime config changes (`@RefreshScope`, restart vs refresh trade-offs)?
14. How do you build a production-grade Docker image for Spring Boot (layered JAR, buildpacks, JRE image, non-root user)?

## 10. Spring Boot 3 / 4 and Framework 6 / 7 Features

1. What are the baselines for Spring Boot 3 (Java 17, Jakarta EE 9+) and what broke in the `javax` to `jakarta` migration? What are the baselines for Boot 4 / Framework 7 (Jakarta EE 11)?
2. What changed in Spring Boot 4's modularisation of auto-configuration, and how does it affect starters, dependencies, and your upgrade?
3. How does Framework 7 support API versioning (path, header, query parameter, media type)? How would you evolve an API for old clients?
4. What are `@Retryable`, `@ConcurrencyLimit`, and `@EnableResilientMethods` in Framework 7? How do they compare with Resilience4j and the old Spring Retry project?
5. What is JSpecify null-safety in Spring, and how does it affect your code and IDE/Kotlin interop?
6. What does Jackson 3 support change for your serialisation setup?
7. What is `@ImportHttpServices`, and how do declarative HTTP interface clients (`@HttpExchange`) reduce boilerplate?
8. What are `RestClient`, `JdbcClient`, `RestTestClient`, and the newer `JmsClient`? When do you use each?
9. What are `ProblemDetail` responses, and how do you standardise error contracts?
10. What is Micrometer Observation / the observability starter, and how do metrics, logs, and traces correlate?
11. What are virtual threads support, AOT processing, and GraalVM native images in Spring Boot? What are the limits (reflection, proxies, dynamic classpath)?
12. What is `@ServiceConnection` and Docker Compose / Testcontainers support in Spring Boot?
13. What are Spring Modulith, Spring Cloud (current release train), and Spring AI? Where do they fit in your architecture?
14. How do you plan an upgrade path from Boot 2.x or 3.x to 4.x (deprecations, third-party compatibility, staged upgrade, automated recipes like OpenRewrite)?

## 11. Code and Debugging Questions (with Solutions)

### Q1. This `@Transactional` method does not roll back or does not start a transaction. Why?

```java
@Service
public class OrderService {

    public void placeOrder(Order order) {
        validate(order);
        save(order);            // self-invocation: proxy is bypassed
    }

    @Transactional
    public void save(Order order) {
        orderRepository.save(order);
        paymentClient.charge(order);   // throws checked IOException: no rollback by default
    }
}
```

Fix:

```java
@Service
public class OrderService {

    @Transactional(rollbackFor = Exception.class)   // placeOrder is now the transactional boundary
    public void placeOrder(Order order) {
        validate(order);
        orderRepository.save(order);
        paymentClient.charge(order);
    }
}
// Or move save() to another bean so the call goes through the proxy.
```

### Q2. Circular dependency: A needs B and B needs A. How do you fix the design?

```java
@Service
class A { A(B b) {} }

@Service
class B { B(A a) {} }   // fails at startup: BeanCurrentlyInCreationException
```

Fix options, best first:

```java
// 1. Extract the shared logic into a third bean C, so A -> C and B -> C.
// 2. Replace one direction with an event:
applicationEventPublisher.publishEvent(new SomethingHappened(id));
// 3. Last resort: break the cycle lazily.
@Service
class B { B(@Lazy A a) {} }
```

### Q3. Global exception handling with `ProblemDetail`.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail handleNotFound(OrderNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Order not found");
        pd.setProperty("orderId", ex.getOrderId());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
            .map(f -> Map.of("field", f.getField(), "message", String.valueOf(f.getDefaultMessage())))
            .toList());
        return pd;
    }
}
```

### Q4. Type-safe configuration with a record and validation.

```java
@ConfigurationProperties(prefix = "app.payment")
@Validated
public record PaymentProperties(
        @NotBlank String baseUrl,
        @DefaultValue("PT3S") Duration timeout,
        @Min(1) @Max(10) @DefaultValue("3") int maxRetries) {}

@SpringBootApplication
@ConfigurationPropertiesScan
public class App {}
```

```yaml
app:
  payment:
    base-url: https://pay.example.com
    timeout: 2s
    max-retries: 4
```

### Q5. Write a minimal custom auto-configuration.

```java
@AutoConfiguration
@ConditionalOnClass(AuditClient.class)
@EnableConfigurationProperties(AuditProperties.class)
public class AuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    AuditClient auditClient(AuditProperties props) {
        return new AuditClient(props.url());
    }
}
```

Register it in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:

```
com.example.audit.AuditAutoConfiguration
```

### Q6. Add a correlation ID to every request and log line.

```java
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String id = Optional.ofNullable(req.getHeader(HEADER)).orElse(UUID.randomUUID().toString());
        MDC.put("correlationId", id);
        res.setHeader(HEADER, id);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
```

### Q7. Stateless JWT resource server (Spring Security 6+).

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())   // acceptable for a stateless token API without cookies
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").hasAuthority("SCOPE_products.read")
                .anyRequest().authenticated())
            .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
            .build();
    }
}
```

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://login.example.com/realms/demo
```

### Q8. `REQUIRES_NEW` for audit logging that must survive a rollback.

```java
@Service
public class AuditService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String event) {
        auditRepository.save(new AuditEntry(event));
    }
}
// Call it from a different bean than the one with the business transaction,
// otherwise the proxy is bypassed.
```

### Q9. Fix the N+1 problem.

```java
// Problem: loading 100 orders triggers 1 + 100 queries for items.
List<Order> orders = orderRepository.findAll();
orders.forEach(o -> o.getItems().size());

// Fix 1: entity graph
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "items")
    List<Order> findByStatus(String status);

    // Fix 2: fetch join
    @Query("select distinct o from Order o join fetch o.items where o.status = :status")
    List<Order> findWithItems(@Param("status") String status);

    // Fix 3: DTO projection when you do not need entities
    @Query("select new com.example.OrderSummary(o.id, o.total) from Order o where o.status = :status")
    List<OrderSummary> summaries(@Param("status") String status);
}
```

### Q10. Configure `@Async` with a proper executor.

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("emailExecutor")
    ThreadPoolTaskExecutor emailExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(8);
        ex.setQueueCapacity(200);
        ex.setThreadNamePrefix("email-");
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        ex.initialize();
        return ex;
    }
}

@Service
class EmailService {
    @Async("emailExecutor")
    public CompletableFuture<Void> send(String to) { /* ... */ return CompletableFuture.completedFuture(null); }
}
// Questions: what happens to the security context, MDC, and the transaction in the new thread?
```

### Q11. Publish an event only after the transaction commits.

```java
@Service
class OrderService {
    @Transactional
    public void place(Order o) {
        orderRepository.save(o);
        publisher.publishEvent(new OrderPlaced(o.getId()));
    }
}

@Component
class OrderPlacedListener {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(OrderPlaced e) {
        // safe to call Kafka / email; if this fails, the DB commit already happened
        // which is why a durable outbox is preferable for critical events
    }
}
```

### Q12. Controller slice test with security.

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean OrderService orderService;   // @MockBean on older Boot versions

    @Test
    void returns404WhenMissing() throws Exception {
        when(orderService.get(99L)).thenThrow(new OrderNotFoundException(99L));

        mvc.perform(get("/api/orders/99").with(jwt()))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.title").value("Order not found"));
    }
}
```

### Q13. Integration test with Testcontainers and `@ServiceConnection`.

```java
@SpringBootTest
@Testcontainers
class OrderRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired OrderRepository repository;

    @Test
    void savesAndLoads() {
        Order saved = repository.save(new Order("NEW"));
        assertThat(repository.findById(saved.getId())).isPresent();
    }
}
```

### Q14. Graceful shutdown, probes, and virtual threads.

```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
  threads:
    virtual:
      enabled: true
management:
  endpoint:
    health:
      probes:
        enabled: true
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### Q15. Declarative retry and concurrency limit (Spring Framework 7 / Boot 4).

```java
@Configuration
@EnableResilientMethods
class ResilienceConfig {}

@Service
class RateQuoteService {

    @Retryable(maxAttempts = 3, delay = 200)
    @ConcurrencyLimit(5)
    public Quote fetch(String symbol) {
        return externalClient.quote(symbol);   // retried on failure; at most 5 concurrent calls
    }
}
// Follow-up: how is this different from Resilience4j (circuit breaker, bulkhead, metrics)?
```

### Q16. API versioning (Spring Framework 7). Check the reference docs for the exact configuration in your version.

```java
@Configuration
class WebConfig implements WebMvcConfigurer {
    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer.useRequestHeader("API-Version");   // other strategies: path, query param, media type
    }
}

@RestController
@RequestMapping("/api/products")
class ProductController {

    @GetMapping(version = "1.0")
    List<ProductV1> listV1() { /* ... */ return List.of(); }

    @GetMapping(version = "2.0")
    List<ProductV2> listV2() { /* ... */ return List.of(); }
}
```

### Q17. Declarative HTTP client with `RestClient`.

```java
public interface InventoryClient {
    @GetExchange("/stock/{sku}")
    StockResponse getStock(@PathVariable String sku);
}

@Configuration
class ClientConfig {
    @Bean
    InventoryClient inventoryClient(RestClient.Builder builder) {
        RestClient rc = builder.baseUrl("https://inventory.internal").build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(rc))
                .build()
                .createClient(InventoryClient.class);
    }
}
```

### Q18. Why is this `@Cacheable` never hitting the cache?

```java
@Service
class ProductService {

    public Product details(String id) {
        return load(id);                 // self-invocation: cache proxy bypassed
    }

    @Cacheable(value = "products", key = "#id")
    public Product load(String id) { /* DB call */ return null; }
}
// Fix: call load() from another bean, or put @Cacheable on the method that the caller invokes.
// Also check: cache enabled with @EnableCaching, key expression, serialisation, TTL config.
```

### Q19. Observe the startup order yourself: print every hook and compare with your answer.

```java
@Component
class StartupProbe implements SmartInitializingSingleton, SmartLifecycle,
        ApplicationRunner, CommandLineRunner {

    private boolean running;

    @PostConstruct
    void postConstruct() { log("1 @PostConstruct"); }

    @Override public void afterSingletonsInstantiated() { log("2 afterSingletonsInstantiated"); }

    @Override public void start() { running = true; log("3 SmartLifecycle.start"); }
    @Override public void stop()  { running = false; log("X SmartLifecycle.stop (shutdown)"); }
    @Override public boolean isRunning() { return running; }

    @Override public void run(ApplicationArguments args) { log("5 ApplicationRunner"); }
    @Override public void run(String... args)           { log("5 CommandLineRunner"); }

    @EventListener void onRefreshed(ContextRefreshedEvent e)   { log("4 ContextRefreshedEvent"); }
    @EventListener void onStarted(ApplicationStartedEvent e)   { log("4b ApplicationStartedEvent"); }
    @EventListener void onReady(ApplicationReadyEvent e)       { log("6 ApplicationReadyEvent"); }
    @EventListener void onAvailability(AvailabilityChangeEvent<?> e) { log("availability -> " + e.getState()); }
    @PreDestroy void preDestroy() { log("Y @PreDestroy (shutdown)"); }

    private static void log(String s) { System.out.println("[probe] " + s); }
}
// Follow-ups: where does the web server start accepting traffic relative to these lines?
// Which line proves readiness is not yet ACCEPTING_TRAFFIC while runners execute?
```

### Q20. Capture a very early startup event (a normal `@EventListener` cannot).

```java
public class EarlyListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        System.out.println("Active profiles: " + Arrays.toString(event.getEnvironment().getActiveProfiles()));
    }
}

// Option 1: register programmatically
public static void main(String[] args) {
    SpringApplication app = new SpringApplication(App.class);
    app.addListeners(new EarlyListener());
    app.run(args);
}
// Option 2: register via META-INF/spring.factories (key: org.springframework.context.ApplicationListener)
```

### Q21. Exception thrown in a filter is not handled by `@ControllerAdvice`. Return a JSON `ProblemDetail` anyway.

```java
@Component
class SafeFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver resolver;

    SafeFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            chain.doFilter(req, res);
        } catch (Exception ex) {
            resolver.resolveException(req, res, null, ex);   // routes to @ControllerAdvice
        }
    }
}
// For Spring Security failures use AuthenticationEntryPoint / AccessDeniedHandler instead.
```

### Q22. Log the order in which filter, interceptor, and advice run for one request.

```java
@Component
class OrderFilter extends OncePerRequestFilter {
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        System.out.println("filter before");
        chain.doFilter(req, res);
        System.out.println("filter after");
    }
}

@Component
class OrderInterceptor implements HandlerInterceptor {
    public boolean preHandle(HttpServletRequest r, HttpServletResponse s, Object h) {
        System.out.println("interceptor preHandle"); return true;
    }
    public void postHandle(HttpServletRequest r, HttpServletResponse s, Object h, ModelAndView m) {
        System.out.println("interceptor postHandle");
    }
    public void afterCompletion(HttpServletRequest r, HttpServletResponse s, Object h, Exception ex) {
        System.out.println("interceptor afterCompletion");
    }
}

@Configuration
class WebMvcConfig implements WebMvcConfigurer {
    @Autowired OrderInterceptor interceptor;
    @Override public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(interceptor); }
}
// Expected order for a normal request:
// filter before -> interceptor preHandle -> controller -> interceptor postHandle
// -> (response written) -> interceptor afterCompletion -> filter after
// Predict what changes when the controller throws, and when preHandle returns false.
```

## 12. Architectural Scenario Questions

1. Your Spring Boot service takes 90 seconds to start in Kubernetes and readiness probes keep failing. How do you investigate and fix it?
2. A new engineer put `@Transactional` on a method that calls a remote payment API and Kafka. What are the risks, and how do you restructure it?
3. Intermittent `HikariPool - Connection is not available` errors appear under load. How do you find the cause?
4. After a deploy, memory climbs until pods are OOMKilled, though heap looks fine. What do you check?
5. A shared internal starter library forces every team to upgrade at the same time. How do you design it to avoid that?
6. You must upgrade 40 services from Spring Boot 2.7 / 3.x to Boot 4. How do you plan, sequence, and de-risk it?
7. Your team wants to adopt virtual threads. How do you validate it is safe and beneficial before rollout?
8. A service sometimes returns stale data after an update because of caching. How do you design cache invalidation and consistency?
9. A third-party API is slow and unreliable and is dragging down your request threads. What patterns do you apply (timeouts, bulkheads, circuit breaker, async, fallback)?
10. Developers keep adding `@MockBean` in tests and the CI test time has tripled. What is happening and how do you fix it?
11. You find sensitive data (tokens, PII) in application logs. How do you stop it and prevent recurrence?
12. A scheduled job ran twice in production when the service scaled to 3 pods. How do you guarantee single execution?
13. Multiple teams need to expose consistent error responses, logging, security, and metrics. How do you standardise this without a framework monolith?
14. You need zero-downtime deployments with DB schema changes. How do Spring Boot, Flyway, and your rollout strategy work together?
15. Two services share one database "temporarily". How do you get out of it safely?
16. The first request after every deploy takes 3 seconds and later requests take 30 ms. Explain every possible cause along the startup and request path (lazy servlet initialisation, class loading, connection pool warm-up, JIT, caches) and how you remove each.
17. A servlet filter throws an exception and clients receive an HTML error page instead of your JSON `ProblemDetail`. Explain why and fix it.
18. Under load, requests queue up and latency spikes while CPU stays low. Walk through every place a request can wait (Tomcat `acceptCount` / `maxThreads`, connection pool, downstream calls, locks, DB) and how you identify the bottleneck.
19. Kubernetes shows the pod as Ready, but the first seconds of traffic get 503 or errors. Use your knowledge of the startup sequence, runners, readiness state, and web server start to explain and fix it.
20. A cache warm-up `CommandLineRunner` takes two minutes. Should readiness be UP before it finishes? How do you design startup tasks so traffic only arrives when the service is truly ready?
21. During a rolling deploy, in-flight requests are dropped. Walk through the shutdown sequence (SIGTERM, readiness, `preStop`, graceful web server shutdown, connection draining) and fix it.
22. A controller works in `MockMvc` tests but fails in a real deployment because of a filter and a proxy-related header. What does `MockMvc` skip, and how do you test the real path?

## 13. System Design Scenario Questions

1. Design the backend for an e-commerce checkout with Spring Boot: API layer, transactions, payment integration, idempotency, and failure handling.
2. Design a multi-tenant SaaS API on Spring Boot with tenant isolation in security, data, caching, and rate limits.
3. Design an authentication and authorisation setup for a Spring Boot microservices platform (gateway, JWT, token relay, service-to-service security).
4. Design a file upload and processing service with retries, status tracking, and secure downloads.
5. Design a notification service with templates, channels, retries, and per-user preferences.
6. Design an API platform with versioning, deprecation policy, rate limiting, and consumer-driven contract tests across many teams.
7. Design a scheduled batch and reporting platform (Spring Batch or alternatives) that handles restarts, partial failures, and large volumes.
8. Design an audit and activity log capability for all services without slowing request paths.
9. Design a configuration and feature-flag strategy for 30 services across dev, staging, and prod.
10. Design a Spring Boot service from scratch for production readiness: observability, health, security, resilience, and deployment on AKS.
11. Design a shared platform library (starter) for logging, security, error handling, and metrics, and describe how its auto-configuration, filters, and startup hooks behave.

## 14. Rapid-Fire Questions

1. What is the default scope of a Spring bean?
2. What is the difference between `@Controller` and `@RestController`?
3. What does `@SpringBootApplication` include?
4. How do you change the embedded server port and context path?
5. `@PathVariable` vs `@RequestParam`?
6. What is the difference between `application.properties` and `application.yml` precedence?
7. What does `@Transactional(readOnly = true)` actually do?
8. What is the default isolation level in Spring `@Transactional`?
9. `CrudRepository` vs `JpaRepository` vs `PagingAndSortingRepository`?
10. What is `@Primary` vs `@Qualifier`?
11. What happens when two beans of the same type exist and none is marked primary?
12. What is the difference between `@Bean` and `@Component`?
13. Can you inject a prototype bean into a singleton? What is the catch?
14. What is `@Lazy` injection?
15. What are the Actuator endpoints you expose in production?
16. What is the difference between liveness and readiness?
17. `@Async` return types and their effect on exceptions?
18. What is the default connection pool in Spring Boot?
19. What is the difference between `Filter` and `HandlerInterceptor`?
20. How do you exclude an auto-configuration class?
21. What is `@ConditionalOnMissingBean` used for?
22. How do you read an environment variable in Spring Boot?
23. What is the difference between `@Mock` and `@MockitoBean`?
24. What is the purpose of `spring.jpa.open-in-view`, and what should it be in production?
25. What is the difference between `PUT` and `PATCH`, and how do you implement `PATCH` safely?
26. Which embedded server does `spring-boot-starter-web` use by default, and how do you switch it?
27. What is the default servlet mapping for `DispatcherServlet` in Spring Boot?
28. Which event tells you the application is ready to serve traffic?
29. Which runs first: `CommandLineRunner` or `ApplicationReadyEvent`?
30. What is the default thread name pattern for Tomcat worker threads?