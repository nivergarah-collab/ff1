# Plan de modelos · ff1

Qué modelo trabaja en cada tramo del MVP, y cómo se cambia de uno a otro. Lo mantiene el usuario junto con Claude de la raíz; el agente autónomo no cambia su propio modelo.

## Idea
El motor y su arquitectura (combate, contrato de datos, patrones de extensión) se diseñan con Opus. Cuando esos patrones están escritos y probados, el contenido y los servicios que se apoyan en ellos (historia, pueblo, mazmorra, guardado) los puede hacer Sonnet, más barato, siguiendo una receta. La integración final y la limpieza vuelven a Opus.

## Fases
| Fase | Hitos | Modelo | Por qué |
|---|---|---|---|
| A | H0 a H3 (preparación, combate, progresión e inventario, mundo) | Opus | Decide la arquitectura, el formato de datos y las interfaces de las que depende todo lo demás. |
| B | H4 a H7 (narrativa, pueblo, mazmorra y jefe, guardado) | Sonnet | Trabajo repetitivo sobre patrones ya definidos: escenas, mapas, tienda, guardado. |
| C | H8 (integración, segundo paquete de contenido, refactor, README) | Opus | Cambios que cruzan todo el código y exigen criterio de diseño. |

H0 ya está terminado y H1 está en curso (Opus).

## Ritmo
- La tarea programada corre una vez por hora; es el mínimo que permite la plataforma. Cada ejecución es un bloque de unos 10 minutos.
- Fase A (Opus): de una a tres tareas por bloque.
- Fase B (Sonnet): más tareas por bloque, porque cada una es más chica y sigue la receta (de dos a cuatro, o más si caben sin pasar de la mitad del contexto). El ritmo sube por bloque, no por frecuencia.
- Límite de bloques: el límite de 24 es probablemente corto para todo el MVP a este ritmo. Se sube con el usuario, no por decisión del agente (ver "Decisiones del usuario").

## Puntos de cambio
El agente se detiene solo en cada punto (estado `PAUSA` en `docs/estado.md`), abre el pull request del hito y espera.

### Punto 1: fin de H3 (Opus a Sonnet)
Antes de pausar, el agente deja:
- `docs/receta-de-extension.md`: cómo añadir un enemigo, un objeto, una habilidad, un tipo de habilidad o estado, un mapa y una escena de texto, cada uno con un ejemplo real y la prueba que corresponde.
- `docs/contrato-de-datos.md` al día.
- Suite de lógica en verde y flujos `Pruebas` y `APK` en verde.

Condiciones para entrar a Sonnet (las revisa el usuario):
1. Pull request de H3 fusionado con "Merge commit".
2. La receta existe y un ejemplo de cada tipo funciona.
3. `Pruebas` en verde en `main`.
4. No quedan preguntas pendientes bloqueantes.

### Punto 2: fin de H7 (Sonnet a Opus)
El agente abre el pull request y pausa. El usuario fusiona, revisa que el recorrido pase de pueblo a mazmorra y vuelve a Opus para H8.

### Regla de regreso anticipado
Si en la fase B ocurre alguno de estos casos, se vuelve a Opus para ese tramo:
- Dos bloques seguidos sin avance.
- `Pruebas` en rojo en dos bloques seguidos.
- El agente propone cambiar las interfaces del motor (eso es trabajo de diseño).

## Cómo se hace el cambio
El usuario escribe en Claude (raíz) "pasa a Sonnet" o "vuelve a Opus". Claude:
1. Cambia el modelo de la tarea programada.
2. Si hace falta, ajusta la frecuencia y el texto de la tarea.
3. Pone el estado del MVP en `EN CURSO` en `docs/estado.md` (en la rama pendiente más reciente) y sube el cambio.
4. Confirma qué quedó configurado.

## Decisiones del usuario
- Límite de bloques: subir de 24 a un valor mayor (propuesta: 50 en total).
- Quitar el código nativo C++ y la `GameActivity` de la plantilla cuando el agente lo pida (exige cambiar `CMakeLists.txt` y `build.gradle.kts`).
