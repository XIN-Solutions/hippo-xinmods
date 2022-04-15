#!/bin/bash

mkdir -p storage/

if [ "$1" != "" ]; then
	# export AWS_HIPPOBUS_ARN=yourarn
	mvn clean verify && mvn -P cargo.run -Drepo.path=storage/$1
else
	# export AWS_HIPPOBUS_ARN=yourarn
	mvn clean verify && mvn -P cargo.run -Drepo.path=storage/dev 
fi

