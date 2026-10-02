# 02 · Lectura de contexto

**Ámbito:** maestra.

## Propósito
Definir qué información lee el agente al empezar, en qué orden, y cómo se resuelven los conflictos entre las skills maestras y las de un proyecto.

## Orden de lectura
1. **Skills maestras:** todos los `.md` de `Android/skills/`, en orden numérico.
2. **Constructor del proyecto:** `<proyecto>/skills/00-iniciar.md` (ver `04-constructor-de-proyecto.md`).
3. **Resto de skills del proyecto:** `<proyecto>/skills/*.md`.
4. **README del proyecto:** `<proyecto>/README.md`.
5. **Estado y pruebas:** `<proyecto>/docs/estado.md` y `<proyecto>/docs/pruebas.md`.
6. **Historial reciente:** las últimas entradas de `<proyecto>/CHANGELOG.md`.

Si no hay proyecto activo, solo se aplican los puntos 1 y, de ser útil, el `README.md` y `CHANGELOG.md` de la raíz.

## Jerarquía de skills
- Las skills del proyecto **extienden** a las maestras: se suman a ellas.
- Una skill de proyecto **sobrescribe** una maestra solo si lo declara de forma explícita en su encabezado, con una línea como:
  `Sobrescribe: 01-frase-de-control.md`
- Todo lo que la skill de proyecto no mencione sigue valiendo como está en la maestra.
- Si hay una contradicción sin declaración de sobrescritura, gana la maestra y el agente avisa del conflicto.

## Reglas de lectura
- Lee solo lo necesario para la tarea; no recorre carpetas enteras sin motivo.
- Si falta un archivo esperado, **avisa** en vez de suponer su contenido.
- Nunca inventa el contenido de un archivo que no abrió.
- Si un archivo parece desactualizado respecto de lo que ve en el proyecto, lo señala.
- Antes de afirmar algo sobre el estado del proyecto, se apoya en `docs/estado.md` y `CHANGELOG.md`, no en la memoria del chat.
