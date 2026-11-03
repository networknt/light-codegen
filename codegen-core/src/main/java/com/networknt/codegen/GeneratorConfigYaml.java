package com.networknt.codegen;

import com.networknt.jackson.JacksonDefaults;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.YamlUnicodeReader;
import org.snakeyaml.engine.v2.api.lowlevel.Parse;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.CoreScalarResolver;
import org.snakeyaml.engine.v2.resolver.JsonScalarResolver;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.core.exc.StreamConstraintsException;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLReadFeature;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.Locale;
import java.util.regex.Pattern;

/** Configuration-only Core scalar and legacy boolean compatibility on Jackson 3.1. */
public final class GeneratorConfigYaml {
    private static final Set<String> TRUE = Set.of("yes", "Yes", "YES", "on", "On", "ON");
    private static final Set<String> FALSE = Set.of("no", "No", "NO", "off", "Off", "OFF");
    static final int MAX_CONFIG_CODE_POINTS = 20_000_000;
    private static final CoreScalarResolver CORE_SCALARS = new CoreScalarResolver(true);
    private static final JsonScalarResolver JSON_SCALARS = new JsonScalarResolver();
    // Conservative lexical filter: ordinary JSON scalars need no event pass.
    private static final Pattern COMPATIBILITY_CANDIDATE = Pattern.compile(
            "(?<![\\w-])(?:(?:yes|Yes|YES|on|On|ON|no|No|NO|off|Off|OFF|True|TRUE|False|FALSE|Null|NULL|~)(?![\\w-])"
                    + "|[-+]?(?:0[0-9ox]|\\.[0-9iInN])|\\+[0-9])");
    private static final Pattern NON_FINITE = Pattern.compile("[-+]?\\.(?i:inf|nan)");
    private static final LoadSettings EVENTS = LoadSettings.builder()
            .setCodePointLimit(MAX_CONFIG_CODE_POINTS).setUseMarks(true).build();
    private static final YAMLMapper MAPPER = JacksonDefaults.configure(YAMLMapper.builder(
            JacksonDefaults.configure(YAMLFactory.builder()).enable(YAMLReadFeature.EMPTY_STRING_AS_NULL).build())).build();
    private static final StreamReadConstraints CONSTRAINTS = MAPPER.tokenStreamFactory().streamReadConstraints();

    private GeneratorConfigYaml() { }

    public static JsonNode readTree(String input) {
        if (input == null) return MAPPER.readTree(input);
        if (input.codePointCount(0, input.length()) > MAX_CONFIG_CODE_POINTS)
            throw new StreamConstraintsException("Generator config exceeds " + MAX_CONFIG_CODE_POINTS + " code points");
        CONSTRAINTS.validateDocumentLength(input.length());
        // Jackson 3.1 uses JSON scalar resolution. Preserve the config-only Core
        // schema and legacy booleans through public events instead of 3.2 APIs.
        return MAPPER.readTree(COMPATIBILITY_CANDIDATE.matcher(input).find() ? normalizeScalars(input) : input);
    }

    public static JsonNode readTree(byte[] input) {
        if (input == null) return MAPPER.readTree(input);
        if (input.length > 4L * MAX_CONFIG_CODE_POINTS + 4)
            throw new StreamConstraintsException("Generator config exceeds the maximum encoded document size");
        try (var reader = new YamlUnicodeReader(new ByteArrayInputStream(input))) {
            var text = new StringWriter();
            reader.transferTo(text);
            return readTree(text.toString());
        } catch (IOException exception) {
            throw JacksonIOException.construct(exception);
        }
    }

    /** Rewrite values only; Jackson still owns keys, numeric nodes, aliases and stream limits. */
    private static String normalizeScalars(String input) {
        var containers = new ArrayDeque<Container>();
        StringBuilder output = null;
        int previousCharacter = 0;
        int previousCodePoint = 0;
        try {
            for (var event : new Parse(EVENTS).parseString(input)) {
                switch (event.getEventId()) {
                    case MappingStart, SequenceStart -> {
                        CONSTRAINTS.validateNestingDepth(containers.size() + 1);
                        containers.push(new Container(event.getEventId() == org.snakeyaml.engine.v2.events.Event.ID.MappingStart));
                    }
                    case MappingEnd, SequenceEnd -> {
                        containers.pop();
                        completeNode(containers);
                    }
                    case Scalar -> {
                        var scalar = (ScalarEvent) event;
                        boolean key = !containers.isEmpty() && containers.peek().mapping && containers.peek().key;
                        String value = scalar.getValue();
                        if (key) CONSTRAINTS.validateNameLength(value.length());
                        else CONSTRAINTS.validateStringLength(value.length());
                        String replacement = key ? null : scalarReplacement(scalar);
                        if (replacement != null) {
                            int startPoint = scalar.getStartMark().orElseThrow().getIndex();
                            int endPoint = scalar.getEndMark().orElseThrow().getIndex();
                            int start = input.offsetByCodePoints(previousCharacter, startPoint - previousCodePoint);
                            int end = input.offsetByCodePoints(start, endPoint - startPoint);
                            if (output == null) output = new StringBuilder(input.length());
                            output.append(input, previousCharacter, start);
                            if (scalar.getAnchor().isPresent())
                                output.append('&').append(scalar.getAnchor().orElseThrow().getValue()).append(' ');
                            output.append(replacement);
                            previousCharacter = end;
                            previousCodePoint = endPoint;
                        }
                        completeNode(containers);
                    }
                    case Alias -> completeNode(containers);
                    // readTree consumes one document; preserve its trailing-document policy.
                    case DocumentEnd -> {
                        return output == null ? input : output.append(input, previousCharacter, input.length()).toString();
                    }
                    default -> { }
                }
            }
        } catch (YamlEngineException exception) {
            // Keep Jackson's normal exception type and diagnostics for malformed YAML.
            return input;
        }
        return output == null ? input : output.append(input, previousCharacter, input.length()).toString();
    }

    /** Return a changed scalar spelling, or null when Jackson can consume it unchanged. */
    private static String scalarReplacement(ScalarEvent scalar) {
        String value = scalar.getValue();
        // Empty nodes have context-dependent marks. Let EMPTY_STRING_AS_NULL handle them.
        if (value.isEmpty()) return null;
        boolean legacy = TRUE.contains(value) || FALSE.contains(value);
        boolean explicitBoolean = scalar.getTag().filter(Tag.BOOL.getValue()::equals).isPresent();
        if (legacy && (explicitBoolean || scalar.isPlain() && scalar.getTag().isEmpty()))
            return TRUE.contains(value) ? "true" : "false";
        boolean implicit = scalar.isPlain() && scalar.getTag().isEmpty();
        boolean explicitFloat = scalar.getTag().filter(Tag.FLOAT.getValue()::equals).isPresent();
        if ((implicit || explicitFloat) && NON_FINITE.matcher(value).matches()) {
            // Jackson 3.1 can parse Java non-finite spellings through an explicit float tag.
            String number = value.toLowerCase(Locale.ROOT).endsWith("nan") ? "NaN"
                    : value.startsWith("-") ? "-Infinity" : "Infinity";
            return "!!float " + number;
        }
        if (!implicit) return null;

        Tag core = CORE_SCALARS.resolve(value, true);
        if (Tag.BOOL.equals(core)) {
            String canonical = value.toLowerCase(Locale.ROOT);
            return canonical.equals(value) ? null : canonical;
        }
        if (Tag.NULL.equals(core)) return "null".equals(value) ? null : "null";
        if (core.equals(JSON_SCALARS.resolve(value, true))) return null;
        // Explicit tags preserve Jackson's numeric radix parsing and stream limits.
        if (Tag.INT.equals(core)) return "!!int " + value;
        if (Tag.FLOAT.equals(core)) return "!!float " + value;
        return null;
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
