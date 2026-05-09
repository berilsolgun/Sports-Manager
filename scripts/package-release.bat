@echo off
setlocal

cd /d "%~dp0.."

if "%JAVA_HOME%"=="" (
    echo JAVA_HOME is not set.
    echo Example: setx JAVA_HOME "C:\Program Files\Java\jdk-21"
    echo Open a new terminal after setting JAVA_HOME, then try again.
    exit /b 1
)

echo Running tests and packaging the application...
call mvnw.cmd clean test package
if errorlevel 1 exit /b 1

echo.
echo JAR and runtime libraries are ready:
echo target\sports-manager-1.0-SNAPSHOT.jar
echo target\lib
echo.

set "ISCC=C:\Program Files (x86)\Inno Setup 6\ISCC.exe"
if exist "%ISCC%" (
    echo Inno Setup found. Building installer...
    "%ISCC%" installer\SportsManager.iss
    if errorlevel 1 exit /b 1
    echo Installer ready: installer\Output\SportsManagerSetup.exe
) else (
    echo Inno Setup was not found. Compile installer\SportsManager.iss manually with Inno Setup.
)
