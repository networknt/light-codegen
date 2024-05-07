package com.networknt.codegen.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

import static java.io.File.separator;

/**
 * This is the abstract class that all Lambda generator should extend. It provides the common
 * functions and methods that are shared by all Lambda generators.
 */
public class AbstractLambdaGenerator {

    public List<OpenApiPath> getPathList(List<Map<String, Object>> operationList) {
        List<OpenApiPath> pathList = new ArrayList<>();
        Set<String> pathSet = new HashSet<>();
        OpenApiPath openApiPath = null;
        for(Map<String, Object> op : operationList) {
            String path = (String)op.get("path");
            String method = ((String)op.get("method")).toLowerCase();
            String functionName = (String)op.get("functionName");
            if(!pathSet.contains(path)) {
                openApiPath = new OpenApiPath();
                openApiPath.setPath(path);
                pathSet.add(path);
                MethodFunction methodFunction = new MethodFunction(method, functionName);
                openApiPath.addMethodFunction(methodFunction);
                pathList.add(openApiPath);
            } else {
                MethodFunction methodFunction = new MethodFunction(method, functionName);
                openApiPath.addMethodFunction(methodFunction);
            }
        }
        return pathList;
    }

    public static class OpenApiPath {
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

    public static class MethodFunction {
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

    public boolean isPackageDocker(JsonNode config, Boolean defaultValue) {
        boolean packageDocker = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("packageDocker");
        if(jsonNode == null) {
            ((ObjectNode)config).put("packageDocker", packageDocker);
        } else {
            packageDocker = jsonNode.booleanValue();
        }
        return packageDocker;
    }

    public boolean isPublicVpc(JsonNode config, Boolean defaultValue) {
        boolean publicVpc = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("publicVpc");
        if(jsonNode == null) {
            ((ObjectNode)config).put("publicVpc", publicVpc);
        } else {
            publicVpc = jsonNode.booleanValue();
        }
        return publicVpc;
    }

    public String getLaunchType(JsonNode config, String defaultValue) {
        String launchType = defaultValue == null ? "EC2" : defaultValue;
        JsonNode jsonNode = config.get("launchType");
        if(jsonNode == null) {
            ((ObjectNode)config).put("launchType", launchType);
        } else {
            launchType = jsonNode.textValue();
        }
        return launchType;
    }

    public String getRegion(JsonNode config, String defaultValue) {
        String region = defaultValue == null ? "us-east-1" : defaultValue;
        JsonNode jsonNode = config.get("region");
        if(jsonNode == null) {
            ((ObjectNode)config).put("region", region);
        } else {
            region = jsonNode.textValue();
        }
        return region;
    }


}
