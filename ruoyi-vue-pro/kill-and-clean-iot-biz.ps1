param(
    [int]$Port = 48888,
    [switch]$SkipBuild,
    [switch]$SkipStart
)

$ErrorActionPreference = "Stop"

$baseDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $baseDir

Write-Host "Base dir: $baseDir" -ForegroundColor Yellow
Write-Host "Target port: $Port" -ForegroundColor Yellow

function Get-ListeningPidsByPort {
    param([int]$TargetPort)
    $lines = netstat -ano -p tcp | Select-String ":$TargetPort\s+.*LISTENING"
    if (-not $lines) { return @() }
    $pids = @()
    foreach ($line in $lines) {
        $tokens = ($line.ToString().Trim() -split "\s+")
        if ($tokens.Count -ge 5) {
            $pidValue = 0
            if ([int]::TryParse($tokens[4], [ref]$pidValue)) {
                $pids += $pidValue
            }
        }
    }
    return $pids | Select-Object -Unique
}

# 1) 仅释放目标端口占用，避免误杀全部 Java 进程
$portPids = Get-ListeningPidsByPort -TargetPort $Port
if ($portPids.Count -eq 0) {
    Write-Host "No LISTENING process found on port $Port." -ForegroundColor Green
} else {
    foreach ($pid in $portPids) {
        try {
            $p = Get-CimInstance Win32_Process -Filter "ProcessId = $pid"
            $cmd = if ($p -and $p.CommandLine) { $p.CommandLine } else { "<unknown>" }
            Write-Host "Killing PID=$pid on port $Port" -ForegroundColor Red
            Write-Host "  CommandLine: $cmd" -ForegroundColor DarkYellow
            Stop-Process -Id $pid -Force -ErrorAction Stop
        } catch {
            Write-Host "[WARN] Failed to kill PID=$pid : $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
}
Start-Sleep -Milliseconds 800

# 2) 删除 yudao-module-iot-biz 的 target 目录
$modulePath = Join-Path $baseDir "yudao-module-iot\yudao-module-iot-biz"
$targetPath = Join-Path $modulePath "target"

Write-Host "Deleting target folder: $targetPath" -ForegroundColor Yellow
if (Test-Path $targetPath) {
    Remove-Item -Path $targetPath -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "Deleted target folder." -ForegroundColor Green
} else {
    Write-Host "Target folder not found, skip delete." -ForegroundColor Green
}

# 3) 可选重编译
if (-not $SkipBuild) {
    Write-Host "Running: mvn clean install -DskipTests -rf :yudao-module-iot-biz" -ForegroundColor Yellow
    try {
        & mvn clean install -DskipTests -rf :yudao-module-iot-biz
        if ($LASTEXITCODE -ne 0) {
            throw "mvn exited with code $LASTEXITCODE"
        }
    } catch {
        Write-Host "[WARN] Build failed: $($_.Exception.Message)" -ForegroundColor Yellow
        Write-Host "You can retry manually in $baseDir." -ForegroundColor Yellow
        exit 1
    }
} else {
    Write-Host "Skip build by flag: -SkipBuild" -ForegroundColor Yellow
}

# 4) 启动后端（默认执行）
if (-not $SkipStart) {
    # 避免 spring-boot 前缀解析失败，直接使用已在 yudao-server 配置的 exec-maven-plugin
    Write-Host "Starting backend: mvn -pl yudao-server -am org.codehaus.mojo:exec-maven-plugin:3.6.0:exec" -ForegroundColor Yellow
    & mvn -pl yudao-server -am org.codehaus.mojo:exec-maven-plugin:3.6.0:exec
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[WARN] Backend startup command exited with code $LASTEXITCODE" -ForegroundColor Yellow
        exit $LASTEXITCODE
    }
} else {
    Write-Host "Skip backend start by flag: -SkipStart" -ForegroundColor Yellow
}

Write-Host "All done." -ForegroundColor Green

