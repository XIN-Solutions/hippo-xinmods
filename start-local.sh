#!/bin/bash

# export AWS_HIPPOBUS_ARN=yourarn
mvn verify && mvn -P cargo.run -Drepo.path=storage-dev 
