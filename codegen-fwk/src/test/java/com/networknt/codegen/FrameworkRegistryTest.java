package com.networknt.codegen;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Set;

/**
 * Created by steve on 26/04/17.
 */
public class FrameworkRegistryTest {

    @Test
    public void testRegistry() {
        FrameworkRegistry registry = FrameworkRegistry.getInstance();
        Set<String> frameworks = registry.getFrameworks();
        assertTrue(frameworks.size() > 0);
    }
}
