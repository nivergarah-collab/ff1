# Changelog · ff1

Formato: más reciente primero, con fecha en `AAAA-MM-DD`.

## 2026-10-02

### Añadido (bloque 8, H4 narrativa)
- `docs/historia.md`: historia original de "Crónica de la Cantera" (Pozaluz, la cantera hundida y el Soterrado) con escenas por etapa.
- Escenas de texto: documento `escena` v1 (`guion.Guion`, marcadores `{heroe:<clase>}`), `PantallaEscena` y escena `apertura` enganchada a la partida nueva con el campo opcional `introduccion` de `inicio.json`. 6 pruebas nuevas.

### Añadido (bloque 8, H3.6 menú del grupo)
- Pila de pantallas en `Juego` (`apilar`, `cerrar`, `profundidad`) y menú del grupo (`PantallaMenu`) que se abre desde el mapa con Cancelar: Objetos, Magia, Equipo, Estado, Formación, Ajustes, Guardar (apagado hasta H7) y Salir al título (con confirmación).
- Objetos y magia de curación fuera de combate (`Partida.usarObjeto`, `usarHabilidad`; si no hay efecto no se gasta nada); tipo de habilidad `revivir` y objeto "Pluma de alba", solo fuera de combate (`TipoHabilidad.actuaSobreCaidos`, `Combatiente.revivir`).
- Equipo con comparación de estadísticas (`Partida.equipar`, `quitarEquipo`, `Heroe.estadisticasCon`), ficha de estado, formación (`Partida.intercambiarHeroes`, el combate usa ese orden) y ajustes en caliente (`juego.msMensaje`, `juego.rapidoAlEmpezar`, `combate.ticksPorPaso`).
- El grupo empieza con una pieza de cada ranura y una Pluma de alba en el inventario (`inicio.json`).


### Añadido (usuario)
- Hito H3.6 "Menú del grupo" en `docs/plan.md` (objetos, magia, equipo, estado, formación, ajustes, guardar y salir) antes de H4. Tarea reanudada con Sonnet.

### Añadido (bloque 7, H3.5 primera versión jugable)
- Escena de dibujo en Java puro (`dibujo.Escena`, `Escalado`) y traductor único a `Canvas` (`android.LienzoCanvas`).
- Controles táctiles en pantalla, entrada con repetición y bucle de paso fijo (`entrada`).
- Máquina de pantallas (`juego`): título, exploración del mapa con encuentros, combate con menú (Atacar, Magia, Objeto, Huir), barras de vida y tiempo, avance rápido visible, recompensas y vuelta al mapa; pantalla de error de datos.
- `MainActivity` nueva con vista propia; documento `inicio.json`, `configuracion.json` del juego y color opcional por casilla. 26 pruebas nuevas.

### Quitado (bloque 7)
- Renderizador C++ y `GameActivity` de la plantilla (`app/src/main/cpp/`, `externalNativeBuild`, `prefab`, `games-activity`), rama `chore/quitar-codigo-nativo`.

### Cambiado (bloque 6)
- Limpieza tras H1–H3: helper de pruebas `PaqueteDelJuego`; validación de rangos común en `motor.datos.Documentos`; lectura de efecto/estado compartida entre habilidades y objetos (los objetos aceptan `objetivo` `si-mismo`). 5 pruebas nuevas, sin cambios de comportamiento salvo lo anterior.

### Añadido (bloque 5)
- Mundo (H3 terminado): mapa por casillas con leyenda, colisiones y posición inicial validada (`mundo.Mapa`, `Explorador`), primer mapa `mapas/campo.json`; encuentros aleatorios por zona (`encuentros.json`, `mundo.TablaEncuentros`, `Encuentros`) con `mundo.pasosMinimos` y `mundo.pasosMaximos` en la configuración, que crean los enemigos del combate. 15 pruebas nuevas.
- `docs/receta-de-extension.md`: cómo añadir enemigos, objetos, habilidades, tipos de habilidad y estados, mapas y escenas, con ejemplos reales y la prueba que toca.

### Añadido (bloque 4)
- Combate (H1 terminado): acciones atacar, habilidad, objeto y huir con cálculo de daño físico, mágico y curación; tipos de habilidad y estados registrados en `ReglasCombate`; fin del combate, turnos automáticos y recompensas; interruptor de avance rápido por pasos. Parámetros nuevos `combate.*` en la configuración.
- Progresión (H2): experiencia por nivel y crecimiento por clase (`progresion.json`), héroe persistente con vida y magia entre combates, reparto de experiencia.
- Inventario (H2): objetos consumibles, equipo con ranuras y bonos, objetos clave (`objetos.json`), inventario con máximo por objeto y botín por enemigo (`botin.json`). 57 pruebas nuevas.

### Cambiado
- Límite de bloques 24 a 50, bloques de hasta unos 25 minutos y autorización para quitar el código nativo de la plantilla.

### Añadido (bloque 3)
- Combate (H1): modelo de combatiente, habilidad y estados en `com.example.ff1.combate`, leído y validado desde `combatientes.json` y `habilidades.json` (documentos v1 en `docs/contrato-de-datos.md`).
- Barra de tiempo (ATB) por ticks simulados, con `combate.velocidadBarra` y `combate.cargaLlena` en la configuración, ajustables en caliente.
- Primer paquete de contenido original en `app/src/main/assets/contenido/`. 32 pruebas nuevas.

### Añadido
- Motor (H0): `motor.datos` (lector y escritor JSON en Java puro, errores con ruta del campo, tipo y versión de documentos), `motor.fuentes` (interfaces de contenido, azar, tiempo y guardado, con implementaciones de memoria y archivos) y `motor.config` (configuración validada y reemplazable en caliente, registro de extensiones). 39 pruebas nuevas.
- `docs/contrato-de-datos.md`: formato, reglas comunes, interfaces del motor y documento `configuracion` v1.

### Decisiones
- Menús, texto y mapa se dibujan con `Canvas` en Java desde una escena de dibujo probada en JVM; el renderizador C++ queda sin uso.
- Formato de datos: JSON con lector propio, sin dependencias.

### Añadido (bloque 1)
- `scripts/probar-logica.sh` y `scripts/EjecutorLogica.java`: compilan y ejecutan la lógica en Java puro con JUnit 4, una línea por clase y detalle solo de los fallos (H0).
- `docs/investigacion-ritmo.md`: resumen de ritmo y estructura de RPG clásicos, sin contenido de terceros (H0).
- El agente autónomo puede abrir pull requests (sin fusionarlos ni cerrarlos), con reglas para ramas encadenadas y fusión con "Merge commit". Lista "Ramas y pull requests pendientes" en `docs/estado.md`.

## 2026-10-01

### Añadido
- Instrucciones para el APK descargable (flujo `APK`) y disparadores del flujo `Pruebas` ampliados a las ramas de trabajo. Los archivos de `.github/workflows/` los coloca el usuario.
- Requisito de diseño adaptable (motor separado del contenido, fuentes intercambiables, configuración en caliente, contrato de datos) en la misión, el plan y las reglas del proyecto.
- Definición de terminado medible, límites de parada y sección `## Control` en `docs/estado.md`, para que el agente se detenga solo.
- `docs/mision-mvp.md`, `docs/plan.md` y `skills/01-trabajo-autonomo.md`: misión, ruta por hitos y reglas para un agente que trabaja por bloques.
- `docs/conexiones.md`.
- Estructura estándar del proyecto según las skills maestras: `README.md`, `CHANGELOG.md`, `skills/00-iniciar.md`, `docs/estado.md`, `docs/decisiones.md`, `docs/pruebas.md`.
- `scripts/verificar-estructura.ps1`: comprueba que la estructura estándar exista.
- `.github/workflows/pruebas.yml`: integración continua con la verificación de estructura y las pruebas unitarias. Lo colocó el usuario, porque esa ruta está protegida para el agente. Sin verificar en GitHub todavía.

### Decisiones
- Se mantiene la plantilla nativa de Android Studio (Java + C++).
- ff1 es una versión mini: misma mecánica y estilo del género, sin buscar la duración de un juego completo.
- El combate usa barra de tiempo (ATB), simulada por ticks para que las pruebas sean deterministas.
- El molde de proyectos se hará después, fuera de este repositorio.
- El juego debe incluir un interruptor de avance rápido.
