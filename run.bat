@echo off
echo ================================================================
echo  TaskNest - Personal To-Do and Task Manager REST API
echo ================================================================

:: Check for Java 21 JDK installation
if exist "C:\Program Files\Java\jdk-21.0.12.1" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
    set "PATH=C:\Program Files\Java\jdk-21.0.12.1\bin;%PATH%"
    echo [INFO] Configured JAVA_HOME to JDK 21: %JAVA_HOME%
)

:: Display Java and Maven versions
echo [INFO] Checking Java version...
java -version

echo.
echo [INFO] Checking Maven version...
mvn -version

echo.
echo [INFO] Freeing port 8081 if occupied...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081 ^| findstr LISTENING') do (
    echo Terminating stale process %%a on port 8081...
    taskkill /F /PID %%a >nul 2>&1
)

:: Wait for socket release
ping 127.0.0.1 -n 3 >nul 2>&1

echo.
echo ================================================================
echo [INFO] Starting TaskNest on http://localhost:8081
echo [INFO] Web Frontend UI:  http://localhost:8081/index.html
echo [INFO] Swagger UI Docs:  http://localhost:8081/swagger-ui/index.html
echo ================================================================
mvn spring-boot:run
pause
