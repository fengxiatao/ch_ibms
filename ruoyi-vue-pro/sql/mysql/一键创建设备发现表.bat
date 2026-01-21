@echo off
chcp 65001 >nul
echo ============================================
echo   IoT 设备发现和消息幂等性表结构导入
echo ============================================
echo.
echo 本脚本将创建以下表：
echo   1. iot_discovered_device   - 发现设备表
echo   2. iot_message_idempotent  - 消息幂等性检查表
echo.
echo 请确保：
echo   1. MySQL 服务正在运行
echo   2. 数据库 ch_ibms 已存在
echo   3. 已备份重要数据
echo.

set /p confirm="是否继续？(Y/N): "
if /i not "%confirm%"=="Y" (
    echo 已取消操作
    pause
    exit /b
)

echo.
echo [1/3] 正在连接数据库...
set /p host="请输入数据库地址 (默认: localhost): "
if "%host%"=="" set host=localhost

set /p port="请输入数据库端口 (默认: 3306): "
if "%port%"=="" set port=3306

set /p username="请输入数据库用户名 (默认: root): "
if "%username%"=="" set username=root

set /p password="请输入数据库密码: "

set /p database="请输入数据库名称 (默认: ch_ibms): "
if "%database%"=="" set database=ch_ibms

echo.
echo [2/3] 正在执行 SQL 脚本...
mysql -h%host% -P%port% -u%username% -p%password% %database% < iot_tables_device_discovery_and_message.sql

if %errorlevel% neq 0 (
    echo.
    echo ❌ 执行失败！请检查：
    echo    1. MySQL 服务是否运行
    echo    2. 数据库连接信息是否正确
    echo    3. 用户是否有权限
    pause
    exit /b 1
)

echo.
echo [3/3] 正在验证表结构...
mysql -h%host% -P%port% -u%username% -p%password% -e "USE %database%; SHOW TABLES LIKE 'iot_discovered_device'; SHOW TABLES LIKE 'iot_message_idempotent';"

echo.
echo ============================================
echo   ✅ 表结构创建成功！
echo ============================================
echo.
echo 创建的表：
echo   ✓ iot_discovered_device   - 发现设备表
echo   ✓ iot_message_idempotent  - 消息幂等性检查表
echo.
echo 下一步：
echo   1. 重启 IoT 服务
echo   2. 测试设备发现功能
echo   3. 查看日志验证
echo.
pause














