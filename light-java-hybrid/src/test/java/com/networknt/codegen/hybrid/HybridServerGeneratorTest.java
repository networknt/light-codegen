package com.networknt.codegen.hybrid;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        Map<String, Object> config = mapper.readValue(HybridServerGenerator.class.getResourceAsStream(configName), new TypeReference<Map<String,Object>>(){});

        HybridServerGenerator generator = new HybridServerGenerator();
        generator.generate(targetPath, null, config);
    }

}
