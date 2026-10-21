# Service: User-Rbac-Service
Status: implemented   Owner: @Alex, @Leo   Updated: 2026-10-15
Inputs: context/architecture.md, context/implementation.md

## Summary
Low-Level Design (LLD) for `User-Rbac-Service`—the central authority for identity management, password security, and Role-Based Access Control (RBAC). Implements secure user registration, BCrypt password hashing, asymmetric RS256 JWT token signing, public key exposure for Gateway edge verification, granular permission resolution, and gRPC endpoints for internal user lookups.

---

## Decisions

- **SVC-USER-001**: **Asymmetric RS256 JWT Token Signing (Private Key)**
  - *Why*: `User-Rbac-Service` holds the private RSA key (`2048-bit`) and signs JWT tokens. `Gateway-Service` only holds the public key for zero-latency verification, preventing any service from forging tokens.
  - *Rejected*: Symmetric HMAC shared secrets for public token generation.

- **SVC-USER-002**: **Granular Role-to-Permission Expansion in Token Claims**
  - *Why*: JWT claims contain both high-level roles (`roles: ["ROLE_USER"]`) and resolved granular authorities (`permissions: ["account:read", "transfer:execute"]`), enabling downstream services to use fine-grained `@PreAuthorize("hasAuthority('transfer:execute')")` without extra database lookups.
  - *Rejected*: Storing only roles and requiring database permission queries per request.

- **SVC-USER-003**: **BCrypt Password Hashing with Strength 12**
  - *Why*: Resistant to GPU-based brute-force attacks and rainbow table lookups with auto-generated salts.
  - *Rejected*: Fast hashing algorithms (MD5, SHA-256) for passwords.

- **SVC-USER-004**: **Dual API (REST Authentication + gRPC Internal Profile RPC)**
  - *Why*: REST API for user registration, login, and token refresh; Protobuf/gRPC for internal microservice queries (`GetUserProfile`, `CheckPermissions`).
  - *Rejected*: Exposing internal administrative queries over public REST.

---

## Details

### 1. Dynamic RBAC Domain Models (`Role.java`, `User.java`) & `roles` Collection

Roles are no longer hardcoded in a static Java enum. They are dynamic entities persisted in MongoDB (`roles` collection) with Redis caching.

```java
// Dynamic Domain Entity
public class Role {
    private final String id;
    private final String name;
    private final String description;
    private final Set<String> permissions;
    private final boolean isSystemRole;
    ...
}

// User Aggregate holds role names as strings, decoupled from static enums
public class User {
    private final String id;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private Set<String> roles; // e.g. ["ROLE_USER", "ROLE_ADMIN"]
    private UserStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    ...
}
```

Permissions are resolved dynamically at runtime via `RoleService.resolvePermissionsForRoles(user.getRoles())` leveraging Redis cache.
Seed defaults on boot via `RbacDataSeeder.java`. Dynamic administration via `RoleController` (`/api/v1/roles`).

---

### 2. RS256 Asymmetric JWT Token Service (`JwtTokenService.java`)

```java
@Service
public class JwtTokenService {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private static final long ACCESS_TOKEN_VALIDITY_SECONDS = 900; // 15 Minutes

    public JwtTokenService(
        @Value("${app.jwt.private-key}") RSAPrivateKey privateKey,
        @Value("${app.jwt.public-key}") RSAPublicKey publicKey
    ) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofSeconds(ACCESS_TOKEN_VALIDITY_SECONDS));

        Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);

        return JWT.create()
            .withIssuer("user-rbac-service")
            .withSubject(user.getId())
            .withClaim("email", user.getEmail())
            .withClaim("fullName", user.getFullName())
            .withClaim("roles", user.getRoles().stream().map(Enum::name).toList())
            .withClaim("permissions", new ArrayList<>(user.getAllPermissions()))
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algorithm);
    }

    public RSAPublicKey getPublicKey() {
        return this.publicKey;
    }
}
```

---

### 3. Application & Service Layer (`AuthenticationService.java`)

```java
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserMongoRepository userRepository;
    private final PasswordEncoder passwordEncoder; // BCryptPasswordEncoder(12)
    private final JwtTokenService jwtTokenService;
    private final UserMapper mapper;

    @Override
    public AuthResponseDto register(RegisterRequestDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        String hashedPassword = passwordEncoder.encode(dto.password());
        User user = new User(UUID.randomUUID().toString(), dto.email(), hashedPassword, dto.fullName());

        UserDocument savedDoc = userRepository.save(mapper.toDocument(user));
        String accessToken = jwtTokenService.generateAccessToken(user);

        return new AuthResponseDto(savedDoc.getId(), user.getEmail(), accessToken, "Bearer");
    }

    @Override
    public AuthResponseDto login(LoginRequestDto dto) {
        UserDocument doc = userRepository.findByEmail(dto.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.password(), doc.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = mapper.toDomain(doc);
        String accessToken = jwtTokenService.generateAccessToken(user);

        return new AuthResponseDto(user.getId(), user.getEmail(), accessToken, "Bearer");
    }
}
```

---

### 4. gRPC Interface (`user_rbac_service.proto`)

```protobuf
syntax = "proto3";

package user.v1;

option java_multiple_files = true;
option java_package = "com.banking.user.grpc.v1";

service UserRbacGrpcService {
  rpc GetUserProfile (GetUserProfileRequest) returns (GetUserProfileResponse);
  rpc ValidatePermissions (ValidatePermissionsRequest) returns (ValidatePermissionsResponse);
}

message GetUserProfileRequest {
  string user_id = 1;
}

message GetUserProfileResponse {
  string user_id = 1;
  string email = 2;
  string full_name = 3;
  repeated string roles = 4;
  repeated string permissions = 5;
  string status = 6;
}

message ValidatePermissionsRequest {
  string user_id = 1;
  string required_permission = 2;
}

message ValidatePermissionsResponse {
  bool is_authorized = 1;
  string user_id = 2;
}
```

---

### 5. Persistence Layer (MongoDB: `user_rbac_db`)

```java
@Document(collection = "users")
@CompoundIndexes({
    @CompoundIndex(name = "email_unique_idx", def = "{'email': 1}", unique = true),
    @CompoundIndex(name = "roles_idx", def = "{'roles': 1}")
})
public class UserDocument {
    @Id private String id;
    private String email;
    private String passwordHash;
    private String fullName;
    private Set<Role> roles;
    private UserStatus status; // ACTIVE, SUSPENDED, DELETED
    private Instant createdAt;
    private Instant updatedAt;
    @Version private Long version;
}
```

---

### 6. Presentation Layer (REST Endpoints)

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authService;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @GetMapping("/public-key")
    public ResponseEntity<PublicKeyDto> getPublicKey() {
        String pem = Base64.getEncoder().encodeToString(jwtTokenService.getPublicKey().getEncoded());
        return ResponseEntity.ok(new PublicKeyDto("RS256", pem));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String internalUserId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return ResponseEntity.ok(userService.getUserProfileById(userId));
    }
}
```

---

## Plan
Approved: yes

- [x] 1. Spring Security & JWT dependencies (`spring-boot-starter-security`, `java-jwt`)
- [x] 2. Domain Models (`User`, `Role`, `UserStatus`) with granular permission expansion
- [x] 3. RS256 Key Pair Generator & Asymmetric `JwtTokenService` with `JwtProperties`
- [x] 4. BCrypt Password Encoder (strength 12) & Authentication Service
- [x] 5. MongoDB User Repository with Unique Email index & Optimistic Locking (`@Version`)
- [x] 6. Public Key JWK REST endpoint (`/api/v1/auth/public-key`)
- [x] 7. Protobuf definitions & gRPC service (`UserRbacGrpcService`) routed via `UserService`
- [x] 8. Redis Distributed Cache (`@Cacheable` on `users` with 30m TTL for sub-ms profile/permission lookups)
- [ ] 9. Integration tests with Testcontainers MongoDB & Redis

## Code Progress
- Complete implementation of Domain, Infrastructure, Security, Application (`UserService` + `AuthenticationService`), REST presentation, gRPC presentation, and Redis Caching done and compiling cleanly (`BUILD SUCCESS`).

## Open questions
- None.

## Change requests (to upstream files)
- None.

## Next steps
- Ready for Gateway-Service ingress routing and BFF session verification.
