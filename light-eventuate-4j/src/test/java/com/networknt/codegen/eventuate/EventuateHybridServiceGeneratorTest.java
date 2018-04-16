package com.networknt.codegen.eventuate;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class EventuateHybridServiceGeneratorTest {
    public static String targetPath = "/tmp/hybridservice";
    public static String configName = "/hybrid/serviceConfig.json";
    public static String schemaName = "/hybrid/schema.json";

    @BeforeClass
    public static void setUp() throws IOException {
        // create the output directory
        Files.createDirectories(Paths.get(targetPath));
    }

    //@AfterClass
    public static void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(targetPath));
    }

    @Test
    public void testGenerator() throws IOException {
        Any anyConfig = JsonIterator.parse(EventuateHybridServiceGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        Any anyModel = JsonIterator.parse(EventuateHybridServiceGeneratorTest.class.getResourceAsStream(schemaName), 1024).readAny();

        EventuateHybridServiceGenerator generator = new EventuateHybridServiceGenerator();
        generator.generate(targetPath, anyModel, anyConfig);
    }

    @Test
    public void testGetFramework() {
        EventuateHybridServiceGenerator generator = new EventuateHybridServiceGenerator();
        Assert.assertEquals("light-hybrid-4j-service", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        EventuateHybridServiceGenerator generator = new EventuateHybridServiceGenerator();
        Assert.assertNotNull(generator.getConfigSchema());
        System.out.println(generator.getConfigSchema().toString());
    }

}
