package com.networknt.codegen.rest;

import java.io.IOException;

import org.junit.Test;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.networknt.codegen.OpenApiGeneratorTest;

//@Ignore
public class OpenApiSpecGeneratorTest {
	private static final String configName = "/config.json";
	private static final String outputDir = "/tmp/codegen/";
	
	@Test
	public void test() throws IOException {
		Any anyConfig = JsonIterator.parse(OpenApiGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
		
		OpenApiSpecGenerator generator = new OpenApiSpecGenerator();
		
		generator.generate(outputDir, null, anyConfig);
	}
}
