@echo off
REM ================================
REM 快速编译所有核心模块
REM ================================
REM 作者: Cursor AI
REM 日期: 2025-10-24
REM 用途: 解决多模块项目的编译问题
REM ================================

echo.
echo ╔══════════════════════════════════════════╗
echo ║   快速编译脚本 - Quick Compile Script   ║
echo ╚══════════════════════════════════════════╝
echo.
echo [信息] 正在编译所有核心业务模块...
echo [信息] 包括: system, infra, iot-core, iot-biz
echo.
echo [提示] 如果遇到 "Failed to delete" 错误，请先停止应用！
echo.

cd /d %~dp0

REM 先尝试清理，如果失败就只编译
mvn clean compile -pl yudao-module-system,yudao-module-infra,yudao-module-iot-core,yudao-module-iot-biz -am -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo [警告] 清理失败，可能是应用正在运行
    echo [尝试] 只编译不清理...
    mvn compile -pl yudao-module-system,yudao-module-infra,yudao-module-iot-core,yudao-module-iot-biz -am -DskipTests
    set errorlevel=%errorlevel%
)

if %errorlevel% equ 0 (
    echo.
    echo ╔══════════════════════════════════════════╗
    echo ║            编译成功完成！ ✓              ║
    echo ╚══════════════════════════════════════════╝
    echo.
    echo [下一步] 请在 IDE 中执行以下操作:
    echo   1. 打开 Maven 工具窗口
    echo   2. 点击 "刷新" 按钮 ^(Reload All Maven Projects^)
    echo   3. 等待 IDE 索引完成
    echo   4. 重新运行应用
    echo.
) else (
    echo.
    echo ╔══════════════════════════════════════════╗
    echo ║            编译失败！ ✗                  ║
    echo ╚══════════════════════════════════════════╝
    echo.
    echo [错误] 编译过程中出现错误
    echo [建议] 请检查错误信息并修复后重试
    echo.
)

pause

