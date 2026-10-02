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
La nube no accede a Maven, Google ni Gradle. El agente prueba la lógica en Java puro con `javac` y el JUnit 4 incluido con Gradle, mediante `scripts/probar-logica.sh` (se crea en el hito H0 de `plan.md`). La compilación Android y la suite completa corren en GitHub Actions.

## Integración continua
`.github/workflows/pruebas.yml` (flujo `Pruebas`) corre la verificación de estructura y la suite unitaria en cada push a `main`, `feature/**` y `fix/**`, y en cada pull request. Los flujos los coloca el usuario: `.github/` es de solo lectura para el agente.

## APK descargable
`.github/workflows/apk.yml` (flujo `APK`) construye el APK de depuración en cada push a `main`, `feature/**` y `fix/**`, y lo guarda 14 días como archivo `ff1-debug-apk`. Se descarga desde la pestaña Actions de GitHub: abrir la ejecución `APK` de la rama y bajarlo desde "Artifacts". Es un APK de depuración, que se instala en el celular permitiendo aplicaciones de origen desconocido. Una versión firmada de lanzamiento queda fuera del MVP, porque exige guardar una clave como secreto. El flujo `APK` no está verificado: la plantilla nativa necesita CMake y el NDK.

## Último resultado
Sin ejecutar.
