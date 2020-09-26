#!/bin/bash

echo ">> Add nginx to webapp group"
usermod -a -G webapp nginx

echo ">> Stop nginx"
service nginx stop

echo ">> Start nginx"
service nginx start

