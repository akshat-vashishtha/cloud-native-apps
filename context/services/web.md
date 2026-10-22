# Service: web (SPA)
Status: implemented   Owner: @Alex   Updated: 2026-10-22
Inputs: context/architecture.md, `.agents/rules/frontend-coding-standards.md`

## Summary
Next.js App Router + Strict TypeScript BFF client. Folder layout: `src/app` + `src/ui` + `src/core` (controller_hooks operations/facades, service interface+impl, HttpClient, dto, types, validator). Simple HTML/CSS only. Password login is on the Authorization Server, not the SPA.

## Decisions

- **SVC-WEB-001**: Cookie-only browser auth (`APP_SESSION` via gateway); `credentials: "include"`.
- **SVC-WEB-002**: Sign in = navigate to `{GATEWAY}/oauth2/authorization/gateway-client`.
- **SVC-WEB-003**: Standards in `frontend-coding-standards.md` (full coding-standard template + §7 addenda).
- **SVC-WEB-004**: Simple HTML/CSS — no UI kits / Tailwind / animations.

## Pages (live)
- `/` — Home: profile via `/api/v1/auth/me` or Sign in button; Logout.
- `/register` — Register then Sign in (OAuth redirect).

## Plan
Approved: yes

- [x] Refactor to `src/core` + `src/ui`
- [x] Auth BFF flows
- [ ] Banking/chat UI (later)

## Next steps
- Optional Next rewrites to same-origin proxy gateway for cookie simplicity beyond localhost.
