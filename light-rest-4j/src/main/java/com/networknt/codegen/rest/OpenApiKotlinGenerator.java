package com.networknt.codegen.rest;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.io.File.separator;

public class OpenApiKotlinGenerator implements OpenApiGenerator {

    public static final String FRAMEWORK="openapikotlin";

    @Override
    public String getFramework() {
        return FRAMEWORK;
    }

    /**
     *
     * @param targetPath The output directory of the generated project
     * @param model The optional model data that trigger the generation, i.e. swagger specification, graphql IDL etc.
     * @param config A json object that controls how the generator behaves.
     * @throws IOException IO Exception occurs during code generation
     */
    @Override
    public void generate(String targetPath, Object model, JsonNode config) throws IOException {
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
        boolean useLightProxy = isUseLightProxy(config, null);
        String kafkaTopic = getKafkaTopic(config, null);
        String decryptOption = getDecryptOption(config, null);
        boolean buildMaven = isBuildMaven(config, null);
        // get the list of operations for this model
        List<Map<String, Object>> operationList = getOperationList(model, config);

        // bypass project generation if the mode is the only one requested to be built
        if(!generateModelOnly) {
            // if set to true, regenerate the code only (handlers, model and the handler.yml, potentially affected by operation changes
            if (!specChangeCodeReGenOnly) {
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
                transfer(targetPath, "", "build.sh", templates.restkotlin.buildSh.template(config, serviceId));
                transfer(targetPath, "", ".gitignore", templates.restkotlin.gitignore.template());
                transfer(targetPath, "", "README.md", templates.restkotlin.README.template());
                transfer(targetPath, "", "LICENSE", templates.restkotlin.LICENSE.template());

                if(buildMaven) {
                    transferMaven(targetPath);
                } else {
                    transfer(targetPath, "", "build.gradle.kts", templates.restkotlin.buildGradleKts.template(config));
                    transfer(targetPath, "", "gradle.properties", templates.restkotlin.gradleProperties.template(config));
                    transfer(targetPath, "", "settings.gradle.kts", templates.restkotlin.settingsGradleKts.template(config));
                    transferGradle(targetPath);
                }

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
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "handler.yml", templates.restkotlin.openapi.handlerYml.template(serviceId, handlerPackage, operationList, prometheusMetrics, useLightProxy));

                // exclusion list for Config module
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "config.yml", templates.restkotlin.openapi.config.template());
                // added with #471
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "app-status.yml", templates.restkotlin.appStatusYml.template());
                // values.yml file, transfer to suppress the warning message during start startup and encourage usage.
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "values.yml", templates.restkotlin.openapi.values.template());
            }
        }

        // model
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
                    loadModel(entry.getKey(), null, (Map<String, Object>)entry.getValue(), schemas, overwriteModel, targetPath, modelPackage, modelCreators, references, null, callback);
                }

                for (Runnable r : modelCreators) {
                    r.run();
                }
            }
        }

        // exit after generating the model if the consumer needs only the model classes
        if(generateModelOnly)
            return;

        // handler
        for(Map<String, Object> op : operationList){
            String className = op.get("handlerName").toString();
            @SuppressWarnings("unchecked")
            List<Map> parameters = (List<Map>)op.get("parameters");
            Map<String, String> responseExample = (Map<String, String>)op.get("responseExample");
            String example = responseExample.get("example");
            String statusCode = responseExample.get("statusCode");
            statusCode = StringUtils.isBlank(statusCode) || statusCode.equals("default") ? "-1" : statusCode;
            if (checkExist(targetPath, ("src.main.kotlin." + handlerPackage).replace(".", separator), className + ".kt") && !overwriteHandler) {
                continue;
            }
            transfer(targetPath, ("src.main.kotlin." + handlerPackage).replace(".", separator), className + ".kt", templates.restkotlin.handler.template(handlerPackage, className, example, parameters));
        }

        // handler test cases
        if (!specChangeCodeReGenOnly) {
            transfer(targetPath, ("src.test.kotlin." + handlerPackage + ".").replace(".", separator), "LightTestServer.kt", templates.restkotlin.lightTestServerKt.template(handlerPackage));
        }
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

        try (InputStream is = new ByteArrayInputStream(Generator.yamlMapper.writeValueAsBytes(model))) {
            Generator.copyFile(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "openapi.yaml"));
        }
    }

    ModelCallback callback = new ModelCallback() {
        @Override
        public void callback(String targetPath, String modelPackage, String modelFileName, String enumsIfClass, String parentClassName, String classVarName, boolean abstractIfClass, List<Map<String, Object>> props, List<Map<String, Object>> parentClassProps) {
            try {
                transfer(targetPath,
                        ("src.main.kotlin." + modelPackage).replace(".", separator),
                        modelFileName + ".kt",
                        enumsIfClass == null
                                ? templates.restkotlin.pojo.template(modelPackage, modelFileName, classVarName, props)
                                : templates.restkotlin.enumClass.template(modelPackage, modelFileName, enumsIfClass));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    };

}
