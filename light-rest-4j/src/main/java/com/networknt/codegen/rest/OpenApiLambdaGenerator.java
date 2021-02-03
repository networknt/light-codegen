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
import com.networknt.utility.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static com.networknt.codegen.Generator.copyFile;
import static java.io.File.separator;

/**
 * The input for OpenAPI 3.0 AWS Lambda generator include config with json format and OpenAPI specification
 * in yaml format.
 *
 * The model is OpenAPI specification in yaml format. And config file is config.json in JSON format.
 *
 * The generated project can be built, tested and debugged locally with AWS CLI, SAM and Docker installed.
 *
 * @author Steve Hu
 */
public class OpenApiLambdaGenerator implements Generator {

    private Map<String, String> typeMapping = new HashMap<>();

    // optional generation parameters. if not set, they use default values as
    boolean prometheusMetrics = false;
    boolean skipHealthCheck = false;
    boolean skipServerInfo = false;
    boolean specChangeCodeReGenOnly = false;
    boolean enableParamDescription = true;
    boolean generateModelOnly = false;
    boolean buildMaven = false;
    boolean useLightProxy = false;
    boolean publicVpc = true;
    boolean skipPomFile = false;

    public OpenApiLambdaGenerator() {
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
        return "openapilambda";
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
    public void generate(final String targetPath, Object model, Any config) throws IOException {
        // whoever is calling this needs to make sure that model is converted to Map<String, Object>
        String projectName = config.toString("projectName").trim();

        final String modelPackage = config.toString("modelPackage").trim();
        final String rootPackage = config.toString("rootPackage").trim();
        String handlerPackage = config.toString("handlerPackage").trim();

        boolean overwriteHandler = config.toBoolean("overwriteHandler");
        boolean overwriteHandlerTest = config.toBoolean("overwriteHandlerTest");
        boolean overwriteModel = config.toBoolean("overwriteModel");
        boolean packageDocker = config.toBoolean("packageDocker");
        boolean enableRegistry = config.toBoolean("enableRegistry");
        generateModelOnly = config.toBoolean("generateModelOnly");
        useLightProxy = config.toBoolean("useLightProxy");
        buildMaven = config.toBoolean("buildMaven");
        String launchType = config.toString("launchType").trim();
        String region = config.toString("region").trim();
        publicVpc = config.toBoolean("publicVpc");
        specChangeCodeReGenOnly = config.toBoolean("specChangeCodeReGenOnly");
        enableParamDescription = config.toBoolean("enableParamDescription");
        skipPomFile = config.toBoolean("skipPomFile");
        String artifactId = config.toString("artifactId");
        String serviceId = config.get("groupId").toString().trim() + "." + artifactId.trim() + "-" + config.get("version").toString().trim();
        String version = config.toString("version").trim();
        // get the list of operations for this model
        List<Map<String, Object>> operationList = getOperationList(model);
        List<OpenApiPath> pathList = getPathList(operationList);
        transfer(targetPath, "", ".gitignore", templates.lambda.gitignore.template());

        transfer(targetPath, "", "README.md", templates.lambda.README.template(projectName, packageDocker, operationList));

        if(!useLightProxy) {
            // use AWS API Gateway to access Lambda functions.
            transfer(targetPath, "", "template.yaml", templates.lambda.template.template(projectName, handlerPackage, packageDocker, useLightProxy, operationList, pathList));
        } else {
            // use light-proxy for Lambda function
            if("EC2".equals(launchType)) {
                if(publicVpc) {
                    transfer(targetPath, "", "public-vpc.yaml", templates.lambda.EC2.publicVpcYaml.template());
                    transfer(targetPath, "", "public-proxy.yaml", templates.lambda.EC2.publicProxyYaml.template());
                    transfer(targetPath, "", "template.yaml", templates.lambda.template.template(projectName, handlerPackage, packageDocker, useLightProxy, operationList, pathList));
                } else {
                    transfer(targetPath, "", "private-vpc.yaml", templates.lambda.EC2.privateVpcYaml.template());
                    transfer(targetPath, "", "private-proxy.yaml", templates.lambda.EC2.privateProxyYaml.template());
                    transfer(targetPath, "", "template.yaml", templates.lambda.template.template(projectName, handlerPackage, packageDocker, useLightProxy, operationList, pathList));
                }
            } else {
                // fargate as default
                if(publicVpc) {
                    transfer(targetPath, "", "public-vpc.yaml", templates.lambda.Fargate.publicVpcYaml.template());
                    transfer(targetPath, "", "public-proxy.yaml", templates.lambda.Fargate.publicProxyYaml.template());
                    transfer(targetPath, "", "template.yaml", templates.lambda.template.template(projectName, handlerPackage, packageDocker, useLightProxy, operationList, pathList));
                } else {
                    transfer(targetPath, "", "private-vpc.yaml", templates.lambda.Fargate.privateVpcYaml.template());
                    transfer(targetPath, "", "private-proxy.yaml", templates.lambda.Fargate.privateProxyYaml.template());
                    transfer(targetPath, "", "template.yaml", templates.lambda.template.template(projectName, handlerPackage, packageDocker, useLightProxy, operationList, pathList));
                }
            }
            transfer(targetPath, "proxy", "handler.yml",
                    templates.lambda.proxy.handlerYml.template(serviceId, handlerPackage, operationList, prometheusMetrics, !skipHealthCheck, !skipServerInfo));

            transfer(targetPath, "proxy", "lambda-invoker.yml",
                    templates.lambda.proxy.lambdaInvokerYml.template(region, operationList));
            try (InputStream is = new ByteArrayInputStream(((String)model).getBytes(StandardCharsets.UTF_8))) {
                copyFile(is, Paths.get(targetPath, "proxy", "openapi.yaml"));
            }
            transfer(targetPath, "proxy", "server.yml", templates.lambda.proxy.server.template(serviceId, enableRegistry, version));

            transfer(targetPath, "proxy", "openapi-security.yml", templates.rest.openapiSecurity.template());
            transfer(targetPath, "proxy", "openapi-validator.yml", templates.rest.openapiValidator.template());
            transfer(targetPath, "proxy", "client.yml", templates.rest.clientYml.template());

            transfer(targetPath, "proxy", "primary.crt", templates.rest.primaryCrt.template());
            transfer(targetPath, "proxy", "secondary.crt", templates.rest.secondaryCrt.template());
            // transfer binary files without touching them.
            try (InputStream is = OpenApiLambdaGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
                copyFile(is, Paths.get(targetPath, "proxy", "server.keystore"));
            }
            try (InputStream is = OpenApiLambdaGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
                copyFile(is, Paths.get(targetPath, "proxy", "server.truststore"));
            }
            try (InputStream is = OpenApiLambdaGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                copyFile(is, Paths.get(targetPath, "proxy", "client.keystore"));
            }
            try (InputStream is = OpenApiLambdaGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                copyFile(is, Paths.get(targetPath, "proxy", "client.truststore"));
            }
            // logging
            transfer(targetPath, "proxy", "logback.xml", templates.rest.logback.template(rootPackage));
            // proxy.yml
            transfer(targetPath, "proxy", "proxy.yml", templates.lambda.proxy.proxy.template());

            // exclusion list for Config module
            transfer(targetPath, "proxy", "config.yml", templates.rest.openapi.config.template(config));

            transfer(targetPath, "proxy", "audit.yml", templates.rest.auditYml.template());
            transfer(targetPath, "proxy", "body.yml", templates.rest.bodyYml.template());
            transfer(targetPath, "proxy", "info.yml", templates.rest.infoYml.template());
            transfer(targetPath, "proxy", "correlation.yml", templates.rest.correlationYml.template());
            transfer(targetPath, "proxy", "metrics.yml", templates.rest.metricsYml.template());
            transfer(targetPath, "proxy", "sanitizer.yml", templates.rest.sanitizerYml.template());
            transfer(targetPath, "proxy", "traceability.yml", templates.rest.traceabilityYml.template());
            transfer(targetPath, "proxy", "health.yml", templates.rest.healthYml.template());
            // values.yml file, transfer to suppress the warning message during start startup and encourage usage.
            transfer(targetPath, "proxy", "values.yml", templates.rest.openapi.values.template());
            // buildSh.rocker.raw for the docker image build
            transfer(targetPath, "", "build.sh", templates.lambda.buildSh.template());
            // Dockerfile for the proxy
            transfer(targetPath, "", "Dockerfile-proxy", templates.lambda.DockerfileProxy.template());
        }

        // handler
        for (Map<String, Object> op : operationList) {
            // for each operation, we need to generate a function in a separate folder.
            String functionName = op.get("functionName").toString();

            // generate event.json
            transfer(targetPath, "events", "event" + functionName + ".json", templates.lambda.event.template());


            // generate Dockerfile if packageDocker is true
            if(packageDocker) {
                transfer(targetPath, functionName, "Dockerfile", templates.lambda.Dockerfile.template(handlerPackage));
            }

            if(buildMaven) {
                // generate pom.xml
                transfer(targetPath, functionName, "pom.xml", templates.lambda.pom.template(config, functionName));
                transferMaven(targetPath + separator + functionName);
            } else {
                transfer(targetPath, functionName, "build.gradle", templates.lambda.buildGradle.template(config));
                transfer(targetPath, functionName, "gradle.properties", templates.lambda.gradleProperties.template());
                transferGradle(targetPath + separator + functionName);
                transfer(targetPath, functionName, "bootstrap", templates.lambda.bootstrap.template());
                transfer(targetPath, functionName, "build_graalvm.sh", templates.lambda.buildGraalvmSh.template(functionName));
                transfer(targetPath, functionName, "reflect.json", templates.lambda.reflectJson.template(handlerPackage));
                transfer(targetPath, functionName, "resource-config.json", templates.lambda.resourceJson.template());
                transfer(targetPath, functionName, "Makefile", templates.lambda.Makefile.template(functionName));

            }

            // generate handler
            String className = op.get("handlerName").toString();
            @SuppressWarnings("unchecked")
            List<Map> parameters = (List<Map>)op.get("parameters");
            Map<String, String> responseExample = (Map<String, String>)op.get("responseExample");
            String example = responseExample.get("example");
            String statusCode = responseExample.get("statusCode");
            statusCode = StringUtils.isBlank(statusCode) || statusCode.equals("default") ? "-1" : statusCode;

            if (checkExist(targetPath + separator + functionName, ("src.main.java." + handlerPackage).replace(".", separator), "BusinessHandler.java")) {
                continue;
            } else {
                transfer(targetPath + separator + functionName, ("src.main.java." + handlerPackage).replace(".", separator), "BusinessHandler.java", templates.lambda.BusinessHandler.template(handlerPackage, example));
            }
            if (checkExist(targetPath + separator + functionName, ("src.test.java." + handlerPackage).replace(".", separator), "BusinessHandlerTest.java")) {
                continue;
            } else {
                transfer(targetPath + separator + functionName, ("src.test.java." + handlerPackage).replace(".", separator), "BusinessHandlerTest.java", templates.lambda.BusinessHandlerTest.template(handlerPackage, op));
            }
            if(useLightProxy) {
                transfer(targetPath + separator + functionName, ("src.main.java." + handlerPackage).replace(".", separator), "App.java", templates.lambda.AppProxy.template(handlerPackage));
            } else {
                transfer(targetPath + separator + functionName, ("src.main.java." + handlerPackage).replace(".", separator), "App.java", templates.lambda.AppGateway.template(handlerPackage));
            }
            transfer(targetPath + separator + functionName, ("src.test.java." + handlerPackage).replace(".", separator), "AppTest.java", templates.lambda.AppTest.template(handlerPackage));

            // generate model
            Any anyComponents;
            if (model instanceof Any) {
                anyComponents = ((Any)model).get("components");
            } else if (model instanceof String) {
                // this must be yaml format and we need to convert to json for JsonIterator.
                OpenApi3 openApi3 = null;
                try {
                    openApi3 = (OpenApi3)new OpenApiParser().parse((String)model, new URL("https://oas.lightapi.net/"));
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Failed to parse the model", e);
                }
                anyComponents = JsonIterator.deserialize(Overlay.toJson((OpenApi3Impl)openApi3).toString()).get("components");
            } else {
                throw new RuntimeException("Invalid Model Class: " + model.getClass());
            }

            if (anyComponents.valueType() != ValueType.INVALID) {
                Any schemas = anyComponents.asMap().get("schemas");
                if (schemas != null && schemas.valueType() != ValueType.INVALID) {
                    ArrayList<Runnable> modelCreators = new ArrayList<>();
                    final HashMap<String, Any> references = new HashMap<>();
                    for (Map.Entry<String, Any> entry : schemas.asMap().entrySet()) {
                        loadModel(entry.getKey(), null, entry.getValue().asMap(), schemas, overwriteModel, targetPath +  separator + functionName, modelPackage, modelCreators, references, null);
                    }

                    for (Runnable r : modelCreators) {
                        r.run();
                    }
                }
            }
            if(!useLightProxy) {
                if (model instanceof Any) {
                    try (InputStream is = new ByteArrayInputStream(model.toString().getBytes(StandardCharsets.UTF_8))) {
                        copyFile(is, Paths.get(targetPath + separator + functionName, ("src.main.resources").replace(".", separator), "openapi.yaml"));
                    }
                } else if (model instanceof String) {
                    try (InputStream is = new ByteArrayInputStream(((String)model).getBytes(StandardCharsets.UTF_8))) {
                        copyFile(is, Paths.get(targetPath + separator + functionName, ("src.main.resources").replace(".", separator), "openapi.yaml"));
                    }
                }
            }

            // app.yml
            transfer(targetPath + separator + functionName, ("src.main.resources").replace(".", separator), "app.yml", templates.lambda.appYml.template(useLightProxy));

            // logback.xml
            transfer(targetPath + separator + functionName, ("src.main.resources").replace(".", separator), "logback.xml", templates.lambda.logback.template(rootPackage));
            transfer(targetPath + separator + functionName, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.lambda.logback.template(rootPackage));
            // client truststore for the Prod stage.
            if(!useLightProxy) {
                try (InputStream is = OpenApiLambdaGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                    copyFile(is, Paths.get(targetPath + separator + functionName, ("src.main.resources").replace(".", separator), "prod.truststore"));
                }
            }
        }
    }

    /**
     * Initialize the property map with base elements as name, getter, setters, etc
     *
     * @param entry The entry for which to generate
     * @param propMap The property map to add to, created in the caller
     */
    private void initializePropertyMap(Map.Entry<String, Any> entry, Map<String, Any> propMap) {
        String name = convertToValidJavaVariableName(entry.getKey());
        propMap.put("jsonProperty", Any.wrap(name));
        if (name.startsWith("@")) {
            name = name.substring(1);

        }
        propMap.put("name", Any.wrap(name));
        propMap.put("getter", Any.wrap("get" + name.substring(0, 1).toUpperCase() + name.substring(1)));
        propMap.put("setter", Any.wrap("set" + name.substring(0, 1).toUpperCase() + name.substring(1)));
        // assume it is not enum unless it is overwritten
        propMap.put("isEnum", Any.wrap(false));
        propMap.put("isNumEnum", Any.wrap(false));
    }

    /**
     * Handle elements listed as "properties"
     *
     * @param props The properties map to add to
     */
    //private void handleProperties(List<Map<String, Any>> props, Map.Entry<String, Any> entrySchema) {
    private void handleProperties(List<Map<String, Any>> props, Map<String, Any> properties) {
        // transform properties
        for (Map.Entry<String, Any> entryProp : properties.entrySet()) {
            //System.out.println("key = " + entryProp.getKey() + " value = " + entryProp.getValue());
            Map<String, Any> propMap = new HashMap<>();

            // initialize property map
            initializePropertyMap(entryProp, propMap);

            String name = entryProp.getKey();
            String type = null;
            boolean isArray = false;
            for (Map.Entry<String, Any> entryElement : entryProp.getValue().asMap().entrySet()) {
                //System.out.println("key = " + entryElement.getKey() + " value = " + entryElement.getValue());

                if ("type".equals(entryElement.getKey())) {
                    String t = typeMapping.get(entryElement.getValue().toString());
                    type = t;
                    if ("java.util.List".equals(t)) {
                        isArray = true;
                    } else {
                        propMap.putIfAbsent("type", Any.wrap(t));
                    }
                }
                if ("items".equals(entryElement.getKey())) {
                    Any a = entryElement.getValue();
                    if (a.get("$ref").valueType() != ValueType.INVALID && isArray) {
                        String s = a.get("$ref").toString();
                        s = s.substring(s.lastIndexOf('/') + 1);
                        s = s.substring(0,1).toUpperCase() + (s.length() > 1 ? s.substring(1) : "");
                        propMap.put("type", getListOf(s));
                    }
                    if (a.get("type").valueType() != ValueType.INVALID && isArray) {
                        propMap.put("type", getListOf(typeMapping.get(a.get("type").toString())));
                    }
                }
                if ("$ref".equals(entryElement.getKey())) {
                    String s = entryElement.getValue().toString();
                    s = s.substring(s.lastIndexOf('/') + 1);
                    s = s.substring(0,1).toUpperCase() + (s.length() > 1 ? s.substring(1) : "");
                    propMap.put("type", Any.wrap(s));
                }
                if ("default".equals(entryElement.getKey())) {
                    Any a = entryElement.getValue();
                    propMap.put("default", a);
                }
                if ("enum".equals(entryElement.getKey())) {
                    // different generate format for number enum
                    if ("Integer".equals(type) || "Double".equals(type) || "Float".equals(type)
                            || "Long".equals(type) || "Short".equals(type) || "java.math.BigDecimal".equals(type)) {
                        propMap.put("isNumEnum", Any.wrap(true));
                    }
                    propMap.put("isEnum", Any.wrap(true));
                    propMap.put("nameWithEnum", Any.wrap(name.substring(0, 1).toUpperCase() + name.substring(1) + "Enum"));
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
                            propMap.put(COMPARATOR, Any.wrap("Arrays"));
                            propMap.put(HASHER, Any.wrap("Arrays"));
                            break;

                        case "byte":
                            ultimateType = "byte";
                            break;

                        default:
                            ultimateType = null;
                    }

                    if (ultimateType != null) {
                        propMap.put("type", Any.wrap(ultimateType));
                    }
                }

                if ("oneOf".equals(entryElement.getKey())) {
                    List<Any> list = entryElement.getValue().asList();
                    Any t = list.get(0).asMap().get("type");
                    if (t != null) {
                        propMap.put("type", Any.wrap(typeMapping.get(t.toString())));
                    } else {
                        // maybe reference? default type to object.
                        propMap.put("type", Any.wrap("Object"));
                    }
                }
                if ("anyOf".equals(entryElement.getKey())) {
                    List<Any> list = entryElement.getValue().asList();
                    Any t = list.get(0).asMap().get("type");
                    if (t != null) {
                        propMap.put("type", Any.wrap(typeMapping.get(t.toString())));
                    } else {
                        // maybe reference? default type to object.
                        propMap.put("type", Any.wrap("Object"));
                    }
                }
                if ("allOf".equals(entryElement.getKey())) {
                    List<Any> list = entryElement.getValue().asList();
                    Any t = list.get(0).asMap().get("type");
                    if (t != null) {
                        propMap.put("type", Any.wrap(typeMapping.get(t.toString())));
                    } else {
                        // maybe reference? default type to object.
                        propMap.put("type", Any.wrap("Object"));
                    }
                }
                if ("not".equals(entryElement.getKey())) {
                    Map<String, Any> m = entryElement.getValue().asMap();
                    Any t = m.get("type");
                    if (t != null) {
                        propMap.put("type", t);
                    } else {
                        propMap.put("type", Any.wrap("Object"));
                    }
                }
            }
            props.add(propMap);
        }
    }
    public static final String HASHER = "hasher";
    public static final String COMPARATOR = "comparator";

    private Any getListOf(String s) {
        return new UnresolvedTypeListAny(s);
    }

    private static abstract class UnresolvedTypeAny extends Any {

        Any type;

        UnresolvedTypeAny(Any type) {
            this.type = type;
        }

        private Any get() {
            return type;
        }

        private void set(Any type) {
            this.type = type;
        }

        @Override
        public Object object() {
            return toString();
        }

        @Override
        public boolean toBoolean() {
            return Boolean.parseBoolean(toString());
        }

        @Override
        public int toInt() {
            return Integer.parseInt(toString());
        }

        @Override
        public long toLong() {
            return Long.parseLong(toString());
        }

        @Override
        public float toFloat() {
            return Float.parseFloat(toString());
        }

        @Override
        public double toDouble() {
            return Double.parseDouble(toString());
        }
    }

    private static class UnresolvedTypeHolderAny extends UnresolvedTypeAny {

        UnresolvedTypeHolderAny(Any resolved) {
            super(resolved);
        }

        @Override
        public ValueType valueType() {
            return type.valueType();
        }

        @Override
        public String toString() {
            return type.toString();
        }

        @Override
        public void writeTo(JsonStream stream) throws IOException {
            type.writeTo(stream);
        }
    }

    private static class UnresolvedTypeListAny extends UnresolvedTypeAny {

        UnresolvedTypeListAny(Any type) {
            super(type);
        }

        UnresolvedTypeListAny(String string) {
            super(Any.wrap(string));
        }

        @Override
        public ValueType valueType() {
            return ValueType.ARRAY;
        }

        @Override
        public void writeTo(JsonStream stream) throws IOException {
            stream.writeRaw(toString());
        }

        @Override
        public String toString() {
            return UnresolvedTypeListAny.toString(type);
        }

        private static <T> String toString(Any type) {
            return String.format("java.util.List<%s>", type.toString());
        }
    }

    public List<Map<String, Object>> getOperationList(Object model) {
        List<Map<String, Object>> result = new ArrayList<>();
        String s;
        if (model instanceof Any) {
            s = ((Any)model).toString();
        } else if (model instanceof String) {
            s = (String)model;
        } else {
            throw new RuntimeException("Invalid Model Class: " + model.getClass());
        }
        OpenApi3 openApi3 = null;
        try {
            openApi3 = (OpenApi3)new OpenApiParser().parse(s, new URL("https://oas.lightapi.net/"));
        } catch (MalformedURLException e) {
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
                flattened.put("functionName", Utils.camelize(normalizedPath) + Utils.camelize(entryOps.getKey()) + "Function");
                flattened.put("endpoint", path + "@" + entryOps.getKey().toLowerCase());
                flattened.put("apiName", Utils.camelize(normalizedPath) + Utils.camelize(entryOps.getKey()));
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
                flattened.put("scopes", getScopes(operation));
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
    private Any getValidEnumName(Map.Entry<String, Any> entryElement) {
        Iterator<Any> iterator = entryElement.getValue().iterator();
        Map<String, Any> map = new HashMap<>();
        while (iterator.hasNext()) {
            String string = iterator.next().toString().trim();
            if (string.equals("")) continue;
            if (isEnumHasDescription(string)) {
                map.put(convertToValidJavaVariableName(getEnumName(string)).toUpperCase(), Any.wrap(getEnumDescription(string)));
            } else {
                map.put(convertToValidJavaVariableName(string).toUpperCase(), Any.wrap(string));
            }
        }
        return Any.wrap(map);
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

    private String getScopes(Operation operation) {
        String scopes = null;
        SecurityRequirement securityRequirement = operation.getSecurityRequirement(0);
        if(securityRequirement != null) {
            Map<String, SecurityParameter> requirements = securityRequirement.getRequirements();
            for(SecurityParameter parameter : requirements.values()) {
                List<String> ls = parameter.getParameters();
                if(ls != null) scopes = StringUtils.join(ls, ' ');
            }
        }
        return scopes;
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
                result = JsonStream.serialize(valueToBeStringify);
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
                    result.put("example", JsonStream.serialize(example));
                } else {
                    // check if there are multiple examples
                    Map<String, Example> exampleMap = mediaType.get().getExamples();
                    // use the first example if there are multiple
                    if (exampleMap.size() > 0) {
                        Map.Entry<String, Example> entry = exampleMap.entrySet().iterator().next();
                        Example e = entry.getValue();
                        if (e != null) {
                            result.put("statusCode", statusCode);
                            result.put("example", JsonStream.serialize(e.getValue()));
                        }
                    }
                }
            }
        }
        return result;
    }

    private static final Logger logger = LoggerFactory.getLogger(OpenApiGenerator.class);

    private void loadModel(String classVarName, String parentClassName, Map<String, Any> value, Any schemas, boolean overwriteModel, String targetPath, String modelPackage, List<Runnable> modelCreators, Map<String, Any> references, List<Map<String, Any>> parentClassProps) throws IOException {
        final String modelFileName = classVarName.substring(0, 1).toUpperCase() + classVarName.substring(1);
        final List<Map<String, Any>> props = new ArrayList<>();
        final List<Map<String, Any>> parentProps = (parentClassProps == null) ? new ArrayList<>() : new ArrayList<>(parentClassProps);
        String type = null;
        String enums = null;
        boolean isEnumClass = false;
        List<Any> required = null;
        boolean isAbstractClass = false;

        // iterate through each schema in the components
        Queue<Map.Entry<String, Any>> schemaElementQueue = new LinkedList<>();
        // cache the visited elements to prevent loop reference
        Set<String> seen = new HashSet<>();
        // add elements into queue to perform a BFS
        for (Map.Entry<String, Any> entrySchema : value.entrySet()) {
            schemaElementQueue.offer(entrySchema);
        }
        while (!schemaElementQueue.isEmpty()) {
            Map.Entry<String, Any> currentElement = schemaElementQueue.poll();
            String currentElementKey = currentElement.getKey();
            // handle the base elements
            if ("type".equals(currentElementKey) && type == null) {
                type = currentElement.getValue().toString();
            }
            if ("enum".equals(currentElementKey)) {
                isEnumClass = true;
                enums = currentElement.getValue().asList().toString();
                enums = enums.substring(enums.indexOf("[") + 1, enums.indexOf("]"));
            }
            if ("properties".equals(currentElementKey)) {
                handleProperties(props, currentElement.getValue().asMap());
            }
            if ("required".equals(currentElementKey)) {
                if (required == null) {
                    required = new ArrayList<>();
                }
                required.addAll(currentElement.getValue().asList());
            }
            // expend the ref elements and add to the queue
            if ("$ref".equals(currentElementKey)) {
                String s = currentElement.getValue().toString();
                s = s.substring(s.lastIndexOf('/') + 1);
                if (seen.contains(s)) continue;
                seen.add(s);
                for (Map.Entry<String, Any> schema : schemas.get(s).asMap().entrySet()) {
                    schemaElementQueue.offer(schema);
                }
            }
            // expand the allOf elements and add to the queue
            if ("allOf".equals(currentElementKey)) {
                for (Any listItem : currentElement.getValue().asList()) {
                    for (Map.Entry<String, Any> allOfItem : listItem.asMap().entrySet()) {
                        schemaElementQueue.offer(allOfItem);
                    }
                }
            }
            // call loadModel recursively to generate new model corresponding to each oneOf elements
            if ("oneOf".equals(currentElementKey)) {
                isAbstractClass = true;
                parentProps.addAll(props);
                String parentName = classVarName.substring(0, 1) + classVarName.substring(1);
                for (Any listItem : currentElement.getValue().asList()) {
                    for (Map.Entry<String, Any> oneOfItem : listItem.asMap().entrySet()) {
                        if ("$ref".equals(oneOfItem.getKey())) {
                            String s = oneOfItem.getValue().toString();
                            s = s.substring(s.lastIndexOf('/') + 1);
                            loadModel(extendModelName(s, classVarName), s, schemas.get(s).asMap(), schemas, overwriteModel, targetPath, modelPackage, modelCreators, references, parentProps);
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
                for (Map<String, Any> properties : props) {
                    Any any = properties.get("type");
                    if (any != null) {
                        if (any.valueType() == ValueType.STRING) {
                            Any resolved = references.get(any.toString());
                            if (resolved == null) {
                                continue;
                            }
                            any = new UnresolvedTypeHolderAny(resolved);
                            properties.put("type", any);
                        }

                        int iteration = 0;
                        do {
                            UnresolvedTypeAny previous = null;
                            while (any instanceof UnresolvedTypeAny) {
                                previous = (UnresolvedTypeAny)any;
                                any = ((UnresolvedTypeAny)any).get();
                            }

                            if (any == null) {
                                break;
                            } else if (iteration++ > referencesCount) {
                                throw new TypeNotPresentException(any.toString(), null);
                            }

                            if (any.valueType() == ValueType.STRING) {
                                any = references.get(any.toString());
                                if (any == null) {
                                    break;
                                } else {
                                    previous.set(any);
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
            HashMap<String, Any> map = new HashMap<>(1);
            map.put(classVarName, Any.wrap(value));
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

    private List<OpenApiPath> getPathList(List<Map<String, Object>> operationList) {
        List<OpenApiPath> pathList = new ArrayList<>();
        Set<String> pathSet = new HashSet<>();
        OpenApiPath openApiPath = null;
        for(Map<String, Object> op : operationList) {
            String path = (String)op.get("path");
            String method = ((String)op.get("method")).toLowerCase();
            String functionName = (String)op.get("functionName");
            if(!pathSet.contains(path)) {
                openApiPath = new OpenApiPath();
                openApiPath.setPath(path);
                pathSet.add(path);
                MethodFunction methodFunction = new MethodFunction(method, functionName);
                openApiPath.addMethodFunction(methodFunction);
                pathList.add(openApiPath);
            } else {
                MethodFunction methodFunction = new MethodFunction(method, functionName);
                openApiPath.addMethodFunction(methodFunction);
            }
        }
        return pathList;
    }

    public class OpenApiPath {
        String path;
        List<MethodFunction> methodList = new ArrayList<>();

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public List<MethodFunction> getMethodList() {
            return methodList;
        }

        public void addMethodFunction(MethodFunction methodFunction) {
            methodList.add(methodFunction);
        }

    }

    public class MethodFunction {
        String method;
        String functionName;

        public MethodFunction(String method, String functionName) {
            this.method = method;
            this.functionName = functionName;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getFunctionName() {
            return functionName;
        }

        public void setFunctionName(String functionName) {
            this.functionName = functionName;
        }
    }

}
