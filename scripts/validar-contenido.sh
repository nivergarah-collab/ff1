#!/usr/bin/env bash
# Valida una carpeta de contenido con los cargadores reales del motor (Java puro, sin Android).
# Imprime una línea por documento y el detalle, con la ruta del campo, solo de los errores.
#
# Uso, desde la raíz del proyecto:
#   scripts/validar-contenido.sh app/src/main/assets/contenido
#   scripts/validar-contenido.sh <carpeta> --json     # salida en JSON, para el editor
#
# Código de salida: 0 válido, 1 con errores o documentos sin comprobar, 2 uso incorrecto.
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
MAIN="$RAIZ/app/src/main/java"
SALIDA="${TMPDIR:-/tmp}/ff1-validador"

# Solo archivos en Java puro: se excluyen los que importan Android.
mapfile -t FUENTES < <(find "$MAIN" -name '*.java' -print0 | xargs -0 -r grep -L -E '^import (android|androidx|com\.google)\.' || true)

# Se recompila solo si algún fuente es más nuevo que la última compilación.
MARCA="$SALIDA/compilado"
if [[ ! -f "$MARCA" ]] || [[ -n "$(find "$MAIN" -name '*.java' -newer "$MARCA" -print -quit)" ]]; then
  rm -rf "$SALIDA" && mkdir -p "$SALIDA/clases"
  if ! javac --release 11 -encoding UTF-8 -nowarn -d "$SALIDA/clases" "${FUENTES[@]}" 2> "$SALIDA/javac.txt"; then
    echo "FALLA: compilación"
    grep -E 'error:' "$SALIDA/javac.txt" | head -n 20
    exit 1
  fi
  touch "$MARCA"
fi

# Las rutas relativas se resuelven desde la carpeta desde donde se llamó al script.
exec java -Dstdout.encoding=UTF-8 -cp "$SALIDA/clases" com.example.ff1.herramientas.ValidadorContenido "$@"
