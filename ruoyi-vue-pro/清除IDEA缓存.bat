@echo off
echo ================================
echo 清除 IDEA 缓存和 Maven 编译文件
echo ================================
echo.

echo [1/4] 清理 Maven 编译输出...
call mvn clean -T 4
if %errorlevel% neq 0 (
    echo Maven clean 失败！
    pause
    exit /b 1
)
echo.

echo [2/4] 删除 IDEA 缓存目录...
if exist ".idea" (
    echo 正在删除 .idea 目录...
    rmdir /s /q .idea
    echo .idea 目录已删除
) else (
    echo .idea 目录不存在，跳过
)
echo.

echo [3/4] 删除所有 *.iml 文件...
for /r %%i in (*.iml) do (
    echo 删除: %%i
    del /f /q "%%i"
)
echo 所有 .iml 文件已删除
echo.

echo [4/4] 重新编译和安装所有模块...
call mvn install -DskipTests -T 4 -U
if %errorlevel% neq 0 (
    echo Maven install 失败！
    pause
    exit /b 1
)
echo.

echo ================================
echo 清理完成！
echo ================================
echo.
echo 现在请执行以下步骤：
echo 1. 关闭 IDEA
echo 2. 在 IDEA 中选择 File -^> Invalidate Caches... 
echo 3. 勾选所有选项并点击 "Invalidate and Restart"
echo 4. 等待 IDEA 重新索引完成
echo 5. 重新导入 Maven 项目 (右键项目 -^> Maven -^> Reload project)
echo.
pause





















