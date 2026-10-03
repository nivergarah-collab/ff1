# Estado · ff1

## Control
- Estado del MVP: EN CURSO
- Bloques ejecutados: 14 de 50
- Bloques seguidos sin avance: 0

Última actualización: 2026-10-03 (bloque 14, Sonnet: H11 terminado; sigue un bloque de limpieza y luego H12)

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
- Bloque 10 (Sonnet, H5, H6 y H7 terminados, rama `feature/h5-pueblo` desde `chore/limpieza-h36-h4`): Pozaluz (mapa con tienda de Lupe, posada de Casilda y dos vecinos; la partida empieza ahí) con salidas entre mapas (`Mapa.Salida`, `requiere` para puertas con llave) y lugares (`Mapa.Lugar`), `servicios.json` (`pueblo.Servicios`), compra y venta (`pueblo.ventaPorCiento`), posada y escenas de vecinos; la Cantera Hundida (puerta de hierro con la Llave de cantera, galerías alta y baja con escalera y tres enemigos nuevos); el Soterrado (jefe con `golpeFuerte` cada 3 turnos, escena previa, combate sin huida y escena `cierre` que devuelve al grupo a Pozaluz); guardado y carga (`Partida.guardar/cargar`, documento `partida` v1, `AlmacenArchivos`, Guardar en el menú y Continuar en el título). 254 pruebas.
- Pull requests #16, #17 (H5–H7) y #18 fusionados en `main` por el usuario; reanudado con Opus.
- Bloque 11 (Opus, limpieza tras H5–H7, rama `chore/limpieza-h5-h7` desde `main`): guardado separado de `Partida` en `juego.Guardado`; registro de lugares en vez de `if` por tipo (`Mapa.TIPOS_LUGAR`, `Servicios.Servicio`/`servicio(lugar)`, `PantallaExploracion.lugares()`); `Bando.contrario` sin uso quitado; tope del oro en `Partida.ORO_MAXIMO`. Sin cambios de comportamiento.
- Bloque 11 (Opus, H8, rama `feature/h8-integracion` desde `chore/limpieza-h5-h7`): segundo paquete de contenido mínimo "El faro de la ensenada" en `app/src/test/resources/contenido-minimo/` y `PaqueteMinimoTest` (partida entera sin tocar código); `RecorridoCompletoTest` (título → apertura → Pozaluz → encuentro → puerta con llave → jefe → cierre → guardar → Continuar); contrato y receta al día; balance del Soterrado (vida 420, ataque 24, defensa 10, Sacudida 30: atacando sin más se vence a nivel 4–5, antes bastaba el 2). 257 pruebas. Nota: el primer commit de H8 (paquete mínimo) quedó también en la rama de limpieza, que se fusiona antes.
- Pull requests #19 (limpieza) y #20 (H8, primera parte) fusionados en `main` por el usuario.
- Bloque 12 (Opus, H8 terminado, rama `feature/h8-cierre` desde `main`): el jugador elige el nombre de cada héroe al empezar una partida nueva (`EditorNombre`, `PantallaNombres`, `juego.largoNombre`; propone el de `inicio.json` con el cursor en "Fin", Cancelar borra); ventana de listas común `Estilo.desdeVentana` (combate, tienda y objetos se desplazan si la lista no cabe); README final; contrato y receta al día. 262 pruebas. **MVP listo para revisión.**
- Pull request #23 (H9 y H10) fusionado en `main` por el usuario.
- Bloque 14 (Sonnet, Fase 2: H11 terminado, rama `feature/h11-contenido` desde `main`): **editores de tabla** (`tablas.js` con el modelo declarativo de columnas y las funciones puras; `editor-tablas.js` con la interfaz): combatientes, habilidades, objetos, botín, encuentros y servicios (tiendas, posadas, vecinos y jefes en pestañas), con añadir, duplicar (id libre `x-copia`) y borrar (avisa dónde se usa el id), referencias por lista desplegable, ids rotos marcados, y guardado bloqueado mientras haya problemas. **Vista de balance** (`balance.js`, `vista-balance.js`): daño y golpes héroe↔enemigo por nivel y tabla de experiencia con las fórmulas de `Acciones`; `BalanceEditorTest` (Java) y `balance.test.js` leen el mismo `pruebas/balance-casos.json`, así que el motor real y el editor no pueden discrepar. **Mapas y escenas** (`mundo.js`, `editor-mundo.js`): cuadrícula del mapa en solo lectura con inicio, salidas y lugares, y edición de los textos de las escenas. Humo en Chromium sin cabeza con una carpeta simulada: crear un enemigo y guardar, botín con id, pestañas de servicios, balance, mapa y escena con `.bak`, sin errores de consola. 275 pruebas Java y 50 de node.
- Autorizado por el usuario el 2026-10-02: límite de 50 bloques (antes 24), quitar el código nativo de la plantilla (ver `skills/01-trabajo-autonomo.md`) y bloques de hasta unos 25 minutos, una sesión por hora.
- Bloque 13 (Sonnet, Fase 2: H9 y H10 terminados, rama `feature/h9-validador` desde `main`): **H9** `herramientas.ValidadorContenido` (Java puro, usa los cargadores reales del motor) y `scripts/validar-contenido.sh <carpeta> [--json]`: una línea por documento (`OK`/`ERROR`/`OMITIDO`), detalle con la ruta del campo solo en los errores, código de salida 0/1/2; `ValidadorContenidoTest` (paquete del juego, paquete mínimo, referencia rota, JSON mal formado, rango, documento ausente, salida JSON). `LectorArchivos` ahora lanza `FileNotFoundException` si falta el archivo y `CatalogoCombate.cargar` antepone el documento (`habilidades.json:`/`combatientes.json:`) a sus errores. **H10** `herramientas/editor/` (`index.html`, `estilo.css`, `app.js`, `logica.js`, `almacen.js`, `esquema-configuracion.js`, `LEEME.md`): abre la carpeta con la API de archivos, lista los documentos, formulario de `configuracion.json` con rango, descripción y valor por defecto de los 17 parámetros, validación al escribir, guardado con `.bak`, aviso de cambios sin guardar y cuadro "Revisar con el motor" (comando + carga de `informe.json`). `EsquemaConfiguracion` expone `nombres/esEntero/minimo/maximo/defecto` de solo lectura y `EsquemaEditorTest` compara el esquema del editor con el del motor. `scripts/probar-editor.sh` (node, sin dependencias): 23 pruebas, entre ellas que el motor acepta lo que el editor guarda (mínimos, máximos y un cambio normal). Humo en Chromium sin cabeza con una carpeta simulada: abrir, editar, error de rango, guardar y `.bak` correctos, sin errores de consola. 274 pruebas Java.

## Siguiente
1. **Bloque de limpieza** (H9, H10 y H11 terminados): revisar `herramientas/editor/` (las tres funciones `el()` repetidas en `app.js`, `editor-tablas.js`, `vista-balance.js` y `editor-mundo.js` podrían pasar a un archivo común; `app.js` ya pasa de 450 líneas) y `ValidadorContenido`. Rama `chore/limpieza-editor`, encadenada a `feature/h11-contenido` si sigue sin fusionar.
2. **H12 · Conexión con el juego** (ver `docs/plan.md`): documentar y probar el flujo editar → validar → copiar → compilar, opción de exportar un paquete como carpeta lista, revisión final y, al terminar, `LISTO PARA REVISIÓN` con los pasos para que el usuario pruebe el editor.
3. Pixel art: fase posterior, requiere un contrato de arte.

## Cómo probar el APK
1. **Bajarlo:** en GitHub, pestaña **Actions** del repositorio → ejecución **APK** de la rama (`feature/h8-cierre`, o `main` cuando esté fusionada) → sección **Artifacts** → **ff1-debug-apk** (un .zip con el .apk; dura 14 días). Enlace directo a las ejecuciones: https://github.com/nivergarah-collab/ff1/actions/workflows/apk.yml
2. **Instalarlo:** pasar el .apk al celular (Android 11 o superior), abrirlo y permitir "instalar apps de origen desconocido" para la app con que se abrió. Si había una versión anterior de ff1 instalada con otra firma, desinstalarla primero.
3. **Qué probar** (en vertical):
   - Título "Crónica de la Cantera": "Continuar" aparece apagado hasta que exista un guardado; "Aceptar" en "Nueva partida".
   - **Nombres (H8):** una pantalla por héroe con su clase y el nombre propuesto. "Aceptar" sobre "Fin" lo deja así. Para cambiarlo: la cruceta mueve por la rejilla de letras, "Aceptar" escribe la letra (hasta 8), "Borrar" o "Cancelar" quitan la última y "Fin" pasa al siguiente héroe (un nombre vacío no se acepta). Los nombres elegidos se ven en la apertura, en el menú, en el combate y tras Continuar.
   - **Apertura:** una escena de texto de 7 líneas (Aceptar avanza; los personajes usan los nombres del grupo) y después el mapa.
   - **Pozaluz (H5):** tras la apertura el grupo está en la plaza (la fuente azul no se pisa). Mirando al mostrador amarillo de arriba a la derecha, "Aceptar" abre la **Tienda de Lupe**: izquierda/derecha cambian entre Comprar y Vender, "Aceptar" compra o vende una unidad (se vende a la mitad del precio). El mostrador rojo de la izquierda es la **Posada de Casilda** (15 de oro: cura a todos y levanta a los caídos; no cobra si nadie lo necesita). Los dos vecinos (violeta y turquesa) tienen su escena. El camino de arriba lleva al campo.
   - **La Cantera Hundida (H6):** en el campo, la puerta de hierro oscura de arriba (centro) se abre porque el grupo lleva la Llave de cantera. Dos galerías con escalera entre ellas y enemigos nuevos (murciélagos de cal, escarabajos de roca, sombras de sal). Al fondo de la galería baja, a la izquierda, está la grieta: "Aceptar" frente a ella lanza la escena previa y el combate contra el Soterrado (no se puede huir; cada 3 turnos da una sacudida fuerte). Al vencerlo, escena de cierre y el grupo vuelve a Pozaluz.
   - **Guardar (H7):** Menú → Guardar escribe la partida ("Partida guardada."). Al cerrar y abrir la app, "Continuar" en el título la retoma en el mismo sitio, con el grupo, el oro, los objetos, el equipo, la formación, los jefes vencidos y los ajustes.
   - Mapa: la cruceta mueve al grupo (cuadro amarillo); mantenerla pulsada lo hace caminar. Los riscos, la laguna y la arboleda no se pueden pisar. Por el sendero no hay encuentros; por la pradera y el matorral salta uno cada 15–30 pasos.
   - **Menú del grupo:** en el mapa, "Cancelar" abre el menú; "Cancelar" otra vez vuelve al mismo sitio.
   - **Objetos:** elegir un Tónico de raíz y un héroe herido (tras algún combate): cura y gasta una unidad; con la vida llena avisa y no gasta. La Pluma de alba levanta a un héroe caído con un tercio de vida.
   - **Magia:** con Mirta, "Bálsamo" cura a un aliado y gasta PM; "Muro de ramas" avisa que solo sirve en combate; el Guardián no conoce magia.
   - **Equipo:** el grupo empieza con una hoja, una vara, dos jubones y un cordel en el inventario. Al elegir una pieza se ve la comparación (por ejemplo Ataque +4) antes de confirmar; "(Quitar)" la devuelve al inventario.
   - **Estado:** ficha de cada héroe (arriba/abajo cambia de héroe). **Formación:** Aceptar marca, mover y Aceptar cambia de sitio; en el combate siguiente el orden es el nuevo.
   - **Ajustes:** izquierda/derecha cambian la velocidad del combate, el tiempo de los mensajes y si el avance rápido empieza encendido; el cambio se nota al instante en el próximo combate. **Salir al título** pide confirmación.
   - Combate: la barra amarilla de cada héroe se llena; en su turno aparece el menú. Probar Atacar (elegir objetivo con la cruceta), Magia con Tadeo o Mirta, Objeto (Tónico de raíz; la Pluma no aparece aquí), Cancelar para volver y Huir. "Rápido" acelera el combate y muestra "Avance rápido x2".
   - Al ganar: experiencia, oro, subidas de nivel y botín; "Aceptar" vuelve al mapa en el mismo sitio. Si cae todo el grupo, vuelve al título.
   - En emulador también sirven las flechas, Intro/Z (aceptar), X/Escape (cancelar) y F (rápido).
4. Anotar en "Preguntas pendientes" (o decírselo al agente) lo que se vea mal: tamaño de letra, botones difíciles de tocar, ritmo del combate o cualquier pantalla del menú que quede apretada.

Limitaciones conocidas: una sola ranura de guardado; no se guarda a mitad de un combate ni dentro de las escenas; los enemigos (salvo el jefe) solo atacan; no hay cofres. La semilla del azar no se guarda.

## Ramas y pull requests pendientes
Orden de fusión: de la más antigua a la más nueva, con "Merge commit".
1. `feature/h11-contenido` (desde `main`, tras el pull request #23): H11 (editor de contenido). Pull request abierto al cerrar el bloque 14.

## Preguntas pendientes
- Las ramas `chore/**` no disparan los flujos `Pruebas` ni `APK` (solo `main`, `feature/**` y `fix/**`); `chore/limpieza-h5-h7` no tiene ejecuciones, pero su contenido está incluido en `feature/h8-integracion`, que sí está en verde. Si se quiere CI en `chore/**`, lo añade el usuario en `.github/`.
- `gradle/libs.versions.toml` conserva la entrada `games-activity`, ya sin uso; quitarla es opcional y lo decide el usuario (`gradle/` no lo toca el agente).

## Pruebas
Nube, 2026-10-03 (bloque 14): `scripts/probar-logica.sh` 275 de 275 pruebas pasan en `feature/h11-contenido`; `scripts/probar-editor.sh` 50 de 50 (node 22). GitHub Actions: ver la línea siguiente.

## Notas
- `ExampleUnitTest` y `ExampleInstrumentedTest` son de la plantilla y se reemplazarán.
- `.github/` es una ruta protegida: el agente no puede escribir en ella, los cambios al flujo los hace el usuario.
- Las skills maestras originales están en `Android/skills/` del computador del usuario; la copia de `skills/maestras/` es la que leen los agentes en la nube.
