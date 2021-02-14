#!/bin/bash

if [ -z "$1" ]; then
	echo "Expecting a key identifier as first argument"
	exit
fi


openssl genrsa -out $1_private_key.pem 2048
openssl pkcs8 -topk8 -inform PEM -outform DER -in $1_private_key.pem -out $1_private_key.der -nocrypt
openssl rsa -in $1_private_key.pem -pubout -outform DER -out $1_public_key.der
openssl rsa -in $1_private_key.pem -outform PEM -pubout -out $1_public_key.pem
