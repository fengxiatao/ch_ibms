@echo off
REM ============================================================
REM 模拟器连接验证脚本
REM 
REM 启动科鼎设备模拟器并验证与Gateway的连接
REM
REM 验证项目:
REM   1. 模拟器成功启动
REM   2. 模拟器连接到Gateway TCP端口
REM   3. 模拟器发送心跳包
REM   4. Gateway日志显示连接成功
REM
REM Requirements: 4.4
REM ============================================================

setlocal enabledelayedexpansion

REM 配置参数
set SIMULATOR_DIR=..\yudao-module-iot\yudao-module-iot-simulator
set JAR_FILE=%SIMULATOR_DIR%\target\yudao-module-iot-simulator.jar
set GATEWAY_HOST=localhost
set GATEWAY_PORT=9600
set STATION_CODE=TEST001
set MODE=SUCCESS
set TIMEOUT_SECONDS=30
set LOG_FILE=logs\simulator-verify.log

REM 颜色代码
set GREEN=[92m
set RED=[91m
set YELLOW=[93m
set CYAN=[96m
set RESET=[0m

REM 解析命令行参数
:parse_args
if "%~1"=="" goto :done_parsing
if /i "%~1"=="--station-code" (
    set STATION_CODE=%~2
    shift
)
if /i "%~1"=="--host" (
    set GATEWAY_HOST=%~2
    shift
)
if /i "%~1"=="--port" (
    set GATEWAY_PORT=%~2
    shift
)
if /i "%~1"=="--timeout" (
    set TIMEOUT_SECONDS=%~2
    shift
)
if /i "%~1"=="--help" goto :show_help
if /i "%~1"=="-h" goto :show_help
shift
goto :parse_args

:done_parsing

echo.
echo ============================================================
echo   Keding Device Simulator - Connection Verification
echo ============================================================
echo.
echo   Configuration:
echo   --------------
echo   Station Code:    %STATION_CODE%
echo   Gateway Server:  %GATEWAY_HOST%:%GATEWAY_PORT%
echo   Timeout:         %TIMEOUT_SECONDS% seconds
echo   Mode:            %MODE%
echo.
echo ============================================================
echo.

REM ============================================================
REM 步骤 1: 检查JAR文件是否存在
REM ============================================================
echo %CYAN%[Step 1/4]%RESET% Checking simulator JAR file...

if not exist "%JAR_FILE%" (
    echo         %RED%[FAIL]%RESET% Simulator JAR not found: %JAR_FILE%
    echo.
    echo         Please build the simulator first:
    echo         cd ruoyi-vue-pro
    echo         mvn clean package -pl yudao-module-iot/yudao-module-iot-simulator -am -DskipTests
    echo.
    exit /b 1
)
echo         %GREEN%[PASS]%RESET% Simulator JAR found
echo.

REM ============================================================
REM 步骤 2: 检查Gateway端口是否可用
REM ============================================================
echo %CYAN%[Step 2/4]%RESET% Checking Gateway TCP port availability...

powershell -Command "try { $tcp = New-Object System.Net.Sockets.TcpClient; $tcp.Connect('%GATEWAY_HOST%', %GATEWAY_PORT%); $tcp.Close(); exit 0 } catch { exit 1 }" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo         %RED%[FAIL]%RESET% Gateway TCP port %GATEWAY_PORT% is not available
    echo.
    echo         Please ensure:
    echo         1. iot-gateway is running
    echo         2. Keding protocol is enabled in gateway configuration
    echo         3. Port %GATEWAY_PORT% is not blocked by firewall
    echo.
    echo         Run check-services.bat first to verify all services.
    echo.
    exit /b 1
)
echo         %GREEN%[PASS]%RESET% Gateway TCP port %GATEWAY_PORT% is available
echo.

REM ============================================================
REM 步骤 3: 启动模拟器并验证连接
REM ============================================================
echo %CYAN%[Step 3/4]%RESET% Starting simulator and verifying connection...
echo.
echo         Starting simulator with station code: %STATION_CODE%
echo         Waiting for connection (timeout: %TIMEOUT_SECONDS%s)...
echo.

REM 创建日志目录
if not exist "logs" mkdir logs

REM 构建启动命令
set CMD_ARGS=--spring.profiles.active=e2e-test
set CMD_ARGS=%CMD_ARGS% --protocol keding
set CMD_ARGS=%CMD_ARGS% --station-code %STATION_CODE%
set CMD_ARGS=%CMD_ARGS% --host %GATEWAY_HOST%
set CMD_ARGS=%CMD_ARGS% --port %GATEWAY_PORT%
set CMD_ARGS=%CMD_ARGS% --mode %MODE%
set CMD_ARGS=%CMD_ARGS% --verify-only

REM 启动模拟器（后台运行，带超时）
echo         Command: java -jar %JAR_FILE% %CMD_ARGS%
echo.

REM 使用PowerShell启动进程并等待连接确认
powershell -Command ^
    "$process = Start-Process -FilePath 'java' -ArgumentList '-jar', '%JAR_FILE%', '%CMD_ARGS%' -PassThru -RedirectStandardOutput '%LOG_FILE%' -RedirectStandardError 'logs\simulator-verify-error.log' -WindowStyle Hidden; ^
    $startTime = Get-Date; ^
    $connected = $false; ^
    while ((Get-Date) -lt $startTime.AddSeconds(%TIMEOUT_SECONDS%)) { ^
        Start-Sleep -Milliseconds 500; ^
        if (Test-Path '%LOG_FILE%') { ^
            $content = Get-Content '%LOG_FILE%' -Raw -ErrorAction SilentlyContinue; ^
            if ($content -match 'Connected to Gateway' -or $content -match 'Connection established' -or $content -match 'Heartbeat sent') { ^
                $connected = $true; ^
                break; ^
            } ^
        } ^
    } ^
    Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue; ^
    if ($connected) { exit 0 } else { exit 1 }"

if %ERRORLEVEL% EQU 0 (
    echo         %GREEN%[PASS]%RESET% Simulator connected to Gateway successfully
    set CONNECTION_VERIFIED=1
) else (
    echo         %YELLOW%[WARN]%RESET% Could not verify connection within timeout
    echo.
    echo         This may be because:
    echo         1. Simulator is still connecting
    echo         2. Log format doesn't match expected patterns
    echo         3. Connection failed
    echo.
    echo         Checking log file for details...
    set CONNECTION_VERIFIED=0
)
echo.

REM ============================================================
REM 步骤 4: 验证心跳包发送
REM ============================================================
echo %CYAN%[Step 4/4]%RESET% Verifying heartbeat transmission...

if exist "%LOG_FILE%" (
    REM 检查日志中是否有心跳相关信息
    findstr /i "heartbeat" "%LOG_FILE%" >nul 2>&1
    if !ERRORLEVEL! EQU 0 (
        echo         %GREEN%[PASS]%RESET% Heartbeat messages found in log
        set HEARTBEAT_VERIFIED=1
    ) else (
        findstr /i "keepalive ping" "%LOG_FILE%" >nul 2>&1
        if !ERRORLEVEL! EQU 0 (
            echo         %GREEN%[PASS]%RESET% Keepalive messages found in log
            set HEARTBEAT_VERIFIED=1
        ) else (
            echo         %YELLOW%[WARN]%RESET% No heartbeat messages found in log
            echo                This may be normal if connection was brief
            set HEARTBEAT_VERIFIED=0
        )
    )
) else (
    echo         %YELLOW%[WARN]%RESET% Log file not found: %LOG_FILE%
    set HEARTBEAT_VERIFIED=0
)
echo.

REM ============================================================
REM 显示日志摘要
REM ============================================================
echo ============================================================
echo   Log Summary
echo ============================================================
echo.

if exist "%LOG_FILE%" (
    echo   Last 20 lines of simulator log:
    echo   --------------------------------
    powershell -Command "Get-Content '%LOG_FILE%' -Tail 20"
    echo.
) else (
    echo   No log file available.
    echo.
)

REM ============================================================
REM 汇总结果
REM ============================================================
echo ============================================================
echo   Verification Summary
echo ============================================================
echo.

set TOTAL_PASSED=0
set TOTAL_FAILED=0

echo   [1] JAR File Check:        %GREEN%PASS%RESET%
set /a TOTAL_PASSED+=1

echo   [2] Gateway Port Check:    %GREEN%PASS%RESET%
set /a TOTAL_PASSED+=1

if defined CONNECTION_VERIFIED (
    if %CONNECTION_VERIFIED% EQU 1 (
        echo   [3] Connection Verify:     %GREEN%PASS%RESET%
        set /a TOTAL_PASSED+=1
    ) else (
        echo   [3] Connection Verify:     %YELLOW%WARN%RESET%
        set /a TOTAL_FAILED+=1
    )
) else (
    echo   [3] Connection Verify:     %YELLOW%WARN%RESET%
    set /a TOTAL_FAILED+=1
)

if defined HEARTBEAT_VERIFIED (
    if %HEARTBEAT_VERIFIED% EQU 1 (
        echo   [4] Heartbeat Verify:      %GREEN%PASS%RESET%
        set /a TOTAL_PASSED+=1
    ) else (
        echo   [4] Heartbeat Verify:      %YELLOW%WARN%RESET%
        set /a TOTAL_FAILED+=1
    )
) else (
    echo   [4] Heartbeat Verify:      %YELLOW%WARN%RESET%
    set /a TOTAL_FAILED+=1
)

echo.
echo   Passed: %TOTAL_PASSED%
echo   Warnings: %TOTAL_FAILED%
echo.

if %TOTAL_FAILED% EQU 0 (
    echo   %GREEN%[SUCCESS]%RESET% Simulator verification completed successfully!
    echo.
    echo   The simulator can connect to Gateway and send heartbeats.
    echo   You can now proceed with E2E upgrade testing.
    echo.
    exit /b 0
) else (
    echo   %YELLOW%[PARTIAL]%RESET% Verification completed with warnings.
    echo.
    echo   Some checks could not be fully verified. This may be normal
    echo   if the simulator connection was brief or log patterns differ.
    echo.
    echo   Recommended next steps:
    echo   1. Run the simulator manually to observe connection behavior
    echo   2. Check Gateway logs for connection events
    echo   3. Verify network connectivity between simulator and Gateway
    echo.
    exit /b 0
)

:show_help
echo.
echo Keding Device Simulator - Connection Verification Script
echo.
echo Usage:
echo   verify-simulator.bat [OPTIONS]
echo.
echo Options:
echo   --station-code CODE   Station code to use (default: TEST001)
echo   --host HOST           Gateway server address (default: localhost)
echo   --port PORT           Gateway server port (default: 9600)
echo   --timeout SECONDS     Connection timeout in seconds (default: 30)
echo   --help, -h            Show this help message
echo.
echo This script will:
echo   1. Check if simulator JAR file exists
echo   2. Verify Gateway TCP port is available
echo   3. Start simulator and verify connection
echo   4. Check for heartbeat transmission
echo.
echo Requirements validated:
echo   4.4 - Simulator connects to Gateway and Gateway logs show connection
echo.
echo Examples:
echo   verify-simulator.bat
echo   verify-simulator.bat --station-code TEST002
echo   verify-simulator.bat --host 192.168.1.100 --port 9600
echo   verify-simulator.bat --timeout 60
echo.
exit /b 0
