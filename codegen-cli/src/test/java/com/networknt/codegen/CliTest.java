package com.networknt.codegen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;


/**
 * Created by steve on 24/04/17.
 */
public class CliTest {
    private ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
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
