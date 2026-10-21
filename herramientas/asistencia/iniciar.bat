@echo off
rem Compila y abre la app de asistencia (necesita Java 17 o superior).
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out srcsistencia\*.java
if errorlevel 1 (
  echo.
  echo No se pudo compilar. Comprueba que tienes el JDK instalado: javac -version
  pause
  exit /b 1
)
start "" javaw -cp out asistencia.App
