package com.networknt.codegen;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.config.Config;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.networknt.codegen.rest.OpenApiGenerator;

import static java.io.File.separator;
import static org.junit.Assert.assertTrue;

/**
 * @author Steve Hu
 */
public class OpenApiGeneratorTest {
    public static String targetPath = "/tmp/openapi";
    public static String configName = "/config.json";
    public static String configKafkaName = "/configKafka.json";
    public static String openapiJson = "/openapi.json";
    public static String openapiYaml = "/openapi.yaml";
    public static String accountInfoYaml = "/account-info.yaml";
    public static String openapiEnumYaml = "/openapi-enum.yaml";
    public static String openapiErrorYaml = "/openapi-error.yaml";
    public static String openapiNoServersYaml = "/openapi-noServers.yaml";
    public static String packageName = "com.networknt.petstore.model";

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
        JsonNode configNode = Generator.jsonMapper.readTree(OpenApiGeneratorTest.class.getResourceAsStream(configName));
        JsonNode modelNode = Generator.jsonMapper.readTree(OpenApiGeneratorTest.class.getResourceAsStream(openapiJson));
        OpenApiGenerator generator = new OpenApiGenerator();
        generator.generate(targetPath, modelNode, configNode);
    }

    @Test
    public void testGeneratorYaml() throws IOException {
        JsonNode configNode = Generator.jsonMapper.readTree(OpenApiGeneratorTest.class.getResourceAsStream(configName));
        JsonNode modelNode = Generator.yamlMapper.readTree(OpenApiGeneratorTest.class.getResourceAsStream(openapiYaml));
        OpenApiGenerator generator = new OpenApiGenerator();
        generator.generate(targetPath, modelNode, configNode);
    }

    @Test
    public void testGeneratorAccountInfo() throws IOException {
        JsonNode configNode = Generator.jsonMapper.readTree(OpenApiGeneratorTest.class.getResourceAsStream(configName));
        JsonNode modelNode = Generator.yamlMapper.readTree(OpenApiGeneratorTest.class.getResourceAsStream(accountInfoYaml));
        OpenApiGenerator generator = new OpenApiGenerator();
        generator.generate(targetPath, modelNode, configNode);
    }

    @Test
    public void testGetOperationList() throws IOException {
        JsonNode anyModel = Config.getInstance().getMapper().readTree(OpenApiGeneratorTest.class.getResourceAsStream(openapiJson));
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
    @Test
    public void testNoServersGeneratorYaml() throws IOException {
        JsonNode configNode = Generator.jsonMapper.readTree(OpenApiGeneratorTest.class.getResourceAsStream(configName));
        JsonNode modelNode = Generator.yamlMapper.readTree(OpenApiGeneratorTest.class.getResourceAsStream(openapiNoServersYaml));
        OpenApiGenerator generator = new OpenApiGenerator();
        generator.generate(targetPath, modelNode, configNode);
    }

    @Test
    public void testConvertInvalidVariableName() {
        String[] invalidVariableNames = {"na me", "new", "1", "1+1", "n/a"};
        String[] validVariableNames = {"na_me", "_new", "_1", "_1_1", "n_a"};
        for (int i = 0; i < invalidVariableNames.length; i++) {
            String string = OpenApiGenerator.convertToValidJavaVariableName(invalidVariableNames[i]);
            Assert.assertEquals(validVariableNames[i], string);
        }
    }

    @Test
    public void testGeneratorYamlEnum() throws IOException {
        JsonNode configNode = Generator.jsonMapper.readTree(OpenApiGeneratorTest.class.getResourceAsStream(configName));
        JsonNode modelNode = Generator.yamlMapper.readTree(OpenApiGeneratorTest.class.getResourceAsStream(openapiEnumYaml));
        OpenApiGenerator generator = new OpenApiGenerator();
        generator.generate(targetPath, modelNode, configNode);
    }

    @Test
    @Ignore
    public void testGeneratorYamlError() throws IOException {
        JsonNode configNode = Generator.jsonMapper.readTree(OpenApiGeneratorTest.class.getResourceAsStream(configName));
        JsonNode modelNode = Generator.yamlMapper.readTree(OpenApiGeneratorTest.class.getResourceAsStream(openapiErrorYaml));
        OpenApiGenerator generator = new OpenApiGenerator();
        generator.generate(targetPath, modelNode, configNode);
    }

    @Test
    public void testGeneratorKafka() throws IOException {
        JsonNode configNode = Generator.jsonMapper.readTree(OpenApiGeneratorTest.class.getResourceAsStream(configKafkaName));
        JsonNode modelNode = Generator.yamlMapper.readTree(OpenApiGeneratorTest.class.getResourceAsStream(openapiYaml));
        OpenApiGenerator generator = new OpenApiGenerator();
        generator.generate(targetPath, modelNode, configNode);
    }

    @Test
    public void testBasePath() {
        String basePath = null;
        String url = "https://lightapi.net/service";
        int protocolIndex = url.indexOf("://");
        int pathIndex = url.indexOf('/', protocolIndex + 3);
        if (pathIndex > 0) {
            basePath = url.substring(pathIndex);
        }
        Assert.assertEquals("/service", basePath);
    }
}
