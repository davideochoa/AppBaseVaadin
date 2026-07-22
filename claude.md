# CLAUDE-RESTART.md

This is a second, leaner spec file. Use it instead of `claude.md` if you
ever need to **regenerate this project from zero** (new repo, new
machine, or a fresh AI session with no memory of Phases 1-2). `claude.md`
is the full historical log (every bug found, every narrative detail of
what was verified and how) — useful as an appendix, but not what you want
to feed a fresh build. This file is the distilled spec **plus the
hard-won lessons already baked in as rules**, so the same bugs aren't
rediscovered a second time.

If both files exist, `claude.md` remains the source of truth for what is
*actually built*; this file is the source of truth for what to build
*next time*, correctly, from the start.

## Language convention

All code, comments, commit messages, docs (`README.md`, `index.html`,
this file) and any other project annotations are written in **English**
from here on — including entity/field/table names, REST paths, and error
messages. This applies going forward; it does not require renaming
already-pushed git branches purely for their name.

## Goal

Multi-module monorepo for Vaadin with a microservices architecture.
Frontend (`app-vaadin`) and backend are fully decoupled from the start
(REST/JWT, plus Kafka events for auditing), so Vaadin can be swapped for
another frontend later without touching the backend.

## Monorepo structure

```
repo-root/
├── .github/
│   ├── dependabot.yml
│   └── workflows/
│       ├── ms-users-ci.yml         (paths: ms-users/**)
│       ├── ms-security-ci.yml      (paths: ms-security/**)
│       ├── ms-audit-ci.yml         (paths: ms-audit/**)
│       └── app-vaadin-ci.yml       (paths: app-vaadin/**)
├── ms-users/
├── ms-security/
├── ms-audit/
├── app-vaadin/
├── docker-compose.yml
├── .env.example
├── .gitignore
├── README.md
├── claude.md              (full historical log)
└── CLAUDE-RESTART.md       (this file)
```

## Stack

Java 21, Spring Boot 3.3.5, Maven, Vaadin Flow, PostgreSQL (one DB per
microservice, own least-privilege user), Flyway, Spring Security + JWT,
Google OAuth2/OIDC login (ms-security only), Kafka + Zookeeper, Jakarta
Bean Validation, springdoc-openapi, Spring Boot Actuator, Testcontainers +
JUnit 5.

## Architecture

- `app-vaadin`: no DB access, no business logic. Only consumes the 3
  microservices via REST. `facade` layer delegates to `client`, no
  `@Transactional`, no `Repository` — a convenience layer so `views`
  don't call `client` directly.
- Microservices `service` layer: `@Transactional`, uses `Repository`
  (JPA). This is the real business logic, each service owning its own
  database.
- `ms-security` is the single source of identity: it's the only service
  that issues JWTs and the only one that talks to Google. `ms-users`
  and `ms-audit` only *validate* the JWT.
- `ms-security` has its own `client` **only** to call `ms-users` for
  Google auto-provisioning (create the user if it doesn't exist on first
  Google login). This is the only inter-service REST call in the system.
  **Local login (email+password) never calls `ms-users`** — see
  Lesson 5 below for why.
- `ms-audit` has no business logic: it only consumes Kafka events
  from the `audit` topic and exposes a read-only endpoint.

---

## Lessons learned in Phases 1-2 — apply these from day one

These aren't historical trivia — each one cost real debugging time last
time and is very likely to recur if the equivalent code is written the
same "obvious" way again.

### 1. PostgreSQL: `CONCAT`/`LOWER` on a nullable String JPQL parameter resolves to `bytea`

**Symptom:** `function lower(bytea) does not exist`, only visible against
a real PostgreSQL (Testcontainers/`docker run`), never against H2.

**Cause:** Hibernate 6 + `PostgreSQLDialect` translates JPQL `CONCAT` into
the `||` operator. If a bound `String` parameter has no explicit JDBC
type (the normal case for an optional filter param that may be `null`),
Postgres resolves the ambiguous `||` overload as `bytea || bytea` instead
of `text || text`.

**Rule going forward:** any JPQL filter that does
`LOWER(CONCAT('%', :param, '%'))` on an optional String parameter must
wrap the parameter as `CAST(:param AS string)` inside the `CONCAT(...)`
call. Also add explicit `columnDefinition` on the entity's String
`@Column`s used this way, as a defensive, self-documenting guard. Do this
the first time a "search with optional filters + Pageable" repository
method is written — don't wait for CI to catch it.

### 2. `LazyInitializationException` mapping a lazy `@ManyToOne` to a response DTO

**Symptom:** exception thrown when a controller (outside the service's
`@Transactional` boundary) reads a lazy association via
`SomeResponse.from(entity)` after the Hibernate session has closed.

**Rule going forward:** keep the association `fetch = LAZY` on the
entity (don't flip it to EAGER — that's a blunt fix with a performance
cost). Instead fix it at the query level: override the single-`findById`
repository method with `@EntityGraph(attributePaths = "associatedEntity")`,
and add `JOIN FETCH` on the same association in any paginated "search"
query (safe with `Pageable` for a to-one association; would NOT be safe
for a `@OneToMany`/collection — don't `JOIN FETCH` a collection alongside
`Pageable`, that's a different, well-known Hibernate pitfall). Any
service method that always assigns a freshly-loaded entity before saving
(rather than passing through a lazy reference) is already safe and needs
no change.

### 3. Spring Security's default `LogoutFilter` silently hijacks a custom `POST /logout` endpoint

**Symptom:** a hand-written `@PostMapping("/logout")` controller method
that does real DB revocation is never actually invoked; the request gets
a `302 Location: /login?logout` instead of the controller's response, and
whatever the endpoint was supposed to revoke keeps working afterward.
Easy to miss with mock-based tests that don't assert the literal HTTP
status/Location header.

**Cause:** `spring-boot-starter-security` on the classpath auto-registers
a `LogoutFilter` on `POST /logout` (its own conventional URL) that
intercepts the request in the filter chain before it ever reaches the
`DispatcherServlet`.

**Rule going forward:** any stateless REST API secured with Spring
Security that defines its own endpoint at a path Spring Security has an
opinion about by default (`/logout` is the one that bit us; `/login`
under `formLogin()` is the same category of trap) must explicitly disable
the corresponding default handler in `SecurityConfig` from the start:
`.logout(logout -> logout.disable())`. When manually verifying, check the
actual HTTP status/headers of a raw request — a 200-range status is not
proof; this bug returned a 302 that looked superficially like "it
responded."

### 4. `JWT_SECRET` "known only to ms-security" only makes sense with asymmetric signing

If ms-users/ms-audit need to validate JWTs without ever sharing a
secret with ms-security, the signing algorithm must be **RS256 (or other
asymmetric alg), not HS256**. Build this in from the start:
- `ms-security` owns an RSA keypair (persist via
  `app.jwt.private/public-key-pem` env vars in real environments;
  ephemeral-at-startup with a loud `WARN` log is acceptable for local
  dev, never for anything shared).
- `ms-security` exposes `GET /.well-known/jwks.json`.
- Every other microservice points
  `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` at that JWKS
  endpoint (env-var configurable, e.g. `MS_SECURITY_JWKS_URI`).
- Don't design around a shared `JWT_SECRET` string and try to retrofit
  asymmetric signing later — decide this before writing `JwtService`.

### 5. Local (email+password) login must never depend on another microservice being up

If `ms-security` looked up the user in `ms-users` on every login,
`ms-users` being down would take down authentication entirely — a
single point of failure the architecture doesn't need. Instead:
`ms-security` keeps **its own cache table** (`user_security`:
user id, email, password hash, cached role, auth provider, active
flag) with everything needed to authenticate and mint a JWT on its own.
Only the Google login path calls `UsersClient` (check-or-create by
email + a memoized lookup of the default non-admin `user_type`), and
only the first time a given Google email is seen — cache the result so
repeat Google logins don't call `ms-users` again either. Apply this
split the first time `AuthService`/`GoogleLoginService` are written, not
as a later refactor.

### 5b. A Google-provisioned user must not default to admin privileges

The catalog seed must include a non-admin default `user_type` (e.g.
"User") from the *first* migration pass, not just "Administrator".
Auto-provisioning on Google login should assign that non-admin type by
default. Retrofitting this after seed data already shipped with only one
admin `user_type` means an extra migration later — seed both
`user_type` rows in `ms-users` Phase 1, even though ms-security
doesn't exist yet at that point.

### 6. Testcontainers is currently broken on this dev machine — plan verification around that, don't fight it again

Environment: Windows, Docker Desktop 4.73.1 / Engine 29.4.3 / API 1.54
(built 2026-05). `docker-java` (both the Testcontainers-bundled version
and an upgraded 1.20.4/docker-java 3.4.0) gets `BadRequestException
Status 400` with an all-empty `Info` body over both the named pipe and
plain TCP. The daemon itself is healthy (confirmed via a raw hand-crafted
HTTP request against the same pipe/port) — this is specifically
`docker-java`'s HTTP client failing against this Docker Engine build.
Already ruled out: Avast, Docker Desktop's Resource Saver, named-pipe
framing. Full detail: see the `ms-users-testcontainers-blocked`
memory.

**Rule going forward:**
- Still **write** the Testcontainers integration tests per module (they
  work fine in CI/GitHub Actions and on other machines) — don't skip
  writing them just because they can't run here.
- Don't burn time re-diagnosing this on this machine; check once per
  session whether a newer Testcontainers/docker-java release fixed it,
  otherwise move on.
- For manual end-to-end verification on this machine specifically, use
  the proven workaround: `docker run -d postgres:16-alpine` (plain CLI,
  which works fine) for each microservice's DB, point the real Spring
  Boot app at it via env vars (`DB_<SERVICE>_HOST/PORT/NAME/USER/PASSWORD`,
  `SERVER_PORT`) with `mvn spring-boot:run`, let Flyway apply real
  migrations, then `curl`/Postman the endpoints directly. This is how
  every bug in Lessons 1-3 above was actually found and confirmed fixed.
- Treat "`mvn clean package -DskipTests` succeeds" and "manually verified
  via the workaround above" as the two checkpoints per phase on this
  machine — not "`mvn verify` passes," which has never completed here.
- **Before assuming it's the `docker-java` bug, rule out the simpler
  failure first:** confirm Docker Desktop is actually running with
  `docker info`. On this machine it is not left running between
  sessions, so `docker run` fails with a `dockerDesktopLinuxEngine` named
  pipe connection error — a different, mundane problem, not the
  `docker-java`/Testcontainers incompatibility. Launch it with
  `"/c/Program Files/Docker/Docker/Docker Desktop.exe"` and poll
  `docker info` until it succeeds (~1-2 min) before reaching for the
  workaround below.

### 7. `AutoConfigureTestDatabase` lives in the `jdbc` package, not `orm.jpa`

**Symptom:** `cannot find symbol: class AutoConfigureTestDatabase` when
importing `org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase`
in a `@DataJpaTest` + Testcontainers test — a plausible-looking guess
that doesn't compile.

**Rule going forward:** the real package is
`org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase`.
Needed alongside `@DataJpaTest` with
`@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`
any time the test should hit a real Testcontainers database instead of
the auto-replaced in-memory one.

---

## Security spec (build this in from Phase 1, not retrofitted)

- Passwords: BCrypt, never plain text.
- Postgres credentials, `JWT_SECRET`-equivalent (RSA keys), Google client
  id/secret: env vars only, via `.env` (git-ignored) +
  `docker-compose.yml`; `.env.example` as a template with no real values.
- Distinct Postgres user per microservice, least privilege.
- JWT claims: `sub`, `email`, `role`/`userType`. Access token ~5-10 min,
  refresh token ~7 days, individually revocable (DB table).
- Logout does real revocation (see Lesson 3 for the trap that breaks
  this silently).
- `app-vaadin` stores the JWT server-side in `VaadinSession`, never
  browser localStorage/cookies. Outgoing-call interceptor attaches the
  JWT and refreshes it on expiry before retrying.
- All 3 microservices validate JWT via
  `oauth2ResourceServer().jwt()` + JWKS (Lesson 4), role-gated writes via
  `@PreAuthorize("hasRole('ADMINISTRATOR')")`, GET endpoints `permitAll()`.
- Custom `JwtAuthEntryPoint`/`JwtAccessDeniedHandler` so 401/403 use the
  same `ApiError` shape as every other error response.
- CORS: each microservice accepts only the `app-vaadin` origin.
- Rate limiting (Bucket4j, in-memory per-IP) on `/login` and
  `/login/google` specifically, not every endpoint.
- Explicit HTTP security headers (don't rely on Spring Security
  defaults): CSP, `X-Frame-Options: DENY`, `X-Content-Type-Options:
  nosniff`, HSTS, `Referrer-Policy`.
- Centralized `@RestControllerAdvice` per microservice: 400 with
  field-level detail for `MethodArgumentNotValidException`, generic
  message + code for everything else, full detail only in internal logs.

## Build order

1. **ms-users**: full CRUD + Flyway + tests + Swagger + Actuator,
   working standalone. Seed **both** `user_type` rows (Lesson 5b), not
   just "Administrator". Write the "search" query with `CAST(:param AS
   string)` from the start (Lesson 1). `findById` with `@EntityGraph` and
   "search" with `JOIN FETCH` on the to-one association from the start
   (Lesson 2). No real JWT validation needed until Phase 2 lands.
2. **ms-security**: RS256 + JWKS from the start (Lesson 4), own
   `user_security` cache so local login has zero runtime dependency
   on ms-users (Lesson 5), `.logout(logout -> logout.disable())` from
   the start (Lesson 3), Google login as idToken-passthrough (client
   already obtained it — no server-side redirect flow, since app-vaadin
   doesn't exist yet). Go back and add JWT validation +
   `@PreAuthorize` to `ms-users` in this phase, plus
   `findByEmailIgnoreCase`/`GET /users/by-email` for the
   auto-provisioning lookup.
3. **ms-audit + Kafka**: event producer in ms-users/ms-security,
   consumer in ms-audit. `ms-security` publishes to `audit` on
   every failed login (email attempted, IP, timestamp — never the
   password), type `LOGIN_FAILED`.
4. **app-vaadin**: consumes the 3 microservices, no DB, no business
   logic (see Architecture). i18n via `I18NProvider`, EN/ES.
5. **Integration**: full `docker-compose.yml`, CI workflows scoped by
   `paths:`, Dependabot, final polish.

---

## Progress log

### Phase 1 — `ms-users`: DONE (2026-07-22)

Built standalone per the build order: full CRUD (`User` +
read-only `UserType` catalog), Flyway migrations seeding both
`user_type` rows from `V3` (Lesson 5b), `search` query with
`CAST(:param AS string)` inside `CONCAT`/`LOWER` (Lesson 1),
`findById` via `@EntityGraph` + `search` via `JOIN FETCH` on
`userType` (Lesson 2), centralized `ApiError`/`GlobalExceptionHandler`
(400 validation / 404 not-found / 409 duplicate email / 500 generic),
Swagger UI + `springdoc`, Actuator `health`/`info`. No Spring Security /
JWT yet, as scoped. Soft-delete (`active=false`) chosen for `DELETE
/users/{id}`; Lombok added for boilerplate (not in the original spec,
low-risk addition). Package base: `com.appbasevaadin.msusers`.

Verified two ways, per Lesson 6:
- `mvn clean package -DskipTests` — compiles and packages cleanly.
- Full manual end-to-end pass against a real, disposable Postgres
  (`docker run postgres:16-alpine` + `mvn spring-boot:run` + `curl`):
  health UP, both seeded `user_type` rows present, full CRUD flow,
  `search?text=...` confirmed **not** hitting the `bytea` bug from
  Lesson 1 against a real Postgres, `findById`/`search` confirmed
  **not** throwing `LazyInitializationException` from Lesson 2, 400/404/409
  all returning the shared `ApiError` shape, Swagger UI and
  `/v3/api-docs` reachable.
- Testcontainers-backed tests (`UserRepositoryIT`,
  `UserControllerIT`) were written but, per Lesson 6, could not be run
  locally — they run in CI.

Also added `.github/workflows/ms-users-ci.yml` — CI for this module
scoped to `paths: ms-users/**` running `mvn clean verify` (the real
Testcontainers suite, since GitHub Actions runners have Docker and this
machine doesn't). This is pulled forward from Phase 5 on purpose, scoped
to just this one module — the rest of Phase 5 (`docker-compose.yml`,
the other services' workflows, Dependabot) is still pending.

New lessons from actually building this phase were folded into the
Lessons section above as **Lesson 6's added bullet** (Docker Desktop not
running vs. the `docker-java` bug are two different failure modes — check
`docker info` first) and **Lesson 7** (`AutoConfigureTestDatabase` is in
`...test.autoconfigure.jdbc`, not `...orm.jpa`).

Not yet started: `ms-security`, `ms-audit`, `app-vaadin`,
`docker-compose.yml`, the remaining CI workflows, Dependabot.

### Language switch to English (2026-07-22)

Decided to standardize on English for all code, comments, docs, and
annotations going forward (see "Language convention" above). Applied
retroactively to everything built in Phase 1, since it was originally
written with Spanish entity/field/endpoint names:
- Module renamed `ms-usuarios` → `ms-users` (directory, Maven
  `artifactId`, Java package `com.appbasevaadin.msusuarios` →
  `com.appbasevaadin.msusers`, CI workflow file and its `paths:`
  trigger, env var prefix `DB_USUARIOS_*` → `DB_USERS_*`).
- Entities/tables: `Usuario`/`usuario` → `User`/`app_user` (table named
  `app_user`, not the bare `user`, since `USER` is a reserved word in
  PostgreSQL); `TipoUsuario`/`tipo_usuario` → `UserType`/`user_type`.
- Fields: `nombre`/`apellidos` → `firstName`/`lastName`, `activo` →
  `active`, `fechaCreacion` → `createdAt`, `tipoUsuario` → `userType`,
  `descripcion` → `description`.
- REST paths: `/usuarios` → `/users`, `/tipos-usuario` → `/user-types`.
- Seed values: `"Administrador"` → `"Administrator"`, `"Usuario"` (the
  non-admin type) → `"User"`.
- `ApiError` fields translated too: `codigo`/`mensaje`/`errores` →
  `code`/`message`/`errors`; nested `ErrorCampo` → `FieldError`
  (`campo` → `field`); error codes `VALIDACION`/`NO_ENCONTRADO`/
  `CONFLICTO_DATOS`/`ERROR_INTERNO` → `VALIDATION`/`NOT_FOUND`/
  `DATA_CONFLICT`/`INTERNAL_ERROR`.
- Flyway migrations renamed and rewritten in place (`V1`-`V3`) rather
  than superseded by new versions — safe to do because no shared/
  persistent database had ever run them; the only Postgres that saw
  them was the disposable manual-verification container from Lesson 6,
  already torn down.
- This document's own architecture/lessons/build-order sections updated
  throughout for the new module and business-term names (`ms-seguridad`
  → `ms-security`, `ms-auditoria` → `ms-audit`, `usuario_seguridad` →
  `user_security`, `auditoria` Kafka topic → `audit`, `LOGIN_FALLIDO` →
  `LOGIN_FAILED`, `rol`/`tipoUsuario` JWT claim → `role`/`userType`).
- Left unchanged: the git branch name (`feature/ms-usuarios_fase1`) and
  the `.gitignore` fix that was needed alongside this rename (Maven's
  `target/` had been accidentally committed; added `target/` to
  `.gitignore` and removed it from version control) — noted here since
  it was discovered as a side effect of trying to `git mv` the module
  directory, not because it's part of the language-convention change.

Re-verified after the rename: `mvn clean package -DskipTests` compiles
cleanly (main and test sources) under the new package/class names.

At the end of each phase, fold anything newly learned back into this
file's Lessons section (rule form: symptom → cause → rule), and record
the full narrative in `claude.md` as before.
