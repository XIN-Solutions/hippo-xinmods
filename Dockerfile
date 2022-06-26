# syntax=docker/dockerfile:1
FROM ubuntu:22.10

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

#
# setup nginx
#
RUN adduser --system --no-create-home --shell /bin/false --group --disabled-login nginx
RUN cp .platform/nginx/nginx.conf /etc/nginx/nginx.conf
RUN /etc/init.d/nginx restart

#
# expose 80: nginx
#
EXPOSE 80

#
#	When a new container with this image runs, let's start the tomcat instance
#
CMD ["./bin/startup.sh"]
