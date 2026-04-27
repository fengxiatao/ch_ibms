$files = @(
  'e:\ch\.tenant_sync\phase3_sync_resume.sql',
  'e:\ch\.tenant_sync\phase3_sync_resume2.sql',
  'e:\ch\.tenant_sync\phase3_sync_resume3.sql'
)
foreach ($f in $files) {
  $c = Get-Content $f -Raw
  $c = $c -replace 'SUBSTRING\(remark,10\)','SUBSTRING(remark,9)'
  $c = $c -replace 'SUBSTRING\(description,10\)','SUBSTRING(description,9)'
  $c = $c -replace 'SUBSTRING\(detail_text,10\)','SUBSTRING(detail_text,9)'
  $c = $c -replace 'SUBSTRING\(icon,10\)','SUBSTRING(icon,9)'
  Set-Content -Path $f -Value $c -NoNewline
  Write-Host "fixed: $f"
}
