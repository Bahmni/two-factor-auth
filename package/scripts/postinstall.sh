#!/bin/bash

if [ -f /etc/bahmni-installer/bahmni.conf ]; then
. /etc/bahmni-installer/bahmni.conf
fi

#create bahmni user and group if doesn't exist
USERID=bahmni
GROUPID=bahmni
/bin/id -g $GROUPID 2>/dev/null
[ $? -eq 1 ]
groupadd bahmni

/bin/id $USERID 2>/dev/null
[ $? -eq 1 ]
useradd -g bahmni bahmni

chkconfig --add bahmni-two-factor-auth

#copy web.xml
mkdir -p /opt/bahmni-two-factor-auth/etc/openmrs/
mkdir -p /opt/bahmni-two-factor-auth/run/
mkdir -p /opt/bahmni-two-factor-auth/log/
mv -f /opt/openmrs/openmrs/WEB-INF/web.xml /opt/bahmni-two-factor-auth/etc/openmrs/
cp -f /opt/bahmni-two-factor-auth/etc/externalauth.jar /opt/openmrs/openmrs/WEB-INF/lib/
cp -f /opt/bahmni-two-factor-auth/etc/web.xml /opt/openmrs/openmrs/WEB-INF/
chown -R bahmni:bahmni /opt/openmrs/openmrs/WEB-INF/web.xml
chown -R bahmni:bahmni /opt/openmrs/openmrs/WEB-INF/lib/externalauth.jar


link_directories(){
    #create links
    ln -s /opt/bahmni-two-factor-auth/etc /etc/bahmni-two-factor-auth
    ln -s /opt/bahmni-two-factor-auth/bin/bahmni-two-factor-auth /etc/init.d/bahmni-two-factor-auth
    ln -s /opt/bahmni-two-factor-auth/run /var/run/bahmni-two-factor-auth
    ln -s /opt/bahmni-two-factor-auth/log /var/log/bahmni-two-factor-auth
}

manage_permissions(){
    # permissions
    chown -R bahmni:bahmni /opt/bahmni-two-factor-auth
    chown -R bahmni:bahmni /var/log/bahmni-two-factor-auth
    chown -R bahmni:bahmni /var/run/bahmni-two-factor-auth
    chown -R bahmni:bahmni /etc/init.d/bahmni-two-factor-auth
    chown -R bahmni:bahmni /etc/bahmni-two-factor-auth
}

link_directories
manage_permissions

