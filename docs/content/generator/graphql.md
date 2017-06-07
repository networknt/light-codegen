---
date: 2017-06-05T13:28:11-04:00
title: light-graphql-4j generator
---

# Input

## Model

There are two different ways to generate projects with light-graphql-4j generator.

- With IDL - Pass in IDL and you need to wire in your backend logic. 
- Without IDL - A simple Hello World graphql application runnable as starting point.

NOTE: IDL is not currently part of the formal graphql spec. The implementation in this 
library is based off the reference implementation. However plenty of code out there is 
based on this IDL syntax and hence you can be fairly confident that you are building on 
solid technology ground.

We recommend using IDL. For more information about IDL, please check [here](http://graphql-java.readthedocs.io/en/latest/schema.html)

## Config

Here is an exmaple of config.json for light-rest-4j generator.

```
{
  "schemaPackage": "com.networknt.starwars.schema",
  "artifactId": "starwars",
  "groupId": "com.networknt",
  "name": "starwars",
  "version": "1.0.1",
  "schemaClass": "StarWarsSchema",
  "overwriteSchemaClass": true
}
```

- schemaPackage is the package name for generated schema class
- artifactId is used in generated pom.xml for project artifactId
- groupId is used in generated pom.xml for project groupId
- name is used in generated pom.xml for project name
- version is used in generated pom.xml for project vesion
- schemaClass is the generated schema class name
- overwriteSchemaClass controls if the schema class needs to be generated or not. For new project, it should be true. If you want to upgrade the framework to a new version but don't want to overwrite the updated schema class, then set it to false and regenerate to the same folder. 

In most of the cases, developers will only update schema class and other depending classes in schema package. 


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

### Without IDL

First, let's generate schema as "Hello World" as a starting point so that developers can update 
it to manually code their schema class.

The following command will create a sample GraphQl server in the /tmp/graphql folder:
Working directory: light-codegen

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

### With IDL

Let's generate a graphql project with IDL. There is a starwars IDL in our test folder.

Working directory: light-codegen

```
java -jar codegen-cli/target/codegen-cli.jar -f light-graphql-4j -o /tmp/graphql -m light-graphql-4j/src/test/resources/schema.graphqls -c light-graphql-4j/src/test/resources/config.json
```

The generated project can be built but not runnable as there is no backend code wired in yet. Take a look
at the schema class and make the change in the commented section to complete it.


## Docker Command Line

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


## Docker Scripting

```
git clone git@github.com:networknt/model-config.git
cd model-config
./generate.sh light-graphql-4j ~/networknt/model-config/graphql/helloworld /tmp/graphql
```

Now you should have a project generated in /tmp/graphql/generated

## Codegen Site

The service API is ready. We are working on the UI with a generation wizard.
 
