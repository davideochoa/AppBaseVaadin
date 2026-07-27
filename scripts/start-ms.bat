@echo off
REM Arranca los 3 microservicios nativos (mvn spring-boot:run), cada uno
REM en su propia ventana, apuntando a las BDs/Kafka levantados por
REM start-infra.bat. Fuerza JDK 21 explicitamente (ver CLAUDE-RESTART.md,
REM nota de Phase 4: el JAVA_HOME por defecto de esta maquina es JDK 25).

set "JDK21=C:\Program Files\Java\jdk-21"
set "REPO=%~dp0.."

echo Arrancando ms-users (puerto 8081)...
start "ms-users" cmd /k ^
  "cd /d "%REPO%\ms-users" && set "JAVA_HOME=%JDK21%" && set "PATH=%JDK21%\bin;%PATH%" && set "DB_USERS_HOST=localhost" && set "DB_USERS_PORT=5433" && set "SERVER_PORT=8081" && mvn spring-boot:run"

timeout /t 5 >nul

echo Arrancando ms-security (puerto 8082)...
start "ms-security" cmd /k ^
  "cd /d "%REPO%\ms-security" && set "JAVA_HOME=%JDK21%" && set "PATH=%JDK21%\bin;%PATH%" && set "DB_SECURITY_HOST=localhost" && set "DB_SECURITY_PORT=5434" && set "SERVER_PORT=8082" && set "MS_USERS_BASE_URL=http://localhost:8081" && set "KAFKA_BOOTSTRAP_SERVERS=localhost:9092" && mvn spring-boot:run"

timeout /t 5 >nul

echo Arrancando ms-audit (puerto 8083)...
start "ms-audit" cmd /k ^
  "cd /d "%REPO%\ms-audit" && set "JAVA_HOME=%JDK21%" && set "PATH=%JDK21%\bin;%PATH%" && set "DB_AUDIT_HOST=localhost" && set "DB_AUDIT_PORT=5435" && set "SERVER_PORT=8083" && set "KAFKA_BOOTSTRAP_SERVERS=localhost:9092" && mvn spring-boot:run"

echo.
echo Los 3 microservicios se estan lanzando cada uno en su propia ventana:
echo   ms-users    -^> http://localhost:8081/actuator/health
echo   ms-security -^> http://localhost:8082/actuator/health  (JWKS: /.well-known/jwks.json)
echo   ms-audit    -^> http://localhost:8083/actuator/health
echo.
echo Espera a ver "Started ... in N seconds" en cada ventana antes de probar.
pause
