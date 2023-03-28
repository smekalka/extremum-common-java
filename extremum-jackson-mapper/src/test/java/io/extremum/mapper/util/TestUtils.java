package io.extremum.mapper.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TestUtils {
    public static InputStream loadFromResources(String path) {
        return TestUtils.class.getClassLoader().getResourceAsStream(path);
    }

    public static String inputStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is, "utf-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static String loadAsStringFromResource(String path) {
        return inputStreamToString(loadFromResources(path));
    }
}
