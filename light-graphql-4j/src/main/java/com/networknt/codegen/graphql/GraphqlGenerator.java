package com.networknt.codegen.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.codegen.Generator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    public void generate(String targetPath, Object model, Map<String, Object> config) throws IOException {
        // whoever is calling this needs to make sure that model is converted to Map<String, Object>
        String rootPackage = (String)config.get("rootPackage");
        String modelPackage = (String)config.get("modelPackage");
        String handlerPackage = (String)config.get("handlerPackage");
        String schemaPackage = (String)config.get("schemaPackage");
        String schemaClass = (String)config.get("schemaClass");

        transfer(targetPath, "", "pom.xml", templates.graphql.pom.template(config));
        transfer(targetPath, "", "Dockerfile", templates.graphql.dockerfile.template(config));
        transfer(targetPath, "", ".gitignore", templates.graphql.gitignore.template());
        transfer(targetPath, "", "README.md", templates.graphql.README.template());
        transfer(targetPath, "", "LICENSE", templates.graphql.LICENSE.template());
        transfer(targetPath, "", ".classpath", templates.graphql.classpath.template());
        transfer(targetPath, "", ".project", templates.graphql.project.template());

        // config
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "server.yml", templates.graphql.serverYml.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version")));
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "secret.yml", templates.graphql.secretYml.template());
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "security.yml", templates.graphql.securityYml.template());


        transfer(targetPath, ("src.main.resources.config.oauth").replace(".", separator), "primary.crt", templates.graphql.primaryCrt.template());
        transfer(targetPath, ("src.main.resources.config.oauth").replace(".", separator), "secondary.crt", templates.graphql.secondaryCrt.template());

        transfer(targetPath, ("src.main.resources.META-INF.services").replace(".", separator), "com.networknt.handler.MiddlewareHandler", templates.graphql.middlewareService.template());
        transfer(targetPath, ("src.main.resources.META-INF.services").replace(".", separator), "com.networknt.server.StartupHookProvider", templates.graphql.startupHookProvider.template());
        transfer(targetPath, ("src.main.resources.META-INF.services").replace(".", separator), "com.networknt.server.ShutdownHookProvider", templates.graphql.shutdownHookProvider.template());
        transfer(targetPath, ("src.main.resources.META-INF.services").replace(".", separator), "com.networknt.graphql.router.SchemaProvider", templates.graphql.schemaProvider.template(schemaPackage + "." + schemaClass));
        transfer(targetPath, ("src.main.resources.META-INF.services").replace(".", separator), "com.networknt.server.HandlerProvider", templates.graphql.routingProvider.template());

        // logging
        transfer(targetPath, ("src.main.resources").replace(".", separator), "logback.xml", templates.graphql.logback.template());
        transfer(targetPath, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.graphql.logback.template());

        // Generate schema
        // TODO waiting for graphql-java 3.0.0 release with IDL to generate schema from definition file
        // Now it is hard coded hello world
        transfer(targetPath, ("src.main.java." + schemaPackage).replace(".", separator), schemaClass + ".java", templates.graphql.schemaClass.template(schemaPackage, schemaClass));


        // no handler test case as this is a server platform which supports other handlers to be deployed.

        // transfer binary files without touching them.
        if(Files.notExists(Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator)))) {
            Files.createDirectories(Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator)));
        }
        try (InputStream is = GraphqlGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
            Files.copy(is, Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator), "server.keystore"), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream is = GraphqlGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
            Files.copy(is, Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator), "server.truststore"), StandardCopyOption.REPLACE_EXISTING);
        }

    }

}
