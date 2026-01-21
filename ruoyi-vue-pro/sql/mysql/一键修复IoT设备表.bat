@echo off
REM ================================================
REM IoT 设备表 - 综合修复
REM ================================================
REM 说明：一键修复所有数据库字段问题
REM 1. 添加 device_key 字段
REM 2. 修复 Boolean 字段类型
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

REM ==================== 检查文件 ====================
echo.
echo 【IoT 设备表 - 综合修复工具】
echo ================================================
echo.

if not exist "iot_device_add_device_key.sql" (
    echo ❌ 错误：找不到 iot_device_add_device_key.sql
    pause
    exit /b 1
)

if not exist "iot_device_fix_boolean_fields.sql" (
    echo ❌ 错误：找不到 iot_device_fix_boolean_fields.sql
    pause
    exit /b 1
)

echo ✅ 找到所有 SQL 文件
echo.

REM ==================== 显示配置 ====================
echo 📝 数据库连接配置：
echo    主机：%MYSQL_HOST%:%MYSQL_PORT%
echo    用户：%MYSQL_USER%
echo    数据库：%MYSQL_DB%
echo.
echo 📋 将要执行的修复：
echo    1. 添加 device_key 字段
echo    2. 为已有设备生成 device_key
echo    3. 修复 subsystem_override 字段类型 (VARCHAR → BIT)
echo    4. 修复 menu_override 字段类型 (VARCHAR → BIT)
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
echo 🚀 开始执行修复脚本...
echo ================================================
echo.

echo [1/2] 执行 device_key 字段添加...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% -D%MYSQL_DB% < "iot_device_add_device_key.sql"
if %ERRORLEVEL% neq 0 (
    echo ❌ device_key 字段添加失败！
    goto :error
)
echo ✅ device_key 字段添加成功
echo.

echo [2/2] 执行 Boolean 字段类型修复（终极版）...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% -D%MYSQL_DB% < "终极修复IoT设备表_Boolean字段.sql"
if %ERRORLEVEL% neq 0 (
    echo ❌ Boolean 字段修复失败！
    goto :error
)
echo ✅ Boolean 字段修复成功
echo.

REM ==================== 成功 ====================
echo ================================================
echo ✅ 所有修复执行成功！
echo.
echo 📋 完成的修复：
echo    ✔ device_key 字段已添加
echo    ✔ 已有设备已生成 device_key
echo    ✔ subsystem_override 字段类型已修复 (BIT)
echo    ✔ menu_override 字段类型已修复 (BIT)
echo.
echo 💡 下一步操作：
echo    1. 重启后端服务
echo    2. 测试设备激活功能
echo    3. 验证字段类型正确
echo ================================================
echo.
pause
exit /b 0

REM ==================== 错误处理 ====================
:error
echo.
echo ================================================
echo ❌ 执行过程中出现错误！
echo.
echo 🔍 排查建议：
echo    1. 检查数据库连接配置
echo    2. 确认 MySQL 密码正确
echo    3. 查看 SQL 脚本语法
echo    4. 检查数据库权限
echo ================================================
echo.
pause
exit /b 1

