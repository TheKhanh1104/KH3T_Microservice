<#
Start each Spring Boot microservice in its own PowerShell window.
Usage: Right-click -> Run with PowerShell, or from an elevated PowerShell: .\start-services.ps1
#>

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
$mvnw = Join-Path $repoRoot 'kh3tshop-be\mvnw.cmd'

function Start-ServiceWindow([string]$title, [string]$cmd) {
    $args = "-NoExit", "-Command", $cmd
    Start-Process -FilePath powershell -ArgumentList $args -WindowStyle Normal -WorkingDirectory $repoRoot -Verb RunAs -PassThru | Out-Null
}

Write-Output "Starting KH3T microservices (PowerShell windows will open)..."

Start-ServiceWindow -title 'kh3t-discovery' -cmd "& '$mvnw' -f '$repoRoot\kh3tshop-microservices\kh3t-discovery\pom.xml' spring-boot:run"
Start-Sleep -Seconds 2
Start-ServiceWindow -title 'kh3t-identity-service' -cmd "& '$mvnw' -f '$repoRoot\kh3tshop-microservices\kh3t-identity-service\pom.xml' spring-boot:run"
Start-Sleep -Seconds 1
Start-ServiceWindow -title 'kh3t-catalog-service' -cmd "& '$mvnw' -f '$repoRoot\kh3tshop-microservices\kh3t-catalog-service\pom.xml' spring-boot:run"
Start-Sleep -Seconds 1
Start-ServiceWindow -title 'kh3t-order-service' -cmd "& '$mvnw' -f '$repoRoot\kh3tshop-microservices\kh3t-order-service\pom.xml' spring-boot:run"
Start-Sleep -Seconds 1
Start-ServiceWindow -title 'kh3t-gateway' -cmd "& '$mvnw' -f '$repoRoot\kh3tshop-microservices\kh3t-gateway\pom.xml' spring-boot:run"

Write-Output "Launched start commands. Check the new windows for logs."
