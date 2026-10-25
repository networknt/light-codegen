package com.networknt.codegen;

import com.networknt.jackson.Jackson2Defaults;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;

/** Preserves the framework's Jackson 2 policy for generator YAML inputs. */
final class CodegenMappers {
    private CodegenMappers() { }

    static YAMLMapper yamlMapper() {
        var factory = Jackson2Defaults.configure(YAMLFactory.builder()).build();
        return Jackson2Defaults.configure(YAMLMapper.builder(factory)).build();
    }
}
