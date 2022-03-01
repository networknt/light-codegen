package com.networknt.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.codegen.rest.OpenApiLightGenerator;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaParameterizedType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.networknt.codegen.rest.OpenApiGenerator;

public class OpenApiArrayReferenceGeneratorTest {

    public static String targetPath = "target/" + OpenApiArrayReferenceGeneratorTest.class.getSimpleName();
    public static String configName = "/config.json";
    public static String openapiJson = "/array_ref-oa3.json";
    public static String packageName = "com.networknt.petstore.model";

    static JavaPackage javaPackage;

    @BeforeAll
    public static void setUp() throws IOException {
        delete(Paths.get(targetPath).toFile());
        Files.createDirectories(Paths.get(targetPath));

        javaPackage = prepareJavaPackage(targetPath, packageName);
    }

    static void delete(File f) throws IOException {
        if (f.exists()) {
            if (f.isDirectory()) {
                for (File c : f.listFiles())
                    delete(c);
            }
            if (!f.delete()) {
                throw new IOException("Failed to delete file: " + f);
            }
        }
    }

    public static JavaPackage prepareJavaPackage(String targetPath, String packageName) throws IOException {
        JsonNode config = Generator.jsonMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(OpenApiArrayReferenceGeneratorTest.configName));
        JsonNode model = Generator.jsonMapper.readTree(OpenApiKotlinGeneratorTest.class.getResourceAsStream(OpenApiArrayReferenceGeneratorTest.openapiJson));
        OpenApiLightGenerator generator = new OpenApiLightGenerator();
        generator.generate(targetPath, model, config);

        File file = new File(targetPath);
        JavaProjectBuilder javaProjectBuilder = new JavaProjectBuilder();
        javaProjectBuilder.addSourceTree(file);
        return javaProjectBuilder.getPackageByName(packageName);
    }

    @Test
    public void testTypeReferences() {
        JavaClass classResponse = javaPackage.getClassByName("Response");
        assertEquals( 3, classResponse.getFields().size(), "Count of fields");

        logger.debug("The test is to check that the type of contacts is List<List<Contact>> and not ArrayofContacts");
        JavaField contacts = classResponse.getFieldByName("contacts");
        assertTrue(contacts.getType() instanceof JavaParameterizedType, "Data structure for contacts type");

        JavaParameterizedType type = (JavaParameterizedType)contacts.getType();
        assertEquals(java.util.List.class.getName(), type.getFullyQualifiedName(), "Type of contacts");
        assertTrue( type.getActualTypeArguments().get(0) instanceof JavaParameterizedType, "Data structure for the generic");

        type = (JavaParameterizedType)type.getActualTypeArguments().get(0);
        assertEquals( java.util.List.class.getName(), type.getFullyQualifiedName(), "Type of generic");
        assertTrue( type.getActualTypeArguments().get(0) instanceof JavaParameterizedType, "Data structure for the generic of generic");

        type = (JavaParameterizedType)type.getActualTypeArguments().get(0);
        assertEquals( String.format("%s.Contact", packageName), type.getFullyQualifiedName(), "Type of generic of generic");
    }

    @Test
    public void testStringFormats() {
        JavaClass classSignature = javaPackage.getClassByName("Signature");
        assertEquals( 2, classSignature.getFields().size(), "Count of fields");

        logger.debug("The test is to check that the type of contacts  is List<List<Contact> and not ArrayofContacts");
        JavaField fieldType = classSignature.getFieldByName("type");
        JavaField fieldData = classSignature.getFieldByName("data");

        JavaClass type = fieldType.getType();
        assertEquals( "byte", type.getFullyQualifiedName(), "Type of type");

        JavaClass data = fieldData.getType();
        assertEquals("byte[]", data.getFullyQualifiedName(), "Type of type");
    }

    private static final Logger logger = LoggerFactory.getLogger(OpenApiArrayReferenceGeneratorTest.class);
}
