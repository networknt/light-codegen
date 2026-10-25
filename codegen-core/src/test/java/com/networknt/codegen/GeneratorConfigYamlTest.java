package com.networknt.codegen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorConfigYamlTest {
    @Test
    void preservesLegacyKeysNumbersAliasesAndMixedCaseStrings() {
        var input = GeneratorConfigYaml.readTree("yes: on\nno: off\noctal: 010\na: &x yes\nb: *x\n");
        assertTrue(input.get("yes").booleanValue());
        assertFalse(input.get("no").booleanValue());
        assertEquals(8, input.get("octal").intValue());
        assertEquals("x", input.get("b").asString());
        for (String value : new String[]{"y", "Y", "n", "N", "yEs", "tRuE", "oN"})
            assertEquals(value, GeneratorConfigYaml.readTree("flag: " + value).get("flag").asString());
        assertEquals("yes", GeneratorConfigYaml.readTree("flag: !!str yes").get("flag").asString());
        assertTrue(GeneratorConfigYaml.readTree("flag: !!bool 'yes'").get("flag").booleanValue());
    }

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
