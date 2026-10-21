# STOMP & WebSockets: Zero to Hero Beginner Guide 🚀
*(By Alex: Pure Conceptual Clarity, Step-by-Step Implementation & Real Architecture)*

---

## 📌 Index (Hum Kya Seekhenge)
1. [Fundamental Concept: HTTP vs WebSocket vs STOMP](#1-fundamental-concept-http-vs-websocket-vs-stomp)
2. [Handshake Deep Dive (Kab, Kaise, aur Server/Client Par Kya Hota Hai?)](#2-handshake-deep-dive)
3. [The 4 Crucial Words: `/ws`, `/app`, `/topic`, `/queue`](#3-the-4-crucial-words-ws-app-topic-queue)
4. [Diff: `/topic` vs `/queue` (Kab Kisko Use Karein?)](#4-diff-topic-vs-queue)
5. [Scratch Se Implementation Path (Step-by-Step Coding Guide)](#5-scratch-se-implementation-path)
   - Step 1: Maven Dependencies
   - Step 2: WebSocket Configuration (`WebSocketConfig.java`)
   - Step 3: Identity & Authentication Hooks (`Interceptor` + `Handler`)
   - Step 4: STOMP Controller (`ChatController.java`)
   - Step 5: Frontend Client Code (`JavaScript / HTML`)
6. [Annotations & Tools Cheat Sheet (Usecase & Purpose)](#6-annotations--tools-cheat-sheet)

---

## 1. Fundamental Concept: HTTP vs WebSocket vs STOMP

### A. HTTP Ki Problem (Kyu Chat Apps HTTP Par Nahi Bante?)
HTTP ek **Request-Response** protocol hai:
* Client bolta hai: *"Mujhe naya message do"* (Request)
* Server bolta hai: *"Yeh lo message"* ya *"Koi naya message nahi hai"* (Response)
* Aur connection **close** ho jata hai!

Agar tumhe real-time WhatsApp banana hai, toh client ko har 1 second me server se poochna padega: *"Naya message aaya kya? Naya message aaya kya?"* (Isse **HTTP Polling** kehte hain).
* ❌ Server par lakho useless requests ka load.
* ❌ Har request me heavy HTTP headers (Cookie, User-Agent) travel karte hain (bandwidth waste).
* ❌ Server client ko apni taraf se proactively message nahi bhej sakta jab tak client na maange.

---

### B. WebSocket Kya Hai? (The Persistent Full-Duplex Pipe)
WebSocket ek aisi technology hai jo browser aur server ke beech ek **single, permanent TCP connection (pipe)** khol deti hai:
* Ek bar connection ban gaya, toh pipe **24x7 khula rehta hai**.
* **Full-Duplex:** Client aur Server dono ek hi time par bina ruke ek doosre ko data bhej sakte hain.
* Server ko jab bhi naya message milega, wo turant client ke mobile/browser par push kar dega!
* **Problem:** WebSocket ek **"Dumb Pipe"** hai. Yeh sirf raw text ya binary bytes bhejta hai. Ise yeh nahi pata hota ki:
  - Yeh message group me bhejna hai ya private?
  - Yeh user subscribe kar raha hai ya message post kar raha hai?

---

### C. STOMP Kya Hai? (The Smart Messaging Protocol on Top of WebSocket)
**STOMP = Simple Text Oriented Messaging Protocol**

Jaise **TCP** (raw cable) ke upar hum **HTTP** chalate hain taaki `GET`, `POST`, `Headers`, `URLs` mil sakein...
Waise hi **WebSocket** (raw cable) ke upar hum **STOMP** chalate hain!

STOMP messages ko structured **Frames** me pack karta hai:
```text
COMMAND
header1:value1
header2:value2

Body Payload (JSON, Text)^@
```

STOMP aane ke baad hamare paas standard commands aa gaye:
- `CONNECT` / `CONNECTED`
- `SUBSCRIBE` (kisi topic ko sunne ke liye)
- `SEND` (kisi destination par post karne ke liye)
- `MESSAGE` (server se client ko aane wala data)

---

## 2. Handshake Deep Dive
*(Kab hota hai? Kaise hota hai? Server aur Client side kya chalta hai?)*

### Handshake Kya Hai?
Handshake woh **one-time process** hai jisme browser aur server aapas me agree karte hain ki:
> *"Bhai, hum standard HTTP connection ko band karke ek 2-way continuous WebSocket pipe me convert kar rahe hain!"*

### Yeh Kab Hota Hai?
Jab user aapki chat application open karta hai aur client side se WebSocket connect karne ka code trigger hota hai (jaise `new SockJS('/ws')` ya `new WebSocket('ws://...')`).

### Kaise Hota Hai? (Step-by-Step Technical Flow)

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

#### Step 1: Client-Side Par Kya Hota Hai?
Browser ek ordinary HTTP GET request bhejta hai, lekin specific upgrade headers ke sath:
```http
GET /ws HTTP/1.1
Host: localhost:8080
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
Sec-WebSocket-Version: 13
```

#### Step 2: Server-Side Par Kya Hota Hai? (Spring Boot Hooks)
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

#### Step 3: STOMP Session Start
Connection banne ke baad Client pehla STOMP frame bhejta hai: `CONNECT`.  
Server reply karta hai: `CONNECTED`. Ab connection messages lene-dene ke liye bilkul tayar hai!

---

## 3. The 4 Crucial Words: `/ws`, `/app`, `/topic`, `/queue`

In char shabdon ka concept samajh liya toh STOMP 80% samajh aa gaya!

### 1. `/ws` (Entry Gate URL)
* **Kyu chahiye:** Yeh woh HTTP URL endpoint hai jahan client pehli baar handshake karne aata hai.
* **Analogy:** Building ka main gate.
* **Code me:** `registry.addEndpoint("/ws")`

---

### 2. `/app` (Java Controller / Business Logic Counter)
* **Kyu chahiye:** Jab client koi message bhejta hai aur chahta hai ki **pehle mere Java backend code (`@MessageMapping` method)** me jaye (validation karne, database me MongoDB/Postgres me save karne, ya Kafka me push karne ke liye).
* **Analogy:** Office ka Receptionist / Manager desk jahan pehle checking hoti hai.
* **Example:** Client message bhejta hai `/app/chat.sendMessage`. Spring dekhta hai shuruat `/app` se ho rahi hai, toh seedha `@Controller` ke `@MessageMapping("/chat.sendMessage")` method ko call kar deta hai.

---

### 3. `/topic` (Public Broadcast / Loudspeaker)
* **Kyu chahiye:** **1-to-Many communication.**
* **Analogy:** Colony ka loudspeaker ya WhatsApp Group — jo bhi message aayega, sab sunenge.
* **Example:** `/topic/public`, `/topic/room-101`, `/topic/cricket-score`.  
  Jo jo client is topic ko subscribe karenge, broker un sabke screen par message broadcast kar dega.

---

### 4. `/queue` (Private Inbox / Personal Letterbox)
* **Kyu chahiye:** **1-to-1 Private communication.**
* **Analogy:** Tumhare ghar ka private letterbox jisme sirf tumhari personal chithhi aati hai.
* **Example:** `/user/queue/messages`, `/user/queue/notifications`.  
  Yeh message kisi group ko nahi balki sirf aur sirf ek targeted user ko deliver hota hai.

---

## 4. Diff: `/topic` vs `/queue`

| Feature | `/topic` (Broadcast) | `/queue` (Point-to-Point / Private) |
| :--- | :--- | :--- |
| **Communication Pattern** | **Pub-Sub (1-to-Many)** | **Point-to-Point (1-to-1)** |
| **Recipients** | Sabhi active subscribers jo us topic ko listen kar rahe hain | Sirf ek specific targeted user |
| **Real Life Use Case** | WhatsApp Group, Public Chat Room, Live Stock Prices | 1-on-1 Direct Chat, Personal Notification, Error Alerts |
| **Client Subscription URL** | `/topic/group-123` | `/user/queue/private` |
| **Server Sending Method** | `messagingTemplate.convertAndSend("/topic/group-123", msg)` | `messagingTemplate.convertAndSendToUser("userId", "/queue/private", msg)` |
| **Mental Model** | Radio Station broadcast 📻 | Personal SMS / Courier ✉️ |

---

## 5. Scratch Se Implementation Path (Step-by-Step Coding Guide)

Agar tumhe blank project se STOMP implement karna ho, toh yeh exact steps follow karo:

### Step 1: Maven Dependency (`pom.xml`)
Spring Boot me sirf ek dependency chahiye:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

---

### Step 2: Configuration Class (`WebSocketConfig.java`)
Yeh hamare WebSocket ka **Master Switch** hai.

```java
package com.learning.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 1. STOMP messaging support activate karta hai
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // A. In-Memory Broker Destinations (jo server se client ko jaate hain)
        registry.enableSimpleBroker("/topic", "/queue");

        // B. Client-to-Server Routing Prefix
        registry.setApplicationDestinationPrefixes("/app");

        // C. Private User Destination Prefix
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Yeh hamara Entry Gate (/ws) hai jahan client aakar handshake karega
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // CORS allow
                .withSockJS(); // Fallback for browsers that don't support pure WebSocket
    }
}
```

#### 🔍 Line-by-Line Breakdown: `WebSocketConfig.java`

| Line / Code | Iska Asli Meaning (Kyu Likha?) |
| :--- | :--- |
| `@Configuration` | Spring ko batata hai ki yeh ek config class hai jiske andar bean definitions aur settings hain. |
| `@EnableWebSocketMessageBroker` | **Sabse zaroori annotation:** Spring ke andar STOMP protocol, message channels, aur broker pipeline ko switch ON kar deta hai. |
| `implements WebSocketMessageBrokerConfigurer` | Spring ka standard interface jisme hum broker routes aur connection endpoints customize karte hain. |
| `configureMessageBroker(...)` | Yeh method decide karta hai: Message kis raste se aayega aur kis raste se bahar jayega. |
| `registry.enableSimpleBroker("/topic", "/queue");` | **Spring ke In-Memory Post Office ko chalu karta hai.** Jo bhi message `/topic` (group broadcast) ya `/queue` (private) par aayega, Spring use seedha subscriber clients tak deliver karega (bina Java controller ko involve kiye). |
| `registry.setApplicationDestinationPrefixes("/app");` | **Client Routing Prefix.** Agar client chahta hai ki message pehle Java logic (`@Controller`) me jaye, toh client ko address me `/app` lagana padega (e.g. `/app/chat.send`). Spring `/app` dekh kar message ko `@MessageMapping` controller par bhej deta hai. |
| `registry.setUserDestinationPrefix("/user");` | Spring ko batata hai ki private 1-to-1 routing ke liye prefix `/user` hoga (e.g., `/user/{username}/queue/messages`). |
| `registerStompEndpoints(...)` | Yeh method WebSocket ka physical HTTP connection URL define karta hai. |
| `registry.addEndpoint("/ws")` | **Main Gate ka path:** Client connection request `http://localhost:8080/ws` par aayegi. |
| `.setAllowedOriginPatterns("*")` | **CORS Policy:** Kisi bhi domain (React `localhost:3000`, Mobile App) se connection allow karta hai. |
| `.withSockJS();` | Agar kisi purane browser ya office proxy me pure WebSocket block ho, toh SockJS automatically HTTP Long-Polling par fallback kar deta hai (connection tootne nahi deta). |

---

### Step 3: Identity & Authentication Hooks (Handshake & Principal)
*Yeh optional hai agar sirf basic public chat banana ho, lekin **Production aur Private 1-to-1 chat ke liye MUST hai**.*

#### A. `UserHandshakeInterceptor.java` (Ticket Checker)
HTTP request aate time token ya query se `userId` nikal kar bag (`attributes`) me daalta hai:

```java
package com.learning.chat.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class UserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                  WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // URL Query Parameter se userId nikalte hain (e.g. /ws?userId=user_101)
            String userId = servletRequest.getServletRequest().getParameter("userId");
            if (userId != null && !userId.isBlank()) {
                attributes.put("userId", userId);
                return true;
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}
```

#### 🔍 Line-by-Line Breakdown: `UserHandshakeInterceptor.java`

| Line / Code | Iska Asli Meaning (Kyu Likha?) |
| :--- | :--- |
| `implements HandshakeInterceptor` | Spring ka hook jo HTTP connection bante time (Handshake ke dauran) chalta hai. |
| `beforeHandshake(request, ..., attributes)` | Connection open hone se theek pehle execute hota hai. Iska kaam hai request ko verify karna. |
| `request instanceof ServletServerHttpRequest` | Check karta hai ki kya yeh standard HTTP servlet request hai (taaki hum query params/cookies read kar sakein). |
| `servletRequest.getServletRequest().getParameter("userId")` | Client jab connect hota hai (`/ws?userId=user_101`), toh URL query param se `userId` read karta hai. *(Production me yahan JWT Token ya Cookie read ki jaati hai).* |
| `attributes.put("userId", userId);` | **Attributes Bag:** Spring har WebSocket session ke sath ek Map (bag) provide karta hai. Humne `userId` ko is bag me rakh diya taaki agle step me nikal sakein. |
| `return true;` | Handshake ko approve karta hai. Agar yahan `return false;` kar dein, toh connection turant reject ho jayega (Unauthorized 401). |

---

#### B. `UserHandshakeHandler.java` (ID Badge Creator)
Bag (`attributes`) se `userId` nikal kar socket ke gale me `Principal` (ID card) taang deta hai:

```java
package com.learning.chat.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class UserHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        String userId = (String) attributes.get("userId");
        if (userId == null) {
            return null;
        }
        // Principal implement karne wala simple record
        return new StompPrincipal(userId);
    }

    public record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
```

#### 🔍 Line-by-Line Breakdown: `UserHandshakeHandler.java`

| Line / Code | Iska Asli Meaning (Kyu Likha?) |
| :--- | :--- |
| `extends DefaultHandshakeHandler` | Spring ka class jo decide karta hai ki is specific TCP socket connection ka maalik kaun sa User hai. |
| `determineUser(..., attributes)` | Handshake approve hone ke turant baad Spring is method ko call karta hai: *"Batao is connection ka Principal kaun hai?"* |
| `attributes.get("userId")` | Jo `userId` pichle step me Interceptor ne bag me daali thi, use wapas nikalta hai. |
| `return new StompPrincipal(userId);` | Spring ko **`java.security.Principal`** object return karta hai. Iska fayda: Ab Spring ke har Controller me `Principal principal` inject ho sakega aur `principal.getName()` hamesha wahi `userId` dega! |
| `record StompPrincipal(String name) implements Principal` | Ek lightweight Java 17 Record jo Java ke standard `Principal` interface ko satisfy karta hai. |

*In dono ko `WebSocketConfig.java` me connect kiya jata hai:*
```java
registry.addEndpoint("/ws")
        .addInterceptors(new UserHandshakeInterceptor())   // Pehle Ticket check
        .setHandshakeHandler(new UserHandshakeHandler())   // Phir ID Badge issue
        .setAllowedOriginPatterns("*")
        .withSockJS();
```

---

### Step 4: STOMP Controller (`ChatController.java`)
Jab client message bhejega, Java backend kaise handle karega:

```java
package com.learning.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // ==========================================
    // 1. PUBLIC BROADCAST (Group Chat)
    // ==========================================
    @MessageMapping("/chat.sendPublic")
    @SendTo("/topic/public")
    public ChatMessage sendToGroup(@Payload ChatMessage message, Principal principal) {
        message.setSender(principal != null ? principal.getName() : "Anonymous");
        // DB me save karne ka logic yahan likho...
        return message;
    }

    // ==========================================
    // 2. PRIVATE 1-to-1 CHAT (Direct Message)
    // ==========================================
    @MessageMapping("/chat.sendPrivate")
    public void sendToUser(@Payload PrivateMessage message, Principal principal) {
        message.setSender(principal.getName());
        
        // Target user ke private queue par push karo
        messagingTemplate.convertAndSendToUser(
                message.getRecipientId(), 
                "/queue/private", 
                message
        );
    }
}
```

#### 🔍 Line-by-Line Breakdown: `ChatController.java`

| Line / Code | Iska Asli Meaning (Kyu Likha?) |
| :--- | :--- |
| `@Controller` | Spring bean register karta hai jo WebSocket STOMP messages ko listen karega. |
| `SimpMessagingTemplate messagingTemplate` | Spring ka built-in push engine. Iski madad se Java code kabhi bhi kisi bhi user ya topic par message bhej sakta hai. |
| `@MessageMapping("/chat.sendPublic")` | Client jab `/app/chat.sendPublic` par STOMP SEND frame bhejta hai, toh Spring is method ko trigger karta hai. |
| `@SendTo("/topic/public")` | Is method ka jo `return message` hoga, Spring use automatically `/topic/public` topic par bhej dega, jisse sabhi subscribers ko ek sath mil jaye! |
| `@Payload ChatMessage message` | STOMP message body ke JSON ko deserialize karke Java POJO/DTO me convert karta hai (jaise REST me `@RequestBody` karta hai). |
| `Principal principal` | Method parameter me current connected user ki identity Spring automatically inject kar deta hai (jo HandshakeHandler ne banayi thi). |
| `@MessageMapping("/chat.sendPrivate")` | Client jab `/app/chat.sendPrivate` par direct message post karta hai tab trigger hota hai. |
| `messagingTemplate.convertAndSendToUser(...)` | **1-to-1 Magic Method:** Yeh method 3 cheezein leta hai: `(recipientId, destination, payload)`. Spring internally is destination ko transform karke `/user/{recipientId}/queue/private` bana deta hai aur sirf us user ko deliver karta hai! |

---

### Step 5: Frontend Client Code (`JavaScript`)
Frontend browser me STOMP client kaise connect hota hai:

```html
<!DOCTYPE html>
<html>
<head>
    <title>STOMP Chat Demo</title>
    <!-- SockJS and STOMP Libraries -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
</head>
<body>
    <script>
        const myUserId = "user_101";

        // 1. Handshake Endpoint (/ws) par connect karo
        const socket = new SockJS('/ws?userId=' + myUserId);
        const stompClient = Stomp.over(socket);

        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);

            // 2. PUBLIC TOPIC KO SUBSCRIBE KARO (Group messages sunne ke liye)
            stompClient.subscribe('/topic/public', function (messageOutput) {
                console.log("Group Message Aaya: ", JSON.parse(messageOutput.body));
            });

            // 3. PRIVATE QUEUE KO SUBSCRIBE KARO (Direct messages sunne ke liye)
            stompClient.subscribe('/user/queue/private', function (messageOutput) {
                console.log("Personal Message Aaya: ", JSON.parse(messageOutput.body));
            });

            // 4. GROUP ME MESSAGE BHEJO (/app prefix laga kar)
            stompClient.send("/app/chat.sendPublic", {}, JSON.stringify({
                content: "Hello everyone!"
            }));

            // 5. KISI KO PRIVATE MESSAGE BHEJO
            stompClient.send("/app/chat.sendPrivate", {}, JSON.stringify({
                recipientId: "user_202",
                content: "Hey, private baat karni hai!"
            }));
        });
    </script>
</body>
</html>
```

#### 🔍 Line-by-Line Breakdown: Frontend JavaScript

| Line / Code | Iska Asli Meaning (Kyu Likha?) |
| :--- | :--- |
| `new SockJS('/ws?userId=' + myUserId)` | Browser HTTP GET request bhej kar `/ws` par Handshake start karta hai aur query param me user ID pass karta hai. |
| `Stomp.over(socket)` | Raw WebSocket connection ke upar STOMP protocol ka layer chadhata hai taaki frame-based messaging ho sake. |
| `stompClient.connect({}, function(frame) { ... })` | Server ko STOMP `CONNECT` frame bhejta hai. Jab server se `CONNECTED` frame aata hai tab yeh callback function execute hota hai. |
| `stompClient.subscribe('/topic/public', ...)` | Client server ko bolta hai: *"Mujhe `/topic/public` group me add karo, jab bhi koi yahan message dale mujhe turant forward karna."* |
| `stompClient.subscribe('/user/queue/private', ...)` | Client apne personal mailbox ko listen karne lagta hai. *(Note: Client hamesha `/user/queue/...` likhta hai, Spring internally session ID se map kar leta hai).* |
| `stompClient.send("/app/chat.sendPublic", ...)` | Client message ko **Java Controller** me bhejne ke liye destination me `/app` lagata hai taaki `@MessageMapping` run ho sake. |
| `stompClient.send("/app/chat.sendPrivate", ...)` | Private message bhejta hai jisme body ke andar `recipientId` hoti hai. |

---

## 6. Annotations & Tools Cheat Sheet (Usecase & Purpose)

| Annotation / Class | Usecase | Purpose & Kaam |
| :--- | :--- | :--- |
| **`@EnableWebSocketMessageBroker`** | Config class par lagti hai | Spring Boot ko bolti hai: *"Is application me STOMP protocol messaging enable kar do."* |
| **`WebSocketMessageBrokerConfigurer`** | Config class implement karti hai | Endpoints (`/ws`) register karne aur broker paths (`/app`, `/topic`, `/queue`) set karne ka interface. |
| **`@MessageMapping("/path")`** | Controller methods par | Jaise REST me `@PostMapping` hota hai, waise hi STOMP me client se aane wale frame ko Java method se map karta hai. |
| **`@SendTo("/topic/...")`** | Controller method return par | Method jo object return karega, use seedha specified `/topic` par broadcast kar do. |
| **`@SendToUser("/queue/...")`** | Controller method return par | Method jo object return karega, use sirf message bhejne wale user ke private `/queue` par push karo. |
| **`SimpMessagingTemplate`** | Controller ya Service me inject hoti hai | Java code se jab server ko khud (proactively) kisi topic ya specific user ko message deliver karna ho (`convertAndSend`, `convertAndSendToUser`). |
| **`Principal`** | Controller method parameter | Connected socket ke authenticated user ka naam/ID deta hai (`principal.getName()`). |

---

## 🚀 The Complete Mental Picture (Ek Nazar Me Flow)

```text
[ Browser / App ]
      │
      │ 1. Connect karta hai: http://localhost:8080/ws?userId=alex
      ▼
[ UserHandshakeInterceptor ] ──► URL se "alex" nikala, session bag me dala
      │
      ▼
[ UserHandshakeHandler ]     ──► Session bag se "alex" nikal kar Principal("alex") banaya
      │
      ▼
[ WebSocket Line Open (Handshake Complete!) ]
      │
      │ 2. Client message bhejta hai: SEND destination: /app/chat.sendPublic
      ▼
[ @MessageMapping("/chat.sendPublic") in ChatController ]
      │
      │ 3. Method returns response + @SendTo("/topic/public")
      ▼
[ SimpleBroker (/topic/public) ]
      │
      │ 4. Broadcasts to all connected listeners
      ▼
[ Sabhi Users ki Screens par message pop up ho gaya! 🎉 ]
```
