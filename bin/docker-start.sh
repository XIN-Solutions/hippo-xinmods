#!/bin/bash

# start nginx and keep trying to start it in the background
./bin/nginx-keepalive.sh &

# start tomcat, this will be a long running process
./bin/startup.sh
