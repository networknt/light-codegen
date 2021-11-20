package com.networknt.codegen.hybrid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.networknt.codegen.Generator;
import org.apache.commons.text.StringEscapeUtils;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.io.File.separator;

/**
 * Created by steve on 28/04/17.
 */
public class HybridServiceGenerator implements HybridGenerator {

    @Override
    public String getFramework() {
        return "light-hybrid-4j-service";
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
        boolean kafkaStreams = isKafkaStreams(config, null);
        boolean supportAvro = isSupportAvro(config, null);
        boolean useLightProxy = isUseLightProxy(config, null);
        String kafkaTopic = getKafkaTopic(config, null);
        String decryptOption = getDecryptOption(config, null);
        String jsonPath = getJsonPath(config, null);
        boolean buildMaven = isBuildMaven(config, null);

        if(buildMaven) {
            transfer(targetPath, "", "pom.xml", templates.hybrid.service.pom.template(config));
            transferMaven(targetPath);
        } else {
            transferGradle(targetPath);
        }
        //transfer(targetPath, "", "Dockerfile", templates.dockerfile.template(config));
        transfer(targetPath, "", ".gitignore", templates.hybrid.gitignore.template());
        transfer(targetPath, "", "README.md", templates.hybrid.service.README.template());
        transfer(targetPath, "", "LICENSE", templates.hybrid.LICENSE.template());
        if(eclipseIDE) {
            transfer(targetPath, "", ".classpath", templates.hybrid.classpath.template());
            transfer(targetPath, "", ".project", templates.hybrid.project.template());
        }

        // config
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "service.yml", templates.hybrid.serviceYml.template(config));

        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml", templates.hybrid.serverYml.template(serviceId, enableHttp, "49587", enableHttps, "49588", enableHttp2, enableRegistry, version));
        //transfer(targetPath, ("src.test.resources.config").replace(".", separator), "secret.yml", templates.hybrid.secretYml.template());
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "hybrid-security.yml", templates.hybrid.securityYml.template());
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "client.yml", templates.hybrid.clientYml.template());
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "primary.crt", templates.hybrid.primaryCrt.template());
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "secondary.crt", templates.hybrid.secondaryCrt.template());
        if(kafkaProducer) {
            transfer(targetPath, ("src.test.resources.config").replace(".", separator), "kafka-producer.yml", templates.hybrid.kafkaProducerYml.template(kafkaTopic));
        }
        if(kafkaConsumer) {
            transfer(targetPath, ("src.test.resources.config").replace(".", separator), "kafka-consumer.yml", templates.hybrid.kafkaConsumerYml.template(kafkaTopic));
        }
        if(kafkaStreams) {
            transfer(targetPath, ("src.test.resources.config").replace(".", separator), "kafka-streams.yml", templates.hybrid.kafkaStreamsYml.template(artifactId));
        }

        // logging
        transfer(targetPath, ("src.main.resources").replace(".", separator), "logback.xml", templates.hybrid.logback.template());
        transfer(targetPath, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.hybrid.logback.template());
        // added with #471
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "app-status.yml", templates.hybrid.appStatusYml.template());
        // values.yml file, transfer to suppress the warning message during start startup and encourage usage.
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "values.yml", templates.hybrid.values.template());

        // handler
        Map<String, Object> services = new HashMap<String, Object>();
        JsonNode anyModel = (JsonNode)model;
        String host = anyModel.get("host").textValue();
        String service = anyModel.get("service").textValue();
        JsonNode jsonNode = anyModel.get("action");
        if(jsonNode != null && jsonNode.isArray()) {
            ArrayNode items = (ArrayNode)jsonNode;
            for(JsonNode item : items) {
                JsonNode any = item.get("example");
                String example = any != null ? StringEscapeUtils.escapeJson(any.toString()).trim() : "";
                if(!overwriteHandler && checkExist(targetPath, ("src.main.java." + handlerPackage).replace(".", separator), item.get("handler").textValue() + ".java")) {
                    continue;
                }
                transfer(targetPath, ("src.main.java." + handlerPackage).replace(".", separator), item.get("handler").textValue() + ".java", templates.hybrid.handler.template(handlerPackage, host, service, item, example));
                String sId  = host + "/" + service + "/" + item.get("name").textValue() + "/" + item.get("version").textValue();
                Map<String, Object> map = new HashMap<>();
                map.put("schema", item.get("schema"));
                JsonNode anyScope = item.get("scope");
                String scope = anyScope != null ? anyScope.textValue().trim() : null;
                if(scope != null) map.put("scope", scope);
                JsonNode anySkipAuth = item.get("skipAuth");
                Boolean skipAuth = anySkipAuth != null ? anySkipAuth.booleanValue() : false;
                if(skipAuth != null) map.put("skipAuth", skipAuth);
                services.put(sId, map);
            }

            // handler test cases
            transfer(targetPath, ("src.test.java." + handlerPackage + ".").replace(".", separator),  "TestServer.java", templates.hybrid.testServer.template(handlerPackage));
            for(JsonNode item : items) {
                if(!overwriteHandlerTest && checkExist(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), item.get("handler").textValue() + "Test.java")) {
                    continue;
                }
                transfer(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), item.get("handler").textValue() + "Test.java", templates.hybrid.handlerTest.template(handlerPackage, host, service, item));
            }
        }

        // transfer binary files without touching them.
        try (InputStream is = HybridServiceGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
            Files.copy(is, Paths.get(targetPath, ("src.test.resources.config").replace(".", separator), "server.keystore"), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream is = HybridServiceGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
            Files.copy(is, Paths.get(targetPath, ("src.test.resources.config").replace(".", separator), "server.truststore"), StandardCopyOption.REPLACE_EXISTING);
        }
        // to support unit test with HTTPS/HTTP2, we need to have client.truststore and client.keystore
        try (InputStream is = HybridServiceGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
            Files.copy(is, Paths.get(targetPath, ("src.test.resources.config").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream is = HybridServiceGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
            Files.copy(is, Paths.get(targetPath, ("src.test.resources.config").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
        }

        if(Files.notExists(Paths.get(targetPath, ("src.main.resources.config").replace(".", separator)))) {
            Files.createDirectories(Paths.get(targetPath, ("src.main.resources.config").replace(".", separator)));
        }

        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "handler.yml",
                templates.hybrid.handlerYml.template(serviceId, handlerPackage, jsonPath, prometheusMetrics));

        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "rpc-router.yml",
                templates.hybrid.rpcRouterYml.template(handlerPackage, jsonPath));

        // write the generated schema into the config folder for schema validation.
        try (InputStream is = new ByteArrayInputStream(Generator.jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(services))) {
            Generator.copyFile(is, Paths.get(targetPath, ("src.main.resources").replace(".", separator), "schema.json"));
        }
    }
}
