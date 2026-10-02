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

## Integración continua
El borrador `pruebas.yml` (verificación de estructura y suite unitaria en cada push a `main` y en cada pull request) debe colocarse a mano en `.github/workflows/`; todavía no está en el proyecto.

## Último resultado
Sin ejecutar.
