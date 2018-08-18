package com.networknt.codegen;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.networknt.codegen.rest.OpenApiGenerator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * @author Steve Hu
 */
public class OpenApiGeneratorTest {
    public static String targetPath = "/tmp/openapi";
    public static String configName = "/config.json";
    public static String openapiJson = "/openapi.json";
    public static String openapiYaml = "/openapi.yaml";
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
    public void testGeneratorJson() throws IOException {
        Any anyConfig = JsonIterator.parse(OpenApiGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        Any anyModel = JsonIterator.parse(OpenApiGeneratorTest.class.getResourceAsStream(openapiJson), 1024).readAny();

        OpenApiGenerator generator = new OpenApiGenerator();
        generator.generate(targetPath, anyModel, anyConfig);
    }

    @Test
    public void testGeneratorYaml() throws IOException {
        Any anyConfig = JsonIterator.parse(OpenApiGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        String strModel = new Scanner(OpenApiGeneratorTest.class.getResourceAsStream(openapiYaml), "UTF-8").useDelimiter("\\A").next();
        OpenApiGenerator generator = new OpenApiGenerator();
        generator.generate(targetPath, strModel, anyConfig);
    }

    @Test
    public void testGetOperationList() throws IOException {
        Any anyModel = JsonIterator.parse(SwaggerGeneratorTest.class.getResourceAsStream(openapiJson), 1024).readAny();
        OpenApiGenerator generator = new OpenApiGenerator();
        List list = generator.getOperationList(anyModel);
        System.out.println(list);
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
