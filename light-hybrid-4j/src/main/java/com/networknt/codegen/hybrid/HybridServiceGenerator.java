package com.networknt.codegen.hybrid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import com.networknt.codegen.Generator;
import com.networknt.utility.NioUtils;
import org.apache.commons.lang3.StringEscapeUtils;

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
        String rootPackage = config.get("rootPackage").toString();
        String modelPackage = config.get("modelPackage").toString();
        String handlerPackage = config.get("handlerPackage").toString();
        boolean overwriteHandler = config.toBoolean("overwriteHandler");
        boolean overwriteHandlerTest = config.toBoolean("overwriteHandlerTest");
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

        transfer(targetPath, "", "pom.xml", templates.hybrid.service.pom.template(config));
        //transfer(targetPath, "", "Dockerfile", templates.dockerfile.template(config));
        transfer(targetPath, "", ".gitignore", templates.hybrid.gitignore.template());
        transfer(targetPath, "", "README.md", templates.hybrid.service.README.template());
        transfer(targetPath, "", "LICENSE", templates.hybrid.LICENSE.template());
        transfer(targetPath, "", ".classpath", templates.hybrid.classpath.template());
        transfer(targetPath, "", ".project", templates.hybrid.project.template());


        // config
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "service.yml", templates.hybrid.serviceYml.template(config));

        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "server.yml", templates.hybrid.serverYml.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version"), enableHttp, "49587", enableHttps, "49588", enableRegistry));
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "secret.yml", templates.hybrid.secretYml.template());
        transfer(targetPath, ("src.test.resources.config").replace(".", separator), "security.yml", templates.hybrid.securityYml.template());

        if(supportClient) {
            transfer(targetPath, ("src.main.resources.config").replace(".", separator), "client.yml", templates.hybrid.clientYml.template());
        } else {
            transfer(targetPath, ("src.test.resources.config").replace(".", separator), "client.yml", templates.hybrid.clientYml.template());
        }

        transfer(targetPath, ("src.test.resources.config.oauth").replace(".", separator), "primary.crt", templates.hybrid.primaryCrt.template());
        transfer(targetPath, ("src.test.resources.config.oauth").replace(".", separator), "secondary.crt", templates.hybrid.secondaryCrt.template());

        // logging
        transfer(targetPath, ("src.main.resources").replace(".", separator), "logback.xml", templates.hybrid.logback.template());
        transfer(targetPath, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.hybrid.logback.template());

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
                if(overwriteHandler) transfer(targetPath, ("src.main.java." + handlerPackage).replace(".", separator), item.get("handler") + ".java", templates.hybrid.handler.template(handlerPackage, host, service, item, example));
                String serviceId  = host + "/" + service + "/" + item.get("name") + "/" + item.get("version");
                Map<String, Object> map = new HashMap<>();
                map.put("schema", item.get("schema"));
                map.put("scope", item.get("scope"));
                services.put(serviceId, map);
            }

            // handler test cases
            transfer(targetPath, ("src.test.java." + handlerPackage + ".").replace(".", separator),  "TestServer.java", templates.hybrid.testServer.template(handlerPackage));
            if(overwriteHandlerTest) {
                for(Any item : items) {
                    transfer(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), item.get("handler") + "Test.java", templates.hybrid.handlerTest.template(handlerPackage, host, service, item));
                }
            }
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
        JsonStream.serialize(services, new FileOutputStream(FileSystems.getDefault().getPath(targetPath, ("src.main.resources").replace(".", separator), "schema.json").toFile()));
    }
}
