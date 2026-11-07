$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$jarPath = Join-Path $repoRoot "app\build\libs\app-0.0.1-SNAPSHOT.jar"
$javaExecutable = if ($env:JAVA_HOME) {
    Join-Path $env:JAVA_HOME "bin\java.exe"
} else {
    "java"
}

function Stop-RotationWithError {
    param([string]$Details)

    $message = "Error: no se pudo rotar la API key de scraping"
    if (-not [string]::IsNullOrWhiteSpace($Details)) {
        $message += ". Detalle: " + $Details.Trim()
    }

    [Console]::Error.WriteLine($message)
    exit 1
}

if (-not (Test-Path $jarPath)) {
    Stop-RotationWithError "no se encontró el artefacto de la aplicación; ejecute .\gradlew.bat :app:bootJar primero"
}

if ($javaExecutable -ne "java" -and -not (Test-Path $javaExecutable)) {
    Stop-RotationWithError "JAVA_HOME no apunta a una instalación válida de Java"
}

try {
    $output = @(& $javaExecutable "-Duser.timezone=America/Argentina/Buenos_Aires" "-Dlogging.level.root=OFF" -jar $jarPath --app.command=rotate-scraping-api-key 2>&1)
    if ($LASTEXITCODE -ne 0) {
        $details = ($output | ForEach-Object { $_.ToString() } | Where-Object { $_.Trim() -ne "" }) -join [Environment]::NewLine
        Stop-RotationWithError $details
    }

    $secret = ($output | Where-Object { $_ -and $_.Trim() -ne "" } | Select-Object -Last 1)
    if (-not $secret) {
        Stop-RotationWithError "la aplicación terminó correctamente pero no devolvió una API key"
    }

    Write-Output $secret
} catch {
    Stop-RotationWithError $_.Exception.Message
}
