package com.networknt.codegen.rest;

import com.jsoniter.JsonIterator;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import com.networknt.codegen.Generator;
import com.networknt.codegen.Utils;
import com.networknt.jsonoverlay.Overlay;
import com.networknt.oas.OpenApiParser;
import com.networknt.oas.model.*;
import com.networknt.oas.model.impl.OpenApi3Impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

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
    boolean prometheusMetrics =false;
    boolean skipHealthCheck = false;
    boolean skipServerInfo = false;
    boolean specChangeCodeReGenOnly = false;

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
        String dockerOrganization = config.toString("dockerOrganization");
        prometheusMetrics = config.toBoolean("prometheusMetrics");
        skipHealthCheck = config.toBoolean("skipHealthCheck");
        skipServerInfo = config.toBoolean("skipServerInfo");
        specChangeCodeReGenOnly = config.toBoolean("specChangeCodeReGenOnly");

        String version = config.toString("version");
        String serviceId = config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version");

        if(dockerOrganization == null || dockerOrganization.length() == 0) dockerOrganization = "networknt";

        if (!specChangeCodeReGenOnly) {
            transfer(targetPath, "", "pom.xml", templates.rest.openapi.pom.template(config));
            // There is only one port that should be exposed in Dockerfile, otherwise, the service
            // discovery will be so confused. If https is enabled, expose the https port. Otherwise http port.
            String expose = "";
            if(enableHttps) {
                expose = httpsPort;
            } else {
                expose = httpPort;
            }

            transfer(targetPath, "docker", "Dockerfile", templates.rest.dockerfile.template(config, expose));
            transfer(targetPath, "docker", "Dockerfile-Redhat", templates.rest.dockerfileredhat.template(config, expose));
            transfer(targetPath, "", "build.sh", templates.rest.buildSh.template(dockerOrganization, serviceId));
            transfer(targetPath, "", ".gitignore", templates.rest.gitignore.template());
            transfer(targetPath, "", "README.md", templates.rest.README.template());
            transfer(targetPath, "", "LICENSE", templates.rest.LICENSE.template());
            transfer(targetPath, "", ".classpath", templates.rest.classpath.template());
            transfer(targetPath, "", ".project", templates.rest.project.template(config));

            // config
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "service.yml", templates.rest.openapi.service.template(config));

            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "server.yml", templates.rest.server.template(serviceId, enableHttp, httpPort, enableHttps, httpsPort, enableRegistry, version));
            transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml", templates.rest.server.template(serviceId, enableHttp, "49587", enableHttps, "49588", enableRegistry, version));

            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "secret.yml", templates.rest.secret.template());
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "openapi-security.yml", templates.rest.openapiSecurity.template());
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "openapi-validator.yml", templates.rest.openapiValidator.template());
            if(supportClient) {
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "client.yml", templates.rest.clientYml.template());
            } else {
                transfer(targetPath, ("src.test.resources.config").replace(".", separator), "client.yml", templates.rest.clientYml.template());
            }

            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "primary.crt", templates.rest.primaryCrt.template());
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "secondary.crt", templates.rest.secondaryCrt.template());

            // logging
            transfer(targetPath, ("src.main.resources").replace(".", separator), "logback.xml", templates.rest.logback.template());
            transfer(targetPath, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.rest.logback.template());
        }

        List<Map<String, Object>> operationList = getOperationList(model);
        // routing
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "handler.yml", templates.rest.openapi.handlerYml.template(serviceId, handlerPackage, operationList, prometheusMetrics, !skipHealthCheck, !skipServerInfo));

        // model
        Any anyComponents;
        if(model instanceof Any) {
            anyComponents = ((Any)model).get("components");
        } else if(model instanceof String){
            // this must be yaml format and we need to convert to json for JsonIterator.
            OpenApi3 openApi3 = null;
            try {
                openApi3 = (OpenApi3) new OpenApiParser().parse((String)model, new URL("https://oas.lightapi.net/"));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Failed to parse the model", e);
            }
            anyComponents = JsonIterator.deserialize(Overlay.toJson((OpenApi3Impl)openApi3).toString()).get("components");
        } else {
            throw new RuntimeException("Invalid Model Class: " + model.getClass());
        }
        if(anyComponents.valueType() != ValueType.INVALID) {
            Any schemas = anyComponents.asMap().get("schemas");
            if(schemas != null && schemas.valueType() != ValueType.INVALID) {
                for(Map.Entry<String, Any> entry : schemas.asMap().entrySet()) {
                    List<Map<String, Any>> props = new ArrayList<>();
                    String key = entry.getKey();
                    Map<String, Any> value = entry.getValue().asMap();
                    String type = null;
                    String enums = null;
                    boolean isEnum = false;
                    boolean isEnumClass = false;
                    Map<String, Any> properties = null;
                    List<Any> required = null;

                    for(Map.Entry<String, Any> entrySchema: value.entrySet()) {
                        if("type".equals(entrySchema.getKey())) {
                            type = entrySchema.getValue().toString();
                            if("enum".equals(type)) isEnum = true;
                        }
                        if("enum".equals(entrySchema.getKey())) {
                            isEnumClass = true;
                            enums = entrySchema.getValue().asList().toString();
                            enums = enums.substring(enums.indexOf("[") + 1, enums.indexOf("]"));
                        }
                        if("properties".equals(entrySchema.getKey())) {
                            properties = entrySchema.getValue().asMap();
                            // transform properties

                            for(Map.Entry<String, Any> entryProp: properties.entrySet()) {
                                //System.out.println("key = " + entryProp.getKey() + " value = " + entryProp.getValue());
                                Map<String, Any> propMap = new HashMap<>();
                                String name = entryProp.getKey();
                                name = name.trim().replaceAll(" ", "_");
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
                                            propMap.putIfAbsent("type", Any.wrap(t));
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
                                        this.addUnderscores(entryElement);
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
                                            propMap.put("type", Any.wrap("java.lang.Double"));
                                        }
                                        if("float".equals(s)) {
                                            propMap.put("type", Any.wrap("java.lang.Float"));
                                        }
                                        if("int64".equals(s)){
                                            propMap.put("type", Any.wrap("java.lang.Long"));
                                        }
                                    }
                                    if("oneOf".equals(entryElement.getKey())) {
                                        List<Any> list = entryElement.getValue().asList();
                                        Any t = list.get(0).asMap().get("type");
                                        if(t != null) {
                                            propMap.put("type", Any.wrap(typeMapping.get(t.toString())));
                                        } else {
                                            // maybe reference? default type to object.
                                            propMap.put("type", Any.wrap("Object"));
                                        }
                                    }
                                    if("anyOf".equals(entryElement.getKey())) {
                                        List<Any> list = entryElement.getValue().asList();
                                        Any t = list.get(0).asMap().get("type");
                                        if(t != null) {
                                            propMap.put("type", Any.wrap(typeMapping.get(t.toString())));
                                        } else {
                                            // maybe reference? default type to object.
                                            propMap.put("type", Any.wrap("Object"));
                                        }
                                    }
                                    if("allOf".equals(entryElement.getKey())) {
                                        List<Any> list = entryElement.getValue().asList();
                                        Any t = list.get(0).asMap().get("type");
                                        if(t != null) {
                                            propMap.put("type", Any.wrap(typeMapping.get(t.toString())));
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
                    if(!overwriteModel && checkExist(targetPath, ("src.main.java." + modelPackage).replace(".", separator), modelFileName + ".java")) {
                        continue;
                    }
                    if (isEnumClass) {
                        transfer(targetPath, ("src.main.java." + modelPackage).replace(".", separator), modelFileName + ".java", templates.rest.enumClass.template(modelPackage, modelFileName, enums));
                        continue;
                    }
                    transfer(targetPath, ("src.main.java." + modelPackage).replace(".", separator), modelFileName + ".java", templates.rest.pojo.template(modelPackage, modelFileName, classVarName,  props));
                }
            }
        }

        // handler
        for(Map<String, Object> op : operationList){
            String className = op.get("handlerName").toString();
            String example = null;
            if(op.get("example") != null) {
                //example = mapper.writeValueAsString(op.get("example"));
                example = JsonStream.serialize(op.get("example"));
            }
            if(checkExist(targetPath, ("src.main.java." + handlerPackage).replace(".", separator), className + ".java") && !overwriteHandler) {
                continue;
            }
            transfer(targetPath, ("src.main.java." + handlerPackage).replace(".", separator), className + ".java", templates.rest.handler.template(handlerPackage, className, example));
        }

        // handler test cases
        transfer(targetPath, ("src.test.java." + handlerPackage + ".").replace(".", separator),  "TestServer.java", templates.rest.testServer.template(handlerPackage));

        for(Map<String, Object> op : operationList){
            if(checkExist(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), op.get("handlerName") + "Test.java") && !overwriteHandlerTest) {
                continue;
            }
            transfer(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), op.get("handlerName") + "Test.java", templates.rest.openapi.handlerTest.template(handlerPackage, op));
        }

        // transfer binary files without touching them.
        try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
            Files.copy(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "server.keystore"), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
            Files.copy(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "server.truststore"), StandardCopyOption.REPLACE_EXISTING);
        }
        if(supportClient) {
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Files.copy(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Files.copy(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Files.copy(is, Paths.get(targetPath, ("src.test.resources.config").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Files.copy(is, Paths.get(targetPath, ("src.test.resources.config").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        if(model instanceof Any) {
            try (InputStream is = new ByteArrayInputStream(model.toString().getBytes(StandardCharsets.UTF_8))) {
                Files.copy(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "openapi.json"), StandardCopyOption.REPLACE_EXISTING);
            }
        } else if(model instanceof String){
            try (InputStream is = new ByteArrayInputStream(((String)model).getBytes(StandardCharsets.UTF_8))) {
                Files.copy(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "openapi.yaml"), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public List<Map<String, Object>> getOperationList(Object model) {
        List<Map<String, Object>> result = new ArrayList<>();
        String s;
        if(model instanceof Any) {
            s = ((Any)model).toString();
        } else if(model instanceof String){
            s = (String)model;
        } else {
            throw new RuntimeException("Invalid Model Class: " + model.getClass());
        }
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
        String url = null;
        if (openApi3.getServers().size() > 0) {
            Server server = openApi3.getServer(0);
            url = server.getUrl();
        }
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

    private static void addUnderscores(Map.Entry<String, Any> entryElement) {
        Iterator<Any> iterator = entryElement.getValue().iterator();
        List<Any> list = new ArrayList<>();
        while (iterator.hasNext()) {
            Any any = iterator.next();
            String value = any.toString().trim().replaceAll(" ", "_");
            list.add(Any.wrap(value));
        }
        entryElement.setValue(Any.wrap(list));
    }
}
