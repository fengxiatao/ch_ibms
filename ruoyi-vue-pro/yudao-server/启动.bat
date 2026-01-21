@echo off
echo ========================================
echo 启动 YuDao 后端服务
echo ========================================

cd /d %~dp0

echo [1/2] 清理编译...
call mvn clean compile -DskipTests

if %errorlevel% neq 0 (
    echo [错误] 编译失败，请检查代码错误！
    pause
    exit /b 1
)

echo [2/2] 启动服务...
call mvn spring-boot:run "-Dspring-boot.run.profiles=local"

pause

















