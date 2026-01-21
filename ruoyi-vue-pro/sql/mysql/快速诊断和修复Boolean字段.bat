@echo off
REM ================================================
REM IoT 设备表 - Boolean 字段快速诊断和修复
REM ================================================
REM 说明：先诊断，再根据情况修复
REM 日期：2025-10-27
REM ================================================

chcp 65001 >nul
setlocal

REM ==================== 配置区 ====================
set MYSQL_HOST=127.0.0.1
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASS=123456
set MYSQL_DB=ruoyi-vue-pro

REM ==================== 标题 ====================
echo.
echo 【Boolean 字段诊断和修复工具】
echo ================================================
echo.
echo 🔍 当前问题：
echo    Incorrect integer value: 'false' for column 'subsystem_override'
echo.
echo 💡 可能原因：
echo    1. 字段类型是 TINYINT，但尝试写入字符串 'false'
echo    2. 字段类型是 VARCHAR，但应该是 BIT(1)
echo    3. 数据中包含异常值
echo.

REM ==================== 步骤1: 诊断 ====================
echo ================================================
echo 📊 第1步：诊断当前字段类型
echo ================================================
echo.

mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% -D%MYSQL_DB% < "诊断IoT设备表字段类型.sql"

if %ERRORLEVEL% neq 0 (
    echo.
    echo ❌ 诊断失败！请检查数据库连接
    pause
    exit /b 1
)

echo.
echo ✅ 诊断完成！请查看上方输出
echo.
echo 📋 诊断说明：
echo    ✅ 正确 (BIT)               - 字段类型正确，无需修复
echo    ⚠️  TINYINT (可用但不标准)   - 建议修复为 BIT(1)
echo    ❌ 错误 (字符串类型)         - 必须修复为 BIT(1)
echo    ❌ 错误 (整数类型过大)       - 必须修复为 BIT(1)
echo.

REM ==================== 确认修复 ====================
set /p confirm="是否继续执行修复？(Y/N): "
if /i not "%confirm%"=="Y" (
    echo.
    echo ⏹️  操作已取消
    echo.
    pause
    exit /b 0
)

REM ==================== 步骤2: 修复 ====================
echo.
echo ================================================
echo 🔧 第2步：执行终极修复
echo ================================================
echo.
echo 修复内容：
echo    1. 清理异常数据（'true'/'false' → 1/0）
echo    2. 修改字段类型为 BIT(1)
echo    3. 设置默认值为 b'0'
echo.

mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% -D%MYSQL_DB% < "终极修复IoT设备表_Boolean字段.sql"

if %ERRORLEVEL% neq 0 (
    echo.
    echo ❌ 修复失败！
    echo.
    echo 🔍 排查建议：
    echo    1. 检查是否有表锁
    echo    2. 确认数据库权限
    echo    3. 查看上方错误信息
    echo.
    pause
    exit /b 1
)

REM ==================== 成功 ====================
echo.
echo ================================================
echo ✅ 修复成功！
echo ================================================
echo.
echo 📋 完成的操作：
echo    ✔ 清理了异常数据
echo    ✔ subsystem_override → BIT(1)
echo    ✔ menu_override → BIT(1)
echo    ✔ 设置默认值为 0 (false)
echo.
echo 💡 下一步：
echo    1. 重启后端服务
echo    2. 测试设备激活功能
echo    3. 确认不再报错 "Incorrect integer value"
echo.
echo 🔗 相关文档：
echo    docs/errors/后端_设备激活失败_Boolean字段类型不匹配.md
echo.
pause














