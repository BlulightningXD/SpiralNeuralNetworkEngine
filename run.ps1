$ErrorActionPreference = "Continue"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

Write-Host "===============================================================" -ForegroundColor Cyan
Write-Host "  Spiral Neural Network Interface Engine Using Java" -ForegroundColor Yellow
Write-Host "  JIIT Noida - B.Tech CSE (OOPS Java Lab - 24B15CS215)" -ForegroundColor Green
Write-Host "===============================================================" -ForegroundColor Cyan
Write-Host ""

$java = $null
$javac = $null

# 1. Check if java is in PATH
if (Get-Command "java" -ErrorAction SilentlyContinue) {
    $java = "java"
    $javac = "javac"
}

# 2. Check Android Studio JBR
if (-not $java) {
    $jbrJava = "C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
    if (Test-Path $jbrJava) {
        $java = $jbrJava
        $javac = "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
        $env:PATH = "C:\Program Files\Android\Android Studio\jbr\bin;" + $env:PATH
    }
}

# 3. Check Program Files Java locations
if (-not $java) {
    $candidates = Get-ChildItem -Path "C:\Program Files\Java", "C:\Program Files\Eclipse Adoptium", "C:\Program Files\Microsoft" -Filter "java.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
    if ($candidates) {
        $java = $candidates[0]
        $javac = Join-Path (Split-Path $java) "javac.exe"
    }
}

if (-not $java) {
    Write-Host "[ERROR] Could not find a Java installation on your PC." -ForegroundColor Red
    Write-Host "Please download and install JDK 17+ from: https://adoptium.net" -ForegroundColor Yellow
    Read-Host "Press Enter to exit..."
    exit 1
}

Write-Host "Java detected: $java" -ForegroundColor Green
& $java -version
Write-Host ""

if (-not (Test-Path "bin\Main.class")) {
    Write-Host "Compiling Java sources into bin directory..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path "bin" | Out-Null
    $files = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
    & $javac -d bin -sourcepath src $files
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
        Read-Host "Press Enter to exit..."
        exit 1
    }
    Write-Host "Compilation successful!" -ForegroundColor Green
}

Write-Host "Launching Spiral Neural Network Interface Engine..." -ForegroundColor Cyan
& $java -cp bin Main

if ($LASTEXITCODE -ne 0) {
    Read-Host "Application stopped. Press Enter to exit..."
}
