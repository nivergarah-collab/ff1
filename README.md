# ff1 · Crónica de la Cantera

Juego de rol por turnos para Android al estilo de los RPG clásicos, con historia, personajes, nombres y arte originales. El grupo de Pozaluz baja a la Cantera Hundida para enfrentarse al Soterrado. Es también un proyecto de prueba de la integración entre Claude, Android Studio y GitHub.

## Estado
MVP completo, listo para revisión: título (nueva partida y continuar), elección de los nombres del grupo, escenas de texto, pueblo con tienda, posada y vecinos, mapa exterior con encuentros aleatorios, mazmorra con jefe, combate con barra de tiempo (atacar, magia, objeto y huir), experiencia, niveles, botín, inventario y equipo, menú del grupo, ajustes en caliente, interruptor de avance rápido, escena de cierre y guardado. Todo se dibuja con `Canvas` en Java. Detalle y qué revisar en `docs/estado.md`.

## Cómo jugarlo
- **APK sin Android Studio:** en GitHub, pestaña Actions → ejecución `APK` de la rama → "Artifacts" → `ff1-debug-apk`. Requiere Android 11 o superior.
- **Desde Android Studio:** abrir la carpeta `ff1`, sincronizar Gradle y ejecutar el módulo `app` en un dispositivo o emulador con API 30 o superior.

Controles: la cruceta mueve, "Aceptar" confirma y habla, "Cancelar" vuelve o abre el menú del grupo en el mapa, "Rápido" enciende el avance rápido. En emulador también sirven las flechas, Intro/Z, X/Escape y F. Los pasos para probar un recorrido completo están en `docs/estado.md`.

## Pruebas
`scripts/probar-logica.sh` compila la lógica en Java puro y corre la suite JUnit en la JVM (sin Android). Incluye una partida simulada de principio a fin (`RecorridoCompletoTest`) y otra con un segundo paquete de contenido (`PaqueteMinimoTest`). GitHub Actions corre `Pruebas` y `APK` en cada push. Más en `docs/pruebas.md`.

## Diseño: motor y contenido separados
- El motor (reglas, pantallas, guardado) es genérico; el juego concreto vive en datos JSON en `app/src/main/assets/contenido/`: héroes, enemigos, habilidades, objetos, botín, mapas, encuentros, servicios del pueblo, escenas, configuración e inicio. Cambiar de juego es cambiar el paquete: `app/src/test/resources/contenido-minimo/` ("El faro de la ensenada") funciona con el mismo código.
- El contenido y la configuración llegan por interfaces intercambiables (`motor.fuentes`): archivos, memoria y, en el futuro, una API o un editor.
- Los parámetros de balance y ritmo viven en una configuración validada que se puede cambiar en caliente.
- Cada documento lleva tipo y versión y se valida; un dato inválido muestra un error claro en lugar de cerrar el juego.
- Los tipos de habilidad, estado y lugar se añaden por registro.

Contrato de cada documento en `docs/contrato-de-datos.md`; cómo añadir contenido o tipos nuevos en `docs/receta-de-extension.md`.

## Estructura
- `app/src/main/java/com/example/ff1/`: lógica en Java puro (`motor`, `combate`, `progresion`, `inventario`, `mundo`, `pueblo`, `guion`, `dibujo`, `entrada`, `juego`) y la capa Android (`MainActivity` y el paquete `android`: vista, `Canvas`, assets y almacén de archivos).
- `app/src/main/assets/contenido/`: paquete de contenido del juego.
- `app/src/test/`: pruebas unitarias en JVM y el paquete de contenido mínimo.
- `docs/`: misión, plan, estado, decisiones, historia, contrato de datos, receta de extensión y pruebas.
- `skills/`: reglas del proyecto y del trabajo autónomo del agente.
- `scripts/`: suite de lógica.
- `.github/workflows/`: integración continua y construcción del APK.

## Decisiones importantes
- Todo en Java, sin Kotlin ni Compose ni dependencias externas; el código C++ de la plantilla se quitó.
- Formato de datos JSON con lector y escritor propios.
- Contenido original: nada de nombres, personajes, tramas, diálogos, música ni arte de otras franquicias.
- Limitaciones aceptadas del MVP: una ranura de guardado, no se guarda dentro de un combate, solo el jefe usa habilidades, textos fijos de la interfaz en el código.

Detalle en `docs/decisiones.md`; historial en `CHANGELOG.md`.
