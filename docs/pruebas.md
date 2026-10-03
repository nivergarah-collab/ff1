# Pruebas · ff1

Estrategia según `05-pruebas-automatizadas.md`. Los comandos aún no fueron verificados.

## Niveles
| Nivel | Dónde | Quién lo corre |
|---|---|---|
| Estructura | `scripts/verificar-estructura.ps1` | Agente e integración continua |
| Unitarias | `app/src/test/` | Agente e integración continua |
| Integración (con dobles) | `app/src/test/` | Agente e integración continua |
| Instrumentadas | `app/src/androidTest/` | Usuario (requiere dispositivo) |

Prioridad de cobertura: sistema de combate (daño, orden de turnos, estados, fin del combate).

## Comandos (Windows, desde la raíz de ff1)
- Estructura: `.\scripts\verificar-estructura.ps1`
- Una clase o paquete: `.\gradlew.bat testDebugUnitTest --tests "com.example.ff1.combate.*" --console=plain -q`
- Suite unitaria completa: `.\gradlew.bat testDebugUnitTest --console=plain -q`
- Instrumentadas: `.\gradlew.bat connectedDebugAndroidTest`

## Lectura eficiente de resultados
Con `-q` Gradle solo imprime los fallos. Si hay fallos, leer únicamente las etiquetas `<failure` de los XML en `app/build/test-results/testDebugUnitTest/`, no el reporte HTML ni el registro completo.

## Ciclo rápido en la nube (agentes)
La nube no accede a Maven, Google ni Gradle. El agente prueba la lógica en Java puro con `javac` (opción `--release 11`) y el JUnit 4 y Hamcrest incluidos con Gradle (`/opt/gradle*/lib` o la caché `~/.gradle`; se pueden fijar con `JUNIT_JAR` y `HAMCREST_JAR`).
- Suite de lógica: `scripts/probar-logica.sh`
- Solo un alcance: `scripts/probar-logica.sh combate` (clases de prueba cuyo nombre completo contiene el texto).

El script compila los `.java` de `app/src/main/java` y `app/src/test/java` que no importan Android, ejecuta las clases `*Test` con `scripts/EjecutorLogica.java` e imprime una línea por clase, el detalle de cada fallo (mensaje y línea) y un resumen. Sale con código 1 si algo falla. La compilación Android y la suite completa corren en GitHub Actions.

## Validar un paquete de contenido (H9)
`scripts/validar-contenido.sh <carpeta> [--json]` carga la carpeta con los cargadores reales del motor (Java puro, clase `herramientas.ValidadorContenido`) y revisa cada documento: configuración, habilidades, combatientes, objetos, progresión, botín, encuentros, servicios, cada mapa y cada escena, y por último la partida nueva (`inicio.json` y las referencias entre mapas).
- Salida: una línea por documento (`OK`, `ERROR` u `OMITIDO`) y, solo bajo los errores, el detalle con la ruta del campo, por ejemplo `combatientes.json: lista[10].golpeFuerte.habilidad: habilidad "x" no existe`. `OMITIDO` es un documento que no se puede comprobar porque depende de otro con error.
- `--json`: la misma información en una línea de JSON (`carpeta`, `ok`, `errores`, `omitidos`, `documentos[]` con `documento`, `estado` y `detalle`); es lo que leerá el editor.
- Código de salida: 0 válido, 1 con errores u omitidos, 2 uso incorrecto. Ejemplos: `scripts/validar-contenido.sh app/src/main/assets/contenido` y `scripts/validar-contenido.sh app/src/test/resources/contenido-minimo`.
- Pruebas: `ValidadorContenidoTest` (el paquete del juego y el mínimo, referencias rotas, JSON mal formado, rangos, documento ausente, salida JSON y códigos de salida).

## Integración continua
`.github/workflows/pruebas.yml` (flujo `Pruebas`) corre la verificación de estructura y la suite unitaria en cada push a `main`, `feature/**` y `fix/**`, y en cada pull request. Los flujos los coloca el usuario: `.github/` es de solo lectura para el agente.

## APK descargable
`.github/workflows/apk.yml` (flujo `APK`) construye el APK de depuración en cada push a `main`, `feature/**` y `fix/**`, y lo guarda 14 días como archivo `ff1-debug-apk`. Se descarga desde la pestaña Actions de GitHub: abrir la ejecución `APK` de la rama y bajarlo desde "Artifacts". Es un APK de depuración, que se instala en el celular permitiendo aplicaciones de origen desconocido. Una versión firmada de lanzamiento queda fuera del MVP, porque exige guardar una clave como secreto. Desde H3.5 el proyecto no tiene código nativo, así que el APK no necesita CMake ni el NDK.

## Último resultado
2026-10-02, nube (bloque 7): `scripts/probar-logica.sh` 175 de 175 pruebas pasan en `chore/quitar-codigo-nativo`. GitHub Actions en `feature/h35-jugable`: `Pruebas` y `APK` en verde. El flujo `APK` también se puede lanzar a mano (`workflow_dispatch`) en ramas que no lo disparan al hacer push.
