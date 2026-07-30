# Running the project locally

Two ways to run the stack on a dev machine. Start with **Option 1**; fall
back to **Option 2** only for whichever module gives you trouble (this
repo's `CLAUDE-RESTART.md` documents machine-specific Docker/Testcontainers
issues seen before — Option 2 is the proven workaround for those).

## Option 1 — `docker-compose` (everything together)

```bash
# 1. Copy the example env file (local/demo defaults work as-is)
cp .env.example .env

# 2. Make sure Docker Desktop is running
docker info

# 3. Build and start everything: 3 Postgres instances, Kafka+Zookeeper,
#    the 3 microservices, and app-vaadin
docker compose up --build
```

Once everything is up:

- `app-vaadin` (the app itself): http://localhost:8080
- `ms-users` Swagger UI: http://localhost:8081/swagger-ui.html
- `ms-security` Swagger UI: http://localhost:8082/swagger-ui.html
- `ms-audit` Swagger UI: http://localhost:8083/swagger-ui.html

Bootstrap admin login (unless `BOOTSTRAP_ADMIN_PASSWORD` was set in
`.env`): `admin@local` / `admin123`.

`docker compose down` keeps the Postgres data volumes; add `-v` to wipe
them and start from a clean seed on the next `up`.

On a slow or metered connection, pulling/building 4 images plus
Kafka/Zookeeper is heavy. Two things help:
- Lower Docker Desktop's `max-concurrent-downloads`/`max-concurrent-uploads`
  in `~/.docker/daemon.json` (e.g. `2`) — requires a Docker Desktop restart.
- Run `COMPOSE_PARALLEL_LIMIT=1 COMPOSE_HTTP_TIMEOUT=300 docker compose up
  --build` to pull/start services one at a time instead of all at once.

**Known caveat:** on machines where a TLS-intercepting antivirus (e.g.
Avast Web/Mail Shield) is active, `app-vaadin`'s containerized JRE may
fail Vaadin's online license check on startup, even though the other 3
services start fine in containers — the host-level certificate fix
doesn't carry over into the container's own JRE truststore. If you hit
this, run `app-vaadin` via Option 2 instead (plain `mvn spring-boot:run`
on the host uses the host JDK's already-fixed truststore).

## Option 2 — run each piece by hand

Useful when Docker/Testcontainers is acting up for a specific module,
most commonly `app-vaadin`.

**On Windows, `scripts/start-infra.bat` + `scripts/start-ms.bat` automate
this option** (still uses plain `docker run` containers for Postgres/Kafka
only — the 3 microservices and `app-vaadin` itself run natively via `mvn
spring-boot:run`, no Docker involved for them):

```bat
scripts\start-infra.bat
REM wait a few seconds for Postgres/Kafka to finish starting, then:
scripts\start-ms.bat
REM stop later with:
scripts\stop-infra.bat
```

`start-ms.bat` opens 4 separate `cmd` windows (ms-users, ms-security,
ms-audit, app-vaadin), each forcing `JAVA_HOME` to JDK 21 explicitly (see
the note on `app-vaadin`'s CI/Phase 4 entry in `CLAUDE-RESTART.md` about
this machine's default `JAVA_HOME` being a newer JDK). Manual steps below
are what those scripts automate, useful if you want to run a subset by
hand or troubleshoot one module in isolation.

```bash
# Standalone Postgres containers, one per service
docker run -d --name pg-users    -p 5433:5432 -e POSTGRES_DB=ms_users    -e POSTGRES_USER=ms_users_app    -e POSTGRES_PASSWORD=ms_users_pass    postgres:16-alpine
docker run -d --name pg-security -p 5434:5432 -e POSTGRES_DB=ms_security -e POSTGRES_USER=ms_security_app -e POSTGRES_PASSWORD=ms_security_pass postgres:16-alpine
docker run -d --name pg-audit    -p 5435:5432 -e POSTGRES_DB=ms_audit    -e POSTGRES_USER=ms_audit_app    -e POSTGRES_PASSWORD=ms_audit_pass    postgres:16-alpine
docker run -d --name kafka -p 9092:9092 apache/kafka-native
```

If the containers already exist from a previous session, `docker start
pg-users pg-security pg-audit kafka` instead of `docker run` again.

```bash
# Point JAVA_HOME at JDK 21 explicitly if the machine's default is a
# different version
export JAVA_HOME="/c/Program Files/Java/jdk-21"

# Each microservice in its own terminal
cd ms-users    && DB_USERS_PORT=5433 SERVER_PORT=8081 mvn spring-boot:run

cd ms-security && DB_SECURITY_PORT=5434 SERVER_PORT=8082 \
  KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
  MS_USERS_BASE_URL=http://localhost:8081 mvn spring-boot:run

cd ms-audit    && DB_AUDIT_PORT=5435 SERVER_PORT=8083 \
  KAFKA_BOOTSTRAP_SERVERS=localhost:9092 mvn spring-boot:run

cd app-vaadin  && mvn spring-boot:run   # plain dev mode, no VAADIN_PRODUCTIONMODE
```

Health/JWKS endpoints worth checking while services come up:
- http://localhost:8081/actuator/health
- http://localhost:8082/actuator/health
- http://localhost:8082/.well-known/jwks.json
- http://localhost:8083/actuator/health

Bootstrap admin login (unless `BOOTSTRAP_ADMIN_PASSWORD` was set in the
environment): `admin@local` / `admin123`.

Containers from this workaround are disposable — safe to leave running
between sessions or to remove (`docker rm -f pg-users pg-security
pg-audit kafka`) once you're done.
