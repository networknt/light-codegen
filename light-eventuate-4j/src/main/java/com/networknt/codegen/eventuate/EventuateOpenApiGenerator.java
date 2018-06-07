package com.networknt.codegen.eventuate;

import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import com.networknt.codegen.Generator;
import com.networknt.codegen.Utils;
import com.networknt.oas.OpenApiParser;
import com.networknt.oas.model.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.io.File.separator;


public class EventuateOpenApiGenerator implements Generator {
    private Map<String, String> typeMapping = new HashMap<>();
    boolean prometheusMetrics =false;
    public EventuateOpenApiGenerator() {
        typeMapping.put("array", "java.util.List");
        typeMapping.put("map", "java.util.Map");
        typeMapping.put("List", "java.util.List");
        typeMapping.put("boolean", "Boolean");
        typeMapping.put("string", "String");
        typeMapping.put("int", "Integer");
        typeMapping.put("float", "Float");
        typeMapping.put("number", "java.math.BigDecimal");
        typeMapping.put("DateTime", "Date");
        typeMapping.put("long", "Long");
        typeMapping.put("short", "Short");
        typeMapping.put("char", "String");
        typeMapping.put("double", "Double");
        typeMapping.put("object", "Object");
        typeMapping.put("integer", "Integer");
        typeMapping.put("ByteArray", "byte[]");
        typeMapping.put("binary", "byte[]");
    }

    @Override
    public String getFramework() {
        return "eventuate-rest";
    }

    /**
     *
     * @param targetPath The output directory of the generated project
     * @param model The optional model data that trigger the generation, i.e. swagger specification, graphql IDL etc.
     * @param config A json object that controls how the generator behaves.
     * @throws IOException IO Exception occurs during code generation
     */
    @Override
    public void generate(String targetPath, Object model, Any config) throws IOException {
        // whoever is calling this needs to make sure that model is converted to Map
        String projectName = config.toString("name");
        String projectPath = targetPath + "/" + projectName;

        String eventuateEventPackage = config.toString("eventuateEventPackage");
        String eventuateCommandPackage = config.toString("eventuateCommandPackage");
        String eventuateQueryPackage = config.toString("eventuateQueryPackage");
        boolean overwriteEventuateModule = config.toBoolean("overwriteEventuateModule");
        String httpPort = config.toString("httpPort");
        boolean enableHttps = config.toBoolean("enableHttps");
        String httpsPort = config.toString("httpsPort");
        String dockerOrganization = config.toString("dockerOrganization");
        prometheusMetrics = config.toBoolean("prometheusMetrics");

        String expose = "";
        if(enableHttps) {
            expose = httpsPort;
        } else {
            expose = httpPort;
        }

        if(dockerOrganization == null || dockerOrganization.length() == 0) dockerOrganization = "networknt";

        // files for root project
        transfer(projectPath, "", "pom.xml", templates.eventuate.rest.openapi.pom.template(config));
        transfer(projectPath, "", "build.sh", templates.eventuate.rest.buildSh.template(dockerOrganization, config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version")));
        transfer(projectPath, "", ".gitignore", templates.eventuate.rest.gitignore.template());
        transfer(projectPath, "", "README.md", templates.eventuate.rest.README.template());
        transfer(projectPath, "", "LICENSE", templates.eventuate.rest.LICENSE.template());
        transfer(projectPath, "", ".classpath", templates.eventuate.rest.classpath.template());
        transfer(projectPath, "", ".project", templates.eventuate.rest.project.template(config));

        // files for common module
        transfer(projectPath, "common", "pom.xml", templates.eventuate.rest.openapi.common.pom.template(config));

        // files for command module
        transfer(projectPath, "command", "pom.xml", templates.eventuate.rest.openapi.command.pom.template(config));

        // files for query module
        transfer(projectPath, "query", "pom.xml", templates.eventuate.rest.openapi.query.pom.template(config));

        // event & command & query modules
        if(overwriteEventuateModule) {
            String basedEventInterface = config.toString("basedEventInterface");
            if (basedEventInterface!=null && basedEventInterface.length()>0)  {
                transfer(projectPath + "/common", ("src.main.java." + eventuateEventPackage).replace(".", separator), basedEventInterface + ".java", templates.eventuate.rest.openapi.common.event.template(eventuateEventPackage, basedEventInterface));
            } else {
                transfer(projectPath + "/common", ("src.main.java." + eventuateEventPackage).replace(".", separator), "BaseEvent" + ".java", templates.eventuate.rest.openapi.common.event.template(eventuateEventPackage, "BaseEvent"));
            }
            String basedCommandInterface = config.toString("basedCommandInterface");
            if (basedCommandInterface!=null && basedCommandInterface.length()>0)  {
                transfer(projectPath + "/command", ("src.main.java." + eventuateCommandPackage).replace(".", separator), basedCommandInterface + ".java", templates.eventuate.rest.openapi.command.command.template(eventuateCommandPackage, basedCommandInterface));
            } else {
                transfer(projectPath + "/command", ("src.main.java." + eventuateCommandPackage).replace(".", separator), "BaseCommand" + ".java", templates.eventuate.rest.openapi.command.command.template(eventuateCommandPackage, "BaseCommand"));
            }
            transfer(projectPath + "/command", ("src.main.java." + eventuateCommandPackage + ".domain").replace(".", separator), "SampleAggregate.java", templates.eventuate.rest.openapi.command.aggregate.template(eventuateCommandPackage + ".domain", "SampleAggregate"));
            transfer(projectPath + "/query", ("src.main.java." + eventuateQueryPackage).replace(".", separator), "package-info.java", templates.eventuate.rest.openapi.query.packageInfo.template(eventuateQueryPackage));
            transfer(projectPath + "/query", ("src.main.java." + eventuateQueryPackage).replace(".", separator), "BaseQueryService.java", templates.eventuate.rest.openapi.query.queryService.template(eventuateQueryPackage, "BaseQueryService"));

        }

       // eventuate framework follow CQRS pattern design, command side service:
        String commandService = projectPath + "/command-rest-service";

        // eventuate framework follow CQRS pattern design, query side service:
        String queryService = projectPath + "/query-rest-service";

        Any anyModel = (Any)model;
        Any commandModel = anyModel.get("command");
        Any queryModel = anyModel.get("query");

        if (commandModel!=null) {
            processCommandService(commandService, commandModel, config, expose);
        }
        if (queryModel!=null) {
            processQueryService(queryService, queryModel, config, expose);
        }
    }

    public void processCommandService(String commandService, Object model, Any config, String expose) throws IOException {
        boolean enableHttp = config.toBoolean("enableHttp");
        boolean enableHttps = config.toBoolean("enableHttps");
        boolean enableRegistry = config.toBoolean("enableRegistry");
        boolean supportClient = config.toBoolean("supportClient");
        String dockerOrganization = config.toString("dockerOrganization");
        boolean overwriteHandler = config.toBoolean("overwriteHandler");
        boolean overwriteHandlerTest = config.toBoolean("overwriteHandlerTest");
        boolean overwriteModel = config.toBoolean("overwriteModel");
        String rootPackage = config.toString("rootPackage");
        String handlerPackage = config.toString("handlerPackage");
        String version = config.toString("version");

        if(dockerOrganization == null || dockerOrganization.length() == 0) dockerOrganization = "networknt";

        transfer(commandService, "docker", "Dockerfile", templates.eventuate.rest.openapi.commandservice.dockerfile.template(config));
        transfer(commandService, "docker", "Dockerfile-Redhat", templates.eventuate.rest.openapi.commandservice.dockerfileredhat.template(config));
        transfer(commandService, "", "build.sh", templates.eventuate.rest.buildSh.template(dockerOrganization, config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version")));
        transfer(commandService, "", ".gitignore", templates.eventuate.rest.gitignore.template());
        transfer(commandService, "", "README.md", templates.eventuate.rest.openapi.commandservice.README.template());
        transfer(commandService, "", "LICENSE", templates.eventuate.rest.LICENSE.template());
        transfer(commandService, "", ".classpath", templates.eventuate.rest.classpath.template());
        transfer(commandService, "", ".project", templates.eventuate.rest.project.template(config));

        transfer(commandService, "", "pom.xml", templates.eventuate.rest.openapi.commandservice.pom.template(config));

        // config
        transfer(commandService, ("src.main.resources.config").replace(".", separator), "service.yml", templates.eventuate.rest.openapi.commandservice.service.template(config));

        transfer(commandService, ("src.main.resources.config").replace(".", separator), "server.yml", templates.eventuate.rest.server.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version"), enableHttp, "8081", enableHttps, "8441", enableRegistry, version));
        transfer(commandService, ("src.test.resources.config").replace(".", separator), "server.yml", templates.eventuate.rest.server.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version"), enableHttp, "49587", enableHttps, "49588", enableRegistry, version));

        transfer(commandService, ("src.main.resources.config").replace(".", separator), "secret.yml", templates.eventuate.rest.secret.template());
        transfer(commandService, ("src.main.resources.config").replace(".", separator), "security.yml", templates.eventuate.rest.security.template());
        if(supportClient) {
            transfer(commandService, ("src.main.resources.config").replace(".", separator), "client.yml", templates.eventuate.rest.clientYml.template());
        } else {
            transfer(commandService, ("src.test.resources.config").replace(".", separator), "client.yml", templates.eventuate.rest.clientYml.template());
        }
        transfer(commandService, ("src.main.resources.config.oauth").replace(".", separator), "primary.crt", templates.eventuate.rest.primaryCrt.template());
        transfer(commandService, ("src.main.resources.config.oauth").replace(".", separator), "secondary.crt", templates.eventuate.rest.secondaryCrt.template());

        // logging
        transfer(commandService, ("src.main.resources").replace(".", separator), "logback.xml", templates.eventuate.rest.logback.template());
        transfer(commandService, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.eventuate.rest.logback.template());

        if(overwriteModel) {
            generateModule(commandService, model, config);
        }

        injectEndpoints(model);

        List<Map<String, Object>> operationList = getOperationList(model);
        // routing
        transfer(commandService, ("src.main.java." + rootPackage).replace(".", separator), "PathHandlerProvider.java", templates.eventuate.rest.openapi.handlerProvider.template(rootPackage, handlerPackage, operationList, prometheusMetrics));

        // handler
        if(overwriteHandler) {
            for(Map<String, Object> op : operationList){
                String className = op.get("handlerName").toString();
                String example = null;
                if(op.get("example") != null) {
                    //example = mapper.writeValueAsString(op.get("example"));
                    example = JsonStream.serialize(op.get("example"));
                }
                if("ServerInfoGetHandler".equals(className) || "HealthGetHandler".equals(className) || "PrometheusGetHandler".equals(className)) {
                    // don't generate handler for server info and health as they are injected and the impls are in light-4j
                    continue;
                }
                transfer(commandService, ("src.main.java." + handlerPackage).replace(".", separator), className + ".java", templates.eventuate.rest.handler.template(handlerPackage, className, example));
            }
        }

        // handler test cases
        transfer(commandService, ("src.test.java." + handlerPackage + ".").replace(".", separator),  "TestServer.java", templates.eventuate.rest.testServer.template(handlerPackage));
        if(overwriteHandlerTest) {
            for(Map<String, Object> op : operationList){
                transfer(commandService, ("src.test.java." + handlerPackage).replace(".", separator), op.get("handlerName") + "Test.java", templates.eventuate.rest.openapi.handlerTest.template(handlerPackage, op));
            }
        }

        // transfer binary files without touching them.
        if(Files.notExists(Paths.get(commandService, ("src.main.resources.config.tls").replace(".", separator)))) {
            Files.createDirectories(Paths.get(commandService, ("src.main.resources.config.tls").replace(".", separator)));
        }
        try (InputStream is = EventuateOpenApiGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
            Files.copy(is, Paths.get(commandService, ("src.main.resources.config.tls").replace(".", separator), "server.keystore"), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream is = EventuateOpenApiGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
            Files.copy(is, Paths.get(commandService, ("src.main.resources.config.tls").replace(".", separator), "server.truststore"), StandardCopyOption.REPLACE_EXISTING);
        }
        if(supportClient) {
            try (InputStream is = EventuateOpenApiGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Files.copy(is, Paths.get(commandService, ("src.main.resources.config.tls").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = EventuateOpenApiGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Files.copy(is, Paths.get(commandService, ("src.main.resources.config.tls").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            if(Files.notExists(Paths.get(commandService, ("src.test.resources.config.tls").replace(".", separator)))) {
                Files.createDirectories(Paths.get(commandService, ("src.test.resources.config.tls").replace(".", separator)));
            }
            try (InputStream is = EventuateOpenApiGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Files.copy(is, Paths.get(commandService, ("src.test.resources.config.tls").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = EventuateOpenApiGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Files.copy(is, Paths.get(commandService, ("src.test.resources.config.tls").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        JsonStream.serialize(model, new FileOutputStream(FileSystems.getDefault().getPath(commandService, ("src.main.resources.config").replace(".", separator), "openapi.json").toFile()));

    }

    public void processQueryService(String queryService, Object model, Any config, String expose) throws IOException {
        boolean enableHttp = config.toBoolean("enableHttp");
        boolean enableHttps = config.toBoolean("enableHttps");
        boolean enableRegistry = config.toBoolean("enableRegistry");
        boolean supportClient = config.toBoolean("supportClient");
        String dockerOrganization = config.toString("dockerOrganization");
        boolean overwriteHandler = config.toBoolean("overwriteHandler");
        boolean overwriteHandlerTest = config.toBoolean("overwriteHandlerTest");
        boolean overwriteModel = config.toBoolean("overwriteModel");
        String rootPackage = config.toString("rootPackage");
        String handlerPackage = config.toString("handlerPackage");
        String version = config.toString("version");
        
        if(dockerOrganization == null || dockerOrganization.length() == 0) dockerOrganization = "networknt";

        transfer(queryService, "docker", "Dockerfile", templates.eventuate.rest.openapi.queryservice.dockerfile.template(config));
        transfer(queryService, "docker", "Dockerfile-Redhat", templates.eventuate.rest.openapi.queryservice.dockerfileredhat.template(config));
        transfer(queryService, "", "build.sh", templates.eventuate.rest.buildSh.template(dockerOrganization, config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version")));
        transfer(queryService, "", ".gitignore", templates.eventuate.rest.gitignore.template());
        transfer(queryService, "", "README.md", templates.eventuate.rest.openapi.queryservice.README.template());
        transfer(queryService, "", "LICENSE", templates.eventuate.rest.LICENSE.template());
        transfer(queryService, "", ".classpath", templates.eventuate.rest.classpath.template());
        transfer(queryService, "", ".project", templates.eventuate.rest.project.template(config));

        transfer(queryService, "", "pom.xml", templates.eventuate.rest.openapi.queryservice.pom.template(config));

        // config
        transfer(queryService, ("src.main.resources.config").replace(".", separator), "service.yml", templates.eventuate.rest.openapi.queryservice.service.template(config));

        transfer(queryService, ("src.main.resources.config").replace(".", separator), "server.yml", templates.eventuate.rest.server.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version"), enableHttp, "8082", enableHttps, "8442", enableRegistry, version));
        transfer(queryService, ("src.test.resources.config").replace(".", separator), "server.yml", templates.eventuate.rest.server.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version"), enableHttp, "49587", enableHttps, "49588", enableRegistry, version));

        transfer(queryService, ("src.main.resources.config").replace(".", separator), "secret.yml", templates.eventuate.rest.secret.template());
        transfer(queryService, ("src.main.resources.config").replace(".", separator), "security.yml", templates.eventuate.rest.security.template());
        if(supportClient) {
            transfer(queryService, ("src.main.resources.config").replace(".", separator), "client.yml", templates.eventuate.rest.clientYml.template());
        } else {
            transfer(queryService, ("src.test.resources.config").replace(".", separator), "client.yml", templates.eventuate.rest.clientYml.template());
        }
        transfer(queryService, ("src.main.resources.config.oauth").replace(".", separator), "primary.crt", templates.eventuate.rest.primaryCrt.template());
        transfer(queryService, ("src.main.resources.config.oauth").replace(".", separator), "secondary.crt", templates.eventuate.rest.secondaryCrt.template());

        // logging
        transfer(queryService, ("src.main.resources").replace(".", separator), "logback.xml", templates.eventuate.rest.logback.template());
        transfer(queryService, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.eventuate.rest.logback.template());
        // query side event store connection config
        transfer(queryService, ("src.main.resources.config").replace(".", separator), "kafka.yml", templates.eventuate.rest.openapi.queryservice.kafka.template());
        transfer(queryService, ("src.main.resources.config").replace(".", separator), "eventuate-client.yml", templates.eventuate.rest.openapi.queryservice.eventuateClient.template());

        if(overwriteModel) {
            generateModule(queryService, model, config);
        }

        injectEndpoints(model);

        List<Map<String, Object>> operationList = getOperationList(model);
        // routing
        transfer(queryService, ("src.main.java." + rootPackage).replace(".", separator), "PathHandlerProvider.java", templates.eventuate.rest.openapi.handlerProvider.template(rootPackage, handlerPackage, operationList, prometheusMetrics));

        // handler
        if(overwriteHandler) {
            for(Map<String, Object> op : operationList){
                String className = op.get("handlerName").toString();
                String example = null;
                if(op.get("example") != null) {
                    //example = mapper.writeValueAsString(op.get("example"));
                    example = JsonStream.serialize(op.get("example"));
                }
                if("ServerInfoGetHandler".equals(className) || "HealthGetHandler".equals(className) || "PrometheusGetHandler".equals(className)) {
                    // don't generate handler for server info and health as they are injected and the impls are in light-4j
                    continue;
                }
                transfer(queryService, ("src.main.java." + handlerPackage).replace(".", separator), className + ".java", templates.eventuate.rest.handler.template(handlerPackage, className, example));
            }
        }

        // handler test cases
        transfer(queryService, ("src.test.java." + handlerPackage + ".").replace(".", separator),  "TestServer.java", templates.eventuate.rest.testServer.template(handlerPackage));
        if(overwriteHandlerTest) {
            for(Map<String, Object> op : operationList){
                transfer(queryService, ("src.test.java." + handlerPackage).replace(".", separator), op.get("handlerName") + "Test.java", templates.eventuate.rest.openapi.handlerTest.template(handlerPackage, op));
            }
        }

        // transfer binary files without touching them.
        if(Files.notExists(Paths.get(queryService, ("src.main.resources.config.tls").replace(".", separator)))) {
            Files.createDirectories(Paths.get(queryService, ("src.main.resources.config.tls").replace(".", separator)));
        }
        try (InputStream is = EventuateOpenApiGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
            Files.copy(is, Paths.get(queryService, ("src.main.resources.config.tls").replace(".", separator), "server.keystore"), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream is = EventuateOpenApiGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
            Files.copy(is, Paths.get(queryService, ("src.main.resources.config.tls").replace(".", separator), "server.truststore"), StandardCopyOption.REPLACE_EXISTING);
        }
        if(supportClient) {
            try (InputStream is = EventuateOpenApiGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Files.copy(is, Paths.get(queryService, ("src.main.resources.config.tls").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = EventuateOpenApiGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Files.copy(is, Paths.get(queryService, ("src.main.resources.config.tls").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            if(Files.notExists(Paths.get(queryService, ("src.test.resources.config.tls").replace(".", separator)))) {
                Files.createDirectories(Paths.get(queryService, ("src.test.resources.config.tls").replace(".", separator)));
            }
            try (InputStream is = EventuateOpenApiGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Files.copy(is, Paths.get(queryService, ("src.test.resources.config.tls").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = EventuateOpenApiGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Files.copy(is, Paths.get(queryService, ("src.test.resources.config.tls").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        JsonStream.serialize(model, new FileOutputStream(FileSystems.getDefault().getPath(queryService, ("src.main.resources.config").replace(".", separator), "openapi.json").toFile()));

    }

    public void generateModule (String path, Object model, Any config) throws IOException{

        String modelPackage = config.toString("modelPackage");

        Any anyComponents = ((Any)model).get("components");
        if(anyComponents.valueType() != ValueType.INVALID) {
            Any schemas = anyComponents.asMap().get("schemas");
            if(schemas != null && schemas.valueType() != ValueType.INVALID) {
                for(Map.Entry<String, Any> entry : schemas.asMap().entrySet()) {
                    List<Map<String, Any>> props = new ArrayList<>();
                    String key = entry.getKey();
                    Map<String, Any> value = entry.getValue().asMap();
                    String type = null;
                    boolean isEnum = false;
                    Map<String, Any> properties = null;
                    List<Any> required = null;

                    for(Map.Entry<String, Any> entrySchema: value.entrySet()) {
                        if("type".equals(entrySchema.getKey())) {
                            type = entrySchema.getValue().toString();
                            if("enum".equals(type)) isEnum = true;
                        }
                        if("properties".equals(entrySchema.getKey())) {
                            properties = entrySchema.getValue().asMap();
                            // transform properties

                            for(Map.Entry<String, Any> entryProp: properties.entrySet()) {
                                //System.out.println("key = " + entryProp.getKey() + " value = " + entryProp.getValue());
                                Map<String, Any> propMap = new HashMap<>();
                                String name = entryProp.getKey();
                                propMap.put("jsonProperty", Any.wrap(name));
                                if(name.startsWith("@")) {
                                    name = name.substring(1);

                                }
                                propMap.put("name", Any.wrap(name));
                                propMap.put("getter", Any.wrap("get" + name.substring(0, 1).toUpperCase() + name.substring(1)));
                                propMap.put("setter", Any.wrap("set" + name.substring(0, 1).toUpperCase() + name.substring(1)));
                                // assume it is not enum unless it is overwritten
                                propMap.put("isEnum", Any.wrap(false));

                                boolean isArray = false;
                                for(Map.Entry<String, Any> entryElement: entryProp.getValue().asMap().entrySet()) {
                                    //System.out.println("key = " + entryElement.getKey() + " value = " + entryElement.getValue());

                                    if("type".equals(entryElement.getKey())) {
                                        String t = typeMapping.get(entryElement.getValue().toString());
                                        if("java.util.List".equals(t)) {
                                            isArray = true;
                                        } else {
                                            propMap.put("type", Any.wrap(t));
                                        }
                                    }
                                    if("items".equals(entryElement.getKey())) {
                                        Any a = entryElement.getValue();
                                        if(a.get("$ref").valueType() != ValueType.INVALID && isArray) {
                                            String s = a.get("$ref").toString();
                                            s = s.substring(s.lastIndexOf('/') + 1);
                                            propMap.put("type", Any.wrap("java.util.List<" + s + ">"));
                                        }
                                        if(a.get("type").valueType() != ValueType.INVALID && isArray) {
                                            propMap.put("type", Any.wrap("java.util.List<" + typeMapping.get(a.get("type").toString()) + ">"));
                                        }
                                    }
                                    if("$ref".equals(entryElement.getKey())) {
                                        String s = entryElement.getValue().toString();
                                        s = s.substring(s.lastIndexOf('/') + 1);
                                        propMap.put("type", Any.wrap(s));
                                    }
                                    if("default".equals(entryElement.getKey())) {
                                        Any a = entryElement.getValue();
                                        propMap.put("default", a);
                                    }
                                    if("enum".equals(entryElement.getKey())) {
                                        propMap.put("isEnum", Any.wrap(true));
                                        propMap.put("nameWithEnum", Any.wrap(name.substring(0, 1).toUpperCase() + name.substring(1) + "Enum"));
                                        propMap.put("value", Any.wrap(entryElement.getValue()));
                                    }
                                    if("format".equals(entryElement.getKey())) {
                                        String s = entryElement.getValue().toString();
                                        if("date-time".equals(s)) {
                                            propMap.put("type", Any.wrap("java.time.LocalDateTime"));
                                        }
                                        if("date".equals(s)) {
                                            propMap.put("type", Any.wrap("java.time.LocalDate"));
                                        }
                                        if("double".equals(s)) {
                                            propMap.put("type", Any.wrap(s));
                                        }
                                        if("float".equals(s)) {
                                            propMap.put("type", Any.wrap(s));
                                        }
                                    }
                                    if("oneOf".equals(entryElement.getKey())) {
                                        List<Any> list = entryElement.getValue().asList();
                                        String t = list.get(0).asMap().get("type").toString();
                                        if(t != null) {
                                            propMap.put("type", Any.wrap(typeMapping.get(t)));
                                        } else {
                                            // maybe reference? default type to object.
                                            propMap.put("type", Any.wrap("Object"));
                                        }
                                    }
                                    if("anyOf".equals(entryElement.getKey())) {
                                        List<Any> list = entryElement.getValue().asList();
                                        String t = list.get(0).asMap().get("type").toString();
                                        if(t != null) {
                                            propMap.put("type", Any.wrap(typeMapping.get(t)));
                                        } else {
                                            // maybe reference? default type to object.
                                            propMap.put("type", Any.wrap("Object"));
                                        }
                                    }
                                    if("allOf".equals(entryElement.getKey())) {
                                        List<Any> list = entryElement.getValue().asList();
                                        String t = list.get(0).asMap().get("type").toString();
                                        if(t != null) {
                                            propMap.put("type", Any.wrap(typeMapping.get(t)));
                                        } else {
                                            // maybe reference? default type to object.
                                            propMap.put("type", Any.wrap("Object"));
                                        }
                                    }
                                    if("not".equals(entryElement.getKey())) {
                                        Map<String, Any> m = entryElement.getValue().asMap();
                                        Any t = m.get("type");
                                        if(t != null) {
                                            propMap.put("type", t);
                                        } else {
                                            propMap.put("type", Any.wrap("Object"));
                                        }
                                    }
                                }
                                props.add(propMap);
                            }
                        }
                        if("required".equals(entrySchema.getKey())) {
                            required = entrySchema.getValue().asList();
                        }
                    }
                    String classVarName = key;
                    String modelFileName = key.substring(0, 1).toUpperCase() + key.substring(1);
                    //System.out.println("props = " + Any.wrap(props));
                    transfer(path, ("src.main.java." + modelPackage).replace(".", separator), modelFileName + ".java", templates.eventuate.rest.pojo.template(modelPackage, modelFileName, classVarName,  props));
                }
            }
        }

    }



    public void injectEndpoints(Object model) {
        Any anyModel = (Any)model;

        Map<String, Any> paths = anyModel.get("paths").asMap();
        Any components = anyModel.get("components");

        // inject scope server.info.r
        String authName = null;
        if(components != null) {
            Map<String, Any> cpMap = components.asMap();
            Map<String, Any> ssMap = cpMap.get("securitySchemes").asMap();
            for(String name : ssMap.keySet()) {
                Map<String, Any> def = ssMap.get(name).asMap();
                if(def != null && "oauth2".equals(def.get("type").toString())) {
                    authName = name;
                    Map<String, Any> flows = def.get("flows").asMap();
                    for(Map.Entry<String, Any> entry: flows.entrySet()) {
                        Map<String, Any> oauthMap = entry.getValue().asMap();
                        if(oauthMap != null) {
                            Any scopes = oauthMap.get("scopes");
                            if(scopes != null) {
                                scopes.asMap().put("server.info.r", Any.wrap("read server info"));
                            }
                        }
                    }
                }
            }
        }
        // inject server info endpoint
        Map<String, Object> serverInfoMap = new HashMap<>();

        List<String> scopes = new ArrayList<>();
        scopes.add("server.info.r");
        Map<String, List> authMap = new HashMap<>();
        authMap.put(authName, scopes);
        List<Map<String, List>> authList = new ArrayList<>();
        authList.add(authMap);
        serverInfoMap.put("security", authList);

        Map<String, Object> descMap = new HashMap<>();
        descMap.put("description", "successful operation");
        Map<String, Object> codeMap = new HashMap<>();
        codeMap.put("200", descMap);
        serverInfoMap.put("responses", codeMap);
        serverInfoMap.put("parameters", new ArrayList());

        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("get", serverInfoMap);
        paths.put("/server/info", Any.wrap(serverInfo));

        // inject health check endpoint
        Map<String, Object> healthMap = new HashMap<>();
        healthMap.put("responses", codeMap);
        healthMap.put("parameters", new ArrayList());

        Map<String, Object> health = new HashMap<>();
        health.put("get", healthMap);
        paths.put("/health", Any.wrap(health));

        if (prometheusMetrics) {
            // inject prometheus metrics collect endpoint
            Map<String, Object> prometheusMap = new HashMap<>();
            prometheusMap.put("responses", codeMap);
            prometheusMap.put("parameters", new ArrayList());

            Map<String, Object> prometheus = new HashMap<>();
            prometheus.put("get", prometheusMap);
            paths.put("/prometheus", Any.wrap(prometheus));
        }

    }

    public List<Map<String, Object>> getOperationList(Object model) {
        List<Map<String, Object>> result = new ArrayList<>();
        String s = ((Any)model).toString();
        OpenApi3 openApi3 = null;
        try {
            openApi3 = (OpenApi3) new OpenApiParser().parse(s, new URL("https://oas.lightapi.net/"));
        } catch (MalformedURLException e) {
        }
        String basePath = getBasePath(openApi3);

        Map<String, Path> paths = openApi3.getPaths();
        for(Map.Entry<String, Path> entryPath: paths.entrySet()) {
            String path = entryPath.getKey();
            Path pathValue = entryPath.getValue();
            for(Map.Entry<String, Operation> entryOps: pathValue.getOperations().entrySet()) {
                // skip all the entries that are not http method. The only possible entries
                // here are extensions. which will be just a key value pair.
                if(entryOps.getKey().startsWith("x-")) continue;
                Map<String, Object> flattened = new HashMap<>();
                flattened.put("method", entryOps.getKey().toUpperCase());
                flattened.put("capMethod", entryOps.getKey().substring(0, 1).toUpperCase() + entryOps.getKey().substring(1));
                flattened.put("path", basePath + path);
                String normalizedPath = path.replace("{", "").replace("}", "");
                flattened.put("normalizedPath", basePath + normalizedPath);
                flattened.put("handlerName", Utils.camelize(normalizedPath) + Utils.camelize(entryOps.getKey()) + "Handler");
                Operation operation = entryOps.getValue();
                Response response = operation.getResponse("200");
                if(response != null) {
                    MediaType mediaType = response.getContentMediaType("application/json");
                    if(mediaType != null) {
                        // first check if there is a single example defined.
                        Object example = mediaType.getExample();
                        if(example != null) {
                            flattened.put("example", example);
                        } else {
                            // check if there are multiple examples
                            Map<String, Example> exampleMap = mediaType.getExamples();
                            // use the first example if there are multiple
                            if(exampleMap.size() > 0) {
                                Map.Entry<String,Example> entry = exampleMap.entrySet().iterator().next();
                                Example e = entry.getValue();
                                if(e != null) {
                                    flattened.put("example", e.getValue());
                                }
                            }
                        }
                    }
                }
                result.add(flattened);
            }
        }
        return result;
    }

    private static String getBasePath(OpenApi3 openApi3) {
        String basePath = "";
        Server server = openApi3.getServer(0);
        String url = server.getUrl();
        if(url != null) {
            // find "://" index
            int protocolIndex = url.indexOf("://");
            int pathIndex = url.indexOf('/', protocolIndex + 3);
            if(pathIndex > 0) {
                basePath = url.substring(pathIndex);
            }
        }
        return basePath;
    }

}
