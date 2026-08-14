Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$envFile = Join-Path $PSScriptRoot '..\..\.env'
if (-not (Test-Path -LiteralPath $envFile)) {
    throw 'Missing root .env. Copy .env.example to .env and fill the local values first.'
}

Get-Content -LiteralPath $envFile | ForEach-Object {
    if ($_ -match '^\s*#' -or $_ -notmatch '=') { return }
    $name, $value = $_ -split '=', 2
    Set-Item -Path "Env:$($name.Trim())" -Value $value.Trim()
}

# Docker Compose provisions the local cityhub user from MYSQL_PASSWORD. Reuse it
# when DB_PASSWORD is intentionally omitted rather than attempting an empty login.
if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD) -and -not [string]::IsNullOrWhiteSpace($env:MYSQL_PASSWORD)) {
    Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue
}

if (-not [string]::IsNullOrWhiteSpace($env:DB_URL) -and $env:DB_URL -notmatch 'allowPublicKeyRetrieval=') {
    $separator = if ($env:DB_URL.Contains('?')) { '&' } else { '?' }
    $env:DB_URL = $env:DB_URL + $separator + 'allowPublicKeyRetrieval=true'
}

# MyBatis-Plus 3.4.x uses JDK internals that require this opening on Java 17.
$existingJavaToolOptions = [Environment]::GetEnvironmentVariable('JAVA_TOOL_OPTIONS', 'Process')
$requiredJavaToolOption = '--add-opens=java.base/java.lang.invoke=ALL-UNNAMED'
if ($existingJavaToolOptions -notmatch [regex]::Escape($requiredJavaToolOption)) {
    [Environment]::SetEnvironmentVariable(
        'JAVA_TOOL_OPTIONS',
        "$existingJavaToolOptions $requiredJavaToolOption".Trim(),
        'Process'
    )
}

& mvn spring-boot:run
