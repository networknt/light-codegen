package com.networknt.codegen.rest;

import com.networknt.codegen.rest.model.Stock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stevehu on 2017-04-23.
 */
public class Test {
    static public void main(String[] args) {
        Map<String, Object> model = new HashMap<>();
        model.put("v1", "value1");
        Map<String, Object> config = new HashMap<>();
        config.put("v2", "value2");

        String output = templates.test.template(model, config)
                .render()
                .toString();
        System.out.println(output);
    }

}
