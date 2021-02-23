package com.networknt.codegen;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class ConfigTest {
    public static String configName = "/config.json";
    public static String configYamlName = "/config.yaml";
    public static JsonNode anyConfig = null;
    public static JsonNode anyYamlConfig = null;

    @BeforeClass
    public static void setUp() throws IOException {
        // load config file
        anyConfig = Generator.jsonMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(configName));
        anyYamlConfig = Generator.yamlMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(configYamlName));
    }

    @Test
    public void testDbName() {
        Assert.assertEquals("mysql", anyConfig.path("dbInfo").path("name").textValue());
    }

    @Test
    public void testDbSupport() {
        Assert.assertTrue(anyConfig.get("supportDb").booleanValue());
    }

    @Test
    public void testUseSidecar() {
        Assert.assertTrue(anyYamlConfig.get("useSidecar").booleanValue());
    }

    @Test
    public void testUseLightProxy() {
        Assert.assertTrue(anyYamlConfig.get("useLightProxy").booleanValue());
    }

}
