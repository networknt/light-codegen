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
import java.util.stream.Collectors;

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
public class OpenApiKotlinGenerator implements Generator {
    private Map<String, String> typeMapping = new HashMap<>();

    // optional generation parameters. if not set, they use default values as
    boolean prometheusMetrics =false;
    boolean skipHealthCheck = false;
    boolean skipServerInfo = false;
    boolean regenerateCodeOnly = false;
    boolean enableParamDescription = true;
    boolean generateModelOnly = false;
    boolean generateValuesYml = false;

    public OpenApiKotlinGenerator() {
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
        typeMapping.put("object", "Any");
        typeMapping.put("integer", "Int");
        typeMapping.put("ByteArray", "byte[]");
        typeMapping.put("binary", "byte[]");
    }

    @Override
    public String getFramework() {
        return "openapikotlin";
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
        String rootPackage = config.toString("rootPackage").trim();
        String modelPackage = config.toString("modelPackage").trim();
        String handlerPackage = config.toString("handlerPackage").trim();

        boolean overwriteHandler = config.toBoolean("overwriteHandler");
        boolean overwriteHandlerTest = config.toBoolean("overwriteHandlerTest");
        boolean overwriteModel = config.toBoolean("overwriteModel");
        generateModelOnly = config.toBoolean("generateModelOnly");

        boolean enableHttp = config.toBoolean("enableHttp");
        String httpPort = config.toString("httpPort").trim();
        boolean enableHttps = config.toBoolean("enableHttps");
        String httpsPort = config.toString("httpsPort").trim();
        boolean enableHttp2 = config.toBoolean("enableHttp2");
        boolean enableRegistry = config.toBoolean("enableRegistry");
        boolean supportClient = config.toBoolean("supportClient");
        String dockerOrganization = config.toString("dockerOrganization").trim();

        prometheusMetrics = config.toBoolean("prometheusMetrics");
        skipHealthCheck = config.toBoolean("skipHealthCheck");
        skipServerInfo = config.toBoolean("skipServerInfo");
        regenerateCodeOnly = config.toBoolean("specChangeCodeReGenOnly");
        enableParamDescription = config.toBoolean("enableParamDescription");
        String version = config.toString("version").trim();
        String serviceId = config.get("groupId").toString().trim() + "." + config.get("artifactId").toString().trim() + "-" + config.get("version").toString().trim();

        if(dockerOrganization == null || dockerOrganization.length() == 0) dockerOrganization = "networknt";

        // get the list of operations for this model
        List<Map<String, Object>> operationList = getOperationList(model);

        // bypass project generation if the mode is the only one requested to be built
        if(!generateModelOnly) {
            // if set to true, regenerate the code only (handlers, model and the handler.yml, potentially affected by operation changes
            if (!regenerateCodeOnly) {
                // generate configurations, project, masks, certs, etc
                // There is only one port that should be exposed in Dockerfile, otherwise, the service
                // discovery will be so confused. If https is enabled, expose the https port. Otherwise http port.
                String expose = "";
                if(enableHttps) {
                    expose = httpsPort;
                } else {
                    expose = httpPort;
                }

                transfer(targetPath, "docker", "Dockerfile", templates.restkotlin.dockerfile.template(config, expose));
                transfer(targetPath, "docker", "Dockerfile-Slim", templates.restkotlin.dockerfileslim.template(config, expose));
                transfer(targetPath, "", "build.sh", templates.restkotlin.buildSh.template(dockerOrganization, serviceId));
                transfer(targetPath, "", ".gitignore", templates.restkotlin.gitignore.template());
                transfer(targetPath, "", "README.md", templates.restkotlin.README.template());
                transfer(targetPath, "", "LICENSE", templates.restkotlin.LICENSE.template());
                transfer(targetPath, "", "build.gradle.kts", templates.restkotlin.buildGradleKts.template(config));
                transfer(targetPath, "", "gradle.properties", templates.restkotlin.gradleProperties.template(config));
                transfer(targetPath, "", "settings.gradle.kts", templates.restkotlin.settingsGradleKts.template(config));

                transferGradle(targetPath);

                // config
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "service.yml", templates.restkotlin.openapi.service.template(config));

                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "server.yml", templates.restkotlin.server.template(serviceId, enableHttp, httpPort, enableHttps, httpsPort, enableHttp2, enableRegistry, version));
                transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml", templates.restkotlin.server.template(serviceId, enableHttp, "49587", enableHttps, "49588", enableHttp2, enableRegistry, version));

                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "openapi-security.yml", templates.restkotlin.openapiSecurity.template());
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "openapi-validator.yml", templates.restkotlin.openapiValidator.template());
                if(supportClient) {
                    transfer(targetPath, ("src.main.resources.config").replace(".", separator), "client.yml", templates.restkotlin.clientYml.template());
                } else {
                    transfer(targetPath, ("src.test.resources.config").replace(".", separator), "client.yml", templates.restkotlin.clientYml.template());
                }

                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "primary.crt", templates.restkotlin.primaryCrt.template());
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "secondary.crt", templates.restkotlin.secondaryCrt.template());

                // mask
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "mask.yml", templates.restkotlin.maskYml.template());
                // logging
                transfer(targetPath, ("src.main.resources").replace(".", separator), "logback.xml", templates.restkotlin.logback.template());
                transfer(targetPath, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.restkotlin.logback.template());
                transfer(targetPath, ("src.test.resources").replace(".", separator), "junit-platform.properties", templates.restkotlin.junitPlatformProperties.template());

                // routing handler
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "handler.yml", templates.restkotlin.openapi.handlerYml.template(serviceId, handlerPackage, operationList, prometheusMetrics, !skipHealthCheck, !skipServerInfo));

                // exclusion list for Config module
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "config.yml", templates.restkotlin.openapi.config.template());
                // added with #471
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "app-status.yml", templates.restkotlin.appStatusYml.template());
                // values.yml file, transfer to suppress the warning message during start startup and encourage usage.
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "values.yml", templates.restkotlin.openapi.values.template());
            }
        }

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
                    // Map<String, Any> properties = null;
                    List<Any> required = null;

                    // iterate through each schema in the components
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
                            handleProperties(props, entrySchema.getValue().asMap());
                        }
                        if("required".equals(entrySchema.getKey())) {
                            required = entrySchema.getValue().asList();
                        }
                        if("allOf".equals(entrySchema.getKey())) {
                            type = "object";

                            // could be referred to as "$ref" references or listed in "properties"
                            for(Any listItem : entrySchema.getValue().asList()) {
                                //Map<String, Any> allOfItem = (Map<String, Any>)listItem.asMap().entrySet();

                                for(Map.Entry<String, Any> allOfItem : ((Map<String, Any>)listItem.asMap()).entrySet()) {
                                    if("$ref".equals(allOfItem.getKey())) {
                                        String s = allOfItem.getValue().toString();
                                        s = s.substring(s.lastIndexOf('/') + 1);
                                        handleProperties(props, schemas.get(s).get("properties").asMap());
                                    }
                                    if("properties".equals(allOfItem.getKey())) {
                                        handleProperties(props, allOfItem.getValue().asMap());
                                    }
                                }
                            }
                        }
                    }
                    String classVarName = key;
                    String modelFileName = key.substring(0, 1).toUpperCase() + key.substring(1);
                    //System.out.println("props = " + Any.wrap(props));

                    // Check the type of current schema. Generation will be executed only if the type of the schema equals to object.
                    // Since generate a model for primitive types and arrays do not make sense, and an error class would be generated
                    // due to lack of properties if force to generate.
                    if (!"object".equals(type)) {
                        continue;
                    }
                    if(!overwriteModel && checkExist(targetPath, ("src.main.kotlin." + modelPackage).replace(".", separator), modelFileName + ".kt")) {
                        continue;
                    }
                    if (isEnumClass) {
                        transfer(targetPath, ("src.main.kotlin." + modelPackage).replace(".", separator), modelFileName + ".kt", templates.restkotlin.enumClass.template(modelPackage, modelFileName, enums));
                        continue;
                    }
                    transfer(targetPath, ("src.main.kotlin." + modelPackage).replace(".", separator), modelFileName + ".kt", templates.restkotlin.pojo.template(modelPackage, modelFileName, classVarName,  props));
                }
            }
        }

        // exit after generating the model if the consumer needs only the model classes
        if(generateModelOnly)
            return;

        // handler
        for(Map<String, Object> op : operationList){
            String className = op.get("handlerName").toString();
            String example = null;
            List<Map> parameters = (List) op.get("parameters");
            if(op.get("example") != null) {
                //example = mapper.writeValueAsString(op.get("example"));
                example = JsonStream.serialize(op.get("example"));
            }
            if(checkExist(targetPath, ("src.main.kotlin." + handlerPackage).replace(".", separator), className + ".kt") && !overwriteHandler) {
                continue;
            }
            transfer(targetPath, ("src.main.kotlin." + handlerPackage).replace(".", separator), className + ".kt", templates.restkotlin.handler.template(handlerPackage, className, example, parameters));
        }

        // handler test cases
        transfer(targetPath, ("src.test.kotlin." + handlerPackage + ".").replace(".", separator),  "LightTestServer.kt", templates.restkotlin.lightTestServerKt.template(handlerPackage));
        for(Map<String, Object> op : operationList){
            if(checkExist(targetPath, ("src.test.kotlin." + handlerPackage).replace(".", separator), op.get("handlerName") + "Test.kt") && !overwriteHandlerTest) {
                continue;
            }
            transfer(targetPath, ("src.test.kotlin." + handlerPackage).replace(".", separator), op.get("handlerName") + "Test.kt", templates.restkotlin.handlerTest.template(handlerPackage, op));
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

    /**
     * Initialize the property map with base elements as name, getter, setters, etc
     * @param entry The entry for which to generate
     * @param propMap The property map to add to, created in the caller
     */
    private void initializePropertyMap(Map.Entry<String, Any> entry, Map<String, Any> propMap) {
        String name = entry.getKey();
        propMap.put("jsonProperty", Any.wrap(name));
        if(name.startsWith("@")) {
            name = name.substring(1);

        }
        propMap.put("name", Any.wrap(name));
        propMap.put("getter", Any.wrap("get" + name.substring(0, 1).toUpperCase() + name.substring(1)));
        propMap.put("setter", Any.wrap("set" + name.substring(0, 1).toUpperCase() + name.substring(1)));
        // assume it is not enum unless it is overwritten
        propMap.put("isEnum", Any.wrap(false));
    }

    /**
     * Handle elements listed as "properties"
     *
     * @param props The properties map to add to
     */
    //private void handleProperties(List<Map<String, Any>> props, Map.Entry<String, Any> entrySchema) {
    private void handleProperties(List<Map<String, Any>> props, Map<String, Any> properties) {
        // transform properties
        for(Map.Entry<String, Any> entryProp: properties.entrySet()) {
            //System.out.println("key = " + entryProp.getKey() + " value = " + entryProp.getValue());
            Map<String, Any> propMap = new HashMap<>();

            // initialize property map
            initializePropertyMap(entryProp, propMap);

            String name = entryProp.getKey();
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
                        propMap.put("type", Any.wrap("List<" + s + ">"));
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
                        propMap.put("type", Any.wrap("Double"));
                    }
                    if("float".equals(s)) {
                        propMap.put("type", Any.wrap("Float"));
                    }
                    if("int64".equals(s)){
                        propMap.put("type", Any.wrap("Long"));
                    }
                    if("int32".equals(s)) {
                        propMap.put("type", Any.wrap("Int"));
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
                flattened.put("handlerName", Utils.camelize(normalizedPath) + Utils.camelize(entryOps.getKey()) + "Handler");
                Operation operation = entryOps.getValue();
                flattened.put("normalizedPath", UrlGenerator.generateUrl(basePath, path, entryOps.getValue().getParameters()));
                //eg. 200 || statusCode == 400 || statusCode == 500
                flattened.put("supportedStatusCodesStr", operation.getResponses().keySet().stream().collect(Collectors.joining(" || statusCode = ")));
                Map<String, Object> headerNameValueMap = operation.getParameters()
                        .stream()
                        .filter(parameter -> parameter.getIn().equals("header"))
                        .collect(Collectors.toMap(k -> k.getName(), v -> UrlGenerator.generateValidParam(v)));
                flattened.put("headerNameValueMap", headerNameValueMap);
                if (enableParamDescription) {
                    //get parameters info and put into result
                    List<Parameter> parameterRawList = operation.getParameters();
                    List<Map> parametersResultList = new LinkedList<>();
                    parameterRawList.forEach(parameter -> {
                        Map<String, String> parameterMap = new HashMap<>();
                        parameterMap.put("name", parameter.getName());
                        parameterMap.put("description", parameter.getDescription());
                        if(parameter.getRequired() != null) {
                            parameterMap.put("required", String.valueOf(parameter.getRequired()));
                        }
                        Schema schema = parameter.getSchema();
                        if(schema != null) {
                            parameterMap.put("type", schema.getType());
                            if(schema.getMinLength() != null) {
                                parameterMap.put("minLength", String.valueOf(schema.getMinLength()));
                            }
                            if(schema.getMaxLength() != null) {
                                parameterMap.put("maxLength", String.valueOf(schema.getMaxLength()));
                            }
                        }
                        parametersResultList.add(parameterMap);
                    });
                    flattened.put("parameters", parametersResultList);
                }
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
