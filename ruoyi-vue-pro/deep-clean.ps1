# ============================================================================
# Changhui IBMS Project - Deep Clean and Rebuild Script (PowerShell)
# Fixes NoClassDefFoundError, Unresolved compilation problems
# ============================================================================
# Usage:
#   .\deep-clean.ps1                    # Full clean and rebuild
#   .\deep-clean.ps1 -SkipMavenBuild    # Clean only, no build
#   .\deep-clean.ps1 -CleanIdeaConfig   # Also clean IDEA config
#   .\deep-clean.ps1 -KillAllJava       # Kill ALL Java processes (including IDEA)
# 
# If deletion fails due to locked files:
#   1. Close IDEA completely
#   2. Run: .\deep-clean.ps1 -KillAllJava
# ============================================================================

param(
    [switch]$SkipMavenBuild,
    [switch]$CleanIdeaConfig,
    [switch]$Force,
    [switch]$KillAllJava
)

$ErrorActionPreference = "Continue"

function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host ("=" * 80) -ForegroundColor Cyan
    Write-Host "  $Message" -ForegroundColor Cyan
    Write-Host ("=" * 80) -ForegroundColor Cyan
    Write-Host ""
}

function Write-Success {
    param([string]$Message)
    Write-Host "  [OK] $Message" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host "  [WARN] $Message" -ForegroundColor Yellow
}

function Write-Err {
    param([string]$Message)
    Write-Host "  [ERROR] $Message" -ForegroundColor Red
}

function Write-Info {
    param([string]$Message)
    Write-Host "  -> $Message" -ForegroundColor White
}

Set-Location -Path $PSScriptRoot

Write-Host ""
Write-Host ("=" * 80) -ForegroundColor Magenta
Write-Host "  Changhui IBMS - Deep Clean and Rebuild Script" -ForegroundColor Magenta
Write-Host "  Fixes NoClassDefFoundError and class loading issues" -ForegroundColor Gray
Write-Host ("=" * 80) -ForegroundColor Magenta
Write-Host ""
Write-Host "  Project Path: $PSScriptRoot" -ForegroundColor Gray
$currentTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
Write-Host "  Time: $currentTime" -ForegroundColor Gray

# ============================================================================
# Step 1: Kill related processes
# ============================================================================
Write-Header "Step 1/7: Killing Java processes that may lock files"

$ports = @(48888, 8080, 40088, 40083)
foreach ($port in $ports) {
    $connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Where-Object { $_.State -eq "Listen" }
    foreach ($conn in $connections) {
        $process = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
        if ($process) {
            Write-Info "Killing process on port ${port}: $($process.Name) (PID: $($process.Id))"
            Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
        }
    }
}

$javaProcesses = Get-WmiObject Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue
foreach ($proc in $javaProcesses) {
    if ($KillAllJava) {
        Write-Info "Killing ALL Java process (PID: $($proc.ProcessId)): $($proc.Name)"
        Stop-Process -Id $proc.ProcessId -Force -ErrorAction SilentlyContinue
    }
    elseif ($proc.CommandLine -match "maven|yudao|YudaoServerApplication|IotNewGatewayServerApplication|newgateway|spring-boot|iot-biz") {
        Write-Info "Killing Java process (PID: $($proc.ProcessId))"
        Stop-Process -Id $proc.ProcessId -Force -ErrorAction SilentlyContinue
    }
}

$fsNotifier = Get-Process -Name "fsnotifier*" -ErrorAction SilentlyContinue
foreach ($proc in $fsNotifier) {
    Write-Info "Killing fsnotifier process (PID: $($proc.Id))"
    Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
}

if ($KillAllJava) {
    $cursorProcesses = Get-Process -Name "*cursor*", "*code*" -ErrorAction SilentlyContinue | Where-Object { $_.Name -match "cursor|code" -and $_.Name -notmatch "codecatalyst" }
    foreach ($proc in $cursorProcesses) {
        Write-Info "Killing Cursor/VSCode process (PID: $($proc.Id)): $($proc.Name)"
        Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
    }
}

Start-Sleep -Seconds 3
Write-Success "Process cleanup complete"

# ============================================================================
# Step 2: Delete all target directories
# ============================================================================
Write-Header "Step 2/7: Deleting all target directories"

function Force-RemoveDirectory {
    param([string]$Path, [int]$MaxRetries = 3)
    
    for ($retry = 1; $retry -le $MaxRetries; $retry++) {
        if (-not (Test-Path $Path)) {
            return $true
        }
        
        try {
            Remove-Item -Path $Path -Recurse -Force -ErrorAction Stop
            if (-not (Test-Path $Path)) { return $true }
        } catch { }
        
        try {
            cmd /c "rd /s /q `"$Path`"" 2>$null
            if (-not (Test-Path $Path)) { return $true }
        } catch { }
        
        try {
            $emptyDir = Join-Path $env:TEMP "empty_$(Get-Random)"
            New-Item -ItemType Directory -Path $emptyDir -Force | Out-Null
            robocopy $emptyDir $Path /MIR /NFL /NDL /NJH /NJS /nc /ns /np 2>$null
            Remove-Item -Path $Path -Force -Recurse -ErrorAction SilentlyContinue
            Remove-Item -Path $emptyDir -Force -ErrorAction SilentlyContinue
            if (-not (Test-Path $Path)) { return $true }
        } catch { }
        
        try {
            Get-ChildItem -Path $Path -Recurse -Force -ErrorAction SilentlyContinue | Sort-Object { $_.FullName.Length } -Descending | ForEach-Object {
                $_.Attributes = 'Normal'
                Remove-Item -Path $_.FullName -Force -ErrorAction SilentlyContinue
            }
            Remove-Item -Path $Path -Force -Recurse -ErrorAction SilentlyContinue
            if (-not (Test-Path $Path)) { return $true }
        } catch { }
        
        if ($retry -lt $MaxRetries) {
            Write-Warn "Retry $retry/$MaxRetries for: $Path"
            Start-Sleep -Seconds 2
        }
    }
    
    return -not (Test-Path $Path)
}

$targetDirs = Get-ChildItem -Path . -Directory -Recurse -Filter "target" -ErrorAction SilentlyContinue
$deletedCount = 0
$failedDirs = @()

foreach ($dir in $targetDirs) {
    Write-Info "Deleting: $($dir.FullName)"
    if (Force-RemoveDirectory -Path $dir.FullName) {
        $deletedCount++
    }
    else {
        Write-Warn "Failed to delete (may be locked): $($dir.FullName)"
        $failedDirs += $dir.FullName
    }
}

if ($failedDirs.Count -gt 0) {
    Write-Warn "The following directories could not be deleted (likely locked by IDEA or other process):"
    foreach ($dir in $failedDirs) {
        Write-Host "    - $dir" -ForegroundColor Yellow
    }
    Write-Host ""
    Write-Warn "Try: 1) Close IDEA completely  2) Run with -KillAllJava parameter"
}

Write-Success "Deleted $deletedCount target directories"

# ============================================================================
# Step 3: Delete .class file residues
# ============================================================================
Write-Header "Step 3/7: Deleting .class file residues and temp directories"

$dirsToClean = @("cn", "BOOT-INF", "META-INF", "tmp_inspect", "tmp_inspect2", "out")
foreach ($dirName in $dirsToClean) {
    if (Test-Path $dirName) {
        Write-Info "Deleting: $dirName"
        Remove-Item -Path $dirName -Recurse -Force -ErrorAction SilentlyContinue
    }
}

$srcClassFiles = Get-ChildItem -Path . -Recurse -Filter "*.class" -File -ErrorAction SilentlyContinue | Where-Object { $_.FullName -match "\\src\\" }

$classFileCount = 0
foreach ($file in $srcClassFiles) {
    Write-Info "Deleting class file in src: $($file.FullName)"
    Remove-Item -Path $file.FullName -Force -ErrorAction SilentlyContinue
    $classFileCount++
}

$flattenedPoms = Get-ChildItem -Path . -Recurse -Filter ".flattened-pom.xml" -File -ErrorAction SilentlyContinue
foreach ($file in $flattenedPoms) {
    Write-Info "Deleting: $($file.FullName)"
    Remove-Item -Path $file.FullName -Force -ErrorAction SilentlyContinue
}

Write-Success "Class file cleanup complete (deleted $classFileCount class files in src)"

# ============================================================================
# Step 4: Clean Maven local repository cache
# ============================================================================
Write-Header "Step 4/7: Cleaning Maven local repository cache"

$m2Paths = @(
    "$env:USERPROFILE\.m2\repository\cn\iocoder\boot",
    "F:\repo\cn\iocoder\boot",
    "D:\repo\cn\iocoder\boot"
)

$cleanedCount = 0
foreach ($m2Path in $m2Paths) {
    if (Test-Path $m2Path) {
        Write-Info "Deleting Maven cache: $m2Path"
        Remove-Item -Path $m2Path -Recurse -Force -ErrorAction SilentlyContinue
        $cleanedCount++
    }
}

if ($cleanedCount -gt 0) {
    Write-Success "Maven local repository cache cleaned ($cleanedCount locations)"
}
else {
    Write-Info "No Maven cache directories found, skipping"
}

# ============================================================================
# Step 5: Clean IDEA compilation output cache
# ============================================================================
Write-Header "Step 5/7: Cleaning IDEA compilation output cache"

$outDirs = Get-ChildItem -Path . -Directory -Recurse -Filter "out" -ErrorAction SilentlyContinue
foreach ($dir in $outDirs) {
    Write-Info "Deleting: $($dir.FullName)"
    Remove-Item -Path $dir.FullName -Recurse -Force -ErrorAction SilentlyContinue
}

$ideaDirs = @(".idea\modules", ".idea\libraries", ".idea\artifacts")
foreach ($ideaDir in $ideaDirs) {
    if (Test-Path $ideaDir) {
        Write-Info "Deleting: $ideaDir"
        Remove-Item -Path $ideaDir -Recurse -Force -ErrorAction SilentlyContinue
    }
}

if ($CleanIdeaConfig) {
    Write-Warn "Cleaning complete .idea directory (project reimport required)"
    if (Test-Path ".idea") {
        Write-Info "Deleting: .idea directory"
        Remove-Item -Path ".idea" -Recurse -Force -ErrorAction SilentlyContinue
    }
    
    $imlFiles = Get-ChildItem -Path . -Recurse -Filter "*.iml" -File -ErrorAction SilentlyContinue
    foreach ($file in $imlFiles) {
        Write-Info "Deleting: $($file.FullName)"
        Remove-Item -Path $file.FullName -Force -ErrorAction SilentlyContinue
    }
    
    $ideaProjectFiles = @("yudao.ipr", "yudao.iws")
    foreach ($file in $ideaProjectFiles) {
        if (Test-Path $file) {
            Write-Info "Deleting: $file"
            Remove-Item -Path $file -Force -ErrorAction SilentlyContinue
        }
    }
}

Write-Success "IDEA compilation cache cleaned"

# ============================================================================
# Step 6: Execute Maven full rebuild
# ============================================================================
if (-not $SkipMavenBuild) {
    Write-Header "Step 6/7: Executing Maven full rebuild"
    
    Write-Host "  Command: mvn install -DskipTests -U -T 1C" -ForegroundColor White
    Write-Host "  Options:" -ForegroundColor Gray
    Write-Host "    (no clean) Script already cleaned target directories thoroughly" -ForegroundColor Gray
    Write-Host "    -U         Force update snapshots" -ForegroundColor Gray
    Write-Host "    -T 1C      Multi-threaded build (1 thread per CPU core)" -ForegroundColor Gray
    Write-Host ""
    
    if (Test-Path "mvnw.cmd") {
        Write-Info "Detected Maven Wrapper, using mvnw.cmd"
        & .\mvnw.cmd install -DskipTests -U -T 1C
    }
    else {
        Write-Info "Using system Maven"
        & mvn install -DskipTests -U -T 1C
    }
    $buildSuccess = ($LASTEXITCODE -eq 0)
}
else {
    Write-Header "Step 6/7: Skipping Maven build (-SkipMavenBuild parameter used)"
    $buildSuccess = $true
}

# ============================================================================
# Step 7: Complete
# ============================================================================
Write-Header "Step 7/7: Complete"

if ($buildSuccess) {
    Write-Host ""
    Write-Host ("=" * 80) -ForegroundColor Green
    Write-Host "  [SUCCESS] Deep clean and rebuild complete!" -ForegroundColor Green
    Write-Host ("=" * 80) -ForegroundColor Green
    Write-Host ""
    Write-Host "  Recommended next steps:" -ForegroundColor Yellow
    Write-Host "    1. In IDEA: File -> Invalidate Caches... -> Invalidate and Restart"
    Write-Host "    2. Reload Maven project: Right-click project -> Maven -> Reload project"
    Write-Host "    3. Wait for IDEA indexing to complete before starting the project"
    Write-Host ""
    Write-Host "  To start the project:" -ForegroundColor Yellow
    Write-Host "    Option 1: Run YudaoServerApplication in IDE"
    Write-Host "    Option 2: Run .\start-server.bat"
    Write-Host "    Option 3: cd yudao-server; mvn spring-boot:run"
    Write-Host ""
    Write-Host "  Project Info:" -ForegroundColor Gray
    Write-Host "    - Server Port: 48888"
    Write-Host "    - Database: 192.168.1.126:3306/ch_ibms"
    Write-Host ""
}
else {
    Write-Host ""
    Write-Host ("=" * 80) -ForegroundColor Red
    Write-Host "  [FAILED] Build error occurred, please check the error messages above" -ForegroundColor Red
    Write-Host ("=" * 80) -ForegroundColor Red
    Write-Host ""
    Write-Host "  Troubleshooting:" -ForegroundColor Yellow
    Write-Host "    1. Check network connection (dependencies need to be downloaded)"
    Write-Host "    2. Check Maven settings.xml configuration"
    Write-Host "    3. Try closing all IDEs and run this script again"
    Write-Host "    4. Check specific compilation errors (may be code issues)"
    Write-Host ""
}

Write-Host "Press any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
