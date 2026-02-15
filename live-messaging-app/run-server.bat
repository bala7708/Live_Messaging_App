@echo off
echo ==================================
echo    Live Messaging Server
echo ==================================
echo.

REM Check if JAR exists
if not exist "target\messaging-server.jar" (
    echo Server JAR not found. Building...
    call mvn clean package
    
    if errorlevel 1 (
        echo Build failed!
        pause
        exit /b 1
    )
)

REM Get port from argument or use default
set PORT=%1
if "%PORT%"=="" set PORT=5000

echo Starting server on port %PORT%...
echo.

java -jar target\messaging-server.jar %PORT%

pause
