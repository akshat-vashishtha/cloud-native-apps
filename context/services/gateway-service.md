# Service: Gateway-Service
Status: implemented   Owner: @Alex, @Maya   Updated: 2026-10-22
Inputs: context/architecture.md, context/implementation.md

## Summary
Reactive Spring Cloud Gateway BFF. Confidential OAuth2 client against `oauth2-authorization-server`, Spring Session Redis (`APP_SESSION` cookie), `TokenRelay` of access tokens to JWT resource servers. CORS for `web` (`http://localhost:3000`). HMAC identity signing **retired**.

## Decisions

- **SVC-GW-001**: Spring Cloud Gateway (WebFlux).
- **SVC-GW-002**: Spring Session Redis + cookie name `APP_SESSION` (not custom Hash-only BFF filter).
- **SVC-GW-003**: OAuth2 Authorization Code login; success redirect to `WEB_BASE_URL`.
- **SVC-GW-004**: Default filter `TokenRelay=` on routes; downstream trust = Bearer JWT.
- **SVC-GW-005**: Public paths: actuator, `/api/v1/auth/register`, `/oauth2/**`, `/login/**`.

## Routes (live)

| Route | Upstream |
| :--- | :--- |
| `/api/v1/auth/**`, `/api/v1/roles/**` | user-rbac `:8081` |
| `/api/v1/banking/**` | business-operation `:8082` |
| `/api/v1/kpi/**` | kpi-analytics `:8084` |
| **`Gateway-Service`** | `/api/v1/ai/**` | `intelligence-ai-service` `:8085` |

## Plan
Approved: yes

- [x] OAuth2 client + Spring Session + TokenRelay
- [x] CORS credentials for SPA
- [x] Remove HMAC signer / custom JWT public-key BFF filter
- [ ] E2E integration tests

## Next steps
- Harden cookie `Secure` for HTTPS; optional SameSite tuning for non-localhost.
