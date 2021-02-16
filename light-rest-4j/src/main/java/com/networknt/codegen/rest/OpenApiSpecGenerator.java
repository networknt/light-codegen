package com.networknt.codegen.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.codegen.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OpenApiSpecGenerator implements Generator {
    private static final Logger logger = LoggerFactory.getLogger(OpenApiSpecGenerator.class);

    private static final String FRAMEWORK="openapi-spec";

    @Override
    public String getFramework() {
        return FRAMEWORK;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void generate(String targetPath, Object model, JsonNode config) throws IOException {

    }

}
