package com.networknt.codegen;

import com.networknt.jackson.Jackson2Defaults;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.io.IOContext;
import tools.jackson.core.util.BufferRecycler;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLParser;
import tools.jackson.dataformat.yaml.YAMLReadFeature;
import tools.jackson.dataformat.yaml.YAMLSchema;

import java.io.CharArrayReader;
import java.io.InputStream;
import java.io.Reader;
import java.util.Set;

/** Configuration-only YAML 1.1 scalar compatibility; model parsing remains YAML 1.2. */
public final class GeneratorConfigYaml {
    private static final Set<String> TRUE = Set.of("yes", "Yes", "YES", "on", "On", "ON");
    private static final Set<String> FALSE = Set.of("no", "No", "NO", "off", "Off", "OFF");
    private static final YAMLMapper MAPPER = Jackson2Defaults.configure(
            YAMLMapper.builder(new ConfigFactory())).build();

    private GeneratorConfigYaml() { }

    public static JsonNode readTree(String input) {
        return MAPPER.readTree(input);
    }

    public static JsonNode readTree(byte[] input) {
        return MAPPER.readTree(input);
    }

    /** Keep Jackson's field names, alias representation, numeric nodes and limits. */
    private static final class ConfigFactory extends YAMLFactory {
        private static final long serialVersionUID = 1L;

        private ConfigFactory() {
            super(Jackson2Defaults.configure(YAMLFactory.builder()).yamlSchema(YAMLSchema.CORE)
                    .enable(YAMLReadFeature.PARSE_OCTAL_NUMBERS));
        }

        @Override
        protected YAMLParser _createParser(ObjectReadContext context, IOContext io, Reader reader) {
            return new ConfigParser(context, io, _getBufferRecycler(),
                    context.getStreamReadFeatures(_streamReadFeatures),
                    context.getFormatReadFeatures(_formatReadFeatures), _loadSettings, reader);
        }

        @Override
        protected YAMLParser _createParser(ObjectReadContext context, IOContext io, InputStream input) {
            return _createParser(context, io, _createReader(input, null, io));
        }

        @Override
        protected YAMLParser _createParser(ObjectReadContext context, IOContext io,
                                           byte[] input, int offset, int length) {
            return _createParser(context, io, _createReader(input, offset, length, null, io));
        }

        @Override
        protected YAMLParser _createParser(ObjectReadContext context, IOContext io,
                                           char[] input, int offset, int length, boolean recyclable) {
            return _createParser(context, io, new CharArrayReader(input, offset, length));
        }
    }

    private static final class ConfigParser extends YAMLParser {
        private ConfigParser(ObjectReadContext context, IOContext io, BufferRecycler recycler,
                             int streamFeatures, int formatFeatures, LoadSettings settings, Reader reader) {
            super(context, io, recycler, streamFeatures, formatFeatures, settings, reader);
        }

        @Override
        protected Boolean _matchYAMLBoolean(String value, int length) {
            if (TRUE.contains(value)) return Boolean.TRUE;
            if (FALSE.contains(value)) return Boolean.FALSE;
            return super._matchYAMLBoolean(value, length);
        }

        @Override
        protected JsonToken _decodeScalar(ScalarEvent event) throws JacksonException {
            JsonToken token = super._decodeScalar(event);
            if (token == JsonToken.VALUE_STRING && event.isPlain() && event.getTag().isEmpty()) {
                if (TRUE.contains(event.getValue())) return JsonToken.VALUE_TRUE;
                if (FALSE.contains(event.getValue())) return JsonToken.VALUE_FALSE;
            }
            return token;
        }
    }
}
