# Estado · ff1

## Control
- Estado del MVP: EN CURSO
- Bloques ejecutados: 1 de 24
- Bloques seguidos sin avance: 0

Última actualización: 2026-10-02 (bloque 1)

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
- Bloque 1 (H0, rama `feature/h0-preparacion`): entorno verificado (javac 21 con `--release 11`, JUnit 4.13.2 y Hamcrest 1.3 en `/opt/gradle-8.14.3/lib`); `scripts/probar-logica.sh` creado y probado; resumen de ritmo en `docs/investigacion-ritmo.md`.

## Siguiente
1. **Usuario:** fusionar en orden, con "Merge commit", las ramas de "Ramas y pull requests pendientes".
2. Bloque 2 del agente: terminar H0 (decidir cómo se dibujan menús y texto; interfaces del motor, formato de datos y `docs/contrato-de-datos.md`). Al terminar H0, abrir el pull request de `feature/h0-preparacion`.

La ruta completa está en `docs/plan.md`.

## Ramas y pull requests pendientes
Orden de fusión: de la más antigua a la más nueva, con "Merge commit".
1. `feature/autorizar-pull-requests` (sin pull request; la abrió el usuario).
2. `feature/h0-preparacion` (parte de la anterior; H0 en curso, pull request al terminar el hito).

## Preguntas pendientes
Ninguna.

## Pruebas
Nube, 2026-10-02: `scripts/probar-logica.sh` 1 de 1 pruebas pasan. GitHub Actions en `feature/h0-preparacion` (commit `efeee36`): `Pruebas` #14 en verde y `APK` #4 en verde.

## Notas
- `ExampleUnitTest` y `ExampleInstrumentedTest` son de la plantilla y se reemplazarán.
- `.github/` es una ruta protegida: el agente no puede escribir en ella, los cambios al flujo los hace el usuario.
- Las skills maestras originales están en `Android/skills/` del computador del usuario; la copia de `skills/maestras/` es la que leen los agentes en la nube.
