#!/bin/bash

CONFIG=$1
DEPLOY=$2

TOMCAT_VERSION=8.5.58

BASE=$(pwd)
SCRIPT_BASE=$(dirname $0)

#
# build project
# 
mvn clean verify
mvn -Pdist clean package

rm -f $BASE/target/eb-application.zip

#
# create target
# 
mkdir -p $SCRIPT_BASE/target
mkdir -p $SCRIPT_BASE/target/bin


#
# copy distribution
# 
cp target/*-distribution.tar.gz $SCRIPT_BASE/target/dist.tar.gz
cp $SCRIPT_BASE/eternal.jar $SCRIPT_BASE/target
cp $SCRIPT_BASE/Procfile $SCRIPT_BASE/target
cp $SCRIPT_BASE/platform $SCRIPT_BASE/target/.platform -R
cd $SCRIPT_BASE/target

wget "https://downloads.apache.org/tomcat/tomcat-8/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz"
tar -xvzf apache-tomcat*.tar.gz --strip 1
rm apache-tomcat*
rm webapps -Rf

#
# unzip distribution
# 
tar -xvzf dist.tar.gz
rm dist.tar.gz

cd $BASE

#
# copy mysql connector
# 
cp $SCRIPT_BASE/libs/mysql-connector-*.jar $SCRIPT_BASE/target/common/lib

#
# move webapps 
# 
mv $SCRIPT_BASE/target/webapps/cms.war $SCRIPT_BASE/target
mv $SCRIPT_BASE/target/webapps/site.war $SCRIPT_BASE/target
mv $SCRIPT_BASE/target/site.war $SCRIPT_BASE/target/webapps
mv $SCRIPT_BASE/target/cms.war $SCRIPT_BASE/target/webapps

#
# interpret configurations
# 
node $SCRIPT_BASE/merge.js $CONFIG $SCRIPT_BASE/templates/context.xml.tpl > $SCRIPT_BASE/target/conf/context.xml
node $SCRIPT_BASE/merge.js $CONFIG $SCRIPT_BASE/templates/repository.xml.tpl > $SCRIPT_BASE/target/conf/repository.xml
node $SCRIPT_BASE/merge.js $CONFIG $SCRIPT_BASE/templates/catalina.properties.tpl > $SCRIPT_BASE/target/conf/catalina.properties
node $SCRIPT_BASE/merge.js $CONFIG $SCRIPT_BASE/templates/server.xml.tpl > $SCRIPT_BASE/target/conf/server.xml
node $SCRIPT_BASE/merge.js $CONFIG $SCRIPT_BASE/templates/setenv.sh.tpl > $SCRIPT_BASE/target/bin/setenv.sh

cd $SCRIPT_BASE/target 
zip $BASE/target/eb-application.zip . -r
cd $BASE

rm $SCRIPT_BASE/target -Rf
