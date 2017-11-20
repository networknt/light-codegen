package com.networknt.codegen.hybrid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.any.Any;
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
        boolean supportOracle = config.toBoolean("supportOracle");
        boolean supportMysql  = config.toBoolean("supportMysql");
        boolean supportPostgresql = config.toBoolean("supportPostgresql");
        boolean supportH2ForTest  = config.toBoolean("supportH2ForTest");

        boolean supportClient = config.toBoolean("supportClient");

        transfer(targetPath, "", "pom.xml", templates.hybrid.server.pom.template(config));

        // There is only one port that should be exposed in Dockerfile, otherwise, the service
        // discovery will be so confused. If https is enabled, expose the https port. Otherwise http port.
        String expose = "";
        if(enableHttps) {
            expose = httpsPort;
        } else {
            expose = httpPort;
        }
        transfer(targetPath, "", "Dockerfile", templates.hybrid.server.dockerfile.template(config, expose));
        transfer(targetPath, "", ".gitignore", templates.hybrid.gitignore.template());
        transfer(targetPath, "", "README.md", templates.hybrid.server.README.template());
        transfer(targetPath, "", "LICENSE", templates.hybrid.LICENSE.template());
        transfer(targetPath, "", ".classpath", templates.hybrid.classpath.template());
        transfer(targetPath, "", ".project", templates.hybrid.project.template());

        // config
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "service.yml", templates.hybrid.serviceYml.template(config));

        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "server.yml", templates.hybrid.serverYml.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version"), enableHttp, httpPort, enableHttps, httpsPort, enableRegistry));
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml", templates.hybrid.serverYml.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version"), enableHttp, "49587", enableHttps, "49588", enableRegistry));

        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "secret.yml", templates.hybrid.secretYml.template());
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "security.yml", templates.hybrid.securityYml.template());
        if(supportClient) {
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "client.yml", templates.hybrid.clientYml.template());
        } else {
            transfer(targetPath, ("src.test.resources.config").replace(".", separator), "client.yml", templates.hybrid.clientYml.template());
        }

        transfer(targetPath, ("src.main.resources.config.oauth").replace(".", separator), "primary.crt", templates.hybrid.primaryCrt.template());
        transfer(targetPath, ("src.main.resources.config.oauth").replace(".", separator), "secondary.crt", templates.hybrid.secondaryCrt.template());

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
        if(supportClient) {
            try (InputStream is = HybridServerGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Files.copy(is, Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = HybridServerGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Files.copy(is, Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            if(Files.notExists(Paths.get(targetPath, ("src.test.resources.config.tls").replace(".", separator)))) {
                Files.createDirectories(Paths.get(targetPath, ("src.test.resources.config.tls").replace(".", separator)));
            }
            try (InputStream is = HybridServerGenerator.class.getResourceAsStream("/binaries/client.keystore")) {
                Files.copy(is, Paths.get(targetPath, ("src.test.resources.config.tls").replace(".", separator), "client.keystore"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = HybridServerGenerator.class.getResourceAsStream("/binaries/client.truststore")) {
                Files.copy(is, Paths.get(targetPath, ("src.test.resources.config.tls").replace(".", separator), "client.truststore"), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

}
