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

# 1. Check JAVA_HOME
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $java = "$env:JAVA_HOME\bin\java.exe"
    $javac = "$env:JAVA_HOME\bin\javac.exe"
    $env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
}

# 2. Check User Microsoft JDK
if (-not $java) {
    $msJdk = Get-ChildItem -Path "$env:LOCALAPPDATA\Programs\Microsoft" -Filter "jdk*" -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
    if ($msJdk -and (Test-Path "$msJdk\bin\java.exe")) {
        $java = "$msJdk\bin\java.exe"
        $javac = "$msJdk\bin\javac.exe"
        $env:PATH = "$msJdk\bin;" + $env:PATH
    }
}

# 3. Check system PATH
if (-not $java -and (Get-Command "java" -ErrorAction SilentlyContinue)) {
    $java = "java"
    $javac = "javac"
}

# 4. Check Android Studio JBR
if (-not $java) {
    $jbrJava = "C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
    if (Test-Path $jbrJava) {
        $java = $jbrJava
        $javac = "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
        $env:PATH = "C:\Program Files\Android\Android Studio\jbr\bin;" + $env:PATH
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
    Write-Host "Compiling Java sources (targeting Java 17 compatibility)..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path "bin" | Out-Null
    $files = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
    & $javac --release 17 -d bin -sourcepath src $files
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
    Write-Host "[NOTICE] Recompiling cleanly with your local JDK..." -ForegroundColor Yellow
    Remove-Item "bin\*" -Recurse -Force
    $files = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
    & $javac --release 17 -d bin -sourcepath src $files
    Write-Host "Retrying launch..." -ForegroundColor Cyan
    & $java -cp bin Main
    if ($LASTEXITCODE -ne 0) {
        Read-Host "Application stopped. Press Enter to exit..."
    }
}
