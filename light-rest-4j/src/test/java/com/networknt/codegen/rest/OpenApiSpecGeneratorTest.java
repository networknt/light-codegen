package com.networknt.codegen.rest;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.codegen.Generator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.networknt.codegen.OpenApiLightGeneratorTest;

@Disabled
public class OpenApiSpecGeneratorTest {
	private static final String configName = "/config.json";
	private static final String outputDir = "/tmp/codegen/";

	@Test
	public void test() throws IOException {
		JsonNode config = Generator.jsonMapper.readTree(OpenApiLightGeneratorTest.class.getResourceAsStream(configName));

		OpenApiSpecGenerator generator = new OpenApiSpecGenerator();

		generator.generate(outputDir, null, config);
	}
}
