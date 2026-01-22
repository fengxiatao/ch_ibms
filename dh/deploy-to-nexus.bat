@echo off
echo 部署大华SDK到Nexus私服...
echo.

echo 当前目录: %cd%
echo Maven坐标: com.dahua:netsdk-demo:1.0
echo Nexus服务器: http://192.168.1.126:8082/
echo.

echo 开始部署...
mvn clean deploy -s "../nexus-settings.xml"

if %ERRORLEVEL% == 0 (
    echo.
    echo ✅ 部署成功！
    echo.
    echo 现在可以在其他项目中使用以下依赖:
    echo ^<dependency^>
    echo     ^<groupId^>com.dahua^</groupId^>
    echo     ^<artifactId^>netsdk-demo^</artifactId^>
    echo     ^<version^>1.0^</version^>
    echo ^</dependency^>
) else (
    echo.
    echo ❌ 部署失败！请检查网络连接和Nexus服务器状态。
)

echo.
pause
