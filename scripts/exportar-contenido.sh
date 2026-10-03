#!/usr/bin/env bash
# Exporta un paquete de contenido a una carpeta nueva, lista para una variante del juego.
# Primero lo valida con el motor real; si hay errores, no copia nada.
#
# Uso, desde la raíz del proyecto:
#   scripts/exportar-contenido.sh <carpeta-origen> <carpeta-destino>
# El destino debe no existir o estar vacío (no se pisa ni se borra nada).
# Para usar el paquete en el juego, copia su contenido a app/src/main/assets/contenido/ y compila el APK.
#
# Código de salida: 0 exportado, 1 contenido inválido o destino ocupado, 2 uso incorrecto.
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Uso: scripts/exportar-contenido.sh <carpeta-origen> <carpeta-destino>" >&2
  exit 2
fi
ORIGEN="$1"
DESTINO="$2"
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"

[[ -d "$ORIGEN" ]] || { echo "FALLA: no existe la carpeta de origen: $ORIGEN" >&2; exit 2; }
if [[ -e "$DESTINO" ]] && { [[ ! -d "$DESTINO" ]] || [[ -n "$(ls -A "$DESTINO")" ]]; }; then
  echo "FALLA: el destino existe y no está vacío: $DESTINO" >&2
  exit 1
fi

if ! "$RAIZ/scripts/validar-contenido.sh" "$ORIGEN"; then
  echo "FALLA: el contenido no es válido; no se exportó nada." >&2
  exit 1
fi

mkdir -p "$DESTINO"
cp -R "$ORIGEN"/. "$DESTINO"/
find "$DESTINO" \( -name '*.bak' -o -name 'informe.json' \) -type f -print0 | xargs -0 -r rm -f
echo "EXPORTADO: $(find "$DESTINO" -name '*.json' | wc -l) documentos en $DESTINO"
