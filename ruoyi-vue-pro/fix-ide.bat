@echo off
chcp 65001 >nul
echo ========================================
echo Fix IntelliJ IDEA Index Issues
echo ========================================
echo.

echo [1/3] Cleaning Maven cache...
mvn clean -DskipTests
echo.

echo [2/3] Recompiling project...
mvn compile -DskipTests
echo.

echo [3/3] Please refresh IDEA project...
echo In IDEA: File - Invalidate Caches / Restart
echo.

echo ========================================
echo Fix completed!
echo ========================================
pause
