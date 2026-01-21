@echo off
echo ========================================
echo Video Patrol Deployment
echo ========================================
echo.

set /p MYSQL_PASSWORD=Enter MySQL password: 

echo.
echo [1/3] Creating tables...
mysql -uroot -p%MYSQL_PASSWORD% ruoyi-vue-pro < iot_video_patrol_tables.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed!
    pause
    exit /b 1
)
echo SUCCESS!

echo.
echo [2/3] Cleaning old menus...
mysql -uroot -p%MYSQL_PASSWORD% ruoyi-vue-pro < 清理并重新部署.sql
echo Cleaned!

echo.
echo [3/3] Creating menus...
mysql -uroot -p%MYSQL_PASSWORD% ruoyi-vue-pro < video_patrol_menu_final.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed!
    pause
    exit /b 1
)
echo SUCCESS!

echo.
echo ========================================
echo Deployment completed successfully!
echo ========================================
echo.
echo Next steps:
echo 1. Restart backend service
echo 2. Clear browser cache
echo 3. Re-login to system
echo.
pause
