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
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.networknt.codegen.Generator.copyFile;
import static java.io.File.separator;

/**
 * The input for OpenAPI 3.0 generator include config with json format and OpenAPI specification in yaml format.
 *
 * The model is OpenAPI spec in yaml format. And config file is config.json in JSON format.
 *
 * @author Steve Hu
 */
public class OpenApiGenerator implements Generator {

    private Map<String, String> typeMapping = new HashMap<>();

    // optional generation parameters. if not set, they use default values as
    boolean prometheusMetrics = false;
    boolean skipHealthCheck = false;
    boolean skipServerInfo = false;
    boolean specChangeCodeReGenOnly = false;
    boolean enableParamDescription = true;
    boolean generateModelOnly = false;
    boolean generateValuesYml = false;
    boolean skipPomFile = false;

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
    public void generate(final String targetPath, Object model, Any config) throws IOException {
        // whoever is calling this needs to make sure that model is converted to Map<String, Object>
        String rootPackage = config.toString("rootPackage").trim();
        final String modelPackage = config.toString("modelPackage").trim();
        String handlerPackage = config.toString("handlerPackage").trim();

        boolean overwriteHandler = config.toBoolean("overwriteHandler");
        boolean overwriteHandlerTest = config.toBoolean("overwriteHandlerTest");
        boolean overwriteModel = config.toBoolean("overwriteModel");
        generateModelOnly = config.toBoolean("generateModelOnly");

        boolean enableHttp = config.toBoolean("enableHttp");
        String httpPort = config.toString("httpPort").trim();
        boolean enableHttps = config.toBoolean("enableHttps");
        String httpsPort = config.toString("httpsPort").trim();

        boolean enableRegistry = config.toBoolean("enableRegistry");
        boolean eclipseIDE = config.toBoolean("eclipseIDE");
        boolean supportClient = config.toBoolean("supportClient");
        String dockerOrganization = config.toString("dockerOrganization").trim();

        prometheusMetrics = config.toBoolean("prometheusMetrics");
        skipHealthCheck = config.toBoolean("skipHealthCheck");
        skipServerInfo = config.toBoolean("skipServerInfo");
        specChangeCodeReGenOnly = config.toBoolean("specChangeCodeReGenOnly");
        enableParamDescription = config.toBoolean("enableParamDescription");
        skipPomFile = config.toBoolean("skipPomFile");

        generateValuesYml = config.toBoolean("generateValuesYml");

        String version = config.toString("version").trim();
        String serviceId = config.get("groupId").toString().trim() + "." + config.get("artifactId").toString().trim() + "-" + config.get("version").toString().trim();

        if (dockerOrganization == null || dockerOrganization.length() == 0) {
            dockerOrganization = "networknt";
        }

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
                transfer(targetPath, "", "kubernetes.yml", templates.rest.kubernetes.template(dockerOrganization, serviceId, config.get("artifactId").toString().trim(), expose, version));
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
                        templates.rest.server.template(serviceId, enableHttp, httpPort, enableHttps, httpsPort, enableRegistry, version));
                transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml",
                        templates.rest.server.template(serviceId, enableHttp, "49587", enableHttps, "49588", enableRegistry, version));

                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "secret.yml", templates.rest.secret.template());
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "openapi-security.yml", templates.rest.openapiSecurity.template());
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "openapi-validator.yml", templates.rest.openapiValidator.template());
                if (supportClient) {
                    transfer(targetPath, ("src.main.resources.config").replace(".", separator), "client.yml", templates.rest.clientYml.template());
                } else {
                    transfer(targetPath, ("src.test.resources.config").replace(".", separator), "client.yml", templates.rest.clientYml.template());
                }

                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "primary.crt", templates.rest.primaryCrt.template());
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "secondary.crt", templates.rest.secondaryCrt.template());

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

                // values.yml file, transfer only if explicitly set in the config.json
                if (generateValuesYml) {
                    transfer(targetPath, ("src.main.resources.config").replace(".", separator), "values.yml", templates.rest.openapi.values.template());
                }
            }
            // routing handler
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "handler.yml",
                    templates.rest.openapi.handlerYml.template(serviceId, handlerPackage, operationList, prometheusMetrics, !skipHealthCheck, !skipServerInfo));

        }

        // model
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
                    final List<Map<String, Any>> props = new ArrayList<>();
                    String key = entry.getKey();
                    Map<String, Any> value = entry.getValue().asMap();
                    String type = null;
                    String enums = null;
                    boolean isEnum = false;
                    boolean isEnumClass = false;
                    // Map<String, Any> properties = null;
                    List<Any> required = null;

                    // iterate through each schema in the components
                    for (Map.Entry<String, Any> entrySchema : value.entrySet()) {
                        if ("type".equals(entrySchema.getKey())) {
                            type = entrySchema.getValue().toString();
                            if ("enum".equals(type)) {
                                isEnum = true;
                            }
                        }
                        if ("enum".equals(entrySchema.getKey())) {
                            isEnumClass = true;
                            enums = entrySchema.getValue().asList().toString();
                            enums = enums.substring(enums.indexOf("[") + 1, enums.indexOf("]"));
                        }
                        if ("properties".equals(entrySchema.getKey())) {
                            handleProperties(props, entrySchema.getValue().asMap());
                        }
                        if ("required".equals(entrySchema.getKey())) {
                            required = entrySchema.getValue().asList();
                        }
                        if ("allOf".equals(entrySchema.getKey())) {
                            type = "object";

                            // could be referred to as "$ref" references or listed in "properties"
                            for (Any listItem : entrySchema.getValue().asList()) {
                                //Map<String, Any> allOfItem = (Map<String, Any>)listItem.asMap().entrySet();

                                for (Map.Entry<String, Any> allOfItem : listItem.asMap().entrySet()) {
                                    if ("$ref".equals(allOfItem.getKey())) {
                                        String s = allOfItem.getValue().toString();
                                        s = s.substring(s.lastIndexOf('/') + 1);
                                        handleProperties(props, schemas.get(s).get("properties").asMap());
                                    }
                                    if ("properties".equals(allOfItem.getKey())) {
                                        handleProperties(props, allOfItem.getValue().asMap());
                                    }
                                }
                            }
                        }
                    }
                    final String classVarName = key;
                    final String modelFileName = key.substring(0, 1).toUpperCase() + key.substring(1);
                    //System.out.println("props = " + Any.wrap(props));

                    // Check the type of current schema. Generation will be executed only if the type of the schema equals to object.
                    // Since generate a model for primitive types and arrays do not make sense, and an error class would be generated
                    // due to lack of properties if force to generate.
                    if (type == null) {
                        throw new RuntimeException("Cannot find the type of \"" + modelFileName + "\" in #/components/schemas/ of the specification file.");
                    }

                    if ("object".equals(type) || isEnumClass) {
                        if (!overwriteModel && checkExist(targetPath, ("src.main.java." + modelPackage).replace(".", separator), modelFileName + ".java")) {
                            continue;
                        }

                        final String enumsIfClass = isEnumClass ? enums : null;
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
                                                ? templates.rest.pojo.template(modelPackage, modelFileName, classVarName, props)
                                                : templates.rest.enumClass.template(modelPackage, modelFileName, enumsIfClass));
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                    } else {
                        HashMap<String, Any> map = new HashMap<>(1);
                        map.put(key, Any.wrap(value));
                        handleProperties(props, map);
                        if (props.isEmpty()) {
                            throw new IllegalStateException("Properties empty for " + classVarName + "!");
                        }

                        references.put(modelFileName, props.get(0).get("type"));
                    }
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
            copyFile(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "server.keystore"));
        }
        try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
            copyFile(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "server.truststore"));
        }
        if (supportClient) {
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                copyFile(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "client.keystore"));
            }
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                copyFile(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "client.truststore"));
            }
        } else {
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                copyFile(is, Paths.get(targetPath, ("src.test.resources.config").replace(".", separator), "client.keystore"));
            }
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                copyFile(is, Paths.get(targetPath, ("src.test.resources.config").replace(".", separator), "client.truststore"));
            }
        }

        if (model instanceof Any) {
            try (InputStream is = new ByteArrayInputStream(model.toString().getBytes(StandardCharsets.UTF_8))) {
                copyFile(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "openapi.json"));
            }
        } else if (model instanceof String) {
            try (InputStream is = new ByteArrayInputStream(((String)model).getBytes(StandardCharsets.UTF_8))) {
                copyFile(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "openapi.yaml"));
            }
        }
    }

    /**
     * Initialize the property map with base elements as name, getter, setters, etc
     *
     * @param entry The entry for which to generate
     * @param propMap The property map to add to, created in the caller
     */
    private void initializePropertyMap(Entry<String, Any> entry, Map<String, Any> propMap) {
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
		            this.attachValidEnumName(entryElement);
		            propMap.put("value", entryElement.getValue());
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
    private static void attachValidEnumName(Map.Entry<String, Any> entryElement) {
        Iterator<Any> iterator = entryElement.getValue().iterator();
        Map<String, Any> map = new HashMap<>();
        while (iterator.hasNext()) {
            String string = iterator.next().toString().trim();
            if (string.equals("")) continue;
            map.put(convertToValidJavaVariableName(string).toUpperCase(), Any.wrap(string));
        }
        entryElement.setValue(Any.wrap(map));
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

    private String populateRequestBodyExample(Operation operation) {
        String result = "{\"content\": \"request body to be replaced\"}";
        RequestBody body = operation.getRequestBody();
        if (body != null) {
            MediaType mediaType = body.getContentMediaType("application/json");
            if (mediaType != null) {
                Object valueToBeStringify = null;
                if (mediaType.getExamples() != null && !mediaType.getExamples().isEmpty()) {
                    for (Entry<String, Example> entry : mediaType.getExamples().entrySet()) {
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

}
