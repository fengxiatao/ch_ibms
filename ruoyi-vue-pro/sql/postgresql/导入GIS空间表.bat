@echo off
chcp 65001 >nul
cls
echo ╔════════════════════════════════════════════════════════╗
echo ║                                                        ║
echo ║        PostgreSQL GIS空间设施表结构导入工具            ║
echo ║                                                        ║
echo ╚════════════════════════════════════════════════════════╝
echo.

:: 配置PostgreSQL数据库信息（请根据实际情况修改）
set PG_HOST=127.0.0.1
set PG_PORT=5432
set PG_DB=ibms_gis
set PG_USER=postgres
set PG_PASS=

:: 提示用户输入数据库密码
echo 📌 当前配置：
echo    ├─ 主机：%PG_HOST%:%PG_PORT%
echo    ├─ 数据库：%PG_DB%
echo    └─ 用户：%PG_USER%
echo.
set /p PG_PASS=🔑 请输入PostgreSQL密码：

echo.
echo ════════════════════════════════════════════════════════
echo.

:: 设置密码环境变量（psql使用）
set PGPASSWORD=%PG_PASS%

:: 检查文件是否存在
if not exist "..\mysql\gis_spatial_module.sql" (
    echo ❌ 错误：找不到 gis_spatial_module.sql 文件
    echo    预期位置：..\mysql\gis_spatial_module.sql
    echo.
    pause
    exit /b 1
)

echo [1/1] 正在导入 GIS 空间设施模块表结构...
echo.

:: 执行SQL脚本
psql -h %PG_HOST% -p %PG_PORT% -U %PG_USER% -d %PG_DB% -f "..\mysql\gis_spatial_module.sql" 2>&1

if %errorlevel% equ 0 (
    echo.
    echo ════════════════════════════════════════════════════════
    echo.
    echo 🎉 恭喜！GIS空间表结构已成功导入PostgreSQL！
    echo.
    echo 📋 已创建的表：
    echo    ├─ ibms_gis_campus（园区表）
    echo    ├─ ibms_gis_building（建筑表）
    echo    ├─ ibms_gis_floor（楼层表）
    echo    └─ ibms_gis_area（区域表）
    echo.
    echo 🔄 接下来请：
    echo    1. 验证表结构是否正确
    echo    2. 检查测试数据是否插入
    echo    3. 重启后端服务
    echo    4. 测试空间设施功能
    echo.
) else (
    echo.
    echo ════════════════════════════════════════════════════════
    echo.
    echo ❌ 导入失败！请检查：
    echo    1. PostgreSQL服务是否已启动
    echo    2. 数据库连接信息是否正确
    echo    3. 数据库 ibms_gis 是否存在
    echo    4. 用户是否有足够的权限
    echo    5. psql命令是否在环境变量PATH中
    echo.
    echo 💡 创建数据库的命令：
    echo    psql -U postgres
    echo    CREATE DATABASE ibms_gis;
    echo    \q
    echo.
)

:: 清除密码环境变量
set PGPASSWORD=

pause

