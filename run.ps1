$ErrorActionPreference = "Stop"
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "Starting Return Hub (Lost and Found Web Application)..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

if (Test-Path "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot") {
    $env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
} elseif (Test-Path "$env:USERPROFILE\.jdks\jdk-21.0.12.1+1") {
    $env:JAVA_HOME = "$env:USERPROFILE\.jdks\jdk-21.0.12.1+1"
}

$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Using Java: $env:JAVA_HOME" -ForegroundColor Green
& "$env:JAVA_HOME\bin\java.exe" -version

# Free port 8080 if lingering from a previous run
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | ForEach-Object {
    Write-Host "Freeing port 8080 (Killing PID $($_.OwningProcess))..." -ForegroundColor Yellow
    Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue
}

& .\mvnw.cmd spring-boot:run
