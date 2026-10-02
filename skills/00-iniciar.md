# Iniciar · ff1

Sobrescribe: ninguna

## Rol del agente
Desarrollador Android del juego ff1: lógica de juego por turnos probada con pruebas automáticas y mantenimiento de la plantilla nativa (Java + C++).

## Qué leer
Además del orden estándar de `02-lectura-de-contexto.md`:
- `app/build.gradle.kts`, solo si la tarea toca la configuración.
- Los archivos que se vayan a modificar.

No leer `app/build/`, `app/.cxx/`, `.gradle/` ni `app/src/main/cpp/` completa, salvo que la tarea sea de dibujo nativo.

## Reglas específicas
- Contenido original: no usar nombres, personajes, música ni arte de Final Fantasy ni de otras franquicias.
- Java para la lógica y la actividad; C++ solo para el dibujo nativo. No añadir Kotlin ni Compose sin registrar antes la decisión en `docs/decisiones.md`.
- La lógica de combate va en `com.example.ff1.combate`, en clases Java puras, sin dependencias de Android, para probarlas en JVM.
- Confirmar con el usuario antes de modificar `build.gradle.kts`, `settings.gradle.kts`, `gradle/` o `CMakeLists.txt`. `local.properties` no se toca ni se versiona.
- Datos de la plantilla: `minSdk` 30, `compileSdk` y `targetSdk` 37, compatibilidad Java 11.
- El juego debe tener un interruptor de avance rápido (ver `docs/decisiones.md`), con pruebas.
- Repositorio propio de ff1, con las reglas de `06-git-y-repositorios.md`.

## Cómo se trabaja aquí
1. Leer `docs/estado.md` y elegir un objetivo pequeño.
2. Escribir primero la prueba y luego la lógica.
3. Correr solo la prueba afectada.
4. Al cerrar, seguir el cierre de sesión de `04-constructor-de-proyecto.md`.

## Pruebas
Comandos y alcance en `docs/pruebas.md`.
