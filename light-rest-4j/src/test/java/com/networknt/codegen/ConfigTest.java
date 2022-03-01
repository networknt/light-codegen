package com.networknt.codegen;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

public class ConfigTest {
    public static String configName = "/config.json";
    public static String configYamlName = "/config.yaml";
    public static JsonNode anyConfig = null;
    public static JsonNode anyYamlConfig = null;

    @BeforeAll
    public static void setUp() throws IOException {
        // load config file
        anyConfig = Generator.jsonMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(configName));
        anyYamlConfig = Generator.yamlMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(configYamlName));
    }

    @Test
    public void testDbName() {
        assertEquals("mysql", anyConfig.path("dbInfo").path("name").textValue());
    }

    @Test
    public void testDbSupport() {
        assertTrue(anyConfig.get("supportDb").booleanValue());
    }

    @Test
    public void testUseLightProxy() {
        assertTrue(anyYamlConfig.get("useLightProxy").booleanValue());
    }

}
