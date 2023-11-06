package com.networknt.codegen.rest;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.resolver.Resolver;

import com.jsoniter.any.Any;

import static java.nio.charset.StandardCharsets.UTF_8;

public class YAMLFileParameterizer {
	private static final Logger logger = LoggerFactory.getLogger(YAMLFileParameterizer.class);
	private static final String KEY_SEPARATOR = ".";
	private static final String YML_EXT = ".yml";
	private static final String BAK_EXT = ".bak";
	private static final String SLASH = "/";
	
	public static final String GENERATE_ENV_VARS="generateEnvVars";
	public static final String CLASS_PATH_PREFIX="classpath:";
	public static final String DEFAULT_RESOURCE_LOCATION = CLASS_PATH_PREFIX+"handlerconfig/";
	public static final String DEFAULT_DEST_DIR = "src/main/resources/config";
	
	protected static final String KEY_GENERATE="generate";
	protected static final String KEY_SKIP_ARRAY="skipArray";
	protected static final String KEY_SKIP_MAP="skipMap";
	protected static final String KEY_IN_PLACE="inPlace";
	protected static final String KEY_EXCLUDE="exclude";
	
	/**
	 * In place rewriting.
	 * 
	 * @param dir - file dir
	 * @param generateEnvVars - config
	 */
	public static void rewriteAll(String dir, Map<String, Any> generateEnvVars) {
		if (logger.isDebugEnabled()) {
			logger.debug("rewriting files in {}", dir);
		}
		
		generateEnvVars.put(KEY_IN_PLACE, Any.wrap(true));
		rewriteFiles(new File(dir), new File(dir), generateEnvVars);
	}
	
	public static void rewriteAll(String srcLocation, String destDir, Map<String, Any> generateEnvVars) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("rewriting files in {}", srcLocation);
		}
		
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
			if (!destDir.mkdirs()) {
				logger.error("Failed to create dir {}", destDir);
				return;
			}
		}
		
		String dirPath = getAbsolutePath(sourceDir);
		
		if (null==dirPath) {
			logger.error("dir does not exist.", sourceDir.getAbsolutePath());
			return;		
		}
		
		Set<String> excludeSet = buildFileExcludeSet(dirPath, generateEnvVars);

		File[] files = sourceDir.listFiles((dir, name)->name.toLowerCase().endsWith(YML_EXT) && !excludeFile(dir, name, excludeSet));

		for (File file: files) {
			rewriteFile(file, new File(destDir, file.getName()), generateEnvVars);
		}
	}
	
	public static void rewriteResources(String resourceLocation, String destDir, Map<String, Any> generateEnvVars) {
		if (StringUtils.isBlank(resourceLocation)) {
			return;
		}
		
		String location = StringUtils.trimToEmpty(resourceLocation);
		if (!location.endsWith(SLASH)) {
			location = location+SLASH;
		}
		
		List<String> filenames = listClasspathDir(resourceLocation);
		
		if (logger.isDebugEnabled()) {
			logger.debug("files in {}: {}", resourceLocation, String.join(",", filenames));
		}
		
		Set<String> excludeSet = buildResourceExcludeSet(resourceLocation, generateEnvVars);
		
		List<String> ymlFileNames = filenames.stream()
											.filter(name->name.toLowerCase().endsWith(YML_EXT) && !excludeResource(resourceLocation, name, excludeSet))
											.collect(Collectors.toList());
		
		if (logger.isDebugEnabled()) {
			logger.debug("ymlFileNames in {}: {}", resourceLocation, String.join(",", ymlFileNames));
		}
		
		File dest = new File(destDir);
		
		if (!dest.isDirectory() || !dest.exists()) {
			if (!dest.mkdirs()) {
				logger.error("Failed to create dir {}", destDir);
				return;
			}
		}
		
		for (String filename: ymlFileNames) {
			rewriteResource(stripExtension(filename), resourceLocation+filename, destDir+File.separator+filename, generateEnvVars);
		}
	}
	
	public static void copyResources(String resourceLocation, String destDir) {
		String location= resolveLocation(resourceLocation);
		
		List<String> filenames = listClasspathDir(location);
		
		if (logger.isDebugEnabled()) {
			logger.debug("files in {}: {}", resourceLocation, String.join(",", filenames));
		}
		
		List<String> ymlFileNames = filenames.stream().filter(name->name.toLowerCase().endsWith(YML_EXT)).collect(Collectors.toList());
		
		if (logger.isDebugEnabled()) {
			logger.debug("ymlFileNames in {}: {}", resourceLocation, String.join(",", ymlFileNames));
		}
		
		File dest = new File(destDir);
		
		if (!dest.isDirectory() || !dest.exists()) {
			if (!dest.mkdirs()) {
				logger.error("Failed to create dir {}", destDir);
				return;
			}
		}
		
		for (String filename: ymlFileNames) {
			copyResource(location+filename, destDir+File.separator+filename);
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
			if (!getValue(generateEnvVars, KEY_IN_PLACE)) {// in-place copy is meaningless
				copyFile(srcFile, destFile);
			}
		}else {
			try {
				String filename = stripExtension(srcFile.getName());
				
				String srcFilename = getAbsolutePath(srcFile);
				
				if (null==srcFilename) {
					logger.error("file {} does not exist.", srcFile.getAbsolutePath());
					return;
				}
				
				File inputFile = srcFile;
				boolean inPlace = getValue(generateEnvVars, KEY_IN_PLACE);
				
				if (inPlace) {// rename the original file as a back up
					String bakFilename =  srcFilename + BAK_EXT;
					File bakFile = new File(bakFilename);
					
					if (!srcFile.renameTo(bakFile)) {
						logger.error("Failed to reanme file {} to {}.", srcFilename, bakFilename);
						return;
					}	
					
					inputFile = bakFile;
				}
				
				List<String> lines = readFile(inputFile);
				Node document = loadFile(inputFile);
				
				if (null!=document && null!=lines) {
					parameterize(filename, document, lines, destFile, generateEnvVars);
				}				
				
				if (inPlace) {// delete the original file as it's not needed any more.
					inputFile.delete();
				}
			}catch(Exception e) {
				logger.error(e.getMessage(), e);
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
		try (InputStream in = getResourceAsStream(resourceLocation);
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
	
	protected static URL getResourceURL(String resource) {
		return YAMLFileParameterizer.class.getClassLoader().getResource(resource);
	}
	
	protected static InputStream getResourceAsStream(String resource) {
		return YAMLFileParameterizer.class.getClassLoader().getResourceAsStream(resource);
	}
	
	protected static List<String> listClasspathDir(String dir) {
		List<String> result = new ArrayList<>();

		try {
			URL dirURL = getResourceURL(dir);
			
			if (null==dirURL) {
				logger.error("cannot locate file {} in classpath.", dir);
				return result;
			}
			
			if (dirURL.getProtocol().equals("file")) {
				result.addAll(Arrays.asList(new File(dirURL.toURI()).list()));
				return result;
			}else if (dirURL.getProtocol().equals("jar")) { /* A JAR path */
				// strip out only the JAR file
				String path = dirURL.getPath();
				String jarPath = path.substring("file:".length(), path.indexOf("!"));
				
				JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
				
				Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
				int length = dir.length();
				
				while (entries.hasMoreElements()) {
					String name = entries.nextElement().getName();
					
					int pathIndex = name.lastIndexOf(dir);
					if (pathIndex >= 0) {
						result.add(name.substring(pathIndex+length));
					}
				}
				jar.close();
				return result;
			}
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		throw new UnsupportedOperationException("Cannot list files in " + dir);
	}
	
	protected static Node loadResource(String resourceLocation) {
		try (InputStream in = getResourceAsStream(resourceLocation);
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(in, UTF_8))) {
			Composer composer = new Composer(new ParserImpl(new StreamReader(inputReader), new LoaderOptions()), new Resolver(), new LoaderOptions());
			return composer.getSingleNode();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
	
	protected static List<String> readResource(String resourceLocation) {
		List<String> lines = new ArrayList<>();
		
		try (InputStream in = getResourceAsStream(resourceLocation);
				InputStreamReader reader = new InputStreamReader(in, UTF_8)) {
			lines.addAll(IOUtils.readLines(reader));
			lines.add(StringUtils.EMPTY); // because trailing empty lines are ignored.
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return lines;
	}
	
	protected static Node loadFile(File file) {
		try (Reader inputReader = Files.newBufferedReader(file.toPath(), UTF_8)) {
			Composer composer = new Composer(new ParserImpl(new StreamReader(inputReader), new LoaderOptions()), new Resolver(), new LoaderOptions());
			return composer.getSingleNode();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
	
	protected static List<String> readFile(File file) {
		List<String> lines = new ArrayList<>();
		
		try {
			lines.addAll(FileUtils.readLines(file, (String) null));
			lines.add(StringUtils.EMPTY); // because trailing empty lines are ignored.
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return lines;
	}
	
	protected static void parameterize(String filename, Node document, List<String> srclines, File destFile, Map<String, Any> generateEnvVars) {
		try (Writer writer = Files.newBufferedWriter(destFile.toPath(), UTF_8)) {
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
		
		int tupleNum = tuples.size();
		
		for (int i=0; i<tupleNum; ++i) {
			NodeTuple tuple = tuples.get(i);
			
			NodeTuple nextTuple = null;
			
			if (i+1<tupleNum) {
				nextTuple =  tuples.get(i+1);
			}
			
			
			Node k = tuple.getKeyNode();
			Node v = tuple.getValueNode();
			
			int startLine = k.getStartMark().getLine();
			int stopLine = v.getEndMark().getLine();
			
			// stop line can be:
			//  1. the start line of the next node.
			// 	2. last line of file (empty line or non-empty line).
			if (null!=nextTuple && nextTuple.getKeyNode().getStartMark().getLine()==stopLine) {
				stopLine -= 1;
			}
			
			if (k instanceof ScalarNode) {
				ScalarNode sk = (ScalarNode)k;
				
				if (v instanceof ScalarNode) {
					ScalarNode sv = (ScalarNode)v;
					
					copy(srclines, destlines, pos, startLine-1);
					destlines.add(String.format("%s: ${%s%s%s:%s}", sk.getValue(), filename, KEY_SEPARATOR, sk.getValue(), sv.getValue()));
				}else {
					if (((v instanceof SequenceNode) && !skipArray) 
							|| ((v instanceof MappingNode) && !skipMap)) {
						copy(srclines, destlines, pos, startLine-1);
						destlines.add(String.format("%s: ${%s%s%s}", sk.getValue(), filename, KEY_SEPARATOR, sk.getValue()));
					}else {
						copy(srclines, destlines, pos, stopLine);
					}
				}		
				
				pos = stopLine+1;
			}else {
				throw new UnsupportedNodeTypeException(k.getClass().getCanonicalName());
			}
		}
		
		return destlines;
	}
	
	protected static void copy(List<String> srclines, List<String> destlines, int start, int end) {
		if (end>=srclines.size()) {
			logger.error("attemp to read line {}. total length {}", end, srclines.size());
			throw new IndexOutOfBoundsException();
		}
		
		for (int i=start; i<=end; ++i) {
			destlines.add(srclines.get(i));
		}
	}
	
    protected static String stripExtension (String str) {
        if (StringUtils.isBlank(str)) return str;

        int pos = str.lastIndexOf(".");

        if (pos == -1) return str;

        return str.substring(0, pos);
    }
    
    protected static Set<String> buildFileExcludeSet(String sourceDir, Map<String, Any> generateEnvVars) {
    	if (generateEnvVars.containsKey(KEY_EXCLUDE)) {
    		return buildFileExcludeSet(sourceDir, generateEnvVars.get(KEY_EXCLUDE).asList());
    	}
    	
    	return Collections.emptySet();
    }
    
	protected static Set<String> buildFileExcludeSet(String sourceDir, Collection<Any> excludes) {
		return excludes.stream()
				.map(Any::toString)
				.filter(StringUtils::isNotBlank)
				.map(s->sourceDir+normalizeFilename(s))
				.collect(Collectors.toSet());
	}
	
	protected static Set<String> buildResourceExcludeSet(String resourceLocation, Map<String, Any> generateEnvVars) {
		if (generateEnvVars.containsKey(KEY_EXCLUDE)) {
			return buildResourceExcludeSet(resourceLocation, generateEnvVars.get(KEY_EXCLUDE).asList());
		}

		return Collections.emptySet();
	}
	
	protected static Set<String> buildResourceExcludeSet(String resourceLocation, Collection<Any> excludes) {
		return excludes.stream()
				.map(Any::toString)
				.filter(StringUtils::isNotBlank)
				.map(s->toNonNullString(getResourceURL(resourceLocation+s)))
				.collect(Collectors.toSet());
	}	
    
    protected static String normalizeFilename(String filename) {
    	return StringUtils.trimToEmpty(filename).replaceAll("\\\\|/", "\\"+File.separator);
    }
    
    public static String toNonNullString(URL url) {
    	return null==url?StringUtils.EMPTY:url.toString();
    }
    
    public static String getAbsolutePath(File f) {
    	try {
    		String path = f.getCanonicalFile().getAbsolutePath();
    		
    		if (f.isDirectory() && !path.endsWith(File.separator)) {
    			path = path+File.separator;
    		}
    		
    		return path;
    	}catch (Exception e) {
    		logger.error(e.getMessage(), e);
    	}
    	
    	return null;
    }
    
	protected static boolean excludeFile(File f, Set<String> excludeSet) {
		if (excludeSet.isEmpty()) {
			return false;
		}
		
		String filename = getAbsolutePath(f);

		return null == filename ? true : excludeSet.contains(filename);
	}
    
    protected static boolean excludeFile(File dir, String name, Set<String> excludeSet) {
    	return excludeFile(new File(dir, name), excludeSet);
    }
    
    protected static boolean excludeResource(String resourceLocation, String name, Set<String> excludeSet) {
		if (excludeSet.isEmpty()) {
			return false;
		}
		
    	String url = toNonNullString(getResourceURL(resourceLocation+name));
    
    	return excludeSet.contains(url);
    }
	
	@SuppressWarnings("serial")
	public static class UnsupportedNodeTypeException extends RuntimeException{
		public UnsupportedNodeTypeException(String nodeType) {
			super(StringUtils.isBlank(nodeType)?"Unsupported node type.":String.format("Nodetype %s is not supported", nodeType));
		}
	}
}
