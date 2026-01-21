@echo off
chcp 65001 >nul
setlocal

:: =====================================================
:: 快速启动脚本 (不重新编译)
:: 适用于代码没有改动的情况下快速启动
:: =====================================================

set MAVEN_REPO=F:\repo
set PROJECT_DIR=%~dp0
set MAIN_CLASS=cn.iocoder.yudao.server.YudaoServerApplication

echo.
echo ============================================
echo   快速启动 - YuDao Server
echo ============================================
echo.
echo 提示: 如果遇到类找不到的错误，请运行 rebuild-and-run.bat
echo.
echo -----------------------------------------------------------
echo   应用启动中，请等待 "Started" 字样出现
echo   访问地址: http://localhost:48888
echo   按 Ctrl+C 可停止应用
echo -----------------------------------------------------------
echo.

cd /d "%PROJECT_DIR%yudao-server"
call mvn exec:java "-Dexec.mainClass=%MAIN_CLASS%" "-Dmaven.repo.local=%MAVEN_REPO%"

echo.
echo 应用已停止。
pause
















