# 04 · Constructor de proyecto

**Ámbito:** maestra.

## Propósito
Permitir crear agentes especializados en la raíz y, con una sola instrucción ("inicialízate en la carpeta X"), dejarlos listos para trabajar en un proyecto de forma reproducible.

## Estructura estándar de un proyecto

```
<proyecto>/
├── README.md              ← qué es y cómo se usa (ver 03)
├── CHANGELOG.md           ← historial de cambios
├── skills/
│   └── 00-iniciar.md      ← el constructor del proyecto
├── docs/
│   ├── estado.md          ← dónde quedamos y qué sigue
│   ├── decisiones.md      ← decisiones tomadas y su motivo
│   ├── pruebas.md         ← estrategia de pruebas (ver 05)
│   └── conexiones.md      ← servicios externos que usa el proyecto (ver 07)
└── scripts/               ← verificaciones automáticas (ver 05)
```

## Procedimiento de inicialización
Cuando el usuario diga "inicialízate en la carpeta X":

1. Confirmar que la carpeta existe y que hay acceso a ella; si no hay acceso, pedirlo.
2. Leer las skills maestras (`Android/skills/`).
3. Leer `<proyecto>/skills/00-iniciar.md` y seguirlo, siguiendo el orden de `02-lectura-de-contexto.md`.
4. Si el constructor no existe, avisarlo y ofrecer crearlo a partir de la plantilla de abajo. No continuar suponiendo.
5. Verificar que el proyecto tenga estrategia de pruebas (`docs/pruebas.md`). Si no la tiene, proponerla antes de escribir funcionalidades (ver `05-pruebas-automatizadas.md`).
6. Informar en pocas líneas: proyecto activo, rol que asume el agente, estado actual, qué leyó y qué falta.
7. Desde ese momento, usar la frase de control con el nombre del proyecto (`01-frase-de-control.md`).

## Plantilla de `00-iniciar.md`

```markdown
# Iniciar · <nombre del proyecto>

Sobrescribe: (ninguna, o el archivo maestro que se sobrescribe)

## Rol del agente
Qué especialidad tiene y qué responsabilidad asume en este proyecto.

## Qué leer
Archivos clave, en orden, además del orden estándar.

## Reglas específicas
Convenciones, herramientas, versiones o límites propios del proyecto.

## Cómo se trabaja aquí
Flujo habitual: dónde se escribe código y cómo se avanza.

## Pruebas
Comandos para correr las pruebas rápidas y la suite completa, y qué alcance mínimo se espera.
```

## Cierre de sesión
Al terminar una sesión de trabajo, el agente:
1. Corre las pruebas del alcance trabajado y anota el resultado en una línea (ver `05-pruebas-automatizadas.md`).
2. Actualiza `docs/estado.md` con lo hecho, lo pendiente y lo siguiente.
3. Registra en `docs/decisiones.md` cualquier decisión nueva y su motivo.
4. Añade una entrada en `CHANGELOG.md`.
5. Actualiza el `README.md` solo si cambió algo que allí se describe.
6. Propone el commit según `06-git-y-repositorios.md`, sin ejecutarlo hasta que el usuario confirme.

Así el siguiente agente que se inicialice en el proyecto retoma sin que haya que explicarle nada.
