package com.networknt.codegen;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.codegen.rest.OpenApiLightGenerator;
import com.networknt.config.Config;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.networknt.codegen.rest.OpenApiGenerator;

import static org.junit.Assert.assertTrue;

/**
 * @author Steve Hu
 */
public class OpenApiLightGeneratorTest {
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
        JsonNode config = Generator.jsonMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(configName));
        JsonNode model = Generator.jsonMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(openapiJson));
        OpenApiLightGenerator generator = new OpenApiLightGenerator();
        generator.generate(targetPath, model, config);
    }

    @Test
    public void testGeneratorYaml() throws IOException {
        JsonNode configNode = Generator.jsonMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(configName));
        JsonNode modelNode = Generator.yamlMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(openapiYaml));
        OpenApiLightGenerator generator = new OpenApiLightGenerator();
        generator.generate(targetPath, modelNode, configNode);
    }

    @Test
    public void testGeneratorAccountInfo() throws IOException {
        JsonNode configNode = Generator.jsonMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(configName));
        JsonNode modelNode = Generator.yamlMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(accountInfoYaml));
        OpenApiLightGenerator generator = new OpenApiLightGenerator();
        generator.generate(targetPath, modelNode, configNode);
    }

    @Test
    public void testGetOperationList() throws IOException {
        JsonNode model = Config.getInstance().getMapper().readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(openapiJson));
        JsonNode config = Generator.jsonMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(configName));
        OpenApiLightGenerator generator = new OpenApiLightGenerator();
        List list = generator.getOperationList(model, config);
        System.out.println(list);
    }

    @Test
    public void testGetFramework() {
        OpenApiLightGenerator generator = new OpenApiLightGenerator();
        Assert.assertEquals("openapi", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        OpenApiLightGenerator generator = new OpenApiLightGenerator();
        ByteBuffer bf = generator.getConfigSchema();
        Assert.assertNotNull(bf);
        System.out.println(bf.toString());
    }
    @Test
    public void testNoServersGeneratorYaml() throws IOException {
        JsonNode configNode = Generator.jsonMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(configName));
        JsonNode modelNode = Generator.yamlMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(openapiNoServersYaml));
        OpenApiLightGenerator generator = new OpenApiLightGenerator();
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
        JsonNode configNode = Generator.jsonMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(configName));
        JsonNode modelNode = Generator.yamlMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(openapiEnumYaml));
        OpenApiLightGenerator generator = new OpenApiLightGenerator();
        generator.generate(targetPath, modelNode, configNode);
    }

    @Test
    @Ignore
    public void testGeneratorYamlError() throws IOException {
        JsonNode configNode = Generator.jsonMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(configName));
        JsonNode modelNode = Generator.yamlMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(openapiErrorYaml));
        OpenApiLightGenerator generator = new OpenApiLightGenerator();
        generator.generate(targetPath, modelNode, configNode);
    }

    @Test
    public void testGeneratorKafka() throws IOException {
        JsonNode configNode = Generator.jsonMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(configKafkaName));
        JsonNode modelNode = Generator.yamlMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(openapiYaml));
        OpenApiLightGenerator generator = new OpenApiLightGenerator();
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
