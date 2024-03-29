package com.networknt.codegen;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by steve on 24/04/17.
 */
public class UtilsTest {


    static  Map<String, String> camelToUnderscore_ = new HashMap<>();
    static  Map<String, String> underscoreToLowerCamel_ = new HashMap<>();
    static Map<String, String> underscoreToCamel_ = new HashMap<>();
    static Map<String, String> slashToCamel_ = new HashMap<>();
    static Map<String, String> dashToCamel_ = new HashMap<>();

    @BeforeAll
    public static void setUp() {

        camelToUnderscore_.put("Product", "product");
        camelToUnderscore_.put("SpecialGuest", "special_guest");
        camelToUnderscore_.put("ApplicationController", "application_controller");
        camelToUnderscore_.put("Area51Controller", "area51_controller");
        camelToUnderscore_.put("InnerClassTest", "inner_class__test");

        underscoreToLowerCamel_.put("product","product");
        underscoreToLowerCamel_.put("special_guest","specialGuest");
        underscoreToLowerCamel_.put("application_controller","applicationController");
        underscoreToLowerCamel_.put("area51_controller","area51Controller");

        underscoreToCamel_.put("product","Product");
        underscoreToCamel_.put("special_guest","SpecialGuest");
        underscoreToCamel_.put("application_controller","ApplicationController");
        underscoreToCamel_.put("area51_controller","Area51Controller");

        slashToCamel_.put("product","Product");
        slashToCamel_.put("/pet/findByStatus","PetFindByStatus");

        dashToCamel_.put("/money-requests/receive/referenceNumber", "MoneyRequestsReceiveReferenceNumber");
    }


    @Test
    public void testUnderscoreToLowerCamel() {
        for (Map.Entry<String, String> entry : underscoreToLowerCamel_.entrySet()) {
            assertEquals(entry.getValue(), Utils.camelize(entry.getKey(), true));
        }

    }

    @Test
    public void testUnderscoreToCamel() {
        for (Map.Entry<String, String> entry : underscoreToCamel_.entrySet()) {
            assertEquals(entry.getValue(), Utils.camelize(entry.getKey()));
        }

    }

    @Test
    public void testSlashToCamel() {
        for (Map.Entry<String, String> entry : slashToCamel_.entrySet()) {
            assertEquals(entry.getValue(), Utils.camelize(entry.getKey()));
        }

    }

    @Test
    public void testCamelToUnderscore() {
        for (Map.Entry<String, String> entry : camelToUnderscore_.entrySet()) {
            assertEquals(entry.getKey(), Utils.camelize(entry.getValue()));
        }
    }

    @Test
    public void testDashToCamel() {
        for (Map.Entry<String, String> entry : dashToCamel_.entrySet()) {
            assertEquals(entry.getValue(), Utils.camelize(entry.getKey()));
        }
    }

}
