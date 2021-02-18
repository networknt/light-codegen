package com.networknt.codegen.rest;

import java.util.List;
import java.util.Map;

public interface ModelCallback {
    void callback(String targetPath, String modelPackage, String modelFileName, String enumsIfClass, String parentClassName, String classVarName, boolean abstractIfClass, List<Map<String, Object>> props, List<Map<String, Object>> parentClassProps);
}
