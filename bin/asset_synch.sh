#!/bin/bash

CWD=`pwd`

cd site/src/main/webapp

while inotifywait -e modify,create -r ./*; do
	rsync -avz ./*  $CWD/target/tomcat8x/webapps/site
done
 
