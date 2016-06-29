#!/bin/sh
nohup $JAVA_HOME/bin/java -Dloader.path=/home/bahmni/.bahmni-security/ -jar $SERVER_OPTS $DEBUG_OPTS /opt/bahmni-two-factor-auth/lib/bahmni-two-factor-auth.jar >> /var/log/bahmni-two-factor-auth/bahmni-two-factor-auth.log 2>&1 &
echo $! > /var/run/bahmni-two-factor-auth/bahmni-two-factor-auth.pid


