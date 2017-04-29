package com.networknt.codegen.hybrid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.codegen.Generator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import static java.io.File.separator;

/**
 * Created by steve on 28/04/17.
 */
public class HybridServiceGenerator implements Generator {
    static ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getFramework() {
        return "light-java-hybrid-service";
    }

    @Override
    public void generate(String targetPath, Object model, Map<String, Object> config) throws IOException {
        // whoever is calling this needs to make sure that model is converted to Map<String, Object>
        String rootPackage = (String)config.get("rootPackage");
        String modelPackage = (String)config.get("modelPackage");
        String handlerPackage = (String)config.get("handlerPackage");

        transfer(targetPath, "", "pom.xml", templates.service.pom.template(config));
        //transfer(targetPath, "", "Dockerfile", templates.dockerfile.template(config));
        transfer(targetPath, "", ".gitignore", templates.gitignore.template());
        transfer(targetPath, "", "README.md", templates.service.README.template());
        transfer(targetPath, "", "LICENSE", templates.LICENSE.template());
        transfer(targetPath, "", ".classpath", templates.classpath.template());
        transfer(targetPath, "", ".project", templates.project.template());

        // config
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml", templates.serverYml.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version")));
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "secret.yml", templates.secretYml.template());
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "security.yml", templates.securityYml.template());


        transfer(targetPath, ("src.test.resources.config.oauth").replace(".", separator), "primary.crt", templates.primaryCrt.template());
        transfer(targetPath, ("src.test.resources.config.oauth").replace(".", separator), "secondary.crt", templates.secondaryCrt.template());

        transfer(targetPath, ("src.test.resources.META-INF.services").replace(".", separator), "com.networknt.server.HandlerProvider", templates.routingService.template());
        transfer(targetPath, ("src.test.resources.META-INF.services").replace(".", separator), "com.networknt.server.MiddlewareHandler", templates.middlewareService.template());
        transfer(targetPath, ("src.test.resources.META-INF.services").replace(".", separator), "com.networknt.server.StartupHookProvider", templates.startupHookProvider.template());
        transfer(targetPath, ("src.test.resources.META-INF.services").replace(".", separator), "com.networknt.server.ShutdownHookProvider", templates.shutdownHookProvider.template());

        // logging
        transfer(targetPath, ("src.main.resources").replace(".", separator), "logback.xml", templates.logback.template());
        transfer(targetPath, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.logback.template());

        // handler
        String host = (String)((Map<String, Object>)model).get("host");
        String service = (String)((Map<String, Object>)model).get("service");
        List<Map<String, Object>> items = (List<Map<String, Object>>)((Map<String, Object>)model).get("action");
        for(Map<String, Object> item : items) {
            transfer(targetPath, ("src.main.java." + handlerPackage).replace(".", separator), (String)item.get("handler") + ".java", templates.handler.template(handlerPackage, host, service, item));
        }

        // handler test cases
        transfer(targetPath, ("src.test.java." + handlerPackage + ".").replace(".", separator),  "TestServer.java", templates.testServer.template(handlerPackage));
        for(Map<String, Object> item : items) {
            transfer(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), (String)item.get("handler") + "Test.java", templates.handlerTest.template(handlerPackage, host, service, item));
        }

        // transfer binary files without touching them.
        if(Files.notExists(Paths.get(targetPath, ("src.test.resources.config.tls").replace(".", separator)))) {
            Files.createDirectories(Paths.get(targetPath, ("src.test.resources.config.tls").replace(".", separator)));
        }
        try (InputStream is = HybridServiceGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
            Files.copy(is, Paths.get(targetPath, ("src.test.resources.config.tls").replace(".", separator), "server.keystore"), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream is = HybridServiceGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
            Files.copy(is, Paths.get(targetPath, ("src.test.resources.config.tls").replace(".", separator), "server.truststore"), StandardCopyOption.REPLACE_EXISTING);
        }

    }
}
