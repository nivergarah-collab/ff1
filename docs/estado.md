# Estado · ff1

Última actualización: 2026-10-01

## Hecho
- Proyecto movido a `Desktop\Android\ff1`.
- Tecnología confirmada: plantilla nativa (Java + C++).
- Estructura estándar creada.
- Flujo de integración continua (`.github/workflows/pruebas.yml`) colocado por el usuario.

## Siguiente
1. Crear el repositorio de ff1 en GitHub (vacío, sin README) y conectarlo.
2. Verificar que el proyecto compila y corre tal cual viene de la plantilla.
3. Definir el modelo del combate ATB (héroe, enemigo, habilidad, barra de tiempo) y escribir sus pruebas.
4. Implementar un combate ATB mínimo, con el interruptor de avance rápido.
5. Decidir cómo se dibujan los menús.
6. Cuando ff1 mini funcione, extraer el molde a `Android/plantilla-proyecto/`.

## Pruebas
Sin ejecutar todavía.

## Notas
- `ExampleUnitTest` y `ExampleInstrumentedTest` son de la plantilla y se reemplazarán.
- Riesgo: la integración continua puede necesitar el NDK y CMake para compilar; por verificar.
- `.github/` es una ruta protegida: el agente no puede escribir en ella, los cambios al flujo los hace el usuario.
