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

#### 1. Explain Inversion of Control and Dependency Injection. How does the `ApplicationContext` differ from a `BeanFactory`?
- **Simple Meaning (Dev-Friendly Question):** Who creates our objects and gives them the objects they need?
- **Interview Answer:** Inversion of Control means Spring manages object creation and lifecycle instead of application code doing it everywhere. Dependency Injection is how Spring supplies those objects' dependencies. `BeanFactory` provides the basic bean container; `ApplicationContext` adds events, messages, resource loading, and integrated processing of bean configuration. An application context normally creates non-lazy singletons during refresh, while a basic bean factory normally creates beans when requested.

#### 2. Constructor vs setter vs field injection: which do you prefer and why (immutability, testability, circular dependencies)?
- **Simple Meaning (Dev-Friendly Question):** What is the cleanest way to pass dependencies into a class?
- **Interview Answer:** Prefer constructor injection for required dependencies because they are visible, can be `final`, and are easy to pass in unit tests. Setter injection suits optional or genuinely replaceable dependencies. Field injection hides requirements and makes plain Java testing harder. A constructor cycle exposes a design problem; changing to field injection only hides it and may still fail under Boot's default circular-reference policy.

#### 3. Explain the bean lifecycle in detail (instantiation, populate, Aware callbacks, `BeanPostProcessor`, `@PostConstruct`, `InitializingBean`, `@PreDestroy`).
- **Simple Meaning (Dev-Friendly Question):** What happens between Spring creating an object and eventually cleaning it up?
- **Interview Answer:** Spring constructs the bean, injects fields/setters, and runs awareness callbacks such as `BeanNameAware` and `BeanFactoryAware`; context-aware callbacks are supplied by a post-processor. Before-initialization processors run, with `@PostConstruct` handled during this processing, followed by `InitializingBean.afterPropertiesSet()` and a configured custom init method. After-initialization processors then run and may return an AOP proxy. At destruction, a managed bean normally receives `@PreDestroy`, `DisposableBean.destroy()`, and its configured destroy method, without duplicating an identical callback. Prototype beans do not receive automatic destruction after Spring hands them to the caller.

#### 4. What are the bean scopes (singleton, prototype, request, session, application)? How do you inject a prototype bean into a singleton correctly?
- **Simple Meaning (Dev-Friendly Question):** How long does each bean live, and how do you get a fresh object when needed?
- **Interview Answer:** Singleton means one instance per bean definition per container; prototype means a new instance on each lookup. Request, session, and application scopes follow an HTTP request, HTTP session, and `ServletContext` respectively. Directly injecting a prototype into a singleton supplies only the instance created at injection time. Use `ObjectProvider.getObject()` for a fresh instance per lookup, or a suitable scoped proxy; explicitly manage prototype cleanup if needed.

#### 5. Is a singleton bean thread-safe? What are the common mistakes with mutable state in singletons?
- **Simple Meaning (Dev-Friendly Question):** Can many requests safely use the same service object?
- **Interview Answer:** Singleton scope does not make an object thread-safe. Avoid storing the current user, request data, or intermediate results in instance fields because requests can overwrite each other's values. Keep request-specific data in local variables and prefer immutable dependencies. For genuinely shared mutable state, use appropriate synchronization or concurrent data structures and account for multiple application instances.

#### 6. How does Spring resolve circular dependencies? Why does constructor injection fail, and how should you fix the design?
- **Simple Meaning (Dev-Friendly Question):** What if two beans both need each other to be created?
- **Interview Answer:** Two required constructor dependencies cannot normally be created because neither object can exist first. Spring can resolve some singleton setter/field cycles through early references when circular references are enabled, but Boot disables them by default and proxying can make them harder. Prefer extracting shared logic, introducing an orchestrator, or replacing one direction with an event. A lazy dependency or provider can defer lookup as a last resort, but does not remove the design coupling.

#### 7. `@Component` vs `@Service` vs `@Repository` vs `@Bean` vs `@Configuration`: differences and when to use each. What does `@Repository` add (exception translation)?
- **Simple Meaning (Dev-Friendly Question):** Which annotation should mark each kind of Spring object?
- **Interview Answer:** `@Component` is a general scanned bean, `@Service` describes business logic, and `@Repository` describes persistence access. With exception-translation infrastructure enabled, eligible repository exceptions can be converted into Spring's `DataAccessException` hierarchy. `@Bean` registers an object returned by a factory method, which is useful for third-party classes. `@Configuration` groups bean definitions and can preserve container-managed references between its `@Bean` methods.

#### 8. What does `@Configuration` do differently from `@Component` (CGLIB proxying, `proxyBeanMethods`)?
- **Simple Meaning (Dev-Friendly Question):** Why can calling one bean factory method from another behave differently in a configuration class?
- **Interview Answer:** With the default `proxyBeanMethods=true`, Spring enhances a `@Configuration` class so eligible calls between `@Bean` methods resolve through the container instead of creating extra objects. A `@Component` containing `@Bean` methods, or `@Configuration(proxyBeanMethods=false)`, has no such interception. In that case a direct Java call executes the method normally. Prefer passing dependencies as `@Bean` method parameters when method interception is unnecessary.

#### 9. `@Autowired` vs `@Resource` vs `@Inject`. How does Spring resolve ambiguity (`@Primary`, `@Qualifier`, name matching)?
- **Simple Meaning (Dev-Friendly Question):** How does Spring choose which dependency to inject when several beans match?
- **Interview Answer:** `@Autowired` is Spring's type-based injection annotation; Jakarta `@Inject` provides similar standard injection with qualifiers and `@Named`. Jakarta `@Resource` normally looks up a field or setter dependency by name and can fall back to type matching when no explicit name is supplied. For type-based injection, use `@Qualifier` to narrow candidates or `@Primary` to choose a default. Injection-point name matching can help resolve remaining ambiguity when name metadata is available, but explicit qualifiers are clearer.

#### 10. What are `BeanFactoryPostProcessor` and `BeanPostProcessor`? Give a practical use of each.
- **Simple Meaning (Dev-Friendly Question):** How do you change bean setup before creation versus change the created objects?
- **Interview Answer:** A `BeanFactoryPostProcessor` changes bean definitions or factory configuration before regular beans are created, for example resolving property placeholders. Its registry-aware subtype can add definitions, as configuration-class processing does. A `BeanPostProcessor` works on bean instances during their creation, for example injecting annotated fields or creating AOP proxies. Avoid requesting normal beans from factory post-processors because that can create them too early.

#### 11. How does Spring AOP work (JDK dynamic proxy vs CGLIB)? Why does a self-invocation bypass the proxy?
- **Simple Meaning (Dev-Friendly Question):** How does Spring run extra logic around a method, and why does calling it through `this` skip that logic?
- **Interview Answer:** Spring usually wraps a bean in a proxy that runs advice before or after a target method. A JDK proxy exposes interface methods, while a CGLIB proxy subclasses the target class. A target's call to another method on `this` does not re-enter the proxy, so the second method's advice is bypassed. Move the advised operation to another bean or change the calling boundary rather than relying on self-invocation.

#### 12. Spring AOP vs AspectJ: when do you need load-time or compile-time weaving?
- **Simple Meaning (Dev-Friendly Question):** When is a Spring wrapper enough, and when must the class bytecode itself change?
- **Interview Answer:** Spring AOP is usually enough for method-level concerns on Spring-managed beans, such as transactions and caching. AspectJ weaving changes bytecode at build time or class-loading time and can cover self-invocation, constructors, field access, and objects outside Spring. Use it when those interception points are genuinely required. It adds build or runtime configuration, so prefer ordinary proxies for typical service applications.

#### 13. Which Spring features rely on proxies (`@Transactional`, `@Async`, `@Cacheable`, `@Retryable`, `@PreAuthorize`)? What limitations follow (public methods, self-invocation, final classes)?
- **Simple Meaning (Dev-Friendly Question):** Which annotations can silently stop working when a call does not pass through Spring's wrapper?
- **Interview Answer:** Transactions, async execution, caching, enabled retry support, and method security commonly use proxy-based advice. Self-invocation bypasses that advice; a JDK proxy exposes public interface methods, while class-based proxies can intercept eligible overridable methods. Since Spring 6, class-based transactional proxies support protected and package-visible methods by default, so “all advised methods must be public” is too broad. Private methods and final methods cannot be overridden by class proxies, and final classes cannot be subclass-proxied; a final implementation can still be advised through a suitable interface proxy. Check each feature's visibility rules and which `@Retryable` package/version is being used.

#### 14. How do you use `@Conditional`, `@Profile`, and `@ConditionalOnProperty`? How would you write a custom condition?
- **Simple Meaning (Dev-Friendly Question):** How do you create a bean only when a particular setting or environment applies?
- **Interview Answer:** `@Conditional` registers configuration only when its condition matches, and `@Profile` is a condition based on active profiles. Boot's `@ConditionalOnProperty` checks configuration values, with options such as `havingValue` and `matchIfMissing`. For custom logic, implement `Condition.matches()` using the supplied environment and metadata, then attach it with `@Conditional`. Keep conditions about configuration rather than performing slow remote calls or creating regular beans.

#### 15. What are Spring events (`ApplicationEvent`, `@EventListener`, `@TransactionalEventListener`)? Are they synchronous by default? What is the phase problem with transactions?
- **Simple Meaning (Dev-Friendly Question):** How can one part of the application notify another without calling it directly, and when does the listener run?
- **Interview Answer:** Publish an `ApplicationEvent` or an ordinary object, and receive it with `@EventListener`. Ordinary listeners run synchronously by default, so their failure can affect the publisher. `@TransactionalEventListener` binds processing to a transaction phase, defaulting to `AFTER_COMMIT`, and normally does not run without a transaction unless fallback execution is enabled. After-commit work cannot undo the committed transaction, and database writes usually need a new transaction. In-process events are not durable delivery; use an outbox when delivery must survive crashes.

#### 16. What is `@Lazy`, and when does lazy initialisation help or hurt?
- **Simple Meaning (Dev-Friendly Question):** Can Spring wait to create an object until it is actually needed?
- **Interview Answer:** `@Lazy` on a bean delays creation until it is needed, while using it on an injection point supplies a lazy-resolving proxy. It can reduce startup work for rarely used features. It can also hide configuration failures until a real request and increase first-use latency. A lazy bean directly needed by an eager bean may still be created during startup, so it is not an unconditional delay.

#### 17. What is the difference between `@Value`, `@ConfigurationProperties`, and `Environment`?
- **Simple Meaning (Dev-Friendly Question):** Which API should read a single setting, a group of settings, or a setting chosen at runtime?
- **Interview Answer:** `@Value` suits a small number of individual values and can evaluate expressions. `@ConfigurationProperties` binds related settings into a typed object with relaxed naming and validation support, making it better for application configuration. `Environment` lets code look up properties or profiles programmatically. Prefer validated configuration records for important settings instead of scattering string keys throughout services.

#### 18. How does Spring handle `Optional`, `ObjectProvider`, and `List<T>` / `Map<String, T>` injection?
- **Simple Meaning (Dev-Friendly Question):** How do you inject an optional bean, fetch one later, or receive every implementation?
- **Interview Answer:** `Optional<T>` expresses that a matching dependency may be absent, but it does not resolve multiple matching candidates. `ObjectProvider<T>` allows delayed lookup, optional lookup, and iteration over candidates. `List<T>` injects matching beans, with ordering supported through `@Order` or `Ordered`; `Map<String, T>` uses bean names as keys. Use provider lookups when scope or delayed creation matters, and handle missing or ambiguous candidates deliberately.

#### 19. What is the `BeanRegistrar` / programmatic bean registration API in Framework 7, and when would you use it over `@Bean` or `ImportBeanDefinitionRegistrar`?
- **Simple Meaning (Dev-Friendly Question):** How can Spring 7 register a variable set of beans using normal Java conditions and loops?
- **Interview Answer:** Framework 7 introduces `BeanRegistrar`, whose `register(BeanRegistry, Environment)` method describes beans programmatically. It is typically imported with `@Import` and supports settings such as scope, lazy creation, and suppliers, including AOT support. Use it when bean registration naturally depends on loops or environment checks. Keep `@Bean` for simple fixed definitions, and use the lower-level `ImportBeanDefinitionRegistrar` when you specifically need its definition-level or importing-class metadata control.

## 2. Spring Boot Fundamentals

#### 1. What does `@SpringBootApplication` combine? How does component scanning decide what to load?
- **Simple Meaning (Dev-Friendly Question):** What does the annotation on the main class switch on?
- **Interview Answer:** It combines `@SpringBootConfiguration`, `@EnableAutoConfiguration`, and `@ComponentScan`. By default, component scanning starts at the annotated class's package and includes its subpackages, subject to filters and conditions. Place the main class in a sensible root package or configure scan packages explicitly. JPA entity and repository scanning have their own rules, so changing component scan alone does not necessarily change them.

#### 2. How does auto-configuration work end to end (`AutoConfiguration.imports`, `@ConditionalOnClass`, `@ConditionalOnMissingBean`, ordering)?
- **Simple Meaning (Dev-Friendly Question):** How does Boot decide which useful default beans to create?
- **Interview Answer:** Boot discovers auto-configuration candidates from `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Conditions check available classes, properties, application type, and existing beans. `@ConditionalOnMissingBean` lets a user bean replace a default when that auto-configuration supports backing off. Before/after ordering controls how auto-configuration definitions are considered, not the general runtime order in which all beans are instantiated.

#### 3. How do you debug why an auto-configuration did or did not apply (`--debug`, conditions report, `/actuator/conditions`)?
- **Simple Meaning (Dev-Friendly Question):** How do you find the reason Boot created or skipped a default bean?
- **Interview Answer:** Enable `--debug` to print the condition evaluation report and inspect positive and negative matches. Check classpath dependencies, property values, excluded configurations, and user-defined beans that caused defaults to back off. The secured `/actuator/conditions` endpoint helps inspect a running application. This report explains configuration decisions; it is not a bean timing profiler.

#### 4. How do you write a custom starter? What goes in the autoconfigure module vs the starter module?
- **Simple Meaning (Dev-Friendly Question):** How do you package reusable Boot defaults so another service can add one dependency?
- **Interview Answer:** Put conditional bean definitions, typed properties, and the auto-configuration imports entry in the autoconfigure module. The starter module mainly brings that module and the needed libraries together as dependencies. Let applications override defaults and avoid broad component scanning inside the library. Test matching and non-matching conditions with `ApplicationContextRunner`, and maintain compatible starter lines for major Boot versions.

#### 5. Explain externalised configuration and property source precedence (command line, env vars, `application-{profile}.yml`, config tree, Config Server).
- **Simple Meaning (Dev-Friendly Question):** If the same setting exists in several places, which value wins?
- **Interview Answer:** External configuration lets the same build run in different environments. In the usual production sources, command-line arguments override Java system properties, which override OS environment variables, which override Config Data. Within normal Config Data locations, external files and profile-specific files can override packaged defaults; imported config trees or Config Server data follow Config Data import and location ordering rather than one universal extra priority. Use `spring.config.import` for these sources and inspect the effective value and its origin when debugging conflicts.

#### 6. How do you bind configuration safely (`@ConfigurationProperties`, records, validation, relaxed binding, `@ConfigurationPropertiesScan`)?
- **Simple Meaning (Dev-Friendly Question):** How do you turn settings into a typed object and reject bad values at startup?
- **Interview Answer:** Bind a prefix to a `@ConfigurationProperties` class or record and register it with scanning or `@EnableConfigurationProperties`. Relaxed binding maps forms such as `base-url` to `baseUrl`, and Spring converts types such as durations. Add `@Validated` and Bean Validation constraints, using nested `@Valid` where needed. Keep the holder immutable and free of service logic, and ensure the validation provider is present.

#### 7. How do profiles work? How do you avoid profile sprawl, and what are the risks of `spring.profiles.active` set in the wrong place?
- **Simple Meaning (Dev-Friendly Question):** How do you switch environment-specific settings without creating dozens of profile combinations?
- **Interview Answer:** Profiles activate selected beans and configuration documents; profile-specific configuration adds to or overrides shared defaults. Use a small set of environment profiles and separate feature flags instead of encoding every feature combination as a profile. Set active profiles through deployment configuration rather than hard-coding production choices in the application. Do not put `spring.profiles.active` in a profile-specific or profile-activated document, where Boot does not allow it.

#### 8. How does the embedded server work (Tomcat, Jetty, Undertow)? How do you tune threads, connections, and timeouts?
- **Simple Meaning (Dev-Friendly Question):** How can the application start its own HTTP server, and which capacity settings matter?
- **Interview Answer:** Boot configures a server factory and starts the embedded server as part of the web application context lifecycle. Boot 3 supports Tomcat, Jetty, and Undertow for servlet applications; Boot 4.0 dropped Undertow support because of the Servlet 6.1 baseline. Tune server-specific worker/executor settings, maximum connections, backlog, and timeouts using measurements rather than copying arbitrary values. With a virtual-thread executor, traditional worker-pool limits may no longer control concurrency, so also protect downstream pools and services.

#### 9. What is the fat JAR layout? How do layered JARs help Docker builds?
- **Simple Meaning (Dev-Friendly Question):** How does one executable archive carry the application and its libraries, and why split image layers?
- **Interview Answer:** A Boot executable JAR normally stores application classes under `BOOT-INF/classes`, dependencies under `BOOT-INF/lib`, and launcher classes at the archive root. The launcher can load these nested dependency JARs. Layer metadata separates stable dependencies from frequently changing application code so image builds can reuse cached layers. The image build must actually extract or use those layers; metadata alone does not split a copied JAR into Docker layers.

#### 10. What are the `CommandLineRunner`, `ApplicationRunner`, and `SmartLifecycle` hooks? When do you use each?
- **Simple Meaning (Dev-Friendly Question):** Which hook should run one-time startup work, and which should manage a long-lived component?
- **Interview Answer:** Runners execute after context refresh but before the ready event; `CommandLineRunner` receives raw arguments and `ApplicationRunner` receives parsed arguments. Use them for bounded startup tasks that must finish before readiness, such as cache warm-up. `SmartLifecycle` manages start/stop behavior and phase ordering for components such as message consumers, with auto-start during context refresh. Runners can be ordered with `@Order`, while lifecycle phases start low to high and stop high to low, subject to dependencies.

#### 11. How do you implement graceful shutdown, and how does it interact with Kubernetes readiness and `preStop`?
- **Simple Meaning (Dev-Friendly Question):** How do you stop a pod without cutting off requests already being handled?
- **Interview Answer:** Enable or verify `server.shutdown=graceful` for your Boot version and configure `spring.lifecycle.timeout-per-shutdown-phase`. During normal closure, Boot refuses readiness and the server stops accepting new work while existing requests get time to finish. Kubernetes endpoint removal and traffic draining take time, so a carefully designed `preStop` can provide a propagation window before SIGTERM. The pod termination grace period must include the hook, draining, and cleanup time; `preStop` time is not free extra time.

#### 12. What is Spring Boot Actuator? Which endpoints do you expose in production and how do you secure them?
- **Simple Meaning (Dev-Friendly Question):** How do operators inspect an application safely while it is running?
- **Interview Answer:** Actuator exposes operational information such as health, metrics, and selected diagnostics. Usually expose only required endpoints, for example health and a metrics scrape endpoint, and restrict detailed diagnostics. Use network controls and explicit Spring Security rules, especially when defining your own security chain. Never expose environment values, heap dumps, or configuration details publicly just because they help troubleshooting.

#### 13. How do health groups, liveness and readiness probes work in Spring Boot? What should and should not be in readiness?
- **Simple Meaning (Dev-Friendly Question):** How do you tell Kubernetes whether to restart a pod or merely stop sending traffic to it?
- **Interview Answer:** Health groups combine selected health contributors, including Boot's liveness and readiness state indicators. A failing liveness probe can restart a pod; a failing readiness probe removes it from service traffic without restarting it. Keep shared external dependency checks out of liveness to avoid restart storms. Add a dependency to readiness only when its failure really makes this instance unable to serve useful traffic; Boot does not automatically add every external dependency. Where possible probe the main server port so a healthy management port does not hide a broken application connector.

#### 14. How do you customise logging (Logback, structured logging, MDC, correlation IDs)?
- **Simple Meaning (Dev-Friendly Question):** How do you produce useful logs and connect all messages from one request?
- **Interview Answer:** Configure levels and Logback appenders centrally, usually writing structured logs to standard output. Put trace IDs or validated correlation IDs in MDC and include them in the log format, with cleanup in `finally` blocks. Propagate context explicitly across executors and outbound calls rather than assuming thread-local values follow the work. Avoid secrets and personal data, and use built-in structured logging only where your Boot version supports it or provide an appropriate encoder.

#### 15. How do you reduce startup time and memory (lazy init, AOT, CDS, GraalVM native image, class data sharing)?
- **Simple Meaning (Dev-Friendly Question):** Which changes make the service start faster without moving failures into real requests?
- **Interview Answer:** Measure first, remove unused dependencies and scanning, and avoid slow external work during bean creation. Lazy initialization defers work but can delay failures and first-request responses rather than reducing total work. AOT generates setup code ahead of time; CDS, meaning class data sharing, reduces some JVM class-loading work and can share class metadata pages. Native images often improve startup and memory use but require compatible libraries and reflection/resource hints, so test startup, throughput, and behavior on the actual deployment.

#### 16. How do you manage multiple environments and secrets (Config Server, Kubernetes ConfigMaps/Secrets, Key Vault, `spring.config.import`)?
- **Simple Meaning (Dev-Friendly Question):** How do you reuse one application build while keeping passwords outside source control?
- **Interview Answer:** Keep safe defaults in the application and supply environment differences through deployment configuration, Config Server, mounted files, or a managed secret store. Use supported `spring.config.import` integrations, such as `configtree:` for mounted files, rather than assuming every secret store has a built-in import prefix. Prefer workload identity for accessing Key Vault and apply least privilege and rotation. Kubernetes Secrets are base64-encoded, not automatically a complete encryption/security solution, and imported values do not automatically refresh every bean at runtime.

#### 17. What is `DevTools`, and why must it not ship to production?
- **Simple Meaning (Dev-Friendly Question):** What helps local development but should be absent from the production runtime?
- **Interview Answer:** DevTools provides development conveniences such as classpath-triggered restarts and development-oriented defaults. Its restart classloader and optional remote features are unnecessary operational and security risks in production. Keep it development-only and verify the packaged artifact excludes it; Boot's repackaging normally does so. Do not depend only on a runtime disabled state when the dependency can be left out entirely.

## 3. Application Startup Lifecycle

#### 1. Walk through `SpringApplication.run()` step by step, from `main()` to `ApplicationReadyEvent`.
- **Simple Meaning (Dev-Friendly Question):** What happens from launching the Java process until Boot declares it ready?
- **Interview Answer:** Boot creates early bootstrap support, publishes starting events, prepares configuration, and creates the appropriate application context. It applies initializers, loads the primary sources, then refreshes the context to process definitions, create beans, and start lifecycle components including the web server. After refresh it publishes `ApplicationStartedEvent`, then liveness `CORRECT`, and invokes ordered runners. Finally it publishes `ApplicationReadyEvent`, followed by readiness `ACCEPTING_TRAFFIC`. The listening server can exist before readiness, so traffic routing must respect readiness checks.

#### 2. What happens in the `SpringApplication` constructor (web application type deduction from the classpath, initialisers, listeners, main class detection)?
- **Simple Meaning (Dev-Friendly Question):** What setup happens before `run()` even starts?
- **Interview Answer:** The constructor stores the primary sources and deduces the application type from classpath markers. It loads configured bootstrap initializers, application context initializers, and application listeners, commonly through factory metadata, and tries to identify the main application class. This prepares the launcher but does not refresh the context or create ordinary application beans. The inferred web type and other defaults can be overridden before calling `run()`.

#### 3. What is the bootstrap context (`BootstrapRegistryInitializer`, `DefaultBootstrapContext`), when does it exist, and who uses it (Config Data, Config Server)?
- **Simple Meaning (Dev-Friendly Question):** Where can Boot share helper objects before the normal Spring container exists?
- **Interview Answer:** The bootstrap context is a small early registry created at the start of `run()`, before the application context is available. `BootstrapRegistryInitializer` registers suppliers for objects that early configuration loading may need. Config Data integrations, including compatible Config Server support, can use it to share clients or other expensive setup objects. It closes during application context preparation and is not the same thing as Spring Cloud's optional legacy bootstrap application context.

#### 4. List the `SpringApplication` run events in order (`ApplicationStartingEvent`, `ApplicationEnvironmentPreparedEvent`, `ApplicationContextInitializedEvent`, `ApplicationPreparedEvent`, `ContextRefreshedEvent`, `WebServerInitializedEvent`, `ApplicationStartedEvent`, `AvailabilityChangeEvent`, `ApplicationReadyEvent`, `ApplicationFailedEvent`). Which ones can a normal `@EventListener` bean receive, and why can't it receive the early ones?
- **Simple Meaning (Dev-Friendly Question):** In what order are startup milestones announced, and which listeners exist early enough to hear them?
- **Interview Answer:** For a normal embedded servlet Boot 3 startup: starting → environment prepared → context initialized → prepared → servlet web server initialized during lifecycle startup → context refreshed → started → liveness `CORRECT` → runners → ready → readiness `ACCEPTING_TRAFFIC`. `ApplicationFailedEvent` is an alternative failure path, not a final success event; child contexts can add more context/server events. Normal `@EventListener` beans can receive the refresh/server and later events once listener processing is registered, but cannot receive the earlier launcher events because their beans are not ready yet. Register an `ApplicationListener` with `SpringApplication` or factory metadata to receive early events. An environment-prepared listener itself triggers environment post-processors, so listener ordering affects which configuration an early listener sees.

#### 5. How is the `Environment` prepared? Who loads `application.yml` (`ConfigDataEnvironmentPostProcessor`), when are profiles activated, and how do you add a custom `EnvironmentPostProcessor`?
- **Simple Meaning (Dev-Friendly Question):** How does Boot discover settings and profiles before creating application beans?
- **Interview Answer:** Boot builds the environment and initial property sources, including system properties, environment variables, and command-line arguments. Publishing `ApplicationEnvironmentPreparedEvent` invokes Boot's environment-post-processing listener; that listener runs processors including `ConfigDataEnvironmentPostProcessor`, which loads configuration documents and resolves profile activation and imports. Register a custom `EnvironmentPostProcessor` in `META-INF/spring.factories` and order it deliberately relative to Config Data processing. It is not a normal component-scanned bean, and Boot 4 changes its main API package, so use the key for your version.

#### 6. Which application context class is created for a servlet web app, and what is different for reactive and non-web apps?
- **Simple Meaning (Dev-Friendly Question):** Does Boot use the same kind of container for every application?
- **Interview Answer:** A standard servlet application uses `AnnotationConfigServletWebServerApplicationContext`, which manages an embedded servlet server. A reactive application uses `AnnotationConfigReactiveWebServerApplicationContext`, and a non-web application normally uses `AnnotationConfigApplicationContext`. MVC usually wins by default when both MVC and WebFlux are present, allowing MVC applications to use `WebClient`. These defaults can be changed through the application context factory or explicit web application type.

#### 7. What does `prepareContext` do (apply initialisers, register the primary source, load bean definitions) before the refresh starts?
- **Simple Meaning (Dev-Friendly Question):** What is put into the container before Spring starts building its beans?
- **Interview Answer:** Boot attaches the environment, performs context setup, applies initializers, and publishes the context-initialized milestone before loading sources. It registers supporting objects such as application arguments and loads primary sources as bean definitions. It then publishes `ApplicationPreparedEvent` before refresh begins. This is not yet a complete scan of every configuration class; that expansion happens through factory post-processing during refresh.

#### 8. Explain `AbstractApplicationContext.refresh()` step by step (`prepareRefresh`, `obtainFreshBeanFactory`, `prepareBeanFactory`, `postProcessBeanFactory`, `invokeBeanFactoryPostProcessors`, `registerBeanPostProcessors`, `initMessageSource`, `initApplicationEventMulticaster`, `onRefresh`, `registerListeners`, `finishBeanFactoryInitialization`, `finishRefresh`).
- **Simple Meaning (Dev-Friendly Question):** How does Spring turn bean definitions into a working application context?
- **Interview Answer:** `prepareRefresh` sets up refresh state, `obtainFreshBeanFactory` obtains the factory, and `prepareBeanFactory` plus `postProcessBeanFactory` configure it. `invokeBeanFactoryPostProcessors` expands or changes definitions, then `registerBeanPostProcessors` installs instance-processing infrastructure. Spring initializes the message source and event multicaster, invokes `onRefresh` for context-specific work such as web server creation, and registers listeners. `finishBeanFactoryInitialization` completes factory setup and creates the remaining non-lazy singletons. `finishRefresh` starts auto-start lifecycle components before publishing `ContextRefreshedEvent`; for embedded servlet Boot, server startup publishes its initialized event during that lifecycle phase.

#### 9. When does `ConfigurationClassPostProcessor` run, and how do `@ComponentScan`, `@Import`, `@Bean`, and auto-configuration imports turn into bean definitions before any regular bean is created?
- **Simple Meaning (Dev-Friendly Question):** When does Spring discover the classes and factory methods it will later instantiate?
- **Interview Answer:** `ConfigurationClassPostProcessor` runs during `invokeBeanFactoryPostProcessors` as a bean-definition registry post-processor. It parses configuration classes, scans components, follows imports, and registers definitions for `@Bean` methods. Deferred import processing brings in Boot auto-configuration candidates and applies relevant conditions. This normally precedes ordinary singleton creation, but infrastructure or incorrectly written post-processors can request beans early, so “before any bean exists” is too absolute.

#### 10. When are `BeanPostProcessor`s registered? Why do beans created too early not get proxied (the "not eligible for getting processed by all BeanPostProcessors" warning)?
- **Simple Meaning (Dev-Friendly Question):** Why can a bean miss transactions or other processing if something creates it too soon?
- **Interview Answer:** Spring registers bean post-processors after factory post-processors have run and before normal eager singleton creation. A bean requested while this registration is still happening sees only the processors installed at that point. It may therefore miss later advice or other processing, producing the warning, although the warning does not prove that every proxy is missing. Keep post-processors independent of ordinary services and use appropriate static factory methods for post-processor beans to avoid prematurely creating configuration objects.

#### 11. When are non-lazy singleton beans instantiated, and what changes with `spring.main.lazy-initialization=true`?
- **Simple Meaning (Dev-Friendly Question):** Which beans are created at startup and which wait until first use?
- **Interview Answer:** Remaining non-lazy singleton beans are normally instantiated during `finishBeanFactoryInitialization`, although dependencies and infrastructure can be created earlier. Boot's global lazy initialization marks eligible definitions lazy, moving their creation to first use. Infrastructure and beans required by eager components may still initialize during startup. This improves the measured startup time but can delay failures and require extra first-request capacity.

#### 12. When and where is the embedded Tomcat created, and when does it actually start accepting connections? Why does Boot separate "web server created" from "web server started"?
- **Simple Meaning (Dev-Friendly Question):** Why can logs say Tomcat is initialized before the application is ready to serve requests?
- **Interview Answer:** The servlet web application context creates the server through its factory during `onRefresh`. Tomcat initializes its container and servlet registrations then, but Boot defers normal connector request processing until the web-server lifecycle starts during `finishRefresh`. That start publishes `ServletWebServerInitializedEvent` before the context's refreshed event in the usual Boot 3 embedded path. This avoids serving the application before eager bean initialization completes, but runners and readiness still come later. Socket binding details can differ from accepting and processing application traffic, so those should not be treated as the same milestone.

#### 13. When is `DispatcherServlet` registered with Tomcat, and when is it initialised? When are `@RequestMapping` handler mappings built? Why can the first request be slower than later ones?
- **Simple Meaning (Dev-Friendly Question):** Is the MVC front controller fully initialized before the first HTTP request?
- **Interview Answer:** Boot registers `DispatcherServlet` through servlet registration infrastructure when the embedded servlet context is initialized. Its servlet `init()` may wait until the first matching request unless load-on-startup is configured for eager initialization, for example with `spring.mvc.servlet.load-on-startup`. `RequestMappingHandlerMapping` normally discovers controller mappings when its bean initializes during context refresh, independently of the servlet's lazy `init()`. Global lazy initialization can defer more of that work. Servlet setup, lazy beans, caches, connections, and JVM warm-up can all make the first request slower.

#### 14. Put these in order: `@PostConstruct`, `InitializingBean.afterPropertiesSet`, `@Bean(initMethod)`, `SmartInitializingSingleton.afterSingletonsInstantiated`, `ContextRefreshedEvent`, `SmartLifecycle.start()`, `ApplicationRunner` / `CommandLineRunner`, `ApplicationReadyEvent`.
- **Simple Meaning (Dev-Friendly Question):** Which bean, context, and Boot callbacks run first?
- **Interview Answer:** For a normal eager singleton: `@PostConstruct` → `afterPropertiesSet()` → configured init method. After ordinary eager singletons are instantiated, Spring calls applicable `SmartInitializingSingleton.afterSingletonsInstantiated()` callbacks. During `finishRefresh`, auto-start `SmartLifecycle.start()` runs before `ContextRefreshedEvent`; Boot then runs the application/command-line runners and publishes `ApplicationReadyEvent`. Lifecycle phases and dependencies determine component ordering, and lazy beans do not all fit into the eager-singleton sequence.

#### 15. Where would you put each of these: cache warm-up, schema/config validation, starting a Kafka listener, registering with service discovery, a startup smoke test? Compare `@PostConstruct`, runners, `SmartLifecycle`, and `ApplicationReadyEvent`.
- **Simple Meaning (Dev-Friendly Question):** Which startup hook fits each real production task?
- **Interview Answer:** Keep `@PostConstruct` for local bean setup, and validate configuration through validated properties; use migration tooling for schema changes. Use a runner for bounded required cache warm-up or a startup smoke test that should fail startup before readiness if it fails. Use the Kafka container's lifecycle support for listeners and `SmartLifecycle` for other managed start/stop components. Use the discovery integration's lifecycle and readiness facilities rather than advertising usable service too early. `ApplicationReadyEvent` suits notifications after runners, but its synchronous listeners still run before Boot publishes accepting-traffic readiness and can delay that transition.

#### 16. How do liveness and readiness states change during startup and shutdown (`LivenessState`, `ReadinessState`, `AvailabilityChangeEvent`), and how does that map to Kubernetes probes?
- **Simple Meaning (Dev-Friendly Question):** When should Kubernetes restart the process versus route requests to it?
- **Interview Answer:** Before successful startup, availability defaults are liveness `BROKEN` and readiness `REFUSING_TRAFFIC`. After `ApplicationStartedEvent`, Boot publishes liveness `CORRECT`; after runners and `ApplicationReadyEvent`, it publishes readiness `ACCEPTING_TRAFFIC`. On normal shutdown, Boot changes readiness back to refusing traffic as context closure begins; graceful shutdown does not require marking liveness broken. Kubernetes readiness stops routing and liveness can trigger restarts, while a startup probe protects slow-starting applications from premature liveness failures. Availability states inform health checks and routing; they are not themselves a universal HTTP request gate.

#### 17. What happens when startup fails (`FailureAnalyzer`, `ApplicationFailedEvent`, exit code, context closure)? How do you write a custom failure analyser?
- **Simple Meaning (Dev-Friendly Question):** How does Boot clean up a failed startup and explain the cause?
- **Interview Answer:** Boot handles startup exceptions by reporting failure, publishing `ApplicationFailedEvent` when possible, and closing an existing context to release managed resources. Failure analyzers can turn known exceptions into a useful description and suggested action; their reporting order should not be confused with the success-event sequence. Implement `FailureAnalyzer` or extend `AbstractFailureAnalyzer<T>` and register it through `META-INF/spring.factories`. The failure is rethrown, so an uncaught failure from the main entry point normally produces a non-zero process exit; custom exit handling can use Boot's exit-code contracts.

#### 18. What happens if a runner throws an exception or blocks for a long time? What does that do to readiness?
- **Simple Meaning (Dev-Friendly Question):** Can the server be listening while startup is still incomplete?
- **Interview Answer:** Yes: runners execute after the context and web server have started but before Boot declares readiness. A thrown exception fails startup and leads to failure handling and context closure. A runner that never returns prevents the ready event and normal accepting-traffic transition, even though liveness may already be correct. Bound external calls with timeouts, respect readiness at the traffic router, and avoid moving required warm-up to an untracked background task.

#### 19. How do you measure startup time per bean (`ApplicationStartup`, `BufferingApplicationStartup`, `/actuator/startup`, `--debug`)?
- **Simple Meaning (Dev-Friendly Question):** How do you find which part of startup is actually slow?
- **Interview Answer:** Set an `ApplicationStartup` implementation before running the application; `BufferingApplicationStartup` records timed startup steps and tags such as bean names. Expose the secured `/actuator/startup` endpoint to inspect those buffered steps. Nested bean creation means a step's elapsed time may include dependency creation, so do not simply add every duration. Use Java Flight Recorder for class loading, CPU, and allocation detail; `--debug` explains conditions rather than providing a complete per-bean timing profile.

#### 20. Describe the shutdown sequence after SIGTERM (shutdown hook, `ContextClosedEvent`, `SmartLifecycle` stop in reverse phase order, graceful web server shutdown, `@PreDestroy`, `DisposableBean`). Where does readiness change to `REFUSING_TRAFFIC`?
- **Simple Meaning (Dev-Friendly Question):** In what order does Boot stop traffic, stop background work, and destroy beans?
- **Interview Answer:** With the normal shutdown hook enabled, SIGTERM initiates context closure and a `ContextClosedEvent`; Boot's availability handling changes readiness to `REFUSING_TRAFFIC` as closure starts. The lifecycle processor stops components from higher to lower phases, subject to dependencies, including the web server's graceful-drain lifecycle when configured. It then destroys managed singletons, normally invoking `@PreDestroy`, `DisposableBean.destroy()`, and configured destroy callbacks. Graceful shutdown allows active requests only within the configured timeout, so align it with the platform's total termination grace period. SIGKILL does not allow these callbacks, and liveness need not become broken for an orderly stop.

#### 21. What gets created differently when `management.server.port` differs from `server.port` (child management context)?
- **Simple Meaning (Dev-Friendly Question):** What changes if health and metrics use their own HTTP port?
- **Interview Answer:** Boot normally creates a separate child management application context and embedded server for the different management port. It has management-specific web infrastructure and can see relevant beans from its parent, while the parent does not generally see child-only beans. Context and server events can therefore occur more than once, and child events can reach parent listeners. A healthy management connector does not prove the main connector can handle traffic, so expose probe paths on the main port when appropriate.

#### 22. How do AOT processing, native images, CDS, lazy init, and Boot 4's modular auto-configuration change the startup path?
- **Simple Meaning (Dev-Friendly Question):** Which optimizations remove startup work, defer it, or move it to build time?
- **Interview Answer:** AOT moves supported bean setup and metadata work to the build, while native images compile ahead of time and need explicit support for dynamic behavior. CDS reuses archived class metadata on the JVM; lazy initialization moves eligible object creation to first use rather than eliminating it. Boot 4 splits technology support into smaller modules, so the chosen starters determine which configuration is available on the classpath. These change discovery cost and available features, not the need to initialize dependencies and coordinate lifecycle/readiness correctly. AOT-generated bean arrangements cannot be assumed to support arbitrary runtime changes to build-time conditions.

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
         - publish ApplicationEnvironmentPreparedEvent
             -> Boot listener invokes EnvironmentPostProcessors
             -> Config Data loads application.yml, profiles, spring.config.import
      3. Print banner
      4. Create ApplicationContext (servlet web server application context)
      5. prepareContext
           - set environment, apply initialisers, ApplicationContextInitializedEvent
           - register main class as a bean source
           - ApplicationPreparedEvent
      6. refreshContext -> refresh()
           - invokeBeanFactoryPostProcessors (component scan, @Import, auto-config, @Bean definitions)
             - registerBeanPostProcessors (install injection and auto-proxy processors;
                 individual beans are processed/proxied as they are created)
           - onRefresh: create embedded web server (Tomcat set up, not yet accepting traffic)
           - finishBeanFactoryInitialization: instantiate non-lazy singletons
                 (@PostConstruct, afterPropertiesSet, initMethod run per bean)
             - finishRefresh: auto-start SmartLifecycle beans start in phase order
                 -> web-server lifecycle starts connectors and publishes ServletWebServerInitializedEvent
                 -> ContextRefreshedEvent after lifecycle startup completes
        7. ApplicationStartedEvent -> AvailabilityChangeEvent(LivenessState.CORRECT)
             - readiness still REFUSING_TRAFFIC, although the server can already receive requests
      8. callRunners (ApplicationRunner / CommandLineRunner by @Order)
     9. ApplicationReadyEvent -> AvailabilityChangeEvent(ReadinessState.ACCEPTING_TRAFFIC)
 On startup failure: publish ApplicationFailedEvent when possible, close any created context,
     report through FailureAnalyzers/logging and rethrow (normally a non-zero process exit)
```

## 4. Request Journey: Tomcat to RestController

#### 1. Trace a `GET /api/orders/42` request from the client socket to your `@RestController` method and back, naming each component on the way.
- **Simple Meaning (Dev-Friendly Question):** Which layers touch a request before and after the controller runs?
- **Interview Answer:** Tomcat accepts the connection, parses HTTP, and routes through its Engine, Host, Context, and servlet Wrapper pipeline. The selected servlet's filter chain, including Spring Security when configured, runs before `DispatcherServlet`. MVC finds a handler and adapter, runs interceptor `preHandle`, resolves arguments, and invokes the controller and any proxied services. Return-value handlers and message converters write the response, then successful interceptor callbacks and outer filters unwind. Exceptions follow the relevant MVC, security, or container error path rather than always taking the success route.

#### 2. What happens inside Tomcat before your code runs (Acceptor, Poller, worker thread pool, `Http11Processor`, `CoyoteAdapter`, Engine / Host / Context / Wrapper pipeline)? What do `maxThreads`, `acceptCount`, `maxConnections`, and `connectionTimeout` control?
- **Simple Meaning (Dev-Friendly Question):** How does Tomcat turn a socket into a servlet call, and where can requests queue up?
- **Interview Answer:** For a typical NIO HTTP/1.1 connector, the Acceptor accepts sockets, the Poller monitors readiness, and an executor runs processing through `Http11Processor` and `CoyoteAdapter` into the container pipeline. `maxThreads` limits workers for the default platform-thread executor, while `maxConnections` limits accepted connections being handled. `acceptCount` controls the requested OS connection backlog when the server cannot accept more connections, not a universal application request queue. `connectionTimeout` covers waiting for request input under connector rules, not a maximum controller execution time. Custom executors, virtual threads, and HTTP/2 change parts of this model.

#### 3. Which thread executes your controller method? Explain the thread-per-request model and what changes with virtual threads.
- **Simple Meaning (Dev-Friendly Question):** Does every HTTP request occupy a real OS thread while it waits on a database?
- **Interview Answer:** A synchronous MVC controller normally executes on Tomcat's request worker thread, which stays occupied during blocking calls. With supported Boot/server configuration and Java 21+, a virtual thread can run the request instead, letting many blocking waits share fewer platform threads. This does not turn MVC into reactive code or make CPU-heavy work faster. Bound database connections and downstream concurrency because cheap threads do not create unlimited backend capacity.

#### 4. How does the container route the request to `DispatcherServlet` (servlet mapping `/`)? Where do static resources, `/error`, and actuator endpoints fit?
- **Simple Meaning (Dev-Friendly Question):** How does Tomcat know whether a URL belongs to MVC, a static file, or management functionality?
- **Interview Answer:** Tomcat chooses a servlet using the web application's servlet mappings, with Boot's `DispatcherServlet` commonly mapped to `/`. MVC then selects controller methods or resource handlers; static files are often served through MVC's resource handling rather than a separate controller. Boot commonly maps `/error` to `BasicErrorController` for container error dispatches. Servlet Actuator uses dedicated endpoint handler mappings on the same server, or separate management web infrastructure when configured on another port. Security filters can reject a request before any of these handlers run.

#### 5. List the typical filters in a Boot MVC application in order (character encoding, form content, request context, observation / metrics, Spring Security filter chain, others). Who decides the order (`FilterRegistrationBean`, `@Order`, `OrderedFilter` constants)?
- **Simple Meaning (Dev-Friendly Question):** Which filters run first, and is their order the same in every Boot application?
- **Interview Answer:** There is no single fixed list: in a common Boot 3 setup, character encoding is very early, observation is also early, then form-content and request-context processing precede the usual security proxy order. Actual filters depend on dependencies, properties, and custom registrations, so inspect the registrations for the exact release. Use `FilterRegistrationBean.setOrder()`, Boot's `OrderedFilter` conventions, or supported `Ordered`/`@Order` registration to control container filter order. Security's internal filter order is separate and is controlled through `HttpSecurity`, not by applying `@Order` to each security filter.

#### 6. How is Spring Security plugged in (`DelegatingFilterProxy`, `FilterChainProxy`, `SecurityFilterChain`)? How is that different from an ordinary servlet filter?
- **Simple Meaning (Dev-Friendly Question):** How does one servlet filter connect to Spring's larger security pipeline?
- **Interview Answer:** The container calls `DelegatingFilterProxy`, which delegates to the Spring bean normally named `springSecurityFilterChain`. That bean is a `FilterChainProxy` containing ordered `SecurityFilterChain` definitions. It picks the first matching chain and runs that chain's authentication, authorization, and related filters before continuing to the servlet chain. An ordinary servlet filter does not automatically provide this request-matching security-chain selection or Spring-managed security lifecycle.

#### 7. What does `DispatcherServlet.doDispatch()` do step by step (multipart check, `getHandler`, `getHandlerAdapter`, interceptors, `handle`, view/response processing, exception resolution)?
- **Simple Meaning (Dev-Friendly Question):** What does MVC's front controller actually do for each request?
- **Interview Answer:** It checks/wraps multipart requests, obtains a handler execution chain, chooses a handler adapter, and runs interceptor `preHandle` callbacks. The adapter resolves arguments, invokes the handler, and processes its return value; a REST body may already be written at this stage. For synchronous success, MVC invokes `postHandle`, then renders a view if one is needed. It resolves dispatch exceptions through configured exception resolvers and finally invokes completion callbacks for participating interceptors. Async processing can exit this sequence early and resume on a later dispatch, and multipart resources need cleanup.

#### 8. How does `RequestMappingHandlerMapping` pick the handler (path, method, params, headers, consumes, produces, version)? Which status codes result when matching fails (404, 405, 415, 406)?
- **Simple Meaning (Dev-Friendly Question):** How does Spring select the correct controller method and explain why no method matches?
- **Interview Answer:** MVC compares path, HTTP method, required parameters/headers, input content type, and acceptable output types, then chooses the most specific match. No suitable path normally leads to 404, a method mismatch to 405, unsupported request media to 415, and no acceptable response media to 406. Missing required parameter conditions can produce 400, so matching failures are not limited to those four codes. Framework 7 adds native version conditions when versioning is configured; an unsupported requested version is rejected with 400 by default. Security and custom exception handling can alter the final response.

#### 9. What is a `HandlerAdapter`, and why does Spring MVC need it (strategy pattern, different handler types)?
- **Simple Meaning (Dev-Friendly Question):** How can the same front controller invoke different styles of handler?
- **Interview Answer:** A handler adapter knows how to invoke a supported handler type. `DispatcherServlet` finds the handler, then asks an appropriate adapter to execute it without hard-coding every handler API. `RequestMappingHandlerAdapter` handles annotated controller methods, including argument resolution and return-value handling. Other adapters support other handler styles, making this a strategy-based extension point.

#### 10. How do `HandlerInterceptor.preHandle`, `postHandle`, and `afterCompletion` work? What is the call order with several interceptors, and what happens when `preHandle` returns false or the controller throws?
- **Simple Meaning (Dev-Friendly Question):** Which interceptor callbacks run on success, rejection, and failure?
- **Interview Answer:** `preHandle` runs in registration order; after synchronous success, `postHandle` and `afterCompletion` run in reverse order. If an interceptor returns false, later interceptors and the controller are skipped, and completion runs only for earlier interceptors whose `preHandle` returned true. A controller exception skips normal `postHandle`, then MVC attempts exception resolution and runs applicable completion callbacks. `afterCompletion` can receive a null exception when a resolver has already handled it. Async handling has additional callbacks and redispatch behavior, so it is not just the synchronous sequence on another thread.

#### 11. How are controller arguments resolved (`HandlerMethodArgumentResolver`, `@RequestBody` through `HttpMessageConverter`, `@PathVariable`, `@RequestParam`, `ConversionService`, data binding, `@Valid`)?
- **Simple Meaning (Dev-Friendly Question):** How do URL values and JSON become Java parameters before the method is called?
- **Interview Answer:** `RequestMappingHandlerAdapter` selects a `HandlerMethodArgumentResolver` for each parameter. Path and query values are extracted and converted with conversion and binding support; model attributes bind multiple request values to an object. `@RequestBody` is read by an appropriate message converter, such as Jackson for JSON. Applicable validation runs after binding/deserialization, and failures can prevent controller invocation. Use dedicated input DTOs and limit bindable fields instead of binding persistence entities directly.

#### 12. How is the return value written (`HandlerMethodReturnValueHandler`, `RequestResponseBodyMethodProcessor`, content negotiation using `Accept`, Jackson converter, `ResponseBodyAdvice`)?
- **Simple Meaning (Dev-Friendly Question):** How does a returned Java object become the HTTP response body?
- **Interview Answer:** A return-value handler interprets the method's return type and annotations. For `@ResponseBody`, including `@RestController`, `RequestResponseBodyMethodProcessor` normally handles the body; `ResponseEntity` uses its own handler. MVC chooses a compatible response media type using `Accept`, mapping declarations, and available converters. `ResponseBodyAdvice` can adjust the body before the selected converter writes it, with Jackson commonly producing JSON. For REST responses this can happen before interceptor `postHandle`, so that callback may be too late to change the body.

#### 13. Where does validation run, and which exceptions occur for `@RequestBody` validation vs `@RequestParam` / `@PathVariable` validation?
- **Simple Meaning (Dev-Friendly Question):** Why do different invalid controller inputs produce different exception types?
- **Interview Answer:** An individually validated `@Valid @RequestBody` normally fails with `MethodArgumentNotValidException` after deserialization. In Framework 6.1+, built-in MVC method validation for constraints directly on parameters or return values raises `HandlerMethodValidationException`, and can supersede individual argument validation. Older or class-level `@Validated` proxy-based method validation commonly raises `ConstraintViolationException`, depending on its configuration. Malformed JSON or failed type conversion is a reading/binding error, not necessarily a Bean Validation error. Handle the relevant paths for your version and avoid assuming every validation exception means the same client error; invalid return values are server-side failures.

#### 14. If the controller throws an exception, who handles it, and in what order do the `HandlerExceptionResolver`s run (`ExceptionHandlerExceptionResolver`, `ResponseStatusExceptionResolver`, `DefaultHandlerExceptionResolver`)? What happens if nobody handles it (`/error`, `BasicErrorController`, error dispatch)?
- **Simple Meaning (Dev-Friendly Question):** Who gets the first chance to turn a controller failure into an HTTP response?
- **Interview Answer:** In the standard MVC resolver chain, `ExceptionHandlerExceptionResolver` tries controller-local and applicable advice `@ExceptionHandler` methods first. Then `ResponseStatusExceptionResolver` handles status exceptions/annotations, and `DefaultHandlerExceptionResolver` handles standard MVC exceptions. Boot or application configuration can add resolvers or change ordering. If the failure remains unhandled, it propagates to the servlet container, which can make an ERROR dispatch to Boot's `/error` and `BasicErrorController`. A resolver can also call `sendError`, and already-committed responses limit what any error handler can change.

#### 15. Why does `@ControllerAdvice` not catch an exception thrown in a servlet filter or Spring Security filter? How do you handle it properly (`AuthenticationEntryPoint`, `AccessDeniedHandler`, `HandlerExceptionResolver` delegation, try/catch in the filter)?
- **Simple Meaning (Dev-Friendly Question):** Why does a global MVC error handler miss authentication or custom-filter failures?
- **Interview Answer:** MVC advice is invoked through `DispatcherServlet` exception resolution, so it does not automatically wrap filters that run outside that servlet. Configure Spring Security's `AuthenticationEntryPoint` for authentication failures and `AccessDeniedHandler` for authorization failures in the appropriate security path. In a custom filter, catch its own failures and either write a consistent response or deliberately delegate to a suitable `HandlerExceptionResolver`. Security exception translation only handles applicable downstream exceptions, so a filter positioned before it may need its own handler. Do not write a second error body after the response has been committed.

#### 16. Where do AOP proxies (`@Transactional`, `@PreAuthorize`, `@Cacheable`) sit in the controller to service to repository call chain? Is the controller itself usually proxied?
- **Simple Meaning (Dev-Friendly Question):** At which method boundaries does Spring run transaction, security, or cache logic?
- **Interview Answer:** Advice surrounds calls to eligible proxied beans, commonly a service transaction/security/cache boundary and repository infrastructure. A controller is not proxied merely because it has `@RestController`; it can be proxied if applicable advice targets it. Calls must pass through the proxy, so self-invocation bypasses proxy advice. The order of multiple interceptors is configured rather than implied by annotation placement, and framework-generated repository proxies may add their own behavior.

#### 17. Where does the transaction begin and end for a typical request? With Open Session In View, where does the `EntityManager` open and close, and how does that affect lazy loading during JSON serialisation?
- **Simple Meaning (Dev-Friendly Question):** Is the database transaction open for the entire HTTP request?
- **Interview Answer:** A typical service transaction starts when the call enters its transactional proxy and commits or rolls back before that call returns to the controller. With servlet Open EntityManager In View enabled, an `EntityManager` can be bound for the wider request, opened before handler work and closed at request completion. That longer persistence-context lifetime is not the same as a transaction spanning the whole request. It can allow serialization to trigger lazy SQL after the service transaction has ended, causing hidden queries and inconsistent snapshots. Prefer fetching the needed data and mapping response DTOs inside the intended service boundary.

#### 18. Order these for an incoming and outgoing request: servlet filter, Spring Security filter, `HandlerInterceptor`, `@ControllerAdvice`, an AOP aspect around the controller, and the `@Transactional` interceptor on a service.
- **Simple Meaning (Dev-Friendly Question):** Which wrapper is outside which, and where does error advice fit?
- **Interview Answer:** In a typical setup: outer servlet filters → security's filters → MVC interceptor `preHandle` → controller AOP advice, if present → controller → service transaction advice → service. On success, the transaction finishes before the controller returns, controller advice unwinds, MVC writes the return value, and interceptor completion and outer filters finish. `@ControllerAdvice` exception handlers are not an unconditional wrapper; MVC invokes them only when resolving relevant failures. Other advice methods, such as `@InitBinder` or `ResponseBodyAdvice`, participate at their own stages. Exact filter/advisor ordering and asynchronous processing can change the details.

#### 19. How are `RequestContextHolder`, `SecurityContextHolder`, `LocaleContextHolder`, and MDC bound to the thread? What breaks when you hop threads (`@Async`, `CompletableFuture`, executors)?
- **Simple Meaning (Dev-Friendly Question):** Why can a background task lose the current user, request, locale, or trace ID?
- **Interview Answer:** Servlet/framework infrastructure binds request and locale context, security filters manage the security context, and logging/tracing code populates MDC, usually using thread-local storage. Those values do not automatically follow arbitrary executor tasks or `CompletableFuture` work. Use supported security/context propagation, task decorators, or explicit immutable values, and always clear context when tasks finish. Avoid carrying an entire live request into work that outlives the request, and do not assume inheritable thread-locals are safe with reusable pools. A thread-bound database transaction also does not automatically move with the task.

#### 20. How does async processing change the journey (`Callable`, `DeferredResult`, async dispatch, second pass through filters, SSE, async timeouts)?
- **Simple Meaning (Dev-Friendly Question):** How can MVC release its request thread while the HTTP response is still pending?
- **Interview Answer:** MVC can start servlet async processing, release the initial container thread, and complete work through a configured executor for `Callable` or application-supplied completion for `DeferredResult`. When a result is available, an ASYNC dispatch normally resumes MVC processing and response writing. Only appropriately mapped filters run on that dispatch, and `OncePerRequestFilter` has additional async-dispatch rules, so it is not always a repeat of every filter. Async interceptors receive lifecycle-specific callbacks; configure timeouts, error handling, executor limits, and context propagation. SSE keeps a streaming response open for multiple events and must handle disconnects and buffering rather than just one delayed result.

#### 21. How do keep-alive, HTTP/2, compression, and timeouts work at Tomcat level (`connectionTimeout`, `keepAliveTimeout`, `maxKeepAliveRequests`)? What is a client-side vs server-side timeout?
- **Simple Meaning (Dev-Friendly Question):** How does the server reuse connections and decide when to stop waiting?
- **Interview Answer:** HTTP/1.1 keep-alive reuses a connection; `keepAliveTimeout` limits idle waiting for the next request and `maxKeepAliveRequests` caps reuse under connector rules. HTTP/2 multiplexes streams on one connection and has additional stream/protocol settings. Compression reduces selected response bodies but costs CPU and should be configured by content type and size. `connectionTimeout` is not a general application execution deadline, and MVC async, upstream proxies, and outbound clients have separate timeouts. A client timeout stops that client's waiting but does not guarantee cancellation of server-side or downstream work.

#### 22. Where do metrics, tracing, and access logs hook in (observation filter, `WebMvcMetrics`, Tomcat access log, trace propagation, MDC)?
- **Simple Meaning (Dev-Friendly Question):** Where do you measure a request and connect it to logs and downstream calls?
- **Interview Answer:** In Boot 3 / Framework 6, servlet observation infrastructure such as `ServerHttpObservationFilter` records HTTP observations that metrics and tracing handlers can consume. Older `WebMvcMetrics` terminology should not be assumed to name the current implementation. Tracing integrations extract incoming trace context and propagate it through instrumented outbound clients; MDC can add trace IDs to logs when logging correlation is configured. Tomcat access logs record at the container boundary, while application observations provide Spring-aware request information. Avoid high-cardinality metric labels such as raw IDs, and check async/error coverage in the chosen instrumentation.

#### 23. How does the same request differ in WebFlux on Netty (event loop, `DispatcherHandler`, `WebFilter`, no servlet, no thread-per-request)?
- **Simple Meaning (Dev-Friendly Question):** What changes when HTTP requests run through a reactive server instead of servlet MVC?
- **Interview Answer:** With WebFlux on Reactor Netty, event-loop threads handle network events and the request passes through `WebFilter` and `DispatcherHandler`, not Tomcat's servlet filter chain and `DispatcherServlet`. Handlers return reactive results and use reactive codecs to read/write data. Work can switch threads, so Reactor context replaces assumptions about a single request thread, including reactive security and transactions. Do not block event loops with JDBC or other blocking calls; use non-blocking clients or deliberately offload bounded blocking work. WebFlux can also run on other supported servers, so “WebFlux always means Netty” is too broad.

#### 24. How does a multipart upload flow through the stack (`MultipartResolver`, size limits, Tomcat `maxPostSize` / `maxSwallowSize`, memory vs disk, streaming)?
- **Simple Meaning (Dev-Friendly Question):** How does an uploaded file reach the controller without exhausting memory or disk?
- **Interview Answer:** A servlet multipart resolver typically delegates parsing to the container's multipart support and exposes uploaded parts to MVC parameters. Configure per-file and total request limits, temporary storage, and the memory-to-disk threshold through Boot multipart settings. Tomcat's `maxPostSize` concerns supported form parameter parsing limits and is not a general replacement for upload limits; `maxSwallowSize` limits discarded unread data after an aborted upload. Read file streams rather than copying all content into byte arrays, and clean up temporary files. Servlet multipart parsing may already spool content before the controller runs, so true end-to-end streaming needs deliberate design and aligned proxy limits.

#### 25. Where do you add a new cross-cutting concern: servlet filter, interceptor, AOP aspect, or controller advice? State your decision criteria.
- **Simple Meaning (Dev-Friendly Question):** Which extension point is closest to the information and scope the feature needs?
- **Interview Answer:** Choose a servlet filter for container-level HTTP wrapping or concerns that must run before MVC, such as request observation. Choose an MVC interceptor when handler metadata and MVC lifecycle callbacks matter. Choose AOP for service/method concerns that should also apply outside HTTP, and controller advice for MVC binding, response-body customization, or exception translation. Use Spring Security's facilities for authentication and authorization instead of inventing an interceptor-based substitute. Check async/error dispatch coverage and avoid implementing the same concern twice.

#### 26. How does API versioning (Framework 7) fit into handler mapping and the request journey?
- **Simple Meaning (Dev-Friendly Question):** At what point does Spring 7 use the requested API version to select a controller method?
- **Interview Answer:** Configure MVC versioning through `WebMvcConfigurer.configureApiVersioning`, choosing a header, query parameter, path segment, media-type parameter, or custom resolver. The resulting version strategy resolves and validates the version, and handler mapping compares it with a mapping's `version` condition alongside path and HTTP method conditions. Unsupported versions normally produce 400 rather than automatically falling back to an arbitrary handler. Supported-version and deprecation handling can be configured centrally. This is native Framework 7 functionality, not a Framework 6 mapping attribute.

### Answer Outline: Request Journey (for self-check)

```text
Client
  │  TCP / TLS / HTTP
  ▼
Tomcat Connector (NIO)
    - Acceptor accepts socket, Poller monitors socket readiness
    - executor worker takes over (platform pool by default; virtual threads when configured)
    - for HTTP/1.1: Http11Processor parses HTTP -> CoyoteAdapter adapts Request/Response
  ▼
Tomcat container pipeline: Engine -> Host -> Context -> Wrapper (DispatcherServlet)
  ▼
Servlet filter chain (in order)
    - commonly encoding, observation/metrics, form content, request context ...
        (exact registrations/order depend on Boot version and application configuration)
  - Spring Security: DelegatingFilterProxy -> FilterChainProxy -> SecurityFilterChain
        (context holder, CSRF, authentication, exception translation, authorization)
  ▼
DispatcherServlet.doDispatch()
    0. check multipart request and wrap if needed
  1. getHandler        -> HandlerExecutionChain (handler method + interceptors)
  2. getHandlerAdapter -> RequestMappingHandlerAdapter
  3. interceptors preHandle (in order)
  4. adapter.handle    -> resolve arguments (path vars, @RequestBody via HttpMessageConverter, @Valid)
  5. invoke controller method (may call AOP-proxied services: security, transaction, cache)
  6. return value handling -> content negotiation -> HttpMessageConverter writes the body
  7. on synchronous success: interceptors postHandle (reverse order); render view if needed
  8. on dispatch exception instead: HandlerExceptionResolvers
         -> local/applicable @ControllerAdvice handlers -> ResponseStatus -> default
  9. interceptors afterCompletion (reverse order for participating interceptors), multipart cleanup
      (async handling exits early and normally resumes through a later ASYNC dispatch)
  ▼
Servlet filters unwind (reverse order)
  ▼
Tomcat writes the response, keeps the connection alive or closes it
  ▼
Client

Exceptions originating in filters are outside normal MVC exception resolution;
handle them in the appropriate filter/security handler or deliberately delegate to a resolver.
Unhandled servlet failures or sendError responses can trigger a container ERROR dispatch
to /error (BasicErrorController), subject to registration and response-commit state.
```

## 5. Spring MVC and REST

#### 1. `@Controller` vs `@RestController`. `@RequestBody`, `@RequestParam`, `@PathVariable`, `@ModelAttribute`, `@RequestHeader`: when do you use each?
- **Simple Meaning (Dev-Friendly Question):** Which annotations return web pages or data, and where does each input value come from?
- **Interview Answer:** `@Controller` supports MVC views, while `@RestController` combines it with `@ResponseBody` so return values are normally written to the response body. Use `@RequestBody` for content such as JSON, `@RequestParam` for query/form values, and `@PathVariable` for values in the URL path. Use `@ModelAttribute` to bind request parameters to an object, often for forms or search filters. Use `@RequestHeader` for headers, and apply explicit conversion, validation, and safe binding rules to all input.

#### 2. How do you design global exception handling (`@ControllerAdvice`, `ProblemDetail`, RFC 7807/9457)?
- **Simple Meaning (Dev-Friendly Question):** How do all controllers return the same clear, safe error format?
- **Interview Answer:** Use `@RestControllerAdvice` with specific `@ExceptionHandler` methods to map known failures to appropriate HTTP status codes. Return `ProblemDetail` with stable type, title, status, detail, and optional safe fields such as validation errors or a trace ID. RFC 9457 supersedes RFC 7807 and defines the current problem-details format, commonly sent as `application/problem+json`. Log unexpected exceptions internally but avoid exposing stack traces, SQL, secrets, or sensitive identifiers. MVC advice does not automatically handle failures originating outside MVC, such as servlet-filter errors.

#### 3. How do you do request validation (Bean Validation, `@Valid`, `@Validated`, groups, custom validators)? What is the difference between `MethodArgumentNotValidException` and `ConstraintViolationException`?
- **Simple Meaning (Dev-Friendly Question):** How do you reject bad input, including nested fields and method parameters?
- **Interview Answer:** Put Bean Validation constraints on input DTOs, use `@Valid` for object and nested validation, and use `@Validated` when selecting validation groups or enabling applicable method validation. Implement a custom constraint and validator for reusable rules that standard annotations cannot express, while keeping business checks in the service. `MethodArgumentNotValidException` normally represents MVC argument-object validation failure; `ConstraintViolationException` commonly comes from proxy-based executable validation. Framework 6.1+ built-in MVC method validation instead uses `HandlerMethodValidationException` for direct method constraints and can supersede individual argument validation. Cover these paths separately and treat return-value validation failures as server errors rather than blaming the caller.

#### 4. Filters vs interceptors vs `@ControllerAdvice` vs AOP: when do you use each?
- **Simple Meaning (Dev-Friendly Question):** Where should shared HTTP logic, controller behavior, error handling, and business-method wrappers live?
- **Interview Answer:** Filters work at the servlet boundary and can wrap request/response processing before MVC selects a handler. Interceptors are MVC-aware callbacks useful when you need the chosen handler's metadata. Controller advice centralizes MVC exception handling, binding setup, model additions, or response-body advice. AOP wraps eligible bean methods, making it appropriate for service transactions or other behavior that should apply outside HTTP too. Use established security infrastructure instead of treating these extension points as interchangeable authentication mechanisms.

#### 5. How do you design REST APIs well (resource naming, status codes, idempotency, pagination, filtering, HATEOAS, versioning)?
- **Simple Meaning (Dev-Friendly Question):** What makes an HTTP API predictable for clients and safe to evolve?
- **Interview Answer:** Model resources with consistent names, use HTTP methods according to their meaning, and return meaningful status codes such as 201 with a location for creation and 404 for a missing resource. Preserve idempotent method semantics and define safe retry behavior for operations such as payments. Use bounded pagination, stable sorting, explicit filtering, and consistent error contracts. Add links with HATEOAS where they help clients navigate, rather than as an automatic requirement for every internal API. Prefer compatible additions, document changes, and version genuine breaking changes with a retirement plan.

#### 6. How do you handle API versioning (URL, header, media type)? What does Framework 7 offer natively?
- **Simple Meaning (Dev-Friendly Question):** How can old and new clients use different API contracts without breaking each other?
- **Interview Answer:** URL versions are visible and easy to route, while header and media-type versions keep resource URLs stable but need deliberate tooling and cache behavior. Keep the strategy consistent and support old contracts for a documented transition period. Framework 7 adds native version resolution from a header, query parameter, path segment, or media-type parameter, plus version conditions on request mappings. Configure it through `WebMvcConfigurer.configureApiVersioning`, including supported versions and optional deprecation behavior. Framework 6 applications need explicit mappings or other custom versioning support instead of assuming those native APIs exist.

#### 7. How do you implement idempotent POST endpoints (idempotency keys)?
- **Simple Meaning (Dev-Friendly Question):** How do you stop a retried payment or order request from creating a second operation?
- **Interview Answer:** Require an idempotency key scoped to the caller and operation, and store a request fingerprint plus processing/result state in a shared durable store. Atomically claim the key using a unique constraint or equivalent coordination so simultaneous requests cannot both execute. Replay the stored outcome for the same key and payload, and reject incompatible payload reuse according to the API contract. Coordinate the idempotency record with the business write and use provider-side idempotency or an outbox where external side effects are involved. Define retention and in-progress recovery; a local cache alone cannot guarantee correctness across instances or crashes.

#### 8. How do you handle large file upload and download (streaming, `StreamingResponseBody`, multipart limits)?
- **Simple Meaning (Dev-Friendly Question):** How do you transfer a large file without holding the entire file in memory?
- **Interview Answer:** Configure multipart file/request limits, disk thresholds, temporary directories, and matching gateway/container limits. Read uploaded content through streams and enforce file-type, size, path-safety, and security checks without trusting the supplied filename. For downloads, use suitable resource responses or `StreamingResponseBody` to copy bounded chunks, with a configured MVC async executor and timeout where required. Servlet multipart uploads may already be spooled before controller execution, and streaming does not remove disk, connection, or bandwidth limits. For very large transfers, consider direct object-storage uploads/downloads through short-lived scoped URLs.

#### 9. How do you customise JSON serialisation (Jackson modules, `@JsonView`, date handling, unknown properties, polymorphism)? What changes with Jackson 3?
- **Simple Meaning (Dev-Friendly Question):** How do you control the JSON contract and avoid surprises when the JSON library changes?
- **Interview Answer:** Configure the mapper and compatible modules centrally, define date/time formats and timezone behavior, and choose an explicit policy for unknown input fields. Use dedicated DTOs and, where useful, `@JsonView` for representations, but do not rely on a view alone as an authorization boundary. For polymorphic input, allow only known subtypes rather than enabling unrestricted type handling. Boot 4 prefers Jackson 3, whose main packages move to `tools.jackson` while Jackson annotations remain under `com.fasterxml.jackson.annotation`; builder/customizer APIs and defaults also change. Review custom modules and Boot's `JsonMapper` configuration, then verify actual JSON contracts instead of treating it as a drop-in package rename.

#### 10. Content negotiation, CORS, and CSRF: how do they work and how do you configure them?
- **Simple Meaning (Dev-Friendly Question):** How does Spring select response formats, allow cross-origin browsers, and prevent unwanted authenticated browser actions?
- **Interview Answer:** Content negotiation uses `Accept`, supported converters, and mapping `produces` declarations; `Content-Type` and `consumes` describe request content. CORS controls which browser origins may read cross-origin responses, so configure explicit origins, methods, headers, and credential policy, including preflight handling with security. CSRF protection checks that state-changing requests using browser-supplied credentials are intentional, commonly through a token. Do not disable CSRF merely because an API uses JSON or is called stateless: automatically sent cookies or other browser credentials still matter. CORS is not authentication and is not a substitute for CSRF protection.

#### 11. `RestTemplate` vs `WebClient` vs `RestClient` vs HTTP interface clients (`@HttpExchange`): which do you use and why?
- **Simple Meaning (Dev-Friendly Question):** Which Spring HTTP client fits blocking, reactive, and declarative calls?
- **Interview Answer:** `RestClient`, available since Framework 6.1, is a modern fluent choice for synchronous calls; `RestTemplate` remains common in older applications but is deprecated in Framework 7. `WebClient` supports non-blocking reactive calls and streaming, especially within WebFlux. HTTP interfaces use `@HttpExchange` to describe a typed contract and delegate to a configured underlying client adapter. Pick based on execution model, streaming needs, and framework version rather than assuming a declarative interface controls whether I/O blocks. Configure pools, timeouts, authentication, and observability on the underlying client.

#### 12. How do you set timeouts, connection pooling, retries, and error handling on outbound HTTP calls?
- **Simple Meaning (Dev-Friendly Question):** How do you prevent a slow or failing dependency from exhausting the service?
- **Interview Answer:** Reuse a configured client and connection pool, with explicit connection, pool-acquisition, and response/read timeouts supported by its implementation. Set an overall deadline because individual I/O timeouts may not bound total work. Retry only suitable transient failures with bounded attempts, backoff, jitter, and respect for rate-limit signals such as `Retry-After`. Retry writes only when their semantics or idempotency protection make that safe, and avoid stacked retries across layers. Map error responses deliberately and measure latency, pool saturation, failures, and retry volume without logging secrets.

#### 13. Spring MVC (blocking) vs WebFlux (reactive): when do you choose each, and how do virtual threads change that decision?
- **Simple Meaning (Dev-Friendly Question):** Should the service use familiar blocking code or a reactive pipeline?
- **Interview Answer:** Choose MVC when the stack is mainly blocking, such as JPA/JDBC, and straightforward imperative code fits the team. Choose WebFlux when the workload benefits from end-to-end non-blocking I/O, many concurrent streams, and reactive composition/backpressure. Virtual threads make blocking MVC much cheaper for many waiting requests, but do not remove database-pool limits or automatically add reactive flow control. Avoid blocking on WebFlux event loops and do not expect either model to accelerate CPU-bound work by itself. Decide using real workload measurements and operational complexity, not framework popularity.

#### 14. How do you implement async request processing (`DeferredResult`, `Callable`, `@Async`, SSE, WebSocket)?
- **Simple Meaning (Dev-Friendly Question):** How do you return a delayed result or maintain a live connection without holding a normal servlet thread throughout?
- **Interview Answer:** MVC `Callable` runs work on its configured async executor, while `DeferredResult` lets application code complete the response later. `@Async` moves eligible bean-method calls to an executor; it does not by itself make every controller response use servlet async processing, and self-invocation bypasses its proxy. SSE provides a long-lived server-to-client HTTP event stream, while WebSocket supports bidirectional messages after the handshake. Configure bounded resources, completion/error callbacks, timeouts, disconnect handling, and context propagation. A pending async request still consumes connections and other resources even when no container worker is waiting on it.

#### 15. How do you document APIs (springdoc-openapi, contract-first) and enforce backward compatibility?
- **Simple Meaning (Dev-Friendly Question):** How do clients know the contract, and how do you catch breaking changes before release?
- **Interview Answer:** Use a compatible springdoc-openapi version to generate an OpenAPI description from the code, or maintain the specification first and implement against it. Document request/response schemas, status codes, security, pagination, and examples rather than only endpoint names. Store the contract in version control and use automated schema diffs, contract tests, and representative consumer tests in CI. Treat field removals, new required inputs, changed meanings, and even new enum values as potentially breaking for existing clients. Avoid exposing interactive documentation publicly in production unless its access is deliberately controlled.

#### 16. How do you implement rate limiting, request tracing, and correlation IDs?
- **Simple Meaning (Dev-Friendly Question):** How do you protect the API from too much traffic and track one request across services?
- **Interview Answer:** Apply an appropriate token-bucket or similar limiter at the gateway or application boundary, keyed by trusted caller/tenant identity rather than an easily spoofed header. Use shared coordination when a limit must apply across instances, and return 429 with useful retry guidance where possible. Use Micrometer/compatible tracing instrumentation and standard trace-context propagation for incoming and outgoing calls. Generate or validate a separate correlation ID when needed, put it in logs/MDC, propagate it deliberately, and clear it afterward. Do not trust a correlation ID for authorization or use request-specific IDs as metric labels.

#### 17. How do you implement ETag / conditional requests and caching headers?
- **Simple Meaning (Dev-Friendly Question):** How can clients avoid downloading unchanged data and prevent overwriting someone else's update?
- **Interview Answer:** Return an ETag for a representation and check `If-None-Match` on reads, replying with 304 and no body when the client's cached representation is still valid. Use `If-Match` with an atomic version check for updates, returning 412 when the precondition fails; weak ETags are not suitable for strong `If-Match` checks. Set `Cache-Control`, freshness, and appropriate `Vary` headers according to whether the data is public, private, or must not be stored. A shallow content-hash ETag can save response bytes without avoiding controller/database work, while a known resource version can support an earlier check. Keep authorization and representation variants in mind so caches do not leak one user's data to another.

## 6. Spring Data JPA and Transactions

#### 1. How does `@Transactional` work internally (proxy, `TransactionInterceptor`, `PlatformTransactionManager`, thread-bound resources)?
- **Simple Meaning (Dev-Friendly Question):** How does Spring start, commit, or roll back a database transaction around a method?
- **Interview Answer:** A call through the Spring proxy reaches `TransactionInterceptor`, which reads the transaction settings and asks a `PlatformTransactionManager` to join or start a transaction. For ordinary JDBC/JPA transactions, resources such as the connection or `EntityManager` are bound to the current thread. Spring commits on success or applies rollback rules on failure, then cleans up. `readOnly = true` is an optimisation hint, not a general guarantee that writes are blocked.

#### 2. Explain propagation levels (`REQUIRED`, `REQUIRES_NEW`, `NESTED`, `SUPPORTS`, `NOT_SUPPORTED`, `MANDATORY`, `NEVER`) with a real use case for `REQUIRES_NEW`.
- **Simple Meaning (Dev-Friendly Question):** What happens when one transactional method calls another?
- **Interview Answer:** `REQUIRED` joins an existing transaction or starts one; `REQUIRES_NEW` suspends the current one and starts an independent transaction. `NESTED` uses a savepoint inside the current transaction when the manager and database support it; it is not generally supported for JPA entity work. `SUPPORTS` joins if one exists, `NOT_SUPPORTED` suspends it, `MANDATORY` requires one, and `NEVER` rejects one. An independent audit record can use `REQUIRES_NEW` so it survives an outer rollback, but the extra connection needs pool capacity.

#### 3. Rollback rules: why does a checked exception not roll back by default? How do you change it?
- **Simple Meaning (Dev-Friendly Question):** Which failures undo database changes, and how can we change that rule?
- **Interview Answer:** By default, Spring rolls back for `RuntimeException` and `Error`, but not checked exceptions, following its traditional assumption that checked exceptions may be recoverable business outcomes. Use `rollbackFor = Exception.class` or a specific checked exception; use `noRollbackFor` for exceptions that should not trigger rollback. A caught exception normally does not reach the interceptor, though the transaction may already have been marked rollback-only.

#### 4. Why does `@Transactional` not work on private methods or self-invocation? How do you fix it?
- **Simple Meaning (Dev-Friendly Question):** Why can a method have the annotation but still run without the expected transaction?
- **Interview Answer:** In the default proxy mode, Spring can intercept only calls that pass through the proxy; private methods cannot be intercepted and calls through `this` bypass it. Put the transaction boundary on an externally called service method or move the transactional work into another bean. `TransactionTemplate` gives explicit control without relying on interception; AspectJ weaving is another option when truly needed.

#### 5. What is the persistence context? Explain first-level cache, dirty checking, flush modes, and `EntityManager` lifecycle.
- **Simple Meaning (Dev-Friendly Question):** How does JPA track loaded objects and decide when to send SQL?
- **Interview Answer:** A persistence context tracks managed entities and acts as a first-level cache, keeping one managed object per entity identity. Dirty checking detects changes and flush sends pending SQL to the database; flush does not mean commit. `AUTO` may flush before relevant queries and at commit, while `COMMIT` generally defers flushing until commit, subject to provider behaviour. Spring usually supplies a shared `EntityManager` proxy backed by the current transaction's context and closes the underlying context when its scope ends.

#### 6. Entity states (transient, managed, detached, removed). `save()` vs `saveAndFlush()` vs `merge()` vs `persist()`.
- **Simple Meaning (Dev-Friendly Question):** What states can an entity be in, and what do the different save methods actually do?
- **Interview Answer:** Transient means new and untracked, managed means tracked, detached means no longer tracked, and removed means scheduled for deletion. `persist()` makes a new entity managed; `merge()` copies state into a managed entity and returns that managed instance, leaving the supplied detached object detached. Spring Data `save()` chooses `persist()` or `merge()` using its new-entity detection. `saveAndFlush()` also flushes pending changes, but does not commit the transaction.

#### 7. What is the N+1 select problem, and how do you fix it (`JOIN FETCH`, `@EntityGraph`, batch size, DTO projections)?
- **Simple Meaning (Dev-Friendly Question):** Why can loading a list unexpectedly run one extra query for every row?
- **Interview Answer:** N+1 happens when one query loads parent entities and later access loads related data separately for each parent. Use a fetch join or `@EntityGraph` for required relationships, batch fetching to group loads, or DTO projections to fetch only needed fields. Check the actual SQL and query count, and avoid collection fetch joins with pagination unless you handle paging separately.

#### 8. `LAZY` vs `EAGER`. What is `LazyInitializationException`, and why is Open Session in View considered an anti-pattern?
- **Simple Meaning (Dev-Friendly Question):** When should related data load, and why can reading it fail after the service returns?
- **Interview Answer:** `LAZY` defers loading where supported, while `EAGER` requires the relationship to be loaded but does not guarantee a single SQL query. Hibernate throws `LazyInitializationException` when an unloaded relationship is accessed without an open session. Open Session in View keeps the persistence context open during web processing, which can hide extra queries and allow database reads during JSON rendering. Prefer fetching the needed data and building DTOs inside the service transaction.

#### 9. How do you map relationships correctly (`@OneToMany` owning side, `mappedBy`, cascade, `orphanRemoval`, bidirectional helper methods)?
- **Simple Meaning (Dev-Friendly Question):** Which side controls a relationship in the database, and how do we keep both Java objects consistent?
- **Interview Answer:** In a common bidirectional one-to-many relationship, the child's `@ManyToOne` owns the foreign key and the parent's `@OneToMany(mappedBy = "parent")` is the inverse side. A unidirectional `@OneToMany` can itself own the relationship. Cascade forwards selected entity operations, while `orphanRemoval` deletes a child removed from its owning parent's collection. Add helper methods that update both sides, and avoid cascading remove into shared entities.

#### 10. Optimistic vs pessimistic locking in JPA (`@Version`, `LockModeType`). How do you handle `OptimisticLockException`?
- **Simple Meaning (Dev-Friendly Question):** How do we stop concurrent updates from silently overwriting each other?
- **Interview Answer:** Optimistic locking uses `@Version` to detect conflicting updates without holding a database lock during the whole operation. Pessimistic locking uses modes such as `PESSIMISTIC_WRITE` to request database locks, which can block or time out. After an optimistic conflict, roll back and either return a conflict response or retry the whole operation in a fresh transaction after reloading the data. Retry only when repeating the business operation is safe.

#### 11. How do you implement pagination and sorting efficiently (`Pageable`, `Slice`, keyset pagination)? Why can `Page` be expensive?
- **Simple Meaning (Dev-Friendly Question):** How do we return small, stable chunks of data without expensive database work?
- **Interview Answer:** Use `Pageable` with a capped page size and a stable, indexed sort that includes a unique tie-breaker. `Page` usually needs an extra count query for totals; `Slice` checks whether another chunk exists without calculating the total. Keyset pagination resumes after the last sort values and avoids large offsets, but does not naturally support arbitrary page jumps. Since Spring Data 3, sorting repository interfaces no longer extend CRUD interfaces; extend both as needed, or use `JpaRepository`.

#### 12. Derived queries vs `@Query` vs Specifications vs Querydsl vs native queries: when do you use each?
- **Simple Meaning (Dev-Friendly Question):** Which query style should we choose as search requirements become more complex?
- **Interview Answer:** Use derived method names for simple fixed conditions and `@Query` for explicit JPQL that would make the method name unwieldy. Specifications suit composable filters through the Criteria API, while Querydsl offers a type-safe query API with generated query types. Use native SQL for database-specific features or carefully tuned queries, accepting more database coupling and mapping work.

#### 13. What are projections (interface, class-based, dynamic)? When do they reduce load?
- **Simple Meaning (Dev-Friendly Question):** Can we fetch only the fields an API needs instead of complete entities?
- **Interview Answer:** Interface projections expose selected getters, and class-based projections return DTOs or records. Dynamic projections let a repository method select the result type through a `Class<T>` argument. They reduce database and mapping work when the generated query selects fewer columns or avoids loading full entities. Nested or expression-based projections may still fetch more data, so verify the SQL.

#### 14. How do you handle bulk operations and batch inserts (`saveAll`, `hibernate.jdbc.batch_size`, `@Modifying`, `IDENTITY` generator limitations)?
- **Simple Meaning (Dev-Friendly Question):** How do we write many rows without making one expensive database round trip per row?
- **Interview Answer:** `saveAll()` is not automatically one bulk SQL statement; configure JDBC batching and use suitable identifier generation to group inserts. Hibernate's `IDENTITY` generation generally prevents normal insert batching because it needs each generated ID immediately. For large inputs, flush and clear in chunks to control memory. `@Modifying` update/delete queries bypass normal entity dirty checking and callbacks, so flush before them as needed and clear or refresh stale managed entities afterwards.

#### 15. How do you configure HikariCP (pool size, timeouts, leak detection)? How do you size a pool?
- **Simple Meaning (Dev-Friendly Question):** How many database connections should the app keep, and how do we detect connection trouble?
- **Interview Answer:** Configure `spring.datasource.hikari` settings such as maximum pool size, connection wait timeout, idle timeout, and maximum lifetime. Keep connection lifetime below infrastructure limits, and use leak detection to investigate connections held too long, not as an automatic repair. Size the pool with load tests and database capacity, counting connections across all replicas. Watch active connections, waiting callers, query time, and timeouts before increasing the pool.

#### 16. How do you manage schema changes (Flyway/Liquibase, `ddl-auto` risks in production)?
- **Simple Meaning (Dev-Friendly Question):** How do we safely change database tables between releases?
- **Interview Answer:** Use version-controlled Flyway migrations or Liquibase changesets and test them against realistic existing data. In production, prefer `ddl-auto=validate` or `none` rather than automatic `update`, `create`, or `create-drop`. For rolling releases, use expand-and-contract changes so old and new application versions can both work during deployment.

#### 17. How do you implement auditing (`@CreatedDate`, `@LastModifiedBy`, Envers)?
- **Simple Meaning (Dev-Friendly Question):** How do we record who changed a row, when they changed it, and possibly its full history?
- **Interview Answer:** Enable JPA auditing and register `AuditingEntityListener` to populate fields such as `@CreatedDate`, `@LastModifiedDate`, and `@LastModifiedBy`. Supply `AuditorAware` to resolve the current user, including a system identity for background work. These fields describe creation and the latest change; Hibernate Envers stores revision history when you need previous entity versions. Bulk updates can bypass entity auditing callbacks.

#### 18. How do you manage multiple data sources or read/write splitting?
- **Simple Meaning (Dev-Friendly Question):** How do we choose the right database and transaction manager for each operation?
- **Interview Answer:** Define separate data sources and, for JPA, the matching entity manager factories, repository packages, and transaction managers. Select the transaction manager explicitly where needed, or route connections before the transaction obtains one. Read replicas can lag, so send reads needing immediate consistency to the primary. Separate local transaction managers do not give atomic commits across databases, and `readOnly` alone does not implement routing.

#### 19. `JdbcTemplate` vs `JdbcClient` vs JPA: when do you drop below JPA?
- **Simple Meaning (Dev-Friendly Question):** When is direct SQL easier or faster than entity-based persistence?
- **Interview Answer:** JPA suits entity relationships and change tracking, while `JdbcTemplate` gives direct SQL with Spring's resource handling and exception translation. `JdbcClient`, introduced in Framework 6.1, offers a fluent API for common JDBC queries and updates. Use JDBC for reports, complex SQL, or performance-sensitive operations where entity management adds little value. Keep transaction boundaries clear and account for stale JPA state if you mix JDBC and managed entities.

#### 20. How do you use `@TransactionalEventListener` to publish events only after commit? How does it relate to the outbox pattern?
- **Simple Meaning (Dev-Friendly Question):** How do we react only to successful database changes without losing important events?
- **Interview Answer:** Publish the application event inside the transaction and use `@TransactionalEventListener(phase = AFTER_COMMIT)` to run the listener after a successful commit. By default, the listener is not called when there is no transaction. This is not durable delivery: a process crash after commit can lose the event, and listener failure cannot undo the committed data. For reliable messaging, write an outbox row in the same transaction and relay it with retries and duplicate-safe consumers; new database writes in an after-commit listener need a separate transaction.

#### 21. What happens to a transaction when you call an `@Async` method or a new thread from a transactional method?
- **Simple Meaning (Dev-Friendly Question):** Does background work share the caller's database transaction?
- **Interview Answer:** Ordinary JDBC/JPA transactions are thread-bound and do not propagate to an `@Async` worker or a newly created thread. The worker needs its own transaction boundary, and its failure does not automatically roll back the caller. It can also run before the caller commits, so pass IDs rather than managed entities and use after-commit triggering or an outbox when ordering matters. Propagating logging or security context does not propagate the transaction.

## 7. Spring Security

#### 1. Explain the Spring Security architecture (`DelegatingFilterProxy`, `FilterChainProxy`, `SecurityFilterChain`, `AuthenticationManager`, `SecurityContextHolder`).
- **Simple Meaning (Dev-Friendly Question):** Which Spring components check a request's identity and permissions?
- **Interview Answer:** In a servlet app, `DelegatingFilterProxy` connects the container's filter chain to Spring's `FilterChainProxy`. It selects the first matching `SecurityFilterChain`, whose filters handle tasks such as authentication and authorisation. `AuthenticationManager` delegates authentication to suitable providers, and `SecurityContextHolder` holds the current security context, usually per thread. WebFlux uses a different, reactive security chain and context model.

#### 2. How do you configure security in Spring Security 6+ (`SecurityFilterChain` bean, lambda DSL) now that `WebSecurityConfigurerAdapter` is gone?
- **Simple Meaning (Dev-Friendly Question):** How do we declare modern Spring Security rules without extending an adapter class?
- **Interview Answer:** Declare a `SecurityFilterChain` bean, configure `HttpSecurity` with the lambda DSL, and return `http.build()`. Set request rules, login or resource-server authentication, session policy, CORS, and CSRF based on the client type. For multiple chains, use explicit matchers and ordering, with a final catch-all chain so requests are not accidentally unprotected. Enable method security separately when service methods need permission checks.

#### 3. Authentication vs authorisation. How do `AuthenticationProvider`, `UserDetailsService`, and `PasswordEncoder` fit together?
- **Simple Meaning (Dev-Friendly Question):** How do we prove who a user is and then decide what they may do?
- **Interview Answer:** Authentication verifies identity; authorisation checks whether that identity has permission for an action. In username/password authentication, a provider such as `DaoAuthenticationProvider` loads account details through `UserDetailsService` and checks the submitted password with `PasswordEncoder`. The resulting authenticated object contains authorities used by request and method rules. Token-based providers do not necessarily use a user-details lookup or password encoder.

#### 4. How do you implement stateless JWT authentication? Where do you validate tokens, and how do you handle refresh and revocation?
- **Simple Meaning (Dev-Friendly Question):** How does an API safely trust a bearer token without storing a login session?
- **Interview Answer:** Use Spring Security's OAuth2 resource-server support to validate bearer tokens before controller code runs. Check the signature with trusted keys and allowed algorithms, issuer, expiry/not-before, and the intended audience; configure audience validation explicitly rather than assuming issuer configuration covers it. The authorisation server issues and rotates refresh tokens, not the resource API. Use short-lived access tokens and, where needed, a denylist or opaque-token introspection because a self-contained JWT has no automatic instant revocation.

#### 5. OAuth2 roles: client, resource server, authorisation server. How do you configure a resource server with JWT or opaque tokens?
- **Simple Meaning (Dev-Friendly Question):** Who issues tokens, who uses them, and who protects the API?
- **Interview Answer:** The client obtains tokens, the authorisation server issues them, and the resource server validates them to protect data. Configure JWT resource-server support with a trusted issuer or decoder and the required claim validators. For opaque tokens, configure an introspection endpoint and client credentials so the API can ask the authorisation server whether a token is active. JWT validation is usually local, while introspection adds a network dependency and can reflect central revocation.

#### 6. Authorization Code with PKCE vs Client Credentials: when do you use each?
- **Simple Meaning (Dev-Friendly Question):** Which OAuth2 flow suits a person logging in, and which suits a background service?
- **Interview Answer:** Use Authorization Code with PKCE for user-facing clients; the verifier binds the token exchange to the client that started the flow and helps stop stolen-code use. Public clients such as SPAs cannot safely store a client secret. Use Client Credentials for a confidential service acting as itself, without a logged-in user. Grant narrow scopes and do not treat a service token as proof of a user's identity.

#### 7. How do you secure service-to-service calls (client credentials, token relay, mTLS)?
- **Simple Meaning (Dev-Friendly Question):** How do internal services prove their identity and avoid sharing too much access?
- **Interview Answer:** Use Client Credentials when a service acts as itself, or relay a user token only when the downstream service should act for that user and accepts its audience and scopes. Every downstream service must validate the token and enforce its own permissions. mTLS authenticates the connecting workload and protects transport, but does not replace business authorisation. Rotate credentials and certificates, use short-lived tokens, and never write them to logs.

#### 8. Method security: `@PreAuthorize`, `@PostAuthorize`, `@Secured`, SpEL expressions. What are the proxy limitations?
- **Simple Meaning (Dev-Friendly Question):** How do we enforce permissions at the service-method level?
- **Interview Answer:** Enable `@EnableMethodSecurity`; use `@PreAuthorize` to check before a call and `@PostAuthorize` to check the returned result. Expressions can inspect authorities, method arguments, and ownership; enable `securedEnabled = true` for `@Secured`. In proxy mode, self-invocation and private methods bypass interception, and class proxies cannot override final methods. Do not rely on a post-call check to prevent a write or an external side effect that has already happened.

#### 9. CSRF: when can you disable it, and when must you not? How do SPAs and cookies change that?
- **Simple Meaning (Dev-Friendly Question):** When can another site trick a browser into sending an authenticated write request?
- **Interview Answer:** CSRF matters when browsers automatically attach credentials, especially session or authentication cookies. Keep CSRF protection for cookie-authenticated applications, including SPAs, and send the expected CSRF token on unsafe requests. Disabling it can be reasonable for endpoints authenticated only by an explicitly supplied bearer header, with no automatically attached authentication credentials. Stateless sessions alone do not make an application CSRF-safe; a JWT in a cookie can still be vulnerable.

#### 10. CORS vs CSRF vs XSS: how does each attack work and what is the Spring mitigation?
- **Simple Meaning (Dev-Friendly Question):** What is the difference between cross-origin access rules, forged requests, and injected browser scripts?
- **Interview Answer:** CORS is a browser cross-origin access policy, not an attack; configure a narrow origin allowlist and handle preflight before authentication rejects it. CSRF tricks a browser into authenticated actions, so use Spring Security CSRF tokens and appropriate cookie settings; CORS is not CSRF protection. XSS runs untrusted script in your site's origin, so use safe output encoding, sanitise permitted HTML, and add a suitable Content Security Policy. Security headers help but do not replace safe handling of user content.

#### 11. How do you implement RBAC vs ABAC, and how do you map JWT claims to authorities?
- **Simple Meaning (Dev-Friendly Question):** Should permissions depend on roles, resource ownership, or other request details?
- **Interview Answer:** RBAC grants access through roles such as admin, while ABAC checks attributes such as tenant, ownership, or account state. Use a JWT authentication converter to map trusted role or permission claims into Spring authorities; scopes normally become `SCOPE_` authorities. `hasRole("ADMIN")` normally checks `ROLE_ADMIN`, while `hasAuthority` checks the exact value. Combine broad role checks with resource-level rules rather than trusting IDs supplied by the caller.

#### 12. How do you handle password storage (BCrypt, Argon2, `DelegatingPasswordEncoder`, upgrade strategy)?
- **Simple Meaning (Dev-Friendly Question):** How do we store passwords safely and improve old hashes over time?
- **Interview Answer:** Store salted adaptive password hashes using an algorithm such as BCrypt or Argon2, never plaintext or reversible encryption. Tune the work cost on production-like hardware so verification is deliberately expensive but still usable. `DelegatingPasswordEncoder` uses an algorithm prefix to support existing hashes alongside the preferred encoder. After a successful login, rehash with the new settings when an upgrade is needed, and avoid logging passwords.

#### 13. How do you secure Actuator, H2 console, and Swagger UI in production?
- **Simple Meaning (Dev-Friendly Question):** How do we stop operational and developer tools from becoming public entry points?
- **Interview Answer:** Expose only necessary Actuator endpoints and restrict sensitive ones by network and authentication, keeping public health details minimal. Disable the H2 console in production. Disable Swagger UI if not needed or protect it and the API specification with suitable access rules. If a custom `SecurityFilterChain` is present, explicitly protect management endpoints instead of assuming Boot still supplies all default rules.

#### 14. How do you implement multi-tenancy security (tenant claim, tenant-scoped queries, filters)?
- **Simple Meaning (Dev-Friendly Question):** How do we ensure one customer can never read or change another customer's data?
- **Interview Answer:** Resolve the tenant from validated identity information and verify membership rather than trusting an arbitrary request header. Enforce tenant scope in database queries, including reads by ID, updates, deletes, joins, and background jobs. Include tenant identity in cache keys and propagate it safely across async boundaries, clearing thread-local values afterwards. Test cross-tenant access explicitly; a web filter alone is not enough to secure every data path.

#### 15. How do you audit security events (`AuthenticationSuccessEvent`, failures, access denied)?
- **Simple Meaning (Dev-Friendly Question):** How do we record logins, failed attempts, and denied actions without leaking secrets?
- **Interview Answer:** Configure authentication event publishing and listen for success and failure events; record access-denied outcomes through authorisation events or handlers as appropriate. Capture time, user or client identity, action, result, and trace ID, but never passwords or bearer tokens. Send audit records to protected storage with a clear retention policy and alert on suspicious patterns. Verify which events your actual authentication flow publishes rather than assuming every path is covered.

#### 16. How do you protect against brute force, token theft, and session fixation?
- **Simple Meaning (Dev-Friendly Question):** How do we make repeated guessing, stolen credentials, and reused sessions harder to exploit?
- **Interview Answer:** Rate-limit authentication attempts, detect suspicious activity, use MFA where appropriate, and avoid error messages that reveal whether an account exists. Reduce token theft risk with TLS, short lifetimes, secure storage, and `Secure`/`HttpOnly` cookies when using cookies, along with CSRF protection. Spring Security changes the session ID on authentication to defend against session fixation when session-based login is used. Rotate refresh tokens and invalidate sessions or credentials after suspicious activity.

#### 17. How do you test secured endpoints (`@WithMockUser`, `jwt()` post-processor, `@WithUserDetails`)?
- **Simple Meaning (Dev-Friendly Question):** How do we prove both allowed and forbidden requests behave correctly?
- **Interview Answer:** Use `@WithMockUser` for a simple test identity, `@WithUserDetails` to load a configured user, and MockMvc's `jwt()` support for claims and authorities. Test anonymous access, missing permissions, ownership, tenant boundaries, and CSRF on cookie-authenticated writes. These helpers inject an identity and do not prove that real JWT signature or claim validation works. Add tests through the actual decoder for invalid signature, issuer, audience, and expired tokens.

## 8. Testing

#### 1. Test slices: `@WebMvcTest`, `@DataJpaTest`, `@JsonTest`, `@RestClientTest`, `@SpringBootTest`. When do you use each, and what gets loaded?
- **Simple Meaning (Dev-Friendly Question):** How much of Spring should a test start for the behaviour being checked?
- **Interview Answer:** `@WebMvcTest` loads selected MVC components and web test infrastructure, not the full service layer; mock its collaborators. `@DataJpaTest` loads JPA entities, repositories, and database support, usually with rollback; `@JsonTest` focuses on JSON mapping. `@RestClientTest` loads supported outbound REST-client configuration and mock-server support. `@SpringBootTest` loads the full Boot context and starts a real server only when configured to do so, so reserve it for wiring and integration behaviour.

#### 2. `@MockBean` / `@MockitoBean` vs `@Mock` vs `@SpyBean`: differences, context caching impact.
- **Simple Meaning (Dev-Friendly Question):** Which mocks live only in a unit test, and which replace beans inside Spring?
- **Interview Answer:** Mockito's `@Mock` creates a test-local mock and does not replace a Spring bean; initialise it with Mockito's extension or equivalent setup. `@MockitoBean`, available since Framework 6.2, replaces or creates a bean in the test context, while `@MockitoSpyBean` wraps an existing bean and calls real methods unless stubbed. Boot's older `@MockBean` and `@SpyBean` were deprecated in Boot 3.4 and removed in Boot 4. Different mock definitions can produce different context cache keys, so keep shared test setups consistent.

#### 3. How does Spring's test context caching work, and what breaks it (`@MockBean` combinations, `@DirtiesContext`)?
- **Simple Meaning (Dev-Friendly Question):** Why do some tests reuse Spring startup while others rebuild the whole application?
- **Interview Answer:** Spring caches test contexts by configuration, including profiles, properties, imported configuration, and bean overrides. Compatible tests in the same JVM reuse the context, while differing mock definitions or other settings can require a new one. `@DirtiesContext` removes a context from the cache, forcing a rebuild when it is needed again. Avoid using it as a substitute for cleaning test data, and remember separate test JVMs do not share the cache.

#### 4. How do you use Testcontainers with `@ServiceConnection` for databases, Kafka, MongoDB, and Redis?
- **Simple Meaning (Dev-Friendly Question):** How can tests start real dependencies without hard-coded hostnames and ports?
- **Interview Answer:** Start a supported Testcontainers container and annotate it with Boot's `@ServiceConnection`, available since Boot 3.1, to supply connection details automatically. Include the Boot Testcontainers test support and the required container modules, and let tests use dynamically allocated ports. Support depends on Boot's connection-detail factories: databases, Kafka, and MongoDB have recognised types, while a generic Redis container may need `name = "redis"`. Use `@DynamicPropertySource` when a service or custom image is not recognised.

#### 5. How do you test transactional code (`@Transactional` on tests, rollback behaviour, why it can hide bugs)?
- **Simple Meaning (Dev-Friendly Question):** Why can a database test pass even though the same operation fails in production?
- **Interview Answer:** Spring-managed transactional tests normally roll back at the end, which keeps data clean but can hide missing service transaction boundaries, lazy-loading failures, and commit-time errors. Flush explicitly to expose SQL constraints, and use separate non-transactional or commit-focused tests for after-commit behaviour. A real HTTP request runs on the server thread and does not join the test thread's transaction. Clean committed test data explicitly and avoid assuming async work is rolled back with the test.

#### 6. How do you test security rules and exception handlers?
- **Simple Meaning (Dev-Friendly Question):** How do we check access decisions and consistent error responses together?
- **Interview Answer:** Use MockMvc with the relevant security filters and controller advice loaded, then test anonymous, permitted, and forbidden requests. Verify status, content type, safe error fields, and validation details, not just the response body. Test authentication failures through `AuthenticationEntryPoint` and access denials through `AccessDeniedHandler`, because filter failures do not normally reach controller advice. Add method-security tests when permissions are enforced below the controller.

#### 7. How do you do contract testing (Spring Cloud Contract, Pact) between services?
- **Simple Meaning (Dev-Friendly Question):** How do we detect API changes that would break another service without deploying both together?
- **Interview Answer:** A contract describes the requests and responses or message shapes that consumers depend on. Spring Cloud Contract can generate provider tests and stubs, while Pact supports consumer-driven contracts verified by providers. Publish versioned contracts or stubs and verify them in CI before release. Contract tests catch interface mismatches but do not replace full integration tests for networking, security, and business workflows.

#### 8. How do you test asynchronous code, scheduled jobs, and event listeners?
- **Simple Meaning (Dev-Friendly Question):** How do we test background work without fragile sleeps?
- **Interview Answer:** Wait for an observable result with a bounded timeout using a future, latch, or Awaitility instead of a fixed sleep. Test the job's business method directly and keep scheduling tests small; inject a controllable clock for time-based rules. For transactional listeners, explicitly complete the transaction before checking after-commit behaviour. Check failure paths, duplicate execution, and cleanup, and do not assume the test's transaction or thread context reaches workers.

#### 9. How do you test REST clients (MockRestServiceServer, WireMock, `RestTestClient` in Framework 7)?
- **Simple Meaning (Dev-Friendly Question):** How do we test outbound HTTP calls and our own HTTP endpoints with the right tools?
- **Interview Answer:** `MockRestServiceServer` supports isolated tests of `RestTemplate` and `RestClient`, checking requests and returning controlled responses without a network server. WireMock runs an HTTP stub server, useful for client integration tests involving delays, errors, and actual HTTP I/O. Framework 7's `RestTestClient` tests server endpoints with response assertions, either through MockMvc or against a running server; it is not a replacement for an outbound stub server. Cover error mapping, timeouts, and safe retry behaviour as well as success.

#### 10. What is your test pyramid for a Spring Boot microservice, and what do you avoid testing with `@SpringBootTest`?
- **Simple Meaning (Dev-Friendly Question):** How do we get strong coverage without making every test slow?
- **Interview Answer:** Use many plain unit tests for business rules, focused slice tests for framework boundaries, and fewer integration tests with real dependencies. Keep a small set of end-to-end tests for critical user flows. Avoid `@SpringBootTest` for pure calculations, simple mappings, and every validation edge case because the full context adds cost without extra confidence. Choose test scope by the risk being checked rather than by a fixed percentage.

#### 11. How do you test configuration properties and conditional beans (`ApplicationContextRunner`)?
- **Simple Meaning (Dev-Friendly Question):** How do we prove configuration binding and auto-configuration work under different settings?
- **Interview Answer:** `ApplicationContextRunner` creates a small context with selected configuration, properties, and test beans. Assert which beans appear or back off when a property, class, or user-defined bean changes. For configuration records, test successful binding and invalid values with validation enabled, checking startup failure when required. Use a filtered classloader when testing a missing optional dependency.

#### 12. How do you keep integration tests fast and non-flaky in CI?
- **Simple Meaning (Dev-Friendly Question):** How do we make integration tests repeatable without paying startup cost for every test?
- **Interview Answer:** Reuse compatible Spring contexts and suite-scoped containers, keeping the containers alive as long as cached contexts need them. Isolate test data, avoid fixed ports and external shared services, and wait on readiness or bounded conditions rather than sleeps. Run tests in parallel only when their data and resources are independent. Pin dependency image versions and collect logs on failure instead of hiding intermittent failures with retries.

#### 13. How do you test filters, interceptors, and the full request journey (`MockMvc` vs `@SpringBootTest(webEnvironment = RANDOM_PORT)` with a real Tomcat)? What does `MockMvc` not exercise?
- **Simple Meaning (Dev-Friendly Question):** Which request tests run inside a simulated web layer, and which use real sockets and a server?
- **Interview Answer:** MockMvc exercises Spring MVC dispatch, argument binding, advice, interceptors, and configured filters using mock servlet requests, without starting Tomcat. Standalone setups require you to add the relevant infrastructure yourself. A random-port `@SpringBootTest` starts the configured real server and exercises HTTP, container filter registration, and server behaviour. MockMvc does not prove TCP/TLS, connector limits, real container error dispatch, or graceful network shutdown, so use real-server tests for those risks.

## 9. Performance and Production Readiness

#### 1. How do you profile a slow Spring Boot endpoint (Micrometer timers, tracing, JFR, async-profiler, SQL logging)?
- **Simple Meaning (Dev-Friendly Question):** How do we find where a slow request spends its time before changing code?
- **Interview Answer:** Start with latency percentiles, error rate, throughput, and resource or pool saturation under representative load. Follow a trace to separate application work, SQL, and outbound calls, then use JFR or async-profiler for CPU, allocation, or blocking hotspots. Inspect slow queries and query counts with safe, targeted SQL logging. Fix the measured bottleneck and repeat the same test to verify improvement.

#### 2. What are the typical causes of slow startup and high memory in Spring Boot, and how do you fix them?
- **Simple Meaning (Dev-Friendly Question):** Why does the application take a long time to become ready or use too much memory?
- **Interview Answer:** Slow startup often comes from unnecessary beans, broad scanning, database migrations, remote calls, or eager warm-up. Measure startup steps and heap usage, then remove unused starters, narrow scans, and move non-essential work off the readiness path. High memory can come from large caches, queued tasks, retained entities, many threads, or oversized buffers and pools. Lazy loading of beans, AOT, or native images may help specific cases, but each has trade-offs and needs measurement.

#### 3. How do you tune Tomcat (`server.tomcat.threads.max`, `accept-count`, `max-connections`, keep-alive) for a given load?
- **Simple Meaning (Dev-Friendly Question):** How do we choose request concurrency and connection limits without overloading the app?
- **Interview Answer:** With a platform-thread executor, `threads.max` limits request workers, `max-connections` limits accepted connections, and `accept-count` controls the connection backlog when capacity is reached. Tune these with load tests, request duration, CPU, and downstream database or HTTP capacity in mind. Set keep-alive and request-related timeouts to release idle or stuck connections, and watch queueing and tail latency. More threads are not always faster, and worker-pool settings do not have the same effect with a virtual-thread executor.

#### 4. Do virtual threads (`spring.threads.virtual.enabled`) help? What are the pitfalls (pinning, connection pools, `ThreadLocal`, unbounded concurrency)?
- **Simple Meaning (Dev-Friendly Question):** Can lightweight threads improve blocking request handling, and what limits still remain?
- **Interview Answer:** In supported Boot versions on Java 21+, virtual threads can improve concurrency for blocking I/O, but they do not speed up CPU-bound work. Database connections, remote services, and memory remain limited, so add concurrency limits rather than accepting unlimited work. Blocking inside `synchronized` could pin carrier threads on JDK 21–23; JDK 24 largely fixed this through JEP 491, though native or foreign calls can still cause pinning. Large per-thread `ThreadLocal` values can become expensive when there are many virtual threads.

#### 5. How do you configure caching (`@Cacheable`, `@CacheEvict`, TTL, Redis, cache stampede, serialisation)? Why does `@Cacheable` fail on self-invocation?
- **Simple Meaning (Dev-Friendly Question):** How do we cache results safely and keep stale data or repeated cache misses under control?
- **Interview Answer:** Enable caching and choose a provider such as Redis, with explicit keys, serialisation, TTL, and invalidation rules; Spring's cache annotations do not define a universal TTL. `@Cacheable` reuses results, while `@CacheEvict` removes entries when data changes. Prevent stampedes with controlled loading, jittered expiry, or suitable locking; `sync = true` support and cross-instance behaviour depend on the provider. Default proxy-based caching does not intercept self-invocation, and tenant or user boundaries must be reflected in cache keys.

#### 6. How do you handle thread pools for `@Async` and `@Scheduled` correctly (custom executor, rejection policy, context propagation)?
- **Simple Meaning (Dev-Friendly Question):** How do we keep background tasks from exhausting resources or losing request context?
- **Interview Answer:** Enable the required feature and configure explicit executors or schedulers, choosing bounded queues and concurrency appropriate to the workload. Separate slow background work from scheduling threads, and define overload behaviour such as rejection instead of allowing an endless queue. Propagate trace, MDC, and security context deliberately with suitable wrappers or task decorators, then clean it up. Transactions do not propagate across threads; handle task exceptions and wait or stop safely during shutdown.

#### 7. How do you run scheduled jobs safely across multiple instances (ShedLock, Quartz clustering, leader election, Kubernetes CronJob)?
- **Simple Meaning (Dev-Friendly Question):** How do we stop every application replica from running the same job at once?
- **Interview Answer:** A plain `@Scheduled` method runs in every replica, so use a shared lock such as ShedLock, clustered Quartz, leader election, or a separately managed Kubernetes CronJob. Choose lock expiry, overlap policy, and retry behaviour for the job's actual duration and failure cases. Locks and schedulers do not guarantee exactly-once execution, especially after crashes or lease expiry. Make jobs idempotent and track progress so safe retries are possible.

#### 8. How do you capture metrics and traces (Micrometer, OpenTelemetry, Prometheus, Grafana)? Which metrics do you alert on?
- **Simple Meaning (Dev-Friendly Question):** How do we see service health and follow a request across services?
- **Interview Answer:** Use Micrometer instrumentation and observations, an appropriate tracing bridge/exporter, Prometheus-compatible metrics collection, and Grafana dashboards. Propagate trace context through inbound requests, outbound clients, and messaging so spans connect across services. Alert on user-facing latency and error objectives, plus CPU, memory, connection-pool waits, queue depth, and consumer lag where relevant. Keep metric labels low-cardinality and exclude tokens or personal data from telemetry.

#### 9. How do you diagnose memory leaks and OOMKilled pods (heap vs container memory, `MaxRAMPercentage`, heap dump, metaspace)?
- **Simple Meaning (Dev-Friendly Question):** How do we tell whether the Java heap is leaking or the container has run out of total memory?
- **Interview Answer:** First distinguish a JVM `OutOfMemoryError` from a container `OOMKilled` event using pod status, memory metrics, and JVM logs. The container limit covers heap plus metaspace, thread stacks, direct buffers, code cache, and other native memory, not just `-Xmx`. Set heap sizing or `MaxRAMPercentage` with enough headroom, then use GC data, heap dumps, and native-memory tools to identify growth. A kernel kill may leave no heap dump, so collect evidence before the limit is reached.

#### 10. How do you handle backpressure and rate limiting inside a Spring Boot service?
- **Simple Meaning (Dev-Friendly Question):** How do we keep incoming work from growing faster than the service can handle it?
- **Interview Answer:** Use bounded queues, connection pools, concurrency limits, and deadlines to cap outstanding work. Rate limiting caps arrivals over time, while backpressure slows producers or limits demand; they solve related but different problems. Return `429` for client rate limits or an appropriate overload response such as `503` when capacity is unavailable. Reactive demand helps only when the full processing path respects it; do not hide overload behind unbounded buffering or retries.

#### 11. How do you tune JSON serialisation and payload size for performance?
- **Simple Meaning (Dev-Friendly Question):** How do we reduce the CPU, memory, and network cost of API responses?
- **Interview Answer:** Return small DTOs with only required fields, paginate collections, and avoid serialising JPA entities with lazy relationships or cycles. Reuse Boot's configured mapper rather than building one for every request. Measure compression trade-offs and use streaming for large responses where the client can consume them incrementally. Verify custom serializers and date formats for both speed and contract compatibility before changing libraries or settings.

#### 12. How do you design health checks and graceful shutdown so rolling deployments do not drop requests?
- **Simple Meaning (Dev-Friendly Question):** How do we send traffic only to ready pods and let old pods finish their work?
- **Interview Answer:** Readiness should reflect whether the instance can serve traffic, while liveness should detect an unrecoverable local failure rather than a shared database outage. Mark a terminating instance unready, allow routing changes to propagate, and enable graceful server shutdown to drain active requests. Budget Kubernetes `preStop` and application drain time within `terminationGracePeriodSeconds`. Test long requests, workers, and persistent connections during rollout; grace periods reduce dropped requests but cannot guarantee none in every failure.

#### 13. How do you do zero-downtime config changes (`@RefreshScope`, restart vs refresh trade-offs)?
- **Simple Meaning (Dev-Friendly Question):** Should we reload settings live or replace instances when configuration changes?
- **Interview Answer:** For most settings, a rolling restart is simpler because each instance starts with a validated, consistent configuration. Spring Cloud's `@RefreshScope` can recreate selected beans on a refresh, but it does not make every setting dynamically reloadable or update all replicas atomically. Use live refresh for deliberately designed settings, checking validation, in-flight work, and dependent resources. Protect refresh endpoints and verify support for the exact Spring Cloud version and runtime mode.

#### 14. How do you build a production-grade Docker image for Spring Boot (layered JAR, buildpacks, JRE image, non-root user)?
- **Simple Meaning (Dev-Friendly Question):** What makes a Spring Boot container small, reproducible, and safe to operate?
- **Interview Answer:** Use buildpacks or a multi-stage build with a maintained runtime image and a non-root user. Layer dependencies separately from application code to improve rebuild caching, and keep build tools and secrets out of the final image. Pin and regularly refresh the base image, scan it, and set realistic container resource limits with JVM headroom. Ensure the Java process receives termination signals and writes logs to standard output.

## 10. Spring Boot 3 / 4 and Framework 6 / 7 Features

#### 1. What are the baselines for Spring Boot 3 (Java 17, Jakarta EE 9+) and what broke in the `javax` to `jakarta` migration? What are the baselines for Boot 4 / Framework 7 (Jakarta EE 11)?
- **Simple Meaning (Dev-Friendly Question):** Which Java and Jakarta versions do these releases need, and why do older dependencies stop working?
- **Interview Answer:** Boot 3 requires Java 17 and Framework 6; Framework 6 has a Jakarta EE 9 baseline, while Boot 3.0's managed dependencies align with Jakarta EE 10, including Servlet 6.0 and JPA 3.1. Boot 4.0 still requires Java 17, uses Framework 7, and moves to Jakarta EE 11 with Servlet 6.1. Jakarta APIs such as Servlet, Persistence, and Validation moved from `javax.*` to `jakarta.*`, requiring compatible libraries as well as import changes. Java SE packages such as `javax.sql` did not move, so a global text replacement is wrong.

#### 2. What changed in Spring Boot 4's modularisation of auto-configuration, and how does it affect starters, dependencies, and your upgrade?
- **Simple Meaning (Dev-Friendly Question):** Why might a Boot 4 application need different starters or imports even when its business code is unchanged?
- **Interview Answer:** Boot 4 splits support into smaller technology-focused modules, with matching starters and more focused test starters. A third-party dependency alone may no longer bring the Boot integration you need, so review the required starter rather than assuming the old classpath is enough. Module changes also move some Java packages and test annotations. Use the official migration guide to update dependencies and imports, and test auto-configuration behaviour rather than only checking compilation.

#### 3. How does Framework 7 support API versioning (path, header, query parameter, media type)? How would you evolve an API for old clients?
- **Simple Meaning (Dev-Friendly Question):** How can Spring route different API versions while keeping existing clients working?
- **Interview Answer:** Framework 7 adds native version resolution from a path segment, header, query parameter, or media-type parameter. Configure the version strategy and use the `version` attribute on request mappings, with supported and default versions chosen explicitly. Keep old contracts available during a planned transition, prefer additive changes, and test each supported client version. Deprecation and sunset response headers can communicate when an old version will be retired.

#### 4. What are `@Retryable`, `@ConcurrencyLimit`, and `@EnableResilientMethods` in Framework 7? How do they compare with Resilience4j and the old Spring Retry project?
- **Simple Meaning (Dev-Friendly Question):** Which retry and concurrency controls are built into Spring 7, and when is another library still useful?
- **Interview Answer:** Framework 7 provides these annotations in `org.springframework.resilience.annotation`, enabled by `@EnableResilientMethods`. `@Retryable` retries selected failures with configurable delays, while `@ConcurrencyLimit` limits simultaneous method calls through a proxy; self-invocation bypasses them. They are distinct from Spring Retry's older annotations and APIs, so imports and settings are not interchangeable. Use Resilience4j when you need features such as circuit breakers or richer resilience policies, and make retried operations duplicate-safe.

#### 5. What is JSpecify null-safety in Spring, and how does it affect your code and IDE/Kotlin interop?
- **Simple Meaning (Dev-Friendly Question):** How does Spring tell tools which values may be null before the code runs?
- **Interview Answer:** Framework 7 uses JSpecify annotations such as `@NullMarked` and `@Nullable` to describe nullness, including generic type arguments. IDEs and compatible static checkers use these contracts to flag unsafe calls, and Kotlin can interpret them for stronger Java interoperability. These annotations do not automatically add runtime null checks. During upgrades, review changed contracts and replace older Spring nullability annotations where appropriate.

#### 6. What does Jackson 3 support change for your serialisation setup?
- **Simple Meaning (Dev-Friendly Question):** What must we recheck when Boot switches its preferred JSON library to Jackson 3?
- **Interview Answer:** Boot 4 prefers Jackson 3, whose main packages and coordinates move from `com.fasterxml.jackson` to `tools.jackson`; Jackson annotations remain under `com.fasterxml.jackson.annotation`. Boot configures a JSON-specific `JsonMapper`, so review mapper customisers, modules, serializers, and changed defaults. Test actual JSON contracts, especially dates, enums, nulls, and polymorphic data. Boot 4.0 offers deprecated Jackson 2 integration as a transition aid, not a reason to assume Jackson 2 and 3 APIs are interchangeable.

#### 7. What is `@ImportHttpServices`, and how do declarative HTTP interface clients (`@HttpExchange`) reduce boilerplate?
- **Simple Meaning (Dev-Friendly Question):** How can a Java interface become an injectable HTTP client without handwritten request code?
- **Interview Answer:** An HTTP service interface declares endpoints with `@HttpExchange` and related annotations, and Spring creates a proxy that sends the HTTP requests. Framework 7's `@ImportHttpServices` registers these proxies as beans from explicit types or scanned packages and can organise them into named groups. Groups can share client configuration such as base URLs and authentication. This removes request-building boilerplate, but you still need timeouts, error handling, and a safe retry policy.

#### 8. What are `RestClient`, `JdbcClient`, `RestTestClient`, and the newer `JmsClient`? When do you use each?
- **Simple Meaning (Dev-Friendly Question):** Which fluent client should we use for HTTP calls, SQL, endpoint tests, and JMS messages?
- **Interview Answer:** `RestClient`, introduced in Framework 6.1, is a fluent synchronous HTTP client; `JdbcClient`, also from 6.1, simplifies common SQL queries and updates. Framework 7.0 adds `RestTestClient` for testing server endpoints, either without a server through MockMvc or against a live server. The official 7.0 Javadoc also marks `JmsClient` as available since 7.0; it provides fluent JMS send/receive operations backed by `JmsTemplate`. These clients target different tasks and do not remove the need for connection, timeout, or transaction settings.

#### 9. What are `ProblemDetail` responses, and how do you standardise error contracts?
- **Simple Meaning (Dev-Friendly Question):** How do we return predictable API errors instead of a different JSON shape from every endpoint?
- **Interview Answer:** `ProblemDetail` represents the standard problem-details error format, originally RFC 7807 and now RFC 9457, normally returned as `application/problem+json`. Use shared controller advice to map application and validation failures into consistent status, type, title, detail, and optional extension fields. Add stable error codes or trace IDs where useful, but avoid stack traces, secrets, and internal SQL. Configure security entry points and denial handlers too, since filter errors do not normally pass through controller advice.

#### 10. What is Micrometer Observation / the observability starter, and how do metrics, logs, and traces correlate?
- **Simple Meaning (Dev-Friendly Question):** How do we connect timing measurements, distributed traces, and logs for the same operation?
- **Interview Answer:** Micrometer Observation instruments an operation once so configured handlers can produce metrics and tracing data. Boot's Actuator and observability integrations supply supported instrumentation; Boot 4 offers focused starters such as `spring-boot-starter-opentelemetry`, so check actual artifact names rather than assuming a generic observability starter. Trace and span IDs in logging context link logs to spans, while low-cardinality tags make metrics useful for aggregation. Exporters, propagation, and sampling still need configuration; observation alone does not send telemetry anywhere.

#### 11. What are virtual threads support, AOT processing, and GraalVM native images in Spring Boot? What are the limits (reflection, proxies, dynamic classpath)?
- **Simple Meaning (Dev-Friendly Question):** Which options improve concurrency or startup, and what flexibility do they cost?
- **Interview Answer:** Virtual-thread support, available in Boot 3.2+ on Java 21+, makes blocking concurrency cheaper but does not increase database or CPU capacity. AOT processing generates code and metadata ahead of time, while GraalVM native images compile a closed-world application into a native executable for fast startup. Reflection, resources, serialization, and dynamic proxies may need runtime hints, and arbitrary runtime classpath discovery is limited. Test the native executable itself; Boot 4.0's native-image toolchain requires GraalVM 25 or later even though its JVM baseline is Java 17.

#### 12. What is `@ServiceConnection` and Docker Compose / Testcontainers support in Spring Boot?
- **Simple Meaning (Dev-Friendly Question):** How does Boot connect development and test dependencies without manually copying connection settings?
- **Interview Answer:** Boot 3.1 introduced service connections, Docker Compose integration, and Testcontainers integration to simplify local dependencies. For supported services, Boot builds connection details from a running container rather than requiring fixed URLs and ports. `@ServiceConnection` commonly marks a Testcontainers container, while Compose integration recognises supported services from the Compose setup. These are development and test conveniences, not production orchestration, and unsupported service types still need explicit configuration.

#### 13. What are Spring Modulith, Spring Cloud (current release train), and Spring AI? Where do they fit in your architecture?
- **Simple Meaning (Dev-Friendly Question):** Which Spring project helps organise one application, connect distributed services, or add AI features?
- **Interview Answer:** Spring Modulith helps structure and verify business modules inside a modular monolith, including module interaction and event testing. Spring Cloud provides distributed-system integrations such as configuration, gateways, discovery, and circuit-breaker support. Choose a Cloud release train from the official Boot compatibility table and import its `spring-cloud-dependencies` BOM rather than guessing a train from the current date. Spring AI integrates model clients, tool calling, embeddings, and retrieval, with access controls and safe handling of untrusted model input still the application's responsibility.

#### 14. How do you plan an upgrade path from Boot 2.x or 3.x to 4.x (deprecations, third-party compatibility, staged upgrade, automated recipes like OpenRewrite)?
- **Simple Meaning (Dev-Friendly Question):** How do we reach Boot 4 in safe, testable steps instead of changing every dependency at once?
- **Interview Answer:** Establish passing tests first; from Boot 2, move through a suitable 2.7 release and the Boot 3 migration before attempting Boot 4. From Boot 3, the official Boot 4 guide recommends first reaching the latest 3.5 maintenance release and removing deprecated API use. Check Java/Jakarta requirements, third-party starters, Cloud compatibility, security, Jackson, and Boot's module changes at each stage. OpenRewrite can automate known changes, but review its diff and run contract, integration, and runtime tests before release.

### Official references for sections 6–10

- [Spring Boot 4.0 migration guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Framework 7 resilience features](https://docs.spring.io/spring-framework/reference/7.0/core/resilience.html)
- [Framework 7 API versioning](https://docs.spring.io/spring-framework/reference/7.0/web/webmvc-versioning.html)
- [Framework 7 HTTP service registration](https://docs.spring.io/spring-framework/docs/7.0.x/javadoc-api/org/springframework/web/service/registry/ImportHttpServices.html)
- [Framework 7 RestTestClient](https://docs.spring.io/spring-framework/reference/7.0/testing/resttestclient.html)
- [JmsClient Javadoc and its 7.0 introduction](https://docs.spring.io/spring-framework/docs/7.0.x/javadoc-api/org/springframework/jms/core/JmsClient.html)
- [MockitoBean Javadoc and its 6.2 introduction](https://docs.spring.io/spring-framework/docs/6.2.x/javadoc-api/org/springframework/test/context/bean/override/mockito/MockitoBean.html)
- [Spring Cloud release-train compatibility and BOM](https://spring.io/projects/spring-cloud#overview)

## 11. Code and Debugging Questions (with Solutions)

### Q1. This `@Transactional` method does not roll back or does not start a transaction. Why?

- **Simple Meaning (Dev-Friendly Question):** Why does this annotation fail to protect the whole order operation?
- **Interview Answer:** Calling `save()` inside the same bean bypasses Spring's transaction proxy, so it does not create the intended service transaction. Put the boundary on the externally called method or move the transactional work into another bean. Checked exceptions need an explicit rollback rule under the default policy. A database rollback cannot undo a payment charge: the example only demonstrates rollback rules; production payment flows need short local transactions, an outbox, provider idempotency, and reconciliation for unknown results.

```java
@Service
public class OrderService {

    public void placeOrder(Order order) throws IOException {
        validate(order);
        save(order);            // self-invocation: proxy is bypassed
    }

    @Transactional
    public void save(Order order) throws IOException {
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
    public void placeOrder(Order order) throws IOException {
        validate(order);
        orderRepository.save(order);
        paymentClient.charge(order); // teaching example only: DB rollback cannot undo a charge
    }
}
// Or move save() to another bean so the call goes through the proxy.
```

### Q2. Circular dependency: A needs B and B needs A. How do you fix the design?

- **Simple Meaning (Dev-Friendly Question):** How do you remove two services depending on each other?
- **Interview Answer:** Extract shared logic into a third service or let a separate coordinator call both services. An event can remove a direct dependency, but ordinary Spring events are synchronous unless configured otherwise. `@Lazy` can defer one dependency, but it hides rather than fixes a design cycle.

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

- **Simple Meaning (Dev-Friendly Question):** How do all controllers return errors in the same JSON format?
- **Interview Answer:** Use `@RestControllerAdvice` with handlers that map known exceptions to suitable HTTP statuses and `ProblemDetail`. Return safe business messages and validation details, not stack traces, tokens, or internal exception text. This handles MVC exceptions; filter and security failures need their own handling or explicit delegation.

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

- **Simple Meaning (Dev-Friendly Question):** How do you load related settings and fail startup when they are invalid?
- **Interview Answer:** Bind settings into an immutable `@ConfigurationProperties` record and register it through scanning or explicit enablement. Use Bean Validation to reject missing or invalid values early. Keep defaults in YAML, not annotation defaults or business logic in the record; keep secrets outside source control.

```java
@ConfigurationProperties(prefix = "app.payment")
@Validated
public record PaymentProperties(
        @NotBlank String baseUrl,
        @NotNull Duration timeout,
        @Min(1) @Max(10) int maxRetries) {}

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

- **Simple Meaning (Dev-Friendly Question):** How does a library create a default bean only when an application needs it?
- **Interview Answer:** Register the auto-configuration in Boot's imports metadata. Classpath conditions enable it when the library is present, and `@ConditionalOnMissingBean` lets an application replace the default. Keep auto-configuration separate from dependency packaging and test both activation and back-off.

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

- **Simple Meaning (Dev-Friendly Question):** How do you connect logs from one request without trusting unsafe client input?
- **Interview Answer:** Accept only a short, allow-listed incoming ID; otherwise generate one. Add it to MDC and the response, configure the logger to include it, and remove it in `finally` so pooled threads do not leak context. Propagate it on outbound calls; MDC needs explicit propagation across async work, and a correlation ID is not a trusted identity or a replacement for distributed tracing.

```java
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String incoming = req.getHeader(HEADER);
        String id = incoming != null && incoming.length() <= 64
            && incoming.matches("[A-Za-z0-9._-]{1,64}")
            ? incoming : UUID.randomUUID().toString();
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

- **Simple Meaning (Dev-Friendly Question):** How do you protect an API using bearer tokens rather than login sessions?
- **Interview Answer:** Configure a resource server to validate JWT signatures, issuer, and time claims, and configure audience validation for your API. Map scopes to authorities and enforce endpoint and method permissions. Stateless sessions do not automatically remove CSRF risk: disable CSRF only when credentials are not automatically attached by the browser, such as a bearer-header-only API. Keep exposed health details minimal.

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

- **Simple Meaning (Dev-Friendly Question):** How can an audit record commit even when the main operation fails?
- **Interview Answer:** Call a separate proxied bean using `REQUIRES_NEW`; it suspends the outer transaction and commits its own transaction independently. Record an attempted or failed action accurately, not a successful business change that later rolls back. The extra transaction may require another database connection, so size the pool carefully and handle audit failures explicitly.

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

- **Simple Meaning (Dev-Friendly Question):** How do you stop loading child data with one extra query per row?
- **Interview Answer:** Measure SQL counts, then fetch the required data using an entity graph, fetch join, batching, or a DTO projection. Use a left fetch join when parents without children must also be included; the shown inner join excludes them. Avoid collection fetch joins with direct pagination: page IDs first or use a suitable projection. Do not solve every query by making all relationships eager.

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

- **Simple Meaning (Dev-Friendly Question):** How do you run background work without creating unlimited threads or losing context?
- **Interview Answer:** Use a named executor with bounded threads and queue capacity, and invoke the async method through another bean's proxy. `CallerRunsPolicy` slows the caller under saturation and can run work on the request thread; choose rejection behavior deliberately. Security context and MDC are not automatically copied by an ordinary executor: use supported wrappers or a task decorator, while starting a new transaction in the worker if needed. Observe `CompletableFuture` failures; `void` methods need an `AsyncUncaughtExceptionHandler`.

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

- **Simple Meaning (Dev-Friendly Question):** How do you avoid sending a notification for an order that later rolls back?
- **Interview Answer:** Publish inside the transaction and handle it with `@TransactionalEventListener(AFTER_COMMIT)`. The listener runs after a successful commit, normally on the same thread, and is skipped without a transaction unless fallback execution is enabled. A crash or listener failure can still lose delivery because the database is already committed. Use a transactional outbox with retries and idempotent consumers for critical events; new database writes in the listener need their own transaction.

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
        // Kafka / email will not run on rollback, but delivery is not guaranteed
        // if this fails, the DB commit already happened
        // which is why a durable outbox is preferable for critical events
    }
}
```

### Q12. Controller slice test with security.

- **Simple Meaning (Dev-Friendly Question):** How do you test an authenticated endpoint's error response without loading the whole application?
- **Interview Answer:** Use `@WebMvcTest`, mock the service, and send a mock JWT using Spring Security's test support. Import the application's security configuration and advice if the slice does not discover them; provide a mock `JwtDecoder` if the security configuration needs one, and supply required authorities. `jwt()` bypasses real token decoding, so separately test invalid tokens and real security wiring. The assertion below checks that the advice maps the missing order to a 404 problem response.

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

- **Simple Meaning (Dev-Friendly Question):** How do you test persistence against a real disposable database?
- **Interview Answer:** Start a database container and let `@ServiceConnection` supply connection details to Boot. Run the real schema migrations and verify repository behavior rather than mocking the database. Keep tests isolated by cleaning committed data or rolling back test transactions, and pin a database version close to production. CI needs a working container runtime and the relevant test dependencies.

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

- **Simple Meaning (Dev-Friendly Question):** What settings help a service start, stop, and handle blocking requests safely?
- **Interview Answer:** Graceful shutdown gives active requests time to finish, while readiness controls whether new traffic should reach the instance. Enable probe endpoints and align the platform's drain and termination budgets with shutdown time; keep liveness independent of remote dependencies. Boot 3.2+ virtual threads require Java 21+ and can help blocking I/O, but do not increase database or downstream capacity. Secure metrics endpoints and install the Prometheus registry if exposing Prometheus metrics.

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

- **Simple Meaning (Dev-Friendly Question):** How do you retry temporary failures while limiting simultaneous calls?
- **Interview Answer:** Enable resilient methods and use Framework 7's annotations from `org.springframework.resilience.annotation`, not the older Spring Retry annotations. `maxRetries = 2` means one initial call plus up to two retries, with a 200 ms base delay here; restrict retries to known transient failures and configure network timeouts. `@ConcurrencyLimit(5)` limits concurrent method executions per bean instance, not across the whole cluster. For the follow-up, Resilience4j also offers circuit breakers, richer bulkhead choices, and metrics integrations; neither approach makes unsafe operations safe to retry.

Verified against the [Framework 7 Retryable Javadoc](https://docs.spring.io/spring-framework/docs/7.0.x/javadoc-api/org/springframework/resilience/annotation/Retryable.html).

```java
@Configuration
@EnableResilientMethods
class ResilienceConfig {}

@Service
class RateQuoteService {

    @Retryable(maxRetries = 2, delay = 200)
    @ConcurrencyLimit(5)
    public Quote fetch(String symbol) {
        return externalClient.quote(symbol);   // retried on failure; at most 5 concurrent calls
    }
}
// Follow-up: how is this different from Resilience4j (circuit breaker, bulkhead, metrics)?
```

### Q16. API versioning (Spring Framework 7). Check the reference docs for the exact configuration in your version.

- **Simple Meaning (Dev-Friendly Question):** How do old and new clients use different versions of the same endpoint?
- **Interview Answer:** Configure a version resolver, such as an `API-Version` request header, then map handlers to supported versions. Define missing-version behavior, validation, and deprecation rules explicitly instead of silently breaking existing clients. Keep separate response DTOs when contracts differ and test routing for supported, missing, and unsupported versions.

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

- **Simple Meaning (Dev-Friendly Question):** How do you call another service through a Java interface?
- **Interview Answer:** Annotate an interface with HTTP exchange mappings and create a proxy backed by `RestClientAdapter`. The call is synchronous and blocking; the proxy removes request-building boilerplate, not network failure handling. Configure the underlying client with connection and response timeouts, pooling, authentication, and error mapping; retry only safe operations.

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

- **Simple Meaning (Dev-Friendly Question):** Why does the database still get called when caching is enabled?
- **Interview Answer:** The internal call to `load()` bypasses the caching proxy. Move caching to the externally called method or put the loader in another bean, then check `@EnableCaching`, key generation, cache manager, and expiry configuration. The `null` return is only a placeholder: return the real result and decide explicitly whether missing values should be cached. Serialize cached DTOs rather than exposing persistence entities.

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

- **Simple Meaning (Dev-Friendly Question):** How can logs prove when beans, the server, runners, and readiness become available?
- **Interview Answer:** Bean initialization comes before the singleton-completion hook; lifecycle startup happens before `ContextRefreshedEvent`, followed by Boot's started event, runners, and ready event. The embedded server starts during lifecycle startup, so it can accept connections before runners finish; accepting a connection is not the same as being ready for traffic. For the follow-ups, inspect the availability logs: readiness changes to `ACCEPTING_TRAFFIC` after runners and the ready event, not at either runner line. Log the current readiness inside a runner to verify its state directly; do not infer server timing from arbitrary lifecycle beans with different phases.

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

- **Simple Meaning (Dev-Friendly Question):** How do you observe startup before Spring has created listener beans?
- **Interview Answer:** Register an `ApplicationListener` on `SpringApplication` before `run()`, or use the supported listener entry in factory metadata. Ordinary `@EventListener` beans are not available for these early events because the application context has not finished being built. Early listeners should be fast and avoid depending on application beans or logging secrets.

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

- **Simple Meaning (Dev-Friendly Question):** How can an error outside MVC still produce the API's normal JSON error response?
- **Interview Answer:** Filters execute outside `DispatcherServlet`, so their exceptions do not automatically reach MVC advice. A filter may delegate an uncommitted response to `HandlerExceptionResolver`; global advice must be able to handle it without a controller handler. A null result means unresolved, so rethrow instead of swallowing the error; do not rewrite a committed response. Use Spring Security's entry point and access-denied handler for authentication and authorization failures.

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
            if (res.isCommitted() || resolver.resolveException(req, res, null, ex) == null) {
                throw new ServletException("Filter chain failed", ex);
            }
        }
    }
}
// For Spring Security failures use AuthenticationEntryPoint / AccessDeniedHandler instead.
```

### Q22. Log the order in which filter, interceptor, and advice run for one request.

- **Simple Meaning (Dev-Friendly Question):** What runs before and after a controller, and what changes when the request fails?
- **Interview Answer:** For a normal synchronous REST response, filters wrap interceptors and controller execution, and the message converter writes the response body before `postHandle`. `ResponseBodyAdvice`, if configured, runs before that write; exception advice runs only when a matching error occurs. If the controller throws, `postHandle` is skipped, exception resolution runs, then `afterCompletion` runs for interceptors whose `preHandle` succeeded. If `preHandle` returns false, the controller and `postHandle` are skipped; only earlier successful interceptors get `afterCompletion`, and the rejecting interceptor must complete the response.

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
// Expected order for a normal synchronous @RestController request:
// filter before -> interceptor preHandle -> controller -> ResponseBodyAdvice (if any)
// -> response body written -> interceptor postHandle -> interceptor afterCompletion -> filter after
// Predict what changes when the controller throws, and when preHandle returns false.
```

## 12. Architectural Scenario Questions

#### 1. Your Spring Boot service takes 90 seconds to start in Kubernetes and readiness probes keep failing. How do you investigate and fix it?

- **Simple Meaning (Dev-Friendly Question):** Why is startup slow, and how do you stop traffic or restarts before startup finishes?
- **Interview Answer:** Compare startup logs, startup-step timings, CPU throttling, and database or network waits to find the slow phase. Remove unnecessary initialization and bound remote calls with timeouts. Configure a startup probe with a realistic budget and point readiness at the readiness health group. Readiness failures remove traffic; startup or liveness failures can restart the container, so distinguish them before changing thresholds.

#### 2. A new engineer put `@Transactional` on a method that calls a remote payment API and Kafka. What are the risks, and how do you restructure it?

- **Simple Meaning (Dev-Friendly Question):** Why does a database transaction not make payment and messaging one safe operation?
- **Interview Answer:** A local database rollback cannot undo a remote charge or a Kafka message, and slow calls keep database connections and locks occupied. Save the pending business state and an outbox event in one short database transaction, then process external work separately. Use provider idempotency keys and deduplicate events; use compensating actions when business steps fail. If payment times out with an unknown result, query or reconcile the provider's status instead of blindly charging again.

#### 3. Intermittent `HikariPool - Connection is not available` errors appear under load. How do you find the cause?

- **Simple Meaning (Dev-Friendly Question):** Why are requests waiting too long to borrow a database connection?
- **Interview Answer:** Check active, idle, pending, and timeout metrics, then inspect slow queries, lock waits, long transactions, and thread dumps. Look for leaked connections and remote calls inside transactions; temporary leak detection can identify suspicious borrowers. Fix the cause and cap concurrency before increasing the pool. Size all replica pools together against the database's connection and throughput limits.

#### 4. After a deploy, memory climbs until pods are OOMKilled, though heap looks fine. What do you check?

- **Simple Meaning (Dev-Friendly Question):** What uses container memory besides Java objects?
- **Interview Answer:** Compare container memory and process RSS with heap usage and the container limit. Check direct buffers, thread stacks, metaspace, code cache, native libraries, and memory-backed temporary files. Use native-memory tracking when enabled, thread counts, and container events to identify growth. Leave headroom outside the heap and fix the growing allocation rather than only increasing heap size.

#### 5. A shared internal starter library forces every team to upgrade at the same time. How do you design it to avoid that?

- **Simple Meaning (Dev-Friendly Question):** How do teams share defaults without becoming tightly coupled?
- **Interview Answer:** Split the starter into small optional capabilities with stable public settings and documented compatibility ranges. Use conditional auto-configuration and allow applications to override beans and disable features. Version breaking changes clearly and support a migration window with compatibility tests. Share technical policy, not service-specific business logic.

#### 6. You must upgrade 40 services from Spring Boot 2.7 / 3.x to Boot 4. How do you plan, sequence, and de-risk it?

- **Simple Meaning (Dev-Friendly Question):** How do you upgrade many services without breaking them all at once?
- **Interview Answer:** Inventory Java versions, dependencies, deprecated APIs, security configuration, and test coverage first. Move Boot 2.7 services through a supported Boot 3 migration, including Jakarta changes, then follow Boot 4 migration guidance and compatible Cloud and library versions. Pilot a low-risk representative service and automate repeatable changes where useful. Upgrade in batches with contract tests, load checks, canaries, and a rollback plan that includes schema compatibility.

#### 7. Your team wants to adopt virtual threads. How do you validate it is safe and beneficial before rollout?

- **Simple Meaning (Dev-Friendly Question):** How do you prove virtual threads improve this service rather than overwhelm its dependencies?
- **Interview Answer:** Benchmark realistic blocking-I/O traffic on the target JDK and compare throughput, tail latency, memory, and downstream usage. Inspect thread-local assumptions, synchronized or native blocking, and pinning behavior for that JDK version. Keep database pools, timeouts, and concurrency limits bounded because cheap threads do not create extra database capacity. Canary the change and retain an easy rollback.

#### 8. A service sometimes returns stale data after an update because of caching. How do you design cache invalidation and consistency?

- **Simple Meaning (Dev-Friendly Question):** How do you stop the cache serving old values after a write?
- **Interview Answer:** Define the acceptable staleness first; strongly consistent reads may need to bypass the cache. Invalidate or update entries after a successful database commit and use durable events when other instances cache the same data. TTL limits staleness but does not prevent races where an old read repopulates the cache after eviction. Use versioned entries or coordinated updates where required, and include tenant and authorization scope in keys.

#### 9. A third-party API is slow and unreliable and is dragging down your request threads. What patterns do you apply (timeouts, bulkheads, circuit breaker, async, fallback)?

- **Simple Meaning (Dev-Friendly Question):** How do you stop one failing dependency from taking down your service?
- **Interview Answer:** Set connection and response timeouts within the request's overall time budget. Limit concurrent calls with a bulkhead and use a circuit breaker to stop repeatedly calling a failing dependency. Retry only transient, safe operations with bounded backoff and jitter; async work still needs bounded capacity. Return an honest fallback or pending status rather than fake success, and track failures and latency.

#### 10. Developers keep adding `@MockBean` in tests and the CI test time has tripled. What is happening and how do you fix it?

- **Simple Meaning (Dev-Friendly Question):** Why can more Spring mocks cause many slow application startups in tests?
- **Interview Answer:** Different mock declarations can create different test-context cache keys, so Spring builds more contexts instead of reusing them. Measure context cache misses, reduce unnecessary full-context tests, and standardize genuinely shared test configurations. Prefer plain Mockito unit tests for business logic and focused slices for framework behavior. Avoid unnecessary `@DirtiesContext`; renaming `@MockBean` to `@MockitoBean` alone does not solve context churn.

#### 11. You find sensitive data (tokens, PII) in application logs. How do you stop it and prevent recurrence?

- **Simple Meaning (Dev-Friendly Question):** What do you do when logs expose private information or credentials?
- **Interview Answer:** Stop the leaking logger or payload capture, restrict access to affected logs, and rotate exposed credentials. Follow incident and retention procedures for existing copies and downstream log sinks. Replace payload logging with allow-listed metadata and tested redaction, including exception messages and tracing attributes. Add automated checks and review rules so the leak does not return.

#### 12. A scheduled job ran twice in production when the service scaled to 3 pods. How do you guarantee single execution?

- **Simple Meaning (Dev-Friendly Question):** How do you prevent multiple instances or retries from applying the same job's business changes twice?
- **Interview Answer:** Use a shared scheduler or a distributed lease such as ShedLock to reduce overlapping runs across pods. A lease is not an exactly-once guarantee: expiry, crashes, or retries can still produce duplicate work. Store a unique business key, such as job type plus reporting date, and atomically claim or commit work with a database constraint. Make external effects idempotent and track progress so restart and recovery remain safe.

#### 13. Multiple teams need to expose consistent error responses, logging, security, and metrics. How do you standardise this without a framework monolith?

- **Simple Meaning (Dev-Friendly Question):** How do services share operating standards while staying independently maintainable?
- **Interview Answer:** Define small contracts for problem responses, log fields, security requirements, and metric names. Provide optional focused starters with safe defaults, override points, and compatibility tests. Keep business behavior out of these libraries and let services upgrade within a documented support window. Enforce essential rules through CI checks and templates rather than one mandatory giant framework.

#### 14. You need zero-downtime deployments with DB schema changes. How do Spring Boot, Flyway, and your rollout strategy work together?

- **Simple Meaning (Dev-Friendly Question):** How can old and new application versions safely use the database during a rollout?
- **Interview Answer:** Use expand-and-contract: add compatible schema first, deploy code that supports the transition, backfill data, then remove old structures later. Keep Flyway migrations versioned and test lock duration and migration time on realistic data. Use a controlled migration step and roll out only when schema and readiness checks pass. An application rollback is safe only while the schema remains compatible; destructive migrations need a separate recovery plan.

#### 15. Two services share one database "temporarily". How do you get out of it safely?

- **Simple Meaning (Dev-Friendly Question):** How do you give each service data ownership without breaking existing flows?
- **Interview Answer:** Identify which service owns each table and stop adding new cross-service writes. Replace direct access with owner APIs or event-fed read models, then migrate and backfill data in stages. Verify consistency during the transition and avoid uncoordinated dual writes by using an outbox or suitable change capture. Remove old access permissions once consumers have moved, and use business workflows rather than cross-service database transactions.

#### 16. The first request after every deploy takes 3 seconds and later requests take 30 ms. Explain every possible cause along the startup and request path (lazy servlet initialisation, class loading, connection pool warm-up, JIT, caches) and how you remove each.

- **Simple Meaning (Dev-Friendly Question):** Why is the first request slow, and which startup work should happen before traffic arrives?
- **Interview Answer:** Time servlet initialization, lazy bean creation, class loading, first database connections, DNS/TLS setup, and cold caches with traces and startup measurements. Eagerly initialize required components, optionally use `spring.mvc.servlet.load-on-startup=1`, and perform bounded, representative warm-up before readiness. Reuse connection pools and prime only essential caches; avoid warm-up calls with business side effects. JIT compilation needs execution and may still improve after startup, so measure cold and warm latency separately rather than promise zero cold-start cost.

#### 17. A servlet filter throws an exception and clients receive an HTML error page instead of your JSON `ProblemDetail`. Explain why and fix it.

- **Simple Meaning (Dev-Friendly Question):** Why does the controller's JSON error handler not catch a filter failure?
- **Interview Answer:** A filter runs outside MVC's dispatcher, so its exception bypasses normal controller advice resolution and may reach container error handling. For an uncommitted response, explicitly delegate to the MVC exception resolver or write a safe standardized problem response. If the resolver returns null, rethrow; never silently swallow the error or rewrite a committed response. Handle security failures through the security entry point or access-denied handler and test the actual filter path.

#### 18. Under load, requests queue up and latency spikes while CPU stays low. Walk through every place a request can wait (Tomcat `acceptCount` / `maxThreads`, connection pool, downstream calls, locks, DB) and how you identify the bottleneck.

- **Simple Meaning (Dev-Friendly Question):** Where are requests waiting when the application is slow but not busy computing?
- **Interview Answer:** Compare ingress and server timings, connector connections and worker usage, executor queues, Hikari pending borrowers, and downstream latency. Take thread dumps and inspect database lock waits and slow queries to locate blocking. `acceptCount` concerns the connection backlog when the connector cannot accept more connections; it is not the same as a business-work queue. Fix the measured constraint with faster queries, shorter transactions, timeouts, or bounded concurrency rather than simply adding threads.

#### 19. Kubernetes shows the pod as Ready, but the first seconds of traffic get 503 or errors. Use your knowledge of the startup sequence, runners, readiness state, and web server start to explain and fix it.

- **Simple Meaning (Dev-Friendly Question):** Why can a listening server still be unprepared for real requests?
- **Interview Answer:** The server starts accepting connections before Boot finishes runners and publishes readiness as accepting traffic. Check that the probe targets the application's readiness group rather than a TCP port or unrelated health check. Required initialization launched asynchronously must finish before readiness, and custom health checks should cover required local initialization state. Also inspect endpoint propagation, proxy routing, and dependency failures because Ready does not prove every request path works.

#### 20. A cache warm-up `CommandLineRunner` takes two minutes. Should readiness be UP before it finishes? How do you design startup tasks so traffic only arrives when the service is truly ready?

- **Simple Meaning (Dev-Friendly Question):** Must traffic wait for warm-up, or can the service start serving without it?
- **Interview Answer:** If the cache is required for correct responses, keep readiness refusing traffic until bounded warm-up finishes; Boot normally waits for synchronous runners. If the cache only improves speed, serve through a safe fallback and warm it in the background with bounded load. Set a startup-probe budget for expected initialization and fail clearly if essential warm-up cannot succeed. Returning from a runner after merely launching a background task does not mean the task is complete.

#### 21. During a rolling deploy, in-flight requests are dropped. Walk through the shutdown sequence (SIGTERM, readiness, `preStop`, graceful web server shutdown, connection draining) and fix it.

- **Simple Meaning (Dev-Friendly Question):** How do you stop an old instance without cutting off requests already using it?
- **Interview Answer:** On termination, Kubernetes starts endpoint removal and runs any `preStop` hook before sending SIGTERM; these steps and traffic propagation are not instantaneous. Ensure SIGTERM reaches the JVM, readiness refuses new traffic, and Spring's graceful server shutdown lets active requests finish. Budget the termination grace period for the hook, routing drain, and all shutdown work, not just one Spring phase. Test slow requests and long-lived connections through the real ingress during a rollout.

#### 22. A controller works in `MockMvc` tests but fails in a real deployment because of a filter and a proxy-related header. What does `MockMvc` skip, and how do you test the real path?

- **Simple Meaning (Dev-Friendly Question):** Which real networking and container behaviors are missing from controller tests?
- **Interview Answer:** `MockMvc` exercises MVC and configured filters using mock servlet objects, not a listening Tomcat connector or real reverse proxy. It does not reproduce socket/TLS handling, container-specific filter behavior, ingress rewrites, or trusted forwarded-header processing. Add random-port integration tests with the real security and filter configuration, then test through a representative proxy for deployment-specific behavior. Trust forwarded headers only from controlled proxies and strip untrusted client-supplied values at the boundary.

## 13. System Design Scenario Questions

#### 1. Design the backend for an e-commerce checkout with Spring Boot: API layer, transactions, payment integration, idempotency, and failure handling.

- **Simple Meaning (Dev-Friendly Question):** How do you create an order and take payment without duplicate charges or lost work?
- **Interview Answer:** Validate request DTOs and use an idempotency key scoped to the customer, protected by a unique constraint and request-payload check. In a short transaction, save a pending order and an outbox event; workers coordinate inventory and payment using explicit states. Send the same provider idempotency key on safe retries, and reconcile unknown payment results before another charge attempt. Record success, failure, and compensation so clients can query order status even after a timeout.

#### 2. Design a multi-tenant SaaS API on Spring Boot with tenant isolation in security, data, caching, and rate limits.

- **Simple Meaning (Dev-Friendly Question):** How do you prevent one customer's requests from accessing another customer's data or capacity?
- **Interview Answer:** Derive tenant identity from verified authentication and check that the user is allowed to act in that tenant. Enforce tenant scope in repository access and constraints, or use separate schemas or databases when stronger isolation is needed. Include tenant identity in cache keys, background jobs, events, and rate-limit counters. Test cross-tenant access failures and apply per-tenant quotas so one noisy tenant cannot exhaust shared capacity.

#### 3. Design an authentication and authorisation setup for a Spring Boot microservices platform (gateway, JWT, token relay, service-to-service security).

- **Simple Meaning (Dev-Friendly Question):** How do users and services prove identity and get only the permissions they need?
- **Interview Answer:** Use a trusted authorization server, authorization code with PKCE for user-facing clients, and client credentials for machine identities. Let the gateway enforce edge policy, but have every service validate token signature, issuer, audience, expiry, and required permissions. Relay user tokens only to intended audiences or use token exchange; do not treat all internal traffic as trusted. Keep tokens out of logs, rotate keys, and protect cookie-based browser sessions against CSRF.

#### 4. Design a file upload and processing service with retries, status tracking, and secure downloads.

- **Simple Meaning (Dev-Friendly Question):** How do users upload large files, track processing, and download them safely?
- **Interview Answer:** Stream uploads or issue short-lived scoped upload URLs, enforcing size limits, ownership, and allowed content types. Store files privately, quarantine and scan them, and create a durable job with pending, processing, completed, or failed status. Workers claim jobs safely, process idempotently, and use bounded retries with a failed-job path. Authorize every status and download request, use expiring download links, and apply retention rules to files and temporary data.

#### 5. Design a notification service with templates, channels, retries, and per-user preferences.

- **Simple Meaning (Dev-Friendly Question):** How do you deliver email, SMS, or push notifications reliably without sending duplicates?
- **Interview Answer:** Accept durable notification requests with a unique event key, template version, recipient, and channel. Apply consent, preferences, quiet hours, and channel-specific rate limits before dispatch. Track attempts and provider IDs, retry transient failures with backoff, and move exhausted work to a review or dead-letter path. Deduplicate requests and provider callbacks, and avoid storing sensitive message content unnecessarily.

#### 6. Design an API platform with versioning, deprecation policy, rate limiting, and consumer-driven contract tests across many teams.

- **Simple Meaning (Dev-Friendly Question):** How can many teams evolve APIs without breaking their clients?
- **Interview Answer:** Publish API contracts and agree on versioning, errors, pagination, and backward-compatible change rules. Announce deprecations with a support window and use consumer-driven contract checks in CI before release. Apply authenticated client or tenant rate limits at the gateway and resource-specific limits in services. Monitor version usage and rejected requests so old versions are retired based on evidence, not guesses.

#### 7. Design a scheduled batch and reporting platform (Spring Batch or alternatives) that handles restarts, partial failures, and large volumes.

- **Simple Meaning (Dev-Friendly Question):** How do large jobs resume after failure without repeating completed business work?
- **Interview Answer:** Use durable job metadata, stable business parameters, and checkpointed chunk processing, such as Spring Batch with a persistent job repository. Make writes idempotent with unique business keys and define retryable, skippable, and fatal errors explicitly. Partition bounded workloads and stream or page input rather than loading everything into memory. Coordinate scheduling across instances, track partial results, and test restart behavior; scheduler locks alone do not guarantee exactly-once effects.

#### 8. Design an audit and activity log capability for all services without slowing request paths.

- **Simple Meaning (Dev-Friendly Question):** How do you keep trustworthy records without making every request wait on an audit service?
- **Interview Answer:** For business changes that must be audited, persist an audit event or outbox row in the same local transaction. Deliver it asynchronously to an access-controlled append-only store using unique event IDs for deduplication. Record actor, tenant, action, target, time, outcome, and trace ID, but exclude secrets and unnecessary personal data. Monitor delivery lag and failures; an in-memory fire-and-forget queue is not durable enough for mandatory audit records.

#### 9. Design a configuration and feature-flag strategy for 30 services across dev, staging, and prod.

- **Simple Meaning (Dev-Friendly Question):** How do many services get safe settings and controlled feature rollouts in each environment?
- **Interview Answer:** Keep typed, validated settings and non-secret defaults in version-controlled YAML, with environment-specific overrides and secrets from a protected secret store. Separate deploy-time configuration from runtime feature flags, and give every flag an owner, expiry date, and safe default. Roll out flags gradually and keep an audit trail and kill switch. Treat refresh as an explicit feature: immutable startup-bound settings do not automatically update when a remote value changes.

#### 10. Design a Spring Boot service from scratch for production readiness: observability, health, security, resilience, and deployment on AKS.

- **Simple Meaning (Dev-Friendly Question):** What must a service have before it is safe to operate in production?
- **Interview Answer:** Separate controller DTOs, application workflows, pure domain rules, and infrastructure adapters, with validated external configuration. Add least-privilege authentication, safe problem responses, timeouts, bounded concurrency, and idempotent failure recovery. Emit structured logs, metrics, and traces, and define startup, readiness, liveness, and graceful-shutdown behavior. For AKS, plan resource budgets, secret access, restricted networking, and rolling deployments, then verify the design with integration, load, and shutdown tests.

#### 11. Design a shared platform library (starter) for logging, security, error handling, and metrics, and describe how its auto-configuration, filters, and startup hooks behave.

- **Simple Meaning (Dev-Friendly Question):** How can a starter provide consistent defaults without surprising the application?
- **Interview Answer:** Package focused auto-configurations with classpath and property conditions, validated settings, and missing-bean back-off. Register filters with explicit order and dispatch behavior, and avoid duplicate registration in both the servlet and security chains. Keep error contracts safe and metric labels low-cardinality; startup hooks should be bounded and must not hide essential initialization after readiness. Test override behavior, security-chain matching, and compatibility across supported Boot versions.

## 14. Rapid-Fire Questions

#### 1. What is the default scope of a Spring bean?

- **Simple Meaning (Dev-Friendly Question):** How many instances does Spring normally create for one bean definition?
- **Interview Answer:** Singleton: one instance per bean definition per application context, not one per JVM. Singleton does not mean thread-safe.

#### 2. What is the difference between `@Controller` and `@RestController`?

- **Simple Meaning (Dev-Friendly Question):** Which annotation normally returns a view, and which writes response data?
- **Interview Answer:** `@Controller` supports MVC views; use `@ResponseBody` to write data directly. `@RestController` combines `@Controller` and `@ResponseBody`, typically producing JSON through message converters.

#### 3. What does `@SpringBootApplication` include?

- **Simple Meaning (Dev-Friendly Question):** What three main features does the main Boot annotation enable?
- **Interview Answer:** It combines `@SpringBootConfiguration`, `@EnableAutoConfiguration`, and `@ComponentScan`. Scanning starts from the annotated class's package by default.

#### 4. How do you change the embedded server port and context path?

- **Simple Meaning (Dev-Friendly Question):** How do you change the listening port and servlet application's URL prefix?
- **Interview Answer:** Set `server.port` and `server.servlet.context-path` in external configuration. For example, port 9090 and context path `/shop` make servlet endpoints start under that prefix.

#### 5. `@PathVariable` vs `@RequestParam`?

- **Simple Meaning (Dev-Friendly Question):** How do you read a value from the URL path versus a request parameter?
- **Interview Answer:** `@PathVariable` reads path segments such as the ID in `/orders/42`. `@RequestParam` reads query or form parameters such as `?status=NEW`.

#### 6. What is the difference between `application.properties` and `application.yml` precedence?

- **Simple Meaning (Dev-Friendly Question):** Which value wins when both configuration formats define the same key?
- **Interview Answer:** In the same location, Boot gives properties-format values precedence over YAML values. Other property-source, profile, and location precedence rules still apply; prefer one format per location to avoid surprises.

#### 7. What does `@Transactional(readOnly = true)` actually do?

- **Simple Meaning (Dev-Friendly Question):** Does a read-only transaction guarantee that writes are impossible?
- **Interview Answer:** It is mainly a hint that can change provider flush behavior and enable database or driver optimizations. It is not a portable write-protection or security guarantee.

#### 8. What is the default isolation level in Spring `@Transactional`?

- **Simple Meaning (Dev-Friendly Question):** Does Spring choose a fixed database isolation level by default?
- **Interview Answer:** The default is `Isolation.DEFAULT`, which uses the underlying database's configured isolation. It is not always `READ_COMMITTED`.

#### 9. `CrudRepository` vs `JpaRepository` vs `PagingAndSortingRepository`?

- **Simple Meaning (Dev-Friendly Question):** Which repository interface provides basic operations, paging, or JPA-specific features?
- **Interview Answer:** `CrudRepository` provides CRUD, while `PagingAndSortingRepository` provides paging and sorting. In Spring Data 3+, the latter does not itself extend CRUD; combine interfaces if needed. `JpaRepository` combines list-based CRUD and paging with JPA features such as flush and batch operations.

#### 10. What is `@Primary` vs `@Qualifier`?

- **Simple Meaning (Dev-Friendly Question):** How do you choose one bean when several match an injection point?
- **Interview Answer:** `@Primary` marks the preferred bean for a single-valued injection. `@Qualifier` narrows the candidates for that specific injection point.

#### 11. What happens when two beans of the same type exist and none is marked primary?

- **Simple Meaning (Dev-Friendly Question):** Will Spring guess which matching bean you intended?
- **Interview Answer:** If qualifiers, supported name matching, or other selection rules do not resolve a single candidate, injection fails with `NoUniqueBeanDefinitionException`. Injecting a collection instead can intentionally receive all matching beans.

#### 12. What is the difference between `@Bean` and `@Component`?

- **Simple Meaning (Dev-Friendly Question):** When does Spring discover a class versus call a factory method?
- **Interview Answer:** `@Component` marks a class for component scanning. `@Bean` registers the object returned by a configuration method, useful for third-party classes and custom construction.

#### 13. Can you inject a prototype bean into a singleton? What is the catch?

- **Simple Meaning (Dev-Friendly Question):** Does a singleton automatically get a fresh prototype on every call?
- **Interview Answer:** Direct injection resolves one prototype when the singleton is created, so the singleton keeps that instance. Use `ObjectProvider.getObject()` for a fresh instance when needed, or a suitable scoped proxy; prototype destruction is not automatically managed by Spring.

#### 14. What is `@Lazy` injection?

- **Simple Meaning (Dev-Friendly Question):** How do you delay resolving a dependency until it is first used?
- **Interview Answer:** `@Lazy` at an injection point supplies a proxy that resolves the target when needed. It can reduce early initialization but move failures to the first request; an independently eager singleton may still be created at startup.

#### 15. What are the Actuator endpoints you expose in production?

- **Simple Meaning (Dev-Friendly Question):** Which operational endpoints should be accessible, and to whom?
- **Interview Answer:** Expose only required endpoints, usually health and a protected metrics or Prometheus endpoint; optionally expose carefully curated info. Restrict access and details, and avoid publicly exposing environment, configuration, heap dumps, or loggers.

#### 16. What is the difference between liveness and readiness?

- **Simple Meaning (Dev-Friendly Question):** Should this instance restart, or should it temporarily stop receiving traffic?
- **Interview Answer:** Liveness detects a process that cannot recover without restart; readiness controls traffic routing. A shared database outage should not normally fail liveness and restart every instance.

#### 17. `@Async` return types and their effect on exceptions?

- **Simple Meaning (Dev-Friendly Question):** How does the caller learn that background work failed?
- **Interview Answer:** Use `void` or a supported `Future`, commonly `CompletableFuture`. Future failures must be observed through the result or completion handlers; `void` failures go to an `AsyncUncaughtExceptionHandler`, not back to the original caller.

#### 18. What is the default connection pool in Spring Boot?

- **Simple Meaning (Dev-Friendly Question):** Which pool does Boot normally select for JDBC connections?
- **Interview Answer:** HikariCP when it is on the classpath, as with the usual JDBC and JPA starters. This answer concerns JDBC; reactive R2DBC uses different connection infrastructure.

#### 19. What is the difference between `Filter` and `HandlerInterceptor`?

- **Simple Meaning (Dev-Friendly Question):** Which hook works at the servlet boundary and which knows the MVC handler?
- **Interview Answer:** A servlet filter wraps the servlet chain and can wrap requests or responses. A `HandlerInterceptor` runs around a mapped MVC handler; use Spring Security rather than relying on an interceptor as the main security boundary.

#### 20. How do you exclude an auto-configuration class?

- **Simple Meaning (Dev-Friendly Question):** How do you stop Boot applying one of its automatic configurations?
- **Interview Answer:** Use `@SpringBootApplication(exclude = SomeAutoConfiguration.class)` or the `spring.autoconfigure.exclude` property. This excludes auto-configuration, not arbitrary component-scanned beans.

#### 21. What is `@ConditionalOnMissingBean` used for?

- **Simple Meaning (Dev-Friendly Question):** How can a library supply a default without replacing the application's bean?
- **Interview Answer:** It registers a bean only when the specified matching bean is absent. Auto-configurations use it to back off when the application provides its own implementation; definition processing order matters.

#### 22. How do you read an environment variable in Spring Boot?

- **Simple Meaning (Dev-Friendly Question):** How does an operating-system setting become a Spring configuration value?
- **Interview Answer:** Boot includes environment variables in its `Environment` and can bind them to `@ConfigurationProperties`. For example, `SERVER_PORT` supplies `server.port`; prefer typed validated settings for groups of application properties.

#### 23. What is the difference between `@Mock` and `@MockitoBean`?

- **Simple Meaning (Dev-Friendly Question):** Are you mocking a plain test dependency or replacing a Spring-managed bean?
- **Interview Answer:** Mockito's `@Mock` creates a mock in the test, not in the Spring context. `@MockitoBean` overrides a Spring context bean for the test; use `@MockBean` on older versions that lack it.

#### 24. What is the purpose of `spring.jpa.open-in-view`, and what should it be in production?

- **Simple Meaning (Dev-Friendly Question):** Should a web response be allowed to trigger lazy database queries after the service transaction ends?
- **Interview Answer:** Open-in-view keeps the persistence context available through request processing, enabling late lazy loads. Usually disable it with `spring.jpa.open-in-view=false` in production and fetch required data into DTOs within the service transaction to avoid hidden queries.

#### 25. What is the difference between `PUT` and `PATCH`, and how do you implement `PATCH` safely?

- **Simple Meaning (Dev-Friendly Question):** How do you replace a resource versus change selected parts without overwriting someone else's update?
- **Interview Answer:** `PUT` normally replaces the resource representation and is idempotent; `PATCH` applies partial changes and is not automatically idempotent. Allow-list writable fields, distinguish missing from explicit null, validate the resulting state, and use version checks or `If-Match` to prevent lost updates.

#### 26. Which embedded server does `spring-boot-starter-web` use by default, and how do you switch it?

- **Simple Meaning (Dev-Friendly Question):** Which HTTP server comes with the usual MVC starter, and how is it replaced?
- **Interview Answer:** Tomcat is the default. Exclude its starter and add a supported alternative such as Jetty, checking compatibility with your Boot version; Boot 4 no longer supports Undertow.

#### 27. What is the default servlet mapping for `DispatcherServlet` in Spring Boot?

- **Simple Meaning (Dev-Friendly Question):** Which URL mapping normally sends requests into Spring MVC?
- **Interview Answer:** The default mapping is `/`, relative to the application's servlet context path. It is not the same mapping as `/*`.

#### 28. Which event tells you the application is ready to serve traffic?

- **Simple Meaning (Dev-Friendly Question):** Which Boot event happens after startup runners finish?
- **Interview Answer:** `ApplicationReadyEvent` signals completed startup after the runners. Boot then publishes readiness as `ACCEPTING_TRAFFIC`; actual traffic routing should follow the readiness probe and any required custom health checks.

#### 29. Which runs first: `CommandLineRunner` or `ApplicationReadyEvent`?

- **Simple Meaning (Dev-Friendly Question):** Does the ready event happen before or after startup tasks?
- **Interview Answer:** `CommandLineRunner` and `ApplicationRunner` execute before `ApplicationReadyEvent`. A failed runner prevents successful startup readiness; a blocking runner delays it.

#### 30. What is the default thread name pattern for Tomcat worker threads?

- **Simple Meaning (Dev-Friendly Question):** What thread name often identifies an MVC request in logs?
- **Interview Answer:** A typical platform-thread name is `http-nio-8080-exec-1`, with connector, port, and worker number varying. Virtual-thread execution, TLS connectors, or a custom executor can use different names, so do not make behavior depend on this pattern.