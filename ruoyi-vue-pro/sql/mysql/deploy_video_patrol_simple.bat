@echo off
chcp 65001 >nul 2>&1

echo ========================================
echo Video Patrol Module Deployment Script
echo ========================================
echo.

set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_DB=ruoyi-vue-pro
set MYSQL_CHARSET=utf8mb4

set /p MYSQL_PASSWORD=Please enter MySQL password: 

echo.
echo Starting deployment...
echo.

echo [1/2] Creating tables...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% --default-character-set=%MYSQL_CHARSET% %MYSQL_DB% < iot_video_patrol_tables.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to create tables!
    pause
    exit /b 1
)
echo SUCCESS: Tables created!
echo.

echo [2/2] Configuring menus and permissions...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% --default-character-set=%MYSQL_CHARSET% %MYSQL_DB% < video_patrol_menu_permissions.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to configure menus!
    pause
    exit /b 1
)
echo SUCCESS: Menus configured!
echo.

echo ========================================
echo Deployment completed successfully!
echo ========================================
echo.
echo Next steps:
echo 1. Restart backend service
echo 2. Clear browser cache
echo 3. Re-login to system
echo 4. Check Video Patrol menu
echo.
pause
