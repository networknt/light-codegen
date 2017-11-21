package com.networknt.codegen;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class ConfigTest {
    public static String configName = "/config.json";
    public static Any anyConfig = null;

    @BeforeClass
    public static void setUp() throws IOException {
        // load config file
        anyConfig = JsonIterator.parse(SwaggerGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
    }

    @Test
    public void testDbName() {
        Assert.assertEquals("mysql", anyConfig.toString("dbInfo", "name"));
    }

    @Test
    public void testDbSupport() {
        Assert.assertTrue(anyConfig.toBoolean("supportDb"));
    }
}
