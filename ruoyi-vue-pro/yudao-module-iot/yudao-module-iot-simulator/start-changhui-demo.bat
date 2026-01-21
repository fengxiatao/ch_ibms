@echo off
REM ============================================================
REM Changhui Device Simulator - Demo Startup Script
REM 
REM For demo: device heartbeat, online status, firmware upgrade
REM
REM Usage:
REM   start-changhui-demo.bat [STATION_CODE] [OPTIONS]
REM
REM Parameters:
REM   STATION_CODE - Station code, 20-char hex (default: 01020304050607080910)
REM
REM Options:
REM   --host HOST       - Gateway server address (default: localhost)
REM   --port PORT       - Gateway server port (default: 9700)
REM   --mode MODE       - Simulator mode: SUCCESS, REJECT, FRAME_FAIL (default: SUCCESS)
REM   --heartbeat SEC   - Heartbeat interval in seconds (default: 30)
REM
REM Examples:
REM   start-changhui-demo.bat                                  # Default config
REM   start-changhui-demo.bat 01020304050607080910             # Specify station code
REM   start-changhui-demo.bat --host 192.168.1.100 --port 9700 # Specify server
REM   start-changhui-demo.bat --mode REJECT                    # Reject upgrade mode
REM
REM ============================================================

setlocal enabledelayedexpansion

REM Set default values
set STATION_CODE=01020304050607080910
set HOST=351364e2a398bb82.natapp.cc
set PORT=9700
set MODE=SUCCESS
set HEARTBEAT=30

REM Parse first argument (station code, if not an option)
if not "%~1"=="" (
    set FIRST_ARG=%~1
    if not "!FIRST_ARG:~0,2!"=="--" (
        set STATION_CODE=%~1
        shift
    )
)

REM Parse optional arguments
:parse_args
if "%~1"=="" goto :done_parsing
if /i "%~1"=="--host" (
    set HOST=%~2
    shift
    shift
    goto :parse_args
)
if /i "%~1"=="--port" (
    set PORT=%~2
    shift
    shift
    goto :parse_args
)
if /i "%~1"=="--mode" (
    set MODE=%~2
    shift
    shift
    goto :parse_args
)
if /i "%~1"=="--heartbeat" (
    set HEARTBEAT=%~2
    shift
    shift
    goto :parse_args
)
if /i "%~1"=="--help" goto :show_help
if /i "%~1"=="-h" goto :show_help
shift
goto :parse_args

:done_parsing

REM Display configuration
echo.
echo ============================================================
echo   Changhui Device Simulator - Demo
echo ============================================================
echo.
echo   Station Code:     %STATION_CODE%
echo   Server Address:   %HOST%:%PORT%
echo   Run Mode:         %MODE%
echo   Heartbeat:        %HEARTBEAT% sec
echo.
echo ============================================================
echo.
echo   Features:
echo   - Auto heartbeat to keep device online
echo   - Receive upgrade commands and simulate upgrade process
echo   - Report upgrade progress (0-100%%)
echo   - Support multiple test modes (SUCCESS/REJECT/FAIL)
echo.
echo   Frontend Demo:
echo   - Device online status display
echo   - Firmware upgrade dispatch
echo   - Real-time upgrade progress
echo   - Upgrade result view
echo.
echo ============================================================
echo.

REM Check if JAR file exists
set JAR_FILE=target\yudao-module-iot-simulator.jar
if not exist "%JAR_FILE%" (
    echo [ERROR] JAR file not found: %JAR_FILE%
    echo.
    echo Please compile the simulator first:
    echo   cd ruoyi-vue-pro
    echo   mvn clean package -pl yudao-module-iot/yudao-module-iot-simulator -am -DskipTests
    echo.
    exit /b 1
)

REM Build command line arguments
set CMD_ARGS=--spring.profiles.active=changhui
set CMD_ARGS=%CMD_ARGS% --simulator.changhui.server-host=%HOST%
set CMD_ARGS=%CMD_ARGS% --simulator.changhui.server-port=%PORT%
set CMD_ARGS=%CMD_ARGS% --simulator.changhui.heartbeat-interval=%HEARTBEAT%
set CMD_ARGS=%CMD_ARGS% --demo.default-station-code=%STATION_CODE%
set CMD_ARGS=%CMD_ARGS% --demo.default-mode=%MODE%

REM Start simulator
echo Starting simulator...
echo Command: java -jar %JAR_FILE% %CMD_ARGS%
echo.
echo [TIP] Press Ctrl+C to stop the simulator
echo.

java -jar "%JAR_FILE%" %CMD_ARGS%

exit /b %ERRORLEVEL%

:show_help
echo.
echo Changhui Device Simulator - Demo Startup Script
echo.
echo Usage:
echo   start-changhui-demo.bat [STATION_CODE] [OPTIONS]
echo.
echo Parameters:
echo   STATION_CODE  - Station code, 20-char hex string
echo                   Default: 01020304050607080910
echo.
echo Options:
echo   --host HOST       - Gateway server address (default: localhost)
echo   --port PORT       - Gateway server port (default: 9700)
echo   --mode MODE       - Simulator mode (default: SUCCESS)
echo                       SUCCESS    - Success mode, upgrade completes normally
echo                       REJECT     - Reject mode, reject upgrade command
echo                       FRAME_FAIL - Fail mode, upgrade fails during process
echo   --heartbeat SEC   - Heartbeat interval in seconds (default: 30)
echo   --help, -h        - Show this help message
echo.
echo Examples:
echo   start-changhui-demo.bat
echo   start-changhui-demo.bat 01020304050607080910
echo   start-changhui-demo.bat --host 192.168.1.100
echo   start-changhui-demo.bat --mode REJECT
echo   start-changhui-demo.bat 01020304050607080911 --mode FRAME_FAIL
echo.
echo Demo Scenarios:
echo   1. Device Online Demo:
echo      start-changhui-demo.bat
echo      (Device sends heartbeat, frontend shows device online)
echo.
echo   2. Upgrade Success Demo:
echo      start-changhui-demo.bat --mode SUCCESS
echo      (Frontend dispatches upgrade, device reports progress until complete)
echo.
echo   3. Upgrade Reject Demo:
echo      start-changhui-demo.bat --mode REJECT
echo      (Frontend dispatches upgrade, device rejects and returns failure)
echo.
exit /b 0
