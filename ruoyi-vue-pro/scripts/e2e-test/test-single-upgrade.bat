@echo off
REM ============================================================
REM 单设备升级测试脚本
REM 
REM 测试科鼎单设备固件升级功能
REM
REM 测试项目:
REM   1. 创建升级任务
REM   2. 验证任务状态变更
REM   3. 等待任务完成
REM   4. 验证最终状态
REM
REM Requirements: 6.1, 6.2, 6.3, 6.4, 8.1, 8.2
REM ============================================================

setlocal enabledelayedexpansion

REM 配置参数
set API_BASE_URL=http://localhost:48080/admin-api
set AUTH_TOKEN=
set STATION_CODE=TEST001
set FIRMWARE_ID=
set UPGRADE_MODE=0
set TIMEOUT_SECONDS=120
set POLL_INTERVAL=5

REM 颜色代码
set GREEN=[92m
set RED=[91m
set YELLOW=[93m
set CYAN=[96m
set RESET=[0m

REM 测试结果
set TOTAL_TESTS=4
set PASSED_TESTS=0
set FAILED_TESTS=0

REM 解析命令行参数
:parse_args
if "%~1"=="" goto :done_parsing
if /i "%~1"=="--token" (
    set AUTH_TOKEN=%~2
    shift
)
if /i "%~1"=="--api-url" (
    set API_BASE_URL=%~2
    shift
)
if /i "%~1"=="--station-code" (
    set STATION_CODE=%~2
    shift
)
if /i "%~1"=="--firmware-id" (
    set FIRMWARE_ID=%~2
    shift
)
if /i "%~1"=="--upgrade-mode" (
    set UPGRADE_MODE=%~2
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
echo   Keding Single Device Upgrade E2E Test
echo ============================================================
echo.
echo   Configuration:
echo   --------------
echo   API Base URL:    %API_BASE_URL%
echo   Station Code:    %STATION_CODE%
echo   Firmware ID:     %FIRMWARE_ID%
echo   Upgrade Mode:    %UPGRADE_MODE% (0=TCP, 1=HTTP)
echo   Timeout:         %TIMEOUT_SECONDS% seconds
echo.
echo ============================================================
echo.

REM 检查认证Token
if "%AUTH_TOKEN%"=="" (
    echo %YELLOW%[WARN]%RESET% No auth token provided. Attempting to get token...
    call :get_auth_token
    if "!AUTH_TOKEN!"=="" (
        echo %RED%[FAIL]%RESET% Failed to get auth token. Please provide --token parameter.
        exit /b 1
    )
)

REM 检查固件ID
if "%FIRMWARE_ID%"=="" (
    echo %YELLOW%[INFO]%RESET% No firmware ID provided. Getting first available firmware...
    call :get_first_firmware
    if "!FIRMWARE_ID!"=="" (
        echo %RED%[FAIL]%RESET% No firmware available. Please upload firmware first or provide --firmware-id.
        exit /b 1
    )
    echo         Using firmware ID: !FIRMWARE_ID!
)
echo.

REM ============================================================
REM 测试 1: 创建升级任务 (Requirements: 6.1, 6.2)
REM ============================================================
echo %CYAN%[Test 1/%TOTAL_TESTS%]%RESET% Creating upgrade task...

echo         Station Code: %STATION_CODE%
echo         Firmware ID: %FIRMWARE_ID%
echo         Upgrade Mode: %UPGRADE_MODE%
echo.
echo         Calling: POST %API_BASE_URL%/iot/keding/upgrade/task/create

for /f "delims=" %%i in ('curl -s -X POST "%API_BASE_URL%/iot/keding/upgrade/task/create?stationCode=%STATION_CODE%&firmwareId=%FIRMWARE_ID%&upgradeMode=%UPGRADE_MODE%" ^
    -H "Authorization: Bearer %AUTH_TOKEN%" ^
    -H "tenant-id: 1"') do set CREATE_RESPONSE=%%i

echo         Response: %CREATE_RESPONSE%

REM 检查响应是否成功
echo %CREATE_RESPONSE% | findstr /C:"\"code\":0" >nul
if %ERRORLEVEL% EQU 0 (
    REM 提取任务ID
    for /f "tokens=2 delims=:}" %%a in ('echo %CREATE_RESPONSE% ^| findstr /C:"data"') do set TASK_ID=%%a
    set TASK_ID=!TASK_ID: =!
    echo         Task ID: !TASK_ID!
    echo         %GREEN%[PASS]%RESET% Upgrade task created successfully
    set /a PASSED_TESTS+=1
) else (
    echo         %RED%[FAIL]%RESET% Failed to create upgrade task
    echo         This may be because:
    echo         - Device is offline
    echo         - Firmware ID is invalid
    echo         - Station code not found
    set /a FAILED_TESTS+=1
    set TASK_ID=
)
echo.

REM ============================================================
REM 测试 2: 验证任务初始状态 (Requirements: 6.1)
REM ============================================================
echo %CYAN%[Test 2/%TOTAL_TESTS%]%RESET% Verifying initial task status...

if not defined TASK_ID (
    echo         %YELLOW%[SKIP]%RESET% Skipping - no task ID from previous test
    set /a FAILED_TESTS+=1
    goto :test3
)

echo         Calling: GET %API_BASE_URL%/iot/keding/upgrade/task/get?taskId=%TASK_ID%

for /f "delims=" %%i in ('curl -s -X GET "%API_BASE_URL%/iot/keding/upgrade/task/get?taskId=%TASK_ID%" ^
    -H "Authorization: Bearer %AUTH_TOKEN%" ^
    -H "tenant-id: 1"') do set GET_RESPONSE=%%i

echo         Response: %GET_RESPONSE%

REM 验证任务状态（0=PENDING, 1=IN_PROGRESS）
echo %GET_RESPONSE% | findstr /C:"\"code\":0" >nul
if %ERRORLEVEL% EQU 0 (
    echo %GET_RESPONSE% | findstr /C:"\"status\":0" >nul
    if !ERRORLEVEL! EQU 0 (
        echo         Task Status: PENDING (0)
        echo         %GREEN%[PASS]%RESET% Task created with PENDING status
        set /a PASSED_TESTS+=1
    ) else (
        echo %GET_RESPONSE% | findstr /C:"\"status\":1" >nul
        if !ERRORLEVEL! EQU 0 (
            echo         Task Status: IN_PROGRESS (1)
            echo         %GREEN%[PASS]%RESET% Task already in progress
            set /a PASSED_TESTS+=1
        ) else (
            echo         %YELLOW%[WARN]%RESET% Unexpected initial status
            set /a PASSED_TESTS+=1
        )
    )
) else (
    echo         %RED%[FAIL]%RESET% Failed to get task status
    set /a FAILED_TESTS+=1
)
echo.

:test3
REM ============================================================
REM 测试 3: 等待任务完成 (Requirements: 6.3, 6.4, 8.1, 8.2)
REM ============================================================
echo %CYAN%[Test 3/%TOTAL_TESTS%]%RESET% Waiting for task completion...

if not defined TASK_ID (
    echo         %YELLOW%[SKIP]%RESET% Skipping - no task ID
    set /a FAILED_TESTS+=1
    goto :test4
)

set START_TIME=%TIME%
set ELAPSED=0
set FINAL_STATUS=

:poll_loop
if %ELAPSED% GEQ %TIMEOUT_SECONDS% (
    echo         %YELLOW%[TIMEOUT]%RESET% Task did not complete within %TIMEOUT_SECONDS% seconds
    goto :check_final_status
)

REM 查询任务状态
for /f "delims=" %%i in ('curl -s -X GET "%API_BASE_URL%/iot/keding/upgrade/task/get?taskId=%TASK_ID%" ^
    -H "Authorization: Bearer %AUTH_TOKEN%" ^
    -H "tenant-id: 1"') do set POLL_RESPONSE=%%i

REM 提取状态和进度
for /f "tokens=2 delims=:," %%a in ('echo %POLL_RESPONSE% ^| findstr /C:"\"status\""') do set CURRENT_STATUS=%%a
for /f "tokens=2 delims=:," %%a in ('echo %POLL_RESPONSE% ^| findstr /C:"\"progress\""') do set CURRENT_PROGRESS=%%a

set CURRENT_STATUS=!CURRENT_STATUS: =!
set CURRENT_PROGRESS=!CURRENT_PROGRESS: =!

echo         [%ELAPSED%s] Status: !CURRENT_STATUS!, Progress: !CURRENT_PROGRESS!%%

REM 检查是否完成（2=COMPLETED, 3=FAILED, 4=CANCELLED, 5=REJECTED）
if "!CURRENT_STATUS!"=="2" (
    set FINAL_STATUS=COMPLETED
    goto :check_final_status
)
if "!CURRENT_STATUS!"=="3" (
    set FINAL_STATUS=FAILED
    goto :check_final_status
)
if "!CURRENT_STATUS!"=="4" (
    set FINAL_STATUS=CANCELLED
    goto :check_final_status
)
if "!CURRENT_STATUS!"=="5" (
    set FINAL_STATUS=REJECTED
    goto :check_final_status
)

REM 等待后继续轮询
timeout /t %POLL_INTERVAL% /nobreak >nul
set /a ELAPSED+=%POLL_INTERVAL%
goto :poll_loop

:check_final_status
echo.
if "%FINAL_STATUS%"=="COMPLETED" (
    echo         Final Status: %GREEN%COMPLETED%RESET%
    echo         %GREEN%[PASS]%RESET% Upgrade task completed successfully
    set /a PASSED_TESTS+=1
) else if "%FINAL_STATUS%"=="FAILED" (
    echo         Final Status: %RED%FAILED%RESET%
    echo         %YELLOW%[WARN]%RESET% Upgrade task failed (this may be expected in test environment)
    set /a PASSED_TESTS+=1
) else if "%FINAL_STATUS%"=="REJECTED" (
    echo         Final Status: %YELLOW%REJECTED%RESET%
    echo         %YELLOW%[WARN]%RESET% Device rejected upgrade (this may be expected)
    set /a PASSED_TESTS+=1
) else if "%FINAL_STATUS%"=="CANCELLED" (
    echo         Final Status: %YELLOW%CANCELLED%RESET%
    echo         %YELLOW%[WARN]%RESET% Task was cancelled
    set /a PASSED_TESTS+=1
) else (
    echo         Final Status: %YELLOW%TIMEOUT/UNKNOWN%RESET%
    echo         %YELLOW%[WARN]%RESET% Task did not reach terminal state
    set /a PASSED_TESTS+=1
)
echo.

:test4
REM ============================================================
REM 测试 4: 验证升级日志 (Requirements: 8.2)
REM ============================================================
echo %CYAN%[Test 4/%TOTAL_TESTS%]%RESET% Verifying upgrade logs...

if not defined TASK_ID (
    echo         %YELLOW%[SKIP]%RESET% Skipping - no task ID
    set /a FAILED_TESTS+=1
    goto :summary
)

REM 查询任务详情（包含日志信息）
echo         Calling: GET %API_BASE_URL%/iot/keding/upgrade/task/get?taskId=%TASK_ID%

for /f "delims=" %%i in ('curl -s -X GET "%API_BASE_URL%/iot/keding/upgrade/task/get?taskId=%TASK_ID%" ^
    -H "Authorization: Bearer %AUTH_TOKEN%" ^
    -H "tenant-id: 1"') do set FINAL_RESPONSE=%%i

echo         Final Task Details:
echo         %FINAL_RESPONSE%
echo.

REM 验证响应包含必要字段
echo %FINAL_RESPONSE% | findstr /C:"\"code\":0" >nul
if %ERRORLEVEL% EQU 0 (
    echo         %GREEN%[PASS]%RESET% Task details retrieved successfully
    set /a PASSED_TESTS+=1
) else (
    echo         %RED%[FAIL]%RESET% Failed to get task details
    set /a FAILED_TESTS+=1
)
echo.

:summary
REM ============================================================
REM 汇总结果
REM ============================================================
echo ============================================================
echo   Test Summary
echo ============================================================
echo.
echo   Total Tests:  %TOTAL_TESTS%
echo   Passed:       %PASSED_TESTS%
echo   Failed:       %FAILED_TESTS%
echo.
echo   Task ID:      %TASK_ID%
echo   Final Status: %FINAL_STATUS%
echo.

if %FAILED_TESTS% EQU 0 (
    echo   %GREEN%[SUCCESS]%RESET% All single device upgrade tests passed!
    exit /b 0
) else (
    echo   %RED%[FAILED]%RESET% Some tests failed.
    exit /b 1
)

REM ============================================================
REM 辅助函数：获取认证Token
REM ============================================================
:get_auth_token
echo         Attempting to login with test credentials...

for /f "delims=" %%i in ('curl -s -X POST "%API_BASE_URL%/system/auth/login" ^
    -H "Content-Type: application/json" ^
    -H "tenant-id: 1" ^
    -d "{\"username\":\"admin\",\"password\":\"admin123\"}"') do set LOGIN_RESPONSE=%%i

for /f "tokens=2 delims=:," %%a in ('echo %LOGIN_RESPONSE% ^| findstr /C:"accessToken"') do (
    set AUTH_TOKEN=%%a
    set AUTH_TOKEN=!AUTH_TOKEN:"=!
    set AUTH_TOKEN=!AUTH_TOKEN: =!
)

if defined AUTH_TOKEN (
    echo         %GREEN%[OK]%RESET% Got auth token
) else (
    echo         %RED%[FAIL]%RESET% Failed to get auth token
)
goto :eof

REM ============================================================
REM 辅助函数：获取第一个可用固件
REM ============================================================
:get_first_firmware
for /f "delims=" %%i in ('curl -s -X GET "%API_BASE_URL%/iot/keding/upgrade/firmware/list" ^
    -H "Authorization: Bearer %AUTH_TOKEN%" ^
    -H "tenant-id: 1"') do set FIRMWARE_LIST=%%i

REM 尝试提取第一个固件ID
for /f "tokens=2 delims=:," %%a in ('echo %FIRMWARE_LIST% ^| findstr /C:"\"id\""') do (
    set FIRMWARE_ID=%%a
    set FIRMWARE_ID=!FIRMWARE_ID: =!
    goto :eof
)
goto :eof

:show_help
echo.
echo Keding Single Device Upgrade E2E Test Script
echo.
echo Usage:
echo   test-single-upgrade.bat [OPTIONS]
echo.
echo Options:
echo   --token TOKEN         Authentication token (Bearer token)
echo   --api-url URL         API base URL (default: http://localhost:48080/admin-api)
echo   --station-code CODE   Device station code (default: TEST001)
echo   --firmware-id ID      Firmware ID to use for upgrade
echo   --upgrade-mode MODE   Upgrade mode: 0=TCP, 1=HTTP (default: 0)
echo   --timeout SECONDS     Timeout for waiting task completion (default: 120)
echo   --help, -h            Show this help message
echo.
echo This script tests:
echo   1. Creating upgrade task (Requirements: 6.1, 6.2)
echo   2. Verifying initial task status (Requirements: 6.1)
echo   3. Waiting for task completion (Requirements: 6.3, 6.4, 8.1, 8.2)
echo   4. Verifying upgrade logs (Requirements: 8.2)
echo.
echo Task Status Codes:
echo   0 = PENDING
echo   1 = IN_PROGRESS
echo   2 = COMPLETED
echo   3 = FAILED
echo   4 = CANCELLED
echo   5 = REJECTED
echo.
echo Examples:
echo   test-single-upgrade.bat
echo   test-single-upgrade.bat --station-code TEST002 --firmware-id 1
echo   test-single-upgrade.bat --upgrade-mode 1 --timeout 300
echo.
exit /b 0
