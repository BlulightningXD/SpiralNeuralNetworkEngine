@echo off
setlocal EnableDelayedExpansion

title Spiral Neural Network Interface Engine

cd /d "%~dp0"

echo ===============================================================
echo   Spiral Neural Network Interface Engine Using Java
echo   JIIT Noida - B.Tech CSE (OOPS Using Java Lab - 24B15CS215)
echo ===============================================================
echo.

set "JAVA_CMD="
set "JAVAC_CMD="

REM 1. Check JAVA_HOME environment variable
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
        set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
        set "PATH=%JAVA_HOME%\bin;!PATH!"
    )
)

REM 2. Check user Microsoft JDK installation
if not defined JAVA_CMD (
    for /d %%i in ("%LOCALAPPDATA%\Programs\Microsoft\jdk*") do (
        if exist "%%i\bin\java.exe" (
            set "JAVA_CMD=%%i\bin\java.exe"
            set "JAVAC_CMD=%%i\bin\javac.exe"
            set "PATH=%%i\bin;!PATH!"
        )
    )
)

REM 3. Check system PATH
if not defined JAVA_CMD (
    where java >nul 2>nul
    if !ERRORLEVEL! EQU 0 (
        set "JAVA_CMD=java"
        set "JAVAC_CMD=javac"
    )
)

REM 4. Check Android Studio JBR
if not defined JAVA_CMD (
    if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
        set "JAVA_CMD=C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
        set "JAVAC_CMD=C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
        set "PATH=C:\Program Files\Android\Android Studio\jbr\bin;!PATH!"
    )
)

REM 5. Check common Program Files JDK directories
if not defined JAVA_CMD (
    for /d %%i in ("C:\Program Files\Java\jdk*" "C:\Program Files\Eclipse Adoptium\jdk*" "C:\Program Files\Microsoft\jdk*") do (
        if exist "%%i\bin\java.exe" (
            set "JAVA_CMD=%%i\bin\java.exe"
            set "JAVAC_CMD=%%i\bin\javac.exe"
            set "PATH=%%i\bin;!PATH!"
        )
    )
)

if not defined JAVA_CMD (
    echo [ERROR] No Java Runtime / JDK found on your system!
    echo Please install JDK 17 or higher from https://adoptium.net
    echo.
    pause
    exit /b 1
)

echo Java detected:
"!JAVA_CMD!" -version
echo.

REM Recompile if bin\Main.class is missing
if not exist "bin\Main.class" (
    echo Compiling Java source files targeting Java 17 compatibility...
    if not exist "bin" mkdir bin
    if not defined JAVAC_CMD set "JAVAC_CMD=javac"
    "!JAVAC_CMD!" --release 17 -d bin -sourcepath src src\Main.java src\data\*.java src\neural\*.java src\training\*.java src\visualization\*.java src\ui\*.java
    if !ERRORLEVEL! NEQ 0 (
        echo.
        echo [ERROR] Compilation failed.
        pause
        exit /b 1
    )
    echo Compilation successful!
    echo.
)

echo Starting Spiral Neural Network Interface Engine...
"!JAVA_CMD!" -cp bin Main

if !ERRORLEVEL! NEQ 0 (
    echo.
    echo [NOTICE] If a class version mismatch occurred, recompiling cleanly with your JDK...
    if exist "bin" rmdir /s /q bin
    mkdir bin
    "!JAVAC_CMD!" --release 17 -d bin -sourcepath src src\Main.java src\data\*.java src\neural\*.java src\training\*.java src\visualization\*.java src\ui\*.java
    echo Retrying launch...
    "!JAVA_CMD!" -cp bin Main
    if !ERRORLEVEL! NEQ 0 (
        echo [ERROR] Application exited with code !ERRORLEVEL!.
        pause
    )
)

endlocal
