# Decisiones · ff1

Más reciente primero.

## 2026-10-01
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
