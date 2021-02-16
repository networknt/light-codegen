package com.networknt.codegen.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.codegen.Generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenApiLambdaGenerator implements Generator {
    @Override
    public String getFramework() {
        return "openapilambda";
    }

    /**
     *
     * @param targetPath The output directory of the generated project
     * @param model The optional model data that trigger the generation, i.e. swagger specification, graphql IDL etc.
     * @param config A json object that controls how the generator behaves.
     *
     * @throws IOException IO Exception occurs during code generation
     */
    @Override
    public void generate(final String targetPath, Object model, JsonNode config) throws IOException {

    }
    public List<Map<String, Object>> getOperationList(Object model) {
        return null;
    }

    public class OpenApiPath {
        String path;
        List<MethodFunction> methodList = new ArrayList<>();

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public List<MethodFunction> getMethodList() {
            return methodList;
        }

        public void addMethodFunction(MethodFunction methodFunction) {
            methodList.add(methodFunction);
        }

    }

    public class MethodFunction {
        String method;
        String functionName;

        public MethodFunction(String method, String functionName) {
            this.method = method;
            this.functionName = functionName;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getFunctionName() {
            return functionName;
        }

        public void setFunctionName(String functionName) {
            this.functionName = functionName;
        }
    }

}
