package com.networknt.codegen;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.networknt.codegen.rest.RestGenerator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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
    public void testGenerator() throws IOException {
        Map<String, Object> config = mapper.readValue(RestGeneratorTest.class.getResourceAsStream(configName), new TypeReference<Map<String,Object>>(){});
        Map<String, Object> model = mapper.readValue(RestGeneratorTest.class.getResourceAsStream(swaggerName), new TypeReference<Map<String,Object>>(){});

        RestGenerator generator = new RestGenerator();
        generator.generate(targetPath, model, config);
    }

    @Test
    public void testGetOperationList() throws IOException {
        Map<String, Object> model = mapper.readValue(RestGeneratorTest.class.getResourceAsStream(swaggerName), new TypeReference<Map<String,Object>>(){});
        RestGenerator generator = new RestGenerator();
        List list = generator.getOperationList(model);
        System.out.println(list);
    }

    @Test
    public void testGetFramework() {
        RestGenerator generator = new RestGenerator();
        Assert.assertEquals("light-rest-4j", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        RestGenerator generator = new RestGenerator();
        ByteBuffer bf = generator.getConfigSchema();
        Assert.assertNotNull(bf);
        System.out.println(bf.toString());
    }
}
