# Service: oauth2-authorization-server
Status: approved   Owner: @Alex   Updated: 2026-10-22
Inputs: context/architecture.md

## Summary
Spring Authorization Server. Sole token authority (RSA/JWKS). Hosts form login. Loads/verifies users via `user-rbac-service` internal API. Issues access/refresh JWTs with `roles` and `permissions` claims for resource servers.

## Decisions

- **SVC-AS-001**: Spring Authorization Server; issuer `http://localhost:9000`.
- **SVC-AS-002**: Confidential client `gateway-client` (authorization_code + refresh_token).
- **SVC-AS-003**: Form login on AS; UserDetails/auth via user-rbac internal authenticate API.
- **SVC-AS-004**: JWT customizer adds `email`, `roles`, `permissions`.

## Plan
Approved: yes

- [x] Boot app, JWK, registered client, form login
- [x] user-rbac client + token customizer
- [ ] Persist keys/clients for multi-node (later)

## Implementation status (2026-10-22)
- Gateway is wired as OAuth2 client (`gateway-client`); resource servers use `issuer-uri=http://localhost:9000`.
- UserDetails loaded from user-rbac internal by-email + API key; JWT customizer adds `roles` / `permissions`.

## Next steps
- Persist RSA keys / registered clients for multi-node; Theme login HTML if desired.
