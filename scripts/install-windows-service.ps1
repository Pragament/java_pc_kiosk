# install-windows-service.ps1
# Requires Administrator Rights

$ErrorActionPreference = "Stop"

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " Installing Lab Control Student Agent on Windows PC      " -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan

$installDir = "C:\Program Files\LabControlStudentAgent"
$sourceDir = "dist\windows\LabControlStudentAgent"

if (-not (Test-Path $sourceDir)) {
    Write-Error "Source package directory not found ($sourceDir). Run scripts\build-windows-installer.ps1 first."
    exit 1
}

# Check if Program Files is writable, else fallback to LocalAppData
try {
    $testPath = Join-Path $installDir ".test_write"
    New-Item -ItemType Directory -Force -Path $installDir -ErrorAction Stop | Out-Null
    New-Item -ItemType File -Path $testPath -Force -ErrorAction Stop | Out-Null
    Remove-Item -Force $testPath -ErrorAction SilentlyContinue
    Write-Host "Target location: $installDir (System Administrative Mode)" -ForegroundColor Green
} catch {
    $installDir = "$env:LOCALAPPDATA\LabControlStudentAgent"
    Write-Host "Target location: $installDir (User Local Application Mode)" -ForegroundColor Yellow
}

if (Test-Path $installDir) {
    Remove-Item -Recurse -Force $installDir -ErrorAction SilentlyContinue
}

$parentDir = Split-Path -Path $installDir -Parent
if (-not (Test-Path $parentDir)) {
    New-Item -ItemType Directory -Force -Path $parentDir | Out-Null
}

Copy-Item -Path $sourceDir -Destination $parentDir -Recurse -Force

# 3. Create default LOCKED policy.json in install directory
$policyFile = Join-Path $installDir "policy.json"
if (-not (Test-Path $policyFile)) {
    Write-Host "[3/4] Writing default LOCKED policy (blocking msedge.exe)..." -ForegroundColor Yellow
    $defaultPolicy = @{
        mode = "LOCKED"
        blockedApps = @("msedge.exe")
        blockedSites = @()
    } | ConvertTo-Json -Depth 3
    Set-Content -Path $policyFile -Value $defaultPolicy -Encoding UTF8
}

# 4. Create Windows Startup Task in Task Scheduler
Write-Host "[4/4] Configuring automatic Windows startup background task..." -ForegroundColor Yellow
$exePath = Join-Path $installDir "LabControlStudentAgent.exe"

# Create task running at system logon/boot in background
schtasks /Create /TN "LabControlStudentAgent" /TR "`"$exePath`"" /SC ONLOGON /RL HIGHEST /F

if ($LASTEXITCODE -eq 0) {
    Write-Host "Auto-start task successfully registered." -ForegroundColor Green
} else {
    Write-Warning "schtasks returned non-zero code. Falling back to Windows Startup folder auto-start..."
    $startupFolder = "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\Startup"
    $vbsPath = Join-Path $startupFolder "LabControlStudentAgent.vbs"
    $vbsContent = "Set WshShell = CreateObject(`"WScript.Shell`")`nWshShell.Run `"`"$exePath`"`", 0, False"
    Set-Content -Path $vbsPath -Value $vbsContent -Encoding UTF8
    Write-Host "Created silent startup script in Windows Startup folder: $vbsPath" -ForegroundColor Green
}

# 5. Start task immediately
Write-Host "Starting Student Agent background service..." -ForegroundColor Yellow
try {
    schtasks /Run /TN "LabControlStudentAgent" 2>$null
} catch {}

# Fallback direct start if schtasks run fails
if (-not (Get-Process "LabControlStudentAgent" -ErrorAction SilentlyContinue)) {
    Start-Process -FilePath $exePath -WorkingDirectory $installDir -WindowStyle Hidden
}

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " Installation Completed Successfully!                     " -ForegroundColor Cyan
Write-Host " Student Agent is now running in the background.         " -ForegroundColor Cyan
Write-Host " Microsoft Edge (msedge.exe) is blocked automatically.   " -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan
