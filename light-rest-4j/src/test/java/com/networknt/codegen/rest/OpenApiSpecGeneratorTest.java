package com.networknt.codegen.rest;

import java.io.IOException;

import org.junit.Test;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.networknt.codegen.OpenApiGeneratorTest;

public class OpenApiSpecGeneratorTest {
	private static String configName = "/config.json";
	
	@Test
	public void test() throws IOException {
		Any anyConfig = JsonIterator.parse(OpenApiGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
		
		OpenApiSpecGenerator generator = new OpenApiSpecGenerator();
		
		generator.generate(null, null, anyConfig);
		
		
	}
}
