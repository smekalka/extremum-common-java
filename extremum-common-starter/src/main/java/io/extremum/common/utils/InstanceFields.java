package io.extremum.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author rpuch
 */
public class InstanceFields {
    private final Class<?> targetClass;

    public InstanceFields(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public Stream<Field> stream() {
        List<Field> fields = findInstanceFields();
        return fields.stream();
    }

    private List<Field> findInstanceFields() {
        List<Field> fields = new ArrayList<>();
        collectInstanceFields(targetClass, fields);
        return fields;
    }

    private void collectInstanceFields(Class<?> aClass, List<Field> fields) {
        Arrays.stream(aClass.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .filter(field -> !field.isSynthetic())
                .forEach(fields::add);

        Class<?> superclass = aClass.getSuperclass();
        if (superclass != null) {
            collectInstanceFields(superclass, fields);
        }
    }
}
