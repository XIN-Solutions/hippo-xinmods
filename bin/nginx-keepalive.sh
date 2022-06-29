#!/bin/bash

while [ "1" == "1" ]
do
	nginx -g "daemon off;"
	echo "Restarting Nginx."
	sleep 5
done