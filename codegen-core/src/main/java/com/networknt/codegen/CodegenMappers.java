package com.networknt.codegen;

import com.networknt.config.Jackson2Defaults;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;

/** Preserves the framework's Jackson 2 policy for generator YAML inputs. */
final class CodegenMappers {
    private CodegenMappers() { }

    static YAMLMapper yamlMapper() {
        var defaults = Jackson2Defaults.mapper();
        var factory = YAMLFactory.builder().configureForJackson2()
                .streamReadConstraints(defaults.tokenStreamFactory().streamReadConstraints())
                .streamWriteConstraints(defaults.tokenStreamFactory().streamWriteConstraints()).build();
        var builder = YAMLMapper.builder(factory).configureForJackson2();
        for (MapperFeature feature : MapperFeature.values()) {
            builder.configure(feature, defaults.isEnabled(feature));
        }
        for (SerializationFeature feature : SerializationFeature.values()) {
            builder.configure(feature, defaults.isEnabled(feature));
        }
        for (DeserializationFeature feature : DeserializationFeature.values()) {
            builder.configure(feature, defaults.isEnabled(feature));
        }
        for (StreamReadFeature feature : StreamReadFeature.values()) {
            builder.configure(feature, defaults.tokenStreamFactory().isEnabled(feature));
        }
        for (StreamWriteFeature feature : StreamWriteFeature.values()) {
            builder.configure(feature, defaults.tokenStreamFactory().isEnabled(feature));
        }
        return builder.build();
    }
}
