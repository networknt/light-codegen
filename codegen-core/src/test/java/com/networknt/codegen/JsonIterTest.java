package com.networknt.codegen;

import com.jsoniter.any.Any;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by steve on 09/07/17.
 */
public class JsonIterTest {
    private static Map<String, Object> mapOf(Object... args) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < args.length; i += 2) {
            map.put((String) args[i], args[i + 1]);
        }
        return map;
    }

    @Test
    public void testMapSize() {
        Any any = Any.wrap(mapOf("hello", 1, "world", 2));
        assertEquals(2, any.size());
    }

    @Test
    public void testListSize() {
        Any any = Any.wrap(Arrays.asList(1, 2, 3));
        assertEquals(3, any.size());
    }

    @Test
    public void testArraySize() {
        Any any = Any.wrap(new int[]{1, 2, 3});
        assertEquals(3, any.size());
    }

    @Test
    public void testGetNestedMapLevel1() {
        Any any = Any.wrap(mapOf("a", mapOf("b", "c"), "d", mapOf("e", "f")));
        assertEquals("c", any.get("a", "b").toString());
        // if using * it must be single quoted.
        assertEquals("{\"a\":\"c\"}", any.get('*', "b").toString());
    }

    @Test
    public void testGetNestedMapLevel2() {
        Any any = Any.wrap(mapOf("a", mapOf("b", mapOf("c", "d")), "e", mapOf("f", mapOf("g", "h"))));
        System.out.println("any = " + any.toString());
        assertEquals("d", any.get("a", "b", "c").toString());
        String s = any.get('*', "b").toString();
        assertEquals("{\"a\":{\"c\":\"d\"}}", s);
        s = any.get('*', "b", "c").toString();
        assertEquals("{\"a\":\"d\"}", s);
        s = any.get('*', '*', "f").toString();
        System.out.println("s = " + s);
        assertEquals("{\"a\":{},\"e\":{}}", s);

    }

    @Test
    public void testGetNestedListLevel1() {
        Any any = Any.wrap(Arrays.asList(Arrays.asList("hello"), Arrays.asList("world")));
        System.out.println("any = " + any.toString());

        assertEquals("hello", any.get(0, 0).toString());
        assertEquals("[\"hello\",\"world\"]", any.get('*', 0).toString());
    }

    @Test
    public void testGetNestedListLevel2() {
        Any any = Any.wrap(Arrays.asList(Arrays.asList(Arrays.asList("hello")), Arrays.asList(Arrays.asList("world"))));
        System.out.println("any = " + any.toString());
        assertEquals("hello", any.get(0, 0, 0).toString());
        assertEquals("[\"hello\",\"world\"]", any.get('*', 0, 0).toString());
    }

    @Test
    public void testGetListOfMaps() {
        Any any = Any.wrap(Arrays.asList(Arrays.asList(Arrays.asList(mapOf("hello", "world")))));
        System.out.println("any = " + any.toString());
        assertEquals("world", any.get(0, 0, 0, "hello").toString());
        String s = any.get(0, 0, 0).toString();
        System.out.println("s = " + s);
        assertEquals("{\"hello\":\"world\"}", s);
    }

    @Test
    public void testGetMapOfLists() {
        Any any = Any.wrap(mapOf("a", Arrays.asList(Arrays.asList(Arrays.asList(mapOf("hello", "world"))))));
        System.out.println("any = " + any.toString());
        assertEquals("world", any.get("a", 0, 0, 0, "hello").toString());
        String s = any.get("a", 0, 0, 0).toString();
        System.out.println("s = " + s);
        assertEquals("{\"hello\":\"world\"}", s);
    }

}
