# Estado · ff1

Última actualización: 2026-10-01

## Hecho
- Proyecto movido a `Desktop\Android\ff1`.
- Tecnología confirmada: plantilla nativa (Java + C++).
- Estructura estándar creada.
- Flujo de integración continua (`.github/workflows/pruebas.yml`) colocado por el usuario.
- Repositorio creado en GitHub: https://github.com/nivergarah-collab/ff1
- Commit inicial subido a `main` (`5097248`, 66 archivos). `local.properties` no se versionó.
- Escritos la misión del MVP (`docs/mision-mvp.md`), el plan por hitos (`docs/plan.md`) y la skill de trabajo autónomo (`skills/01-trabajo-autonomo.md`). Aún no están en el repositorio.

## Siguiente
1. **Usuario:** dar permiso de escritura a Claude en el repositorio (instalar la app de Claude en GitHub o reconectar GitHub en claude.ai). Sin esto el agente en la nube no puede subir ramas.
2. **Usuario:** subir a `main` los documentos nuevos (rama `docs/mision-mvp`, commit, push y fusión en GitHub).
3. Crear la tarea programada de sesiones de trabajo.
4. Primer bloque del agente: hito H0 de `docs/plan.md`.

La ruta completa está en `docs/plan.md`.

## Preguntas pendientes
Ninguna.

## Pruebas
Sin ejecutar localmente. La primera ejecución en GitHub Actions estaba en curso en la última revisión.

## Notas
- `ExampleUnitTest` y `ExampleInstrumentedTest` son de la plantilla y se reemplazarán.
- Riesgo: la integración continua puede necesitar el NDK y CMake para compilar; por verificar.
- `.github/` es una ruta protegida: el agente no puede escribir en ella, los cambios al flujo los hace el usuario.
