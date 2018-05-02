#!/bin/sh

cd `dirname $0`

if [ -z "$OPENSPHERE_JAVA" ]
then
    OPENSPHERE_JAVA_EXE=java

    # relative path to our own jre
    if [ -x ./jre/bin/$OPENSPHERE_JAVA_EXE ]
    then
        OPENSPHERE_JAVA=./jre/bin/$OPENSPHERE_JAVA_EXE
    else
        OPENSPHERE_JAVA=$OPENSPHERE_JAVA_EXE
    fi
fi

echo Java set to $OPENSPHERE_JAVA

echo Running OpenSphere...
echo

if [ -z "$OPENSPHERE_PATH_RUNTIME" ]; then OPENSPHERE_PATH_RUNTIME=replaceme; fi
if [ "$OPENSPHERE_PATH_RUNTIME" == "replaceme" ]
then
    OPENSPHERE_PATH_RUNTIME_ARG=-Dopensphere.path.runtime="$HOME"/opensphere/vortex
else
    if [ "$OPENSPHERE_PATH_RUNTIME" != "$HOME" ]; then OPENSPHERE_PATH_RUNTIME="$OPENSPHERE_PATH_RUNTIME/opensphere_$USER"; fi
    OPENSPHERE_PATH_RUNTIME_ARG=-Dopensphere.path.runtime="$OPENSPHERE_PATH_RUNTIME"/opensphere/vortex
    echo OPENSPHERE_PATH_RUNTIME set to $OPENSPHERE_PATH_RUNTIME/opensphere/vortex
fi


if [ -z "$OPENSPHERE_DB_PATH" ]; then OPENSPHERE_DB_PATH=replaceme; fi
if [ "$OPENSPHERE_DB_PATH" != "replaceme" ]
then
    if [ "$OPENSPHERE_DB_PATH" != "$HOME" ]; then OPENSPHERE_DB_PATH="$OPENSPHERE_DB_PATH/opensphere_$USER"; fi
    OPENSPHERE_DB_PATH_ARG=-Dopensphere.db.path="$OPENSPHERE_DB_PATH"/opensphere/vortex/db
    echo OPENSPHERE_DB_PATH set to $OPENSPHERE_DB_PATH/opensphere/vortex/db
fi

if [ -z "$OPENSPHERE_MAX_MEM"]
then
    if [ cat </dev/null -a perl </dev/null -a -f /proc/meminfo ]
    then
        mem=$(cat /proc/meminfo | perl -ne 'if (/MemTotal:\s*(\d+)/) { $b = $1 * 1024; print "$b\n"; }')
        if [ $mem ]
        then
            TOTAL_PHYSICAL_MEMORY=$mem
        fi
    fi
fi
if [ "$OPENSPHERE_MAX_MEM" ]
then
    MAX_MEM_ARG=-Dopensphere.launch.maxMemTestBytes=$OPENSPHERE_MAX_MEM
elif [ "$TOTAL_PHYSICAL_MEMORY" ]
then
    MAX_MEM_ARG=-Dopensphere.launch.totalPhysicalMemoryBytes=$TOTAL_PHYSICAL_MEMORY
fi

if [ -z "$OPENSPHERE_CACHE_SIZE_HINT" ]
then
    set OPENSPHERE_CACHE_SIZE_HINT=replaceme
fi
if [ "$OPENSPHERE_CACHE_SIZE_HINT" != "replaceme" ]
then
    OPENSPHERE_CACHE_SIZE_HINT_ARG=-Dopensphere.db.defaultSizeHintMB=%OPENSPHERE_CACHE_SIZE_HINT%
fi

OPENSPHERE_JVM_ARGS=\""$OPENSPHERE_PATH_RUNTIME_ARG"\"\ \""$OPENSPHERE_DB_PATH_ARG"\"\ $OPENSPHERE_CACHE_SIZE_HINT_ARG\ \""-XX:ErrorFile=$OPENSPHERE_PATH_RUNTIME/opensphere/vortex/logs/hs_err_pid%p.log"\"\ -XX:+UseMembar\ -XX:+AggressiveOpts\ -XX:+UseG1GC\ -XX:G1ReservePercent=40\ -Djava.net.preferIPv4Stack=true\ $OPENSPHERE_CUSTOM_JVM_ARGS \
LD_LIBRARY_PATH="${LD_LIBRARY_PATH}":lib/linux/$OPENSPHERE_ARCH \
"$OPENSPHERE_JAVA" --add-modules java.se,java.se.ee --add-opens java.base/java.lang=java.xml.bind $MAX_MEM_ARG "$OPENSPHERE_PATH_RUNTIME_ARG" -cp override.jar:log4j-1.2.17.jar:core-${project.version}.jar:open-sphere-dark-laf-${project.version}.jar -splash:splash.png io.opensphere.core.launch.Launch
