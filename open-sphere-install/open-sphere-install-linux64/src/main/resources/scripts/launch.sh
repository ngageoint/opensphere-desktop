#!/bin/sh

SOURCE_DIR=`dirname "$0"`
CONFIG_PROPERTIES="${SOURCE_DIR}/config.properties"
if [ -e "${CONFIG_PROPERTIES}" ]
then
    PREFERRED_VERSION=$(grep -i 'preferred.version' "${CONFIG_PROPERTIES}" | cut -f2 -d'=')
else
    PREFERRED_VERSION=$(ls -td */ | grep -v 'vortex' | head -n 1 | cut -d'/' -f1)
    cat << EOF > "${CONFIG_PROPERTIES}"
#config settings
#$(date)
newest.version=${PREFERRED_VERSION}
preferred.version=${PREFERRED_VERSION}

EOF
fi

echo "Preferred version: ${PREFERRED_VERSION}"

VERSION_RUN_PATH="${SOURCE_DIR}/${PREFERRED_VERSION}"

echo "Preferred version run path: ${VERSION_RUN_PATH}"

cd "${VERSION_RUN_PATH}"
./run.sh
