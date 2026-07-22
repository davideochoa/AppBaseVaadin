# AppBaseVaadin

Reference monorepo for a microservices architecture with a Vaadin
frontend, fully decoupled from the backend from the start (REST + JWT,
Kafka events for auditing). The goal is to be able to swap Vaadin for
another frontend later without touching the microservices.

## Modules

| Module         | Responsibility                                                              | Status |
|----------------|------------------------------------------------------------------------------|--------|
| `ms-users`     | User CRUD and user-type catalog, owns its own database                      | ✅ Phase 1 complete |
| `ms-security`  | Single source of identity: local login + Google OAuth2/OIDC, issues JWTs (RS256) | ⏳ Pending |
| `ms-audit`     | Kafka event consumer (`audit` topic), exposes read-only history             | ⏳ Pending |
| `app-vaadin`   | Vaadin Flow frontend, no database or business logic of its own              | ⏳ Pending |

## Stack

Java 21 · Spring Boot 3.3.5 · Maven · Vaadin Flow · PostgreSQL (one
database per microservice) · Flyway · Spring Security + JWT (RS256/JWKS) ·
Google OAuth2/OIDC · Kafka · Bean Validation · springdoc-openapi ·
Spring Boot Actuator · Testcontainers + JUnit 5.

## Build plan

1. **`ms-users`** — full CRUD + Flyway + tests + Swagger + Actuator,
   working standalone. ✅ **Complete.**
2. **`ms-security`** — RS256 JWT + JWKS, its own credentials cache
   (`user_security`) so local login doesn't depend on `ms-users`,
   Google login via id-token, and back in `ms-users`: JWT validation +
   `@PreAuthorize` + lookup by email for auto-provisioning.
3. **`ms-audit` + Kafka** — event producer in `ms-users`/`ms-security`,
   consumer in `ms-audit` (includes `LOGIN_FAILED` on every failed login
   attempt).
4. **`app-vaadin`** — consumes the three microservices over REST, no
   database or business logic of its own, i18n EN/ES.
5. **Integration** — full `docker-compose.yml`, per-module CI workflows
   (`paths:`), Dependabot, final polish.

Full architecture detail, security decisions, and lessons learned
(bugs already fixed and the rules that keep them from recurring): see
[`claude.md`](./claude.md).

## Current status

Active branch: `feature/ms-usuarios_fase1`. `ms-users` has been built,
manually verified end-to-end against a real Postgres database, and has
its own CI workflow (`.github/workflows/ms-users-ci.yml`) that runs on
every change under `ms-users/**`.
