@echo off
REM ============================================================
REM 后端服务健康检查脚本
REM 
REM 检查科鼎固件升级E2E测试所需的所有后端服务
REM
REM 检查项目:
REM   1. yudao-server HTTP 端口 (48080)
REM   2. iot-gateway HTTP 端口 (48082)
REM   3. iot-gateway 科鼎协议 TCP 端口 (9600)
REM   4. MySQL 连接 (3306)
REM   5. RocketMQ 连接 (9876)
REM
REM Requirements: 4.1, 4.2, 4.3
REM ============================================================

setlocal enabledelayedexpansion

REM 配置参数
set YUDAO_SERVER_HOST=localhost
set YUDAO_SERVER_PORT=48080
set GATEWAY_HTTP_HOST=localhost
set GATEWAY_HTTP_PORT=48082
set GATEWAY_KEDING_HOST=localhost
set GATEWAY_KEDING_PORT=9600
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set ROCKETMQ_HOST=192.168.1.126
set ROCKETMQ_PORT=9876

REM 计数器
set TOTAL_CHECKS=5
set PASSED_CHECKS=0
set FAILED_CHECKS=0

REM 颜色代码（Windows 10+支持）
set GREEN=[92m
set RED=[91m
set YELLOW=[93m
set RESET=[0m

echo.
echo ============================================================
echo   Keding Firmware Upgrade E2E Test - Service Health Check
echo ============================================================
echo.
echo   Checking %TOTAL_CHECKS% services...
echo.

REM ============================================================
REM 检查 1: yudao-server HTTP 端口
REM Requirements: 4.1
REM ============================================================
echo [1/%TOTAL_CHECKS%] Checking yudao-server HTTP port (%YUDAO_SERVER_HOST%:%YUDAO_SERVER_PORT%)...

REM 使用 PowerShell 检查 HTTP 端口
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://%YUDAO_SERVER_HOST%:%YUDAO_SERVER_PORT%/admin-api/system/auth/check-token' -Method GET -TimeoutSec 5 -UseBasicParsing -ErrorAction SilentlyContinue; exit 0 } catch { exit 1 }" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo         %GREEN%[PASS]%RESET% yudao-server is responding on port %YUDAO_SERVER_PORT%
    set /a PASSED_CHECKS+=1
) else (
    REM 尝试检查端口是否开放
    powershell -Command "try { $tcp = New-Object System.Net.Sockets.TcpClient; $tcp.Connect('%YUDAO_SERVER_HOST%', %YUDAO_SERVER_PORT%); $tcp.Close(); exit 0 } catch { exit 1 }" 2>nul
    if !ERRORLEVEL! EQU 0 (
        echo         %GREEN%[PASS]%RESET% yudao-server port %YUDAO_SERVER_PORT% is open
        set /a PASSED_CHECKS+=1
    ) else (
        echo         %RED%[FAIL]%RESET% yudao-server is not responding on port %YUDAO_SERVER_PORT%
        echo                Hint: Start yudao-server with: cd ruoyi-vue-pro ^& mvn spring-boot:run -pl yudao-server
        set /a FAILED_CHECKS+=1
    )
)
echo.

REM ============================================================
REM 检查 2: iot-gateway HTTP 端口
REM Requirements: 4.2
REM ============================================================
echo [2/%TOTAL_CHECKS%] Checking iot-gateway HTTP port (%GATEWAY_HTTP_HOST%:%GATEWAY_HTTP_PORT%)...

powershell -Command "try { $tcp = New-Object System.Net.Sockets.TcpClient; $tcp.Connect('%GATEWAY_HTTP_HOST%', %GATEWAY_HTTP_PORT%); $tcp.Close(); exit 0 } catch { exit 1 }" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo         %GREEN%[PASS]%RESET% iot-gateway HTTP port %GATEWAY_HTTP_PORT% is open
    set /a PASSED_CHECKS+=1
) else (
    echo         %RED%[FAIL]%RESET% iot-gateway HTTP port %GATEWAY_HTTP_PORT% is not responding
    echo                Hint: Start iot-gateway with: cd ruoyi-vue-pro ^& mvn spring-boot:run -pl yudao-module-iot/yudao-module-iot-gateway
    set /a FAILED_CHECKS+=1
)
echo.

REM ============================================================
REM 检查 3: iot-gateway 科鼎协议 TCP 端口
REM Requirements: 4.2
REM ============================================================
echo [3/%TOTAL_CHECKS%] Checking iot-gateway Keding TCP port (%GATEWAY_KEDING_HOST%:%GATEWAY_KEDING_PORT%)...

powershell -Command "try { $tcp = New-Object System.Net.Sockets.TcpClient; $tcp.Connect('%GATEWAY_KEDING_HOST%', %GATEWAY_KEDING_PORT%); $tcp.Close(); exit 0 } catch { exit 1 }" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo         %GREEN%[PASS]%RESET% iot-gateway Keding TCP port %GATEWAY_KEDING_PORT% is open
    set /a PASSED_CHECKS+=1
) else (
    echo         %YELLOW%[WARN]%RESET% iot-gateway Keding TCP port %GATEWAY_KEDING_PORT% is not responding
    echo                Note: Keding protocol server may not be enabled in current configuration
    echo                Check gateway.protocol.keding.enabled in application.yaml
    set /a FAILED_CHECKS+=1
)
echo.

REM ============================================================
REM 检查 4: MySQL 连接
REM Requirements: 4.3
REM ============================================================
echo [4/%TOTAL_CHECKS%] Checking MySQL connection (%MYSQL_HOST%:%MYSQL_PORT%)...

powershell -Command "try { $tcp = New-Object System.Net.Sockets.TcpClient; $tcp.Connect('%MYSQL_HOST%', %MYSQL_PORT%); $tcp.Close(); exit 0 } catch { exit 1 }" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo         %GREEN%[PASS]%RESET% MySQL port %MYSQL_PORT% is open
    set /a PASSED_CHECKS+=1
) else (
    echo         %RED%[FAIL]%RESET% MySQL is not responding on port %MYSQL_PORT%
    echo                Hint: Ensure MySQL server is running
    set /a FAILED_CHECKS+=1
)
echo.

REM ============================================================
REM 检查 5: RocketMQ 连接
REM Requirements: 4.3
REM ============================================================
echo [5/%TOTAL_CHECKS%] Checking RocketMQ connection (%ROCKETMQ_HOST%:%ROCKETMQ_PORT%)...

powershell -Command "try { $tcp = New-Object System.Net.Sockets.TcpClient; $tcp.Connect('%ROCKETMQ_HOST%', %ROCKETMQ_PORT%); $tcp.Close(); exit 0 } catch { exit 1 }" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo         %GREEN%[PASS]%RESET% RocketMQ Namesrv port %ROCKETMQ_PORT% is open
    set /a PASSED_CHECKS+=1
) else (
    echo         %RED%[FAIL]%RESET% RocketMQ is not responding on %ROCKETMQ_HOST%:%ROCKETMQ_PORT%
    echo                Hint: Ensure RocketMQ Namesrv is running
    set /a FAILED_CHECKS+=1
)
echo.

REM ============================================================
REM 汇总结果
REM ============================================================
echo ============================================================
echo   Health Check Summary
echo ============================================================
echo.
echo   Total Checks:  %TOTAL_CHECKS%
echo   Passed:        %PASSED_CHECKS%
echo   Failed:        %FAILED_CHECKS%
echo.

if %FAILED_CHECKS% EQU 0 (
    echo   %GREEN%[SUCCESS]%RESET% All services are healthy!
    echo.
    echo   You can now proceed with E2E testing.
    echo.
    exit /b 0
) else (
    echo   %RED%[FAILED]%RESET% Some services are not available.
    echo.
    echo   Please fix the issues above before running E2E tests.
    echo.
    echo   Quick Start Guide:
    echo   ------------------
    echo   1. Start MySQL:     Ensure MySQL server is running
    echo   2. Start RocketMQ:  mqnamesrv ^& mqbroker -n localhost:9876
    echo   3. Start yudao-server:
    echo      cd ruoyi-vue-pro
    echo      mvn spring-boot:run -pl yudao-server
    echo   4. Start iot-gateway:
    echo      cd ruoyi-vue-pro
    echo      mvn spring-boot:run -pl yudao-module-iot/yudao-module-iot-gateway
    echo.
    exit /b 1
)
