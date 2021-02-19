package com.networknt.codegen.hybrid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.codegen.Generator;

public interface HybridGenerator extends Generator {
    default String getJsonPath(JsonNode config, String defaultValue) {
        String jsonPath = defaultValue == null ? "/api/json" : defaultValue;
        JsonNode jsonNode = config.get("jsonPath");
        if(jsonNode == null) {
            ((ObjectNode)config).put("jsonPath", jsonPath);
        } else {
            jsonPath = jsonNode.textValue();
        }
        return jsonPath;
    }

}
