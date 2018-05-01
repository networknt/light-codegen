package com.networknt.codegen.eventuate;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class EventuateOpenApiGeneratorTest {
    public static String targetPath = "/tmp/openapi";
    public static String configName = "/rest/config.json";
    public static String openapiName = "/rest/eventuate-rest.json";

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
        Any anyConfig = JsonIterator.parse(EventuateOpenApiGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        Any anyModel = JsonIterator.parse(EventuateOpenApiGeneratorTest.class.getResourceAsStream(openapiName), 1024).readAny();

        EventuateOpenApiGenerator generator = new EventuateOpenApiGenerator();
        generator.generate(targetPath, anyModel, anyConfig);
    }

    @Test
    public void testGetOperationList() throws IOException {
        Any anyModel = JsonIterator.parse(EventuateOpenApiGeneratorTest.class.getResourceAsStream(openapiName), 1024).readAny();
        EventuateOpenApiGenerator generator = new EventuateOpenApiGenerator();
        Any queryModel = anyModel.get("query");

        generator.injectEndpoints(queryModel);

        List list = generator.getOperationList(queryModel);
        System.out.println(list);
    }

    @Test
    public void testInjectEndpoints() throws IOException {
        Any anyModel = JsonIterator.parse(EventuateOpenApiGeneratorTest.class.getResourceAsStream(openapiName), 1024).readAny();
        EventuateOpenApiGenerator generator = new EventuateOpenApiGenerator();
        Any commandModel = anyModel.get("command");

        generator.injectEndpoints(commandModel);
        System.out.println(anyModel.toString());
    }

    @Test
    public void testGetFramework() {
        EventuateOpenApiGenerator generator = new EventuateOpenApiGenerator();
        Assert.assertEquals("eventuate-rest", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        EventuateOpenApiGenerator generator = new EventuateOpenApiGenerator();
        ByteBuffer bf = generator.getConfigSchema();
        Assert.assertNotNull(bf);
        System.out.println(bf.toString());
    }
}
