@echo off
setlocal

set "APP_DIR=%~dp0"
set "JAR=%APP_DIR%sports-manager-1.0-SNAPSHOT.jar"
set "LIB=%APP_DIR%lib"

where java >nul 2>nul
if errorlevel 1 (
    echo Java Runtime was not found.
    echo Please install Java 21 or a newer Java Runtime.
    pause
    exit /b 1
)

if not exist "%JAR%" (
    echo Application JAR file was not found:
    echo %JAR%
    pause
    exit /b 1
)

java --module-path "%JAR%;%LIB%" --module com.sportsmanager/com.sportsmanager.MainApp
if errorlevel 1 pause
