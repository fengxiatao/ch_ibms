@echo off
REM ============================================================
REM 科鼎固件升级端到端测试 - 清理脚本
REM 
REM 清理测试环境：
REM   1. 执行数据库清理脚本
REM   2. 停止模拟器进程
REM   3. 清理临时文件和日志
REM
REM Requirements: 12.4
REM ============================================================

setlocal enabledelayedexpansion

REM 配置参数
set SCRIPT_DIR=%~dp0
set SQL_DIR=%SCRIPT_DIR%..\..\sql\mysql\keding
set CLEANUP_SQL=%SQL_DIR%\iot_keding_e2e_test_cleanup.sql
set LOG_DIR=%SCRIPT_DIR%logs
set REPORT_DIR=%SCRIPT_DIR%reports

REM MySQL 连接参数
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASSWORD=
set MYSQL_DATABASE=ruoyi-vue-pro

REM 清理选项
set CLEAN_DB=1
set CLEAN_LOGS=0
set CLEAN_REPORTS=0
set STOP_SIMULATOR=1
set FORCE=0

REM 颜色代码
set GREEN=[92m
set RED=[91m
set YELLOW=[93m
set CYAN=[96m
set BLUE=[94m
set RESET=[0m

REM 解析命令行参数
:parse_args
if "%~1"=="" goto :done_parsing
if /i "%~1"=="--mysql-host" (
    set MYSQL_HOST=%~2
    shift
)
if /i "%~1"=="--mysql-port" (
    set MYSQL_PORT=%~2
    shift
)
if /i "%~1"=="--mysql-user" (
    set MYSQL_USER=%~2
    shift
)
if /i "%~1"=="--mysql-password" (
    set MYSQL_PASSWORD=%~2
    shift
)
if /i "%~1"=="--mysql-database" (
    set MYSQL_DATABASE=%~2
    shift
)
if /i "%~1"=="--skip-db" (
    set CLEAN_DB=0
)
if /i "%~1"=="--clean-logs" (
    set CLEAN_LOGS=1
)
if /i "%~1"=="--clean-reports" (
    set CLEAN_REPORTS=1
)
if /i "%~1"=="--clean-all" (
    set CLEAN_LOGS=1
    set CLEAN_REPORTS=1
)
if /i "%~1"=="--skip-simulator" (
    set STOP_SIMULATOR=0
)
if /i "%~1"=="--force" (
    set FORCE=1
)
if /i "%~1"=="--help" goto :show_help
if /i "%~1"=="-h" goto :show_help
shift
goto :parse_args

:done_parsing

echo.
echo %BLUE%============================================================%RESET%
echo %BLUE%   Keding Firmware Upgrade E2E Test - Cleanup%RESET%
echo %BLUE%============================================================%RESET%
echo.
echo   Configuration:
echo   --------------
echo   MySQL Host:      %MYSQL_HOST%:%MYSQL_PORT%
echo   MySQL Database:  %MYSQL_DATABASE%
echo   MySQL User:      %MYSQL_USER%
echo.
echo   Cleanup Options:
echo   ----------------
echo   Clean Database:  %CLEAN_DB%
echo   Clean Logs:      %CLEAN_LOGS%
echo   Clean Reports:   %CLEAN_REPORTS%
echo   Stop Simulator:  %STOP_SIMULATOR%
echo.
echo %BLUE%============================================================%RESET%
echo.

REM 确认清理操作
if %FORCE% EQU 0 (
    echo %YELLOW%[WARNING]%RESET% This will delete E2E test data from the database.
    echo.
    set /p CONFIRM="Are you sure you want to continue? (y/N): "
    if /i not "!CONFIRM!"=="y" (
        echo.
        echo %YELLOW%[CANCELLED]%RESET% Cleanup cancelled by user.
        exit /b 0
    )
    echo.
)

set TOTAL_STEPS=0
set COMPLETED_STEPS=0
set FAILED_STEPS=0

REM 计算总步骤数
if %STOP_SIMULATOR% EQU 1 set /a TOTAL_STEPS+=1
if %CLEAN_DB% EQU 1 set /a TOTAL_STEPS+=1
if %CLEAN_LOGS% EQU 1 set /a TOTAL_STEPS+=1
if %CLEAN_REPORTS% EQU 1 set /a TOTAL_STEPS+=1

set CURRENT_STEP=0

REM ============================================================
REM 步骤 1: 停止模拟器进程
REM ============================================================
if %STOP_SIMULATOR% EQU 1 (
    set /a CURRENT_STEP+=1
    echo %CYAN%[Step !CURRENT_STEP!/%TOTAL_STEPS%]%RESET% Stopping simulator processes...
    
    REM 查找并终止模拟器 Java 进程
    REM 使用 tasklist 和 taskkill 来管理进程
    
    REM 查找包含 simulator 的 Java 进程
    set SIMULATOR_FOUND=0
    for /f "tokens=2" %%a in ('tasklist /fi "imagename eq java.exe" /fo list 2^>nul ^| findstr /i "PID"') do (
        REM 检查进程命令行是否包含 simulator
        wmic process where "ProcessId=%%a" get CommandLine 2>nul | findstr /i "simulator" >nul
        if !ERRORLEVEL! EQU 0 (
            echo         Found simulator process: PID %%a
            taskkill /PID %%a /F >nul 2>&1
            if !ERRORLEVEL! EQU 0 (
                echo         %GREEN%[OK]%RESET% Killed process %%a
                set SIMULATOR_FOUND=1
            ) else (
                echo         %YELLOW%[WARN]%RESET% Could not kill process %%a
            )
        )
    )
    
    if !SIMULATOR_FOUND! EQU 0 (
        echo         %GREEN%[OK]%RESET% No simulator processes found
    )
    
    set /a COMPLETED_STEPS+=1
    echo.
)

REM ============================================================
REM 步骤 2: 执行数据库清理脚本
REM ============================================================
if %CLEAN_DB% EQU 1 (
    set /a CURRENT_STEP+=1
    echo %CYAN%[Step !CURRENT_STEP!/%TOTAL_STEPS%]%RESET% Executing database cleanup script...
    
    REM 检查清理脚本是否存在
    if not exist "%CLEANUP_SQL%" (
        echo         %RED%[FAIL]%RESET% Cleanup SQL script not found: %CLEANUP_SQL%
        set /a FAILED_STEPS+=1
        goto :skip_db_cleanup
    )
    
    echo         SQL Script: %CLEANUP_SQL%
    echo.
    
    REM 构建 MySQL 命令
    set MYSQL_CMD=mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER%
    if defined MYSQL_PASSWORD (
        if not "%MYSQL_PASSWORD%"=="" (
            set MYSQL_CMD=!MYSQL_CMD! -p%MYSQL_PASSWORD%
        )
    )
    set MYSQL_CMD=!MYSQL_CMD! %MYSQL_DATABASE%
    
    REM 执行清理脚本
    echo         Executing cleanup SQL...
    !MYSQL_CMD! < "%CLEANUP_SQL%" > "%LOG_DIR%\cleanup-db.log" 2>&1
    set DB_EXIT=!ERRORLEVEL!
    
    if !DB_EXIT! EQU 0 (
        echo         %GREEN%[OK]%RESET% Database cleanup completed successfully
        set /a COMPLETED_STEPS+=1
        
        REM 显示清理结果摘要
        if exist "%LOG_DIR%\cleanup-db.log" (
            echo.
            echo         Cleanup Summary:
            type "%LOG_DIR%\cleanup-db.log" | findstr /i "已删除"
        )
    ) else (
        echo         %RED%[FAIL]%RESET% Database cleanup failed
        echo         Check log: %LOG_DIR%\cleanup-db.log
        set /a FAILED_STEPS+=1
        
        REM 显示错误信息
        if exist "%LOG_DIR%\cleanup-db.log" (
            echo.
            echo         Error details:
            type "%LOG_DIR%\cleanup-db.log"
        )
    )
    
    :skip_db_cleanup
    echo.
)

REM ============================================================
REM 步骤 3: 清理日志文件
REM ============================================================
if %CLEAN_LOGS% EQU 1 (
    set /a CURRENT_STEP+=1
    echo %CYAN%[Step !CURRENT_STEP!/%TOTAL_STEPS%]%RESET% Cleaning log files...
    
    if exist "%LOG_DIR%" (
        set LOG_COUNT=0
        for %%f in ("%LOG_DIR%\*.log") do set /a LOG_COUNT+=1
        
        if !LOG_COUNT! GTR 0 (
            del /q "%LOG_DIR%\*.log" 2>nul
            echo         %GREEN%[OK]%RESET% Deleted !LOG_COUNT! log file(s)
        ) else (
            echo         %GREEN%[OK]%RESET% No log files to clean
        )
        set /a COMPLETED_STEPS+=1
    ) else (
        echo         %GREEN%[OK]%RESET% Log directory does not exist
        set /a COMPLETED_STEPS+=1
    )
    echo.
)

REM ============================================================
REM 步骤 4: 清理报告文件
REM ============================================================
if %CLEAN_REPORTS% EQU 1 (
    set /a CURRENT_STEP+=1
    echo %CYAN%[Step !CURRENT_STEP!/%TOTAL_STEPS%]%RESET% Cleaning report files...
    
    if exist "%REPORT_DIR%" (
        set REPORT_COUNT=0
        for %%f in ("%REPORT_DIR%\*.txt") do set /a REPORT_COUNT+=1
        
        if !REPORT_COUNT! GTR 0 (
            del /q "%REPORT_DIR%\*.txt" 2>nul
            echo         %GREEN%[OK]%RESET% Deleted !REPORT_COUNT! report file(s)
        ) else (
            echo         %GREEN%[OK]%RESET% No report files to clean
        )
        set /a COMPLETED_STEPS+=1
    ) else (
        echo         %GREEN%[OK]%RESET% Report directory does not exist
        set /a COMPLETED_STEPS+=1
    )
    echo.
)

REM ============================================================
REM 清理临时测试文件
REM ============================================================
echo %CYAN%[Cleanup]%RESET% Removing temporary test files...

REM 删除测试固件文件
if exist "%SCRIPT_DIR%test-firmware.bin" (
    del /q "%SCRIPT_DIR%test-firmware.bin" 2>nul
    echo         %GREEN%[OK]%RESET% Deleted test-firmware.bin
)

REM 删除其他临时文件
if exist "%SCRIPT_DIR%*.tmp" (
    del /q "%SCRIPT_DIR%*.tmp" 2>nul
    echo         %GREEN%[OK]%RESET% Deleted temporary files
)

echo.

REM ============================================================
REM 汇总结果
REM ============================================================
echo %BLUE%============================================================%RESET%
echo %BLUE%   Cleanup Summary%RESET%
echo %BLUE%============================================================%RESET%
echo.
echo   Total Steps:     %TOTAL_STEPS%
echo   Completed:       %COMPLETED_STEPS%
echo   Failed:          %FAILED_STEPS%
echo.

if %FAILED_STEPS% EQU 0 (
    echo   %GREEN%[SUCCESS]%RESET% Cleanup completed successfully!
    echo.
    echo   The following items have been cleaned:
    if %STOP_SIMULATOR% EQU 1 echo   - Simulator processes stopped
    if %CLEAN_DB% EQU 1 echo   - E2E test data removed from database
    if %CLEAN_LOGS% EQU 1 echo   - Log files deleted
    if %CLEAN_REPORTS% EQU 1 echo   - Report files deleted
    echo   - Temporary test files removed
    echo.
    exit /b 0
) else (
    echo   %RED%[PARTIAL]%RESET% Cleanup completed with %FAILED_STEPS% error(s).
    echo.
    echo   Please check the error messages above and resolve manually.
    echo.
    exit /b 1
)

:show_help
echo.
echo Keding Firmware Upgrade E2E Test - Cleanup Script
echo.
echo Usage:
echo   cleanup.bat [OPTIONS]
echo.
echo MySQL Options:
echo   --mysql-host HOST       MySQL server host (default: localhost)
echo   --mysql-port PORT       MySQL server port (default: 3306)
echo   --mysql-user USER       MySQL username (default: root)
echo   --mysql-password PASS   MySQL password (default: empty)
echo   --mysql-database DB     MySQL database name (default: ruoyi-vue-pro)
echo.
echo Cleanup Options:
echo   --skip-db               Skip database cleanup
echo   --skip-simulator        Skip stopping simulator processes
echo   --clean-logs            Also clean log files
echo   --clean-reports         Also clean report files
echo   --clean-all             Clean logs and reports (same as --clean-logs --clean-reports)
echo   --force                 Skip confirmation prompt
echo.
echo General Options:
echo   --help, -h              Show this help message
echo.
echo This script will:
echo   1. Stop any running simulator processes
echo   2. Execute database cleanup SQL script
echo   3. Optionally clean log and report files
echo   4. Remove temporary test files
echo.
echo Database Cleanup:
echo   The cleanup SQL script removes:
echo   - Test devices (station_code: TEST001, TEST002, TEST003)
echo   - Test firmware (version: 1.0.0-test)
echo   - Related upgrade tasks and logs
echo.
echo Requirements validated:
echo   12.4 - Delete test-created data records
echo.
echo Examples:
echo   cleanup.bat
echo   cleanup.bat --force
echo   cleanup.bat --mysql-password mypassword
echo   cleanup.bat --clean-all --force
echo   cleanup.bat --skip-db --clean-logs
echo.
exit /b 0
