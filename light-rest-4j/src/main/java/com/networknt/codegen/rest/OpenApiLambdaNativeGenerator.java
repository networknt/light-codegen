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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.io.File.separator;

public class OpenApiLambdaNativeGenerator extends AbstractLambdaGenerator implements OpenApiGenerator {

    public static final String FRAMEWORK="lambda-native";

    /**
     *
     * @param targetPath The output directory of the generated project
     * @param model The optional model data that trigger the generation, i.e. swagger specification, graphql IDL etc.
     * @param config A json object that controls how the generator behaves.
     *
     * @throws IOException IO Exception occurs during code generation
     */
    @Override
    public void generate(String targetPath, Object model, JsonNode config) throws IOException {
        // Lambda specific config
        boolean packageDocker = isPackageDocker(config, null);
        boolean buildMaven = isBuildMaven(config, null);
        String lambdaTrigger = getLambdaTrigger(config, null);  // NONE, HTTP, REST, URL, ALB

        String region = getRegion(config, null);
        // Common config
        String rootPackage = getRootPackage(config, null);
        String modelPackage = getModelPackage(config, null);
        String handlerPackage = getHandlerPackage(config, null);
        String servicePackage = getServicePackage(config, null);
        boolean overwriteHandler = isOverwriteHandler(config, null);
        boolean overwriteHandlerTest = isOverwriteHandlerTest(config, null);
        boolean overwriteModel = isOverwriteModel(config, null);
        boolean generateModelOnly = isGenerateModelOnly(config, null);
        boolean enableRegistry = isEnableRegistry(config, null);
        boolean supportClient = isSupportClient(config, null);
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
        List<OpenApiLambdaGenerator.OpenApiPath> pathList = getPathList(operationList);
        transfer(targetPath, "", ".gitignore", templates.lambdanative.gitignore.template());

        transfer(targetPath, "", "README.md", templates.lambdanative.README.template(artifactId, packageDocker, operationList));
        transfer(targetPath, "", "template.yaml", templates.lambdanative.template.template(artifactId, serviceId, handlerPackage, packageDocker, lambdaTrigger, operationList, pathList));
        transfer(targetPath, "config", "values.yml",
                templates.lambdanative.values.template(region, operationList, serviceId));

        try (InputStream is = new ByteArrayInputStream(Generator.yamlMapper.writeValueAsBytes(model))) {
            Generator.copyFile(is, Paths.get(targetPath, "config", "openapi.yaml"));
        }

        // handler
        for (Map<String, Object> op : operationList) {
            // for each operation, we need to generate a function in a separate folder.
            String functionName = op.get("functionName").toString();

            // generate event.json
            transfer(targetPath, "events", "event" + functionName + ".json", templates.lambdanative.event.template());


            // generate Dockerfile if packageDocker is true
            if(packageDocker) {
                transfer(targetPath, functionName, "Dockerfile", templates.lambdanative.Dockerfile.template(handlerPackage));
            }

            if(buildMaven) {
                // generate pom.xml
                transfer(targetPath, functionName, "pom.xml", templates.lambdanative.pom.template(config, functionName));
                transferMaven(targetPath + separator + functionName);
            } else {
                transfer(targetPath, functionName, "build.gradle", templates.lambdanative.buildGradle.template(config));
                transfer(targetPath, functionName, "gradle.properties", templates.lambdanative.gradleProperties.template());
                transferGradle(targetPath + separator + functionName);
                transfer(targetPath, functionName, "bootstrap", templates.lambdanative.bootstrap.template());
                transfer(targetPath, functionName, "build_graalvm.sh", templates.lambdanative.buildGraalvmSh.template(functionName));
                transfer(targetPath, functionName, "reflect.json", templates.lambdanative.reflectJson.template(handlerPackage));
                transfer(targetPath, functionName, "resource-config.json", templates.lambdanative.resourceJson.template());
                transfer(targetPath, functionName, "Makefile", templates.lambdanative.Makefile.template(functionName));

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
                transfer(targetPath + separator + functionName, ("src.main.java." + handlerPackage).replace(".", separator), "BusinessHandler.java", templates.lambdanative.BusinessHandler.template(handlerPackage, example));
            }
            if (checkExist(targetPath + separator + functionName, ("src.test.java." + handlerPackage).replace(".", separator), "BusinessHandlerTest.java")) {
                continue;
            } else {
                transfer(targetPath + separator + functionName, ("src.test.java." + handlerPackage).replace(".", separator), "BusinessHandlerTest.java", templates.lambdanative.BusinessHandlerTest.template(handlerPackage, op));
            }
            transfer(targetPath + separator + functionName, ("src.main.java." + handlerPackage).replace(".", separator), "App.java", templates.lambdanative.App.template(handlerPackage));
            transfer(targetPath + separator + functionName, ("src.test.java." + handlerPackage).replace(".", separator), "AppTest.java", templates.lambdanative.AppTest.template(handlerPackage));

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

            // logback.xml
            transfer(targetPath + separator + functionName, ("src.main.resources").replace(".", separator), "logback.xml", templates.lambdanative.logback.template(rootPackage));
            transfer(targetPath + separator + functionName, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.lambdanative.logback.template(rootPackage));
        }
    }

    public String getLambdaTrigger(JsonNode config, String defaultValue) {
        String lambdaTrigger = defaultValue;
        JsonNode jsonNode = config.get("lambdaTrigger");
        if(jsonNode == null) {
            ((ObjectNode)config).put("lambdaTrigger", lambdaTrigger);
        } else {
            lambdaTrigger = jsonNode.textValue();
        }
        return lambdaTrigger;
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

    @Override
    public String getFramework() {
        return FRAMEWORK;
    }
}
