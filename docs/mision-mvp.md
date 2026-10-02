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
- Todo lo del apartado "Qué incluye" está implementado.
- Cada funcionalidad tiene pruebas, y la suite completa pasa en GitHub Actions.
- El juego se puede completar de inicio a fin en un emulador.
- No queda código muerto ni duplicado evidente, y la documentación está al día.
- `docs/estado.md` resume el resultado final.

## Restricciones
- Plantilla nativa (Java + C++), `minSdk` 30. Sin Kotlin ni Compose.
- Lógica de juego en Java puro, sin dependencias de Android.
- Cómo se dibujan menús y texto: lo decide el agente en el hito H0, lo justifica y lo registra en `decisiones.md`.
