package com.networknt.codegen;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorConfigYamlReviewRegressionTest {
    @Test
    void valuelessKeysAndEmptyDocumentsRetainTheirStructure() {
        for (String yaml : List.of("{a, b: 1}", "? a\n? b\n", "empty:\nnext: 1\n",
                "items: [one, two]\nmissing:\n", "mapping: {a, b: 1}\n")) {
            var expected = Generator.jsonMapper.readTree(switch (yaml) {
                case "{a, b: 1}" -> "{\"a\":null,\"b\":1}";
                case "? a\n? b\n" -> "{\"a\":null,\"b\":null}";
                case "empty:\nnext: 1\n" -> "{\"empty\":null,\"next\":1}";
                case "items: [one, two]\nmissing:\n" -> "{\"items\":[\"one\",\"two\"],\"missing\":null}";
                default -> "{\"mapping\":{\"a\":null,\"b\":1}}";
            });
            assertEquals(expected, GeneratorConfigYaml.readTree(yaml), yaml);
            assertEquals(expected, GeneratorConfigYaml.readTree(yaml.getBytes(StandardCharsets.UTF_16)), yaml);
        }
        for (String yaml : List.of("---\n---\na: 1", "---\n", "---\n# empty\n...\n---\na: 1", "---\n---\nlegacy: yes")) {
            var result = GeneratorConfigYaml.readTree(yaml);
            assertTrue(result == null || result.isNull(), yaml + " -> " + result);
        }
    }

    @Test
    void valuelessKeysAlsoWorkWhenOtherScalarsNeedNormalization() {
        assertEquals(Generator.jsonMapper.readTree("{\"a\":null,\"b\":1,\"legacy\":true}"),
                GeneratorConfigYaml.readTree("{a, b: 1, legacy: yes}"));
        assertEquals(Generator.jsonMapper.readTree("{\"a\":null,\"b\":null,\"legacy\":true}"),
                GeneratorConfigYaml.readTree("? a\n? b\nlegacy: yes"));
        assertEquals("", GeneratorConfigYaml.readTree("empty: ''\nlegacy: yes").get("empty").asString());
    }

    @Test
    void implicitNonFiniteFloatsMatchTheCoreBaseline() {
        for (String value : List.of(".inf", "-.inf", "+.inf", ".nan", ".INF", ".Inf", ".NaN", ".NAN")) {
            for (String prefix : List.of("", "legacy: yes\n")) {
                String yaml = prefix + "value: " + value;
                double expected = value.toLowerCase(java.util.Locale.ROOT).endsWith("nan") ? Double.NaN
                        : value.startsWith("-") ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                var result = GeneratorConfigYaml.readTree(yaml).get("value");
                assertTrue(result.isNumber(), yaml);
                assertEquals(expected, result.doubleValue(), yaml);
                assertEquals(result, GeneratorConfigYaml.readTree(yaml.getBytes(StandardCharsets.UTF_16))
                        .get("value"), yaml);
                assertEquals(value, GeneratorConfigYaml.readTree(prefix + "value: '" + value + "'")
                        .get("value").asString());
                assertEquals(result, GeneratorConfigYaml.readTree(prefix + "value: !!float " + value).get("value"));
            }
        }
    }
}
