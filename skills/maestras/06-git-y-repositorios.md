# 06 · Git y repositorios de proyecto

**Ámbito:** maestra. La raíz `Android` **no tiene repositorio propio**: esta skill solo define las reglas generales que cada proyecto aplica a su repositorio.

## Normalización de repositorios
- Un repositorio por proyecto, con la raíz del repositorio en la carpeta del proyecto.
- Nombre del repositorio: el de la carpeta del proyecto, en minúsculas y con guiones.
- Rama principal: `main`. Siempre estable y con las pruebas pasando.
- Archivos mínimos desde el primer commit: `README.md`, `CHANGELOG.md`, `.gitignore`, y la carpeta `skills/` y `docs/` del proyecto.
- Licencia: se pregunta al usuario; no se asume.
- Nunca se versionan: `local.properties`, claves o `*.keystore`, tokens, contraseñas, ni carpetas generadas (`build/`, `.gradle/`, `.idea/` salvo lo que el usuario decida compartir). Si algo sensible ya se subió, el agente lo avisa de inmediato.
- Integración continua en GitHub Actions que corra las pruebas definidas en `05-pruebas-automatizadas.md`.

## Ramas
- Nadie trabaja directo sobre `main`.
- Nombres: `feature/<nombre>`, `fix/<nombre>`, `docs/<nombre>`, `test/<nombre>`, `chore/<nombre>`, en minúsculas y con guiones.
- Ramas cortas, con un solo objetivo, creadas a partir de `main` actualizado.

## Commits
- Pequeños y atómicos: un propósito por commit.
- Mensaje en español, con el formato `tipo(alcance): resumen en imperativo`, de hasta 72 caracteres.
- Tipos: `feat`, `fix`, `test`, `docs`, `refactor`, `build`, `ci`, `chore`.
- Cuerpo opcional, para explicar el motivo del cambio y no el cómo.
- Antes de proponer un commit: pasar las pruebas del alcance (ver `05`) y actualizar `CHANGELOG.md` si el cambio es relevante.
- No se incluyen en un commit archivos ajenos al cambio.

## Push y merge
- Subir una rama: `git push` sin forzar. El `push --force` está prohibido salvo orden explícita del usuario.
- Los cambios entran a `main` mediante pull request, con integración continua en verde.
- Fusión por defecto: *squash* para ramas de funcionalidad, para dejar un commit claro en `main`.
- Tras fusionar, se propone borrar la rama.
- Ante un conflicto, el agente se detiene, lo explica y pregunta; no resuelve a ciegas.
- Versiones: etiquetas `vMAYOR.MENOR.PARCHE`, alineadas con las entradas de `CHANGELOG.md`.

## Confirmaciones obligatorias
El agente pregunta y espera un sí claro antes de:
- Hacer un commit (mostrando archivos incluidos y mensaje propuesto).
- Hacer push, crear una rama remota, abrir o fusionar un pull request.
- Crear etiquetas o publicar versiones.
- Borrar ramas, etiquetas o archivos versionados.
- Cualquier operación destructiva o que reescriba el historial (`reset --hard`, `rebase`, `push --force`, `clean`).
- Cambiar la configuración del repositorio, de Git o de GitHub.

La aprobación vale solo para esa acción; no se extiende a las siguientes.

## Ejecución
El agente ejecuta comandos de Git solo si tiene una herramienta de terminal en la sesión y el usuario confirmó. Si no la tiene, prepara el mensaje de commit y los comandos exactos para que el usuario los ejecute.
