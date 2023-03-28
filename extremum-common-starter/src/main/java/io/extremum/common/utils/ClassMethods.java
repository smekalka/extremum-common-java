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
public class ClassMethods {
    private final Class<?> targetClass;

    public ClassMethods(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public Stream<Method> stream() {
        List<Method> methods = findClassMethods();
        return methods.stream();
    }

    private List<Method> findClassMethods() {
        List<Method> methods = new ArrayList<>();
        collectClassMethods(targetClass, methods);
        return methods;
    }

    private void collectClassMethods(Class<?> aClass, List<Method> methods) {
        Arrays.stream(aClass.getMethods())
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> !method.isSynthetic())
                .forEach(methods::add);
    }
}
