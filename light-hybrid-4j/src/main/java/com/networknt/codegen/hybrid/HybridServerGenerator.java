package com.networknt.codegen.hybrid;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static java.io.File.separator;

/**
 * Created by steve on 28/04/17.
 */
public class HybridServerGenerator implements HybridGenerator {
    public static final String FRAMEWORK="light-hybrid-4j-server";

    @Override
    public String getFramework() {
        return FRAMEWORK;
    }

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
        String jsonPath = getJsonPath(config, null);
        boolean buildMaven = isBuildMaven(config, null);
        if(buildMaven) {
            transfer(targetPath, "", "pom.xml", templates.hybrid.server.pom.template(config));
            transferMaven(targetPath);
        } else {
            transferGradle(targetPath);
        }

        // There is only one port that should be exposed in Dockerfile, otherwise, the service
        // discovery will be so confused. If https is enabled, expose the https port. Otherwise http port.
        String expose = "";
        if(enableHttps) {
            expose = httpsPort;
        } else {
            expose = httpPort;
        }
        transfer(targetPath, "docker", "Dockerfile", templates.hybrid.server.dockerfile.template(config, expose));
        transfer(targetPath, "docker", "Dockerfile-Slim", templates.hybrid.server.dockerfileslim.template(config, expose));
        transfer(targetPath, "", "build.sh", templates.hybrid.server.buildSh.template(config, serviceId));
        transfer(targetPath, "", ".gitignore", templates.hybrid.gitignore.template());
        transfer(targetPath, "", "README.md", templates.hybrid.server.README.template());
        transfer(targetPath, "", "LICENSE", templates.hybrid.LICENSE.template());
        if(eclipseIDE) {
            transfer(targetPath, "", ".classpath", templates.hybrid.classpath.template());
            transfer(targetPath, "", ".project", templates.hybrid.project.template());
        }

        // config
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "service.yml", templates.hybrid.serviceYml.template(config));

        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "server.yml", templates.hybrid.serverYml.template(serviceId, enableHttp, httpPort, enableHttps, httpsPort, enableHttp2, enableRegistry, version));
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml", templates.hybrid.serverYml.template(serviceId, enableHttp, "49587", enableHttps, "49588", enableHttp2, enableRegistry, version));

        if(kafkaProducer) {
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "kafka-producer.yml", templates.hybrid.kafkaProducerYml.template(kafkaTopic));
        }
        if(kafkaConsumer) {
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "kafka-streams.yml", templates.hybrid.kafkaStreamsYml.template(artifactId));
        }
        if(supportAvro) {
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "schema-registry.yml", templates.hybrid.schemaRegistryYml.template());
        }

        // transfer(targetPath, ("src.main.resources.config").replace(".", separator), "secret.yml", templates.hybrid.secretYml.template());
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "hybrid-security.yml", templates.hybrid.securityYml.template());
        if(supportClient) {
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "client.yml", templates.hybrid.clientYml.template());
        } else {
            transfer(targetPath, ("src.test.resources.config").replace(".", separator), "client.yml", templates.hybrid.clientYml.template());
        }

        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "primary.crt", templates.hybrid.primaryCrt.template());
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "secondary.crt", templates.hybrid.secondaryCrt.template());

        // logging
        transfer(targetPath, ("src.main.resources").replace(".", separator), "logback.xml", templates.hybrid.logback.template());
        transfer(targetPath, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.hybrid.logback.template());

        // added with #471
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "app-status.yml", templates.hybrid.appStatusYml.template());
        // values.yml file, transfer to suppress the warning message during start startup and encourage usage.
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "values.yml", templates.hybrid.values.template());

        // transfer binary files without touching them.
        try (InputStream is = HybridServerGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
            Files.copy(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "server.keystore"), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream is = HybridServerGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
            Files.copy(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "server.truststore"), StandardCopyOption.REPLACE_EXISTING);
        }
        if(supportClient) {
            try (InputStream is = HybridServerGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Files.copy(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = HybridServerGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Files.copy(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            try (InputStream is = HybridServerGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Files.copy(is, Paths.get(targetPath, ("src.test.resources.config").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = HybridServerGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Files.copy(is, Paths.get(targetPath, ("src.test.resources.config").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "handler.yml",
                templates.hybrid.handlerYml.template(serviceId, handlerPackage, jsonPath, prometheusMetrics));

        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "rpc-router.yml",
                templates.hybrid.rpcRouterYml.template(handlerPackage, jsonPath));

    }


}
