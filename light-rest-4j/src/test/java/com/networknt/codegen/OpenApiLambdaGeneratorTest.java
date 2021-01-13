package com.networknt.codegen;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.networknt.codegen.rest.OpenApiLambdaGenerator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class OpenApiLambdaGeneratorTest {
    public static String targetPath = "/tmp/openapilambda";
    public static String proxyTargetPath = "/tmp/openapiProxyLambda";
    public static String configName = "/configlambda.json";
    public static String configProxyLambda = "/configProxyLambda.json";
    public static String openapiJson = "/openapilambda.json";
    public static String openapiYaml = "/openapilambda.yaml";

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
        Any anyConfig = JsonIterator.parse(OpenApiLambdaGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        Any anyModel = JsonIterator.parse(OpenApiLambdaGeneratorTest.class.getResourceAsStream(openapiJson), 1024).readAny();
        OpenApiLambdaGenerator generator = new OpenApiLambdaGenerator();
        generator.generate(targetPath, anyModel, anyConfig);
    }

    @Test
    public void testGeneratorYaml() throws IOException {
        Any anyConfig = JsonIterator.parse(OpenApiLambdaGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        String strModel = new Scanner(OpenApiLambdaGeneratorTest.class.getResourceAsStream(openapiYaml), "UTF-8").useDelimiter("\\A").next();
        OpenApiLambdaGenerator generator = new OpenApiLambdaGenerator();
        generator.generate(targetPath, strModel, anyConfig);
    }

    @Test
    public void testProxyGeneratorYaml() throws IOException {
        Any anyConfig = JsonIterator.parse(OpenApiLambdaGeneratorTest.class.getResourceAsStream(configProxyLambda), 1024).readAny();
        String strModel = new Scanner(OpenApiLambdaGeneratorTest.class.getResourceAsStream(openapiYaml), "UTF-8").useDelimiter("\\A").next();
        OpenApiLambdaGenerator generator = new OpenApiLambdaGenerator();
        generator.generate(proxyTargetPath, strModel, anyConfig);
    }

    @Test
    public void testGetOperationList() throws IOException {
        Any anyModel = JsonIterator.parse(OpenApiLambdaGeneratorTest.class.getResourceAsStream(openapiJson), 1024).readAny();
        OpenApiLambdaGenerator generator = new OpenApiLambdaGenerator();
        List list = generator.getOperationList(anyModel);
        System.out.println(list);
    }

    @Test
    public void testGetFramework() {
        OpenApiLambdaGenerator generator = new OpenApiLambdaGenerator();
        Assert.assertEquals("openapilambda", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        OpenApiLambdaGenerator generator = new OpenApiLambdaGenerator();
        ByteBuffer bf = generator.getConfigSchema();
        Assert.assertNotNull(bf);
        System.out.println(bf.toString());
    }

}
