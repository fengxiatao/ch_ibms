@echo off
setlocal enabledelayedexpansion

echo 正在检查端口 48888 和 48083 ...

for %%P in (48888 48083) do (
    echo.
    echo === 端口 %%P ===
    for /f "tokens=5" %%A in ('netstat -ano ^| findstr :%%P') do (
        echo 找到进程 PID=%%A ，正在结束...
        taskkill /PID %%A /F >nul 2>&1
    )
)

echo.
echo 处理完成
pause
