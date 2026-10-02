# Trabajo autónomo · ff1

Sobrescribe: 06-git-y-repositorios.md (solo las confirmaciones de commit y push en ramas de trabajo, según la sección "Autorización previa")

## Propósito
Guiar a un agente que trabaja por bloques, sin supervisión, para construir el MVP descrito en `docs/mision-mvp.md` siguiendo `docs/plan.md`.

## Entorno
- El agente trabaja en un clon del repositorio en la nube, con terminal. No usa la carpeta del computador del usuario.
- La nube no tiene acceso a Maven, Google ni Gradle: no puede compilar la app Android. Sí compila y prueba la lógica en Java puro con `javac` y el JUnit 4 incluido con Gradle (`scripts/probar-logica.sh`).
- La compilación Android y la suite completa corren en GitHub Actions. El agente lee solo el resumen del resultado.
- El usuario recibe el trabajo con `git pull` y prueba en Android Studio.

## Autorización previa
Sin preguntar, el agente puede:
- Crear ramas `feature/<hito>-<tema>` y `fix/<tema>`.
- Hacer commits atómicos en ellas, con el formato de `06`.
- Hacer push de esas ramas, sin forzar.

Sigue requiriendo confirmación (se deja anotado en `docs/estado.md`, en "Preguntas pendientes", y el agente continúa con otra tarea independiente):
- Merge a `main`, abrir o cerrar pull requests.
- Borrar ramas, etiquetas o archivos versionados.
- `push --force` y cualquier reescritura del historial.
- Modificar `build.gradle.kts`, `settings.gradle.kts`, `gradle/`, `CMakeLists.txt`, `.github/` o `local.properties`, y añadir dependencias.
- Publicar versiones.

## Ciclo de un bloque de trabajo
1. Inicializarse según `04` y leer en el orden de `02`, más `docs/mision-mvp.md` y `docs/plan.md`.
2. Verificar el entorno y las conexiones (`07`) con comprobaciones ligeras.
3. Elegir la siguiente tarea pendiente del plan; en un bloque, una a tres tareas pequeñas.
4. Crear o continuar la rama de la tarea.
5. Escribir la prueba, implementar, correr las pruebas del alcance y hacer commit.
6. Marcar la tarea en `docs/plan.md`.
7. No empezar una tarea nueva si el contexto ya va por la mitad: cerrar limpio.
8. Al cerrar el bloque: correr la suite de lógica completa, hacer push, mirar el resultado de GitHub Actions, y actualizar `docs/estado.md` (hecho, siguiente, ramas listas para revisar, preguntas pendientes), `CHANGELOG.md` y `docs/decisiones.md`.

## Planificación
- `docs/plan.md` es la ruta. El agente puede reordenar tareas o dividirlas, anotando el motivo.
- Cada dos hitos terminados, dedica un bloque a refactorizar y limpiar: código muerto, duplicación, nombres, pruebas redundantes y documentación. Lo que no alcance queda en "Deuda técnica" del plan.

## Eficiencia de tokens
- Buscar antes de leer: usar búsquedas por texto y leer solo los fragmentos necesarios.
- No releer lo que ya conoce ni pegar registros completos.
- Investigar guías una sola vez (H0), guardar un resumen corto y no volver a ellas.
- Commits pequeños y mensajes breves.

## Eficiencia en las pruebas
- Durante el trabajo se corre solo la clase o el paquete afectado.
- El script de pruebas imprime una línea por clase y el detalle solo de lo que falla.
- La suite completa se corre al cerrar el bloque; la compilación Android y lo demás, en GitHub Actions.
- Pruebas deterministas (ticks simulados, sin reloj ni azar sin control) y sin duplicados.

## Cuándo detenerse y avisar
- Una prueba falla tres veces por la misma causa: diagnosticar, dejarlo escrito en el estado y pasar a otra tarea.
- Hay un conflicto de merge o se necesita una acción que exige confirmación.
- Una tarea cambia el alcance de la misión.

## Control de parada
La sección `## Control` de `docs/estado.md` (al principio del archivo) guía el trabajo:

```
## Control
- Estado del MVP: EN CURSO
- Bloques ejecutados: 0 de 24
- Bloques seguidos sin avance: 0
```

El estado puede ser `EN CURSO`, `LISTO PARA REVISIÓN` o `DETENIDO: <motivo>`.

1. **Lo primero de cada bloque**, tras clonar el repositorio, es leer solo esa sección. Si no existe, se crea con los valores de arriba.
2. Si el estado no es `EN CURSO`, el bloque termina de inmediato con un resumen de una línea, sin leer nada más ni cambiar nada. Así las sesiones que sigan llegando casi no gastan tokens.
3. Si los bloques ejecutados ya son 24 o más, el estado pasa a `DETENIDO: límite de bloques`, se escribe y el bloque termina.
4. En cada bloque que sigue: sumar 1 a "Bloques ejecutados"; sumar 1 a "sin avance" si no se marcó ninguna tarea del plan, o ponerlo en 0 si se marcó alguna.
5. Si "sin avance" llega a 3, o GitHub Actions queda en rojo en 2 bloques seguidos sin poder corregirlo, el estado pasa a `DETENIDO: <motivo>` y se explica en el resumen.

## Cierre del MVP
Cuando se cumple la definición de terminado de `docs/mision-mvp.md`, el agente pone el estado en `LISTO PARA REVISIÓN`, escribe en `docs/estado.md` qué debe revisar el usuario, deja el README final y termina. No inventa funcionalidades nuevas. Pausar la tarea programada es decisión del usuario; mientras siga activa, las sesiones salen de inmediato según el punto 2.

## Contenido y derechos
Ver `docs/mision-mvp.md`: guías solo para ritmo y estructura; trama, personajes, nombres y diálogos originales.
