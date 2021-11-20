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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.networknt.codegen.rest.OpenApiGenerator;

public class OpenApiArrayReferenceGeneratorTest {

    public static String targetPath = "target/" + OpenApiArrayReferenceGeneratorTest.class.getSimpleName();
    public static String configName = "/config.json";
    public static String openapiJson = "/array_ref-oa3.json";
    public static String packageName = "com.networknt.petstore.model";

    static JavaPackage javaPackage;

    @BeforeClass
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
        Assert.assertEquals("Count of fields", 3, classResponse.getFields().size());

        logger.debug("The test is to check that the type of contacts is List<List<Contact>> and not ArrayofContacts");
        JavaField contacts = classResponse.getFieldByName("contacts");
        Assert.assertTrue("Data structure for contacts type", contacts.getType() instanceof JavaParameterizedType);

        JavaParameterizedType type = (JavaParameterizedType)contacts.getType();
        Assert.assertEquals("Type of contacts", java.util.List.class.getName(), type.getFullyQualifiedName());
        Assert.assertTrue("Data structure for the generic", type.getActualTypeArguments().get(0) instanceof JavaParameterizedType);

        type = (JavaParameterizedType)type.getActualTypeArguments().get(0);
        Assert.assertEquals("Type of generic", java.util.List.class.getName(), type.getFullyQualifiedName());
        Assert.assertTrue("Data structure for the generic of generic", type.getActualTypeArguments().get(0) instanceof JavaParameterizedType);

        type = (JavaParameterizedType)type.getActualTypeArguments().get(0);
        Assert.assertEquals("Type of generic of generic", String.format("%s.Contact", packageName), type.getFullyQualifiedName());
    }

    @Test
    public void testStringFormats() {
        JavaClass classSignature = javaPackage.getClassByName("Signature");
        Assert.assertEquals("Count of fields", 2, classSignature.getFields().size());

        logger.debug("The test is to check that the type of contacts  is List<List<Contact> and not ArrayofContacts");
        JavaField fieldType = classSignature.getFieldByName("type");
        JavaField fieldData = classSignature.getFieldByName("data");

        JavaClass type = fieldType.getType();
        Assert.assertEquals("Type of type", "byte", type.getFullyQualifiedName());

        JavaClass data = fieldData.getType();
        Assert.assertEquals("Type of type", "byte[]", data.getFullyQualifiedName());
    }

    private static final Logger logger = LoggerFactory.getLogger(OpenApiArrayReferenceGeneratorTest.class);
}
