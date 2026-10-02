# Estado · ff1

## Control
- Estado del MVP: PAUSA: cambio de modelo (pasar a Sonnet para H4 a H7)
- Bloques ejecutados: 5 de 50
- Bloques seguidos sin avance: 0

Última actualización: 2026-10-02 (bloque 5)

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
- Pull requests #7 (H1) y #8 (H2) fusionados en `main` por el usuario.
- Bloque 5 (H3 terminado, rama `feature/h3-mundo` desde `main`): mapa por casillas con leyenda, colisiones e inicio validado (`mundo.Mapa`, `Direccion`, `Explorador`, primer mapa `mapas/campo.json`); encuentros aleatorios por zona con cuenta atrás de pasos tomada de la configuración (`mundo.TablaEncuentros`, `Encuentros`, `ConfiguracionMundo`, `encuentros.json`) que crean los enemigos de un `Combate`; `docs/receta-de-extension.md` escrita y `docs/contrato-de-datos.md` al día (`mapa` y `encuentros` v1). Pausa para el cambio de modelo.
- Autorizado por el usuario el 2026-10-02: límite de 50 bloques (antes 24), quitar el código nativo de la plantilla (ver `skills/01-trabajo-autonomo.md`) y bloques de hasta unos 25 minutos, una sesión por hora.

## Siguiente
1. **Usuario:** revisar y fusionar con "Merge commit" el pull request de H3 (`feature/h3-mundo`, ver abajo). Luego cambiar el modelo de la tarea programada a Sonnet y poner el estado de Control en `EN CURSO`.
2. Bloque 6 (Sonnet): el bloque de refactorización y limpieza tras H1–H3 que quedó pendiente (ver "Deuda técnica" de `docs/plan.md`), en `chore/limpieza-h1-h3`, partiendo de `feature/h3-mundo` si aún no está fusionada o de `main`.
3. Después: H4 (historia original en `docs/historia.md` y motor de escenas de texto). Seguir `docs/receta-de-extension.md` para añadir contenido; al hacer las escenas, reemplazar su sección 6 por un ejemplo real.
4. Al marcar la última tarea de H7: pull request y pausa para volver a Opus (H8).

La ruta completa está en `docs/plan.md`.

## Ramas y pull requests pendientes
Orden de fusión: de la más antigua a la más nueva, con "Merge commit".
1. `feature/h3-mundo` (parte de `main`): H3 terminado, pull request #9 https://github.com/nivergarah-collab/ff1/pull/9

## Preguntas pendientes
Ninguna.

## Pruebas
Nube, 2026-10-02 (bloque 5): `scripts/probar-logica.sh` 144 de 144 pruebas pasan en `feature/h3-mundo`. GitHub Actions en `feature/h3-mundo`: `Pruebas` #39 y `APK` #24 en verde en `89e0cde` (y `Pruebas` #40 del pull request).

## Notas
- `ExampleUnitTest` y `ExampleInstrumentedTest` son de la plantilla y se reemplazarán.
- `.github/` es una ruta protegida: el agente no puede escribir en ella, los cambios al flujo los hace el usuario.
- Las skills maestras originales están en `Android/skills/` del computador del usuario; la copia de `skills/maestras/` es la que leen los agentes en la nube.
