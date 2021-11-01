package com.networknt.codegen.hybrid;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.codegen.Generator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by steve on 28/04/17.
 */
public class HybridServiceGeneratorTest {
    public static String targetPath = "/tmp/hybridservice";
    public static String configName = "/serviceConfig.json";
    public static String schemaName = "/schema.json";

    @BeforeAll
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
        JsonNode config = Generator.jsonMapper.readTree(HybridServiceGeneratorTest.class.getResourceAsStream(configName));
        JsonNode model = Generator.jsonMapper.readTree(HybridServiceGeneratorTest.class.getResourceAsStream(schemaName));
        HybridServiceGenerator generator = new HybridServiceGenerator();
        generator.generate(targetPath, model, config);
    }

    @Test
    public void testGetFramework() {
        HybridServiceGenerator generator = new HybridServiceGenerator();
        assertEquals("light-hybrid-4j-service", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        HybridServiceGenerator generator = new HybridServiceGenerator();
        assertNotNull(generator.getConfigSchema());
        System.out.println(generator.getConfigSchema().toString());
    }

}
