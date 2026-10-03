#!/usr/bin/env bash
# Pruebas del JavaScript del editor (herramientas/editor/pruebas), sin dependencias: usa `node --test`.
# Uso, desde la raíz del proyecto: scripts/probar-editor.sh
set -euo pipefail
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
if ! command -v node >/dev/null 2>&1; then
  echo "FALLA: node no está instalado (las pruebas del editor lo necesitan)"
  exit 1
fi
cd "$RAIZ"
exec node --test --test-reporter=spec herramientas/editor/pruebas/*.test.js
