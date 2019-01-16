package com.networknt.codegen.rest;

import com.networknt.oas.model.Parameter;
import com.networknt.oas.model.Schema;
import com.networknt.utility.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public class UrlGenerator {
    private static String NUMBER = "number";
    private static String INTEGER = "integer";
    private static String STRING = "string";
    private static String INT32 = "int32";
    private static String INT64 = "int64";
    private static String FLOAT = "float";
    private static String DOUBLE = "double";


    private static Logger logger = LoggerFactory.getLogger(UrlGenerator.class);
    public static String generateUrl(List<Parameter> parameters) {
        String url = "";
        for(Parameter parameter : parameters) {

            String validParam = generateValidParam(parameter);
        }
        return url;
    }

    private static String generateValidParam(Parameter parameter) {
        String validParam = "";
        Schema schema = parameter.getSchema();
        if(!schema.getAllOfSchemas().isEmpty()){
            logger.info("dont support one of schema test case generation");
            return "";
        } else if(!schema.getOneOfSchemas().isEmpty()) {
            schema = schema.getOneOfSchemas().get(0);
        } else if(!schema.getAnyOfSchemas().isEmpty()) {
            schema = schema.getAnyOfSchemas().get(0);
        }
        String type = schema.getType();
        if (!StringUtils.isEmpty(type)) {
            if(NUMBER.equals(type.toLowerCase()) || INTEGER.equals(type.toLowerCase())) {
                validParam = generateValidNum(schema);
            } else if(type.toLowerCase().equals(STRING)) {
                validParam = generateValidStr(schema);
            } else {
                logger.info("unsupported param type to generate test case: {}/ {}", parameter.getName(), type);
            }
        }
        return validParam;
    }

    private static String generateValidStr(Schema schema) {
        return "";
    }

    private static String generateValidNum(Schema schema) {
        String format = StringUtils.isBlank(schema.getFormat()) ? INT32 : schema.getFormat();
        Number min = schema.getMinimum();
        Number max = schema.getMaximum();
        String validNumStr = "";

        if(INT32.equals(format) || INT64.equals(format)) {
            int validInt;
            do {
                //validInt is from [min, max),  thus always exclude the max from range
                validInt = (int) (Math.random() * (max.intValue() - min.intValue() + 1) + min.intValue());
                //when exclusiveMinimum is true, validInt shouldn't equals to minimum value, regenerate.
            } while (schema.getExclusiveMinimum() && validInt == min.intValue());
            validNumStr = String.valueOf(validInt);
        } else if(DOUBLE.equals(format) || FLOAT.equals(format)) {
            double validDouble;
            do {
                //validDouble is from [min, max),  thus always exclude the max from range
                validDouble = Math.random() * (max.doubleValue() - min.doubleValue() + 1) + min.doubleValue();
                //when exclusiveMinimum is true, validDouble shouldn't equals to minimum value, regenerate.
            } while ((schema.getExclusiveMinimum() && validDouble == min.intValue()) );
            validNumStr = String.valueOf(validDouble);
        }

        return validNumStr;
    }

    public static String generateUrl(String basePath, String path, List<Parameter> parameters) {
        String url = "";
        if(!parameters.isEmpty()){
            url = basePath + path + generateUrl(parameters);
        }
        return url;
    }
}
