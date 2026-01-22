@echo off
setlocal

REM 设置 JRE（若不存在则使用系统默认 Java）
IF EXIST "C:\Program Files\Java\jdk1.8.0_65\bin\java.exe" (
  set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_65
) ELSE (
  echo 使用系统默认 Java
)
IF DEFINED JAVA_HOME (
  set PATH=%JAVA_HOME%\bin;%PATH%
)

REM 设置本地原生库路径（优先使用项目随附库，确保 play.dll 与 NetSDK 版本匹配）
set NATIVE_DIR=%CD%\libs\win64
set NATIVE_DIR2=%CD%\target
set NATIVE_DIR3=%CD%
set PATH=%NATIVE_DIR%;%NATIVE_DIR2%;%NATIVE_DIR3%;%PATH%

set JNA_LIB_PATH=%NATIVE_DIR%;%NATIVE_DIR2%;%NATIVE_DIR3%
set JAVA_LIB_PATH=%JNA_LIB_PATH%

echo 运行 netsdk-1.0-demo.jar
java -Djna.library.path=%JNA_LIB_PATH% -Djava.library.path=%JAVA_LIB_PATH% -Xms256m -Xmx512m -jar "%CD%\target\netsdk-1.0-demo.jar"

endlocal