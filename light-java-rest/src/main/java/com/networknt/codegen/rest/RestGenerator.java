package com.networknt.codegen.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import com.networknt.codegen.Generator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
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
    public void generate(String targetPath, Object model, Map<String, Object> config) throws IOException {
        // whoever is calling this needs to make sure that model is converted to Map<String, Object>
        String rootPackage = (String)config.get("rootPackage");
        String modelPackage = (String)config.get("modelPackage");
        String handlerPackage = (String)config.get("handlerPackage");

        transfer(targetPath, "", "pom.xml", templates.pom.template(config));
        transfer(targetPath, "", "Dockerfile", templates.dockerfile.template(config));
        transfer(targetPath, "", ".gitignore", templates.gitignore.template());
        transfer(targetPath, "", "README.md", templates.README.template());
        transfer(targetPath, "", "LICENSE", templates.LICENSE.template());
        transfer(targetPath, "", ".classpath", templates.classpath.template());
        transfer(targetPath, "", ".project", templates.project.template());

        // config
        transfer(targetPath, ("src.main.resources.config").replace(".", separator), "server.yml", templates.server.template(config.get("groupId") + "." + config.get("artifactId") + "-" + config.get("version")));

        // test cases
        transfer(targetPath, ("src.test.java." + handlerPackage + ".").replace(".", separator),  "TestServer.java", templates.testServer.template(handlerPackage));


        // last step to write swagger.json as the directory must be there already.
        writeSwagger(FileSystems.getDefault().getPath(targetPath, ("src.main.resources.config").replace(".", separator), "swagger.json"), model);

    }

    public void writeSwagger(Path path, Object model) throws IOException {

        mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(path.toFile()), model);
    }
}
