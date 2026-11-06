@echo off
setlocal

cd /d "%~dp0"

echo ===============================================================
echo   Spiral Neural Network Interface Engine Using Java
echo   JIIT Noida - B.Tech CSE (OOPS Using Java Lab - 24B15CS215)
echo ===============================================================
echo.

set JAVA_CMD=java
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
        set JAVA_CMD="C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
    ) else (
        echo [ERROR] Java runtime not found in PATH or standard locations.
        echo Please ensure JDK 17+ is installed.
        pause
        exit /b 1
    )
)

set JAVAC_CMD=javac
where javac >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe" (
        set JAVAC_CMD="C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
    )
)

if not exist "bin\Main.class" (
    echo Compiling Java source files into bin directory...
    if not exist "bin" mkdir bin
    %JAVAC_CMD% -d bin -sourcepath src src\Main.java src\data\*.java src\neural\*.java src\training\*.java src\visualization\*.java src\ui\*.java
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Compilation failed.
        pause
        exit /b 1
    )
    echo Compilation successful!
)

echo Starting application...
%JAVA_CMD% -cp bin Main
endlocal
