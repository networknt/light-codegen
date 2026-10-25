package com.networknt.codegen;

import com.networknt.jackson.JacksonDefaults;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;

/** Preserves the framework's Jackson 2 policy for generator YAML inputs. */
final class CodegenMappers {
    private CodegenMappers() { }

    static YAMLMapper yamlMapper() {
        var factory = JacksonDefaults.configure(YAMLFactory.builder()).build();
        return JacksonDefaults.configure(YAMLMapper.builder(factory)).build();
    }
}
