# Estado · ff1

Última actualización: 2026-10-01

## Hecho
- Proyecto movido a `Desktop\Android\ff1`.
- Tecnología confirmada: plantilla nativa (Java + C++).
- Estructura estándar creada.
- Flujo de integración continua (`.github/workflows/pruebas.yml`) colocado por el usuario.
- Repositorio creado en GitHub: https://github.com/nivergarah-collab/ff1
- Commit inicial subido a `main` (`5097248`, 66 archivos). `local.properties` no se versionó.
- Misión del MVP, plan por hitos y skill de trabajo autónomo en `main` (pull request #1).
- GitHub Actions en verde en las ejecuciones #1 y #3.
- Copia de las skills maestras en `skills/maestras/` (rama `feature/skills-maestras`, pendiente de fusionar).

## Siguiente
1. **Usuario:** fusionar en GitHub la rama `feature/skills-maestras` y hacer `git pull` en `Desktop\Android\ff1`.
2. Crear la tarea programada de sesiones de trabajo (cada 3 horas, con notificación al celular).
3. Primer bloque del agente: hito H0 de `docs/plan.md`.

La ruta completa está en `docs/plan.md`.

## Preguntas pendientes
Ninguna.

## Pruebas
GitHub Actions en verde (ejecuciones #1 y #3). Sin ejecutar localmente.

## Notas
- `ExampleUnitTest` y `ExampleInstrumentedTest` son de la plantilla y se reemplazarán.
- `.github/` es una ruta protegida: el agente no puede escribir en ella, los cambios al flujo los hace el usuario.
- Las skills maestras originales están en `Android/skills/` del computador del usuario; la copia de `skills/maestras/` es la que leen los agentes en la nube.
