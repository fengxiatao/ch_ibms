@echo off
echo ========================================
echo 强制清理所有编译缓存
echo ========================================
echo.

echo [1/5] 停止所有 Java 进程（如果有）...
taskkill /F /IM java.exe 2>nul
timeout /t 2 >nul

echo.
echo [2/5] 删除所有 target 目录...
for /d /r . %%d in (target) do @if exist "%%d" (
    echo 删除: %%d
    rd /s /q "%%d" 2>nul
)

echo.
echo [3/5] 删除 IDE 缓存...
if exist ".idea" rd /s /q ".idea" 2>nul
if exist "*.iml" del /s /q "*.iml" 2>nul

echo.
echo [4/5] 清理 Maven 本地仓库中的项目缓存...
if exist "%USERPROFILE%\.m2\repository\cn\iocoder\boot" (
    echo 删除: %USERPROFILE%\.m2\repository\cn\iocoder\boot
    rd /s /q "%USERPROFILE%\.m2\repository\cn\iocoder\boot" 2>nul
)

echo.
echo [5/5] 清理完成！
echo ========================================
echo 建议：
echo 1. 在 IDEA 中：File → Invalidate Caches → Invalidate and Restart
echo 2. 或者运行：mvn clean install -DskipTests
echo ========================================
pause
