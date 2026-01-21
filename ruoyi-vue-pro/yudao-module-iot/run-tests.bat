@echo off
REM IoT模块测试运行脚本 (Windows版本)
REM 使用方法：
REM   run-tests.bat              # 运行所有测试
REM   run-tests.bat unit         # 只运行单元测试
REM   run-tests.bat integration  # 只运行集成测试
REM   run-tests.bat coverage     # 生成覆盖率报告

setlocal enabledelayedexpansion

echo [INFO] IoT模块测试脚本启动...
echo [INFO] 工作目录: %CD%

REM 检查Maven是否安装
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Maven未安装，请先安装Maven
    exit /b 1
)

REM 根据参数决定运行哪些测试
set TEST_TYPE=%1
if "%TEST_TYPE%"=="" set TEST_TYPE=all

if "%TEST_TYPE%"=="unit" (
    echo [INFO] 运行单元测试...
    call mvn clean test -Dtest=!*IntegrationTest
) else if "%TEST_TYPE%"=="integration" (
    echo [INFO] 运行集成测试...
    call mvn clean test -Dtest=*IntegrationTest
) else if "%TEST_TYPE%"=="coverage" (
    echo [INFO] 运行测试并生成覆盖率报告...
    call mvn clean test jacoco:report
    
    REM 打开覆盖率报告
    if exist "target\site\jacoco\index.html" (
        echo [INFO] 覆盖率报告已生成
        start target\site\jacoco\index.html
    ) else (
        echo [ERROR] 覆盖率报告生成失败
    )
) else if "%TEST_TYPE%"=="gateway" (
    echo [INFO] 运行Gateway模块测试...
    cd yudao-module-iot-gateway
    call mvn clean test
    cd ..
) else if "%TEST_TYPE%"=="biz" (
    echo [INFO] 运行Biz模块测试...
    cd yudao-module-iot-biz
    call mvn clean test
    cd ..
) else (
    echo [INFO] 运行所有测试...
    call mvn clean test
)

REM 检查测试结果
if %errorlevel% equ 0 (
    echo [INFO] ✅ 测试执行成功！
) else (
    echo [ERROR] ❌ 测试执行失败！
    exit /b 1
)

echo [INFO] 测试执行完成，查看详细报告：
echo [INFO]   - Surefire报告: target\surefire-reports\
echo [INFO]   - HTML报告: target\site\surefire-report.html

endlocal
exit /b 0












