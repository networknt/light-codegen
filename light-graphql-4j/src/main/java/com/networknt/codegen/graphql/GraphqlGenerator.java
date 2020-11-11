package com.networknt.codegen.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.any.Any;
import com.networknt.codegen.Generator;
import com.networknt.utility.NioUtils;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;

import static java.io.File.separator;

/**
 * Created by steve on 01/05/17.
 */
public class GraphqlGenerator implements Generator {
    static ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getFramework() {
        return "light-graphql-4j";
    }

    @Override
    public void generate(String targetPath, Object model, Any config) throws IOException {
        // whoever is calling this needs to make sure that model is converted to Map<String, Object>
        String schemaPackage = config.get("schemaPackage").toString();
        String schemaClass = config.get("schemaClass").toString();
        boolean overwriteSchemaClass = config.toBoolean("overwriteSchemaClass");
        boolean enableHttp = config.toBoolean("enableHttp");
        String httpPort = config.toString("httpPort");
        boolean enableHttps = config.toBoolean("enableHttps");
        String httpsPort = config.toString("httpsPort");
        boolean enableHttp2 = config.toBoolean("enableHttp2");
        boolean enableRegistry = config.toBoolean("enableRegistry");
        boolean eclipseIDE = config.toBoolean("eclipseIDE");
        boolean supportClient = config.toBoolean("supportClient");
        boolean prometheusMetrics = config.toBoolean("prometheusMetrics");
        boolean skipHealthCheck = config.toBoolean("skipHealthCheck");
        boolean skipServerInfo = config.toBoolean("skipServerInfo");
        String dockerOrganization = config.toString("dockerOrganization");
        String version = config.toString("version");
        String serviceId = config.get("groupId").toString().trim() + "." + config.get("artifactId").toString().trim() + "-" + config.get("version").toString().trim();

        if(dockerOrganization == null || dockerOrganization.length() == 0) dockerOrganization = "networknt";

        transfer(targetPath, "", "pom.xml", templates.graphql.pom.template(config));
        transferMaven(targetPath);
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
        transfer(targetPath, "", "build.sh", templates.graphql.buildSh.template(dockerOrganization, config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version")));
        transfer(targetPath, "", ".gitignore", templates.graphql.gitignore.template());
        transfer(targetPath, "", "README.md", templates.graphql.README.template());
        transfer(targetPath, "", "LICENSE", templates.graphql.LICENSE.template());
        if(eclipseIDE) {
            transfer(targetPath, "", ".classpath", templates.graphql.classpath.template());
            transfer(targetPath, "", ".project", templates.graphql.project.template());
        }
        // config
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "service.yml", templates.graphql.serviceYml.template(config));

        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "server.yml", templates.graphql.serverYml.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version"), enableHttp, httpPort, enableHttps, httpsPort, enableHttp2, enableRegistry, version));
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml", templates.graphql.serverYml.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version"), enableHttp, "49587", enableHttps, "49588", enableHttp2, enableRegistry, version));
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "graphql-security.yml", templates.graphql.securityYml.template());
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "graphql-validator.yml", templates.graphql.validatorYml.template());
        if(supportClient) {
            // copy client.yml to main/resources/config
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "client.yml", templates.graphql.clientYml.template());
        } else {
            // copy client.yml to test/resources/config for test cases
            transfer(targetPath, ("src.test.resources.config").replace(".", separator), "client.yml", templates.graphql.clientYml.template());
        }

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
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "handler.yml", templates.graphql.handlerYml.template(serviceId, prometheusMetrics, !skipHealthCheck, !skipServerInfo));

        // Copy schema
        // The generator support both manually coded schema or schema defined in IDL. If schema.graphqls exists
        // then it will be copied to the resources folder and corresponding code will be generated to load it and
        // to generate schema on the fly.
        // If no schema file is passed in, then it will just hard-coded as a Hello World example so that developer
        // can expand that to code his/her own schema.
        if(overwriteSchemaClass) {
            if(model == null) {
                transfer(targetPath, ("src.main.java." + schemaPackage).replace(".", separator), schemaClass + ".java", templates.graphql.schemaClassExample.template(schemaPackage, schemaClass));
            } else {
                Files.write(FileSystems.getDefault().getPath(targetPath, ("src.main.resources").replace(".", separator), "schema.graphqls"), ((String)model).getBytes(StandardCharsets.UTF_8));
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

}
