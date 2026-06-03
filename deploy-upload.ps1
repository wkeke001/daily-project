param(
    [string]$ProjectPath = "D:\project\daily-project",
    [string]$ServerUser = "ubuntu",
    [string]$ServerHost = "175.178.44.121",
    [string]$RemoteDir = "/home/www/lingke-todo/app",
    [string]$JarName = "lingke-todo.jar",
    [string]$Profile = "prod"
)

$Server = "$ServerUser@$ServerHost"

Write-Host "====================================="
Write-Host "Lingke Todo Deploy Script"
Write-Host "====================================="
Write-Host "Project: $ProjectPath"
Write-Host "Server: $Server"
Write-Host "Remote: $RemoteDir"
Write-Host "Jar: $JarName"
Write-Host "Profile: $Profile"
Write-Host "====================================="

if (!(Test-Path $ProjectPath)) {
    Write-Host "Project path not found: $ProjectPath" -ForegroundColor Red
    exit 1
}

Set-Location $ProjectPath

if (!(Test-Path "$ProjectPath\pom.xml")) {
    Write-Host "pom.xml not found" -ForegroundColor Red
    exit 1
}

Write-Host "`n[1/6] Maven package (profile: $Profile)..." -ForegroundColor Cyan

mvn clean package --define skipTests --define "spring.profiles.active=$Profile"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build failed" -ForegroundColor Red
    exit 1
}

Write-Host "`n[2/6] Finding jar..." -ForegroundColor Cyan

$JarFile = Get-ChildItem -Path "$ProjectPath\target" -Filter "*.jar" |
    Where-Object {
        $_.Name -notlike "*sources*" `
        -and $_.Name -notlike "*javadoc*" `
        -and $_.Name -notlike "*.original"
    } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if ($null -eq $JarFile) {
    Write-Host "No jar found in target" -ForegroundColor Red
    exit 1
}

Write-Host "Found: $($JarFile.FullName)" -ForegroundColor Green

Write-Host "`n[3/6] Creating remote dir & stopping service..." -ForegroundColor Cyan

ssh $Server "mkdir -p $RemoteDir && sudo systemctl stop lingke-todo 2>/dev/null; sleep 2"

if ($LASTEXITCODE -ne 0) {
    Write-Host "SSH connection failed" -ForegroundColor Red
    exit 1
}

Write-Host "`n[4/6] Uploading jar & starting service..." -ForegroundColor Cyan

scp $JarFile.FullName "${Server}:$RemoteDir/$JarName"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Upload failed" -ForegroundColor Red
    exit 1
}

ssh $Server "sudo systemctl start lingke-todo"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Start failed, check manually" -ForegroundColor Yellow
} else {
    Write-Host "Service started" -ForegroundColor Green
}

Write-Host "`n====================================="
Write-Host "Deploy done!" -ForegroundColor Green
Write-Host "Remote: $RemoteDir/$JarName"
Write-Host "Profile: $Profile"
Write-Host "URL: https://lingling.fun/todo/"
Write-Host "====================================="
