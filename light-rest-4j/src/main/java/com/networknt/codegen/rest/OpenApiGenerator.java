package com.networknt.codegen.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.codegen.Generator;
import com.networknt.codegen.Utils;
import com.networknt.config.ConfigException;
import com.networknt.config.JsonMapper;
import com.networknt.jsonoverlay.Overlay;
import com.networknt.oas.OpenApiParser;
import com.networknt.oas.model.*;
import com.networknt.oas.model.impl.OpenApi3Impl;
import com.networknt.oas.model.impl.SchemaImpl;
import com.networknt.utility.StringUtils;

import javax.lang.model.SourceVersion;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.io.File.separator;

public class OpenApiGenerator implements Generator {
    private Map<String, String> typeMapping = new HashMap<>();

    boolean enableParamDescription = false;

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
     *
     * @throws IOException IO Exception occurs during code generation
     */
    @Override
    public void generate(final String targetPath, Object model, JsonNode config) throws IOException {
        // whoever is calling this needs to make sure that model is converted to Map<String, Object>
        String rootPackage = getRootPackage(config, null);
        String modelPackage = getModelPackage(config, null);
        String handlerPackage = getHandlerPackage(config, null);
        boolean overwriteHandler = isOverwriteHandler(config, null);
        boolean overwriteHandlerTest = isOverwriteHandlerTest(config, null);
        boolean overwriteModel = isOverwriteModel(config, null);
        boolean generateModelOnly = isGenerateModelOnly(config, null);
        boolean enableHttp = isEnableHttp(config, null);
        String httpPort = getHttpPort(config, null);
        boolean enableHttps = isEnableHttps(config, null);
        String httpsPort = getHttpsPort(config, null);
        boolean enableHttp2 = isEnableHttp2(config, null);
        boolean enableRegistry = isEnableRegistry(config, null);
        boolean eclipseIDE = isEclipseIDE(config, null);
        boolean supportClient = isSupportClient(config, null);
        boolean prometheusMetrics = isPrometheusMetrics(config, null);
        String dockerOrganization = getDockerOrganization(config, null);
        String version = getVersion(config, null);
        String groupId = getGroupId(config, null);
        String artifactId = getArtifactId(config, null);
        String serviceId = groupId + "." + artifactId + "-" + version;
        boolean specChangeCodeReGenOnly = isSpecChangeCodeReGenOnly(config, null);
        boolean enableParamDescription = isEnableParamDescription(config, null);
        boolean skipPomFile = isSkipPomFile(config, null);
        boolean kafkaProducer = isKafkaProducer(config, null);
        boolean kafkaConsumer = isKafkaConsumer(config, null);
        boolean supportAvro = isSupportAvro(config, null);
        String kafkaTopic = getKafkaTopic(config, null);
        String decryptOption = getDecryptOption(config, null);

        // get the list of operations for this model
        List<Map<String, Object>> operationList = getOperationList(model);

        // bypass project generation if the mode is the only one requested to be built
        if (!generateModelOnly) {
            // if set to true, regenerate the code only (handlers, model and the handler.yml, potentially affected by operation changes
            if (!specChangeCodeReGenOnly) {
                // generate configurations, project, masks, certs, etc
                if (!skipPomFile) {
                    transfer(targetPath, "", "pom.xml", templates.rest.openapi.pom.template(config));
                }


                transferMaven(targetPath);
                // There is only one port that should be exposed in Dockerfile, otherwise, the service
                // discovery will be so confused. If https is enabled, expose the https port. Otherwise http port.
                String expose = "";
                if (enableHttps) {
                    expose = httpsPort;
                } else {
                    expose = httpPort;
                }

                transfer(targetPath, "docker", "Dockerfile", templates.rest.dockerfile.template(config, expose));
                transfer(targetPath, "docker", "Dockerfile-Slim", templates.rest.dockerfileslim.template(config, expose));
                transfer(targetPath, "", "build.sh", templates.rest.buildSh.template(dockerOrganization, serviceId));
                transfer(targetPath, "", "kubernetes.yml", templates.rest.kubernetes.template(dockerOrganization, serviceId, config.get("artifactId").textValue(), expose, version));
                transfer(targetPath, "", ".gitignore", templates.rest.gitignore.template());
                transfer(targetPath, "", "README.md", templates.rest.README.template());
                transfer(targetPath, "", "LICENSE", templates.rest.LICENSE.template());
                if(eclipseIDE) {
                    transfer(targetPath, "", ".classpath", templates.rest.classpath.template());
                    transfer(targetPath, "", ".project", templates.rest.project.template(config));
                }
                // config
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "service.yml", templates.rest.openapi.service.template(config));

                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "server.yml",
                        templates.rest.server.template(serviceId, enableHttp, httpPort, enableHttps, httpsPort, enableHttp2, enableRegistry, version));
                transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml",
                        templates.rest.server.template(serviceId, enableHttp, "49587", enableHttps, "49588", enableHttp2, enableRegistry, version));

                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "openapi-security.yml", templates.rest.openapiSecurity.template());
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "openapi-validator.yml", templates.rest.openapiValidator.template());
                if (supportClient) {
                    transfer(targetPath, ("src.main.resources.config").replace(".", separator), "client.yml", templates.rest.clientYml.template());
                } else {
                    transfer(targetPath, ("src.test.resources.config").replace(".", separator), "client.yml", templates.rest.clientYml.template());
                }

                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "primary.crt", templates.rest.primaryCrt.template());
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "secondary.crt", templates.rest.secondaryCrt.template());
                if(kafkaProducer) {
                    transfer(targetPath, ("src.main.resources.config").replace(".", separator), "kafka-producer.yml", templates.rest.kafkaProducerYml.template(kafkaTopic));
                }
                if(kafkaConsumer) {
                    transfer(targetPath, ("src.main.resources.config").replace(".", separator), "kafka-streams.yml", templates.rest.kafkaStreamsYml.template(artifactId));
                }
                if(supportAvro) {
                    transfer(targetPath, ("src.main.resources.config").replace(".", separator), "schema-registry.yml", templates.rest.schemaRegistryYml.template());
                }

                // mask
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "mask.yml", templates.rest.maskYml.template());
                // logging
                transfer(targetPath, ("src.main.resources").replace(".", separator), "logback.xml", templates.rest.logback.template(rootPackage));
                transfer(targetPath, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.rest.logback.template(rootPackage));

                // exclusion list for Config module
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "config.yml", templates.rest.openapi.config.template(config));

                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "audit.yml", templates.rest.auditYml.template());
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "body.yml", templates.rest.bodyYml.template());
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "info.yml", templates.rest.infoYml.template());
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "correlation.yml", templates.rest.correlationYml.template());
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "metrics.yml", templates.rest.metricsYml.template());
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "sanitizer.yml", templates.rest.sanitizerYml.template());
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "traceability.yml", templates.rest.traceabilityYml.template());
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "health.yml", templates.rest.healthYml.template());
                // added with #471
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "app-status.yml", templates.rest.appStatusYml.template());
                // values.yml file, transfer to suppress the warning message during start startup and encourage usage.
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "values.yml", templates.rest.openapi.values.template());
            }
            // routing handler
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "handler.yml",
                    templates.rest.openapi.handlerYml.template(serviceId, handlerPackage, operationList, prometheusMetrics));

        }

        // model
        OpenApi3 openApi3 = null;
        try {
            openApi3 = (OpenApi3)new OpenApiParser().parse((JsonNode)model, new URL("https://oas.lightapi.net/"));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to parse the model", e);
        }
        Map<String, Object> specMap = JsonMapper.string2Map(Overlay.toJson((OpenApi3Impl)openApi3).toString());
        Map<String, Object> components = (Map<String, Object>)specMap.get("components");
        if(components != null) {
            Map<String, Object> schemas = (Map<String, Object>)components.get("schemas");
            if(schemas != null) {
                ArrayList<Runnable> modelCreators = new ArrayList<>();
                final HashMap<String, Object> references = new HashMap<>();
                for (Map.Entry<String, Object> entry : schemas.entrySet()) {
                    loadModel(entry.getKey(), null, (Map<String, Object>)entry.getValue(), schemas, overwriteModel, targetPath, modelPackage, modelCreators, references, null);
                }

                for (Runnable r : modelCreators) {
                    r.run();
                }
            }
        }

        // exit after generating the model if the consumer needs only the model classes
        if (generateModelOnly) {
            return;
        }

        // handler
        for (Map<String, Object> op : operationList) {
            String className = op.get("handlerName").toString();
            @SuppressWarnings("unchecked")
            List<Map> parameters = (List<Map>)op.get("parameters");
            Map<String, String> responseExample = (Map<String, String>)op.get("responseExample");
            String example = responseExample.get("example");
            String statusCode = responseExample.get("statusCode");
            statusCode = StringUtils.isBlank(statusCode) || statusCode.equals("default") ? "-1" : statusCode;
            if (checkExist(targetPath, ("src.main.java." + handlerPackage).replace(".", separator), className + ".java") && !overwriteHandler) {
                continue;
            }
            transfer(targetPath, ("src.main.java." + handlerPackage).replace(".", separator), className + ".java", templates.rest.handler.template(handlerPackage, className, statusCode, example, parameters));
        }

        // handler test cases
        if (!specChangeCodeReGenOnly) {
            transfer(targetPath, ("src.test.java." + handlerPackage + ".").replace(".", separator), "TestServer.java", templates.rest.testServer.template(handlerPackage));
        }

        for (Map<String, Object> op : operationList) {
            if (checkExist(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), op.get("handlerName") + "Test.java") && !overwriteHandlerTest) {
                continue;
            }
            transfer(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), op.get("handlerName") + "Test.java", templates.rest.openapi.handlerTest.template(handlerPackage, op));
        }

        // transfer binary files without touching them.
        try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
            Generator.copyFile(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "server.keystore"));
        }
        try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
            Generator.copyFile(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "server.truststore"));
        }
        if (supportClient) {
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Generator.copyFile(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "client.keystore"));
            }
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Generator.copyFile(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "client.truststore"));
            }
        } else {
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Generator.copyFile(is, Paths.get(targetPath, ("src.test.resources.config").replace(".", separator), "client.keystore"));
            }
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Generator.copyFile(is, Paths.get(targetPath, ("src.test.resources.config").replace(".", separator), "client.truststore"));
            }
        }

        try (InputStream is = new ByteArrayInputStream(Generator.yamlMapper.writeValueAsBytes(model))) {
            Generator.copyFile(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "openapi.yaml"));
        }
    }

    /**
     * Initialize the property map with base elements as name, getter, setters, etc
     *
     * @param entry The entry for which to generate
     * @param propMap The property map to add to, created in the caller
     */
    private void initializePropertyMap(Map.Entry<String, Object> entry, Map<String, Object> propMap) {
        String name = convertToValidJavaVariableName(entry.getKey());
        propMap.put("jsonProperty", name);
        if (name.startsWith("@")) {
            name = name.substring(1);

        }
        propMap.put("name", name);
        propMap.put("getter", "get" + name.substring(0, 1).toUpperCase() + name.substring(1));
        propMap.put("setter", "set" + name.substring(0, 1).toUpperCase() + name.substring(1));
        // assume it is not enum unless it is overwritten
        propMap.put("isEnum", false);
        propMap.put("isNumEnum", false);
    }

    /**
     * Handle elements listed as "properties"
     *
     * @param props The properties map to add to
     */
    //private void handleProperties(List<Map<String, Any>> props, Map.Entry<String, Any> entrySchema) {
    private void handleProperties(List<Map<String, Object>> props, Map<String, Object> properties) {
        // transform properties
        for (Map.Entry<String, Object> entryProp : properties.entrySet()) {
            //System.out.println("key = " + entryProp.getKey() + " value = " + entryProp.getValue());
            Map<String, Object> propMap = new HashMap<>();

            // initialize property map
            initializePropertyMap(entryProp, propMap);

            String name = entryProp.getKey();
            String type = null;
            boolean isArray = false;
            for (Map.Entry<String, Object> entryElement : ((Map<String, Object>)entryProp.getValue()).entrySet()) {
                //System.out.println("key = " + entryElement.getKey() + " value = " + entryElement.getValue());

                if ("type".equals(entryElement.getKey())) {
                    String t = typeMapping.get(entryElement.getValue().toString());
                    type = t;
                    if ("java.util.List".equals(t)) {
                        isArray = true;
                    } else {
                        propMap.putIfAbsent("type", t);
                    }
                }
                if ("items".equals(entryElement.getKey())) {
                    Map<String, Object> a = (Map)entryElement.getValue();
                    if (a.get("$ref") != null && isArray) {
                        String s = a.get("$ref").toString();
                        s = s.substring(s.lastIndexOf('/') + 1);
                        s = s.substring(0,1).toUpperCase() + (s.length() > 1 ? s.substring(1) : "");
                        propMap.put("type", getListOf(s));
                    }
                    if (a.get("type") != null && isArray) {
                        propMap.put("type", getListOf(typeMapping.get(a.get("type").toString())));
                    }
                }
                if ("$ref".equals(entryElement.getKey())) {
                    String s = entryElement.getValue().toString();
                    s = s.substring(s.lastIndexOf('/') + 1);
                    s = s.substring(0,1).toUpperCase() + (s.length() > 1 ? s.substring(1) : "");
                    propMap.put("type", s);
                }
                if ("default".equals(entryElement.getKey())) {
                    Object a = entryElement.getValue();
                    propMap.put("default", a);
                }
                if ("enum".equals(entryElement.getKey())) {
                    // different generate format for number enum
                    if ("Integer".equals(type) || "Double".equals(type) || "Float".equals(type)
                            || "Long".equals(type) || "Short".equals(type) || "java.math.BigDecimal".equals(type)) {
                        propMap.put("isNumEnum", true);
                    }
                    propMap.put("isEnum", true);
                    propMap.put("nameWithEnum", name.substring(0, 1).toUpperCase() + name.substring(1) + "Enum");
                    propMap.put("value", getValidEnumName(entryElement));
                }

                if ("format".equals(entryElement.getKey())) {
                    String s = entryElement.getValue().toString();

                    String ultimateType;
                    switch (s) {
                        case "date-time":
                            ultimateType = "java.time.LocalDateTime";
                            break;

                        case "date":
                            ultimateType = "java.time.LocalDate";
                            break;

                        case "double":
                            ultimateType = "java.lang.Double";
                            break;

                        case "float":
                            ultimateType = "java.lang.Float";
                            break;

                        case "int64":
                            ultimateType = "java.lang.Long";
                            break;

                        case "binary":
                            ultimateType = "byte[]";
                            propMap.put(COMPARATOR, "Arrays");
                            propMap.put(HASHER, "Arrays");
                            break;

                        case "byte":
                            ultimateType = "byte";
                            break;

                        default:
                            ultimateType = null;
                    }

                    if (ultimateType != null) {
                        propMap.put("type", ultimateType);
                    }
                }

                if ("oneOf".equals(entryElement.getKey())) {
                    List<Object> list = (List<Object>)entryElement.getValue();
                    Object t = ((Map)list.get(0)).get("type");
                    if (t != null) {
                        propMap.put("type", typeMapping.get(t.toString()));
                    } else {
                        // maybe reference? default type to object.
                        propMap.put("type", "Object");
                    }
                }
                if ("anyOf".equals(entryElement.getKey())) {
                    List<Object> list = (List)entryElement.getValue();
                    Object t = ((Map)list.get(0)).get("type");
                    if (t != null) {
                        propMap.put("type", typeMapping.get(t.toString()));
                    } else {
                        // maybe reference? default type to object.
                        propMap.put("type", "Object");
                    }
                }
                if ("allOf".equals(entryElement.getKey())) {
                    List<Object> list = (List)entryElement.getValue();
                    Object t = ((Map)list.get(0)).get("type");
                    if (t != null) {
                        propMap.put("type", typeMapping.get(t.toString()));
                    } else {
                        // maybe reference? default type to object.
                        propMap.put("type", "Object");
                    }
                }
                if ("not".equals(entryElement.getKey())) {
                    Map<String, Object> m = (Map)entryElement.getValue();
                    Object t = m.get("type");
                    if (t != null) {
                        propMap.put("type", t);
                    } else {
                        propMap.put("type", "Object");
                    }
                }
            }
            props.add(propMap);
        }
    }
    public static final String HASHER = "hasher";
    public static final String COMPARATOR = "comparator";

    private String getListOf(String s) {
        return String.format("java.util.List<%s>", s);
    }

    // method used to convert string to valid java variable name
    // 1. replace invalid character with '_'
    // 2. prefix number with '_'
    // 3. convert the first character of java keywords to upper case
    public static String convertToValidJavaVariableName(String string) {
        if (string == null || string.equals("") || SourceVersion.isName(string)) {
            return string;
        }
        // to validate whether the string is Java keyword
        if (SourceVersion.isKeyword(string)) {
            return "_" + string;
        }
        // replace invalid characters with underscore
        StringBuilder stringBuilder = new StringBuilder();
        if (!Character.isJavaIdentifierStart(string.charAt(0))) {
            stringBuilder.append('_');
        }
        for (char c : string.toCharArray()) {
            if (!Character.isJavaIdentifierPart(c)) {
                stringBuilder.append('_');
            } else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    private  boolean isEnumHasDescription(String string) {
        return string.contains(":") || string.contains("{") || string.contains("(");
    }

    private  String getEnumName(String string) {
        if (string.contains(":")) return string.substring(0, string.indexOf(":")).trim();
        if (string.contains("(") && string.contains(")")) return string.substring(0, string.indexOf("(")).trim();
        if (string.contains("{") && string.contains("}")) return string.substring(0, string.indexOf("{")).trim();
        return string;
    }

    private  String getEnumDescription(String string) {
        if (string.contains(":")) return string.substring(string.indexOf(":") + 1).trim();
        if (string.contains("(") && string.contains(")")) return string.substring(string.indexOf("(") + 1, string.indexOf(")")).trim();
        if (string.contains("{") && string.contains("}")) return string.substring(string.indexOf("{") + 1, string.indexOf("}")).trim();

        return string;
    }

    public List<Map<String, Object>> getOperationList(Object model) {
        List<Map<String, Object>> result = new ArrayList<>();
        OpenApi3 openApi3 = null;
        try {
            openApi3 = (OpenApi3)new OpenApiParser().parse((JsonNode)model, new URL("https://oas.lightapi.net/"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String basePath = getBasePath(openApi3);

        Map<String, Path> paths = openApi3.getPaths();
        for (Map.Entry<String, Path> entryPath : paths.entrySet()) {
            String path = entryPath.getKey();
            Path pathValue = entryPath.getValue();
            for (Map.Entry<String, Operation> entryOps : pathValue.getOperations().entrySet()) {
                // skip all the entries that are not http method. The only possible entries
                // here are extensions. which will be just a key value pair.
                if (entryOps.getKey().startsWith("x-")) {
                    continue;
                }
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
                flattened.put("requestBodyExample", populateRequestBodyExample(operation));
                Map<String, String> responseExample = populateResponseExample(operation);
                flattened.put("responseExample", responseExample);
                if (enableParamDescription) {
                    //get parameters info and put into result
                    List<Parameter> parameterRawList = operation.getParameters();
                    List<Map> parametersResultList = new LinkedList<>();
                    parameterRawList.forEach(parameter -> {
                        Map<String, String> parameterMap = new HashMap<>();
                        parameterMap.put("name", parameter.getName());
                        parameterMap.put("description", parameter.getDescription());
                        if (parameter.getRequired() != null) {
                            parameterMap.put("required", String.valueOf(parameter.getRequired()));
                        }
                        Schema schema = parameter.getSchema();
                        if (schema != null) {
                            parameterMap.put("type", schema.getType());
                            if (schema.getMinLength() != null) {
                                parameterMap.put("minLength", String.valueOf(schema.getMinLength()));
                            }
                            if (schema.getMaxLength() != null) {
                                parameterMap.put("maxLength", String.valueOf(schema.getMaxLength()));
                            }
                        }
                        parametersResultList.add(parameterMap);
                    });
                    flattened.put("parameters", parametersResultList);
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
        if (url != null) {
            // find "://" index
            int protocolIndex = url.indexOf("://");
            int pathIndex = url.indexOf('/', protocolIndex + 3);
            if (pathIndex > 0) {
                basePath = url.substring(pathIndex);
            }
        }
        return basePath;
    }

    // method used to generate valid enum keys for enum contents
    private Object getValidEnumName(Map.Entry<String, Object> entryElement) {
        Iterator<Object> iterator = ((List)entryElement.getValue()).iterator();
        Map<String, Object> map = new HashMap<>();
        while (iterator.hasNext()) {
            String string = iterator.next().toString().trim();
            if (string.equals("")) continue;
            if (isEnumHasDescription(string)) {
                map.put(convertToValidJavaVariableName(getEnumName(string)).toUpperCase(), getEnumDescription(string));
            } else {
                map.put(convertToValidJavaVariableName(string).toUpperCase(), string);
            }
        }
        return map;
    }

    private String populateRequestBodyExample(Operation operation) {
        String result = "{\"content\": \"request body to be replaced\"}";
        RequestBody body = operation.getRequestBody();
        if (body != null) {
            MediaType mediaType = body.getContentMediaType("application/json");
            if (mediaType != null) {
                Object valueToBeStringify = null;
                if (mediaType.getExamples() != null && !mediaType.getExamples().isEmpty()) {
                    for (Map.Entry<String, Example> entry : mediaType.getExamples().entrySet()) {
                        valueToBeStringify = entry.getValue().getValue();
                    }
                } else if (mediaType.getExample() != null) {
                    valueToBeStringify = mediaType.getExample();
                }
                if (valueToBeStringify == null) {
                    return result;
                }
                try {
                    result = Generator.jsonMapper.writeValueAsString(valueToBeStringify);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                if (result.startsWith("\"")) {
                    result = result.substring(1, result.length() - 1);
                }
            }
        }
        return result;
    }

    private Map<String, String> populateResponseExample(Operation operation) {
        Map<String, String> result = new HashMap<>();
        Object example;
        for (String statusCode : operation.getResponses().keySet()) {
            Optional<Response> response = Optional.ofNullable(operation.getResponse(String.valueOf(statusCode)));
            if (response.get().getContentMediaTypes().size() == 0) {
                result.put("statusCode", statusCode);
                result.put("example", "{}");
            }
            for (String mediaTypeStr : response.get().getContentMediaTypes().keySet()) {
                Optional<MediaType> mediaType = Optional.ofNullable(response.get().getContentMediaType(mediaTypeStr));
                example = mediaType.get().getExample();
                if (example != null) {
                    result.put("statusCode", statusCode);
                    String ex = null;
                    try {
                        ex = Generator.jsonMapper.writeValueAsString(example);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    result.put("example", ex);
                } else {
                    // check if there are multiple examples
                    Map<String, Example> exampleMap = mediaType.get().getExamples();
                    // use the first example if there are multiple
                    if (exampleMap.size() > 0) {
                        Map.Entry<String, Example> entry = exampleMap.entrySet().iterator().next();
                        Example e = entry.getValue();
                        if (e != null) {
                            result.put("statusCode", statusCode);
                            String s = null;
                            try {
                                s = Generator.jsonMapper.writeValueAsString(e.getValue());
                            } catch (JsonProcessingException ex) {
                                ex.printStackTrace();
                            }
                            result.put("example", s);
                        }
                    }
                }
            }
        }
        return result;
    }

    private void loadModel(String classVarName, String parentClassName, Map<String, Object> value, Map<String, Object> schemas, boolean overwriteModel, String targetPath, String modelPackage, List<Runnable> modelCreators, Map<String, Object> references, List<Map<String, Object>> parentClassProps) throws IOException {
        final String modelFileName = classVarName.substring(0, 1).toUpperCase() + classVarName.substring(1);
        final List<Map<String, Object>> props = new ArrayList<>();
        final List<Map<String, Object>> parentProps = (parentClassProps == null) ? new ArrayList<>() : new ArrayList<>(parentClassProps);
        String type = null;
        String enums = null;
        boolean isEnumClass = false;
        List<Object> required = null;
        boolean isAbstractClass = false;

        // iterate through each schema in the components
        Queue<Map.Entry<String, Object>> schemaElementQueue = new LinkedList<>();
        // cache the visited elements to prevent loop reference
        Set<String> seen = new HashSet<>();
        // add elements into queue to perform a BFS
        for (Map.Entry<String, Object> entrySchema : value.entrySet()) {
            schemaElementQueue.offer(entrySchema);
        }
        while (!schemaElementQueue.isEmpty()) {
            Map.Entry<String, Object> currentElement = schemaElementQueue.poll();
            String currentElementKey = currentElement.getKey();
            // handle the base elements
            if ("type".equals(currentElementKey) && type == null) {
                type = currentElement.getValue().toString();
            }
            if ("enum".equals(currentElementKey)) {
                isEnumClass = true;
                enums = currentElement.getValue().toString();
                enums = enums.substring(enums.indexOf("[") + 1, enums.indexOf("]"));
            }
            if ("properties".equals(currentElementKey)) {
                handleProperties(props, (Map<String, Object>)currentElement.getValue());
            }
            if ("required".equals(currentElementKey)) {
                if (required == null) {
                    required = new ArrayList<>();
                }
                required.addAll((List)currentElement.getValue());
            }
            // expend the ref elements and add to the queue
            if ("$ref".equals(currentElementKey)) {
                String s = currentElement.getValue().toString();
                s = s.substring(s.lastIndexOf('/') + 1);
                if (seen.contains(s)) continue;
                seen.add(s);
                for (Map.Entry<String, Object> schema : ((Map<String, Object>)schemas.get(s)).entrySet()) {
                    schemaElementQueue.offer(schema);
                }
            }
            // expand the allOf elements and add to the queue
            if ("allOf".equals(currentElementKey)) {
                for (Object listItem : (List)currentElement.getValue()) {
                    for (Map.Entry<String, Object> allOfItem : ((Map<String, Object>)listItem).entrySet()) {
                        schemaElementQueue.offer(allOfItem);
                    }
                }
            }
            // call loadModel recursively to generate new model corresponding to each oneOf elements
            if ("oneOf".equals(currentElementKey)) {
                isAbstractClass = true;
                parentProps.addAll(props);
                String parentName = classVarName.substring(0, 1) + classVarName.substring(1);
                for (Object listItem : (List)currentElement.getValue()) {
                    for (Map.Entry<String, Object> oneOfItem : ((Map<String, Object>)listItem).entrySet()) {
                        if ("$ref".equals(oneOfItem.getKey())) {
                            String s = oneOfItem.getValue().toString();
                            s = s.substring(s.lastIndexOf('/') + 1);
                            loadModel(extendModelName(s, classVarName), s, (Map<String, Object>)schemas.get(s), schemas, overwriteModel, targetPath, modelPackage, modelCreators, references, parentProps);
                        }
                    }
                }
            }
        }
        // Check the type of current schema. Generation will be executed only if the type of the schema equals to object.
        // Since generate a model for primitive types and arrays do not make sense, and an error class would be generated
        // due to lack of properties if force to generate.
        if (type == null && !isAbstractClass) {
            throw new RuntimeException("Cannot find the schema type of \"" + modelFileName + "\" in #/components/schemas/ of the specification file. In most cases, you need to add \"type: object\" if you want to generate a POJO. Otherwise, give it a type of primary like string or number.");
        }

        if ("object".equals(type) || isEnumClass) {
            if (!overwriteModel && checkExist(targetPath, ("src.main.java." + modelPackage).replace(".", separator), modelFileName + ".java")) {
                return;
            }

            final String enumsIfClass = isEnumClass ? enums : null;
            final boolean abstractIfClass = isAbstractClass;
            modelCreators.add(() -> {
                final int referencesCount = references.size();
                for (Map<String, Object> properties : props) {
                    Object any = properties.get("type");
                    if (any != null) {
                        if (any instanceof String) {
                            Object resolved = references.get(any);
                            if (resolved == null) {
                                continue;
                            }
                            any = resolved;
                            properties.put("type", resolved);
                        }

                        int iteration = 0;
                        do {
                            Object previous = null;
                            while (unresolvedListType((String)any)) {
                                previous = any;
                                any = getListObjectType((String)any);
                            }

                            if (any == null) {
                                break;
                            } else if (iteration++ > referencesCount) {
                                throw new TypeNotPresentException(any.toString(), null);
                            }

                            if (any instanceof String) {
                                any = references.get(any);
                                if (any == null) {
                                    break;
                                } else {
                                    previous = setListObjectType((String)previous, (String)any);
                                    properties.put("type", previous);
                                }
                            } else {
                                break;
                            }
                        } while (true);
                    }
                }

                try {
                    transfer(targetPath,
                            ("src.main.java." + modelPackage).replace(".", separator),
                            modelFileName + ".java",
                            enumsIfClass == null
                                    ? templates.rest.pojo.template(modelPackage, modelFileName, parentClassName, classVarName, abstractIfClass, props, parentClassProps)
                                    : templates.rest.enumClass.template(modelPackage, modelFileName, enumsIfClass));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        } else {
            HashMap<String, Object> map = new HashMap<>(1);
            map.put(classVarName, value);
            handleProperties(props, map);
            if (props.isEmpty()) {
                throw new IllegalStateException("Properties empty for " + classVarName + "!");
            }

            references.put(modelFileName, props.get(0).get("type"));
        }
    }
    private String extendModelName(String str1, String str2) {
        return str1 + str2.substring(0, 1).toUpperCase() + str2.substring(1);
    }
    private String getListObjectType(String listType) {
        if(listType != null && listType.contains("<") && listType.contains(">")) {
            return listType.substring(listType.indexOf("<") + 1, listType.indexOf(">"));
        } else {
            return listType;
        }
    }

    private String setListObjectType(String original, String resolved) {
        if(!original.equals(resolved) && original.contains("<") && original.contains(">")) {
            String replace = original.substring(original.indexOf("<") + 1, original.indexOf(">"));
            original = original.replace(replace, resolved);
        }
        return original;
    }

    private boolean unresolvedListType(String listType) {
        boolean result = false;
        if(listType != null && listType.contains("java.util.List")) {
            String objType = getListObjectType(listType);
            if(!objType.equals("Boolean")
                    && !objType.equals("Integer")
                    && !objType.equals("Long")
                    && !objType.equals("Float")
                    && !objType.equals("Double")
                    && !objType.equals("Object")) {
                result = true;
            }
        }
        return result;
    }
}
