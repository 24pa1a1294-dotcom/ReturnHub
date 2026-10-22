@echo off
setlocal
echo ========================================================
echo Starting Return Hub (Lost and Found Web Application)...
echo ========================================================

if exist "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot" (
    set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
) else if exist "%USERPROFILE%\.jdks\jdk-21.0.12.1+1" (
    set "JAVA_HOME=%USERPROFILE%\.jdks\jdk-21.0.12.1+1"
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Using Java: %JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" -version

echo Checking port 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    echo Freeing port 8080 (PID %%a)...
    taskkill /F /PID %%a >nul 2>&1
)

call mvnw.cmd spring-boot:run
pause
