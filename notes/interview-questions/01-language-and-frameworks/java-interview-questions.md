# Java Interview Questions: Java 8 to 25 (15+ Years Experience)

Covers language features, exception handling, String, collections, concurrency, JVM, GC, performance, and production scenarios. This list stops at Java 25 (LTS). For anything newer, check the latest release notes.

## Table of Contents

- [1. Java Version Evolution (8 to 25)](#1-java-version-evolution-8-to-25)
- [2. Core Java and OOP Staples](#2-core-java-and-oop-staples)
- [3. Exception Handling](#3-exception-handling)
- [4. String](#4-string)
- [5. Collections Framework](#5-collections-framework)
- [6. Generics, Lambdas, Streams, Optional](#6-generics-lambdas-streams-optional)
- [7. Multithreading and Concurrency](#7-multithreading-and-concurrency)
- [8. Modern Concurrency: Virtual Threads, Structured Concurrency, Scoped Values](#8-modern-concurrency-virtual-threads-structured-concurrency-scoped-values)
- [9. JVM Internals](#9-jvm-internals)
- [10. Garbage Collection](#10-garbage-collection)
- [11. Performance Tuning and Troubleshooting](#11-performance-tuning-and-troubleshooting)
- [12. I/O, Serialization, Date-Time, Reflection, Modules](#12-io-serialization-date-time-reflection-modules)
- [13. Code and Output Questions (with Solutions)](#13-code-and-output-questions-with-solutions)
- [14. Production Scenario Questions](#14-production-scenario-questions)
- [15. Leadership and Design-Level Java Questions](#15-leadership-and-design-level-java-questions)
- [16. Rapid-Fire Questions](#16-rapid-fire-questions)
- [Appendix A: JVM Flags Cheat Sheet](#appendix-a-jvm-flags-cheat-sheet)
- [Appendix B: Diagnostic Commands Cheat Sheet](#appendix-b-diagnostic-commands-cheat-sheet)

---

## 1. Java Version Evolution (8 to 25)

### Version Cheat Sheet (for revision)

| Version | LTS | Highlights |
|---|---|---|
| 8 | Yes | Lambdas, Streams, `Optional`, default/static interface methods, `java.time`, `CompletableFuture`, PermGen replaced by Metaspace |
| 9 | No | Modules (JPMS), `List.of` / `Set.of` / `Map.of`, private interface methods, JShell, compact strings, G1 default, `Flow` API |
| 10 | No | `var` for locals, container awareness, parallel full GC for G1 |
| 11 | Yes | Standard `HttpClient`, new `String` methods, `var` in lambdas, single-file source launch, Epsilon GC, ZGC (experimental), Java EE/CORBA modules removed |
| 12 to 13 | No | Switch expressions (preview), text blocks (preview), Shenandoah (experimental) |
| 14 | No | Switch expressions final, helpful `NullPointerException` messages, records and pattern `instanceof` (preview) |
| 15 | No | Text blocks final, sealed classes (preview), ZGC and Shenandoah production-ready, hidden classes |
| 16 | No | Records and pattern `instanceof` final, `Stream.toList()`, Unix-domain sockets, strong encapsulation of JDK internals by default |
| 17 | Yes | Sealed classes final, strong encapsulation enforced, new pseudo-random generator API, Security Manager deprecated for removal |
| 18 | No | UTF-8 default charset, `jwebserver`, finalization deprecated for removal |
| 19 to 20 | No | Virtual threads (preview), structured concurrency (incubator), record patterns (preview), scoped values (incubator) |
| 21 | Yes | Virtual threads final, record patterns and pattern matching for `switch` final, Sequenced Collections, generational ZGC |
| 22 | No | Unnamed variables and patterns final, Foreign Function and Memory API final, multi-file source launch |
| 23 | No | Markdown in Javadoc, generational ZGC by default |
| 24 | No | Stream Gatherers final, Class-File API final, virtual threads no longer pinned by `synchronized`, AOT class loading and linking, Security Manager permanently disabled, non-generational ZGC removed |
| 25 | Yes | Scoped Values, compact source files and instance main methods, flexible constructor bodies, module import declarations, compact object headers, generational Shenandoah, AOT cache improvements, JFR enhancements (final); Structured Concurrency, Stable Values, primitive patterns (preview) |

### Questions

1. Walk through the language features that changed how you write Java since 8. Which ones changed your design style, and which ones are mostly syntax sugar?
2. What are the LTS releases (8, 11, 17, 21, 25)? How do you choose an upgrade target and cadence for a large estate of services?
3. How would you plan a migration from Java 8 to 21 or 25 for 100 services (module and illegal-access issues, removed APIs, dependency compatibility, GC defaults, container behaviour, build tooling, staged rollout)?
4. What changed in Java 9 modules (JPMS)? Why did strong encapsulation break libraries (`--add-opens`), and how do you handle it?
5. What are `var`, text blocks, switch expressions, and `yield`? What are the readability pitfalls of `var`?
6. Records: what do they generate, what are their limits (no inheritance, shallow immutability), and when do you not use them (JPA entities, mutable state)? How do compact constructors work?
7. Sealed classes and interfaces: what problem do they solve? How do they combine with records and pattern-matching `switch` to model algebraic data types and get exhaustiveness checks?
8. Pattern matching: `instanceof` patterns, `switch` patterns, guards (`when`), record deconstruction patterns, and nested patterns. How is dominance and exhaustiveness checked?
9. What are unnamed variables and patterns (`_`), and where do they improve code (catch blocks, lambda parameters, unused pattern components)?
10. Sequenced Collections (Java 21): which interfaces were added (`SequencedCollection`, `SequencedSet`, `SequencedMap`), which methods (`getFirst`, `getLast`, `reversed`, `addFirst`), and what problem did they solve?
11. Virtual threads (Java 21): what are they, how are they scheduled, and what do they change for blocking I/O code? What changed in Java 24 regarding `synchronized` pinning?
12. Stream Gatherers (Java 24): what gap in the Streams API do they fill (windowing, folding, custom intermediate operations)? Give an example.
13. Foreign Function and Memory API (Java 22): how does it replace JNI and `Unsafe`, and why does it matter for native interop and off-heap memory?
14. Class-File API (Java 24): who benefits (frameworks, bytecode tooling) and how does it relate to ASM and ByteBuddy?
15. What are the Java 25 language changes: compact source files with instance `main`, flexible constructor bodies (statements before `super(...)` / `this(...)`), and module import declarations? What problem does each solve?
16. Scoped Values (final in Java 25): how do they differ from `ThreadLocal`, why are they a better fit for virtual threads, and what are their limits (immutable, bounded lifetime, rebind via nested `where`)?
17. What is Structured Concurrency (still preview in 25), and why is it considered a better model than fire-and-forget `ExecutorService` tasks?
18. What are Compact Object Headers (final in 25), and what do you gain (heap size, cache efficiency)? What should you benchmark before enabling them?
19. What is Generational Shenandoah, and how does it relate to the generational ZGC change?
20. What is the AOT work (class loading and linking in 24, command-line ergonomics and method profiling in 25) aimed at, and how does it relate to CDS, GraalVM native image, and Project Leyden?
21. What JFR additions came in 25 (cooperative sampling, method timing and tracing, CPU-time profiling), and how would you use them in production?
22. What is the status of Primitive Types in Patterns, Stable Values, and PEM encodings in 25? What does "preview" mean and how do you use or avoid preview features in production?
23. What was removed or deprecated that can bite upgrades (Security Manager, `finalize`, Nashorn, applets, CORBA, `Thread.stop`, 32-bit ports, Unsafe memory-access methods)?
24. String templates were previewed and then dropped. What does this tell you about relying on preview features?
25. What does "Java is faster/slower after upgrade" depend on (JIT changes, GC defaults, compact strings, object headers, container ergonomics)? How do you benchmark an upgrade properly?

## 2. Core Java and OOP Staples

1. Why is Java "pass by value" even for objects? Show with a method that reassigns a parameter.
2. `==` vs `equals()` vs `hashCode()`: explain the contract and what breaks in hash-based collections if it is violated.
3. `final`, `finally`, `finalize`: differences. Why was `finalize` deprecated for removal, and what replaces it (`Cleaner`, try-with-resources)?
4. Abstract class vs interface in modern Java (default methods, static methods, private methods, sealed). How does the diamond problem appear with default methods?
5. What is the order of initialisation for static blocks, instance blocks, constructors, and field initialisers across a class hierarchy?
6. Inner class types (member, static nested, local, anonymous). What does an inner class hold implicitly, and how can that leak memory?
7. How do lambdas differ from anonymous classes (`this` meaning, compilation via `invokedynamic`, capture rules, effectively final)?
8. Autoboxing pitfalls: `Integer` cache range, `==` on boxed values, `NullPointerException` on unboxing, performance cost in loops.
9. How do you design an immutable class correctly (final fields, defensive copies, no leaking `this`, `List.copyOf`)? Why does immutability help concurrency and caching?
10. Explain `clone()` pitfalls (shallow copy, `Cloneable` design flaw). What do you use instead (copy constructors, records, factories)?
11. Enums: why are they a good singleton and strategy holder? How do `EnumMap`, `EnumSet`, and enum-based state machines work?
12. `transient`, `volatile`, `synchronized`, `static`, `strictfp` (now redundant): what does each guarantee?
13. How do access modifiers interact with modules (`exports`, `opens`, `requires transitive`) and reflection?
14. What is the difference between checked/unchecked, compile-time/runtime polymorphism, and static/dynamic binding? How does the JVM dispatch a virtual call?
15. What is covariant return, method hiding vs overriding, and why can't `static` methods be overridden?
16. What is the difference between `Comparable` and `Comparator`? How do you write a null-safe, multi-field comparator?
17. How does `Object.hashCode()` default work, and why must you not rely on it for identity across runs?
18. What are annotations, retention policies, and how do annotation processors differ from runtime reflection? When do frameworks use each?
19. What are marker interfaces vs annotations? Why was `Serializable` designed as a marker and what are its downsides?
20. What are the SOLID violations you most commonly find in a 10-year-old Java codebase, and how do you fix them incrementally?

## 3. Exception Handling

1. Explain the `Throwable` hierarchy (`Error`, `Exception`, `RuntimeException`). Which should be caught, which should not, and why?
2. Checked vs unchecked exceptions: what was the original rationale, what went wrong in practice (lambdas, leaky abstractions), and what is your team policy today?
3. How do you design a custom exception hierarchy for a service (domain exceptions, error codes, cause chaining, immutability, serialisation)?
4. Try-with-resources: how does it work with `AutoCloseable`, in which order are resources closed, and what are suppressed exceptions (`addSuppressed`, `getSuppressed`)?
5. `finally` pitfalls: `return` inside `finally`, exceptions thrown in `finally` masking the original, `System.exit` in `try`. Why does a `finally` not run in some cases?
6. Multi-catch and precise rethrow: what compile-time rules apply? Why can't you catch a subclass after a superclass?
7. Overriding rules with `throws` clauses. What can an overriding method declare, and why?
8. What does exception translation mean (DAO layer converting `SQLException` into domain exceptions)? Where do you draw the layer boundaries?
9. Exception chaining: how do you preserve the root cause, and what is the cost of building a stack trace (`fillInStackTrace`)? When are stackless or preallocated exceptions justified?
10. Should you use exceptions for control flow? What is the performance and design argument? Compare exceptions vs `Optional` vs result types (`Either`-style) for expected failures.
11. How do you handle exceptions in lambdas and streams (checked exceptions in functional interfaces, wrapper helpers, avoiding silent swallowing)?
12. How do exceptions behave in threads: `Thread.UncaughtExceptionHandler`, tasks submitted to an `ExecutorService` (`execute` vs `submit`), `Future.get()` wrapping in `ExecutionException`?
13. How do you handle failures in `CompletableFuture` (`exceptionally`, `handle`, `whenComplete`, `CompletionException` wrapping, timeouts)?
14. `StackOverflowError`, `OutOfMemoryError`, `NoClassDefFoundError` vs `ClassNotFoundException`, `ExceptionInInitializerError`: causes and what you can reasonably do about each.
15. How do you handle exceptions in static initialisers and constructors? What state is left behind (partially constructed objects, class in error state)?
16. What are good logging practices for exceptions (log once at the boundary, do not log and rethrow, include context, avoid leaking sensitive data)?
17. How do you design retry behaviour with exceptions (idempotency, which exceptions are retryable, back-off, jitter, max attempts, circuit breaker)?
18. What are helpful `NullPointerException` messages, and how do `Objects.requireNonNull`, `Optional`, and null-safety annotations fit a null-handling strategy?
19. What does your REST API error design look like (global handler, error contract, correlation IDs, mapping domain to HTTP status)?
20. How do you test exception paths (JUnit `assertThrows`, testing cause and message, fault injection, chaos testing)?
21. What is the behaviour of an exception thrown inside a `synchronized` block or while holding a `Lock`? How do you make sure locks are released?
22. How do you handle `InterruptedException` correctly (restore the interrupt flag, do not swallow, propagate or exit cleanly)?

## 4. String

1. Why is `String` immutable? List the benefits (security, caching of hash, string pool, thread safety) and the costs.
2. Explain the string pool. Where does it live (heap since Java 7), what does `intern()` do, and when is it harmful?
3. `new String("a")` vs literal `"a"`: how many objects are created, and what does `==` return in each case?
4. `String` vs `StringBuilder` vs `StringBuffer`: differences, thread safety, and when each is right. Why is `+` in a loop a problem and how does `invokedynamic`-based concatenation (Java 9+) change it?
5. What are compact strings (Java 9), and how does the Latin-1 vs UTF-16 byte-array representation affect memory?
6. How does `String.hashCode()` work and why is it cached? How could poor hashing be abused (hash-collision DoS) and how does `HashMap` mitigate it?
7. Why use `char[]` for passwords instead of `String`? Is it still meaningful today?
8. Unicode: `char` vs code point, surrogate pairs, `length()` vs `codePointCount()`, normalisation (NFC/NFD), locale-sensitive operations (`toUpperCase(Locale)`, the Turkish "i" problem).
9. `trim()` vs `strip()`, `isBlank()`, `lines()`, `repeat()`, `formatted()`, `transform()`, `indent()`: what did Java 11+ add and what is the whitespace difference?
10. Text blocks: how is indentation handled, what are escape sequences like `\` at line end and `\s`, and where do they help (SQL, JSON)?
11. `String.format` vs `MessageFormat` vs `StringBuilder` vs `String.join` vs `StringJoiner`: performance and use cases.
12. Regular expressions: what is catastrophic backtracking, how do you avoid ReDoS, and when do you precompile `Pattern`?
13. `split` pitfalls (regex, trailing empty strings, limit parameter) and `substring` behaviour (memory leak history before Java 7u6).
14. How do you compare strings safely (`equals`, `equalsIgnoreCase`, `compareTo`, `Objects.equals`, constant-time comparison for secrets)?
15. String switch: how is it compiled (hash then equals)? What are null and case-sensitivity gotchas?
16. What is G1 string deduplication, and when does it help?
17. How do you handle very large strings and text processing (streaming, `BufferedReader`, `CharSequence`, memory-mapped files)?
18. How do you convert between `String`, `byte[]`, and charsets safely? What changed with UTF-8 as the default charset in Java 18?
19. Why were String Templates previewed and then removed, and what should you use today (`formatted`, `MessageFormat`, `StringBuilder`, libraries)?
20. What is `CharSequence`, and why do APIs accept it instead of `String`?

## 5. Collections Framework

1. Draw the collection hierarchy (`Iterable`, `Collection`, `List`, `Set`, `Queue`, `Deque`, `Map`, `SequencedCollection`). Where does `Map` sit and why?
2. Explain `HashMap` internals: hash spreading, table index, buckets, resize and load factor, treeification of long chains (threshold 8 with minimum table size 64), untreeify, and why capacity is a power of two.
3. What breaks if `hashCode` or `equals` is wrong or mutable keys are used? How do you detect and fix key problems?
4. How does `HashMap` behave under concurrent access (lost updates, infinite loops in old versions, `ConcurrentModificationException`)? Why is it not thread-safe?
5. `ConcurrentHashMap` internals (CAS and per-bin locking in Java 8+, `computeIfAbsent`, `merge`, size counters, no null keys or values). What are the pitfalls of long-running `computeIfAbsent` functions?
6. `HashMap` vs `Hashtable` vs `Collections.synchronizedMap` vs `ConcurrentHashMap`: differences and use cases.
7. `LinkedHashMap`: insertion vs access order, building an LRU cache with `removeEldestEntry`.
8. `TreeMap` / `TreeSet`: red-black tree, ordering via `Comparable` / `Comparator`, `compareTo` consistent with `equals`, `NavigableMap` operations.
9. `ArrayList` vs `LinkedList` vs `ArrayDeque`: complexity, memory, and cache locality. Why is `LinkedList` rarely the right answer?
10. `ArrayList` growth strategy, `ensureCapacity`, `trimToSize`, and the cost of `remove(int)` and `add(int, E)`.
11. `PriorityQueue`: binary heap, complexity, iteration order not sorted, using a comparator for top-K problems.
12. Fail-fast vs fail-safe iterators: how `modCount` works, `ConcurrentModificationException`, `CopyOnWriteArrayList`, and weakly consistent iterators in concurrent collections.
13. `CopyOnWriteArrayList` and `CopyOnWriteArraySet`: when are they appropriate (read-mostly) and when do they hurt?
14. Immutable collections: `List.of`, `Set.of`, `Map.of`, `copyOf`, `Collections.unmodifiableList`, `Arrays.asList`. Differences in mutability, null handling, and views.
15. `Collections.unmodifiableList` vs `List.copyOf`: view vs copy and what that means for defensive copying.
16. `Comparable` vs `Comparator`, `Comparator.comparing().thenComparing()`, `nullsFirst`, and sorting stability (`List.sort` is stable, `Arrays.sort` on primitives is not required to be).
17. Sequenced Collections in Java 21: new methods, `reversed()` views, and `LinkedHashMap` / `LinkedHashSet` / `TreeSet` integration.
18. `EnumMap`, `IdentityHashMap`, `WeakHashMap`, `ConcurrentSkipListMap`: what are they for?
19. How do you pick initial capacity and load factor for a `HashMap` of a known size (`HashMap.newHashMap(n)` in Java 19+)?
20. `subList`, `keySet`, `values`, `entrySet` are views. What are the pitfalls (structural modification, memory retention)?
21. `BlockingQueue` implementations (`ArrayBlockingQueue`, `LinkedBlockingQueue`, `PriorityBlockingQueue`, `SynchronousQueue`, `DelayQueue`, `LinkedTransferQueue`): when do you use each?
22. How would you implement an LRU cache, an LFU cache, a time-bounded cache, and a thread-safe bounded buffer? What would you use in production instead (Caffeine)?
23. How do you detect and avoid memory leaks caused by collections (unbounded caches, static maps, listeners, `ThreadLocal` values)?
24. How do you choose between `List`, `Set`, `Map`, and `Queue` for a given problem, and what are the complexity trade-offs?
25. What is the cost of autoboxing in collections of primitives, and what are the alternatives (primitive arrays, specialised libraries, Valhalla direction)?

## 6. Generics, Lambdas, Streams, Optional

1. What is type erasure? What are its consequences (no `new T()`, no generic arrays, `instanceof List<String>` not allowed, bridge methods, heap pollution)?
2. Explain bounded types and wildcards: `? extends T`, `? super T`, and the PECS rule with a concrete example.
3. What are the limitations of generics with primitives, and how does that affect API design?
4. What are functional interfaces? Name the core ones (`Function`, `Predicate`, `Supplier`, `Consumer`, `UnaryOperator`, `BiFunction`) and primitive specialisations. Why do the specialisations exist?
5. How do method references work, and how do you choose between them and lambdas?
6. How do lambdas capture variables, and why must they be effectively final?
7. Streams: lazy evaluation, intermediate vs terminal operations, short-circuiting, stateful vs stateless operations, and what "a stream can be consumed once" means.
8. `map` vs `flatMap`, `reduce` vs `collect`, `Collectors.groupingBy`, `partitioningBy`, `toMap` (duplicate key behaviour), `teeing`, `mapMulti`.
9. When do parallel streams help or hurt (ForkJoin common pool, splittability, boxing, ordering, blocking calls, shared state)? How would you decide?
10. What are common stream anti-patterns (side effects in `peek` / `map`, mutation of external state, using streams for simple loops, huge intermediate objects)?
11. What are Stream Gatherers (Java 24), and when would you write a custom one?
12. `Optional`: intended use, misuse (as fields, parameters, collections), `orElse` vs `orElseGet`, `map` / `flatMap` / `or` / `stream`, `orElseThrow`.
13. `Collectors.toList()` vs `Stream.toList()` (Java 16): mutability and null differences.
14. How do streams compare with loops for performance (JIT, boxing, allocation)? When do you drop to a loop?
15. How do you handle checked exceptions and resource handling (files, `Stream.close()` for `Files.lines`) in stream pipelines?
16. How do you write a custom `Collector`, and what are characteristics (`CONCURRENT`, `UNORDERED`, `IDENTITY_FINISH`)?
17. What is `Spliterator`, and how does it enable parallel decomposition?
18. How do you design a fluent, type-safe API with generics (builder with self-types, recursive bounds)?

## 7. Multithreading and Concurrency

1. Process vs thread. What are the thread states in Java (`NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, `TIMED_WAITING`, `TERMINATED`) and what causes each transition?
2. Explain the Java Memory Model: happens-before, visibility, atomicity, ordering, and reordering by compiler and CPU. Why is the JMM needed?
3. `volatile`: what exactly does it guarantee, what does it not (compound actions like `count++`), and when is it enough?
4. `synchronized`: monitors, intrinsic locks, reentrancy, method vs block, lock on `this` vs private lock object. How are locks implemented in HotSpot today?
5. `wait` / `notify` / `notifyAll`: why must they be in a synchronized block and in a loop (spurious wakeups)? `sleep` vs `wait` vs `yield` vs `join`.
6. `ReentrantLock` vs `synchronized`: fairness, `tryLock`, interruptible locking, multiple `Condition`s. When do you prefer which?
7. `ReadWriteLock` and `StampedLock` (optimistic reads): where do they help, and what are the pitfalls (writer starvation, no reentrancy for `StampedLock`)?
8. What is a deadlock? Describe four necessary conditions, how to detect it in a thread dump, and how to prevent it (lock ordering, `tryLock` with timeout, fewer locks).
9. Livelock, starvation, priority inversion: explain each with an example.
10. Atomic classes and CAS: how do `AtomicInteger`, `AtomicReference`, `LongAdder`, and `LongAccumulator` work? What is the ABA problem and how do you handle it (`AtomicStampedReference`)?
11. `ThreadLocal`: use cases, memory leak with thread pools, `remove()`, `InheritableThreadLocal`, and why scoped values are the modern alternative.
12. Executor framework: `ThreadPoolExecutor` parameters (core, max, queue, keep-alive, thread factory, rejection policy), task submission flow, and common pool misconfigurations (unbounded queue, `newCachedThreadPool`).
13. How do you size a thread pool for CPU-bound vs I/O-bound work? Explain Little's Law and how you validate with measurements.
14. `Callable` / `Future` limitations. How do `CompletableFuture` (`supplyAsync`, `thenApply`, `thenCompose`, `thenCombine`, `allOf`, `anyOf`, timeouts) fix them?
15. Which executor does `CompletableFuture` use by default, why is that dangerous for blocking calls, and how do you supply your own?
16. `ForkJoinPool`: work-stealing, `RecursiveTask`, common pool and parallelism, and when `parallelStream` interacts with it.
17. Synchronisers: `CountDownLatch`, `CyclicBarrier`, `Semaphore`, `Phaser`, `Exchanger`. Give a use case for each.
18. Producer-consumer: implement with `BlockingQueue`, then with `wait` / `notify`. What are the back-pressure and shutdown (poison pill) considerations?
19. Thread safety strategies: immutability, confinement, stateless design, synchronisation, concurrent collections, lock-free structures. How do you document thread-safety of a class?
20. Double-checked locking: why was it broken pre-Java 5, why is `volatile` needed, and what are safer alternatives (holder idiom, enum)?
21. What is false sharing, how does `@Contended` or padding address it, and where have you seen it matter?
22. How do you shut down an executor gracefully (`shutdown`, `awaitTermination`, `shutdownNow`, handling interrupts)? How do you shut down a service with in-flight tasks?
23. How do you propagate context (security, MDC, tracing) across threads and pools?
24. How do you test concurrent code (stress tests, `jcstress`, deterministic schedulers, avoiding `Thread.sleep`)?
25. How do you diagnose a hung production service with thread dumps (reading `BLOCKED` threads, lock owners, thread pool exhaustion, `jstack` / `jcmd`)?
26. Reactive vs thread-per-request vs virtual threads: when do you choose each, and how do they affect debugging and observability?
27. How do you implement rate limiting, bulkheads, and timeouts at thread-pool level to protect a service?
28. What is the difference between concurrency and parallelism? Amdahl's law and the limits of parallel speedup.

## 8. Modern Concurrency: Virtual Threads, Structured Concurrency, Scoped Values

1. What is a virtual thread? How does it differ from a platform thread (scheduling on carrier threads, stack stored on heap, cheap creation)?
2. When do virtual threads help (high-concurrency blocking I/O) and when not (CPU-bound work, code with long native or foreign frames)?
3. What was pinning, why did `synchronized` pin virtual threads before Java 24, and what still can pin (native frames, some `Object.wait` cases)? How do you diagnose it (JFR events, `jdk.tracePinnedThreads` in older releases)?
4. Why should you not pool virtual threads? How do you limit concurrency instead (`Semaphore`, bounded work queues, per-resource limits)?
5. How do virtual threads interact with `ThreadLocal`, connection pools (HikariCP size), and `ExecutorService` shutdown semantics?
6. How do virtual threads change the case for reactive programming?
7. What is Structured Concurrency (preview in 25)? Explain scopes, forking subtasks, joining, shutdown policies (fail-fast, first-success), and how errors and cancellation propagate.
8. What is `ScopedValue` (final in 25)? How do `ScopedValue.where(...).run(...)` / `call(...)` work, and how do child tasks inherit bindings?
9. `ScopedValue` vs `ThreadLocal`: mutability, lifetime, inheritance cost, and performance with millions of virtual threads.
10. How would you migrate a Tomcat / Spring MVC service from a platform-thread pool to virtual threads, and what do you measure before and after?
11. What are the risks of unbounded virtual thread creation (downstream overload, memory), and how do you add back-pressure?
12. How do you observe virtual threads (thread dumps in JSON format, JFR, `jcmd Thread.dump_to_file`)?

## 9. JVM Internals

1. Describe the JVM architecture: class loader subsystem, runtime data areas, execution engine (interpreter, JIT), native interface, and GC.
2. Runtime memory areas: heap (young/old), Metaspace, thread stacks, PC registers, native method stacks, code cache, direct buffers. What lives in each, and which can cause which `OutOfMemoryError`?
3. Class loading phases (loading, linking: verification, preparation, resolution; initialisation). When is a class initialised?
4. Class loader hierarchy (bootstrap, platform, application), parent delegation, and how custom class loaders are used (plugins, containers). What causes `ClassNotFoundException`, `NoClassDefFoundError`, `LinkageError`, and class-loader leaks?
5. What is Metaspace, how is it different from PermGen, and what causes Metaspace growth?
6. Object layout: header (mark word, class pointer), fields, alignment, compressed oops, and what compact object headers (Java 25) change.
7. What is the JIT compiler? Explain tiered compilation (C1, C2), profiling, inlining, loop optimisations, OSR, escape analysis, and scalar replacement. What is deoptimisation?
8. What is warm-up, and why do cold JVMs perform badly? How do CDS, AppCDS, AOT cache (Java 24 and 25 work), and GraalVM native image address it?
9. What is a safepoint? Which operations need one, and how do long safepoints show up as latency spikes?
10. What is the difference between `invokevirtual`, `invokeinterface`, `invokespecial`, `invokestatic`, and `invokedynamic`? How do lambdas and string concatenation use `invokedynamic`?
11. How does the JVM implement `synchronized` (mark word, thin/lightweight lock, inflation to a monitor)? What happened to biased locking?
12. What is the Java Memory Model's relationship with hardware memory barriers, and how does the JIT respect `volatile`?
13. What is `Unsafe`, and what replaces it (VarHandles, FFM API, `MemorySegment`)? Why is it being restricted?
14. How does the JVM detect container limits (cgroups), and what do `-XX:MaxRAMPercentage`, `-XX:ActiveProcessorCount`, and CPU limits do to heap and GC thread sizing?
15. How do you read a bytecode listing (`javap -c -v`) to understand string concatenation, autoboxing, try-with-resources, or `switch` compilation?
16. What is the difference between JDK, JRE, JVM, and distributions (Temurin, Corretto, Zulu, Oracle, GraalVM)? How do you choose, and what about support and licensing?
17. What are `jlink` and `jpackage`, and when do you use them to create smaller runtimes?
18. What are the JVM diagnostic options for JIT (`-XX:+PrintCompilation`), inlining, and code cache sizing? When do you ever tune them?
19. How does the JVM handle `StackOverflowError`, and how do you size thread stacks (`-Xss`) for deep recursion or many threads?
20. What is the role of the code cache, and what happens when it fills (compilation stops, performance cliff)?
21. How do Java agents and instrumentation (APM, profilers, Byte Buddy) work, and what are their risks? What changed with dynamic agent loading restrictions?
22. Explain native memory usage beyond the heap (thread stacks, direct buffers, Metaspace, GC structures, code cache) and how Native Memory Tracking helps.

## 10. Garbage Collection

1. What is garbage collection, and what does "reachable" mean? Name GC roots.
2. Explain generational GC: why do generations exist (weak generational hypothesis), what are Eden, survivor spaces, and old generation, and how does promotion/tenuring work?
3. Compare the collectors: Serial, Parallel, G1, ZGC, Shenandoah, and Epsilon. State the goal, pause characteristics, and ideal workload for each.
4. G1 in depth: regions, young and mixed collections, remembered sets and card tables, SATB marking, `MaxGCPauseMillis`, `InitiatingHeapOccupancyPercent`, humongous objects, evacuation failure (to-space exhausted), and full GC fallbacks.
5. ZGC in depth: coloured pointers, load barriers, concurrent relocation, sub-millisecond pauses, and generational ZGC (default since 23; the non-generational mode was removed in 24).
6. Shenandoah in depth: concurrent evacuation, barriers, and the generational mode finalised in Java 25. When would you choose it over ZGC or G1?
7. How do you choose a GC for (a) a low-latency trading or API service, (b) a batch job, (c) a small container with 512 MB, (d) a 64 GB heap? What would you measure to decide?
8. What are stop-the-world pauses, and what contributes to them (root scanning, evacuation, remark, safepoint time, `System.gc()`)?
9. Define allocation rate, promotion rate, live set, GC overhead, and throughput. How do you compute them from GC logs?
10. Explain minor, mixed, and full GC. What triggers a full GC, and why is it a warning sign in G1 or ZGC?
11. What is premature promotion, and what are its effects (old gen fills, frequent mixed or full collections)? How do you fix it?
12. TLABs and allocation: how does fast allocation work, and why is short-lived garbage cheap?
13. How do escape analysis and scalar replacement reduce garbage? When do they not apply?
14. Reference types: strong, soft, weak, phantom. How do `WeakHashMap`, `SoftReference` caches, and `Cleaner` use them? Why are soft-reference caches risky?
15. What is finalisation, why was it deprecated, and what are the hazards (resurrection, delayed reclamation)?
16. How do you read GC logs (`-Xlog:gc*`)? Identify pause causes, heap occupancy after GC, and signs of a leak vs undersized heap.
17. What do you check when you see "GC overhead limit exceeded", "Java heap space", "Metaspace", "Direct buffer memory", or "unable to create native thread"?
18. Sizing: how do you set heap (`-Xms`, `-Xmx`, `MaxRAMPercentage`), why set min equal to max in servers, and how much memory must be left for non-heap in a container?
19. How do you tune G1 safely, and when do you stop tuning and fix the allocation behaviour in code?
20. What is string deduplication, and what is its cost?
21. How do large heaps (tens or hundreds of GB), NUMA, and transparent huge pages affect GC choice and tuning?
22. How do you diagnose a memory leak (heap histograms, heap dumps, dominator tree, retained size, GC root path, leak suspects)?
23. A container is OOMKilled but the Java heap looks fine. Explain what else consumes memory and how you find it.
24. What GC-related behaviour changed between Java 8, 11, 17, 21, and 25 that affects upgrade planning (G1 default, ZGC and Shenandoah maturity, generational modes, compact headers)?

## 11. Performance Tuning and Troubleshooting

1. Describe your methodology for performance work: define the goal and SLOs, reproduce, measure, find the bottleneck, change one thing, and re-measure. How do you avoid guessing?
2. Latency vs throughput, percentiles (p50, p95, p99, p99.9), coordinated omission, and why averages mislead.
3. How do you profile a Java service in production safely (JFR, async-profiler, sampling vs instrumentation, overhead, safepoint bias)? How do you read a flame graph?
4. What are the common JMH benchmarking mistakes (dead-code elimination, constant folding, warm-up, shared state, wrong forks)?
5. CPU is at 100%. What are your first ten minutes (top threads, `jcmd Thread.print`, JFR, GC activity, lock contention, tight loops, regex)?
6. Latency spikes at regular intervals. How do you separate GC, safepoints, noisy neighbours, JIT deoptimisation, connection pool waits, and downstream slowness?
7. How do you reduce allocation (reuse buffers, avoid boxing, avoid temporary strings, primitive collections, streams in hot paths)? When is it worth it?
8. Memory footprint: how do compact strings, compact object headers, and data structure choices (`Integer` lists, `HashMap` overhead) reduce memory? How do you measure retained size?
9. Lock contention: how do you find it (JFR lock profiling, thread dumps) and reduce it (smaller critical sections, striping, `LongAdder`, concurrent collections, immutable data)?
10. Cache design: hit rate, eviction, TTL, stampede protection, on-heap vs off-heap vs Redis. What does Caffeine give you over a hand-rolled map?
11. I/O performance: buffered vs unbuffered, NIO and channels, memory-mapped files, zero-copy, async I/O, and when blocking I/O with virtual threads is enough.
12. Serialisation performance and size (JSON vs Protobuf vs Avro), reflection cost, and `MethodHandle` / `VarHandle` alternatives.
13. Database access performance from Java: connection pool sizing, batching, N+1 queries, fetch size, prepared statement caching, and transaction scope.
14. Thread pool and queue sizing, back-pressure, timeouts, and the effect of unbounded queues on latency under overload.
15. Startup time: class loading, reflection and classpath scanning, CDS / AppCDS, AOT cache, lazy initialisation, native image trade-offs.
16. What JVM flags would you set for a container-based service, and which defaults would you leave alone? Justify each.
17. How do you validate that a JVM or GC upgrade (for example 17 to 21 or 25) improved performance and did not regress tail latency?
18. What are the typical performance myths (final methods are faster, `StringBuffer` never, `i++` vs `++i`, micro-optimisations, manual `System.gc()`)?
19. How do you capture and interpret a thread dump: states, lock owners, "waiting to lock", thread pool exhaustion, repeated dumps over time?
20. How do you diagnose a native memory leak (NMT, direct buffers, JNI, thread stacks, malloc arenas)?
21. How do you monitor a JVM in production (Micrometer, JMX, JFR streaming, GC metrics, thread counts, pool metrics) and which alerts matter?
22. How do you load-test responsibly and compare environments (realistic data, warm-up, steady state, soak tests, resource limits)?

## 12. I/O, Serialization, Date-Time, Reflection, Modules

1. `java.io` vs `java.nio` vs `java.nio.file`: streams vs channels and buffers, blocking vs non-blocking, `Files` and `Path` APIs, and when to use each.
2. Direct vs heap `ByteBuffer`, memory-mapped files, and the risks of direct memory (`OutOfMemoryError: Direct buffer memory`).
3. How do you read and write large files efficiently (buffering, streaming, `Files.lines`, chunking, `try-with-resources`)?
4. Java serialisation: `Serializable`, `serialVersionUID`, `transient`, custom `readObject` / `writeObject`, and security risks (deserialisation attacks, `ObjectInputFilter`). Why avoid it?
5. How do records and enums serialise? What are the alternatives (JSON, Protobuf, Avro, Kryo) and their trade-offs for compatibility and speed?
6. `java.time`: `Instant`, `LocalDateTime`, `ZonedDateTime`, `OffsetDateTime`, `Duration`, `Period`, `DateTimeFormatter`. Why is `java.time` better than `Date` / `Calendar` / `SimpleDateFormat` (immutability, thread-safety)?
7. Time zones, DST, and storing time: why store UTC `Instant`, convert at the edge, and inject `Clock` for tests?
8. Reflection: what it can do, its performance cost, and security/module restrictions. How do `MethodHandle` and `VarHandle` compare?
9. Annotations processing at compile time vs runtime, and how frameworks like Spring and Lombok work with them.
10. Dynamic proxies (JDK vs CGLIB / ByteBuddy) and how AOP frameworks use them.
11. `ServiceLoader` and SPI: how does it support plugin architectures, and how does it interact with modules?
12. JPMS essentials: `module-info.java`, `exports`, `opens`, `requires`, automatic modules, and why many teams still run on the classpath.
13. HTTP clients in modern Java: `HttpClient` (Java 11), HTTP/2 support, async API, timeouts, and comparison to library clients.
14. Foreign Function and Memory API vs JNI: safety, performance, and when you would use it.
15. Logging: SLF4J facade vs implementation, async logging cost, structured logs, MDC propagation across threads.
16. Random numbers: `Random` vs `ThreadLocalRandom` vs `SecureRandom` vs `RandomGenerator` (Java 17+). Which do you pick for what?
17. `BigDecimal` for money: scale, rounding modes, `equals` vs `compareTo`, and the constructor pitfall (`new BigDecimal(0.1)`).
18. Locale, charset, and internationalisation pitfalls (default charset, number and date formatting, `Collator`).
19. Basic Java security: avoiding SQL injection, safe deserialisation, TLS configuration, secrets handling, and the removal of the Security Manager.

## 13. Code and Output Questions (with Solutions)

### Q1. String pool and `==`

```java
String s1 = "hello";
String s2 = "hel" + "lo";
String s3 = new String("hello");
String part = "hel";
String s4 = part + "lo";
final String fp = "hel";
String s5 = fp + "lo";

System.out.println(s1 == s2);            // ?
System.out.println(s1 == s3);            // ?
System.out.println(s1 == s4);            // ?
System.out.println(s1 == s4.intern());   // ?
System.out.println(s1 == s5);            // ?
```

Answer: `true`, `false`, `false`, `true`, `true`. Compile-time constants are folded and pooled. `s4` is computed at runtime. `fp` is a constant variable, so `s5` is folded.

### Q2. Integer cache

```java
Integer a = 127, b = 127;
Integer c = 128, d = 128;
System.out.println(a == b);   // true  (cached -128..127)
System.out.println(c == d);   // false (different objects)
System.out.println(c.equals(d)); // true
Integer n = null;
int x = n;                    // NullPointerException on unboxing
```

### Q3. `finally` overrides the return

```java
static int f() { try { return 1; } finally { return 2; } }                  // returns 2
static int g() { int x = 1; try { return x; } finally { x = 2; } }          // returns 1
```

Explain: the return value of `g()` is evaluated before `finally` runs, and a `return` in `finally` discards any exception or earlier return. Avoid both patterns.

### Q4. Try-with-resources and suppressed exceptions

```java
class R implements AutoCloseable {
    private final String name;
    R(String name) { this.name = name; System.out.println("open " + name); }
    @Override public void close() {
        System.out.println("close " + name);
        throw new IllegalStateException("close " + name);
    }
}

try (R a = new R("A"); R b = new R("B")) {
    throw new RuntimeException("body");
} catch (Exception e) {
    System.out.println(e.getMessage());                       // body
    for (Throwable t : e.getSuppressed()) System.out.println(t.getMessage());
}
```

Output: `open A`, `open B`, `close B`, `close A`, `body`, `close B`, `close A`. Resources close in reverse order and their exceptions are attached as suppressed to the primary one.

### Q5. Initialisation order

```java
class P {
    static { System.out.println("P static"); }
    { System.out.println("P instance"); }
    P() { System.out.println("P ctor"); }
}
class C extends P {
    static { System.out.println("C static"); }
    { System.out.println("C instance"); }
    C() { System.out.println("C ctor"); }
}
new C();
```

Output: `P static`, `C static`, `P instance`, `P ctor`, `C instance`, `C ctor`.

### Q6. Mutable key in a `HashMap`

```java
class Key {
    int id;
    Key(int id) { this.id = id; }
    @Override public boolean equals(Object o) { return o instanceof Key k && k.id == id; }
    @Override public int hashCode() { return Integer.hashCode(id); }
}
Map<Key, String> map = new HashMap<>();
Key k = new Key(1);
map.put(k, "v");
k.id = 2;
System.out.println(map.get(k));          // null (looks in the bucket for hash 2)
System.out.println(map.get(new Key(1))); // null (right bucket, but stored key now has id 2)
System.out.println(map.size());          // 1  (entry is stuck: a leak)
```

Lesson: keys must be immutable, or at least their `hashCode` / `equals` fields must never change after insertion.

### Q7. `ConcurrentModificationException`

```java
List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4));
for (Integer i : list) {
    if (i % 2 == 0) list.remove(i);   // ConcurrentModificationException (or skips elements)
}

// Fixes
list.removeIf(i -> i % 2 == 0);

Iterator<Integer> it = list.iterator();
while (it.hasNext()) if (it.next() % 2 == 0) it.remove();
```

### Q8. `volatile` is not atomic

```java
class Counter {
    private volatile int count;
    void inc() { count++; }      // read-modify-write: not atomic, updates are lost under contention
}

// Fixes
class SafeCounter {
    private final AtomicInteger count = new AtomicInteger();
    void inc() { count.incrementAndGet(); }          // or LongAdder for heavy write contention
}
```

### Q9. Deadlock and the fix

```java
Object a = new Object(), b = new Object();

new Thread(() -> { synchronized (a) { sleep(100); synchronized (b) { /* work */ } } }).start();
new Thread(() -> { synchronized (b) { sleep(100); synchronized (a) { /* work */ } } }).start();
// Each thread holds one lock and waits for the other: deadlock.
```

Fix with a global lock order, or `tryLock` with a timeout:

```java
void transfer(Account from, Account to, long amount) {
    Account first  = from.id() < to.id() ? from : to;
    Account second = from.id() < to.id() ? to : from;
    synchronized (first) {
        synchronized (second) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}
```

### Q10. `ThreadLocal` leak in a pool

```java
private static final ThreadLocal<UserContext> CTX = new ThreadLocal<>();

void handle(Request r) {
    CTX.set(new UserContext(r.user()));
    try {
        process(r);
    } finally {
        CTX.remove();        // mandatory with pooled threads; otherwise data leaks across requests
    }
}
```

### Q11. `CompletableFuture` composition with timeout and fallback

```java
ExecutorService io = Executors.newFixedThreadPool(32);

CompletableFuture<User> user = CompletableFuture.supplyAsync(() -> userClient.get(id), io);
CompletableFuture<List<Order>> orders = user.thenComposeAsync(
        u -> CompletableFuture.supplyAsync(() -> orderClient.byUser(u.id()), io), io);

String result = user.thenCombine(orders, (u, o) -> render(u, o))
        .orTimeout(2, TimeUnit.SECONDS)
        .exceptionally(ex -> "fallback")     // ex is usually a CompletionException wrapping the cause
        .join();
```

Pitfalls to discuss: the default pool is `ForkJoinPool.commonPool`, `join()` vs `get()` exception types, losing context (MDC, security) across threads, and no cancellation of the underlying work after a timeout.

### Q12. Virtual threads: simple fan-out with a concurrency limit

```java
Semaphore limit = new Semaphore(100);   // protect the downstream service

try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<String>> futures = urls.stream()
        .map(u -> executor.submit(() -> {
            limit.acquire();
            try { return http.fetch(u); }
            finally { limit.release(); }
        }))
        .toList();
    for (Future<String> f : futures) System.out.println(f.get());
}   // close() waits for all tasks
```

### Q13. Records, sealed types, and pattern-matching `switch` (Java 21)

```java
sealed interface Shape permits Circle, Rect, Triangle {}
record Circle(double r) implements Shape {}
record Rect(double w, double h) implements Shape {}
record Triangle(double base, double height) implements Shape {}

static double area(Shape s) {
    return switch (s) {
        case Circle c when c.r() > 1000 -> throw new IllegalArgumentException("too large");
        case Circle c                    -> Math.PI * c.r() * c.r();
        case Rect(double w, double h)    -> w * h;                // record deconstruction pattern
        case Triangle t                  -> 0.5 * t.base() * t.height();
    };   // no default needed: the switch is exhaustive over the sealed hierarchy
}
// Follow-up: what happens at compile time if you add a new permitted subtype?
```

### Q14. Stream Gatherers (Java 24)

```java
List<List<Integer>> windows = Stream.of(1, 2, 3, 4, 5, 6, 7)
        .gather(Gatherers.windowFixed(3))
        .toList();
// [[1, 2, 3], [4, 5, 6], [7]]

List<List<Integer>> sliding = Stream.of(1, 2, 3, 4)
        .gather(Gatherers.windowSliding(2))
        .toList();
// [[1, 2], [2, 3], [3, 4]]
```

### Q15. Scoped Values (final in Java 25)

```java
private static final ScopedValue<String> USER = ScopedValue.newInstance();

void handle(Request request) {
    ScopedValue.where(USER, request.user())
               .run(() -> service.process());      // USER is bound only inside this call tree
}

class Service {
    void process() {
        String user = USER.get();                  // throws if not bound; immutable within the scope
        audit.log(user, "processed");
    }
}
// Follow-up: why is this cheaper and safer than ThreadLocal with millions of virtual threads?
```

### Q16. Flexible constructor bodies (final in Java 25)

```java
class PositiveBigInteger extends BigInteger {
    PositiveBigInteger(long value) {
        if (value <= 0) throw new IllegalArgumentException("non-positive value");   // runs before super(...)
        super(Long.toString(value));
    }
}
// Rule: code before super(...) must not reference `this` (no instance fields or methods yet).
```

### Q17. Compact source file and instance `main` (final in Java 25)

```java
// Hello.java
void main() {
    IO.println("Hello, Java 25");
}
// Run with: java Hello.java
```

### Q18. Sequenced Collections (Java 21)

```java
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
list.getFirst();            // "a"
list.getLast();             // "c"
list.reversed();            // reverse view: [c, b, a]
list.addFirst("z");

LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
m.put("x", 1); m.put("y", 2);
m.firstEntry();             // x=1
m.reversed();               // reverse-ordered map view
```

### Q19. Stream pitfalls

```java
// 1. Duplicate keys
Map<String, User> byEmail = users.stream()
        .collect(Collectors.toMap(User::email, u -> u));                  // IllegalStateException on duplicates
Map<String, User> safe = users.stream()
        .collect(Collectors.toMap(User::email, u -> u, (a, b) -> a));     // merge function

// 2. Shared mutable state in parallel streams
List<Integer> out = new ArrayList<>();
IntStream.range(0, 10_000).parallel().forEach(out::add);                  // data race
List<Integer> ok = IntStream.range(0, 10_000).parallel().boxed().toList();

// 3. Reusing a stream
Stream<String> s = Stream.of("a");
s.count();
s.count();                                                                // IllegalStateException
```

### Q20. `Arrays.asList` vs `List.of`

```java
List<String> a = Arrays.asList("x", "y");
a.set(0, "z");              // allowed, writes through to the array
a.add("w");                 // UnsupportedOperationException (fixed size)

List<String> b = List.of("x", "y");
b.set(0, "z");              // UnsupportedOperationException (immutable)
List.of("x", null);         // NullPointerException (nulls not allowed)
```

### Q21. Typical memory leaks

```java
// 1. Static collection that only grows
static final Map<String, Session> SESSIONS = new HashMap<>();

// 2. Listener never unregistered
eventBus.register(this);                 // missing: eventBus.unregister(this)

// 3. ThreadLocal never removed on pooled threads

// 4. Unbounded cache
static final Map<Key, Value> CACHE = new ConcurrentHashMap<>();   // use Caffeine with size/TTL

// 5. Inner or anonymous class holding the outer instance
class Outer { class Inner {} }           // Inner keeps a hidden reference to Outer; prefer static nested
```

### Q22. Producer-consumer with `BlockingQueue` and graceful stop

```java
BlockingQueue<Task> queue = new ArrayBlockingQueue<>(100);   // bounded: back-pressure
Task POISON = new Task(-1);

Runnable producer = () -> {
    try {
        for (int i = 0; i < 1000; i++) queue.put(new Task(i));  // blocks when full
        queue.put(POISON);
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
};

Runnable consumer = () -> {
    try {
        while (true) {
            Task t = queue.take();
            if (t == POISON) break;
            process(t);
        }
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
};
```

### Q23. Write a thread-safe lazy singleton (three ways) and explain failure modes

```java
// Holder idiom
final class A { private A() {} private static class H { static final A I = new A(); } static A get() { return H.I; } }

// Enum
enum B { INSTANCE }

// Double-checked locking (volatile required)
final class C {
    private static volatile C instance;
    private C() {}
    static C get() {
        C local = instance;
        if (local == null) {
            synchronized (C.class) {
                local = instance;
                if (local == null) instance = local = new C();
            }
        }
        return local;
    }
}
```

### Q24. Custom exception design

```java
public class PaymentFailedException extends RuntimeException {
    private final String orderId;
    private final ErrorCode code;

    public PaymentFailedException(String orderId, ErrorCode code, Throwable cause) {
        super("Payment failed for order " + orderId + " (" + code + ")", cause);   // keep the cause
        this.orderId = orderId;
        this.code = code;
    }
    public String orderId() { return orderId; }
    public ErrorCode code() { return code; }
}
// Discuss: checked or unchecked, what the API boundary exposes, and whether it should capture a stack trace.
```

## 14. Production Scenario Questions

1. A Java service's CPU suddenly hits 100% after a release. Walk through the investigation from first alert to root cause (thread CPU, GC, JFR, recent changes, regex or loops).
2. Heap usage grows slowly for days and the pod eventually restarts. How do you prove it is a leak and find the owner (histograms, heap dump, dominator tree, GC roots)?
3. p99 latency spikes every few minutes while p50 is fine. List every cause you would test (GC pauses, safepoints, JIT deopt, noisy neighbour, pool exhaustion, downstream).
4. The container is OOMKilled but the Java heap is well below `-Xmx`. What else uses memory, and how do you diagnose and fix it?
5. All request threads are stuck and the service stops responding. How do you read thread dumps to distinguish a deadlock, pool exhaustion, a slow downstream, or a lock convoy?
6. After upgrading Java 11 to 21, a service shows higher memory use and different GC behaviour. How do you investigate and decide whether to tune or roll back?
7. A batch job that used to take 20 minutes now takes 3 hours. How do you find the cause (data volume, GC, N+1, pool sizing, parallelism, a changed library)?
8. Frequent full GCs appear in a G1 service during peak traffic. What do the logs tell you, and what are the fix options (allocation reduction, heap sizing, humongous objects, IHOP, collector change)?
9. `OutOfMemoryError: unable to create native thread` appears under load. What are the causes (thread count, ulimit, container limits, stack size) and the fixes?
10. A new engineer used `parallelStream()` with blocking calls and the whole service slowed down. Explain what happened and how to fix it.
11. A scheduled task silently stopped running. What could have swallowed the exception, and how do you make failures visible?
12. A `HashMap` shared between threads caused data loss intermittently. How do you prove and fix it, and what testing would have caught it?
13. A service's startup time went from 20s to 90s. How do you investigate (class scanning, bean creation, network calls at startup, JIT, CDS)?
14. Your team wants to adopt virtual threads for a high-traffic API. How do you evaluate benefits, risks (pinning, pool limits, thread-local usage), and roll out safely?
15. A library upgrade breaks at runtime with `NoSuchMethodError` or `ClassCastException` across modules. How do you find the dependency conflict (classpath, shading, class loader)?
16. JSON serialisation is the top CPU consumer in a profile. What options do you have (library settings, schema-based formats, caching, reducing payloads)?
17. Metaspace keeps growing in an application server after redeploys. What is the likely cause and how do you fix it?
18. A service suddenly becomes slow for one tenant only. How would you use JFR, metrics, and logs to isolate it?
19. You must reduce the memory cost of 200 JVM services in Kubernetes. What do you change (heap sizing, compact headers, class sharing, smaller base images, GC choice, native image where appropriate)?
20. Time-related bugs appear only around DST changes and month ends. How do you find and prevent them (`java.time`, UTC storage, injected `Clock`, property-based tests)?

## 15. Leadership and Design-Level Java Questions

1. How do you decide which JDK version and distribution your organisation standardises on, and how do you keep 100 services from falling behind?
2. How do you run a performance review culture (budgets, benchmarks in CI, load tests, regressions caught before release)?
3. How do you review Java code for concurrency bugs and what checklist do you use?
4. How do you coach a team away from over-engineering (unneeded patterns, premature optimisation, reactive everywhere)?
5. When do you pick Java over Go, Kotlin, or Node for a new service, and what are Java's current strengths and weaknesses?
6. How do you manage technical debt in a long-lived Java codebase (deprecation policy, test-first refactoring, strangler approach, build and dependency hygiene)?
7. How do you design a shared library so that consumers can upgrade independently (API stability, semantic versioning, `@Deprecated(forRemoval)`, binary compatibility)?
8. How do you evaluate a new Java feature for adoption (preview vs final, ecosystem support, tooling, team readiness, rollback plan)?
9. How do you set coding standards for exceptions, logging, nullability, immutability, and concurrency across teams?
10. How do you onboard and mentor engineers on JVM internals and production troubleshooting (runbooks, game days, post-mortems)?
11. Describe the most difficult production incident you handled in a Java system, what you learned, and what you changed afterwards.
12. How do you balance delivery pressure and quality when a deadline conflicts with a needed refactor or upgrade?

## 16. Rapid-Fire Questions

1. What is the difference between JDK, JRE, and JVM?
2. Is Java pass-by-value or pass-by-reference?
3. What is the default value of an `int`, `boolean`, and object reference field?
4. Can you override a `static` method? A `private` method?
5. What is the difference between `==` and `equals`?
6. What does `final` mean on a class, method, and variable?
7. Why is `String` immutable?
8. `StringBuilder` vs `StringBuffer`?
9. Which collection would you use for a unique, ordered set? A FIFO queue? A priority queue?
10. What is the initial capacity and load factor of `HashMap`?
11. At what chain length does `HashMap` treeify a bucket?
12. How does `ConcurrentHashMap` achieve thread safety in Java 8+?
13. What is the difference between fail-fast and fail-safe iterators?
14. What is the difference between `sleep()` and `wait()`?
15. What does `volatile` guarantee?
16. What is a daemon thread?
17. What is the difference between `submit()` and `execute()`?
18. What is a virtual thread?
19. What is the maximum number of threads a JVM can create?
20. What is Metaspace?
21. What are GC roots?
22. Which GC is the default in Java 8? In Java 9+?
23. What causes `StackOverflowError`?
24. What causes `OutOfMemoryError: Metaspace`?
25. What is the difference between `ClassNotFoundException` and `NoClassDefFoundError`?
26. What is the effect of `System.gc()`?
27. What is type erasure?
28. What is the difference between `Comparable` and `Comparator`?
29. What is a functional interface?
30. What is the difference between `map` and `flatMap`?
31. Name the new Java 25 language features.
32. Which Java versions are LTS?
33. What is a record, and what is a sealed class?
34. What is the diamond problem and how does Java resolve it with default methods?
35. What is a memory leak in Java, and give three common causes?

## Appendix A: JVM Flags Cheat Sheet

Verify each flag against the documentation for your JDK version before using it.

```text
# Heap and container awareness
-XX:MaxRAMPercentage=75.0            # heap as % of container memory (leave room for non-heap)
-XX:InitialRAMPercentage=50.0
-Xms2g -Xmx2g                        # fixed heap for servers (avoids resize pauses)
-XX:ActiveProcessorCount=4           # override detected CPUs if needed

# Collector selection
-XX:+UseG1GC                         # default on most server-class machines
-XX:+UseZGC                          # low-latency, generational by default in recent releases
-XX:+UseShenandoahGC                 # low pause, concurrent compaction
-XX:+UseParallelGC                   # throughput / batch
-XX:+UseSerialGC                     # tiny containers
-XX:MaxGCPauseMillis=200             # G1 pause goal (a target, not a guarantee)

# Logging and diagnostics
-Xlog:gc*:file=gc.log:time,uptime,level,tags:filecount=5,filesize=50m
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/dumps
-XX:+ExitOnOutOfMemoryError          # let the orchestrator restart a broken JVM
-XX:NativeMemoryTracking=summary     # then: jcmd <pid> VM.native_memory summary
-XX:StartFlightRecording=filename=/tmp/rec.jfr,duration=120s,settings=profile

# Memory regions
-XX:MaxMetaspaceSize=512m
-XX:MaxDirectMemorySize=512m
-Xss512k                             # thread stack size (affects recursion depth and thread count)
-XX:ReservedCodeCacheSize=240m

# Newer features (confirm flag names and status on your JDK)
-XX:+UseCompactObjectHeaders         # product feature in Java 25
-XX:+UseStringDeduplication          # G1
```
