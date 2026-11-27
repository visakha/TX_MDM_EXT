# Force-sync this repo to origin/main. Discards local edits to tracked files.
# Untracked files (e.g. your own application.properties) are kept.
# Close Excel or any editor holding repo files first.
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
git fetch origin
if ($LASTEXITCODE -ne 0) { exit 1 }
git reset --hard origin/main
if ($LASTEXITCODE -ne 0) { exit 1 }
git log -1 --oneline
