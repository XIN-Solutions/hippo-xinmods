#!/bin/bash

# installing java (might already be there depending on the AMI)
#yum -y install java-11-amazon-corretto-headless

# make space for repository storage
mkdir /var/lib/hippostorage -p
chown webapp.webapp /var/lib/hippostorage -R

# shutdown the old onw if it's there
su -c "cd /var/app/current && ./bin/shutdown.sh" webapp
sleep 20

# REALLY DO SOME DAMAGE 
PID=$(ps ax | grep tomcat | head -n1 | cut -d' ' -f1)
kill -9 $PID

# Restart tomcat
su -c "cd /var/app/current && ./bin/startup.sh" webapp
