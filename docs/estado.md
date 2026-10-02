# Estado · ff1

## Control
- Estado del MVP: EN CURSO
- Bloques ejecutados: 9 de 50
- Bloques seguidos sin avance: 1

Última actualización: 2026-10-02 (bloque 9, Sonnet: limpieza tras H3.6 y H4)

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
- Pull requests #11, #12 (H3.5 y código nativo) y #13 (plan H3.6) fusionados en `main` por el usuario; reanudado con Sonnet (EN CURSO).
- Bloque 8 (Sonnet, H3.6 "Menú del grupo" terminado, rama `feature/h36-menu-del-grupo` desde `main`): pila de pantallas en `Juego`; menú del grupo abierto con Cancelar en el mapa; Objetos y Magia de curación fuera de combate; revivir con la Pluma de alba (solo fuera de combate); Equipo con comparación de estadísticas; ficha de Estado; Formación que respeta el combate; Ajustes en caliente (`combate.ticksPorPaso`, `juego.msMensaje`, `juego.rapidoAlEmpezar`); Guardar apagado hasta H7; receta (sección 8b) y contrato al día. 214 pruebas.
- Bloque 8 (H4 "Narrativa" terminado, rama `feature/h4-narrativa` desde `feature/h36-menu-del-grupo`): `docs/historia.md` (historia original: Pozaluz, la Cantera Hundida y el Soterrado) y escenas de texto (documento `escena` v1, `guion.Guion`, `PantallaEscena`, marcadores `{heroe:<clase>}`); la apertura se muestra al empezar una partida nueva (`introduccion` en `inicio.json`). 220 pruebas.
- Pull requests #14 (H3.6) y #15 (H4) fusionados en `main` por el usuario.
- Bloque 9 (Sonnet, limpieza tras H3.6 y H4, rama `chore/limpieza-h36-h4` desde `main`): `Menu.vuelta` y `Estilo.marcador` sustituyen el giro de cursor y el `>` repetidos; prueba `MenuCursorTest`. 222 pruebas.
- Autorizado por el usuario el 2026-10-02: límite de 50 bloques (antes 24), quitar el código nativo de la plantilla (ver `skills/01-trabajo-autonomo.md`) y bloques de hasta unos 25 minutos, una sesión por hora.

## Siguiente
1. **H5** (tienda, posada y personajes con diálogo; usa `PantallaEscena`), rama `feature/h5-pueblo` desde `chore/limpieza-h36-h4`; luego H6 y H7. Siguiente limpieza tras H6.
2. Reglas para Sonnet: no cambiar de forma incompatible las interfaces del motor, `Pantalla`, `Escena` ni el contrato de datos (si hace falta, anotarlo en "Preguntas pendientes"); con 2 bloques sin avance o `Pruebas` en rojo 2 bloques seguidos, poner `PAUSA: volver a Opus (motivo)`; al marcar la última tarea de H7, abrir el pull request y poner `PAUSA: cambio de modelo (volver a Opus para H8)`.

La ruta completa está en `docs/plan.md`.

## Cómo probar el APK
1. **Bajarlo:** en GitHub, pestaña **Actions** del repositorio → ejecución **APK** de la rama (`feature/h36-menu-del-grupo`, o `main` cuando esté fusionada) → sección **Artifacts** → **ff1-debug-apk** (un .zip con el .apk; dura 14 días). Enlace directo a las ejecuciones: https://github.com/nivergarah-collab/ff1/actions/workflows/apk.yml
2. **Instalarlo:** pasar el .apk al celular (Android 11 o superior), abrirlo y permitir "instalar apps de origen desconocido" para la app con que se abrió. Si había una versión anterior de ff1 instalada con otra firma, desinstalarla primero.
3. **Qué probar** (unos 8 minutos, en vertical):
   - Título "Crónica de la Cantera": "Continuar" aparece apagado; "Aceptar" en "Nueva partida".
   - **Apertura:** una escena de texto de 7 líneas (Aceptar avanza; los personajes usan los nombres del grupo) y después el mapa.
   - Mapa: la cruceta mueve al grupo (cuadro amarillo); mantenerla pulsada lo hace caminar. Los riscos, la laguna y la arboleda no se pueden pisar. Por el sendero no hay encuentros; por la pradera y el matorral salta uno cada 15–30 pasos.
   - **Menú del grupo:** en el mapa, "Cancelar" abre el menú; "Cancelar" otra vez vuelve al mismo sitio. "Guardar" está apagado (llega en H7).
   - **Objetos:** elegir un Tónico de raíz y un héroe herido (tras algún combate): cura y gasta una unidad; con la vida llena avisa y no gasta. La Pluma de alba levanta a un héroe caído con un tercio de vida.
   - **Magia:** con Mirta, "Bálsamo" cura a un aliado y gasta PM; "Muro de ramas" avisa que solo sirve en combate; el Guardián no conoce magia.
   - **Equipo:** el grupo empieza con una hoja, una vara, dos jubones y un cordel en el inventario. Al elegir una pieza se ve la comparación (por ejemplo Ataque +4) antes de confirmar; "(Quitar)" la devuelve al inventario.
   - **Estado:** ficha de cada héroe (arriba/abajo cambia de héroe). **Formación:** Aceptar marca, mover y Aceptar cambia de sitio; en el combate siguiente el orden es el nuevo.
   - **Ajustes:** izquierda/derecha cambian la velocidad del combate, el tiempo de los mensajes y si el avance rápido empieza encendido; el cambio se nota al instante en el próximo combate. **Salir al título** pide confirmación.
   - Combate: la barra amarilla de cada héroe se llena; en su turno aparece el menú. Probar Atacar (elegir objetivo con la cruceta), Magia con Tadeo o Mirta, Objeto (Tónico de raíz; la Pluma no aparece aquí), Cancelar para volver y Huir. "Rápido" acelera el combate y muestra "Avance rápido x2".
   - Al ganar: experiencia, oro, subidas de nivel y botín; "Aceptar" vuelve al mapa en el mismo sitio. Si cae todo el grupo, vuelve al título.
   - En emulador también sirven las flechas, Intro/Z (aceptar), X/Escape (cancelar) y F (rápido).
4. Anotar en "Preguntas pendientes" (o decírselo al agente) lo que se vea mal: tamaño de letra, botones difíciles de tocar, ritmo del combate o cualquier pantalla del menú que quede apretada.

Limitaciones conocidas de esta versión: no hay guardado (H7), ni pueblo, posada o tienda (H5): los héroes caídos se levantan con la Pluma de alba (solo hay una) o al volver al título. Los ajustes no se guardan en disco hasta H7.

## Ramas y pull requests pendientes
Orden de fusión: de la más antigua a la más nueva, con "Merge commit".
1. `chore/limpieza-h36-h4` (parte de `main`): limpieza de las pantallas del menú. Pull request abierto (ver enlace en el resumen del bloque).

## Preguntas pendientes
- `gradle/libs.versions.toml` conserva la entrada `games-activity`, ya sin uso; quitarla es opcional y lo decide el usuario (`gradle/` no lo toca el agente).

## Pruebas
Nube, 2026-10-02 (bloque 9): `scripts/probar-logica.sh` 222 de 222 pruebas pasan en `chore/limpieza-h36-h4`. Antes (bloque 8): 220 de 220 en `feature/h4-narrativa`. GitHub Actions: `feature/h36-menu-del-grupo` con `Pruebas` y `APK` en verde; `feature/h4-narrativa` con `Pruebas` (push y pull request) y `APK` en verde.

## Notas
- `ExampleUnitTest` y `ExampleInstrumentedTest` son de la plantilla y se reemplazarán.
- `.github/` es una ruta protegida: el agente no puede escribir en ella, los cambios al flujo los hace el usuario.
- Las skills maestras originales están en `Android/skills/` del computador del usuario; la copia de `skills/maestras/` es la que leen los agentes en la nube.
