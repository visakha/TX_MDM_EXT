# Writes seeded random B360 File Import test files (rounds 1-3 + expected results).
# Plain Java main: no Spring start, no database, no scan job.
# Usage: .\generate-file-import.ps1 -Seed 42 -Guests 5 -OutDir C:\tx-mdm-ext\file-import
param(
  [long]$Seed = 42,
  [int]$Guests = 5,
  [int]$Rounds = 3,
  [string]$BaseDate = "2026-11-01",
  [string]$OutDir = ".\file-import-out"
)
$ErrorActionPreference = "Stop"
$classes = Join-Path $PSScriptRoot "target\classes"
if (-not (Test-Path $classes)) {
  Write-Host "[generate] target\classes missing - run: mvn -B -DskipTests package"
  exit 1
}
java -cp $classes com.visakha.txmdmext.model.zlvguest.fileimport.FileImportGeneratorMain `
  "--seed=$Seed" "--guests=$Guests" "--rounds=$Rounds" "--base-date=$BaseDate" "--out-dir=$OutDir"
