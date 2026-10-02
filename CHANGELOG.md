# Changelog · ff1

Formato: más reciente primero, con fecha en `AAAA-MM-DD`.

## 2026-10-01

### Añadido
- Instrucciones para el APK descargable (flujo `APK`) y disparadores del flujo `Pruebas` ampliados a las ramas de trabajo. Los archivos de `.github/workflows/` los coloca el usuario.
- Requisito de diseño adaptable (motor separado del contenido, fuentes intercambiables, configuración en caliente, contrato de datos) en la misión, el plan y las reglas del proyecto.
- Definición de terminado medible, límites de parada y sección `## Control` en `docs/estado.md`, para que el agente se detenga solo.
- `docs/mision-mvp.md`, `docs/plan.md` y `skills/01-trabajo-autonomo.md`: misión, ruta por hitos y reglas para un agente que trabaja por bloques.
- `docs/conexiones.md`.
- Estructura estándar del proyecto según las skills maestras: `README.md`, `CHANGELOG.md`, `skills/00-iniciar.md`, `docs/estado.md`, `docs/decisiones.md`, `docs/pruebas.md`.
- `scripts/verificar-estructura.ps1`: comprueba que la estructura estándar exista.
- `.github/workflows/pruebas.yml`: integración continua con la verificación de estructura y las pruebas unitarias. Lo colocó el usuario, porque esa ruta está protegida para el agente. Sin verificar en GitHub todavía.

### Decisiones
- Se mantiene la plantilla nativa de Android Studio (Java + C++).
- ff1 es una versión mini: misma mecánica y estilo del género, sin buscar la duración de un juego completo.
- El combate usa barra de tiempo (ATB), simulada por ticks para que las pruebas sean deterministas.
- El molde de proyectos se hará después, fuera de este repositorio.
- El juego debe incluir un interruptor de avance rápido.
