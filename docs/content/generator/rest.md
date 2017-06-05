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
  "rootPackage": "com.networknt.petstore",
  "handlerPackage":"com.networknt.petstore.handler",
  "modelPackage":"com.networknt.petstore.model",
  "artifactId": "petstore",
  "groupId": "com.networknt",
  "name": "petstore",
  "version": "1.0.1",
  "overwriteHandler": false,
  "overwriteHandlerTest": false,
  "overwriteModel": false
}
```

- rootPackage is the root package name for your project and it will normally be your domain plug project name.
- handlerPackage is the Java package for all generated handlers. 
- modelPackage is the Java package for all generated models or POJOs.
- artifactId is used in generated pom.xml for project artifactId
- groupId is used in generated pom.xml for project groupId
- name is used in generated pom.xml for project name
- version is used in generated pom.xml for project vesion
- overwriteHandler controls if you want to overwrite handler when regenerate the same project into the same folder. If you only want to upgrade the framework to another minor version and don't want to overwrite handlers, then set this property to false. 
- overwriteHandlerTest controls if you want to overwrite handler test cases.
- overwriteModel controls if you want to overwrite generated models.

In most of the cases, developers will only update handlers, handler tests and model in a project. 


# Output

