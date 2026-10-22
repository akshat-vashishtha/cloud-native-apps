# Service: User-Rbac-Service
Status: implemented   Owner: @Alex, @Leo   Updated: 2026-10-22
Inputs: context/architecture.md, context/implementation.md

## Summary
Identity + RBAC data plane only. MongoDB `user_rbac_db` (`users`, `roles`), BCrypt passwords, Redis Spring Cache. **Does not mint JWTs.** Authorization Server loads credentials via internal API. Public APIs are OAuth2 JWT resource servers (`issuer-uri`).

## Decisions

- **SVC-USER-001**: No local RSA/JWT issuance — tokens owned by `oauth2-authorization-server` (ARCH-001).
- **SVC-USER-002**: Roles/permissions resolved dynamically; seeded via `RbacDataSeeder`.
- **SVC-USER-003**: BCrypt strength 12.
- **SVC-USER-004**: Dual API — REST (register, `/me`, roles admin) + gRPC (`GetUserProfile`, `ValidatePermissions`).
- **SVC-USER-005**: Internal `GET /api/v1/internal/users/by-email` with `X-Internal-Api-Key` for AS (returns passwordHash + roles/permissions).

## Live endpoints

| Endpoint | Auth | Notes |
| :--- | :--- | :--- |
| `POST /api/v1/auth/register` | Public | Returns `{userId,email}` — **no accessToken** |
| `GET /api/v1/auth/me` | Bearer JWT | Subject = userId |
| `GET /api/v1/internal/users/by-email` | API key | AS only |
| `/api/v1/roles/**` | JWT + `user:manage_roles` | Admin |
| gRPC `:9091` | Mesh | Profile / permission checks |

## Plan
Approved: yes

- [x] Strip JwtTokenService / public-key / login token response
- [x] Internal by-email for AS
- [x] JWT resource server
- [ ] Integration tests (Testcontainers)

## Next steps
- NetworkPolicy so only AS can hit `/api/v1/internal/**` in AKS.
