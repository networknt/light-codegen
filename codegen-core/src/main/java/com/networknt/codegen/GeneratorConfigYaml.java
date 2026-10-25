package com.networknt.codegen;

import com.networknt.jackson.JacksonDefaults;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.YamlUnicodeReader;
import org.snakeyaml.engine.v2.api.lowlevel.Parse;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.Tag;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLReadFeature;
import tools.jackson.dataformat.yaml.YAMLSchema;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Set;

/** Configuration-only legacy boolean compatibility using public YAML event APIs. */
public final class GeneratorConfigYaml {
    private static final Set<String> TRUE = Set.of("yes", "Yes", "YES", "on", "On", "ON");
    private static final Set<String> FALSE = Set.of("no", "No", "NO", "off", "Off", "OFF");
    private static final LoadSettings EVENTS = LoadSettings.builder()
            .setCodePointLimit(Integer.MAX_VALUE).setUseMarks(true).build();
    private static final YAMLMapper MAPPER = JacksonDefaults.configure(YAMLMapper.builder(
            JacksonDefaults.configure(YAMLFactory.builder()).yamlSchema(YAMLSchema.CORE)
                    .enable(YAMLReadFeature.PARSE_OCTAL_NUMBERS).build())).build();

    private GeneratorConfigYaml() { }

    public static JsonNode readTree(String input) {
        return MAPPER.readTree(input == null ? null : normalizeBooleans(input));
    }

    public static JsonNode readTree(byte[] input) {
        if (input == null) return MAPPER.readTree(input);
        try (var reader = new YamlUnicodeReader(new ByteArrayInputStream(input))) {
            var text = new StringWriter();
            reader.transferTo(text);
            return readTree(text.toString());
        } catch (IOException exception) {
            throw JacksonIOException.construct(exception);
        }
    }

    /** Rewrite values only; Jackson still owns keys, numeric nodes, aliases and stream limits. */
    private static String normalizeBooleans(String input) {
        var containers = new ArrayDeque<Container>();
        var output = new StringBuilder(input.length());
        int previousCharacter = 0;
        int previousCodePoint = 0;
        try {
            for (var event : new Parse(EVENTS).parseString(input)) {
                switch (event.getEventId()) {
                    case MappingStart -> containers.push(new Container(true));
                    case SequenceStart -> containers.push(new Container(false));
                    case MappingEnd, SequenceEnd -> {
                        containers.pop();
                        completeNode(containers);
                    }
                    case Scalar -> {
                        var scalar = (ScalarEvent) event;
                        boolean key = !containers.isEmpty() && containers.peek().mapping && containers.peek().key;
                        String value = scalar.getValue();
                        boolean legacy = TRUE.contains(value) || FALSE.contains(value);
                        boolean explicitBoolean = scalar.getTag().filter(Tag.BOOL.getValue()::equals).isPresent();
                        if (!key && legacy && (explicitBoolean || scalar.isPlain() && scalar.getTag().isEmpty())) {
                            int startPoint = scalar.getStartMark().orElseThrow().getIndex();
                            int endPoint = scalar.getEndMark().orElseThrow().getIndex();
                            int start = input.offsetByCodePoints(previousCharacter, startPoint - previousCodePoint);
                            int end = input.offsetByCodePoints(start, endPoint - startPoint);
                            output.append(input, previousCharacter, start);
                            scalar.getAnchor().ifPresent(anchor -> output.append('&').append(anchor.getValue()).append(' '));
                            output.append(TRUE.contains(value) ? "true" : "false");
                            previousCharacter = end;
                            previousCodePoint = endPoint;
                        }
                        completeNode(containers);
                    }
                    case Alias -> completeNode(containers);
                    // readTree consumes one document; preserve its trailing-document policy.
                    case DocumentEnd -> {
                        output.append(input, previousCharacter, input.length());
                        return output.toString();
                    }
                    default -> { }
                }
            }
        } catch (YamlEngineException exception) {
            // Keep Jackson's normal exception type and diagnostics for malformed YAML.
            return input;
        }
        return output.append(input, previousCharacter, input.length()).toString();
    }

    private static void completeNode(ArrayDeque<Container> containers) {
        if (!containers.isEmpty() && containers.peek().mapping) {
            containers.peek().key = !containers.peek().key;
        }
    }

    private static final class Container {
        private final boolean mapping;
        private boolean key = true;

        private Container(boolean mapping) {
            this.mapping = mapping;
        }
    }
}
