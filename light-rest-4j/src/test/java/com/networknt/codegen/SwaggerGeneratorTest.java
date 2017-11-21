package com.networknt.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.networknt.codegen.rest.SwaggerGenerator;
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
public class SwaggerGeneratorTest {
    public static String targetPath = "/tmp/swagger";
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
        Any anyConfig = JsonIterator.parse(SwaggerGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        Any anyModel = JsonIterator.parse(SwaggerGeneratorTest.class.getResourceAsStream(swaggerName), 1024).readAny();

        SwaggerGenerator generator = new SwaggerGenerator();
        generator.generate(targetPath, anyModel, anyConfig);
    }

    @Test
    public void testGetOperationList() throws IOException {
        Any anyModel = JsonIterator.parse(SwaggerGeneratorTest.class.getResourceAsStream(swaggerName), 1024).readAny();
        SwaggerGenerator generator = new SwaggerGenerator();
        List list = generator.getOperationList(anyModel);
        System.out.println(mapper.writeValueAsString(list));
    }

    @Test
    public void testGetFramework() {
        SwaggerGenerator generator = new SwaggerGenerator();
        Assert.assertEquals("swagger", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        SwaggerGenerator generator = new SwaggerGenerator();
        ByteBuffer bf = generator.getConfigSchema();
        Assert.assertNotNull(bf);
        System.out.println(bf.toString());
    }
}
