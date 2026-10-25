package com.networknt.codegen;

import com.networknt.codegen.rest.OpenApiKotlinGenerator;
import com.networknt.codegen.rest.OpenApiLightGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.node.ObjectNode;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class Jackson3GeneratedBuildTest {
    @TempDir
    Path output;

    private ObjectNode config() {
        return (ObjectNode) Generator.jsonMapper.readTree(getClass().getResourceAsStream("/config.json"));
    }

    @Test
    void kotlinBuildHasUsableFilenamesAndJackson3Toolchain() throws Exception {
        ObjectNode config = config();
        config.put("buildMaven", false);
        new OpenApiKotlinGenerator().generate(output.toString(),
                Generator.yamlMapper.readTree(getClass().getResourceAsStream("/openapi.yaml")), config);
        String build = Files.readString(output.resolve("build.gradle.kts"));
        assertTrue(Files.isRegularFile(output.resolve("settings.gradle.kts")));
        assertFalse(Files.exists(output.resolve("buildGradleKts.rocker.raw")));
        assertTrue(build.contains("implementation(\"tools.jackson.module\", \"jackson-module-kotlin\""));
        assertTrue(build.contains("jvmToolchain(25)"));
        assertFalse(build.contains("jackson-datatype-jsr310"));
        assertTrue(Files.readString(output.resolve("gradle/wrapper/gradle-wrapper.properties"))
                .contains("gradle-9.3.1-bin.zip"));
    }

    @Test
    void restPomUsesJackson3AndJackson3LoggingEncoder() throws Exception {
        ObjectNode config = config();
        config.put("buildMaven", true);
        new OpenApiLightGenerator().generate(output.toString(),
                Generator.yamlMapper.readTree(getClass().getResourceAsStream("/openapi.yaml")), config);
        String pom = Files.readString(output.resolve("pom.xml"));
        assertTrue(pom.contains("<version.jackson>3.2.3</version.jackson>"));
        assertTrue(pom.contains("<version.json-schema-validator>3.0.8</version.json-schema-validator>"));
        assertTrue(pom.contains("<version.logstash>9.0</version.logstash>"));
        assertTrue(pom.contains("<groupId>tools.jackson.core</groupId>"));
        assertFalse(pom.contains("jackson-datatype-jsr310"));
    }
}
