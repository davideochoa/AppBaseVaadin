# Plan: rename del proyecto `AppBaseVaadin` → `VaadinBaseApp`

**Estado: NO EJECUTADO.** Este archivo documenta el plan acordado el
2026-07-31; se aplica en una rama dedicada, después de commitear y
mergear el trabajo en curso en `feature/run-ms` (username + reset
forzado de contraseña). No borrar este archivo hasta que el rename
esté hecho y verificado — bórralo como parte del PR que lo aplique.

**Precondición ya cumplida (verificado 2026-07-31):** `feature/run-ms`
(PR #40, merge `f974a45`) ya está mergeado en `main` —
`git log --oneline main..feature/run-ms` da vacío. `main` está en
`f974a45`, es la punta real. Se puede arrancar la Etapa 0 de la
sección "Plan de ejecución por etapas" de inmediato, sin esperar nada
más.

## Alcance confirmado con el usuario

- Rename **profundo**, no solo cosmético: incluye paquetes Java,
  `groupId` de Maven, y el repositorio de GitHub.
- Ramas de git: no hace falta renombrar ninguna a nivel de nombre —
  ver auditoría abajo, ninguna rama existente contiene literalmente
  "AppBaseVaadin". El commit del rename se aplica solo sobre `main` +
  la rama activa en el momento de ejecutarlo; las 7 ramas históricas
  de fases ya mergeadas se dejan intactas (no se reescriben).
- Momento: solo después de commitear (e idealmente mergear) el trabajo
  actual sin commitear en `feature/run-ms` — no mezclar ambos cambios
  grandes en el mismo diff.

## Inventario completo (auditado 2026-07-31)

### 1. Paquetes Java — 173 archivos `.java`

Paquete base `com.appbasevaadin` → `com.vaadinbaseapp`, en:

- `ms-users`: `com.appbasevaadin.msusers` → `com.vaadinbaseapp.msusers`
- `ms-security`: `com.appbasevaadin.mssecurity` → `com.vaadinbaseapp.mssecurity`
- `ms-audit`: `com.appbasevaadin.msaudit` → `com.vaadinbaseapp.msaudit`
- `app-vaadin`: `com.appbasevaadin.appvaadin` → `com.vaadinbaseapp.appvaadin`
- `app-vaadin-dummy`: mismo patrón, confirmado — también usa
  `com.appbasevaadin.appvaadin`.

Implica mover los directorios físicos bajo `src/main/java` y
`src/test/java` para que coincidan con el paquete (Java exige que la
estructura de carpetas refleje el paquete).

**No tocar**: las migraciones Java de Flyway ya viven en el paquete
`db.migration` (p. ej.
`ms-security/src/main/java/db/migration/V5__SetBootstrapAdminUsername.java`),
no bajo `com.appbasevaadin` — no les afecta este rename.

### 2. `groupId` de Maven — 5 `pom.xml`

`com.appbasevaadin` → `com.vaadinbaseapp` en: `ms-users/pom.xml`,
`ms-security/pom.xml`, `ms-audit/pom.xml`, `app-vaadin/pom.xml`,
`app-vaadin-dummy/pom.xml`. Los `artifactId` (`ms-users`, `app-vaadin`,
etc.) no derivan del nombre del proyecto — se quedan igual.

### 3. ⚠️ Trampa conocida — deserialización JSON de Kafka

`ms-audit/src/main/resources/application.yml` tiene el nombre de clase
completo **como string literal**, algo que un refactor automático de
"rename package" del IDE no va a tocar:

```yaml
spring.json.trusted.packages: com.appbasevaadin.msaudit.messaging
spring.json.value.default.type: com.appbasevaadin.msaudit.messaging.AuditEventMessage
```

Debe quedar como `com.vaadinbaseapp.msaudit.messaging` /
`...AuditEventMessage`. Si se omite: el consumer lanza
`MessageConversionException` en runtime, silencioso hasta que llega el
primer mensaje real de Kafka — exactamente la misma clase de bug ya
documentada como **Lección 10** en `CLAUDE-RESTART.md`. Verificar con
un `grep` explícito sobre `.yml`/`.properties`, no solo `.java` (paso 6
del orden de ejecución, abajo) — no confiar en que el refactor del IDE
lo haya cubierto.

### 4. Otros archivos con referencias al nombre viejo

- `.gitignore`: línea `/.idea/AppBaseVaadin.iml` — actualizar al nuevo
  nombre del `.iml` que genere IntelliJ tras el rename.
- `claude.md`, `README.md`, `index.html`: menciones narrativas/doc —
  actualizar por consistencia (no bloquean nada en ejecución, pero
  quedan obsoletas si no se tocan).
- `.idea/modules.xml`, `.idea/workspace.xml`: generados por el IDE, se
  regeneran solos al reabrir el proyecto tras el rename — no editar a
  mano.
- `app-vaadin-run.log`, `ms-*-run.log`, `app-vaadin-dummy-run.log`:
  logs sueltos de corridas manuales pasadas, no forman parte de la
  app — ignorar, no son parte del rename.

### 5. Repositorio de GitHub

- Renombrar `davideochoa/AppBaseVaadin` → `davideochoa/VaadinBaseApp`
  (UI de GitHub o `gh repo rename VaadinBaseApp`). GitHub crea un
  redirect automático desde la URL vieja, pero:
  - Actualizar el remoto local después:
    `git remote set-url origin https://github.com/davideochoa/VaadinBaseApp.git`
  - Cualquier link externo (badges, docs fuera del repo) que apunte a
    la URL vieja debe actualizarse a mano — el redirect cubre
    operaciones git y tráfico web, pero no es garantía permanente ni
    arregla texto desactualizado.
- Revisado `.github/workflows/*.yml`: los `paths:` filtran por nombre
  de **directorio de módulo** (`app-vaadin/**`, `ms-users/**`, etc.),
  no por nombre de proyecto — el rename del repo no rompe CI.

### 6. Entorno local

- Renombrar la carpeta local `C:\Proyectos\AppBaseVaadin` →
  `C:\Proyectos\VaadinBaseApp` — hacerlo **al final**, después del
  rename en GitHub y un push final, para no perder referencia de
  directorio a mitad de una operación.
- Reabrir en IntelliJ después del rename de carpeta para que
  `.idea/*` se regenere limpio.

### 7. Ramas de git

Auditadas todas las locales: `main`, `feature/run-ms`,
`feature/app-vaadin-fase4`, `feature/integration-fase5`,
`feature/ms-audit-Kafka-fase3`, `feature/ms-security_fase2`,
`feature/ms-usuarios_fase1`, `feature/security_issues`,
`feature/test-standalone`, `testUI` — **ninguna contiene literalmente
"AppBaseVaadin"**, no hay nada que renombrar a nivel de nombre de rama
hoy. Por el alcance acordado, el/los commits del rename se aplican
solo sobre `main` + la rama activa en el momento de ejecutar — las 7
ramas históricas ya mergeadas quedan intactas (registro histórico, no
código vivo). No tocar las ramas `dependabot/...` (las gestiona el bot
automáticamente y se realinean solas una vez que `main` refleje el
nuevo `groupId`).

## Orden de ejecución sugerido (cuando se aplique)

1. Terminar y commitear la feature de username/reset-password en
   `feature/run-ms`, mergear a `main`.
2. Nueva rama desde `main`, p. ej. `rename/vaadinbaseapp`.
3. Refactor "rename package" del IDE (IntelliJ: clic derecho sobre
   `com.appbasevaadin` → Refactor → Rename) por cada uno de los 4
   módulos reales + `app-vaadin-dummy`. Esto mueve archivos/carpetas y
   corrige imports automáticamente — no hacerlo a mano archivo por
   archivo.
4. Actualizar a mano los 5 `groupId` en los `pom.xml` (el refactor del
   IDE no los toca).
5. Corregir la trampa de Kafka en `ms-audit/application.yml` (sección 3).
6. `grep -rn "com.appbasevaadin\|appbasevaadin" --include="*.java" --include="*.yml" --include="*.properties" .`
   sobre todo el repo — debe dar cero resultados antes de seguir. Este
   grep es el gate real, no "el IDE no se quejó".
7. `mvn clean compile` en los 4 módulos reales (con `JAVA_HOME` a JDK
   21 explícito, por la peculiaridad ya conocida de esta máquina) —
   debe quedar limpio.
8. `mvn clean test -Dtest='!*IT'` en los 4 — deben pasar, con especial
   atención a los tests del listener de Kafka en `ms-audit` (son los
   que más fácilmente pasarían en falso con un nombre de clase
   desactualizado si están mockeados; verificar además con un mensaje
   real de Kafka de punta a punta, no solo con los tests unitarios).
9. Actualizar menciones en `README.md`, `claude.md`, `.gitignore`.
10. Push de la rama, abrir PR, mergear a `main`.
11. Renombrar el repo de GitHub (sección 5) — **después** de mergear
    el PR de código, no antes, para que el PR se abra contra el repo
    todavía con el nombre correcto en ese momento.
12. Actualizar la URL del remoto local + renombrar la carpeta local
    (sección 6), al final.

## Plan de ejecución por etapas (de menor a mayor impacto)

Reordena el "Orden de ejecución sugerido" de arriba en etapas
independientes y commiteables por separado (no un único commit
gigante), para poder compilar/testear/arrancar la app después de cada
una en vez de validar todo el rename de una sola vez. Cada etapa
asume que la anterior ya quedó en verde. Si algo falla en una etapa
media, las etapas previas ya están confirmadas y no hay que
re-auditar todo el diff para encontrar el problema.

### Etapa 0 — Rama de trabajo (impacto: ninguno)

- `git checkout main` (ya debería estar actualizado — ver
  precondición arriba) → `git checkout -b rename/vaadinbaseapp`.
- Test: `git status` limpio, nada tocado todavía.

### Etapa 1 — Documentación pura (impacto: cosmético, cero riesgo funcional)

- `README.md`, `claude.md`, y el comentario en `.gitignore`
  (`/.idea/AppBaseVaadin.iml`) — nada de código ni `pom.xml`.
- Test: `mvn clean compile` en los 4 módulos reales debe seguir en
  verde exactamente igual que antes de tocar nada (confirma que no se
  rozó código por accidente al editar los `.md`).
- Commit: p. ej. `docs: update project name references`.

### Etapa 2 — `app-vaadin-dummy` (impacto: mínimo, módulo aislado)

Por qué va primero entre los módulos de código: no tiene workflow de
CI propio (no aparece en `.github/workflows/`), no lo menciona
`README.md`/`claude.md`, y ningún otro módulo depende de él — es el
candidato de menor blast radius para ensayar el refactor completo
(IDE "rename package" + `groupId` a mano en `pom.xml`) antes de tocar
código que sí importa en producción.

- Refactor de paquete `com.appbasevaadin` → `com.vaadinbaseapp` +
  `groupId` en `app-vaadin-dummy/pom.xml`.
- Test: `grep -rn "com.appbasevaadin\|appbasevaadin" app-vaadin-dummy/`
  → cero resultados. `mvn -f app-vaadin-dummy/pom.xml clean compile`.
- Commit separado.

### Etapa 3 — `ms-users` (impacto: bajo-medio, backend standalone)

Menos acoplado de los tres microservicios reales: nadie lo llama por
REST salvo `ms-security` (y solo para el auto-provisioning de
Google). Tiene CI propio (`ms-users-ci.yml`) como red de seguridad
adicional.

- Refactor de paquete + `groupId`.
- Test: grep scoped a `ms-users/` en cero, `mvn -f ms-users/pom.xml
  clean compile`, `mvn -f ms-users/pom.xml clean test -Dtest='!*IT'`.
  Si es viable en la máquina: `mvn -f ms-users/pom.xml spring-boot:run`
  contra un Postgres descartable (workaround de la Lección 6) +
  `curl` de humo a `/actuator/health` y `GET /user-types`.
- Commit separado.

### Etapa 4 — `ms-audit` (impacto: medio — contiene la trampa conocida de Kafka)

- Refactor de paquete + `groupId`, **más** la corrección manual de los
  dos strings literales en
  `ms-audit/src/main/resources/application.yml` (sección 3 arriba) —
  el refactor automático del IDE no los toca.
- Test: repetir el grep pero incluyendo `--include="*.yml"` esta vez
  (no solo `.java`) → cero resultados. `mvn -f ms-audit/pom.xml clean
  compile` + `test -Dtest='!*IT'`. Si es viable, verificación real de
  punta a punta con un broker Kafka descartable (Lección 6/Fase 3):
  publicar un evento real y confirmar que el listener lo deserializa
  — un test mockeado pasaría en falso con el nombre de clase viejo
  todavía en el YAML, que es justo la advertencia de la sección 3.
- Commit separado.

### Etapa 5 — `ms-security` (impacto: medio-alto — emisor de JWT/JWKS, acoplado a `ms-users`)

- Refactor de paquete + `groupId`.
- Test: grep en cero, compile + test unitario. Verificación manual
  clave: levantar `ms-users` (ya renombrado en la Etapa 3) +
  `ms-security` juntos y confirmar que `UsersClient` sigue resolviendo
  el auto-provisioning de Google y que
  `GET /.well-known/jwks.json` sigue siendo consumible — primer punto
  donde dos módulos ya renombrados interactúan en tiempo de ejecución.
- Commit separado.

### Etapa 6 — `app-vaadin` (impacto: alto — depende de los 3 backends a la vez)

- Refactor de paquete + `groupId`.
- Test: grep en cero, `mvn clean verify` offline (el mismo comando que
  corre su CI). Luego el golden path manual ya usado en la Fase 4
  (login, CRUD, RBAC, cambio EN/ES) contra los tres backends ya
  renombrados — primera vez que se ejercita el stack completo
  post-rename.
- Commit separado.

### Etapa 7 — Verificación integrada completa (impacto: alto, pero de solo lectura/verificación)

- `docker compose build && docker compose up` con los 4 módulos ya
  renombrados — repite el smoke test de la Fase 5 (login admin →
  token → `GET /users` autorizado) sobre las imágenes Docker
  completas, no solo `mvn spring-boot:run` en local.
- Gate final de todas las etapas de código:
  `grep -rn "com.appbasevaadin\|appbasevaadin" --include="*.java" --include="*.yml" --include="*.properties" .`
  sobre **todo** el repo → cero resultados.

### Etapa 8 — Push, PR y merge (impacto: alto — visible para colaboradores/CI)

- Push de `rename/vaadinbaseapp`, abrir PR, esperar los 5 workflows en
  verde (incluyendo `integration-ci.yml`), mergear a `main`.

### Etapa 9 — Rename del repositorio de GitHub (impacto: alto, external-facing)

- **Después** de mergear el PR de código, no antes — para que el PR
  se haya abierto contra el nombre de repo todavía correcto en su
  momento. `gh repo rename VaadinBaseApp` (o vía UI).
- Test: `git ls-remote https://github.com/davideochoa/VaadinBaseApp.git`
  responde antes de tocar nada localmente.

### Etapa 10 — Entorno local (impacto: alto solo para esta máquina, no para el código)

- `git remote set-url origin https://github.com/davideochoa/VaadinBaseApp.git`.
- Renombrar la carpeta local `C:\Proyectos\AppBaseVaadin` →
  `C:\Proyectos\VaadinBaseApp` — al final, como ya indicaba la sección
  6 original.
- Reabrir en IntelliJ para que `.idea/*` se regenere limpio.

## Explícitamente fuera de alcance (según lo acordado)

- Renombrar las ramas auto-generadas de Dependabot.
- Reescribir las 7 ramas históricas ya mergeadas.
- Hacer cualquier parte de esto antes de commitear el trabajo actual.
