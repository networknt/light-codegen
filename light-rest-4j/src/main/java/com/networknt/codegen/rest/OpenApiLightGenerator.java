package com.networknt.codegen.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.codegen.Generator;
import com.networknt.config.JsonMapper;
import com.networknt.jsonoverlay.Overlay;
import com.networknt.oas.OpenApiParser;
import com.networknt.oas.model.*;
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

public class OpenApiLightGenerator implements OpenApiGenerator {
    public static final String FRAMEWORK="openapi";

    @Override
    public String getFramework() {
        return FRAMEWORK;
    }

    public OpenApiLightGenerator() {
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
        boolean useLightProxy = isUseLightProxy(config, null);
        String kafkaTopic = getKafkaTopic(config, null);
        String decryptOption = getDecryptOption(config, null);
        boolean supportDb = isSupportDb(config, null);
        boolean supportH2ForTest = isSupportH2ForTest(config, null);
        boolean buildMaven = isBuildMaven(config, true); // override the default value false to make sure backward compatible.
        boolean multipleModule = isMultipleModule(config, null);
        String configFolder = multipleModule ? "server.src.main.resources.config" : "src.main.resources.config";
        String testConfigFolder = multipleModule ? "server.src.test.resources.config" : "src.test.resources.config";

        // get the list of operations for this model
        List<Map<String, Object>> operationList = getOperationList(model, config);

        // bypass project generation if the mode is the only one requested to be built
        if (!generateModelOnly) {
            // if set to true, regenerate the code only (handlers, model and the handler.yml, potentially affected by operation changes
            if (!specChangeCodeReGenOnly) {
                // generate configurations, project, masks, certs, etc

                if(buildMaven) {
                    if (!skipPomFile) {
                        if(multipleModule) {
                            transfer(targetPath, "", "pom.xml", templates.rest.parent.pom.template(config));
                            transfer(targetPath, "model", "pom.xml", templates.rest.model.pom.template(config));
                            transfer(targetPath, "service", "pom.xml", templates.rest.service.pom.template(config));
                            transfer(targetPath, "server", "pom.xml", templates.rest.server.pom.template(config));
                            transfer(targetPath, "client", "pom.xml", templates.rest.client.pom.template(config));
                        } else {
                            transfer(targetPath, "", "pom.xml", templates.rest.single.pom.template(config));
                        }
                    }
                    transferMaven(targetPath);
                } else {
                    if(multipleModule) {
                        transfer(targetPath, "", "build.gradle.kts", templates.rest.parent.buildGradleKts.template(config));
                        transfer(targetPath, "", "gradle.properties", templates.rest.parent.gradleProperties.template(config));
                        transfer(targetPath, "", "settings.gradle.kts", templates.rest.parent.settingsGradleKts.template());

                        transfer(targetPath, "model", "build.gradle.kts", templates.rest.model.buildGradleKts.template(config));
                        transfer(targetPath, "model", "settings.gradle.kts", templates.rest.model.settingsGradleKts.template());

                        transfer(targetPath, "service", "build.gradle.kts", templates.rest.service.buildGradleKts.template(config));
                        transfer(targetPath, "service", "settings.gradle.kts", templates.rest.service.settingsGradleKts.template());

                        transfer(targetPath, "server", "build.gradle.kts", templates.rest.server.buildGradleKts.template(config));
                        transfer(targetPath, "server", "settings.gradle.kts", templates.rest.server.settingsGradleKts.template());

                        transfer(targetPath, "client", "build.gradle.kts", templates.rest.client.buildGradleKts.template(config));
                        transfer(targetPath, "client", "settings.gradle.kts", templates.rest.client.settingsGradleKts.template());
                    } else {
                        transfer(targetPath, "", "build.gradle.kts", templates.rest.single.buildGradleKts.template(config));
                        transfer(targetPath, "", "gradle.properties", templates.rest.single.gradleProperties.template(config));
                        transfer(targetPath, "", "settings.gradle.kts", templates.rest.single.settingsGradleKts.template());
                    }
                    transferGradle(targetPath);
                }

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
                transfer(targetPath, "", "build.sh", templates.rest.buildSh.template(config, serviceId));
                transfer(targetPath, "", "kubernetes.yml", templates.rest.kubernetes.template(dockerOrganization, serviceId, config.get("artifactId").textValue(), expose, version));
                transfer(targetPath, "", ".gitignore", templates.rest.gitignore.template());
                transfer(targetPath, "", "README.md", templates.rest.README.template(config));
                transfer(targetPath, "", "LICENSE", templates.rest.LICENSE.template());
                if(eclipseIDE) {
                    transfer(targetPath, "", ".classpath", templates.rest.classpath.template());
                    transfer(targetPath, "", ".project", templates.rest.project.template(config));
                }
                // config
                transfer(targetPath, (configFolder).replace(".", separator), "primary.crt", templates.rest.primaryCrt.template());
                transfer(targetPath, (configFolder).replace(".", separator), "secondary.crt", templates.rest.secondaryCrt.template());
                if(kafkaProducer) {
                    transfer(targetPath, (configFolder).replace(".", separator), "kafka-producer.yml", templates.rest.kafkaProducerYml.template(kafkaTopic));
                }
                if(kafkaConsumer) {
                    transfer(targetPath, (configFolder).replace(".", separator), "kafka-streams.yml", templates.rest.kafkaStreamsYml.template(artifactId));
                }
                if(supportAvro) {
                    transfer(targetPath, (configFolder).replace(".", separator), "schema-registry.yml", templates.rest.schemaRegistryYml.template());
                }

                // logging
                transfer(targetPath, (multipleModule ? "server.src.main.resources" : "src.main.resources").replace(".", separator), "logback.xml", templates.rest.logback.template(rootPackage));
                transfer(targetPath, (multipleModule ? "server.src.test.resources" : "src.test.resources").replace(".", separator), "logback-test.xml", templates.rest.logback.template(rootPackage));

                // values.yml file, transfer to suppress the warning message during start startup and encourage usage.
                transfer(targetPath, (configFolder).replace(".", separator), "values.yml",
                        templates.rest.values.template(serviceId, enableHttp, httpPort, enableHttps, httpsPort, enableHttp2, enableRegistry, version, config));
                transfer(targetPath, (testConfigFolder).replace(".", separator), "values.yml",
                        templates.rest.values.template(serviceId, enableHttp, "49587", enableHttps, "49588", enableHttp2, enableRegistry, version, config));

            }
            // routing handler
            transfer(targetPath, (configFolder).replace(".", separator), "handler.yml",
                    templates.rest.handlerYml.template(serviceId, handlerPackage, operationList, prometheusMetrics, useLightProxy));

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
                    loadModel(multipleModule, entry.getKey(), null, (Map<String, Object>)entry.getValue(), schemas, overwriteModel, targetPath, modelPackage, modelCreators, references, null, callback);
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
        String serverFolder = multipleModule ? "server.src.main.java." + handlerPackage : "src.main.java." + handlerPackage;
        String serviceFolder = multipleModule ? "service.src.main.java." + servicePackage : "src.main.java." + servicePackage;
        String serverTestFolder = multipleModule ? "server.src.test.java." + handlerPackage : "src.test.java." + handlerPackage;
        for (Map<String, Object> op : operationList) {
            String className = op.get("handlerName").toString();
            String serviceName = op.get("serviceName").toString();
            Object requestModelNameObject = op.get("requestModelName");
            String requestModelName = null;
            if(requestModelNameObject != null) {
                requestModelName = requestModelNameObject.toString();
            }
            @SuppressWarnings("unchecked")
            List<Map> parameters = (List<Map>)op.get("parameters");
            Map<String, String> responseExample = (Map<String, String>)op.get("responseExample");
            String example = responseExample.get("example");
            String statusCode = responseExample.get("statusCode");
            statusCode = StringUtils.isBlank(statusCode) || statusCode.equals("default") ? "-1" : statusCode;

            if (multipleModule) {
                transfer(targetPath, (serverFolder).replace(".", separator), className + ".java", templates.rest.handler.template(handlerPackage, servicePackage, modelPackage, className, serviceName, requestModelName, parameters));
            } else {
                transfer(targetPath, (serverFolder).replace(".", separator), className + ".java", templates.rest.handlerSingle.template(handlerPackage, servicePackage, modelPackage, className, serviceName, requestModelName, example, parameters));
            }
            if (multipleModule) {
                if (checkExist(targetPath, (serviceFolder).replace(".", separator), serviceName + ".java") && !overwriteHandler) {
                    continue;
                }
                transfer(targetPath, (serviceFolder).replace(".", separator), serviceName + ".java", templates.rest.handlerService.template(servicePackage, modelPackage, serviceName, statusCode, requestModelName, example, parameters));
            }
        }

        // handler test cases
        if (!specChangeCodeReGenOnly) {
            transfer(targetPath, (serverTestFolder + ".").replace(".", separator), "TestServer.java", templates.rest.testServer.template(handlerPackage));
        }
        for (Map<String, Object> op : operationList) {
            if (checkExist(targetPath, (serverTestFolder).replace(".", separator), op.get("handlerName") + "Test.java") && !overwriteHandlerTest) {
                continue;
            }
            transfer(targetPath, (serverTestFolder).replace(".", separator), op.get("handlerName") + "Test.java", templates.rest.handlerTest.template(handlerPackage, op));
        }

        // transfer binary files without touching them.
        try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
            Generator.copyFile(is, Paths.get(targetPath, (configFolder).replace(".", separator), "server.keystore"));
        }
        try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
            Generator.copyFile(is, Paths.get(targetPath, (configFolder).replace(".", separator), "server.truststore"));
        }
        if (supportClient) {
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Generator.copyFile(is, Paths.get(targetPath, (configFolder).replace(".", separator), "client.keystore"));
            }
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Generator.copyFile(is, Paths.get(targetPath, (configFolder).replace(".", separator), "client.truststore"));
            }
        } else {
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Generator.copyFile(is, Paths.get(targetPath, (testConfigFolder).replace(".", separator), "client.keystore"));
            }
            try (InputStream is = OpenApiGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Generator.copyFile(is, Paths.get(targetPath, (testConfigFolder).replace(".", separator), "client.truststore"));
            }
        }

        try (InputStream is = new ByteArrayInputStream(Generator.yamlMapper.writeValueAsBytes(model))) {
            Generator.copyFile(is, Paths.get(targetPath, (configFolder).replace(".", separator), "openapi.yaml"));
        }
    }

    ModelCallback callback = new ModelCallback() {
        @Override
        public void callback(boolean multipleModule, String targetPath, String modelPackage, String modelFileName, String enumsIfClass, String parentClassName, String classVarName, boolean abstractIfClass, List<Map<String, Object>> props, List<Map<String, Object>> parentClassProps) {
            try {
                transfer(targetPath,
                        ((multipleModule ? "model.src.main.java." : "src.main.java.") + modelPackage).replace(".", separator),
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
