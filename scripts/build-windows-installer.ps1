# build-windows-installer.ps1
$ErrorActionPreference = "Stop"

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " Building Lab Control Student Agent Windows Package       " -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan

# 1. Clean & Build Fat JAR
Write-Host "[1/2] Compiling fat JAR with Maven..." -ForegroundColor Yellow
mvn clean package
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven build failed!"
    exit 1
}

# 2. Output destination directory
$distDir = "dist\windows"
if (Test-Path $distDir) {
    Remove-Item -Recurse -Force $distDir
}
New-Item -ItemType Directory -Force -Path $distDir | Out-Null

# 3. Create bundled runtime app-image using jpackage
Write-Host "[2/2] Running jpackage to bundle Java runtime & application..." -ForegroundColor Yellow

& jpackage --name "LabControlStudentAgent" `
           --app-version "1.0.0" `
           --input "target" `
           --main-jar "LabControlStudentAgent-1.0.0.jar" `
           --main-class "com.labcontrol.Main" `
           --type app-image `
           --dest $distDir `
           --win-console

if ($LASTEXITCODE -eq 0) {
    Write-Host "SUCCESS: Bundled runtime application package generated at:" -ForegroundColor Green
    Write-Host "         $(Resolve-Path "$distDir\LabControlStudentAgent")" -ForegroundColor Green
} else {
    Write-Error "jpackage failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}

# 4. Write .bat launcher scripts inside distDir
$installBatContent = @"
@echo off
title Installing Lab Control Student Agent on PC-1
echo =========================================================
echo  Installing Lab Control Student Agent on Windows PC-1    
echo =========================================================

set SCRIPT_PATH=%~dp0scripts\install-windows-service.ps1
if not exist "%SCRIPT_PATH%" set SCRIPT_PATH=%~dp0..\..\scripts\install-windows-service.ps1

powershell -ExecutionPolicy Bypass -File "%SCRIPT_PATH%"
if %ERRORLEVEL% EQU 0 (
    echo.
    echo =========================================================
    echo  SUCCESS: Lab Control Student Agent Installed!           
    echo  The application is now running in the background.       
    echo =========================================================
) else (
    echo.
    echo Installation failed with exit code %ERRORLEVEL%
)
pause
"@

$uninstallBatContent = @"
@echo off
title Uninstalling Lab Control Student Agent from PC-1
echo =========================================================
echo  Uninstalling Lab Control Student Agent                  
echo =========================================================

set SCRIPT_PATH=%~dp0scripts\uninstall-windows.ps1
if not exist "%SCRIPT_PATH%" set SCRIPT_PATH=%~dp0..\..\scripts\uninstall-windows.ps1

powershell -ExecutionPolicy Bypass -File "%SCRIPT_PATH%"
if %ERRORLEVEL% EQU 0 (
    echo.
    echo =========================================================
    echo  SUCCESS: Lab Control Student Agent Uninstalled.         
    echo =========================================================
) else (
    echo.
    echo Uninstallation failed with exit code %ERRORLEVEL%
)
pause
"@

Set-Content -Path "$distDir\Install-LabControlStudentAgent.bat" -Value $installBatContent -Encoding UTF8
Set-Content -Path "$distDir\Uninstall-LabControlStudentAgent.bat" -Value $uninstallBatContent -Encoding UTF8

# 5. Create ZIP package for GitHub download & Lab PC deployment
Write-Host "[3/3] Creating standalone ZIP package: LabControlStudentAgent-Windows-1.0.0.zip..." -ForegroundColor Yellow

$zipPath = "$distDir\LabControlStudentAgent-Windows-1.0.0.zip"
if (Test-Path $zipPath) {
    Remove-Item -Force $zipPath
}

# Temporary zip staging folder
$stagingDir = "$distDir\staging"
if (Test-Path $stagingDir) {
    Remove-Item -Recurse -Force $stagingDir
}
New-Item -ItemType Directory -Force -Path $stagingDir | Out-Null

Copy-Item -Path "$distDir\LabControlStudentAgent" -Destination $stagingDir -Recurse -Force
Copy-Item -Path "$distDir\Install-LabControlStudentAgent.bat" -Destination $stagingDir -Force
Copy-Item -Path "$distDir\Uninstall-LabControlStudentAgent.bat" -Destination $stagingDir -Force
Copy-Item -Path "scripts" -Destination $stagingDir -Recurse -Force

Compress-Archive -Path "$stagingDir\*" -DestinationPath $zipPath -Force
Remove-Item -Recurse -Force $stagingDir

if (Test-Path $zipPath) {
    $zipSize = (Get-Item $zipPath).Length / 1MB
    Write-Host "SUCCESS: Final ZIP package generated at:" -ForegroundColor Green
    Write-Host "         $(Resolve-Path $zipPath) ($([math]::Round($zipSize, 2)) MB)" -ForegroundColor Green
}

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " Build & Packaging Finished Successfully!                " -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan
