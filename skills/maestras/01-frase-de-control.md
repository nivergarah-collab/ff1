# 01 · Frase de control

**Ámbito:** maestra (vale para todos los proyectos).

## Propósito
Comprobar de un vistazo que el chat no perdió el contexto ni los protocolos. Si la frase no aparece, el agente olvidó las skills y hay que pedirle que las relea.

## Regla
Toda respuesta empieza con esta frase:

> **Nicolas, dentro del proyecto [nombre del proyecto], te menciono...**

y continúa con la respuesta de forma natural.

Cuando el trabajo se hace en la carpeta `Android` (el orquestador general) y no hay un proyecto activo, la frase es:

> **Nicolas, dentro de la raíz, te menciono...**

## Detalles
- Aplica a todas las respuestas, también a las cortas y a las que siguen a una tarea con herramientas.
- `[nombre del proyecto]` es el nombre de la carpeta del proyecto activo.
- Si el usuario cambia de proyecto, la frase cambia en la respuesta siguiente.
- Si el usuario dice "raíz", significa que está en la carpeta `Android` del escritorio.
- Un proyecto puede sobrescribir esta frase solo si lo declara explícitamente (ver `02-lectura-de-contexto.md`).
