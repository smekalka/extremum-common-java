package io.extremum.common.utils;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author rpuch
 */
public class StreamUtils {
    public static <T> Stream<? extends T> fromIterable(Iterable<? extends T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
