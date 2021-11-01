package com.networknt.codegen.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.codegen.Generator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by steve on 01/05/17.
 */
public class GraphqlGeneratorTest {

    public static String targetPath = "/tmp/graphql";
    public static String configName = "/config.json";
    public static String schemaName = "/schema.graphqls";

    ObjectMapper mapper = new ObjectMapper();

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
    public void testGeneratorWithSchema() throws IOException {
        JsonNode configNode = Generator.jsonMapper.readTree(GraphqlGeneratorTest.class.getResourceAsStream(configName));
        try(InputStream is = GraphqlGenerator.class.getResourceAsStream(schemaName)) {
            String schema = convertStreamToString(is);
            GraphqlGenerator generator = new GraphqlGenerator();
            generator.generate(targetPath, schema, configNode);
        }
    }

    @Test
    public void testGeneratorWithoutSchema() throws IOException {
        JsonNode configNode = Generator.jsonMapper.readTree(GraphqlGeneratorTest.class.getResourceAsStream(configName));
        GraphqlGenerator generator = new GraphqlGenerator();
        generator.generate(targetPath, null, configNode);
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Test
    public void testGetFramework() {
        GraphqlGenerator generator = new GraphqlGenerator();
        assertEquals("light-graphql-4j", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        GraphqlGenerator generator = new GraphqlGenerator();
        ByteBuffer bf = generator.getConfigSchema();
        assertNotNull(bf);
        System.out.println(bf.toString());
    }

}
