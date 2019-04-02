package com.networknt.codegen.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
	private static final String CONFIG_OUTPUTFORMAT="outputFormat";
	private static final String CONFIG_OUTPUTFILENAME="outputFilename";
	private static final String COMMA = ",";
	private static final String JSON="json";
	private static final String YAML="yaml";
	private static final String YML="y l";
	private static final String DEFAULT_OUTPUT_NAME="openapi_generated";
	

	@Override
	public String getFramework() {
		return FRAMEWORK;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void generate(String targetPath, Object model, Any config) throws IOException {
		if (StringUtils.isBlank(targetPath)) {
			logger.error("Output location is not specified.");
			return;			
		}
		
		if (!config.keys().contains(CONFIG_SPECGENERATION)) {
			logger.error("Missing config: cannot find {} in the specified config file", CONFIG_SPECGENERATION);
			return;
		}
		
		Map<String, Any> genConfig = config.get(CONFIG_SPECGENERATION).asMap();
		String basePackages = genConfig.get(CONFIG_BASEPACKAGES).toString();
		String mergeTo = genConfig.get(CONFIG_MERGETO).toString();
		String outputFormat = genConfig.get(CONFIG_OUTPUTFORMAT).toString();
		String outputFilename = genConfig.get(CONFIG_OUTPUTFILENAME).toString();
		
		File output_dir = new File(targetPath);
		
		if (!output_dir.exists() || !output_dir.isDirectory()) {
			output_dir.mkdirs();
		}
		
		File outputFile = new File(output_dir, StringUtils.isBlank(outputFilename)?DEFAULT_OUTPUT_NAME:outputFilename);
		
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
		
		openApi = merge(openApi, mergeTo);
		
		String specStr=StringUtils.EMPTY;
		
		if (StringUtils.equalsIgnoreCase(outputFormat, JSON)) {
			specStr = Json.pretty(openApi);
		}else {
			specStr = Yaml.pretty(openApi);
		}
		
		Files.write(Paths.get(outputFile.toURI()), specStr.getBytes());
	}

	@SuppressWarnings("rawtypes")
	private OpenAPI merge(OpenAPI generatedSpec, String mergeTo) {
		if (StringUtils.isNotBlank(mergeTo)) {
			File destFile = new File(mergeTo);
			
			if (destFile.isFile()) {
				try {
					OpenAPI openAPI=null;
					String ext = getFileExtension(destFile);
					if (StringUtils.equalsIgnoreCase(ext, JSON)) {
						openAPI = Json.mapper().readValue(destFile, OpenAPI.class);
					}else if (StringUtils.equalsIgnoreCase(ext, YML)||StringUtils.equalsIgnoreCase(ext, YAML)) {
						openAPI = Yaml.mapper().readValue(destFile, OpenAPI.class);
					}else {
						logger.error("unknow file format: {}", ext);
					}
					
					if (null!=openAPI) {
						Components components = openAPI.getComponents();
						
						if (null == components) {
							components = new Components();
						}
						
						Map<String, Schema> schemas = new HashMap<>();
						
						schemas.putAll(components.getSchemas());
						
						schemas.putAll(generatedSpec.getComponents().getSchemas());
						
						components.setSchemas(schemas);
						
						openAPI.setComponents(components);
						
						return openAPI;
					}
					
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		
		return generatedSpec;
	}
	
	private String getFileExtension(File file) {
	    String name = file.getName();
	    int lastIndexOf = name.lastIndexOf(".");
	    if (lastIndexOf == -1) {
	        return ""; // empty extension
	    }
	    return name.substring(lastIndexOf);
	}
}
