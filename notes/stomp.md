# Head First WebSockets & STOMP in Spring Boot 🛰️
*(Pure Technical Guide: Architecture, Pipeline, Frames, Destinations & Code)*

> *"Raw WebSocket sirf ek dumb TCP pipe (cable) hai jisme bytes beh sakte hain. STOMP (Simple Text Oriented Messaging Protocol) us pipe par chalne wala Layer-7 protocol hai — jisme HTTP ki tarah Commands, Headers, Destinations (Topics/Queues), aur Payload body hoti hai!"*

---

## 🧭 Visual Roadmap: Hum Kya Seekhne Wale Hain?

```
┌────────────────────────────────────────────────────────────────────────┐
│                        TECHNICAL LEARNING ROADMAP                      │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  1. The Core Engineering Problem: Raw WebSocket vs STOMP               │
│     - Raw WebSocket kyu dumb pipe hai?                                 │
│     - STOMP Frame Anatomy (COMMAND + Headers + Body)                   │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  2. Spring STOMP Pipeline Architecture (Pure Tech Flow)                │
│     - HTTP Handshake vs STOMP Connection vs Broker Pipeline            │
│     - 6 Core Technical Classes & Interfaces                            │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  3. Destinations Demystified (/app, /topic, /queue, /user)             │
│     - Inbound Routing vs Broker Broadcast                              │
│     - /user Magic: Spring UserDestinationMessageHandler translation    │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  4. The Complete Annotation Reference                                  │
│     - @MessageMapping, @SubscribeMapping                               │
│     - @SendTo, @SendToUser (Multi-device broadcast flag)               │
│     - @MessageExceptionHandler (Kyu @ExceptionHandler fail hota hai)   │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  5. Step-by-Step Clean Implementation (Production Code)                │
│     - Configurer -> Interceptor -> Handler -> Controller -> Listener   │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  6. Production Scaling (Multi-Pod Architecture)                        │
│     - In-Memory SimpleBroker ka limitation                             │
│     - External Broker Relay (RabbitMQ) vs Redis Pub/Sub Bridge         │
│     - Frame-level Security via ChannelInterceptor                      │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 🔌 1. The Core Engineering Problem: Raw WebSocket vs STOMP

### Raw WebSocket Ek "Dumb Pipe" Kyu Hai?

Jab browser aur server ke beech Raw WebSocket connection banta hai, toh dono ke beech ek **persistent, bi-directional TCP socket** khul jata hai.

Lekin dikkat yeh hai:
1. **No Routing:** WebSocket ke paas URL ya Destination ka concept nahi hota. Agar Client server ko ek text message bhejta hai, server ko nahi pata chal sakta ki yeh Chat Room 101 ke liye hai ya User B ke liye private message hai!
2. **No Headers:** Aap har message ke sath authentication token, session ID ya content-type nahi bhej sakte.
3. **Manual JSON Parsing Nightmare:** Developer ko har message ke andar custom JSON structure banana padta tha:
   ```json
   { "action": "SUBSCRIBE", "room": "crypto", "data": "..." }
   ```
   Aur server par lambe `switch/case` ya `if/else` likh kar manually handle karna padta tha.

### 🌐 1. Fundamental Concept: HTTP vs WebSocket vs STOMP

#### A. HTTP Ki Problem (Kyu Chat Apps HTTP Par Nahi Bante?)
HTTP ek **Request-Response** protocol hai:
* Client bolta hai: *"Mujhe naya message do"* (Request)
* Server bolta hai: *"Yeh lo message"* ya *"Koi naya message nahi hai"* (Response)
* Aur connection **close** ho jata hai!

Agar tumhe real-time chat banana hai, toh client ko har 1 second me server se poochna padega: *"Naya message aaya kya? Naya message aaya kya?"* (Isse **HTTP Polling** kehte hain).
* ❌ Server par lakho useless requests ka load.
* ❌ Har request me heavy HTTP headers (Cookie, User-Agent) travel karte hain (bandwidth waste).
* ❌ Server client ko apni taraf se proactively message nahi bhej sakta jab tak client na maange.

#### B. WebSocket Kya Hai? (The Persistent Full-Duplex Pipe)
WebSocket ek aisi technology hai jo browser aur server ke beech ek **single, permanent TCP connection (pipe)** khol deti hai:
* Ek bar connection ban gaya, toh pipe **24x7 khula rehta hai**.
* **Full-Duplex:** Client aur Server dono ek hi time par bina ruke ek doosre ko data bhej sakte hain.
* Server ko jab bhi naya message milega, wo turant client ke mobile/browser par push kar dega!
* **Problem:** WebSocket ek **"Dumb Pipe"** hai. Yeh sirf raw text ya binary bytes bhejta hai. Ise yeh nahi pata hota ki message group me bhejna hai ya private, aur user subscribe kar raha hai ya publish.

---

### 🤝 2. Handshake Deep Dive
*(Kab hota hai? Kaise hota hai? Server aur Client side kya chalta hai?)*

#### Handshake Kya Hai?
Handshake woh **one-time process** hai jisme browser aur server aapas me agree karte hain ki:
> *"Bhai, hum standard HTTP connection ko band karke ek 2-way continuous WebSocket pipe me convert kar rahe hain!"*

#### Yeh Kab Hota Hai?
Jab user chat app open karta hai aur client side se WebSocket connect karne ka code trigger hota hai (jaise `new SockJS('/ws')` ya `new WebSocket('ws://...')`).

#### Kaise Hota Hai? (Step-by-Step Technical Flow)

```text
CLIENT (Browser)                                              SERVER (Spring Boot)
      │                                                                │
      │ 1. HTTP GET /ws (Upgrade: websocket)                           │
      ├───────────────────────────────────────────────────────────────>│
      │    [Headers: Connection: Upgrade, Sec-WebSocket-Key: abc...]   │
      │                                                                ▼
      │                                                  ┌───────────────────────────┐
      │                                                  │ 2. HandshakeInterceptor   │
      │                                                  │    - Token/Cookie read    │
      │                                                  │    - attributes.put(user) │
      │                                                  └─────────────┬─────────────┘
      │                                                                │
      │                                                                ▼
      │                                                  ┌───────────────────────────┐
      │                                                  │ 3. HandshakeHandler       │
      │                                                  │    - User identify        │
      │                                                  │    - Principal bind       │
      │                                                  └─────────────┬─────────────┘
      │                                                                │
      │ 4. HTTP 101 Switching Protocols                                │
      │<───────────────────────────────────────────────────────────────┤
      │                                                                │
      ══════════════ [ HTTP CLOSED -> TCP WEBSOCKET OPEN ] ═════════════
      │                                                                │
      │ 5. STOMP Frame: CONNECT                                        │
      ├───────────────────────────────────────────────────────────────>│
      │                                                                │
      │ 6. STOMP Frame: CONNECTED                                      │
      │<───────────────────────────────────────────────────────────────┤
      ▼                                                                ▼
```

##### Step 1: Client-Side Par Kya Hota Hai?
Browser ek ordinary HTTP GET request bhejta hai, lekin specific upgrade headers ke sath:
```http
GET /ws HTTP/1.1
Host: localhost:8080
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
Sec-WebSocket-Version: 13
```

##### Step 2: Server-Side Par Kya Hota Hai? (Spring Boot Hooks)
1. **`HandshakeInterceptor (beforeHandshake)` run hota hai:**
   - Server request ke headers, cookies, ya query params check karta hai.
   - User ka ID ya JWT token nikalta hai aur Spring ke session map (`attributes.put("userId", id)`) me save karta hai.
2. **`DefaultHandshakeHandler (determineUser)` run hota hai:**
   - Spring attributes map se user ID uthata hai aur use **`java.security.Principal`** bana kar is TCP connection ke sath permanently jod (bind) deta hai!
3. **Server HTTP 101 Response bhejta hai:**
   ```http
   HTTP/1.1 101 Switching Protocols
   Upgrade: websocket
   Connection: Upgrade
   ```
4. **Is moment par HTTP request khatam ho gayi.** Ab wahi TCP connection direct duplex WebSocket pipe ban chuka hai!

##### Step 3: STOMP Session Start
Connection banne ke baad Client pehla STOMP frame bhejta hai: `CONNECT`.  
Server reply karta hai: `CONNECTED`. Ab connection messages lene-dene ke liye bilkul tayar hai!

---

### 🔑 3. The 4 Crucial Words: `/ws`, `/app`, `/topic`, `/queue`

In char shabdon ka concept samajh liya toh STOMP 80% samajh aa gaya!

#### 1. `/ws` (Entry Gate URL)
* **Kyu chahiye:** Yeh woh HTTP URL endpoint hai jahan client pehli baar handshake karne aata hai.
* **Analogy:** Building ka main gate.
* **Code me:** `registry.addEndpoint("/ws")` in [`WebSocketConfig.java`](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/micro-service/chat-communication-service/src/main/java/com/learning/chat/infrastructure/config/WebSocketConfig.java).

#### 2. `/app` (Java Controller / Business Logic Counter)
* **Kyu chahiye:** Jab client koi message bhejta hai aur chahta hai ki **pehle mere Java backend code (`@MessageMapping` method)** me jaye (validation karne, DB me save karne, ya Kafka me push karne ke liye).
* **Analogy:** Office ka Receptionist / Manager desk jahan pehle checking hoti hai.
* **Example:** Client bhejta hai `/app/chat.send` $\rightarrow$ Spring isko seedha [`ChatStompController.java`](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/micro-service/chat-communication-service/src/main/java/com/learning/chat/presentation/websocket/ChatStompController.java) ke `@MessageMapping("/chat.send")` method par bhej deta hai.

#### 3. `/topic` (Public Broadcast / Loudspeaker)
* **Kyu chahiye:** **1-to-Many communication.**
* **Analogy:** Colony ka loudspeaker ya WhatsApp Group — jo bhi message aayega, sab sunenge.
* **Example:** `/topic/public`, `/topic/room-101`, `/topic/cricket-score`. Jo jo client is topic ko subscribe karenge, broker un sabke screen par message broadcast kar dega.

#### 4. `/queue` (Private Inbox / Personal Letterbox)
* **Kyu chahiye:** **1-to-1 Private communication.**
* **Analogy:** Tumhare ghar ka private letterbox jisme sirf tumhari personal chithhi aati hai.
* **Example:** `/user/queue/private` ya user-specific notifications. Yeh message kisi group ko nahi balki sirf ek targeted user ko deliver hota hai.

---

### ⚖️ 4. Diff: `/topic` vs `/queue`

| Feature | `/topic` (Broadcast) | `/queue` (Point-to-Point / Private) |
| :--- | :--- | :--- |
| **Communication Pattern** | **Pub-Sub (1-to-Many)** | **Point-to-Point (1-to-1)** |
| **Recipients** | Sabhi active subscribers jo us topic ko listen kar rahe hain | Sirf ek specific targeted user |
| **Real Life Use Case** | WhatsApp Group, Public Chat Room, Live Stock Prices | 1-on-1 Direct Chat, Personal Notification, Error Alerts |
| **Client Subscription URL** | `/topic/group-123` | `/user/queue/private` |
| **Server Sending Method** | `messagingTemplate.convertAndSend("/topic/group-123", msg)` | `messagingTemplate.convertAndSendToUser("userId", "/queue/private", msg)` |
| **Mental Model** | Radio Station broadcast 📻 | Personal SMS / Courier ✉️ |

---

### 📨 5. STOMP Kya Hai? (The Protocol Layer)

Jaise **TCP** ke upar hum **HTTP** chalate hain (taki methods: `GET/POST`, headers, aur status codes mil sakein), waise hi **WebSocket** ke upar hum **STOMP** chalate hain!

STOMP ek simple text-based messaging protocol hai jo messages ko **Frames** me divide karta hai:

```
COMMAND
header1:value1
header2:value2

Body Payload (JSON, Text, Binary)^@
```
*(End of frame ek null byte `^@` se terminate hota hai).*

#### STOMP ke Core 5 Commands:

| Command | Direction | Kaam Kya Hai? |
| :--- | :--- | :--- |
| `CONNECT` | Client $\rightarrow$ Server | TCP handshake hone ke baad STOMP session start karna (credentials/heartbeat headers bhejta hai). |
| `CONNECTED` | Server $\rightarrow$ Client | Server confirm karta hai: *"STOMP session active ho gaya!"* |
| `SUBSCRIBE` | Client $\rightarrow$ Server | Kisi specific destination ko listen karna: `destination:/topic/news` |
| `SEND` | Client $\rightarrow$ Server | Kisi specific destination par message publish karna: `destination:/app/chat.send` |
| `MESSAGE` | Server $\rightarrow$ Client | Broker se subscriber ko actual data deliver hona. |
| `DISCONNECT` | Client $\rightarrow$ Server | STOMP session aur socket close karna. |

> [!TIP]
> **Simplified Breakdown (Dono mein "Client $\rightarrow$ Server" kyu hota hai?):**
> 
> | Frame / Action | Kon kisko bhejta hai? | Iska matlab kya hai? |
> | :--- | :--- | :--- |
> | `SUBSCRIBE` | Client $\rightarrow$ Server | *"Mujhe iss topic par register karo taaki aage ke messages mujhe milein."* |
> | `SEND` | Client $\rightarrow$ Server | *"Yeh lo mera naya message aur isko aage bhej do."* |
> | `MESSAGE` *(Asli receiving)* | Server $\rightarrow$ Client | *"Lo, naya message aaya hai jo tumne subscribe kiya tha!"* |

---

## 🏗️ 2. Spring STOMP Pipeline Architecture (Pure Tech Breakdown)

Spring Boot ke andar jab STOMP packet travel karta hai, toh woh ek well-defined **Pipeline (Channels)** se hokar gujarta hai:

```
                  ┌────────────────────────────────────────────────────────────────────────┐
                  │                 SPRING STOMP INTERNAL PIPELINE                         │
                  └────────────────────────────────────────────────────────────────────────┘

 [ Client (Browser) ]
         │
         │ 1. HTTP GET /ws (Upgrade: websocket) 
         ▼
 ┌──────────────────────────────┐
 │   HandshakeInterceptor       │  <── Extracts Cookie/Token from HTTP Request
 │   (beforeHandshake)          │      Puts data in WebSocket session attributes map!
 └──────────────┬───────────────┘
                │
                │ 2. Handshake approved, creates TCP Socket
                ▼
 ┌──────────────────────────────┐
 │   DefaultHandshakeHandler    │  <── Creates java.security.Principal
 │   (determineUser)            │      Binds authenticated identity to this TCP Socket!
 └──────────────┬───────────────┘
                │
                │ 3. Client sends STOMP Frames (CONNECT, SUBSCRIBE, SEND)
                ▼
 ┌──────────────────────────────┐
 │   clientInboundChannel       │  <── Thread pool queue for incoming client frames
 ├──────────────────────────────┤
 │   ChannelInterceptor         │  <── Frame-level Security: validates permissions
 │   (preSend)                  │      before message reaches controller or broker!
 └──────────────┬───────────────┘
                │
                ├──► Path starts with /app ──────────► [ @Controller @MessageMapping ]
                │                                               │
                │                                               │ (Method return / @SendTo)
                │                                               ▼
                └──► Path starts with /topic or /queue ──► [ Message Broker (SimpleBroker) ]
                                                                │
                                                                │ Pushes message out
                                                                ▼
                                                   ┌──────────────────────────────┐
                                                   │   clientOutboundChannel      │
                                                   └────────────┬─────────────────┘
                                                                │
                                                                ▼
                                                     [ Client (Browser Socket) ]
```

---

### 🗺️ Project Mapping: Architecture vs Aapka Code

Confusion door karne ke liye simple rule yaad rakho: **Poore flow ko 2 hisso me baanto:**
1. **Entry Gate (Connection banna):** HTTP request aayi aur WebSocket mein badal gayi (1 bar hoti hai).
2. **Chat Room (Messages aana-jaana):** Connection banne ke baad STOMP frames ka travel (baar-baar hota hai).

| Spring Architecture Component | Aapke Project Ki Exact File | Iska Asli Kaam (Simple Words) |
| :--- | :--- | :--- |
| **`WebSocketMessageBrokerConfigurer`** | [`WebSocketConfig.java`](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/micro-service/chat-communication-service/src/main/java/com/learning/chat/infrastructure/config/WebSocketConfig.java) | **Master Switch:** Batata hai WebSocket URL `/ws` kya hai aur message kaha route honge (`/app`, `/topic`, `/queue`). |
| **`HandshakeInterceptor`** | [`UserHandshakeInterceptor.java`](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/micro-service/chat-communication-service/src/main/java/com/learning/chat/infrastructure/interceptor/UserHandshakeInterceptor.java) | **Ticket Checker:** Connection bante time token/header/query se `userId` nikal kar bag (`attributes`) mein rakh deta hai. |
| **`DefaultHandshakeHandler`** | [`UserHandshakeHandler.java`](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/micro-service/chat-communication-service/src/main/java/com/learning/chat/infrastructure/interceptor/UserHandshakeHandler.java) | **ID Badge Creator:** Bag se `userId` nikal kar socket ke gale mein `Principal` (ID card) taang deta hai. |
| **`/app` Prefix Destination** | [`ChatStompController.java`](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/micro-service/chat-communication-service/src/main/java/com/learning/chat/presentation/websocket/ChatStompController.java) | **Java Controller:** Jab client message bhejta hai (`/app/chat.send`), toh yeh Spring `@MessageMapping` method trigger karta hai. |
| **`SimpleBroker` (`/topic`, `/queue`)** | Spring's In-Memory Broker (configured in `WebSocketConfig`) | **Post Office:** Messages ko store/route karke sabhi subscribers ke paas deliver karta hai. |
| **`SimpMessagingTemplate`** | Injected in `ChatStompController` / Service / Kafka | **Push Engine:** Java code se jab server ko khud client ke inbox mein message fekna ho. |

---

### The 6 Core Technical Classes & Interfaces (Project-Mapped)

#### 1. `WebSocketMessageBrokerConfigurer` (Configuration Interface)
* 🎯 **Aapke Project Me:** [`WebSocketConfig.java`](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/micro-service/chat-communication-service/src/main/java/com/learning/chat/infrastructure/config/WebSocketConfig.java)
* **Kyu chahiye:** Yeh Spring ka main configuration hook hai.
* **Aap kya karte ho:** Is interface ko implement karte ho aur 2 main methods override karte ho:
  1. `configureMessageBroker(MessageBrokerRegistry registry)`: Broker prefixes set karna (`/topic`, `/queue`, `/app`, `/user`).
  2. `registerStompEndpoints(StompEndpointRegistry registry)`: HTTP WebSocket connection URL register karna (`/ws`), aur interceptors lagana.

#### 2. `HandshakeInterceptor` (HTTP Upgrade Hook)
* 🎯 **Aapke Project Me:** [`UserHandshakeInterceptor.java`](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/micro-service/chat-communication-service/src/main/java/com/learning/chat/infrastructure/interceptor/UserHandshakeInterceptor.java)
* **Kyu chahiye:** WebSocket connection shuru hone se pehle standard **HTTP GET request** aati hai jisme `Upgrade: websocket` header hota hai.
* **Engineering Role:** Is phase me aap HTTP headers, cookies, ya query params se user identify karte ho.
* **Critical Code:** `attributes.put("userId", extractedUserId);`  
  Yeh `attributes` map Spring ke internal WebSocket session ke sath bind ho jata hai.

#### 3. `DefaultHandshakeHandler` (Principal Binding Hook)
* 🎯 **Aapke Project Me:** [`UserHandshakeHandler.java`](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/micro-service/chat-communication-service/src/main/java/com/learning/chat/infrastructure/interceptor/UserHandshakeHandler.java)
* **Kyu chahiye:** Spring ko pata hona chahiye ki is specific socket connection ka maalik kaun hai, taaki baad me `@SendToUser` use kiya ja sake.
* **Engineering Role:** Method `determineUser()` ko override karke attributes map se `userId` nikalte ho aur **`java.security.Principal`** (jaise aapka `StompPrincipal`) return karte ho.
* **Result:** `principal.getName()` ab is socket connection ka permanent unique identifier ban gaya!

#### 4. `ChannelInterceptor` (STOMP Message Channel Hook)
* **Kyu chahiye:** Handshake interceptor sirf pehli bar (HTTP connection bante time) chalta hai. Lekin connection banne ke baad client 100 STOMP frames bhej sakta hai (`SUBSCRIBE`, `SEND`).
* **Engineering Role:** Har STOMP message frame inbound ya outbound channel par dispatch hone se pehle `preSend()` method me aata hai.
* **Use Case:** Topic authorization. Agar user kisi unauthorized destination ko subscribe karne ki koshish kare, toh `preSend()` me destination check karke block kar sakte ho.

#### 5. `SimpMessagingTemplate` (Server-Side Push Engine)
* 🎯 **Aapke Project Me:** Injected in [`ChatStompController.java`](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/micro-service/chat-communication-service/src/main/java/com/learning/chat/presentation/websocket/ChatStompController.java)
* **Kyu chahiye:** Jab server ko background me ya event aane par client ko message bhejte rehna ho.
* **Methods:**
  - `convertAndSend(destination, payload)`: Public topic par broadcast karna (`/topic/public`).
  - `convertAndSendToUser(username, destination, payload)`: Specific user ke private queue par push karna (`/user/{userId}/queue/messages`).

#### 6. `StompHeaderAccessor` (Frame Header Wrapper)
* **Kyu chahiye:** Spring messaging ke low-level generic `Message<?>` object me se STOMP-specific headers (Session ID, Destination, Command, Principal) direct nahi milte.
* **Code:** `StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);`  
  Iske baad `accessor.getDestination()`, `accessor.getSessionId()`, aur `accessor.getUser()` directly mil jate hain.

---

## 🗺️ 3. Destinations Demystified: `/app`, `/topic`, `/queue`, `/user`

Spring STOMP me destinations sirf strings hote hain, lekin unke **Prefixes** decide karte hain ki packet kahan route hoga:

```
Incoming Destination: "/app/chat.send"
  └── Prefix "/app" matched! ──► Controller ke @MessageMapping("/chat.send") method ko do!

Incoming Destination: "/topic/crypto-updates"
  └── Prefix "/topic" matched! ──► Controller ko bypass karo, seedha Broker ko do (Broadcast to all subscribers)!

Incoming Destination: "/user/queue/private-alerts"
  └── Prefix "/user" matched! ──► Spring UserDestinationMessageHandler isko resolve karega user ke unique session me!
```

---

### Deep Dive: `/user` Prefix Ka Asli Magic (Behind The Scenes)

Interviews ka favourite question:  
*"Jab client `/user/queue/private` ko subscribe karta hai, toh server ko kaise pata chalta hai ki ye message sirf usi user ko milega?"*

#### The Internal Translation Pipeline:

```
Client (User "alice", Session ID "sess_001") subscribes to:
Destination: "/user/queue/alerts"
                        │
                        ▼
┌────────────────────────────────────────────────────────┐
│ Spring's UserDestinationMessageHandler                 │
│ 1. Principal se user ka naam nikalta hai: "alice"      │
│ 2. Alice ka active WebSocket Session ID dekhta hai:    │
│    "sess_001"                                          │
│ 3. Destination ko rewrite/transform karta hai:         │
│    "/queue/alerts-user-sess_001"                       │
└───────────────────────┬────────────────────────────────┘
                        │
                        ▼
Broker internal subscription table:
Subscribed Destination: "/queue/alerts-user-sess_001"
```

Jab server call karta hai:
```java
messagingTemplate.convertAndSendToUser("alice", "/queue/alerts", payload);
```
Spring internally destination ko wapas transform karta hai:  
`"/user/alice/queue/alerts"` $\longrightarrow$ `"/queue/alerts-user-sess_001"`.  
Kyunki is transformed destination par sirf Alice ka socket sun raha hai, message **sirf aur sirf Alice ke browser par deliver hota hai**!

---

## 🏷️ 4. The Complete Annotation Reference

### 1. Controller Routing Annotations

#### `@MessageMapping("/path")`
* **Trigger:** Jab client koi message bhejta hai (`SEND /app/path`).
* **Concept:** REST ke `@PostMapping` jaisa hai. Request payload ko accept karta hai, business logic execute karta hai, aur chahe toh response return karta hai.

#### `@SubscribeMapping("/path")` (The One-Shot History Superpower! 💎)
* **Trigger:** Jab client pehli baar kisi topic ko `SUBSCRIBE` karta hai (e.g., `SUBSCRIBE /app/channel/101/history`).
* **The Magic:** Is method ka return value **kisi topic par broadcast nahi hota**!
* Spring is response ko direct usi request bhejne wale client ke socket par ek single one-time `MESSAGE` frame bana kar return kar deta hai.
* **Best Use Case:** Jab user chat room join kare aur use pichle 50 messages load karne hon (Request-Response over WebSocket).

---

### 2. Destination Target Annotations

#### `@SendTo("/topic/broadcast")`
* Method ka return value automatically serialize hokar is specified public topic ke **saare subscribers** ko fan-out (broadcast) ho jata hai.

#### `@SendToUser("/queue/reply")`
* Method ka return value **sirf request bhejne wale user** ke private session par jata hai.
* ⚠️ **Multi-Device Setting (`broadcast = true` vs `false`)**:
  ```java
  // Case A: Default (broadcast = true)
  // Agar user Mobile aur Laptop dono se logged in hai, reply DONO devices par jayega.
  @SendToUser("/queue/reply")

  // Case B: Single Device Only (broadcast = false)
  // Reply SIRF USI SPECIFIC SOCKET SESSION par jayega jisne request trigger ki thi!
  @SendToUser(destinations = "/queue/reply", broadcast = false)
  ```

---

### 3. Error Handling: `@MessageExceptionHandler`

> [!IMPORTANT]
> **Why `@ExceptionHandler` Fails in STOMP:**  
> Spring MVC ka `@ExceptionHandler` ya `@RestControllerAdvice` HTTP Servlets ke liye bana hai. STOMP WebSocket messages ke exceptions ko catch **nahi** kar sakta!  
> STOMP controllers me errors handle karne ke liye Spring ne dedicated **`@MessageExceptionHandler`** diya hai.

```java
@Controller
public class ChatController {

    @MessageMapping("/transfer")
    public void executeTransfer(@Valid @Payload TransferDto dto) {
        if (dto.amount() <= 0) {
            throw new IllegalArgumentException("Amount must be positive!");
        }
    }

    // Catches exceptions from @MessageMapping methods in this controller
    @MessageExceptionHandler(IllegalArgumentException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorDto handleValidationFailure(IllegalArgumentException ex) {
        // Sends error JSON ONLY to the offending client!
        return new ErrorDto("BAD_REQUEST", ex.getMessage());
    }
}
```

---

### 4. Method Parameter Annotations

| Annotation / Type | Example | Kaam Kya Hai? |
| :--- | :--- | :--- |
| `@Payload` | `@Valid @Payload ChatDto dto` | Incoming STOMP body ko Java DTO me deserialize karta hai. `@Valid` support karta hai. |
| `@DestinationVariable` | `@DestinationVariable String roomId` | Path variable extract karta hai (`/chat/{roomId}/send`). |
| `@Header` | `@Header("simpSessionId") String sId` | Ek specific STOMP frame header nikalta hai. |
| `Principal` | `Principal principal` | Handshake handler dwara bind kiya gaya user identity object. `principal.getName()` |
| `StompHeaderAccessor` | `StompHeaderAccessor headers` | Saare low-level STOMP frame headers ka direct access. |

---

## 💻 5. Step-by-Step Clean Implementation (Production Code)

Chaliye ek clean, standalone Spring Boot STOMP implementation dekhte hain:

---

### Step 1: Configuration (`WebSocketConfig.java`)

```java
package com.example.stomp.config;

import com.example.stomp.interceptor.AuthHandshakeInterceptor;
import com.example.stomp.interceptor.CustomHandshakeHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 1. STOMP Message Broker Pipeline On
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // A. Broker Destinations: Server -> Client push
        // /topic for Broadcast (1-to-N), /queue for 1-to-1 Private messages
        registry.enableSimpleBroker("/topic", "/queue");

        // B. Inbound Prefix: Client -> Server
        // Messages starting with /app are routed to @MessageMapping controllers
        registry.setApplicationDestinationPrefixes("/app");

        // C. User Prefix for Private Messaging
        // Enables /user/{username}/queue/... resolution
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // HTTP WebSocket Handshake Endpoint
        registry.addEndpoint("/ws")
                .addInterceptors(new AuthHandshakeInterceptor())   // HTTP Interceptor
                .setHandshakeHandler(new CustomHandshakeHandler())   // Principal Binder
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Fallback for browsers with WebSocket blocked
    }
}
```

---

### Step 2: HTTP Handshake Interceptor (`AuthHandshakeInterceptor.java`)

```java
package com.example.stomp.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, 
                                   ServerHttpResponse response, 
                                   WebSocketHandler wsHandler, 
                                   Map<String, Object> attributes) throws Exception {
        
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // Extract token or userId from Query Param or HTTP Header during upgrade
            String userId = servletRequest.getServletRequest().getParameter("userId");
            
            if (userId != null && !userId.isBlank()) {
                // Store in WebSocket session attributes map
                attributes.put("userId", userId);
                return true; // Allow handshake
            }
        }
        return false; // Reject handshake if unauthenticated
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                               WebSocketHandler wsHandler, Exception exception) {
        // No-op cleanup
    }
}
```

---

### Step 3: Principal Binding Handler (`CustomHandshakeHandler.java`)

```java
package com.example.stomp.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, 
                                      WebSocketHandler wsHandler, 
                                      Map<String, Object> attributes) {
        
        String userId = (String) attributes.get("userId");
        if (userId == null) {
            return null;
        }
        
        // Return a lightweight Principal object (getName() = userId)
        return () -> userId;
    }
}
```

---

### Step 4: Controller (`ChatController.java`)

```java
package com.example.stomp.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 1. Initial State Fetch on Subscription (Request-Response over WebSocket)
    // Client subscribes to: /app/room/{roomId}/history
    @SubscribeMapping("/room/{roomId}/history")
    public List<String> getInitialHistory(@DestinationVariable String roomId) {
        // Delivered ONLY to the subscribing client, NOT broadcasted!
        return List.of("Message 1: Welcome!", "Message 2: Say hello!");
    }

    // 2. Public Broadcast (Group Chat)
    // Client sends to: /app/room/{roomId}/send
    @MessageMapping("/room/{roomId}/send")
    @SendTo("/topic/room.{roomId}")
    public ChatMessage broadcastMessage(@DestinationVariable String roomId, 
                                        @Payload ChatMessage message, 
                                        Principal principal) {
        // Sender identity comes from authenticated Principal
        message.setSender(principal.getName());
        return message; // Pushed to all subscribers of /topic/room.{roomId}
    }

    // 3. Private 1-on-1 Direct Message
    // Client sends to: /app/chat.private
    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload PrivateMessage dto, Principal principal) {
        dto.setSender(principal.getName());

        // Target specific user's private queue: /user/{recipient}/queue/messages
        messagingTemplate.convertAndSendToUser(
                dto.getRecipient(), 
                "/queue/messages", 
                dto
        );
    }
}
```

---

### Step 5: Connection Lifecycle Event Listener (`WebSocketEventListener.java`)

```java
package com.example.stomp.listener;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headers.getUser();
        if (user != null) {
            // Broadcast User Online Status
            messagingTemplate.convertAndSend("/topic/presence", user.getName() + " is ONLINE");
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headers.getUser();
        if (user != null) {
            // Broadcast User Offline Status
            messagingTemplate.convertAndSend("/topic/presence", user.getName() + " went OFFLINE");
        }
    }
}
```

---

## 🔬 6. Under The Hood: The Wire Protocol Frame Flow

Yeh exact packet trace hai jo wire par client aur server ke beech travel karta hai:

```
CLIENT (Browser)                                            SERVER (Spring Boot)
      │                                                              │
      │ 1. HTTP Upgrade: GET /ws (Headers: Upgrade: websocket)       │
      ├─────────────────────────────────────────────────────────────►│
      │    [HandshakeInterceptor validates token/userId]             │
      │    [DefaultHandshakeHandler creates StompPrincipal]          │
      │                                                              │
      │ 2. HTTP 101 Switching Protocols (TCP Socket Now Open!)       │
      │◄─────────────────────────────────────────────────────────────┤
      │                                                              │
      │ 3. STOMP CONNECT Frame                                       │
      │    COMMAND: CONNECT                                          │
      │    accept-version:1.2, host:localhost                        │
      │    heart-beat:10000,10000                                    │
      ├─────────────────────────────────────────────────────────────►│
      │                                                              │
      │ 4. STOMP CONNECTED Frame                                     │
      │    COMMAND: CONNECTED, version:1.2, session:sess_101         │
      │◄─────────────────────────────────────────────────────────────┤
      │                                                              │
      │ 5. STOMP SUBSCRIBE Frame (Listen to group room)              │
      │    COMMAND: SUBSCRIBE, id:sub-0, destination:/topic/room.1   │
      ├─────────────────────────────────────────────────────────────►│
      │                                                              │
      │ 6. STOMP SEND Frame (Publish message to group)               │
      │    COMMAND: SEND, destination:/app/room/1/send               │
      │    {"content":"Hello everyone!"}                             │
      ├─────────────────────────────────────────────────────────────►│
      │    [Routes to @MessageMapping("/room/{roomId}/send")]        │
      │    [Pushes return value to /topic/room.1]                    │
      │                                                              │
      │ 7. STOMP MESSAGE Frame (Delivered to all subscribers)        │
      │    COMMAND: MESSAGE, destination:/topic/room.1, id:sub-0     │
      │    {"sender":"usr_alice","content":"Hello everyone!"}        │
      │◄─────────────────────────────────────────────────────────────┤
```

---

## 🚀 7. Production Scaling: Multi-Pod Architecture

### The Single-Pod Limitation (`SimpleBroker`)

Spring Boot ka default `enableSimpleBroker("/topic", "/queue")` **pure in-memory Java HashMap** hota hai.

```
                  ┌──────────────────────┐       ┌──────────────────────┐
                  │   Pod 1 (Spring)     │       │   Pod 2 (Spring)     │
                  │                      │       │                      │
                  │ In-Memory Broker     │       │ In-Memory Broker     │
                  └──────────┬───────────┘       └──────────┬───────────┘
                             │                              │
                             ▼                              ▼
                       [ User Alice ]                 [ User Bob ]
```
- Alice Pod-1 se connected hai.
- Bob Pod-2 se connected hai.
- Alice jab `/topic/all` par message bhejti hai, toh Pod-1 ka in-memory broker sirf Pod-1 par connected clients ko janta hai. **Bob ko message kabhi nahi milega!**

---

### Solution 1: External Full STOMP Broker Relay (RabbitMQ / ActiveMQ)

Spring STOMP broker logic ko **RabbitMQ ya Apache ActiveMQ** par offload kar deta hai:

```
[ Pod 1 ] ──TCP Relay (Port 61613)──► ┌───────────────────────────┐ ◄──TCP Relay── [ Pod 2 ]
                                      │  Clustered RabbitMQ       │
                                      │  (Full STOMP Broker)      │
                                      └───────────────────────────┘
```

#### Configuration:
```java
@Override
public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableStompBrokerRelay("/topic", "/queue")
            .setRelayHost("rabbitmq.production.svc")
            .setRelayPort(61613) // Standard STOMP port
            .setClientLogin("guest")
            .setClientPasscode("guest")
            .setSystemLogin("admin")
            .setSystemPasscode("admin");

    registry.setApplicationDestinationPrefixes("/app");
}
```
*Benefits:* True multi-pod horizontal scalability, persistent queues, clustering, aur dead-letter support.

---

### Solution 2: The Redis Pub/Sub Bridge (Cost-Effective Scaling)

Agar aapko RabbitMQ ka separate cluster maintain nahi karna aur Redis already use ho raha hai:

1. Pod-1 par message aate hi Pod-1 message ko local broker ke bajaye **Redis Channel** par `PUBLISH` karta hai.
2. Sabhi pods (Pod-1, Pod-2, Pod-3) us Redis channel ke subscribers hote hain.
3. Jab Redis message broadcast karta hai, toh har pod apne local `SimpMessagingTemplate` se apne connected clients ko frame deliver kar deta hai!

---

### Frame-Level Security via `ChannelInterceptor`

> [!WARNING]
> `HandshakeInterceptor` sirf connection establish karte waqt chalta hai. Connection banne ke baad client agar unauthorized destination ko `SUBSCRIBE` karne lage (e.g. `SUBSCRIBE /topic/admin_room`), toh use rokne ka kaam **`ChannelInterceptor`** karta hai!

```java
@Configuration
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = 
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    Principal user = accessor.getUser();

                    if (destination.startsWith("/topic/admin") && !isAdmin(user)) {
                        throw new AccessDeniedException("Unauthorized subscription to admin topic!");
                    }
                }
                return message;
            }
        });
    }
}
```

---

## 📋 8. Architect's Quick Revision Summary

| Technical Concept | What it Actually Does |
| :--- | :--- |
| **WebSocket** | Persistent, bi-directional TCP socket connection (Layer-4 style byte pipe). |
| **STOMP** | Layer-7 protocol on top of WebSocket providing Frames (COMMAND, Headers, Destination, Body). |
| **`HandshakeInterceptor`** | HTTP Upgrade hook. Extracts auth tokens/cookies and stores them in session attributes. |
| **`DefaultHandshakeHandler`** | Binds user identity to the socket by returning a `Principal` (`determineUser()`). |
| **`ChannelInterceptor`** | Frame-level interceptor. Validates `SUBSCRIBE` and `SEND` frame permissions. |
| **`/app` prefix** | Routes incoming frames to `@Controller` methods (`@MessageMapping`). |
| **`/topic` prefix** | Routes directly to the message broker for 1-to-N broadcasting. |
| **`/user` prefix** | Spring resolves user name to their active session ID (`/queue/...-user<sessionId>`). |
| **`@SubscribeMapping`** | Returns a one-shot response directly to the subscribing client (Great for chat history). |
| **In-Memory Broker Limit** | Memory is not shared across pods; requires External Broker Relay (RabbitMQ) or Redis Pub/Sub for clustering. |
