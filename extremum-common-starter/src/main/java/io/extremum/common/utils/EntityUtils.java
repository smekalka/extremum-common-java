package io.extremum.common.utils;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * @author rpuch
 */
public final class EntityUtils {
    private static final List<String> PROXY_MARKERS = ImmutableList.of("$HibernateProxy$");

    public static boolean isProxyClass(Class<?> classToCheck) {
        final String name = classToCheck.getName();
        return PROXY_MARKERS.stream().anyMatch(name::contains);
    }

    private EntityUtils() {
    }
}
