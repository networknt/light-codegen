package com.networknt.codegen;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.networknt.codegen.rest.OpenApiGenerator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Steve Hu
 */
public class OpenApiGeneratorTest {
    public static String targetPath = "/tmp/openapi";
    public static String configName = "/config.json";
    public static String openapiName = "/openapi.json";

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
        Any anyConfig = JsonIterator.parse(OpenApiGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        Any anyModel = JsonIterator.parse(OpenApiGeneratorTest.class.getResourceAsStream(openapiName), 1024).readAny();

        OpenApiGenerator generator = new OpenApiGenerator();
        generator.generate(targetPath, anyModel, anyConfig);
    }

    @Test
    public void testGetOperationList() throws IOException {
        Any anyModel = JsonIterator.parse(SwaggerGeneratorTest.class.getResourceAsStream(openapiName), 1024).readAny();
        OpenApiGenerator generator = new OpenApiGenerator();
        List list = generator.getOperationList(anyModel);
        System.out.println(list);
    }

    @Test
    public void testInjectEndpoints() throws IOException {
        Any anyModel = JsonIterator.parse(SwaggerGeneratorTest.class.getResourceAsStream(openapiName), 1024).readAny();
        OpenApiGenerator generator = new OpenApiGenerator();
        generator.injectEndpoints(anyModel);
        System.out.println(anyModel.toString());
    }

    @Test
    public void testGetFramework() {
        OpenApiGenerator generator = new OpenApiGenerator();
        Assert.assertEquals("openapi", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        OpenApiGenerator generator = new OpenApiGenerator();
        ByteBuffer bf = generator.getConfigSchema();
        Assert.assertNotNull(bf);
        System.out.println(bf.toString());
    }
}
