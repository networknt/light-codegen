package com.networknt.codegen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorConfigYamlTest {
    @Test
    void legacyFlagsRetainMeaningOnlyAtYamlConfigBoundary() {
        for (String value : new String[]{"true", "True", "TRUE", "yes", "Yes", "YES", "on", "On", "ON"}) {
            assertTrue(GeneratorConfigYaml.readTree("enableHttps: " + value).get("enableHttps").booleanValue(), value);
            assertTrue(GeneratorConfigYaml.readTree(("enableHttps: " + value).getBytes(java.nio.charset.StandardCharsets.UTF_8))
                    .get("enableHttps").booleanValue(), value);
        }
        for (String value : new String[]{"false", "False", "FALSE", "no", "No", "NO", "off", "Off", "OFF"})
            assertFalse(GeneratorConfigYaml.readTree("enableHttps: " + value).get("enableHttps").booleanValue(), value);
        assertTrue(GeneratorConfigYaml.readTree("enableHttps: 'yes'").get("enableHttps").isString());
        assertTrue(Generator.jsonMapper.readTree("{\"enableHttps\":\"yes\"}").get("enableHttps").isString());
        assertTrue(Generator.yamlMapper.readTree("enableHttps: yes").get("enableHttps").isString());
        assertTrue(GeneratorConfigYaml.readTree("value: ~").get("value").isNull());
        assertEquals("~", GeneratorConfigYaml.readTree("value: '~'").get("value").asString());
    }
}
