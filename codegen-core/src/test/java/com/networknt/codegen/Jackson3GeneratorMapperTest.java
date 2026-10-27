package com.networknt.codegen;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Jackson3GeneratorMapperTest {
    public static class Input {
        public int value;
    }

    @Test
    void jsonInputRetainsUnknownPropertyNullPrimitiveAndTrailingTokenPolicy() {
        assertThrows(JacksonException.class, () -> Generator.jsonMapper.readValue("{\"unknown\":1}", Input.class));
        assertEquals(0, Generator.jsonMapper.readValue("{\"value\":null}", Input.class).value);
        assertEquals(7, Generator.jsonMapper.readValue("{\"value\":7} trailing", Input.class).value);
    }

    @Test
    void generatorMappersRetainJackson2StreamDepth() {
        String input = "[".repeat(600) + "0" + "]".repeat(600);
        assertDoesNotThrow(() -> Generator.jsonMapper.readTree(input));
        assertDoesNotThrow(() -> Generator.yamlMapper.readTree(input));
        for (var mapper : new tools.jackson.databind.ObjectMapper[]{Generator.jsonMapper, Generator.yamlMapper}) {
            assertEquals(1000, mapper.tokenStreamFactory().streamReadConstraints().getMaxNestingDepth());
            assertEquals(20_000_000, mapper.tokenStreamFactory().streamReadConstraints().getMaxStringLength());
        }
    }

    @Test
    void yamlInputUsesDocumentedYaml12Scalars() {
        Map<?, ?> input = Generator.yamlMapper.readValue("legacy: yes\nflag: true\nlegacyNull: ~\n", Map.class);
        assertEquals("yes", input.get("legacy"));
        assertEquals(Boolean.TRUE, input.get("flag"));
        assertEquals("~", input.get("legacyNull"));
    }
}
