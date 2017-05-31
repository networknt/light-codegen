package com.networknt.codegen.graphql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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
    public void testGeneratorWithSchema() throws IOException {
        Any anyConfig = JsonIterator.parse(GraphqlGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        try(InputStream is = GraphqlGenerator.class.getResourceAsStream(schemaName)) {
            String schema = convertStreamToString(is);
            GraphqlGenerator generator = new GraphqlGenerator();
            generator.generate(targetPath, schema, anyConfig);
        }
    }

    @Test
    public void testGeneratorWithoutSchema() throws IOException {
        Any anyConfig = JsonIterator.parse(GraphqlGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        GraphqlGenerator generator = new GraphqlGenerator();
        generator.generate(targetPath, null, anyConfig);
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Test
    public void testGetFramework() {
        GraphqlGenerator generator = new GraphqlGenerator();
        Assert.assertEquals("light-graphql-4j", generator.getFramework());
    }

    @Test
    public void testGetConfigSchema() throws IOException {
        GraphqlGenerator generator = new GraphqlGenerator();
        ByteBuffer bf = generator.getConfigSchema();
        Assert.assertNotNull(bf);
        System.out.println(bf.toString());
    }

}
