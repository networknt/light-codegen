package com.networknt.codegen.hybrid;

import com.networknt.codegen.Generator;
import com.networknt.jackson.JacksonDefaults;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by steve on 28/04/17.
 */
public class HybridServerGeneratorTest {

    public static String targetPath = "/tmp/hybridserver";
    public static String configName = "/serverConfig.json";

    ObjectMapper mapper = JacksonDefaults.mapper();

    @BeforeAll
    public static void setUp() throws IOException {
        // create the output directory
        Files.createDirectories(Paths.get(targetPath));
    }

    //@AfterAll
    public static void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(targetPath));
    }

    @Test
    public void testGenerator() throws IOException {
        JsonNode config = Generator.jsonMapper.readTree(HybridServerGeneratorTest.class.getResourceAsStream(configName));
        HybridServerGenerator generator = new HybridServerGenerator();
        generator.generate(targetPath, null, config);
    }

    @Test
    public void testGetFramework() {
        HybridServerGenerator generator = new HybridServerGenerator();
        assertEquals("light-hybrid-4j-server", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        HybridServerGenerator generator = new HybridServerGenerator();
        assertNotNull(generator.getConfigSchema());
        System.out.println(generator.getConfigSchema().toString());
    }
}
