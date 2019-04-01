package com.networknt.codegen.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jsoniter.any.Any;
import com.networknt.codegen.Generator;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

public class OpenApiSpecGenerator implements Generator {
	private static final Logger logger = LoggerFactory.getLogger(OpenApiSpecGenerator.class);
	
	private static final String FRAMEWORK="openapi-spec";
	private static final String CONFIG_SPECGENERATION ="specGeneration";
	private static final String CONFIG_BASEPACKAGES ="basePackages";
	private static final String CONFIG_MERGETO ="mergeTo";
	private static final String COMMA = ",";
	

	@Override
	public String getFramework() {
		return FRAMEWORK;
	}

	@Override
	public void generate(String targetPath, Object model, Any config) throws IOException {
		if (!config.keys().contains(CONFIG_SPECGENERATION)) {
			logger.error("Missing config: cannot find {} in the specified config file", CONFIG_SPECGENERATION);
			return;
		}
		
		Map<String, Any> genConfig = config.get(CONFIG_SPECGENERATION).asMap();
		String basePackages = genConfig.get(CONFIG_BASEPACKAGES).toString();
		String mergeTo = genConfig.get(CONFIG_MERGETO).toString();
		
		String[] basePackageArray = basePackages.split(COMMA);
		
		Map<String, Schema> schemas = new HashMap<>();
		
		for (String packageName: basePackageArray) {
			try (ScanResult scanResult =
			        new ClassGraph()
			            .enableClassInfo()             
			            .whitelistPackages(packageName) 
			            .scan()) {
				
				List<Class<?>> classes = scanResult.getAllClasses().loadClasses();
				
				for (Class<?> cls: classes) {
					
					schemas.putAll(ModelConverters.getInstance().read(cls));
						
				}
			}
		}
		
		OpenAPI openApi = new OpenAPI();
		
		openApi.setComponents(new Components().schemas(schemas));
		
		Json.prettyPrint(openApi);
		Yaml.prettyPrint(openApi);
	}
}
