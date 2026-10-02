# 03 · README por proyecto

**Ámbito:** maestra.

## Propósito
Que cada proyecto tenga un `README.md` claro y al día, porque es la primera fuente de contexto para cualquier agente (ver `02-lectura-de-contexto.md`).

## Ubicación
`<proyecto>/README.md`, en la raíz del proyecto.

## Plantilla

```markdown
# Nombre del proyecto

Una o dos frases: qué es y para qué sirve.

## Estado
Etapa actual (idea / en desarrollo / estable) y qué se está haciendo ahora.

## Cómo ejecutarlo
Pasos concretos para compilar o correr el proyecto, con versiones relevantes (Android Studio, JDK, Gradle, SDK).

## Estructura
Mapa breve de carpetas y archivos importantes y para qué sirve cada uno.

## Decisiones importantes
Decisiones técnicas o de diseño que no se deben deshacer sin pensarlo. Detalle en `docs/decisiones.md`.

## Documentación relacionada
Enlaces a `docs/estado.md`, `CHANGELOG.md` y a las skills específicas del proyecto.
```

## Reglas de escritura
- Escribir en español, con frases cortas y sin relleno.
- Describir lo que **existe hoy**, no lo que se planea; lo planeado va en `docs/estado.md`.
- No incluir claves, contraseñas ni rutas con datos personales.
- Usar comandos reales y verificados; si algo no se probó, decirlo.

## Cuándo actualizarlo
- Al cerrar una sesión de trabajo en la que cambió algo relevante.
- Al cambiar la estructura de carpetas, la forma de ejecutar el proyecto o una decisión importante.
- Cada actualización relevante deja una entrada en `CHANGELOG.md`.
