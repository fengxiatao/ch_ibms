$pattern = [regex]"(?:hasPermi|checkPermi|v-hasPermi)\b[^`r`n]*?['`"]([a-z][a-z\-]+:[a-zA-Z0-9_:\-]+)['`"]"
$results = New-Object System.Collections.Generic.HashSet[string]
Get-ChildItem -Path "yudao-ui-admin-vue3\src" -Recurse -Include *.vue,*.ts,*.tsx -File | ForEach-Object {
    $text = Get-Content -Path $_.FullName -Raw
    foreach ($m in $pattern.Matches($text)) {
        [void]$results.Add($m.Groups[1].Value)
    }
}
$results | Sort-Object | Set-Content -Encoding UTF8 .tmp_frontend_permissions.txt
"Total unique: $($results.Count)"
