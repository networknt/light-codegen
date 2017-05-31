package com.networknt.codegen.hybrid;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by steve on 28/04/17.
 */
public class HybridServiceGeneratorTest {
    public static String targetPath = "/tmp/hybridservice";
    public static String configName = "/serviceConfig.json";
    public static String schemaName = "/schema.json";

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
        Any anyConfig = JsonIterator.parse(HybridServiceGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        Any anyModel = JsonIterator.parse(HybridServiceGeneratorTest.class.getResourceAsStream(schemaName), 1024).readAny();

        HybridServiceGenerator generator = new HybridServiceGenerator();
        generator.generate(targetPath, anyModel, anyConfig);
    }

    @Test
    public void testGetFramework() {
        HybridServiceGenerator generator = new HybridServiceGenerator();
        Assert.assertEquals("light-hybrid-4j-service", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        HybridServiceGenerator generator = new HybridServiceGenerator();
        Assert.assertNotNull(generator.getConfigSchema());
        System.out.println(generator.getConfigSchema().toString());
    }

}
