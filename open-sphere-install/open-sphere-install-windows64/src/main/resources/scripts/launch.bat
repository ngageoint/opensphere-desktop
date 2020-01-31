@echo off

setlocal

set CONFIG_PROPERTIES=config.properties

if exist "%~dp0%CONFIG_PROPERTIES%" (
    for /f "tokens=1* delims==" %%A in (%CONFIG_PROPERTIES%) do (
        if "%%A"=="preferred.version" set PREFERRED_VERSION=%%B
    )
) else (
    for /f " tokens=*" %%d in ('dir /b /ad-h /o-d ^| findstr /v "vortex"') do (
        set PREFERRED_VERSION=%%d
        goto :createfile
    )
    :createfile
    (
        echo #config settings
        echo #%date% %time%
        echo newest.version=%PREFERRED_VERSION%
        echo preferred.version=%PREFERRED_VERSION%
    ) >> %CONFIG_PROPERTIES%
)

echo Preferred version: %PREFERRED_VERSION%

set VERSION_RUN_PATH="%~dp0%PREFERRED_VERSION%""

echo Preferred version run path: %VERSION_RUN_PATH%

pushd "%VERSION_RUN_PATH%"
start /min run.bat
