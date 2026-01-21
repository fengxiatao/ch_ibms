@echo off
chcp 65001 >nul
echo ========================================
echo    IoT 菜单完整性检查与修复工具
echo ========================================
echo.
echo 此工具将帮助您：
echo 1. 检查哪些 IoT 页面缺少菜单配置
echo 2. 批量添加缺失的菜单记录
echo.
echo ========================================

set MYSQL_HOST=192.168.1.126
set MYSQL_USER=root
set MYSQL_PASS=123456
set MYSQL_DB=ch_ibms

echo 请选择操作：
echo.
echo [1] 检查缺失的菜单（推荐先执行）
echo [2] 添加设备发现菜单
echo [3] 批量添加所有缺失的菜单
echo [4] 退出
echo.
set /p choice=请输入选项（1-4）：

if "%choice%"=="1" goto check_menus
if "%choice%"=="2" goto add_discovery
if "%choice%"=="3" goto add_all
if "%choice%"=="4" goto end

:check_menus
echo.
echo ========================================
echo 正在检查菜单完整性...
echo ========================================
echo.
mysql -h %MYSQL_HOST% -u %MYSQL_USER% -p%MYSQL_PASS% %MYSQL_DB% < check_iot_missing_menus.sql
echo.
echo 检查完成！请查看上方结果。
echo ❌ 表示菜单缺失，需要添加
echo ✅ 表示菜单已存在
echo.
pause
goto menu

:add_discovery
echo.
echo ========================================
echo 正在添加设备发现菜单...
echo ========================================
echo.
echo 【重要提示】
echo 执行前请确认以下信息：
echo 1. 已备份 system_menu 表
echo 2. 已确认父菜单ID（parent_id）
echo.
set /p confirm=确认继续？(Y/N): 

if /i "%confirm%" NEQ "Y" goto menu

mysql -h %MYSQL_HOST% -u %MYSQL_USER% -p%MYSQL_PASS% %MYSQL_DB% < iot_device_discovery_menu.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ 设备发现菜单添加成功！
    echo.
    echo 后续步骤：
    echo 1. 重新登录前端系统
    echo 2. 在"设备接入"或"设备管理"菜单下查看
    echo 3. 如果看不到，请检查权限配置
) else (
    echo.
    echo ❌ 添加失败！请检查SQL文件和数据库连接
)
echo.
pause
goto menu

:add_all
echo.
echo ========================================
echo 正在批量添加所有缺失的菜单...
echo ========================================
echo.
echo 【警告】
echo 此操作将添加多个菜单记录！
echo.
echo 将添加的菜单：
echo - 设备发现
echo - 设备配置
echo - 设备控制
echo - 设备事件
echo - 视频预览
echo - 视频回放
echo - GIS地图
echo - 空间平面图
echo - 任务监控
echo - 物模型
echo - 数据规则
echo.
set /p confirm=确认批量添加？(Y/N): 

if /i "%confirm%" NEQ "Y" goto menu

echo.
echo 正在执行...
mysql -h %MYSQL_HOST% -u %MYSQL_USER% -p%MYSQL_PASS% %MYSQL_DB% < iot_missing_pages_menu.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ 批量添加完成！
    echo.
    echo 后续步骤：
    echo 1. 清除浏览器缓存
    echo 2. 重新登录前端系统
    echo 3. 检查菜单是否显示
    echo 4. 如有问题，请查看日志
) else (
    echo.
    echo ❌ 添加失败！
    echo.
    echo 可能的原因：
    echo - 数据库连接失败
    echo - parent_id 不正确
    echo - 菜单已存在（重复键冲突）
    echo.
    echo 请手动检查 SQL 文件并调整
)
echo.
pause
goto menu

:menu
echo.
echo ========================================
echo 是否继续其他操作？
echo ========================================
echo.
echo [1] 返回主菜单
echo [2] 退出
echo.
set /p continue=请选择（1-2）：

if "%continue%"=="1" goto start
if "%continue%"=="2" goto end
goto menu

:start
cls
goto choice

:end
echo.
echo 感谢使用！
echo.
pause














