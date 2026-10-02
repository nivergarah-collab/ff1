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
Videos, música o arte comerciales, multijugador, tiendas dentro de la app, más de una mazmorra.

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
7. `docs/estado.md` resume el resultado y lista lo que el usuario debe revisar.

"Listo para revisión" no significa "aceptado": la aceptación la da el usuario tras probar el juego en un emulador.

## Límites del trabajo autónomo
El agente se detiene, y lo declara en `docs/estado.md`, cuando ocurre lo primero de esto:
- El MVP queda listo para revisión (ver arriba).
- Se ejecutaron **24 bloques** (unos tres días a ocho por día), aunque falte trabajo.
- Pasaron **3 bloques seguidos sin marcar ninguna tarea** del plan.
- GitHub Actions queda en rojo en **2 bloques seguidos** y el agente no logra corregirlo.

No añade funcionalidades fuera de esta misión. El procedimiento exacto está en `skills/01-trabajo-autonomo.md`.

## Restricciones
- Plantilla nativa (Java + C++), `minSdk` 30. Sin Kotlin ni Compose.
- Lógica de juego en Java puro, sin dependencias de Android.
- Cómo se dibujan menús y texto: lo decide el agente en el hito H0, lo justifica y lo registra en `decisiones.md`.
