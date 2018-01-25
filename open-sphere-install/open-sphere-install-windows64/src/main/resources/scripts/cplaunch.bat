@echo off

setlocal

set ICON_PATH=%~dp0icon.ico
set LAUNCH_SCRIPT_PATH=%~dp0launch.bat
set CONFIG_PROPERTIES_PATH=%~dp0config.properties

if exist "%LAUNCH_SCRIPT_PATH%" (
    xcopy "%LAUNCH_SCRIPT_PATH%" "%~dp0.." /y
)

if exist "%ICON_PATH%" (
	xcopy "%ICON_PATH%" "%~dp0.." /y
)

REM Always copy config.properties during install, overwriting what's already there.
xcopy "%CONFIG_PROPERTIES_PATH%" "%~dp0.." /y

