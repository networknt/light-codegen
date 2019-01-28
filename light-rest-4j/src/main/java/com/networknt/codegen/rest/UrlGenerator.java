package com.networknt.codegen.rest;

import com.networknt.oas.model.Parameter;
import com.networknt.oas.model.Schema;
import com.networknt.utility.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * This class is to generate random url based on com.networknt.oas.model.Schema;
 */
public class UrlGenerator {
    private static final String NUMBER = "number";
    private static final String INTEGER = "integer";
    private static final String STRING = "string";
    private static final String BOOLEAN = "boolean";
    private static final String INT32 = "int32";
    private static final String INT64 = "int64";
    private static final String FLOAT = "float";
    private static final String DOUBLE = "double";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final int DEFAULT_MIN_NUM = 1;
    private static final int DEFAULT_MAX_NUM = 100;
    private static final int DEFAULT_MIN_LENGTH = 5;
    private static final int DEFAULT_MAX_LENGTH = 30;
    //replace ${}
    private static final String PATH_TEMPLATE_PATTERN = "\\{(.*?)\\}";
    private static final String IN_PATH = "path";
    private static final String IN_QUERY = "query";
    private static final Logger logger = LoggerFactory.getLogger(UrlGenerator.class);

    /**
     *
     * @param basePath base path of the url
     * @param path path of the url
     * @param parameters all the parameters under an operation
     * @return String generated url
     */
    public static String generateUrl(String basePath, String path, List<Parameter> parameters) {
        String url = basePath + path;
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

    /**
     * based on parameter schemas generate query parameter part of the url
     */
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

    /**
     * generate a valid parameter value based on schema of that parameter
     * @param parameter parameter under an operation
     * @return String parameter value
     */
    protected static String generateValidParam(Parameter parameter) {
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
            } else if(type.toLowerCase().equals(BOOLEAN)) {
                validParam = generateValidBool(schema);
            } else {
                logger.info("unsupported param type to generate test case: {}/ {}", parameter.getName(), type);
            }
        }
        return validParam;
    }

    /**
     * generate bool based on schema
     * @param schema schema of a parameter
     * @return "true" or "false"
     */
    private static String generateValidBool(Schema schema) {
        return ThreadLocalRandom.current().nextBoolean() ? TRUE : FALSE;
    }

    /**
     * generate String based on schema: minLength, maxLength, pattern
     * @param schema schema of a parameter
     * @return String
     */
    private static String generateValidStr(Schema schema) {
        int minLength = schema.getMinLength() == null ? DEFAULT_MIN_LENGTH : schema.getMinLength();
        int maxLength = schema.getMaxLength() == null ? DEFAULT_MAX_LENGTH : schema.getMaxLength();
        String validStr;
        if(maxLength <= minLength) {
            logger.error("maximum length {} should be larger than minimum length {}", maxLength, minLength);
            validStr = RandomStringUtils.random(ThreadLocalRandom.current().nextInt(DEFAULT_MIN_LENGTH, DEFAULT_MAX_LENGTH), true, false);
        } else {
            validStr = RandomStringUtils.random(ThreadLocalRandom.current().nextInt(minLength, maxLength), true, false);
        }
        return validStr;
    }

    /**
     * generate number based on schema: format, minimum, maximum, exclusiveMinimum, exclusiveMaximum,
     * @param schema
     * @return String generated number
     */
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
                || (Boolean.TRUE.equals(schema.getExclusiveMaximum()) && validInt == max.intValue()));
            validNumStr = String.valueOf(validInt);
        } else if(DOUBLE.equals(format) || FLOAT.equals(format)) {
            double validDouble;
            do {
                //validDouble is from [min, max]
                validDouble = ThreadLocalRandom.current().nextDouble(min.doubleValue(), max.doubleValue());
                //when exclusiveMinimum || exclusiveMaximum is true, validDouble shouldn't equals to minimum/max value, regenerate.
            } while ((Boolean.TRUE.equals(schema.getExclusiveMinimum()) && validDouble == min.intValue())
                    || (Boolean.TRUE.equals(schema.getExclusiveMaximum()) && validDouble == max.intValue()));
            validNumStr = String.valueOf(validDouble);
        }
        return validNumStr;
    }
}
