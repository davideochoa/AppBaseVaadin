@echo off
REM Levanta Docker Desktop (si hace falta) y los contenedores sueltos de
REM Postgres (uno por microservicio) + Kafka, para poder correr los MS
REM nativos con "mvn spring-boot:run" apuntando a estas BDs.
REM Ver CLAUDE-RESTART.md, Leccion 6.

setlocal enabledelayedexpansion

call "%~dp0load-env.bat"

echo Comprobando Docker Desktop...
docker info >nul 2>&1
if errorlevel 1 (
    echo Docker no responde, arrancando Docker Desktop...
    start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    :waitdocker
    timeout /t 5 >nul
    docker info >nul 2>&1
    if errorlevel 1 (
        echo   ...esperando a Docker...
        goto waitdocker
    )
    echo Docker esta arriba.
) else (
    echo Docker ya esta corriendo.
)

call :ensure_container pg-users "postgres:16-alpine" -p 5433:5432 -e POSTGRES_DB=%DB_USERS_NAME% -e POSTGRES_USER=%DB_USERS_USER% -e POSTGRES_PASSWORD=%DB_USERS_PASSWORD%
call :ensure_container pg-security "postgres:16-alpine" -p 5434:5432 -e POSTGRES_DB=%DB_SECURITY_NAME% -e POSTGRES_USER=%DB_SECURITY_USER% -e POSTGRES_PASSWORD=%DB_SECURITY_PASSWORD%
call :ensure_container pg-audit "postgres:16-alpine" -p 5435:5432 -e POSTGRES_DB=%DB_AUDIT_NAME% -e POSTGRES_USER=%DB_AUDIT_USER% -e POSTGRES_PASSWORD=%DB_AUDIT_PASSWORD%
call :ensure_container kafka "apache/kafka-native" -p 9092:9092

echo.
echo Estado de los contenedores de infraestructura:
docker ps -a --filter "name=pg-users" --filter "name=pg-security" --filter "name=pg-audit" --filter "name=kafka" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo.
echo Listo. Espera unos segundos a que Postgres/Kafka terminen de arrancar
echo antes de lanzar los microservicios (start-ms.bat).
endlocal
goto :eof

:ensure_container
set "NAME=%~1"
set "IMAGE=%~2"
shift
shift
set "OPTS="
:collect_opts
if "%~1"=="" goto run_ensure
set "OPTS=%OPTS% %1"
shift
goto collect_opts

:run_ensure
docker inspect %NAME% >nul 2>&1
if errorlevel 1 (
    echo Creando contenedor %NAME%...
    docker run -d --name %NAME% %OPTS% %IMAGE%
) else (
    for /f "tokens=*" %%s in ('docker inspect -f "{{.State.Running}}" %NAME%') do set "RUNNING=%%s"
    if /I "!RUNNING!"=="true" (
        echo %NAME% ya estaba corriendo.
    ) else (
        echo Arrancando contenedor existente %NAME%...
        docker start %NAME%
    )
)
goto :eof
pause
