package com.networknt.codegen.rest;

import static java.io.File.separator;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.networknt.codegen.OpenApiGeneratorTest;

@Ignore
public class YAMLFileParameterizerTest {
	private static String configName = "/config.json";

    @Test
    public void testNormalizeFilename() {
    	String s1="/a/b/c\\d.txt";
    	String s2="\\a\\b\\c\\d.txt";
    	
    	String expected = String.format("%sa%sb%sc%sd.txt", File.separator, File.separator, File.separator, File.separator);
    	
    	String ns1 = YAMLFileParameterizer.normalizeFilename(s1);
    	String ns2 = YAMLFileParameterizer.normalizeFilename(s2);
    	
    	assertTrue(expected.equals(ns1));
    	assertTrue(expected.equals(ns2));
    }
    
    @Test
    public void testFileExcludeSet() {
    	List<Any> excludes = Arrays.asList(Any.wrap("/a/b/c\\d.txt"), Any.wrap("2.yml"), Any.wrap("\\a\\b\\c\\d.txt"), Any.wrap(""));
    	
    	Set<String> excludeSet = YAMLFileParameterizer.buildFileExcludeSet(".",excludes);
    	
    	assertTrue(2==excludeSet.size());
    }
    
    @Test
    public void testResourceExcludeSet() {
    	List<Any> excludes = Arrays.asList(Any.wrap("audit.yml"), Any.wrap("body.yml"));
    	
    	Set<String> excludeSet = YAMLFileParameterizer.buildResourceExcludeSet("handlerconfig/", excludes);
    	
    	assertTrue(2==excludeSet.size());
    }
    
    @Test
    public void testParameterizing() throws IOException {
    	String destDirName = "/tmp/yml_param_test";
    	File destDir = new File(destDirName);
    	
    	if (destDir.exists() && destDir.isDirectory()) {
    		destDir.delete();
    	}
    	
        YAMLFileParameterizer.copyResources(YAMLFileParameterizer.DEFAULT_RESOURCE_LOCATION, destDirName+separator+YAMLFileParameterizer.DEFAULT_DEST_DIR);
        
        Any anyConfig = JsonIterator.parse(OpenApiGeneratorTest.class.getResourceAsStream(configName), 1024).readAny();
        
        Map<String, Any> genConfig = anyConfig.get(YAMLFileParameterizer.GENERATE_ENV_VARS).asMap();
        
        if (anyConfig.keys().contains(YAMLFileParameterizer.GENERATE_ENV_VARS)) {
    		YAMLFileParameterizer.rewriteAll(destDirName+separator+YAMLFileParameterizer.DEFAULT_DEST_DIR, genConfig);
        }
        
    	FilenameFilter filter = (dir, name)->name.toLowerCase().endsWith(".yml");
    	File outputDestDir = new File(destDirName+separator+YAMLFileParameterizer.DEFAULT_DEST_DIR);
    	int destCount = outputDestDir.list(filter).length;
    	
    	assertTrue(destCount>0);
    	
    	//genConfig.remove(YAMLFileParameterizer.KEY_IN_PLACE);
    	
    	//YAMLFileParameterizer.rewriteFile(new File("src/main/resources/handlerconfig/config.yml"), new File("/tmp/config.yml"), genConfig);
    }
}
