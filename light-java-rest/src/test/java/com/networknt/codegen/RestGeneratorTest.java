package com.networknt.codegen;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.networknt.codegen.rest.RestGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by stevehu on 2017-04-23.
 */
public class RestGeneratorTest {
    public static String targetPath = "/tmp/generated";
    public static String configName = "/config.json";
    public static String swaggerName = "/swagger.json";

    ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void setUp() throws IOException {
        // create the output directory
        Files.createDirectories(Paths.get(targetPath));
    }

    //@AfterClass
    public static void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(targetPath));
    }

    @Test
    public void testGenerate() throws IOException {
        Map<String, Object> config = mapper.readValue(RestGeneratorTest.class.getResourceAsStream(configName), new TypeReference<Map<String,Object>>(){});
        Map<String, Object> model = mapper.readValue(RestGeneratorTest.class.getResourceAsStream(swaggerName), new TypeReference<Map<String,Object>>(){});
        System.out.println(config);
        System.out.println(model);
        String rootPackage = (String)config.get("rootPackage");
        String modelPackage = (String)config.get("modelPackage");
        String handlerPackage = (String)config.get("handlerPackage");

        /*
        String output = templates.pom.template(config)
                .render()
                .toString();
        System.out.println(output);
        */
        /*
        ArrayOfByteArraysOutput out = templates.pom.template(config)
                .render(ArrayOfByteArraysOutput.FACTORY);
        ReadableByteChannel rbc = out.asReadableByteChannel();
        FileOutputStream fos = new FileOutputStream(targetPath + "/pom.xml");
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
        System.out.println("File is written");
        */

        try(FileOutputStream fos = new FileOutputStream(targetPath + "/pom.xml");
            ReadableByteChannel rbc = templates.pom.template(config).render(ArrayOfByteArraysOutput.FACTORY).asReadableByteChannel()) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
        /*
        supportingFiles.add(new SupportingFile("swagger.mustache", ("src.main.resources.config").replace(".", java.io.File.separator), "swagger.json"));
        supportingFiles.add(new SupportingFile("handler.mustache", ("src.main.java." + invokerPackage).replace(".", java.io.File.separator), "PathHandlerProvider.java"));
        supportingFiles.add(new SupportingFile("testServer.mustache", ("src.test.java." + apiPackage).replace(".", java.io.File.separator), "TestServer.java"));
        */

    }

    @Test
    public void testGenerator() throws IOException {
        Map<String, Object> config = mapper.readValue(RestGeneratorTest.class.getResourceAsStream(configName), new TypeReference<Map<String,Object>>(){});
        Map<String, Object> model = mapper.readValue(RestGeneratorTest.class.getResourceAsStream(swaggerName), new TypeReference<Map<String,Object>>(){});

        RestGenerator generator = new RestGenerator();
        generator.generate(targetPath, model, config);
    }
}
