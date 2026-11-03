package com.networknt.codegen;

import org.junit.jupiter.api.Test;
import tools.jackson.core.exc.StreamConstraintsException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorConfigScalarCompatibilityTest {
    @Test
    void coreScalarsWorkWithoutALegacyBooleanTrigger() {
        String yaml = """
                True: True
                nulls: [~, Null, NULL, null]
                integers: [+12, 010, -010, 0o10, 0x10]
                floats: [.5, 1e3]
                quoted: ['True', '~', '010']
                tagged: !!str ~
                anchored: &octal 010
                alias: *octal
                """;
        var input = GeneratorConfigYaml.readTree(yaml);
        assertTrue(input.get("True").booleanValue());
        for (var value : input.get("nulls")) assertTrue(value.isNull());
        for (int i = 0; i < 5; i++)
            assertEquals(List.of(12, 8, -8, 8, 16).get(i), input.get("integers").get(i).intValue());
        assertEquals(0.5, input.get("floats").get(0).doubleValue());
        assertEquals(1000.0, input.get("floats").get(1).doubleValue());
        assertEquals("True", input.get("quoted").get(0).asString());
        assertEquals("~", input.get("quoted").get(1).asString());
        assertEquals("010", input.get("quoted").get(2).asString());
        assertEquals("~", input.get("tagged").asString());
        assertEquals(8, input.get("anchored").intValue());
        assertEquals("octal", input.get("alias").asString());
        assertEquals(input, GeneratorConfigYaml.readTree(yaml.getBytes(StandardCharsets.UTF_16)));
        assertTrue(Generator.yamlMapper.readTree("value: True").get("value").isString());
    }

    @Test
    void emptyAndBlockScalarsKeepTheirMeaning() {
        var input = GeneratorConfigYaml.readTree("empty:\nnext: True\nblock: |\n  True ~ 010\n");
        assertTrue(input.get("empty").isNull());
        assertTrue(input.get("next").booleanValue());
        assertEquals("True ~ 010\n", input.get("block").asString());
        assertTrue(GeneratorConfigYaml.readTree("[~, NULL]").get(0).isNull());
    }

    @Test
    void explicitNumericTagsStillEnforceJacksonLimits() {
        assertThrows(StreamConstraintsException.class,
                () -> GeneratorConfigYaml.readTree("octal: 0" + "1".repeat(1001)));
        assertThrows(StreamConstraintsException.class,
                () -> GeneratorConfigYaml.readTree("decimal: +" + "1".repeat(1001)));
    }
}
