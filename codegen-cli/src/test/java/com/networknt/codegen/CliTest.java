package com.networknt.codegen;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;
import org.junit.jupiter.api.Test;


/**
 * Created by steve on 24/04/17.
 */
public class CliTest {
    private ObjectMapper objectMapper = Generator.yamlMapper;
    private final ObjectReader reader = objectMapper.reader();

    @Test
    public void testJsonModel() throws Exception {
        JsonNode modelNode = reader.readTree(CliTest.class.getClassLoader().getResourceAsStream("openapi.json"));
        System.out.println(modelNode);
    }

    @Test
    public void testYamlModel() throws Exception {
        JsonNode modelNode = reader.readTree(CliTest.class.getClassLoader().getResourceAsStream("openapi.yaml"));
        System.out.println(modelNode);
    }
}
