package com.networknt.codegen.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.resolver.Resolver;

import com.jsoniter.any.Any;

public class YAMLFileParameterizer {
	private static final Logger logger = LoggerFactory.getLogger(YAMLFileParameterizer.class);
	private static final String KEY_SEPARATOR = ".";
	private static final String YML_EXT = ".yml";
	
	public static final String CLASS_PATH_PREFIX="classpath:";
	public static final String DEFAULT_RESOURCE_LOCATION = CLASS_PATH_PREFIX+"handlerconfig/";
	public static final String DEFAULT_DEST_DIR = "src/main/resources/config";
	
	protected static final String KEY_GENERATE="generate";
	protected static final String KEY_SKIP_ARRAY="skipArray";
	protected static final String KEY_SKIP_MAP="skipMap";
	
	public static void rewriteAll(String srcLocation, String destDir, Map<String, Any> generateEnvVars) {
		if (fromClasspath(srcLocation)) {
			rewriteResources(resolveLocation(srcLocation), destDir, generateEnvVars);
		}else {
			rewriteFiles(new File(srcLocation), new File(destDir), generateEnvVars);
		}
	}
	
	public static void rewriteFiles(File sourceDir, File destDir, Map<String, Any> generateEnvVars) {
		if (!sourceDir.exists() || !sourceDir.isDirectory()) {
			logger.error("{} does not exist or is not a folder.", sourceDir);
			return;
		}

		if (!destDir.isDirectory() || !destDir.exists()) {
			if (!destDir.mkdir()) {
				logger.error("Failed to create dir {}", destDir);
				return;
			}
		}

		File[] files = sourceDir.listFiles((dir, name)->name.toLowerCase().endsWith(YML_EXT));

		for (File file: files) {
			rewriteFile(file, new File(destDir, file.getName()), generateEnvVars);
		}
	}

	
	public static void rewriteResources(String resourceLocation, String destDir, Map<String, Any> generateEnvVars) {
		try {
			List<String> filenames = IOUtils.readLines(YAMLFileParameterizer.class.getClassLoader().getResourceAsStream(resourceLocation), (String)null);
			
			List<String> ymlFileNames = filenames.stream().filter(name->name.toLowerCase().endsWith(YML_EXT)).collect(Collectors.toList());
			
			File dest = new File(destDir);
			
			if (!dest.isDirectory() || !dest.exists()) {
				if (!dest.mkdir()) {
					logger.error("Failed to create dir {}", destDir);
					return;
				}
			}
			
			for (String filename: ymlFileNames) {
				rewriteResource(stripExtension(filename), resourceLocation+File.separator+filename, destDir+File.separator+filename, generateEnvVars);
			}
			
			
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public static void rewriteResource(String filename, String resourceLocation, String destFilename, Map<String, Any> generateEnvVars) {
		if (logger.isDebugEnabled()) {
			logger.debug("rewriting resource {}", resourceLocation);
		}
		
		if (!getValue(generateEnvVars, KEY_GENERATE)) {
			copyResource(resourceLocation, destFilename);
		}else {
			Node document = loadResource(resourceLocation);
			List<String> lines = readResource(resourceLocation);
			
			if (null!=document && null!=lines) {
				parameterize(filename, document, lines, new File(destFilename), generateEnvVars);
			}
		}
	}
	
	public static void rewriteFile(File srcFile, File destFile, Map<String, Any> generateEnvVars) {
		if (logger.isDebugEnabled()) {
			logger.debug("rewriting file {}", srcFile.getAbsolutePath());
		}
		
		if (!getValue(generateEnvVars, KEY_GENERATE)) {
			copyFile(srcFile, destFile);
		}else {
			Node document = loadFile(srcFile);
			List<String> lines = readFile(srcFile);
			
			String filename = stripExtension(srcFile.getName());
			
			if (null!=document && null!=lines) {
				parameterize(filename, document, lines, destFile, generateEnvVars);
			}
		}
	}
	
	protected static boolean fromClasspath(String location) {
		return StringUtils.trimToEmpty(location).toLowerCase().startsWith(CLASS_PATH_PREFIX);
	}
	
	protected static String resolveLocation(String location) {
		if (StringUtils.isNotBlank(location) && location.contains(CLASS_PATH_PREFIX)) {
			return StringUtils.trimToEmpty(location).substring(CLASS_PATH_PREFIX.length());
		}
		
		return location;
	}
	
	protected static boolean getValue(Map<String, Any> generateEnvVars, String key) {
		if (generateEnvVars.containsKey(key)) {
			return generateEnvVars.get(key).toBoolean();
		}
		
		return false;
	}
	
	protected static void copyResource(String resourceLocation, String destFilename) {
		try (InputStream in = YAMLFileParameterizer.class.getClassLoader().getResourceAsStream(resourceLocation);
				FileOutputStream out = new FileOutputStream(destFilename)) {
			IOUtils.copy(in, out);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	protected static void copyFile(File srcFile, File destFile) {
		if (!srcFile.exists()) {
			logger.error("The file {} cannot be found", srcFile.getAbsolutePath());
			
			return;
		}
		
		try {
			FileUtils.copyFile(srcFile, destFile);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	protected static Node loadResource(String resourceLocation) {
		try (InputStream in = YAMLFileParameterizer.class.getClassLoader().getResourceAsStream(resourceLocation);
				BufferedReader inputReader = new BufferedReader(new InputStreamReader(in))) {
			Composer composer = new Composer(new ParserImpl(new StreamReader(inputReader)), new Resolver());
			return composer.getSingleNode();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
	
	protected static List<String> readResource(String resourceLocation) {
		try (InputStream in = YAMLFileParameterizer.class.getClassLoader().getResourceAsStream(resourceLocation)) {
			return IOUtils.readLines(in, (String) null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
	
	protected static Node loadFile(File file) {
		try (FileReader inputReader = new FileReader(file)) {
			Composer composer = new Composer(new ParserImpl(new StreamReader(inputReader)), new Resolver());
			return composer.getSingleNode();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
	
	protected static List<String> readFile(File file) {
		try {
			return FileUtils.readLines(file, (String) null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
	
	protected static void parameterize(String filename, Node document, List<String> srclines, File destFile, Map<String, Any> generateEnvVars) {
		try (FileWriter writer = new FileWriter(destFile)) {
			if (document instanceof MappingNode) {
				List<String> destlines = parameterize(filename, srclines, (MappingNode) document, generateEnvVars);

				FileUtils.writeLines(destFile, destlines);

			} else {
				throw new UnsupportedNodeTypeException(document.getClass().getCanonicalName());
			}

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	protected static List<String> parameterize(String filename, List<String> srclines, MappingNode node, Map<String, Any> generateEnvVars) {
		List<String> destlines = new ArrayList<>();
		
		List<NodeTuple> tuples = node.getValue();
		
		boolean skipArray = getValue(generateEnvVars, KEY_SKIP_ARRAY);
		boolean skipMap = getValue(generateEnvVars, KEY_SKIP_MAP);
		
		int pos = 0;
		
		for (NodeTuple tuple: tuples) {
			Node k = tuple.getKeyNode();
			Node v = tuple.getValueNode();
			
			int startLine = k.getStartMark().getLine();
			int stopLine = v.getEndMark().getLine();
			
			if (k instanceof ScalarNode) {
				ScalarNode sk = (ScalarNode)k;
				
				if (v instanceof ScalarNode) {
					ScalarNode sv = (ScalarNode)v;
					
					copy(srclines, destlines, pos, startLine);
					destlines.add(String.format("%s:${%s%s%s:%s}", sk.getValue(), filename, KEY_SEPARATOR, sk.getValue(), sv.getValue()));
					
					pos = stopLine+1;
				}else {
					if (!skipArray && !skipMap) {
						copy(srclines, destlines, pos, startLine);
						destlines.add(String.format("%s:${%s%s%s}", sk.getValue(), filename, KEY_SEPARATOR, sk.getValue()));
						
						pos = stopLine+1;
					}else {
						copy(srclines, destlines, pos, stopLine);
						
						pos = stopLine;
					}
				}		
				
				
			}else {
				throw new UnsupportedNodeTypeException(k.getClass().getCanonicalName());
			}
		}
		
		return destlines;
	}
	
	protected static void copy(List<String> srclines, List<String> destlines, int start, int end) {
		if (end>srclines.size()) {
			logger.error("attemp to read line {}. total length {}", end, srclines.size());
			throw new IndexOutOfBoundsException();
		}
		
		for (int i=start; i<end; ++i) {
			destlines.add(srclines.get(i));
		}
	}
	
    protected static String stripExtension (String str) {
        if (StringUtils.isBlank(str)) return str;

        int pos = str.lastIndexOf(".");

        if (pos == -1) return str;

        return str.substring(0, pos);
    }
	
	@SuppressWarnings("serial")
	public static class UnsupportedNodeTypeException extends RuntimeException{
		public UnsupportedNodeTypeException(String nodeType) {
			super(StringUtils.isBlank(nodeType)?"Unsupported node type.":String.format("Nodetype %s is not supported", nodeType));
		}
	}
}
