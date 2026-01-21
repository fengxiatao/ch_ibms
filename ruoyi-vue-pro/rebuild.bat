@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo    项目重新编译脚本
echo ========================================
echo.

:: 步骤1: 查询并杀掉 48888 端口的进程
echo [1/7] 检查并终止 48888 端口进程...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":48888" ^| findstr "LISTENING" 2^>nul') do (
    echo     发现进程 PID: %%a 占用 48888 端口
    taskkill /F /PID %%a >nul 2>&1
    if !errorlevel! equ 0 (
        echo     已终止进程 %%a
    ) else (
        echo     终止进程 %%a 失败或进程不存在
    )
)
echo     48888 端口检查完成

:: 步骤2: 查询并杀掉 48083 端口的进程
echo [2/7] 检查并终止 48083 端口进程...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":48083" ^| findstr "LISTENING" 2^>nul') do (
    echo     发现进程 PID: %%a 占用 48083 端口
    taskkill /F /PID %%a >nul 2>&1
    if !errorlevel! equ 0 (
        echo     已终止进程 %%a
    ) else (
        echo     终止进程 %%a 失败或进程不存在
    )
)
echo     48083 端口检查完成

:: 步骤3: 终止可能残留的 maven-exec 进程
echo [3/7] 清理残留的 Maven 进程...
for /f "tokens=2 delims=," %%a in ('wmic process where "commandline like '%%maven-exec%%'" get processid /format:csv 2^>nul ^| findstr /r "[0-9]"') do (
    taskkill /F /PID %%a >nul 2>&1
    echo     已终止 Maven 进程 %%a
)
echo     Maven 进程清理完成

:: 步骤4: 终止 YudaoServerApplication 相关的 Java 进程
echo [4/7] 清理 Yudao 应用进程...
for /f "tokens=2 delims=," %%a in ('wmic process where "commandline like '%%YudaoServerApplication%%'" get processid /format:csv 2^>nul ^| findstr /r "[0-9]"') do (
    taskkill /F /PID %%a >nul 2>&1
    echo     已终止 Yudao 进程 %%a
)
for /f "tokens=2 delims=," %%a in ('wmic process where "commandline like '%%yudao-server%%'" get processid /format:csv 2^>nul ^| findstr /r "[0-9]"') do (
    taskkill /F /PID %%a >nul 2>&1
    echo     已终止 yudao-server 进程 %%a
)
echo     Yudao 进程清理完成

:: 步骤5: 等待进程完全释放资源
echo [5/7] 等待资源释放...
timeout /t 3 /nobreak >nul
echo     资源释放完成

:: 步骤6: 强制删除所有 target 目录
echo [6/7] 清理所有 target 目录...

:: 使用 robocopy 空目录的方式强制删除（更可靠）
set "EMPTY_DIR=%TEMP%\empty_dir_%RANDOM%"
mkdir "%EMPTY_DIR%" 2>nul

:: 清理主要模块的 target 目录
set "DIRS_TO_CLEAN=yudao-module-system\target yudao-server\target yudao-module-iot\yudao-module-iot-biz\target yudao-module-iot\yudao-module-iot-core\target yudao-module-iot\yudao-module-iot-api\target yudao-module-infra\target"

for %%d in (%DIRS_TO_CLEAN%) do (
    if exist "%~dp0%%d" (
        echo     清理 %%d ...
        rd /s /q "%~dp0%%d" 2>nul
        if exist "%~dp0%%d" (
            :: 如果普通删除失败，使用 robocopy 强制删除
            robocopy "%EMPTY_DIR%" "%~dp0%%d" /mir /njh /njs /ndl /nc /ns >nul 2>&1
            rd /s /q "%~dp0%%d" 2>nul
        )
    )
)

:: 清理临时空目录
rd /q "%EMPTY_DIR%" 2>nul

echo     target 目录清理完成

:: 步骤7: 再次等待确保文件锁释放
echo [7/7] 最终资源释放检查...
timeout /t 2 /nobreak >nul
echo     检查完成

echo.
echo ========================================
echo    开始 Maven 编译
echo ========================================
echo.

:: 切换到脚本所在目录（项目根目录）
cd /d "%~dp0"

:: 执行 Maven clean compile，使用多线程加速
call mvn clean compile -DskipTests -T 1C

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo    编译成功！
    echo ========================================
    echo.
    echo 您现在可以启动项目了。
) else (
    echo.
    echo ========================================
    echo    编译失败！
    echo ========================================
    echo.
    echo 请检查上方错误信息。
    echo.
    echo 如果仍然失败，请尝试：
    echo 1. 关闭 IntelliJ IDEA 或其他 IDE
    echo 2. 重新运行此脚本
)

echo.
pause
