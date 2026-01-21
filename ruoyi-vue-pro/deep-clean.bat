@echo off
chcp 65001 >nul 2>&1
title Changhui IBMS - Deep Clean and Rebuild

echo.
echo ================================================================================
echo   Changhui IBMS Project - Deep Clean and Rebuild
echo   Fixes NoClassDefFoundError and class loading issues
echo ================================================================================
echo.
echo   This script will:
echo     1. Kill Java processes that may lock files
echo     2. Delete all target directories
echo     3. Clean .class file residues
echo     4. Clean Maven local repository cache
echo     5. Clean IDEA compilation cache
echo     6. Execute Maven full rebuild
echo.
echo ================================================================================
echo.

set /p confirm="Confirm deep clean? (Y/N): "
if /i not "%confirm%"=="Y" (
    echo Cancelled
    pause
    exit /b 0
)

echo.
echo Starting PowerShell script...
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0deep-clean.ps1"

if errorlevel 1 (
    echo.
    echo [ERROR] Script execution failed
    echo.
) else (
    echo.
    echo [DONE] Script execution completed
    echo.
)

pause
