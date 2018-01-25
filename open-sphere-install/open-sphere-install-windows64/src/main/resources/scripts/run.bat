@echo off

setlocal

set OPENSPHERE_JAVA_EXE=java.exe

if not defined OPENSPHERE_JAVA (
    if exist jre\bin\java.exe (
        set OPENSPHERE_JAVA=jre\bin\%OPENSPHERE_JAVA_EXE%
    ) else if exist "%JAVA_HOME%"\bin\%OPENSPHERE_JAVA_EXE% (
        set OPENSPHERE_JAVA="%JAVA_HOME%\bin\%OPENSPHERE_JAVA_EXE%"
    ) else if exist "%JAVA_HOME%"\jre\bin\%OPENSPHERE_JAVA_EXE% (
        set OPENSPHERE_JAVA="%JAVA_HOME%\jre\bin\%OPENSPHERE_JAVA_EXE%"
    ) else (
        set OPENSPHERE_JAVA=%OPENSPHERE_JAVA_EXE%
    )
)

echo Java set to %OPENSPHERE_JAVA%

set PATH=%PATH%;lib/win32/%OPENSPHERE_ARCH%

if not defined OPENSPHERE_PATH_RUNTIME set OPENSPHERE_PATH_RUNTIME=replaceme
if "%OPENSPHERE_PATH_RUNTIME%"=="replaceme" (
    set OPENSPHERE_PATH_RUNTIME=%USERPROFILE%
)
if "%OPENSPHERE_PATH_RUNTIME%"=="%USERPROFILE%" (
    set OPENSPHERE_PATH_RUNTIME_ARG="-Dopensphere.path.runtime=%OPENSPHERE_PATH_RUNTIME%\opensphere\vortex"
    echo OPENSPHERE_PATH_RUNTIME set to "%OPENSPHERE_PATH_RUNTIME%\opensphere\vortex"
) else (
    set OPENSPHERE_PATH_RUNTIME_ARG="-Dopensphere.path.runtime=%OPENSPHERE_PATH_RUNTIME%\opensphere_%USERNAME%\opensphere\vortex"
    echo OPENSPHERE_PATH_RUNTIME set to "%OPENSPHERE_PATH_RUNTIME%\opensphere_%USERNAME%\opensphere\vortex"
)


if not defined OPENSPHERE_DB_PATH set OPENSPHERE_DB_PATH=replaceme
if "%OPENSPHERE_DB_PATH%"=="replaceme" (
    set OPENSPHERE_DB_PATH=%OPENSPHERE_PATH_RUNTIME%
)
if "%OPENSPHERE_DB_PATH%"=="%USERPROFILE%" (
    set OPENSPHERE_DB_PATH_ARG="-Dopensphere.db.path=%OPENSPHERE_DB_PATH%\opensphere\vortex\db"
    echo OPENSPHERE_DB_PATH set to "%OPENSPHERE_DB_PATH%\opensphere\vortex\db"
) else (
    set OPENSPHERE_DB_PATH_ARG="-Dopensphere.db.path=%OPENSPHERE_DB_PATH%\opensphere_%USERNAME%\opensphere\vortex\db"
    echo OPENSPHERE_DB_PATH set to "%OPENSPHERE_DB_PATH%\opensphere_%USERNAME%\opensphere\vortex\db"
)

echo Running OpenSphere...
echo.

if not defined OPENSPHERE_MAX_MEM (
    set OPENSPHERE_MAX_MEM=replaceme
)

if "%OPENSPHERE_MAX_MEM%"=="replaceme" (
    set OPENSPHERE_MAX_MEM=
)

if not defined OPENSPHERE_MAX_MEM (
    for /f %%i in ('wmic computersystem get TotalPhysicalMemory ^| findstr "[0-9]"') do set TOTAL_PHYSICAL_MEMORY=%%i
)
if defined OPENSPHERE_MAX_MEM (
    set MAX_MEM_ARG=-Dopensphere.launch.maxMemTestBytes=%OPENSPHERE_MAX_MEM%
) else if defined TOTAL_PHYSICAL_MEMORY (
    set MAX_MEM_ARG=-Dopensphere.launch.totalPhysicalMemoryBytes=%TOTAL_PHYSICAL_MEMORY%
)

if not defined OPENSPHERE_CACHE_SIZE_HINT (
    set OPENSPHERE_CACHE_SIZE_HINT=replaceme
)
if "%OPENSPHERE_CACHE_SIZE_HINT%"=="replaceme" (
    set OPENSPHERE_CACHE_SIZE_HINT_ARG=
) else (
    set OPENSPHERE_CACHE_SIZE_HINT_ARG=-Dopensphere.db.defaultSizeHintMB=%OPENSPHERE_CACHE_SIZE_HINT%
)

if not defined OPENSPHERE_CUSTOM_JVM_ARGS (
    set OPENSPHERE_CUSTOM_JVM_ARGS=
)

set OPENSPHERE_JVM_ARGS=%OPENSPHERE_PATH_RUNTIME_ARG% %OPENSPHERE_DB_PATH_ARG% %OPENSPHERE_CACHE_SIZE_HINT_ARG% "-XX:ErrorFile=%OPENSPHERE_PATH_RUNTIME%\opensphere\vortex\logs\hs_err_pid%%p.log" -XX:+UseMembar -XX:+AggressiveOpts -XX:+UseG1GC -XX:G1ReservePercent=40 -Djava.net.preferIPv4Stack=true %OPENSPHERE_CUSTOM_JVM_ARGS%

%OPENSPHERE_JAVA% %MAX_MEM_ARG% %OPENSPHERE_PATH_RUNTIME_ARG% %OPENSPHERE_DB_PATH_ARG% -cp override.jar;log4j-1.2.17.jar;core-${project.version}.jar;open-sphere-dark-laf-${project.version}.jar -splash:splash.png io.opensphere.core.launch.Launch
