package com.networknt.codegen.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import com.networknt.codegen.Generator;
import com.networknt.codegen.Utils;
import com.networknt.utility.NioUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.io.File.separator;

/**
 * The input for rest generator include config with json format
 * and swagger specification in json format.
 *
 * The module is swagger spec in json format. And it will be used
 * as Map like config. It is not necessary to parse it to POJO with
 * swagger-parser for code generator.
 *
 * Created by stevehu on 2017-04-23.
 */
public class RestGenerator implements Generator {
    static ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getFramework() {
        return "light-java-rest";
    }

    @Override
    public void generate(String targetPath, Object model, Map<String, Object> config) throws IOException {
        // whoever is calling this needs to make sure that model is converted to Map<String, Object>
        String rootPackage = (String)config.get("rootPackage");
        String modelPackage = (String)config.get("modelPackage");
        String handlerPackage = (String)config.get("handlerPackage");

        transfer(targetPath, "", "pom.xml", templates.rest.pom.template(config));
        transfer(targetPath, "", "Dockerfile", templates.rest.dockerfile.template(config));
        transfer(targetPath, "", ".gitignore", templates.rest.gitignore.template());
        transfer(targetPath, "", "README.md", templates.rest.README.template());
        transfer(targetPath, "", "LICENSE", templates.rest.LICENSE.template());
        transfer(targetPath, "", ".classpath", templates.rest.classpath.template());
        transfer(targetPath, "", ".project", templates.rest.project.template());

        // config
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "server.yml", templates.rest.server.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version")));
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "secret.yml", templates.rest.secret.template());
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "security.yml", templates.rest.security.template());


        transfer(targetPath, ("src.main.resources.config.oauth").replace(".", separator), "primary.crt", templates.rest.primaryCrt.template());
        transfer(targetPath, ("src.main.resources.config.oauth").replace(".", separator), "secondary.crt", templates.rest.secondaryCrt.template());

        transfer(targetPath, ("src.main.resources.META-INF.services").replace(".", separator), "com.networknt.server.HandlerProvider", templates.rest.routingService.template(rootPackage));
        transfer(targetPath, ("src.main.resources.META-INF.services").replace(".", separator), "com.networknt.handler.MiddlewareHandler", templates.rest.middlewareService.template());
        transfer(targetPath, ("src.main.resources.META-INF.services").replace(".", separator), "com.networknt.server.StartupHookProvider", templates.rest.startupHookProvider.template());
        transfer(targetPath, ("src.main.resources.META-INF.services").replace(".", separator), "com.networknt.server.ShutdownHookProvider", templates.rest.shutdownHookProvider.template());

        // logging
        transfer(targetPath, ("src.main.resources").replace(".", separator), "logback.xml", templates.rest.logback.template());
        transfer(targetPath, ("src.test.resources").replace(".", separator), "logback-test.xml", templates.rest.logback.template());

        List<Map<String, Object>> operationList = getOperationList(model);
        // routing
        transfer(targetPath, ("src.main.java." + rootPackage).replace(".", separator), "PathHandlerProvider.java", templates.rest.handlerProvider.template(rootPackage, handlerPackage, operationList));


        // model


        // handler

        for(Map<String, Object> op : operationList){
            String className = (String)op.get("handlerName");
            String example = null;
            if(op.get("example") != null) {
                example = mapper.writeValueAsString(op.get("example"));
            }
            transfer(targetPath, ("src.main.java." + handlerPackage).replace(".", separator), className + ".java", templates.rest.handler.template(handlerPackage, className, example));
        }

        // handler test cases
        transfer(targetPath, ("src.test.java." + handlerPackage + ".").replace(".", separator),  "TestServer.java", templates.rest.testServer.template(handlerPackage));
        for(Map<String, Object> op : operationList){
            transfer(targetPath, ("src.test.java." + handlerPackage).replace(".", separator), (String)op.get("handlerName") + "Test.java", templates.rest.handlerTest.template(handlerPackage, op));
        }

        // transfer binary files without touching them.
        if(Files.notExists(Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator)))) {
            Files.createDirectories(Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator)));
        }
        try (InputStream is = RestGenerator.class.getResourceAsStream("/binaries/server.keystore")) {
            Files.copy(is, Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator), "server.keystore"), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream is = RestGenerator.class.getResourceAsStream("/binaries/server.truststore")) {
            Files.copy(is, Paths.get(targetPath, ("src.main.resources.config.tls").replace(".", separator), "server.truststore"), StandardCopyOption.REPLACE_EXISTING);
        }

        // last step to write swagger.json as the directory must be there already.
        // TODO add server info before write it.
        NioUtils.writeJson(FileSystems.getDefault().getPath(targetPath, ("src.main.resources.config").replace(".", separator), "swagger.json"), model);
    }

    public List<Map<String, Object>> getOperationList(Object model) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> map = (Map<String, Object>)model;
        String basePath = (String)map.get("basePath");
        Map<String, Object> paths = (Map<String, Object>)map.get("paths");

        for(Map.Entry<String, Object> entryPath: paths.entrySet()) {
            String path = entryPath.getKey();
            Map<String, Object> pathValues = (Map<String, Object>)entryPath.getValue();
            for(Map.Entry<String, Object> entryOps: pathValues.entrySet()) {
                Map<String, Object> flattened = new HashMap<>();
                flattened.put("method", entryOps.getKey().toUpperCase());
                flattened.put("capMethod", entryOps.getKey().substring(0, 1).toUpperCase() + entryOps.getKey().substring(1));
                flattened.put("path", basePath + path);
                String normalizedPath = path.replace("{", "").replace("}", "");
                flattened.put("normalizedPath", basePath + normalizedPath);
                flattened.put("handlerName", Utils.camelize(normalizedPath) + Utils.camelize(entryOps.getKey()) + "Handler");
                Map<String, Object> values = (Map<String, Object>)entryOps.getValue();
                Map<String, Object> responses = (Map<String, Object>)values.get("responses");
                if(responses != null) {
                    Map<String, Object> sucessRes = (Map<String, Object>)responses.get("200");
                    if(sucessRes != null) {
                        Map<String, Object> examples = (Map<String, Object>)sucessRes.get("examples");
                        if(examples != null) {
                            Object jsonRes = examples.get("application/json");
                            flattened.put("example", jsonRes);
                        }
                    }
                }
                result.add(flattened);
            }
        }


        return result;
    }
}
