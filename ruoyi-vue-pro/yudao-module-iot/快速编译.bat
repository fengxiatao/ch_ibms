@echo off
chcp 65001 >nul
echo ============================================
echo     IoT模块快速编译脚本
echo ============================================
echo.

echo [1/3] 正在编译 iot-core 模块...
cd yudao-module-iot-core
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo [错误] iot-core模块编译失败
    pause
    exit /b 1
)
echo [✓] iot-core模块编译成功
echo.

echo [2/3] 正在编译 iot-gateway 模块...
cd ..\yudao-module-iot-gateway
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo [错误] iot-gateway模块编译失败
    pause
    exit /b 1
)
echo [✓] iot-gateway模块编译成功
echo.

echo [3/3] 正在编译 iot-biz 模块...
cd ..\yudao-module-iot-biz
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo [错误] iot-biz模块编译失败
    pause
    exit /b 1
)
echo [✓] iot-biz模块编译成功
echo.

cd ..
echo ============================================
echo     ✓ 所有IoT模块编译成功！
echo ============================================
echo.
pause

















