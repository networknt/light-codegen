---
date: 2017-06-05T13:31:52-04:00
title: light-hybrid-4j server generator
---

# Input

## Model

This generator only generate the server platform that can host many other serivices. It doesn't have
any model for input.

## Config

Here is an exmaple of config.json for light-hybrid-4j generator.

```
{
  "rootPackage": "com.networknt.petstore",
  "handlerPackage":"com.networknt.petstore.handler",
  "modelPackage":"com.networknt.petstore.model",
  "artifactId": "petstore",
  "groupId": "com.networknt",
  "name": "petstore",
  "version": "1.0.1"
}
```

- rootPackage is the root package name for your project and it will normally be your domain plug project name.
- handlerPackage is the Java package for all generated handlers. 
- modelPackage is the Java package for all generated models or POJOs.
- artifactId is used in generated pom.xml for project artifactId
- groupId is used in generated pom.xml for project groupId
- name is used in generated pom.xml for project name
- version is used in generated pom.xml for project vesion


# Usage

## Java Command line

Before using the command line to generate the code, you need to check out the repo and build it.
I am using ~/networknt as workspace but it can be anywhere in your home.  

```
cd ~/networknt
git clone git@github.com:networknt/light-codegen.git
cd light-codegen
mvn clean install
```

This scaffolds a server platform which can host a number of light-hybrid-4j services.
The generated project is just a skeleton without any service. You have to generate one or more services
(by using the light-hybrid-4j service generator) and put these jar file(s) into the classpath and start this server.

For more information about light-hybrid-4j, please refer to the [project](https://github.com/networknt/light-hybrid-4j) 
and its [documentation](https://networknt.github.io/light-hybrid-4j/).

The following will generate a sample light-hybrid-4j **server** based on test configuration for a petstore.
Working directory: light-codegen

```
java -jar codegen-cli/target/codegen-cli.jar -f light-hybrid-4j-server -o /tmp/hybridserver -c light-hybrid-4j/src/test/resources/serverConfig.json
```


## Docker Command Line

The following command is using docker to generate light-hybrid-4j server into 
/tmp/light-codegen/hybridserver folder

```
docker run -it -v ~/networknt/light-codegen/light-hybrid-4j/src/test/resources:/light-api/input -v /tmp/light-codegen:/light-api/out networknt/light-codegen -f light-hybrid-4j-server -c /light-api/input/serverConfig.json -o /light-api/out/hybridserver
```

Let's change the owner and build the server

```
cd /tmp/light-codegen
sudo chown -R steve:steve hybridserver
cd hybridserver
mvn clean install
```
Let's wait until we have a server generated to start the server and test it.


## Docker Scripting

```
git clone git@github.com:networknt/model-config.git
cd model-config
./generate.sh light-hybrid-4j-server ~/networknt/model-config/hybrid/generic-server /tmp/hybridserver
```
Now you should have a project generated in /tmp/hybridserver/generated

## Codegen Site

The service API is ready. We are working on the UI with a generation wizard.
 
