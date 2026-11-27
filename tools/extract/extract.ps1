param(
    [Parameter(Mandatory=$true)][string]$User,     # Oracle schema user that owns ZLV_MDM_EVENTS_STORE
    [Parameter(Mandatory=$true)][string]$Tns,      # TNS alias or host:port/service
    [string[]]$Tables = @("ZLV_MDM_EVENTS_STORE"),
    [string]$OutDir = ".\extract_out"
)
# extract.ps1 - spool Oracle tables to pipe-delimited files and zip them.
# Read-only: SELECT only. Password is asked at the prompt, kept in memory,
# and passed to sqlplus on stdin (never on the command line, never on disk).
# Needs sqlplus 12.2+ (SET MARKUP CSV). ASCII output only.
$ErrorActionPreference = "Stop"

$sec = Read-Host "Oracle password for $User" -AsSecureString
$pw = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec))

$stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$run = Join-Path $OutDir $stamp
New-Item -ItemType Directory -Force -Path $run | Out-Null
$runFull = (Resolve-Path $run).Path

$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine("WHENEVER SQLERROR CONTINUE")
[void]$sb.AppendLine("CONNECT $User/`"$pw`"@$Tns")
[void]$sb.AppendLine("SET MARKUP CSV ON DELIMITER | QUOTE OFF")
[void]$sb.AppendLine("SET FEEDBACK OFF")
[void]$sb.AppendLine("SET TERMOUT OFF")
[void]$sb.AppendLine("SET NUMWIDTH 20")  # epoch-ms numbers in full, not 1.7958E+12
[void]$sb.AppendLine("SET LONG 20000000")
[void]$sb.AppendLine("SET LONGCHUNKSIZE 20000000")
[void]$sb.AppendLine("SET TRIMSPOOL ON")
[void]$sb.AppendLine("SET PAGESIZE 0 EMBEDDED ON")
[void]$sb.AppendLine("SET HEADING ON")
[void]$sb.AppendLine("ALTER SESSION SET NLS_TIMESTAMP_FORMAT='YYYY-MM-DD HH24:MI:SS.FF3';")
foreach ($t in $Tables) {
    $f = Join-Path $runFull ("{0}.dsv" -f $t)
    [void]$sb.AppendLine("SPOOL `"$f`"")
    [void]$sb.AppendLine("SELECT * FROM $t;")
    [void]$sb.AppendLine("SPOOL OFF")
}
[void]$sb.AppendLine("EXIT")

$sb.ToString() | sqlplus -s /nolog | Out-Null
$pw = $null

Get-ChildItem $runFull -Filter *.dsv | ForEach-Object {
    $n = (Get-Content $_.FullName | Measure-Object -Line).Lines - 1
    Write-Host ("[extract] {0}: {1} rows" -f $_.Name, $n)
}
$zip = Join-Path (Resolve-Path $OutDir).Path ("extract_{0}.zip" -f $stamp)
Compress-Archive -Path (Join-Path $runFull "*") -DestinationPath $zip -Force
Write-Host "[extract] zip: $zip"
