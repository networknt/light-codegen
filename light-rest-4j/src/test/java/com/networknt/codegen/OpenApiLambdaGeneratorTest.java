package com.networknt.codegen;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.codegen.rest.OpenApiLambdaGenerator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class OpenApiLambdaGeneratorTest {
    public static String targetPath = "/tmp/openapilambda";
    public static String proxyTargetPath = "/tmp/openapiProxyGradle";
    public static String proxyMavenTargetPath = "/tmp/openapiProxyMaven";
    public static String configName = "/configlambda.json";
    public static String configProxyLambda = "/configProxyLambda.json";
    public static String configProxyMaven = "/configlambdamaven.json";
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
        JsonNode config = Generator.jsonMapper.readTree(OpenApiLambdaGeneratorTest.class.getResourceAsStream(configName));
        JsonNode model = Generator.jsonMapper.readTree(OpenApiLambdaGeneratorTest.class.getResourceAsStream(openapiJson));
        OpenApiLambdaGenerator generator = new OpenApiLambdaGenerator();
        generator.generate(targetPath, model, config);
    }

    @Test
    public void testGeneratorYaml() throws IOException {
        JsonNode config = Generator.jsonMapper.readTree(OpenApiLambdaGeneratorTest.class.getResourceAsStream(configName));
        JsonNode model = Generator.yamlMapper.readTree(OpenApiLambdaGeneratorTest.class.getResourceAsStream(openapiYaml));
        OpenApiLambdaGenerator generator = new OpenApiLambdaGenerator();
        generator.generate(targetPath, model, config);
    }

    @Test
    public void testProxyGeneratorYaml() throws IOException {
        JsonNode config = Generator.jsonMapper.readTree(OpenApiLambdaGeneratorTest.class.getResourceAsStream(configProxyLambda));
        JsonNode model = Generator.yamlMapper.readTree(OpenApiLambdaGeneratorTest.class.getResourceAsStream(openapiYaml));
        OpenApiLambdaGenerator generator = new OpenApiLambdaGenerator();
        generator.generate(proxyTargetPath, model, config);
    }

    @Test
    public void testProxyMavenGeneratorYaml() throws IOException {
        JsonNode config = Generator.jsonMapper.readTree(OpenApiLambdaGeneratorTest.class.getResourceAsStream(configProxyMaven));
        JsonNode model = Generator.yamlMapper.readTree(OpenApiLambdaGeneratorTest.class.getResourceAsStream(openapiYaml));
        OpenApiLambdaGenerator generator = new OpenApiLambdaGenerator();
        generator.generate(proxyMavenTargetPath, model, config);
    }

    @Test
    public void testGetOperationList() throws IOException {
        JsonNode model = Generator.jsonMapper.readTree(OpenApiLambdaGeneratorTest.class.getResourceAsStream(openapiJson));
        OpenApiLambdaGenerator generator = new OpenApiLambdaGenerator();
        List list = generator.getOperationList(model);
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
