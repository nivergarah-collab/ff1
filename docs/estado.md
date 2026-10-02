# Estado · ff1

## Control
- Estado del MVP: EN CURSO
- Bloques ejecutados: 0 de 24
- Bloques seguidos sin avance: 0

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
- Copia de las skills maestras en `skills/maestras/`, fusionada en `main` (pull request #2).
- Tarea programada creada: una sesión cada 3 horas, con notificación al celular.
- Definición de hecho, límites de parada y diseño adaptable en `main` (pull request #3).
- APK descargable y flujos `Pruebas` y `APK` en `main` (pull requests #4 y #5); el APK #1 se construyó bien.
- Autorización para que el agente abra pull requests, en la rama `feature/autorizar-pull-requests`.

## Siguiente
1. **Usuario:** fusionar la rama `feature/autorizar-pull-requests` (autorización para que el agente abra pull requests).
2. Primer bloque del agente: hito H0 de `docs/plan.md`.

La ruta completa está en `docs/plan.md`.

## Ramas y pull requests pendientes
Ninguna. (Orden de fusión: de la más antigua a la más nueva, con "Merge commit".)

## Preguntas pendientes
Ninguna.

## Pruebas
GitHub Actions en verde (ejecuciones #1 y #3). Sin ejecutar localmente.

## Notas
- `ExampleUnitTest` y `ExampleInstrumentedTest` son de la plantilla y se reemplazarán.
- `.github/` es una ruta protegida: el agente no puede escribir en ella, los cambios al flujo los hace el usuario.
- Las skills maestras originales están en `Android/skills/` del computador del usuario; la copia de `skills/maestras/` es la que leen los agentes en la nube.
