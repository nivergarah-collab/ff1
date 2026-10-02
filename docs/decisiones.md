# Decisiones · ff1

Más reciente primero.

## 2026-10-02
- **Pull requests del agente:** el agente puede abrir pull requests (uno por hito terminado o al detenerse con trabajo sin fusionar), pero nunca fusionarlos, aprobarlos ni cerrarlos. Si no tiene herramienta para abrirlos, deja el enlace `pull/new/<rama>` en "Preguntas pendientes". Sus ramas se encadenan, así que el usuario fusiona con "Merge commit" (no "Squash") y en orden. Sobrescribe de `06` solo la confirmación para abrir pull requests y la preferencia de fusión. Sin verificar: el permiso de la app de GitHub para pull requests y que la sesión en la nube tenga herramienta para abrirlos.

## 2026-10-01
- **APK descargable:** el APK de depuración lo construye GitHub Actions (flujo `APK`) y queda como archivo descargable por rama; el agente no puede construirlo ni modificar los flujos. Los flujos corren también en `feature/**` y `fix/**`, para que las ramas del agente tengan pruebas sin esperar a un pull request. Solo el flujo `Pruebas` cuenta para la regla de parada. La versión firmada de lanzamiento queda fuera del MVP.
- **Diseño adaptable:** el motor se separa del contenido y de la configuración, que entran por interfaces intercambiables (archivos locales y memoria en el MVP; API o editor después). Parámetros ajustables en caliente, datos versionados y validados, extensión por registro y contrato de datos documentado. Se hace para poder extraer después un molde del motor reutilizable en otros juegos, distinto del molde de proyectos de `Android/plantilla-proyecto/`. La API remota y la interfaz de edición quedan fuera del MVP.
- **Definición de hecho y límites:** el MVP queda "listo para revisión" al cumplir los ocho criterios de `mision-mvp.md`, incluida una prueba de recorrido completo. El agente se detiene también a los 24 bloques, tras 3 bloques sin avance o con 2 bloques seguidos de GitHub Actions en rojo. El control vive en la sección `## Control` de `estado.md`.
- **Modo de ejecución:** sesiones programadas por bloques. Cada una arranca desde cero, retoma desde `docs/estado.md`, trabaja un bloque, hace commit y deja el estado escrito. El agente trabaja en un clon del repositorio en la nube.
- **Historia:** mismas etapas que un RPG clásico, con trama, personajes y nombres originales. Las guías se estudian solo para ritmo y estructura; las cinemáticas son escenas de texto.
- **Autorización previa al agente:** commit y push en ramas `feature/*` y `fix/*` sin preguntar. Siguen pidiendo confirmación el merge a `main`, los borrados, el `push --force` y los cambios de Gradle, CMake o CI. Sobrescribe de forma parcial `06-git-y-repositorios.md`.
- **Tecnología:** mantener la plantilla nativa (Java + C++), sin Kotlin ni Compose. Se evaluaron rehacer con Compose y convertir el proyecto; el usuario eligió conservar la plantilla.
- **Alcance de la primera versión:** ff1 mini. Se prioriza reproducir la mecánica y el estilo del género, no la duración del juego: un combate funcional con héroes, enemigos, habilidades y menú.
- **Estilo de combate:** barra de tiempo (ATB). La barra avanza por «ticks» simulados y no por reloj real, para que las pruebas sean deterministas.
- **Avance rápido (requisito):** el juego debe tener un interruptor de avance rápido que acelere el combate (barra de tiempo y animaciones) sin cambiar las reglas. Se implementa como multiplicador de ticks y debe tener pruebas.
- **Molde de proyectos:** se hará después de ff1 mini, fuera de este repositorio, en `Android/plantilla-proyecto/`, extrayendo la estructura que haya funcionado aquí.
- **Lógica de combate:** en clases Java puras (`com.example.ff1.combate`) para poder probarla en JVM, sin depender de Android.
- **Contenido:** nombres, personajes, arte y música originales; el proyecto se inspira en el género, no en la franquicia.
- **Repositorio:** propio de ff1. Lo crea el usuario en GitHub, vacío y sin README.
