---
date: 2017-06-05T13:27:59-04:00
title: light-rest-4j generator
---

# Input

## Model

In light-rest-4j framework generator, the model that drives code generation is the OpenAPI 
specification previously named swagger specification. When editing it, normally it will
be in yaml format with separate files for readability and flexibility. Before leverage it
in light-rest-4j framework, all yaml files need to be bundled and converted into json format
in order to be consumed by the framework. Also, a validation needs to be done to make sure
that the generated swagger.json is valid against json schema of OpenAPI specification. 
 
Note: currently, we support OpenAPI specification 2.0 and will support 3.0 once it is released.


- [Swagger Editor](https://networknt.github.io/light-4j/tools/swagger-editor/)

- [Swagger CLI](https://networknt.github.io/light-4j/tools/swagger-cli/)


## Config

Here is an exmaple of config.json for light-rest-4j generator.

```
{
  "name": "petstore",
  "version": "1.0.1",
  "groupId": "com.networknt",
  "artifactId": "petstore",
  "rootPackage": "com.networknt.petstore",
  "handlerPackage":"com.networknt.petstore.handler",
  "modelPackage":"com.networknt.petstore.model",
  "overwriteHandler": true,
  "overwriteHandlerTest": true,
  "overwriteModel": true,
  "httpPort": 8080,
  "enableHttp": true,
  "httpsPort": 8443,
  "enableHttps": false,
  "enableRegistry": false,
  "supportOracle": false,
  "supportMysql": false,
  "supportPostgresql": false,
  "supportH2ForTest": false,
  "supportClient": false
}
```

- name is used in generated pom.xml for project name
- version is used in generated pom.xml for project vesion
- groupId is used in generated pom.xml for project groupId
- artifactId is used in generated pom.xml for project artifactId
- rootPackage is the root package name for your project and it will normally be your domain plug project name.
- handlerPackage is the Java package for all generated handlers. 
- modelPackage is the Java package for all generated models or POJOs.
- overwriteHandler controls if you want to overwrite handler when regenerate the same project into the same folder. If you only want to upgrade the framework to another minor version and don't want to overwrite handlers, then set this property to false. 
- overwriteHandlerTest controls if you want to overwrite handler test cases.
- overwriteModel controls if you want to overwrite generated models.
- httpPort is the port number of Http listener if enableHttp is true.
- enableHttp to specify if the server listens to http port. Http should only be enabled in dev.
- httpsPort is the port number of Https listener if enableHttps is true.
- enableHttps to specify if the server listens to https port. Https should be used in any official environment for security reason.
- enableRegistry to control if built-in service registry/discovery is used. Only necessary if running as standalone java -jar xxx.
- supportOracle if true, add Oracle JDBC client in pom.xml dependencies and add service.yml to connect Oracle during server startup.
- supportMysql if true, add Mysql JDBC client in pom.xml dependencies and add service.yml to connect Mysql during server startup.
- supportPostgresql if true, add Postgresql JDBC client in pom.xml dependencies and add service.yml to connect to Postgresql during server startup.
- supportH2ForTest if true, add H2 in pom.xml as test scope to support unit test with H2 database.
- supportClient if true, add com.networknt.client module to pom.xml to support service to service call.

In most of the cases, developers will only update handlers, handler tests and model in a project. 


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

Given we have test swagger.json and config.json in light-rest-4j/src/test/resources folder,
the following command line will generate a RESTful petstore API at /tmp/gen folder. 

Working directory: light-codegen
```
java -jar codegen-cli/target/codegen-cli.jar -f light-rest-4j -o /tmp/gen -m light-rest-4j/src/test/resources/swagger.json -c light-rest-4j/src/test/resources/config.json
```
 
After you run the above command, you can build and start the service:
```
cd /tmp/gen
mvn clean install exec:exec
```

To test the service from another terminal:
```
curl http://localhost:8080/v2/pet/11
```

The above example use local swagger specification and config file. Let's try to use files from
github.com:

Working directory: light-codegen
```
java -jar codegen-cli/target/codegen-cli.jar -f light-rest-4j -o /tmp/petstore -m https://raw.githubusercontent.com/networknt/model-config/master/rest/petstore/2.0.0/swagger.json -c https://raw.githubusercontent.com/networknt/model-config/master/rest/petstore/2.0.0/config.json
```

Please note that you need to use a raw url when accessing github files. The above command line will
generate a petstore service in /tmp/petstore.

Given we have most of the model and config files in model-config repo, most generator input would
from the rest folder in model-config. Here is the example to generate petstore. Assuming model-config
is in the same workspace as light-codegen.

Working directory: light-codegen

```
java -jar codegen-cli/target/codegen-cli.jar -f light-rest-4j -o /tmp/petstore -m ../model-config/rest/petstore/2.0.0/swagger.json -c ../model-config/rest/petstore/2.0.0/config.json

```

## Docker Command Line

Above local build and command line utility works but it is very hard to use that in devops script. 
In order to make scripting easier, we have dockerized the command line utility. 


The following command is using docker image to generate the code into /tmp/light-codegen/generated:
```
docker run -it -v ~/networknt/light-codegen/light-rest-4j/src/test/resources:/light-api/input -v /tmp/light-codegen:/light-api/out networknt/light-codegen -f light-rest-4j -m /light-api/input/swagger.json -c /light-api/input/config.json -o /light-api/out/generated
```
On Linux environment, the generated code might belong to root:root and you need to change the
owner to yourself before building it.

```
cd /tmp/light-codegen
sudo chown -R steve:steve generated
cd generated
mvn clean install exec:exec
```
To test it.
```
curl localhost:8080/v2/pet/111
```

## Docker Scripting

You can use docker run command to call the generator but it is very complicated for the parameters.
In order to make things easier and friendlier to devops flow. Let's create a script to call the
command line from docker image.

If you look at the docker run command you can see that we basically need one input folder for 
schema and config files and one output folder to generated code. Once these volumes are mapped to 
local directory and with framework specified, it is easy to derive other files based on
convention. 


```
git clone git@github.com:networknt/model-config.git
cd model-config
./generate.sh light-rest-4j ~/networknt/model-config/rest/petstore/2.0.0 /tmp/petstore
```
Now you should have a project generated in /tmp/petstore/genereted

## Codegen Site

The service API is ready. We are working on the UI with a generation wizard.
 

