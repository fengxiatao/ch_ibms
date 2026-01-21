@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: =====================================================
:: 完整重建并启动脚本
:: 清理本地仓库缓存 → 重新编译 → 启动应用
:: =====================================================

set MAVEN_REPO=F:\repo
set PROJECT_DIR=%~dp0
set GROUP_ID=cn\iocoder\boot
set MAIN_CLASS=cn.iocoder.yudao.server.YudaoServerApplication

:: 颜色定义（通过 PowerShell 实现）
set "GREEN=[92m"
set "YELLOW=[93m"
set "RED=[91m"
set "CYAN=[96m"
set "RESET=[0m"

echo.
echo %CYAN%============================================%RESET%
echo %CYAN%   完整重建并启动 - YuDao Server%RESET%
echo %CYAN%============================================%RESET%
echo.

:: =====================================================
:: 步骤1: 清理本地仓库缓存
:: =====================================================
echo %YELLOW%[1/4] 清理本地仓库缓存...%RESET%
if exist "%MAVEN_REPO%\%GROUP_ID%" (
    rd /s /q "%MAVEN_REPO%\%GROUP_ID%" 2>nul
    if !errorlevel! equ 0 (
        echo       %GREEN%√ 已删除: %MAVEN_REPO%\%GROUP_ID%%RESET%
    ) else (
        echo       %RED%× 删除失败，可能有文件被占用%RESET%
    )
) else (
    echo       缓存目录不存在，跳过
)

:: =====================================================
:: 步骤2: 进入项目目录
:: =====================================================
cd /d "%PROJECT_DIR%"
echo.
echo %YELLOW%[2/4] 工作目录: %PROJECT_DIR%%RESET%

:: =====================================================
:: 步骤3: 清理并编译安装
:: =====================================================
echo.
echo %YELLOW%[3/4] 清理并编译所有模块...%RESET%
echo       请耐心等待，这可能需要1-2分钟...
echo.

call mvn clean install -DskipTests "-Dmaven.repo.local=%MAVEN_REPO%" -T 1C

if !errorlevel! neq 0 (
    echo.
    echo %RED%============================================%RESET%
    echo %RED%   [失败] 编译出错，请检查上面的错误信息%RESET%
    echo %RED%============================================%RESET%
    echo.
    pause
    exit /b 1
)

echo.
echo %GREEN%============================================%RESET%
echo %GREEN%   [成功] 编译完成！%RESET%
echo %GREEN%============================================%RESET%

:: =====================================================
:: 步骤4: 启动应用
:: =====================================================
echo.
echo %YELLOW%[4/4] 启动应用...%RESET%
echo.
echo %CYAN%-----------------------------------------------------------%RESET%
echo %CYAN%   应用启动中，请等待 "Started" 字样出现%RESET%
echo %CYAN%   访问地址: http://localhost:48888%RESET%
echo %CYAN%   按 Ctrl+C 可停止应用%RESET%
echo %CYAN%-----------------------------------------------------------%RESET%
echo.

cd /d "%PROJECT_DIR%yudao-server"
call mvn exec:java "-Dexec.mainClass=%MAIN_CLASS%" "-Dmaven.repo.local=%MAVEN_REPO%"

echo.
echo 应用已停止。
pause
















