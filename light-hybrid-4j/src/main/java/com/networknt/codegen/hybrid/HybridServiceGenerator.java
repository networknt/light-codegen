package com.networknt.codegen.hybrid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.codegen.Generator;
import com.networknt.utility.NioUtils;

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
    static ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getFramework() {
        return "light-hybrid-4j-service";
    }

    @Override
    public void generate(String targetPath, Object model, Map<String, Object> config) throws IOException {
        // whoever is calling this needs to make sure that model is converted to Map<String, Object>
        String rootPackage = (String)config.get("rootPackage");
        String modelPackage = (String)config.get("modelPackage");
        String handlerPackage = (String)config.get("handlerPackage");

        transfer(targetPath, "", "pom.xml", templates.hybrid.service.pom.template(config));
        //transfer(targetPath, "", "Dockerfile", templates.dockerfile.template(config));
        transfer(targetPath, "", ".gitignore", templates.hybrid.gitignore.template());
        transfer(targetPath, "", "README.md", templates.hybrid.service.README.template());
        transfer(targetPath, "", "LICENSE", templates.hybrid.LICENSE.template());
        transfer(targetPath, "", ".classpath", templates.hybrid.classpath.template());
        transfer(targetPath, "", ".project", templates.hybrid.project.template());

        // config
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml", templates.hybrid.serverYml.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version")));
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "secret.yml", templates.hybrid.secretYml.template());
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "security.yml", templates.hybrid.securityYml.template());


        transfer(targetPath, ("src.test.resources.config.oauth").replace(".", separator), "primary.crt", templates.hybrid.primaryCrt.template());
        transfer(targetPath, ("src.test.resources.config.oauth").replace(".", separator), "secondary.crt", templates.hybrid.secondaryCrt.template());

        transfer(targetPath, ("src.test.resources.META-INF.services").replace(".", separator), "com.networknt.server.HandlerProvider", templates.hybrid.routingProvider.template());
        transfer(targetPath, ("src.test.resources.META-INF.services").replace(".", separator), "com.networknt.handler.MiddlewareHandler", templates.hybrid.middlewareService.template());
        transfer(targetPath, ("src.test.resources.META-INF.services").replace(".", separator), "com.networknt.server.StartupHookProvider", templates.hybrid.startupHookProvider.template());
        transfer(targetPath, ("src.test.resources.META-INF.services").replace(".", separator), "com.networknt.server.ShutdownHookProvider", templates.hybrid.shutdownHookProvider.template());

        // logging
        transfer(targetPath, ("src.main.resources").replace(".", separator), "logback.xml", templates.hybrid.logback.template());
        transfer(targetPath, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.hybrid.logback.template());

        // handler
        Map<String, Object> services = new HashMap<String, Object>();
        String host = (String)((Map<String, Object>)model).get("host");
        String service = (String)((Map<String, Object>)model).get("service");
        List<Map<String, Object>> items = (List<Map<String, Object>>)((Map<String, Object>)model).get("action");
        for(Map<String, Object> item : items) {
            transfer(targetPath, ("src.main.java." + handlerPackage).replace(".", separator), (String)item.get("handler") + ".java", templates.hybrid.handler.template(handlerPackage, host, service, item));

            String serviceId  = host + "/" + service + "/" + item.get("name") + "/" + item.get("version");
            Map<String, Object> map = new HashMap<>();
            map.put("schema", item.get("schema"));
            map.put("scope", item.get("scope"));
            services.put(serviceId, map);
        }

        // handler test cases
        transfer(targetPath, ("src.test.java." + handlerPackage + ".").replace(".", separator),  "TestServer.java", templates.hybrid.testServer.template(handlerPackage));
        for(Map<String, Object> item : items) {
            transfer(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), (String)item.get("handler") + "Test.java", templates.hybrid.handlerTest.template(handlerPackage, host, service, item));
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

        if(Files.notExists(Paths.get(targetPath, ("src.main.resources.config").replace(".", separator)))) {
            Files.createDirectories(Paths.get(targetPath, ("src.main.resources.config").replace(".", separator)));
        }
        // write the generated schema into the config folder for schema validation.
        NioUtils.writeJson(FileSystems.getDefault().getPath(targetPath, ("src.main.resources.config").replace(".", separator), "schema.json"), services);
    }
}
