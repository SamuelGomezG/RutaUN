<#
.SYNOPSIS
  Consulta la base de desarrollo de RutaUN (DOC-11, #136).

.DESCRIPTION
  Corre org.h2.tools.Shell sobre data/rutaun.mv.db, con la aplicación detenida.
  Abre la base en solo lectura y usa el jar de H2 que resuelve el wrapper de
  Maven (.\mvnw.cmd), así que la versión es siempre la del proyecto.

  Uso:
    scripts\consultar-base.ps1 "<consulta SQL>" [ruta\de\la\base]

  La ruta es opcional y va sin «.mv.db»; si es relativa, se resuelve desde el
  directorio actual. Códigos de salida: 0 si la consulta corrió, 1 si falló la
  conexión o el SQL, 2 si faltó la consulta.

  Convención y comandos: docs\proceso\definicion-de-hecho.md, «Cómo se
  demuestra un cambio» (comando B). La contraseña es la vacía por defecto de
  org.h2.tools.Shell; no se pasa -password.

  Guardado en UTF-8 con BOM para Windows PowerShell 5.1. Validado en Windows con
  PowerShell 5.1 y 7.7.0. Con la política de ejecución por defecto, PowerShell no
  corre scripts: habilita la sesión con
  Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass.
#>

param(
  [Parameter(Position = 0)]
  [string] $Consulta,

  [Parameter(Position = 1)]
  [string] $RutaBase
)

function Mostrar-Uso {
  [Console]::Error.WriteLine('uso: scripts\consultar-base.ps1 "<consulta SQL>" [ruta\de\la\base]')
  [Console]::Error.WriteLine('')
  [Console]::Error.WriteLine('Consulta la base de desarrollo (data\rutaun) con la aplicación detenida.')
  [Console]::Error.WriteLine('La ruta es opcional y va sin .mv.db; si es relativa, se resuelve desde el')
  [Console]::Error.WriteLine('directorio actual.')
}

function ConvertTo-Argumento([string] $valor) {
  $cuerpo = $valor -replace '(\\*)"', '$1$1\"'
  $cuerpo = $cuerpo -replace '(\\+)$', '$1$1'
  return '"' + $cuerpo + '"'
}

$raiz = Split-Path -Parent $PSScriptRoot

if ([string]::IsNullOrWhiteSpace($Consulta)) {
  Mostrar-Uso
  exit 2
}

if ([string]::IsNullOrWhiteSpace($RutaBase)) {
  $base = Join-Path $raiz 'data/rutaun'
} else {
  if ([System.IO.Path]::IsPathRooted($RutaBase)) {
    $base = $RutaBase
  } else {
    $base = Join-Path (Get-Location).Path $RutaBase
  }
  if ($base.EndsWith('.mv.db')) {
    $base = $base.Substring(0, $base.Length - '.mv.db'.Length)
  }
}

$base = $base.TrimEnd('\', '/')

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
  [Console]::Error.WriteLine('consultar-base: no encuentro «java» en el PATH. Instala un JDK 21.')
  exit 1
}

if (-not (Test-Path -LiteralPath "$base.mv.db")) {
  [Console]::Error.WriteLine("consultar-base: no encuentro $base.mv.db.")
  [Console]::Error.WriteLine('  Arranca primero con el perfil dev:  .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"')
  exit 1
}

$temporal = [System.IO.Path]::GetTempFileName()
try {
  $pom = Join-Path $raiz 'pom.xml'
  & (Join-Path $raiz 'mvnw.cmd') -q -f $pom '-DincludeArtifactIds=h2' "-Dmdep.outputFile=$temporal" dependency:build-classpath *> $null
  if ($LASTEXITCODE -ne 0) {
    [Console]::Error.WriteLine('consultar-base: no pude resolver el jar de H2 con el wrapper de Maven (mvnw.cmd).')
    exit 1
  }

  $h2 = (Get-Content -LiteralPath $temporal -Raw -Encoding UTF8).Trim()
  if ([string]::IsNullOrWhiteSpace($h2)) {
    [Console]::Error.WriteLine('consultar-base: el wrapper no devolvió la ruta del jar de H2.')
    exit 1
  }

  $argumentos = @(
    '-cp', (ConvertTo-Argumento $h2),
    'org.h2.tools.Shell',
    '-url', (ConvertTo-Argumento "jdbc:h2:file:$base;IFEXISTS=TRUE;ACCESS_MODE_DATA=r"),
    '-user', 'sa',
    '-sql', (ConvertTo-Argumento $Consulta)
  ) -join ' '

  $psi = [System.Diagnostics.ProcessStartInfo]::new()
  $psi.FileName = 'java'
  $psi.Arguments = $argumentos
  $psi.UseShellExecute = $false
  $psi.RedirectStandardOutput = $true
  $psi.RedirectStandardError = $true
  $psi.CreateNoWindow = $true
  $proceso = [System.Diagnostics.Process]::Start($psi)
  $stdout = $proceso.StandardOutput.ReadToEndAsync()
  $stderr = $proceso.StandardError.ReadToEndAsync()
  $proceso.WaitForExit()
  $salida = $stdout.Result + $stderr.Result
  $codigo = $proceso.ExitCode
  $proceso.Dispose()
} finally {
  Remove-Item -LiteralPath $temporal -ErrorAction SilentlyContinue
}

if ($salida -match '\[90020-\d+|Database may be already in use') {
  [Console]::Error.WriteLine('consultar-base: la base está abierta por otro proceso.')
  [Console]::Error.WriteLine('  Detén la aplicación: H2 en archivo admite un solo proceso.')
  exit 1
}

Write-Output $salida

if ($codigo -ne 0) {
  exit 1
}

if ($salida -match '(?m)^Error:') {
  exit 1
}

exit 0
