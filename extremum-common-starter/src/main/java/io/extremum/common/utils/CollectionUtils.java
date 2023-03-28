package io.extremum.common.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @author vov4a on 23.11.15
 */
public class CollectionUtils {
    public static boolean isNullOrEmpty(final Collection<?> c ) {
        return c == null || c.isEmpty();
    }

    public static boolean isNullOrEmpty(final Map<?,?> m ) {
        return m == null || m.isEmpty();
    }
}
