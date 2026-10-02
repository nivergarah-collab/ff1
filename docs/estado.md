# Estado · ff1

## Control
- Estado del MVP: EN CURSO
- Bloques ejecutados: 4 de 50
- Bloques seguidos sin avance: 0

Última actualización: 2026-10-02 (bloque 4)

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
- Bloque 4 (H1 y H2 terminados): acciones y cálculo de daño con registros de tipos de habilidad y estados (`Acciones`, `ReglasCombate`), fin del combate y recompensas (`Combate`, `Recompensa`), avance rápido por pasos (`AvanceRapido`) y todos los parámetros del combate en la configuración (rama `feature/h1-combate`, pull request #7). En `feature/h2-progresion`: experiencia y niveles (`progresion.TablaProgresion`, `Heroe`, `Reparto`), objetos, inventario y equipo (`inventario.CatalogoObjetos`, `Inventario`, `Equipo`) y botín (`TablaBotin`), con contenido en `progresion.json`, `objetos.json` y `botin.json`.
- Autorizado por el usuario el 2026-10-02: límite de 50 bloques (antes 24), quitar el código nativo de la plantilla (ver `skills/01-trabajo-autonomo.md`) y bloques de hasta unos 25 minutos, una sesión por hora.

## Siguiente
1. **Usuario:** fusionar con "Merge commit", en orden, el pull request #7 (H1) y luego el de H2 (`feature/h2-progresion`).
2. Bloque 5 del agente: bloque de refactorización y limpieza tras dos hitos (H1 y H2), según `skills/01-trabajo-autonomo.md`. Candidatos: helper común de pruebas para cargar el paquete de contenido (hoy repetido en `ProgresionTest`, `InventarioTest`, `BotinTest` y `combate.Datos`), validación de rangos e ids repetida en los catálogos (`CatalogoCombate`, `CatalogoObjetos`, `TablaBotin`, `TablaProgresion`) y lectura de `efecto`/`estado` duplicada entre habilidades y objetos. Rama `chore/limpieza-h1-h2` desde `feature/h2-progresion`.
3. Después: H3 (mapa por casillas y encuentros aleatorios). Al marcar la última tarea de H3 toca escribir `docs/receta-de-extension.md` y pausar para el cambio de modelo.

La ruta completa está en `docs/plan.md`.

## Ramas y pull requests pendientes
Orden de fusión: de la más antigua a la más nueva, con "Merge commit".
1. `feature/h1-combate` (parte de `main`): H1 terminado, pull request #7 https://github.com/nivergarah-collab/ff1/pull/7
2. `feature/h2-progresion` (parte de `feature/h1-combate`): H2 terminado, pull request abierto en este bloque (ver lista de pull requests).

## Preguntas pendientes
Ninguna.

## Pruebas
Nube, 2026-10-02 (bloque 4): `scripts/probar-logica.sh` 129 de 129 pruebas pasan en `feature/h2-progresion` (107 en `feature/h1-combate`). GitHub Actions en `feature/h1-combate` (`e767abe`): `Pruebas` #27 y `APK` #15 en verde. En `feature/h2-progresion`: ver el pull request de H2.

## Notas
- `ExampleUnitTest` y `ExampleInstrumentedTest` son de la plantilla y se reemplazarán.
- `.github/` es una ruta protegida: el agente no puede escribir en ella, los cambios al flujo los hace el usuario.
- Las skills maestras originales están en `Android/skills/` del computador del usuario; la copia de `skills/maestras/` es la que leen los agentes en la nube.
