# Change Log

## [1.5.8](https://github.com/networknt/light-codegen/tree/1.5.8) (2018-02-03)
[Full Changelog](https://github.com/networknt/light-codegen/compare/1.5.7...1.5.8)

**Closed issues:**

- config null check [\#100](https://github.com/networknt/light-codegen/issues/100)

## [1.5.7](https://github.com/networknt/light-codegen/tree/1.5.7) (2018-01-10)
[Full Changelog](https://github.com/networknt/light-codegen/compare/1.5.6...1.5.7)

**Closed issues:**

- Add dependency-reduced-pom.xml to .gitignore template [\#99](https://github.com/networknt/light-codegen/issues/99)

## [1.5.6](https://github.com/networknt/light-codegen/tree/1.5.6) (2017-12-31)
[Full Changelog](https://github.com/networknt/light-codegen/compare/1.5.4...1.5.6)

**Fixed bugs:**

- The cli treat graphqls model file as JSON for light-graphql-4j generator [\#92](https://github.com/networknt/light-codegen/issues/92)

**Closed issues:**

- Add emailPassword to secret.yml for release 1.5.6 [\#98](https://github.com/networknt/light-codegen/issues/98)
- Fixes maven build warnings [\#97](https://github.com/networknt/light-codegen/issues/97)
- remove prerequisites maven version [\#96](https://github.com/networknt/light-codegen/issues/96)
- Update client.yml for client\_id, client\_secret and server\_url [\#95](https://github.com/networknt/light-codegen/issues/95)
- Add bootstrapFromKeyService to false as default in security.yml [\#94](https://github.com/networknt/light-codegen/issues/94)
- Add keyClientSecret in secret.yml for service to access key distribution service in light-oauth2 [\#93](https://github.com/networknt/light-codegen/issues/93)
- Following docs to generate code openapi scaffolding [\#91](https://github.com/networknt/light-codegen/issues/91)
- OpenAPI validators are not loaded correctly on codegen-cli [\#90](https://github.com/networknt/light-codegen/issues/90)
- Allow codegen to generate code without security definitions being required [\#82](https://github.com/networknt/light-codegen/issues/82)

## [1.5.4](https://github.com/networknt/light-codegen/tree/1.5.4) (2017-11-22)
[Full Changelog](https://github.com/networknt/light-codegen/compare/1.5.0...1.5.4)

**Implemented enhancements:**

- Switch to service IoC injection from Java SPI [\#89](https://github.com/networknt/light-codegen/issues/89)

**Closed issues:**

- Add OpenAPI generator to SPI config file [\#88](https://github.com/networknt/light-codegen/issues/88)
- Split RestGenerator to SwaggerGenerator and OpenApiGenerator [\#87](https://github.com/networknt/light-codegen/issues/87)
- Update all generators to replace Java Service Provider with service.yml [\#86](https://github.com/networknt/light-codegen/issues/86)
- Update security.yml template for new properties [\#85](https://github.com/networknt/light-codegen/issues/85)

## [1.5.0](https://github.com/networknt/light-codegen/tree/1.5.0) (2017-10-21)
[Full Changelog](https://github.com/networknt/light-codegen/compare/1.4.6...1.5.0)

**Closed issues:**

- Update server.yml and secret.yml for environment tag and consul token [\#84](https://github.com/networknt/light-codegen/issues/84)
- Upgrade dependencies and add maven-version [\#81](https://github.com/networknt/light-codegen/issues/81)

## [1.4.6](https://github.com/networknt/light-codegen/tree/1.4.6) (2017-09-24)
[Full Changelog](https://github.com/networknt/light-codegen/compare/1.4.4...1.4.6)

## [1.4.4](https://github.com/networknt/light-codegen/tree/1.4.4) (2017-09-21)
[Full Changelog](https://github.com/networknt/light-codegen/compare/1.4.3...1.4.4)

## [1.4.3](https://github.com/networknt/light-codegen/tree/1.4.3) (2017-09-10)
[Full Changelog](https://github.com/networknt/light-codegen/compare/1.4.2...1.4.3)

## [1.4.2](https://github.com/networknt/light-codegen/tree/1.4.2) (2017-08-31)
[Full Changelog](https://github.com/networknt/light-codegen/compare/1.4.1...1.4.2)

## [1.4.1](https://github.com/networknt/light-codegen/tree/1.4.1) (2017-08-31)
[Full Changelog](https://github.com/networknt/light-codegen/compare/1.4.0...1.4.1)

**Closed issues:**

- Upgrade to newer version of Undertow and Jackson [\#80](https://github.com/networknt/light-codegen/issues/80)
- Add key section in client.yml template to enable key distribution support for JWT [\#79](https://github.com/networknt/light-codegen/issues/79)
- Update server.yml to enableHttp2 by default [\#78](https://github.com/networknt/light-codegen/issues/78)
- Remove HTTP1.1 settings in client.yml in rest, graphql and hybrid generators [\#77](https://github.com/networknt/light-codegen/issues/77)
- Add Headers in test case import for rest and hybrid [\#75](https://github.com/networknt/light-codegen/issues/75)
- The test cases generated for post, put and patch missing a header paramete [\#74](https://github.com/networknt/light-codegen/issues/74)
- Change codegen-web testing httpPort and HttpsPort and update test cases [\#73](https://github.com/networknt/light-codegen/issues/73)
- Change test httpPort and httpsPort to something seldom used in hybrid and graphql [\#72](https://github.com/networknt/light-codegen/issues/72)

## [1.4.0](https://github.com/networknt/light-codegen/tree/1.4.0) (2017-08-23)
[Full Changelog](https://github.com/networknt/light-codegen/compare/1.3.5...1.4.0)

**Closed issues:**

- Upgrade generate project test case to use Http2Client instead of Client [\#71](https://github.com/networknt/light-codegen/issues/71)
- Upgrade to Undertow 1.4.18.Final and remove jsonpath [\#69](https://github.com/networknt/light-codegen/issues/69)
- Add enableHttp2 to server.yml template for all frameworks [\#68](https://github.com/networknt/light-codegen/issues/68)
- Add server.yml into the generated src/test/resources/config with seldom used port [\#67](https://github.com/networknt/light-codegen/issues/67)
- Add Jackson datatype jsr310 into dependencies for all frameworks pom.xml [\#65](https://github.com/networknt/light-codegen/issues/65)

## [1.3.5](https://github.com/networknt/light-codegen/tree/1.3.5) (2017-08-02)
[Full Changelog](https://github.com/networknt/light-codegen/compare/1.3.3...1.3.5)

**Closed issues:**

- Update hybrid and graphql to support databases and server configurations [\#64](https://github.com/networknt/light-codegen/issues/64)
- Update light-hybrid-server and light-hybrid-service generator to remove jsoniter and add json-schema-validator [\#63](https://github.com/networknt/light-codegen/issues/63)
- Update light-rest-4j pom.xml template to remove jsonpath and add json-schema-validator [\#62](https://github.com/networknt/light-codegen/issues/62)
- Add JsonProperty annotation for generated getters [\#61](https://github.com/networknt/light-codegen/issues/61)
- Handle special character "@" as part of the variable name in swagger in light-rest-4j generator. [\#60](https://github.com/networknt/light-codegen/issues/60)
- Generate model \(POJO\) from swagger.json for light-rest-4j generator [\#59](https://github.com/networknt/light-codegen/issues/59)
- Replace jsonpath with jsoniter in the dependencies and StartupHookProvider config [\#58](https://github.com/networknt/light-codegen/issues/58)
- Create a build.sh script to build docker image and push to docker hub automatically [\#57](https://github.com/networknt/light-codegen/issues/57)
- Form-data request content support [\#55](https://github.com/networknt/light-codegen/issues/55)
- Expose port in Dockerfile is not updated based on config.json like server.yml [\#54](https://github.com/networknt/light-codegen/issues/54)
- light-rest-4j generator handler test extract statusCode and body as variables for easy debugging [\#53](https://github.com/networknt/light-codegen/issues/53)
- A typo in httpsPort needs to be fixed in light-rest-4j generator [\#52](https://github.com/networknt/light-codegen/issues/52)
- Summary review of schema/config json to be issued for codgen. [\#31](https://github.com/networknt/light-codegen/issues/31)
- Allow configuration by file upload [\#30](https://github.com/networknt/light-codegen/issues/30)

## [1.3.3](https://github.com/networknt/light-codegen/tree/1.3.3) (2017-06-14)
**Fixed bugs:**

- Need to skip extensions in operation list. [\#16](https://github.com/networknt/light-codegen/issues/16)

**Closed issues:**

- Add serviceId \(sId\) into logback.xml as part of MDC in slf4j [\#51](https://github.com/networknt/light-codegen/issues/51)
- Update client and server keystore and truststore along with secret.yml for rest generator [\#50](https://github.com/networknt/light-codegen/issues/50)
- Update hybrid generator for server and client keystore and truststore along with secret.yml [\#49](https://github.com/networknt/light-codegen/issues/49)
- Update graphql generator server and client keystore and truststore and secret.yml [\#48](https://github.com/networknt/light-codegen/issues/48)
- update server and client trust stores and key stores along with secret.yml [\#47](https://github.com/networknt/light-codegen/issues/47)
- Add support for example in light-hybrid-4j service generator.  [\#46](https://github.com/networknt/light-codegen/issues/46)
- Move schema.json to resources folder from resources/config folder for light-hybrid-4j service [\#45](https://github.com/networknt/light-codegen/issues/45)
- Fix the error when swagger.json has no definitions for models in light-rest-4j generator [\#44](https://github.com/networknt/light-codegen/issues/44)
- Fix the classpath for services to /service/\* in light-hybrid-4j server Dockerfile [\#43](https://github.com/networknt/light-codegen/issues/43)
- Make http port and https port configurable in codegen [\#42](https://github.com/networknt/light-codegen/issues/42)
- Add server and service module into light-hybrid server pom.xml template [\#41](https://github.com/networknt/light-codegen/issues/41)
- Add json-schema-validator dependency into light-hybrid service generator as it is used in the test cases. [\#40](https://github.com/networknt/light-codegen/issues/40)
- Update generated Dockerfile to pass in externalized logback.xml in order to override the default logging config. [\#39](https://github.com/networknt/light-codegen/issues/39)
- Add rpc-security dependency for light-hybrid-4j service generator pom.xml template [\#38](https://github.com/networknt/light-codegen/issues/38)
- implement logic to control overwritten handlers and handler tests with config [\#37](https://github.com/networknt/light-codegen/issues/37)
- inject /server/info and /health into swagger.json for light-rest-4j generator [\#36](https://github.com/networknt/light-codegen/issues/36)
- Upgrade graphql-java to 3.0.0 with IDL schema [\#35](https://github.com/networknt/light-codegen/issues/35)
- Add zookeeper dependencies to light-rest-4j and exclude log4j [\#33](https://github.com/networknt/light-codegen/issues/33)
- Add rpc-security in light-hybrid-4j server generator for 1.3.1 release. [\#32](https://github.com/networknt/light-codegen/issues/32)
- Retrieve configuration from the server given a selected generator. [\#28](https://github.com/networknt/light-codegen/issues/28)
- Add and display multiple pairings of generator/schema combinations. [\#25](https://github.com/networknt/light-codegen/issues/25)
- README.md updates pending review and merge [\#24](https://github.com/networknt/light-codegen/issues/24)
- Update light-hybrid-4j pom and readme [\#22](https://github.com/networknt/light-codegen/issues/22)
- Add another handler in codegen-web to handler multiple generator requests. [\#20](https://github.com/networknt/light-codegen/issues/20)
- Add schema for each generator and serve the schema from codegen-web for UI [\#19](https://github.com/networknt/light-codegen/issues/19)
- Remove swagger-annotation dependency as it is not in use [\#17](https://github.com/networknt/light-codegen/issues/17)
- Update light-rest-4j generated pom.xml for swagger and mockito version [\#15](https://github.com/networknt/light-codegen/issues/15)
- change all project names to remove java [\#14](https://github.com/networknt/light-codegen/issues/14)
- Change the codegen-cli jar to a fixed name without version number [\#13](https://github.com/networknt/light-codegen/issues/13)
- Name space issue with templates [\#12](https://github.com/networknt/light-codegen/issues/12)
- create a generator for light-java-graphql [\#11](https://github.com/networknt/light-codegen/issues/11)
- Move toByteBuffer from String to light-java utility module in order to share [\#10](https://github.com/networknt/light-codegen/issues/10)
- Add schema for light-java-hybrid service [\#9](https://github.com/networknt/light-codegen/issues/9)
- create a generator for light-java-hybrid [\#8](https://github.com/networknt/light-codegen/issues/8)
- create a generator for light-java-hybrid-server platform [\#7](https://github.com/networknt/light-codegen/issues/7)
- light-java-rest generator data model generation from swagger.json [\#5](https://github.com/networknt/light-codegen/issues/5)
- Update each individual generator to SPI so that they can be looked up by framework name. [\#4](https://github.com/networknt/light-codegen/issues/4)
- Inject server info into the swagger.json as well as in generated code [\#3](https://github.com/networknt/light-codegen/issues/3)
- Accept yaml format for both config and swagger specification [\#2](https://github.com/networknt/light-codegen/issues/2)
- Build web service with light-java-hybrid framework in codegen-web module [\#1](https://github.com/networknt/light-codegen/issues/1)

**Merged pull requests:**

- View initial commit [\#18](https://github.com/networknt/light-codegen/pull/18) ([NicholasAzar](https://github.com/NicholasAzar))



\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*