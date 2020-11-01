package com.networknt.codegen.hybrid;

import com.jsoniter.any.Any;
import com.networknt.codegen.Generator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static java.io.File.separator;

/**
 * Created by steve on 28/04/17.
 */
public class HybridServerGenerator implements Generator {

    @Override
    public String getFramework() {
        return "light-hybrid-4j-server";
    }

    @Override
    public void generate(String targetPath, Object model, Any config) throws IOException {
        // whoever is calling this needs to make sure that model is converted to Map<String, Object>
        String rootPackage = config.get("rootPackage").toString();
        String modelPackage = config.get("modelPackage").toString();
        String handlerPackage = config.get("handlerPackage").toString();

        boolean enableHttp = config.toBoolean("enableHttp");
        String httpPort = config.toString("httpPort");
        boolean enableHttps = config.toBoolean("enableHttps");
        String httpsPort = config.toString("httpsPort");
        boolean enableRegistry = config.toBoolean("enableRegistry");
        boolean eclipseIDE = config.toBoolean("eclipseIDE");
        String dockerOrganization = config.toString("dockerOrganization");
        String artifactId = config.toString("artifactId");
        String version = config.toString("version");
        if(dockerOrganization ==  null || dockerOrganization.length() == 0) dockerOrganization = "networknt";

        boolean supportClient = config.toBoolean("supportClient");
        String serviceId = config.get("groupId").toString().trim() + "." + artifactId.trim() + "-" + config.get("version").toString().trim();
        boolean prometheusMetrics = config.toBoolean("prometheusMetrics");
        boolean skipHealthCheck = config.toBoolean("skipHealthCheck");
        boolean skipServerInfo = config.toBoolean("skipServerInfo");
        String jsonPath = config.get("jsonPath").toString();
        boolean kafkaProducer = config.toBoolean("kafkaProducer");
        boolean kafkaConsumer = config.toBoolean("kafkaConsumer");
        boolean supportAvro = config.toBoolean("supportAvro");
        String kafkaTopic = config.get("kafkaTopic").toString();

        transfer(targetPath, "", "pom.xml", templates.hybrid.server.pom.template(config));
        transferMaven(targetPath);

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
        transfer(targetPath, "", "build.sh", templates.hybrid.server.buildSh.template(dockerOrganization, config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version")));
        transfer(targetPath, "", ".gitignore", templates.hybrid.gitignore.template());
        transfer(targetPath, "", "README.md", templates.hybrid.server.README.template());
        transfer(targetPath, "", "LICENSE", templates.hybrid.LICENSE.template());
        if(eclipseIDE) {
            transfer(targetPath, "", ".classpath", templates.hybrid.classpath.template());
            transfer(targetPath, "", ".project", templates.hybrid.project.template());
        }

        // config
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "service.yml", templates.hybrid.serviceYml.template(config));

        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "server.yml", templates.hybrid.serverYml.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version"), enableHttp, httpPort, enableHttps, httpsPort, enableRegistry, version));
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml", templates.hybrid.serverYml.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version"), enableHttp, "49587", enableHttps, "49588", enableRegistry, version));

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
                templates.hybrid.handlerYml.template(serviceId, handlerPackage, jsonPath, prometheusMetrics, !skipHealthCheck, !skipServerInfo));

        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "rpc-router.yml",
                templates.hybrid.rpcRouterYml.template(handlerPackage, jsonPath));

    }

}
