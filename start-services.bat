@echo off
REM Start each Spring Boot microservice in its own CMD window (Windows)
REM Usage: double-click this file or run from PowerShell/CMD in repository root

set MVNW=%~dp0kh3tshop-be\mvnw.cmd

echo Starting KH3T microservices (each will open in its own window)...

REM Discovery (Eureka)
start "kh3t-discovery" cmd /k "%MVNW% -f %~dp0kh3tshop-microservices\kh3t-discovery\pom.xml spring-boot:run"
timeout /t 2 >nul

REM Identity
start "kh3t-identity-service" cmd /k "%MVNW% -f %~dp0kh3tshop-microservices\kh3t-identity-service\pom.xml spring-boot:run"
timeout /t 1 >nul

REM Catalog
start "kh3t-catalog-service" cmd /k "%MVNW% -f %~dp0kh3tshop-microservices\kh3t-catalog-service\pom.xml spring-boot:run"
timeout /t 1 >nul

REM Order
start "kh3t-order-service" cmd /k "%MVNW% -f %~dp0kh3tshop-microservices\kh3t-order-service\pom.xml spring-boot:run"
timeout /t 1 >nul

REM Gateway (run last)
start "kh3t-gateway" cmd /k "%MVNW% -f %~dp0kh3tshop-microservices\kh3t-gateway\pom.xml spring-boot:run"

echo All start commands issued. Watch the new windows for logs.
pause
