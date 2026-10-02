# Changelog · ff1

Formato: más reciente primero, con fecha en `AAAA-MM-DD`.

## 2026-10-01

### Añadido
- Estructura estándar del proyecto según las skills maestras: `README.md`, `CHANGELOG.md`, `skills/00-iniciar.md`, `docs/estado.md`, `docs/decisiones.md`, `docs/pruebas.md`.
- `scripts/verificar-estructura.ps1`: comprueba que la estructura estándar exista.
- `.github/workflows/pruebas.yml`: integración continua con la verificación de estructura y las pruebas unitarias. Lo colocó el usuario, porque esa ruta está protegida para el agente. Sin verificar en GitHub todavía.

### Decisiones
- Se mantiene la plantilla nativa de Android Studio (Java + C++).
- ff1 es una versión mini: misma mecánica y estilo del género, sin buscar la duración de un juego completo.
- El combate usa barra de tiempo (ATB), simulada por ticks para que las pruebas sean deterministas.
- El molde de proyectos se hará después, fuera de este repositorio.
