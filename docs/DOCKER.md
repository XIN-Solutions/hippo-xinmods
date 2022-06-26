# Bloomreach Docker Image

Follow these instructions to build a docker image for your preconfigured Bloomreach instance:

## Build with a configuration

Create a preconfigured ElasticBeanstalk distribution. The Dockerfile knows how to unzip it and place the 
configurations in the correct location for the docker image. 

    ./bin/deploy/build-eb.sh s3://bucket/path/to/config.json

Then create a Docker image from the resulting ZIP file:

    sudo docker build -t bloomreach-backend .

You can run it up locally. Make sure to specify a volume for the Bloomreach Repository that attaches to
`/var/lib/hippostorage`, which is the configuration location for the repository indexes and other storage.

    sudo docker run --name brxm -v bloomreach-repo:/var/lib/hippostorage bloomreach-backend:latest

## Networking

To get access to the container from its own IP address attach the `brxm` container 

Create a new network and add the brxm container

    sudo docker network create bloomreach
    sudo docker network connect bloomreach devtest

Find the IP for Bloomreach instance by inspecting network

    sudo docker network inspect bloomreach

## Debugging

Login to the newly started container with a shell:

    sudo docker exec -it brxm bash

