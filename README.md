# ff1

Juego de rol por turnos para Android, inspirado en el género de los Final Fantasy, con historia, personajes y arte originales. Es un proyecto de prueba de la integración entre Claude, Android Studio y GitHub.

## Estado
Plantilla nativa de Android Studio (GameActivity) sin código de juego propio todavía. Primera meta (ff1 mini): un combate con barra de tiempo (ATB) funcional, con héroes, enemigos, habilidades y menú. Detalle en `docs/estado.md`.

## Cómo ejecutarlo
1. Abrir la carpeta `ff1` en Android Studio y sincronizar Gradle.
2. Ejecutar el módulo `app` en un dispositivo o emulador con Android 11 (API 30) o superior.
3. Se necesitan el NDK y CMake 3.22.1, que Android Studio instala desde el SDK Manager.

Los requisitos salen de `app/build.gradle.kts`; la ejecución aún no fue verificada.

También se puede instalar un APK de depuración sin Android Studio: en GitHub, pestaña Actions, abrir la ejecución `APK` de la rama y bajar `ff1-debug-apk` desde "Artifacts".

## Estructura
- `app/src/main/java/com/example/ff1/`: código Java; `MainActivity` es la actividad del juego.
- `app/src/main/cpp/`: dibujo nativo en C++ (`Renderer`, `Shader`, `Model`), compilado con CMake.
- `app/src/test/`: pruebas unitarias en JVM.
- `app/src/androidTest/`: pruebas instrumentadas (requieren dispositivo).
- `skills/`: constructor y reglas específicas del proyecto.
- `docs/`: estado, decisiones y estrategia de pruebas.
- `scripts/`: verificaciones automáticas.
- `.github/workflows/`: integración continua y construcción del APK.

## Decisiones importantes
- Se mantiene la plantilla nativa (Java + C++), sin Kotlin ni Compose.
- La lógica de combate va en clases Java puras, para probarla en JVM.
- El juego incluye un interruptor de avance rápido.
- Todo el contenido (nombres, personajes, arte, música) es original.

Detalle en `docs/decisiones.md`.

## Documentación relacionada
- `docs/estado.md`
- `docs/decisiones.md`
- `docs/pruebas.md`
- `skills/00-iniciar.md`
- `CHANGELOG.md`
