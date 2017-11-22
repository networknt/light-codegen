# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Added

### Changed

## 1.5.4 - 2017-11-20
### Added

### Changed
- fixes #89 Switch to service IoC injection from Java SPI
- fixes #88 Add OpenAPI generator to SPI config file
- fixes #87 Split RestGenerator to SwaggerGenerator and OpenApiGenerator
- fixes #86 Update all generators to replace Java Service Provider with service.yml
- Upgrade to light-4j 1.5.4
- Upgrade to light-rest-4j 1.5.4
- Upgrade to light-graphql-4j 1.5.4
- Upgrade to openapi-parser 1.5.4
- Upgrade to light-hybrid-4j 1.5.4

## 1.5.1 - 2017-11-08
### Added

### Changed
- fixes #85 Update security.yml template for new properties
- Upgrade to light-4j 1.5.1
- Upgrade to light-rest-4j 1.5.1
- Upgrade to light-graphql-4j 1.5.1
- Upgrade to light-hybrid-4j 1.5.1

## 1.5.0 - 2017-10-21
### Added

### Changed
- fixes #81 Upgrade dependencies and add maven-version 
- Upgrade to light-4j 1.5.0
- Upgrade to light-rest-4j 1.5.0
- Upgrade to light-graphql-4j 1.5.0
- Upgrade to light-hybrid-4j 1.5.0

## 1.4.6 - 2017-09-24
### Added

### Changed
- Upgrade to light-4j 1.4.6
- Upgrade to light-rest-4j 1.4.6
- Upgrade to light-graphql-4j 1.4.6
- Upgrade to light-hybrid-4j 1.4.6

## 1.4.4 - 2017-09-20
### Added

### Changed
- Upgrade to light-4j 1.4.4
- Upgrade to light-rest-4j 1.4.4
- Upgrade to light-graphql-4j 1.4.4
- Upgrade to light-hybrid-4j 1.4.4

## 1.4.3 - 2017-09-10
### Added

### Changed
- Upgrade to light-4j 1.4.3
- Upgrade to light-rest-4j 1.4.3
- Upgrade to light-graphql-4j 1.4.3
- Upgrade to light-hybrid-4j 1.4.3

## 1.4.2 - 2017-08-31
### Added

### Changed
- Upgrade to light-4j 1.4.2
- Upgrade to light-rest-4j 1.4.2
- Upgrade to light-graphql-4j 1.4.2
- Upgrade to light-hybrid-4j 1.4.2

## 1.4.1 - 2017-08-30
### Added

### Changed
- fixes #80 Upgrade to newer version of Undertow and Jackson
- fixes #79 Add key section in client.yml template to enable key distribution
- fixes #78 Update server.yml to enableHttp2 by default
- fixes #77 Remove HTTP1.1 settings in client.yml in rest, graphql and
- fixes #75 Add Headers in test case import for rest and hybrid
- fixes #74 add content type header as body parser needs it
- fixes #73 Change codegen-web testing httpPort and HttpsPort
- fixes #72 Change test httpPort and httpsPort to something seldom used
- Upgrade to light-4j 1.4.1
- Upgrade to light-rest-4j 1.4.1
- Upgrade to light-graphql-4j 1.4.1
- Upgrade to light-hybrid-4j 1.4.1

## 1.4.0 - 2017-08-22
### Added

### Changed
- fixes #71 replace Client to Http2Client
- fixes #69 Upgrade to Undertow 1.4.18.Final and remove jsonpath
- fixes #68 disable http2 by default as it allows only https
- fixes #67 Add server.yml into the generated src/test/resources/config
- fixes #65 Add Jackson datatype jsr310 into dependencies
- Upgrade to light-4j 1.4.0
- Upgrade to light-rest-4j 1.4.0
- Upgrade to light-graphql-4j 1.4.0
- Upgrade to light-hybrid-4j 1.4.0


## 1.3.5 - 2017-08-02
### Added

### Changed
- fixes #64 Update hybrid and graphql to support databases and server c…
- fixes #63 Update light-hybrid-server and light-hybrid-service generat… 
- fixes #62 update light-rest-4j pom.xml template to remove jsonpath an…
- fixes #61 add JsonProperty annotation
- fixes #60 handler special @ character in name from swagger.json
- fixes #59 use double or float if format is specified for number
- fixes #59 handle BigDecimal mapping
- fixes #59 generate pojo from swagger.json for light-rest-4j generator
- rollback #58 as the corresponding release in light-*-4j is not done yet
- Getting frameworks from server
- Fix some schema and config issues and properly display json in generate
- fixes #58 replace jsonpath with jsoniter in dependencies and StartupHookProvider
- Add entry point to validate uploaded schema/config files
- add test cases for JsonIter
- fixes #56 codegen-web will be compile as a thin jar instead of fat jar
- adding networknt favicon
- fixes #54 expose the right port in Dockerfile for light-rest-4j
- Upgrade to light-4j 1.3.5
- Upgrade to light-rest-4j 1.3.5
- Upgrade to light-graphql-4j 1.3.5
- Upgrade to light-hybrid-4j 1.3.5

## 1.3.4 - 2017-07-08
### Added

### Changed
- added individual http/https port enabling/configuration (Thanks @ddobrin)
- fixes #52 a typo in httpsPort in light-rest-4j generator
- added generation for db artifacts: oracle,mysql,postgres,h2 (Thanks @ddobrin)
- updated Postgres and MySql driver version (Thanks @ddobrin)
- fixes #53 light-rest-4j generator handler test extract statusCode
- Capitalize model package class names (Thanks @ddobrin)
- Updated POM template with a second Maven repo (Thanks @ddobrin)
- Add React SPA for light-codegen Web interface (Thanks @NicholasAzar)
- Upgrade to light-4j 1.3.4
- Upgrade to light-rest-4j 1.3.4
- Upgrade to light-graphql-4j 1.3.4
- Upgrade to light-hybrid-4j 1.3.4


## 1.3.3 - 2017-06-14
### Added

### Changed
- Upgrade to light-4j 1.3.3
- Upgrade to light-rest-4j 1.3.3
- Upgrade to light-graphql-4j 1.3.3
- Upgrade to light-hybrid-4j 1.3.3

## 1.3.2 - 2017-06-14
### Added

### Changed
- Upgrade to light-4j 1.3.2
- Upgrade to light-rest-4j 1.3.2
- Upgrade to light-graphql-4j 1.3.2
- Upgrade to light-hybrid-4j 1.3.2


## 1.3.1 - 2017-06-03
### Added

### Changed
- Upgrade to light-4j 1.3.1
- Upgrade to light-rest-4j 1.3.1
- Upgrade to light-graphql-4j 1.3.1
- Upgrade to light-hybrid-4j 1.3.1

