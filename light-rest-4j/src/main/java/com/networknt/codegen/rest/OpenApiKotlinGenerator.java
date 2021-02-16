package com.networknt.codegen.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.codegen.Generator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class OpenApiKotlinGenerator implements Generator {
    @Override
    public String getFramework() {
        return "openapikotlin";
    }

    /**
     *
     * @param targetPath The output directory of the generated project
     * @param model The optional model data that trigger the generation, i.e. swagger specification, graphql IDL etc.
     * @param config A json object that controls how the generator behaves.
     * @throws IOException IO Exception occurs during code generation
     */
    @Override
    public void generate(String targetPath, Object model, JsonNode config) throws IOException {

    }
    public List<Map<String, Object>> getOperationList(Object model) {
        return null;
    }

}
