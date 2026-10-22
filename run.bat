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

call mvnw.cmd spring-boot:run
pause
