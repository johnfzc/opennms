#!/bin/sh -e

BAMBOO_HOST="https://bamboo.opennms.org"

# We need a url
if [ $# -ne 1 ]; then
    echo "Usage: $0 <url>"
    exit 1
fi

# PARSE URL
URL=$1
BUILD=$(echo $URL | awk -F'/' '{ print $NF }')
PLAN_KEY=$(echo $BUILD | awk -F'-' '{ print $(NF-2) "-" $(NF-1) }')
BUILD_ID=$(echo $BUILD | awk -F'-' '{ print $NF }')

# Figure out RPM_VERSION
RPM_VERSION=$(curl -s ${BAMBOO_HOST}/artifact/$PLAN_KEY/shared/build-$BUILD_ID/RPMs/ | grep -i opennms-core | sed -E 's/(.*>)(opennms-core-)(.*)\.noarch.rpm<\/a>.*/\3/g')

RPMS_HORIZON=("opennms-${RPM_VERSION}.noarch.rpm"
              "opennms-core-${RPM_VERSION}.noarch.rpm"
              "opennms-webapp-jetty-${RPM_VERSION}.noarch.rpm")
RPMS_MINION=("opennms-minion-${RPM_VERSION}.noarch.rpm"
             "opennms-minion-container-${RPM_VERSION}.noarch.rpm"
             "opennms-minion-features-core-${RPM_VERSION}.noarch.rpm"
             "opennms-minion-features-default-${RPM_VERSION}.noarch.rpm")

RPMS_SENTINEL=("opennms-sentinel-${RPM_VERSION}.noarch.rpm")

# Start Downloading

echo "Downloading rpms to build docker images"
echo "BUILD: ${BUILD}"
echo "PLAN_KEY: ${PLAN_KEY}"
echo "BUILD_ID: ${BUILD_ID}"
echo "RPM_VERSION: ${RPM_VERSION}"
echo "RPMS: ${RPMS[*]}"

# ensure everything is initialized
if [ -z "${BUILD}" -o -z "${PLAN_KEY}" -o -z "${BUILD_ID}" -o -z "${RPM_VERSION}" ]; then
    echo "Something went wrong, not initialized correctly. Bailing.."
    exit 2
fi

for RPM in ${RPMS_HORIZON[*]}; do
    echo "RPM: ${RPM}"
    wget --no-clobber "${BAMBOO_HOST}/artifact/${PLAN_KEY}/shared/build-${BUILD_ID}/RPMs/${RPM}" -P ./horizon/rpms
done

for RPM in ${RPMS_MINION[*]}; do
    echo "RPM: ${RPM}"
    wget --no-clobber "${BAMBOO_HOST}/artifact/${PLAN_KEY}/shared/build-${BUILD_ID}/RPMs/${RPM}" -P ./minion/rpms
done

for RPM in ${RPMS_SENTINEL[*]}; do
    echo "RPM: ${RPM}"
    wget --no-clobber "${BAMBOO_HOST}/artifact/${PLAN_KEY}/shared/build-${BUILD_ID}/RPMs/${RPM}" -P ./sentinel/rpms
done