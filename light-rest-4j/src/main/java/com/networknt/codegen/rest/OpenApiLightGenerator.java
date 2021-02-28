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
        if (!generateModelOnly) {
            // if set to true, regenerate the code only (handlers, model and the handler.yml, potentially affected by operation changes
            if (!specChangeCodeReGenOnly) {
                // generate configurations, project, masks, certs, etc

                if(buildMaven) {
                    if (!skipPomFile) {
                        transfer(targetPath, "", "pom.xml", templates.rest.pom.template(config));
                    }
                    transferMaven(targetPath);
                } else {
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
                transfer(targetPath, "", "README.md", templates.rest.README.template());
                transfer(targetPath, "", "LICENSE", templates.rest.LICENSE.template());
                if(eclipseIDE) {
                    transfer(targetPath, "", ".classpath", templates.rest.classpath.template());
                    transfer(targetPath, "", ".project", templates.rest.project.template(config));
                }
                // config
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "service.yml", templates.rest.serviceYml.template(config));

                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "server.yml",
                        templates.rest.serverYml.template(serviceId, enableHttp, httpPort, enableHttps, httpsPort, enableHttp2, enableRegistry, version));
                transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml",
                        templates.rest.serverYml.template(serviceId, enableHttp, "49587", enableHttps, "49588", enableHttp2, enableRegistry, version));

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
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "config.yml", templates.rest.config.template(config));

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
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "values.yml", templates.rest.values.template());
                // add portal-registry.yml
                transfer(targetPath, ("src.main.resources.config").replace(".", separator), "portal-registry.yml", templates.rest.portalRegistryYml.template());
            }
            // routing handler
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "handler.yml",
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
                    loadModel(entry.getKey(), null, (Map<String, Object>)entry.getValue(), schemas, overwriteModel, targetPath, modelPackage, modelCreators, references, null, callback);
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
            transfer(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), op.get("handlerName") + "Test.java", templates.rest.handlerTest.template(handlerPackage, op));
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

    ModelCallback callback = new ModelCallback() {
        @Override
        public void callback(String targetPath, String modelPackage, String modelFileName, String enumsIfClass, String parentClassName, String classVarName, boolean abstractIfClass, List<Map<String, Object>> props, List<Map<String, Object>> parentClassProps) {
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
