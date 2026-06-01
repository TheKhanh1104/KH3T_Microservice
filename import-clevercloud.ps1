# Script to reset and import data to Clever Cloud MySQL/MariaDB database
Write-Host "Starting database reset and import on Clever Cloud via Python..." -ForegroundColor Cyan

python "${PSScriptRoot}/import_clevercloud.py"

if ($LASTEXITCODE -eq 0) {
    Write-Host "Database operation completed successfully!" -ForegroundColor Green
} else {
    Write-Host "Failed to perform database operation!" -ForegroundColor Red
}
