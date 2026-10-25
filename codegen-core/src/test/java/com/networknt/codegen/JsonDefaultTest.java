package com.networknt.codegen;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;


public class JsonDefaultTest {
    String s = "{\"enabled\":true,\"level\":5,\"name\":\"steve\"}";

    @Test
    public void testBooleanDefault() throws Exception {
        JsonNode node = Generator.jsonMapper.readTree(s);
        ((ObjectNode)node).put("useData", true);
        boolean b = node.get("useData").booleanValue();
        System.out.println(b);
    }
}
