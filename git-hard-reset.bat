@echo off
REM Force-sync this repo to origin/main. Discards local edits to tracked files.
REM Untracked files (e.g. your own application.properties) are kept.
REM Close Excel or any editor holding repo files first.
cd /d "%~dp0"
git fetch origin || exit /b 1
git reset --hard origin/main || exit /b 1
git log -1 --oneline
