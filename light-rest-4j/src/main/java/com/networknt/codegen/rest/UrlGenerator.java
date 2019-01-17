package com.networknt.codegen.rest;

import com.mifmif.common.regex.Generex;
import com.networknt.oas.model.Parameter;
import com.networknt.oas.model.Schema;
import com.networknt.utility.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class UrlGenerator {
    private static String NUMBER = "number";
    private static String INTEGER = "integer";
    private static String STRING = "string";
    private static String INT32 = "int32";
    private static String INT64 = "int64";
    private static String FLOAT = "float";
    private static String DOUBLE = "double";
    private static int DEFAULT_MIN_NUM = 1;
    private static int DEFAULT_MAX_NUM = 100;
    private static int DEFAULT_MIN_LENGTH = 5;
    private static int DEFAULT_MAX_LENGTH = 30;
    private static String PATH_TEMPLATE_PATTERN = "\\{(.*?)\\}";
    private static String DEFAULT_STR_PATTERN = "[a-zA-Z]+";
    private static String IN_PATH = "path";
    private static String IN_QUERY = "query";


    private static Logger logger = LoggerFactory.getLogger(UrlGenerator.class);
    public static String generateQueryParamUrl(List<Parameter> parameters) {
        String url = "";
        url += parameters.stream()
                .filter(parameter -> IN_QUERY.equals(parameter.getIn()))
                .map(parameter -> parameter.getName() + "=" + generateValidParam(parameter))
                .collect(Collectors.joining("&"));
        //if query params have value, put a "?" in ahead
        url = StringUtils.isBlank(url) ? "" : "?" + url;
        return url;
    }

    private static String generateEncodedValidParam(Parameter parameter) {
        String encoded = "";
        try {
            encoded = URLEncoder.encode(generateValidParam(parameter), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encoded;
    }

    private static String generateValidParam(Parameter parameter) {
        String validParam = "";
        Schema schema = parameter.getSchema();
        if(!schema.getAllOfSchemas().isEmpty() || !schema.getOneOfSchemas().isEmpty()){
            logger.info("dont support one of/ all of schema test case generation");
            return "";
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
        String pattern = schema.getPattern() == null ? DEFAULT_STR_PATTERN : schema.getPattern();
        int minLength = schema.getMinLength() == null ? DEFAULT_MIN_LENGTH : schema.getMinLength();
        int maxLength = schema.getMaxLength() == null ? DEFAULT_MAX_LENGTH : schema.getMaxLength();
        Generex generex = new Generex(pattern);
        return generex.random(minLength, maxLength);
    }

    private static String generateValidNum(Schema schema) {
        //if format is empty, consider it's an int.
        String format = StringUtils.isBlank(schema.getFormat()) ? INT32 : schema.getFormat();
        Number min = schema.getMinimum() == null ? Integer.valueOf(DEFAULT_MIN_NUM) : schema.getMinimum();
        Number max = schema.getMaximum() == null ? Integer.valueOf(DEFAULT_MAX_NUM) : schema.getMaximum();
        String validNumStr = "";
        if(INT32.equals(format) || INT64.equals(format)) {
            int validInt;
            do {
                //validInt is from [min, max]
                validInt = ThreadLocalRandom.current().nextInt(min.intValue(), max.intValue());
                //when exclusiveMinimum || exclusiveMaximum is true, validDouble shouldn't equals to minimum/max value, regenerate.
            } while ((Boolean.TRUE.equals(schema.getExclusiveMinimum()) && validInt == min.intValue())
                || (Boolean.TRUE.equals(schema.getExclusiveMaximum()) == true&& validInt == max.intValue()));
            validNumStr = String.valueOf(validInt);
        } else if(DOUBLE.equals(format) || FLOAT.equals(format)) {
            double validDouble;
            do {
                //validDouble is from [min, max]
                validDouble = ThreadLocalRandom.current().nextDouble(min.doubleValue(), max.doubleValue());
                //when exclusiveMinimum || exclusiveMaximum is true, validDouble shouldn't equals to minimum/max value, regenerate.
            } while ((Boolean.TRUE.equals(schema.getExclusiveMinimum()) && validDouble == min.intValue())
                    || (Boolean.TRUE.equals(schema.getExclusiveMaximum()) == true&& validDouble == max.intValue()));
            validNumStr = String.valueOf(validDouble);
        }

        return validNumStr;
    }

    public static String generateUrl(String basePath, String path, List<Parameter> parameters) {
        String url = "";
        if(!parameters.isEmpty()){
            Optional<Parameter> pathParameter = parameters.stream()
                    .filter(parameter -> IN_PATH.equals(parameter.getIn()))
                    .findFirst();
            if(pathParameter.isPresent()) {
                //generate a valid path parameter then replace {} with it.
                String pathParameterStr = generateValidParam(pathParameter.get());
                path = path.replaceAll(PATH_TEMPLATE_PATTERN, pathParameterStr);
            }

            url = basePath + path + generateQueryParamUrl(parameters);
        }
        return url;
    }
}
