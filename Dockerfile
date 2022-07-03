# syntax=docker/dockerfile:1
ARG arch=amd64
FROM --platform=${arch} ubuntu:22.10

#
#	Install required packages
#
RUN apt-get update
RUN apt-get install -y zip unzip nginx openjdk-8-jdk vim links

#
#	Extract the configured distribution
#
WORKDIR /app
COPY target/eb-application* .
RUN unzip eb-application*

# replace context.xml/setenv.sh with environment based configurations
COPY bin/deploy/docker/context.xml ./conf/context.xml
COPY bin/deploy/docker/setenv.sh ./bin/setenv.sh

#
# setup nginx
#
RUN adduser --system --no-create-home --shell /bin/false --group --disabled-login nginx
RUN cp .platform/nginx/nginx.conf /etc/nginx/nginx.conf
RUN /etc/init.d/nginx restart

COPY ./bin/nginx-keepalive.sh ./bin/nginx-keepalive.sh
COPY ./bin/docker-start.sh ./bin/docker-start.sh
RUN chmod +x ./bin/nginx-keepalive.sh ./bin/docker-start.sh

#
# expose 80: nginx
#
EXPOSE 80

#
#	When a new container with this image runs, let's start the tomcat instance
#
CMD ["./bin/docker-start.sh"]
