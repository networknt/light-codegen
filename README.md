# light-codegen
A code generator based on rocker that can be used as an utility or web service

## Input

* A list of files as template
* Data model like OpenAPI Specification
* Config in JSON format which can be collected as A JSON, command line prompt or web wizard.

## Output

* A project that can be built and executed.

## Usage

### Command Line

Given we have test swagger.json and config.json in light-java-rest/src/main/resources folder, 
following command line will generate petstore API at /tmp/gen folder.

```
java -jar target/codegen-0.1.0.jar -f light-java-rest -o /tmp/gen -m ~/networknt/light-codegen/light-java-rest/src/test/resources/swagger.json -c ~/networknt/light-codegen/light-java-rest/src/test/resources/config.json
```

Note: Above command assume that your working directory is ~/networknt
 
After you run the above command, you can build and start the service.

```
cd /tmp/gen
mvn clean install exec:exec
```

To test the service from another terminal.

```
curl http://localhost:8080/v2/pet/11
```

The above example use local swagger specification and config file. Let's try to use files from
github.com


```
java -jar target/codegen-0.1.0.jar -f light-java-rest -o /tmp/petstore -m https://raw.githubusercontent.com/networknt/swagger/master/petstore/swagger.json -c https://raw.githubusercontent.com/networknt/swagger/master/petstore/config.json
```

Please note that you need to use raw url when access github file. The above command line will
generate petstore service in /tmp/petstore.


### Codegen Site

