package com.networknt.codegen.rest;

import com.fasterxml.jackson.databind.JsonNode;
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

/**
 * The input for OpenAPI 3.0 generator include config with json format
 * and OpenAPI specification in yaml format.
 *
 * The model is OpenAPI spec in yaml format. And config file is config.json
 * in JSON format.
 *
 * @author Steve Hu
 */
public class OpenApiGenerator implements Generator {
    private Map<String, String> typeMapping = new HashMap<>();

    public OpenApiGenerator() {
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
        return "openapi";
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
        // whoever is calling this needs to make sure that model is converted to Map<String, Object>
        String rootPackage = config.toString("rootPackage");
        String modelPackage = config.toString("modelPackage");
        String handlerPackage = config.toString("handlerPackage");
        boolean overwriteHandler = config.toBoolean("overwriteHandler");
        boolean overwriteHandlerTest = config.toBoolean("overwriteHandlerTest");
        boolean overwriteModel = config.toBoolean("overwriteModel");
        boolean enableHttp = config.toBoolean("enableHttp");
        String httpPort = config.toString("httpPort");
        boolean enableHttps = config.toBoolean("enableHttps");
        String httpsPort = config.toString("httpsPort");
        boolean enableRegistry = config.toBoolean("enableRegistry");
        boolean supportClient = config.toBoolean("supportClient");

        transfer(targetPath, "", "pom.xml", templates.rest.openapi.pom.template(config));
        // There is only one port that should be exposed in Dockerfile, otherwise, the service
        // discovery will be so confused. If https is enabled, expose the https port. Otherwise http port.
        String expose = "";
        if(enableHttps) {
            expose = httpsPort;
        } else {
            expose = httpPort;
        }
        transfer(targetPath, "", "Dockerfile", templates.rest.dockerfile.template(config, expose));
        transfer(targetPath, "", ".gitignore", templates.rest.gitignore.template());
        transfer(targetPath, "", "README.md", templates.rest.README.template());
        transfer(targetPath, "", "LICENSE", templates.rest.LICENSE.template());
        transfer(targetPath, "", ".classpath", templates.rest.classpath.template());
        transfer(targetPath, "", ".project", templates.rest.project.template(config));

        // config
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "service.yml", templates.rest.openapi.service.template(config));

        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "server.yml", templates.rest.server.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version"), enableHttp, httpPort, enableHttps, httpsPort, enableRegistry));
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml", templates.rest.server.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version"), enableHttp, "49587", enableHttps, "49588", enableRegistry));

        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "secret.yml", templates.rest.secret.template());
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "security.yml", templates.rest.security.template());
        if(supportClient) {
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "client.yml", templates.rest.clientYml.template());
        } else {
            transfer(targetPath, ("src.test.resources.config").replace(".", separator), "client.yml", templates.rest.clientYml.template());
        }

        transfer(targetPath, ("src.main.resources.config.oauth").replace(".", separator), "primary.crt", templates.rest.primaryCrt.template());
        transfer(targetPath, ("src.main.resources.config.oauth").replace(".", separator), "secondary.crt", templates.rest.secondaryCrt.template());

        // logging
        transfer(targetPath, ("src.main.resources").replace(".", separator), "logback.xml", templates.rest.logback.template());
        transfer(targetPath, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.rest.logback.template());

        // preprocess the openapi.yaml to inject health check and server info endpoints
        injectEndpoints(model);

        List<Map<String, Object>> operationList = getOperationList(model);
        // routing
        transfer(targetPath, ("src.main.java." + rootPackage).replace(".", separator), "PathHandlerProvider.java", templates.rest.openapi.handlerProvider.template(rootPackage, handlerPackage, operationList));


        // model
        if(overwriteModel) {
            Any anyComponents = ((Any)model).get("components");
            if(anyComponents.valueType() != ValueType.INVALID) {
                Any schemas = anyComponents.asMap().get("schemas");
                if(schemas.valueType() != ValueType.INVALID) {
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
                        transfer(targetPath, ("src.main.java." + modelPackage).replace(".", separator), modelFileName + ".java", templates.rest.pojo.template(modelPackage, modelFileName, classVarName,  props));
                    }
                }
            }
        }

        // handler
        if(overwriteHandler) {
            for(Map<String, Object> op : operationList){
                String className = op.get("handlerName").toString();
                String example = null;
                if(op.get("example") != null) {
                    //example = mapper.writeValueAsString(op.get("example"));
                    example = JsonStream.serialize(op.get("example"));
                }
                if("ServerInfoGetHandler".equals(className) || "HealthGetHandler".equals(className)) {
                    // don't generate handler for server info and health as they are injected and the impls are in light-4j
                    continue;
                }
                transfer(targetPath, ("src.main.java." + handlerPackage).replace(".", separator), className + ".java", templates.rest.handler.template(handlerPackage, className, example));
            }
        }

        // handler test cases
        transfer(targetPath, ("src.test.java." + handlerPackage + ".").replace(".", separator),  "TestServer.java", templates.rest.testServer.template(handlerPackage));
        if(overwriteHandlerTest) {
            for(Map<String, Object> op : operationList){
                transfer(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), op.get("handlerName") + "Test.java", templates.rest.openapi.handlerTest.template(handlerPackage, op));
            }
        }

        // transfer binary files without touching them.
        if(Files.notExists(Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator)))) {
            Files.createDirectories(Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator)));
        }
        try (InputStream is = SwaggerGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
            Files.copy(is, Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator), "server.keystore"), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream is = SwaggerGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
            Files.copy(is, Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator), "server.truststore"), StandardCopyOption.REPLACE_EXISTING);
        }
        if(supportClient) {
            try (InputStream is = SwaggerGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Files.copy(is, Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = SwaggerGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Files.copy(is, Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            if(Files.notExists(Paths.get(targetPath, ("src.test.resources.config.tls").replace(".", separator)))) {
                Files.createDirectories(Paths.get(targetPath, ("src.test.resources.config.tls").replace(".", separator)));
            }
            try (InputStream is = SwaggerGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Files.copy(is, Paths.get(targetPath, ("src.test.resources.config.tls").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = SwaggerGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Files.copy(is, Paths.get(targetPath, ("src.test.resources.config.tls").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        JsonStream.serialize(model, new FileOutputStream(FileSystems.getDefault().getPath(targetPath, ("src.main.resources.config").replace(".", separator), "openapi.json").toFile()));
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
                    Map<String, Any> ccMap = flows.get("clientCredentials").asMap();
                    Any scopes = ccMap.get("scopes");
                    if(scopes != null) {
                        scopes.asMap().put("server.info.r", Any.wrap("read server info"));
                    }
                    break;
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
                        Object example = mediaType.getExample();
                        if(example != null) {
                            flattened.put("example", example);
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
