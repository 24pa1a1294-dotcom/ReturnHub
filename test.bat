@echo off
setlocal
if exist "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot" (
    set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
) else if exist "%USERPROFILE%\.jdks\jdk-21.0.12.1+1" (
    set "JAVA_HOME=%USERPROFILE%\.jdks\jdk-21.0.12.1+1"
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
call mvnw.cmd test
pause
