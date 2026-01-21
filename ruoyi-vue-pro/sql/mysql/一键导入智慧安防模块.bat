@echo off
chcp 65001 >nul
echo ========================================
echo 智慧安防模块一键导入工具
echo ========================================
echo.

REM 设置MySQL连接参数（请根据实际情况修改）
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_DATABASE=ch_ibms

echo 请输入MySQL密码:
set /p MYSQL_PASSWORD=

echo.
echo ========================================
echo 开始导入智慧安防模块...
echo ========================================
echo.

REM 1. 导入数据表
echo [1/3] 正在导入安防数据表...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% %MYSQL_DATABASE% < security_module.sql
if %errorlevel% neq 0 (
    echo ❌ 数据表导入失败！
    pause
    exit /b 1
)
echo ✅ 数据表导入成功

echo.
REM 2. 导入菜单配置
echo [2/3] 正在导入菜单配置...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% %MYSQL_DATABASE% < security_menu.sql
if %errorlevel% neq 0 (
    echo ❌ 菜单配置导入失败！
    pause
    exit /b 1
)
echo ✅ 菜单配置导入成功

echo.
REM 3. 导入角色权限
echo [3/3] 正在导入角色权限...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% %MYSQL_DATABASE% < security_role_menu.sql
if %errorlevel% neq 0 (
    echo ❌ 角色权限导入失败！
    pause
    exit /b 1
)
echo ✅ 角色权限导入成功

echo.
echo ========================================
echo ✅ 智慧安防模块导入完成！
echo ========================================
echo.
echo 📌 下一步操作：
echo 1. 重启Spring Boot应用
echo 2. 清理Redis缓存（或重启Redis）
echo 3. 管理员重新登录系统
echo 4. 访问"智慧安防"菜单
echo.
echo 📚 详细说明请查看：
echo    docs/sessions/session_20251024_213950_智慧安防系统完整实施/菜单配置说明.md
echo.

pause


















