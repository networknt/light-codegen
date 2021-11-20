package com.networknt.codegen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

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
