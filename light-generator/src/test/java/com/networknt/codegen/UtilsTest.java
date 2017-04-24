package com.networknt.codegen;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by steve on 24/04/17.
 */
public class UtilsTest {

    private static final Map<String, String> camelToUnderscore_ = ImmutableMap.<String, String>builder()
            .put("Product", "product")
            .put("SpecialGuest", "special_guest")
            .put("ApplicationController", "application_controller")
            .put("Area51Controller", "area51_controller")
            .put("InnerClassTest", "inner_class__test")
            .build();

    private static final Map<String, String> underscoreToLowerCamel_ = ImmutableMap.<String, String>builder()
            .put("product","product")
            .put("special_guest","specialGuest")
            .put("application_controller","applicationController")
            .put("area51_controller","area51Controller")
            .build();

    private static final Map<String, String> underscoreToCamel_ = ImmutableMap.<String, String>builder()
            .put("product","Product")
            .put("special_guest","SpecialGuest")
            .put("application_controller","ApplicationController")
            .put("area51_controller","Area51Controller")
            .build();

    private static final Map<String, String> slashToCamel_ = ImmutableMap.<String, String>builder()
            .put("product","Product")
            .put("/pet/findByStatus","PetFindByStatus")
            .build();

    @Test
    public void testUnderscoreToLowerCamel() {
        for (Map.Entry<String, String> entry : underscoreToLowerCamel_.entrySet()) {
            Assert.assertEquals(entry.getValue(), Utils.camelize(entry.getKey(), true));
        }

    }

    @Test
    public void testUnderscoreToCamel() {
        for (Map.Entry<String, String> entry : underscoreToCamel_.entrySet()) {
            Assert.assertEquals(entry.getValue(), Utils.camelize(entry.getKey()));
        }

    }

    @Test
    public void testSlashToCamel() {
        for (Map.Entry<String, String> entry : slashToCamel_.entrySet()) {
            Assert.assertEquals(entry.getValue(), Utils.camelize(entry.getKey()));
        }

    }

    @Test
    public void testCamelToUnderscore() {
        for (Map.Entry<String, String> entry : camelToUnderscore_.entrySet()) {
            Assert.assertEquals(entry.getKey(), Utils.camelize(entry.getValue()));
        }
    }
}
