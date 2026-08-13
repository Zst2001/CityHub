Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$envFile = Join-Path $PSScriptRoot '..\..\.env'
if (-not (Test-Path -LiteralPath $envFile)) {
    throw 'Missing root .env. Copy .env.example to .env and fill the local values first.'
}

Get-Content -LiteralPath $envFile | ForEach-Object {
    if ($_ -match '^\s*#' -or $_ -notmatch '=') { return }
    $name, $value = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), 'Process')
}

& mvn spring-boot:run
