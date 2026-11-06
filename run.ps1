$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

Write-Host "===============================================================" -ForegroundColor Cyan
Write-Host "  Spiral Neural Network Interface Engine Using Java" -ForegroundColor Yellow
Write-Host "  JIIT Noida - B.Tech CSE (OOPS Java Lab - 24B15CS215)" -ForegroundColor Green
Write-Host "===============================================================" -ForegroundColor Cyan

$java = "java"
$javac = "javac"

if (-not (Get-Command "java" -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Program Files\Android\Android Studio\jbr\bin\java.exe") {
        $java = "C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
        $javac = "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
    } else {
        Write-Error "Java not found. Please install JDK 17+ or add it to PATH."
        exit 1
    }
}

if (-not (Test-Path "bin\Main.class")) {
    Write-Host "Compiling project..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path "bin" | Out-Null
    $files = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
    & $javac -d bin -sourcepath src $files
    Write-Host "Compilation complete." -ForegroundColor Green
}

Write-Host "Launching Spiral Neural Network Engine UI..." -ForegroundColor Cyan
& $java -cp bin Main
