# Verifica la estructura estandar del proyecto (skills maestras 03, 04 y 05).
# Uso, desde la raiz del proyecto:  .\scripts\verificar-estructura.ps1
# Imprime una linea por comprobacion y termina con codigo 1 si alguna falla.

$raiz = Split-Path -Parent $PSScriptRoot
$total = 0
$fallas = 0

function Revisar([bool]$ok, [string]$nombre) {
    $script:total++
    if ($ok) {
        Write-Output "OK: $nombre"
    } else {
        Write-Output "FALLA: $nombre"
        $script:fallas++
    }
}

$archivos = @(
    "README.md",
    "CHANGELOG.md",
    ".gitignore",
    "skills/00-iniciar.md",
    "docs/estado.md",
    "docs/decisiones.md",
    "docs/pruebas.md"
)
foreach ($archivo in $archivos) {
    Revisar (Test-Path (Join-Path $raiz $archivo)) "existe $archivo"
}

# Secciones obligatorias del README (el punto reemplaza letras con tilde).
$readme = Join-Path $raiz "README.md"
if (Test-Path $readme) {
    $texto = Get-Content $readme -Raw -Encoding UTF8
    $secciones = @(
        "## Estado",
        "## C.mo ejecutarlo",
        "## Estructura",
        "## Decisiones importantes",
        "## Documentaci.n relacionada"
    )
    foreach ($seccion in $secciones) {
        Revisar ($texto -match [regex]::Escape($seccion).Replace("\.", ".")) "README tiene '$seccion'"
    }
}

# El .gitignore no debe dejar pasar archivos locales.
$gitignore = Join-Path $raiz ".gitignore"
if (Test-Path $gitignore) {
    $contenido = Get-Content $gitignore -Raw
    Revisar ($contenido -match "local\.properties") ".gitignore excluye local.properties"
}

Write-Output "Resultado: $($total - $fallas) de $total comprobaciones correctas"
if ($fallas -gt 0) { exit 1 }
