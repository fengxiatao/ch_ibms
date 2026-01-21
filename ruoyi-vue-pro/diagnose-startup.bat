@echo off
echo ========================================
echo Spring Boot 启动诊断脚本
echo ========================================
echo.

echo 1. 清理并重新编译项目...
call mvn clean compile -DskipTests

echo.
echo 2. 启动应用并输出详细日志...
echo 请查看控制台输出的完整错误堆栈
echo.

java -jar yudao-server/target/yudao-server.jar --debug 2>&1

pause
