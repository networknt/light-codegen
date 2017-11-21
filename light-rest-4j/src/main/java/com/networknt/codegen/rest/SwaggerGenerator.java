package com.networknt.codegen.rest;

import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import com.networknt.codegen.Generator;
import com.networknt.codegen.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * The input for rest generator include config with json format
 * and swagger specification in json format.
 *
 * The module is swagger spec in json format. And it will be used
 * as Map like config. It is not necessary to parse it to POJO with
 * swagger-parser for code generator.
 *
 * Created by stevehu on 2017-04-23.
 */
public class SwaggerGenerator implements Generator {
    //static ObjectMapper mapper = new ObjectMapper();

    private Map<String, String> typeMapping = new HashMap<>();

    public SwaggerGenerator() {
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
        return "swagger";
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
        boolean supportOracle = config.toBoolean("supportOracle");
        boolean supportMysql  = config.toBoolean("supportMysql");
        boolean supportPostgresql = config.toBoolean("supportPostgresql");
        boolean supportH2ForTest  = config.toBoolean("supportH2ForTest");
        boolean supportClient = config.toBoolean("supportClient");

        transfer(targetPath, "", "pom.xml", templates.rest.swagger.pom.template(config));
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
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "service.yml", templates.rest.swagger.service.template(config));

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

        // preprocess the swagger.json to inject health check and server info endpoints
        injectEndpoints(model);

        List<Map<String, Any>> operationList = getOperationList(model);
        // routing
        transfer(targetPath, ("src.main.java." + rootPackage).replace(".", separator), "PathHandlerProvider.java", templates.rest.swagger.handlerProvider.template(rootPackage, handlerPackage, operationList));


        // model
        if(overwriteModel) {
            Any any = ((Any)model).get("definitions");
            if(any.valueType() != ValueType.INVALID) {
                for(Map.Entry<String, Any> entry : any.asMap().entrySet()) {
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




        // TODO implement model generation based on this object.
        //List<Map<String, Any>> modelList = getPojoList(model);

        // handler
        if(overwriteHandler) {
            for(Map<String, Any> op : operationList){
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
            for(Map<String, Any> op : operationList){
                transfer(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), op.get("handlerName") + "Test.java", templates.rest.swagger.handlerTest.template(handlerPackage, op));
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

        JsonStream.serialize(model, new FileOutputStream(FileSystems.getDefault().getPath(targetPath, ("src.main.resources.config").replace(".", separator), "swagger.json").toFile()));
    }

    public void injectHealthCheck(Map<String, Any> paths) {

    }

    public void injectEndpoints(Object model) {
        Any anyModel = (Any)model;
        Map<String, Any> paths = anyModel.get("paths").asMap();
        Any securityDefinitions = anyModel.get("securityDefinitions");

        // inject scope server.info.r
        String authName = null;
        if(securityDefinitions != null) {
            Map<String, Any> sdMap = securityDefinitions.asMap();
            for(String name : sdMap.keySet()) {
                Map<String, Any> def = sdMap.get(name).asMap();
                if(def != null && "oauth2".equals(def.get("type").toString())) {
                    authName = name;
                    Any scopes = def.get("scopes");
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

    public List<Map<String, Any>> getOperationList(Object model) {
        List<Map<String, Any>> result = new ArrayList<>();
        Any anyModel = (Any)model;
        String basePath = anyModel.get("basePath").toString();
        Map<String, Any> paths = anyModel.get("paths").asMap();

        for(Map.Entry<String, Any> entryPath: paths.entrySet()) {
            String path = entryPath.getKey();
            Map<String, Any> pathValues = entryPath.getValue().asMap();
            for(Map.Entry<String, Any> entryOps: pathValues.entrySet()) {
                // skip all the entries that are not http method. The only possible entries
                // here are extensions. which will be just a key value pair.
                if(entryOps.getKey().startsWith("x-")) continue;
                Map<String, Any> flattened = new HashMap<>();
                flattened.put("method", Any.wrap(entryOps.getKey().toUpperCase()));
                flattened.put("capMethod", Any.wrap(entryOps.getKey().substring(0, 1).toUpperCase() + entryOps.getKey().substring(1)));
                flattened.put("path", Any.wrap(basePath + path));
                String normalizedPath = path.replace("{", "").replace("}", "");
                flattened.put("normalizedPath", Any.wrap(basePath + normalizedPath));
                flattened.put("handlerName", Any.wrap(Utils.camelize(normalizedPath) + Utils.camelize(entryOps.getKey()) + "Handler"));
                Map<String, Any> values = entryOps.getValue().asMap();
                Any responses = values.get("responses");
                if(responses != null) {
                    Any response = responses.asMap().get("200");
                    if(response != null) {
                        Any examples = response.asMap().get("examples");
                        if(examples != null) {
                            Any jsonRes = examples.asMap().get("application/json");
                            flattened.put("example", jsonRes);
                        }
                    }
                }
                result.add(flattened);
            }
        }
        return result;
    }
}
