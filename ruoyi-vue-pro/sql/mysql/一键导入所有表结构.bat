@echo off
chcp 65001 >nul
cls
echo ╔════════════════════════════════════════════════════════╗
echo ║                                                        ║
echo ║     IoT 物联网 + GIS 空间设施 表结构一键导入工具       ║
echo ║                                                        ║
echo ╚════════════════════════════════════════════════════════╝
echo.

:: 配置数据库信息（请根据实际情况修改）
set DB_HOST=127.0.0.1
set DB_PORT=3306
set DB_NAME=ruoyi-vue-pro
set DB_USER=root
set DB_PASS=

:: 提示用户输入数据库密码
echo 📌 当前配置：
echo    ├─ 主机：%DB_HOST%:%DB_PORT%
echo    ├─ 数据库：%DB_NAME%
echo    └─ 用户：%DB_USER%
echo.
set /p DB_PASS=🔑 请输入MySQL密码（直接回车如果没有密码）：

echo.
echo ════════════════════════════════════════════════════════
echo.

:: 检查文件是否存在
if not exist "iot_module.sql" (
    echo ❌ 错误：找不到 iot_module.sql 文件
    echo.
    pause
    exit /b 1
)

if not exist "gis_spatial_module.sql" (
    echo ❌ 错误：找不到 gis_spatial_module.sql 文件
    echo.
    pause
    exit /b 1
)

echo [1/3] 正在导入 IoT 物联网模块表结构...
echo.
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < iot_module.sql 2>&1

if %errorlevel% equ 0 (
    echo    ✅ IoT 模块导入成功！
    echo.
) else (
    echo    ❌ IoT 模块导入失败！
    echo.
    goto error_handler
)

echo [2/3] 正在导入 IoT 任务类型定义...
echo.
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < iot_job_type_definition.sql 2>&1

if %errorlevel% equ 0 (
    echo    ✅ 任务类型定义导入成功！
    echo.
) else (
    echo    ❌ 任务类型定义导入失败！
    echo.
    goto error_handler
)

echo [3/4] 正在导入 GIS 空间设施模块表结构...
echo.
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < gis_spatial_module.sql 2>&1

if %errorlevel% equ 0 (
    echo    ✅ GIS 模块导入成功！
    echo.
) else (
    echo    ❌ GIS 模块导入失败！
    echo.
    goto error_handler
)

echo [4/4] 正在导入 定时任务配置表结构...
echo.
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < iot_scheduled_task.sql 2>&1

if %errorlevel% equ 0 (
    echo    ✅ 定时任务配置表导入成功！
    echo.
) else (
    echo    ❌ 定时任务配置表导入失败！
    echo.
    goto error_handler
)

echo.
echo ════════════════════════════════════════════════════════
echo.
echo 🎉 恭喜！所有表结构已成功导入！
echo.
echo 📋 已创建的表：
echo    IoT模块：
echo    ├─ iot_product（产品表）
echo    ├─ iot_product_category（产品分类表）
echo    ├─ iot_device（设备表）
echo    └─ iot_job_type_definition（任务类型定义表）⭐
echo.
echo    GIS模块：
echo    ├─ ibms_gis_campus（园区表）
echo    ├─ ibms_gis_building（建筑表）
echo    ├─ ibms_gis_floor（楼层表）
echo    └─ ibms_gis_area（区域表）
echo.
echo 🔄 接下来请：
echo    1. 重启后端服务（YudaoServerApplication）
echo    2. 刷新浏览器页面（Ctrl + F5）
echo    3. 测试产品管理的定时任务功能
echo    4. 测试空间设施的定时任务功能
echo.
goto end

:error_handler
echo.
echo ════════════════════════════════════════════════════════
echo.
echo ❌ 导入过程中出现错误！
echo.
echo 🔍 请检查：
echo    1. MySQL服务是否已启动
echo    2. 数据库连接信息是否正确
echo    3. 数据库名称是否存在
echo    4. 用户是否有足够的权限
echo    5. mysql命令是否在环境变量PATH中
echo.
echo 💡 如果表已存在，可以：
echo    1. 先手动删除旧表
echo    2. 或者编辑SQL文件，保留需要的部分
echo.

:end
pause

