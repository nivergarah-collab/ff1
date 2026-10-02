#!/usr/bin/env bash
# Compila la lógica en Java puro (sin Android) y sus pruebas JUnit 4, y las ejecuta.
# Imprime una línea por clase de prueba y el detalle solo de los fallos.
#
# Uso, desde la raíz del proyecto:
#   scripts/probar-logica.sh                 # suite de lógica completa
#   scripts/probar-logica.sh combate         # solo las pruebas cuyo nombre contiene "combate"
#
# JUnit 4 y Hamcrest: se buscan en JUNIT_JAR y HAMCREST_JAR, en /opt/gradle*/lib
# o en la caché de Gradle (~/.gradle). Código de salida 1 si algo falla.
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
FILTRO="${1:-}"
MAIN="$RAIZ/app/src/main/java"
TEST="$RAIZ/app/src/test/java"
SALIDA="${TMPDIR:-/tmp}/ff1-logica"

buscar_jar() { # $1 = patrón de nombre
  find /opt/gradle*/lib "$HOME/.gradle" -name "$1" 2>/dev/null | sort | tail -n 1
}
JUNIT="${JUNIT_JAR:-$(buscar_jar 'junit-4.*.jar')}"
HAMCREST="${HAMCREST_JAR:-$(buscar_jar 'hamcrest-core-*.jar')}"
if [[ -z "$JUNIT" || -z "$HAMCREST" ]]; then
  echo "FALLA: no se encontró JUnit 4 o Hamcrest (definir JUNIT_JAR y HAMCREST_JAR)"
  exit 1
fi

# Solo archivos en Java puro: se excluyen los que importan Android.
puros() { # $1 = carpeta
  [[ -d "$1" ]] || return 0
  find "$1" -name '*.java' -print0 | xargs -0 -r grep -L -E '^import (android|androidx|com\.google)\.' || true
}
mapfile -t FUENTES < <(puros "$MAIN")
mapfile -t PRUEBAS < <(puros "$TEST")

rm -rf "$SALIDA" && mkdir -p "$SALIDA/clases"
CP="$JUNIT:$HAMCREST:$SALIDA/clases"
if ! javac --release 11 -encoding UTF-8 -nowarn -d "$SALIDA/clases" -cp "$CP" \
    "${FUENTES[@]}" "${PRUEBAS[@]}" "$RAIZ/scripts/EjecutorLogica.java" 2> "$SALIDA/javac.txt"; then
  echo "FALLA: compilación"
  grep -E 'error:' "$SALIDA/javac.txt" | head -n 20
  exit 1
fi

# Clases de prueba: archivos de app/src/test cuyo nombre termina en Test.
CLASES=()
for f in "${PRUEBAS[@]}"; do
  [[ "$f" == *Test.java ]] || continue
  clase="${f#"$TEST/"}"; clase="${clase%.java}"; clase="${clase//\//.}"
  if [[ -z "$FILTRO" || "${clase,,}" == *"${FILTRO,,}"* ]]; then
    CLASES+=("$clase")
  fi
done
if [[ ${#CLASES[@]} -eq 0 ]]; then
  echo "FALLA: ninguna clase de prueba coincide con '${FILTRO}'"
  exit 1
fi

java -Dstdout.encoding=UTF-8 -cp "$CP" EjecutorLogica "${CLASES[@]}"
