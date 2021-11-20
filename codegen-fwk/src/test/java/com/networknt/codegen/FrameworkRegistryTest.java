package com.networknt.codegen;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * Created by steve on 26/04/17.
 */
public class FrameworkRegistryTest {

    @Test
    public void testRegistry() {
        FrameworkRegistry registry = FrameworkRegistry.getInstance();
        Set<String> frameworks = registry.getFrameworks();
        Assert.assertTrue(frameworks.size() > 0);
    }
}
