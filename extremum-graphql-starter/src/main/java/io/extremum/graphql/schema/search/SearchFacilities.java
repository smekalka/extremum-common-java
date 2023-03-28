package io.extremum.graphql.schema.search;

import io.extremum.sharedmodels.basic.ModelSettings;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchFacilities {

    static List<String> getDeclaringClassSettings(Class<?> declaringClass, Class<?> topLevelClass, ModelSettings settings) {
        List<String> pointers = settings
                .getProperties()
                .getVisible()
                .stream()
                .filter(property -> property.contains("."))
                .collect(Collectors.toList());


        Map<Class<?>, List<String>> settingsMap = new HashMap<>();
        pointers.forEach(
                s -> {
                    try {
                        Field field = topLevelClass.getDeclaredField(StringUtils.substringBefore(s, "."));
                        settingsMap.put(
                                field.getType(),
                                pointers
                                        .stream()
                                        .map(pointer -> StringUtils.substringAfter(pointer, "."))
                                        .filter(pointer -> !pointer.contains("."))
                                        .collect(Collectors.toList()));
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        return settingsMap.get(declaringClass);
    }

    public static Class<?> getTopLevelClass(Class<?> clazz) {
        Class<?> declaringClass = clazz.getDeclaringClass();
        if (declaringClass == null) {
            return clazz;
        }
        if (declaringClass.getDeclaringClass() == null) {
            return declaringClass;
        } else {
            return getTopLevelClass(declaringClass);
        }
    }
}
