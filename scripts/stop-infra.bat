@echo off
REM Detiene (sin borrar) los contenedores de infraestructura levantados
REM por start-infra.bat. Los datos persisten en los contenedores; usa
REM start-infra.bat de nuevo para reanudarlos, o "docker rm" si quieres
REM partir de cero.

echo Deteniendo pg-users, pg-security, pg-audit, kafka...
docker stop pg-users pg-security pg-audit kafka
echo Hecho.
