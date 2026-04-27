$backend = Get-Content .tmp_backend_permissions.txt
$frontend = Get-Content .tmp_frontend_permissions.txt

$backendSet = [System.Collections.Generic.HashSet[string]]::new()
foreach ($p in $backend) { [void]$backendSet.Add($p) }
$frontendSet = [System.Collections.Generic.HashSet[string]]::new()
foreach ($p in $frontend) { [void]$frontendSet.Add($p) }

function Write-Audit($lines, $path) {
    [System.IO.File]::WriteAllLines((Join-Path (Get-Location) $path), $lines, [System.Text.UTF8Encoding]::new($false))
}

# Backend permissions grouped
$out1 = New-Object System.Collections.Generic.List[string]
$out1.Add("# Backend @PreAuthorize permission inventory")
$out1.Add("")
$out1.Add("Total unique: " + $backend.Count + " (from ruoyi-vue-pro/yudao-module-*/controller/**/*.java)")
$out1.Add("")
$out1.Add("## Grouped by module prefix")
foreach ($g in ($backend | Group-Object { ($_ -split ':')[0] } | Sort-Object Name)) {
    $out1.Add("")
    $out1.Add("### " + $g.Name + "  (" + $g.Count + ")")
    $out1.Add("")
    foreach ($p in ($g.Group | Sort-Object)) { $out1.Add("- ``" + $p + "``") }
}
Write-Audit $out1 "docs/permission-audit/backend-preauthorize.md"

# Frontend permissions grouped with backend match marker
$out2 = New-Object System.Collections.Generic.List[string]
$out2.Add("# Frontend v-hasPermi / checkPermi permission inventory")
$out2.Add("")
$out2.Add("Total unique: " + $frontend.Count + " (from yudao-ui-admin-vue3/src)")
$out2.Add("")
$out2.Add("## Grouped by module prefix (OK = matched in backend, MISS = no backend @PreAuthorize)")
foreach ($g in ($frontend | Group-Object { ($_ -split ':')[0] } | Sort-Object Name)) {
    $inB = 0; $outB = 0
    foreach ($p in $g.Group) { if ($backendSet.Contains($p)) { $inB++ } else { $outB++ } }
    $out2.Add("")
    $out2.Add("### " + $g.Name + "  (" + $g.Count + " total; OK=" + $inB + ", MISS=" + $outB + ")")
    $out2.Add("")
    foreach ($p in ($g.Group | Sort-Object)) {
        $marker = if ($backendSet.Contains($p)) { "OK" } else { "**MISS**" }
        $out2.Add("- [" + $marker + "] ``" + $p + "``")
    }
}
Write-Audit $out2 "docs/permission-audit/frontend-haspermi.md"

# Mismatch report
$frontendOnly = $frontend | Where-Object { -not $backendSet.Contains($_) } | Sort-Object
$backendOnly = $backend | Where-Object { -not $frontendSet.Contains($_) } | Sort-Object
$out3 = New-Object System.Collections.Generic.List[string]
$out3.Add("# Permission mismatch report")
$out3.Add("")
$out3.Add("## A. Frontend-only (v-hasPermi strings with no matching backend @PreAuthorize)")
$out3.Add("")
$out3.Add("Count: " + $frontendOnly.Count + ". These are the primary fix targets (tenant_admin will see a button but API will 403, or the button stays hidden forever).")
$out3.Add("")
foreach ($p in $frontendOnly) { $out3.Add("- ``" + $p + "``") }
$out3.Add("")
$out3.Add("## B. Backend-only (@PreAuthorize with no frontend reference)")
$out3.Add("")
$out3.Add("Count: " + $backendOnly.Count + ". Often internal endpoints / batch import / called via composed UI; safe to ignore unless a button is missing.")
$out3.Add("")
foreach ($p in $backendOnly) { $out3.Add("- ``" + $p + "``") }
Write-Audit $out3 "docs/permission-audit/mismatch-report.md"

"Backend: " + $backend.Count + "; Frontend: " + $frontend.Count + "; FrontOnly: " + $frontendOnly.Count + "; BackOnly: " + $backendOnly.Count
