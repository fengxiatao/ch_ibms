@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: =====================================================
:: 快速重建脚本 - 用于解决依赖问题
:: 一键清理本地仓库缓存并重新编译整个项目
:: =====================================================

set MAVEN_REPO=F:\repo
set PROJECT_DIR=%~dp0
set GROUP_ID=cn\iocoder\boot

echo.
echo ============================================
echo   快速重建 - 解决依赖/编译问题
echo ============================================
echo.

:: 步骤1: 删除本地仓库中的项目缓存
echo [1/3] 清理本地仓库缓存...
if exist "%MAVEN_REPO%\%GROUP_ID%" (
    rd /s /q "%MAVEN_REPO%\%GROUP_ID%" 2>nul
    echo       已删除: %MAVEN_REPO%\%GROUP_ID%
) else (
    echo       缓存目录不存在，跳过
)

:: 步骤2: 进入项目目录
cd /d "%PROJECT_DIR%"

:: 步骤3: 清理并编译
echo.
echo [2/3] 清理项目...
call mvn clean -q "-Dmaven.repo.local=%MAVEN_REPO%"

echo.
echo [3/3] 编译安装所有模块 (使用多线程加速)...
echo       请耐心等待，这可能需要1-2分钟...
echo.

call mvn install -DskipTests "-Dmaven.repo.local=%MAVEN_REPO%" -T 1C

if !errorlevel! equ 0 (
    echo.
    echo ============================================
    echo   [成功] 编译完成！现在可以运行程序了
    echo ============================================
) else (
    echo.
    echo ============================================
    echo   [失败] 编译出错，请检查上面的错误信息
    echo ============================================
)

echo.
pause




















