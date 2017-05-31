package com.networknt.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.networknt.codegen.rest.RestGenerator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by stevehu on 2017-04-23.
 */
public class RestGeneratorTest {
    public static String targetPath = "/tmp/rest";
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
        Any anyConfig = JsonIterator.parse(RestGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        Any anyModel = JsonIterator.parse(RestGeneratorTest.class.getResourceAsStream(swaggerName), 1024).readAny();

        RestGenerator generator = new RestGenerator();
        generator.generate(targetPath, anyModel, anyConfig);
    }

    @Test
    public void testGetOperationList() throws IOException {
        Any anyModel = JsonIterator.parse(RestGeneratorTest.class.getResourceAsStream(swaggerName), 1024).readAny();
        RestGenerator generator = new RestGenerator();
        List list = generator.getOperationList(anyModel);
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
