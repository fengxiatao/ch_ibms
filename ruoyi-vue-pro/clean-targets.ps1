# Force clean all target directories
# Usage: .\clean-targets.ps1

$rootPath = "E:\ch\ruoyi-vue-pro"

Write-Host "========================================"
Write-Host "Starting target directory cleanup..."
Write-Host "Root: $rootPath"
Write-Host "========================================"

# Find all target directories
$targetDirs = Get-ChildItem -Path $rootPath -Directory -Recurse -Filter "target" -ErrorAction SilentlyContinue

if ($targetDirs.Count -eq 0) {
    Write-Host "No target directories found"
    exit 0
}

Write-Host ""
Write-Host "Found $($targetDirs.Count) target directories:"
Write-Host ""

$totalSize = 0
$deletedCount = 0
$failedCount = 0

foreach ($dir in $targetDirs) {
    $relativePath = $dir.FullName.Replace($rootPath, ".")
    
    try {
        # Calculate directory size
        $size = (Get-ChildItem -Path $dir.FullName -Recurse -File -ErrorAction SilentlyContinue | 
                 Measure-Object -Property Length -Sum).Sum
        if ($null -eq $size) { $size = 0 }
        $sizeMB = [math]::Round($size / 1MB, 2)
        $totalSize += $size
        
        Write-Host "Deleting: $relativePath ($sizeMB MB)"
        
        # Force delete directory and all contents
        Remove-Item -Path $dir.FullName -Recurse -Force -ErrorAction Stop
        
        $deletedCount++
        Write-Host "  Done"
    }
    catch {
        $failedCount++
        Write-Host "  Failed: $($_.Exception.Message)"
    }
}

$totalSizeMB = [math]::Round($totalSize / 1MB, 2)
$totalSizeGB = [math]::Round($totalSize / 1GB, 2)

Write-Host ""
Write-Host "========================================"
Write-Host "Cleanup completed!"
Write-Host "Deleted: $deletedCount directories"
if ($failedCount -gt 0) {
    Write-Host "Failed: $failedCount directories"
}
Write-Host "Space freed: $totalSizeMB MB"
Write-Host "========================================"
