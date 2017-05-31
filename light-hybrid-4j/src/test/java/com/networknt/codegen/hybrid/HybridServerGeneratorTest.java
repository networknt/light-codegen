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
public class HybridServerGeneratorTest {

    public static String targetPath = "/tmp/hybridserver";
    public static String configName = "/serverConfig.json";

    ObjectMapper mapper = new ObjectMapper();

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
        Any anyConfig = JsonIterator.parse(HybridServerGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();

        HybridServerGenerator generator = new HybridServerGenerator();
        generator.generate(targetPath, null, anyConfig);
    }

    @Test
    public void testGetFramework() {
        HybridServerGenerator generator = new HybridServerGenerator();
        Assert.assertEquals("light-hybrid-4j-server", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        HybridServerGenerator generator = new HybridServerGenerator();
        Assert.assertNotNull(generator.getConfigSchema());
        System.out.println(generator.getConfigSchema().toString());
    }
}
