# Plan · MVP de ff1 mini

Lo mantiene el agente. Marcar `[x]` al terminar y anotar aquí cualquier cambio de orden con su motivo. Las tareas se dividen para que cada una quepa en un bloque de trabajo.

## H0 · Preparación
- [x] Comprobar el entorno en la nube: `javac`, JUnit 4 y Hamcrest incluidos con Gradle.
- [x] Crear `scripts/probar-logica.sh`: compila la lógica en Java puro y sus pruebas, las ejecuta e imprime una línea por clase y el detalle solo de los fallos.
- [x] Investigar guías de RPG clásicos, solo ritmo y estructura; resumir en `docs/investigacion-ritmo.md` (sin trama, nombres ni diálogos).
- [x] Decidir cómo se dibujan menús y texto, y registrarlo en `docs/decisiones.md`.
- [x] Definir las interfaces del motor (fuente de contenido, configuración, azar, tiempo, guardado), elegir el formato de datos sin dependencias externas y escribir `docs/contrato-de-datos.md` (ver "Diseño adaptable" en `mision-mvp.md`).
- **Salida:** el script corre una prueba de ejemplo, el resumen de ritmo existe y el contrato de datos está escrito.

## H1 · Núcleo del combate ATB
- [x] Modelo: personaje, enemigo, habilidad, estados.
- [x] Barra de tiempo por ticks simulados.
- [x] Acciones: atacar, magia, objeto, huir. Cálculo de daño.
- [x] Fin del combate y recompensas.
- [x] Interruptor de avance rápido como multiplicador de ticks.
- [x] Los parámetros del combate (velocidad de la barra, daño, avance rápido) vienen de la configuración, no de constantes en el código.
- **Salida:** un combate completo se simula en pruebas, con y sin avance rápido, y cambiando la configuración cambia el resultado.

## H2 · Progresión e inventario
- [x] Experiencia y niveles.
- [x] Inventario, objetos y equipamiento.
- [x] Botín.

## H3 · Mundo
- [x] Mapa por casillas, movimiento y colisiones.
- [x] Encuentros aleatorios que conectan con el combate.

## H3.5 · Primera versión jugable
Hito insertado por el usuario el 2026-10-02 (ver `decisiones.md`): un APK instalable con un recorrido corto (título, mapa, encuentro, combate con menú, vuelta al mapa). Lo hace Opus porque define cómo se dibuja y se maneja todo el juego.
- [x] Escena de dibujo en Java puro (órdenes rectángulo, texto y casilla sobre una rejilla virtual fija) y una clase Android que la traduce a `Canvas` y escala a la pantalla. Pruebas de las órdenes.
- [x] Entrada táctil: cruceta y botones aceptar/cancelar en pantalla, traducidos a una entrada en Java puro. Bucle de juego con la interfaz `Tiempo` y una `MainActivity` nueva con vista propia como actividad de lanzamiento.
- [x] Máquina de pantallas en Java puro, con pruebas: título (Nueva partida; Continuar deshabilitado hasta H7), exploración y combate.
- [x] Exploración: dibujar el mapa de `mapas/campo.json`, mover al jugador con colisiones y encuentros aleatorios que abren el combate.
- [x] Combate con menú (Atacar, Magia, Objeto, Huir), barras de tiempo y de vida, interruptor de avance rápido visible, fin con recompensas y regreso al mapa.
- [x] Quitar el código nativo (rama `chore/quitar-codigo-nativo`), con `Pruebas` y `APK` en verde.
- [x] Dejar en `docs/estado.md` cómo bajar, instalar y probar el APK.

## H3.6 · Menú del grupo
Hito insertado por el usuario el 2026-10-02 (ver `decisiones.md`): el juego necesita el menú de grupo propio de los RPG clásicos. Se abre desde el mapa con un botón y se maneja con la misma cruceta y los botones aceptar/cancelar. Cada sección es una `Pantalla` nueva sobre el patrón de `receta-de-extension.md`; la lógica va en Java puro con pruebas y los datos (héroes, objetos, equipo, habilidades) salen de los JSON, sin números fijos en el código.
- [x] Pila de pantallas: abrir el menú desde la exploración y volver al mapa sin perder la posición, con pruebas. Si hace falta cambiar `Pantalla`, `Juego` o `Escena` de forma incompatible, anotarlo en "Preguntas pendientes" y poner `PAUSA: volver a Opus`.
- [x] Menú principal con las secciones: Objetos, Magia, Equipo, Estado, Formación, Ajustes, Guardar y Salir al título. (Las secciones aún sin pantalla aparecen apagadas; se habilitan al terminar cada tarea. El menú se abre con Cancelar en el mapa; no se añadió un botón nuevo para no tocar `Controles`.)
- [x] Objetos: ver el inventario y usar objetos consumibles fuera de combate sobre un héroe (curar, revivir), con pruebas. (Curar hecho; revivir pendiente: exige que `Acciones` admita héroes caídos como objetivo, ver `estado.md`.)
- [x] Magia: ver las habilidades de cada héroe y lanzar las de curación fuera de combate gastando magia, con pruebas.
- [x] Equipo: ranuras de arma, armadura y accesorio por héroe, equipar y quitar con comparación de estadísticas antes de confirmar, con pruebas (el motor de equipo ya existe en `inventario`).
- [ ] Estado: nivel, experiencia, estadísticas y estados alterados de cada héroe.
- [ ] Formación: cambiar el orden del grupo y que el combate respete el nuevo orden, con pruebas.
- [ ] Ajustes: velocidad del texto y del combate y avance rápido por defecto, guardados en la configuración (en caliente), con pruebas.
- [ ] Guardar: la opción aparece, pero desactivada hasta H7 (ahí se conecta).
- [ ] Actualizar `docs/receta-de-extension.md` (cómo añadir una sección al menú) y `docs/estado.md` ("Cómo probar el APK" con el menú).

## H4 · Narrativa
- [ ] Historia original en `docs/historia.md`, con las mismas etapas de un RPG clásico.
- [ ] Motor de escenas de texto y diálogos.

## H5 · Pueblo y servicios
- [ ] Tienda, posada y personajes con diálogo.

## H6 · Mazmorra y jefe
- [ ] Mapa de la mazmorra, enemigos y jefe.
- [ ] Escena de cierre.

## H7 · Guardado y carga
- [ ] Guardar y cargar partida, con pruebas de ida y vuelta.

## H8 · Integración y limpieza
- [ ] Prueba con un segundo paquete de contenido mínimo: el motor funciona sin cambiar código.
- [ ] Actualizar `docs/contrato-de-datos.md` con lo que quedó.
- [ ] Recorrido completo en emulador (lo verifica el usuario).
- [ ] Refactorización, eliminación de código muerto, balance y README final.

## Deuda técnica
Registrar aquí lo que se deja pendiente de refactorizar o limpiar.
- Combate: los factores estructurales de las fórmulas (defensa / 2 y / 4, el 2 × de la huida y sus límites 5–95 %) son constantes en `Acciones`; pasarlos a la configuración si el balance lo pide (H8).
- Combate: `turnoAutomatico` solo ataca; una IA de enemigos que use habilidades queda para H6 (jefe).
- Limpieza tras H1–H3 hecha en el bloque 6 (helper `PaqueteDelJuego` para pruebas, `Documentos.rango`/`rangoO`/`dentro` para rangos y `CatalogoCombate.leerHabilidad` compartido con los objetos). Queda sin unificar la comprobación de ids repetidos (cada catálogo tiene su mensaje) y los `if` de objetivo; revisar en H8.
- Mundo: el mapa v1 no tiene salidas entre mapas; añadirlas en H5–H6.
- H3.5: los textos de la interfaz (menús, mensajes de combate en `juego.Mensajes`) están en el código; pasarlos a datos si se quiere traducir o reutilizar (H8). Los héroes caídos siguen caídos tras ganar un combate hasta que exista la posada (H5).
