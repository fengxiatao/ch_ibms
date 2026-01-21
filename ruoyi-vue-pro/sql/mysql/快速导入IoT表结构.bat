@echo off
chcp 65001 >nul
echo ================================================
echo    快速导入IoT模块表结构
echo ================================================
echo.

REM 设置数据库连接信息
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=ch_ibms
set DB_USER=root

echo 请输入MySQL密码:
set /p DB_PASSWORD=

echo.
echo [1/3] 检查MySQL连接...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% -e "SELECT 1" 2>nul
if errorlevel 1 (
    echo ❌ MySQL连接失败！请检查：
    echo    - MySQL服务是否启动
    echo    - 用户名密码是否正确
    pause
    exit /b 1
)
echo ✅ MySQL连接成功

echo.
echo [2/3] 导入 iot_module.sql ...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% < iot_module.sql
if errorlevel 1 (
    echo ❌ 导入失败
    pause
    exit /b 1
)
echo ✅ iot_module.sql 导入成功

echo.
echo [3/3] 导入 iot_job_type_definition.sql ...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% < iot_job_type_definition.sql
if errorlevel 1 (
    echo ❌ 导入失败
    pause
    exit /b 1
)
echo ✅ iot_job_type_definition.sql 导入成功

echo.
echo ================================================
echo ✅ 所有表结构导入完成！
echo ================================================
echo.
echo 📋 已导入的表：
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% -e "SHOW TABLES LIKE 'iot_%%'" 2>nul

echo.
echo 🎯 下一步：重启后端服务
echo.
pause

