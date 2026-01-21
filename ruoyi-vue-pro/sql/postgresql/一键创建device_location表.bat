@echo off
chcp 65001 >nul
echo ========================================
echo   一键创建 device_location 表
echo ========================================
echo.

echo 正在连接 PostgreSQL 数据库...
echo.

REM 请根据实际情况修改以下参数
set PGHOST=localhost
set PGPORT=5432
set PGDATABASE=ruoyi-vue-pro
set PGUSER=postgres
set PGPASSWORD=123456

echo 数据库连接信息：
echo   主机: %PGHOST%:%PGPORT%
echo   数据库: %PGDATABASE%
echo   用户: %PGUSER%
echo.

pause

echo.
echo 正在执行 SQL 脚本...
echo.

psql -h %PGHOST% -p %PGPORT% -U %PGUSER% -d %PGDATABASE% -f device_location.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   ✅ device_location 表创建成功！
    echo ========================================
) else (
    echo.
    echo ========================================
    echo   ❌ 创建失败！错误代码: %ERRORLEVEL%
    echo ========================================
    echo.
    echo 可能的原因：
    echo   1. PostgreSQL 未安装或未启动
    echo   2. 数据库连接参数不正确
    echo   3. 用户权限不足
    echo   4. 表已存在（请手动删除后重试）
    echo.
)

echo.
pause





