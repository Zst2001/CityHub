param()

$envFile = Join-Path $PSScriptRoot '..\..\.env'
if (-not (Test-Path -LiteralPath $envFile)) {
    throw "Local .env was not found. Copy .env.example and configure local values first."
}

Get-Content -LiteralPath $envFile | ForEach-Object {
    $line = $_.Trim()
    if (-not $line -or $line.StartsWith('#') -or -not $line.Contains('=')) { return }
    $name, $value = $line.Split('=', 2)
    [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), 'Process')
}

if ([string]::IsNullOrWhiteSpace($env:ALIYUNCS_API_KEY)) {
    throw 'ALIYUNCS_API_KEY is required for the CityHub AI consultant.'
}

Write-Host "Starting CityHub AI consultant with model: $env:LLM_MODEL_NAME"
mvn spring-boot:run
