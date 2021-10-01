#!/bin/bash

CONFIG=$1

PROJECT_VERSION=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)

if [ ! -f $CONFIG ]; then
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  echo "  ERROR: The configuration you've specified ($1) does not exist"
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  exit 1
fi


TOMCAT_VERSION=8.5.71

BASE=$(pwd)
SCRIPT_BASE=$(dirname $0)

# clean up previous
rm -f $BASE/target/eb-application.zip
#
# build project
#
mvn clean verify && mvn -Pdist clean package

if [ "$?" != "0" ]; then
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  echo "  ERROR: The project didn't build properly, refusing to create EB application ZIP"
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  exit 1
fi


#
# create target
#
mkdir -p $SCRIPT_BASE/target
mkdir -p $SCRIPT_BASE/target/bin


#
# copy distribution
#
cp target/*-distribution.tar.gz $SCRIPT_BASE/target/dist.tar.gz
cp $SCRIPT_BASE/Procfile $SCRIPT_BASE/target
cp $SCRIPT_BASE/platform $SCRIPT_BASE/target/.platform -R
cd $SCRIPT_BASE/target

TOMCAT_FILE=apache-tomcat-$TOMCAT_VERSION.tar.gz

# did we already download this tomcat? copy it back from /tmp otherwise download it
if [ -f "/tmp/$TOMCAT_FILE" ]; then
  cp /tmp/$TOMCAT_FILE .
else
  TOMCAT_URL="https://downloads.apache.org/tomcat/tomcat-8/v$TOMCAT_VERSION/bin/$TOMCAT_FILE"
  wget $TOMCAT_URL

  if [ "$?" != "0" ]; then
    echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
    echo "  Could not download Tomcat $TOMCAT_VERSION from $TOMCAT_URL"
    echo "  Perhaps this version no longer exists."
    echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
    exit 1
  fi

  cp $TOMCAT_FILE /tmp
fi

tar -xvzf $TOMCAT_FILE --strip 1
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
if [ "$?" != "0" ]; then
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  echo "  Could merge $CONFIG into $SCRIPT_BASE/templates/context.xml.tpl, check your config "
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  exit 1
fi

node $SCRIPT_BASE/merge.js $CONFIG $SCRIPT_BASE/templates/repository.xml.tpl > $SCRIPT_BASE/target/conf/repository.xml
if [ "$?" != "0" ]; then
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  echo "  Could merge $CONFIG into $SCRIPT_BASE/templates/repository.xml.tpl, check your config "
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  exit 1
fi

node $SCRIPT_BASE/merge.js $CONFIG $SCRIPT_BASE/templates/catalina.properties.tpl > $SCRIPT_BASE/target/conf/catalina.properties
if [ "$?" != "0" ]; then
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  echo "  Could merge $CONFIG into $SCRIPT_BASE/templates/catalina.properties.tpl, check your config "
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  exit 1
fi

node $SCRIPT_BASE/merge.js $CONFIG $SCRIPT_BASE/templates/server.xml.tpl > $SCRIPT_BASE/target/conf/server.xml
if [ "$?" != "0" ]; then
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  echo "  Could merge $CONFIG into $SCRIPT_BASE/templates/server.xml.tpl, check your config "
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  exit 1
fi

node $SCRIPT_BASE/merge.js $CONFIG $SCRIPT_BASE/templates/setenv.sh.tpl > $SCRIPT_BASE/target/bin/setenv.sh
if [ "$?" != "0" ]; then
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  echo "  Could merge $CONFIG into $SCRIPT_BASE/templates/setenv.sh.tpl, check your config "
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  exit 1
fi

# custom script that removes going to background, healthier when used with Procfile.
cp $SCRIPT_BASE/templates/catalina.sh.tpl $SCRIPT_BASE/target/bin/catalina.sh

cd $SCRIPT_BASE/target 
zip $BASE/target/eb-application-$PROJECT_VERSION.zip . -r
cd $BASE

rm $SCRIPT_BASE/target -Rf
