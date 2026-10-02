# Estado · ff1

## Control
- Estado del MVP: EN CURSO
- Bloques ejecutados: 7 de 50
- Bloques seguidos sin avance: 0

Última actualización: 2026-10-02 (usuario: reanudado con Sonnet, hito H3.6 añadido)

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
- Pull request #9 (H3) fusionado en `main`; el usuario cambió el modelo a Sonnet y pidió seguir (EN CURSO).
- Bloque 6 (limpieza tras H1–H3, rama `chore/limpieza-h1-h3` desde `main`): helper de pruebas `PaqueteDelJuego` (sustituye 7 copias del cargador del paquete), validación de rangos común (`motor.datos.Documentos.rango`, `rangoO`, `dentro`) y lectura de efecto/estado compartida entre habilidades y objetos (`CatalogoCombate.leerHabilidad`; los objetos ahora aceptan también `objetivo` `si-mismo`). 149 pruebas.
- Pull request #10 (limpieza) fusionado en `main` por el usuario.
- Bloque 7 (Opus, H3.5 "Primera versión jugable" terminado): escena de dibujo en Java puro y traductor a `Canvas`; controles táctiles, entrada y bucle de paso fijo; máquina de pantallas (título, exploración, combate, error); combate con menú, barras de vida y tiempo, avance rápido visible y recompensas; `MainActivity` nueva como actividad de lanzamiento; `inicio.json` y `configuracion.json`; código nativo quitado en `chore/quitar-codigo-nativo`. 175 pruebas. Pull requests #11 y #12.
- Autorizado por el usuario el 2026-10-02: límite de 50 bloques (antes 24), quitar el código nativo de la plantilla (ver `skills/01-trabajo-autonomo.md`) y bloques de hasta unos 25 minutos, una sesión por hora.

## Siguiente
1. **Sonnet, bloque 8:** hito H3.6 "Menú del grupo" (ver `docs/plan.md`), antes de H4. Rama `feature/h36-menu-del-grupo` desde `main`. Seguir `docs/receta-de-extension.md`.
2. Después: H4 (historia en `docs/historia.md` y motor de escenas de texto como `Pantalla` nueva), H5, H6 y H7.
3. Reglas para Sonnet: no cambiar de forma incompatible las interfaces del motor, `Pantalla`, `Escena` ni el contrato de datos (si hace falta, anotarlo en "Preguntas pendientes"); con 2 bloques sin avance o `Pruebas` en rojo 2 bloques seguidos, poner `PAUSA: volver a Opus (motivo)`; al marcar la última tarea de H7, abrir el pull request y poner `PAUSA: cambio de modelo (volver a Opus para H8)`.

La ruta completa está en `docs/plan.md`.

## Cómo probar el APK
1. **Bajarlo:** en GitHub, pestaña **Actions** del repositorio → ejecución **APK** de la rama (`chore/quitar-codigo-nativo`, o `main` cuando estén fusionadas) → sección **Artifacts** → **ff1-debug-apk** (un .zip con el .apk; dura 14 días). Enlace directo a las ejecuciones: https://github.com/nivergarah-collab/ff1/actions/workflows/apk.yml
2. **Instalarlo:** pasar el .apk al celular (Android 11 o superior), abrirlo y permitir "instalar apps de origen desconocido" para la app con que se abrió. Si había una versión anterior de ff1 instalada con otra firma, desinstalarla primero.
3. **Qué probar** (unos 5 minutos, en vertical):
   - Título "Crónica de la Cantera": "Continuar" aparece apagado; "Aceptar" en "Nueva partida".
   - Mapa: la cruceta mueve al grupo (cuadro amarillo); mantenerla pulsada lo hace caminar. Los riscos, la laguna y la arboleda no se pueden pisar. Por el sendero no hay encuentros; por la pradera y el matorral salta uno cada 15–30 pasos.
   - Combate: la barra amarilla de cada héroe se llena; en su turno aparece el menú. Probar Atacar (elegir objetivo con la cruceta), Magia con Tadeo o Mirta, Objeto (Tónico de raíz), Cancelar para volver y Huir. "Rápido" acelera el combate y muestra "Avance rápido x2".
   - Al ganar: experiencia, oro, subidas de nivel y botín; "Aceptar" vuelve al mapa en el mismo sitio. Si cae todo el grupo, vuelve al título.
   - En emulador también sirven las flechas, Intro/Z (aceptar), X/Escape (cancelar) y F (rápido).
4. Anotar en "Preguntas pendientes" (o decírselo al agente) lo que se vea mal: tamaño de letra, botones difíciles de tocar, ritmo del combate (se ajusta con `combate.ticksPorPaso` en `configuracion.json`).

Limitaciones conocidas de esta versión: no hay guardado (H7), ni pueblo, posada o tienda (H5): los héroes caídos siguen caídos hasta volver al título.

## Ramas y pull requests pendientes
Orden de fusión: de la más antigua a la más nueva, con "Merge commit".
1. `feature/h35-jugable` (parte de `main`): H3.5, pull request #11 https://github.com/nivergarah-collab/ff1/pull/11
2. `chore/quitar-codigo-nativo` (parte de `feature/h35-jugable`): quitar el código nativo y documentación del cierre de H3.5, pull request #12 https://github.com/nivergarah-collab/ff1/pull/12

## Preguntas pendientes
- `gradle/libs.versions.toml` conserva la entrada `games-activity`, ya sin uso; quitarla es opcional y lo decide el usuario (`gradle/` no lo toca el agente).

## Pruebas
Nube, 2026-10-02 (bloque 7): `scripts/probar-logica.sh` 175 de 175 pruebas pasan. GitHub Actions: en `feature/h35-jugable`, `Pruebas` y `APK` en verde; en `chore/quitar-codigo-nativo`, `APK` en verde (lanzado a mano, la rama `chore/*` no dispara flujos al hacer push) y `Pruebas` en el pull request #12.

## Notas
- `ExampleUnitTest` y `ExampleInstrumentedTest` son de la plantilla y se reemplazarán.
- `.github/` es una ruta protegida: el agente no puede escribir en ella, los cambios al flujo los hace el usuario.
- Las skills maestras originales están en `Android/skills/` del computador del usuario; la copia de `skills/maestras/` es la que leen los agentes en la nube.
