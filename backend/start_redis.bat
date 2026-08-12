@echo off


docker-compose up -d
if %errorlevel% neq 0 (
    echo error!
    pause
    exit /b %errorlevel%
)

pause