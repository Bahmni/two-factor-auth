#!/bin/bash

#stop the server
service openmrs stop || true
service bahmni-two-factor-auth stop || true

#Copying the original web.xml before uninstalling
cp -f /opt/bahmni-two-factor-auth/etc/openmrs/web.xml /opt/openmrs/openmrs/WEB-INF/
rm -rf /opt/openmrs/openmrs/WEB-INF/lib/externalauth.jar



