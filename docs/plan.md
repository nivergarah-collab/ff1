# Plan · MVP de ff1 mini

Lo mantiene el agente. Marcar `[x]` al terminar y anotar aquí cualquier cambio de orden con su motivo. Las tareas se dividen para que cada una quepa en un bloque de trabajo.

## H0 · Preparación
- [ ] Comprobar el entorno en la nube: `javac`, JUnit 4 y Hamcrest incluidos con Gradle.
- [ ] Crear `scripts/probar-logica.sh`: compila la lógica en Java puro y sus pruebas, las ejecuta e imprime una línea por clase y el detalle solo de los fallos.
- [ ] Investigar guías de RPG clásicos, solo ritmo y estructura; resumir en `docs/investigacion-ritmo.md` (sin trama, nombres ni diálogos).
- [ ] Decidir cómo se dibujan menús y texto, y registrarlo en `docs/decisiones.md`.
- **Salida:** el script corre una prueba de ejemplo y el resumen de ritmo existe.

## H1 · Núcleo del combate ATB
- [ ] Modelo: personaje, enemigo, habilidad, estados.
- [ ] Barra de tiempo por ticks simulados.
- [ ] Acciones: atacar, magia, objeto, huir. Cálculo de daño.
- [ ] Fin del combate y recompensas.
- [ ] Interruptor de avance rápido como multiplicador de ticks.
- **Salida:** un combate completo se simula en pruebas, con y sin avance rápido.

## H2 · Progresión e inventario
- [ ] Experiencia y niveles.
- [ ] Inventario, objetos y equipamiento.
- [ ] Botín.

## H3 · Mundo
- [ ] Mapa por casillas, movimiento y colisiones.
- [ ] Encuentros aleatorios que conectan con el combate.

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
- [ ] Recorrido completo en emulador (lo verifica el usuario).
- [ ] Refactorización, eliminación de código muerto, balance y README final.

## Deuda técnica
Registrar aquí lo que se deja pendiente de refactorizar o limpiar.
