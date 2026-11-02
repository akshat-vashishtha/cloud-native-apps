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

#### 1. Walk through the language features that changed how you write Java since 8. Which ones changed your design style, and which ones are mostly syntax sugar?
- **Simple Meaning (Dev-Friendly Question):** What are the biggest Java features from Java 8 to 25? Which ones actually changed how you design software, and which ones just save typing?
- **Interview Answer:**
  - **Changed Design Style (Architecture & Modeling):**
    - **Lambdas, Streams, & `Optional` (Java 8):** Shifted code from mutable loops and null checks to functional, declarative pipelines.
    - **Records, Sealed Classes, & Pattern Matching `switch` (Java 16–21):** Enabled **Data-Oriented Programming**. Instead of deep class hierarchies, we model fixed domain states (like `PaymentResult = Approved | Declined | Pending`) and let the compiler verify we handled every case.
    - **Virtual Threads & Scoped Values (Java 21–25):** Allowed us to write simple, synchronous blocking code that scales like reactive WebFlux without callback hell.
  - **Mostly Syntax Sugar (Productivity & Readability):**
    - `var` (Java 10), Text Blocks `"""` (Java 15), Switch Expressions `->` (Java 14), Pattern `instanceof` (Java 16), and Unnamed Variables `_` (Java 22). They make code cleaner and less verbose, but don't change system architecture.

#### 2. What are the LTS releases (8, 11, 17, 21, 25)? How do you choose an upgrade target and cadence for a large estate of services?
- **Simple Meaning (Dev-Friendly Question):** Which Java versions get long-term support, and how do you decide when and how to upgrade dozens of microservices?
- **Interview Answer:**
  - **LTS Releases:** Java **8, 11, 17, 21, and 25** (released every 2 years now).
  - **How to Choose & Upgrade:** Always standardize production microservices on an **LTS version** (today, **Java 21** or **Java 25**).
  - **Upgrade Cadence:** Don't skip more than one LTS (e.g., jumping 8 -> 25 is painful; upgrading 17 -> 21 -> 25 every 2 years is smooth). Upgrade base Docker images and CI pipelines first, test non-critical services in staging, and track framework requirements (e.g., Spring Boot 3 requires Java 17+).

#### 3. How would you plan a migration from Java 8 to 21 or 25 for 100 services (module and illegal-access issues, removed APIs, dependency compatibility, GC defaults, container behaviour, build tooling, staged rollout)?
- **Simple Meaning (Dev-Friendly Question):** If your company has 100 microservices still on Java 8, what is your step-by-step plan to upgrade them to Java 21/25 without breaking production?
- **Interview Answer:**
  1. **Build Tooling & Dependencies First:** Upgrade Maven/Gradle, Lombok, ByteBuddy, Mockito, and Jackson first—old versions can't read new classfile bytecode.
  2. **Fix Removed JDK APIs:** Java 11 removed `javax.xml.bind` (JAXB), `javax.annotation`, and SOAP APIs—add them explicitly as Maven dependencies.
  3. **Handle Strong Encapsulation (JPMS):** Libraries doing deep reflection on JDK internals will fail on Java 17+. Upgrade those libraries, or use temporary `--add-opens` JVM flags until fixed.
  4. **Check GC & Container Defaults:** Java 8 defaulted to Parallel GC and ignored Docker memory limits in early builds; Java 21/25 defaults to **G1GC** and respects container cgroups. Remove outdated Java 8 GC flags and use `-XX:MaxRAMPercentage=75.0`.
  5. **Staged Rollout:** Automate upgrades using **OpenRewrite**, run canary deployments on 1 pod per service, and compare CPU, memory, and p99 latency before full rollout.

#### 4. What changed in Java 9 modules (JPMS)? Why did strong encapsulation break libraries (`--add-opens`), and how do you handle it?
- **Simple Meaning (Dev-Friendly Question):** What is the Java Module System, why did it break older libraries with `InaccessibleObjectException`, and how do you fix it?
- **Interview Answer:**
  - **What Changed:** Java 9 split the monolithic JDK (`rt.jar`) into modules (`java.base`, `java.sql`, etc.) and locked down internal JDK classes (`sun.misc.*`, `jdk.internal.*`) so random code can't call `setAccessible(true)` on private JDK fields.
  - **Why Libraries Broke:** Older Jackson, Hibernate, or Netty versions used reflection on private JDK classes (like `String` or `DirectByteBuffer`). Starting in Java 16/17, the JVM blocks this by default.
  - **How to Fix:** First choice is to upgrade the library to a modern version that uses public APIs or `VarHandle`. As a temporary workaround, add `--add-opens java.base/java.lang=ALL-UNNAMED` to JVM startup flags.

#### 5. What are `var`, text blocks, switch expressions, and `yield`? What are the readability pitfalls of `var`?
- **Simple Meaning (Dev-Friendly Question):** How do `var`, multi-line strings, and modern `switch` expressions work, and when is using `var` a bad idea?
- **Interview Answer:**
  - **`var` (Java 10):** Infers local variable types at compile time (`var list = new ArrayList<String>();`). **Pitfall:** Avoid `var` when the right side hides the type (e.g., `var data = service.process();`), because readers can't tell what `data` is without an IDE.
  - **Text Blocks `"""` (Java 15):** Multi-line strings for JSON, SQL, or HTML without ugly `\n` and `\"` escaping. It automatically strips common leading indentation.
  - **Switch Expressions & `yield` (Java 14):** `switch` can now return a value (`int days = switch(month) { ... };`), uses `->` with no fall-through bugs, and uses `yield value;` when a case block has multiple statements before returning a value.

#### 6. Records: what do they generate, what are their limits (no inheritance, shallow immutability), and when do you not use them (JPA entities, mutable state)? How do compact constructors work?
- **Simple Meaning (Dev-Friendly Question):** What does a Java `record` give you automatically, what are its limitations, and why shouldn't you use it for JPA entities?
- **Interview Answer:**
  - **What It Generates:** `record UserDto(String id, String email) {}` automatically generates `private final` fields, a canonical constructor, getters (`id()`, `email()`), and value-based `equals()`, `hashCode()`, and `toString()`.
  - **Limitations:** Records are implicitly `final` and extend `java.lang.Record` (so they cannot `extends` another class, though they can `implements` interfaces). They are **shallowly immutable**—if a record holds a mutable `List`, someone can still modify the list unless you copy it.
  - **When NOT to Use:** Do not use records for **JPA `@Entity` classes**, because Hibernate requires a no-arg constructor, mutable setters/fields, and runtime proxies. Use records for DTOs, API requests/responses, events, and map keys.
  - **Compact Constructor:** `public UserDto { Objects.requireNonNull(id); }`—lets you validate or normalize fields (`email = email.trim()`) without repeating the parameter list or `this.x = x` assignments.

#### 7. Sealed classes and interfaces: what problem do they solve? How do they combine with records and pattern-matching `switch` to model algebraic data types and get exhaustiveness checks?
- **Simple Meaning (Dev-Friendly Question):** What is a `sealed` class/interface, and why is combining `sealed` + `record` + `switch` so powerful?
- **Interview Answer:**
  - **The Problem Solved:** Normally, an interface can be implemented by *anyone* anywhere, or a package-private class can't be exposed publicly. `sealed interface PaymentResult permits Success, Declined, Error` explicitly restricts which classes are allowed to implement it.
  - **Why Combining Them Is Powerful:** When you pair a `sealed` interface with `record` subtypes and a `switch` expression, the compiler knows the **exact closed set of subtypes**. You don't need a `default` branch, and if a developer adds a new subtype later (e.g., `Timeout`), the compiler immediately flags every `switch` in the codebase that forgot to handle it!

#### 8. Pattern matching: `instanceof` patterns, `switch` patterns, guards (`when`), record deconstruction patterns, and nested patterns. How is dominance and exhaustiveness checked?
- **Simple Meaning (Dev-Friendly Question):** How does modern pattern matching work in `instanceof` and `switch`, and what do "dominance" and "exhaustiveness" mean?
- **Interview Answer:**
  - **`instanceof` Pattern:** `if (obj instanceof String s && !s.isBlank())` checks the type and casts to `s` in one step.
  - **`switch` & Guards (`when`):** `case String s when s.length() > 5 -> ...` matches both type and condition.
  - **Record Deconstruction & Nesting:** `case Order(Customer(String name), double total) -> ...` unpacks fields directly inside the `case` label without calling getters manually.
  - **Dominance:** If a general case (`case Object o ->`) is placed *above* a specific case (`case String s ->`), the compiler throws an error because the second case is unreachable (dominated).
  - **Exhaustiveness:** A `switch` expression must cover all possible inputs (either via a `sealed` hierarchy or a `default` branch), or it won't compile.

#### 9. What are unnamed variables and patterns (`_`), and where do they improve code (catch blocks, lambda parameters, unused pattern components)?
- **Simple Meaning (Dev-Friendly Question):** What does the underscore `_` variable in Java 22+ do, and where should you use it?
- **Interview Answer:**
  - `_` tells the compiler and readers: *"I intentionally don't use this variable."*
  - **Best Use Cases:**
    - Lambda parameters you don't need: `map.computeIfAbsent(key, _ -> new ArrayList<>())`
    - Ignored record fields during pattern matching: `case Point(int x, _) -> x`
    - Catch blocks where the exception object isn't needed: `catch (NumberFormatException _) { return 0; }`

#### 10. Sequenced Collections (Java 21): which interfaces were added (`SequencedCollection`, `SequencedSet`, `SequencedMap`), which methods (`getFirst`, `getLast`, `reversed`, `addFirst`), and what problem did they solve?
- **Simple Meaning (Dev-Friendly Question):** Why did Java 21 add `SequencedCollection`, and what annoying problem did it fix?
- **Interview Answer:**
  - **Problem Fixed:** Before Java 21, getting the first or last element was inconsistent across collections (`list.get(0)` vs `list.get(list.size() - 1)`, `deque.getFirst()`, `sortedSet.first()`, and `LinkedHashSet` had no easy way to get the last element at all!).
  - **New Interfaces:** `SequencedCollection`, `SequencedSet`, and `SequencedMap`.
  - **Key Methods:** `getFirst()`, `getLast()`, `addFirst()`, `addLast()`, `removeFirst()`, `removeLast()`, and `reversed()` (which returns a lightweight reverse-order view without copying the collection).

#### 11. Virtual threads (Java 21): what are they, how are they scheduled, and what do they change for blocking I/O code? What changed in Java 24 regarding `synchronized` pinning?
- **Simple Meaning (Dev-Friendly Question):** How do Virtual Threads work under the hood, why are they great for blocking I/O, and what did Java 24 fix about `synchronized`?
- **Interview Answer:**
  - **What They Are:** Lightweight threads managed by the JVM instead of the OS. Thousands or millions of virtual threads run on top of a small pool of OS "carrier" threads (`ForkJoinPool` sized to CPU cores).
  - **How They Work for I/O:** When a virtual thread makes a blocking DB or HTTP call, the JVM automatically **unmounts** (saves its stack to the heap) and frees the OS carrier thread to run other virtual threads. When I/O completes, it mounts back onto any free carrier thread.
  - **What Changed in Java 24 (JEP 491):** In Java 21, if a virtual thread blocked on I/O *inside* a `synchronized` block, it **pinned** the OS carrier thread (couldn't unmount), risking thread starvation. **Java 24 fixed this** so virtual threads can unmount even inside `synchronized` blocks!

#### 12. Stream Gatherers (Java 24): what gap in the Streams API do they fill (windowing, folding, custom intermediate operations)? Give an example.
- **Simple Meaning (Dev-Friendly Question):** Why did Java 24 add `Stream.gather(...)`, and what can it do that `map`/`filter` couldn't?
- **Interview Answer:**
  - **The Gap:** Since Java 8, `Collector` let you build custom *terminal* steps (`.collect(...)`), but you couldn't easily write stateful *intermediate* operations like batching a stream into chunks of 100, sliding windows, or deduplicating consecutive items.
  - **Example:** `stream.gather(Gatherers.windowFixed(100)).toList()` splits a stream into batches of 100 elements—super useful for batch DB inserts or paginated API calls.

#### 13. Foreign Function and Memory API (Java 22): how does it replace JNI and `Unsafe`, and why does it matter for native interop and off-heap memory?
- **Simple Meaning (Dev-Friendly Question):** What is the Foreign Function & Memory (FFM) API, and why is it much better than old JNI and `sun.misc.Unsafe`?
- **Interview Answer:**
  - **Replaces JNI:** Lets Java call native C/C++ libraries directly in pure Java (`Linker`, `SymbolLookup`) without writing brittle C glue code or risking JVM segfaults.
  - **Replaces `Unsafe`:** Provides `MemorySegment` and `Arena` to allocate and access off-heap memory safely with bounds checking, thread-confinement rules, and deterministic deallocation (`try (Arena arena = Arena.ofConfined())`), preventing use-after-free bugs and memory leaks.

#### 14. Class-File API (Java 24): who benefits (frameworks, bytecode tooling) and how does it relate to ASM and ByteBuddy?
- **Simple Meaning (Dev-Friendly Question):** Why did Java 24 build a standard API for reading and writing `.class` files?
- **Interview Answer:**
  - Previously, every time a new Java version came out every 6 months, frameworks like Spring, Hibernate, Gradle, and Mockito couldn't run on it until third-party bytecode libraries (**ASM**, **ByteBuddy**) updated their classfile parsers.
  - The standard **Class-File API (`java.lang.classfile`)** is built directly into the JDK and always supports the current JDK's bytecode version on day one, eliminating the upgrade bottleneck.

#### 15. What are the Java 25 language changes: compact source files with instance `main`, flexible constructor bodies (statements before `super(...)` / `this(...)`), and module import declarations? What problem does each solve?
- **Simple Meaning (Dev-Friendly Question):** What are the three developer-ergonomics features finalized in Java 25, and why were they added?
- **Interview Answer:**
  - **Compact Source Files & Instance `main`:** Lets you write `void main() { IO.println("Hi"); }` without `public class Hello` or `public static void main(String[] args)`. Makes Java great for scripts and beginners.
  - **Flexible Constructor Bodies:** Lets you validate or compute arguments *before* calling `super(...)` or `this(...)` (as long as you don't touch `this` before the super call). No more awkward static helper methods just to validate a constructor parameter!
  - **Module Import Declarations (`import module java.base;`):** Lets you import all public packages exported by a module in one line, handy for quick prototyping and scripts.

#### 16. Scoped Values (final in Java 25): how do they differ from `ThreadLocal`, why are they a better fit for virtual threads, and what are their limits (immutable, bounded lifetime, rebind via nested `where`)?
- **Simple Meaning (Dev-Friendly Question):** Why should we use `ScopedValue` instead of `ThreadLocal` when working with Virtual Threads?
- **Interview Answer:**
  - **Problems with `ThreadLocal`:** Anyone with a reference can mutate it, if you forget `.remove()` in a thread pool it leaks data across requests, and copying `InheritableThreadLocal` across millions of virtual threads wastes huge memory.
  - **Why `ScopedValue` Wins:**
    1. **Immutable:** Callees can read `USER.get()`, but cannot overwrite it directly (they can only rebind for deeper calls using a nested `ScopedValue.where(...).run(...)`).
    2. **Automatic Cleanup:** Bound only for the duration of `.run(...)` or `.call(...)`—guaranteed to be cleared as soon as the block finishes.
    3. **Zero-Copy Sharing:** Child virtual threads spawned via `StructuredTaskScope` share the parent's `ScopedValue` pointer in $O(1)$ memory without copying maps.

#### 17. What is Structured Concurrency (still preview in 25), and why is it considered a better model than fire-and-forget `ExecutorService` tasks?
- **Simple Meaning (Dev-Friendly Question):** What is Structured Concurrency, and what bug with normal thread pools does it prevent?
- **Interview Answer:**
  - **The Problem:** With traditional `ExecutorService.submit(...)`, if you spawn 3 parallel tasks and Task 1 fails immediately, Tasks 2 and 3 keep running in the background ("thread leaks"), wasting resources unless you write complex cancellation code.
  - **Structured Concurrency:** Treats parallel subtasks started in a `try (var scope = new StructuredTaskScope.ShutdownOnFailure())` block as a single unit of work. If one subtask fails, the scope automatically cancels the remaining subtasks, and the `try` block won't exit until all child threads have terminated cleanly.

#### 18. What are Compact Object Headers (final in Java 25), and what do you gain (heap size, cache efficiency)? What should you benchmark before enabling them?
- **Simple Meaning (Dev-Friendly Question):** What does `-XX:+UseCompactObjectHeaders` in Java 25 do, and how much memory does it save?
- **Interview Answer:**
  - **What It Does:** Shrinks the JVM object header on 64-bit platforms from **96–128 bits (12–16 bytes) down to 64 bits (8 bytes)** by packing the mark word and compressed class pointer together (Project Lilliput).
  - **What You Gain:** Reduces heap memory footprint by **10% to 20%** in object-heavy apps, fits more objects into CPU L1/L2 caches, and lowers GC frequency.
  - **What to Benchmark:** Measure throughput, GC pause times, and identity hashcode / heavy monitor locking workloads under realistic traffic before turning it on globally.

#### 19. What is Generational Shenandoah, and how does it relate to the generational ZGC change?
- **Simple Meaning (Dev-Friendly Question):** Why did both ZGC (Java 21/23) and Shenandoah (Java 25) add "Generational" modes?
- **Interview Answer:**
  - Most objects in Java die very young (like HTTP request DTOs). Originally, ZGC and Shenandoah scanned the *entire* heap together (single-generation), which worked well but required extra CPU and memory headroom at high allocation rates.
  - **Generational ZGC** and **Generational Shenandoah** separate the heap into **Young** and **Old** generations. By collecting young objects frequently without scanning the entire old generation, they drastically cut CPU overhead and need much less free heap headroom while still keeping GC pauses under 1 millisecond.

#### 20. What is the AOT work (class loading and linking in 24, command-line ergonomics and method profiling in 25) aimed at, and how does it relate to CDS, GraalVM native image, and Project Leyden?
- **Simple Meaning (Dev-Friendly Question):** How does Java 24/25's Ahead-of-Time (AOT) cache speed up Spring Boot startup, and how is it different from GraalVM Native Image?
- **Interview Answer:**
  - **Goal (Project Leyden):** Make standard JVM apps start almost instantly and reach peak speed quickly without losing standard JVM features.
  - **How It Works:** Using `-XX:AOTMode=record` / `create` (or `-XX:AOTCacheOutput=app.aot` in Java 25), the JVM records loaded classes, linked bytecode, and method profiles during a training run and saves them to an AOT cache file. On startup, the JVM maps that cache into memory, cutting startup time by **40%–60%**.
  - **vs GraalVM Native Image:** Native Image compiles everything to a static binary (fastest startup, tiny memory, but no dynamic class loading or full JIT peak throughput). Project Leyden AOT cache gives you fast startup **with zero code/reflection restrictions** and full C2 JIT peak performance.

#### 21. What JFR additions came in 25 (cooperative sampling, method timing and tracing, CPU-time profiling), and how would you use them in production?
- **Simple Meaning (Dev-Friendly Question):** What new profiling features did Java Flight Recorder (JFR) get in Java 25?
- **Interview Answer:**
  - **CPU-Time Profiling on Linux:** Accurately samples true OS CPU time per thread rather than just JVM execution states.
  - **Method Timing & Tracing:** Lets you measure execution time and invocation counts of specific Java methods via JFR configuration (`jcmd`) in production **without modifying code or redeploying**.
  - **Cooperative Sampling:** Improves stack-walking safety and accuracy when profiling virtual threads and native transitions.

#### 22. What is the status of Primitive Types in Patterns, Stable Values, and PEM encodings in 25? What does "preview" mean and how do you use or avoid preview features in production?
- **Simple Meaning (Dev-Friendly Question):** What does a "Preview Feature" mean in Java, and should you enable `--enable-preview` in production?
- **Interview Answer:**
  - **Status in Java 25:** Primitive types in patterns (`case int i`), `StableValue` (deferred immutable initialization), Structured Concurrency, and PEM encodings are **Preview** features in Java 25.
  - **What Preview Means:** The feature is fully implemented, but its API can still change—or even be removed—in the next JDK release based on feedback.
  - **Production Rule:** **Never enable `--enable-preview` in production microservices**, because preview bytecode is locked to that exact JDK version and the API may break on the next upgrade. Only use preview features in experiments or sandbox branches.

#### 23. What was removed or deprecated that can bite upgrades (Security Manager, `finalize`, Nashorn, applets, CORBA, `Thread.stop`, 32-bit ports, Unsafe memory-access methods)?
- **Simple Meaning (Dev-Friendly Question):** What old Java features have been deleted or disabled in recent versions that might break legacy apps during an upgrade?
- **Interview Answer:**
  - **`SecurityManager`:** Permanently disabled in Java 24 (causes errors if legacy libraries call `System.setSecurityManager`).
  - **`Object.finalize()`:** Deprecated for removal (use `Cleaner` or `try-with-resources`).
  - **`Thread.stop()` / `suspend()` / `resume()`:** Removed or throw `UnsupportedOperationException`.
  - **`sun.misc.Unsafe` memory methods:** Restricted/deprecated for removal in Java 23–25 (replaced by `VarHandle` and FFM `MemorySegment`).
  - **Nashorn JS engine, Applets, CORBA, JAXB:** Removed in Java 11–17.

#### 24. String templates were previewed and then dropped. What does this tell you about relying on preview features?
- **Simple Meaning (Dev-Friendly Question):** Why did Java remove String Templates (`STR."Hello \{name}"`) after previewing it in Java 21/22, and what's the lesson?
- **Interview Answer:**
  - String Templates were previewed in Java 21 and 22, then completely **withdrawn in Java 23** because the JDK team realized the `STR.` processor syntax was clunky and didn't integrate cleanly with existing APIs.
  - **The Lesson:** Preview features are **not guaranteed to become permanent**. If you wrote production code using `STR."..."` in Java 21, all of that code broke when upgrading to Java 23+. Stick to finalized features in production.

#### 25. What does "Java is faster/slower after upgrade" depend on (JIT changes, GC defaults, compact strings, object headers, container ergonomics)? How do you benchmark an upgrade properly?
- **Simple Meaning (Dev-Friendly Question):** Why does an app's speed and memory change when you upgrade Java, and how do you prove the upgrade actually helped?
- **Interview Answer:**
  - **Why It Changes:**
    - **Memory drops** thanks to Compact Strings (Java 9+), better G1 region management, and Compact Object Headers (Java 25).
    - **Latency improves** because G1GC / Generational ZGC pause times are drastically shorter than Java 8's Parallel GC.
    - **Throughput** can occasionally look slightly lower if moving from Parallel GC (pure throughput) to G1/ZGC (write/load barriers for low latency), or if container CPU limits (`ActiveProcessorCount`) restrict GC/JIT threads to 1 core.
  - **How to Benchmark Properly:** Run the old and new JDK side-by-side in staging or canary pods with identical container limits, warm up the JVM for 15+ minutes, and compare **p50/p95/p99 latency, CPU usage, RSS memory, and GC pause time %**.

---

## 2. Core Java and OOP Staples

#### 1. Why is Java "pass by value" even for objects? Show with a method that reassigns a parameter.
- **Simple Meaning (Dev-Friendly Question):** People get confused about whether Java passes objects by reference or by value. How do you explain it simply?
- **Interview Answer:**
  - Java is **100% pass-by-value**. For primitives (`int`), it passes a copy of the number. For objects, it passes a **copy of the memory address (reference)** pointing to the object.
  - If you modify the object's fields inside the method (`user.setName("Bob")`), the caller sees the change because both references point to the same heap object.
  - **Proof:** If you reassign the parameter itself inside the method (`user = new User("Alice");`), the caller's original `user` variable is **completely unchanged**, because you only overwrote your local copy of the pointer!

#### 2. `==` vs `equals()` vs `hashCode()`: explain the contract and what breaks in hash-based collections if it is violated.
- **Simple Meaning (Dev-Friendly Question):** How do `==`, `equals()`, and `hashCode()` work together, and what happens in a `HashMap` if you override one without the other?
- **Interview Answer:**
  - **`==`:** Checks if two variables point to the **exact same memory address** (or equal value for primitives).
  - **`equals()`:** Checks if two objects are **logically equal** (e.g., two `Customer` objects with the same `id`).
  - **The Contract:**
    1. If `a.equals(b)` is `true`, then `a.hashCode() == b.hashCode()` **must** be `true`.
    2. If two objects have the same `hashCode()`, they do **not** have to be equal (hash collision).
  - **What Breaks in `HashMap`:**
    - **Override `equals` but forget `hashCode`:** Two logically equal keys get different default memory-based hash codes and land in **different buckets**. `map.get(new Key("A"))` returns `null` even though `"A"` is in the map!
    - **Override `hashCode` but forget `equals`:** Keys land in the same bucket, but `equals()` falls back to `==` (memory address), so `map.get(...)` still fails and `HashSet` allows duplicate entries.

#### 3. `final`, `finally`, `finalize`: differences. Why was `finalize` deprecated for removal, and what replaces it (`Cleaner`, try-with-resources)?
- **Simple Meaning (Dev-Friendly Question):** What is the difference between `final`, `finally`, and `finalize()`, and why did Java kill `finalize()`?
- **Interview Answer:**
  - **`final` (Keyword):** Variable cannot be reassigned; method cannot be overridden; class cannot be subclassed.
  - **`finally` (Block):** Code after `try/catch` that always runs (used for cleanup).
  - **`finalize()` (Method - Deprecated for removal):** An old `Object` method called by GC before reclaiming an object.
  - **Why `finalize()` Was Killed:** It had unpredictable timing (might never run), slowed down GC enormously, could accidentally resurrect dead objects, and opened security vulnerabilities.
  - **What Replaces It:** Use **`try-with-resources` (`AutoCloseable`)** for deterministic cleanup (files, DB connections), and **`java.lang.ref.Cleaner`** only as a fallback safety net.

#### 4. Abstract class vs interface in modern Java (default methods, static methods, private methods, sealed). How does the diamond problem appear with default methods?
- **Simple Meaning (Dev-Friendly Question):** Since interfaces can now have method bodies (`default`, `static`, `private`), when do you still need an `abstract class`, and what happens if two interfaces have the same `default` method?
- **Interview Answer:**
  - **Interface vs Abstract Class:**
    - Use an **Interface** to define a *capability or contract* ("what it can do"); a class can implement many interfaces, and interfaces **cannot hold instance fields (state)** or constructors.
    - Use an **Abstract Class** when subclasses share **mutable instance state (`fields`)**, constructors, or `protected` helper methods.
  - **Diamond Problem with `default` Methods:** If `class C implements A, B` and both `A` and `B` define `default void hello()`, the code **will not compile** until `C` explicitly overrides `hello()` (and can optionally call `A.super.hello()`).

#### 5. What is the order of initialisation for static blocks, instance blocks, constructors, and field initialisers across a class hierarchy?
- **Simple Meaning (Dev-Friendly Question):** When you create a child object `new Child()`, in what exact order do static blocks, instance fields, and constructors run?
- **Interview Answer:**
  1. **Parent `static` fields & `static {}` blocks** (in top-to-bottom source order, once when class loads).
  2. **Child `static` fields & `static {}` blocks** (once when class loads).
  3. **Parent instance fields & `{}` instance blocks** (top-to-bottom).
  4. **Parent constructor body**.
  5. **Child instance fields & `{}` instance blocks** (top-to-bottom).
  6. **Child constructor body**.

#### 6. Inner class types (member, static nested, local, anonymous). What does an inner class hold implicitly, and how can that leak memory?
- **Simple Meaning (Dev-Friendly Question):** Why should inner classes almost always be marked `static`, and how does a non-static inner class cause a memory leak?
- **Interview Answer:**
  - **4 Types:**
    1. **`static` nested class:** Just a normal class grouped inside another class; holds **no reference** to the outer object.
    2. **Non-static member inner class:** Secretly holds a hidden `Outer.this` reference to the outer instance that created it.
    3. **Local class:** Defined inside a method.
    4. **Anonymous class:** Inline one-off subclass/implementation (also holds hidden `Outer.this` if created in an instance method).
  - **Memory Leak:** If a long-lived background thread or cache holds an instance of a **non-static inner class** (or anonymous class), that inner object secretly keeps the entire huge **Outer object** alive in memory forever, preventing GC from collecting it. **Rule:** Always make nested classes `static` unless they genuinely need `Outer.this`.

#### 7. How do lambdas differ from anonymous classes (`this` meaning, compilation via `invokedynamic`, capture rules, effectively final)?
- **Simple Meaning (Dev-Friendly Question):** Is a lambda just syntax sugar for an anonymous inner class, or does it work differently?
- **Interview Answer:**
  - They are **different under the hood**:
    1. **Compilation:** Anonymous classes compile into separate `MyClass$1.class` files on disk. Lambdas use **`invokedynamic`** (`LambdaMetafactory`), generating the implementation dynamically in memory at runtime without extra `.class` files.
    2. **Meaning of `this`:** Inside an anonymous class, `this` refers to the **anonymous class instance itself**. Inside a lambda, `this` refers to the **enclosing outer class**.
    3. **No Hidden Outer Leak:** If a lambda doesn't reference any instance fields of the outer class, the JVM compiles it as a stateless singleton function—it won't hold `Outer.this`!
    4. **Effectively Final:** Both require captured local variables to be `final` or **effectively final** (never reassigned after initialization).

#### 8. Autoboxing pitfalls: `Integer` cache range, `==` on boxed values, `NullPointerException` on unboxing, performance cost in loops.
- **Simple Meaning (Dev-Friendly Question):** What bugs and performance problems happen when mixing `int` and `Integer`?
- **Interview Answer:**
  - **1. The `Integer` Cache Trap (`==`):** Java caches `Integer.valueOf(i)` for values **`-128` to `127`**. So `Integer a = 100, b = 100; a == b` is `true`, but `Integer c = 200, d = 200; c == d` is `false`! Always use `.equals()` or unbox to `int`.
  - **2. Hidden `NullPointerException`:** `Integer count = null; if (count > 0)` throws an NPE because Java secretly calls `count.intValue()` to compare it with `0`.
  - **3. Loop Performance:** `Long sum = 0L; for (long i = 0; i < 10_000_000; i++) sum += i;` creates 10 million unnecessary `Long` objects on the heap. Always use primitive `long` in math loops.

#### 9. How do you design an immutable class correctly (final fields, defensive copies, no leaking `this`, `List.copyOf`)? Why does immutability help concurrency and caching?
- **Simple Meaning (Dev-Friendly Question):** What are the rules to make a class 100% immutable, and why do senior engineers love immutable objects?
- **Interview Answer:**
  - **Rules for True Immutability:**
    1. Declare the class `final` (or make it a `record`).
    2. Make all fields `private final` and provide no setters.
    3. **Defensive Copies on Input & Output:** If a field is a mutable object like `List<String>` or `Date`, copy it in the constructor (`this.items = List.copyOf(items);`) so the caller can't mutate their original list later.
    4. Never leak `this` during construction.
  - **Why It Helps:** Immutable objects are **inherently thread-safe** without any `synchronized` locks, safe to use as `HashMap` or cache keys, and can never be corrupted by another method.

#### 10. Explain `clone()` pitfalls (shallow copy, `Cloneable` design flaw). What do you use instead (copy constructors, records, factories)?
- **Simple Meaning (Dev-Friendly Question):** Why is `Object.clone()` considered broken in Java, and how should you copy objects instead?
- **Interview Answer:**
  - **Why `clone()` Is Broken:** `Cloneable` is an empty marker interface with no `clone()` method on it (`Object.clone()` is `protected`). Worse, `super.clone()` performs a **shallow field-by-field copy**—if your object contains a `List` or array, both the original and cloned object share the exact same underlying list!
  - **What to Use Instead:** Write an explicit **copy constructor** (`new Order(existingOrder)`), a static factory (`Order.copyOf(existingOrder)`), or use `record` with a `withX(...)` helper method.

#### 11. Enums: why are they a good singleton and strategy holder? How do `EnumMap`, `EnumSet`, and enum-based state machines work?
- **Simple Meaning (Dev-Friendly Question):** Beyond simple constants, how can you use Java `enum`s for patterns like Strategy, State Machines, and ultra-fast Maps/Sets?
- **Interview Answer:**
  - **Strategy & State Machines:** Each `enum` constant can override an abstract method or hold a lambda (e.g., `OrderStatus.SHIPPED.canTransitionTo(DELIVERED)`), keeping state transition rules cleanly inside the enum instead of scattered `if/else` blocks.
  - **Best Singleton:** An `enum` singleton is guaranteed by the JVM to be instantiated once, thread-safe, and immune to reflection and serialization attacks.
  - **`EnumSet` & `EnumMap`:** `EnumSet` stores flags as a single **64-bit `long` bitmask** (blazing fast, almost zero memory). `EnumMap` uses a plain **fixed array** indexed by `enum.ordinal()`—much faster and lighter than `HashMap`.

#### 12. `transient`, `volatile`, `synchronized`, `static`, `strictfp` (now redundant): what does each guarantee?
- **Simple Meaning (Dev-Friendly Question):** Give a one-sentence explanation of `transient`, `volatile`, `synchronized`, `static`, and `strictfp`.
- **Interview Answer:**
  - **`transient`:** Skips this field when the object is serialized (great for passwords or derived caches).
  - **`volatile`:** Guarantees **visibility and ordering** across threads—reads/writes go straight to main memory, and prevents instruction reordering (does *not* make `count++` atomic).
  - **`synchronized`:** Guarantees both **mutual exclusion** (only one thread inside at a time) and **memory visibility**.
  - **`static`:** Belongs to the **class itself** rather than any individual object instance.
  - **`strictfp`:** Used to force identical IEEE-754 floating-point math across CPUs; since **Java 17**, all floating-point math is strictly consistent by default, making `strictfp` redundant.

#### 13. How do access modifiers interact with modules (`exports`, `opens`, `requires transitive`) and reflection?
- **Simple Meaning (Dev-Friendly Question):** In modular Java, why isn't `public` always truly public anymore?
- **Interview Answer:**
  - Traditional modifiers are `private` (same class), package-private (same package), `protected` (same package + subclasses), and `public`.
  - With **JPMS (`module-info.java`)**, `public` only means public *inside your own module* unless your module explicitly declares:
    - `exports com.app.api;` -> allows other modules to access `public` types at compile and runtime.
    - `opens com.app.model;` -> allows frameworks (like Spring/Jackson) to use **deep reflection** on private fields at runtime.
    - `requires transitive ModuleB;` -> any module that depends on you automatically gets access to `ModuleB` too.

#### 14. What is the difference between checked/unchecked, compile-time/runtime polymorphism, and static/dynamic binding? How does the JVM dispatch a virtual call?
- **Simple Meaning (Dev-Friendly Question):** Explain method overloading vs overriding, static vs dynamic binding, and how the JVM figures out which overridden method to call at runtime.
- **Interview Answer:**
  - **Compile-Time Polymorphism (Static Binding):** **Method Overloading** (`print(String)` vs `print(int)`), plus `private`, `static`, and `final` methods. The compiler picks the exact method at compile time based on the variable's *declared* type.
  - **Runtime Polymorphism (Dynamic Binding):** **Method Overriding**. The JVM picks the method at runtime based on the *actual object's class* in heap memory.
  - **How the JVM Dispatches (`invokevirtual`):** Every class has a **vtable (virtual method table)**—an array of function pointers. At runtime, the JVM looks up the object's class pointer in its header and jumps to the method slot in the vtable (and the JIT compiler optimizes this further with **inline caching** if it sees only 1 or 2 concrete types!).

#### 15. What is covariant return, method hiding vs overriding, and why can't `static` methods be overridden?
- **Simple Meaning (Dev-Friendly Question):** Can an overridden method return a more specific subclass? And what happens if a child class defines a `static` method with the same name as the parent?
- **Interview Answer:**
  - **Covariant Return Type:** When overriding a parent method that returns `Number`, the child method is allowed to return a narrower subtype like `Integer` or `BigDecimal`.
  - **Why `static` Cannot Be Overridden (Method Hiding):** `static` methods belong to the class, not the object, so they don't use the object's runtime vtable (`invokestatic`). If a child class defines the same `static` method, it merely **hides** the parent method—which one runs depends only on the compile-time variable type, not the runtime object!

#### 16. What is the difference between `Comparable` and `Comparator`? How do you write a null-safe, multi-field comparator?
- **Simple Meaning (Dev-Friendly Question):** When do you implement `Comparable` vs passing a `Comparator`, and how do you chain sorting rules cleanly without `NullPointerException`?
- **Interview Answer:**
  - **`Comparable<T>` (`compareTo`):** Built *into* the class itself to define its **single natural ordering** (e.g., `String` alphabetically, `LocalDate` chronologically).
  - **`Comparator<T>` (`compare`):** Separate strategy object used when you want **multiple different sort orders** (by price, by rating) or don't own the class.
  - **Null-Safe Multi-Field Comparator:**
    ```java
    Comparator<User> cmp = Comparator.nullsLast(
        Comparator.comparing(User::lastName, Comparator.nullsLast(String::compareToIgnoreCase))
                  .thenComparingInt(User::age)
    );
    ```

#### 17. How does `Object.hashCode()` default work, and why must you not rely on it for identity across runs?
- **Simple Meaning (Dev-Friendly Question):** Where does the default `Object.hashCode()` number come from, and why is it unsafe to save it in a database or compare across restarts?
- **Interview Answer:**
  - Contrary to the myth that it's just the memory address, HotSpot generates the default **identity hash code** using a per-thread pseudo-random number generator (Marsaglia's xor-shift) and caches it in the object's Mark Word header.
  - **Why Never Persist It:** It changes on every JVM restart, and two distinct objects can even collide (since it's a 31-bit integer). Never use `hashCode()` as a unique ID!

#### 18. What are annotations, retention policies, and how do annotation processors differ from runtime reflection? When do frameworks use each?
- **Simple Meaning (Dev-Friendly Question):** How do annotations work at compile time vs runtime (e.g., Lombok/MapStruct vs Spring)?
- **Interview Answer:**
  - **3 Retention Policies:**
    1. `SOURCE`: Discarded by the compiler (`@Override`, `@SuppressWarnings`, Lombok).
    2. `CLASS`: Saved in `.class` file, ignored by JVM at runtime (default).
    3. `RUNTIME`: Kept in memory so frameworks can inspect it via reflection (`@RestController`, `@Transactional`).
  - **Compile-Time Processor vs Runtime Reflection:**
    - **Annotation Processors (`javax.annotation.processing`):** Run during `javac` compilation to generate new `.java` or bytecode files (**MapStruct, Lombok, Dagger**). Zero runtime reflection overhead!
    - **Runtime Reflection:** Inspects `RUNTIME` annotations when the app starts up (**Spring Boot, Jackson, Hibernate**).

#### 19. What are marker interfaces vs annotations? Why was `Serializable` designed as a marker and what are its downsides?
- **Simple Meaning (Dev-Friendly Question):** What is a marker interface like `Serializable`, and why do we prefer annotations today?
- **Interview Answer:**
  - A **marker interface** is an empty interface with no methods (`Serializable`, `Cloneable`, `RandomAccess`) used to tag a class. `Serializable` was created in Java 1.0 before annotations existed (Java 5).
  - **Advantage of Marker Interfaces:** Compile-time type checking—you can write a method parameter `void save(Serializable obj)`.
  - **Downsides:** Once a parent class implements `Serializable`, every subclass is forced to be `Serializable` forever, and you can't pass configuration metadata like `@JsonIgnoreProperties(ignoreUnknown = true)`. That's why modern frameworks use **annotations**.

#### 20. What are the SOLID violations you most commonly find in a 10-year-old Java codebase, and how do you fix them incrementally?
- **Simple Meaning (Dev-Friendly Question):** In legacy Java projects, how is SOLID usually broken, and how do you clean it up safely without rewriting everything at once?
- **Interview Answer:**
  - **Common Violations:**
    - **Single Responsibility (S):** 3,000-line `OrderService` "God classes" that validate HTTP input, calculate taxes, talk to the DB, and send emails.
    - **Open/Closed (O):** Giant `if/else` or `switch` blocks over customer types that require editing 15 files every time a new type is added.
    - **Dependency Inversion (D):** Business logic directly instantiating `new S3Client()` or depending on concrete database entities instead of interfaces/ports.
  - **How to Fix Incrementally:**
    1. Write characterization/integration tests around the existing method first.
    2. Extract one cohesive responsibility at a time (e.g., `TaxCalculator`, `OrderNotificationPort`) and inject it via constructor.
    3. Replace giant `if/else` branches with a `Map<OrderType, OrderProcessor>` strategy map injected by Spring.

---

## 3. Exception Handling

#### 1. Explain the `Throwable` hierarchy (`Error`, `Exception`, `RuntimeException`). Which should be caught, which should not, and why?
- **Simple Meaning (Dev-Friendly Question):** What is the difference between `Error`, Checked `Exception`, and `RuntimeException`, and which ones should your code never try to catch?
- **Interview Answer:**
  - **`Throwable`** is the root class with two branches:
    1. **`Error` (`OutOfMemoryError`, `StackOverflowError`):** Fatal JVM/system failures. **Never catch these** in business logic because the JVM is in an unrecoverable state.
    2. **`Exception`:** Application-level problems, split into:
       - **Checked Exceptions (`IOException`, `SQLException`):** Compiler forces you to `catch` or declare `throws`. Used for recoverable external failures.
       - **Unchecked / `RuntimeException` (`NullPointerException`, `IllegalArgumentException`, custom domain exceptions):** Not checked at compile time. Catch these at the global API boundary (`@RestControllerAdvice`) or when doing specific fallback/retry logic.

#### 2. Checked vs unchecked exceptions: what was the original rationale, what went wrong in practice (lambdas, leaky abstractions), and what is your team policy today?
- **Simple Meaning (Dev-Friendly Question):** Why did Java invent Checked Exceptions, why do most modern frameworks avoid them, and which one do you use in your projects?
- **Interview Answer:**
  - **Original Idea:** Force developers to handle foreseeable I/O errors (like missing files or network drops).
  - **What Went Wrong:**
    1. **Leaky Abstractions:** If a database DAO throws `SQLException`, every service and controller above it gets polluted with `throws SQLException`.
    2. **Breaks Lambdas/Streams:** Standard Java functional interfaces (`Function<T,R>`) don't allow checked exceptions, forcing ugly `try/catch` blocks inside streams.
  - **Modern Team Policy:** Use **unchecked (`RuntimeException`) subclasses** for all custom domain and application exceptions, and handle them centrally in a global exception handler.

#### 3. How do you design a custom exception hierarchy for a service (domain exceptions, error codes, cause chaining, immutability, serialisation)?
- **Simple Meaning (Dev-Friendly Question):** How do you structure custom exceptions in a microservice so errors are clean, traceable, and easy to map to HTTP responses?
- **Interview Answer:**
  - Create a single base **`DomainException extends RuntimeException`** containing:
    - An immutable `ErrorCode` enum (e.g., `ORDER_NOT_FOUND`, `INSUFFICIENT_FUNDS`).
    - Safe context metadata (like `orderId`), never raw sensitive PII.
    - Constructors that always pass `Throwable cause` to `super(message, cause)` so the root stack trace is never lost.
  - Subclass by category (`ResourceNotFoundException`, `BusinessRuleViolationException`, `DownstreamServiceException`) so `@RestControllerAdvice` can map them cleanly to `404`, `422`, or `503`.

#### 4. Try-with-resources: how does it work with `AutoCloseable`, in which order are resources closed, and what are suppressed exceptions (`addSuppressed`, `getSuppressed`)?
- **Simple Meaning (Dev-Friendly Question):** When `try (var a = ...; var b = ...)` finishes or throws an error, in what order do resources close, and what happens if BOTH the `try` block and `close()` throw an exception?
- **Interview Answer:**
  - **Close Order:** Resources are closed automatically in **reverse order of creation** (`b.close()` runs first, then `a.close()`).
  - **Suppressed Exceptions:** In old `try/finally`, if the `try` block threw an exception AND `close()` in `finally` also threw an exception, the `close()` exception completely **wiped out** the original `try` exception!
  - With `try-with-resources`, the **original `try` block exception wins**, and the `close()` exception is attached to it via `primaryEx.addSuppressed(closeEx)`. You can see both in stack traces or via `ex.getSuppressed()`.

#### 5. `finally` pitfalls: `return` inside `finally`, exceptions thrown in `finally` masking the original, `System.exit` in `try`. Why does a `finally` not run in some cases?
- **Simple Meaning (Dev-Friendly Question):** Why is putting `return` or throwing an exception inside `finally` a terrible bug, and when does `finally` NOT execute?
- **Interview Answer:**
  - **`return` in `finally`:** Silently **swallows any exception** thrown in the `try` block and overwrites the `try` block's return value!
  - **Throwing in `finally`:** Erases the original exception from the `try` block, hiding the real root cause.
  - **When `finally` Does NOT Run:** If `System.exit()` or `Runtime.getRuntime().halt()` is called, if the JVM crashes (`SIGKILL` / `OOMKilled` by Kubernetes), or if the thread enters an infinite loop inside `try`.

#### 6. Multi-catch and precise rethrow: what compile-time rules apply? Why can't you catch a subclass after a superclass?
- **Simple Meaning (Dev-Friendly Question):** What are the compiler rules for `catch (IOException | SQLException e)`, and why is the exception variable `e` implicitly `final`?
- **Interview Answer:**
  - **Multi-Catch Rules:** The exceptions separated by `|` **cannot be related by inheritance** (e.g., `catch (IOException | FileNotFoundException e)` won't compile because `FileNotFoundException` is already an `IOException`). Also, `e` is implicitly `final` in a multi-catch block.
  - **Subclass After Superclass:** If you put `catch (Exception e)` before `catch (IOException e)`, the first block catches everything, making the second block dead/unreachable code—so the compiler rejects it.

#### 7. Overriding rules with `throws` clauses. What can an overriding method declare, and why?
- **Simple Meaning (Dev-Friendly Question):** If a parent method declares `throws IOException`, what exceptions is a child overriding method allowed to throw?
- **Interview Answer:**
  - **For Checked Exceptions:** The overriding child method can throw **the same checked exception, a narrower subclass** (e.g., `FileNotFoundException`), **or nothing at all**. It **cannot** throw a broader checked exception (like `Exception`), because callers holding a `Parent` reference only prepared to catch `IOException` (Liskov Substitution Principle).
  - **For Unchecked (`RuntimeException`):** Any overriding method can throw any `RuntimeException` at any time.

#### 8. What does exception translation mean (DAO layer converting `SQLException` into domain exceptions)? Where do you draw the layer boundaries?
- **Simple Meaning (Dev-Friendly Question):** Why does Spring convert low-level database errors (`SQLException`) into `DataAccessException`, and how should your layers translate exceptions?
- **Interview Answer:**
  - **Meaning:** Catching a low-level technical exception (like `SQLException` or `FeignException`) at an infrastructure boundary and wrapping it into a meaningful domain/application exception (e.g., `DuplicateOrderException`).
  - **Why:** Your Domain/Service layer shouldn't know whether data lives in PostgreSQL, MongoDB, or an HTTP API. Spring's `@Repository` automatically translates vendor-specific SQL error codes into its unified `DataAccessException` hierarchy.

#### 9. Exception chaining: how do you preserve the root cause, and what is the cost of building a stack trace (`fillInStackTrace`)? When are stackless or preallocated exceptions justified?
- **Simple Meaning (Dev-Friendly Question):** Why are exceptions expensive to create in Java, and when would you ever create an exception *without* a stack trace?
- **Interview Answer:**
  - **Preserving Root Cause:** Always pass `e` as the last argument: `throw new OrderException("Failed for " + id, e);`. Never do `e.getMessage()` alone, or you lose the line numbers!
  - **Why Exceptions Are Expensive:** Creating `new Exception()` calls `fillInStackTrace()`, which walks the entire call stack frame-by-frame—taking **10x to 100x longer** than a normal object allocation.
  - **Stackless Exceptions:** Overriding `fillInStackTrace()` to no-op (or calling `super(msg, cause, false, false)`) is only justified in ultra-hot paths where an exception is used as a fast signal (or validation failures occurring thousands of times per second).

#### 10. Should you use exceptions for control flow? What is the performance and design argument? Compare exceptions vs `Optional` vs result types (`Either`-style) for expected failures.
- **Simple Meaning (Dev-Friendly Question):** When should a method throw an exception vs returning `Optional` vs returning a `Result` object?
- **Interview Answer:**
  - **Never use exceptions for normal control flow** (e.g., throwing `UserNotFoundException` just to check if an email exists during registration)—it hurts readability and wastes CPU building stack traces.
  - **Rule of Thumb:**
    - **Return `Optional<T>`:** When "not found" is a normal, expected outcome (`findByEmail(email)`).
    - **Return a Result/Sealed Type (`sealed interface PaymentOutcome permits Approved, Declined`):** When there are multiple expected business outcomes that the caller must handle explicitly.
    - **Throw an Exception:** For unexpected failures, broken invariants, or aborting a transaction (`InsufficientBalanceException`, DB down).

#### 11. How do you handle exceptions in lambdas and streams (checked exceptions in functional interfaces, wrapper helpers, avoiding silent swallowing)?
- **Simple Meaning (Dev-Friendly Question):** If a method inside `.map(...)` throws a checked `IOException`, how do you handle it cleanly without making the stream unreadable?
- **Interview Answer:**
  - Don't put a 10-line `try/catch` block directly inside `.map(...)`.
  - **Cleanest Solutions:**
    1. Extract the body into a private helper method that catches the checked exception and wraps it in **`UncheckedIOException`** (or a custom `RuntimeException`).
    2. Or if partial failures are expected in a batch stream, return a result wrapper (`Result.success(val)` / `Result.failure(ex)`) and partition successes from failures at the end.

#### 12. How do exceptions behave in threads: `Thread.UncaughtExceptionHandler`, tasks submitted to an `ExecutorService` (`execute` vs `submit`), `Future.get()` wrapping in `ExecutionException`?
- **Simple Meaning (Dev-Friendly Question):** What happens when a background thread throws an uncaught exception in `execute()` vs `submit()`?
- **Interview Answer:**
  - **`executor.execute(runnable)`:** The uncaught exception kills that worker thread (the pool creates a replacement) and sends the error to `Thread.UncaughtExceptionHandler` (which prints to stderr/logs by default).
  - **`executor.submit(callable)`:** **Silently catches and stores the exception inside the returned `Future`!** If you never call `future.get()`, **the exception completely disappears without a single log line** (a classic production bug!). When you do call `future.get()`, it rethrows the error wrapped inside `ExecutionException`.

#### 13. How do you handle failures in `CompletableFuture` (`exceptionally`, `handle`, `whenComplete`, `CompletionException` wrapping, timeouts)?
- **Simple Meaning (Dev-Friendly Question):** What is the difference between `exceptionally()`, `handle()`, and `whenComplete()` in `CompletableFuture`?
- **Interview Answer:**
  - **`exceptionally(ex -> fallbackValue)`:** Like a `catch` block that only runs on error and recovers by returning a fallback value.
  - **`handle((result, ex) -> ...)`:** Always runs (whether success or failure) and lets you transform the final result or recover from the error.
  - **`whenComplete((result, ex) -> ...)`:** Like a `finally` block—always runs so you can log or record metrics, but **does not change** the result or swallow the exception.
  - **Timeout:** Always attach `.orTimeout(2, TimeUnit.SECONDS)` or `.completeOnTimeout(fallback, 2, SECONDS)` (Java 9+) so a hung downstream call never blocks forever.

#### 14. `StackOverflowError`, `OutOfMemoryError`, `NoClassDefFoundError` vs `ClassNotFoundException`, `ExceptionInInitializerError`: causes and what you can reasonably do about each.
- **Simple Meaning (Dev-Friendly Question):** What causes each of these 5 classic JVM crashes, and especially what's the difference between `ClassNotFoundException` and `NoClassDefFoundError`?
- **Interview Answer:**
  - **`StackOverflowError`:** Infinite recursion (e.g., bidirectional Jackson `toString()` or bad recursion exit condition).
  - **`OutOfMemoryError`:** Heap is full (leak/huge payload), Metaspace is full, or off-heap Direct Memory is exhausted.
  - **`ClassNotFoundException` (Checked `Exception`):** You tried to load a class dynamically by string name (`Class.forName("com.foo.Bar")`) and the JAR isn't on the classpath.
  - **`NoClassDefFoundError` (`Error`):** The class **was present at compile time**, so your code references it directly, but at runtime either the JAR is missing (Maven dependency clash) OR the class's `static {}` block crashed on the first load attempt!
  - **`ExceptionInInitializerError`:** Thrown the *first* time a class's `static {}` initializer or `static` field assignment throws an uncaught exception. (Any *second* attempt to touch that broken class throws `NoClassDefFoundError`!).

#### 15. How do you handle exceptions in static initialisers and constructors? What state is left behind (partially constructed objects, class in error state)?
- **Simple Meaning (Dev-Friendly Question):** What happens if a `static {}` block or a constructor throws an exception halfway through?
- **Interview Answer:**
  - **In a `static {}` Block:** The JVM wraps it in `ExceptionInInitializerError` and marks the class as **permanently unusable** for the life of that JVM process. Moral: Never do network calls or risky I/O inside `static` initializers!
  - **In a Constructor:** The object construction aborts, the caller never receives the reference, and the partially created memory is garbage collected—**unless** the constructor leaked `this` (e.g., `registry.register(this)` before throwing), which leaves a broken "zombie" object alive!

#### 16. What are good logging practices for exceptions (log once at the boundary, do not log and rethrow, include context, avoid leaking sensitive data)?
- **Simple Meaning (Dev-Friendly Question):** What are the golden rules for logging exceptions in production?
- **Interview Answer:**
  1. **Never "Log and Rethrow":** Doing `log.error("Failed", e); throw e;` prints the exact same 80-line stack trace 3 times in Kibana/Datadog. Instead, wrap with context (`throw new OrderException("Failed order " + orderId, e)`) and **log once at the top boundary** (`@RestControllerAdvice` or Kafka consumer error handler).
  2. **Pass the Exception Object Last:** `log.error("Order {} failed for tenant {}", orderId, tenantId, ex);`—SLF4J automatically prints the full stack trace when `Throwable` is the final argument.
  3. **Never Log Secrets/PII:** Never put passwords, credit cards, or raw JWTs inside exception messages.

#### 17. How do you design retry behaviour with exceptions (idempotency, which exceptions are retryable, back-off, jitter, max attempts, circuit breaker)?
- **Simple Meaning (Dev-Friendly Question):** When an API or DB call fails, which exceptions should you retry, and how do you avoid taking down the downstream server?
- **Interview Answer:**
  - **1. Only Retry Transient Errors:** Retry timeouts (`SocketTimeoutException`), `503 Service Unavailable`, `429 Too Many Requests`, or optimistic lock conflicts. **Never retry** `400 Bad Request`, `401 Unauthorized`, or `NullPointerException`.
  - **2. Require Idempotency:** Only retry writes if you send an `Idempotency-Key` header so the customer isn't charged twice.
  - **3. Exponential Backoff + Jitter:** Wait `100ms -> 200ms -> 400ms` plus random jitter (`±30%`) so 1,000 pods don't retry at the exact same millisecond (thundering herd).
  - **4. Cap Retries + Circuit Breaker:** Limit to 3 attempts max and wrap in a Resilience4j Circuit Breaker to fail fast if the downstream service is dead.

#### 18. What are helpful `NullPointerException` messages, and how do `Objects.requireNonNull`, `Optional`, and null-safety annotations fit a null-handling strategy?
- **Simple Meaning (Dev-Friendly Question):** How did Java 14+ improve `NullPointerException`, and how do you prevent NPEs across a codebase?
- **Interview Answer:**
  - **Helpful NPEs (Java 14+):** If `order.getCustomer().getAddress().getCity()` throws an NPE, Java now tells you the exact variable that was null: `Cannot invoke "Address.getCity()" because the return value of "Customer.getAddress()" is null`.
  - **Complete Null-Safety Strategy:**
    1. **Fail Fast on Inputs:** Use `Objects.requireNonNull(param, "param must not be null")` in constructors/records.
    2. **Return Values:** Return `Optional<T>` for single nullable lookups, and **empty collections (`List.of()`)** instead of `null` for lists.
    3. **Compile-Time Checks:** Use `@NonNull` / `@Nullable` annotations (JSpecify + NullAway/ErrorProne) to catch nulls at compile time.

#### 19. What does your REST API error design look like (global handler, error contract, correlation IDs, mapping domain to HTTP status)?
- **Simple Meaning (Dev-Friendly Question):** How do you standardize error JSON responses across all your Spring Boot REST APIs?
- **Interview Answer:**
  - Use a central **`@RestControllerAdvice`** returning **RFC 9457 / RFC 7807 `ProblemDetail`** (built into Spring Boot 3) with:
    - `type`, `title`, `status` (e.g., `400`, `404`, `409`, `422`, `500`), `detail` (safe user-facing message), `instance` (request URI), plus a **`traceId` / `correlationId`** from OpenTelemetry/MDC.
  - Never expose raw Java stack traces, SQL table names, or internal class names in `500` responses—log the stack trace with `traceId` on the server and return a generic `"Internal server error (traceId: abc-123)"` to the client.

#### 20. How do you test exception paths (JUnit `assertThrows`, testing cause and message, fault injection, chaos testing)?
- **Simple Meaning (Dev-Friendly Question):** How do you write good unit and integration tests for error scenarios?
- **Interview Answer:**
  - **Unit Tests:** Use JUnit 5 `var ex = assertThrows(PaymentFailedException.class, () -> service.pay(req));` or AssertJ `assertThatThrownBy(...)` and assert not just the class, but also the **error code** (`ex.code() == INSUFFICIENT_FUNDS`) and **root cause** (`ex.getCause()`).
  - **Integration Tests:** Use **WireMock** (simulating `503` or socket timeouts) and **Toxiproxy / Testcontainers** to verify that retries, timeouts, and `@Transactional` rollbacks work under real failures.

#### 21. What is the behaviour of an exception thrown inside a `synchronized` block or while holding a `Lock`? How do you make sure locks are released?
- **Simple Meaning (Dev-Friendly Question):** If an exception is thrown inside a `synchronized` block vs a `ReentrantLock`, does the lock get released automatically?
- **Interview Answer:**
  - **`synchronized` Block:** The JVM **automatically releases the monitor lock** when an exception exits the block (the compiler generates an implicit `monitorexit` exception handler in bytecode).
  - **`ReentrantLock`:** Does **NOT** release automatically! You **must** acquire the lock before `try` and release it in `finally`:
    ```java
    lock.lock();
    try { doWork(); } finally { lock.unlock(); }
    ```

#### 22. How do you handle `InterruptedException` correctly (restore the interrupt flag, do not swallow, propagate or exit cleanly)?
- **Simple Meaning (Dev-Friendly Question):** Why do static analyzers (SonarQube) yell at you when you catch `InterruptedException` and only log it?
- **Interview Answer:**
  - When `Thread.sleep()` or `queue.take()` throws `InterruptedException`, the JVM **clears the thread's interrupted status flag (`false`)**!
  - If you just catch and log it, higher-level code (and the thread pool) has no idea someone asked the thread to stop, preventing graceful shutdown.
  - **Rule:** Either declare `throws InterruptedException`, OR immediately restore the flag in the catch block before exiting:
    ```java
    catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // Restore interrupt flag!
        throw new CancellationException("Work interrupted");
    }
    ```

---

## 4. String

#### 1. Why is `String` immutable? List the benefits (security, caching of hash, string pool, thread safety) and the costs.
- **Simple Meaning (Dev-Friendly Question):** Why did Java's creators make `String` unchangeable, and what is the downside?
- **Interview Answer:**
  - **4 Big Benefits:**
    1. **String Pool (Memory Saving):** Thousands of `"ACTIVE"` references can safely point to the exact same `String` object in memory without one caller corrupting another.
    2. **Security:** File paths, DB URLs, and hostnames cannot be mutated by untrusted code after validation passes.
    3. **Thread Safety:** Strings can be shared freely across threads without locks.
    4. **Cached `hashCode()`:** `String` calculates its hash code once and caches it, making it the fastest `HashMap` key.
  - **The Cost:** Modifying a string in a loop (`s += "x"`) allocates a brand-new `String` object every time, creating garbage if you don't use `StringBuilder`.

#### 2. Explain the string pool. Where does it live (heap since Java 7), what does `intern()` do, and when is it harmful?
- **Simple Meaning (Dev-Friendly Question):** How does the JVM String Pool work, where is it stored, and why can calling `String.intern()` on user input crash your server?
- **Interview Answer:**
  - **Where It Lives:** In the **main Heap** (moved out of PermGen in Java 7 so pooled strings can be garbage collected if unused).
  - **What `intern()` Does:** Checks the JVM's internal hash table of pooled strings; if an equal string exists, it returns the pooled reference; otherwise, it adds the string to the pool.
  - **When `intern()` Is Harmful:** The String Pool is a fixed-bucket native JVM hash table. Calling `.intern()` on millions of unique user strings (like UUIDs or JSON bodies) causes massive hash collisions and global JVM lock contention, slowing down CPU and spiking GC pauses. If you need deduplication, use a bounded `ConcurrentHashMap` or Caffeine cache instead.

#### 3. `new String("a")` vs literal `"a"`: how many objects are created, and what does `==` return in each case?
- **Simple Meaning (Dev-Friendly Question):** What is the difference in memory between `String s1 = "a"` and `String s2 = new String("a")`?
- **Interview Answer:**
  - **`String s1 = "a"`:** Uses **1 object** in the String Pool. If `"a"` is already in the pool, **0 new objects** are created!
  - **`String s2 = new String("a")`:** Creates **1 or 2 objects**—first ensures `"a"` is in the String Pool (if not already loaded), and then **always allocates a brand-new `String` object on the regular heap** (though both share the same internal `byte[]` backing array).
  - **`s1 == s2`** returns **`false`** (different memory addresses), while **`s1.equals(s2)`** returns **`true`**.

#### 4. `String` vs `StringBuilder` vs `StringBuffer`: differences, thread safety, and when each is right. Why is `+` in a loop a problem and how does `invokedynamic`-based concatenation (Java 9+) change it?
- **Simple Meaning (Dev-Friendly Question):** When should you use `+`, `StringBuilder`, or `StringBuffer`?
- **Interview Answer:**
  - **`String` (`+` operator):** Since Java 9 (JEP 280), single-statement concatenation like `"User " + id + " active"` compiles to **`invokedynamic` (`StringConcatFactory`)**, which pre-calculates the exact byte length and allocates a single array—even faster than `StringBuilder`!
  - **`StringBuilder`:** Mutable, non-synchronized buffer. **Always use `StringBuilder` when appending inside a `for`/`while` loop**, because `+` inside a loop still allocates a new object on every iteration ($O(N^2)$ copies).
  - **`StringBuffer`:** Legacy Java 1.0 version with `synchronized` on every method. Almost never needed today because strings are built inside a single thread.

#### 5. What are compact strings (Java 9), and how does the Latin-1 vs UTF-16 byte-array representation affect memory?
- **Simple Meaning (Dev-Friendly Question):** How did Java 9 cut `String` memory usage in half?
- **Interview Answer:**
  - In Java 8, `String` used a `char[]` where every character took **2 bytes (UTF-16)**, even though 95%+ of strings in backend apps (JSON keys, ASCII, logs, SQL) only use 1-byte Latin-1 characters.
  - Starting in **Java 9 (Compact Strings)**, `String` uses a **`byte[] value`** plus a 1-byte **`coder` flag** (`LATIN1 = 0` or `UTF16 = 1`). Pure ASCII/Latin-1 strings now use **1 byte per character**, cutting string heap usage by ~40%–50% automatically.

#### 6. How does `String.hashCode()` work and why is it cached? How could poor hashing be abused (hash-collision DoS) and how does `HashMap` mitigate it?
- **Simple Meaning (Dev-Friendly Question):** What is the math formula for `String.hashCode()`, how can hackers abuse it to freeze a server, and how does Java 8+ `HashMap` stop that attack?
- **Interview Answer:**
  - **Formula:** `s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]` (using prime `31` because `31 * i == (i << 5) - i`, which the CPU executes as a fast bit-shift). Because `String` is immutable, the result is cached in a `private int hash` field.
  - **Hash-Collision DoS Attack:** Because `"Aa".hashCode() == "BB".hashCode()` (`2112`), an attacker can send a JSON payload with 50,000 keys that all have the exact same hash code, turning a `HashMap` bucket into a 50,000-element linked list ($O(N^2)$ CPU freeze).
  - **How `HashMap` Mitigates It:** When a bucket chain hits **8 nodes** (and table size $\ge 64$), `HashMap` converts the bucket into a **Red-Black Tree** using `String implements Comparable` as a tie-breaker, keeping lookups at **$O(\log N)$** instead of $O(N)$!

#### 7. Why use `char[]` for passwords instead of `String`? Is it still meaningful today?
- **Simple Meaning (Dev-Friendly Question):** Why do security libraries accept `char[]` instead of `String` for passwords, and does it still matter in cloud microservices?
- **Interview Answer:**
  - **Why `char[]` Exists:** A `String` is immutable—it stays in heap memory until GC eventually overwrites it, so if someone takes a heap dump or core dump, the plaintext password is visible. With `char[]`, you can immediately overwrite it with zeros (`Arrays.fill(pwd, '\0')`) right after hashing.
  - **Does It Still Matter Today?** In practice, if a password arrived over HTTP JSON via Tomcat/Jackson, it was *already* buffered as a `byte[]`/`String` in Netty/Tomcat before reaching your controller. So while zeroing `char[]` is still good practice in crypto libraries, protecting heap dumps via RBAC is equally critical.

#### 8. Unicode: `char` vs code point, surrogate pairs, `length()` vs `codePointCount()`, normalisation (NFC/NFD), locale-sensitive operations (`toUpperCase(Locale)`, the Turkish "i" problem).
- **Simple Meaning (Dev-Friendly Question):** Why does `"😀".length()` return `2` instead of `1`, and why should you always pass `Locale.ROOT` to `toUpperCase()`?
- **Interview Answer:**
  - **Emoji / Surrogate Pairs:** A Java `char` is 16 bits, but Unicode has over 140,000 characters (including emojis). Characters above `U+FFFF` require a **surrogate pair** (two `char`s). So `"😀".length()` is `2`, whereas `"😀".codePointCount(0, s.length())` is `1`.
  - **Normalization (NFC vs NFD):** `"é"` can be represented as 1 single code point OR `e` + combining accent `´`. Use `Normalizer.normalize(s, Form.NFC)` before comparing user text.
  - **The Turkish "i" Bug:** Calling `"title".toUpperCase()` without a locale uses the server OS locale. In Turkish (`tr_TR`), uppercase `'i'` is dotted `'İ'` (`\u0130`), which breaks enum lookups and HTTP headers! Always use **`s.toUpperCase(Locale.ROOT)`** for machine strings.

#### 9. `trim()` vs `strip()`, `isBlank()`, `lines()`, `repeat()`, `formatted()`, `transform()`, `indent()`: what did Java 11+ add and what is the whitespace difference?
- **Simple Meaning (Dev-Friendly Question):** What is the difference between `trim()` and `strip()`, and what useful `String` methods were added in Java 11–15?
- **Interview Answer:**
  - **`trim()` vs `strip()`:** Old `trim()` only removes ASCII characters $\le$ `U+0020` (misses Unicode non-breaking spaces `\u2000` copy-pasted from web pages). **`strip()` (Java 11)** is Unicode-aware (`Character.isWhitespace`) and should always be preferred.
  - **Other Modern Methods:**
    - `isBlank()`: Returns `true` if empty or only whitespace.
    - `lines()`: Lazily streams lines split by `\n` or `\r\n`.
    - `"ab".repeat(3)`: Returns `"ababab"`.
    - `"Hi %s".formatted(name)`: Cleaner instance version of `String.format`.

#### 10. Text blocks: how is indentation handled, what are escape sequences like `\` at line end and `\s`, and where do they help (SQL, JSON)?
- **Simple Meaning (Dev-Friendly Question):** How does Java decide how many leading spaces to strip from a `"""` text block?
- **Interview Answer:**
  - The compiler looks at the **leftmost non-blank line (including the closing `"""` delimiter)** and strips that common "incidental" leading whitespace from every line. Moving the closing `"""` further left adds indentation to the string!
  - **Special Escapes:** `\` at the end of a line joins two lines without a `\n` newline; `\s` preserves trailing spaces that text blocks normally strip.

#### 11. `String.format` vs `MessageFormat` vs `StringBuilder` vs `String.join` vs `StringJoiner`: performance and use cases.
- **Simple Meaning (Dev-Friendly Question):** Which string building tool is fastest, and when should you use each?
- **Interview Answer:**
  - **`+` or `StringBuilder`:** Fastest; use `+` for single expressions and `StringBuilder` inside loops.
  - **`String.join(", ", list)` / `Collectors.joining(", ")`:** Best and cleanest for delimiter-separated lists.
  - **`String.format` / `.formatted()`:** Parses the format pattern via regex every time—convenient for error messages, but **avoid in ultra-hot loops**.
  - **`MessageFormat`:** Best for localized/i18n user messages (`"Hello {0}, you have {1,number,integer} messages"`).

#### 12. Regular expressions: what is catastrophic backtracking, how do you avoid ReDoS, and when do you precompile `Pattern`?
- **Simple Meaning (Dev-Friendly Question):** How can a single bad regex freeze your Java service at 100% CPU, and how do you prevent it?
- **Interview Answer:**
  - **Catastrophic Backtracking (ReDoS):** Nested quantifiers like `(a+)+b` or `([a-zA-Z0-9]+)*` force Java's NFA regex engine to try $2^N$ combinations when a string fails to match at the last character, freezing the thread for minutes on a 50-character input.
  - **How to Prevent It:**
    1. Validate max string length *before* running a regex.
    2. Eliminate nested quantifiers, or use possessive quantifiers (`++`) / Google's linear-time **`RE2/J`** library.
    3. **Always precompile `private static final Pattern`** instead of calling `str.matches(regex)` in a method (which recompiles the regex on every call!).

#### 13. `split` pitfalls (regex, trailing empty strings, limit parameter) and `substring` behaviour (memory leak history before Java 7u6).
- **Simple Meaning (Dev-Friendly Question):** What are the two classic traps with `String.split()`, and how did `substring()` change in Java 7?
- **Interview Answer:**
  - **`split()` Traps:**
    1. **It takes a Regex, not a literal!** `"a.b.c".split(".")` returns an **empty array** because `.` means "any character" in regex! Use `split("\\.")` or `Pattern.quote(".")`.
    2. **Drops Trailing Empty Strings:** `"a,b,,".split(",")` returns length `2` (`["a", "b"]`), silently dropping trailing empty columns in CSVs! Pass `-1` as the limit: **`"a,b,,".split(",", -1)`** to keep trailing empty strings.
  - **`substring()` History:** Before Java 7u6, `substring` shared the original huge `char[]` with an offset, causing memory leaks when extracting a 5-char ID from a 10MB string. Today, `substring()` copies only the needed bytes into a new array ($O(K)$).

#### 14. How do you compare strings safely (`equals`, `equalsIgnoreCase`, `compareTo`, `Objects.equals`, constant-time comparison for secrets)?
- **Simple Meaning (Dev-Friendly Question):** How do you avoid NPEs when comparing strings, and why must you NEVER use `String.equals()` to compare API keys or HMAC signatures?
- **Interview Answer:**
  - **Null-Safe Equality:** Put the constant first (`"ACTIVE".equals(status)`) or use `Objects.equals(a, b)`.
  - **Why Never Use `.equals()` for Secrets (Timing Attacks):** `String.equals()` returns `false` on the **very first mismatched byte**. An attacker can measure nanosecond response time differences to guess an API key or webhook HMAC byte-by-byte!
  - **Fix for Secrets:** Always use **`MessageDigest.isEqual(a.getBytes(UTF_8), b.getBytes(UTF_8))`**, which compares every byte in **constant time**.

#### 15. String switch: how is it compiled (hash then equals)? What are null and case-sensitivity gotchas?
- **Simple Meaning (Dev-Friendly Question):** How does the Java compiler turn a `switch (str)` into fast bytecode, and what happens if `str` is `null`?
- **Interview Answer:**
  - **How It Compiles:** The compiler splits a `switch (str)` into two compiled `switch` steps: first it switches on `str.hashCode()`, and then inside the matching bucket it calls `str.equals("...")` to protect against hash collisions.
  - **Null Gotcha:** In classic Java, `switch (str)` throws a **`NullPointerException`** immediately if `str` is `null`. In **Java 21+**, you can explicitly handle null inside the switch: `case null -> handleNull();`!

#### 16. What is G1 string deduplication, and when does it help?
- **Simple Meaning (Dev-Friendly Question):** What does `-XX:+UseStringDeduplication` do, and how is it different from `String.intern()`?
- **Interview Answer:**
  - Unlike `String.intern()` (which deduplicates the `String` wrapper object itself on the application thread), **G1/ZGC String Deduplication** runs asynchronously in the background during GC.
  - When it sees two distinct `String` objects on the heap with identical characters, it makes both `String` objects point to the **same underlying `byte[]` array**, saving ~10% heap memory in apps with lots of duplicate JSON/DB strings without blocking application threads.

#### 17. How do you handle very large strings and text processing (streaming, `BufferedReader`, `CharSequence`, memory-mapped files)?
- **Simple Meaning (Dev-Friendly Question):** How do you process a 5GB CSV or log file in Java without blowing up the heap with `OutOfMemoryError`?
- **Interview Answer:**
  - **Never** call `Files.readString(path)` or `Files.readAllLines(path)` on large files—that loads the entire 5GB into heap memory at once!
  - **Stream Line-by-Line:** Use `try (Stream<String> lines = Files.lines(path))` or `BufferedReader.readLine()`, or stream JSON tokens using Jackson's streaming `JsonParser` / `MappingIterator` so only one record is in memory at a time.

#### 18. How do you convert between `String`, `byte[]`, and charsets safely? What changed with UTF-8 as the default charset in Java 18?
- **Simple Meaning (Dev-Friendly Question):** Why was calling `str.getBytes()` without arguments dangerous before Java 18?
- **Interview Answer:**
  - Before Java 18, `str.getBytes()` and `new String(bytes)` used the **OS default charset** (e.g., `Windows-1252` on Windows laptops vs `UTF-8` on Linux pods), corrupting emojis and accented characters into `?`.
  - **Java 18 (JEP 400)** made **`UTF-8` the default charset everywhere** across all OSes. Even so, passing `StandardCharsets.UTF_8` explicitly is still best practice for clarity.

#### 19. Why were String Templates previewed and then removed, and what should you use today (`formatted`, `MessageFormat`, `StringBuilder`, libraries)?
- **Simple Meaning (Dev-Friendly Question):** Since String Templates were dropped in Java 23, what is the cleanest way to format strings and SQL queries safely today?
- **Interview Answer:**
  - For readable multi-variable strings: use **`"User %s (id=%d)".formatted(name, id)`** or Text Blocks with `.formatted(...)`.
  - For SQL queries or HTML: **never use string concatenation/formatting** (SQL injection / XSS risk)—use parameterized **`PreparedStatement` / Spring `JdbcClient`** or template engines.

#### 20. What is `CharSequence`, and why do APIs accept it instead of `String`?
- **Simple Meaning (Dev-Friendly Question):** Why do methods like `Pattern.matcher(CharSequence)` or `String.contains(CharSequence)` accept `CharSequence` instead of `String`?
- **Interview Answer:**
  - `CharSequence` is a read-only interface implemented by **`String`, `StringBuilder`, `StringBuffer`, and `CharBuffer`**.
  - Accepting `CharSequence` lets callers pass a `StringBuilder` or `CharBuffer` directly into regex matchers or validators **without calling `.toString()` and allocating a temporary copy** on the heap.

---

## 5. Collections Framework

#### 1. Draw the collection hierarchy (`Iterable`, `Collection`, `List`, `Set`, `Queue`, `Deque`, `Map`, `SequencedCollection`). Where does `Map` sit and why?
- **Simple Meaning (Dev-Friendly Question):** How are the main Java Collection interfaces organized, and why doesn't `Map` extend `Collection`?
- **Interview Answer:**
  - **Root 1 (`Iterable<E>` -> `Collection<E>`):**
    - `SequencedCollection<E>` (Java 21) -> `List<E>` (`ArrayList`, `LinkedList`), `Deque<E>` (`ArrayDeque`), `SequencedSet<E>` (`LinkedHashSet`, `SortedSet` / `TreeSet`).
    - `Set<E>` (`HashSet`, `EnumSet`) and `Queue<E>` (`PriorityQueue`).
  - **Root 2 (`Map<K,V>` -> `SequencedMap<K,V>` -> `SortedMap<K,V>`):**
    - `Map` does **NOT** extend `Collection` because a `Collection` holds single elements (`add(E)`), whereas a `Map` holds **key-value pairs (`put(K, V)`)**. However, `map.keySet()`, `map.values()`, and `map.entrySet()` return `Collection` views!

#### 2. Explain `HashMap` internals: hash spreading, table index, buckets, resize and load factor, treeification of long chains (threshold 8 with minimum table size 64), untreeify, and why capacity is a power of two.
- **Simple Meaning (Dev-Friendly Question):** Walk through step-by-step what happens inside `HashMap` when you call `map.put(key, value)`.
- **Interview Answer:**
  1. **Hash Spreading:** Calls `key.hashCode()` and XORs the top 16 bits with the bottom 16 bits (`(h = key.hashCode()) ^ (h >>> 16)`) so high bits participate even when the table is small.
  2. **Why Power-of-Two Capacity (Default 16):** Instead of slow modulo (`hash % n`), a power-of-two size lets `HashMap` use a single CPU bitmask **`(n - 1) & hash`** to find the bucket index.
  3. **Bucket Insertion:** If the bucket is empty, inserts a new `Node`. If occupied, compares keys via `==` then `.equals()`—overwriting if found, or appending to the bucket chain.
  4. **Treeification (`>= 8` & table `>= 64`):** If a bucket chain reaches **8 nodes** and the array size is at least **64**, the linked list converts into a **Red-Black Tree (`TreeNode`)** ($O(\log N)$ lookup). (If table size < 64, it simply resizes the array instead). When shrinking below **6 nodes**, it **untreeifies** back to a linked list.
  5. **Resize & Load Factor (`0.75`):** When `size > capacity * 0.75` (e.g., `16 * 0.75 = 12`), the array doubles (`16 -> 32`) and re-distributes entries.

#### 3. What breaks if `hashCode` or `equals` is wrong or mutable keys are used? How do you detect and fix key problems?
- **Simple Meaning (Dev-Friendly Question):** What goes wrong if you mutate an object *after* putting it into a `HashMap` as a key?
- **Interview Answer:**
  - When you mutate a field used in `hashCode()`, the key's hash code changes, **but the key is still sitting in its old bucket**!
  - Now `map.get(key)` looks in the *new* bucket and returns `null`, while searching with the old value looks in the old bucket, runs `.equals()` against the mutated key, and also returns `null`! The entry becomes an unremovable **memory leak**.
  - **Fix:** Only use **immutable types** (`String`, `Integer`, `UUID`, `record`) as Map keys.

#### 4. How does `HashMap` behave under concurrent access (lost updates, infinite loops in old versions, `ConcurrentModificationException`)? Why is it not thread-safe?
- **Simple Meaning (Dev-Friendly Question):** What happens in production if two threads write to a plain `HashMap` at the same time?
- **Interview Answer:**
  - **1. Silent Data Loss:** Two threads inserting into the same bucket at the same millisecond overwrite each other's `next` node pointer, silently dropping entries.
  - **2. Corrupted Size / `ConcurrentModificationException`:** One thread resizing while another iterates crashes with `ConcurrentModificationException` or corrupts internal tree pointers.
  - **3. Infinite Loop (Java 7):** In Java 7, concurrent resizing reversed linked-list pointers and created a circular loop, pinning CPU at 100% forever on `map.get()`. (Java 8 fixed tail-insertion order, but tree-node corruption and data loss still happen).

#### 5. `ConcurrentHashMap` internals (CAS and per-bin locking in Java 8+, `computeIfAbsent`, `merge`, size counters, no null keys or values). What are the pitfalls of long-running `computeIfAbsent` functions?
- **Simple Meaning (Dev-Friendly Question):** How is `ConcurrentHashMap` so fast without locking the whole map, why does it forbid `null`, and what is the trap with `computeIfAbsent`?
- **Interview Answer:**
  - **How It Works (Java 8+):**
    - Reads (`get`) are **100% lock-free** (using `volatile` reads).
    - Inserting into an empty bucket uses lock-free **CAS (Compare-And-Swap)**.
    - Updating a non-empty bucket locks **only the first node (head) of that single bucket** (`synchronized (f)`), so 64 threads can write to 64 different buckets in parallel!
    - Size is tracked using striped **`CounterCell[]`** (just like `LongAdder`) to avoid contention on a single counter.
  - **Why No `null` Keys/Values:** In a concurrent map, if `map.get(k)` returns `null`, you can't tell if the key wasn't in the map or if its value was `null` (checking `containsKey` afterward is a race condition because another thread could delete it in between!).
  - **`computeIfAbsent` Pitfall:** `computeIfAbsent` holds a `synchronized` lock on that bucket while your lambda executes! Never make slow HTTP calls inside `computeIfAbsent`, and **never recursively call `computeIfAbsent` on the same map** inside the lambda (instant `IllegalStateException: Recursive update` or deadlock).

#### 6. `HashMap` vs `Hashtable` vs `Collections.synchronizedMap` vs `ConcurrentHashMap`: differences and use cases.
- **Simple Meaning (Dev-Friendly Question):** Compare the four Map options for single-threaded vs multi-threaded code.
- **Interview Answer:**
  - **`HashMap`:** Not thread-safe, fastest for single-threaded or method-local use; allows 1 `null` key and multiple `null` values.
  - **`Hashtable` & `Collections.synchronizedMap`:** Lock the **entire map** with a single `synchronized` lock on every `get()` and `put()`. Terrible scalability under concurrency; legacy only.
  - **`ConcurrentHashMap`:** Fine-grained per-bucket locking + lock-free reads + atomic operations (`compute`, `merge`). Always use this for shared multi-threaded maps.

#### 7. `LinkedHashMap`: insertion vs access order, building an LRU cache with `removeEldestEntry`.
- **Simple Meaning (Dev-Friendly Question):** How does `LinkedHashMap` remember order, and how can you turn it into a 5-line LRU cache?
- **Interview Answer:**
  - `LinkedHashMap` is a `HashMap` where every node also has `before` and `after` pointers forming a **doubly-linked list** across all entries.
  - By default it maintains **insertion order**, or if you pass `accessOrder = true` to the constructor (`new LinkedHashMap<>(16, 0.75f, true)`), every `get()` or `put()` moves that entry to the end of the list (**LRU order**).
  - **5-Line LRU Cache:** Override `protected boolean removeEldestEntry(Map.Entry<K,V> eldest) { return size() > MAX_ENTRIES; }`.

#### 8. `TreeMap` / `TreeSet`: red-black tree, ordering via `Comparable` / `Comparator`, `compareTo` consistent with `equals`, `NavigableMap` operations.
- **Simple Meaning (Dev-Friendly Question):** How does `TreeMap` work, what Happens if `compareTo()` disagrees with `equals()`, and what cool methods does `NavigableMap` have?
- **Interview Answer:**
  - **How It Works:** Self-balancing **Red-Black Tree** where `get`, `put`, and `remove` take **$O(\log N)$** time and keys are always sorted.
  - **The `compareTo` Trap:** `TreeMap` and `TreeSet` use **`compareTo() == 0` (NOT `.equals()`)** to check if two keys are duplicates! That's why `new TreeSet<>()` containing `new BigDecimal("1.0")` and `new BigDecimal("1.00")` keeps only **1 element** (`compareTo` is `0`), whereas `HashSet` keeps **both** (`equals` is `false`).
  - **Best `NavigableMap` Methods:** `floorEntry(k)`, `ceilingEntry(k)`, `subMap(from, to)`—great for time-range lookups and consistent hashing rings!

#### 9. `ArrayList` vs `LinkedList` vs `ArrayDeque`: complexity, memory, and cache locality. Why is `LinkedList` rarely the right answer?
- **Simple Meaning (Dev-Friendly Question):** Why do textbooks teach `LinkedList`, yet senior Java engineers almost never use it in real life?
- **Interview Answer:**
  - **`ArrayList`:** Backed by a contiguous `Object[]` array. $O(1)$ random access, tiny memory overhead, and **CPU cache-friendly** (CPU pre-fetches contiguous memory lines).
  - **`LinkedList`:** Every single item allocates a separate 24-byte `Node` object (`item`, `next`, `prev`) scattered randomly across the heap (**terrible CPU cache locality** and 4x–6x memory overhead). Even inserting in the middle requires $O(N)$ pointer hopping to reach index $i$.
  - **What to Use Instead of `LinkedList`:** Use **`ArrayList`** for lists, and **`ArrayDeque`** (circular array) for FIFO queues and stacks—`ArrayDeque` beats `LinkedList` on both speed and memory!

#### 10. `ArrayList` growth strategy, `ensureCapacity`, `trimToSize`, and the cost of `remove(int)` and `add(int, E)`.
- **Simple Meaning (Dev-Friendly Question):** How does `ArrayList` grow when full, and why is removing from the front of an `ArrayList` slow?
- **Interview Answer:**
  - **Growth Strategy:** Starts at default capacity **10** (allocated lazily on the first `add`). When full, it grows by **1.5x** (`oldCapacity + (oldCapacity >> 1)`) and copies elements via `Arrays.copyOf`.
  - **Pre-sizing:** If you know you're loading 5,000 rows, use `new ArrayList<>(5000)` to avoid 10 intermediate array copies.
  - **Cost of `remove(0)` / `add(0, x)`:** Shifts all remaining $N - 1$ elements left/right via `System.arraycopy`—so doing `list.remove(0)` in a loop is **$O(N^2)$**! Use `ArrayDeque.pollFirst()` instead.

#### 11. `PriorityQueue`: binary heap, complexity, iteration order not sorted, using a comparator for top-K problems.
- **Simple Meaning (Dev-Friendly Question):** How does `PriorityQueue` work, why does a `for (var x : pq)` loop print items out of order, and how do you find the Top K largest items?
- **Interview Answer:**
  - **Internals:** Backed by a **binary min-heap stored in an `Object[]` array**. `offer()` and `poll()` are **$O(\log N)$**; `peek()` is **$O(1)$**.
  - **Iteration Gotcha:** Only index `0` is guaranteed to be the smallest element! Iterating with `for (var x : pq)` just walks the internal heap array, which is **not fully sorted**—you must loop `while (!pq.isEmpty()) pq.poll()` to get sorted order.
  - **Top-K Largest Problem:** Maintain a **min-heap of size $K$**. For each item, if `item > pq.peek()`, `poll()` the smallest and `offer(item)`. Complexity is **$O(N \log K)$** using only $O(K)$ memory!

#### 12. Fail-fast vs fail-safe iterators: how `modCount` works, `ConcurrentModificationException`, `CopyOnWriteArrayList`, and weakly consistent iterators in concurrent collections.
- **Simple Meaning (Dev-Friendly Question):** How does an `ArrayList` know you modified it during a loop (`ConcurrentModificationException`), and how do concurrent collections avoid that crash?
- **Interview Answer:**
  - **Fail-Fast (`ArrayList`, `HashMap`):** Every structural change (`add`, `remove`) increments an internal `int modCount`. When you create an `Iterator`, it saves `expectedModCount = modCount`. On every `next()`, if `modCount != expectedModCount`, it immediately throws `ConcurrentModificationException` (best-effort bug detector, not a thread-safety guarantee).
  - **Weakly Consistent / Fail-Safe (`ConcurrentHashMap`, `CopyOnWriteArrayList`):** Never throw `ConcurrentModificationException`. `CopyOnWriteArrayList` iterates over an immutable snapshot of the array at the moment the iterator was created; `ConcurrentHashMap` reflects updates that happen during iteration on a best-effort basis.

#### 13. `CopyOnWriteArrayList` and `CopyOnWriteArraySet`: when are they appropriate (read-mostly) and when do they hurt?
- **Simple Meaning (Dev-Friendly Question):** When is `CopyOnWriteArrayList` a great choice, and when will it destroy your JVM performance?
- **Interview Answer:**
  - **How It Works:** Reads (`get`, `forEach`) are 100% lock-free on an immutable array. Every write (`add`, `set`, `remove`) acquires a lock and **copies the entire array** into a new one!
  - **When to Use:** **99.9% reads, tiny size**—like a list of 10 event listeners or feature flags updated once an hour.
  - **When It Hurts:** High write rates or large lists (10,000 items)—copying a 10,000-element array on every `add()` causes massive GC pressure and lock contention.

#### 14. Immutable collections: `List.of`, `Set.of`, `Map.of`, `copyOf`, `Collections.unmodifiableList`, `Arrays.asList`. Differences in mutability, null handling, and views.
- **Simple Meaning (Dev-Friendly Question):** Compare `Arrays.asList()`, `Collections.unmodifiableList()`, and `List.of()`—people mix these up all the time!
- **Interview Answer:**

| Feature | `Arrays.asList(...)` | `Collections.unmodifiableList(src)` | `List.of(...)` / `List.copyOf(src)` |
| :--- | :--- | :--- | :--- |
| **`add()` / `remove()`** | Throws `UnsupportedOperationException` | Throws `UnsupportedOperationException` | Throws `UnsupportedOperationException` |
| **`set(i, val)`** | **Allowed!** (Mutates backing array) | Throws `UnsupportedOperationException` | Throws `UnsupportedOperationException` |
| **Allows `null`?** | Yes | Yes (if backing list allows) | **No!** (Throws `NullPointerException`) |
| **Truly Immutable?** | **No** | **No!** (If caller mutates `src`, view changes!) | **Yes!** (100% immutable) |

#### 15. `Collections.unmodifiableList` vs `List.copyOf`: view vs copy and what that means for defensive copying.
- **Simple Meaning (Dev-Friendly Question):** Why is `List.copyOf(list)` safer than `Collections.unmodifiableList(list)` in a constructor?
- **Interview Answer:**
  - `Collections.unmodifiableList(original)` only wraps `original` in a read-only **view**. If the caller keeps a reference to `original` and calls `original.add("hack")`, your "unmodifiable" list changes too!
  - **`List.copyOf(original)` (Java 10+)** makes a **true independent defensive copy** (and smartly returns `original` directly with zero copying if `original` was already created by `List.of()`!).

#### 16. `Comparable` vs `Comparator`, `Comparator.comparing().thenComparing()`, `nullsFirst`, and sorting stability (`List.sort` is stable, `Arrays.sort` on primitives is not required to be).
- **Simple Meaning (Dev-Friendly Question):** What does "stable sort" mean in Java, and which sorting algorithm does Java use for objects vs primitives?
- **Interview Answer:**
  - **Stable Sort:** If two items have equal sort keys (e.g., two orders at the same price), their original relative order is preserved after sorting.
  - **Objects (`List.sort` / `Arrays.sort(Object[])`):** Uses **TimSort** (hybrid of MergeSort + InsertionSort), which is **guaranteed stable** ($O(N \log N)$).
  - **Primitives (`Arrays.sort(int[])`):** Uses **Dual-Pivot QuickSort** because primitive `5` is indistinguishable from another `5` (stability doesn't matter for primitives, and QuickSort uses zero extra heap memory).

#### 17. Sequenced Collections in Java 21: new methods, `reversed()` views, and `LinkedHashMap` / `LinkedHashSet` / `TreeSet` integration.
- **Simple Meaning (Dev-Friendly Question):** How did Java 21 make `LinkedHashMap` and `LinkedHashSet` much easier to work with?
- **Interview Answer:**
  - `LinkedHashSet` now has `getFirst()`, `getLast()`, `addFirst(e)` (which moves an existing element to the front!), and `reversed()`.
  - `LinkedHashMap` implements `SequencedMap`, adding `firstEntry()`, `lastEntry()`, `pollFirstEntry()`, `putFirst(k, v)`, and `reversed()`.

#### 18. `EnumMap`, `IdentityHashMap`, `WeakHashMap`, `ConcurrentSkipListMap`: what are they for?
- **Simple Meaning (Dev-Friendly Question):** Give a real-world use case for each of these 4 specialized Map classes.
- **Interview Answer:**
  - **`EnumMap`:** Keys are a single `enum` type; backed by a compact array. Use whenever mapping from an `enum` (fastest Map in Java).
  - **`IdentityHashMap`:** Compares keys using **`==` (memory address)** instead of `.equals()`. Used by serializers (Jackson) and object-graph traversers to detect circular references.
  - **`WeakHashMap`:** Keys are wrapped in `WeakReference`—if nothing else in the app references a key object, GC automatically removes the map entry. Used for classloader/proxy metadata associations.
  - **`ConcurrentSkipListMap`:** Thread-safe sorted `NavigableMap` ($O(\log N)$ lock-free skip list). Use when you need a **thread-safe `TreeMap`**.

#### 19. How do you pick initial capacity and load factor for a `HashMap` of a known size (`HashMap.newHashMap(n)` in Java 19+)?
- **Simple Meaning (Dev-Friendly Question):** If you want to store exactly 100 items in a `HashMap` without any resizing, why is `new HashMap<>(100)` wrong?
- **Interview Answer:**
  - Because of the `0.75` load factor, `new HashMap<>(100)` rounds up to capacity `128`, which triggers a resize at `128 * 0.75 = 96` items—so inserting the 97th item **still resizes**!
  - You had to divide by `0.75`: `new HashMap<>((int) (100 / 0.75) + 1)`.
  - **Java 19+ Fix:** Call **`HashMap.newHashMap(100)`** (and `HashSet.newHashSet(100)`), which does the `/ 0.75` math for you automatically!

#### 20. `subList`, `keySet`, `values`, `entrySet` are views. What are the pitfalls (structural modification, memory retention)?
- **Simple Meaning (Dev-Friendly Question):** What are the two hidden traps with `list.subList(0, 5)`?
- **Interview Answer:**
  1. **`subList` is a Live View, Not a Copy:** Calling `subList.clear()` actually deletes those 5 items from the original list! And if you modify the original list directly afterward, touching the `subList` throws `ConcurrentModificationException`.
  2. **Memory Leak Trap:** `subList` holds a strong reference to the original backing `ArrayList`. If you take a 2-item `subList` from a 1-million-item list and cache that `subList`, the entire 1-million-item list stays trapped in memory! **Fix:** Use `List.copyOf(bigList.subList(0, 2))` if keeping it around.

#### 21. `BlockingQueue` implementations (`ArrayBlockingQueue`, `LinkedBlockingQueue`, `PriorityBlockingQueue`, `SynchronousQueue`, `DelayQueue`, `LinkedTransferQueue`): when do you use each?
- **Simple Meaning (Dev-Friendly Question):** How do you choose the right `BlockingQueue` for producer-consumer or thread pools?
- **Interview Answer:**
  - **`ArrayBlockingQueue`:** Bounded circular array with 1 lock. Best default for **strict memory bounds and back-pressure**.
  - **`LinkedBlockingQueue`:** Linked nodes with 2 separate locks (`putLock` and `takeLock`) for higher throughput. **Warning:** Always pass a capacity (`new LinkedBlockingQueue<>(1000)`), because the default constructor is unbounded (`Integer.MAX_VALUE`), risking OOM!
  - **`SynchronousQueue`:** Capacity is **0**—every `put()` waits for a thread calling `take()` for a direct handoff (used by `Executors.newCachedThreadPool()`).
  - **`PriorityBlockingQueue`:** Thread-safe priority heap (unbounded).
  - **`DelayQueue`:** Elements can only be taken after their TTL delay expires (great for local scheduled task timers).

#### 22. How would you implement an LRU cache, an LFU cache, a time-bounded cache, and a thread-safe bounded buffer? What would you use in production instead (Caffeine)?
- **Simple Meaning (Dev-Friendly Question):** How do you write an LRU cache on a whiteboard in an interview vs what you actually use in production?
- **Interview Answer:**
  - **Whiteboard Interview (LRU Cache):** Combine a **`HashMap<K, Node>`** (for $O(1)$ lookup) with a **Doubly-Linked List** (to move accessed nodes to the head and evict the tail in $O(1)$), or extend `LinkedHashMap(cap, 0.75f, true)` with `removeEldestEntry`.
  - **Production Reality:** **Never roll your own cache in production.** Always use **Caffeine** (`Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(Duration.ofMinutes(5)).build()`), which uses the **W-TinyLFU** eviction algorithm (near-optimal hit rate), striped ring-buffer concurrency, async refresh, and stampede protection (`get(key, loader)`).

#### 23. How do you detect and avoid memory leaks caused by collections (unbounded caches, static maps, listeners, `ThreadLocal` values)?
- **Simple Meaning (Dev-Friendly Question):** Since Java has a Garbage Collector, how do Collections cause memory leaks, and how do you prevent them?
- **Interview Answer:**
  - GC only collects objects that are **unreachable**. If a `static Map` or Spring Singleton `@Service` holds a `Map`/`List` and keeps adding entries without ever removing them, those objects stay reachable forever.
  - **Prevention:** Never use a raw `ConcurrentHashMap` as a cache for dynamic IDs—always use **Caffeine** with `.maximumSize(...)` and `.expireAfterWrite(...)`. Always unregister listeners and call `ThreadLocal.remove()` in `finally`.

#### 24. How do you choose between `List`, `Set`, `Map`, and `Queue` for a given problem, and what are the complexity trade-offs?
- **Simple Meaning (Dev-Friendly Question):** Give a quick cheat sheet for picking the right Collection and its Big-O speed.
- **Interview Answer:**
  - **Need index access or duplicates in order?** -> `ArrayList` ($O(1)$ get/append, $O(N)$ `contains`).
  - **Need fast uniqueness check (`contains`)?** -> `HashSet` ($O(1)$ add/contains) or `LinkedHashSet` (keeps insertion order).
  - **Need key -> value lookup?** -> `HashMap` ($O(1)$) or `TreeMap` ($O(\log N)$ sorted keys).
  - **Need FIFO / LIFO processing?** -> `ArrayDeque` ($O(1)$ both ends).

#### 25. What is the cost of autoboxing in collections of primitives, and what are the alternatives (primitive arrays, specialised libraries, Valhalla direction)?
- **Simple Meaning (Dev-Friendly Question):** Why does `List<Integer>` use 4x–5x more memory than `int[]`, and how do you fix it if you have millions of numbers?
- **Interview Answer:**
  - In `int[]`, each `int` takes **4 bytes** contiguously. In `ArrayList<Integer>`, each element is a heap `Integer` object (12–16 byte header + 4 byte int + padding = **16–24 bytes**) PLUS a 4-byte reference in the array = **~20–28 bytes per number** (5x–7x overhead) and poor CPU cache locality!
  - **Alternatives Today:** Use primitive arrays (`int[]`, `long[]`), `IntStream` / `LongStream`, or specialized primitive collection libraries (**Eclipse Collections**, **FastUtil**, **Agrona**). In the future, **Project Valhalla** value classes will eliminate this boxing overhead natively.

---

## 6. Generics, Lambdas, Streams, Optional

#### 1. What is type erasure? What are its consequences (no `new T()`, no generic arrays, `instanceof List<String>` not allowed, bridge methods, heap pollution)?
- **Simple Meaning (Dev-Friendly Question):** What does "Type Erasure" mean in Java Generics, and what things can't you do at runtime because of it?
- **Interview Answer:**
  - **What It Means:** Generics exist at **compile time** for type safety, but the compiler erases `<T>` to `Object` (or its upper bound like `Comparable`) in the compiled bytecode so old Java code stays binary-compatible. At runtime, `List<String>` and `List<Integer>` are both just `ArrayList.class`.
  - **Consequences:**
    - You cannot do `new T()` or `new T[10]` (pass `Class<T>` or `IntFunction<T[]>` instead).
    - You cannot check `obj instanceof List<String>` (only `obj instanceof List<?>`).
    - The compiler generates synthetic **bridge methods** to preserve polymorphism when a subclass narrows a generic type.
    - **Heap Pollution:** Mixing raw types or `@SafeVarargs` unchecked casts can put an `Integer` into a `List<String>`, causing a `ClassCastException` later when reading.

#### 2. Explain bounded types and wildcards: `? extends T`, `? super T`, and the PECS rule with a concrete example.
- **Simple Meaning (Dev-Friendly Question):** Nobody remembers when to use `? extends T` vs `? super T`. How do you explain **PECS** simply?
- **Interview Answer:**
  - **PECS = Producer `extends`, Consumer `super`:**
    - **`List<? extends Number>` (Producer—Read Only):** You can **read** `Number`s out of it (whether someone passed a `List<Integer>` or `List<Double>`), but you **cannot add** anything to it (because you don't know if the underlying list is `List<Double>` while you try to add an `Integer`).
    - **`List<? super Integer>` (Consumer—Write Into):** You can **add** `Integer`s into it (whether someone passed `List<Integer>`, `List<Number>`, or `List<Object>`), because an `Integer` is valid in all of them!
  - **Concrete Example:** `void copy(List<? extends T> src, List<? super T> dest)`—reads from `src` (`extends`) and writes into `dest` (`super`).

#### 3. What are the limitations of generics with primitives, and how does that affect API design?
- **Simple Meaning (Dev-Friendly Question):** Why can't you write `List<int>` or `Function<int, boolean>` in Java?
- **Interview Answer:**
  - Because type erasure replaces `<T>` with `Object`, and primitive types (`int`, `long`, `double`) do not extend `Object`.
  - **How It Affects API Design:** Standard libraries must provide primitive specializations (`IntPredicate`, `ToLongFunction<T>`, `IntStream`, `OptionalInt`) to avoid autoboxing millions of primitives on hot paths.

#### 4. What are functional interfaces? Name the core ones (`Function`, `Predicate`, `Supplier`, `Consumer`, `UnaryOperator`, `BiFunction`) and primitive specialisations. Why do the specialisations exist?
- **Simple Meaning (Dev-Friendly Question):** What makes an interface a `@FunctionalInterface`, and what are the 6 built-in ones you use every day?
- **Interview Answer:**
  - A **Functional Interface** has **exactly 1 abstract method** (it can have any number of `default` or `static` methods), allowing it to be implemented by a lambda or method reference.
  - **Core 6 in `java.util.function`:**
    1. `Predicate<T>` (`T -> boolean`): Filter check (`user -> user.isActive()`).
    2. `Function<T, R>` (`T -> R`): Transform (`User::email`).
    3. `Consumer<T>` (`T -> void`): Side effect (`System.out::println`).
    4. `Supplier<T>` (`() -> T`): Lazy factory (`ArrayList::new`).
    5. `UnaryOperator<T>` (`T -> T`): Same-type transform (`String::trim`).
    6. `BiFunction<T, U, R>` (`(T, U) -> R`): Two inputs to one output.
  - **Primitive Specializations (`IntPredicate`, `ToIntFunction`, `LongConsumer`):** Exist to operate directly on raw `int`/`long`/`double` without heap autoboxing overhead.

#### 5. How do method references work, and how do you choose between them and lambdas?
- **Simple Meaning (Dev-Friendly Question):** What are the 4 types of method references (`::`), and when do you use `::` vs `->`?
- **Interview Answer:**
  - **4 Types:**
    1. Static method: `Integer::parseInt` (`s -> Integer.parseInt(s)`)
    2. Instance method on an arbitrary object of a type: `String::toLowerCase` (`s -> s.toLowerCase()`)
    3. Instance method on a specific object: `service::save` (`x -> service.save(x)`)
    4. Constructor reference: `ArrayList::new` (`() -> new ArrayList<>()`)
  - **Rule:** Use a method reference when the lambda does nothing except call a single clearly-named method (`users.stream().map(User::email)`). Use a lambda when passing extra arguments or when the lambda parameter name adds clarity.

#### 6. How do lambdas capture variables, and why must they be effectively final?
- **Simple Meaning (Dev-Friendly Question):** Why does Java forbid modifying a local variable inside a lambda (`int count = 0; list.forEach(x -> count++);`)?
- **Interview Answer:**
  - Local variables live on the **thread's stack**. A lambda might be passed to another thread (`CompletableFuture`) and run long after the original method returned and its stack frame was destroyed!
  - Therefore, the lambda captures a **copy of the local variable's value**.
  - **Why Effectively Final:** If Java allowed the lambda to mutate its copy, developers would think they were mutating the outer variable (or cause data races in parallel streams). Forcing it to be effectively final makes the value-capture safe and thread-safe.

#### 7. Streams: lazy evaluation, intermediate vs terminal operations, short-circuiting, stateful vs stateless operations, and what "a stream can be consumed once" means.
- **Simple Meaning (Dev-Friendly Question):** What does "Streams are lazy" mean, and how does the pipeline actually execute?
- **Interview Answer:**
  - **Lazy Evaluation:** Calling `.filter(...).map(...)` does **zero work**—it just builds a recipe. Nothing runs until you call a **Terminal Operation** (`.toList()`, `.count()`, `.findFirst()`).
  - **Loop Fusion & Short-Circuiting:** Stateless intermediate steps (`filter`, `map`) are fused into a single pass element-by-element. If you end with `.findFirst()` or `.anyMatch()`, the stream stops processing as soon as the first match is found!
  - **Stateful vs Stateless:** `filter`/`map` are stateless (need 1 item at a time). `sorted()` and `distinct()` are **stateful**—`sorted()` must buffer the entire stream before emitting the first item!
  - **Consumed Once:** A `Stream` is like an iterator over data, not a data structure—calling a second terminal operation on the same stream variable throws `IllegalStateException`.

#### 8. `map` vs `flatMap`, `reduce` vs `collect`, `Collectors.groupingBy`, `partitioningBy`, `toMap` (duplicate key behaviour), `teeing`, `mapMulti`.
- **Simple Meaning (Dev-Friendly Question):** Explain the most common Stream transformations and collectors in simple words.
- **Interview Answer:**
  - **`map` vs `flatMap`:** `map` is **1-to-1** (`Order -> Customer`). `flatMap` is **1-to-Many flattened** (`Order -> List<OrderItem>` flattened into a single `Stream<OrderItem>`). (`mapMulti` is a faster, imperative alternative to `flatMap` that avoids creating a new inner `Stream` object per item).
  - **`reduce` vs `collect`:** `reduce` combines items immutably (like summing numbers). `collect` mutates a container (`ArrayList`, `HashMap`) to accumulate results efficiently.
  - **Key Collectors:**
    - `groupingBy(User::role)`: Groups into `Map<Role, List<User>>`.
    - `partitioningBy(User::isActive)`: Splits into a 2-key `Map<Boolean, List<User>>` (`true` and `false`).
    - `toMap(User::email, u -> u, (oldVal, newVal) -> newVal)`: **Always provide the 3rd merge function** if keys might have duplicates, otherwise `toMap` throws `IllegalStateException`!
    - `teeing(collector1, collector2, merger)` (Java 12): Computes two aggregations (e.g., `min` and `max`) in a single stream pass.

#### 9. When do parallel streams help or hurt (ForkJoin common pool, splittability, boxing, ordering, blocking calls, shared state)? How would you decide?
- **Simple Meaning (Dev-Friendly Question):** Why is `.parallelStream()` almost always a bad idea in a Spring Boot web application?
- **Interview Answer:**
  - **Why It Hurts in Web Apps:**
    1. **Shared `ForkJoinPool.commonPool()`:** All `.parallelStream()` calls across the entire JVM share a tiny thread pool (`CPU cores - 1`). A single blocking DB/HTTP call inside `parallelStream()` freezes the whole pool for all requests!
    2. **Thread Overhead:** For small lists (< 10,000 items), splitting tasks and merging results across threads takes *longer* than a single-threaded loop.
  - **When It Actually Helps:** Only when all 3 are true: (1) **Pure CPU-heavy math** (zero I/O), (2) **Large dataset** ($N \times \text{cost per item}$ is large), and (3) **Easily splittable source** (`ArrayList`, `IntStream.range`, arrays—NOT `LinkedList` or `Stream.iterate`).

#### 10. What are common stream anti-patterns (side effects in `peek` / `map`, mutation of external state, using streams for simple loops, huge intermediate objects)?
- **Simple Meaning (Dev-Friendly Question):** What are the worst mistakes developers make when writing Java Streams?
- **Interview Answer:**
  1. **Mutating an external list inside `forEach`/`map`:** (`list.stream().forEach(externalList::add)`)—breaks under parallel streams and violates functional purity; use `.toList()` instead.
  2. **Putting business logic inside `peek()`:** `peek()` is strictly for debugging. If a terminal operation like `.count()` optimizes away the pipeline, **`peek()` won't even execute at all**!
  3. **Forcing complex imperative logic into a unreadable 40-line nested stream:** If you need `break`, `continue`, checked exceptions, or mutating two local variables, a plain `for` loop is much cleaner.

#### 11. What are Stream Gatherers (Java 24), and when would you write a custom one?
- **Simple Meaning (Dev-Friendly Question):** How does `stream.gather(...)` work in Java 24, and when would you use it?
- **Interview Answer:**
  - `Gatherer<T, A, R>` is to **intermediate operations** what `Collector` is to terminal operations.
  - Built-in `Gatherers` give you **`windowFixed(batchSize)`**, **`windowSliding(size)`**, **`scan(...)`** (running totals), and **`fold(...)`**.
  - You write a custom `Gatherer` when you need a stateful intermediate filter—for example, deduplicating consecutive identical events or rate-limiting elements mid-stream.

#### 12. `Optional`: intended use, misuse (as fields, parameters, collections), `orElse` vs `orElseGet`, `map` / `flatMap` / `or` / `stream`, `orElseThrow`.
- **Simple Meaning (Dev-Friendly Question):** Where should you use `Optional`, where should you NEVER use it, and what is the bug with `orElse()` vs `orElseGet()`?
- **Interview Answer:**
  - **Intended Use:** Strictly as a **method return type** when a single result might be absent (`Optional<User> findById(id)`).
  - **Where NEVER to Use `Optional`:**
    1. **Class Fields / JPA Entities:** `Optional` is not `Serializable` and adds an extra heap wrapper object per field.
    2. **Method Parameters:** Forces callers to wrap values (`doWork(Optional.ofNullable(x))`); use method overloading or `@Nullable` instead.
    3. **Wrapping a `List` (`Optional<List<T>>`):** Never return an `Optional` list—just return an empty `List.of()`!
  - **`orElse(...)` vs `orElseGet(...)` Trap:**
    - `opt.orElse(fetchDefaultFromDb())` **ALWAYS executes `fetchDefaultFromDb()`** even when `opt` is present!
    - `opt.orElseGet(() -> fetchDefaultFromDb())` takes a lazy `Supplier` and **only executes when `opt` is empty**! Always use `orElseGet` for anything other than a simple constant.

#### 13. `Collectors.toList()` vs `Stream.toList()` (Java 16): mutability and null differences.
- **Simple Meaning (Dev-Friendly Question):** What is the difference between `.collect(Collectors.toList())` and `.toList()`?
- **Interview Answer:**
  - **`Stream.toList()` (Java 16+):** Returns an **unmodifiable list**, is faster/cleaner (sizes the array directly without copying), and **allows `null` elements** (unlike `List.of()`, which rejects `null`).
  - **`Collectors.toList()`:** Returns a **mutable `ArrayList`**. Prefer `.toList()` by default unless you specifically need to sort or mutate the returned list afterward.

#### 14. How do streams compare with loops for performance (JIT, boxing, allocation)? When do you drop to a loop?
- **Simple Meaning (Dev-Friendly Question):** Are Java Streams slower than traditional `for` loops?
- **Interview Answer:**
  - For 99% of backend I/O services (where DB/network takes milliseconds), the nanosecond difference between a `Stream` and a `for` loop is **completely irrelevant**—prefer whichever is more readable.
  - Drop down to a primitive `for` loop only in **ultra-hot CPU-bound paths** (tight math loops, parsers, game/trading engines) where allocating stream pipeline objects or boxing primitives adds measurable GC pressure.

#### 15. How do you handle checked exceptions and resource handling (files, `Stream.close()` for `Files.lines`) in stream pipelines?
- **Simple Meaning (Dev-Friendly Question):** Why does `Files.lines(path).toList()` leak file descriptors if you aren't careful?
- **Interview Answer:**
  - Unlike `List.stream()` (which is in-memory), **`Files.lines(path)` and `Files.list(dir)` hold an open OS file handle** that is NOT closed automatically when the terminal operation finishes!
  - You **must** wrap I/O streams in **`try-with-resources`**:
    ```java
    try (Stream<String> lines = Files.lines(path)) {
        return lines.filter(...).toList();
    }
    ```

#### 16. How do you write a custom `Collector`, and what are characteristics (`CONCURRENT`, `UNORDERED`, `IDENTITY_FINISH`)?
- **Simple Meaning (Dev-Friendly Question):** What are the 4 functions inside `Collector.of(...)`?
- **Interview Answer:**
  1. **`supplier()`:** Creates a new mutable accumulator container (`ArrayList::new`).
  2. **`accumulator()`:** Adds one element into the container (`List::add`).
  3. **`combiner()`:** Merges two containers from parallel threads (`(list1, list2) -> { list1.addAll(list2); return list1; }`).
  4. **`finisher()`:** Final transformation (`Collections::unmodifiableList`), or skipped if `Characteristics.IDENTITY_FINISH` is set.

#### 17. What is `Spliterator`, and how does it enable parallel decomposition?
- **Simple Meaning (Dev-Friendly Question):** What is a `Spliterator`, and how does a parallel stream split a list across CPU cores?
- **Interview Answer:**
  - `Spliterator` ("Splittable Iterator") is the engine behind `Stream`. Its key method is **`trySplit()`**, which cuts the remaining elements in half and hands the first half to another `ForkJoinPool` worker thread.
  - An `ArrayList` splits cleanly in $O(1)$ by dividing array indexes (`0..500` and `501..1000`), whereas a `LinkedList` or `Iterator` splits poorly because it doesn't know where the middle is without walking the nodes.

#### 18. How do you design a fluent, type-safe API with generics (builder with self-types, recursive bounds)?
- **Simple Meaning (Dev-Friendly Question):** What does `class BaseBuilder<B extends BaseBuilder<B>>` mean, and why is it used in Builder classes?
- **Interview Answer:**
  - If `ParentBuilder.withId(id)` returns `ParentBuilder`, then calling `new ChildBuilder().withId(id).withChildField(x)` won't compile because `.withId(id)` returned the `ParentBuilder` type!
  - Using a **self-bounded generic (`B extends BaseBuilder<B>`)** and returning `(B) this` (`self()`) ensures inherited builder methods always return the **exact child builder type** for smooth method chaining.

---

## 7. Multithreading and Concurrency

#### 1. Process vs thread. What are the thread states in Java (`NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, `TIMED_WAITING`, `TERMINATED`) and what causes each transition?
- **Simple Meaning (Dev-Friendly Question):** What is the difference between a process and a thread, and what do each of the 6 `Thread.State` values mean in a thread dump?
- **Interview Answer:**
  - **Process vs Thread:** A **Process** is an isolated OS program with its own private memory space (one JVM = one process). A **Thread** is a lightweight unit of execution *inside* a process—all threads in a JVM share the same Heap memory, while each has its own private Call Stack.
  - **The 6 Java Thread States:**
    1. **`NEW`:** Created (`new Thread()`), but `.start()` hasn't been called yet.
    2. **`RUNNABLE`:** Running on CPU, ready to run, or waiting on OS network/socket I/O.
    3. **`BLOCKED`:** Waiting at the door of a **`synchronized` block/method** for another thread to release the monitor lock.
    4. **`WAITING`:** Parked indefinitely waiting for a signal (`Object.wait()`, `Thread.join()`, `LockSupport.park()`, `ReentrantLock.lock()`).
    5. **`TIMED_WAITING`:** Waiting with a timeout (`Thread.sleep(1000)`, `wait(500)`, `future.get(2, SECONDS)`).
    6. **`TERMINATED`:** Finished execution or crashed with an unhandled exception.

#### 2. Explain the Java Memory Model: happens-before, visibility, atomicity, ordering, and reordering by compiler and CPU. Why is the JMM needed?
- **Simple Meaning (Dev-Friendly Question):** Why can Thread B still see an old value after Thread A updated a variable, and how does "happens-before" fix it?
- **Interview Answer:**
  - **Why JMM Is Needed:** Modern CPUs have per-core L1/L2 caches and write buffers, and both the JIT compiler and CPU reorder instructions for speed. Without synchronization, Thread A can update `ready = true` in Core 1's cache while Thread B on Core 2 keeps reading `ready = false` from its own cache forever!
  - **3 Guarantees:**
    - **Visibility:** When Thread A writes a value, Thread B is guaranteed to see the latest value.
    - **Atomicity:** An operation completes in a single indivisible step without another thread seeing half-written state.
    - **Ordering:** Instructions are not reordered across memory barriers.
  - **Happens-Before Rule:** A formal guarantee that if Action A *happens-before* Action B (via `volatile` write -> read, `synchronized` unlock -> lock, `Thread.start()`, or `Thread.join()`), then everything Thread A did before Action A is **100% visible** to Thread B.

#### 3. `volatile`: what exactly does it guarantee, what does it not (compound actions like `count++`), and when is it enough?
- **Simple Meaning (Dev-Friendly Question):** What does `volatile` actually do, why doesn't `volatile int count` fix `count++`, and when is `volatile` enough on its own?
- **Interview Answer:**
  - **What It Guarantees:**
    1. **Visibility:** Every write is flushed immediately to main memory, and every read fetches fresh from main memory.
    2. **No Reordering:** Inserts CPU memory barriers so instructions before a `volatile` write can never be reordered after it.
  - **What It Does NOT Guarantee:** **Compound atomicity!** `count++` is actually 3 steps: (1) read `count`, (2) add `1`, (3) write `count`. Two threads can both read `5` at the same time and both write `6`, losing an update.
  - **When `volatile` Is Enough:** When **only one thread writes** (or the new value doesn't depend on the old value), such as a `volatile boolean shutdownRequested = false;` flag or publishing an immutable config snapshot.

#### 4. `synchronized`: monitors, intrinsic locks, reentrancy, method vs block, lock on `this` vs private lock object. How are locks implemented in HotSpot today?
- **Simple Meaning (Dev-Friendly Question):** How does `synchronized` work under the hood, what is "reentrancy", and why is locking on a `private final Object lock` safer than `synchronized(this)`?
- **Interview Answer:**
  - **Reentrancy:** If a thread already holds a lock on `obj`, it can enter another `synchronized` method on the same `obj` (or recurse) without deadlocking itself—the JVM simply increments a lock recursion counter.
  - **Why Lock on a `private final Object lock = new Object()`:** If you use `synchronized(this)`, external code outside your class can also do `synchronized(yourService)` and accidentally cause a deadlock or denial-of-service!
  - **HotSpot Implementation:** Starts as a fast **Lightweight/Thin Lock** using a CAS operation on the object's Mark Word header (no OS kernel call if uncontended). If multiple threads fight for the lock, it **inflates** to a heavyweight OS **`ObjectMonitor`** that parks waiting threads. (Biased locking was removed in Java 15+ because its revocation cost outweighed its benefits on modern hardware).

#### 5. `wait` / `notify` / `notifyAll`: why must they be in a synchronized block and in a loop (spurious wakeups)? `sleep` vs `wait` vs `yield` vs `join`.
- **Simple Meaning (Dev-Friendly Question):** Why does Java throw `IllegalMonitorStateException` if you call `wait()` outside `synchronized`, why must `wait()` always be inside a `while` loop, and how does `wait()` differ from `sleep()`?
- **Interview Answer:**
  - **Why Inside `synchronized`:** Without holding the lock first, a producer could check `queue.isEmpty()`, and *right before* the consumer calls `wait()`, the producer adds an item and calls `notify()`—the consumer would miss the signal and sleep forever (**lost wakeup bug**)!
  - **Why Inside a `while (!condition)` Loop (Never `if`):** A waiting thread can wake up due to a **spurious OS wakeup** or because another thread grabbed the item between `notifyAll()` and lock re-acquisition. Always re-check the condition in a `while` loop!
  - **`wait()` vs `sleep()`:** `obj.wait()` **releases the lock** and waits to be notified; `Thread.sleep(ms)` **keeps holding all locks** while pausing the thread.

#### 6. `ReentrantLock` vs `synchronized`: fairness, `tryLock`, interruptible locking, multiple `Condition`s. When do you prefer which?
- **Simple Meaning (Dev-Friendly Question):** What can `ReentrantLock` do that `synchronized` cannot?
- **Interview Answer:**
  - **4 Superpowers of `ReentrantLock`:**
    1. **`tryLock(timeout)`:** Back off gracefully if the lock isn't available within 100ms instead of blocking forever.
    2. **`lockInterruptibly()`:** Allows a waiting thread to respond to `Thread.interrupt()`.
    3. **Multiple `Condition` Queues:** Separate wait sets (e.g., `notFull` and `notEmpty` in `ArrayBlockingQueue`) so producers only wake consumers.
    4. **Fairness Option (`new ReentrantLock(true)`):** Grants lock to the longest-waiting thread (prevents starvation, though lowers throughput).
  - **When to Prefer Which:** Use `synchronized` for simple, fast in-memory critical sections (especially on Java 24+ where pinning is gone). Use `ReentrantLock` whenever you need timeouts (`tryLock`), multiple conditions, or Java 21 virtual-thread compatibility around I/O.

#### 7. `ReadWriteLock` and `StampedLock` (optimistic reads): where do they help, and what are the pitfalls (writer starvation, no reentrancy for `StampedLock`)?
- **Simple Meaning (Dev-Friendly Question):** How do `ReadWriteLock` and `StampedLock` speed up read-heavy data, and what is the dangerous trap with `StampedLock`?
- **Interview Answer:**
  - **`ReentrantReadWriteLock`:** Allows many concurrent readers OR 1 exclusive writer. Under extreme read load, acquiring read locks still contends on an atomic reader-count integer and can starve writers.
  - **`StampedLock` (Java 8):** Adds **Optimistic Reads (`tryOptimisticRead()`)**—readers don't even acquire a lock! They read the fields and then check `lock.validate(stamp)` to see if a writer modified the data during the read; if so, they fall back to a read lock.
  - **`StampedLock` Trap:** `StampedLock` is **NOT reentrant**! If a thread holds a `StampedLock` and calls another method that tries to acquire the same lock, it **deadlocks itself immediately**.

#### 8. What is a deadlock? Describe four necessary conditions, how to detect it in a thread dump, and how to prevent it (lock ordering, `tryLock` with timeout, fewer locks).
- **Simple Meaning (Dev-Friendly Question):** What causes a deadlock, how do you spot it in a thread dump, and how do you guarantee it can never happen?
- **Interview Answer:**
  - **4 Coffman Conditions:** (1) Mutual exclusion, (2) Hold and wait, (3) No preemption, (4) **Circular wait** (Thread 1 holds A & wants B; Thread 2 holds B & wants A).
  - **How to Spot in Thread Dump:** Run `jcmd <pid> Thread.print`—the JVM literally prints `"Found one Java-level deadlock:"` at the bottom, showing which thread is `waiting to lock <0x001>` held by the other!
  - **How to Prevent:** Break **Circular Wait** by enforcing a **strict global lock order** (e.g., always lock the `Account` with the smaller `accountId` first), or use `ReentrantLock.tryLock(timeout)`.

#### 9. Livelock, starvation, priority inversion: explain each with an example.
- **Simple Meaning (Dev-Friendly Question):** How are Livelock, Starvation, and Priority Inversion different from a Deadlock?
- **Interview Answer:**
  - **Livelock:** Threads are not blocked (`RUNNABLE` at 100% CPU), but keep backing off for each other forever—like two polite people in a hallway stepping left and right at the exact same time. (Fix: add **random jitter** before retrying!).
  - **Starvation:** A thread never gets CPU time or a lock because greedy threads or short jobs keep jumping ahead of it.
  - **Priority Inversion:** A low-priority thread holds a lock that a high-priority thread needs, while medium-priority threads hog the CPU and prevent the low-priority thread from finishing and releasing the lock.

#### 10. Atomic classes and CAS: how do `AtomicInteger`, `AtomicReference`, `LongAdder`, and `LongAccumulator` work? What is the ABA problem and how do you handle it (`AtomicStampedReference`)?
- **Simple Meaning (Dev-Friendly Question):** How does lock-free CAS work, why is `LongAdder` faster than `AtomicLong` under heavy traffic, and what is the ABA problem?
- **Interview Answer:**
  - **CAS (Compare-And-Swap):** Single hardware CPU instruction (`CMPXCHG`) that says: *"Update memory address X from `expected` to `new` only if it still equals `expected`; otherwise tell me it failed so I can retry."*
  - **`LongAdder` vs `AtomicLong`:** Under high contention (32 threads incrementing 1 counter), `AtomicLong` wastes CPU spinning on CAS retries on a single memory variable. **`LongAdder`** stripes the counter across an array of **`Cell[]`** so threads update different cells with zero contention, and `sum()` adds them up at read time!
  - **The ABA Problem:** Thread 1 reads value `A`. Before Thread 1 runs CAS, Thread 2 changes `A -> B -> A`. Thread 1's CAS sees `A` and succeeds, unaware that the underlying node/state was replaced in between! **Fix:** Use **`AtomicStampedReference`** (attaches an incrementing integer version stamp `(value, version)`).

#### 11. `ThreadLocal`: use cases, memory leak with thread pools, `remove()`, `InheritableThreadLocal`, and why scoped values are the modern alternative.
- **Simple Meaning (Dev-Friendly Question):** How does `ThreadLocal` cause memory leaks and security bugs in Spring Boot apps, and why is `ScopedValue` better?
- **Interview Answer:**
  - Every `Thread` object has an internal `ThreadLocalMap` where keys are `WeakReference<ThreadLocal>` and values are **Strong References**.
  - In a Tomcat thread pool, threads live forever. If you set a `ThreadLocal` and forget `try { ... } finally { threadLocal.remove(); }`, the value stays attached to that thread forever (**memory leak** + **next HTTP request sees previous user's data**!).
  - **`ScopedValue` (Java 25)** fixes this permanently by being immutable and automatically clearing itself when the `ScopedValue.where(...).run(...)` block exits.

#### 12. Executor framework: `ThreadPoolExecutor` parameters (core, max, queue, keep-alive, thread factory, rejection policy), task submission flow, and common pool misconfigurations (unbounded queue, `newCachedThreadPool`).
- **Simple Meaning (Dev-Friendly Question):** This is a classic interview trap: when you submit a task to a `ThreadPoolExecutor(core=5, max=20, queue=100)`, when does it create the 6th thread?
- **Interview Answer:**
  - **Exact Submission Order (The Trap!):**
    1. If running threads `< corePoolSize (5)` -> creates a **core thread**.
    2. Once 5 core threads are busy -> new tasks go into the **`workQueue` (up to 100)**! It does **NOT** create thread #6 yet!
    3. **Only when the queue is 100% FULL (100 tasks queued)** -> it creates extra threads up to `maximumPoolSize (20)`.
    4. If the queue is full AND 20 threads are busy -> triggers the **`RejectedExecutionHandler`** (`AbortPolicy` throws `RejectedExecutionException`, `CallerRunsPolicy` runs the task on the caller's thread for natural back-pressure).
  - **Deadly Misconfiguration:** Using `Executors.newFixedThreadPool()` or default `LinkedBlockingQueue()` creates an **unbounded queue (`Integer.MAX_VALUE`)**—which means `maxPoolSize` is completely ignored and the queue grows until `OutOfMemoryError`! And `newCachedThreadPool()` creates unbounded threads (`Integer.MAX_VALUE`), crashing the OS under traffic spikes. Always construct `ThreadPoolExecutor` (or `ThreadPoolTaskExecutor`) with explicit bounds.

#### 13. How do you size a thread pool for CPU-bound vs I/O-bound work? Explain Little's Law and how you validate with measurements.
- **Simple Meaning (Dev-Friendly Question):** How many threads should you configure for CPU work vs database/HTTP work?
- **Interview Answer:**
  - **CPU-Bound Work:** **`Threads = Number of CPU Cores`** (or `Cores + 1`). Adding more threads only wastes CPU on context switching.
  - **Blocking I/O Work (Platform Threads):**
    $$\text{Threads} = \text{Cores} \times \left(1 + \frac{\text{Wait Time}}{\text{Compute Time}}\right)$$
    Or by **Little's Law ($L = \lambda \times W$)**: if you need to handle $\lambda = 500\text{ req/s}$ and each request waits $W = 0.1\text{s}$ (100ms), you need at least $500 \times 0.1 = 50$ concurrent threads (capped by downstream limits like your HikariCP DB pool size!).
  - **With Virtual Threads (Java 21+):** You don't size a thread pool at all—use virtual threads + a `Semaphore` or connection pool limit to protect the downstream dependency.

#### 14. `Callable` / `Future` limitations. How do `CompletableFuture` (`supplyAsync`, `thenApply`, `thenCompose`, `thenCombine`, `allOf`, `anyOf`, timeouts) fix them?
- **Simple Meaning (Dev-Friendly Question):** Why was Java 5's `Future` painful to use, and what is the difference between `thenApply`, `thenCompose`, and `thenCombine` in `CompletableFuture`?
- **Interview Answer:**
  - **Old `Future` Limitations:** The only way to get the result was calling `future.get()`, which **blocks the thread**. You couldn't chain callbacks, combine multiple futures, or attach a fallback value.
  - **`CompletableFuture` Methods:**
    - **`thenApply(fn)` (like `Stream.map`):** Synchronous transformation (`User -> String`).
    - **`thenCompose(fn)` (like `Stream.flatMap`):** Chains a **dependent** async call that itself returns a `CompletableFuture` (`getUser(id).thenCompose(u -> getOrdersAsync(u.id()))`).
    - **`thenCombine(otherFuture, fn)`:** Runs two **independent** futures in parallel and merges both results when both finish.
    - **`allOf(f1, f2, f3)`:** Waits for all parallel futures to complete.

#### 15. Which executor does `CompletableFuture` use by default, why is that dangerous for blocking calls, and how do you supply your own?
- **Simple Meaning (Dev-Friendly Question):** Why is calling `CompletableFuture.supplyAsync(() -> restClient.get())` without a second parameter a huge production bug?
- **Interview Answer:**
  - By default, `supplyAsync(supplier)` uses **`ForkJoinPool.commonPool()`**, which only has `CPU Cores - 1` threads shared across the entire JVM!
  - If 8 threads make a slow blocking HTTP call inside `commonPool()`, the entire common pool freezes—starving all other `CompletableFuture`s and `parallelStream()`s in the app.
  - **Fix:** Always pass a dedicated `Executor` as the 2nd argument: `CompletableFuture.supplyAsync(() -> callApi(), ioExecutor)`.

#### 16. `ForkJoinPool`: work-stealing, `RecursiveTask`, common pool and parallelism, and when `parallelStream` interacts with it.
- **Simple Meaning (Dev-Friendly Question):** How does "work-stealing" in `ForkJoinPool` work?
- **Interview Answer:**
  - Each worker thread in a `ForkJoinPool` has its own **double-ended queue (deque)** of tasks.
  - A worker pushes and pops its own subtasks from the **top (LIFO)** of its deque (warmest in CPU cache).
  - If a worker finishes all its own tasks and becomes idle, it **steals** a large chunk of work from the **bottom (FIFO)** of a busy thread's deque, keeping all CPU cores busy with minimal lock contention.

#### 17. Synchronisers: `CountDownLatch`, `CyclicBarrier`, `Semaphore`, `Phaser`, `Exchanger`. Give a use case for each.
- **Simple Meaning (Dev-Friendly Question):** When do you use `CountDownLatch` vs `CyclicBarrier` vs `Semaphore`?
- **Interview Answer:**
  - **`CountDownLatch(N)` (One-shot countdown):** One or more threads wait (`await()`) until $N$ operations complete (`countDown()`). **Cannot be reset.** Use case: waiting for 3 caches to warm up before starting traffic, or coordinating concurrent test threads.
  - **`CyclicBarrier(N)` (Reusable rendezvous point):** $N$ worker threads wait for **each other** at a barrier point until all $N$ arrive, then all proceed together (and the barrier resets for the next phase). Use case: multi-step parallel simulations.
  - **`Semaphore(permits)` (Concurrency limiter):** Controls how many threads can access a resource simultaneously (`acquire()` / `release()`). Use case: throttling Virtual Threads to max 50 concurrent calls to a rate-limited third-party API.
  - **`Phaser`:** Flexible, multi-phase barrier where threads can dynamically register and deregister.

#### 18. Producer-consumer: implement with `BlockingQueue`, then with `wait` / `notify`. What are the back-pressure and shutdown (poison pill) considerations?
- **Simple Meaning (Dev-Friendly Question):** What is the cleanest way to build a Producer-Consumer pipeline with back-pressure and clean shutdown?
- **Interview Answer:**
  - **Best Approach:** Use a **bounded `ArrayBlockingQueue<>(1000)`**.
  - **Back-pressure:** Producers call `queue.put(item)` (which blocks when the queue is full, automatically slowing down producers) or `queue.offer(item, 500, MILLISECONDS)` (to fail fast and return `429 Too Many Requests`).
  - **Clean Shutdown:** Either send a special **`POISON_PILL`** sentinel object into the queue to tell consumers to exit their loop, or interrupt the consumer threads after draining remaining items.

#### 19. Thread safety strategies: immutability, confinement, stateless design, synchronisation, concurrent collections, lock-free structures. How do you document thread-safety of a class?
- **Simple Meaning (Dev-Friendly Question):** Rank the ways to make Java code thread-safe from easiest/best to hardest.
- **Interview Answer:**
  1. **Stateless classes** (like Spring `@Service` / `@RestController` with no mutable fields)—zero shared state = zero bugs.
  2. **Immutability** (`record`, `final` fields, `List.of()`).
  3. **Thread Confinement** (local stack variables, `ScopedValue`).
  4. **Concurrent Collections & Atomics** (`ConcurrentHashMap`, `BlockingQueue`, `LongAdder`).
  5. **Coarse/Fine Locks** (`synchronized`, `ReentrantLock`)—last resort when coordinating multiple mutable invariants.

#### 20. Double-checked locking: why was it broken pre-Java 5, why is `volatile` needed, and what are safer alternatives (holder idiom, enum)?
- **Simple Meaning (Dev-Friendly Question):** Why MUST the `instance` field be marked `volatile` in a Double-Checked Locking singleton?
- **Interview Answer:**
  - `instance = new Singleton();` is actually 3 CPU steps:
    1. Allocate empty memory.
    2. Run the `Singleton()` constructor to initialize fields.
    3. Assign the memory address to `instance`.
  - **Without `volatile`:** The CPU/JIT is allowed to **reorder steps 2 and 3**! Thread A assigns the non-null memory address to `instance` *before* the constructor finishes, and Thread B sees `instance != null` and uses an **uninitialized, half-built object**!
  - Adding `volatile` forbids that reordering. (Even simpler: use the **Static Holder Class idiom** or an **`enum` singleton**, which need no manual locking at all).

#### 21. What is false sharing, how does `@Contended` or padding address it, and where have you seen it matter?
- **Simple Meaning (Dev-Friendly Question):** How can two threads modifying two *completely different* variables still slow each other down at the CPU hardware level?
- **Interview Answer:**
  - **False Sharing:** CPUs don't sync single variables to L1/L2 cache—they sync **64-byte Cache Lines**. If `volatile long counterA` and `volatile long counterB` sit next to each other in memory on the same 64-byte cache line, Core 1 writing `counterA` invalidates the entire 64-byte cache line for Core 2 reading `counterB` ("cache-line ping-pong")!
  - **Fix:** `@jdk.internal.vm.annotation.Contended` (used inside `LongAdder`'s `Cell` class and `ConcurrentHashMap`) adds **128 bytes of padding** around hot fields so each sits on its own dedicated CPU cache line.

#### 22. How do you shut down an executor gracefully (`shutdown`, `awaitTermination`, `shutdownNow`, handling interrupts)? How do you shut down a service with in-flight tasks?
- **Simple Meaning (Dev-Friendly Question):** What is the exact 3-step sequence to shut down an `ExecutorService` without dropping in-flight requests?
- **Interview Answer:**
  1. Call **`executor.shutdown()`**: Stops accepting *new* tasks, but lets already-running and queued tasks finish.
  2. Call **`if (!executor.awaitTermination(30, TimeUnit.SECONDS))`**: Waits up to 30s for in-flight tasks to complete.
  3. If they still haven't finished after 30s, call **`executor.shutdownNow()`** to send `Thread.interrupt()` to stuck threads.
  - (In Java 19+, `ExecutorService` implements `AutoCloseable`, so `try (var ex = ...)` automatically waits for tasks on close!).

#### 23. How do you propagate context (security, MDC, tracing) across threads and pools?
- **Simple Meaning (Dev-Friendly Question):** Why do your log `traceId` (MDC) and Spring `SecurityContext` disappear when you run code inside `@Async` or `CompletableFuture`, and how do you fix it?
- **Interview Answer:**
  - **Why They Disappear:** Both SLF4J `MDC` and Spring `SecurityContextHolder` store data in a **`ThreadLocal`** on the HTTP request thread. When work hops to a background pool thread, that worker thread has an empty `ThreadLocal`.
  - **How to Fix:**
    - Configure a **`TaskDecorator`** on your Spring `ThreadPoolTaskExecutor` that snapshots `MDC.getCopyOfContextMap()` and `SecurityContextHolder.getContext()` on the caller thread, sets them on the worker thread before `runnable.run()`, and calls `.clear()` in `finally`.
    - Use Micrometer **`ContextSnapshotFactory`** for automatic OpenTelemetry trace propagation.

#### 24. How do you test concurrent code (stress tests, `jcstress`, deterministic schedulers, avoiding `Thread.sleep`)?
- **Simple Meaning (Dev-Friendly Question):** Why is `Thread.sleep(100)` bad in unit tests for multi-threaded code, and what should you use instead?
- **Interview Answer:**
  - **`Thread.sleep` Is Flaky:** Either sleeps too long (slowing down CI) or too short on a busy CI runner (causing random test failures).
  - **Better Tools:**
    - Use **`CountDownLatch` / `CyclicBarrier`** to force all test threads to start at the exact same instant (`startLatch.countDown()`) to maximize race-condition collisions.
    - Use **Awaitility** (`await().atMost(Duration.ofSeconds(2)).untilAsserted(...)`) for async assertions.
    - Use OpenJDK **`jcstress`** for low-level lock-free/JMM memory-ordering verification.

#### 25. How do you diagnose a hung production service with thread dumps (reading `BLOCKED` threads, lock owners, thread pool exhaustion, `jstack` / `jcmd`)?
- **Simple Meaning (Dev-Friendly Question):** When a service hangs in production, what is your exact checklist when reading thread dumps?
- **Interview Answer:**
  1. Take **3 thread dumps 5 seconds apart** (`jcmd <pid> Thread.print -l`) + `top -H -p <pid>`.
  2. **Look for Deadlocks:** Check the bottom of the dump for `"Found one Java-level deadlock"`.
  3. **Look for Lock Convoys:** If 150 Tomcat threads are `BLOCKED (on object monitor)` waiting to lock `<0x000000071234>`, search the dump for `locked <0x000000071234>` to find the **1 owner thread** holding everyone up (usually stuck in a slow network/DB call!).
  4. **Look for Pool Exhaustion:** If 200 Tomcat threads are `WAITING` inside `HikariPool.getConnection()`, your DB connection pool is exhausted (check which threads are currently inside JDBC socket reads).

#### 26. Reactive vs thread-per-request vs virtual threads: when do you choose each, and how do they affect debugging and observability?
- **Simple Meaning (Dev-Friendly Question):** In Java 21/25, should you build new services with Reactive (Spring WebFlux) or Virtual Threads (Spring MVC)?
- **Interview Answer:**
  - **Virtual Threads (Spring MVC on Java 21+):** **Default choice for 95% of microservices.** Gives you the high-concurrency I/O scalability of reactive programming, while keeping simple sequential code, normal stack traces, step-through IDE debugging, and standard `try/catch` blocks.
  - **Reactive (WebFlux / Project Reactor):** Only choose today when you need rich **streaming back-pressure** pipelines, bi-directional streaming gateways (Spring Cloud Gateway, RSocket, SSE fan-out), or complex stream operators (`bufferTimeout`, `sample`).
  - **Platform Thread-Per-Request:** For CPU-heavy services or legacy libraries that pin native frames.

#### 27. How do you implement rate limiting, bulkheads, and timeouts at thread-pool level to protect a service?
- **Simple Meaning (Dev-Friendly Question):** How do you stop one slow downstream API from eating all threads in your microservice?
- **Interview Answer:**
  - **1. Timeouts First:** Never make an HTTP/DB call without an explicit connect + read timeout (`< 2s`).
  - **2. Bulkhead Pattern:** Isolate each downstream dependency into its own bounded `Semaphore` (for virtual threads) or separate bounded `ThreadPoolExecutor` (for platform threads). If the Recommendation API hangs, it can only exhaust its 20 bulkhead permits—leaving Checkout 100% healthy.
  - **3. Rate Limiting & Circuit Breaker:** Use Resilience4j `@Bulkhead`, `@RateLimiter`, and `@CircuitBreaker` to shed load fast (`429` / fallback) when capacity is reached.

#### 28. What is the difference between concurrency and parallelism? Amdahl's law and the limits of parallel speedup.
- **Simple Meaning (Dev-Friendly Question):** Explain Concurrency vs Parallelism in one sentence, and what is Amdahl's Law?
- **Interview Answer:**
  - **Concurrency vs Parallelism:** **Concurrency** is *dealing* with many tasks at once (1 barista taking orders and switching between making 3 coffees); **Parallelism** is *doing* many tasks at the exact same physical instant on multiple CPU cores (4 baristas making 4 coffees simultaneously).
  - **Amdahl's Law:** The maximum speedup of a program is limited by its **serial (non-parallelizable) fraction $S$**:
    $$\text{Max Speedup} = \frac{1}{S + \frac{1 - S}{N}}$$
    If 20% of your task must run sequentially ($S = 0.2$), even with **infinite CPU cores**, your program can never be more than **5x faster** ($\frac{1}{0.2} = 5$)!

---

## 8. Modern Concurrency: Virtual Threads, Structured Concurrency, Scoped Values

#### 1. What is a virtual thread? How does it differ from a platform thread (scheduling on carrier threads, stack stored on heap, cheap creation)?
- **Simple Meaning (Dev-Friendly Question):** Compare a traditional OS Platform Thread vs a Java 21+ Virtual Thread.
- **Interview Answer:**
  - **Platform Thread:** 1-to-1 wrapper around an OS kernel thread. Reserves **~1MB of native stack memory upfront**, takes ~1,000+ CPU cycles to create, and is scheduled by the OS kernel. Max practical limit is a few thousand.
  - **Virtual Thread:** Managed entirely by the JVM. Starts at just **a few hundred bytes on the Garbage-Collected Heap** (its stack grows/shrinks dynamically as a `Continuation` object), costs almost nothing to create, and runs on a small pool of OS **carrier threads** (`ForkJoinPool`). You can run **1,000,000+ concurrent virtual threads** in a single JVM!

#### 2. When do virtual threads help (high-concurrency blocking I/O) and when not (CPU-bound work, code with long native or foreign frames)?
- **Simple Meaning (Dev-Friendly Question):** Will switching to Virtual Threads make every Java app faster?
- **Interview Answer:**
  - **NO—Virtual threads do not run code faster; they let more requests wait concurrently!**
  - **Where They Help Hugely:** High-concurrency **I/O-bound microservices** that spend 95% of their time waiting on database queries, Redis, Kafka, or REST/gRPC calls.
  - **Where They Do NOT Help:** Pure **CPU-bound tasks** (video encoding, crypto hashing, sorting in-memory arrays). 8 CPU cores can still only execute 8 CPU instructions at once.

#### 3. What was pinning, why did `synchronized` pin virtual threads before Java 24, and what still can pin (native frames, some `Object.wait` cases)? How do you diagnose it (JFR events, `jdk.tracePinnedThreads` in older releases)?
- **Simple Meaning (Dev-Friendly Question):** What does "pinning" a virtual thread mean, how did Java 24 fix the biggest cause, and what still pins?
- **Interview Answer:**
  - **What Pinning Means:** When a virtual thread hits blocking I/O, it wants to **unmount** from its OS carrier thread. If it is *pinned*, it cannot unmount—so it blocks the underlying OS carrier thread too!
  - **Java 21–23 vs Java 24+:** In Java 21–23, JVM `synchronized` monitors tracked ownership by the OS carrier thread rather than the virtual thread, so blocking I/O inside `synchronized` pinned the carrier thread (we had to replace `synchronized` with `ReentrantLock`). **Java 24 (JEP 491) fixed this** so `synchronized` no longer pins!
  - **What Still Pins:** Blocking while a **JNI / Foreign Function native C frame** or class initializer (`<clinit>`) is on the call stack.
  - **How to Diagnose:** Monitor the **`jdk.VirtualThreadPinned`** JFR event (recorded automatically whenever a thread blocks while pinned for $> 20\text{ms}$).

#### 4. Why should you not pool virtual threads? How do you limit concurrency instead (`Semaphore`, bounded work queues, per-resource limits)?
- **Simple Meaning (Dev-Friendly Question):** Why is `Executors.newFixedThreadPool(200, Thread.ofVirtual().factory())` an anti-pattern, and how do you throttle traffic instead?
- **Interview Answer:**
  - **Why Never Pool Virtual Threads:** Thread pools exist only because OS threads are expensive to create. Virtual threads are as cheap as allocating a `String`—creating a new virtual thread per task (`Executors.newVirtualThreadPerTaskExecutor()`) is *faster and cleaner* than pulling one from a shared pool queue!
  - **How to Limit Concurrency Instead:** If your database or downstream API can only handle 50 concurrent calls, guard that call with a **`Semaphore(50)`** or your HikariCP connection pool (`maximumPoolSize = 50`). Thousands of virtual threads can cheaply park on `semaphore.acquire()` without consuming OS threads.

#### 5. How do virtual threads interact with `ThreadLocal`, connection pools (HikariCP size), and `ExecutorService` shutdown semantics?
- **Simple Meaning (Dev-Friendly Question):** What three traps do teams hit with `ThreadLocal` and HikariCP when enabling Virtual Threads?
- **Interview Answer:**
  1. **`ThreadLocal` Memory Explosion:** Since virtual threads are not pooled, caching expensive objects (like `SimpleDateFormat` or `byte[]` buffers) in a `ThreadLocal` across 500,000 virtual threads will blow up heap memory! Use thread-safe shared objects (`DateTimeFormatter`) or `ScopedValue`.
  2. **HikariCP Pool Size:** Having 10,000 virtual threads does **NOT** mean you should set HikariCP `maximumPoolSize=10000` (that would crash PostgreSQL!). Keep HikariCP small (`20–50`), and make sure non-DB work runs *outside* `@Transactional` so connections are held for milliseconds.

#### 6. How do virtual threads change the case for reactive programming?
- **Simple Meaning (Dev-Friendly Question):** Did Virtual Threads kill Reactive Programming (`Mono` / `Flux`)?
- **Interview Answer:**
  - For **standard CRUD/REST microservices**, yes—most teams adopted WebFlux only because 200 Tomcat platform threads couldn't handle 10,000 concurrent slow I/O requests. Virtual threads give you that exact same 10,000+ concurrency using plain Spring MVC.
  - Reactive (`Flux`) remains valuable only when you need **true event-stream processing**, back-pressure negotiation, or reactive pub/sub pipelines.

#### 7. What is Structured Concurrency (preview in 25)? Explain scopes, forking subtasks, joining, shutdown policies (fail-fast, first-success), and how errors and cancellation propagate.
- **Simple Meaning (Dev-Friendly Question):** Walk through how `StructuredTaskScope` works when calling 3 microservices in parallel.
- **Interview Answer:**
  - You open a scope in `try (var scope = StructuredTaskScope.open())` (or `ShutdownOnFailure` / `ShutdownOnSuccess`):
    1. **Fork:** `Subtask<User> user = scope.fork(() -> fetchUser());` and `Subtask<Order> order = scope.fork(() -> fetchOrder());` (each spawns a virtual thread).
    2. **Join:** `scope.join()` waits for them.
    3. **Short-Circuit Policies:**
       - **Fail-Fast (`ShutdownOnFailure`):** If `fetchUser()` throws an exception after 5ms, the scope **immediately cancels `fetchOrder()`** so you don't waste time or leak orphan threads!
       - **First-Success (`ShutdownOnSuccess`):** Returns as soon as the *first* replica responds and cancels the slower ones.

#### 8. What is `ScopedValue` (final in 25)? How do `ScopedValue.where(...).run(...)` / `call(...)` work, and how do child tasks inherit bindings?
- **Simple Meaning (Dev-Friendly Question):** Show how `ScopedValue` passes user/trace context down a call chain and into child threads.
- **Interview Answer:**
  - Declare `private static final ScopedValue<UserContext> CURRENT_USER = ScopedValue.newInstance();`.
  - In your filter/controller: `ScopedValue.where(CURRENT_USER, ctx).run(() -> orderService.placeOrder());`.
  - Anywhere inside `placeOrder()` (or 10 methods deep), calling `CURRENT_USER.get()` returns `ctx`—and if `placeOrder()` forks child virtual threads using **`StructuredTaskScope`**, those child threads **automatically inherit** `CURRENT_USER` with zero copying! Once `run()` finishes, `CURRENT_USER` is automatically unbound.

#### 9. `ScopedValue` vs `ThreadLocal`: mutability, lifetime, inheritance cost, and performance with millions of virtual threads.
- **Simple Meaning (Dev-Friendly Question):** Give a quick comparison table of `ScopedValue` vs `ThreadLocal`.
- **Interview Answer:**

| Feature | `ThreadLocal` | `ScopedValue` (Java 25) |
| :--- | :--- | :--- |
| **Mutability** | Anyone can call `.set()` anywhere | **Immutable** (can only rebind for a sub-tree via `.where()`) |
| **Cleanup / Lifetime** | Unbounded; leaks if `.remove()` is forgotten | **Strictly bounded** to `.run()` / `.call()` block |
| **Child Thread Cost** | `InheritableThreadLocal` copies the whole map ($O(N)$ memory) | **$O(1)$ zero-copy** pointer sharing in `StructuredTaskScope` |

#### 10. How would you migrate a Tomcat / Spring MVC service from a platform-thread pool to virtual threads, and what do you measure before and after?
- **Simple Meaning (Dev-Friendly Question):** What is the checklist before flipping `spring.threads.virtual.enabled=true` in a Spring Boot 3.2+ service?
- **Interview Answer:**
  1. **Audit `ThreadLocal` Usage:** Search for custom `ThreadLocal` caches that store heavy objects per thread and replace them with shared thread-safe beans or `ScopedValue`.
  2. **Check for Pinning (if on Java 21):** Ensure JDBC drivers, logging frameworks (Logback 1.4.14+), and SDKs are upgraded so no I/O happens inside `synchronized` (or run on Java 24/25).
  3. **Add Downstream Bulkheads (`Semaphore`):** Since Tomcat will no longer cap concurrency at 200 threads, add semaphores/rate-limiters around fragile downstream APIs.
  4. **Measure Before & After:** Compare p99 latency under peak load, `jdk.VirtualThreadPinned` JFR events, DB connection pool wait time, and pod RSS memory.

#### 11. What are the risks of unbounded virtual thread creation (downstream overload, memory), and how do you add back-pressure?
- **Simple Meaning (Dev-Friendly Question):** If your service suddenly accepts 50,000 concurrent HTTP requests on Virtual Threads instead of queuing at 200 Tomcat threads, what can break?
- **Interview Answer:**
  - **1. Downstream DDoS:** Your service can accidentally DDoS your own database or downstream payment service with 50,000 simultaneous requests!
  - **2. Heap OOM from Request Payloads:** Even though a virtual thread itself is tiny (~1KB), if 50,000 virtual threads each deserialize a **100KB JSON request**, `50,000 * 100KB = 5GB` of live heap objects, triggering `OutOfMemoryError`!
  - **Fix:** Always enforce concurrency limits at the ingress/controller layer using a **Semaphore / Resilience4j Bulkhead / Tomcat `max-connections`** to reject excess load with `429 Too Many Requests`.

#### 12. How do you observe virtual threads (thread dumps in JSON format, JFR, `jcmd Thread.dump_to_file`)?
- **Simple Meaning (Dev-Friendly Question):** Why doesn't old `jstack` show virtual threads, and what command do you run instead?
- **Interview Answer:**
  - Traditional `jstack` / `jcmd Thread.print` only prints **platform/carrier threads** (printing 1,000,000 virtual threads in plain text would freeze the JVM).
  - To dump all virtual threads (grouped by `StructuredTaskScope` and executor), run:
    ```bash
    jcmd <PID> Thread.dump_to_file -format=json thread_dump.json
    ```
    And monitor **`jdk.VirtualThreadPinned`** and **`jdk.VirtualThreadSubmitFailed`** in JFR.

---

## 9. JVM Internals

#### 1. Describe the JVM architecture: class loader subsystem, runtime data areas, execution engine (interpreter, JIT), native interface, and GC.
- **Simple Meaning (Dev-Friendly Question):** What are the 4 main building blocks inside the JVM, and how does `.class` bytecode become machine code on the CPU?
- **Interview Answer:**
  1. **Class Loader Subsystem:** Loads `.class` files into memory, verifies bytecode safety, links symbolic references, and runs `static` initializers.
  2. **Runtime Data Areas (Memory):**
     - **Shared across all threads:** **Heap** (objects) and **Metaspace** (class metadata).
     - **Private per thread:** **Java Thread Stack** (local variables & method frames), **PC Register** (current bytecode instruction pointer), and **Native Method Stack** (C/C++ calls).
  3. **Execution Engine:** Starts by running bytecode immediately via the **Interpreter**, profiles which methods are "hot", and compiles those hot methods into ultra-fast native machine code using the **Tiered JIT Compiler (C1 & C2)**, alongside the **Garbage Collector (GC)**.
  4. **Native Interface (JNI / FFM API):** Bridges Java to OS/C libraries.

#### 2. Runtime memory areas: heap (young/old), Metaspace, thread stacks, PC registers, native method stacks, code cache, direct buffers. What lives in each, and which can cause which `OutOfMemoryError`?
- **Simple Meaning (Dev-Friendly Question):** What lives in each JVM memory region, and which `OutOfMemoryError` message points to which region?
- **Interview Answer:**
  - **Heap (`-Xmx`):** All object instances & arrays -> `OOM: Java heap space` or `OOM: GC overhead limit exceeded`.
  - **Metaspace (`-XX:MaxMetaspaceSize`):** Class definitions, method metadata, constant pool -> `OOM: Metaspace`.
  - **Thread Stacks (`-Xss`):** Method call frames and primitive local variables -> `StackOverflowError` (deep recursion) or `OOM: unable to create native thread` (too many OS threads for available RAM/OS limits).
  - **Direct ByteBuffers (`-XX:MaxDirectMemorySize`):** Off-heap buffers used by Netty/Kafka/NIO -> `OOM: Direct buffer memory`.
  - **Code Cache (`-XX:ReservedCodeCacheSize`):** Native machine code produced by the JIT compiler -> `CodeCache is full. Compiler has been disabled`.

#### 3. Class loading phases (loading, linking: verification, preparation, resolution; initialisation). When is a class initialised?
- **Simple Meaning (Dev-Friendly Question):** What happens step-by-step when the JVM loads a class, and what triggers its `static {}` block to run?
- **Interview Answer:**
  - **3 Phases:**
    1. **Loading:** Reads the `.class` bytes from JAR/disk and creates the `java.lang.Class<?>` object.
    2. **Linking:**
       - **Verification:** Checks bytecode is valid and doesn't violate type/stack rules.
       - **Preparation:** Allocates memory for `static` fields and sets them to **default zero values** (`0`, `null`, `false`), plus assigns compile-time `static final` constants.
       - **Resolution:** Turns symbolic class/method names into actual memory pointers.
    3. **Initialization (`<clinit>`):** Runs `static` variable assignments and `static {}` blocks.
  - **When Is a Class Initialized?** **Lazily on first active use**: `new MyClass()`, calling a `static` method, reading a non-constant `static` field, or `Class.forName("MyClass")`. Merely referencing `MyClass.class` or reading a `static final int X = 10` compile-time constant does **not** initialize the class!

#### 4. Class loader hierarchy (bootstrap, platform, application), parent delegation, and how custom class loaders are used (plugins, containers). What causes `ClassNotFoundException`, `NoClassDefFoundError`, `LinkageError`, and class-loader leaks?
- **Simple Meaning (Dev-Friendly Question):** How does the ClassLoader parent-delegation model work, and why do plugin systems or app servers sometimes break it?
- **Interview Answer:**
  - **3 Built-in ClassLoaders:**
    1. **Bootstrap ClassLoader (Native):** Loads core JDK classes (`java.base`—`String`, `Object`, `List`).
    2. **Platform ClassLoader:** Loads non-core JDK modules (`java.sql`, `java.xml`).
    3. **Application (System) ClassLoader:** Loads your app's JARs from the classpath.
  - **Parent Delegation:** Before loading a class, a ClassLoader asks its parent first. This prevents someone from putting a fake `java.lang.String` on the classpath to hijack the JVM.
  - **Custom ClassLoaders & Leaks:** Used by Tomcat, OSGi, and Kafka Connect to isolate plugins. A **ClassLoader leak** happens when a long-lived thread (`ThreadLocal`), JDBC `DriverManager`, or static map holds a reference to an object from a child plugin ClassLoader—preventing the entire plugin and its Metaspace classes from ever being garbage collected!

#### 5. What is Metaspace, how is it different from PermGen, and what causes Metaspace growth?
- **Simple Meaning (Dev-Friendly Question):** Why did Java 8 replace PermGen with Metaspace, and what causes a Metaspace memory leak today?
- **Interview Answer:**
  - **PermGen (Java 7):** Fixed-size contiguous region inside the Java Heap (`-XX:MaxPermSize`); notoriously threw `OOM: PermGen space` whenever apps used reflection/Spring proxies.
  - **Metaspace (Java 8+):** Lives in **Native OS Memory** and grows dynamically by default (though in production containers you should cap it with `-XX:MaxMetaspaceSize=256m`).
  - **What Causes Metaspace Leaks:** Dynamically generating classes at runtime without reusing them—e.g., creating a new Jackson `ObjectMapper`, JAXB `JAXBContext`, Groq/Groovy script, or CGLIB/ByteBuddy proxy inside every HTTP request!

#### 6. Object layout: header (mark word, class pointer), fields, alignment, compressed oops, and what compact object headers (Java 25) change.
- **Simple Meaning (Dev-Friendly Question):** How many bytes does a simple `new Object()` or `new Integer(5)` actually take in memory, and how does Java 25 shrink it?
- **Interview Answer:**
  - **Standard 64-bit Object Layout (with Compressed OOPs `< 32GB` heap):**
    1. **Mark Word (8 bytes):** Identity hashcode, GC age, lock bits.
    2. **Klass Pointer (4 bytes compressed):** Points to the class metadata in Metaspace.
    3. **Instance Fields + 8-byte Alignment Padding:** Every object size is rounded up to a multiple of **8 bytes**.
    - So `new Object()` = 12-byte header + 4-byte padding = **16 bytes**! And `new Integer(5)` = 12-byte header + 4-byte `int` = **16 bytes** (4x larger than a primitive `int`!).
  - **Compact Object Headers (Java 25 - JEP 519):** Packs the Mark Word and Klass Pointer together into a single **64-bit (8-byte) header**, shrinking almost every small object by 4 to 8 bytes and cutting total heap usage by **10–20%**!

#### 7. What is the JIT compiler? Explain tiered compilation (C1, C2), profiling, inlining, loop optimisations, OSR, escape analysis, and scalar replacement. What is deoptimisation?
- **Simple Meaning (Dev-Friendly Question):** How do the Interpreter, C1, and C2 compilers work together in Tiered Compilation, and what are Inlining, Escape Analysis, and Deoptimization?
- **Interview Answer:**
  - **Tiered Compilation (Levels 0 to 4):**
    - **Level 0 (Interpreter):** Runs bytecode immediately on startup so you don't wait for compilation, while counting method calls/loops.
    - **Levels 1–3 (C1 "Client" Compiler):** Quickly compiles warm methods with lightweight profiling instrumentation.
    - **Level 4 (C2 "Server" Compiler):** Uses the profiling data collected by C1 to apply aggressive optimizations for the hottest 5% of code.
  - **Top C2 Optimizations:**
    - **Method Inlining:** Copies the body of small methods (getters, lambdas) directly into the caller to eliminate call overhead and unlock further optimizations.
    - **Escape Analysis & Scalar Replacement:** If C2 proves a temporary object (`new Point(x, y)`) never leaves the current method, it **doesn't allocate it on the Heap at all**—it breaks its fields into CPU registers (**Scalar Replacement** = zero GC pressure!).
    - **OSR (On-Stack Replacement):** Swaps in JIT-compiled machine code for a long-running loop *while the loop is still executing*.
  - **Deoptimization:** C2 makes speculative bets (e.g., *"this interface only has 1 implementation loaded so far, so I'll inline it"*). If a second implementation is loaded later, the JVM hits an **uncommon trap**, throws away the compiled code, and falls back to the Interpreter/C1 to re-optimize safely.

#### 8. What is warm-up, and why do cold JVMs perform badly? How do CDS, AppCDS, AOT cache (Java 24 and 25 work), and GraalVM native image address it?
- **Simple Meaning (Dev-Friendly Question):** Why is a newly started Spring Boot pod slow for the first 30 seconds, and what are the modern ways to make startup and warm-up fast?
- **Interview Answer:**
  - **Why Cold JVMs Are Slow:** During the first few seconds after pod startup, thousands of classes are being loaded/verified from JARs, bytecode is running in the slow **Interpreter**, and C1/C2 background threads are competing with request threads for CPU to JIT-compile hot methods.
  - **Solutions:**
    1. **CDS / AppCDS / Project Leyden AOT Cache (`-XX:AOTCache` in Java 24/25):** Pre-parses, links, and caches loaded classes + method profiles into a memory-mapped archive during a training run, cutting Spring Boot startup time by **40–60%** while keeping full JVM peak throughput.
    2. **GraalVM Native Image:** Compiles Java ahead-of-time into a standalone native binary (`~50ms` startup, low RAM), at the cost of build time, reflection config, and slightly lower peak C2 throughput.

#### 9. What is a safepoint? Which operations need one, and how do long safepoints show up as latency spikes?
- **Simple Meaning (Dev-Friendly Question):** What is a JVM Safepoint, and what is "Time-To-Safepoint (TTSP)" lag?
- **Interview Answer:**
  - **What a Safepoint Is:** A point in execution where all application threads' heap references are known and consistent, allowing the JVM to pause threads safely for **Stop-The-World (STW)** operations (GC root scanning, thread dumps, heap dumps, C2 deoptimization).
  - **Time-To-Safepoint (TTSP) Spike:** All threads must reach a safepoint before STW work can start. If 99 threads stop in $10\mu\text{s}$, but **1 thread is stuck** inside a massive counted loop or a slow memory-mapped file page fault (`MappedByteBuffer`), the other 99 threads sit completely frozen waiting for that 1 thread to reach its safepoint check! Diagnose with `-Xlog:safepoint`.

#### 10. What is the difference between `invokevirtual`, `invokeinterface`, `invokespecial`, `invokestatic`, and `invokedynamic`? How do lambdas and string concatenation use `invokedynamic`?
- **Simple Meaning (Dev-Friendly Question):** What are the 5 method invocation bytecode instructions in the JVM, and why is `invokedynamic` so important?
- **Interview Answer:**
  - **`invokestatic`:** Calls `static` methods (bound at compile/link time).
  - **`invokespecial`:** Calls constructors (`<init>`), `private` methods, and `super.method()` (no dynamic override lookup needed).
  - **`invokevirtual`:** Standard polymorphic dispatch on class methods using a fixed-index **vtable**.
  - **`invokeinterface`:** Polymorphic dispatch on interface methods using an **itable**.
  - **`invokedynamic` (Java 7+):** Lets the JVM decide *at runtime* (via a **Bootstrap Method** that returns a `CallSite`) how to link a call—and then caches it! Java uses `invokedynamic` for **Lambdas** (`LambdaMetafactory`) and **`String` concatenation** (`StringConcatFactory` in Java 9+), so future JDK upgrades can make lambdas and string concat faster without you recompiling your `.class` files!

#### 11. How does the JVM implement `synchronized` (mark word, thin/lightweight lock, inflation to a monitor)? What happened to biased locking?
- **Simple Meaning (Dev-Friendly Question):** How does the JVM avoid an expensive OS lock when only one thread enters a `synchronized` block, and why was Biased Locking removed?
- **Interview Answer:**
  - **Thin / Lightweight Lock:** When Thread A enters an uncontended `synchronized(obj)` block, the JVM uses a fast CPU CAS instruction to point the object's **Mark Word** to a lock record on Thread A's stack—zero OS kernel calls!
  - **Inflation to Heavyweight Monitor:** Only if Thread B tries to acquire the same lock while Thread A holds it does the JVM "inflate" the lock into an OS-level `ObjectMonitor` and park Thread B.
  - **Why Biased Locking Was Removed (Java 15+):** Biased locking assumed the *exact same thread* locks an object forever (like old `Vector`/`Hashtable`). In modern thread-pool apps where multiple threads touch objects, revoking a biased lock required an expensive **global Stop-The-World safepoint**, hurting latency more than it helped.

#### 12. What is the Java Memory Model's relationship with hardware memory barriers, and how does the JIT respect `volatile`?
- **Simple Meaning (Dev-Friendly Question):** What does the JIT compiler actually emit at the CPU assembly level when it sees a `volatile` variable?
- **Interview Answer:**
  - The JIT disables compiler optimizations that would hoist a `volatile` read out of a loop (`while (!stopFlag)`), and emits hardware **Memory Fence / Barrier instructions** (`LoadLoad`, `LoadStore`, `StoreStore`, `StoreLoad`—or `lock addl` / `dmb` on x86/ARM) to flush the CPU store buffer and guarantee cache coherence across cores.

#### 13. What is `Unsafe`, and what replaces it (VarHandles, FFM API, `MemorySegment`)? Why is it being restricted?
- **Simple Meaning (Dev-Friendly Question):** What was `sun.misc.Unsafe`, why is Java locking it down, and what are the official replacements?
- **Interview Answer:**
  - **`sun.misc.Unsafe`:** An internal backdoor class that let libraries (Netty, Kafka, Mockito, Hazelcast) do C-style raw memory allocation, off-heap pointer math, and CAS operations—bypassing all Java safety checks (one bad pointer crashes the entire JVM with `SIGSEGV`).
  - **Why Restricted (Java 23–25):** To guarantee memory integrity by default.
  - **Official Replacements:**
    1. **`VarHandle` (Java 9+):** Safe, zero-overhead atomic/CAS/volatile operations on fields and arrays.
    2. **Foreign Function & Memory (FFM) API (`MemorySegment` & `Arena` in Java 22+):** Safe, bounds-checked, deterministic off-heap memory access.

#### 14. How does the JVM detect container limits (cgroups), and what do `-XX:MaxRAMPercentage`, `-XX:ActiveProcessorCount`, and CPU limits do to heap and GC thread sizing?
- **Simple Meaning (Dev-Friendly Question):** How does the JVM size its heap and thread pools inside a Docker/Kubernetes container, and what is the common Kubernetes CPU-limit trap?
- **Interview Answer:**
  - **Container Awareness (cgroups v1/v2):** Modern JVMs automatically read the container's cgroup memory and CPU limits instead of the host node's 64 cores / 256GB RAM.
  - **Memory Sizing (`-XX:MaxRAMPercentage=75.0`):** Tells the JVM to use 75% of the container's memory limit for the Java Heap, leaving 25% for Metaspace, thread stacks, direct buffers, and OS overhead.
  - **The Kubernetes CPU Limit Trap:** If a pod has `requests.cpu: 500m, limits.cpu: 1000m`, the JVM sees **1 CPU core** (`Runtime.getRuntime().availableProcessors() == 1`)! That forces `ForkJoinPool.commonPool()` to 1 thread and sets Parallel/G1 GC threads very low (or even picks `SerialGC` if `< 2` CPUs and `< 2GB` RAM!). You can override with `-XX:ActiveProcessorCount=4` or `-XX:+UseG1GC`.

#### 15. How do you read a bytecode listing (`javap -c -v`) to understand string concatenation, autoboxing, try-with-resources, or `switch` compilation?
- **Simple Meaning (Dev-Friendly Question):** How do you inspect what the Java compiler actually generated inside a `.class` file?
- **Interview Answer:**
  - Run `javap -c -p -v MyClass.class`.
  - It reveals compiler syntactic sugar:
    - **Autoboxing:** `Integer x = 5` compiles to `invokestatic Integer.valueOf:(I)Ljava/lang/Integer;`.
    - **`String` `+` inside a loop:** Compiles to `new StringBuilder()` inside the loop body, whereas a single-line `"a" + x` compiles to `invokedynamic makeConcatWithConstants`.
    - **`try-with-resources`:** Generates hidden nested `try/catch` blocks calling `resource.close()` and `Throwable.addSuppressed()`.

#### 16. What is the difference between JDK, JRE, JVM, and distributions (Temurin, Corretto, Zulu, Oracle, GraalVM)? How do you choose, and what about support and licensing?
- **Simple Meaning (Dev-Friendly Question):** What is the difference between JVM, JRE, and JDK, and which OpenJDK distribution should you run in production containers?
- **Interview Answer:**
  - **JVM:** Runs bytecode. **JRE:** JVM + standard class libraries. **JDK:** JRE + compilers/diagnostic tools (`javac`, `jcmd`, `jfr`). (Since Java 11+, standalone JRE builds aren't published by OpenJDK; you either use a full JDK or build a custom slim runtime with `jlink`).
  - **Distributions:** All major OpenJDK builds (**Eclipse Temurin**, **Amazon Corretto**, **Azul Zulu**, **Microsoft Build of OpenJDK**) are built from the exact same OpenJDK source, pass the Java TCK, and are free for production use with long-term security patches.

#### 17. What are `jlink` and `jpackage`, and when do you use them to create smaller runtimes?
- **Simple Meaning (Dev-Friendly Question):** How does `jlink` shrink a 300MB Docker image down to 60MB?
- **Interview Answer:**
  - **`jlink`:** Analyzes which Java modules your app actually uses (`jdeps`) and bundles **only those modules** (`java.base`, `java.sql`, `java.logging`) into a custom minimal JRE (~35–45MB instead of a 200MB full JDK)—shrinking Docker image pull times and reducing CVE attack surface!
  - **`jpackage`:** Wraps a Java app + its `jlink` runtime into native OS installers (`.dmg`, `.msi`, `.deb`, `.rpm`).

#### 18. What are the JVM diagnostic options for JIT (`-XX:+PrintCompilation`), inlining, and code cache sizing? When do you ever tune them?
- **Simple Meaning (Dev-Friendly Question):** How do you check if the JIT compiler is failing to inline a hot method or running out of Code Cache?
- **Interview Answer:**
  - Use JFR (`jdk.CompilerInlining`, `jdk.CodeCacheFull`) or `-XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:+PrintInlining`.
  - If a huge microservice loads tens of thousands of classes and fills the default 240MB Code Cache (`CodeCache is full`), increase **`-XX:ReservedCodeCacheSize=512m`**. Avoid manually tweaking `-XX:MaxInlineSize` unless proven by JMH benchmarks.

#### 19. How does the JVM handle `StackOverflowError`, and how do you size thread stacks (`-Xss`) for deep recursion or many threads?
- **Simple Meaning (Dev-Friendly Question):** How does the JVM catch a `StackOverflowError` without crashing the OS process, and how do you tune `-Xss`?
- **Interview Answer:**
  - Each thread stack has **Yellow and Red Guard Pages** at the end. When recursion hits the Yellow Zone, the JVM unprotects it temporarily so it has just enough stack space to construct the `StackOverflowError` and unwind `finally` blocks!
  - Default `-Xss` on 64-bit Linux is **1024 KB (1 MB)**. For microservices without deep recursion, lowering to `-Xss512k` saves native memory across hundreds of platform threads (or use Virtual Threads, whose stack chunks grow dynamically on the heap).

#### 20. What is the role of the code cache, and what happens when it fills (compilation stops, performance cliff)?
- **Simple Meaning (Dev-Friendly Question):** Why does a JVM suddenly become 5x–10x slower if the Code Cache fills up?
- **Interview Answer:**
  - The **Code Cache** stores all native machine code generated by the JIT compiler.
  - If it reaches 100% capacity, the JVM prints `CodeCache is full. Compiler has been disabled` and **stops JIT-compiling any new hot methods** (and may flush existing compiled methods), forcing hot code back into the slow **Interpreter**—causing an immediate CPU spike and latency cliff! Monitor `jvm.memory.used{area="nonheap",id="CodeCache"}`.

#### 21. How do Java agents and instrumentation (APM, profilers, Byte Buddy) work, and what are their risks? What changed with dynamic agent loading restrictions?
- **Simple Meaning (Dev-Friendly Question):** How do Datadog, OpenTelemetry, and Mockito rewrite bytecode at runtime, and what changed in Java 21+?
- **Interview Answer:**
  - **How Agents Work:** Using `java.lang.instrument.Instrumentation`:
    - **Startup (`-javaagent:otel.jar`):** Runs `premain()` and registers a `ClassFileTransformer` that intercepts and modifies `.class` bytecode as classes load.
    - **Dynamic Attach (`agentmain()`):** Attaches to an already-running JVM PID at runtime (used by Mockito/ByteBuddy and profilers).
  - **Java 21+ Restriction (JEP 451):** Dynamic agent loading into a running JVM without `-XX:+EnableDynamicAgentLoading` now prints a warning (and will be blocked by default in a future release) so malicious libraries cannot secretly rewrite JDK security classes at runtime.

#### 22. Explain native memory usage beyond the heap (thread stacks, direct buffers, Metaspace, GC structures, code cache) and how Native Memory Tracking helps.
- **Simple Meaning (Dev-Friendly Question):** Why does an `-Xmx2g` Java app actually use 2.8 GB of RAM in Kubernetes, and how do you inspect the extra 800 MB?
- **Interview Answer:**
  - **`Total JVM RSS = Heap (-Xmx) + Metaspace + Code Cache + Thread Stacks (Threads * -Xss) + Direct ByteBuffers + GC Card Tables/Bitmaps + JNI/glibc malloc`**!
  - Enable **Native Memory Tracking (`-XX:NativeMemoryTracking=summary`)** and run:
    ```bash
    jcmd <PID> VM.native_memory summary.diff
    ```
    It breaks down exact byte usage by category (`Java Heap`, `Class`, `Thread`, `Code`, `GC`, `Internal/Direct Buffers`) so you can immediately see which off-heap area is growing.

---

## 10. Garbage Collection

#### 1. What is garbage collection, and what does "reachable" mean? Name GC roots.
- **Simple Meaning (Dev-Friendly Question):** How does the JVM decide whether an object is still needed or can be deleted (and why don't circular references `A <-> B` fool it)?
- **Interview Answer:**
  - Java does **not** use reference counting (which fails on `A <-> B` circular references). Instead, it uses **Reachability Analysis**: starting from **GC Roots**, any object that can be reached by following a chain of pointers is **Live**; everything else—even an isolated island of 100 objects pointing to each other—is **Garbage**.
  - **The 4 Main GC Roots:**
    1. **Local variables & parameters** on active Thread Stacks.
    2. **`static` fields** of loaded classes.
    3. **Active threads** (`Thread` objects).
    4. **JNI Global References** held by native C/C++ code.

#### 2. Explain generational GC: why do generations exist (weak generational hypothesis), what are Eden, survivor spaces, and old generation, and how does promotion/tenuring work?
- **Simple Meaning (Dev-Friendly Question):** Why does the JVM split the Heap into Young (Eden + Survivor) and Old generations?
- **Interview Answer:**
  - **Weak Generational Hypothesis:** **95%+ of objects die within milliseconds of being created** (HTTP request DTOs, temporary `StringBuilder`s, stream iterators).
  - By keeping new objects in a small **Young Generation**:
    1. New objects allocate in **Eden**.
    2. When Eden fills up, a fast **Minor GC** copies only the tiny 2–5% of surviving objects into **Survivor (`S0`/`S1`)** and wipes Eden clean in one shot!
    3. Each time an object survives a Minor GC, its **age counter** increments. Once its age reaches the **Tenuring Threshold** (default max 15, or dynamically calculated), it is **promoted** to the **Old (Tenured) Generation** (where long-lived Spring singletons and caches live).

#### 3. Compare the collectors: Serial, Parallel, G1, ZGC, Shenandoah, and Epsilon. State the goal, pause characteristics, and ideal workload for each.
- **Simple Meaning (Dev-Friendly Question):** Give a cheat-sheet comparing Java's 6 Garbage Collectors and when to pick each.
- **Interview Answer:**

| Collector | Flag | Target Pause | Best Workload |
| :--- | :--- | :--- | :--- |
| **Serial GC** | `-XX:+UseSerialGC` | Hundreds of ms (Single thread) | Tiny containers (`< 1GB` RAM, 1 CPU core), CLI tools |
| **Parallel GC** | `-XX:+UseParallelGC` | `100ms–1s+` (Max throughput) | Offline batch jobs, ETL pipelines where pauses don't matter |
| **G1 GC** *(Default)* | `-XX:+UseG1GC` | `10ms–200ms` (Balanced) | General-purpose microservices (`2GB–32GB` heap) |
| **Generational ZGC** | `-XX:+UseZGC` | **`< 1ms` (`~0.2ms`)** | Latency-critical APIs, payments, trading, large heaps (`4GB–16TB`) |
| **Generational Shenandoah** | `-XX:+UseShenandoahGC` | **`< 1–2ms`** | Low-latency services on moderate heaps (`2GB–64GB`) |
| **Epsilon GC** | `-XX:+UseEpsilonGC` | **0ms (Never collects!)** | Short-lived serverless functions or zero-allocation benchmarks |

#### 4. G1 in depth: regions, young and mixed collections, remembered sets and card tables, SATB marking, `MaxGCPauseMillis`, `InitiatingHeapOccupancyPercent`, humongous objects, evacuation failure (to-space exhausted), and full GC fallbacks.
- **Simple Meaning (Dev-Friendly Question):** How does G1 GC ("Garbage First") work under the hood, and what are Humongous Objects and Evacuation Failures?
- **Interview Answer:**
  - **Region-Based Heap:** Instead of one contiguous Young and Old block, G1 splits the heap into ~2,048 equal-sized **Regions (`1MB` to `32MB` each)** dynamically assigned as Eden, Survivor, Old, or Humongous.
  - **Why It's Called "Garbage First":** During **Mixed GC**, G1 collects all Young regions *plus* the Old regions that have the **most garbage** (highest space reclaimed per millisecond) to stay under `-XX:MaxGCPauseMillis=200` (default 200ms).
  - **Remembered Sets (RSet) & Card Tables:** Track which Old regions point into a Young region so Minor GC doesn't have to scan the entire Old Gen!
  - **SATB (Snapshot-At-The-Beginning):** G1's concurrent marking starts when Old Gen hits `InitiatingHeapOccupancyPercent` (IHOP, default 45%).
  - **Humongous Objects:** Any single object/array **`>= 50%` of a G1 Region size** (e.g., a `2.5MB byte[]` when region size is `4MB`) is allocated directly into contiguous **Humongous Regions** in Old Gen, causing fragmentation.
  - **Evacuation Failure (`To-space exhausted`):** Happens when G1 tries to copy surviving objects during a GC pause, but there are **no free regions left** to copy them into—triggering a painful, slow **Full GC**!

#### 5. ZGC in depth: coloured pointers, load barriers, concurrent relocation, sub-millisecond pauses, and generational ZGC (default since 23; the non-generational mode was removed in 24).
- **Simple Meaning (Dev-Friendly Question):** How does Generational ZGC achieve sub-millisecond (`< 1ms`) pauses even on a 500GB heap?
- **Interview Answer:**
  - In G1, the **evacuation (copying objects)** step is Stop-The-World—so the more live objects copied, the longer the pause.
  - **ZGC moves objects concurrently *while application threads are running*!**
  - **How (Colored Pointers + Load Barriers):** ZGC stores metadata bits inside the 64-bit object reference itself. Whenever your application reads a reference (`obj.field`), a tiny JIT-compiled **Load Barrier** checks if the object is currently being relocated by ZGC; if so, the thread immediately heals the pointer to the new address (**self-healing pointers**) without waiting for GC to finish!
  - **Generational ZGC (Default in Java 23+, sole ZGC mode in Java 24/25):** Separates Young and Old generations so short-lived objects are collected frequently with **4x lower CPU/memory overhead** and `< 0.5ms` pauses.

#### 6. Shenandoah in depth: concurrent evacuation, barriers, and the generational mode finalised in Java 25. When would you choose it over ZGC or G1?
- **Simple Meaning (Dev-Friendly Question):** How does Shenandoah GC work, what was finalized in Java 25, and when do you pick it?
- **Interview Answer:**
  - Like ZGC, **Shenandoah** performs **concurrent evacuation/compaction** alongside running application threads using Load Reference Barriers so pause times (`< 1–2ms`) never grow with heap size.
  - **Java 25 (JEP 521):** Finalized **Generational Shenandoah (`-XX:ShenandoahGCMode=generational`)** as a production feature!
  - **When to Choose Shenandoah:** Great for low-latency microservices running in tighter memory footprints (`2GB–32GB` containers) where you want sub-millisecond pauses with smaller region/headroom overhead than ZGC.

#### 7. How do you choose a GC for (a) a low-latency trading or API service, (b) a batch job, (c) a small container with 512 MB, (d) a 64 GB heap? What would you measure to decide?
- **Simple Meaning (Dev-Friendly Question):** Pick the best GC for those 4 real-world production scenarios.
- **Interview Answer:**
  - **(a) Low-latency trading or strict p99.9 API (`< 10ms` SLA):** **Generational ZGC** (`-XX:+UseZGC`) or **Generational Shenandoah** (`< 1ms` pauses).
  - **(b) Nightly batch/ETL job:** **Parallel GC** (`-XX:+UseParallelGC`)—maximizes raw CPU throughput when pauses don't matter.
  - **(c) Tiny 512 MB / 1 vCPU container:** **Serial GC** (`-XX:+UseSerialGC`)—lowest native memory footprint (G1/ZGC metadata takes too much RAM in 512MB).
  - **(d) 64 GB heap service:** **Generational ZGC** (avoids G1's multi-hundred-millisecond Mixed GC pauses on large heaps, and avoids the `32GB` Compressed OOPs cliff).

#### 8. What are stop-the-world pauses, and what contributes to them (root scanning, evacuation, remark, safepoint time, `System.gc()`)?
- **Simple Meaning (Dev-Friendly Question):** What actually freezes application threads during a Stop-The-World (STW) pause?
- **Interview Answer:**
  - **1. Time-To-Safepoint (TTSP):** Waiting for all running threads to reach a safepoint.
  - **2. Root Scanning:** Scanning active thread stacks and static variables.
  - **3. Evacuation / Copying (in G1/Parallel GC):** Moving live objects from Eden/Survivor/Old regions to new regions and updating pointers.
  - **4. Final Remark / Reference Processing:** Processing `SoftReference`, `WeakReference`, `PhantomReference`, and string deduplication tables.
  - **5. Explicit `System.gc()` calls:** (Disable in production with `-XX:+DisableExplicitGC`!).

#### 9. Define allocation rate, promotion rate, live set, GC overhead, and throughput. How do you compute them from GC logs?
- **Simple Meaning (Dev-Friendly Question):** Define the 5 core GC metrics every senior engineer looks at when analysing GC logs.
- **Interview Answer:**
  - **Allocation Rate (`MB/s`):** How fast your code creates new objects in Eden (measured as `Eden used before GC` divided by time since last GC).
  - **Promotion Rate (`MB/s`):** How fast objects move from Young Gen to Old Gen.
  - **Live Set Size (`MB`):** How much Old Gen memory remains occupied *right after* a Concurrent Mark / Full GC finishes (this is your true baseline memory usage!).
  - **GC Overhead (`%`):** Percentage of total wall-clock/CPU time spent in GC.
  - **GC Throughput (`%`):** `100% - GC Overhead %` (healthy services have **> 98% throughput**).

#### 10. Explain minor, mixed, and full GC. What triggers a full GC, and why is it a warning sign in G1 or ZGC?
- **Simple Meaning (Dev-Friendly Question):** What is the difference between Minor, Mixed, and Full GC, and why is a Full GC an emergency in G1?
- **Interview Answer:**
  - **Minor (Young) GC:** Collects only Eden + Survivor regions (fast, `5–30ms`).
  - **Mixed GC (G1):** Collects all Young regions + a few high-garbage Old regions.
  - **Full GC:** Emergency fallback that pauses the entire JVM for **seconds** to mark, sweep, and compact the **entire Heap** from scratch!
  - **What Triggers Full GC in G1:** Old Gen fills up faster than background Concurrent Marking can finish (**Evacuation Failure / To-space exhausted**), Humongous object allocation failure, or Metaspace exhaustion.

#### 11. What is premature promotion, and what are its effects (old gen fills, frequent mixed or full collections)? How do you fix it?
- **Simple Meaning (Dev-Friendly Question):** What happens when short-lived request objects spill into the Old Generation before requests finish, and how do you fix it?
- **Interview Answer:**
  - **What It Is:** If your **Allocation Rate** is huge (or Young Gen / Survivor space is too small), objects created for a slow 500ms HTTP request survive several rapid Young GCs and get **promoted to Old Gen**, only to become garbage 100ms later!
  - **Bad Effect:** Old Gen fills up rapidly with dead request objects, triggering constant expensive Mixed/Full GCs.
  - **How to Fix:** Reduce allocation rate in hot code, give the JVM more total heap (`-Xmx`) so Young Gen can be larger, or increase survivor capacity.

#### 12. TLABs and allocation: how does fast allocation work, and why is short-lived garbage cheap?
- **Simple Meaning (Dev-Friendly Question):** If 100 threads are calling `new Object()` at the same time on the same Heap, why don't they need a lock? And why is `new` in Java faster than `malloc` in C?
- **Interview Answer:**
  - **TLAB (Thread-Local Allocation Buffer):** The JVM gives every thread its own private slice of Eden memory (a TLAB).
  - When a thread calls `new Order()`, it simply **bumps a pointer (`top += size`)** inside its own private TLAB—**zero locks, zero CAS, ~3 CPU instructions**!
  - And when that short-lived object dies in Eden, the GC **never touches dead objects**—it only copies the few *live* objects out and resets the Eden pointer back to zero!

#### 13. How do escape analysis and scalar replacement reduce garbage? When do they not apply?
- **Simple Meaning (Dev-Friendly Question):** How can the JIT compiler eliminate heap allocations completely, and what breaks that optimization?
- **Interview Answer:**
  - **Escape Analysis + Scalar Replacement:** If C2 proves an object created inside a method (`new Point(x, y)`) never escapes to another thread or return value, it replaces the object with primitive local variables `int x, int y` right in CPU registers!
  - **When It Breaks:** If the method is too big to be **inlined**, if the object is stored into a collection/field, or if it's passed to a non-inlined virtual method call.

#### 14. Reference types: strong, soft, weak, phantom. How do `WeakHashMap`, `SoftReference` caches, and `Cleaner` use them? Why are soft-reference caches risky?
- **Simple Meaning (Dev-Friendly Question):** Explain Strong, Soft, Weak, and Phantom references, and why you should use Caffeine instead of `SoftReference` for caching.
- **Interview Answer:**
  - **Strong (`Object o = new Object()`):** Never collected while reachable.
  - **Soft (`SoftReference<T>`):** Collected *only* when the JVM is running critically low on heap memory before throwing `OOM`.
  - **Weak (`WeakReference<T>`):** Collected eagerly on the **very next GC cycle** once no strong references remain (used in `WeakHashMap` and `ThreadLocalMap` keys).
  - **Phantom (`PhantomReference<T>` + `ReferenceQueue`):** Enqueued *after* the object is finalized/unreachable to trigger post-mortem native cleanup (`java.lang.ref.Cleaner`).
  - **Why `SoftReference` Caches Are Dangerous:** They blind the GC's sizing heuristics, keep Old Gen artificially 95% full (causing constant GC churn), and when memory gets tight, GC wipes the entire soft cache at once (**cache stampede**)! Always use **`Caffeine`** with an explicit `.maximumSize(10_000)` or `.maximumWeight(...)` instead.

#### 15. What is finalisation, why was it deprecated, and what are the hazards (resurrection, delayed reclamation)?
- **Simple Meaning (Dev-Friendly Question):** Why was `Object.finalize()` deprecated and scheduled for removal?
- **Interview Answer:**
  - **1. Unpredictable Latency & OOM:** Objects with `finalize()` take at least **2 GC cycles** to reclaim and are processed on a single slow `Finalizer` thread—if one finalizer blocks, millions of objects pile up and crash the JVM with `OOM`!
  - **2. Object Resurrection Security Bug:** Code inside `finalize()` could assign `globalStatic = this;`, bringing a dead, partially-initialized object back to life!
  - **Modern Replacement:** `AutoCloseable` (`try-with-resources`) + `java.lang.ref.Cleaner` as a safety net.

#### 16. How do you read GC logs (`-Xlog:gc*`)? Identify pause causes, heap occupancy after GC, and signs of a leak vs undersized heap.
- **Simple Meaning (Dev-Friendly Question):** When you look at a GC graph or `-Xlog:gc*` output, how do you tell a Memory Leak apart from an Undersized Heap?
- **Interview Answer:**
  - Look at **Heap Occupancy *After* Each Old/Mixed GC** (the bottom of the saw-tooth pattern):
    - **Healthy:** Saw-tooth drops back to a flat baseline (e.g., `1.2GB -> 400MB -> 1.2GB -> 400MB`).
    - **Undersized Heap:** Baseline is flat, but it sits at **85% (`1.7GB` out of `2GB`)**, forcing GC to run every second because `Live Set` is simply too close to `-Xmx`.
    - **Memory Leak:** The bottom of the saw-tooth **climbs higher and higher after every GC** (`400MB -> 650MB -> 900MB -> 1.4GB -> 1.9GB -> OOM`), even when traffic drops!

#### 17. What do you check when you see "GC overhead limit exceeded", "Java heap space", "Metaspace", "Direct buffer memory", or "unable to create native thread"?
- **Simple Meaning (Dev-Friendly Question):** Map each of the 5 `OutOfMemoryError` messages to its exact root cause and fix.
- **Interview Answer:**
  1. **`Java heap space`:** Single giant allocation (e.g., loading 5M DB rows into a `List`) or steady heap leak -> capture heap dump (`-XX:+HeapDumpOnOutOfMemoryError`).
  2. **`GC overhead limit exceeded`:** JVM spent **> 98% of its time in GC** and recovered **< 2% of the heap** over 5 consecutive GCs (heap is completely choked).
  3. **`Metaspace`:** `-XX:MaxMetaspaceSize` hit due to dynamic classloader/proxy leak.
  4. **`Direct buffer memory`:** Netty/NIO off-heap `ByteBuffer.allocateDirect()` exceeded `-XX:MaxDirectMemorySize` (often leaked Netty `ByteBuf` missing `.release()`).
  5. **`unable to create native thread`:** OS `ulimit -u` / `pids.max` or container native RAM exhausted by creating thousands of platform threads (e.g., `newCachedThreadPool()`).

#### 18. Sizing: how do you set heap (`-Xms`, `-Xmx`, `MaxRAMPercentage`), why set min equal to max in servers, and how much memory must be left for non-heap in a container?
- **Simple Meaning (Dev-Friendly Question):** How should you set `-Xms` and `-Xmx` (or `MaxRAMPercentage`) for a Kubernetes pod, and how much headroom do you leave for non-heap?
- **Interview Answer:**
  - **Rule 1 (Leave 25%–30% for Non-Heap):** In a `4GB` Kubernetes pod, set Heap to **`70%–75%` (`~2.8GB–3GB`)**, leaving `~1GB` for Metaspace (`~256MB`), Code Cache (`~240MB`), Thread Stacks, Direct Buffers, and GC structures.
  - **Rule 2 (`-Xms == -Xmx` or `InitialRAMPercentage == MaxRAMPercentage`):** Setting initial heap equal to max heap avoids OS memory page-allocation pauses during traffic spikes and ensures that if the host doesn't have enough RAM, the container fails immediately at startup rather than crashing at 3 AM.

#### 19. How do you tune G1 safely, and when do you stop tuning and fix the allocation behaviour in code?
- **Simple Meaning (Dev-Friendly Question):** Why does Oracle recommend NOT setting `-Xmn` (fixed Young Gen size) with G1 GC, and what flags *are* safe to tune?
- **Interview Answer:**
  - **Never pin `-Xmn` (Young Gen size) in G1!** G1's entire pause-time prediction engine works by dynamically growing and shrinking Young Gen regions every cycle to meet `-XX:MaxGCPauseMillis`. Setting `-Xmn` breaks G1's pause target!
  - **Safe G1 Knobs:**
    1. Give enough total heap (`-Xmx` = `3x–4x` your Live Set).
    2. Set pause goal: `-XX:MaxGCPauseMillis=100` (default `200`).
    3. If Humongous objects (`> 50%` region size) appear in logs, either increase `-XX:G1HeapRegionSize=16m` or stream the data in code instead of allocating giant `byte[]` arrays!

#### 20. What is string deduplication, and what is its cost?
- **Simple Meaning (Dev-Friendly Question):** How does `-XX:+UseStringDeduplication` save heap memory without calling `String.intern()`?
- **Interview Answer:**
  - Many JSON/DB objects hold duplicate `new String("USD")` or `new String("COMPLETED")` instances.
  - With `-XX:+UseStringDeduplication` (supported in G1, ZGC, Shenandoah), when a `String` survives a few Young GCs, a background GC thread hashes its internal `byte[] value` array and makes duplicate `String` objects **share the exact same underlying `byte[]` array**—typically saving **5–10% total heap** at the cost of a tiny bit of background GC CPU.

#### 21. How do large heaps (tens or hundreds of GB), NUMA, and transparent huge pages affect GC choice and tuning?
- **Simple Meaning (Dev-Friendly Question):** What special JVM rules apply when running heaps larger than 32 GB (like 64 GB or 256 GB)?
- **Interview Answer:**
  1. **The 32 GB Compressed OOPs Cliff:** Between `32 GB` and `~48 GB`, 32-bit compressed object pointers turn off and become 64-bit pointers—so a **33 GB heap actually holds *fewer* objects than a 31 GB heap**! Never set `-Xmx` between `32g` and `48g`.
  2. **Use Generational ZGC (`-XX:+UseZGC`)** on heaps `>= 32 GB` so pause times stay `< 1ms`.
  3. **Enable NUMA (`-XX:+UseNUMA`)** on multi-socket servers so threads allocate memory on their local CPU socket's RAM controller.

#### 22. How do you diagnose a memory leak (heap histograms, heap dumps, dominator tree, retained size, GC root path, leak suspects)?
- **Simple Meaning (Dev-Friendly Question):** Explain Shallow Size vs Retained Size, and walk through how you find a memory leak in Eclipse MAT.
- **Interview Answer:**
  - **Shallow Size vs Retained Size:**
    - **Shallow Size:** The memory of the object header + its own fields (`HashMap` itself is only **48 bytes**).
    - **Retained Size:** The total memory that would be freed if this object were garbage collected (that 48-byte `HashMap` + all 500,000 `Order` objects inside it = **800 MB Retained Size**!).
  - **Step-by-Step Diagnosis:**
    1. First run lightweight `jcmd <pid> GC.class_histogram` twice 10 minutes apart to see which class count is growing.
    2. Capture a heap dump (`jcmd <pid> GC.heap_dump dump.hprof`) and open in **Eclipse MAT**.
    3. Open the **Dominator Tree** (sorted by largest **Retained Size**).
    4. Right-click the biggest object -> **`Path to GC Roots` -> `exclude weak/soft references`** -> it shows the exact `static` field or thread holding the leak!

#### 23. A container is OOMKilled but the Java heap looks fine. Explain what else consumes memory and how you find it.
- **Simple Meaning (Dev-Friendly Question):** Your Kubernetes pod died with `Exit Code 137 (OOMKilled)`, even though Grafana shows Heap was only at 600MB out of 1GB. Why did it die, and how do you prove it?
- **Interview Answer:**
  - **Why:** Kubernetes `OOMKiller` kills the pod based on **Total Process RSS (Heap + Native Memory)**, not Java Heap alone!
  - **Top 5 Culprits When Heap Is Fine:**
    1. **Direct ByteBuffers** (Netty/Kafka/gRPC off-heap buffers).
    2. **Too Many Platform Threads** (`500 threads * 1MB -Xss = 500MB`).
    3. **Unbounded Metaspace / Code Cache**.
    4. **Native C/JNI Libraries** (`RocksDB`, `OpenSSL`, `ImageIO`, `GZIPInputStream`/`Deflater` not closed!).
    5. **glibc malloc arena fragmentation** on multi-core Linux (`MALLOC_ARENA_MAX=2` or `jemalloc` fixes this).
  - **How to Find It:** Enable `-XX:NativeMemoryTracking=summary` and inspect `jcmd <pid> VM.native_memory summary`.

#### 24. What GC-related behaviour changed between Java 8, 11, 17, 21, and 25 that affects upgrade planning (G1 default, ZGC and Shenandoah maturity, generational modes, compact headers)?
- **Simple Meaning (Dev-Friendly Question):** How has Java's Garbage Collection evolved across LTS releases from Java 8 to Java 25?
- **Interview Answer:**
  - **Java 8:** **Parallel GC** was the default; G1 was early; no container cgroup awareness in early 8u releases.
  - **Java 11:** **G1 GC** became the default; G1 Full GC became multi-threaded (was single-threaded in Java 8!).
  - **Java 17:** Huge G1 memory reductions (shrunk Remembered Sets) + production-ready single-generation **ZGC** and **Shenandoah**.
  - **Java 21:** **Generational ZGC** introduced (`-XX:+UseZGC -XX:+ZGenerational`).
  - **Java 25:** **Generational ZGC** is the only ZGC mode (non-generational removed), **Generational Shenandoah** finalized (JEP 521), G1 Write Barriers shrunk (JEP 522), and **Compact Object Headers** (`-XX:+UseCompactObjectHeaders`, JEP 519) cut heap size by 10–20%!

---

## 11. Performance Tuning and Troubleshooting

#### 1. Describe your methodology for performance work: define the goal and SLOs, reproduce, measure, find the bottleneck, change one thing, and re-measure. How do you avoid guessing?
- **Simple Meaning (Dev-Friendly Question):** When someone says "our API is slow, please optimize it," what is your step-by-step engineering process instead of randomly tweaking code or JVM flags?
- **Interview Answer:**
  1. **Define Target SLOs:** Never optimize "make it faster"—define concrete numbers (e.g., *"p99 latency `< 100ms` at `2,000 RPS` with `< 1%` error rate"*).
  2. **Reproduce & Establish Baseline:** Run a steady-state load test after JVM warm-up and capture baseline metrics (p50/p95/p99, CPU, GC logs, DB query times).
  3. **Measure & Locate the Bottleneck (Amdahl's Law):** Use distributed tracing (OpenTelemetry) + JFR / `async-profiler` flame graphs to see where time is *actually* spent (80% of the time it's an N+1 DB query, missing index, lock contention, or downstream HTTP call—not JVM flags!).
  4. **Change ONE Variable at a Time:** Apply a single fix, re-run the exact same load test, and compare before/after graphs.

#### 2. Latency vs throughput, percentiles (p50, p95, p99, p99.9), coordinated omission, and why averages mislead.
- **Simple Meaning (Dev-Friendly Question):** Why is "Average Response Time" a useless metric, and what is "Coordinated Omission" in load testing?
- **Interview Answer:**
  - **Why Averages Lie:** If 99 requests take `1ms` and 1 request takes `10,000ms` (10 seconds), the **average is `100ms`**—which looks great on a dashboard while 1 in 100 users is staring at a frozen screen! Always track **p95, p99, and p99.9 tail percentiles**.
  - **Coordinated Omission:** If your load-testing tool sends requests on a fixed pool of threads and waits for each response before sending the next one, then whenever the server freezes for 2 seconds (e.g., a GC pause), the load tester *stops sending requests during those 2 seconds*—so only 1 request records the 2-second stall instead of the 2,000 requests that *would* have queued up in real life! Use open-workload rate generators (k6, Gatling `constantUsersPerSec`, wrk2).

#### 3. How do you profile a Java service in production safely (JFR, async-profiler, sampling vs instrumentation, overhead, safepoint bias)? How do you read a flame graph?
- **Simple Meaning (Dev-Friendly Question):** Why can you run JFR or `async-profiler` safely in live production, what is "Safepoint Bias", and how do you read a Flame Graph?
- **Interview Answer:**
  - **Sampling vs Instrumentation:**
    - **Instrumentation** injects timers into every method entry/exit—adds **30%–500% overhead** and skews results (never run in prod!).
    - **Sampling (`JFR`, `async-profiler`)** periodically snapshots stack traces via OS kernel/perf timers at **`< 1%` CPU overhead**—100% safe for live production!
  - **Safepoint Bias:** Old Java profilers (`VisualVM` sampler) only took stack snapshots at **JVM Safepoints**, missing hot native code or tight uncounted loops. **`async-profiler`** uses Linux `perf_events` + `AsyncGetCallTrace` to sample at *any* instruction without safepoint bias.
  - **How to Read a Flame Graph:**
    - **X-axis = Percentage of total samples (width = time/CPU spent)** (it is NOT time order—it's sorted alphabetically!).
    - **Y-axis = Call stack depth** (bottom is `Thread.run()`, top is the leaf method actively burning CPU).
    - **Look for wide flat "plateaus" at the top of a tower**—that method is your bottleneck!

#### 4. What are the common JMH benchmarking mistakes (dead-code elimination, constant folding, warm-up, shared state, wrong forks)?
- **Simple Meaning (Dev-Friendly Question):** Why is measuring code with `System.nanoTime()` in a `main()` loop completely wrong, and how does JMH fix it?
- **Interview Answer:**
  - **1. Dead-Code Elimination:** If you compute a result inside a benchmark loop and don't use it, the C2 JIT compiler notices the result is unused and **deletes your entire code** (`0 ns/op`)! **Fix:** Always return the result from the `@Benchmark` method or pass it to JMH's **`Blackhole.consume(result)`**.
  - **2. Constant Folding:** Passing hardcoded literals (`2 + 2`) lets JIT compute it at compile time. **Fix:** Read inputs from a **`@State` object** field.
  - **3. Cold JVM / Profile Pollution:** Always configure `@Warmup(iterations = 5)` and `@Fork(2)` so C2 compiles the benchmark cleanly in fresh JVM processes.

#### 5. CPU is at 100%. What are your first ten minutes (top threads, `jcmd Thread.print`, JFR, GC activity, lock contention, tight loops, regex)?
- **Simple Meaning (Dev-Friendly Question):** Production alert: a Java pod is pinned at 100% CPU. What exact commands do you run in the first 10 minutes?
- **Interview Answer:**
  1. **Check if it's GC Thrashing or App Threads:** Look at `jvm.gc.pause` / `jstat -gcutil <pid> 1000`. If GC threads are burning 95% CPU because Heap is 99% full, capture a heap dump.
  2. **Find the Exact Hot Thread ID:** Run `top -H -p <pid>` to see which Linux thread (`PID/TID`) is using 99% CPU, convert that decimal TID to hex (`printf "0x%x\n" <TID>`), and match `nid=0x...` in `jcmd <pid> Thread.print`!
  3. **Capture a 30-second Profile:** Run `jcmd <pid> JFR.start duration=30s filename=cpu.jfr` (or `asprof -d 30 -f flamegraph.html <pid>`).
  4. **Usual Suspects:** Catastrophic regex backtracking (`Pattern`), infinite `while` loop, `HashMap` mutated concurrently (Java 7), or massive JSON serialization loop.

#### 6. Latency spikes at regular intervals. How do you separate GC, safepoints, noisy neighbours, JIT deoptimisation, connection pool waits, and downstream slowness?
- **Simple Meaning (Dev-Friendly Question):** Every 5 minutes, API p99 latency spikes from 20ms to 2 seconds. How do you isolate which layer caused it?
- **Interview Answer:**
  - **Check Distributed Traces (OpenTelemetry) vs Server Access Logs:**
    - If your Spring Controller span took `2,000ms` and the child PostgreSQL/HTTP span inside it took `1,990ms` -> **Downstream slowness**.
    - If `HikariPool.getConnection()` took `1,980ms` -> **DB connection pool exhaustion**.
    - If there is a **2-second gap between spans where NO Java code ran at all** -> **JVM Pause (GC / Safepoint / OS CPU Throttling)**:
      - Check `-Xlog:gc*,safepoint` to separate a **GC pause** from **Time-To-Safepoint (TTSP)**.
      - Check Kubernetes `container_cpu_cfs_throttled_seconds_total` to see if Linux **CFS CPU quota throttling** froze the pod!

#### 7. How do you reduce allocation (reuse buffers, avoid boxing, avoid temporary strings, primitive collections, streams in hot paths)? When is it worth it?
- **Simple Meaning (Dev-Friendly Question):** What are the most effective ways to cut memory allocation in hot code paths, and when should you bother?
- **Interview Answer:**
  - **When It's Worth It:** Only in **profiler-proven hot paths** (check JFR **Allocation Flame Graph** first—don't micro-optimize cold CRUD code!).
  - **Top High-Impact Fixes:**
    1. Replace boxed `Map<Long, Double>` / `List<Integer>` with primitive collections (**Eclipse Collections** / **FastUtil** `LongDoubleHashMap`) or `long[]`.
    2. Pre-size collections (`HashMap.newHashMap(expectedSize)`, `new StringBuilder(256)`).
    3. Avoid `.split(",")` / `String.format()` inside tight loops (use `indexOf` or pre-compiled `Pattern`).
    4. Stream large JSON/CSV payloads directly to `OutputStream` instead of building a giant `String` or `byte[]` in memory.

#### 8. Memory footprint: how do compact strings, compact object headers, and data structure choices (`Integer` lists, `HashMap` overhead) reduce memory? How do you measure retained size?
- **Simple Meaning (Dev-Friendly Question):** Why does a `HashMap<Long, Long>` with 1 million entries use ~80 MB instead of 16 MB, and how do you measure exact object memory?
- **Interview Answer:**
  - Two primitive `long`s are only `16 bytes`. But in `HashMap<Long, Long>`, every entry allocates:
    - Boxed `Long` key (`24 bytes`) + Boxed `Long` value (`24 bytes`) + `HashMap$Node` object (`32 bytes`) + `table[]` slot (`4 bytes`) = **~84 bytes per entry (5x overhead!)**.
  - **How to Shrink Memory:**
    1. Turn on **Java 25 Compact Object Headers (`-XX:+UseCompactObjectHeaders`)** to shrink every object header from 12/16 bytes to 8 bytes.
    2. Use **Compact Strings** (default since Java 9, 1 byte per Latin-1 char).
    3. Use **JOL (Java Object Layout)** in dev or **Eclipse MAT Retained Size** on heap dumps to measure exact byte footprints.

#### 9. Lock contention: how do you find it (JFR lock profiling, thread dumps) and reduce it (smaller critical sections, striping, `LongAdder`, concurrent collections, immutable data)?
- **Simple Meaning (Dev-Friendly Question):** How do you find which lock is slowing down your service, and what are the 4 ways to fix lock contention?
- **Interview Answer:**
  - **How to Find It:** Inspect **`jdk.JavaMonitorEnter`** and **`jdk.ThreadPark`** events in JFR—it shows the exact class, line number, and total time threads spent waiting on each lock!
  - **4 Ways to Reduce Contention:**
    1. **Shrink Lock Scope:** Move logging, JSON parsing, and network/DB I/O **outside** the `synchronized` block.
    2. **Lock Striping / Sharding:** Split one global lock into 16/32 striped locks (like `ConcurrentHashMap` and `LongAdder`).
    3. **Switch to Lock-Free / Optimistic Reads:** Use `ConcurrentHashMap`, `AtomicReference`, `LongAdder`, or `StampedLock`.
    4. **Copy-on-Write / Immutable Snapshots:** For read-mostly config, swap an immutable `record` inside a `volatile` reference.

#### 10. Cache design: hit rate, eviction, TTL, stampede protection, on-heap vs off-heap vs Redis. What does Caffeine give you over a hand-rolled map?
- **Simple Meaning (Dev-Friendly Question):** Why should you never use a plain `ConcurrentHashMap` as an in-memory cache, and how does Caffeine prevent a "Cache Stampede"?
- **Interview Answer:**
  - **Why Never Use `ConcurrentHashMap` as a Cache:** It has **no size limit** (causes `OutOfMemoryError`), **no TTL expiration**, and **no eviction policy**.
  - **What Caffeine Gives You:**
    1. **Window-TinyLFU Eviction:** Near-optimal hit rate that resists one-time bulk scans from flushing hot keys.
    2. **Size & Time Bounds:** `maximumSize(10_000)`, `expireAfterWrite(5, MINUTES)`.
    3. **Cache Stampede (Thundering Herd) Protection:** If a hot key expires and 500 threads ask for it at the exact same millisecond, `cache.get(key, k -> loadFromDb(k))` ensures **only 1 thread calls the DB** while the other 499 wait for its result, and **`refreshAfterWrite`** reloads hot keys asynchronously in the background *before* they expire!

#### 11. I/O performance: buffered vs unbuffered, NIO and channels, memory-mapped files, zero-copy, async I/O, and when blocking I/O with virtual threads is enough.
- **Simple Meaning (Dev-Friendly Question):** What is "Zero-Copy" (`FileChannel.transferTo`), why must I/O always be buffered, and when are Virtual Threads enough?
- **Interview Answer:**
  - **Buffered vs Unbuffered:** Calling `FileInputStream.read()` reads 1 byte at a time via an expensive OS syscall; wrapping it in `BufferedInputStream` reads `8KB` blocks at once (**100x faster**).
  - **Zero-Copy (`FileChannel.transferTo`):** Normally, sending a file over HTTP copies bytes `Disk -> OS Kernel Buffer -> JVM User Space -> OS Socket Buffer -> NIC` (4 copies + context switches). `transferTo` uses the Linux `sendfile()` syscall to stream bytes **directly from Disk Kernel Cache to the Network Card** without ever copying bytes into JVM memory!
  - **When Virtual Threads Are Enough:** For 99% of JSON/REST/DB microservices, simple blocking I/O on Virtual Threads uses non-blocking OS `epoll`/`kqueue` under the hood automatically.

#### 12. Serialisation performance and size (JSON vs Protobuf vs Avro), reflection cost, and `MethodHandle` / `VarHandle` alternatives.
- **Simple Meaning (Dev-Friendly Question):** Compare JSON (Jackson) vs Protobuf vs Avro for microservices.
- **Interview Answer:**
  - **JSON (Jackson):** Human-readable and best for public browser/REST APIs, but repeats field names in every payload and burns CPU parsing UTF-8 text. (Tip: reuse a single `ObjectMapper` singleton and use Java `record`s or Jackson Afterburner/Blackbird module, which uses `MethodHandle` instead of slow reflection!).
  - **Protobuf (gRPC):** Binary tag-numbered schema, **3x–5x smaller payload** and **5x–10x faster serialization** than JSON—ideal for synchronous internal service-to-service RPC.
  - **Apache Avro:** Compact binary format paired with a **Schema Registry**—the gold standard for **Kafka event streaming** because the schema isn't repeated inside every message.

#### 13. Database access performance from Java: connection pool sizing, batching, N+1 queries, fetch size, prepared statement caching, and transaction scope.
- **Simple Meaning (Dev-Friendly Question):** What are the 5 biggest Java/JPA database performance killers and how do you fix them?
- **Interview Answer:**
  1. **The N+1 Select Problem:** Loading 100 `Order`s (`1 query`) and lazily fetching `order.getItems()` inside a loop (`100 queries`). **Fix:** Use `JOIN FETCH`, `@EntityGraph`, or DTO projections.
  2. **Over-sized HikariCP Pool:** A pool of `200` DB connections thrashes the DB with disk/index lock contention. Keep HikariCP tight (`10–30` connections per pod).
  3. **Holding `@Transactional` During HTTP/Kafka Calls:** Never call a slow external REST API inside an `@Transactional` method—it holds the DB connection hostage for seconds and starves the entire pod!
  4. **Missing JDBC Batching:** Inserting 5,000 rows one-by-one instead of enabling `hibernate.jdbc.batch_size=50` + `reWriteBatchedInserts=true`.
  5. **Default Fetch Size:** Loading large result sets without cursor streaming (`setFetchSize(500)`).

#### 14. Thread pool and queue sizing, back-pressure, timeouts, and the effect of unbounded queues on latency under overload.
- **Simple Meaning (Dev-Friendly Question):** Why does a large or unbounded thread-pool queue actually make outages *worse* during a traffic spike?
- **Interview Answer:**
  - **Queueing Delay Trap:** If your caller has a `2-second` HTTP timeout, and your thread pool has a queue of `10,000` requests that takes `15 seconds` to drain, **100% of the work your service does is wasted** because every single caller already timed out `13 seconds` ago!
  - **Fix:** Keep queues **small and bounded** (or use a `Semaphore` with fail-fast rejection + `429 Too Many Requests`), enforce strict timeouts, and shed excess load early.

#### 15. Startup time: class loading, reflection and classpath scanning, CDS / AppCDS, AOT cache, lazy initialisation, native image trade-offs.
- **Simple Meaning (Dev-Friendly Question):** How do you cut a Spring Boot 3 application's startup time from 15 seconds down to 2–3 seconds?
- **Interview Answer:**
  1. **Extract Layered JAR (`java -Djarmode=tools -jar app.jar extract`):** Running an unpacked JAR instead of nested fat-JAR zip streams speeds up startup by 15–20% immediately.
  2. **Enable CDS / Java 24–25 AOT Cache (`-XX:AOTCache=app.aot`):** Pre-caches class loading, linking, and method profiles—cuts startup by **40–60%**.
  3. **Spring AOT Processing (`spring-boot-starter-parent` AOT goal):** Evaluates `@ConditionalOn...` and bean wiring at build time instead of runtime classpath scanning.

#### 16. What JVM flags would you set for a container-based service, and which defaults would you leave alone? Justify each.
- **Simple Meaning (Dev-Friendly Question):** What is your production-ready `JAVA_TOOL_OPTIONS` baseline for a Java 21/25 microservice running in Kubernetes?
- **Interview Answer:**
  ```bash
  JAVA_TOOL_OPTIONS="
    -XX:InitialRAMPercentage=75.0
    -XX:MaxRAMPercentage=75.0
    -XX:+UseZGC
    -XX:+UseCompactObjectHeaders
    -XX:+HeapDumpOnOutOfMemoryError
    -XX:HeapDumpPath=/tmp/heapdump.hprof
    -XX:+ExitOnOutOfMemoryError
    -XX:+DisableExplicitGC
    -Xlog:gc*,safepoint:file=/tmp/gc.log:time,uptime,level,tags:filecount=5,filesize=20m
  "
  ```
  - **What to Leave Alone:** Never hardcode `-Xmn` (Young Gen size), survivor ratios, or JIT inlining thresholds—let the GC ergonomics self-tune!

#### 17. How do you validate that a JVM or GC upgrade (for example 17 to 21 or 25) improved performance and did not regress tail latency?
- **Simple Meaning (Dev-Friendly Question):** How do you prove with data that upgrading from Java 17 to Java 25 (or G1 to ZGC) actually improved production performance?
- **Interview Answer:**
  - Run a **Canary Deployment** receiving identical split production traffic (or a shadow/load test) and compare over a full 24-hour cycle:
    1. **p50, p95, p99, and p99.9 latency** (tail latency is where GC shows up).
    2. **Max GC Pause Time** and **Total GC CPU %**.
    3. **Container RSS Memory** (not just Heap!) and CPU utilization at identical RPS.

#### 18. What are the typical performance myths (final methods are faster, `StringBuffer` never, `i++` vs `++i`, micro-optimisations, manual `System.gc()`)?
- **Simple Meaning (Dev-Friendly Question):** Debunk 4 common Java performance myths that developers still repeat in code reviews.
- **Interview Answer:**
  1. **Myth: *"Adding `final` to methods makes them faster."*** False! Modern HotSpot C2 uses **Class Hierarchy Analysis (CHA)**—if a method has only one loaded implementation at runtime, C2 inlines it automatically whether you wrote `final` or not.
  2. **Myth: *"`++i` is faster than `i++` in Java."*** False! They compile to the exact same `iinc` bytecode instruction when the return value isn't used.
  3. **Myth: *"Calling `System.gc()` frees memory and helps performance."*** False! It triggers a massive Full GC pause and destroys latency.
  4. **Myth: *"Object pooling is always faster than `new`."*** False! Apart from heavy network/DB connections (`HikariCP`), pooling tiny Java objects is *slower* than TLAB pointer-bump allocation and causes Old Gen reference scanning overhead.

#### 19. How do you capture and interpret a thread dump: states, lock owners, "waiting to lock", thread pool exhaustion, repeated dumps over time?
- **Simple Meaning (Dev-Friendly Question):** Why is a single thread dump never enough, and how do you read `"waiting to lock"` vs `"locked"`?
- **Interview Answer:**
  - A single thread dump is just a **1-millisecond photograph**—you can't tell if a `RUNNABLE` thread has been stuck on that line for 30 seconds or just passed through it for $1\mu\text{s}$!
  - Always take **3 dumps 5 seconds apart** (`jcmd <pid> Thread.print -l`). If the exact same thread ID (`tid`) has the exact same call stack in all 3 dumps, it is genuinely stuck.
  - Match `waiting to lock <0x00000007a1b2>` in `BLOCKED` threads against the single thread that says `locked <0x00000007a1b2>` to find the root culprit.

#### 20. How do you diagnose a native memory leak (NMT, direct buffers, JNI, thread stacks, malloc arenas)?
- **Simple Meaning (Dev-Friendly Question):** What is the exact command workflow to catch a Native (off-heap) Memory Leak?
- **Interview Answer:**
  1. Start JVM with `-XX:NativeMemoryTracking=detail`.
  2. Take a baseline snapshot: `jcmd <pid> VM.native_memory baseline`.
  3. Wait 1 hour, then run diff: `jcmd <pid> VM.native_memory detail.diff`.
  4. If growth is outside NMT (inside native C/JNI `malloc`, e.g., unclosed `GZIPInputStream`/`Deflater` or `RocksDB`), profile native allocations using **`async-profiler -e malloc`** or **`jemalloc` (`jeprof`)**!

#### 21. How do you monitor a JVM in production (Micrometer, JMX, JFR streaming, GC metrics, thread counts, pool metrics) and which alerts matter?
- **Simple Meaning (Dev-Friendly Question):** Which 5 JVM/Spring Actuator metrics should actually page an on-call engineer vs just sit on a dashboard?
- **Interview Answer:**
  - **Metrics Worth Alerting On (Symptom + Saturation):**
    1. **SLO Breach:** HTTP error rate (`5xx > 1%`) or p99 latency above SLO.
    2. **Heap Live Set Saturation:** Old Gen occupancy *after GC* `> 85%` for 5+ minutes (`jvm.memory.used` / `jvm.memory.max`).
    3. **GC Pause / Thrashing:** `rate(jvm_gc_pause_seconds_sum[1m]) > 0.15` (spending >15% of time in GC).
    4. **Connection Pool Exhaustion:** `hikaricp.connections.pending > 0` sustained or `hikaricp.connections.timeout > 0`.
    5. **Container Memory Near Limit:** `container_memory_working_set_bytes / container_spec_memory_limit_bytes > 0.90`.

#### 22. How do you load-test responsibly and compare environments (realistic data, warm-up, steady state, soak tests, resource limits)?
- **Simple Meaning (Dev-Friendly Question):** Why do load tests pass in staging and still fail in production, and how do you design a realistic load test?
- **Interview Answer:**
  - **Why Staging Tests Lie:** Querying the *same 1 `userId`* 10,000 times hits 100% L1/Redis/DB buffer cache!
  - **Responsible Load-Test Rules:**
    1. **Realistic Data Distribution:** Randomize IDs across millions of records using a Zipfian/Pareto distribution.
    2. **Warm-Up Phase:** Discard the first 3–5 minutes of metrics while JIT C2 and connection pools warm up.
    3. **Soak Test (2–4+ hours):** Slow memory leaks and Old Gen fragmentation only show up after hours of sustained traffic, never in a 2-minute burst test.

---

## 12. I/O, Serialization, Date-Time, Reflection, Modules

#### 1. `java.io` vs `java.nio` vs `java.nio.file`: streams vs channels and buffers, blocking vs non-blocking, `Files` and `Path` APIs, and when to use each.
- **Simple Meaning (Dev-Friendly Question):** What is the difference between classic `java.io`, `java.nio`, and `java.nio.file` (`Path` / `Files`), and which should you use in modern Java?
- **Interview Answer:**
  - **`java.io` (`InputStream` / `OutputStream`):** Byte-stream oriented and strictly **blocking**.
  - **`java.nio` (`Channel`, `ByteBuffer`, `Selector`):** Buffer/block oriented; supports **non-blocking multiplex I/O** (1 thread monitoring 10,000 sockets via OS `epoll`, used inside Netty/Kafka) and zero-copy file transfers.
  - **`java.nio.file` (`Path.of(...)`, `Files.*`):** Modern replacement for `java.io.File`—always use `Path` and `Files` for file operations because `java.io.File.delete()` silently returns `false` on failure instead of throwing a clear `IOException`, and lacks symlink/atomic-move support.

#### 2. Direct vs heap `ByteBuffer`, memory-mapped files, and the risks of direct memory (`OutOfMemoryError: Direct buffer memory`).
- **Simple Meaning (Dev-Friendly Question):** Why do Netty and Kafka use `ByteBuffer.allocateDirect()` instead of normal Heap arrays, and what is the catch?
- **Interview Answer:**
  - **Why Direct Buffer:** When you pass a normal **Heap `ByteBuffer`** (`byte[]`) to an OS socket/file call, the JVM first has to **copy it into a temporary native off-heap buffer** because the GC might move the `byte[]` in the middle of the OS I/O call! A **Direct `ByteBuffer`** lives outside the GC heap in pinned native OS memory, avoiding that extra copy.
  - **Memory-Mapped Files (`MappedByteBuffer`):** Maps a disk file directly into virtual address space (used by Kafka brokers).
  - **The Catch:** Direct buffers are expensive to allocate (so pool them!), are not counted in `-Xmx`, and if leaked throw `OutOfMemoryError: Direct buffer memory` (capped by `-XX:MaxDirectMemorySize`).

#### 3. How do you read and write large files efficiently (buffering, streaming, `Files.lines`, chunking, `try-with-resources`)?
- **Simple Meaning (Dev-Friendly Question):** How do you process a 10 GB CSV file in a 512 MB container without running out of memory?
- **Interview Answer:**
  - **Never call `Files.readAllLines(path)` or `Files.readString(path)`**—that loads all 10 GB into Heap at once!
  - Instead, stream it lazily line-by-line inside **`try-with-resources`** (because `Files.lines()` holds an open file descriptor that leaks if not closed!):
    ```java
    try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
        lines.filter(...).forEach(...);
    }
    ```

#### 4. Java serialisation: `Serializable`, `serialVersionUID`, `transient`, custom `readObject` / `writeObject`, and security risks (deserialisation attacks, `ObjectInputFilter`). Why avoid it?
- **Simple Meaning (Dev-Friendly Question):** Why did Java architects call `java.io.Serializable` a "horrible mistake", and how do deserialization RCE attacks work?
- **Interview Answer:**
  - **How `Serializable` Works:** `serialVersionUID` is a version hash of the class structure (if you don't declare it explicitly, adding 1 field changes the auto-generated hash and breaks deserialization with `InvalidClassException`). Fields marked `transient` are skipped.
  - **Why Avoid It (Security & Design):**
    1. **Remote Code Execution (RCE) Gadget Chains:** `ObjectInputStream.readObject()` instantiates *any* class on the classpath before you can cast it! Attackers craft malicious byte streams chaining `readObject()` methods of common libraries to run shell commands.
    2. **Bypasses Constructors:** Deserialization creates objects via magic bytecode without running your constructor validation!
    - **If You Must Use Legacy `ObjectInputStream`:** Configure an allow-list using **`ObjectInputFilter`** (JEP 290).

#### 5. How do records and enums serialise? What are the alternatives (JSON, Protobuf, Avro, Kryo) and their trade-offs for compatibility and speed?
- **Simple Meaning (Dev-Friendly Question):** Why is deserializing a Java `record` or `enum` much safer than deserializing a normal `class`?
- **Interview Answer:**
  - **Normal Class:** `ObjectInputStream` bypasses the constructor and stuffs bytes directly into private fields—allowing attackers to create impossible/corrupted objects.
  - **`record` Serialization:** Always calls the **canonical constructor**, so all your validation rules inside `public MyRecord { ... }` are guaranteed to run!
  - **`enum` Serialization:** Serializes only the constant **name** (`"ADMIN"`) and resolves it via `Enum.valueOf()`, making it impossible to create duplicate enum singletons.

#### 6. `java.time`: `Instant`, `LocalDateTime`, `ZonedDateTime`, `OffsetDateTime`, `Duration`, `Period`, `DateTimeFormatter`. Why is `java.time` better than `Date` / `Calendar` / `SimpleDateFormat` (immutability, thread-safety)?
- **Simple Meaning (Dev-Friendly Question):** What were the bugs in `java.util.Date` and `SimpleDateFormat`, and when do you use `Instant` vs `LocalDateTime` vs `ZonedDateTime`?
- **Interview Answer:**
  - **Why `Date` and `SimpleDateFormat` Were Broken:** `java.util.Date` was **mutable** (months were 0-indexed so January was `0`!), and **`SimpleDateFormat` was NOT thread-safe**—sharing a `static final SimpleDateFormat` across threads corrupted dates in production!
  - **All `java.time` classes and `DateTimeFormatter` are 100% immutable and thread-safe:**
    - **`Instant`:** Exact machine timestamp in UTC (`2025-05-01T12:00:00Z`). Use for `createdAt`, `updatedAt`, logs, and events.
    - **`LocalDate` / `LocalDateTime`:** No timezone (`2025-05-01`). Use for birthdays or store opening hours.
    - **`ZonedDateTime`:** Full timezone + Daylight Saving Time rules (`America/New_York`). Use for calendar scheduling.
    - **`Duration` vs `Period`:** `Duration` is machine time (`hours/minutes/seconds`); `Period` is calendar time (`2 months, 3 days`).

#### 7. Time zones, DST, and storing time: why store UTC `Instant`, convert at the edge, and inject `Clock` for tests?
- **Simple Meaning (Dev-Friendly Question):** What is the golden rule for handling timestamps and testing time-dependent code in backend services?
- **Interview Answer:**
  - **1. Store & Transmit in UTC (`Instant`):** Always store timestamps in UTC in the database (`TIMESTAMP WITH TIME ZONE`) and API payloads (ISO-8601 `Z`), and only convert to the user's local timezone (`ZonedDateTime`) at the UI/notification edge (avoids 2 AM Daylight Saving Time ambiguity!).
  - **2. Inject `java.time.Clock`:** Never hardcode `Instant.now()` or `LocalDate.now()` directly in business logic! Inject a `Clock` bean (`Instant.now(clock)`) so unit tests can pass **`Clock.fixed(fixedInstant, ZoneOffset.UTC)`** for 100% deterministic tests.

#### 8. Reflection: what it can do, its performance cost, and security/module restrictions. How do `MethodHandle` and `VarHandle` compare?
- **Simple Meaning (Dev-Friendly Question):** Why is classic Java Reflection (`Method.invoke`) slow and restricted in modern Java, and why is `MethodHandle` faster?
- **Interview Answer:**
  - **Classic Reflection (`Method.invoke`):** Checks access permissions on every call, boxes primitive arguments into `Object[]`, wraps exceptions in `InvocationTargetException`, and blocks JIT inlining. Plus, in Java 17+, calling `setAccessible(true)` on internal JDK classes throws **`InaccessibleObjectException`** due to strong module encapsulation!
  - **`MethodHandle` & `VarHandle` (`java.lang.invoke`):** Perform access checking **once at lookup creation time** (`MethodHandles.lookup()`), use exact type signatures without boxing, and the JIT compiler can **inline** them just like a normal Java method call!

#### 9. Annotations processing at compile time vs runtime, and how frameworks like Spring and Lombok work with them.
- **Simple Meaning (Dev-Friendly Question):** How do Runtime Annotations (classic Spring) differ from Compile-Time Annotation Processors (`Lombok`, `MapStruct`, `Spring AOT`)?
- **Interview Answer:**
  - **Compile-Time (`RetentionPolicy.SOURCE` / `CLASS` + `javax.annotation.processing.Processor`):** Runs *inside `javac`* during build time. **MapStruct** generates concrete `.java` mapper classes (`OrderMapperImpl`), and **Lombok** modifies the compiler AST—zero runtime reflection overhead!
  - **Runtime (`RetentionPolicy.RUNTIME`):** Stored in the `.class` file and inspected at startup via reflection (like `@RestController`, `@Transactional`, `@Valid`).

#### 10. Dynamic proxies (JDK vs CGLIB / ByteBuddy) and how AOP frameworks use them.
- **Simple Meaning (Dev-Friendly Question):** How does Spring `@Transactional` or `@Cacheable` actually work under the hood, and why does calling `this.otherTransactionalMethod()` inside the same class bypass `@Transactional`?
- **Interview Answer:**
  - **How AOP Proxies Work:** Spring wraps your bean in a **Proxy object**:
    - **JDK Dynamic Proxy:** Implements the same **interface(s)** as your bean.
    - **CGLIB / ByteBuddy Proxy (Spring Boot default):** Generates a runtime **subclass** of your class and overrides non-`final` methods to open/commit the DB transaction before/after calling `super.myMethod()`.
  - **The Self-Invocation Trap:** When `methodA()` calls `this.methodB()` inside the same class, it calls `this` (the raw target object directly), **completely bypassing the outer Spring Proxy**—so `@Transactional`, `@Async`, or `@Cacheable` on `methodB()` never runs!

#### 11. `ServiceLoader` and SPI: how does it support plugin architectures, and how does it interact with modules?
- **Simple Meaning (Dev-Friendly Question):** How does `DriverManager.getConnection("jdbc:postgresql://...")` automatically find the PostgreSQL driver without you ever importing `org.postgresql.Driver`?
- **Interview Answer:**
  - It uses Java's **Service Provider Interface (`ServiceLoader.load(Driver.class)`)**!
  - The PostgreSQL JAR includes a file `META-INF/services/java.sql.Driver` (or `provides java.sql.Driver with org.postgresql.Driver` in `module-info.java`). `ServiceLoader` scans all JARs for that declaration and loads the implementation dynamically—enabling zero-coupling plugin architectures (JDBC, SLF4J, Jackson modules, JUnit 5 engines).

#### 12. JPMS essentials: `module-info.java`, `exports`, `opens`, `requires`, automatic modules, and why many teams still run on the classpath.
- **Simple Meaning (Dev-Friendly Question):** In `module-info.java`, what is the difference between `exports` and `opens`, and why do most Spring Boot apps still run on the Classpath?
- **Interview Answer:**
  - **`exports com.app.api`:** Makes `public` types in that package accessible at **compile time and runtime**.
  - **`opens com.app.entity`:** Allows frameworks (Hibernate, Jackson, Spring) to use **deep reflection (`setAccessible(true)`)** on private fields at runtime without exposing the package at compile time.
  - **Why Most Teams Still Use Classpath:** Even if your app stays on the traditional classpath (as the **"Unnamed Module"**), you *still* feel JPMS because the **JDK itself is modularized**—which is why upgrading from Java 8/11 to 17+ breaks old libraries that reflectively access `java.base` internals without `--add-opens`.

#### 13. HTTP clients in modern Java: `HttpClient` (Java 11), HTTP/2 support, async API, timeouts, and comparison to library clients.
- **Simple Meaning (Dev-Friendly Question):** What does the built-in `java.net.http.HttpClient` (Java 11+) support, and how does it fit with Spring 6's `RestClient` and Virtual Threads?
- **Interview Answer:**
  - Built-in `java.net.http.HttpClient` supports **HTTP/1.1 and HTTP/2 multiplexing**, WebSockets, connection pooling, both synchronous (`send()`) and async (`sendAsync()`), and pairs effortlessly with **Virtual Threads** (plus backs Spring Boot 3.2+'s fluent **`RestClient`** via `JdkClientHttpRequestFactory`).
  - Always set both **`connectTimeout`** on the `HttpClient.newBuilder()` AND **`timeout`** on every `HttpRequest.newBuilder()`!

#### 14. Foreign Function and Memory API vs JNI: safety, performance, and when you would use it.
- **Simple Meaning (Dev-Friendly Question):** Why is the Java 22+ Foreign Function & Memory (FFM) API so much better than old JNI?
- **Interview Answer:**
  - **Old JNI:** Required writing C header/glue code (`javah`), compiling platform-specific `.so`/`.dll` libraries, had high call-marshalling overhead, and one out-of-bounds C pointer crashed the entire JVM.
  - **FFM API (`Linker`, `MemorySegment`, `Arena`):** Calls native C libraries (`openssl`, `rocksdb`, `liburing`) **purely from Java code**, runs as fast as (or faster than) JNI via JIT intrinsics, and prevents use-after-free/buffer-overflow bugs via bounded **`Arena`** lifecycles.

#### 15. Logging: SLF4J facade vs implementation, async logging cost, structured logs, MDC propagation across threads.
- **Simple Meaning (Dev-Friendly Question):** What are the best practices for production logging in Java microservices?
- **Interview Answer:**
  1. **Code to SLF4J Facade (`LoggerFactory.getLogger`), Run Logback/Log4j2 Underneath.**
  2. **Use Parameterized Placeholders:** Write `log.debug("Order {} for user {}", orderId, userId)`—never string concatenation `"Order " + orderId`, which allocates a `String` even when `DEBUG` logging is disabled!
  3. **Structured JSON to `STDOUT`:** Emit JSON logs with `traceId`, `spanId`, `service`, and `userId` from **MDC** (Spring Boot 3.4+ has native structured logging `logging.structured.format.console=ecs/logstash` built in!).

#### 16. Random numbers: `Random` vs `ThreadLocalRandom` vs `SecureRandom` vs `RandomGenerator` (Java 17+). Which do you pick for what?
- **Simple Meaning (Dev-Friendly Question):** Why should you never share `java.util.Random` across threads, and when do you use `ThreadLocalRandom` vs `SecureRandom`?
- **Interview Answer:**
  - **`java.util.Random`:** Uses a shared `AtomicLong` seed—under multi-threaded load, threads fight over CAS retries and slow to a crawl!
  - **`ThreadLocalRandom.current()` (or Java 17+ `RandomGenerator`):** Zero contention across threads; use for **retry jitter, load-testing, sampling, and games**.
  - **`SecureRandom`:** Cryptographically strong; **mandatory for anything security-sensitive** (session tokens, OTP codes, password salts, cryptographic nonces/keys).

#### 17. `BigDecimal` for money: scale, rounding modes, `equals` vs `compareTo`, and the constructor pitfall (`new BigDecimal(0.1)`).
- **Simple Meaning (Dev-Friendly Question):** What are the 3 famous `BigDecimal` traps that every fintech/backend developer gets asked in interviews?
- **Interview Answer:**
  1. **Trap 1 (`new BigDecimal(0.1)`):** Passing a `double` to the constructor creates `0.100000000000000005551115...` because binary `double` cannot represent `0.1`! **Fix:** Always use **`new BigDecimal("0.1")`** or **`BigDecimal.valueOf(0.1)`**.
  2. **Trap 2 (`equals()` vs `compareTo()`):** `new BigDecimal("10.0").equals(new BigDecimal("10.00"))` returns **`false`** because `equals()` checks both value AND scale (`1 != 2`)! **Fix:** Always use **`a.compareTo(b) == 0`** for numeric equality.
  3. **Trap 3 (`divide()` without `RoundingMode`):** `new BigDecimal("1").divide(new BigDecimal("3"))` throws `ArithmeticException: Non-terminating decimal expansion`! Always pass scale + `RoundingMode.HALF_EVEN`.

#### 18. Locale, charset, and internationalisation pitfalls (default charset, number and date formatting, `Collator`).
- **Simple Meaning (Dev-Friendly Question):** How did Java 18 fix the classic default-charset bug, and why can `String.toUpperCase()` break in Turkey?
- **Interview Answer:**
  - **Charset:** Before Java 18, `new FileReader()` or `String.getBytes()` used the OS default charset (`Windows-1252` on Windows vs `UTF-8` on Linux), corrupting emojis/accents. **Java 18 (JEP 400) made `UTF-8` the default everywhere** (still pass `StandardCharsets.UTF_8` explicitly for clarity).
  - **Locale Trap:** In Turkish locale (`tr_TR`), `"title".toUpperCase()` produces `"TİTLE"` (with a dotted capital `İ`), breaking enum/SQL keyword matching! Always use **`str.toUpperCase(Locale.ROOT)`** for machine strings.

#### 19. Basic Java security: avoiding SQL injection, safe deserialisation, TLS configuration, secrets handling, and the removal of the Security Manager.
- **Simple Meaning (Dev-Friendly Question):** What are the non-negotiable Java security practices in production, and what happened to `SecurityManager`?
- **Interview Answer:**
  - **1. SQL/NoSQL Injection:** Always use parameterized **`PreparedStatement` (`?`)** or JPA `:namedParams`—never string concatenation or Text Blocks with `%s`!
  - **2. Safe Deserialization:** Never deserialize untrusted `ObjectInputStream`, XML (`XXE`—disable external entities), or polymorphic Jackson `@JsonTypeInfo` without a strict allow-list.
  - **3. TLS & Crypto:** Enforce **TLS 1.3**, store secrets in Vault/KMS (never in git or `application.yml`), and use Java 25's **KEM API** for quantum-safe key exchange.
  - **4. `SecurityManager`:** Deprecated in Java 17 and **permanently disabled in Java 24 (JEP 486)** because container/OS sandboxing (Kubernetes, seccomp, IAM) replaced in-JVM applet sandboxing.

---

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

- **Simple Meaning (Dev-Friendly Question):** When does Java reuse the same `String` reference from the String Pool vs creating a brand-new `String` on the Heap?
- **Interview Answer:**
  - **Output:** `true`, `false`, `false`, `true`, `true`.
  - **Why:**
    - `s1 == s2` is **`true`**: `"hel" + "lo"` is folded by `javac` at compile time into `"hello"`.
    - `s1 == s3` is **`false`**: `new String(...)` forces a brand-new Heap object.
    - `s1 == s4` is **`false`**: Because `part` is a non-`final` variable, `part + "lo"` runs at runtime (`invokedynamic`) and creates a new Heap `String`.
    - `s1 == s4.intern()` is **`true`**: `.intern()` returns the canonical pooled reference (`s1`).
    - `s1 == s5` is **`true`**: Because `fp` is marked `final`, `javac` treats it as a compile-time constant and folds `fp + "lo"` into `"hello"`!

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

- **Simple Meaning (Dev-Friendly Question):** Why does `127 == 127` print `true` for `Integer`, while `128 == 128` prints `false`?
- **Interview Answer:**
  - Autoboxing (`Integer a = 127`) compiles to `Integer.valueOf(127)`, which caches `Integer` objects in the range **`-128` to `127`**.
  - For `128`, `Integer.valueOf(128)` allocates a new `Integer` object each time, so `c == d` compares two different memory addresses (`false`). Always use `.equals()` or unbox to `int` (after null-checking, since unboxing `null` calls `n.intValue()` and throws `NullPointerException`).

### Q3. `finally` overrides the return

```java
static int f() { try { return 1; } finally { return 2; } }                  // returns 2
static int g() { int x = 1; try { return x; } finally { x = 2; } }          // returns 1
```

- **Simple Meaning (Dev-Friendly Question):** Why does `f()` return `2`, while `g()` returns `1` even though `finally` sets `x = 2`?
- **Interview Answer:**
  - In `f()`, putting a `return` statement inside `finally` **hijacks and overwrites** the `try` block's return value (and silently swallows any thrown exception—never write `return` in `finally`!).
  - In `g()`, when `try` executes `return x;`, the JVM **copies the value `1` into a hidden return-value slot** on the stack frame *before* running `finally`. Changing local variable `x = 2` inside `finally` doesn't change the already-saved return slot (`1`).

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

- **Simple Meaning (Dev-Friendly Question):** In what order do resources close in `try-with-resources`, and which exception wins if both the body and `close()` throw?
- **Interview Answer:**
  - **Output:** `open A` -> `open B` -> `close B` -> `close A` -> `body` -> `close B` -> `close A`.
  - **Why:** Resources are always closed in **reverse (LIFO) order** (`B` first, then `A`). The exception from the `try` block (`"body"`) is the **primary exception**, and the exceptions from `b.close()` and `a.close()` are attached to it via `e.addSuppressed(...)`.

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

- **Simple Meaning (Dev-Friendly Question):** In what exact order do static blocks, instance initializer blocks, and constructors run across a Parent and Child class?
- **Interview Answer:**
  - **Output:** `P static`, `C static`, `P instance`, `P ctor`, `C instance`, `C ctor`.
  - **Rule to Remember:**
    1. **All `static` blocks run first** (Parent -> Child, once when classes load).
    2. **Parent (`P`) instance block + Parent (`P`) constructor** run next.
    3. **Child (`C`) instance block + Child (`C`) constructor** run last.

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

- **Simple Meaning (Dev-Friendly Question):** Why do BOTH `map.get(k)` and `map.get(new Key(1))` return `null` after mutating `k.id = 2`?
- **Interview Answer:**
  - The entry was stored in **bucket #1**.
  - `map.get(k)` hashes `k.id` (`2`), looks in **bucket #2**, and finds nothing (`null`).
  - `map.get(new Key(1))` hashes `1`, looks in **bucket #1**, finds the node, and then compares `k.equals(new Key(1))`—which compares `2 == 1` (`false`), so it *also* returns `null`!
  - **Lesson:** Always use **immutable types (`String`, `Integer`, `UUID`, `record`)** as `Map` keys.

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

- **Simple Meaning (Dev-Friendly Question):** Why does calling `list.remove(i)` inside a `for-each` loop throw `ConcurrentModificationException` even in a single-threaded program?
- **Interview Answer:**
  - A `for-each` loop uses an internal `Iterator` that checks `modCount == expectedModCount` on every `.next()` call. Calling `list.remove()` directly increments `list.modCount` behind the iterator's back, triggering `ConcurrentModificationException`!
  - **Fix:** Use `list.removeIf(i -> i % 2 == 0)` (cleanest and $O(N)$ single-pass shift!) or `iterator.remove()`.

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

- **Simple Meaning (Dev-Friendly Question):** Why does `count++` lose updates even when `count` is declared `volatile`?
- **Interview Answer:**
  - `count++` is 3 separate operations (`read -> add 1 -> write`). `volatile` only guarantees each individual read and write is visible—it does not stop two threads from both reading `10` at the same time and both writing `11`. Use `AtomicInteger` or `LongAdder`.

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

- **Simple Meaning (Dev-Friendly Question):** Why does sorting the locks by `account.id()` make a deadlock mathematically impossible?
- **Interview Answer:**
  - Whether Thread 1 calls `transfer(acc1, acc2)` and Thread 2 calls `transfer(acc2, acc1)`, **both threads try to lock `acc1` first, then `acc2`**. That eliminates the **Circular Wait** condition, making deadlock impossible!

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

- **Simple Meaning (Dev-Friendly Question):** Why is `finally { CTX.remove(); }` mandatory whenever you use `ThreadLocal`?
- **Interview Answer:**
  - Pooled worker threads are reused across thousands of requests. Forgetting `CTX.remove()` causes both a **heap memory leak** and a **critical security bug** where User B's request inherits User A's `UserContext`!

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

- **Simple Meaning (Dev-Friendly Question):** Walk through what this `CompletableFuture` pipeline does and name the 4 interview gotchas interviewers ask about it.
- **Interview Answer:**
  1. **Always pass a custom `io` executor** so blocking HTTP calls don't starve `ForkJoinPool.commonPool()`.
  2. **`join()` vs `get()`:** `join()` throws unchecked `CompletionException`; `get()` throws checked `ExecutionException` + `InterruptedException`.
  3. **`orTimeout(2, SECONDS)` nuance:** It completes the `CompletableFuture` with a `TimeoutException` after 2s so the caller gets `"fallback"`, **but it does NOT interrupt/cancel the underlying `userClient` / `orderClient` HTTP threads** (unlike `StructuredTaskScope`!).
  4. **`exceptionally(ex -> ...)`:** Receives a `CompletionException` wrapping the real exception (`ex.getCause()`).

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

- **Simple Meaning (Dev-Friendly Question):** Why is this the canonical Java 21+ pattern for calling an external API across 10,000 URLs?
- **Interview Answer:**
  - It spawns 10,000 lightweight **Virtual Threads** (never pooling them!), uses a **`Semaphore(100)`** so at most 100 HTTP requests hit the downstream service at the same time, and uses **`try-with-resources`** on `ExecutorService` so `close()` automatically waits for all tasks to finish.

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

- **Simple Meaning (Dev-Friendly Question):** What happens at compile time if someone adds `Hexagon` to `permits Circle, Rect, Triangle, Hexagon`?
- **Interview Answer:**
  - Because there is **no `default:` branch**, the compiler **immediately fails with a compile error** (`the switch expression does not cover all possible input values`) on every `switch` that forgot to handle `Hexagon`! That compile-time safety is the #1 reason to avoid `default:` on sealed types.

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

- **Simple Meaning (Dev-Friendly Question):** What do `Gatherers.windowFixed(3)` and `Gatherers.windowSliding(2)` output?
- **Interview Answer:**
  - **`windowFixed(3)`:** Batches stream elements into non-overlapping chunks of 3 (`[[1, 2, 3], [4, 5, 6], [7]]`)—perfect for bulk DB/API calls!
  - **`windowSliding(2)`:** Creates overlapping sliding windows of size 2 (`[[1, 2], [2, 3], [3, 4]]`)—perfect for moving averages or diffing adjacent items.

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

- **Simple Meaning (Dev-Friendly Question):** What happens if you call `USER.get()` *after* `.run()` finishes, and why is `ScopedValue` cheaper than `ThreadLocal` for Virtual Threads?
- **Interview Answer:**
  - Calling `USER.get()` outside the `.run()` scope throws `NoSuchElementException` (check `USER.isBound()` or `USER.orElse("anonymous")`), making context leaks impossible.
  - Because `ScopedValue` is **immutable**, child virtual threads forked inside `StructuredTaskScope` share a single pointer to the parent's binding in **$O(1)$ memory** instead of copying a `ThreadLocalMap`.

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

- **Simple Meaning (Dev-Friendly Question):** What can you do before `super(...)` in Java 25, and what is still forbidden before `super(...)`?
- **Interview Answer:**
  - **Allowed:** Validate arguments (`if (value <= 0) throw ...`), transform parameters, and assign the subclass's own fields.
  - **Forbidden:** Reading `this` or calling instance methods on `this` before the superclass constructor `super(...)` has completed.

### Q17. Compact source file and instance `main` (final in Java 25)

```java
// Hello.java
void main() {
    IO.println("Hello, Java 25");
}
// Run with: java Hello.java
```

- **Simple Meaning (Dev-Friendly Question):** Why does `void main() { IO.println("Hello, Java 25"); }` compile and run without `public class Hello` or `String[] args`?
- **Interview Answer:**
  - **JEP 512 (Final in Java 25)** wraps top-level methods in an implicit final class, supports an instance `void main()` launch protocol, and adds `java.lang.IO` (`IO.println`, `IO.readln`) for clean scripting and teaching.

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

- **Simple Meaning (Dev-Friendly Question):** Does `list.reversed()` copy the list, or return a live view?
- **Interview Answer:**
  - `list.reversed()` returns a **$O(1)$ live reverse-ordered view** backed by the original collection—it does not copy the elements!

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

- **Simple Meaning (Dev-Friendly Question):** Explain the 3 classic `Stream` bugs shown above.
- **Interview Answer:**
  1. **`Collectors.toMap` without a 3rd merge argument** crashes with `IllegalStateException: Duplicate key` the first time two records share the same key.
  2. **`parallel().forEach(arrayList::add)`** mutates a non-thread-safe `ArrayList` from multiple threads—losing elements or throwing `ArrayIndexOutOfBoundsException`. Always use `.toList()` or `.collect()`.
  3. **Streams are single-use:** Calling a second terminal operation on the same `Stream` throws `IllegalStateException: stream has already been operated upon or closed`.

### Q20. `Arrays.asList` vs `List.of`

```java
List<String> a = Arrays.asList("x", "y");
a.set(0, "z");              // allowed, writes through to the array
a.add("w");                 // UnsupportedOperationException (fixed size)

List<String> b = List.of("x", "y");
b.set(0, "z");              // UnsupportedOperationException (immutable)
List.of("x", null);         // NullPointerException (nulls not allowed)
```

- **Simple Meaning (Dev-Friendly Question):** What is the difference between `Arrays.asList("x", "y")` and `List.of("x", "y")`?
- **Interview Answer:**
  - `Arrays.asList` is a **mutable fixed-size view** backed by an array: `.set()` works, `null` is allowed, `.add()` throws `UnsupportedOperationException`.
  - `List.of` is **100% immutable and rejects `null`**: both `.set()` and `.add()` throw `UnsupportedOperationException`, and passing `null` throws `NullPointerException`.

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

- **Simple Meaning (Dev-Friendly Question):** Name the 5 classic coding patterns that cause memory leaks in Java.
- **Interview Answer:**
  1. Unbounded `static` collections (`Map`/`List` that only ever `.put()`).
  2. Event listeners/callbacks registered on a long-lived publisher and never unregistered.
  3. Uncleaned `ThreadLocal` variables on pooled threads.
  4. Using `ConcurrentHashMap` as a cache without Caffeine size/TTL eviction.
  5. Non-`static` inner classes holding a hidden `this$0` reference to a huge outer class.

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

- **Simple Meaning (Dev-Friendly Question):** What three production best practices does this 20-line Producer-Consumer snippet demonstrate?
- **Interview Answer:**
  1. **Bounded Queue (`ArrayBlockingQueue(100)`):** `queue.put()` blocks when full, providing automatic **back-pressure**.
  2. **Poison Pill (`POISON`):** Cleanly signals the consumer to exit its `while (true)` loop after draining all prior items.
  3. **Restoring the Interrupt Flag:** `Thread.currentThread().interrupt()` inside `catch (InterruptedException)`. (Note: if you have $N$ consumers, the producer must enqueue $N$ `POISON` pills!).

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

- **Simple Meaning (Dev-Friendly Question):** Compare the 3 thread-safe Singleton patterns above—and why does `C.get()` copy `instance` into `C local = instance`?
- **Interview Answer:**
  - **1. Holder Idiom (`class A`):** Lazily initialized by the JVM's classloader lock the first time `A.get()` touches `H.I`—zero `synchronized` or `volatile` overhead!
  - **2. `enum B`:** Protects against both reflection (`setAccessible`) and serialization attacks automatically.
  - **3. Double-Checked Locking (`class C`):** Requires `volatile` to prevent instruction reordering. Reading `instance` into a local variable `C local = instance` avoids a **second `volatile` main-memory read** on the happy path (giving a ~25% micro-speedup)!

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

- **Simple Meaning (Dev-Friendly Question):** What makes a well-designed custom domain exception in a Spring Boot microservice?
- **Interview Answer:**
  - Extend **`RuntimeException`** (so it works cleanly with Lambdas and rolls back Spring `@Transactional` by default).
  - Always pass `cause` to `super(message, cause)` to preserve the root stack trace.
  - Carry structured fields (`orderId`, machine-readable `ErrorCode`) so `@RestControllerAdvice` can map it cleanly to an **RFC 7807 `ProblemDetail`** JSON response without leaking internal stack traces to API callers.

---

## 14. Production Scenario Questions

#### 1. A Java service's CPU suddenly hits 100% after a release. Walk through the investigation from first alert to root cause (thread CPU, GC, JFR, recent changes, regex or loops).
- **Simple Meaning (Dev-Friendly Question):** Right after deploying a new release, pod CPU jumps to 100%. How do you mitigate first and find the exact line of code causing it?
- **Interview Answer:**
  1. **Capture Forensics Before Rolling Back (60 seconds):** Keep 1 canary pod alive (or pull it out of the load balancer) and run `jcmd <pid> JFR.start duration=30s filename=/tmp/cpu.jfr` + `top -H -p <pid>` + `jcmd <pid> Thread.print`, then roll back traffic to restore user SLA.
  2. **Separate GC CPU vs Application Thread CPU:** If `G1 Young RemSet` / GC threads are burning 90% CPU, the release introduced a massive memory allocation or leak. If application worker threads (`http-nio-8080-exec-*`) are burning 99% CPU, match the hex thread ID `nid=0x...` from `top -H` in the thread dump.
  3. **Open the JFR Flame Graph:** Look at the widest plateau at the top of the CPU flame graph—9 times out of 10 after a release, it's a catastrophic regex backtracking (`java.util.regex.Pattern$Loop.match`), an infinite `while` retry loop on a null/edge case, or serializing a massive object graph.

#### 2. Heap usage grows slowly for days and the pod eventually restarts. How do you prove it is a leak and find the owner (histograms, heap dump, dominator tree, GC roots)?
- **Simple Meaning (Dev-Friendly Question):** Your pod runs fine for 4 days, then crashes with `OOM` and repeats. How do you prove it's a memory leak and pinpoint the exact leaking field?
- **Interview Answer:**
  1. **Prove It on Grafana:** Plot **Old Gen memory *immediately after* Old/Mixed GC**. If that baseline floor climbs steadily day after day (`300MB -> 600MB -> 1.1GB -> 1.8GB`) regardless of traffic dips, it is 100% a memory leak.
  2. **Lightweight Live Check:** Run `jcmd <pid> GC.class_histogram` on Day 1 and Day 3 (triggers a brief STW, much lighter than a full heap dump) and diff which class count/bytes grew by millions.
  3. **Pinpoint Owner in Eclipse MAT:** Take a heap dump (`jcmd <pid> GC.heap_dump`), open the **Dominator Tree** sorted by **Retained Size**, right-click the top object -> **`Path to GC Roots (exclude weak/soft references)`**, and inspect the exact `static` map, cache, or `ThreadLocal` holding it.

#### 3. p99 latency spikes every few minutes while p50 is fine. List every cause you would test (GC pauses, safepoints, JIT deopt, noisy neighbour, pool exhaustion, downstream).
- **Simple Meaning (Dev-Friendly Question):** Why would 50% of requests be super fast (`5ms`), while the slowest 1% (`p99`) spike to `1.5s` every few minutes?
- **Interview Answer:**
  - **Checklist in Order of Likelihood:**
    1. **GC Stop-The-World Pauses:** Mixed/Old GC or Humongous allocation running every few minutes (`-Xlog:gc*`).
    2. **Time-To-Safepoint (TTSP) Stalls:** One background thread in a huge counted loop or `MappedByteBuffer` disk flush delaying a safepoint (`-Xlog:safepoint`).
    3. **Kubernetes CFS CPU Throttling:** Pod bursts past its `limits.cpu` quota in the first 20ms of a 100ms period and gets paused for 80ms (`container_cpu_cfs_throttled_seconds_total`).
    4. **Connection Pool / Bulkhead Micro-Bursts:** Short spikes exhausting HikariCP (`hikaricp.connections.pending`) or downstream HTTP keep-alive connections expiring and re-handshaking TLS.
    5. **Scheduled Cron / Cache Eviction Job:** A `@Scheduled` job running every 5 minutes and locking a shared resource or flooding the DB.

#### 4. The container is OOMKilled but the Java heap is well below `-Xmx`. What else uses memory, and how do you diagnose and fix it?
- **Simple Meaning (Dev-Friendly Question):** Kubernetes killed your container (`OOMKilled / Exit 137`), even though `-Xmx` is `2GB`, container limit is `3GB`, and Heap was only at `1.2GB`. What ate the remaining memory?
- **Interview Answer:**
  - **What Ate It:** Off-heap / Native memory (`Direct ByteBuffers` in Netty/Kafka, `500+` platform thread stacks at `1MB` each, unbounded `Metaspace`, unclosed `GZIPInputStream`/`Deflater` native zlib buffers, or Linux glibc `malloc` arena fragmentation).
  - **How to Diagnose & Fix:**
    1. Enable `-XX:NativeMemoryTracking=detail` and run `jcmd <pid> VM.native_memory detail.diff`.
    2. Cap off-heap regions (`-XX:MaxDirectMemorySize=256m`, `-XX:MaxMetaspaceSize=256m`, `-Xss512k`).
    3. Set `MALLOC_ARENA_MAX=2` (or `LD_PRELOAD=libjemalloc.so`) in the Dockerfile to stop glibc native memory fragmentation!

#### 5. All request threads are stuck and the service stops responding. How do you read thread dumps to distinguish a deadlock, pool exhaustion, a slow downstream, or a lock convoy?
- **Simple Meaning (Dev-Friendly Question):** Your health check is timing out and all 200 Tomcat threads are stuck. How do you tell the 4 root causes apart in a thread dump?
- **Interview Answer:**
  - **1. Deadlock:** Thread A is `BLOCKED` waiting for Lock X (held by B) and Thread B is `BLOCKED` waiting for Lock Y (held by A). The bottom of `jcmd Thread.print` explicitly prints `"Found one Java-level deadlock"`.
  - **2. Lock Convoy:** 199 threads are `BLOCKED` waiting for the *same* Lock X, and the 1 thread holding Lock X is `RUNNABLE` inside a slow DB/HTTP socket read (move I/O outside `synchronized`!).
  - **3. DB Connection Pool Exhaustion:** 180 threads are `WAITING`/`TIMED_WAITING` at `HikariPool.getConnection()`, while 20 threads hold the DB connections (either stuck in a slow SQL query or waiting on an external HTTP call inside `@Transactional`).
  - **4. Slow Downstream Without Timeouts:** All 200 threads are `RUNNABLE` inside `SocketInputStream.socketRead0` calling a hung third-party REST API that has no read timeout configured.

#### 6. After upgrading Java 11 to 21, a service shows higher memory use and different GC behaviour. How do you investigate and decide whether to tune or roll back?
- **Simple Meaning (Dev-Friendly Question):** You upgraded a microservice from Java 11 to Java 21 and noticed RSS memory increased by 15%. Why does that happen, and how do you handle it?
- **Interview Answer:**
  - **Why It Happens:** G1 GC in newer JDKs may size regions or Young Gen more generously for throughput if `-Xmx` / `MaxRAMPercentage` gives it slack, or JIT Code Cache / native buffers changed defaults.
  - **How to Investigate:** Compare `jcmd <pid> VM.native_memory summary` and `-Xlog:gc*` between a Java 11 pod and a Java 21 pod. Often the **Live Set** is identical, and switching to **Generational ZGC** (`-XX:+UseZGC -XX:+ZGenerational`) or tightening `-XX:MaxRAMPercentage=70.0` (or upgrading to Java 25 with `-XX:+UseCompactObjectHeaders`) brings memory right back down without rolling back.

#### 7. A batch job that used to take 20 minutes now takes 3 hours. How do you find the cause (data volume, GC, N+1, pool sizing, parallelism, a changed library)?
- **Simple Meaning (Dev-Friendly Question):** A nightly Spring Batch job suddenly jumped from 20 minutes to 3 hours. How do you find the bottleneck?
- **Interview Answer:**
  1. **Check Input Data Volume & Skew:** Did row count grow 10x, or did one giant tenant account have 5,000,000 child records?
  2. **Check SQL Query Count & Execution Plan:** Did a lazy Hibernate relationship trigger an **N+1 query** (executing 1,000,000 `SELECT`s instead of 1 `JOIN`), or did PostgreSQL flip from an Index Scan to a Sequential Table Scan due to stale table statistics?
  3. **Check GC Thrashing / Hibernate Session Cache:** In batch jobs, if you don't call `entityManager.flush(); entityManager.clear();` every chunk (e.g., every 500 records), Hibernate's 1st-level Persistence Context caches all 1,000,000 entities in Old Gen until the JVM spends 95% of its time in Full GC!

#### 8. Frequent full GCs appear in a G1 service during peak traffic. What do the logs tell you, and what are the fix options (allocation reduction, heap sizing, humongous objects, IHOP, collector change)?
- **Simple Meaning (Dev-Friendly Question):** Why is G1 falling back to multi-second Full GCs during peak traffic, and how do you stop it?
- **Interview Answer:**
  - **What Logs Show:** Look in `-Xlog:gc*` right before `Pause Full`: you'll see **`To-space exhausted` (Evacuation Failure)** or **`Humongous allocation`**. That means your app is allocating/promoting objects into Old Gen faster than G1's background Concurrent Marking can free Old Gen regions!
  - **Fix Options:**
    1. **Fix Humongous Allocations:** If logs show `Humongous regions`, increase `-XX:G1HeapRegionSize=16m` or stream large `byte[]`/JSON payloads in code.
    2. **Start Concurrent Marking Earlier:** Lower `-XX:InitiatingHeapOccupancyPercent=30` (from default 45%) or increase `-XX:ConcGCThreads` so background marking finishes before Old Gen fills up.
    3. **Switch to Generational ZGC (`-XX:+UseZGC`)** or increase `-Xmx`.

#### 9. `OutOfMemoryError: unable to create native thread` appears under load. What are the causes (thread count, ulimit, container limits, stack size) and the fixes?
- **Simple Meaning (Dev-Friendly Question):** Why does `unable to create native thread` happen even when the JVM has plenty of free Heap memory?
- **Interview Answer:**
  - **Root Cause:** Every platform `new Thread()` calls Linux `pthread_create()`, which checks OS thread/process limits (`ulimit -u`, Kubernetes `pids.max`, `/proc/sys/kernel/threads-max`, and available unallocated native RAM).
  - **Usual Culprit in Code:** Someone called `Executors.newCachedThreadPool()` or `new Thread(...).start()` per request or created a new `HttpClient` / `ThreadPoolExecutor` inside a method without closing it—spawning 10,000 OS threads!
  - **Fix:** Fix the thread leak in code, replace unbounded thread creation with a **bounded `ThreadPoolExecutor`** or **Virtual Threads** (which don't create OS threads!), and verify Kubernetes `pidsLimit`.

#### 10. A new engineer used `parallelStream()` with blocking calls and the whole service slowed down. Explain what happened and how to fix it.
- **Simple Meaning (Dev-Friendly Question):** Why did one endpoint using `list.parallelStream().map(id -> httpClient.get(id))` freeze completely unrelated endpoints in the same JVM?
- **Interview Answer:**
  - **What Happened:** Every `parallelStream()` in the entire JVM shares **`ForkJoinPool.commonPool()`**, which only has `CPU Cores - 1` threads (e.g., 7 threads on an 8-core box) plus the calling thread!
  - Blocking all 7 common-pool threads on slow 2-second HTTP calls starved every other `parallelStream()` and default `CompletableFuture.supplyAsync()` across the whole service.
  - **How to Fix:** Never use `parallelStream()` for I/O! Use **`Executors.newVirtualThreadPerTaskExecutor()`** (or `StructuredTaskScope` + `Semaphore`) for parallel blocking I/O.

#### 11. A scheduled task silently stopped running. What could have swallowed the exception, and how do you make failures visible?
- **Simple Meaning (Dev-Friendly Question):** Why does `ScheduledExecutorService.scheduleAtFixedRate()` silently stop running forever if your task throws a `NullPointerException` once?
- **Interview Answer:**
  - **Why:** By design in `ScheduledThreadPoolExecutor`, if a periodic `Runnable` throws **any uncaught exception**, the `ScheduledFuture` marks itself as failed/completed and **cancels all future executions forever—without printing a single line to the logs** (the exception is trapped inside the `ScheduledFuture` that nobody called `.get()` on)!
  - **Fix:** Always wrap the top-level body of scheduled tasks in `try { ... } catch (Throwable t) { log.error("Scheduled task failed", t); }`, expose a Micrometer counter/last-success-timestamp metric (`scheduler.last.success.epoch`), and alert if the timestamp falls behind!

#### 12. A `HashMap` shared between threads caused data loss intermittently. How do you prove and fix it, and what testing would have caught it?
- **Simple Meaning (Dev-Friendly Question):** Why does sharing a `HashMap` across threads silently lose entries, and how do you fix and test it?
- **Interview Answer:**
  - **Why Entries Are Lost:** Two threads calling `map.put()` simultaneously can hash to the same bucket, read the same tail node pointer, and overwrite each other's node reference—or collide during `resize()` and drop an entire bucket's entries!
  - **Fix:** Replace `HashMap` with **`ConcurrentHashMap`** (or wrap an immutable `Map.copyOf(...)` in an `AtomicReference` if updated in bulk).
  - **How to Catch in Tests:** Run a multi-threaded stress test using `CountDownLatch` where 32 threads insert 10,000 keys concurrently and assert `map.size() == 320_000`.

#### 13. A service's startup time went from 20s to 90s. How do you investigate (class scanning, bean creation, network calls at startup, JIT, CDS)?
- **Simple Meaning (Dev-Friendly Question):** Your Spring Boot service used to start in 20s and now takes 90s (causing Kubernetes liveness probes to kill it). How do you find which bean or step is taking 70s?
- **Interview Answer:**
  1. **Capture a Startup JFR Profile:** Start the JVM with `-XX:StartFlightRecording=filename=startup.jfr,duration=90s` and enable **Spring Boot `BufferingApplicationStartup`** (`/actuator/startup` endpoint), which records the exact milliseconds spent initializing every single Spring Bean!
  2. **Usual Culprits:**
     - A `@PostConstruct` / `ApplicationRunner` bean making a synchronous network/DNS/DB call that hangs until timeout.
     - Wide `@ComponentScan("com")` or Hibernate `ddl-auto: validate` scanning thousands of tables over a slow network.
     - Linux `/dev/random` entropy starvation blocking `SecureRandom` (use `/dev/urandom`).
  3. **Speed Up:** Fix the blocking bean, unpack the layered JAR, and enable **AppCDS / Java 25 AOT Cache (`-XX:AOTCache`)**.

#### 14. Your team wants to adopt virtual threads for a high-traffic API. How do you evaluate benefits, risks (pinning, pool limits, thread-local usage), and roll out safely?
- **Simple Meaning (Dev-Friendly Question):** Give a practical rollout plan for enabling Virtual Threads on a high-traffic production API.
- **Interview Answer:**
  1. **Pre-Flight Audit:** Upgrade to **Java 24/25** (so `synchronized` doesn't pin) or audit libraries for `jdk.VirtualThreadPinned` JFR events on Java 21; remove heavy `ThreadLocal` object caches; add `Semaphore` bulkheads around downstream dependencies.
  2. **Load Test in Staging:** Simulate slow downstream I/O (`200ms` latency) at `5x` normal concurrency and verify p99 latency, Heap RSS, and DB pool wait times.
  3. **Canary Rollout:** Enable `spring.threads.virtual.enabled=true` via feature flag/config on **5% of production pods first**, compare metrics against platform-thread pods for 48 hours, then roll out to 100%.

#### 15. A library upgrade breaks at runtime with `NoSuchMethodError` or `ClassCastException` across modules. How do you find the dependency conflict (classpath, shading, class loader)?
- **Simple Meaning (Dev-Friendly Question):** Your code compiled cleanly in Maven, but crashes in production with `NoSuchMethodError`. How did that happen, and how do you find the conflicting JAR?
- **Interview Answer:**
  - **Root Cause ("Jar Hell" / Diamond Dependency Conflict):** Your code compiled against `library-v2.0.jar`, which has the new method—but another transitive dependency pulled in an older `library-v1.0.jar` earlier on the classpath, so Maven/JVM loaded the v1.0 class at runtime!
  - **How to Find & Fix It:**
    1. Run `mvn dependency:tree -Dverbose` to spot duplicate/conflicting transitive versions.
    2. At runtime, check `Class.getProtectionDomain().getCodeSource().getLocation()` (or `-Xlog:class+load=info`) to see the exact JAR file path the class was loaded from.
    3. Enforce **`maven-enforcer-plugin` (`<banDuplicateClasses/>` + `<requireUpperBoundDeps/>`)** in CI so version conflicts fail the build at compile time!

#### 16. JSON serialisation is the top CPU consumer in a profile. What options do you have (library settings, schema-based formats, caching, reducing payloads)?
- **Simple Meaning (Dev-Friendly Question):** Your flame graph shows 35% of total CPU is spent inside Jackson JSON serialization/deserialization. How do you cut that down?
- **Interview Answer:**
  1. **Check for Accidental `new ObjectMapper()` Creation:** Ensure `ObjectMapper` is a single shared Spring singleton—creating `new ObjectMapper()` per request rebuilds reflection caches every time and destroys CPU!
  2. **Enable Jackson `BlackbirdModule` (or use Java `record`s):** Replaces reflective field access with compiled `MethodHandle` / `LambdaMetafactory` calls (~20–30% faster).
  3. **Trim Payload Over-Fetching:** Use DTO projections so you only serialize the 5 fields the UI needs instead of a 60-field entity graph, and stream directly to `OutputStream` instead of `writeValueAsString()`.
  4. **For Internal Service-to-Service Calls:** Switch hot internal endpoints from JSON to **gRPC / Protobuf** (5x–10x less CPU).

#### 17. Metaspace keeps growing in an application server after redeploys. What is the likely cause and how do you fix it?
- **Simple Meaning (Dev-Friendly Question):** What causes a ClassLoader / Metaspace leak when redeploying apps or reloading plugins, and how do you fix it?
- **Interview Answer:**
  - **Cause:** A class stays in Metaspace as long as its **ClassLoader** is reachable. If an app/plugin spawns a background thread it forgets to stop, registers a JDBC driver/JVM shutdown hook, or leaves an object inside a server-thread `ThreadLocal`, that single reference keeps the **entire old WebAppClassLoader and all 10,000 of its classes** pinned in Metaspace forever!
  - **Fix:** Shut down all thread pools (`ExecutorService.close()`), deregister JDBC drivers/listeners, and call `ThreadLocal.remove()` in a shutdown hook (`ServletContextListener.contextDestroyed`).

#### 18. A service suddenly becomes slow for one tenant only. How would you use JFR, metrics, and logs to isolate it?
- **Simple Meaning (Dev-Friendly Question):** 99% of customers have 20ms responses, but one big enterprise tenant (`tenantId=acme`) has 8-second timeouts. How do you debug a single-tenant bottleneck?
- **Interview Answer:**
  1. **Structured Logs & OpenTelemetry Attributes:** Filter traces by `tenant.id = "acme"`. Check DB query spans (`db.statement`, rows returned) to see if `"acme"` has **data skew** (e.g., 500,000 orders while average tenants have 50) and is missing pagination or a composite index `(tenant_id, created_at)`!
  2. **Custom JFR Context Events:** Create a custom `@Name("app.TenantRequest") class TenantEvent extends jdk.jfr.Event` recording `tenantId` and `allocatedBytes`—in JMC, you can filter the CPU and Allocation Flame Graphs *specifically for threads executing `tenantId = "acme"`*!

#### 19. You must reduce the memory cost of 200 JVM services in Kubernetes. What do you change (heap sizing, compact headers, class sharing, smaller base images, GC choice, native image where appropriate)?
- **Simple Meaning (Dev-Friendly Question):** As a Principal Architect asked to cut your company's Kubernetes RAM bill across 200 Java microservices by 30–40%, what levers do you pull?
- **Interview Answer:**
  1. **Enable Java 25 Compact Object Headers (`-XX:+UseCompactObjectHeaders`) + String Deduplication (`-XX:+UseStringDeduplication`):** Cuts heap usage by **15–25%** across all 200 services with zero code changes!
  2. **Switch from Platform Thread Pools to Virtual Threads (`-Xss` savings):** Eliminates 200MB+ of pre-allocated OS thread stack memory per pod.
  3. **Right-Size Heap & Return Unused Heap to OS:** Tune `-XX:MaxRAMPercentage` based on actual Live Set + enable `-XX:+UseZGC` (with `-XX:ZUncommitDelay=60`) or G1 periodic GC (`-XX:G1PeriodicGCInterval=60000`) so idle pods uncommit unused heap pages back to Linux!
  4. **AppCDS / AOT Cache + Custom `jlink` Base Image:** Shares read-only class metadata across processes and shrinks container footprint.

#### 20. Time-related bugs appear only around DST changes and month ends. How do you find and prevent them (`java.time`, UTC storage, injected `Clock`, property-based tests)?
- **Simple Meaning (Dev-Friendly Question):** Why do billing and scheduling bugs always happen on March/November Daylight Saving Time Sundays or February 28/29, and how do you prevent them permanently?
- **Interview Answer:**
  - **Why They Happen:** Using `LocalDateTime.now()` (which uses the server's local timezone), adding `Duration.ofHours(24)` instead of `Period.ofDays(1)` across a 23-hour or 25-hour DST day, or using `plusMonths(1)` on Jan 31 without understanding how `LocalDate` clamps to Feb 28/29.
  - **How to Prevent Permanently:**
    1. Ban `LocalDateTime.now()` and `new Date()` via **ArchUnit / ErrorProne** rules.
    2. Store and compute machine timestamps strictly in **`Instant` (UTC)**, and use **`ZonedDateTime` + `Period`** when doing calendar math in a customer's timezone.
    3. Inject **`java.time.Clock`** everywhere and write parameterized JUnit tests covering exact edge timestamps (`2025-03-09T01:59:59` US DST spring-forward, `2024-02-29` leap year, `2025-01-31` month end).

---

## 15. Leadership and Design-Level Java Questions

#### 1. How do you decide which JDK version and distribution your organisation standardises on, and how do you keep 100 services from falling behind?
- **Simple Meaning (Dev-Friendly Question):** As a Staff/Principal Engineer, how do you standardize the JDK across 100 microservices and prevent teams from getting stuck on old Java versions?
- **Interview Answer:**
  - **1. Standardize on LTS Releases (`Java 21` -> `Java 25`) + One TCK-Verified Base Image:** Publish a single golden Docker base image (`Eclipse Temurin` or `Amazon Corretto`) with pre-configured `JAVA_TOOL_OPTIONS`, CA certificates, and JFR/OTel agents.
  - **2. Automate Upgrades at Scale:** Use **OpenRewrite** + **Renovate/Dependabot** to automatically open PRs that upgrade Java & Spring Boot versions, migrate deprecated APIs (`javax -> jakarta`), and run the full Testcontainers suite.
  - **3. Support Policy:** Maintain at most **2 active LTS versions** across the company (`N` and `N-1`).

#### 2. How do you run a performance review culture (budgets, benchmarks in CI, load tests, regressions caught before release)?
- **Simple Meaning (Dev-Friendly Question):** How do you make sure performance regressions are caught *before* code reaches production instead of during an outage?
- **Interview Answer:**
  - **1. Explicit Latency & Memory Budgets:** Every API endpoint has a documented SLO (`p99 < 80ms`, `0 N+1 queries`).
  - **2. SQL Query Count Assertions in Integration Tests:** Use `DatasourceProxy` / `QuickPerf` in JUnit tests to assert `assertThat(sqlSelectCount).isLessThanOrEqualTo(2)` so accidental N+1 queries fail CI immediately!
  - **3. Automated Nightly/Release Gate Load Tests:** Run k6/Gatling against staging with JFR recording enabled and block promotion if p99 latency or Old Gen allocation regresses by `> 10%`.

#### 3. How do you review Java code for concurrency bugs and what checklist do you use?
- **Simple Meaning (Dev-Friendly Question):** When reviewing a PR that touches threads, caches, or async code, what is your mental checklist?
- **Interview Answer:**
  1. **Shared Mutable State:** Did someone add a non-`final` field or plain `HashMap`/`ArrayList` to a singleton Spring `@Service`?
  2. **Check-Then-Act Race:** Did someone write `if (!map.containsKey(k)) map.put(k, v)` instead of atomic `map.computeIfAbsent(k, ...)`?
  3. **Blocking on Default Pool:** Did someone call `CompletableFuture.supplyAsync(...)` without a custom `Executor` or use `parallelStream()` for I/O?
  4. **Unbounded Resources:** Are all queues (`ArrayBlockingQueue`), caches (`Caffeine`), and concurrency limits (`Semaphore`) strictly bounded?
  5. **Context Cleanup:** Are `ThreadLocal` variables always cleaned up in `finally` (or replaced with `ScopedValue`)?

#### 4. How do you coach a team away from over-engineering (unneeded patterns, premature optimisation, reactive everywhere)?
- **Simple Meaning (Dev-Friendly Question):** How do you stop developers from adding unnecessary abstractions, complex reactive code, or premature micro-optimizations?
- **Interview Answer:**
  - **1. Require Evidence Before Optimization:** Ask *"Show me the JFR flame graph or load-test metric that proves this is a bottleneck."*
  - **2. Default to Boring, Readable Java 21/25:** Favor **Virtual Threads + plain synchronous Spring MVC + immutable `record`s** over complex WebFlux reactive chains unless streaming back-pressure is explicitly required.
  - **3. Rule of Three for Abstractions:** Don't create a generic `AbstractStrategyFactoryBuilder<T>` for a single implementation—wait until you have at least 2–3 real domain implementations.

#### 5. When do you pick Java over Go, Kotlin, or Node for a new service, and what are Java's current strengths and weaknesses?
- **Simple Meaning (Dev-Friendly Question):** Give an honest, balanced comparison of when Java 21/25 is the best choice vs Go, Kotlin, or Node.js/TypeScript.
- **Interview Answer:**
  - **Java 21/25 Strengths:** Unmatched enterprise ecosystem (Spring Boot, Kafka, gRPC, Spring AI), world-class JIT peak throughput, sub-millisecond Generational ZGC, millions of Virtual Threads, `record`/pattern-matching conciseness, and best-in-class production diagnostics (`JFR`, `async-profiler`).
  - **Java Weaknesses (and How They're Closing):** Higher baseline memory footprint and slower cold start than Go (though Java 25 Compact Object Headers, AOT Cache, and GraalVM Native Image narrow the gap significantly).
  - **When to Pick What:** Pick **Java 21/25** for complex domain microservices, high-throughput event streaming, and financial/enterprise platforms; pick **Go** for tiny CLI tools, Kubernetes operators, or ultra-lightweight network proxies; pick **Node/TypeScript** for Next.js BFFs sharing frontend types.

#### 6. How do you manage technical debt in a long-lived Java codebase (deprecation policy, test-first refactoring, strangler approach, build and dependency hygiene)?
- **Simple Meaning (Dev-Friendly Question):** How do you modernize a 7-year-old legacy Java codebase without halting feature delivery?
- **Interview Answer:**
  - **1. Enforce Architectural Guardrails First (ArchUnit):** Add **ArchUnit** tests to CI so new code cannot introduce new layer violations or circular dependencies ("stop the bleeding").
  - **2. Characterization / Testcontainers Baseline:** Write high-level integration tests against real DB/Kafka containers before refactoring internals.
  - **3. Strangler Fig & Incremental Slices:** Carve out one bounded context or module at a time behind a clean interface/feature flag, and allocate a fixed **15–20% engineering capacity** per sprint to continuous debt paydown and automated OpenRewrite upgrades.

#### 7. How do you design a shared library so that consumers can upgrade independently (API stability, semantic versioning, `@Deprecated(forRemoval)`, binary compatibility)?
- **Simple Meaning (Dev-Friendly Question):** Why do shared internal `.jar` libraries often become a nightmare for microservice teams, and how do you design them safely?
- **Interview Answer:**
  - **1. Keep Shared Libraries Thin (Cross-Cutting Only):** Only put technical plumbing (observability, security filters, error handling) in shared libraries—**never shared business domain logic**!
  - **2. Zero Transitive Dependency Leakage:** Mark internal dependencies `<optional>true</optional>` or `<scope>provided</scope>`, and provide a **Maven BOM (`<dependencyManagement>`)** aligned with Spring Boot.
  - **3. Strict Binary & Source Compatibility:** Never remove or change a public method signature in a minor release—add a new overloaded/default method, annotate the old one with `@Deprecated(since = "2.1", forRemoval = true)`, and verify binary compatibility in CI with **`japicmp` / `revapi`**.

#### 8. How do you evaluate a new Java feature for adoption (preview vs final, ecosystem support, tooling, team readiness, rollback plan)?
- **Simple Meaning (Dev-Friendly Question):** Should you enable `--enable-preview` features in production, and how do you roll out new Java features across a team?
- **Interview Answer:**
  - **Never Enable `--enable-preview` in Production Critical Services:** Preview features can change API signatures in the next 6-month JDK release and lock you into a single non-LTS JDK version. Use Preview features (`StructuredTaskScope`, `StableValue`) in sandbox/POC branches to prepare, and adopt **Finalized features** (`Virtual Threads`, `Record Patterns`, `ScopedValue`, `Stream Gatherers`, `Compact Object Headers`) in production once your IDE, SpotBugs/Sonar, and build tools support them.

#### 9. How do you set coding standards for exceptions, logging, nullability, immutability, and concurrency across teams?
- **Simple Meaning (Dev-Friendly Question):** How do you enforce Java coding standards automatically so reviewers don't waste time arguing on PRs?
- **Interview Answer:**
  - **Automate Everything in the Build Pipeline (Don't Rely on Wiki Docs):**
    - **Formatting:** `Spotless` (`google-java-format` / Palantir).
    - **Nullability:** **`JSpecify` (`@NullMarked` at package level)** + **`NullAway` / `ErrorProne`** failing the compile on unguarded nulls!
    - **Architecture & Immutability:** **`ArchUnit`** tests enforcing 4-layer architecture (`domain` has zero framework imports, DTOs are `record`s, controllers never return `@Entity`/`@Document`).
    - **Exceptions & Logging:** Standardize on RFC 7807 `ProblemDetail` and structured JSON logging with OpenTelemetry trace correlation.

#### 10. How do you onboard and mentor engineers on JVM internals and production troubleshooting (runbooks, game days, post-mortems)?
- **Simple Meaning (Dev-Friendly Question):** How do you train mid-level developers to become confident debugging JVM memory leaks, thread dumps, and GC pauses?
- **Interview Answer:**
  - **1. Actionable 1-Page Runbooks:** Every alert links directly to a runbook with the exact copy-paste `jcmd` commands (`JFR.start`, `Thread.print`, `VM.native_memory`).
  - **2. "Break-and-Fix" Game Days:** Run quarterly chaos exercises in staging where we intentionally inject a `ThreadLocal` memory leak, a lock convoy, or a HikariCP leak, and let engineers diagnose it live using JFR and Eclipse MAT.
  - **3. Blameless Post-Mortems with Flame Graphs:** Review real production flame graphs and GC logs together as a team after every incident.

#### 11. Describe the most difficult production incident you handled in a Java system, what you learned, and what you changed afterwards.
- **Simple Meaning (Dev-Friendly Question):** How do you structure a compelling STAR story for a hard Java production outage in a senior/staff interview?
- **Interview Answer:**
  - **Structure to Use in Your Interview (STAR + Engineering Depth):**
    - **Symptom:** E.g., *"During peak traffic, pods started failing health checks and getting `OOMKilled (Exit 137)` even though Grafana showed Java Heap was at 55%."*
    - **Investigation:** *"Took thread dumps + enabled Native Memory Tracking (`-XX:NativeMemoryTracking=detail`). Discovered two interacting issues: an external partner slowdown caused retries on an improperly configured `HttpClient` / `GZIPInputStream` wrapper that leaked native off-heap buffers and pinned carrier threads."*
    - **Fix & Long-Term Prevention:** *"Fixed the resource lifecycle with `try-with-resources`, added Resilience4j `Semaphore` bulkheads + strict 1.5s read timeouts, capped `-XX:MaxDirectMemorySize`, and added a 4-hour soak test in CI."*

#### 12. How do you balance delivery pressure and quality when a deadline conflicts with a needed refactor or upgrade?
- **Simple Meaning (Dev-Friendly Question):** When Product wants a feature shipped Friday, but the code you need to touch is a fragile mess, how do you handle it?
- **Interview Answer:**
  - **Separate Non-Negotiable Safety Risks from Nice-to-Have Cleanups:**
    - **Never Compromise On:** Security, data integrity, idempotency, timeouts, and observability (metrics/logs) for the new path.
    - **Pragmatic Compromise:** Instead of boiling the ocean with a 3-week rewrite before shipping, isolate the new feature behind a **clean interface / adapter boundary** (using the **Branch by Abstraction / Strangler pattern**) with strong integration tests, ship the business value on time, and schedule the legacy adapter migration as a tracked follow-up milestone.

---

## 16. Rapid-Fire Questions

#### 1. What is the difference between JDK, JRE, and JVM?
- **Simple Meaning (Dev-Friendly Question):** Define JVM, JRE, and JDK in one sentence each.
- **Interview Answer:** **JVM** runs `.class` bytecode; **JRE** = JVM + standard libraries (`java.base`); **JDK** = JRE + compiler & diagnostic tools (`javac`, `jcmd`, `jfr`).

#### 2. Is Java pass-by-value or pass-by-reference?
- **Simple Meaning (Dev-Friendly Question):** Does Java ever pass variables by reference?
- **Interview Answer:** **100% Pass-by-Value.** For primitives, it copies the value; for objects, it copies the **memory address (reference)** by value—so you can mutate the object's fields, but reassigning `param = new Obj()` never changes the caller's variable.

#### 3. What is the default value of an `int`, `boolean`, and object reference field?
- **Simple Meaning (Dev-Friendly Question):** What values do class fields get if you don't initialize them?
- **Interview Answer:** `int = 0`, `boolean = false`, object reference = `null` (Note: local variables inside a method get **no default value** and fail to compile if read before assignment).

#### 4. Can you override a `static` method? A `private` method?
- **Simple Meaning (Dev-Friendly Question):** Why doesn't runtime polymorphism work on `static` or `private` methods?
- **Interview Answer:** **No to both.** `static` methods belong to the class (defining the same signature in a subclass only **hides** it, resolved at compile time), and `private` methods are invisible to subclasses.

#### 5. What is the difference between `==` and `equals`?
- **Simple Meaning (Dev-Friendly Question):** What does `==` check vs `.equals()`?
- **Interview Answer:** `==` compares **primitive values** or **object memory addresses (reference identity)**; `.equals()` compares **logical content equality** (unless not overridden, where `Object.equals()` defaults to `==`).

#### 6. What does `final` mean on a class, method, and variable?
- **Simple Meaning (Dev-Friendly Question):** What are the 3 uses of the `final` keyword?
- **Interview Answer:** **`final class`** cannot be subclassed; **`final method`** cannot be overridden; **`final variable`** can only be assigned once (its reference cannot change, though the object it points to can still be mutated if not immutable).

#### 7. Why is `String` immutable?
- **Simple Meaning (Dev-Friendly Question):** Give the 4 reasons `String` is immutable.
- **Interview Answer:** (1) Safe **String Pool** memory sharing, (2) **Security** (file paths/URLs/hosts can't be mutated after validation), (3) **Thread-safety** without locks, and (4) **`hashCode()` caching** for fast `HashMap` lookups.

#### 8. `StringBuilder` vs `StringBuffer`?
- **Simple Meaning (Dev-Friendly Question):** Which mutable string class should you use?
- **Interview Answer:** Always use **`StringBuilder`** (fast, unsynchronized, for single-threaded use). **`StringBuffer`** synchronizes every method unnecessarily and is obsolete legacy code.

#### 9. Which collection would you use for a unique, ordered set? A FIFO queue? A priority queue?
- **Simple Meaning (Dev-Friendly Question):** Name the best Java collection class for those 3 data structures.
- **Interview Answer:**
  - **Unique + insertion-ordered set:** `LinkedHashSet` (or `TreeSet` for sorted order).
  - **FIFO Queue:** `ArrayDeque` (faster and less memory than `LinkedList`).
  - **Priority Queue:** `PriorityQueue` (binary min-heap).

#### 10. What is the initial capacity and load factor of `HashMap`?
- **Simple Meaning (Dev-Friendly Question):** What are `HashMap`'s default numbers?
- **Interview Answer:** Default initial capacity = **`16`** buckets; default load factor = **`0.75f`** (resizes to `32` when entry count exceeds `16 * 0.75 = 12`).

#### 11. At what chain length does `HashMap` treeify a bucket?
- **Simple Meaning (Dev-Friendly Question):** When does a `HashMap` bucket switch from a LinkedList to a Red-Black Tree?
- **Interview Answer:** When a single bucket reaches **`8` nodes (`TREEIFY_THRESHOLD = 8`) AND** total map capacity is at least **`64` (`MIN_TREEIFY_CAPACITY = 64`)** (otherwise it just resizes the array). It converts back to a list at **`6` nodes**.

#### 12. How does `ConcurrentHashMap` achieve thread safety in Java 8+?
- **Simple Meaning (Dev-Friendly Question):** How does `ConcurrentHashMap` avoid locking the whole map?
- **Interview Answer:** Uses **lock-free `volatile` reads** (`get()` never locks!), **CAS** for inserting into an empty bucket, and fine-grained **`synchronized` on only the first node (head) of a single bucket** when a collision occurs.

#### 13. What is the difference between fail-fast and fail-safe iterators?
- **Simple Meaning (Dev-Friendly Question):** Why does `ArrayList`'s iterator throw `ConcurrentModificationException` while `ConcurrentHashMap`'s does not?
- **Interview Answer:** **Fail-fast** (`ArrayList`, `HashMap`) checks `modCount` and throws `ConcurrentModificationException` if modified during iteration; **Weakly consistent / Fail-safe** (`ConcurrentHashMap`, `CopyOnWriteArrayList`) iterates over live/snapshot state without throwing `CME`.

#### 14. What is the difference between `sleep()` and `wait()`?
- **Simple Meaning (Dev-Friendly Question):** Which one releases the lock: `Thread.sleep()` or `Object.wait()`?
- **Interview Answer:** **`Object.wait()` releases the monitor lock** and parks until `notify()`/`notifyAll()`; **`Thread.sleep()` keeps holding all locks** while pausing execution.

#### 15. What does `volatile` guarantee?
- **Simple Meaning (Dev-Friendly Question):** What 2 things does `volatile` guarantee, and what does it NOT guarantee?
- **Interview Answer:** Guarantees **Visibility** (reads/writes go straight to main memory) and **Ordering** (prevents instruction reordering via memory barriers), but does **NOT guarantee atomicity** for compound operations like `count++`.

#### 16. What is a daemon thread?
- **Simple Meaning (Dev-Friendly Question):** How does a Daemon Thread differ from a User Thread when the JVM shuts down?
- **Interview Answer:** A background housekeeping thread (like GC threads; Virtual Threads are always daemon threads). The JVM **exits immediately once all non-daemon (user) threads finish**, without waiting for daemon threads or running their `finally` blocks!

#### 17. What is the difference between `submit()` and `execute()`?
- **Simple Meaning (Dev-Friendly Question):** On `ExecutorService`, when do you call `execute()` vs `submit()`?
- **Interview Answer:** `execute(Runnable)` is fire-and-forget (`void`) and sends uncaught exceptions to `UncaughtExceptionHandler`; `submit(Callable/Runnable)` returns a **`Future<T>`** and traps any exception inside the `Future` until `.get()` is called.

#### 18. What is a virtual thread?
- **Simple Meaning (Dev-Friendly Question):** Define a Java 21+ Virtual Thread in one sentence.
- **Interview Answer:** A lightweight, JVM-managed thread stored on the heap (~1KB) that automatically unmounts from its OS carrier thread whenever it hits blocking I/O, allowing millions of concurrent threads.

#### 19. What is the maximum number of threads a JVM can create?
- **Simple Meaning (Dev-Friendly Question):** What caps how many threads you can create in Java?
- **Interview Answer:**
  - **Platform Threads:** Capped by OS limits (`ulimit -u`, `pids.max`, `/proc/sys/kernel/threads-max`) and native RAM (`Threads * -Xss`, typically **a few thousand**).
  - **Virtual Threads:** Capped only by available Java Heap memory (**millions**).

#### 20. What is Metaspace?
- **Simple Meaning (Dev-Friendly Question):** What is stored in Metaspace and where does it live?
- **Interview Answer:** The **native (off-heap) memory region** (replacing Java 7 PermGen) that stores loaded **class metadata, method bytecode, and constant pools** (cap it with `-XX:MaxMetaspaceSize`).

#### 21. What are GC roots?
- **Simple Meaning (Dev-Friendly Question):** Name the starting points the Garbage Collector uses to find live objects.
- **Interview Answer:** (1) Local variables on active **Thread Stacks**, (2) **`static` fields** of loaded classes, (3) Active **`Thread` objects**, and (4) **JNI Global References**.

#### 22. Which GC is the default in Java 8? In Java 9+?
- **Simple Meaning (Dev-Friendly Question):** What is the default Garbage Collector in Java 8 vs Java 9 through 25?
- **Interview Answer:** **Java 8:** `Parallel GC` (`-XX:+UseParallelGC`). **Java 9 through 25:** `G1 GC` (`-XX:+UseG1GC`).

#### 23. What causes `StackOverflowError`?
- **Simple Meaning (Dev-Friendly Question):** What causes a `StackOverflowError`?
- **Interview Answer:** A thread's call stack exceeds its `-Xss` limit—almost always caused by **infinite/unbounded recursion** (e.g., bidirectional `toString()` or JSON serialization cycle `A -> B -> A`).

#### 24. What causes `OutOfMemoryError: Metaspace`?
- **Simple Meaning (Dev-Friendly Question):** Why would Metaspace run out of memory?
- **Interview Answer:** Either `-XX:MaxMetaspaceSize` is set too small for the app's libraries, or there is a **ClassLoader / dynamic class generation leak** (creating new CGLIB/ByteBuddy proxies, `ObjectMapper`s, or groovy scripts repeatedly at runtime).

#### 25. What is the difference between `ClassNotFoundException` and `NoClassDefFoundError`?
- **Simple Meaning (Dev-Friendly Question):** Distinguish `ClassNotFoundException` vs `NoClassDefFoundError` in one sentence.
- **Interview Answer:** **`ClassNotFoundException`** is a checked exception when dynamic `Class.forName("X")` can't find `"X"` on the classpath; **`NoClassDefFoundError`** is an `Error` when a class existed at compile time (or failed its `static {}` initializer earlier) and cannot be linked at runtime.

#### 26. What is the effect of `System.gc()`?
- **Simple Meaning (Dev-Friendly Question):** What happens when code calls `System.gc()`?
- **Interview Answer:** It is a hint asking the JVM to run a **Full Garbage Collection** (which causes a painful latency spike in production—always block it with `-XX:+DisableExplicitGC`).

#### 27. What is type erasure?
- **Simple Meaning (Dev-Friendly Question):** What happens to generic types `<T>` when Java compiles your code?
- **Interview Answer:** The compiler checks generic types at compile time, then **erases `<T>` to `Object` (or its upper bound, like `Comparable`)** and inserts casts in the bytecode, so `List<String>` and `List<Integer>` are both just raw `List` at runtime.

#### 28. What is the difference between `Comparable` and `Comparator`?
- **Simple Meaning (Dev-Friendly Question):** When do you implement `Comparable` vs passing a `Comparator`?
- **Interview Answer:** **`Comparable<T>` (`compareTo`)** lives *inside* the class to define its **single natural ordering**; **`Comparator<T>` (`compare`)** is an *external* strategy (`Comparator.comparing(...)`) used to sort by any custom field or order.

#### 29. What is a functional interface?
- **Simple Meaning (Dev-Friendly Question):** What makes an interface a Functional Interface?
- **Interview Answer:** An interface with **exactly ONE abstract method (SAM)** (annotated with `@FunctionalInterface`), allowing it to be implemented by a **Lambda (`x -> x + 1`)** or **Method Reference (`String::length`)**.

#### 30. What is the difference between `map` and `flatMap`?
- **Simple Meaning (Dev-Friendly Question):** When do you use `map()` vs `flatMap()`?
- **Interview Answer:** **`map` (1-to-1)** transforms each element (`User -> String`); **`flatMap` (1-to-Many or unwrapping)** transforms each element into a `Stream`/`Optional` and **flattens** them into a single stream (`Stream<List<Item>> -> Stream<Item>`).

#### 31. Name the new Java 25 language features.
- **Simple Meaning (Dev-Friendly Question):** What are the headline features in Java 25 (LTS)?
- **Interview Answer:**
  - **Finalized in Java 25:** **Flexible Constructor Bodies** (statements before `super()`, JEP 513), **Compact Source Files & Instance `main`** (`void main()`, `IO.println()`, JEP 512), **Module Import Declarations** (`import module java.base;`, JEP 511), **`ScopedValue`** (JEP 506), **Compact Object Headers** (JEP 519), **Generational Shenandoah GC** (JEP 521), **Key Derivation Function API** (JEP 510), and **AOT Method Profiling** (JEP 515).
  - **Preview in Java 25:** **Primitive Types in Patterns/`switch`** (JEP 507), **Structured Concurrency** (JEP 505), and **Stable Values** (JEP 502).

#### 32. Which Java versions are LTS?
- **Simple Meaning (Dev-Friendly Question):** List all Long-Term Support (LTS) Java versions from Java 8 to Java 25.
- **Interview Answer:** **Java 8, Java 11, Java 17, Java 21, and Java 25** (released every 2 years since Java 17).

#### 33. What is a record, and what is a sealed class?
- **Simple Meaning (Dev-Friendly Question):** Define `record` and `sealed` class in one sentence each.
- **Interview Answer:** A **`record`** is an immutable, final data carrier that auto-generates `equals`, `hashCode`, `toString`, and accessors; a **`sealed` class/interface** restricts which fixed set of classes (`permits A, B`) can extend or implement it, enabling exhaustive `switch` expressions.

#### 34. What is the diamond problem and how does Java resolve it with default methods?
- **Simple Meaning (Dev-Friendly Question):** What happens if a class implements two interfaces `A` and `B` that both define `default void hello()`?
- **Interview Answer:** **Compile error!** Java forces the implementing class to explicitly override `hello()` and choose which parent implementation to call using **`A.super.hello()`** (or provide its own).

#### 35. What is a memory leak in Java, and give three common causes?
- **Simple Meaning (Dev-Friendly Question):** How can a garbage-collected language like Java have a memory leak, and what are the top 3 causes?
- **Interview Answer:** A Java memory leak happens when **objects that are no longer needed remain reachable from a GC Root**, preventing the GC from freeing them. **Top 3 causes:**
  1. Unbounded `static` collections or caches (`HashMap` without eviction).
  2. Uncleaned `ThreadLocal` variables on pooled worker threads (missing `.remove()`).
  3. Unclosed resources (`Files.lines()`, `InputStream`, DB `Connection`) or unregistered event listeners.

---

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
