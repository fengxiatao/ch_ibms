@echo off
chcp 65001 >nul
echo ================================================================================
echo RocketMQ Topic 检查脚本
echo ================================================================================
echo.

echo [提示] 此脚本需要您手动在RocketMQ服务器上执行命令
echo.

echo 方法1: RocketMQ Dashboard (推荐)
echo ================================
echo 1. 访问: http://192.168.1.126:8080
echo 2. 导航: Topic 管理
echo 3. 查看所有Topic列表
echo.

echo 方法2: SSH命令行
echo ================
echo 1. SSH连接到RocketMQ服务器:
echo    ssh user@192.168.1.126
echo.
echo 2. 进入RocketMQ目录:
echo    cd /opt/rocketmq
echo.
echo 3. 查看所有Topic:
echo    bin/mqadmin topicList -n localhost:9876
echo.
echo 4. 查看特定Topic状态:
echo    bin/mqadmin topicStatus -n localhost:9876 -t iot_device_message
echo.

echo 预期的Topic列表:
echo ================
echo   - iot_device_message       (设备消息主Topic)
echo   - iot_device_status        (设备上下线)
echo   - iot_device_property      (属性上报)
echo   - iot_device_event         (事件上报)
echo   - iot_device_service       (服务调用)
echo   - iot_device_service_reply (服务响应)
echo.

echo [提示] Topic会在第一次发送消息时自动创建
echo        如果现在看不到，是正常的（因为还没有设备上报数据）
echo.

pause

















