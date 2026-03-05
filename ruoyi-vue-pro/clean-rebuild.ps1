# ============================================
# Yudao Project Clean & Rebuild Script
# Fix SNAPSHOT cache issues
# ============================================

param(
    [switch]$Full,      # Full clean (include IDEA cache)
    [switch]$SkipBuild, # Only clean, skip build
    [switch]$Quick,     # Quick mode: only clean resolver cache, use -U
    [switch]$NoKill     # Skip killing Java processes
)

$ErrorActionPreference = "Continue"

# Maven local repository path
$MavenRepo = "F:\repo"
$YudaoGroupPath = "$MavenRepo\cn\iocoder\boot"

# Server ports to check (yudao-server and gateway)
$ServerPorts = @(
    @{ Port = 48888; Name = "yudao-server (业务服务)" },
    @{ Port = 48083; Name = "gateway (网关服务)" }
)

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Yudao Project Clean & Rebuild Script" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

# ============================================
# Step 0: Kill running Java processes
# ============================================
if (-not $NoKill) {
    Write-Host "[0/5] Checking for running Java processes..." -ForegroundColor Yellow
    
    $killedCount = 0
    
    # Method 1: Find processes by port
    foreach ($portInfo in $ServerPorts) {
        $port = $portInfo.Port
        $name = $portInfo.Name
        
        $portProcess = $null
        try {
            $netstatOutput = netstat -ano | Select-String ":$port\s"
            if ($netstatOutput) {
                foreach ($line in $netstatOutput) {
                    if ($line -match '\s+LISTENING\s+(\d+)') {
                        $portProcess = $Matches[1]
                        break
                    }
                }
            }
        } catch {}
        
        if ($portProcess) {
            Write-Host "  Found $name on port $port (PID: $portProcess)" -ForegroundColor Yellow
            try {
                $proc = Get-Process -Id $portProcess -ErrorAction SilentlyContinue
                if ($proc) {
                    Write-Host "    Process: $($proc.ProcessName) (PID: $portProcess)" -ForegroundColor Gray
                    Write-Host "    Killing process..." -ForegroundColor Yellow
                    Stop-Process -Id $portProcess -Force -ErrorAction SilentlyContinue
                    Start-Sleep -Milliseconds 500
                    
                    # Verify process is killed
                    $checkProc = Get-Process -Id $portProcess -ErrorAction SilentlyContinue
                    if ($checkProc) {
                        Write-Host "    WARNING: Process still running, trying taskkill..." -ForegroundColor Red
                        taskkill /F /PID $portProcess 2>$null
                        Start-Sleep -Milliseconds 500
                    }
                    Write-Host "    Process killed successfully" -ForegroundColor Green
                    $killedCount++
                }
            } catch {
                Write-Host "    Could not kill process: $_" -ForegroundColor Red
            }
        } else {
            Write-Host "  No process found on port $port ($name)" -ForegroundColor Green
        }
    }
    
    # Wait a bit if we killed any processes
    if ($killedCount -gt 0) {
        Start-Sleep -Seconds 1
    }
    
    # Method 2: Find YudaoServerApplication/Gateway processes (backup check)
    $javaProcs = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
        try {
            $cmdLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $($_.Id)" -ErrorAction SilentlyContinue).CommandLine
            $cmdLine -and ($cmdLine -like "*YudaoServerApplication*" -or $cmdLine -like "*yudao-server*" -or $cmdLine -like "*yudao-gateway*" -or $cmdLine -like "*GatewayServerApplication*")
        } catch { $false }
    }
    
    if ($javaProcs) {
        foreach ($proc in $javaProcs) {
            Write-Host "  Found Yudao Java process (PID: $($proc.Id))" -ForegroundColor Yellow
            Write-Host "    Killing process..." -ForegroundColor Yellow
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        }
        Start-Sleep -Seconds 1
        Write-Host "  Yudao processes killed" -ForegroundColor Green
    }
    
    Write-Host ""
}

# Quick mode: only clean resolver cache and rebuild with -U
if ($Quick) {
    Write-Host "[Quick Mode] Cleaning Maven resolver cache and MapStruct..." -ForegroundColor Yellow
    
    if (Test-Path $YudaoGroupPath) {
        # Clean resolver-status.properties and _remote.repositories files
        $resolverFiles = Get-ChildItem -Path $YudaoGroupPath -Recurse -File | 
            Where-Object { $_.Name -eq "resolver-status.properties" -or $_.Name -eq "_remote.repositories" -or $_.Name -like "*.lastUpdated" }
        $count = 0
        foreach ($file in $resolverFiles) {
            Remove-Item -Force $file.FullName -ErrorAction SilentlyContinue
            $count++
        }
        Write-Host "  Cleaned $count resolver cache files" -ForegroundColor Green
    }
    
    # Clean MapStruct generated files (important for "Unresolved compilation problems")
    Write-Host "  Cleaning MapStruct generated files..." -ForegroundColor Yellow
    $gsCount = 0
    Get-ChildItem -Path $projectRoot -Recurse -Directory -Filter "generated-sources" -ErrorAction SilentlyContinue | ForEach-Object {
        Remove-Item -Recurse -Force $_.FullName -ErrorAction SilentlyContinue
        $gsCount++
    }
    # Also clean IDEA's out directory
    $ideaOut = Join-Path $projectRoot "out"
    if (Test-Path $ideaOut) {
        Remove-Item -Recurse -Force $ideaOut -ErrorAction SilentlyContinue
        $gsCount++
    }
    Write-Host "  Cleaned $gsCount generated-sources directories" -ForegroundColor Green
    
    Write-Host ""
    Write-Host "[Quick Mode] Rebuilding with -U flag..." -ForegroundColor Yellow
    Push-Location $projectRoot
    
    $mvnCmd = "mvn clean install -DskipTests -U"
    Write-Host "  Running: $mvnCmd" -ForegroundColor Gray
    Write-Host ""
    
    Invoke-Expression $mvnCmd
    $buildResult = $LASTEXITCODE
    
    Pop-Location
    
    if ($buildResult -eq 0) {
        Write-Host ""
        Write-Host "Build SUCCESS!" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "Build FAILED, try running without -Quick for full clean" -ForegroundColor Red
        exit 1
    }
    exit 0
}

# 1. Clean Maven local repository SNAPSHOT
Write-Host "[1/5] Cleaning Maven SNAPSHOT versions..." -ForegroundColor Yellow

if (Test-Path $YudaoGroupPath) {
    $snapshotDirs = Get-ChildItem -Path $YudaoGroupPath -Recurse -Directory | Where-Object { $_.Name -like "*SNAPSHOT*" }
    $count = 0
    foreach ($dir in $snapshotDirs) {
        Write-Host "  Delete: $($dir.FullName)" -ForegroundColor Gray
        Remove-Item -Recurse -Force $dir.FullName -ErrorAction SilentlyContinue
        $count++
    }
    Write-Host "  Cleaned $count SNAPSHOT directories" -ForegroundColor Green
} else {
    Write-Host "  Maven repo path not found: $YudaoGroupPath" -ForegroundColor Red
}

# 2. Clean Maven resolver cache files (important!)
Write-Host ""
Write-Host "[2/5] Cleaning Maven resolver cache files..." -ForegroundColor Yellow

if (Test-Path $YudaoGroupPath) {
    # Clean resolver-status.properties, _remote.repositories, and *.lastUpdated files
    $resolverFiles = Get-ChildItem -Path $YudaoGroupPath -Recurse -File | 
        Where-Object { 
            $_.Name -eq "resolver-status.properties" -or 
            $_.Name -eq "_remote.repositories" -or 
            $_.Name -like "*.lastUpdated" -or
            $_.Name -like "maven-metadata-*.xml"
        }
    $count = 0
    foreach ($file in $resolverFiles) {
        Remove-Item -Force $file.FullName -ErrorAction SilentlyContinue
        $count++
    }
    Write-Host "  Cleaned $count resolver cache files" -ForegroundColor Green
}

# 3. Clean project target directories and MapStruct generated sources
Write-Host ""
Write-Host "[3/5] Cleaning project target directories..." -ForegroundColor Yellow

$targetDirs = Get-ChildItem -Path $projectRoot -Recurse -Directory -Filter "target" -ErrorAction SilentlyContinue | 
    Where-Object { $_.FullName -notlike "*node_modules*" }

$count = 0
foreach ($dir in $targetDirs) {
    Write-Host "  Delete: $($dir.FullName)" -ForegroundColor Gray
    Remove-Item -Recurse -Force $dir.FullName -ErrorAction SilentlyContinue
    $count++
}
Write-Host "  Cleaned $count target directories" -ForegroundColor Green

# 3.1 Clean IDEA's annotation processor output (MapStruct cache)
Write-Host ""
Write-Host "[3.1/5] Cleaning IDEA annotation processor cache..." -ForegroundColor Yellow
$ideaOutDirs = @(
    (Join-Path $projectRoot "out"),
    (Join-Path $projectRoot ".idea\modules")
)
$apCount = 0
foreach ($dir in $ideaOutDirs) {
    if (Test-Path $dir) {
        Write-Host "  Delete: $dir" -ForegroundColor Gray
        Remove-Item -Recurse -Force $dir -ErrorAction SilentlyContinue
        $apCount++
    }
}
# Also clean generated-sources in each module (in case target wasn't fully deleted)
Get-ChildItem -Path $projectRoot -Recurse -Directory -Filter "generated-sources" -ErrorAction SilentlyContinue | ForEach-Object {
    Write-Host "  Delete: $($_.FullName)" -ForegroundColor Gray
    Remove-Item -Recurse -Force $_.FullName -ErrorAction SilentlyContinue
    $apCount++
}
Write-Host "  Cleaned $apCount annotation processor directories" -ForegroundColor Green

# 4. Clean IDEA cache (optional)
if ($Full) {
    Write-Host ""
    Write-Host "[3.5/5] Cleaning IDEA cache files..." -ForegroundColor Yellow
    
    # Clean all .iml files
    Get-ChildItem -Path $projectRoot -Recurse -Filter "*.iml" -ErrorAction SilentlyContinue | ForEach-Object {
        Remove-Item -Force $_.FullName -ErrorAction SilentlyContinue
        Write-Host "  Deleted: $($_.FullName)" -ForegroundColor Gray
    }
    
    # Clean .idea/libraries
    $ideaLibs = Join-Path $projectRoot ".idea\libraries"
    if (Test-Path $ideaLibs) {
        Remove-Item -Recurse -Force $ideaLibs -ErrorAction SilentlyContinue
        Write-Host "  Deleted: $ideaLibs" -ForegroundColor Gray
    }
    
    # Clean .idea/modules.xml
    $modulesXml = Join-Path $projectRoot ".idea\modules.xml"
    if (Test-Path $modulesXml) {
        Remove-Item -Force $modulesXml -ErrorAction SilentlyContinue
        Write-Host "  Deleted: $modulesXml" -ForegroundColor Gray
    }
}

# 5. Rebuild
if (-not $SkipBuild) {
    Write-Host ""
    Write-Host "[4/5] Rebuilding project..." -ForegroundColor Yellow
    Write-Host "  This may take a few minutes, please wait..." -ForegroundColor Gray
    
    Push-Location $projectRoot
    
    # Use mvn clean install with -U flag to force update snapshots
    # First build without parallel to ensure correct dependency order
    $mvnCmd = "mvn clean install -DskipTests -U"
    Write-Host "  Running: $mvnCmd" -ForegroundColor Gray
    Write-Host ""
    
    Invoke-Expression $mvnCmd
    $buildResult = $LASTEXITCODE
    
    Pop-Location
    
    if ($buildResult -eq 0) {
        Write-Host ""
        Write-Host "[5/5] Build SUCCESS!" -ForegroundColor Green
        
        # Verify critical MapStruct class files
        $criticalClasses = @(
            "$projectRoot\yudao-module-system\target\classes\cn\iocoder\yudao\module\system\convert\auth\AuthConvertImpl.class"
        )
        $verifyFailed = $false
        foreach ($classFile in $criticalClasses) {
            if (Test-Path $classFile) {
                $size = (Get-Item $classFile).Length
                if ($size -lt 5000) {
                    Write-Host "  WARNING: $classFile may be corrupted (size: $size bytes)" -ForegroundColor Red
                    $verifyFailed = $true
                }
            }
        }
        if ($verifyFailed) {
            Write-Host "  Some class files may be corrupted, try running: mvn clean compile -DskipTests" -ForegroundColor Yellow
        }
    } else {
        Write-Host ""
        Write-Host "[5/5] Build FAILED, please check error messages" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host ""
    Write-Host "[4/5] Skipped build (-SkipBuild parameter used)" -ForegroundColor Yellow
    Write-Host "[5/5] Clean completed! Please run: mvn clean install -DskipTests -U" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Clean & Rebuild Completed!" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Tips:" -ForegroundColor Yellow
Write-Host "  1. Now you can restart yudao-server in IDEA" -ForegroundColor Gray
Write-Host "  2. If still having issues: File -> Invalidate Caches and Restart" -ForegroundColor Gray
Write-Host "  3. Reimport Maven: Right-click pom.xml -> Maven -> Reload Project" -ForegroundColor Gray
Write-Host ""
Write-Host "Usage:" -ForegroundColor Yellow
Write-Host "  .\clean-rebuild.ps1           # Full clean & rebuild (auto-kills running process)" -ForegroundColor Gray
Write-Host "  .\clean-rebuild.ps1 -Quick    # Quick fix for MapStruct/cache issues" -ForegroundColor Gray
Write-Host "  .\clean-rebuild.ps1 -Full     # Full clean including IDEA cache" -ForegroundColor Gray
Write-Host "  .\clean-rebuild.ps1 -NoKill   # Skip killing Java processes" -ForegroundColor Gray
Write-Host "  .\clean-rebuild.ps1 -SkipBuild # Only clean, don't rebuild" -ForegroundColor Gray
Write-Host ""
Write-Host "Common Issues:" -ForegroundColor Yellow
Write-Host "  - MapStruct 'Unresolved compilation': Run -Quick" -ForegroundColor Gray
Write-Host "  - SNAPSHOT not updated: Run full script (no parameters)" -ForegroundColor Gray
Write-Host "  - Port in use: Script auto-kills 48888(server) & 48083(gateway)" -ForegroundColor Gray
Write-Host ""
