package com.networknt.codegen.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.codegen.Generator;
import com.networknt.config.JsonMapper;
import com.networknt.jsonoverlay.Overlay;
import com.networknt.oas.OpenApiParser;
import com.networknt.oas.model.OpenApi3;
import com.networknt.oas.model.impl.OpenApi3Impl;
import com.networknt.utility.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

import static java.io.File.separator;

public class OpenApiLambdaGenerator implements OpenApiGenerator {

    public static final String FRAMEWORK="openapilambda";

    @Override
    public String getFramework() {
        return FRAMEWORK;
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
        // Lambda specific config
        boolean packageDocker = isPackageDocker(config, null);
        boolean useLightProxy = isUseLightProxy(config, null);
        boolean buildMaven = isBuildMaven(config, null);
        String launchType = getLaunchType(config, null);
        String region = getRegion(config, null);
        boolean publicVpc = isPublicVpc(config, null);
        // Common config
        String rootPackage = getRootPackage(config, null);
        String modelPackage = getModelPackage(config, null);
        String handlerPackage = getHandlerPackage(config, null);
        String servicePackage = getServicePackage(config, null);
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
        boolean multipleModule = isMultipleModule(config, null);

        // get the list of operations for this model
        List<Map<String, Object>> operationList = getOperationList(model, config);
        List<OpenApiPath> pathList = getPathList(operationList);
        transfer(targetPath, "", ".gitignore", templates.lambda.gitignore.template());

        transfer(targetPath, "", "README.md", templates.lambda.README.template(artifactId, packageDocker, operationList));

        if(!useLightProxy) {
            // use AWS API Gateway to access Lambda functions.
            transfer(targetPath, "", "template.yaml", templates.lambda.template.template(artifactId, handlerPackage, packageDocker, useLightProxy, operationList, pathList));
        } else {
            // use light-proxy for Lambda function
            if("EC2".equals(launchType)) {
                if(publicVpc) {
                    transfer(targetPath, "", "public-vpc.yaml", templates.lambda.EC2.publicVpcYaml.template());
                    transfer(targetPath, "", "public-proxy.yaml", templates.lambda.EC2.publicProxyYaml.template());
                    transfer(targetPath, "", "template.yaml", templates.lambda.template.template(artifactId, handlerPackage, packageDocker, useLightProxy, operationList, pathList));
                } else {
                    transfer(targetPath, "", "private-vpc.yaml", templates.lambda.EC2.privateVpcYaml.template());
                    transfer(targetPath, "", "private-proxy.yaml", templates.lambda.EC2.privateProxyYaml.template());
                    transfer(targetPath, "", "template.yaml", templates.lambda.template.template(artifactId, handlerPackage, packageDocker, useLightProxy, operationList, pathList));
                }
            } else {
                // fargate as default
                if(publicVpc) {
                    transfer(targetPath, "", "public-vpc.yaml", templates.lambda.Fargate.publicVpcYaml.template());
                    transfer(targetPath, "", "public-proxy.yaml", templates.lambda.Fargate.publicProxyYaml.template());
                    transfer(targetPath, "", "template.yaml", templates.lambda.template.template(artifactId, handlerPackage, packageDocker, useLightProxy, operationList, pathList));
                } else {
                    transfer(targetPath, "", "private-vpc.yaml", templates.lambda.Fargate.privateVpcYaml.template());
                    transfer(targetPath, "", "private-proxy.yaml", templates.lambda.Fargate.privateProxyYaml.template());
                    transfer(targetPath, "", "template.yaml", templates.lambda.template.template(artifactId, handlerPackage, packageDocker, useLightProxy, operationList, pathList));
                }
            }
            transfer(targetPath, "proxy", "handler.yml",
                    templates.lambda.proxy.handlerYml.template(serviceId, handlerPackage, operationList, prometheusMetrics));

            transfer(targetPath, "proxy", "lambda-invoker.yml",
                    templates.lambda.proxy.lambdaInvokerYml.template(region, operationList));
            try (InputStream is = new ByteArrayInputStream(Generator.yamlMapper.writeValueAsBytes(model))) {
                Generator.copyFile(is, Paths.get(targetPath, "proxy", "openapi.yaml"));
            }
            transfer(targetPath, "proxy", "server.yml", templates.lambda.proxy.server.template(serviceId, enableRegistry, version));

            transfer(targetPath, "proxy", "primary.crt", templates.rest.primaryCrt.template());
            transfer(targetPath, "proxy", "secondary.crt", templates.rest.secondaryCrt.template());
            // transfer binary files without touching them.
            try (InputStream is = OpenApiLambdaGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
                Generator.copyFile(is, Paths.get(targetPath, "proxy", "server.keystore"));
            }
            try (InputStream is = OpenApiLambdaGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
                Generator.copyFile(is, Paths.get(targetPath, "proxy", "server.truststore"));
            }
            try (InputStream is = OpenApiLambdaGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Generator.copyFile(is, Paths.get(targetPath, "proxy", "client.keystore"));
            }
            try (InputStream is = OpenApiLambdaGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Generator.copyFile(is, Paths.get(targetPath, "proxy", "client.truststore"));
            }
            // logging
            transfer(targetPath, "proxy", "logback.xml", templates.rest.logback.template(rootPackage));
            // proxy.yml
            transfer(targetPath, "proxy", "proxy.yml", templates.lambda.proxy.proxy.template());

            // exclusion list for Config module
            // values.yml file, transfer to suppress the warning message during start startup and encourage usage.
            transfer(targetPath, "proxy", "values.yml", templates.rest.values.template(serviceId, enableHttp, httpPort, enableHttps, httpsPort, enableHttp2, enableRegistry, version, config));
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
                        loadModel(multipleModule, entry.getKey(), null, (Map<String, Object>)entry.getValue(), schemas, overwriteModel, targetPath +  separator + functionName, modelPackage, modelCreators, references, null, callback);
                    }

                    for (Runnable r : modelCreators) {
                        r.run();
                    }
                }
            }

            if(!useLightProxy) {
                try (InputStream is = new ByteArrayInputStream(Generator.yamlMapper.writeValueAsBytes(model))) {
                    Generator.copyFile(is, Paths.get(targetPath + separator + functionName, ("src.main.resources").replace(".", separator), "openapi.yaml"));
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
                    Generator.copyFile(is, Paths.get(targetPath + separator + functionName, ("src.main.resources").replace(".", separator), "prod.truststore"));
                }
            }
        }
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

    private boolean isPackageDocker(JsonNode config, Boolean defaultValue) {
        boolean packageDocker = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("packageDocker");
        if(jsonNode == null) {
            ((ObjectNode)config).put("packageDocker", packageDocker);
        } else {
            packageDocker = jsonNode.booleanValue();
        }
        return packageDocker;
    }

    @Override
    public boolean isUseLightProxy(JsonNode config, Boolean defaultValue) {
        boolean useLightProxy = defaultValue == null ? true : defaultValue;
        JsonNode jsonNode = config.get("useLightProxy");
        if(jsonNode == null) {
            ((ObjectNode)config).put("useLightProxy", useLightProxy);
        } else {
            useLightProxy = jsonNode.booleanValue();
        }
        return useLightProxy;
    }

    private boolean isPublicVpc(JsonNode config, Boolean defaultValue) {
        boolean publicVpc = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("publicVpc");
        if(jsonNode == null) {
            ((ObjectNode)config).put("publicVpc", publicVpc);
        } else {
            publicVpc = jsonNode.booleanValue();
        }
        return publicVpc;
    }

    private String getLaunchType(JsonNode config, String defaultValue) {
        String launchType = defaultValue == null ? "EC2" : defaultValue;
        JsonNode jsonNode = config.get("launchType");
        if(jsonNode == null) {
            ((ObjectNode)config).put("launchType", launchType);
        } else {
            launchType = jsonNode.textValue();
        }
        return launchType;
    }

    private String getRegion(JsonNode config, String defaultValue) {
        String region = defaultValue == null ? "us-east-1" : defaultValue;
        JsonNode jsonNode = config.get("region");
        if(jsonNode == null) {
            ((ObjectNode)config).put("region", region);
        } else {
            region = jsonNode.textValue();
        }
        return region;
    }

    ModelCallback callback = new ModelCallback() {
        @Override
        public void callback(boolean multipleModule, String targetPath, String modelPackage, String modelFileName, String enumsIfClass, String parentClassName, String classVarName, boolean abstractIfClass, List<Map<String, Object>> props, List<Map<String, Object>> parentClassProps) {
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
        }
    };

}
