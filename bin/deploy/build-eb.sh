#!/bin/bash

CONFIG=$1
DELETE_AFTERWARDS=false

if [[ $CONFIG == "" ]]; then
  echo
  echo "  Usage: $0 [localfile|s3://bucket/key.json]"
  echo
  cat << 'EOF'
    This build script will combine the CMS distribution build with the latest tomcat version, and insert
    configuration information specified in the configuration JSON into the relevant configuration
    files, and move shared libraries into the correct position.

    It will also shape the distribution in such a way that it can be ingested by ElasticBeanstalk.

    Expected JSON configuration elements:

      {
        "eb" : {
          "app" : "<elastic-beanstalk-application-name>",
          "env" : "<eb-environment-name>",
          "region" : "ap-southeast-2"
        },

        "mysql": {
          "host" : "<database host>",
          "port": 3306,
          "database" : "<database name>",
          "username" : "<database user name>",
          "password": "<database user password>"
        },

        "xin" : {
          "cmsHost": "<root url of where CMS is hosted without trailing slash>"
        }

      }

EOF
  exit 1
elif [[ $CONFIG == s3://* ]]; then
  echo "S3 configuration .. fetching from AWS"

  aws s3 cp $CONFIG /tmp/config.json
  CONFIG=/tmp/config.json
  DELETE_AFTERWARDS=true

elif [ ! -f $CONFIG ]; then
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  echo "  ERROR: The configuration you've specified ($1) does not exist"
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  exit 1
fi


PROJECT_VERSION=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)

echo -n "Determine latest Tomcat 9 version .. "
LISTING=`curl https://downloads.apache.org/tomcat/tomcat-9/ -s -o - | grep -oE 'v[0-9\.]+'`
TOMCAT_VERSION=$(echo "$LISTING" | tr ' ' '\n' | sort -Vr | head -n1 | sed 's/v//' )

echo $TOMCAT_VERSION

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
cp $SCRIPT_BASE/ebextensions $SCRIPT_BASE/target/.ebextensions -R
cd $SCRIPT_BASE/target

TOMCAT_FILE=apache-tomcat-$TOMCAT_VERSION.tar.gz

# did we already download this tomcat? copy it back from /tmp otherwise download it
if [ -f "/tmp/$TOMCAT_FILE" ]; then
  cp /tmp/$TOMCAT_FILE .
else
  TOMCAT_URL="https://downloads.apache.org/tomcat/tomcat-9/v$TOMCAT_VERSION/bin/$TOMCAT_FILE"
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
  echo "  Could not merge $CONFIG into $SCRIPT_BASE/templates/context.xml.tpl, check your config "
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  exit 1
fi

node $SCRIPT_BASE/merge.js $CONFIG $SCRIPT_BASE/templates/repository.xml.tpl > $SCRIPT_BASE/target/conf/repository.xml
if [ "$?" != "0" ]; then
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  echo "  Could not merge $CONFIG into $SCRIPT_BASE/templates/repository.xml.tpl, check your config "
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  exit 1
fi

node $SCRIPT_BASE/merge.js $CONFIG $SCRIPT_BASE/templates/catalina.properties.tpl > $SCRIPT_BASE/target/conf/catalina.properties
if [ "$?" != "0" ]; then
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  echo "  Could not merge $CONFIG into $SCRIPT_BASE/templates/catalina.properties.tpl, check your config "
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  exit 1
fi

node $SCRIPT_BASE/merge.js $CONFIG $SCRIPT_BASE/templates/server.xml.tpl > $SCRIPT_BASE/target/conf/server.xml
if [ "$?" != "0" ]; then
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  echo "  Could not merge $CONFIG into $SCRIPT_BASE/templates/server.xml.tpl, check your config "
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  exit 1
fi

node $SCRIPT_BASE/merge.js $CONFIG $SCRIPT_BASE/templates/setenv.sh.tpl > $SCRIPT_BASE/target/bin/setenv.sh
if [ "$?" != "0" ]; then
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  echo "  Could not merge $CONFIG into $SCRIPT_BASE/templates/setenv.sh.tpl, check your config "
  echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  exit 1
fi

# custom script that removes going to background, healthier when used with Procfile.
cp $SCRIPT_BASE/templates/catalina.sh.tpl $SCRIPT_BASE/target/bin/catalina.sh

cd $SCRIPT_BASE/target 
zip $BASE/target/eb-application-$PROJECT_VERSION.zip . -r
cd $BASE

rm $SCRIPT_BASE/target -Rf

if [ "$DELETE_AFTERWARDS" == "true" ]; then
  echo "Deleting temporary configuration"
  rm -f /tmp/config.json
fi