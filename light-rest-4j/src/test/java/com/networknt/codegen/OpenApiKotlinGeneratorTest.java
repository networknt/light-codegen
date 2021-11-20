package com.networknt.codegen;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.codegen.rest.OpenApiKotlinGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class OpenApiKotlinGeneratorTest {
    public static String targetPath = "/tmp/openapikotlin";
    public static String configName = "/config.json";
    public static String openapiYaml = "/openapi.yaml";
    public static String openapiNoServersYaml = "/openapi-noServers.yaml";

    @BeforeAll
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
        JsonNode config = Generator.jsonMapper.readTree(OpenApiKotlinGeneratorTest.class.getResourceAsStream(configName));
        JsonNode model = Generator.yamlMapper.readTree(OpenApiKotlinGeneratorTest.class.getResourceAsStream(openapiYaml));
        OpenApiKotlinGenerator generator = new OpenApiKotlinGenerator();
        generator.generate(targetPath, model, config);
    }

    @Test
    public void testGeneratorYaml() throws IOException {
        JsonNode config = Generator.jsonMapper.readTree(OpenApiKotlinGeneratorTest.class.getResourceAsStream(configName));
        JsonNode model = Generator.yamlMapper.readTree(OpenApiKotlinGeneratorTest.class.getResourceAsStream(openapiYaml));
        OpenApiKotlinGenerator generator = new OpenApiKotlinGenerator();
        generator.generate(targetPath, model, config);
    }

    @Test
    public void testGetOperationList() throws IOException {
        JsonNode model = Generator.yamlMapper.readTree(OpenApiKotlinGeneratorTest.class.getResourceAsStream(openapiYaml));
        JsonNode config = Generator.jsonMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(configName));
        OpenApiKotlinGenerator generator = new OpenApiKotlinGenerator();
        List list = generator.getOperationList(model, config);
        System.out.println(list);
    }

    @Test
    public void testGetFramework() {
        OpenApiKotlinGenerator generator = new OpenApiKotlinGenerator();
        assertEquals("openapikotlin", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        OpenApiKotlinGenerator generator = new OpenApiKotlinGenerator();
        ByteBuffer bf = generator.getConfigSchema();
        assertNotNull(bf);
        System.out.println(bf.toString());
    }
    @Test
    public void testNoServersGeneratorYaml() throws IOException {
        JsonNode config = Generator.jsonMapper.readTree(OpenApiKotlinGeneratorTest.class.getResourceAsStream(configName));
        JsonNode model = Generator.yamlMapper.readTree(OpenApiKotlinGeneratorTest.class.getResourceAsStream(openapiNoServersYaml));
        OpenApiKotlinGenerator generator = new OpenApiKotlinGenerator();
        generator.generate(targetPath, model, config);
    }
}
