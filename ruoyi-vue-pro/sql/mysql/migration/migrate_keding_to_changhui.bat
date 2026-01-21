@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   科鼎到长辉数据迁移工具
echo ========================================
echo.

:: 设置默认值
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=ruoyi-vue-pro
set DB_USER=root

:: 提示用户输入
set /p DB_HOST="请输入数据库主机 [%DB_HOST%]: " || set DB_HOST=%DB_HOST%
set /p DB_PORT="请输入数据库端口 [%DB_PORT%]: " || set DB_PORT=%DB_PORT%
set /p DB_NAME="请输入数据库名称 [%DB_NAME%]: " || set DB_NAME=%DB_NAME%
set /p DB_USER="请输入数据库用户 [%DB_USER%]: " || set DB_USER=%DB_USER%
set /p DB_PASS="请输入数据库密码: "

echo.
echo 数据库配置:
echo   主机: %DB_HOST%
echo   端口: %DB_PORT%
echo   数据库: %DB_NAME%
echo   用户: %DB_USER%
echo.

:menu
echo ========================================
echo   请选择操作:
echo ========================================
echo   1. 创建长辉表结构
echo   2. 执行数据迁移
echo   3. 验证迁移结果
echo   4. 回滚迁移
echo   5. 退出
echo ========================================
set /p choice="请输入选项 [1-5]: "

if "%choice%"=="1" goto create_tables
if "%choice%"=="2" goto migrate
if "%choice%"=="3" goto validate
if "%choice%"=="4" goto rollback
if "%choice%"=="5" goto end

echo 无效选项，请重新选择
goto menu

:create_tables
echo.
echo 正在创建长辉表结构...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < V6.0__create_changhui_tables.sql
if %errorlevel% neq 0 (
    echo 创建表结构失败！
) else (
    echo 创建表结构成功！
)
echo.
goto menu

:migrate
echo.
echo 正在执行数据迁移...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < V6.2__complete_keding_to_changhui_migration.sql
if %errorlevel% neq 0 (
    echo 数据迁移失败！
) else (
    echo 数据迁移成功！
)
echo.
goto menu

:validate
echo.
echo 正在验证迁移结果...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < V6.2__validate_migration.sql
if %errorlevel% neq 0 (
    echo 验证失败！
) else (
    echo 验证完成！
)
echo.
goto menu

:rollback
echo.
echo 警告：回滚操作将删除已迁移的数据！
set /p confirm="确认回滚？(y/n): "
if /i "%confirm%"=="y" (
    echo 正在执行回滚...
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < V6.2__rollback_changhui_to_keding.sql
    if %errorlevel% neq 0 (
        echo 回滚失败！
    ) else (
        echo 回滚成功！
    )
) else (
    echo 已取消回滚操作
)
echo.
goto menu

:end
echo.
echo 感谢使用，再见！
pause
