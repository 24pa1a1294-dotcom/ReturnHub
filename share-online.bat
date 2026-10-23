@echo off
setlocal
echo ========================================================
echo Generating Public Live URL for Return Hub...
echo ========================================================
echo.
for /f "tokens=*" %%i in ('curl -s https://api.ipify.org') do set "MY_IP=%%i"
echo Your Endpoint IP (for first-time password verification): %MY_IP%
echo.
echo Starting secure tunnel on port 8080...
echo (Keep this window open to keep your public website online!)
echo ========================================================
echo.
call npx --yes localtunnel --port 8080
pause
