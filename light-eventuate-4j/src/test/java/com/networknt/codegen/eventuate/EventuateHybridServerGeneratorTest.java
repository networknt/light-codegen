package com.networknt.codegen.eventuate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class EventuateHybridServerGeneratorTest {

    public static String targetPath = "/tmp/hybridserver";
    public static String configName = "/hybrid/serverConfig.json";

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
        Any anyConfig = JsonIterator.parse(EventuateHybridServerGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();

        EventuateHybridServerGenerator generator = new EventuateHybridServerGenerator();
        generator.generate(targetPath, null, anyConfig);
    }

    @Test
    public void testGetFramework() {
        EventuateHybridServerGenerator generator = new EventuateHybridServerGenerator();
        Assert.assertEquals("light-hybrid-4j-server", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        EventuateHybridServerGenerator generator = new EventuateHybridServerGenerator();
        Assert.assertNotNull(generator.getConfigSchema());
        System.out.println(generator.getConfigSchema().toString());
    }
}
