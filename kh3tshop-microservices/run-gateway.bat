@echo off
setlocal

set ROOT_DIR=%~dp0
pushd "%ROOT_DIR%"

call ..\kh3tshop-be\mvnw.cmd -f .\kh3t-gateway\pom.xml spring-boot:run

popd
endlocal
