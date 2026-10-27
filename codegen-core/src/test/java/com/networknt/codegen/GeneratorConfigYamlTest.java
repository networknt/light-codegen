package com.networknt.codegen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorConfigYamlTest {
    @Test
    void bothParsingPathsEnforceConfigAndStreamLimits() {
        String oversized = "#".repeat(GeneratorConfigYaml.MAX_CONFIG_CODE_POINTS + 1);
        assertThrows(tools.jackson.core.exc.StreamConstraintsException.class,
                () -> GeneratorConfigYaml.readTree(oversized));
        assertThrows(tools.jackson.core.exc.StreamConstraintsException.class,
                () -> GeneratorConfigYaml.readTree("flag: yes\n" + oversized));
        for (String prefix : new String[]{"flag: true\n", "flag: yes\n"}) {
            assertThrows(tools.jackson.core.exc.StreamConstraintsException.class,
                    () -> GeneratorConfigYaml.readTree(prefix + "nested: " + "[".repeat(1001) + "0" + "]".repeat(1001)));
            assertThrows(tools.jackson.core.exc.StreamConstraintsException.class,
                    () -> GeneratorConfigYaml.readTree(prefix + "number: " + "1".repeat(1001)));
        }
    }

    @Test
    void publicEventNormalizationPreservesUnicodeAnchorsContainersAndEncoding() {
        String yaml = "name: '😀'\r\nyes: on\r\nitems: [yes, {no: off}]\r\n" +
                "a: &shared !!bool 'yes'\r\nb: *shared\r\nquoted: 'yes'\r\n";
        var result = GeneratorConfigYaml.readTree(yaml);
        assertEquals("😀", result.get("name").asString());
        assertTrue(result.get("yes").booleanValue());
        assertTrue(result.get("items").get(0).booleanValue());
        assertFalse(result.get("items").get(1).get("no").booleanValue());
        assertTrue(result.get("a").booleanValue());
        assertEquals("shared", result.get("b").asString());
        assertEquals("yes", result.get("quoted").asString());
        assertEquals(result, GeneratorConfigYaml.readTree(yaml.getBytes(java.nio.charset.StandardCharsets.UTF_16)));
        assertTrue(GeneratorConfigYaml.readTree("flag: yes\n---\n[broken").get("flag").booleanValue());
        assertThrows(tools.jackson.core.JacksonException.class, () -> GeneratorConfigYaml.readTree("flag: [broken"));
    }

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
