# Head First Redis & Spring Boot Caching ⚡
*(Aapka Visual, Practical, aur Step-by-Step Deep-Dive Guide)*

> *"Database hamara permanent cold storage (tijori) hai, aur Redis hamara high-speed kitchen counter. Agar har chammach lene basement ki tijori me jaoge, toh restaurant me customer bhookha mar jayega!"*

---

## 🧭 Visual Roadmap: Hum Kya Seekhne Wale Hain?

```
┌────────────────────────────────────────────────────────────────────────┐
│                        VISUAL LEARNING ROADMAP                         │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 1: The Kitchen Counter Metaphor (Kyu Chahiye Redis?)          │
│             - RAM vs Disk Speed                                        │
│             - Single-Threaded Event Loop ka Magic                      │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 2: The Core Wiring (Spring Boot + Lettuce + Serializers)      │
│             - application.yml Configuration                            │
│             - \xac\xed\x00\x05 ka Horror (JDK vs JSON Serializer)      │
│             - Production-Grade RedisConfig.java                        │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 3: Imperative / Low-Level RedisTemplate Operations            │
│             - SET, GET, DELETE, TTL Inspection                         │
│             - Atomic SET NX EX (Distributed Lock / Idempotency Key)    │
│             - HSET, HGET, HGETALL, HDEL (Redis Hashes)                 │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 4: The Spring Declarative Cache Engine (AOP Magic)            │
│             - @EnableCaching & Custom RedisCacheManager                │
│             - Per-Cache Custom TTL Configuration                       │
│             - @Cacheable, @CachePut, @CacheEvict, @Caching             │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 5: The AOP Self-Invocation Trap (Kyu Cache Miss Hua?)         │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 6: Production Disasters (Penetration, Avalanche, Breakdown)   │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 7: Sharpen Your Pencil ✏️ & Architect's Cheat Sheet           │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 🍳 Chapter 1: The Kitchen Counter Metaphor

Imagine karo aap ek 5-star restaurant ke Head Chef ho:

```
┌────────────────────────────────┐         ┌────────────────────────────────┐
│   REDIS (Kitchen Counter/RAM)  │         │   DATABASE (Basement Freezer)  │
├────────────────────────────────┤         ├────────────────────────────────┤
│ • Haath badhaya aur mil gaya   │         │ • Lift se basement jao         │
│ • Access time: ~100 Microsecs  │         │ • Chabi dhoondo, taala kholo   │
│ • Sub-millisecond latency      │         │ • Access time: ~15-50 Millisecs│
│ • Volatile (Bijli gayi toh saaf)│        │ • Permanent Disk Persistence   │
└────────────────────────────────┘         └────────────────────────────────┘
```

### Redis Itna Fast Kyu Hai?
1. **In-Memory RAM**: Sara active data RAM me rehta hai. RAM access speed CPU registers ke sabse kareeb hoti hai.
2. **Single-Threaded Reactor Event Loop**:
   - Redis commands ko execute karne ke liye **Single Execution Thread** use karta hai (`I/O Multiplexing` via `epoll`/`kqueue`).
   - Iska sabse bada fayda: **No Thread Context Switching** aur **No Race Conditions / Lock Contention**.
   - Har single command (`SET`, `INCR`, `HSET`) **guaranteed atomic** hoti hai!
3. **C-Optimized Data Structures**: Skiplists, ziplists, hashtables jo direct memory efficiency par tuned hain.

---

## 🔌 Chapter 2: The Core Wiring (Spring Boot + Lettuce)

Spring Boot by default **Lettuce** driver use karta hai (jo Netty non-blocking architecture par based hai).

### 2.1 Dependencies (`pom.xml`)
```xml
<!-- 1. Spring Data Redis Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- 2. Spring Cache Abstraction Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- 3. Connection Pool for Lettuce (Mandatory for high concurrency) -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

---

### 2.2 External Configuration (`application.yml`)
```yaml
spring:
  cache:
    type: redis               # Spring Cache Abstraction ko bolo Redis use kare
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms         # Socket timeout
      lettuce:
        pool:
          max-active: 16      # Max simultaneous connections
          max-idle: 8         # Ready connections to keep warm
          min-idle: 2
          max-wait: 1000ms    # Fail fast if pool is full
```

---

### 2.3 The `\xac\xed\x00\x05` Horror (JDK vs JSON Serializer)

> [!CAUTION]
> Agar aap custom `RedisTemplate` bean nahi banate, toh Spring default Java Serialization (`JdkSerializationRedisSerializer`) use karta hai.
> 
> **Nateeja (The Horror):**
> Redis CLI me jab aap `KEYS *` chalaoge toh key dikhegi:
> `"\xac\xed\x00\x05t\x00\x07user:101"`
> Na human read kar sakta hai, na doosri language (Node/Python/Go) parse kar sakti hai, aur class ka `serialVersionUID` badalte hi Deserialization Crash ho jata hai!

#### ✅ The Fix: Production-Grade `RedisConfig.java`

```java
package com.learning.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory, 
            ObjectMapper objectMapper) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 1. Keys ko readable UTF-8 String serialize karo
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 2. ObjectMapper me polymorphic type info enable karo taaki generic JSON 
        // deserialize hote time exact Java class me map ho sake
        ObjectMapper redisObjectMapper = objectMapper.copy();
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 3. Values ko clean JSON format me store karo
        GenericJackson2JsonRedisSerializer jsonSerializer = 
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

---

## 🛠️ Chapter 3: Imperative Operations (RedisTemplate)

Jab hume programmatic control chahiye hota hai (jaise distributed lock, rate limiting, ya hash mutations), hum `RedisTemplate` use karte hain.

```
       ┌─────────────────────────────────────────────────────────────┐
       │                       RedisTemplate                         │
       └──────────────────────────────┬──────────────────────────────┘
                                      │
              ┌───────────────────────┴───────────────────────┐
              ▼                                               ▼
┌───────────────────────────┐                   ┌───────────────────────────┐
│     opsForValue()         │                   │       opsForHash()        │
│   (Strings, Key-Value)    │                   │   (Mini-HashMaps inside   │
│   SET, GET, INCR, SETNX   │                   │    single Redis key)      │
└───────────────────────────┘                   └───────────────────────────┘
```

### 3.1 Basic Value Operations (GET, SET, TTL, DELETE)

```java
@Service
public class RedisCommandService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCommandService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * SET with TTL (Time To Live)
     * CLI: SET user:101 '{"name":"Akshat"}' EX 300
     */
    public void setWithTtl(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * GET Value
     * CLI: GET user:101
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * CHECK REMAINING TTL (in seconds)
     * CLI: TTL user:101
     * Output:
     *   > 0 : Seconds bache hain
     *   -1  : Key exist karti hai par koi Expiry (TTL) nahi lagi
     *   -2  : Key exist hi nahi karti ya expire ho chuki hai
     */
    public long getTtl(String key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null ? expire : -2;
    }

    /**
     * UPDATE / EXTEND TTL on an existing key
     * CLI: EXPIRE user:101 600
     */
    public boolean refreshTtl(String key, Duration newTtl) {
        Boolean success = redisTemplate.expire(key, newTtl);
        return Boolean.TRUE.equals(success);
    }

    /**
     * DELETE Key
     * CLI: DEL user:101
     */
    public boolean delete(String key) {
        Boolean deleted = redisTemplate.delete(key);
        return Boolean.TRUE.equals(deleted);
    }
}
```

---

### 3.2 The Billion-Dollar Command: `SET NX EX` (Distributed Lock & Idempotency)

> [!IMPORTANT]
> **What is `SET NX EX`?**
> - `NX` = **Set if Not eXists** (Agar key pehle se hai toh fail ho jao).
> - `EX` = **Set Expiration in seconds** (Deadlock prevention agar process crash ho jaye).
>
> In Spring Data Redis, iska method hota hai: **`opsForValue().setIfAbsent(key, value, duration)`**.

#### Real Example from our Banking Service (`IdempotencyService.java`):
Customer ne transfer button 2 baar fast click kar diya. Double debit kaise rokein?

```java
@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "idempotency:transfer:";
    private static final Duration LOCK_TIMEOUT = Duration.ofMinutes(2);

    public boolean acquireLock(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;

        // ATOMIC OPERATION: Agar pehli baar aaya toh key create hogi aur true milega.
        // Agar doosri duplicate request turant aayi toh ye FALSE dega!
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, "PROCESSING", LOCK_TIMEOUT);

        return Boolean.TRUE.equals(acquired);
    }

    public void releaseLock(String idempotencyKey) {
        redisTemplate.delete(KEY_PREFIX + idempotencyKey);
    }
}
```

---

### 3.3 Redis Hash Operations (`HSET`, `HGET`, `HDEL`)

#### Kyu use karein Hash instead of serialized string?
Agar aapke paas ek user session hai:
`{ "userId": "usr_101", "role": "ADMIN", "loginCount": 4 }`
- **String approach**: Agar sirf `loginCount` badhana hai, toh poora JSON fetch karo $\rightarrow$ parse karo $\rightarrow$ increment karo $\rightarrow$ wapas serialize karo $\rightarrow$ save karo. (Slow & Race-condition prone).
- **Hash approach**: Sirf `HINCRBY session:usr_101 loginCount 1` call karo. Single field in-place update ho jata hai!

```java
@Service
public class RedisHashService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisHashService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * HSET: Set single or multiple fields inside a Hash
     * CLI: HSET session:usr_101 userId "usr_101" role "ADMIN"
     */
    public void setSessionField(String sessionId, String field, Object value) {
        String key = "session:" + sessionId;
        redisTemplate.opsForHash().put(key, field, value);
    }

    public void setSessionAll(String sessionId, Map<String, Object> map, Duration ttl) {
        String key = "session:" + sessionId;
        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key, ttl); // Hash keys par TTL key-level par lagta hai!
    }

    /**
     * HGET: Get single field value
     * CLI: HGET session:usr_101 role
     */
    public Object getSessionField(String sessionId, String field) {
        return redisTemplate.opsForHash().get("session:" + sessionId, field);
    }

    /**
     * HGETALL: Get entire map
     * CLI: HGETALL session:usr_101
     */
    public Map<Object, Object> getAllSessionFields(String sessionId) {
        return redisTemplate.opsForHash().entries("session:" + sessionId);
    }

    /**
     * HINCRBY: Atomically increment a numeric field
     * CLI: HINCRBY session:usr_101 loginCount 1
     */
    public Long incrementLoginCount(String sessionId) {
        return redisTemplate.opsForHash().increment("session:" + sessionId, "loginCount", 1);
    }

    /**
     * HDEL: Delete specific field from hash
     * CLI: HDEL session:usr_101 tempToken
     */
    public Long deleteField(String sessionId, String field) {
        return redisTemplate.opsForHash().delete("session:" + sessionId, field);
    }
}
```

---

## 🎩 Chapter 4: The Spring Declarative Cache Engine

Spring Boot provides high-level annotations jo method execution ke aage **AOP Proxy** khada kar deti hain. Code me business logic rehti hai aur caching automatic ho jaati hai!

### 4.1 Step 1: Enable Caching
Main application class par `@EnableCaching` lagayein:
```java
@SpringBootApplication
@EnableCaching
public class BankingApplication { ... }
```

---

### 4.2 Step 2: Advanced `RedisCacheManager` with Custom TTLs

> [!WARNING]
> By default, Spring Boot Cache **bina TTL ke** keys store karta hai!
> Isse aapka Redis server memory bharte-bharte crash ho jayega. Production me hamesha default TTL aur per-cache custom TTL configure karein:

```java
package com.learning.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        
        // 1. Default Baseline Configuration (10 Minutes TTL + JSON Serializer)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues() // Do not cache nulls unless required
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                );

        // 2. Per-Cache Name Specific TTLs (Fine-grained tuning)
        Map<String, RedisCacheConfiguration> customConfigs = new HashMap<>();
        
        // Accounts cache: 15 minutes
        customConfigs.put("accounts", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Exchange rates cache: 1 minute (Fast changing data)
        customConfigs.put("exchangeRates", defaultConfig.entryTtl(Duration.ofMinutes(1)));
        
        // App system settings: 24 hours (Rarely changing)
        customConfigs.put("systemConfig", defaultConfig.entryTtl(Duration.ofHours(24)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(customConfigs)
                .build();
    }
}
```

---

### 4.3 The Annotation Suite Explained

```
┌────────────────────────────────────────────────────────────────────────┐
│                        SPRING CACHE ANNOTATIONS                        │
├─────────────────┬──────────────────────────────────────────────────────┤
│ @Cacheable      │ Cache me pehle dhoondo. Agar mila toh method RUN     │
│                 │ MAT KARO, direct cache se return karo. Agar nahi     │
│                 │ mila, toh method run karo aur result cache me daalo. │
├─────────────────┼──────────────────────────────────────────────────────┤
│ @CachePut       │ Method ko HAMESHA RUN KARO, aur jo naya return value │
│                 │ aaye usse cache ko update/overwrite kar do.          │
├─────────────────┼──────────────────────────────────────────────────────┤
│ @CacheEvict     │ Cache se key ko DELETE / EVICT kar do.               │
├─────────────────┼──────────────────────────────────────────────────────┤
│ @Caching        │ Multiple cache operations ko ek sath group karo      │
│                 │ (e.g. 2 alag caches evict karna + 1 put karna).      │
└─────────────────┴──────────────────────────────────────────────────────┘
```

#### Detailed Code Example:

```java
@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);
    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * 1. @Cacheable:
     * - value / cacheNames: Cache bucket ka naam ("accounts")
     * - key: SpEL (Spring Expression Language). "#accountId" string banega
     *        Redis me Key banegi: "accounts::acc_001"
     * - unless: Result condition (e.g. null response ko cache mat karo)
     * - sync = true: Cache Stampede (Thundering Herd) se bachata hai!
     */
    @Override
    @Cacheable(value = "accounts", key = "#accountId", unless = "#result == null", sync = true)
    public Account getAccountById(String accountId) {
        log.info("🐢 Cache Miss! Fetching account from MongoDB: {}", accountId);
        return accountRepository.findById(accountId)
                .orElse(null);
    }

    /**
     * 2. @CachePut:
     * - Method execute hoga hi hoga (Account balance update/save hoga)
     * - Save hone ke baad fresh updated Account cache me overwrite ho jayega!
     * - Notice key: "#account.id" (Object ke property se key extract hoti hai)
     */
    @Override
    @CachePut(value = "accounts", key = "#account.id")
    public Account updateAccount(Account account) {
        log.info("💾 Updating account in DB and refreshing Cache: {}", account.getId());
        return accountRepository.save(account);
    }

    /**
     * 3. @CacheEvict (Single Item):
     * - Account close/delete hone par cache se remove kar deta hai
     * - beforeInvocation = false (default): Method successful hone par hi evict karega
     */
    @Override
    @CacheEvict(value = "accounts", key = "#accountId")
    public void closeAccount(String accountId) {
        log.info("🗑️ Closing account and removing from cache: {}", accountId);
        accountRepository.deleteById(accountId);
    }

    /**
     * 4. @CacheEvict (All Entries):
     * - allEntries = true: Poore "accounts" bucket ki saari keys delete kar dega
     */
    @Override
    @CacheEvict(value = "accounts", allEntries = true)
    public void evictAllAccountsCache() {
        log.info("🧹 Cleared entire accounts cache bucket!");
    }

    /**
     * 5. @Caching:
     * - Complex scenario: Jab ek operation multiple caches ko affect kare
     */
    @Override
    @Caching(
        evict = {
            @CacheEvict(value = "accounts", key = "#sourceAccountId"),
            @CacheEvict(value = "accounts", key = "#targetAccountId")
        },
        put = {
            @CachePut(value = "recentTransfers", key = "#result.transferId")
        }
    )
    public TransferResult transferFunds(String sourceAccountId, String targetAccountId, BigDecimal amount) {
        // Business logic...
        return new TransferResult("tx_999", sourceAccountId, targetAccountId, amount);
    }
}
```

---

## 🪤 Chapter 5: The AOP Self-Invocation Trap

Interviews ka sabse favourite tricky question:

> **Sawaal:**
> *"Mene method par `@Cacheable` lagaya hai, par jab main service ke andar se hi doosre method se usko call karta hoon toh cache miss ho jata hai aur har baar DB query chalti hai! Kyu?"*

```java
@Service
public class UserService {

    public User getFullProfile(String id) {
        // ❌ SELF-INVOCATION TRAP:
        return this.getUser(id); // Calling its own method!
    }

    @Cacheable(value = "users", key = "#id")
    public User getUser(String id) {
        return userRepository.findById(id).orElse(null);
    }
}
```

### Why it Fails:
Spring Security, Spring Transaction (`@Transactional`), aur Spring Cache (`@Cacheable`) sab **Spring Dynamic AOP Proxy** ke zariye kaam karte hain:

```
External Caller (Controller) ────► [ Spring AOP Proxy ] ────► Target Service
                                         │
                                  Checks Redis Cache
                                  (Cache Hit/Miss Logic)
```

Lekin jab service **`this.getUser()`** call karti hai, toh call **Proxy ke through nahi jaati**, balki seedha raw Java object ke andar chali jaati hai! AOP proxy intercept hi nahi kar pata!

### 🔧 2 Ways to Fix:
1. **Best Practice**: Caching method ko ek dedicated separate service class (`UserCacheService`) me move karo taaki caller hamesha Spring Proxy bean ko call kare.
2. **Self-Injection (Workaround)**:
   ```java
   @Service
   public class UserService {
       @Autowired
       @Lazy
       private UserService self; // Injects the Spring AOP Proxy itself!

       public User getFullProfile(String id) {
           return self.getUser(id); // Goes through the Proxy! ✅
       }
   }
   ```

---

## 🌋 Chapter 6: Production Disasters & Their Remedies

```
┌────────────────────────────────────────────────────────────────────────┐
│                        3 BIG REDIS DISASTERS                           │
└────────────────────────────────────────────────────────────────────────┘
```

### 1. Cache Penetration (Missing Keys Attack)
* **Problem**: Hacker aisi IDs se queries karta hai jo database me exist hi nahi karti (`id = -99999`). Cache miss hota hai, aur har request sidha database par jaakar DB ko crash kar deti hai.
* **Remedy**:
  * **Cache Null Values with Short TTL**: Agar DB me null mila, toh Redis me `SET user:-99999 NULL EX 60` save kar do taaki agle 60 seconds tak DB safe rahe.
  * **Bloom Filter**: Ek probabilistic data structure jo 100% guarantee deta hai agar koi element DB me exist *nahi* karta.

### 2. Cache Breakdown / Hotspot Key Expiration
* **Problem**: Ek bohot hot key hai (e.g. "IPL Live Score" ya "Diwali Offer"). The moment iska TTL expire hota hai, usi millisecond me 50,000 concurrent threads cache miss pate hain aur sabke sab DB par query marte hain (**Thundering Herd Problem**).
* **Remedy**:
  * Spring Cache me `@Cacheable(..., sync = true)` lagayein. Ye internally ek thread ko lock deta hai DB query karne ka, jab tak baki threads wait karti hain.
  * Mutex Lock (`opsForValue().setIfAbsent("lock:" + key, ...)`).

### 3. Cache Avalanche (Sabka Ek Sath Collapse)
* **Problem**: Developer ne saari keys par fixed `1 Hour` TTL laga diya. Theek 1 ghante baad lakhon keys ek hi second me expire ho gayi. Database instantly crash ho gaya.
* **Remedy**:
  * **Random Jitter Add Karo**: Base TTL + Random extra seconds:
    $$\text{Actual TTL} = 3600 + \text{random}(0, 300) \text{ seconds}$$

---

## ✏️ Chapter 7: Sharpen Your Pencil (Architect Q&A)

### Q1: `redisTemplate.opsForValue().set()` vs `opsForHash().put()` — kab kaun sa choose karein?
* **Ans**: Agar poora entity ek single cohesive unit hai jise hamesha pura read kiya jata hai (e.g. Bank Account details), toh JSON String `opsForValue()` simple aur best hai. Agar entity ke fields ko independent update ya increment karna hai (e.g. User Session jisme sirf `lastActiveTime` ya `failedLoginAttempts` badalna hai), toh `opsForHash()` choose karein.

### Q2: `@Cacheable` me `key` generate kaise hoti hai agar hum `key` attribute na likhein?
* **Ans**: Spring ka default `SimpleKeyGenerator` use hota hai:
  - Agar 0 method parameters hain $\rightarrow$ `SimpleKey.EMPTY`.
  - Agar 1 parameter hai $\rightarrow$ Wahi parameter key ban jata hai.
  - Agar multiple parameters hain $\rightarrow$ Unka combined composite hash code key ban jata hai.
  - *Best Practice*: Hamesha explicit SpEL likhein: `key = "#userId"`.

### Q3: Redis Cluster me Multi-Key operations (MGET, Transactions) me `CROSSSLOT Keys in request don't hash to the same slot` error kyu aati hai?
* **Ans**: Redis Cluster keyspace ko 16384 hash slots me baant-ta hai. Agar do alag keys alag cluster nodes par mapping slots me chali gayi, toh cluster single atomic operation reject kar deta hai.
* **Fix**: **Hash Tags** `{...}` use karein:
  `user:{1001}:profile` aur `user:{1001}:orders` dono hamesha exact same node slot par map hongi kyunki hash slot calculation sirf `{1001}` ka hoga!

---

## 📋 Chapter 8: Quick Revision Cheat Sheet

| Feature | Imperative (`RedisTemplate`) | Declarative (`@Cacheable`) |
| :--- | :--- | :--- |
| **Control** | Fine-grained, atomic commands, manual TTL | Transparent, zero boilerplate, method-level |
| **Use Cases** | Locks, Rate Limiting, Counters, STOMP presence | Read-heavy database query caching |
| **Key Generation** | Manual string concatenation (`"user:" + id`) | SpEL (`key = "#id"`) |
| **Eviction** | `redisTemplate.delete(key)` | `@CacheEvict(key = "#id")` |
| **Locking / Sync** | `setIfAbsent()` (SET NX) | `@Cacheable(sync = true)` |
| **Serialization** | Configured in `RedisTemplate` Bean | Configured in `RedisCacheManager` Bean |

---

> 🎓 **Alex's Golden Architect Rule**:
> *"Redis ko kabhi primary source of truth mat samjho jab tak persistence configuration (AOF/RDB) strict na ho. Redis is your high-speed acceleration layer — designed to protect your databases and make your system fly!"*
