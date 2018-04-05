#!/bin/sh -xv

SOURCE_DIR=`dirname "$0"`
TARGET_DIR=`dirname "${SOURCE_DIR}"`
LAUNCH_SCRIPT=launch.sh
CONFIG_PROPERTIES=config.properties

if [ -e "${SOURCE_DIR}/${LAUNCH_SCRIPT}" ]
then
    chmod +x "${SOURCE_DIR}/${LAUNCH_SCRIPT}"
    cp "${SOURCE_DIR}/${LAUNCH_SCRIPT}" "${TARGET_DIR}"
fi

#always copy config.properties during install:
cp "${SOURCE_DIR}/${CONFIG_PROPERTIES}" "${TARGET_DIR}"

