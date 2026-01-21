# ========================================
# 视频巡更模块一键部署脚本 (PowerShell版本)
# 注意：此文件使用UTF-8编码保存
# ========================================

# 设置控制台编码为UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "视频巡更模块一键部署脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# MySQL连接参数
$MYSQL_HOST = "localhost"
$MYSQL_PORT = "3306"
$MYSQL_USER = "root"
$MYSQL_DB = "ruoyi-vue-pro"
$MYSQL_CHARSET = "utf8mb4"

# 提示输入MySQL密码
$MYSQL_PASSWORD = Read-Host "请输入MySQL密码" -AsSecureString
$MYSQL_PASSWORD_PLAIN = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($MYSQL_PASSWORD)
)

Write-Host ""
Write-Host "开始部署视频巡更模块..." -ForegroundColor Yellow
Write-Host ""

# 检查MySQL命令是否可用
try {
    $null = Get-Command mysql -ErrorAction Stop
} catch {
    Write-Host "❌ 错误：未找到mysql命令，请确保MySQL客户端已安装并添加到PATH环境变量" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}

# 检查SQL文件是否存在
$sqlFiles = @(
    "iot_video_patrol_tables.sql",
    "video_patrol_menu_permissions.sql"
)

foreach ($file in $sqlFiles) {
    if (-not (Test-Path $file)) {
        Write-Host "❌ 错误：找不到文件 $file" -ForegroundColor Red
        Read-Host "按任意键退出"
        exit 1
    }
}

# 执行数据表创建SQL
Write-Host "[1/2] 创建数据表..." -ForegroundColor Yellow
$cmd1 = "mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD_PLAIN --default-character-set=$MYSQL_CHARSET $MYSQL_DB"
Get-Content "iot_video_patrol_tables.sql" -Encoding UTF8 | & cmd /c "$cmd1" 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 数据表创建失败！" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}
Write-Host "✅ 数据表创建成功！" -ForegroundColor Green
Write-Host ""

# 执行菜单权限配置SQL
Write-Host "[2/2] 配置菜单和权限..." -ForegroundColor Yellow
$cmd2 = "mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD_PLAIN --default-character-set=$MYSQL_CHARSET $MYSQL_DB"
Get-Content "video_patrol_menu_permissions.sql" -Encoding UTF8 | & cmd /c "$cmd2" 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 菜单权限配置失败！" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}
Write-Host "✅ 菜单权限配置成功！" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🎉 视频巡更模块部署完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "请执行以下步骤：" -ForegroundColor Yellow
Write-Host "1. 重启后端服务"
Write-Host "2. 清除浏览器缓存"
Write-Host "3. 重新登录系统"
Write-Host "4. 在左侧菜单查看'视频巡更'模块"
Write-Host ""

# 清除密码变量
$MYSQL_PASSWORD_PLAIN = $null
$MYSQL_PASSWORD = $null

Read-Host "按任意键退出"
