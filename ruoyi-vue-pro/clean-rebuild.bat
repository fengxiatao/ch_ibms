@echo off
echo 清理所有编译缓存...
call mvn clean -q
echo 重新编译项目...
call mvn compile -DskipTests -T 1C
echo 完成！
pause
