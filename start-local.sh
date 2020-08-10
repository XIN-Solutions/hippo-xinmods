#!/bin/bash

mkdir -p storage/
# export AWS_HIPPOBUS_ARN=yourarn
mvn clean verify && mvn -P cargo.run -Drepo.path=storage/dev 
