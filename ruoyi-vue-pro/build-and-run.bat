@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: =====================================================
:: 若依 Vue Pro 项目编译运行脚本
:: 作者: 长辉信息科技
:: 功能: 清理、编译、安装整个项目并启动服务
:: =====================================================

:: 配置参数
set MAVEN_REPO=F:\repo
set PROJECT_DIR=%~dp0
set GROUP_ID=cn\iocoder\boot

:: 颜色输出函数
echo.
echo ========================================
echo    若依 Vue Pro 编译运行脚本
echo ========================================
echo.

:: 检查参数
if "%1"=="clean" goto :clean_repo
if "%1"=="build" goto :build_only
if "%1"=="run" goto :run_only
if "%1"=="full" goto :full_build
if "%1"=="" goto :full_build

echo 用法: %~nx0 [命令]
echo.
echo 命令:
echo   (无参数)  完整编译并安装到本地仓库
echo   clean     清理 Maven 本地仓库中的项目缓存
echo   build     仅编译安装（不清理仓库缓存）
echo   run       仅运行服务（不编译）
echo   full      完整清理+编译+安装
echo.
goto :eof

:: =====================================================
:: 清理 Maven 本地仓库中的项目缓存
:: =====================================================
:clean_repo
echo [1/1] 清理 Maven 本地仓库缓存...
echo 删除目录: %MAVEN_REPO%\%GROUP_ID%

if exist "%MAVEN_REPO%\%GROUP_ID%" (
    rd /s /q "%MAVEN_REPO%\%GROUP_ID%"
    if !errorlevel! equ 0 (
        echo [成功] 缓存清理完成
    ) else (
        echo [警告] 缓存清理可能不完整
    )
) else (
    echo [信息] 缓存目录不存在，无需清理
)
echo.
goto :eof

:: =====================================================
:: 完整编译（清理仓库+编译）
:: =====================================================
:full_build
echo [1/4] 清理 Maven 本地仓库中的项目缓存...
if exist "%MAVEN_REPO%\%GROUP_ID%" (
    rd /s /q "%MAVEN_REPO%\%GROUP_ID%"
    echo [成功] 缓存清理完成
) else (
    echo [信息] 缓存目录不存在，跳过
)
echo.

:build_only
cd /d "%PROJECT_DIR%"

echo [2/4] 清理项目 target 目录...
call mvn clean -q "-Dmaven.repo.local=%MAVEN_REPO%"
if !errorlevel! neq 0 (
    echo [错误] 清理失败！
    pause
    goto :eof
)
echo [成功] 清理完成
echo.

echo [3/4] 编译并安装所有模块...
echo 这可能需要几分钟，请耐心等待...
echo.

call mvn install -DskipTests "-Dmaven.repo.local=%MAVEN_REPO%" -T 1C
if !errorlevel! neq 0 (
    echo.
    echo ========================================
    echo [错误] 编译失败！
    echo ========================================
    echo.
    echo 常见解决方案:
    echo 1. 检查代码是否有语法错误
    echo 2. 运行 "%~nx0 full" 完整重建
    echo 3. 检查网络连接（下载依赖）
    echo.
    pause
    goto :eof
)

echo.
echo ========================================
echo [成功] 编译安装完成！
echo ========================================
echo.
echo [4/4] 准备运行...
echo 你现在可以启动 yudao-server 了
echo.

:: 询问是否运行
set /p RUN_NOW="是否立即运行服务? (Y/N): "
if /i "%RUN_NOW%"=="Y" goto :run_only
goto :eof

:: =====================================================
:: 仅运行服务
:: =====================================================
:run_only
cd /d "%PROJECT_DIR%"
echo.
echo [运行] 启动 yudao-server...
echo 按 Ctrl+C 停止服务
echo.

call mvn exec:exec -pl yudao-server "-Dmaven.repo.local=%MAVEN_REPO%"
goto :eof




















