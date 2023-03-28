package io.extremum.common.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author rpuch
 */
public class InstanceMethods {
    private final Class<?> targetClass;

    public InstanceMethods(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public Stream<Method> stream() {
        List<Method> methods = findInstanceMethods();
        return methods.stream();
    }

    private List<Method> findInstanceMethods() {
        List<Method> methods = new ArrayList<>();
        collectInstanceMethods(targetClass, methods);
        return methods;
    }

    private void collectInstanceMethods(Class<?> aClass, List<Method> methods) {
        Arrays.stream(aClass.getMethods())
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> !method.isSynthetic())
                .forEach(methods::add);
    }
}
