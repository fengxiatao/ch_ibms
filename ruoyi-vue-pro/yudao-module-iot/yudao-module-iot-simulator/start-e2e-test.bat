@echo off
REM ============================================================
REM 科鼎设备模拟器 - E2E测试启动脚本
REM 
REM 用法:
REM   start-e2e-test.bat [MODE] [STATION_CODE] [OPTIONS]
REM
REM 参数:
REM   MODE         - 模拟器模式: SUCCESS, REJECT, FRAME_FAIL (默认: SUCCESS)
REM   STATION_CODE - 测站编码 (默认: TEST001)
REM
REM 选项:
REM   --host HOST       - Gateway服务器地址 (默认: localhost)
REM   --port PORT       - Gateway服务器端口 (默认: 9600)
REM   --fail-frames N   - 失败帧号列表，逗号分隔 (FRAME_FAIL模式使用)
REM   --delay MS        - 响应延迟毫秒数 (TIMEOUT模式使用)
REM
REM 示例:
REM   start-e2e-test.bat                           # SUCCESS模式，TEST001设备
REM   start-e2e-test.bat SUCCESS TEST001           # SUCCESS模式，TEST001设备
REM   start-e2e-test.bat REJECT TEST002            # REJECT模式，TEST002设备
REM   start-e2e-test.bat FRAME_FAIL TEST003 --fail-frames 5,10
REM
REM Requirements: 2.1, 2.2, 2.3, 2.4, 2.5
REM ============================================================

setlocal enabledelayedexpansion

REM 设置默认值
set MODE=SUCCESS
set STATION_CODE=TEST001
set HOST=localhost
set PORT=9600
set FAIL_FRAMES=
set DELAY=

REM 解析第一个参数（模式）
if not "%~1"=="" (
    set FIRST_ARG=%~1
    if /i "!FIRST_ARG!"=="SUCCESS" set MODE=SUCCESS
    if /i "!FIRST_ARG!"=="REJECT" set MODE=REJECT
    if /i "!FIRST_ARG!"=="FRAME_FAIL" set MODE=FRAME_FAIL
    if /i "!FIRST_ARG!"=="TIMEOUT" set MODE=TIMEOUT
    if /i "!FIRST_ARG!"=="--help" goto :show_help
    if /i "!FIRST_ARG!"=="-h" goto :show_help
)

REM 解析第二个参数（测站编码）
if not "%~2"=="" (
    set SECOND_ARG=%~2
    if not "!SECOND_ARG:~0,2!"=="--" (
        set STATION_CODE=%~2
    )
)

REM 解析可选参数
:parse_args
if "%~1"=="" goto :done_parsing
if /i "%~1"=="--host" (
    set HOST=%~2
    shift
)
if /i "%~1"=="--port" (
    set PORT=%~2
    shift
)
if /i "%~1"=="--fail-frames" (
    set FAIL_FRAMES=%~2
    shift
)
if /i "%~1"=="--delay" (
    set DELAY=%~2
    shift
)
shift
goto :parse_args

:done_parsing

REM 显示配置信息
echo ============================================================
echo   Keding Device Simulator - E2E Test
echo ============================================================
echo.
echo   Mode:         %MODE%
echo   Station Code: %STATION_CODE%
echo   Server:       %HOST%:%PORT%
if not "%FAIL_FRAMES%"=="" echo   Fail Frames:  %FAIL_FRAMES%
if not "%DELAY%"=="" echo   Delay:        %DELAY% ms
echo.
echo ============================================================
echo.

REM 检查JAR文件是否存在
set JAR_FILE=target\yudao-module-iot-simulator.jar
if not exist "%JAR_FILE%" (
    echo [ERROR] JAR file not found: %JAR_FILE%
    echo.
    echo Please build the simulator first:
    echo   cd ruoyi-vue-pro
    echo   mvn clean package -pl yudao-module-iot/yudao-module-iot-simulator -am -DskipTests
    echo.
    exit /b 1
)

REM 构建命令行参数
set CMD_ARGS=--spring.profiles.active=e2e-test
set CMD_ARGS=%CMD_ARGS% --protocol keding
set CMD_ARGS=%CMD_ARGS% --station-code %STATION_CODE%
set CMD_ARGS=%CMD_ARGS% --host %HOST%
set CMD_ARGS=%CMD_ARGS% --port %PORT%
set CMD_ARGS=%CMD_ARGS% --mode %MODE%

if not "%FAIL_FRAMES%"=="" (
    set CMD_ARGS=%CMD_ARGS% --fail-frames %FAIL_FRAMES%
)

if not "%DELAY%"=="" (
    set CMD_ARGS=%CMD_ARGS% --delay %DELAY%
)

REM 启动模拟器
echo Starting simulator...
echo Command: java -jar %JAR_FILE% %CMD_ARGS%
echo.

java -jar "%JAR_FILE%" %CMD_ARGS%

exit /b %ERRORLEVEL%

:show_help
echo.
echo Keding Device Simulator - E2E Test Startup Script
echo.
echo Usage:
echo   start-e2e-test.bat [MODE] [STATION_CODE] [OPTIONS]
echo.
echo Modes:
echo   SUCCESS     - Accept all upgrade commands and return success (default)
echo   REJECT      - Reject upgrade trigger command
echo   FRAME_FAIL  - Fail on specified frame numbers
echo   TIMEOUT     - Delay response to simulate slow device
echo.
echo Options:
echo   --host HOST       - Gateway server address (default: localhost)
echo   --port PORT       - Gateway server port (default: 9600)
echo   --fail-frames N   - Comma-separated frame numbers to fail (for FRAME_FAIL mode)
echo   --delay MS        - Response delay in milliseconds (for TIMEOUT mode)
echo   --help, -h        - Show this help message
echo.
echo Test Station Codes (from iot_keding_e2e_test_data.sql):
echo   TEST001 - Primary test device
echo   TEST002 - Secondary test device
echo   TEST003 - Tertiary test device
echo.
echo Examples:
echo   start-e2e-test.bat                                    # SUCCESS mode, TEST001
echo   start-e2e-test.bat SUCCESS TEST001                    # SUCCESS mode, TEST001
echo   start-e2e-test.bat REJECT TEST002                     # REJECT mode, TEST002
echo   start-e2e-test.bat FRAME_FAIL TEST003 --fail-frames 5,10
echo   start-e2e-test.bat TIMEOUT TEST001 --delay 5000
echo.
echo Requirements validated:
echo   2.1 - Simulator connects to Gateway TCP port
echo   2.2 - Simulator sends heartbeat and maintains online status
echo   2.3 - SUCCESS mode accepts all upgrade commands
echo   2.4 - REJECT mode rejects upgrade trigger command
echo   2.5 - FRAME_FAIL mode fails on specified frame numbers
echo.
exit /b 0
