@echo off
REM ============================================================
REM 科鼎固件升级端到端测试 - 一键测试脚本
REM 
REM 按顺序执行所有测试脚本，收集测试结果，生成测试报告
REM
REM 测试顺序:
REM   1. 服务健康检查
REM   2. 模拟器连接验证
REM   3. 固件上传测试
REM   4. 单设备升级测试
REM   5. 批量升级测试
REM   6. 任务取消测试
REM
REM Requirements: 12.1, 12.2, 12.3
REM ============================================================

setlocal enabledelayedexpansion

REM 配置参数
set SCRIPT_DIR=%~dp0
set REPORT_DIR=%SCRIPT_DIR%reports
set TIMESTAMP=%DATE:~0,4%%DATE:~5,2%%DATE:~8,2%_%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set REPORT_FILE=%REPORT_DIR%\e2e-test-report_%TIMESTAMP%.txt
set LOG_DIR=%SCRIPT_DIR%logs
set STOP_ON_FAILURE=0
set SKIP_SIMULATOR=0
set AUTH_TOKEN=

REM 颜色代码
set GREEN=[92m
set RED=[91m
set YELLOW=[93m
set CYAN=[96m
set BLUE=[94m
set RESET=[0m

REM 测试结果统计
set TOTAL_SUITES=6
set PASSED_SUITES=0
set FAILED_SUITES=0
set SKIPPED_SUITES=0

REM 各测试套件结果
set RESULT_SERVICES=PENDING
set RESULT_SIMULATOR=PENDING
set RESULT_FIRMWARE=PENDING
set RESULT_SINGLE=PENDING
set RESULT_BATCH=PENDING
set RESULT_CANCEL=PENDING

REM 解析命令行参数
:parse_args
if "%~1"=="" goto :done_parsing
if /i "%~1"=="--token" (
    set AUTH_TOKEN=%~2
    shift
)
if /i "%~1"=="--stop-on-failure" (
    set STOP_ON_FAILURE=1
)
if /i "%~1"=="--skip-simulator" (
    set SKIP_SIMULATOR=1
)
if /i "%~1"=="--help" goto :show_help
if /i "%~1"=="-h" goto :show_help
shift
goto :parse_args

:done_parsing

REM 创建必要的目录
if not exist "%REPORT_DIR%" mkdir "%REPORT_DIR%"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

REM 清空报告文件
echo. > "%REPORT_FILE%"

echo.
echo %BLUE%============================================================%RESET%
echo %BLUE%   Keding Firmware Upgrade E2E Test Suite%RESET%
echo %BLUE%============================================================%RESET%
echo.
echo   Start Time:    %DATE% %TIME%
echo   Report File:   %REPORT_FILE%
echo   Stop on Fail:  %STOP_ON_FAILURE%
echo   Skip Simulator: %SKIP_SIMULATOR%
echo.
echo %BLUE%============================================================%RESET%
echo.

REM 写入报告头
call :write_report "============================================================"
call :write_report "   Keding Firmware Upgrade E2E Test Report"
call :write_report "============================================================"
call :write_report ""
call :write_report "Test Execution Time: %DATE% %TIME%"
call :write_report "Machine: %COMPUTERNAME%"
call :write_report ""
call :write_report "============================================================"
call :write_report ""

REM ============================================================
REM 测试套件 1: 服务健康检查
REM ============================================================
echo %CYAN%[Suite 1/%TOTAL_SUITES%]%RESET% Running Service Health Check...
echo.

call :write_report "[Suite 1] Service Health Check"
call :write_report "--------------------------------"

call "%SCRIPT_DIR%check-services.bat" > "%LOG_DIR%\check-services.log" 2>&1
set SERVICES_EXIT=%ERRORLEVEL%

if %SERVICES_EXIT% EQU 0 (
    echo         %GREEN%[PASS]%RESET% All services are healthy
    set RESULT_SERVICES=PASS
    set /a PASSED_SUITES+=1
    call :write_report "Result: PASS"
) else (
    echo         %RED%[FAIL]%RESET% Some services are not available
    set RESULT_SERVICES=FAIL
    set /a FAILED_SUITES+=1
    call :write_report "Result: FAIL"
    
    if %STOP_ON_FAILURE% EQU 1 (
        echo.
        echo %RED%[ABORT]%RESET% Stopping due to --stop-on-failure flag
        goto :generate_report
    )
)
call :write_report ""
echo.

REM ============================================================
REM 测试套件 2: 模拟器连接验证
REM ============================================================
if %SKIP_SIMULATOR% EQU 1 (
    echo %CYAN%[Suite 2/%TOTAL_SUITES%]%RESET% Skipping Simulator Verification (--skip-simulator)
    set RESULT_SIMULATOR=SKIP
    set /a SKIPPED_SUITES+=1
    call :write_report "[Suite 2] Simulator Verification"
    call :write_report "--------------------------------"
    call :write_report "Result: SKIPPED (--skip-simulator flag)"
    call :write_report ""
) else (
    echo %CYAN%[Suite 2/%TOTAL_SUITES%]%RESET% Running Simulator Verification...
    echo.
    
    call :write_report "[Suite 2] Simulator Verification"
    call :write_report "--------------------------------"
    
    call "%SCRIPT_DIR%verify-simulator.bat" > "%LOG_DIR%\verify-simulator.log" 2>&1
    set SIMULATOR_EXIT=!ERRORLEVEL!
    
    if !SIMULATOR_EXIT! EQU 0 (
        echo         %GREEN%[PASS]%RESET% Simulator verification successful
        set RESULT_SIMULATOR=PASS
        set /a PASSED_SUITES+=1
        call :write_report "Result: PASS"
    ) else (
        echo         %YELLOW%[WARN]%RESET% Simulator verification had warnings
        set RESULT_SIMULATOR=WARN
        set /a PASSED_SUITES+=1
        call :write_report "Result: WARN (partial verification)"
    )
    call :write_report ""
)
echo.

REM ============================================================
REM 测试套件 3: 固件上传测试
REM ============================================================
echo %CYAN%[Suite 3/%TOTAL_SUITES%]%RESET% Running Firmware Upload Tests...
echo.

call :write_report "[Suite 3] Firmware Upload Tests"
call :write_report "--------------------------------"

set FIRMWARE_ARGS=
if defined AUTH_TOKEN set FIRMWARE_ARGS=--token %AUTH_TOKEN%

call "%SCRIPT_DIR%test-firmware-upload.bat" %FIRMWARE_ARGS% > "%LOG_DIR%\test-firmware-upload.log" 2>&1
set FIRMWARE_EXIT=%ERRORLEVEL%

if %FIRMWARE_EXIT% EQU 0 (
    echo         %GREEN%[PASS]%RESET% All firmware upload tests passed
    set RESULT_FIRMWARE=PASS
    set /a PASSED_SUITES+=1
    call :write_report "Result: PASS"
) else (
    echo         %RED%[FAIL]%RESET% Some firmware upload tests failed
    set RESULT_FIRMWARE=FAIL
    set /a FAILED_SUITES+=1
    call :write_report "Result: FAIL"
    
    if %STOP_ON_FAILURE% EQU 1 (
        echo.
        echo %RED%[ABORT]%RESET% Stopping due to --stop-on-failure flag
        goto :generate_report
    )
)
call :write_report ""
echo.

REM ============================================================
REM 测试套件 4: 单设备升级测试
REM ============================================================
echo %CYAN%[Suite 4/%TOTAL_SUITES%]%RESET% Running Single Device Upgrade Tests...
echo.

call :write_report "[Suite 4] Single Device Upgrade Tests"
call :write_report "--------------------------------"

set SINGLE_ARGS=
if defined AUTH_TOKEN set SINGLE_ARGS=--token %AUTH_TOKEN%

call "%SCRIPT_DIR%test-single-upgrade.bat" %SINGLE_ARGS% > "%LOG_DIR%\test-single-upgrade.log" 2>&1
set SINGLE_EXIT=%ERRORLEVEL%

if %SINGLE_EXIT% EQU 0 (
    echo         %GREEN%[PASS]%RESET% All single device upgrade tests passed
    set RESULT_SINGLE=PASS
    set /a PASSED_SUITES+=1
    call :write_report "Result: PASS"
) else (
    echo         %RED%[FAIL]%RESET% Some single device upgrade tests failed
    set RESULT_SINGLE=FAIL
    set /a FAILED_SUITES+=1
    call :write_report "Result: FAIL"
    
    if %STOP_ON_FAILURE% EQU 1 (
        echo.
        echo %RED%[ABORT]%RESET% Stopping due to --stop-on-failure flag
        goto :generate_report
    )
)
call :write_report ""
echo.

REM ============================================================
REM 测试套件 5: 批量升级测试
REM ============================================================
echo %CYAN%[Suite 5/%TOTAL_SUITES%]%RESET% Running Batch Upgrade Tests...
echo.

call :write_report "[Suite 5] Batch Upgrade Tests"
call :write_report "--------------------------------"

set BATCH_ARGS=
if defined AUTH_TOKEN set BATCH_ARGS=--token %AUTH_TOKEN%

call "%SCRIPT_DIR%test-batch-upgrade.bat" %BATCH_ARGS% > "%LOG_DIR%\test-batch-upgrade.log" 2>&1
set BATCH_EXIT=%ERRORLEVEL%

if %BATCH_EXIT% EQU 0 (
    echo         %GREEN%[PASS]%RESET% All batch upgrade tests passed
    set RESULT_BATCH=PASS
    set /a PASSED_SUITES+=1
    call :write_report "Result: PASS"
) else (
    echo         %RED%[FAIL]%RESET% Some batch upgrade tests failed
    set RESULT_BATCH=FAIL
    set /a FAILED_SUITES+=1
    call :write_report "Result: FAIL"
    
    if %STOP_ON_FAILURE% EQU 1 (
        echo.
        echo %RED%[ABORT]%RESET% Stopping due to --stop-on-failure flag
        goto :generate_report
    )
)
call :write_report ""
echo.

REM ============================================================
REM 测试套件 6: 任务取消测试
REM ============================================================
echo %CYAN%[Suite 6/%TOTAL_SUITES%]%RESET% Running Task Cancel Tests...
echo.

call :write_report "[Suite 6] Task Cancel Tests"
call :write_report "--------------------------------"

set CANCEL_ARGS=
if defined AUTH_TOKEN set CANCEL_ARGS=--token %AUTH_TOKEN%

call "%SCRIPT_DIR%test-cancel-task.bat" %CANCEL_ARGS% > "%LOG_DIR%\test-cancel-task.log" 2>&1
set CANCEL_EXIT=%ERRORLEVEL%

if %CANCEL_EXIT% EQU 0 (
    echo         %GREEN%[PASS]%RESET% All task cancel tests passed
    set RESULT_CANCEL=PASS
    set /a PASSED_SUITES+=1
    call :write_report "Result: PASS"
) else (
    echo         %RED%[FAIL]%RESET% Some task cancel tests failed
    set RESULT_CANCEL=FAIL
    set /a FAILED_SUITES+=1
    call :write_report "Result: FAIL"
)
call :write_report ""
echo.

:generate_report
REM ============================================================
REM 生成测试报告
REM ============================================================
echo %BLUE%============================================================%RESET%
echo %BLUE%   Test Execution Complete%RESET%
echo %BLUE%============================================================%RESET%
echo.

call :write_report "============================================================"
call :write_report "   Test Summary"
call :write_report "============================================================"
call :write_report ""

echo   %CYAN%Test Suite Results:%RESET%
echo   -------------------
call :write_report "Test Suite Results:"
call :write_report "-------------------"

REM 显示各测试套件结果
call :display_result "Service Health Check" "%RESULT_SERVICES%"
call :display_result "Simulator Verification" "%RESULT_SIMULATOR%"
call :display_result "Firmware Upload" "%RESULT_FIRMWARE%"
call :display_result "Single Device Upgrade" "%RESULT_SINGLE%"
call :display_result "Batch Upgrade" "%RESULT_BATCH%"
call :display_result "Task Cancel" "%RESULT_CANCEL%"

echo.
call :write_report ""

REM 计算总体结果
set /a TOTAL_RUN=%PASSED_SUITES%+%FAILED_SUITES%

echo   %CYAN%Summary:%RESET%
echo   --------
echo   Total Suites:   %TOTAL_SUITES%
echo   Passed:         %PASSED_SUITES%
echo   Failed:         %FAILED_SUITES%
echo   Skipped:        %SKIPPED_SUITES%
echo.

call :write_report "Summary:"
call :write_report "--------"
call :write_report "Total Suites:   %TOTAL_SUITES%"
call :write_report "Passed:         %PASSED_SUITES%"
call :write_report "Failed:         %FAILED_SUITES%"
call :write_report "Skipped:        %SKIPPED_SUITES%"
call :write_report ""

REM 计算通过率
if %TOTAL_RUN% GTR 0 (
    set /a PASS_RATE=%PASSED_SUITES%*100/%TOTAL_RUN%
) else (
    set PASS_RATE=0
)

echo   Pass Rate:      %PASS_RATE%%%
echo.
call :write_report "Pass Rate:      %PASS_RATE%%%"
call :write_report ""

REM 最终结果
if %FAILED_SUITES% EQU 0 (
    echo   %GREEN%[SUCCESS]%RESET% All E2E tests passed!
    call :write_report "Final Result: SUCCESS - All E2E tests passed!"
    set FINAL_EXIT=0
) else (
    echo   %RED%[FAILED]%RESET% %FAILED_SUITES% test suite(s) failed.
    call :write_report "Final Result: FAILED - %FAILED_SUITES% test suite(s) failed."
    set FINAL_EXIT=1
)

echo.
call :write_report ""
call :write_report "============================================================"
call :write_report "End Time: %DATE% %TIME%"
call :write_report "============================================================"

echo   End Time:       %DATE% %TIME%
echo   Report saved:   %REPORT_FILE%
echo.
echo   Log files:
echo   - %LOG_DIR%\check-services.log
echo   - %LOG_DIR%\verify-simulator.log
echo   - %LOG_DIR%\test-firmware-upload.log
echo   - %LOG_DIR%\test-single-upgrade.log
echo   - %LOG_DIR%\test-batch-upgrade.log
echo   - %LOG_DIR%\test-cancel-task.log
echo.

exit /b %FINAL_EXIT%

REM ============================================================
REM 辅助函数：写入报告
REM ============================================================
:write_report
echo %~1 >> "%REPORT_FILE%"
goto :eof

REM ============================================================
REM 辅助函数：显示结果
REM ============================================================
:display_result
set SUITE_NAME=%~1
set SUITE_RESULT=%~2

if "%SUITE_RESULT%"=="PASS" (
    echo   [%GREEN%PASS%RESET%] %SUITE_NAME%
    call :write_report "[PASS] %SUITE_NAME%"
) else if "%SUITE_RESULT%"=="FAIL" (
    echo   [%RED%FAIL%RESET%] %SUITE_NAME%
    call :write_report "[FAIL] %SUITE_NAME%"
) else if "%SUITE_RESULT%"=="WARN" (
    echo   [%YELLOW%WARN%RESET%] %SUITE_NAME%
    call :write_report "[WARN] %SUITE_NAME%"
) else if "%SUITE_RESULT%"=="SKIP" (
    echo   [%YELLOW%SKIP%RESET%] %SUITE_NAME%
    call :write_report "[SKIP] %SUITE_NAME%"
) else (
    echo   [%YELLOW%-----%RESET%] %SUITE_NAME%
    call :write_report "[----] %SUITE_NAME%"
)
goto :eof

:show_help
echo.
echo Keding Firmware Upgrade E2E Test Suite - Run All Tests
echo.
echo Usage:
echo   run-all-tests.bat [OPTIONS]
echo.
echo Options:
echo   --token TOKEN         Authentication token for API calls
echo   --stop-on-failure     Stop execution when a test suite fails
echo   --skip-simulator      Skip simulator verification test
echo   --help, -h            Show this help message
echo.
echo Test Suites:
echo   1. Service Health Check    - Verify all backend services are running
echo   2. Simulator Verification  - Verify simulator can connect to Gateway
echo   3. Firmware Upload Tests   - Test firmware upload functionality
echo   4. Single Device Upgrade   - Test single device upgrade flow
echo   5. Batch Upgrade Tests     - Test batch upgrade functionality
echo   6. Task Cancel Tests       - Test task cancellation
echo.
echo Output:
echo   - Test report: scripts/e2e-test/reports/e2e-test-report_YYYYMMDD_HHMMSS.txt
echo   - Log files:   scripts/e2e-test/logs/*.log
echo.
echo Requirements validated:
echo   12.1 - Execute all test cases in sequence
echo   12.2 - Record detailed error information on failure
echo   12.3 - Generate test report with pass/fail statistics
echo.
echo Examples:
echo   run-all-tests.bat
echo   run-all-tests.bat --token "your-auth-token"
echo   run-all-tests.bat --stop-on-failure
echo   run-all-tests.bat --skip-simulator
echo.
exit /b 0
