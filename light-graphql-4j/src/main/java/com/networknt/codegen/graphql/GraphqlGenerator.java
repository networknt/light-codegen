package com.networknt.codegen.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.codegen.Generator;
import com.networknt.config.ConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static java.io.File.separator;

/**
 * Created by steve on 01/05/17.
 */
public class GraphqlGenerator implements Generator {
    public static final String FRAMEWORK="light-graphql-4j";

    @Override
    public String getFramework() {
        return FRAMEWORK;
    }

    @Override
    public void generate(String targetPath, Object schema, JsonNode config) throws IOException {
        // GraphQL specific config
        String schemaPackage = getSchemaPackage(config, null);
        String schemaClass = getSchemaClass(config, null);
        boolean overwriteSchemaClass = isOverwriteSchemaClass(config, null);

        // Generic config
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

        if(buildMaven) {
            transfer(targetPath, "", "pom.xml", templates.graphql.pom.template(config));
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
        transfer(targetPath, "docker", "Dockerfile", templates.graphql.dockerfile.template(config, expose));
        transfer(targetPath, "docker", "Dockerfile-Slim", templates.graphql.dockerfileslim.template(config, expose));
        transfer(targetPath, "", "build.sh", templates.graphql.buildSh.template(config, serviceId));
        transfer(targetPath, "", ".gitignore", templates.graphql.gitignore.template());
        transfer(targetPath, "", "README.md", templates.graphql.README.template());
        transfer(targetPath, "", "LICENSE", templates.graphql.LICENSE.template());
        if(eclipseIDE) {
            transfer(targetPath, "", ".classpath", templates.graphql.classpath.template());
            transfer(targetPath, "", ".project", templates.graphql.project.template());
        }
        // config
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "service.yml", templates.graphql.serviceYml.template(config));

        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "server.yml", templates.graphql.serverYml.template(serviceId, enableHttp, httpPort, enableHttps, httpsPort, enableHttp2, enableRegistry, version));
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml", templates.graphql.serverYml.template(serviceId, enableHttp, "49587", enableHttps, "49588", enableHttp2, enableRegistry, version));
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "graphql-security.yml", templates.graphql.securityYml.template());
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "graphql-validator.yml", templates.graphql.validatorYml.template());
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "primary.crt", templates.graphql.primaryCrt.template());
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "secondary.crt", templates.graphql.secondaryCrt.template());

        // added with #471
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "app-status.yml", templates.graphql.appStatusYml.template());
        // values.yml file, transfer to suppress the warning message during start startup and encourage usage.
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "values.yml", templates.graphql.values.template());

        // logging
        transfer(targetPath, ("src.main.resources").replace(".", separator), "logback.xml", templates.graphql.logback.template());
        transfer(targetPath, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.graphql.logback.template());
        // handler.yml
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "handler.yml", templates.graphql.handlerYml.template(serviceId, prometheusMetrics, useLightProxy));

        // Copy schema
        // The generator support both manually coded schema or schema defined in IDL. If schema.graphqls exists
        // then it will be copied to the resources folder and corresponding code will be generated to load it and
        // to generate schema on the fly.
        // If no schema file is passed in, then it will just hard-coded as a Hello World example so that developer
        // can expand that to code his/her own schema.
        if(overwriteSchemaClass) {
            if(schema == null) {
                transfer(targetPath, ("src.main.java." + schemaPackage).replace(".", separator), schemaClass + ".java", templates.graphql.schemaClassExample.template(schemaPackage, schemaClass));
            } else {
                Files.write(FileSystems.getDefault().getPath(targetPath, ("src.main.resources").replace(".", separator), "schema.graphqls"), ((String)schema).getBytes(StandardCharsets.UTF_8));
                // schema class loader/generator template.
                transfer(targetPath, ("src.main.java." + schemaPackage).replace(".", separator), schemaClass + ".java", templates.graphql.schemaClass.template(schemaPackage, schemaClass));
            }
        }
        // no handler test case as this is a server platform which supports other handlers to be deployed.

        // transfer binary files without touching them.
        try (InputStream is = GraphqlGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
            Files.copy(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "server.keystore"), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream is = GraphqlGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
            Files.copy(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "server.truststore"), StandardCopyOption.REPLACE_EXISTING);
        }
        if(supportClient) {
            try (InputStream is = GraphqlGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Files.copy(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = GraphqlGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Files.copy(is, Paths.get(targetPath, ("src.main.resources.config").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            // copy client keystore and truststore into test resources for test cases.
            try (InputStream is = GraphqlGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Files.copy(is, Paths.get(targetPath, ("src.test.resources.config").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = GraphqlGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Files.copy(is, Paths.get(targetPath, ("src.test.resources.config").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }


    private String getSchemaPackage(JsonNode config, String defaultValue) {
        String schemaPackage = defaultValue == null ? "com.networknt.graphql.schema" : defaultValue;
        JsonNode jsonNode = config.get("schemaPackage");
        if(jsonNode == null) {
            ((ObjectNode)config).put("schemaPackage", schemaPackage);
        } else {
            schemaPackage = jsonNode.textValue();
        }
        return schemaPackage;
    }

    private String getSchemaClass(JsonNode config, String defaultValue) {
        String schemaClass = defaultValue == null ? "GraphQlSchema" : defaultValue;
        JsonNode jsonNode = config.get("schemaClass");
        if(jsonNode == null) {
            ((ObjectNode)config).put("schemaClass", schemaClass);
        } else {
            schemaClass = jsonNode.textValue();
        }
        return schemaClass;
    }

    private boolean isOverwriteSchemaClass(JsonNode config, Boolean defaultValue) {
        boolean overwriteSchemaClass = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("overwriteSchemaClass");
        if(jsonNode == null) {
            ((ObjectNode)config).put("overwriteSchemaClass", overwriteSchemaClass);
        } else {
            overwriteSchemaClass = jsonNode.booleanValue();
        }
        return overwriteSchemaClass;
    }
}
