@echo off
echo ========================================
echo Video Patrol Complete Deployment
echo ========================================
echo.

set /p MYSQL_PASSWORD=Enter MySQL password: 

echo.
echo [1/4] Creating tables...
mysql -uroot -p%MYSQL_PASSWORD% ruoyi-vue-pro < iot_video_patrol_tables.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to create tables!
    pause
    exit /b 1
)
echo SUCCESS!

echo.
echo [2/4] Cleaning duplicate menus...
mysql -uroot -p%MYSQL_PASSWORD% ruoyi-vue-pro < 清理重复视频巡更菜单.sql
echo Cleaned!

echo.
echo [3/4] Creating new menus...
mysql -uroot -p%MYSQL_PASSWORD% ruoyi-vue-pro < video_patrol_menu_final.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to create menus!
    pause
    exit /b 1
)
echo SUCCESS!

echo.
echo [4/4] Verifying...
mysql -uroot -p%MYSQL_PASSWORD% ruoyi-vue-pro -e "SELECT name, path FROM system_menu WHERE parent_id = (SELECT id FROM system_menu WHERE name = '视频巡更' AND deleted = 0 LIMIT 1) AND deleted = 0 ORDER BY sort;"

echo.
echo ========================================
echo Deployment completed successfully!
echo ========================================
echo.
echo Next steps:
echo 1. Restart backend service
echo 2. Clear browser cache (Ctrl+Shift+Delete)
echo 3. Re-login to system
echo 4. Check left menu for Video Patrol
echo.
pause
