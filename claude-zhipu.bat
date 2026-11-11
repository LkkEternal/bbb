@echo off
cd /d "D:\表情检测\glmProxy"
echo Starting Zhipu proxy...
start /B python zhipu_proxy.py > nul 2>&1
timeout /t 3 /nobreak > nul

cd /d "D:\表情检测"
set ANTHROPIC_BASE_URL=http://localhost:23456
set ANTHROPIC_API_KEY=mypassword
echo.
echo Claude Code with Zhipu AI (GLM-4-Flash)
echo =======================================
claude

echo.
echo Stopping proxy...
taskkill /F /IM python.exe /FI "WINDOWTITLE eq zhipu*" > nul 2>&1
