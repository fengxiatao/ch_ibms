@echo off
setlocal

REM 基准目录（当前脚本所在目录），应为 E:\ch\ruoyi-vue-pro
cd /d %~dp0

echo Running PowerShell helper...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0kill-and-clean-iot-biz.ps1"
if errorlevel 1 (
    echo.
    echo [ERROR] Script failed with exit code %errorlevel%.
    pause
    endlocal
    exit /b %errorlevel%
)

echo.
echo Done.
pause
endlocal

