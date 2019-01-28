#!/bin/bash

set -x

RPMS_HORIZON=("opennms-core-*.rpm"
              "opennms-webapp-jetty-*.rpm"
              "opennms-plugin-protocol-cifs-*.rpm"
              "opennms-webapp-hawtio-*.rpm")

RPMS_MINION=("opennms-minion-*.rpm")

RPMS_SENTINEL=("opennms-sentinel-*.rpm")

for RPM in ${RPMS_HORIZON[*]}; do
    cp ../target/rpm/RPMS/noarch/${RPM} horizon/rpms
done

for RPM in ${RPMS_MINION[*]}; do
    cp ../target/rpm/RPMS/noarch/${RPM} minion/rpms
done

for RPM in ${RPMS_SENTINEL[*]}; do
    cp ../target/rpm/RPMS/noarch/${RPM} sentinel/rpms
done

