package com.networknt.codegen.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.codegen.Generator;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
