# 05 · Pruebas automatizadas

**Ámbito:** maestra. Todo agente inicializado en un proyecto debe conocer y aplicar esta skill.

## Propósito
Todo proyecto debe tener pruebas automatizadas de integración y de funcionamiento desde el inicio. Las pruebas deben cubrir también los protocolos del espacio de trabajo y ser eficientes para gastar los mínimos tokens posibles.

## Reglas generales
- Un proyecto sin estrategia de pruebas no está listo para recibir funcionalidades. Si falta `docs/pruebas.md`, el agente propone una antes de escribir código.
- Ninguna funcionalidad se da por terminada sin una prueba que la cubra, o sin una justificación escrita de por qué no se puede probar.
- Cada error corregido deja una prueba de regresión.
- Las pruebas son deterministas: sin dependencias de hora, red o azar sin control. Una prueba inestable se arregla o se elimina, no se ignora.

## Qué se prueba, de más barato a más caro
1. **Verificación de estructura y protocolos** (`scripts/`): comprueba que existan `README.md`, `CHANGELOG.md`, `skills/00-iniciar.md`, `docs/estado.md`, `docs/decisiones.md` y `docs/pruebas.md`, y que el README tenga las secciones de `03-readme-por-proyecto.md`. Es un script que no gasta tokens al correr.
2. **Pruebas unitarias** (`app/src/test`): lógica pura en JVM, rápidas y numerosas. Aquí va la mayor parte de la cobertura.
3. **Pruebas de integración** (`app/src/test` con dobles o bases en memoria): ViewModel con repositorios falsos, persistencia, flujos entre capas.
4. **Pruebas instrumentadas o de interfaz** (`app/src/androidTest`): pocas, solo para los flujos críticos. Requieren emulador o dispositivo, por eso las ejecuta el usuario o la integración continua, no el agente en cada cambio.
5. **Integración continua** (GitHub Actions): corre las verificaciones 1 a 3 y el análisis estático en cada push y pull request.

## Uso eficiente de tokens
- Durante el desarrollo se ejecuta solo la clase o el módulo afectado, no toda la suite. La suite completa se corre al cerrar la sesión y en integración continua.
- El agente lee **solo el resumen**: aprobadas, falladas y las líneas de cada fallo. Nunca pega ni lee registros completos de Gradle.
- Los scripts de verificación imprimen una línea por comprobación (`OK` o `FALLA: motivo`).
- No se abren los archivos de prueba salvo que fallen o haya que modificarlos.
- Si todo pasa, el resultado se registra en una línea en `docs/estado.md`, sin descripción.
- Las pruebas se escriben pequeñas y con nombre descriptivo, para que un fallo se entienda sin leer el código.

## Documento `docs/pruebas.md`
Cada proyecto lo mantiene con:
- Comando para correr las pruebas rápidas y la suite completa.
- Qué cubre cada nivel y qué queda fuera.
- Cómo se ejecutan las pruebas instrumentadas y dónde se corre la integración continua.
- Último resultado conocido.

## Cierre de sesión
Antes de cerrar, el agente corre las pruebas del alcance trabajado, anota el resultado y, si algo falla, lo deja escrito en `docs/estado.md` como pendiente.
