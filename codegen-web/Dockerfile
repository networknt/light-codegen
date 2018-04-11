FROM openjdk:8-jre-alpine

MAINTAINER "Nicholas Azar <nicholas@nicholasazar.com>"

EXPOSE  8080

ADD /target/codegen-web-1.5.13.jar server.jar

CMD ["/bin/sh","-c","java -jar /server.jar"]