package com.networknt.codegen;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.networknt.codegen.rest.OpenApiGenerator;
import com.networknt.codegen.rest.OpenApiKotlinGenerator;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaPackage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class OpenApiKotlinGeneratorTest {
    public static String targetPath = "/tmp/openapikotlin";
    public static String configName = "/config.json";
    public static String openapiJson = "/openapi.json";
    public static String openapiYaml = "/openapi.yaml";
    public static String openapiNoServersYaml = "/openapi-noServers.yaml";

    @BeforeClass
    public static void setUp() throws IOException {
        // create the output directory
        Files.createDirectories(Paths.get(targetPath));
    }

    //    @AfterClass
    public static void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(targetPath));
    }

    @Test
    public void testGeneratorJson() throws IOException {
        Any anyConfig = JsonIterator.parse(OpenApiKotlinGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        Any anyModel = JsonIterator.parse(OpenApiKotlinGeneratorTest.class.getResourceAsStream(openapiJson), 1024).readAny();

        OpenApiKotlinGenerator generator = new OpenApiKotlinGenerator();
        generator.generate(targetPath, anyModel, anyConfig);
    }

    @Test
    public void testGeneratorYaml() throws IOException {
        Any anyConfig = JsonIterator.parse(OpenApiKotlinGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        String strModel = new Scanner(OpenApiKotlinGeneratorTest.class.getResourceAsStream(openapiYaml), "UTF-8").useDelimiter("\\A").next();
        OpenApiKotlinGenerator generator = new OpenApiKotlinGenerator();
        generator.generate(targetPath, strModel, anyConfig);
    }

    @Test
    public void testGetOperationList() throws IOException {
        Any anyModel = JsonIterator.parse(OpenApiGeneratorTest.class.getResourceAsStream(openapiJson), 1024).readAny();
        OpenApiKotlinGenerator generator = new OpenApiKotlinGenerator();
        List list = generator.getOperationList(anyModel);
        System.out.println(list);
    }

    @Test
    public void testGetFramework() {
        OpenApiKotlinGenerator generator = new OpenApiKotlinGenerator();
        Assert.assertEquals("openapikotlin", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        OpenApiKotlinGenerator generator = new OpenApiKotlinGenerator();
        ByteBuffer bf = generator.getConfigSchema();
        Assert.assertNotNull(bf);
        System.out.println(bf.toString());
    }
    @Test
    public void testNoServersGeneratorYaml() throws IOException {
        Any anyConfig = JsonIterator.parse(OpenApiKotlinGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        String strModel = new Scanner(OpenApiKotlinGeneratorTest.class.getResourceAsStream(openapiNoServersYaml), "UTF-8").useDelimiter("\\A").next();
        OpenApiKotlinGenerator generator = new OpenApiKotlinGenerator();
        generator.generate(targetPath, strModel, anyConfig);
    }
}
