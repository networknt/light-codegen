package com.networknt.codegen;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;
import org.snakeyaml.engine.v2.schema.CoreSchema;
import tools.jackson.databind.JsonNode;

/** Configuration-only compatibility for unquoted YAML 1.1 boolean flags.
 * OpenAPI models continue to use the YAML 1.2 mapper; quoted and JSON strings stay strings.
 */
public final class GeneratorConfigYaml {
    private static final Set<String> TRUE = Set.of("true", "yes", "on");
    private static final Set<String> FALSE = Set.of("false", "no", "off");
    private static final LoadSettings SETTINGS = LoadSettings.builder().setAllowDuplicateKeys(true).setSchema(new CoreSchema() {
        @Override
        public ScalarResolver getScalarResolver() {
            var fallback = super.getScalarResolver();
            return (value, implicit) -> {
                String lower = value.toLowerCase(Locale.ROOT);
                return implicit && (TRUE.contains(lower) || FALSE.contains(lower))
                        ? Tag.BOOL : fallback.resolve(value, implicit);
            };
        }

        @Override
        public Map<Tag, ConstructNode> getSchemaTagConstructors() {
            var constructors = new HashMap<>(super.getSchemaTagConstructors());
            constructors.put(Tag.BOOL, node -> {
                String value = ((ScalarNode) node).getValue().toLowerCase(Locale.ROOT);
                if (TRUE.contains(value)) return true;
                if (FALSE.contains(value)) return false;
                throw new IllegalArgumentException("Invalid YAML boolean: " + value);
            });
            return constructors;
        }
    }).build();

    private GeneratorConfigYaml() { }

    public static JsonNode readTree(String input) {
        return Generator.jsonMapper.valueToTree(new Load(SETTINGS).loadFromString(input));
    }

    public static JsonNode readTree(byte[] input) {
        return Generator.jsonMapper.valueToTree(new Load(SETTINGS).loadFromInputStream(new ByteArrayInputStream(input)));
    }
}
