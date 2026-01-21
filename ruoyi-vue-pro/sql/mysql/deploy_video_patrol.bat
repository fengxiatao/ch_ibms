@echo off
:: ========================================
:: 视频巡更模块一键部署脚本
:: 注意：此文件必须以UTF-8 with BOM编码保存
:: ========================================

:: 设置控制台代码页为UTF-8
chcp 65001 >nul 2>&1

echo ========================================
echo 视频巡更模块一键部署脚本
echo ========================================
echo.

:: 设置MySQL连接参数
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_DB=ruoyi-vue-pro
set MYSQL_CHARSET=utf8mb4

:: 提示输入MySQL密码
set /p MYSQL_PASSWORD=请输入MySQL密码: 

echo.
echo 开始部署视频巡更模块...
echo.

:: 执行数据表创建SQL
echo [1/2] 创建数据表...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% --default-character-set=%MYSQL_CHARSET% %MYSQL_DB% < iot_video_patrol_tables.sql
if %errorlevel% neq 0 (
    echo ❌ 数据表创建失败！
    pause
    exit /b 1
)
echo ✅ 数据表创建成功！
echo.

:: 执行菜单权限配置SQL
echo [2/2] 配置菜单和权限...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% --default-character-set=%MYSQL_CHARSET% %MYSQL_DB% < video_patrol_menu_permissions.sql
if %errorlevel% neq 0 (
    echo ❌ 菜单权限配置失败！
    pause
    exit /b 1
)
echo ✅ 菜单权限配置成功！
echo.

echo ========================================
echo 🎉 视频巡更模块部署完成！
echo ========================================
echo.
echo 请执行以下步骤：
echo 1. 重启后端服务
echo 2. 清除浏览器缓存
echo 3. 重新登录系统
echo 4. 在左侧菜单查看"视频巡更"模块
echo.
pause
