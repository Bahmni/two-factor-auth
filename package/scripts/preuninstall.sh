#!/bin/bash

#stop the server
service openmrs stop || true
service bahmni-two-factor-auth stop || true

#Copying the original web.xml before uninstalling
if [ $1 -eq 0 ] ; then
    cp -f /opt/bahmni-two-factor-auth/etc/openmrs/web.xml /opt/openmrs/openmrs/WEB-INF/web.xml
    rm -rf /opt/openmrs/openmrs/WEB-INF/lib/externalauth.jar
fi