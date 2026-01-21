@echo off
chcp 65001 >nul
echo ================================================================================
echo 大华摄像头设备模拟器
echo ================================================================================
echo.

REM 检查Python环境
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 未找到Python环境，请先安装Python 3.7+
    pause
    exit /b 1
)

REM 检查paho-mqtt库
python -c "import paho.mqtt.client" >nul 2>&1
if errorlevel 1 (
    echo [INFO] 正在安装依赖: paho-mqtt...
    pip install paho-mqtt
    if errorlevel 1 (
        echo [ERROR] 安装失败，请手动执行: pip install paho-mqtt
        pause
        exit /b 1
    )
)

echo [OK] Python环境检查通过
echo.
echo ================================================================================
echo 配置信息
echo ================================================================================
echo MQTT Broker: 192.168.1.126:1883
echo 产品: 网络摄像头(枪机)
echo 设备: camera_a19_1906
echo.
echo [提示] 
echo 1. 确保EMQX服务正常运行
echo 2. 确保设备已在前端添加（智慧物联-设备管理）
echo 3. 按 Ctrl+C 停止模拟器
echo ================================================================================
echo.

REM 启动模拟器
python device_simulator.py

pause

















