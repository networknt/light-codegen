package com.networknt.codegen.hybrid;

import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import com.networknt.codegen.Generator;
import org.apache.commons.text.StringEscapeUtils;

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
public class HybridServiceGenerator implements Generator {

    @Override
    public String getFramework() {
        return "light-hybrid-4j-service";
    }

    @Override
    public void generate(String targetPath, Object model, Any config) throws IOException {
        // whoever is calling this needs to make sure that model is converted to Map<String, Object>
        String handlerPackage = config.get("handlerPackage").toString();
        boolean overwriteHandler = config.toBoolean("overwriteHandler");
        boolean overwriteHandlerTest = config.toBoolean("overwriteHandlerTest");
        boolean enableHttp = config.toBoolean("enableHttp");
        boolean enableHttps = config.toBoolean("enableHttps");
        boolean enableHttp2 = config.toBoolean("enableHttp2");
        boolean enableRegistry = config.toBoolean("enableRegistry");
        boolean eclipseIDE = config.toBoolean("eclipseIDE");
        boolean supportClient = config.toBoolean("supportClient");
        String artifactId = config.toString("artifactId");
        String version = config.toString("version");
        String serviceId = config.get("groupId").toString().trim() + "." + artifactId.trim() + "-" + config.get("version").toString().trim();
        boolean prometheusMetrics = config.toBoolean("prometheusMetrics");
        boolean skipHealthCheck = config.toBoolean("skipHealthCheck");
        boolean skipServerInfo = config.toBoolean("skipServerInfo");
        String jsonPath = config.get("jsonPath").toString();
        boolean kafkaProducer = config.toBoolean("kafkaProducer");
        boolean kafkaConsumer = config.toBoolean("kafkaConsumer");
        boolean supportAvro = config.toBoolean("supportAvro");
        String kafkaTopic = config.get("kafkaTopic").toString();

        transfer(targetPath, "", "pom.xml", templates.hybrid.service.pom.template(config));
        transferMaven(targetPath);
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

        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml", templates.hybrid.serverYml.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version"), enableHttp, "49587", enableHttps, "49588", enableHttp2, enableRegistry, version));
        //transfer(targetPath, ("src.test.resources.config").replace(".", separator), "secret.yml", templates.hybrid.secretYml.template());
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "hybrid-security.yml", templates.hybrid.securityYml.template());
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "client.yml", templates.hybrid.clientYml.template());
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "primary.crt", templates.hybrid.primaryCrt.template());
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "secondary.crt", templates.hybrid.secondaryCrt.template());
        if(kafkaProducer) {
            transfer(targetPath, ("src.test.resources.config").replace(".", separator), "kafka-producer.yml", templates.hybrid.kafkaProducerYml.template(kafkaTopic));
        }
        if(kafkaConsumer) {
            transfer(targetPath, ("src.test.resources.config").replace(".", separator), "kafka-streams.yml", templates.hybrid.kafkaStreamsYml.template(artifactId));
        }
        if(supportAvro) {
            transfer(targetPath, ("src.test.resources.config").replace(".", separator), "schema-registry.yml", templates.hybrid.schemaRegistryYml.template());
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
        Any anyModel = (Any)model;
        String host = anyModel.toString("host");
        String service = anyModel.toString("service");
        List<Any> items = anyModel.get("action").asList();
        if(items != null && items.size() > 0) {
            for(Any item : items) {
                Any any = item.get("example");
                String example = any.valueType() != ValueType.INVALID ? StringEscapeUtils.escapeJson(any.toString()).trim() : "";
                if(!overwriteHandler && checkExist(targetPath, ("src.main.java." + handlerPackage).replace(".", separator), item.get("handler") + ".java")) {
                    continue;
                }
                transfer(targetPath, ("src.main.java." + handlerPackage).replace(".", separator), item.get("handler") + ".java", templates.hybrid.handler.template(handlerPackage, host, service, item, example));
                String sId  = host + "/" + service + "/" + item.get("name") + "/" + item.get("version");
                Map<String, Object> map = new HashMap<>();
                map.put("schema", item.get("schema"));
                Any anyScope = item.get("scope");
                String scope = anyScope.valueType() != ValueType.INVALID ? anyScope.toString().trim() : null;
                if(scope != null) map.put("scope", scope);
                Any anySkipAuth = item.get("skipAuth");
                Boolean skipAuth = anySkipAuth.valueType() != ValueType.INVALID ? anySkipAuth.toBoolean() : null;
                if(skipAuth != null) map.put("skipAuth", skipAuth);
                services.put(sId, map);
            }

            // handler test cases
            transfer(targetPath, ("src.test.java." + handlerPackage + ".").replace(".", separator),  "TestServer.java", templates.hybrid.testServer.template(handlerPackage));
            for(Any item : items) {
                if(!overwriteHandlerTest && checkExist(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), item.get("handler") + "Test.java")) {
                    continue;
                }
                transfer(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), item.get("handler") + "Test.java", templates.hybrid.handlerTest.template(handlerPackage, host, service, item));
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
                templates.hybrid.handlerYml.template(serviceId, handlerPackage, jsonPath, prometheusMetrics, !skipHealthCheck, !skipServerInfo));

        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "rpc-router.yml",
                templates.hybrid.rpcRouterYml.template(handlerPackage, jsonPath));

        // write the generated schema into the config folder for schema validation.
        JsonStream.serialize(services, new FileOutputStream(FileSystems.getDefault().getPath(targetPath, ("src.main.resources").replace(".", separator), "schema.json").toFile()));
    }
}
