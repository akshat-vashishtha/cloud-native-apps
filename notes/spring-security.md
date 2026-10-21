# Head First Spring Security & Microservices Architecture 🛡️
*(Aapka Practical, Step-by-Step, Class-by-Class aur Visual Master Guide)*

> *"Security koi aisi cheez nahi hai jise banaye hue code par namak ki tarah upar se chhidak diya jaye. Security woh buniyad (foundation) hai jis par aapka har ek microservice khada hota hai."*

---

## 🧭 Visual Roadmap: Hum Kya Seekhne Wale Hain?

```
┌────────────────────────────────────────────────────────────────────────┐
│                        VISUAL LEARNING ROADMAP                         │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 1: The VIP Nightclub Metaphor (Authentication vs AuthZ)       │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 2: Spring Security Engine & Pipeline (.build() ka Sach &      │
│             Pehle SecurityConfig Chalti Hai Ya Filters?)               │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 3: Method Security (@PreAuthorize ka Asli Magic)              │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 4: Aggregated View (System-Wide Class-to-Class End-to-End)    │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 4.5: RSA-256 Asymmetric Cryptography (Concept & Code Meaning) │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 5: Segregated View (Har Service ka Startup vs Runtime Flow)   │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 6: The Enum Trap aur Dynamic PBAC (MongoDB + Redis)           │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 7: Next Frontiers (Apna Khud ka OAuth2 Server & mTLS)         │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Chapter 8: Architect's Cheat Sheet (Gotchas, Brain Teasers & Q&A)     │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 🚪 Chapter 1: The VIP Nightclub Metaphor

Maan lo tum Mumbai ya New York ke sabse bade aur exclusive VIP club **"Club Distributed"** mein ja rahe ho.

```
                  ┌───────────────────────────────┐
                  │    YOU (Client HTTP Request)  │
                  └──────────────┬────────────────┘
                                 │
                                 ▼
        ┌──────────────────────────────────────────────────┐
        │  GATE BOUNCER #1 (Authentication - AuthN)        │
        │  Sawaal: "Tum kaun ho bhai? ID dikhao!"          │
        └────────────────────────┬─────────────────────────┘
                                 │
                     [ Shows Passport / JWT Token ]
                                 │
                                 ▼
        ┌──────────────────────────────────────────────────┐
        │  BOUNCER: "Theek hai, tum Akshat ho. Enter karo!"│
        └────────────────────────┬─────────────────────────┘
                                 │
                                 ▼
        ┌──────────────────────────────────────────────────┐
        │  VIP LOUNGE GUARD #2 (Authorization - AuthZ)     │
        │  Sawaal: "Tum andar toh aa gaye, par kya VIP     │
        │           Section mein baithne ka stamp hai?"    │
        └────────────────────────┬─────────────────────────┘
                                 │
                   [ Checks Wristband / Authority ]
                                 │
                                 ▼
        ┌──────────────────────────────────────────────────┐
        │  GUARD: "Haan, tumhare paas 'vip:access' hai,     │
        │         jao moj karo!"                           │
        └────────────────────────┬─────────────────────────┘
                                 │
                                 ▼
                  ┌───────────────────────────────┐
                  │   THE VIP BAR (REST Controller)│
                  │   GET /api/v1/banking/accounts│
                  └───────────────────────────────┘
```

### Dono mein Farq kya hai?

| Concept | Desi Bhasha mein Matlab | Security Term | Humare Code mein Kaun Sambhalta hai? |
| :--- | :--- | :--- | :--- |
| **Authentication (AuthN)** | **"Tum Kaun Ho?"** (Identity prove karna). | `Principal` & `Authentication` | `JwtAuthenticationFilter` / `InternalHmacFilter` |
| **Authorization (AuthZ)** | **"Tumhe kya karne ki ijaazat hai?"** (Permissions check karna). | `GrantedAuthority` & `AccessDecision` | `@PreAuthorize("hasAuthority('account:read')")` |

> 💡 **Head First Golden Rule**:
> - Pehle **Authentication** hota hai (Identity banti hai).
> - Uske baad hi **Authorization** check ho sakta hai (Ki is identity ke paas yeh kaam karne ka haq hai ya nahi).

---

## ⚙️ Chapter 2: Spring Security Engine & Pipeline (`.build()` ka Sach)

### 2.1 "Yeh Filter Chain Kahan Use Ho Rahi Hai?"

Aapne apne microservice mein [`SecurityConfig.java`](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/micro-service/user-rbac-service/src/main/java/com/learning/user/infrastructure/security/SecurityConfig.java) dekha hoga:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

    DefaultSecurityFilterChain securityFilterChain = httpSecurity
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build(); // 👈 YAHAN BANTI HAI SAARI PIPELINE!

    return securityFilterChain;
}
```

### `.build()` Call Karne Par Background Mein Kya Banta Hai?

`HttpSecurity` ek **Builder Object** hai. Jab aap `.build()` call karte hain, toh Spring Security background mein ek internal `List<Filter>` assemble karta hai.

Bhale hi aapne `new CorsFilter()` ya `new AuthorizationFilter()` nahi likha, lekin Spring ke methods unhe automatically insert karte hain:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                   Spring Security ki Internal Assembled Filter List                    │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ 1. DisableEncodeUrlFilter           (Spring default URL sanitizer)                     │
│ 2. SecurityContextHolderFilter      (Har request par naya ThreadLocal context banata h)│
│ 3. HeaderWriterFilter               (X-Content-Type, HSTS headers add karta h)         │
│ 4. CorsFilter                       (Browser CORS headers check karta h)               │
│ 5. CsrfFilter (DISABLED)           (Kyunki humne .csrf(disable) kiya h)                │
│ 6. LogoutFilter                     (Default logout check)                             │
│ 7. JwtAuthenticationFilter ⭐       (HAMARA FILTER! Humne .addFilterBefore() se lagaya)│
│ 8. UsernamePasswordAuthFilter       (Spring default form login - bypass ho jata h)     │
│ 9. RequestCacheAwareFilter          (Stateless hone ki wajah se inactive)              │
│ 10. SecurityContextHolderAwareFilter (HttpServletRequest ko auth methods provide krta)  │
│ 11. AnonymousAuthenticationFilter   (Agar token na ho toh 'anonymousUser' banata h)   │
│ 12. ExceptionTranslationFilter      (AccessDeniedException ko pakadta h)               │
│ 13. AuthorizationFilter ⭐          (HAMARA .authorizeHttpRequests() URL rule check!)  │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

**Asli Execution Flow:**
1. Request aati hai $\to$ **Filter #7 [`JwtAuthenticationFilter`](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/micro-service/user-rbac-service/src/main/java/com/learning/user/infrastructure/security/JwtAuthenticationFilter.java)** chalta hai. Yeh token verify karta hai aur `SecurityContextHolder` set karta hai.
2. Fir **Filter #13 `AuthorizationFilter`** chalta hai $\to$ Yeh dekhta hai ki URL `/actuator/**` ya `/api/v1/auth/**` hai toh permitAll, warna block karo agar user authenticated nahi hai.

---

### 2.2 The Holy Trinity: Teen Sabse Zaroori Objects

Spring Security ke andar saara khel sirf in 3 objects ke ird-gird ghumta hai:

```
┌────────────────────────────────────────────────────────────────────────┐
│ 1. SecurityContextHolder                                               │
│    Ek storage box jo ThreadLocal par rehta hai. Is thread par chalne   │
│    wala koi bhi method is box ko khol sakta hai.                       │
│                                                                        │
│    ┌──────────────────────────────────────────────────────────────┐    │
│    │ 2. SecurityContext                                           │    │
│    │    Box ke andar rakha lifafa (Envelope).                     │    │
│    │                                                              │    │
│    │    ┌────────────────────────────────────────────────────┐    │    │
│    │    │ 3. Authentication (Aapka Passport)                 │    │    │
│    │    │    • Principal   = "user-101" (Aapki ID)           │    │    │
│    │    │    • Credentials = null (Password verify hone k bad│    │    │
│    │    │                    delete kar dete hain)           │    │    │
│    │    │    • Authorities = [ "account:read",               │    │    │
│    │    │                      "transfer:execute" ]          │    │    │
│    │    └────────────────────────────────────────────────────┘    │    │
│    └──────────────────────────────────────────────────────────────┘    │
└────────────────────────────────────────────────────────────────────────┘
```

#### Code mein yeh kaise banta hai?
```java
// Hamare filter ke andar hum exactly yahi karte hain:
List<SimpleGrantedAuthority> authorities = List.of(
    new SimpleGrantedAuthority("account:read"),
    new SimpleGrantedAuthority("transfer:execute")
);

// 1. Passport banaya
UsernamePasswordAuthenticationToken passport = 
    new UsernamePasswordAuthenticationToken(userId, null, authorities);

// 2. Storage box mein daal diya
SecurityContextHolder.getContext().setAuthentication(passport);
```

---

### 🗣️ Fireside Chat: Alex aur Ek Confused Developer

**Developer**: *"Alex bhai, humne code mein `.csrf(AbstractHttpConfigurer::disable)` kyu likha? Google par likha hota hai CSRF attack se bank account khali ho jata hai!"*

**Alex**: *"Arey bhai, context samjho! CSRF attack tab hota hai jab browser bina bataye background mein tumhare cookies (jaise `JSESSIONID`) bank ki website par chipka deta hai. 
Lekin hamare microservices mein koi browser session cookie use hi nahi ho rahi downstream! Har request explicit header bhejti hai: `Authorization: Bearer <token>` ya `X-Internal-HMAC`. Kisi doosri evil website ke paas yeh token hota hi nahi. Isliye REST APIs mein CSRF ko band karna standard industry practice hai!"*

**Developer**: *"Aur `.sessionManagement(SessionCreationPolicy.STATELESS)` kyu likha?"*

**Alex**: *"Iska matlab hai Tomcat ko bolna: 'Bhai, memory mein koi session mat banana. Agar 10 lakh users bhi aayein, toh server par 1 byte ki session memory bhi mat gherna. Har request apna saboot (token) khud sath layegi!' Isse hamara microservice 100% horizontal scale ho sakta hai."*

---

### 2.3 Life-or-Death Question: Pehle `SecurityConfig` Chalti Hai Ya `Filters`?

Yeh sawaal har interview aur developer ke dimaag mein sabse bada confuser hota hai:
> *"Alex bhai, jab request aati hai toh pehle SecurityConfig chalti hai ya JwtAuthenticationFilter?"*

**Answer:** Dono **alag-alag lifecycles** mein chalte hain!

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│ PHASE 1: APPLICATION STARTUP (BOOTSTRAPPING) — Sirf Ek Baar Chalta Hai (No HTTP Yet!)   │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ 1. Spring Boot App start hoti hai (`main()` method).                                   │
│ 2. `@Configuration` class `SecurityConfig` pehle instantiate hoti hai.                 │
│ 3. Spring calls `@Bean public SecurityFilterChain securityFilterChain(HttpSecurity)`   │
│ 4. `.csrf(disable)`, `.sessionManagement(STATELESS)`, `.addFilterBefore(...)` chalta h │
│ 5. `.build()` call hote hi Spring ek pipeline (List of Filters) RAM mein assemble      │
│    karke `SecurityFilterChain` Bean bana kar save kar leti hai.                        │
│ 6. Ab `SecurityConfig` ka kaam KHATAM! Yeh dobara kabhi nahi chalti.                   │
└────────────────────────────────────────┬───────────────────────────────────────────────┘
                                         │
                                [ Server is Live & Waiting ]
                                         │
                                         ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│ PHASE 2: REQUEST RUNTIME (WHEN A CLIENT SENDS HTTP REQUEST) — Har Request Par Chalta H │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ 1. Client se Request aati hai (e.g. `GET /api/v1/roles`).                              │
│ 2. `SecurityConfig` NAHI chalti!                                                       │
│ 3. Tomcat request ko Spring ke `DelegatingFilterProxy` ➔ `FilterChainProxy` ko deta h. │
│ 4. Jo filter pipeline Phase 1 mein assemble hui thi, uske FILTERS line-by-line         │
│    execute hote hain:                                                                  │
│    • Filter #1..#6: HeaderWriter, CorsFilter, etc.                                     │
│    • Filter #7: `JwtAuthenticationFilter` (HAMARA FILTER) chalta hai!                  │
│                 Token verify karta hai aur `SecurityContextHolder` set karta hai.      │
│    • Filter #13: `AuthorizationFilter` chalta hai!                                     │
│                  Check karta hai: "Kya user authenticated hai? Kya path permitted h?"  │
│ 5. Agar filter pass hua ➔ Request jaati hai `DispatcherServlet` ➔ `Controller`!        │
│ 6. Agar filter fail hua ➔ Filter wahi se 401/403 return kar deta hai (Controller tak   │
│    request pahunchti hi nahi!).                                                        │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

> 💡 **The Golden Rule to Remember**:
> - **SecurityConfig:** Building ka Naksha (Blueprint) aur CCTVs ka wiring plan hai. Yeh building bante waqt (**Startup Phase**) par sirf ek baar chalti hai.
> - **Filters:** Darwaze par khade active guards hain. Har aane-jaane wale visitor (**Request Runtime Phase**) ko yehi guards check karte hain!

---

## 🎯 Chapter 3: Method Security (`@PreAuthorize` ka Asli Magic)

URL security (`.requestMatchers("/api/v1/banking/**")`) building ke bahar ki deewar hai. 
Lekin **Method Security** (`@PreAuthorize`) tijori ke darwaze par laga biometric sensor hai!

```
                  Client calls: transferFunds(10,000 INR)
                                    │
                                    ▼
                 ┌──────────────────────────────────────┐
                 │    Spring AOP Proxy Interception     │
                 │ (Controller se pehle Proxy pakadti h)│
                 └──────────────────┬───────────────────┘
                                    │
                                    ▼
                 ┌──────────────────────────────────────┐
                 │  SpEL Evaluation:                    │
                 │  hasAuthority('transfer:execute')     │
                 └──────────────────┬───────────────────┘
                                    │
                                    ▼
                 ┌──────────────────────────────────────┐
                 │ Check SecurityContextHolder:         │
                 │ Kya current user ke paas yeh stamp h?│
                 └──────────────────┬───────────────────┘
                                    │
                     ┌──────────────┴──────────────┐
                     │                             │
                  [ YES ]                       [ NO ]
                     │                             │
                     ▼                             ▼
       ┌───────────────────────────┐ ┌───────────────────────────┐
       │ Execute Real Java Method  │ │ Throw AccessDeniedException│
       │ (Paise transfer ho gaye!) │ │ HTTP 403 FORBIDDEN         │
       └───────────────────────────┘ └───────────────────────────┘
```

### ⚠️ Watch Out! (Khatra - The Silent Trap)
Agar aapne controller par `@PreAuthorize` laga diya, lekin `SecurityConfig` par `@EnableMethodSecurity` lagana bhool gaye:

> **Nateeja:** Spring Security bina koi error phenke, **method security ko completely ignore kar degi**! 
> Kisi bhi user ko, bina permission ke bhi method chalane degi!
> Hamesha ensure karo ki `SecurityConfig` par `@EnableMethodSecurity` zaroor laga ho!

---

## 🌐 Chapter 4: AGGREGATED VIEW (System-Wide Class-to-Class End-to-End Flow)

Jab ek User phone app se **Paise Transfer** karta hai, toh classes ka aapas mein exact sequence flow kya hota hai:

```
========================================================================================================
                         AGGREGATED SYSTEM EXECUTION TRACE: MONEY TRANSFER
========================================================================================================

 [ CLIENT / APP ]               [ GATEWAY-SERVICE ]            [ BUSINESS-OPERATION-SERVICE ]
        │                                │                                │
        │ 1. POST /api/v1/auth/login     │                                │
        ├───────────────────────────────►│ (Routes to user-rbac-service)  │
        │                                │                                │
        │ 2. Returns JWT Token           │                                │
        │    Signed with RSA-256         │                                │
        │    Private Key (JwtTokenServ)  │                                │
        │◄───────────────────────────────┤                                │
        │                                │                                │
        │ 3. POST /api/v1/banking/transfers                               │
        │    Header: Bearer <JWT>        │                                │
        ├───────────────────────────────►│                                │
        │                                │                                │
        │                                │ 4. BffAuthenticationFilter     │
        │                                │    Checks Opaque Cookie/Bearer │
        │                                │                                │
        │                                │ 5. JwtPublicKeyValidator       │
        │                                │    Verifies RS256 with PubKey  │
        │                                │    Extracts: userId, roles,    │
        │                                │    permissions                 │
        │                                │                                │
        │                                │ 6. InternalHmacSignerFilter    │
        │                                │    Strips client X-User-*      │
        │                                │    Signs HMAC: timestamp ::    │
        │                                │    userId :: roles :: perms    │
        │                                │                                │
        │                                │ 7. Forward Signed Request      │
        │                                ├───────────────────────────────►│
        │                                │    Headers:                    │
        │                                │    X-User-Id: 101              │
        │                                │    X-User-Permissions: trans:ex│
        │                                │    X-Timestamp: 1729080000     │
        │                                │    X-Internal-HMAC: a8f4b2...  │
        │                                │                                │
        │                                │                                │ 8. InternalHmacFilter
        │                                │                                │    • Checks Clock Skew <= 300s
        │                                │                                │    • MessageDigest.isEqual HMAC
        │                                │                                │    • Sets SecurityContextHolder
        │                                │                                │      with SimpleGrantedAuthority
        │                                │                                │                                
        │                                │                                │ 9. BankingController
        │                                │                                │    • @PreAuthorize checks:
        │                                │                                │      hasAuthority('trans:ex')
        │                                │                                │    • Passes! Calls:
        │                                │                                │      BankingServiceImpl
        │                                │                                │                                
        │                                │                                │ 10. Database Debit/Credit
        │                                │                                │     Mongo saves transaction
        │                                │                                │                                
        │                                │ 11. Return TransferResponse    │
        │                                │◄───────────────────────────────┤
        │ 12. HTTP 200 OK (Transferred!) │                                │
        │◄───────────────────────────────┤                                │
```

---

## 🔐 Chapter 4.5: Deep Dive: RSA-256 Asymmetric Cryptography (Concept & Code in Project)

### 4.5.1 Concept: Symmetric (HMAC) vs Asymmetric (RSA)

Ab tak humne dekha ki security mein do tarah ki chabi (keys) hoti hain:

```
┌────────────────────────────────────────────────────────────────────────┐
│ SYMMETRIC CRYPTOGRAPHY (e.g. HMAC-SHA256)                              │
├────────────────────────────────────────────────────────────────────────┤
│ • Ek hi Secret Key hoti hai.                                           │
│ • Token SIGN (Banana) = Secret Key                                     │
│ • Token VERIFY (Check) = Wahi Same Secret Key                          │
│ ⚠️ Khatra: Agar verify karne wale (Gateway) ko key di, toh woh naye     │
│    fake tokens bhi bana sakta hai! Zero-Trust mein yeh unsafe hai.     │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│ ASYMMETRIC CRYPTOGRAPHY (e.g. RSA-256)                                 │
├────────────────────────────────────────────────────────────────────────┤
│ • Mathematical Linked Pair of 2 Keys hoti hai:                         │
│   1. PRIVATE KEY (Secret Stamp): Sirf User-Service ke paas hoti hai.   │
│      Kaam: Naye JWT Tokens par digital signature (thappa) lagana.      │
│   2. PUBLIC KEY (Open Reader): Poori duniya / Gateway ke paas hoti hai.│
│      Kaam: Sirf check karna ki signature asli hai ya nakli.             │
│ ✅ Zero-Trust: Public Key se koi bhi insaan naya token NAHI bana sakta! │
└────────────────────────────────────────────────────────────────────────┘
```

> 🎟️ **The Royal Wax Seal Metaphor**:
> - **Private Key:** Raja ki personal anghuthi (Ring). Sirf Raja ke paas hai. Raja isse stamp lagata hai.
> - **Public Key:** Raajmahal ke bahar lagi tasveer jo sabko dikhati hai ki Raja ki stamp aisi dikhti hai. Har darbaan (Gateway) tasveer se match karke check kar sakta hai ki stamp asli hai ya nahi, lekin darbaan khud Raja ki anghuthi nahi bana sakta!

---

### 4.5.2 Project Code Postmortem: RSA Kaise Kaam Kar Raha Hai?

Hamare codebase mein RSA teen specific classes mein zinda hai:

#### 1. `RsaKeyConfig.java` (`user-rbac-service`) — Chabi Banana
```java
@Configuration
public class RsaKeyConfig {
    private final KeyPair keyPair;

    public RsaKeyConfig() {
        this.keyPair = generateRsaKeyPair();
    }

    @Bean
    public RSAPublicKey rsaPublicKey() {
        return (RSAPublicKey) keyPair.getPublic();
    }

    @Bean
    public RSAPrivateKey rsaPrivateKey() {
        return (RSAPrivateKey) keyPair.getPrivate();
    }

    private KeyPair generateRsaKeyPair() {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // 2048-bit enterprise security
        return keyPairGenerator.generateKeyPair();
    }
}
```
* **Meaning:** Jab `user-rbac-service` boot hoti hai, Java Cryptography Architecture (`KeyPairGenerator`) se ek 2048-bit RSA pair generate hota hai. Dono keys Spring IoC Container mein `@Bean` ban kar baith jaati hain.

---

#### 2. `JwtTokenService.java` (`user-rbac-service`) — Token Par Thappa Lagana
```java
// TOKEN GENERATION (Login par)
public String generateToken(User user) {
    Algorithm algorithm = Algorithm.RSA256(rsaPublicKey, rsaPrivateKey); // 👈 Private key used to SIGN!
    return JWT.create()
            .withIssuer("user-rbac-service")
            .withSubject(user.getId())
            .withClaim("email", user.getEmail())
            .withClaim("roles", new ArrayList<>(user.getRoles()))
            .withClaim("permissions", new ArrayList<>(permissions))
            .sign(algorithm);
}

// PUBLIC KEY EXPOSURE (Duniya ke liye)
// AuthController.java -> GET /api/v1/auth/public-key
// Returns Base64 encoded X.509 format public key: {"publicKey": "MIIBIjANBgkq..."}
```
* **Meaning:** Token sign karte waqt `rsaPrivateKey` pass ki gayi. Private key ke bina koi aur service is signature ko reproduce nahi kar sakti.

---

#### 3. `JwtPublicKeyValidator.java` (`gateway-service`) — Gateway Ka Verification Engine
```java
// Gateway Startup / First Request Par:
// User-Service se Public Key fetch karta hai:
Map<String, String> response = webClient.get()
    .uri(securityProperties.userServiceUri() + "/api/v1/auth/public-key")
    .retrieve().bodyToMono(...).block();

String publicKeyBase64 = response.get("publicKey");
byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
KeyFactory keyFactory = KeyFactory.getInstance("RSA");
RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(encodedKeySpec);

// 👈 NOTICE LINE BELOW: 2nd Parameter is NULL!
Algorithm algorithm = Algorithm.RSA256(publicKey, null);
this.verifier = JWT.require(algorithm).withIssuer("user-rbac-service").build();
```
* **Meaning (The Masterstroke):** 
  * `Algorithm.RSA256(publicKey, null)` — Gateway ke paas Private Key **NULL** hai! 
  * Gateway ke paas token banane ka 0% access hai. Gateway sirf aur sirf token **VERIFY** karta hai. Agar Gateway hack bhi ho jaye, toh hacker ek bhi fake token generate nahi kar sakta!

---

## 🔬 Chapter 5: SEGREGATED VIEW (Har Microservice ka Startup vs Runtime Flow & Class Postmortem)

Har ek microservice ke andar **kaunsi class kab chalti hai (Startup vs Runtime)** aur uska kya kaam hai, uska exact segregated blueprint:

---

### 1. `user-rbac-service` (Port 8081 - The Identity & Policy Authority)

#### A. Architecture Mapping Table:
```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        user-rbac-service (Architecture Mapping)                        │
├───────────────────────────────────┬────────────────────────────────────────────────────┤
│ Class Name                        │ Responsibility in Project                          │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ RsaKeyConfig.java                 │ Generates 2048-bit RSA KeyPair; exposes Public/    │
│                                   │ Private key beans into Spring ApplicationContext   │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ SecurityConfig.java               │ Configures SecurityFilterChain, enables Method     │
│                                   │ Security (@EnableMethodSecurity), registers filter │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ JwtAuthenticationFilter.java      │ Extracts Bearer token, verifies using RSA Public   │
│                                   │ key, populates SecurityContext with authorities    │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ JwtTokenService.java              │ Signs JWT tokens with RSA-256 Private key and      │
│                                   │ embeds dynamic 'permissions' and 'roles' claims    │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ RoleServiceImpl.java              │ Resolves permissions for roles from MongoDB        │
│                                   │ 'roles' collection. Caches results in Redis        │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ RoleDocument.java                 │ MongoDB Document for collection 'roles'. Stores    │
│                                   │ dynamic role name, description, and permissions    │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ RoleController.java               │ Admin REST endpoints (POST /api/v1/roles).         │
│                                   │ Guarded by @PreAuthorize("hasAuthority('...')")    │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ AuthController.java               │ Public endpoints (/login, /register, /public-key)  │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ RbacDataSeeder.java               │ Seeds default baseline roles (ROLE_USER, etc.)     │
│                                   │ on Day 1 startup if MongoDB collection is empty    │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ GlobalExceptionHandler.java       │ Catches AccessDeniedException and maps to 403      │
└───────────────────────────────────┴────────────────────────────────────────────────────┘
```

#### B. Execution Flow: Pehle Kaun, Baad Mein Kaun?
```
🟡 PHASE 1: STARTUP (Application Bootstrap - Ek Baar Chalti Hai):
1. RsaKeyConfig executes ──► 2048-bit KeyPair generates, rsaPublicKey & rsaPrivateKey beans ready.
2. JwtAuthenticationFilter bean instantiate hota hai (injected with JwtTokenService).
3. SecurityConfig executes ──► securityFilterChain(http) method chalta hai:
   • .csrf(disable)
   • .sessionManagement(STATELESS)
   • .authorizeHttpRequests(/actuator/**, /api/v1/auth/** permitAll, rest authenticated)
   • .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
   • .build() ──► DefaultSecurityFilterChain ready in RAM!
4. RbacDataSeeder executes ──► Seeds MongoDB default roles if empty.
5. Server is Live on Port 8081!

─────────────────────────────────────────────────────────────────────────────────────────

🟢 PHASE 2: RUNTIME (Jab Client Call Karta Hai):
Scenario A: Public Login Call (POST /api/v1/auth/login)
1. Request arrives ──► JwtAuthenticationFilter.shouldNotFilter() returns TRUE (Filter bypassed!).
2. AuthorizationFilter checks path ──► /api/v1/auth/** is permitAll() ──► Request allowed!
3. AuthController.login() calls AuthenticationServiceImpl.
4. PasswordEncoder.matches() checks BCrypt hash.
5. JwtTokenService.generateToken() signs JWT with rsaPrivateKey ──► Returns Token to Client!

Scenario B: Protected Admin Call (GET /api/v1/roles)
1. Request arrives with Header: Authorization: Bearer <token>
2. JwtAuthenticationFilter intercepts:
   • Checks shouldNotFilter() ──► returns FALSE (Must inspect!).
   • Validates token via jwtTokenService.validateToken(token) using rsaPublicKey.
   • Extracts userId, roles, permissions.
   • Creates SimpleGrantedAuthority list (e.g. ROLE_ADMIN, role:read).
   • Sets SecurityContextHolder.getContext().setAuthentication(passport).
   • Calls filterChain.doFilter(request, response).
3. AuthorizationFilter verifies ──► SecurityContext has authenticated user ──► Passes!
4. DispatcherServlet routes to RoleController.getAllRoles().
5. Spring AOP intercepts @PreAuthorize("hasAuthority('role:read')") ──► Matches authority!
6. Method executes ──► Returns Roles List (HTTP 200).
```

#### C. Line-by-Line Code Meaning: SecurityConfig, JwtAuthenticationFilter, & JwtTokenService

Yahan do main libraries milkar kaam karti hain:
* **`spring-boot-starter-security`**: **Gatekeeper & Traffic Police** (Filters, Route rules, BCrypt hashing, Stateless policies, aur `SecurityContextHolder`).
* **`com.auth0:java-jwt`**: **ID Card Maker & Scanner** (RS256 algorithm, Token minting, Claims packing, Private key signing, aur Public key verification).

---

##### 1. `SecurityConfig.java` (`user-rbac-service`) — The Gatekeeper Rules

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        DefaultSecurityFilterChain securityFilterChain = httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

        return securityFilterChain;
    }
}
```

* **Line-by-Line Breakdown:**
  * **`@Configuration`**: Spring IoC container ko batata hai ki is class mein bean definitions (`@Bean`) hain jo application context bootstrap ke time execute honge.
  * **`@EnableWebSecurity`** *(from spring-security)*: Spring Security ka core web security filter subsystem activate karta hai.
  * **`@EnableMethodSecurity`** *(from spring-security)*: Controllers aur services par method-level annotations (jaise `@PreAuthorize("hasAuthority('role:read')")`) ko evaluate karne wala AOP interceptor chalu karta hai.
  * **`JwtAuthenticationFilter jwtAuthenticationFilter`**: Hamara custom JWT filter yahan dependency injection ke through aayega taaki hum use security filter chain mein plug kar sakein.
  * **`passwordEncoder() -> new BCryptPasswordEncoder(12)`** *(from spring-security)*: Passwords plain text mein save nahi hote. BCrypt hashing algorithm (strength: 12 rounds of salt) use hota hai.
  * **`securityFilterChain(HttpSecurity httpSecurity)`** *(from spring-security)*: Yeh Spring Security 6+ ka heart hai jahan hum declarative DSL se HTTP security rules define karte hain.
  * **`.csrf(AbstractHttpConfigurer::disable)`** *(from spring-security)*: CSRF protection disable ki gayi kyunki REST microservices mein browser ambient cookies nahi, balki explicit Bearer headers aate hain.
  * **`.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))`** *(from spring-security)*: Server memory mein koi HTTP session (`JSESSIONID`) create nahi karega. Har request independent hogi.
  * **`.requestMatchers("/actuator/**").permitAll()`**: Kubernetes liveness/readiness probes aur Prometheus metrics scraping endpoints ko bina login ke allow kiya.
  * **`.requestMatchers("/api/v1/auth/**").permitAll()`**: Login, register, aur public-key endpoints open hain taaki naye users login/register kar sakein aur Gateway RSA public key download kar sake.
  * **`.anyRequest().authenticated()`**: Inke alawa baaki har endpoint ke liye valid login hona laazmi hai.
  * **`.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`** *(from spring-security)*: Hamara custom token filter standard form-login filter se pehle chalega taaki token decode karke user passport pehle hi ban jaye.
  * **`.build()`**: Saare rules compile karke immutable `DefaultSecurityFilterChain` memory mein register karta hai.

---

##### 2. `JwtAuthenticationFilter.java` (`user-rbac-service`) — The Inspection Filter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") ||
                path.equals("/api/v1/auth/login") ||
                path.equals("/api/v1/auth/register") ||
                path.equals("/api/v1/auth/public-key");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                DecodedJWT decodedJWT = jwtTokenService.validateToken(token);
                String userId = decodedJWT.getSubject();
                List<String> permissions = decodedJWT.getClaim("permissions").asList(String.class);
                List<String> roles = decodedJWT.getClaim("roles").asList(String.class);

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (permissions != null) {
                    authorities.addAll(permissions.stream().map(SimpleGrantedAuthority::new).toList());
                }
                if (roles != null) {
                    authorities.addAll(roles.stream().map(r -> new SimpleGrantedAuthority(r.startsWith("ROLE_") ? r : "ROLE_" + r)).toList());
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.warn("Invalid JWT token received on {}: {}", request.getRequestURI(), e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

* **Line-by-Line Breakdown:**
  * **`extends OncePerRequestFilter`** *(from spring-security)*: Yeh class guarantee karti hai ki ek incoming HTTP request par yeh filter **sirf aur sirf ek hi baar** execute hoga.
  * **`shouldNotFilter(HttpServletRequest request)`**: Optimization check! Agar request `/actuator` ya public `/api/v1/auth/*` path par ja rahi hai, toh filter ka token check logic bypass ho jata hai.
  * **`request.getHeader(HttpHeaders.AUTHORIZATION)`**: HTTP headers se `Authorization` key nikalta hai.
  * **`authHeader.startsWith("Bearer ")` & `.substring(7)`**: Header format check karta hai (`Bearer <token>`). Aage ke 7 characters (`"Bearer "`) strip karke actual compact token string extract karta hai.
  * **`jwtTokenService.validateToken(token)`** *(uses java-jwt)*: Token ko RS256 Public Key se cryptographically verify karta hai.
  * **`decodedJWT.getSubject()`** *(from java-jwt)*: JWT ke payload se user ka permanent unique identifier (`userId`) nikalta hai.
  * **`decodedJWT.getClaim("permissions").asList(...)` & `"roles"`** *(from java-jwt)*: Token ke andar encoded dynamic roles aur permissions ki array nikalta hai.
  * **`new SimpleGrantedAuthority(...)`** *(from spring-security)*: Har string permission/role ko Spring Security ke formal authorization format mein convert karta hai (roles ke aage `ROLE_` prefix ensure karta hai).
  * **`new UsernamePasswordAuthenticationToken(userId, null, authorities)`** *(from spring-security)*: Principal (`userId`), credentials (`null` - kyunki token already verify ho chuka hai), aur authorities ka authenticated token object create karta hai.
  * **`SecurityContextHolder.getContext().setAuthentication(...)`** *(from spring-security)*: Is passport ko current thread ke `ThreadLocal` context mein save kar deta hai. Ab poori downstream service (jaise `@PreAuthorize`) ko pata chal jata hai ki request kis user ki hai!
  * **`SecurityContextHolder.clearContext()`**: Agar token corrupt, expired ya fake nikla, toh security context wipe kar deta hai taaki unauthorized user access na le sake.
  * **`filterChain.doFilter(request, response)`**: Request ko pipeline mein aage DispatcherServlet / Controller tak bhej deta hai.

---

##### 3. `JwtTokenService.java` (`user-rbac-service`) — Token Minting & Verification

```java
@Getter
@Service
public class JwtTokenService {

    private final RSAPrivateKey rsaPrivateKey;
    private final RSAPublicKey rsaPublicKey;
    private final JwtProperties jwtProperties;
    private final RoleService roleService;

    public JwtTokenService(RSAPrivateKey rsaPrivateKey,
                           RSAPublicKey rsaPublicKey,
                           JwtProperties jwtProperties,
                           RoleService roleService) {
        this.rsaPrivateKey = rsaPrivateKey;
        this.rsaPublicKey = rsaPublicKey;
        this.jwtProperties = jwtProperties;
        this.roleService = roleService;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofSeconds(jwtProperties.accessTokenValiditySeconds()));
        Algorithm algorithm = Algorithm.RSA256(rsaPublicKey, rsaPrivateKey);

        Set<String> permissions = roleService.resolvePermissionsForRoles(user.getRoles());

        return JWT.create()
                .withIssuer("user-rbac-service")
                .withSubject(user.getId())
                .withClaim("email", user.getEmail())
                .withClaim("fullName", user.getFullName())
                .withClaim("roles", new ArrayList<>(user.getRoles()))
                .withClaim("permissions", new ArrayList<>(permissions))
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(algorithm);
    }

    public com.auth0.jwt.interfaces.DecodedJWT validateToken(String token) {
        Algorithm algorithm = Algorithm.RSA256(rsaPublicKey, null);
        return JWT.require(algorithm)
                .withIssuer("user-rbac-service")
                .build()
                .verify(token);
    }

    public String validateAndExtractUserId(String token) {
        return validateToken(token).getSubject();
    }
}
```

* **Line-by-Line Breakdown:**
  * **`Instant now = Instant.now()` & `expiresAt = now.plus(...)`**: Token ka issuance timestamp (`iat`) aur expiration timestamp (`exp`) calculate karta hai.
  * **`Algorithm.RSA256(rsaPublicKey, rsaPrivateKey)`** *(from java-jwt)*: RS256 algorithm instance banata hai. Notice: Token create karte waqt **`rsaPrivateKey`** pass ki gayi hai, jo cryptographic signature generate karegi.
  * **`roleService.resolvePermissionsForRoles(...)`**: MongoDB/Redis se user ke assigned roles ke saare granular permissions resolve karta hai.
  * **`JWT.create()`** *(from java-jwt)*: Auth0 JWT builder start karta hai.
  * **`.withIssuer("user-rbac-service")`**: Standard `iss` claim set karta hai jo identify karta hai ki token kis authority ne issue kiya.
  * **`.withSubject(user.getId())`**: Standard `sub` claim set karta hai jo user ID ko hold karta hai.
  * **`.withClaim("email", ...)` & `.withClaim("permissions", ...)`**: Custom claims attach karta hai taaki downstream services ko DB queries na karni padein.
  * **`.sign(algorithm)`** *(from java-jwt)*: Private Key se digital signature generate karta hai aur standard `header.payload.signature` Base64 URL-safe token return karta hai.
  * **`Algorithm.RSA256(rsaPublicKey, null)`** *(from java-jwt)*: Verification ke waqt **Private Key `null` pass ki gayi hai!** Sirf Public Key se signature verify hota hai.
  * **`JWT.require(algorithm).withIssuer(...).build()`** *(from java-jwt)*: Immutable `JWTVerifier` banata hai jo check karega ki issuer `"user-rbac-service"` hi hai aur signature tamper-proof hai.
  * **`.verify(token)`**: Signature aur expiry check karta hai. Agar valid hai toh `DecodedJWT` return karta hai, warna `JWTVerificationException` throw karta hai.

---

### 2. `gateway-service` (Port 8080 - The BFF & Edge Perimeter Guard)

#### A. Architecture Mapping Table:
```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                          gateway-service (Architecture Mapping)                        │
├───────────────────────────────────┬────────────────────────────────────────────────────┤
│ Class Name                        │ Responsibility in Project                          │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ GatewaySecurityProperties.java    │ Holds immutable config: internalHmacSecret, URIs   │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ JwtPublicKeyValidator.java        │ Calls user-rbac-service (/public-key), caches RSA  │
│                                   │ public key, validates incoming RS256 token claims  │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ BffAuthenticationFilter.java      │ GlobalFilter (Order: -100). Fast-path checks Redis │
│                                   │ APP_SESSION cookie. Fallback validates Bearer JWT  │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ InternalHmacSignerFilter.java     │ GlobalFilter (Order: -50). Strips client headers,  │
│                                   │ generates HMAC-SHA256 signature with permissions   │
└───────────────────────────────────┴────────────────────────────────────────────────────┘
```

#### B. Execution Flow: Pehle Kaun, Baad Mein Kaun?
```
🟡 PHASE 1: STARTUP (WebFlux Netty Bootstrap - Ek Baar Chalti Hai):
1. GatewaySecurityProperties loads internalHmacSecret from environment/Key Vault.
2. JwtPublicKeyValidator initializes (Ready to lazily fetch or pre-fetch RSA Public Key).
3. GlobalFilter beans registered in Netty pipeline with explicit Order:
   • BffAuthenticationFilter (Order: -100)
   • InternalHmacSignerFilter (Order: -50)
4. Gateway is Live on Port 8080!

─────────────────────────────────────────────────────────────────────────────────────────

🟢 PHASE 2: RUNTIME (Jab Client Call Karta Hai):
Client calls: GET /api/v1/banking/accounts
1. BffAuthenticationFilter (Order: -100) runs:
   • Checks for HttpOnly cookie "APP_SESSION".
   • If present ──► Reads Redis session:<id> in < 1ms (O(1) Fast-Path).
   • If absent ──► Fallback to Bearer JWT ──► Calls JwtPublicKeyValidator.validateToken().
   • Validated claims (userId, roles, permissions) are attached to ServerWebExchange attributes.
2. InternalHmacSignerFilter (Order: -50) runs:
   • Strips any incoming spoofed client headers (X-User-Id, X-User-Roles, X-Internal-HMAC).
   • Reads exchange attributes (userId, roles, permissions).
   • Computes current timestamp: currentTimeStamp = Instant.now().getEpochSecond().
   • Creates HMAC payload: "timestamp::userId::roles::permissions".
   • Generates signature: hmacUtils.hmacHex(payloadToSign).
   • Mutates request adding headers:
     X-User-Id, X-User-Roles, X-User-Permissions, X-Timestamp, X-Internal-HMAC.
3. Spring Cloud Gateway routes request to downstream (e.g. http://business-operation-service:8082).
```

#### C. Gateway Architect Notes: Why No `SecurityConfig` & `OncePerRequestFilter` vs `GlobalFilter`

* **Sawaal: Baaki services me `SecurityConfig` banayi, Gateway me kyu nahi?**
  1. **Engine alag hai:** Baaki services Spring MVC (Tomcat) par chalti hain (`HttpServletRequest` & `SecurityFilterChain`). Gateway **Spring Cloud Gateway (WebFlux + Netty)** par chalta hai (`ServerWebExchange`, `Mono<Void>`).
  2. **Role alag hai:** Gateway ke paas apna koi business controller nahi hota jise `@PreAuthorize` se protect karna ho; uska kaam bas traffic inspect, strip aur route karna hai.
  3. **High Performance:** Native `GlobalFilter` direct Netty event loop par bina kisi framework overhead ke ultra-fast ($<0.2\text{ms}$) execute hote hain.

* **`OncePerRequestFilter` vs `GlobalFilter` Quick Difference:**

| Feature | `OncePerRequestFilter` | `GlobalFilter` |
| :--- | :--- | :--- |
| **Framework** | **Spring MVC / Servlet** (Tomcat) | **Spring Cloud Gateway / WebFlux** (Netty) |
| **I/O Model** | Blocking / Thread-per-request | Non-blocking / Reactive Event Loop |
| **Objects** | `HttpServletRequest`, `HttpServletResponse` | `ServerWebExchange`, `GatewayFilterChain` |
| **Return Type** | `void` | `Mono<Void>` |
| **Guarantee** | Ek incoming request par **sirf 1 baar** chalega | Saare configured routes par globally apply hoga |
| **Project Example** | `JwtAuthenticationFilter`, `InternalHmacFilter` | `BffAuthenticationFilter`, `InternalHmacSignerFilter` |

---

#### D. Line-by-Line Code Meaning: Gateway Security Core

Gateway perimeter par teen critical components milkar Zero-Trust enforce karte hain:
1. **`JwtPublicKeyValidator.java`**: Remote Auth-Service se Asymmetric Public Key dynamically download karta hai aur RS256 token verify karta hai (Private key = `null`).
2. **`BffAuthenticationFilter.java`**: GlobalFilter (Order: -100). BFF pattern enforce karta hai: Redis `APP_SESSION` cookie fast-path ($<0.2\text{ms}$), Bearer fallback token promotion, aur instant logout.
3. **`InternalHmacSignerFilter.java`**: GlobalFilter (Order: -50). Client headers strip karta hai aur `X-Internal-HMAC` signature calculate karke downstream inject karta hai.

---

##### 1. `JwtPublicKeyValidator.java` (`gateway-service`) — Asymmetric Public Key Verification

```java
@Component
public class JwtPublicKeyValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtPublicKeyValidator.class);

    private final WebClient webClient;
    private final GatewaySecurityProperties securityProperties;
    private JWTVerifier verifier;

    public JwtPublicKeyValidator(WebClient.Builder webClientBuilder,
                                 GatewaySecurityProperties securityProperties) {
        this.webClient = webClientBuilder.build();
        this.securityProperties = securityProperties;
    }

    public ValidatedClaims validate(String token) {
        try {
            ensureVerifierInitialized();
            DecodedJWT decodedJWT = verifier.verify(token);
            String userId = decodedJWT.getSubject();
            String email = decodedJWT.getClaim("email").asString();
            List<String> roles = decodedJWT.getClaim("roles").asList(String.class);
            List<String> permissions = decodedJWT.getClaim("permissions").asList(String.class);
            return new ValidatedClaims(userId, email, roles != null ? roles : List.of(), permissions != null ? permissions : List.of());
        } catch (JWTVerificationException ex) {
            log.warn("Invalid or expired JWT token: {}", ex.getMessage());
            return null;
        } catch (Exception ex) {
            log.error("Failed to verify JWT token", ex);
            return null;
        }
    }

    private synchronized void ensureVerifierInitialized() {
        if (this.verifier != null) {
            return;
        }

        try {
            Map<String, String> response = webClient.get().uri(securityProperties.userServiceUri() + "/api/v1/auth/public-key")
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                    .block();
            if (response == null || !response.containsKey("publicKey")) {
                throw new IllegalStateException("Failed to obtain public key from user-rbac-service");
            }

            String publicKeyBase64 = response.get("publicKey");
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(encodedKeySpec);

            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            this.verifier = JWT.require(algorithm).withIssuer("user-rbac-service").build();

            log.info("Successfully fetched public key from user-rbac-service [{}] and initialized RS256 verifier", securityProperties.userServiceUri());
        } catch (Exception e) {
            log.error("Could not fetch public key from user-rbac-service on {}", securityProperties.userServiceUri(), e);
            throw new IllegalStateException("Authentication service unavailable", e);
        }
    }
}
```

* **Line-by-Line Breakdown:**
  * **`private JWTVerifier verifier`**: Auth0 ka thread-safe cryptographic verifier object jo token verify karega.
  * **`validate(String token)`**: External calling method jo token lega aur verified `ValidatedClaims` DTO return karega.
  * **`ensureVerifierInitialized()`**: Lazy Initialization pattern. Pehli request aane par yeh check karta hai ki kya Public Key already cached hai. Agar nahi, toh remote call karega. `synchronized` keyword race condition rokta hai.
  * **`webClient.get().uri(... + "/api/v1/auth/public-key").retrieve().bodyToMono(...).block()`**: `user-rbac-service` ke public key endpoint ko call karta hai aur Base64 encoded RSA public key download karta hai.
  * **`Base64.getDecoder().decode(...)` & `X509EncodedKeySpec`**: Base64 string ko standard X.509 ASN.1 binary format mein decode karta hai.
  * **`KeyFactory.getInstance("RSA").generatePublic(...)`**: Java Security standard factory se official `RSAPublicKey` object banata hai.
  * **`Algorithm.RSA256(publicKey, null)`**: **The Masterstroke!** Notice: 2nd argument (Private Key) `null` hai! Gateway ke paas naya token banane ki koi chaabi nahi hai; sirf verify karne ki Public Key hai.
  * **`JWT.require(algorithm).withIssuer("user-rbac-service").build()`**: Verifier lock kar diya jo sirf `"user-rbac-service"` ke issue kiye hue tokens ko hi valid maaneyga.
  * **`verifier.verify(token)`**: Signature, token expiration, aur issuer check karta hai. Tampered ya expired token par `JWTVerificationException` throw hoti hai aur method `null` return karta hai.
  * **`decodedJWT.getSubject()` & `.getClaim("permissions")`**: Token payload se `userId`, `roles`, aur `permissions` nikaal kar immutable `ValidatedClaims` record return kar deta hai.

---

##### 2. `BffAuthenticationFilter.java` (`gateway-service`) — BFF Session & Fast-Path Filter

```java
@Component
public class BffAuthenticationFilter implements GlobalFilter, Ordered {

    public static final String SESSION_COOKIE_NAME = "APP_SESSION";
    private static final String SESSION_KEY_PREFIX = "session:";
    private static final Duration SESSION_TTL = Duration.ofHours(24);

    private static final List<String> PUBLIC_URLS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/public-key",
            "/actuator"
    );

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    private final JwtPublicKeyValidator jwtPublicKeyValidator;
```

* **Line-by-Line Breakdown (Constants & Setup):**
  * **`implements GlobalFilter, Ordered`**: Spring Cloud Gateway ka reactive filter jo har route par execute hoga. `getOrder() = -100` isse HMAC signing filter (`-50`) se pehle chalata hai.
  * **`SESSION_COOKIE_NAME = "APP_SESSION"`**: Browser cookie ka naam jo user ki actual opaque session ID hold karegi.
  * **`SESSION_KEY_PREFIX = "session:"` & `SESSION_TTL = 24 Hours`**: Redis hash key structure (`session:<uuid>`) aur uski expiration life.
  * **`PUBLIC_URLS`**: Endpoints jahan bina kisi session ya token ke request bypass ho sakti hai.

```java
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1. Session Revocation / Logout
        if ("/api/v1/auth/logout".equalsIgnoreCase(path)) {
            return handleLogout(exchange);
        }

        // 2. Bypass public authentication & actuator routes
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // 3. Fast-Path: Check for BFF Opaque Session Cookie (Sub-millisecond Redis read)
        HttpCookie appSessionCookie = exchange.getRequest().getCookies().getFirst(SESSION_COOKIE_NAME);
        if (appSessionCookie != null && !appSessionCookie.getValue().isBlank()) {
            return validateAppSessionCookie(exchange, chain, appSessionCookie.getValue());
        }

        // 4. Fallback: Validate Bearer JWT, Establish Redis Session, and Issue APP_SESSION Cookie
        return validateBearerToken(exchange, chain, path);
    }
```

* **Line-by-Line Breakdown (`filter` Main Router):**
  * **`path = exchange.getRequest().getURI().getPath()`**: Current HTTP request ka URL path nikalta hai.
  * **`if ("/api/v1/auth/logout")`**: Agar logout request aayi, toh downstream call rokk kar turant `handleLogout` chalu karta hai.
  * **`if (isPublicPath(path))`**: Public paths ke liye seedha `chain.filter(exchange)` karke aage bhej deta hai.
  * **`exchange.getRequest().getCookies().getFirst(...)`**: Browser se aane wali cookies me `APP_SESSION` dhoondhta hai.
  * **Fast-Path vs Fallback**: Agar cookie mili toh sub-millisecond Redis lookup (`validateAppSessionCookie`), agar cookie nahi mili toh JWT token fallback (`validateBearerToken`).

```java
    private Mono<Void> validateAppSessionCookie(ServerWebExchange exchange, GatewayFilterChain chain, String sessionId) {
        return reactiveStringRedisTemplate.opsForHash().entries(SESSION_KEY_PREFIX + sessionId)
                .collectMap(entry -> (String) entry.getKey(), entry -> (String) entry.getValue())
                .flatMap(sessionData -> {
                    if (sessionData.isEmpty() || !sessionData.containsKey("userId")) {
                        expireSessionCookie(exchange);
                        return unauthorized(exchange);
                    }

                    exchange.getAttributes().put("userId", sessionData.get("userId"));
                    exchange.getAttributes().put("roles", sessionData.getOrDefault("roles", ""));
                    exchange.getAttributes().put("permissions", sessionData.getOrDefault("permissions", ""));
                    exchange.getAttributes().put("email", sessionData.getOrDefault("email", ""));
                    return chain.filter(exchange);
                });
    }
```

* **Line-by-Line Breakdown (`validateAppSessionCookie` Fast-Path):**
  * **`reactiveStringRedisTemplate.opsForHash().entries(...)`**: Non-blocking reactive call jo Redis se pure session map ko fetch karti hai ($<0.2\text{ms}$).
  * **`if (sessionData.isEmpty() ...)`**: Agar session expire ho chuka hai ya Redis me exist nahi karta, toh browser ki stale cookie ko expire karta hai aur `HTTP 401 Unauthorized` return karta hai.
  * **`exchange.getAttributes().put(...)`**: Redis se mile verified claims ko current reactive exchange ke memory attributes me daal deta hai taaki downstream filters (jaise HMAC signer) inhe use kar sakein.
  * **`chain.filter(exchange)`**: Request ko next filter par pass kar deta hai.

```java
    private Mono<Void> validateBearerToken(ServerWebExchange exchange, GatewayFilterChain chain, String path) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            ValidatedClaims claims = jwtPublicKeyValidator.validate(token);
            if (claims == null) {
                return unauthorized(exchange);
            }

            // Auto-Promotion to BFF Session:
            String sessionId = UUID.randomUUID().toString();
            String sessionKey = SESSION_KEY_PREFIX + sessionId;
            Map<String, String> sessionData = Map.of(
                    "userId", claims.userId(),
                    "roles", String.join(",", claims.roles()),
                    "permissions", String.join(",", claims.permissions()),
                    "email", claims.email() != null ? claims.email() : ""
            );

            ResponseCookie sessionCookie = ResponseCookie.from(SESSION_COOKIE_NAME, sessionId)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(SESSION_TTL)
                    .sameSite("Lax")
                    .build();
            exchange.getResponse().addCookie(sessionCookie);

            return reactiveStringRedisTemplate.opsForHash().putAll(sessionKey, sessionData)
                    .then(reactiveStringRedisTemplate.expire(sessionKey, SESSION_TTL))
                    .then(chain.filter(exchange));
        }
        return unauthorized(exchange);
    }
```

* **Line-by-Line Breakdown (`validateBearerToken` Fallback & Auto-Promotion):**
  * **`jwtPublicKeyValidator.validate(token)`**: Asymmetric public key se RSA signature verify karta hai.
  * **Session Auto-Promotion**: Agar token valid hai, toh Gateway ek naya `UUID` session ID generate karta hai.
  * **`ResponseCookie.from("APP_SESSION", sessionId).httpOnly(true)...`**: Browser ke liye `HttpOnly` cookie issue karta hai. JavaScript is cookie ko read nahi kar sakti (XSS attack immune!).
  * **`reactiveStringRedisTemplate...putAll(...).then(...expire(...))`**: Session claims ko Redis hash me store karta hai aur 24 hour TTL set karke request aage forward kar deta hai. Agli baar se user ko heavy JWT verify karne ki zaroorat nahi padegi!

```java
    private Mono<Void> handleLogout(ServerWebExchange exchange) {
        HttpCookie appSessionCookie = exchange.getRequest().getCookies().getFirst(SESSION_COOKIE_NAME);
        expireSessionCookie(exchange);

        Mono<Void> purgeRedisSession = Mono.empty();
        if (appSessionCookie != null && !appSessionCookie.getValue().isBlank()) {
            purgeRedisSession = reactiveStringRedisTemplate.delete(SESSION_KEY_PREFIX + appSessionCookie.getValue()).then();
        }

        return purgeRedisSession.then(Mono.defer(() -> {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            byte[] bytes = "{\"message\":\"Logged out successfully\"}".getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }));
    }
```

* **Line-by-Line Breakdown (`handleLogout` Instant Revocation):**
  * **`expireSessionCookie(exchange)`**: Browser me `maxAge(0)` wali cookie inject karta hai taaki browser cookie delete kar de.
  * **`reactiveStringRedisTemplate.delete(...)`**: Redis se session key turant destroy karta hai (Instant Server-side Logout).
  * **`exchange.getResponse().writeWith(...)`**: Gateway seedha `HTTP 200 {"message":"Logged out successfully"}` JSON response return karta hai bina downstream ko call kiye.

---

##### 3. `InternalHmacSignerFilter.java` (`gateway-service`) — Downstream HMAC Signing Filter

```java
@Component
public class InternalHmacSignerFilter implements GlobalFilter, Ordered {

    private final HmacUtils hmacUtils;

    public InternalHmacSignerFilter(GatewaySecurityProperties securityProperties) {
        this.hmacUtils = new HmacUtils(
                HmacAlgorithms.HMAC_SHA_256,
                securityProperties.internalHmacSecret().getBytes(StandardCharsets.UTF_8)
        );
    }
```

* **Line-by-Line Breakdown (Setup):**
  * **`getOrder() = -50`**: BffAuthenticationFilter (`-100`) ke baad execute hota hai taaki exchange attributes me verified claims mil sakein.
  * **`HmacUtils(HMAC_SHA_256, secret)`**: Apache Commons Codec ka fast cryptographic hasher initialize karta hai shared internal secret ke sath.

```java
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String userId = exchange.getAttribute("userId");
        String roles = exchange.getAttribute("roles");
        String permissions = exchange.getAttribute("permissions");

        if (userId == null) {
            return chain.filter(exchange); // Public endpoint, bypass signing
        }

        long currentTimeStamp = Instant.now().getEpochSecond();
        String payloadToSign = currentTimeStamp + "::" + userId + "::" + (roles != null ? roles : "") + "::" + (permissions != null ? permissions : "");
        String hmacSignature = hmacUtils.hmacHex(payloadToSign);
```

* **Line-by-Line Breakdown (Signature Calculation):**
  * **`exchange.getAttribute(...)`**: BffAuthenticationFilter dwara verify kiye gaye `userId`, `roles`, aur `permissions` read karta hai.
  * **`if (userId == null)`**: Agar request unauthenticated public endpoint (jaise login) par ja rahi hai, toh signing bypass karke aage bhej deta hai.
  * **`currentTimeStamp = Instant.now().getEpochSecond()`**: Current UNIX epoch timestamp nikalta hai.
  * **`payloadToSign = timestamp::userId::roles::permissions`**: Canonical payload string construct karta hai.
  * **`hmacUtils.hmacHex(payloadToSign)`**: Shared secret se HMAC-SHA256 hex string signature generate karta hai.

```java
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .headers(httpHeaders -> {
                    httpHeaders.remove("X-User-Id");
                    httpHeaders.remove("X-User-Roles");
                    httpHeaders.remove("X-User-Permissions");
                    httpHeaders.remove("X-Internal-HMAC");
                    httpHeaders.remove("X-Timestamp");
                })
                .header("X-User-Id", userId)
                .header("X-User-Roles", roles != null ? roles : "")
                .header("X-User-Permissions", permissions != null ? permissions : "")
                .header("X-Timestamp", String.valueOf(currentTimeStamp))
                .header("X-Internal-HMAC", hmacSignature)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
}
```

* **Line-by-Line Breakdown (Header Mutation & Zero-Trust):**
  * **`exchange.getRequest().mutate()`**: Netty request ko modify karne ke liye builder kholta hai.
  * **`httpHeaders.remove(...)`**: **Zero-Trust Spoofing Defense!** Agar bahar ke kisi malicious client ne khud `X-User-Id: admin` ya fake `X-Internal-HMAC` header bheja ho, toh Gateway use **turant delete (strip)** kar deta hai.
  * **`.header("X-User-Id", userId)...`**: Gateway apne dwara freshly verify kiye gaye claims aur signature inject karta hai.
  * **`chain.filter(exchange.mutate().request(mutatedRequest).build())`**: Tamper-proof signed request ko downstream microservice (jaise `business-operation-service`) ki taraf forward kar deta hai!

---

### 3. `business-operation-service` (Port 8082 - Core Banking Engine)

#### A. Architecture Mapping Table:
```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                   business-operation-service (Architecture Mapping)                    │
├───────────────────────────────────┬────────────────────────────────────────────────────┤
│ Class Name                        │ Responsibility in Project                          │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ SecurityConfig.java               │ Enables @EnableWebSecurity & @EnableMethodSecurity.│
│                                   │ Sets stateless session and registers HMAC filter   │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ InternalHmacFilter.java           │ Verifies Gateway HMAC signature, checks clock skew,│
│                                   │ and loads SimpleGrantedAuthority into context      │
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ BankingController.java            │ REST Endpoints guarded by fine-grained authority:  │
│                                   │ • /transfers ──► @hasAuthority('transfer:execute') │
│                                   │ • /accounts  ──► @hasAuthority('account:read')     │
│                                   │ • /cards     ──► @hasAuthority('card:update_status')│
├───────────────────────────────────┼────────────────────────────────────────────────────┤
│ GlobalExceptionHandler.java       │ Handles AccessDeniedException ──► HTTP 403         │
└───────────────────────────────────┴────────────────────────────────────────────────────┘
```

#### B. Execution Flow: Pehle Kaun, Baad Mein Kaun?
```
🟡 PHASE 1: STARTUP (Servlet Bootstrap - Ek Baar Chalti Hai):
1. SecurityProperties loads internalHmacSecret (from K8s Secret / Azure Key Vault).
2. InternalHmacFilter bean is created with the secret.
3. SecurityConfig executes:
   • Registers InternalHmacFilter before UsernamePasswordAuthenticationFilter.
   • Session set to STATELESS, CSRF disabled.
   • .authorizeHttpRequests(anyRequest().authenticated()).
   • .build() completes ──► SecurityFilterChain Bean saved in RAM.
4. Business service ready on Port 8082!

─────────────────────────────────────────────────────────────────────────────────────────

🟢 PHASE 2: RUNTIME (Jab Gateway Se Request Aati Hai):
Gateway calls: POST /api/v1/banking/transfers (with X-Internal-HMAC header)
1. SecurityConfig NAHI chalti!
2. InternalHmacFilter runs first:
   • Reads X-Timestamp ──► Checks Math.abs(now - timestamp) <= 300s (Replay attack check).
   • Reads X-Internal-HMAC, X-User-Id, X-User-Roles, X-User-Permissions.
   • Re-computes expected HMAC on payload: "timestamp::userId::roles::permissions".
   • Uses MessageDigest.isEqual(expected, actual) ──► Constant-time safe verification!
   • If signature fails ──► Clears context, returns 401 Unauthorized immediately.
   • If signature passes ──► Parses permissions into List<SimpleGrantedAuthority>.
   • Sets SecurityContextHolder.getContext().setAuthentication(passport).
   • Calls filterChain.doFilter().
3. AuthorizationFilter verifies user is authenticated ──► Passes!
4. BankingController.executeTransfer() reached.
5. Spring AOP inspects: @PreAuthorize("hasAuthority('transfer:execute')").
   • User authorities contain "transfer:execute"? ──► YES!
6. BankingServiceImpl executes transactional transfer & persists to MongoDB.
```

---

### 4. `kpi-analytics-service` (Port 8084) & `chat-communication-service` (Port 8083)

#### A. Architecture Mapping & Lifecycle:
* **Startup Phase:**
  * Dono services ke andar `SecurityConfig.java` chalti hai aur `InternalHmacFilter` ko register karke `SecurityFilterChain` build karti hai.
* **Runtime Phase:**
  * Har incoming request (chahe REST query ho ya WebSocket upgrade handshake) ko `InternalHmacFilter` intercept karta hai.
  * Signature aur timestamp verify hone ke baad authorities inject hoti hain:
    * `kpi-analytics-service`: Controller check karta hai `@PreAuthorize("hasAuthority('kpi:read_analytics')")`.
    * `chat-communication-service`: Controller check karta hai `@PreAuthorize("hasAuthority('ai:chat')")`.
* **Zero-Trust Hardening:**
  * K8s `NetworkPolicy` lagne ke baad, agar koi pod bina `gateway-service` ke direct in services ko ping karega, toh TCP layer par hi packet drop ho jayega!

---

## 🎭 Chapter 6: The Enum Trap aur Dynamic PBAC

### Pehle Hamara Code kaisa tha? (The Enum Trap)
```java
// ❌ PURANA DESIGN: Hardcoded Enum
public enum Role {
    ROLE_USER(Set.of("account:read")),
    ROLE_ADMIN(Set.of("account:read", "user:manage_roles"));
}
```
**Problem:**
- Agar bank bole: *"Hume ek naya role `ROLE_BRANCH_MANAGER` banana hai"*, toh Java code change karna padega, recompile karna padega, Docker image banani padegi, aur server restart karna padega!
- Agar MongoDB mein kisi ne naya role daal diya, toh MapStruct ka `Enum.valueOf()` **`IllegalArgumentException` phenk kar server crash kar deta tha!**

---

### Ab Hamara Code kaisa hai? (Dynamic Database PBAC)

```
┌─────────────────┐       POST /api/v1/roles       ┌──────────────────────────────────────┐
│ Admin Dashboard │───────────────────────────────►│ RoleController (user-rbac-service)  │
└─────────────────┘                                └──────────────────┬───────────────────┘
                                                                      │
                                        ┌─────────────────────────────┴────────────────────────┐
                                        │                                                      │
                                        ▼                                                      ▼
                       ┌────────────────────────────────┐                    ┌────────────────────────────────┐
                       │   MongoDB: roles collection    │                    │          Redis Cache           │
                       │   - name: ROLE_CUSTOM          │                    │   Key: roles:ROLE_CUSTOM       │
                       │   - permissions: [acc:read]    │                    │   (Fast O(1) Cache Hit)        │
                       └────────────────────────────────┘                    └────────────────────────────────┘
```

#### Humne 3 Cheezein Badli:
1. `Role.java` ko enum se hatakar **Domain Entity** banaya, aur database mein `roles` collection banayi.
2. `User.java` mein ab `Set<String> roles` hai (Strings like `"ROLE_USER"`, koi enum dependency nahi).
3. **Controller ko Role ka naam pata hi nahi hona chahiye!**
   - ❌ Galat: `@PreAuthorize("hasRole('ADMIN')")`
   - ✅ Sahi: `@PreAuthorize("hasAuthority('account:read')")`
   - Kal ko tum 100 naye roles banao, agar unke paas `account:read` hai, toh code bina chhue chalega!

---

## 🚀 Chapter 7: Next Frontiers (Apna Khud ka OAuth2 Server & mTLS)

### 7.1 Kya hum apna khud ka OAuth2 Server bana sakte hain?
**HAAN! 100%!** Tumhe Okta ya Auth0 ko paise dene ki zaroorat nahi hai.

Spring ka official project hai: **Spring Authorization Server**:
```
┌────────────────────────────────────────────────────────┐
│ user-rbac-service (Apna Khud ka OAuth2 Server)        │
│                                                        │
│ • POST /oauth2/token  (Standard RFC Token Issuance)    │
│ • GET  /oauth2/jwks   (Standard Public Keys Publish)   │
└───────────────────────────┬────────────────────────────┘
                            │
               Auto-fetches JWKS Public Key
                            │
            ┌───────────────┴───────────────┐
            ▼                               ▼
┌────────────────────────┐      ┌────────────────────────┐
│ business-operation-srv │      │  kpi-analytics-service │
│ (OAuth2 Resource Serv) │      │ (OAuth2 Resource Serv) │
└────────────────────────┘      └────────────────────────┘
```

Jab hum baaki services ko **Spring Resource Server** banate hain, toh unke `application.yml` mein sirf 1 line likhni hoti hai:
```yaml
spring.security.oauth2.resourceserver.jwt.jwk-set-uri: http://user-rbac-service:8081/oauth2/jwks
```
Spring Boot boot hote hi public key download karta hai, token verify karta hai, aur SecurityContext bhar deta hai—**hume ek bhi custom filter likhne ki zaroorat nahi padti!**

---

### 7.2 mTLS (Mutual TLS) Kya Hota Hai?

Standard HTTPS mein sirf **Server** apna certificate dikhata hai.
**mTLS mein dono pod ek doosre ko apna identity card dikhate hain:**

```
┌───────────────────────┐                                       ┌───────────────────────┐
│ Banking Service (Pod) │                                       │  KPI Service (Pod)    │
│ (Has cert-banking.pem)│                                       │  (Has cert-kpi.pem)   │
└───────────┬───────────┘                                       └───────────┬───────────┘
            │                                                               │
            │ 1. TCP Connect (ClientHello)                                  │
            ├──────────────────────────────────────────────────────────────►│
            │                                                               │
            │ 2. ServerHello + Server Certificate (cert-kpi.pem)            │
            │◄──────────────────────────────────────────────────────────────┤
            │                                                               │
            │ 3. Banking Service verifies: "KPI cert is signed by Bank CA!" │
            │                                                               │
            │ 4. "Now send YOUR certificate!" (CertificateRequest)          │
            │◄──────────────────────────────────────────────────────────────┤
            │                                                               │
            │ 5. Sends Client Certificate (cert-banking.pem)                │
            ├──────────────────────────────────────────────────────────────►│
            │                                                               │
            │ 6. KPI Service verifies: "Banking cert is signed by Bank CA!" │
            │                                                               │
            │ 7. HANDSHAKE SUCCESS! Wire traffic is 100% Encrypted.         │
            │◄═════════════════════════════════════════════════════════════►│
```
Isse agar koi hacker Kubernetes cluster ke andar koi rogue container run bhi kar de, toh woh baaki microservices se baat nahi kar sakta kyunki uske paas company ke CA se signed certificate nahi hoga!

---

## 🧰 Chapter 8: The Architect's Survival Kit

### 8.1 Top 4 Spring Security Pitfalls (Aur Unka Tod)

```
┌────────────────────────────────────────┬────────────────────────────────────────┐
│ Khatra (Pitfall)                       │ Tod (Fix)                              │
├────────────────────────────────────────┼────────────────────────────────────────┤
│ 1. @PreAuthorize kaam nahi kar raha,   │ SecurityConfig par                     │
│    sabko allow kar raha hai!           │ @EnableMethodSecurity lagao.           │
├────────────────────────────────────────┼────────────────────────────────────────┤
│ 2. Access Denied hone par frontend ko  │ GlobalExceptionHandler mein            │
│    500 Internal Server Error dikh rha h│ AccessDeniedException ko catch karke   │
│                                        │ 403 Forbidden return karo.             │
├────────────────────────────────────────┼────────────────────────────────────────┤
│ 3. Signature verify karte time timing  │ Kabhi String.equals() use mat karo!    │
│    attack ka risk.                     │ MessageDigest.isEqual() use karo.      │
├────────────────────────────────────────┼────────────────────────────────────────┤
│ 4. Header spoofing (X-User-Id: admin)  │ Gateway perimeter par client ke aane   │
│                                        │ wale saare X-User-* headers strip karo.│
└────────────────────────────────────────┴────────────────────────────────────────┘
```

---

### 8.2 Brain Power: Sharpen Your Pencil ✏️

**Sawaal:**
Ek developer ne code likha:
```java
// Filter mein:
List<SimpleGrantedAuthority> auths = List.of(new SimpleGrantedAuthority("ADMIN"));
SecurityContextHolder.getContext().setAuthentication(
    new UsernamePasswordAuthenticationToken("user", null, auths)
);

// Controller par:
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser() { ... }
```
Jab user call karta hai, toh Spring Security usko **HTTP 403 Forbidden** phenk ke maar deti hai! Kyu?

**Jawab:**
Spring Security ka `hasRole('ADMIN')` method automatically authority ke aage **`ROLE_`** prefix dhoondhta hai (`ROLE_ADMIN`)! 
Kyunki filter ne sirf `"ADMIN"` save kiya tha bina prefix ke, isliye match fail ho gaya!
- **Fix:** Ya toh filter mein `new SimpleGrantedAuthority("ROLE_ADMIN")` karo.
- **Better Fix:** Controller par `@PreAuthorize("hasAuthority('ADMIN')")` use karo!

---

### 8.3 The Quick Revision Cheat Sheet

| Question | Answer in One Sentence |
| :--- | :--- |
| **Authentication vs Authorization?** | Authentication = Tum kaun ho; Authorization = Tumhe kya karne ka haq hai. |
| **SecurityContextHolder kya hai?** | Ek `ThreadLocal` storage box jo current request ke user passport ko hold karta hai. |
| **CSRF stateless APIs mein kyu disable karte hain?** | Kyunki Bearer tokens ambient cookies ki tarah browser se automatically attach nahi hote. |
| **RS256 vs HS256?** | RS256 asymmetric hai (Private key sign karti hai, Public key verify karti hai). HS256 symmetric hai (Same secret dono jagah share hota hai). |
| **X-Timestamp kyu use karte hain HMAC mein?** | Replay attack rokne ke liye (300 seconds se puraani request reject ho jati hai). |
| **PBAC kyu RBAC se behtar hai?** | Roles business requirement ke hisaab se badalte rehte hain; capabilities (`account:read`) code se match karti hain aur stable rehti hain. |
| **mTLS kya hai?** | Mutual TLS, jisme client aur server dono X.509 certificate se ek doosre ki machine identity verify karte hain. |
| **Gateway me SecurityConfig kyu nahi banayi?** | Gateway reactive Netty engine par chalta hai aur traffic forward karta hai; native `GlobalFilter` bina framework overhead ke ultra-fast chalte hain. |
| **OncePerRequestFilter vs GlobalFilter?** | `OncePerRequestFilter` Tomcat/MVC mein per-request 1-bar execution guarantee karta hai; `GlobalFilter` Spring Cloud Gateway mein reactive routes par apply hota hai. |
| **Microservices me mTLS lagane ka best way kya hai?** | Java mein manual Keystore/TrustStore ke bajaye Kubernetes Service Mesh (AKS Managed Istio) sidecar proxies se automatic cert rotation ke sath lagana. |

---

> 🎓 **Alex's Golden Advice**:
> *"Distributed architecture mein kabhi kisi par andha vishwas mat karo (Zero-Trust). Bahar se aane wali har request ko verify karo, internal network par tamper-proof sign karo, aur controllers mein hardcoded roles ki jagah dynamic permissions enforce karo!"*
