@echo off
chcp 65001 >nul
echo ========================================
echo   IoT 模块表结构导入工具
echo ========================================
echo.

:: 配置数据库信息（请根据实际情况修改）
set DB_HOST=127.0.0.1
set DB_PORT=3306
set DB_NAME=ruoyi-vue-pro
set DB_USER=root
set DB_PASS=

:: 提示用户输入数据库密码
echo 当前配置：
echo   主机：%DB_HOST%
echo   端口：%DB_PORT%
echo   数据库：%DB_NAME%
echo   用户：%DB_USER%
echo.
set /p DB_PASS=请输入MySQL密码：

echo.
echo 正在导入IoT模块表结构...
echo.

:: 执行SQL脚本
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < iot_module.sql

if %errorlevel% equ 0 (
    echo.
    echo ✅ IoT模块表结构导入成功！
    echo.
    echo 接下来请：
    echo   1. 重启后端服务
    echo   2. 刷新浏览器页面
    echo   3. 测试产品管理的定时任务功能
    echo.
) else (
    echo.
    echo ❌ 导入失败！请检查：
    echo   1. MySQL是否已启动
    echo   2. 数据库连接信息是否正确
    echo   3. mysql命令是否在环境变量PATH中
    echo.
)

pause

