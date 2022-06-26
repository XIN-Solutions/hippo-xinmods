# syntax=docker/dockerfile:1
FROM ubuntu:22.10


RUN apt-get update
RUN apt-get install -y zip unzip nginx openjdk-11-jdk

WORKDIR /app
COPY target/eb-application* .
RUN unzip eb-application*
RUN cp .platform/nginx/nginx.conf /etc/nginx/nginx.conf
#RUN /etc/init.d/nginx restart

# expose 8080 (/cms, /cms/console, /site)
EXPOSE 8080

# expose 80: nginx
EXPOSE 80

#
#	When a new container with this image runs, let's start the tomcat instance
#
CMD ["./bin/startup.sh"]
