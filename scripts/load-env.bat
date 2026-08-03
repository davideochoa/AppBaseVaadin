@echo off
REM Loads repo-root .env (if present) into the calling script's environment,
REM then fills in the same defaults docker-compose.yml/.env.example fall
REM back to for anything still unset. Keeps start-infra.bat/start-ms.bat in
REM sync with Option 1 (docker compose) without duplicating values by hand.
REM A var already set in the calling shell before this runs wins over
REM .env, same precedence spirit as docker-compose's own ${VAR:-default}.
REM Call with "call" (not a fresh cmd) so these "set"s persist in the
REM caller: call "%~dp0load-env.bat"

set "ENV_FILE=%~dp0..\.env"
if exist "%ENV_FILE%" (
    for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%ENV_FILE%") do (
        if not "%%A"=="" if not defined %%A set "%%A=%%B"
    )
)

if not defined DB_USERS_NAME set "DB_USERS_NAME=ms_users"
if not defined DB_USERS_USER set "DB_USERS_USER=ms_users_app"
if not defined DB_USERS_PASSWORD set "DB_USERS_PASSWORD=ms_users_pass"

if not defined DB_SECURITY_NAME set "DB_SECURITY_NAME=ms_security"
if not defined DB_SECURITY_USER set "DB_SECURITY_USER=ms_security_app"
if not defined DB_SECURITY_PASSWORD set "DB_SECURITY_PASSWORD=ms_security_pass"

if not defined DB_AUDIT_NAME set "DB_AUDIT_NAME=ms_audit"
if not defined DB_AUDIT_USER set "DB_AUDIT_USER=ms_audit_app"
if not defined DB_AUDIT_PASSWORD set "DB_AUDIT_PASSWORD=ms_audit_pass"

if not defined APP_JWT_PRIVATE_KEY_PEM set "APP_JWT_PRIVATE_KEY_PEM="
if not defined APP_JWT_PUBLIC_KEY_PEM set "APP_JWT_PUBLIC_KEY_PEM="
if not defined ACCESS_TOKEN_TTL_MINUTES set "ACCESS_TOKEN_TTL_MINUTES=10"
if not defined REFRESH_TOKEN_TTL_DAYS set "REFRESH_TOKEN_TTL_DAYS=7"
if not defined BOOTSTRAP_ADMIN_PASSWORD set "BOOTSTRAP_ADMIN_PASSWORD="

if not defined GOOGLE_CLIENT_ID set "GOOGLE_CLIENT_ID=591353095798-0plff4easbmce6o931sbl55hnicc3g7t.apps.googleusercontent.com"
if not defined AUDIT_TOPIC set "AUDIT_TOPIC=audit"
if not defined APP_VAADIN_ORIGIN set "APP_VAADIN_ORIGIN=http://localhost:8080"

goto :eof
