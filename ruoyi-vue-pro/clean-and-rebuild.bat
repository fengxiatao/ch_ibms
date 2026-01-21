@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ============================================
echo   项目清理与重新编译脚本
echo ============================================
echo.

REM 切换到项目根目录
cd /d "%~dp0"

echo [1/5] 正在检查并终止占用端口 48888 和 48083 的进程...
echo.

REM 查找并终止占用 48888 端口的进程
set "found48888=0"
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":48888" ^| findstr "LISTENING"') do (
    if not "%%a"=="0" (
        set "found48888=1"
        echo 发现端口 48888 被进程 PID=%%a 占用，正在终止...
        taskkill /F /PID %%a >nul 2>&1
    )
)
if "!found48888!"=="0" echo 端口 48888 未被占用

REM 查找并终止占用 48083 端口的进程
set "found48083=0"
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":48083" ^| findstr "LISTENING"') do (
    if not "%%a"=="0" (
        set "found48083=1"
        echo 发现端口 48083 被进程 PID=%%a 占用，正在终止...
        taskkill /F /PID %%a >nul 2>&1
    )
)
if "!found48083!"=="0" echo 端口 48083 未被占用

echo.
echo [2/5] 正在终止可能锁定目录的相关 Java 进程...
echo.

REM 使用 tasklist + findstr 方式查找并终止 maven-exec 和 yudao 相关进程
for /f "tokens=2" %%a in ('tasklist /v /fo csv 2^>nul ^| findstr /i "maven-exec yudao" ^| findstr /r "^\"java"') do (
    echo 正在终止 Java 进程 PID=%%~a ...
    taskkill /F /PID %%~a >nul 2>&1
)

REM 通过命令行查找包含特定关键字的 java 进程
powershell -Command "Get-WmiObject Win32_Process -Filter \"Name='java.exe'\" | Where-Object { $_.CommandLine -match 'maven-exec|yudao' } | ForEach-Object { Write-Host ('终止进程 PID=' + $_.ProcessId); Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }" 2>nul

echo.
echo [3/5] 等待进程完全退出...
timeout /t 3 /nobreak >nul

echo.
echo [4/5] 正在清理可能被锁定的 target 目录...
echo.

REM 强制删除可能被锁定的 target 目录
call :clean_target "yudao-module-system"
call :clean_target "yudao-module-infra"
call :clean_target "yudao-module-iot\yudao-module-iot-biz"
call :clean_target "yudao-module-iot\yudao-module-iot-core"
call :clean_target "yudao-module-iot\yudao-module-iot-api"
call :clean_target "yudao-server"

echo.
echo [5/5] 正在执行 Maven 编译...
echo.

REM 执行 Maven clean compile（使用多线程加速）
call mvn clean compile -DskipTests -T 1C

if %errorlevel%==0 (
    echo.
    echo ============================================
    echo   编译成功！项目已准备就绪
    echo ============================================
    echo.
    echo 您现在可以启动项目：
    echo   方式1: 在 IDE 中运行 YudaoServerApplication
    echo   方式2: 执行 mvn exec:exec -pl yudao-server
    echo.
) else (
    echo.
    echo ============================================
    echo   编译失败！请检查错误信息
    echo ============================================
    echo.
    echo 常见问题排查：
    echo   1. 检查是否有其他进程锁定文件
    echo   2. 尝试关闭 IDE 后重新运行此脚本
    echo   3. 手动删除各模块的 target 目录
    echo.
)

echo 按任意键退出...
pause >nul
goto :eof

REM === 子函数：清理 target 目录 ===
:clean_target
set "module_path=%~1"
if exist "%module_path%\target" (
    echo 正在清理 %module_path%\target ...
    rd /s /q "%module_path%\target" 2>nul
    if exist "%module_path%\target" (
        echo [警告] %module_path%\target 清理失败，尝试使用 PowerShell 强制删除...
        powershell -Command "Remove-Item -Path '%module_path%\target' -Recurse -Force -ErrorAction SilentlyContinue" 2>nul
    )
    if not exist "%module_path%\target" (
        echo [成功] %module_path%\target 已清理
    ) else (
        echo [失败] %module_path%\target 仍被锁定，请手动关闭相关程序
    )
) else (
    echo [跳过] %module_path%\target 不存在
)
goto :eof
