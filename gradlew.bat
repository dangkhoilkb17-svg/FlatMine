@echo off
set APP_HOME=%~dp0
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (gradle %* & exit /b %ERRORLEVEL%)
set BOOT=%APP_HOME%.gradle-bootstrap
set DIST=%BOOT%\gradle-8.10.2
if not exist "%DIST%\bin\gradle.bat" (
  if not exist "%BOOT%" mkdir "%BOOT%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-8.10.2-bin.zip' -OutFile '%BOOT%\gradle.zip'; Expand-Archive -Force '%BOOT%\gradle.zip' '%BOOT%'; Remove-Item '%BOOT%\gradle.zip'"
)
call "%DIST%\bin\gradle.bat" %*
