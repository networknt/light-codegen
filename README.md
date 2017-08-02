A code generator based on [rocker](https://github.com/fizzed/rocker) that can be used as a command line utility or web service.

[Developer Chat](https://gitter.im/networknt/light-codegen) |
[Documentation](https://networknt.github.io/light-codegen) |
[Contribution Guide](CONTRIBUTING.md) |

[![Build Status](https://travis-ci.org/networknt/light-codegen.svg?branch=master)](https://travis-ci.org/networknt/light-codegen)

## Input
* A list of files as template
* Data model like OpenAPI Specification, light-hybrid-4j schema, or Graphql IDL.
* Config in JSON format which can be collected as a file from the command line prompt, or web wizard.

## Output
* A project that can be built and executed locally with the command line tool.
* A zip file that contains a project artifacts. Download, unzip, build and run.

## Rebuild
Every time you have template change, you have to rebuild the entire project in order to
regenerate the template code. Otherwise, the old template is still in use.
 
## Building the CLI
To run the command line tool, you need to build **light-codegen** locally and run the **codegen-cli** executable jar.

```
git clone git@github.com:networknt/light-codegen.git
cd light-codegen
mvn clean install
```

## Usage

This repository currently provides the following generators that can each be used to scaffold different project types.

### Utility Command Line

You can clone the light-codegen repo and build the project locally on an environment that Java 8 JDK and Maven is installed.
Once the project is built, you can call the codegen-cli.jar direct with parameters to generate project.

#### light-rest-4j

Given we have test swagger.json and config.json in light-rest-4j/src/test/resources folder,
the following command line will generate a RESTful petstore API at /tmp/gen folder. 

Working directory: _light-codegen_
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
Working directory: _light-codegen_
```
java -jar codegen-cli/target/codegen-cli.jar -f light-rest-4j -o /tmp/petstore -m https://raw.githubusercontent.com/networknt/model-config/master/rest/petstore/swagger.json -c https://raw.githubusercontent.com/networknt/model-config/master/rest/petstore/config.json
```

Please note that you need to use a raw url when accessing github files. The above command line will
generate a petstore service in _/tmp/petstore_.

#### light-hybrid-4j server

This is a generator that scaffolds a server platform which can host a number of light-hybrid-4j services.
The generated project is just a skeleton without any service. You have to generate one or more services
(by using the below light-hybrid-4j service generator) and put these jar file(s) into the classpath and start this server.

For more information about light-hybrid-4j, please refer to the [project](https://github.com/networknt/light-hybrid-4j) 
and its [documentation](https://networknt.github.io/light-hybrid-4j/) (working in progress).

The following will generate a sample light-hybrid-4j **server** based on test configuration for a petstore.
Working directory: _light-codegen_

```
java -jar codegen-cli/target/codegen-cli.jar -f light-hybrid-4j-server -o /tmp/hybridserver -c light-hybrid-4j/src/test/resources/serverConfig.json
```


#### light-hybrid-4j service

This is a generator that scaffolds a service module that will be hosted on a light-hybrid-4j server
platform as a jar file. Multiple modules can be hosted on the same server if needed and they can
interact with each other through a module interface/contract defined by schema files. The generated
project cannot run directly as it's only a small jar file without a main class. However its services are
enabled by placing the jar file into the classpath of the server. For multiple jar files, we recommend
creating a folder like /service or /lib within the class path of the server and having it contained all
necessary light-hybrid-4j services.

The following will generate a sample light-hybrid-4j service which exposes a hello-world endpoint.
Working directory: _light-codegen_
```
java -jar codegen-cli/target/codegen-cli.jar -f light-hybrid-4j-service -o /tmp/hybridservice -m light-hybrid-4j/src/test/resources/schema.json -c light-hybrid-4j/src/test/resources/serviceConfig.json
```

Now we have a server and a service generated. Let's go through deploying the service and starting the server.

First let's build the **service** and copy the jar file into the **server** folder:
```
cd /tmp/hybridservice
mvn clean install
cp target/petstore-1.0.1.jar /tmp/hybridserver
```

Now let's build the **server** and start it while including the above **service** in its classpath:
```
cd /tmp/hybridserver
mvn clean install
java -cp petstore-1.0.1.jar:target/petstore-1.0.1.jar com.networknt.server.Server
```

Let's use curl to test one of the services:
```
curl -X POST \
  http://localhost:8080/api/json \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -H 'postman-token: 58bb63eb-de70-b855-a633-5b043bb52c95' \
  -d '{
  "host": "lightapi.net",
  "service": "world",
  "action": "hello",
  "version": "0.1.1",
  "lastName": "Hu",
  "firstName": "Steve"
}'
```


#### light-graphql-4j

This is a generator that scaffolds a light-graphql-4j project. Currently, it generates schema as
"Hello World" and in the future it will support GraphQl IDL to generate schema from IDL.

The following command will create a sample GraphQl server in the /tmp/graphql folder:
Working directory: _light-codegen_
```
java -jar codegen-cli/target/codegen-cli.jar -f light-graphql-4j -o /tmp/graphql -c light-graphql-4j/src/test/resources/config.json
```

And to build and run the server:
```
cd /tmp/graphql
mvn clean install exec:exec
```

Open your browser and point to http://localhost:8080/graphql for the graphiql interface to show
up in your browser. 

### Docker Command Line

Above local build and command line utilty works but it is very hard to use that in devops script. 
In order to make scripting easier, we have dockerized the command line utility. 

#### light-rest-4j

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

#### light-hybrid-4j server

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


#### light-hybrid-4j service

The following command is using docker to generate light-hybrid-4j service into
/tmp/light-codegen/hybridservice folder

```
docker run -it -v ~/networknt/light-codegen/light-hybrid-4j/src/test/resources:/light-api/input -v /tmp/light-codegen:/light-api/out networknt/light-codegen -f light-hybrid-4j-service -m /light-api/input/schema.json -c /light-api/input/serviceConfig.json -o /light-api/out/hybridservice
```

Let's change the owner and build the service

```
cd /tmp/light-codegen
sudo chown -R steve:steve hybridservice
cd hybridservice
mvn clean install

```

To run the server with services, please following the instruction in utility command line.


#### light-graphql-4j

The following command is using docker to generate light-graphql-4j into 
/tmp/light-codegen/graphql folder

```
docker run -it -v ~/networknt/light-codegen/light-graphql-4j/src/test/resources:/light-api/input -v /tmp/light-codegen:/light-api/out networknt/light-codegen -f light-graphql-4j -c /light-api/input/config.json -o /light-api/out/graphql
```
Let's change the owner and build the service

```
cd /tmp/light-codegen
sudo chown -R steve:steve graphql
cd graphql
mvn clean install exec:exec

```

To test the server, please follow the instructions above in utility command line.

### Scripting

You can use docker run command to call the generator but it is very complicated for the parameters.
In order to make things easier and friendlier to devops flow. Let's create a script to call the
command line from docker image.

If you look at the docker run command you can see that we basically need one input folder for 
schema and config files and one output folder to generated code. Once these volumes are mapped to 
local directory and with framework specified, it is easy to derive other files based on
convention. 

#### light-rest-4j

```
git clone git@github.com:networknt/model-config.git
cd model-config
./generate.sh light-rest-4j ~/networknt/model-config/rest/petstore /tmp/petstore
```
Now you should have a project generated in /tmp/petstore/genereted


#### light-hybrid-4j server

```
git clone git@github.com:networknt/model-config.git
cd model-config
./generate.sh light-hybrid-4j-server ~/networknt/model-config/hybrid/generic-server /tmp/hybridserver
```
Now you should have a project generated in /tmp/hybridserver/generated


#### light-hybrid-4j service

```
git clone git@github.com:networknt/model-config.git
cd model-config
./generate.sh light-hybrid-4j-service ~/networknt/model-config/hybrid/generic-service /tmp/hybridservice
```

Now you should have a project generated in /tmp/hybridservice/generated


#### light-graphql-4j

```
git clone git@github.com:networknt/model-config.git
cd model-config
./generate.sh light-graphql-4j ~/networknt/model-config/graphql/helloworld /tmp/graphql
```

Now you should have a project generated in /tmp/graphql/generated

### Codegen Site

The service API is ready. We are working on the UI with a generation wizard.

### Multiple Frameworks

Whether or not you are using command line or the web site to generate code, you can choose more
than one frameworks at a time to combine the framework code together. Normally, you choose one to
generate backend service and another one to generate front end single page application based on
Angular or React. 

For command line tool, you can choose to generate the backend service first to a target directory
and then run another command line to generate the front end application into the same target folder.
The final target folder should have a running application with both front end and back end tightly
integrated together.

For web interface, it is a little bit complicated as both backend and front end frameworks must
be select before triggering generation and the final result will be zipped and moved to download
folder. You first choose the first framework from a dropdown and give model and config (can be a
url link or upload/copy from local file system in JSON format). And then you can choose the second
framework and provide detailed model and config. After all frameworks are selected and configured,
you click the submit button to send the request to the server. The server response will have a
url to the downloadable zip file that contains all the generated files. You just need to click the
link to download the project file to your local drive. Then unzip, build, execute and test your
project. 

