# uninstall-windows.ps1
# Requires Administrator Rights

$ErrorActionPreference = "Stop"

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " Uninstalling Lab Control Student Agent                  " -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan

# 1. Stop background task and process
Write-Host "[1/3] Stopping background task and processes..." -ForegroundColor Yellow
try {
    schtasks /End /TN "LabControlStudentAgent" 2>$null
    schtasks /Delete /TN "LabControlStudentAgent" /F 2>$null
} catch {}

try {
    Stop-Process -Name "LabControlStudentAgent" -Force -ErrorAction SilentlyContinue
} catch {}

# 2. Remove Startup shortcut if created
Write-Host "[2/3] Cleaning up auto-start configurations..." -ForegroundColor Yellow
$startupVbs = "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\Startup\LabControlStudentAgent.vbs"
if (Test-Path $startupVbs) {
    Remove-Item -Force $startupVbs -ErrorAction SilentlyContinue
}

# 3. Remove Program Files and LocalAppData directories
Write-Host "[3/3] Removing application files..." -ForegroundColor Yellow
$dirs = @("C:\Program Files\LabControlStudentAgent", "$env:LOCALAPPDATA\LabControlStudentAgent")
foreach ($dir in $dirs) {
    if (Test-Path $dir) {
        Remove-Item -Recurse -Force $dir -ErrorAction SilentlyContinue
    }
}

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " Uninstallation Completed Cleanly.                       " -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan
