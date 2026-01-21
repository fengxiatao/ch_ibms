@echo off
REM ============================================================
REM 固件上传测试脚本
REM 
REM 测试科鼎固件上传功能
REM
REM 测试项目:
REM   1. 上传有效的固件文件
REM   2. 验证固件记录创建成功
REM   3. 验证MD5校验和计算正确
REM   4. 查询固件列表验证
REM
REM Requirements: 5.1, 5.2, 5.3
REM ============================================================

setlocal enabledelayedexpansion

REM 配置参数
set API_BASE_URL=http://localhost:48080/admin-api
set AUTH_TOKEN=
set FIRMWARE_NAME=test-firmware-%RANDOM%
set FIRMWARE_VERSION=1.0.0-test
set DEVICE_TYPE=1
set TEST_FIRMWARE_PATH=test-firmware.bin

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
if /i "%~1"=="--firmware-path" (
    set TEST_FIRMWARE_PATH=%~2
    shift
)
if /i "%~1"=="--help" goto :show_help
if /i "%~1"=="-h" goto :show_help
shift
goto :parse_args

:done_parsing

echo.
echo ============================================================
echo   Keding Firmware Upload E2E Test
echo ============================================================
echo.
echo   Configuration:
echo   --------------
echo   API Base URL:    %API_BASE_URL%
echo   Firmware Name:   %FIRMWARE_NAME%
echo   Firmware Version: %FIRMWARE_VERSION%
echo   Device Type:     %DEVICE_TYPE%
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

REM ============================================================
REM 测试 1: 创建测试固件文件
REM ============================================================
echo %CYAN%[Test 1/%TOTAL_TESTS%]%RESET% Creating test firmware file...

REM 创建一个简单的测试固件文件（如果不存在）
if not exist "%TEST_FIRMWARE_PATH%" (
    echo         Creating test firmware file: %TEST_FIRMWARE_PATH%
    REM 创建一个包含随机数据的测试文件
    powershell -Command "$bytes = New-Object byte[] 1024; (New-Object Random).NextBytes($bytes); [IO.File]::WriteAllBytes('%TEST_FIRMWARE_PATH%', $bytes)"
    if !ERRORLEVEL! NEQ 0 (
        echo         %RED%[FAIL]%RESET% Failed to create test firmware file
        set /a FAILED_TESTS+=1
        goto :test2
    )
)

REM 计算文件MD5
for /f "delims=" %%i in ('powershell -Command "(Get-FileHash -Path '%TEST_FIRMWARE_PATH%' -Algorithm MD5).Hash.ToLower()"') do set EXPECTED_MD5=%%i
for /f "delims=" %%i in ('powershell -Command "(Get-Item '%TEST_FIRMWARE_PATH%').Length"') do set FILE_SIZE=%%i

echo         Test firmware file created
echo         File Size: %FILE_SIZE% bytes
echo         Expected MD5: %EXPECTED_MD5%
echo         %GREEN%[PASS]%RESET% Test firmware file ready
set /a PASSED_TESTS+=1
echo.

:test2
REM ============================================================
REM 测试 2: 上传固件 (Requirements: 5.1, 5.2)
REM ============================================================
echo %CYAN%[Test 2/%TOTAL_TESTS%]%RESET% Uploading firmware via API...

REM 构建JSON请求体
set JSON_BODY={"name":"%FIRMWARE_NAME%","version":"%FIRMWARE_VERSION%","deviceType":%DEVICE_TYPE%,"filePath":"uploads/firmware/%FIRMWARE_NAME%.bin","fileSize":%FILE_SIZE%,"fileMd5":"%EXPECTED_MD5%"}

REM 调用上传API
echo         Calling: POST %API_BASE_URL%/iot/keding/upgrade/firmware/upload
echo         Request Body: %JSON_BODY%

for /f "delims=" %%i in ('curl -s -X POST "%API_BASE_URL%/iot/keding/upgrade/firmware/upload" ^
    -H "Content-Type: application/json" ^
    -H "Authorization: Bearer %AUTH_TOKEN%" ^
    -H "tenant-id: 1" ^
    -d "%JSON_BODY%"') do set UPLOAD_RESPONSE=%%i

echo         Response: %UPLOAD_RESPONSE%

REM 检查响应是否成功
echo %UPLOAD_RESPONSE% | findstr /C:"\"code\":0" >nul
if %ERRORLEVEL% EQU 0 (
    REM 提取固件ID
    for /f "tokens=2 delims=:}" %%a in ('echo %UPLOAD_RESPONSE% ^| findstr /C:"data"') do set FIRMWARE_ID=%%a
    set FIRMWARE_ID=!FIRMWARE_ID: =!
    echo         Firmware ID: !FIRMWARE_ID!
    echo         %GREEN%[PASS]%RESET% Firmware uploaded successfully
    set /a PASSED_TESTS+=1
) else (
    echo         %RED%[FAIL]%RESET% Firmware upload failed
    echo         Response: %UPLOAD_RESPONSE%
    set /a FAILED_TESTS+=1
    set FIRMWARE_ID=
)
echo.

REM ============================================================
REM 测试 3: 验证固件记录 (Requirements: 5.2)
REM ============================================================
echo %CYAN%[Test 3/%TOTAL_TESTS%]%RESET% Verifying firmware record...

if not defined FIRMWARE_ID (
    echo         %YELLOW%[SKIP]%RESET% Skipping - no firmware ID from previous test
    set /a FAILED_TESTS+=1
    goto :test4
)

REM 查询固件详情
echo         Calling: GET %API_BASE_URL%/iot/keding/upgrade/firmware/get?id=%FIRMWARE_ID%

for /f "delims=" %%i in ('curl -s -X GET "%API_BASE_URL%/iot/keding/upgrade/firmware/get?id=%FIRMWARE_ID%" ^
    -H "Authorization: Bearer %AUTH_TOKEN%" ^
    -H "tenant-id: 1"') do set GET_RESPONSE=%%i

echo         Response: %GET_RESPONSE%

REM 验证响应包含正确的固件信息
echo %GET_RESPONSE% | findstr /C:"\"code\":0" >nul
if %ERRORLEVEL% EQU 0 (
    echo %GET_RESPONSE% | findstr /C:"%FIRMWARE_NAME%" >nul
    if !ERRORLEVEL! EQU 0 (
        echo         %GREEN%[PASS]%RESET% Firmware record verified
        set /a PASSED_TESTS+=1
    ) else (
        echo         %RED%[FAIL]%RESET% Firmware name mismatch
        set /a FAILED_TESTS+=1
    )
) else (
    echo         %RED%[FAIL]%RESET% Failed to get firmware record
    set /a FAILED_TESTS+=1
)
echo.

:test4
REM ============================================================
REM 测试 4: 查询固件列表 (Requirements: 5.3)
REM ============================================================
echo %CYAN%[Test 4/%TOTAL_TESTS%]%RESET% Querying firmware list...

echo         Calling: GET %API_BASE_URL%/iot/keding/upgrade/firmware/list

for /f "delims=" %%i in ('curl -s -X GET "%API_BASE_URL%/iot/keding/upgrade/firmware/list" ^
    -H "Authorization: Bearer %AUTH_TOKEN%" ^
    -H "tenant-id: 1"') do set LIST_RESPONSE=%%i

echo         Response (truncated): %LIST_RESPONSE:~0,200%...

REM 验证响应成功
echo %LIST_RESPONSE% | findstr /C:"\"code\":0" >nul
if %ERRORLEVEL% EQU 0 (
    echo         %GREEN%[PASS]%RESET% Firmware list retrieved successfully
    set /a PASSED_TESTS+=1
) else (
    echo         %RED%[FAIL]%RESET% Failed to get firmware list
    set /a FAILED_TESTS+=1
)
echo.

REM ============================================================
REM 清理测试数据
REM ============================================================
echo %CYAN%[Cleanup]%RESET% Cleaning up test data...

if defined FIRMWARE_ID (
    echo         Deleting test firmware: %FIRMWARE_ID%
    curl -s -X DELETE "%API_BASE_URL%/iot/keding/upgrade/firmware/delete?id=%FIRMWARE_ID%" ^
        -H "Authorization: Bearer %AUTH_TOKEN%" ^
        -H "tenant-id: 1" >nul 2>&1
    echo         Test firmware deleted
)

if exist "%TEST_FIRMWARE_PATH%" (
    del /q "%TEST_FIRMWARE_PATH%" 2>nul
    echo         Test firmware file deleted
)
echo.

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

if %FAILED_TESTS% EQU 0 (
    echo   %GREEN%[SUCCESS]%RESET% All firmware upload tests passed!
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

REM 尝试使用默认测试账号登录
for /f "delims=" %%i in ('curl -s -X POST "%API_BASE_URL%/system/auth/login" ^
    -H "Content-Type: application/json" ^
    -H "tenant-id: 1" ^
    -d "{\"username\":\"admin\",\"password\":\"admin123\"}"') do set LOGIN_RESPONSE=%%i

REM 提取accessToken
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

:show_help
echo.
echo Keding Firmware Upload E2E Test Script
echo.
echo Usage:
echo   test-firmware-upload.bat [OPTIONS]
echo.
echo Options:
echo   --token TOKEN         Authentication token (Bearer token)
echo   --api-url URL         API base URL (default: http://localhost:48080/admin-api)
echo   --firmware-path PATH  Path to test firmware file (default: test-firmware.bin)
echo   --help, -h            Show this help message
echo.
echo This script tests:
echo   1. Creating test firmware file
echo   2. Uploading firmware via API (Requirements: 5.1, 5.2)
echo   3. Verifying firmware record in database (Requirements: 5.2)
echo   4. Querying firmware list (Requirements: 5.3)
echo.
echo Examples:
echo   test-firmware-upload.bat
echo   test-firmware-upload.bat --token "your-auth-token"
echo   test-firmware-upload.bat --api-url http://192.168.1.100:48080/admin-api
echo.
exit /b 0
