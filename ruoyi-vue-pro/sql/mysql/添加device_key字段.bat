@echo off
REM ================================================
REM IoT 设备表 - 添加 device_key 字段
REM ================================================
REM 说明：一键执行 SQL 脚本，添加 device_key 字段
REM 日期：2025-10-27
REM ================================================

chcp 65001 >nul
setlocal

REM ==================== 配置区 ====================
set MYSQL_HOST=127.0.0.1
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASS=123456
set MYSQL_DB=ruoyi-vue-pro
set SQL_FILE=iot_device_add_device_key.sql

REM ==================== 检查文件 ====================
echo.
echo 【IoT 设备表 - 添加 device_key 字段】
echo ================================================
echo.

if not exist "%SQL_FILE%" (
    echo ❌ 错误：找不到 SQL 文件 %SQL_FILE%
    echo    请确保在 sql/mysql 目录下执行此脚本
    echo.
    pause
    exit /b 1
)

echo ✅ 找到 SQL 文件：%SQL_FILE%
echo.

REM ==================== 显示配置 ====================
echo 📝 数据库连接配置：
echo    主机：%MYSQL_HOST%:%MYSQL_PORT%
echo    用户：%MYSQL_USER%
echo    数据库：%MYSQL_DB%
echo.

REM ==================== 确认执行 ====================
set /p confirm="是否继续执行？(Y/N): "
if /i not "%confirm%"=="Y" (
    echo.
    echo ⏹️  操作已取消
    echo.
    pause
    exit /b 0
)

REM ==================== 执行 SQL ====================
echo.
echo 🚀 开始执行 SQL 脚本...
echo ================================================
echo.

mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% -D%MYSQL_DB% < "%SQL_FILE%"

if %ERRORLEVEL% equ 0 (
    echo.
    echo ================================================
    echo ✅ SQL 脚本执行成功！
    echo.
    echo 📋 变更内容：
    echo    1. 添加 device_key 字段
    echo    2. 为已有设备生成 device_key
    echo    3. 添加唯一索引 uk_device_key
    echo.
    echo 💡 提示：请重启后端服务以使用新字段
    echo ================================================
) else (
    echo.
    echo ================================================
    echo ❌ SQL 脚本执行失败！
    echo.
    echo 🔍 排查建议：
    echo    1. 检查数据库连接配置是否正确
    echo    2. 确认 MySQL 密码是否正确
    echo    3. 查看 SQL 脚本是否有语法错误
    echo    4. 确认数据库 '%MYSQL_DB%' 是否存在
    echo ================================================
)

echo.
pause














