package io.extremum.dynamic.services;

import java.util.Collection;
import java.util.Map;

public interface DateTypesNormalizer {
    Map<String, Object> normalize(Map<String, Object> doc, Collection<String> datePaths);
}
