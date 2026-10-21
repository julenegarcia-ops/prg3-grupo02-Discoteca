#!/bin/sh
# Compila y abre la app de asistencia (macOS / Linux, necesita Java 17 o superior).
cd "$(dirname "$0")" || exit 1
mkdir -p out
javac -encoding UTF-8 -d out src/asistencia/*.java || exit 1
nohup java -cp out asistencia.App >/dev/null 2>&1 &
