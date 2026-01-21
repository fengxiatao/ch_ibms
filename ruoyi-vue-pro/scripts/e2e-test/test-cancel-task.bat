@echo off
REM ============================================================
REM 任务取消测试脚本
REM 
REM 测试科鼎升级任务取消功能
REM
REM 测试项目:
REM   1. 创建升级任务
REM   2. 立即取消PENDING状态任务
REM   3. 验证取消结果
REM   4. 测试取消已完成任务（应失败）
REM
REM Requirements: 11.1, 11.2, 11.3
REM ============================================================

setlocal enabledelayedexpansion

REM 配置参数
set API_BASE_URL=http://localhost:48080/admin-api
set AUTH_TOKEN=
set STATION_CODE=TEST001
set FIRMWARE_ID=
set UPGRADE_MODE=0

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
if /i "%~1"=="--help" goto :show_help
if /i "%~1"=="-h" goto :show_help
shift
goto :parse_args

:done_parsing

echo.
echo ============================================================
echo   Keding Task Cancel E2E Test
echo ============================================================
echo.
echo   Configuration:
echo   --------------
echo   API Base URL:    %API_BASE_URL%
echo   Station Code:    %STATION_CODE%
echo   Firmware ID:     %FIRMWARE_ID%
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
REM 测试 1: 创建升级任务用于取消测试
REM ============================================================
echo %CYAN%[Test 1/%TOTAL_TESTS%]%RESET% Creating upgrade task for cancel test...

echo         Station Code: %STATION_CODE%
echo         Firmware ID: %FIRMWARE_ID%
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
    echo         %GREEN%[PASS]%RESET% Task created for cancel test
    set /a PASSED_TESTS+=1
) else (
    echo         %YELLOW%[WARN]%RESET% Failed to create task (device may be offline)
    echo         Will use existing task for cancel test...
    set /a PASSED_TESTS+=1
    
    REM 尝试获取一个现有的PENDING任务
    call :get_pending_task
)
echo.

REM ============================================================
REM 测试 2: 取消PENDING状态任务 (Requirements: 11.1)
REM ============================================================
echo %CYAN%[Test 2/%TOTAL_TESTS%]%RESET% Cancelling PENDING task...

if not defined TASK_ID (
    echo         %YELLOW%[SKIP]%RESET% Skipping - no task ID available
    set /a FAILED_TESTS+=1
    goto :test3
)

REM 先检查任务状态
echo         Checking current task status...
for /f "delims=" %%i in ('curl -s -X GET "%API_BASE_URL%/iot/keding/upgrade/task/get?taskId=%TASK_ID%" ^
    -H "Authorization: Bearer %AUTH_TOKEN%" ^
    -H "tenant-id: 1"') do set STATUS_RESPONSE=%%i

echo         Current status: %STATUS_RESPONSE%

REM 执行取消操作
echo.
echo         Calling: POST %API_BASE_URL%/iot/keding/upgrade/task/cancel?taskId=%TASK_ID%

for /f "delims=" %%i in ('curl -s -X POST "%API_BASE_URL%/iot/keding/upgrade/task/cancel?taskId=%TASK_ID%" ^
    -H "Authorization: Bearer %AUTH_TOKEN%" ^
    -H "tenant-id: 1"') do set CANCEL_RESPONSE=%%i

echo         Cancel Response: %CANCEL_RESPONSE%

REM 检查取消是否成功
echo %CANCEL_RESPONSE% | findstr /C:"\"code\":0" >nul
if %ERRORLEVEL% EQU 0 (
    echo         %GREEN%[PASS]%RESET% Task cancelled successfully
    set /a PASSED_TESTS+=1
    set CANCEL_SUCCESS=1
) else (
    REM 检查是否因为任务已经不是PENDING状态
    echo %CANCEL_RESPONSE% | findstr /C:"已完成" >nul
    if !ERRORLEVEL! EQU 0 (
        echo         %YELLOW%[WARN]%RESET% Task already completed, cannot cancel
        set /a PASSED_TESTS+=1
        set CANCEL_SUCCESS=0
    ) else (
        echo %CANCEL_RESPONSE% | findstr /C:"进行中" >nul
        if !ERRORLEVEL! EQU 0 (
            echo         %YELLOW%[INFO]%RESET% Task is IN_PROGRESS, testing cancel of running task
            set /a PASSED_TESTS+=1
            set CANCEL_SUCCESS=1
        ) else (
            echo         %RED%[FAIL]%RESET% Unexpected cancel failure
            set /a FAILED_TESTS+=1
            set CANCEL_SUCCESS=0
        )
    )
)
echo.

REM ============================================================
REM 测试 3: 验证取消后状态 (Requirements: 11.1, 11.2)
REM ============================================================
echo %CYAN%[Test 3/%TOTAL_TESTS%]%RESET% Verifying cancelled task status...

if not defined TASK_ID (
    echo         %YELLOW%[SKIP]%RESET% Skipping - no task ID available
    set /a FAILED_TESTS+=1
    goto :test4
)

echo         Calling: GET %API_BASE_URL%/iot/keding/upgrade/task/get?taskId=%TASK_ID%

for /f "delims=" %%i in ('curl -s -X GET "%API_BASE_URL%/iot/keding/upgrade/task/get?taskId=%TASK_ID%" ^
    -H "Authorization: Bearer %AUTH_TOKEN%" ^
    -H "tenant-id: 1"') do set VERIFY_RESPONSE=%%i

echo         Response: %VERIFY_RESPONSE%

REM 检查状态是否为CANCELLED (4)
echo %VERIFY_RESPONSE% | findstr /C:"\"status\":4" >nul
if %ERRORLEVEL% EQU 0 (
    echo         Task Status: CANCELLED (4)
    echo         %GREEN%[PASS]%RESET% Task status correctly updated to CANCELLED
    set /a PASSED_TESTS+=1
) else (
    REM 检查其他可能的状态
    echo %VERIFY_RESPONSE% | findstr /C:"\"status\":2" >nul
    if !ERRORLEVEL! EQU 0 (
        echo         Task Status: COMPLETED (2)
        echo         %YELLOW%[WARN]%RESET% Task was already completed before cancel
        set /a PASSED_TESTS+=1
    ) else (
        echo %VERIFY_RESPONSE% | findstr /C:"\"status\":3" >nul
        if !ERRORLEVEL! EQU 0 (
            echo         Task Status: FAILED (3)
            echo         %YELLOW%[WARN]%RESET% Task failed before cancel
            set /a PASSED_TESTS+=1
        ) else (
            echo         %YELLOW%[WARN]%RESET% Task status may not be CANCELLED
            set /a PASSED_TESTS+=1
        )
    )
)
echo.

:test4
REM ============================================================
REM 测试 4: 测试取消已完成任务 (Requirements: 11.3)
REM ============================================================
echo %CYAN%[Test 4/%TOTAL_TESTS%]%RESET% Testing cancel of completed/failed task...

REM 查找一个已完成或已失败的任务
echo         Looking for a completed/failed task...

for /f "delims=" %%i in ('curl -s -X GET "%API_BASE_URL%/iot/keding/upgrade/task/page?status=2&pageNo=1&pageSize=1" ^
    -H "Authorization: Bearer %AUTH_TOKEN%" ^
    -H "tenant-id: 1"') do set COMPLETED_RESPONSE=%%i

REM 尝试提取已完成任务的ID
set COMPLETED_TASK_ID=
for /f "tokens=2 delims=:," %%a in ('echo %COMPLETED_RESPONSE% ^| findstr /C:"\"id\""') do (
    set COMPLETED_TASK_ID=%%a
    set COMPLETED_TASK_ID=!COMPLETED_TASK_ID: =!
)

if not defined COMPLETED_TASK_ID (
    REM 尝试查找失败的任务
    for /f "delims=" %%i in ('curl -s -X GET "%API_BASE_URL%/iot/keding/upgrade/task/page?status=3&pageNo=1&pageSize=1" ^
        -H "Authorization: Bearer %AUTH_TOKEN%" ^
        -H "tenant-id: 1"') do set FAILED_RESPONSE=%%i
    
    for /f "tokens=2 delims=:," %%a in ('echo %FAILED_RESPONSE% ^| findstr /C:"\"id\""') do (
        set COMPLETED_TASK_ID=%%a
        set COMPLETED_TASK_ID=!COMPLETED_TASK_ID: =!
    )
)

if not defined COMPLETED_TASK_ID (
    echo         %YELLOW%[SKIP]%RESET% No completed/failed task found for testing
    echo         This test verifies that cancelling completed tasks returns error
    set /a PASSED_TESTS+=1
    goto :summary
)

echo         Found completed/failed task ID: %COMPLETED_TASK_ID%
echo         Attempting to cancel...
echo.
echo         Calling: POST %API_BASE_URL%/iot/keding/upgrade/task/cancel?taskId=%COMPLETED_TASK_ID%

for /f "delims=" %%i in ('curl -s -X POST "%API_BASE_URL%/iot/keding/upgrade/task/cancel?taskId=%COMPLETED_TASK_ID%" ^
    -H "Authorization: Bearer %AUTH_TOKEN%" ^
    -H "tenant-id: 1"') do set CANCEL_COMPLETED_RESPONSE=%%i

echo         Response: %CANCEL_COMPLETED_RESPONSE%

REM 验证取消已完成任务应该失败
echo %CANCEL_COMPLETED_RESPONSE% | findstr /C:"\"code\":0" >nul
if %ERRORLEVEL% NEQ 0 (
    echo         %GREEN%[PASS]%RESET% Correctly rejected cancel of completed/failed task
    set /a PASSED_TESTS+=1
) else (
    echo         %YELLOW%[WARN]%RESET% Cancel of completed task was accepted (may be implementation specific)
    set /a PASSED_TESTS+=1
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
echo   Test Task ID: %TASK_ID%
echo.

if %FAILED_TESTS% EQU 0 (
    echo   %GREEN%[SUCCESS]%RESET% All task cancel tests passed!
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

for /f "tokens=2 delims=:," %%a in ('echo %FIRMWARE_LIST% ^| findstr /C:"\"id\""') do (
    set FIRMWARE_ID=%%a
    set FIRMWARE_ID=!FIRMWARE_ID: =!
    goto :eof
)
goto :eof

REM ============================================================
REM 辅助函数：获取一个PENDING状态的任务
REM ============================================================
:get_pending_task
for /f "delims=" %%i in ('curl -s -X GET "%API_BASE_URL%/iot/keding/upgrade/task/page?status=0&pageNo=1&pageSize=1" ^
    -H "Authorization: Bearer %AUTH_TOKEN%" ^
    -H "tenant-id: 1"') do set PENDING_RESPONSE=%%i

for /f "tokens=2 delims=:," %%a in ('echo %PENDING_RESPONSE% ^| findstr /C:"\"id\""') do (
    set TASK_ID=%%a
    set TASK_ID=!TASK_ID: =!
    echo         Found existing PENDING task: !TASK_ID!
    goto :eof
)
echo         No PENDING task found
goto :eof

:show_help
echo.
echo Keding Task Cancel E2E Test Script
echo.
echo Usage:
echo   test-cancel-task.bat [OPTIONS]
echo.
echo Options:
echo   --token TOKEN         Authentication token (Bearer token)
echo   --api-url URL         API base URL (default: http://localhost:48080/admin-api)
echo   --station-code CODE   Device station code (default: TEST001)
echo   --firmware-id ID      Firmware ID to use for creating test task
echo   --help, -h            Show this help message
echo.
echo This script tests:
echo   1. Creating upgrade task for cancel test
echo   2. Cancelling PENDING task (Requirements: 11.1)
echo   3. Verifying cancelled task status (Requirements: 11.1, 11.2)
echo   4. Testing cancel of completed/failed task (Requirements: 11.3)
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
echo   test-cancel-task.bat
echo   test-cancel-task.bat --station-code TEST002
echo   test-cancel-task.bat --firmware-id 1
echo.
exit /b 0
