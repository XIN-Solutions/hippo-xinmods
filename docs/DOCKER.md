# Bloomreach Docker Image

Follow these instructions to build a docker image with a preconfigured Bloomreach instance:

## Start container from published Docker Hub image

Make sure you have MySQL host and credential information available. You'll need to already have an empty database 
created for this to work. And of course the database user needs to have access to this database.

    CREATE DATABASE my_database_for_brxm DEFAULT CHARSET 'utf8';

Create a new network and use the network name in the `docker run` command below:

    sudo docker network create brxm-network

Run remote image:

    sudo docker run --name brxm -d -v bloomreach-repo:/var/lib/hippostorage \
        --network=brxm-network \
        -e MYSQL_HOST=<databasehost> \
        -e MYSQL_DATABASE=<databasename> \
        -e MYSQL_PORT=3306 \
        -e MYSQL_USERNAME=<username> \
        -e MYSQL_PASSWORD=<password> \
        -e BRXM_CMS_HOST=<http-url-for-cms> \
        xinsolutions/bloomreach-xinmods-cms:latest

Of course you can also feed it an environment file, or you can use the image as part of a docker compose yaml.

## Build with a JSON configuration

If you have made changes to the xinmods project to accommodate your own
backend functionality, and you wish to deploy the application as a docker container follow these steps.

Create a preconfigured ElasticBeanstalk distribution. The Dockerfile knows how to unzip it and place the 
configurations in the correct location for the docker image. 

    ./bin/deploy/build-eb.sh s3://bucket/path/to/config.json

You can also reference a local JSON configuration file.

The shape of the configuration is as follows:

    {
    
        "mysql": {
            "host" : "",
            "port": 3306,
            "database" : "",
            "username" : "",
            "password": ""
        },
    
        "xin" : {
            "cmsHost": "https://cms.thedomainitson.com"
        }
    
    }


Alternatively, if you would like to configure your container using environment variables as per the example
at the top of this document, run the following:

    ./bin/deploy/build-docker.sh

Then create a Docker image from the resulting ZIP file by running this command in the root folder of the project:

    sudo docker build -t bloomreach-backend .


### Networking

To get access to the container from its own IP address attach the `brxm` container

Create a new network and add the brxm container

    sudo docker network create brxm-network

### Start your container

You can now run your newly built image up locally as a container. Make sure to specify a volume for the 
Bloomreach Repository that attaches to `/var/lib/hippostorage`, which is the configuration location for the repository indexes and other storage.

    sudo docker run --name brxm -d -v bloomreach-repo:/var/lib/hippostorage --network=brxm-network bloomreach-backend:latest

Parameters:

* `--name`: name of the docker container
* `--network=brxm-network`: the network to attach it 
* `-d`: detach so it runs in the background
* `-v bloomreach-repo:/var/lib/hippostorage`: create a `bloomreach-repo` volume to attach to where the repository nodes are used
* `bloomreach-backend:latest`: use our newly built image

Second time around, you can just start the container:

    sudo docker container start brxm 

## What's the IP?

Find the IP for Bloomreach instance by inspecting network

    sudo docker network inspect brxm-network

## Debugging

Login to the newly started container with a shell:

    sudo docker exec -it brxm bash

# Push to AWS ECR

Push the bloomreach-backend image you created, to the remote ECR:

    aws ecr get-login-password --region <region> | sudo docker login --username AWS --password-stdin <account_id>.dkr.ecr.ap-southeast-2.amazonaws.com

    sudo docker tag bloomreach-backend:latest <ecs_url>


