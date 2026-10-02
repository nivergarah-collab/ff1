# Misión · MVP de ff1 mini

## Objetivo
Un juego de rol por turnos para Android que reproduzca el estilo y la mecánica de los RPG clásicos, con trama, personajes, nombres y arte originales. Prioriza que se juegue de principio a fin con un recorrido corto, no que sea largo.

## Qué incluye el MVP
1. Pantalla de título (nueva partida y continuar).
2. Introducción y cinemáticas como escenas de texto, con avance por toque.
3. Grupo de héroes con clases originales; el jugador elige los nombres.
4. Mundo explorable con mapa por casillas: un pueblo, un mapa exterior pequeño y una mazmorra.
5. Pueblo con tienda (comprar y vender), posada (curar) y personajes con diálogo.
6. Encuentros aleatorios y combate con barra de tiempo (ATB): atacar, magia, objeto y huir; enemigos, experiencia, niveles y botín.
7. Inventario y equipamiento básico.
8. Una mazmorra con un jefe.
9. Guardar y cargar partida.
10. Interruptor de avance rápido (ver `decisiones.md`).
11. Escena de cierre del MVP.

## Fuera del MVP
Videos, música o arte comerciales, multijugador, tiendas dentro de la app, más de una mazmorra, API remota e interfaz de edición de contenido (de estas dos solo se diseñan los puntos de enchufe; ver "Diseño adaptable").

## Diseño adaptable
Requisito de arquitectura: lo que se construya debe poder reutilizarse para otros juegos, alimentarse con configuración en tiempo real (por una API) o por una interfaz de edición, y combinarse con otros módulos. Para eso:
1. **Motor y contenido separados.** Las reglas viven en el código; héroes, enemigos, habilidades, objetos, mapas, escenas, textos y balance viven en datos externos (en `app/src/main/assets/`). Cambiar de juego es cambiar los datos.
2. **Fuentes intercambiables.** El motor obtiene contenido y configuración mediante interfaces (fuente de contenido, configuración, azar, tiempo y guardado). En el MVP hay dos implementaciones: archivos locales y memoria (para pruebas). Una fuente remota o un editor se podrían añadir después sin tocar el motor.
3. **Parámetros ajustables en caliente.** Velocidad de la barra, multiplicador de avance rápido, balance de daño y similares viven en un objeto de configuración que se puede reemplazar mientras el juego corre, con validación.
4. **Datos versionados y validados.** Cada tipo de dato lleva versión y validador; los datos inválidos se rechazan con un mensaje claro y no cierran el juego.
5. **Extensible por registro.** Tipos nuevos de habilidad, efecto, condición de victoria o escena se registran sin modificar el núcleo.
6. **Contrato de datos documentado.** `docs/contrato-de-datos.md` describe cada tipo de dato, sus campos y su versión. Es la base del futuro molde del motor.
7. **Nombres genéricos.** El código y los datos no dependen de este juego en particular (por ejemplo "combatiente", no nombres propios).

En el MVP no se implementan la API remota ni la interfaz de edición, ni se pide permiso de red. Sí se construyen las interfaces y las pruebas que lo harán posible después. No se añaden dependencias: el agente elige en H0 un formato de datos legible sin librerías externas, tanto en la JVM como en Android, y lo registra en `decisiones.md`.

## Contenido y derechos
- Se pueden estudiar guías de juegos clásicos **solo** para el ritmo y la estructura: orden de los primeros pasos, curva de dificultad y momento en que aparece cada sistema.
- No se copian la trama, personajes, nombres, lugares, objetos con nombre propio ni diálogos de ningún juego existente. Todo es original.
- Arte y sonido: marcadores de posición simples, o recursos propios o libres de derechos. No se descargan archivos de terceros sin confirmación.

## Definición de terminado
El MVP está **listo para revisión** cuando se cumplen todas estas condiciones:
1. Los 11 puntos de "Qué incluye el MVP" están implementados.
2. Todas las tareas de los hitos H0 a H8 de `plan.md` están marcadas, salvo el recorrido en emulador (lo verifica el usuario) y la "Deuda técnica" aceptada.
3. Existe una prueba de recorrido completo que simula una partida de principio a fin (título, exploración, encuentros, combate, jefe, escena de cierre, guardado y carga) y pasa.
4. La suite de lógica pasa y GitHub Actions está en verde en la última rama.
5. `docs/historia.md` y las escenas de texto están completos y son originales.
6. La pasada final de refactorización y limpieza está hecha (sin código muerto ni duplicado evidente) y el README final está escrito.
7. El motor funciona con dos paquetes de contenido distintos (el del juego y uno mínimo de prueba) sin cambiar código, y `docs/contrato-de-datos.md` está al día.
8. `docs/estado.md` resume el resultado y lista lo que el usuario debe revisar.

"Listo para revisión" no significa "aceptado": la aceptación la da el usuario tras probar el juego en un emulador.

## Límites del trabajo autónomo
El agente se detiene, y lo declara en `docs/estado.md`, cuando ocurre lo primero de esto:
- El MVP queda listo para revisión (ver arriba).
- Se ejecutaron **50 bloques** (unos dos días a una sesión por hora), aunque falte trabajo.
- Pasaron **3 bloques seguidos sin marcar ninguna tarea** del plan.
- El flujo `Pruebas` de GitHub Actions queda en rojo en **2 bloques seguidos** y el agente no logra corregirlo.

No añade funcionalidades fuera de esta misión. El procedimiento exacto está en `skills/01-trabajo-autonomo.md`.

## Restricciones
- Plantilla de Android Studio en Java (el C++ de la plantilla se quitó en H3.5, autorizado), `minSdk` 30. Sin Kotlin ni Compose.
- Lógica de juego en Java puro, sin dependencias de Android.
- Cómo se dibujan menús y texto: lo decide el agente en el hito H0, lo justifica y lo registra en `decisiones.md`.
