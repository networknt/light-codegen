package com.networknt.codegen.rest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
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

public class YAMLFileParameterizer {
	private static final Logger logger = LoggerFactory.getLogger(YAMLFileParameterizer.class);
	private static final String KEY_SEPARATOR = ".";
	private static final String YML_EXT = ".yml";
	
	public static final String DEFAULT_SOURCE_DIR = "src/main/resources/config/";
	
	public static void rewriteAll(String sourceDir, String destDir, boolean generateEnvVars) {
		rewriteAll(new File(sourceDir), new File(destDir), generateEnvVars);
	}
	
	public static void rewriteAll(File sourceDir, File destDir, boolean generateEnvVars) {
		
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
			rewrite(file, new File(destDir, file.getName()), generateEnvVars);
		}
	}
	
	public static void rewrite(String sourceFilename, String destFilename, boolean generateEnvVars) {
		rewrite(new File(sourceFilename), new File(destFilename), generateEnvVars);
	}
	
	public static void rewrite(File srcFile, File destFile, boolean generateEnvVars) {
		if (!generateEnvVars) {
			copy(srcFile, destFile);
		}else {
			parameterize(srcFile, destFile);
		}
	}
	
	protected static void copy(File srcFile, File destFile) {
		if (!srcFile.exists()) {
			logger.error("File {} does not exist.", srcFile.getAbsolutePath());
			return;
		}
		
		try {
			FileUtils.copyFile(srcFile, destFile);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	protected static void parameterize(File srcFile, File destFile) {
		if (!srcFile.exists()) {
			logger.error("File {} does not exist.", srcFile.getAbsolutePath());
			return;
		}
		
		try {
			Composer composer = new Composer(new ParserImpl(new StreamReader(new FileReader(srcFile))), new Resolver());
			
			Node document = composer.getSingleNode();
			
			if (null==document) {
				logger.warn("Cannot parse the YAML file. The file {} may be empty or invalid.", srcFile.getAbsolutePath());
				return;
			}
			
			String prefix = stripExtension(srcFile.getName());
			
			if (document instanceof MappingNode) {
				List<String> srclines = FileUtils.readLines(srcFile, (String)null);
				
				List<String> destlines = parameterize(prefix, srclines, (MappingNode)document);
				
				FileUtils.writeLines(destFile, destlines);
			}else {
				throw new UnsupportedNodeTypeException(document.getClass().getCanonicalName());
			}
			
			
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	protected static List<String> parameterize(String prefix, List<String> srclines, MappingNode node) {
		List<String> destlines = new ArrayList<>();
		
		List<NodeTuple> tuples = node.getValue();
		
		int pos = 0;
		
		for (NodeTuple tuple: tuples) {
			Node k = tuple.getKeyNode();
			Node v = tuple.getValueNode();
			
			int startLine = k.getStartMark().getLine();
			int stopLine = v.getEndMark().getLine();
			
			copy(srclines, destlines, pos, startLine);
			
			pos = stopLine+1;
			
			if (k instanceof ScalarNode) {
				ScalarNode sk = (ScalarNode)k;
				
				if (v instanceof ScalarNode) {
					ScalarNode sv = (ScalarNode)v;
					
					destlines.add(String.format("%s:${%s%s%s:%s}", sk.getValue(), prefix, KEY_SEPARATOR, sk.getValue(), sv.getValue()));
				}else {
					destlines.add(String.format("%s:${%s%s%s}", sk.getValue(), prefix, KEY_SEPARATOR, sk.getValue()));
				}				
			}else {
				throw new UnsupportedNodeTypeException(k.getClass().getCanonicalName());
			}
		}
		
		return destlines;
	}
	
	protected static void copy(List<String> srclines, List<String> destlines, int start, int end) {
		if (end>=srclines.size()) {
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
