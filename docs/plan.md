# Plan · MVP de ff1 mini

Lo mantiene el agente. Marcar `[x]` al terminar y anotar aquí cualquier cambio de orden con su motivo. Las tareas se dividen para que cada una quepa en un bloque de trabajo.

## H0 · Preparación
- [x] Comprobar el entorno en la nube: `javac`, JUnit 4 y Hamcrest incluidos con Gradle.
- [x] Crear `scripts/probar-logica.sh`: compila la lógica en Java puro y sus pruebas, las ejecuta e imprime una línea por clase y el detalle solo de los fallos.
- [x] Investigar guías de RPG clásicos, solo ritmo y estructura; resumir en `docs/investigacion-ritmo.md` (sin trama, nombres ni diálogos).
- [x] Decidir cómo se dibujan menús y texto, y registrarlo en `docs/decisiones.md`.
- [x] Definir las interfaces del motor (fuente de contenido, configuración, azar, tiempo, guardado), elegir el formato de datos sin dependencias externas y escribir `docs/contrato-de-datos.md` (ver "Diseño adaptable" en `mision-mvp.md`).
- **Salida:** el script corre una prueba de ejemplo, el resumen de ritmo existe y el contrato de datos está escrito.

## H1 · Núcleo del combate ATB
- [x] Modelo: personaje, enemigo, habilidad, estados.
- [x] Barra de tiempo por ticks simulados.
- [x] Acciones: atacar, magia, objeto, huir. Cálculo de daño.
- [x] Fin del combate y recompensas.
- [x] Interruptor de avance rápido como multiplicador de ticks.
- [x] Los parámetros del combate (velocidad de la barra, daño, avance rápido) vienen de la configuración, no de constantes en el código.
- **Salida:** un combate completo se simula en pruebas, con y sin avance rápido, y cambiando la configuración cambia el resultado.

## H2 · Progresión e inventario
- [x] Experiencia y niveles.
- [x] Inventario, objetos y equipamiento.
- [x] Botín.

## H3 · Mundo
- [x] Mapa por casillas, movimiento y colisiones.
- [x] Encuentros aleatorios que conectan con el combate.

## H3.5 · Primera versión jugable
Hito insertado por el usuario el 2026-10-02 (ver `decisiones.md`): un APK instalable con un recorrido corto (título, mapa, encuentro, combate con menú, vuelta al mapa). Lo hace Opus porque define cómo se dibuja y se maneja todo el juego.
- [x] Escena de dibujo en Java puro (órdenes rectángulo, texto y casilla sobre una rejilla virtual fija) y una clase Android que la traduce a `Canvas` y escala a la pantalla. Pruebas de las órdenes.
- [x] Entrada táctil: cruceta y botones aceptar/cancelar en pantalla, traducidos a una entrada en Java puro. Bucle de juego con la interfaz `Tiempo` y una `MainActivity` nueva con vista propia como actividad de lanzamiento.
- [x] Máquina de pantallas en Java puro, con pruebas: título (Nueva partida; Continuar deshabilitado hasta H7), exploración y combate.
- [x] Exploración: dibujar el mapa de `mapas/campo.json`, mover al jugador con colisiones y encuentros aleatorios que abren el combate.
- [x] Combate con menú (Atacar, Magia, Objeto, Huir), barras de tiempo y de vida, interruptor de avance rápido visible, fin con recompensas y regreso al mapa.
- [x] Quitar el código nativo (rama `chore/quitar-codigo-nativo`), con `Pruebas` y `APK` en verde.
- [x] Dejar en `docs/estado.md` cómo bajar, instalar y probar el APK.

## H3.6 · Menú del grupo
Hito insertado por el usuario el 2026-10-02 (ver `decisiones.md`): el juego necesita el menú de grupo propio de los RPG clásicos. Se abre desde el mapa con un botón y se maneja con la misma cruceta y los botones aceptar/cancelar. Cada sección es una `Pantalla` nueva sobre el patrón de `receta-de-extension.md`; la lógica va en Java puro con pruebas y los datos (héroes, objetos, equipo, habilidades) salen de los JSON, sin números fijos en el código.
- [x] Pila de pantallas: abrir el menú desde la exploración y volver al mapa sin perder la posición, con pruebas. Si hace falta cambiar `Pantalla`, `Juego` o `Escena` de forma incompatible, anotarlo en "Preguntas pendientes" y poner `PAUSA: volver a Opus`.
- [x] Menú principal con las secciones: Objetos, Magia, Equipo, Estado, Formación, Ajustes, Guardar y Salir al título. (Las secciones aún sin pantalla aparecen apagadas; se habilitan al terminar cada tarea. El menú se abre con Cancelar en el mapa; no se añadió un botón nuevo para no tocar `Controles`.)
- [x] Objetos: ver el inventario y usar objetos consumibles fuera de combate sobre un héroe (curar, revivir), con pruebas. (Curar y revivir hechos: el tipo `revivir` solo actúa sobre caídos y por eso solo se usa fuera de combate.)
- [x] Magia: ver las habilidades de cada héroe y lanzar las de curación fuera de combate gastando magia, con pruebas.
- [x] Equipo: ranuras de arma, armadura y accesorio por héroe, equipar y quitar con comparación de estadísticas antes de confirmar, con pruebas (el motor de equipo ya existe en `inventario`).
- [x] Estado: nivel, experiencia, estadísticas y estados alterados de cada héroe. (Fuera de combate los estados no se conservan: la ficha muestra la condición "en pie" o "caído"; ver `decisiones.md`.)
- [x] Formación: cambiar el orden del grupo y que el combate respete el nuevo orden, con pruebas.
- [x] Ajustes: velocidad del texto y del combate y avance rápido por defecto, guardados en la configuración (en caliente), con pruebas. (Se cambian en la configuración vigente al instante; escribirlos a disco queda para H7, con el guardado.)
- [x] Guardar: la opción aparece, pero desactivada hasta H7 (ahí se conecta).
- [x] Actualizar `docs/receta-de-extension.md` (cómo añadir una sección al menú) y `docs/estado.md` ("Cómo probar el APK" con el menú).

## H4 · Narrativa
- [x] Historia original en `docs/historia.md`, con las mismas etapas de un RPG clásico.
- [x] Motor de escenas de texto y diálogos. (Escena de apertura enganchada a la partida nueva; las escenas del jefe y del cierre se escriben con H6.)

## H5 · Pueblo y servicios
- [x] Salidas entre mapas y lugares del mapa (tienda, posada, vecino), con `servicios.json` y pruebas.
- [x] Tienda (comprar y vender), posada y personajes con diálogo en Pozaluz; la partida empieza en el pueblo.

## H6 · Mazmorra y jefe
- [x] Cantera Hundida: puerta de hierro con llave, dos galerías enlazadas con escalera y enemigos propios (`cantera-alta`, `cantera-baja`).
- [x] Jefe (el Soterrado): combatiente, golpe fuerte cada pocos turnos, escena previa y encuentro al final de la galería baja.
- [x] Escena de cierre (`cierre`, tras vencer al jefe; el grupo vuelve a Pozaluz).

## H7 · Guardado y carga
- [x] Guardar y cargar partida, con pruebas de ida y vuelta (`Partida.guardar/cargar`, `AlmacenArchivos`, Guardar en el menú y Continuar en el título).

## H8 · Integración y limpieza
- [x] Prueba con un segundo paquete de contenido mínimo: el motor funciona sin cambiar código. (`app/src/test/resources/contenido-minimo`, "El faro de la ensenada", y `PaqueteMinimoTest`.)
- [x] Actualizar `docs/contrato-de-datos.md` con lo que quedó (documentos obligatorios y opcionales, paquete mínimo, tipos de lugar, `juego.Guardado`, textos de la interfaz).
- [ ] Recorrido completo en emulador (lo verifica el usuario; pasos en `docs/estado.md`).
- [x] Elegir los nombres de los héroes al empezar (punto 3 de `mision-mvp.md`, que faltaba: hoy salen de `inicio.json`). Pantalla de nombres en Java puro con rejilla de letras manejada con la cruceta, nombre por defecto el de `inicio.json`, antes de la apertura; con pruebas. Añadida en el bloque 11 al repasar la definición de terminado. Hecho en el bloque 12: `EditorNombre`, `PantallaNombres`, `juego.largoNombre` y `NombresTest`.
- [x] Refactorización, eliminación de código muerto, balance y README final. (Hecho en el bloque 11: guardado separado en `juego.Guardado`, registro de lugares, `Bando.contrario` quitado, tope del oro unificado, prueba `RecorridoCompletoTest` y balance del Soterrado. README final y ventana de listas común `Estilo.desdeVentana` en el bloque 12.)

## Fase 2 · Editor de parámetros (herramienta aparte)
Meta: una herramienta para el computador, separada del juego, que lee y edita el contenido y la configuración (`app/src/main/assets/contenido/`) sin tocar el código del motor. Decisión del usuario (2026-10-03): opción 1 (interfaz paralela que adapta parámetros); el pixel art queda para una fase posterior.
Reglas de la fase:
- La herramienta vive en `herramientas/editor/`: HTML, CSS y JavaScript sin dependencias ni paso de construcción (se abre en el navegador). No toca `gradle/`, `settings.gradle.kts` ni `.github/`.
- Lo que el editor guarda tiene que cumplir `docs/contrato-de-datos.md` y pasar la validación real del motor (Java), no solo una copia en JavaScript.
- No se cambia de forma incompatible el contrato de datos ni las interfaces del motor; si hace falta ampliar el contrato, es una versión nueva compatible, anotada en `docs/decisiones.md`.
- Contenido original, como en el resto del proyecto.

### H9 · Validación de contenido desde la línea de comandos
- [ ] `scripts/validar-contenido.sh <carpeta>`: carga un paquete de contenido con los validadores reales del motor (Java puro) e imprime una línea por documento y el detalle solo de los errores, con la ruta del campo. Código de salida distinto de cero si falla.
- [ ] Pruebas del validador con paquetes buenos y malos (sobre el paquete del juego y sobre el segundo paquete de H8).
- [ ] Una salida opcional en JSON (`--json`) para que el editor la lea.
- **Salida:** validar cualquier carpeta de contenido con un solo comando; documentado en `docs/pruebas.md`.

### H10 · Editor de la configuración
- [ ] `herramientas/editor/index.html`: abrir la carpeta de contenido con la API de archivos del navegador, listar los documentos y mostrar `configuracion.json` como formulario (todos los parámetros, con rangos y descripciones tomados del contrato).
- [ ] Guardado con copia de seguridad `.bak` y aviso de cambios sin guardar.
- [ ] Validación en el editor (rangos y tipos del contrato) y botón para revisar con `scripts/validar-contenido.sh`.
- [ ] `herramientas/editor/LEEME.md`: cómo abrirlo y qué hace.
- **Salida:** cambiar el ritmo del combate o los pasos entre encuentros desde un formulario y verlo en el juego.

### H11 · Editor de contenido
- [ ] Editores de tabla para `combatientes`, `habilidades`, `objetos`, `botin`, `encuentros` y `servicios` (añadir, duplicar, borrar filas; referencias por id con lista desplegable y aviso de ids rotos).
- [ ] Vista de balance: daño esperado, turnos para vencer a un enemigo y experiencia por nivel, calculados con las mismas fórmulas del motor (reutilizar el motor desde una prueba que exporte las tablas, o replicarlas con una prueba que compare ambos resultados).
- [ ] Mapas y escenas: vista de solo lectura del mapa por casillas y edición de textos de escenas.
- **Salida:** crear un enemigo nuevo y su encuentro desde el editor sin escribir JSON a mano.

### H12 · Conexión con el juego
- [ ] Documentar y probar el flujo: editar → validar → copiar la carpeta de contenido → compilar el APK.
- [ ] Opción de exportar un paquete de contenido como carpeta lista para una variante del juego.
- [ ] Revisión final, `docs/estado.md` con pasos para que el usuario pruebe el editor, y estado `LISTO PARA REVISIÓN`.

## Deuda técnica
Registrar aquí lo que se deja pendiente de refactorizar o limpiar.
- Combate: los factores estructurales de las fórmulas (defensa / 2 y / 4, el 2 × de la huida y sus límites 5–95 %) son constantes en `Acciones`; pasarlos a la configuración si el balance lo pide (H8).
- Combate: `turnoAutomatico` solo ataca; una IA de enemigos que use habilidades queda para H6 (jefe).
- Limpieza tras H1–H3 hecha en el bloque 6 (helper `PaqueteDelJuego` para pruebas, `Documentos.rango`/`rangoO`/`dentro` para rangos y `CatalogoCombate.leerHabilidad` compartido con los objetos). Queda sin unificar la comprobación de ids repetidos (cada catálogo tiene su mensaje) y los `if` de objetivo; revisar en H8.
- Mundo: el mapa v1 no tiene salidas entre mapas; añadirlas en H5–H6.
- H3.5: los textos de la interfaz (menús, mensajes de combate en `juego.Mensajes`) están en el código; pasarlos a datos si se quiere traducir o reutilizar (H8). Los héroes caídos siguen caídos tras ganar un combate hasta que exista la posada (H5).
- Limpieza tras H3.6 y H4 hecha en el bloque 9 (`Menu.vuelta`, `Estilo.marcador`). Pendiente para H8: unificar el dibujo de listas con ventana y los textos de las pantallas del menú (siguen en el código).
- Limpieza tras H5–H7 hecha en el bloque 11 (rama `chore/limpieza-h5-h7`): guardado en `juego.Guardado`, registro de lugares (`Mapa.TIPOS_LUGAR`, `Servicios.servicio`, `PantallaExploracion.lugares()`), `Bando.contrario` sin uso quitado y tope del oro en `Partida.ORO_MAXIMO`. Los textos de `PantallaTienda`/`PantallaPosada` siguen en el código: pasarlos a datos exige un documento nuevo (cambio de contrato), así que quedan como deuda aceptada junto al resto de textos de la interfaz.
- Deuda aceptada del MVP: textos fijos de la interfaz en el código (`Mensajes` y cada `Pantalla*`), y la disposición de cada lista propia de su pantalla (bloque 12: el desplazamiento de las listas largas ya es común, `Estilo.desdeVentana`, en combate, tienda y objetos). Los nombres de contenido sí vienen siempre del paquete.
- Enemigos con habilidades: solo el jefe usa `golpeFuerte`; los demás atacan (la IA general queda fuera del MVP).
- Guardado: una sola ranura y no se guarda dentro de un combate.
