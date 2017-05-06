package com.networknt.codegen.hybrid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.codegen.Generator;
import sun.nio.cs.Surrogate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import static java.io.File.separator;

/**
 * Created by steve on 28/04/17.
 */
public class HybridServerGenerator implements Generator {
    static ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getFramework() {
        return "light-hybrid-4j-server";
    }

    @Override
    public void generate(String targetPath, Object model, Map<String, Object> config) throws IOException {
        // whoever is calling this needs to make sure that model is converted to Map<String, Object>
        String rootPackage = (String)config.get("rootPackage");
        String modelPackage = (String)config.get("modelPackage");
        String handlerPackage = (String)config.get("handlerPackage");

        transfer(targetPath, "", "pom.xml", templates.hybrid.server.pom.template(config));
        transfer(targetPath, "", "Dockerfile", templates.hybrid.server.dockerfile.template(config));
        transfer(targetPath, "", ".gitignore", templates.hybrid.gitignore.template());
        transfer(targetPath, "", "README.md", templates.hybrid.server.README.template());
        transfer(targetPath, "", "LICENSE", templates.hybrid.LICENSE.template());
        transfer(targetPath, "", ".classpath", templates.hybrid.classpath.template());
        transfer(targetPath, "", ".project", templates.hybrid.project.template());

        // config
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "server.yml", templates.hybrid.serverYml.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version")));
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "secret.yml", templates.hybrid.secretYml.template());
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "security.yml", templates.hybrid.securityYml.template());


        transfer(targetPath, ("src.main.resources.config.oauth").replace(".", separator), "primary.crt", templates.hybrid.primaryCrt.template());
        transfer(targetPath, ("src.main.resources.config.oauth").replace(".", separator), "secondary.crt", templates.hybrid.secondaryCrt.template());

        transfer(targetPath, ("src.main.resources.META-INF.services").replace(".", separator), "com.networknt.server.HandlerProvider", templates.hybrid.routingProvider.template());
        transfer(targetPath, ("src.main.resources.META-INF.services").replace(".", separator), "com.networknt.handler.MiddlewareHandler", templates.hybrid.middlewareService.template());
        transfer(targetPath, ("src.main.resources.META-INF.services").replace(".", separator), "com.networknt.server.StartupHookProvider", templates.hybrid.startupHookProvider.template());
        transfer(targetPath, ("src.main.resources.META-INF.services").replace(".", separator), "com.networknt.server.ShutdownHookProvider", templates.hybrid.shutdownHookProvider.template());

        // logging
        transfer(targetPath, ("src.main.resources").replace(".", separator), "logback.xml", templates.hybrid.logback.template());
        transfer(targetPath, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.hybrid.logback.template());

        // no handler as this is a server platform which supports other handlers to be deployed

        // no handler test case as this is a server platform which supports other handlers to be deployed.

        // transfer binary files without touching them.
        if(Files.notExists(Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator)))) {
            Files.createDirectories(Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator)));
        }
        try (InputStream is = HybridServerGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
            Files.copy(is, Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator), "server.keystore"), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream is = HybridServerGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
            Files.copy(is, Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator), "server.truststore"), StandardCopyOption.REPLACE_EXISTING);
        }

    }

}
