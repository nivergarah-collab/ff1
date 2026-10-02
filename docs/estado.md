# Estado · ff1

## Control
- Estado del MVP: EN CURSO
- Bloques ejecutados: 3 de 50
- Bloques seguidos sin avance: 0

Última actualización: 2026-10-02 (bloque 3)

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
- Bloque 2 (H0 terminado, rama `feature/h0-preparacion`): menús, texto y mapa se dibujarán con `Canvas` en Java a partir de una escena de dibujo en Java puro; formato de datos JSON con lector y escritor propios (`motor.datos`); interfaces del motor para contenido, azar, tiempo y guardado (`motor.fuentes`); configuración validada y reemplazable en caliente y registro de extensiones (`motor.config`); `docs/contrato-de-datos.md` escrito.
- Pull request #6 (H0) fusionado en `main` por el usuario.
- Bloque 3 (H1, rama `feature/h1-combate` desde `main`): modelo de combate en `com.example.ff1.combate` (`Combatiente`, `DefinicionCombatiente`, `Habilidad`, `Bando`, `CatalogoCombate` que lee y valida `combatientes.json` y `habilidades.json`); barra de tiempo por ticks (`BarraTiempo`) con `combate.velocidadBarra` y `combate.cargaLlena` en la configuración, reemplazables en caliente; primer paquete de contenido original en `app/src/main/assets/contenido/` (4 clases de héroe, 3 enemigos, 7 habilidades).
- Autorizado por el usuario el 2026-10-02: límite de 50 bloques (antes 24), quitar el código nativo de la plantilla (ver `skills/01-trabajo-autonomo.md`) y bloques de hasta unos 25 minutos, una sesión por hora.

## Siguiente
1. **Usuario:** nada que fusionar todavía; el pull request de H1 se abrirá al terminar el hito.
2. Bloque 4 del agente: seguir en `feature/h1-combate`. Tareas: acciones (atacar, magia, objeto, huir) y cálculo de daño, con un registro de tipos de habilidad (`danio`, `curacion`, `alteracion`) y de estados (`veneno`, `sueno`, `proteccion`) que reemplace a los de prueba; luego fin del combate y recompensas.
3. Después: avance rápido como multiplicador de ticks (declarar `combate.avanceRapido`, que ya aparece en el ejemplo de `configuracion` del contrato) y parámetros de daño en la configuración.

La ruta completa está en `docs/plan.md`.

## Ramas y pull requests pendientes
Orden de fusión: de la más antigua a la más nueva, con "Merge commit".
1. `feature/h1-combate` (parte de `main`; H1 en curso, sin pull request todavía).

## Preguntas pendientes
Ninguna.

## Pruebas
Nube, 2026-10-02 (bloque 3): `scripts/probar-logica.sh` 72 de 72 pruebas pasan. GitHub Actions en `feature/h1-combate` (commit `8f2c8e5`): `Pruebas` #23 en verde y `APK` #11 en verde.

## Notas
- `ExampleUnitTest` y `ExampleInstrumentedTest` son de la plantilla y se reemplazarán.
- `.github/` es una ruta protegida: el agente no puede escribir en ella, los cambios al flujo los hace el usuario.
- Las skills maestras originales están en `Android/skills/` del computador del usuario; la copia de `skills/maestras/` es la que leen los agentes en la nube.
