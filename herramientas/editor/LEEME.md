# Editor de parámetros · ff1

Herramienta para el computador, separada del juego. Lee y edita la carpeta de contenido (`app/src/main/assets/contenido/` o la de otro paquete) sin tocar el código del motor. Es HTML, CSS y JavaScript sin dependencias ni paso de construcción.

## Cómo abrirlo
1. Abre `herramientas/editor/index.html` en **Chrome, Edge u otro navegador basado en Chromium** (hace falta la API de archivos para leer y guardar carpetas; Firefox y Safari no la tienen).
2. Pulsa **Abrir carpeta de contenido…** y elige `app/src/main/assets/contenido`. El navegador pide permiso de lectura y escritura sobre esa carpeta; solo la usa mientras la página está abierta.

## Qué hace (versión actual, H10)
- Lista los documentos de la carpeta (`configuracion.json`, el resto de los documentos y los mapas y escenas).
- **configuracion.json** se edita como formulario: todos los parámetros del motor agrupados por módulo, con su descripción, su rango y su valor por defecto, un botón para restablecerlos y el aviso de rango o de entero en el mismo campo. Cada rango es el del contrato (`docs/contrato-de-datos.md`); una prueba compara el esquema del editor con el del motor.
- Los demás documentos se ven en solo lectura (se podrán editar en H11).
- **Guardar** escribe `configuracion.json` con sangría de dos espacios y deja antes la versión anterior en `configuracion.json.bak`. Solo escribe los parámetros que difieren del valor por defecto o que ya estaban en el archivo. El botón se apaga si hay un valor inválido. La pestaña muestra `●` y el navegador avisa si cierras con cambios sin guardar.
- **Revisar con el motor** muestra el comando `scripts/validar-contenido.sh "<carpeta>" --json > informe.json`. El navegador no puede ejecutar Java: corre el comando en la raíz del proyecto y carga `informe.json` en el cuadro para ver el resultado documento por documento (los que tienen error se marcan en rojo en la lista).

## Probarlo con el juego
Cambia, por ejemplo, `combate.ticksPorPaso` (ritmo del combate) o `mundo.pasosMinimos` y `mundo.pasosMaximos` (pasos entre encuentros), guarda, revisa con el motor, compila el APK de depuración (o usa el flujo `APK` de GitHub) y entra a un combate.

## Pruebas
`scripts/probar-editor.sh` (necesita `node`; usa `node --test` sin dependencias). Prueban las funciones puras de `logica.js` y `almacen.js` y, si hay `javac`, que el motor acepte lo que el editor guarda. La parte Java (validador y esquema) corre en `scripts/probar-logica.sh`.

## Archivos
- `index.html`, `estilo.css`, `app.js`: la interfaz.
- `logica.js`: funciones puras (análisis, validación, construcción del documento, guardado con copia).
- `almacen.js`: lectura y escritura de la carpeta (API de archivos del navegador) y su doble de pruebas.
- `esquema-configuracion.js`: parámetros con rango, defecto y descripción.
- `pruebas/`: pruebas de Node.
