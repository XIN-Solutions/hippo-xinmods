#!/bin/bash


#
# To enable S3 assets during development re-enable these values and give them proper values.
#
#export S3_ASSETS_ENABLED=true
#export S3_ASSETS_BUCKET=s3-bucket-name
#export S3_ASSETS_PREFIX=store-here/


mkdir -p storage/

if [ "$1" != "" ]; then
	# export AWS_HIPPOBUS_ARN=yourarn
	mvn clean verify && mvn -P cargo.run -Drepo.path=storage/$1
else
	# export AWS_HIPPOBUS_ARN=yourarn
	mvn clean verify && mvn -P cargo.run -Drepo.path=storage/dev
fi

