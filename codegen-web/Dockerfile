FROM openjdk:8-jre-alpine
MAINTAINER "Nicholas Azar <nicholas@nicholasazar.com>"
ADD /target/codegen-web-shaded.jar server.jar
CMD ["/bin/sh","-c","java -jar server.jar"]
EXPOSE  8080
