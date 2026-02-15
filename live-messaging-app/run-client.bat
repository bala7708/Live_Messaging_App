@echo off
echo ==================================
echo    Live Messaging Client
echo ==================================
echo.

REM Check if JAR exists
if not exist "target\messaging-client.jar" (
    echo Client JAR not found. Building...
    call mvn clean package
    
    if errorlevel 1 (
        echo Build failed!
        pause
        exit /b 1
    )
)

echo Starting client...
echo.

java -jar target\messaging-client.jar

pause
